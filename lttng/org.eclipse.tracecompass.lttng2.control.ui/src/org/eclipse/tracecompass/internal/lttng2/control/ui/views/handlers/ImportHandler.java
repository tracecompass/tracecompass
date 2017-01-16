/**********************************************************************
 * Copyright (c) 2012, 2015 Ericsson
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
 *   Markus Schorn - Bug 448058: Use org.eclipse.remote in favor of RSE
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.handlers;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionHostService;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceSessionState;
import org.eclipse.tracecompass.internal.lttng2.control.ui.Activator;
import org.eclipse.tracecompass.internal.lttng2.control.ui.relayd.LttngRelaydConnectionInfo;
import org.eclipse.tracecompass.internal.lttng2.control.ui.relayd.LttngRelaydConnectionManager;
import org.eclipse.tracecompass.internal.lttng2.control.ui.relayd.LttngRelaydConsumer;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.ControlView;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceSessionComponent;
import org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch.RemoteFetchLogWizard;
import org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch.RemoteFetchLogWizardRemotePage;
import org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch.model.RemoteImportConnectionNodeElement;
import org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch.model.RemoteImportProfileElement;
import org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch.model.RemoteImportTraceGroupElement;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.ImportTraceWizard;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageElement;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageFilesElement;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageTraceElement;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceImportException;
import org.eclipse.tracecompass.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.tracecompass.tmf.ctf.core.CtfConstants;
import org.eclipse.tracecompass.tmf.remote.core.proxy.RemoteSystemProxy;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfOpenTraceHelper;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceTypeUIUtils;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * <p>
 * Command handler implementation to import traces from a (remote) session to a
 * tracing project.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class ImportHandler extends BaseControlViewHandler {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /** The preference key to remeber whether or not the user wants the notification shown next time **/
    private static final String NOTIFY_IMPORT_STREAMED_PREF_KEY = "NOTIFY_IMPORT_STREAMED"; //$NON-NLS-1$

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

        CommandParameter param;
        fLock.lock();
        try {
            param = fParam;
            if (param == null) {
                return null;
            }
            param = param.clone();
        } finally {
            fLock.unlock();
        }

        // create default project
        IProject project = TmfProjectRegistry.createProject(RemoteFetchLogWizardRemotePage.DEFAULT_REMOTE_PROJECT_NAME, null, null);

        if (param.getSession().isLiveTrace()) {
            importLiveTrace(new LttngRelaydConnectionInfo(param.getSession().getLiveUrl(), param.getSession().getLivePort(), param.getSession().getName()), project);
            return null;
        } else if (param.getSession().isStreamedTrace()) {

            IPreferenceStore store = Activator.getDefault().getPreferenceStore();
            String notify = store.getString(NOTIFY_IMPORT_STREAMED_PREF_KEY);
            if (!MessageDialogWithToggle.ALWAYS.equals(notify)) {
                MessageDialogWithToggle.openInformation(window.getShell(), null, Messages.TraceControl_ImportDialogStreamedTraceNotification, Messages.TraceControl_ImportDialogStreamedTraceNotificationToggle, false, store, NOTIFY_IMPORT_STREAMED_PREF_KEY);
            }

            // Streamed trace
            TmfProjectElement projectElement = TmfProjectRegistry.getProject(project, true);
            TmfTraceFolder traceFolder = projectElement.getTracesFolder();

            ImportTraceWizard wizard = new ImportTraceWizard();
            wizard.init(PlatformUI.getWorkbench(), new StructuredSelection(traceFolder));
            WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
            dialog.open();
            return null;
        }

        // Generate the profile
        TraceSessionComponent session = param.getSession();
        RemoteImportProfileElement profile = new RemoteImportProfileElement(null, "LTTng Remote Traces"); //$NON-NLS-1$
        RemoteSystemProxy proxy = session.getTargetNode().getRemoteSystemProxy();
        IRemoteConnection rc = proxy.getRemoteConnection();
        String name = rc.getName();

        if (!rc.hasService(IRemoteConnectionHostService.class)) {
            return null;
        }

        String scheme = rc.getConnectionType().getScheme();
        IRemoteConnectionHostService hostService = checkNotNull(rc.getService(IRemoteConnectionHostService.class));
        String address = hostService.getHostname();
        String user = hostService.getUsername();
        int port =  hostService.getPort();

        URI remoteUri;
        try {
            remoteUri = new URI(scheme, user, address, port, null, null, null);
        } catch (URISyntaxException e) {
            return false;
        }
        RemoteImportConnectionNodeElement connection = new RemoteImportConnectionNodeElement(profile, name, remoteUri.toString());
        String pathString = session.isSnapshotSession() ? session.getSnapshotInfo().getSnapshotPath() : session.getSessionPath();
        IPath path = new Path(pathString);
        RemoteImportTraceGroupElement group = new RemoteImportTraceGroupElement(connection, path.toString());
        group.setRecursive(true);
        TracePackageElement element = new TracePackageTraceElement(group, "", "");  //$NON-NLS-1$//$NON-NLS-2$
        new TracePackageFilesElement(element, ".*"); //$NON-NLS-1$

        String experimentName = path.lastSegment();
        if (!experimentName.startsWith(session.getName())) {
            experimentName = session.getName();
        }

        RemoteFetchLogWizard wizard = new RemoteFetchLogWizard(profile, experimentName);
        wizard.init(PlatformUI.getWorkbench(), StructuredSelection.EMPTY);
        WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
        dialog.open();

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
                    // Add only TraceSessionComponents that are inactive and not
                    // destroyed
                    TraceSessionComponent tmpSession = (TraceSessionComponent) element;
                    if ((tmpSession.isSnapshotSession() || tmpSession.isLiveTrace() || (tmpSession.getSessionState() == TraceSessionState.INACTIVE)) && (!tmpSession.isDestroyed())) {
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
                fParam = new CommandParameter(NonNullUtils.checkNotNull(session));
            }
        } finally {
            fLock.unlock();
        }
        return isEnabled;
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------

   private static void importLiveTrace(final LttngRelaydConnectionInfo connectionInfo, final IProject project) {
        Job job = new Job(Messages.TraceControl_ImportJob) {

            @Override
            protected IStatus run(final IProgressMonitor monitor) {
                try {
                    // We initiate the connection first so that we can retrieve the trace path
                    LttngRelaydConsumer lttngRelaydConsumer = LttngRelaydConnectionManager.getInstance().getConsumer(connectionInfo);
                    try {
                        lttngRelaydConsumer.connect();
                    } catch (CoreException e) {
                        return new Status(IStatus.ERROR, Activator.PLUGIN_ID, org.eclipse.tracecompass.internal.lttng2.control.ui.relayd.Messages.LttngRelaydConnectionManager_ConnectionError, e);
                    }
                    initializeTraceResource(connectionInfo, lttngRelaydConsumer.getTracePath(), project);
                    return Status.OK_STATUS;
                } catch (CoreException | TmfTraceImportException e) {
                    return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.TraceControl_LiveTraceInitError, e);
                }
            }

        };
        job.setSystem(true);
        job.schedule();
    }


   private static void initializeTraceResource(final LttngRelaydConnectionInfo connectionInfo, final String tracePath, final IProject project) throws CoreException, TmfTraceImportException {
       final TmfProjectElement projectElement = TmfProjectRegistry.getProject(project, true);
       final TmfTraceFolder tracesFolder = projectElement.getTracesFolder();
       if (tracesFolder != null) {
           IFolder folder = tracesFolder.getResource();
           IFolder traceFolder = folder.getFolder(connectionInfo.getSessionName());
           Path location = new Path(tracePath);
           IStatus result = ResourcesPlugin.getWorkspace().validateLinkLocation(folder, location);
           if (result.isOK()) {
               traceFolder.createLink(location, IResource.REPLACE, new NullProgressMonitor());
           } else {
               throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, result.getMessage()));
           }

           TraceTypeHelper selectedTraceType = TmfTraceTypeUIUtils.selectTraceType(location.toOSString(), null, null);
           // No trace type was determined.
           TmfTraceTypeUIUtils.setTraceType(traceFolder, selectedTraceType);

           TmfTraceElement found = null;
           final List<TmfTraceElement> traces = tracesFolder.getTraces();
           for (TmfTraceElement candidate : traces) {
               if (candidate.getName().equals(connectionInfo.getSessionName())) {
                   found = candidate;
               }
           }

           if (found == null) {
               throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.TraceControl_LiveTraceElementError));
           }

           // Properties used to be able to reopen a trace in live mode
           traceFolder.setPersistentProperty(CtfConstants.LIVE_HOST, connectionInfo.getHost());
           traceFolder.setPersistentProperty(CtfConstants.LIVE_PORT, Integer.toString(connectionInfo.getPort()));
           traceFolder.setPersistentProperty(CtfConstants.LIVE_SESSION_NAME, connectionInfo.getSessionName());

           final TmfTraceElement finalTrace = found;
           Display.getDefault().syncExec(new Runnable() {

               @Override
               public void run() {
                   TmfOpenTraceHelper.openTraceFromElement(finalTrace);
               }
           });
       }
   }
}
