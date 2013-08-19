/**********************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Bernd Hufmann - Updated for support of streamed traces
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.handlers;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.TraceSessionState;
import org.eclipse.linuxtools.internal.lttng2.ui.Activator;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.ControlView;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.IImportDialog;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.ImportFileInfo;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.TraceControlDialogFactory;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.messages.Messages;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceSessionComponent;
import org.eclipse.linuxtools.internal.tmf.ui.project.model.TmfImportHelper;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceType;
import org.eclipse.linuxtools.tmf.ui.project.model.TraceTypeHelper;
import org.eclipse.linuxtools.tmf.ui.project.wizards.importtrace.BatchImportTraceWizard;
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
    /** Trace Type ID for LTTng Kernel traces */
    private static final String LTTNG_KERNEL_TRACE_TYPE = "org.eclipse.linuxtools.lttng2.kernel.tracetype"; //$NON-NLS-1$
    /** Trace Type ID for Generic CTF traces */
    private static final String GENERIC_CTF_TRACE_TYPE = "org.eclipse.linuxtools.tmf.ui.type.ctf"; //$NON-NLS-1$
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
            TmfImportHelper.forceFolderRefresh(project.getFolder(TmfTraceFolder.TRACE_FOLDER_NAME));

            if (param.getSession().isStreamedTrace()) {
                // Streamed trace
                TmfProjectElement projectElement = TmfProjectRegistry.getProject(project);
                TmfTraceFolder traceFolder = projectElement.getTracesFolder();

                BatchImportTraceWizard wizard = new BatchImportTraceWizard();
                wizard.init(PlatformUI.getWorkbench(), new StructuredSelection(traceFolder));
                WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
                dialog.open();
                traceFolder.refresh();
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
                            ImportFileInfo remoteFile = iterator.next();

                            downloadTrace(remoteFile, selectedProject);

                            // Set trace type
                            IFolder traceFolder = selectedProject.getFolder(TmfTraceFolder.TRACE_FOLDER_NAME);
                            TmfImportHelper.forceFolderRefresh(traceFolder);
                            IFile file = traceFolder.getFile(remoteFile.getLocalTraceName());

                            TraceTypeHelper helper = null;

                            if (remoteFile.isKernel()) {
                                helper = TmfTraceType.getInstance().getTraceType(LTTNG_KERNEL_TRACE_TYPE);
                            } else {
                                helper = TmfTraceType.getInstance().getTraceType(GENERIC_CTF_TRACE_TYPE);
                            }

                            if (helper != null) {
                                status.add(TmfTraceType.setTraceType(file.getFullPath(), helper));
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
     * @throws ExecutionException
     */
    private static void downloadTrace(ImportFileInfo trace, IProject project)
            throws ExecutionException {
        try {
            IRemoteFileSubSystem fsss = trace.getImportFile().getParentRemoteFileSubSystem();

            IFolder traceFolder = project.getFolder(TmfTraceFolder.TRACE_FOLDER_NAME);
            if (!traceFolder.exists()) {
                throw new ExecutionException(Messages.TraceControl_ImportDialogInvalidTracingProject + " (" + TmfTraceFolder.TRACE_FOLDER_NAME + ")");  //$NON-NLS-1$//$NON-NLS-2$
            }

            String traceName = trace.getLocalTraceName();
            IFolder folder = traceFolder.getFolder(traceName);
            if (folder.exists()) {
                if(!trace.isOverwrite()) {
                    throw new ExecutionException(Messages.TraceControl_ImportDialogTraceAlreadyExistError + ": " + traceName); //$NON-NLS-1$
                }
            } else {
                folder.create(true, true, null);
            }

            IRemoteFile[] sources = fsss.list(trace.getImportFile(), IFileService.FILE_TYPE_FILES, new NullProgressMonitor());

            String[] destinations = new String[sources.length];
            String[] encodings = new String[sources.length];
            for (int i = 0; i < sources.length; i++) {
                destinations[i] = folder.getLocation().addTrailingSeparator().append(sources[i].getName()).toString();
                encodings[i] = null;
            }

            fsss.downloadMultiple(sources, destinations, encodings, new NullProgressMonitor());

        } catch (SystemMessageException e) {
            throw new ExecutionException(e.toString(), e);
        } catch (CoreException e) {
            throw new ExecutionException(e.toString(), e);
        }
    }
}
