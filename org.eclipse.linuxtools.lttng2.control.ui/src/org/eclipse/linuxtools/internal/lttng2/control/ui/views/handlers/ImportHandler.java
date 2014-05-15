/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Bernd Hufmann - Updated for support of streamed traces
 *   Patrick Tasse - Add support for source location
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.control.ui.views.handlers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.TraceSessionState;
import org.eclipse.linuxtools.internal.lttng2.control.ui.Activator;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.ControlView;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.dialogs.IImportDialog;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.dialogs.ImportFileInfo;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.dialogs.TraceControlDialogFactory;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl.TraceSessionComponent;
import org.eclipse.linuxtools.internal.tmf.ui.project.wizards.importtrace.ImportTraceWizard;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.core.project.model.TmfTraceImportException;
import org.eclipse.linuxtools.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceTypeUIUtils;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTracesFolder;
import org.eclipse.linuxtools.tmf.ui.project.model.TraceUtils;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * <p>
 * Command handler implementation to import traces from a (remote) session to a tracing project.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class ImportHandler extends BaseControlViewHandler {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /** Name of default project to import traces to */
    public static final String DEFAULT_REMOTE_PROJECT_NAME = "Remote"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The command parameter
     */
    protected CommandParameter fParam;

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

        if (window == null) {
            return false;
        }

        fLock.lock();
        try {
            final CommandParameter param = fParam.clone();

            // create default project
            IProject project = TmfProjectRegistry.createProject(DEFAULT_REMOTE_PROJECT_NAME, null, null);

            if (param.getSession().isStreamedTrace()) {
                // Streamed trace
                TmfProjectElement projectElement = TmfProjectRegistry.getProject(project, true);
                TmfTraceFolder traceFolder = projectElement.getTracesFolder();

                ImportTraceWizard wizard = new ImportTraceWizard();
                wizard.init(PlatformUI.getWorkbench(), new StructuredSelection(traceFolder));
                WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
                dialog.open();
                return null;
            }

            // Remote trace
            final IImportDialog dialog = TraceControlDialogFactory.getInstance().getImportDialog();
            dialog.setSession(param.getSession());
            dialog.setDefaultProject(DEFAULT_REMOTE_PROJECT_NAME);

            if (dialog.open() != Window.OK) {
                return null;
            }

            Job job = new Job(Messages.TraceControl_ImportJob) {
                @Override
                protected IStatus run(IProgressMonitor monitor) {

                    MultiStatus status = new MultiStatus(Activator.PLUGIN_ID, IStatus.OK, Messages.TraceControl_ImportFailure, null);
                    List<ImportFileInfo> traces = dialog.getTracePathes();
                    IProject selectedProject = dialog.getProject();
                    for (Iterator<ImportFileInfo> iterator = traces.iterator(); iterator.hasNext();) {
                        try {

                            if (monitor.isCanceled()) {
                                status.add(Status.CANCEL_STATUS);
                                break;
                            }

                            ImportFileInfo remoteFile = iterator.next();

                            downloadTrace(remoteFile, selectedProject, monitor);

                            // Set trace type
                            IFolder traceFolder = remoteFile.getDestinationFolder();

                            IResource file = traceFolder.findMember(remoteFile.getLocalTraceName());

                            if (file != null) {
                                TraceTypeHelper helper = null;

                                try {
                                    helper = TmfTraceTypeUIUtils.selectTraceType(file.getLocationURI().getPath(), null, null);
                                } catch (TmfTraceImportException e) {
                                    // the trace did not match any trace type
                                }

                                if (helper != null) {
                                    status.add(TmfTraceTypeUIUtils.setTraceType(file, helper));
                                }

                                try {
                                    final String scheme = "sftp"; //$NON-NLS-1$
                                    String host = remoteFile.getImportFile().getHost().getName();
                                    int port = remoteFile.getImportFile().getParentRemoteFileSubSystem().getConnectorService().getPort();
                                    String path = remoteFile.getImportFile().getAbsolutePath();
                                    if (file instanceof IFolder) {
                                        path += IPath.SEPARATOR;
                                    }
                                    URI uri = new URI(scheme, null, host, port, path, null, null);
                                    String sourceLocation = URIUtil.toUnencodedString(uri);
                                    file.setPersistentProperty(TmfCommonConstants.SOURCE_LOCATION, sourceLocation);
                                } catch (URISyntaxException e) {
                                }
                            }
                        } catch (ExecutionException e) {
                            status.add(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.TraceControl_ImportFailure, e));
                        } catch (CoreException e) {
                            status.add(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.TraceControl_ImportFailure, e));
                        }
                    }
                    return status;
                }
            };
            job.setUser(true);
            job.schedule();
        } finally {
            fLock.unlock();
        }
        return null;
    }

    @Override
    public boolean isEnabled() {
        // Get workbench page for the Control View
        IWorkbenchPage page = getWorkbenchPage();
        if (page == null) {
            return false;
        }

        // Check if one or more session are selected
        ISelection selection = page.getSelection(ControlView.ID);
        TraceSessionComponent session = null;
        if (selection instanceof StructuredSelection) {
            StructuredSelection structered = ((StructuredSelection) selection);
            for (Iterator<?> iterator = structered.iterator(); iterator.hasNext();) {
                Object element = iterator.next();
                if (element instanceof TraceSessionComponent) {
                    // Add only TraceSessionComponents that are inactive and not destroyed
                    TraceSessionComponent tmpSession = (TraceSessionComponent) element;
                    if (((tmpSession.isSnapshotSession()) || (tmpSession.getSessionState() == TraceSessionState.INACTIVE)) && (!tmpSession.isDestroyed())) {
                        session = tmpSession;
                    }
                }
            }
        }
        boolean isEnabled = session != null;

        fLock.lock();
        try {
            fParam = null;
            if (isEnabled) {
                fParam = new CommandParameter(session);
            }
        } finally {
            fLock.unlock();
        }
        return isEnabled;
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------

    /**
     * Downloads a trace from the remote host to the given project.
     *
     * @param trace
     *            - trace information of trace to import
     * @param project
     *            - project to import to
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     */
    private static void downloadTrace(ImportFileInfo trace, IProject project, IProgressMonitor monitor)
            throws ExecutionException {
        try {
            IRemoteFileSubSystem fsss = trace.getImportFile().getParentRemoteFileSubSystem();

            IFolder traceFolder = project.getFolder(TmfTracesFolder.TRACES_FOLDER_NAME);
            if (!traceFolder.exists()) {
                throw new ExecutionException(Messages.TraceControl_ImportDialogInvalidTracingProject + " (" + TmfTracesFolder.TRACES_FOLDER_NAME + ")");  //$NON-NLS-1$//$NON-NLS-2$
            }

            IFolder destinationFolder = trace.getDestinationFolder();
            TraceUtils.createFolder(destinationFolder, monitor);

            String traceName = trace.getLocalTraceName();
            IFolder folder = destinationFolder.getFolder(traceName);
            if (folder.exists()) {
                if(!trace.isOverwrite()) {
                    throw new ExecutionException(Messages.TraceControl_ImportDialogTraceAlreadyExistError + ": " + traceName); //$NON-NLS-1$
                }
            } else {
                folder.create(true, true, null);
            }

            IRemoteFile[] sources = fsss.list(trace.getImportFile(), IFileService.FILE_TYPE_FILES, new NullProgressMonitor());
            SubMonitor subMonitor = SubMonitor.convert(monitor, sources.length);
            subMonitor.beginTask(Messages.TraceControl_DownloadTask, sources.length);

            for (int i = 0; i < sources.length; i++) {
                if (subMonitor.isCanceled()) {
                    monitor.setCanceled(true);
                    return;
                }
                String destination = folder.getLocation().addTrailingSeparator().append(sources[i].getName()).toString();
                subMonitor.setTaskName(Messages.TraceControl_DownloadTask + ' '  + traceName + '/' +sources[i].getName());
                fsss.download(sources[i], destination, null, subMonitor.newChild(1));
            }
        } catch (SystemMessageException e) {
            throw new ExecutionException(e.toString(), e);
        } catch (CoreException e) {
            throw new ExecutionException(e.toString(), e);
        }
    }
}
