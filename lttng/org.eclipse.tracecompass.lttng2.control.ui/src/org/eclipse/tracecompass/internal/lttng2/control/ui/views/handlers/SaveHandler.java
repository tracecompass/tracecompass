/**********************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.handlers;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteFileService;
import org.eclipse.remote.core.IRemoteProcessService;
import org.eclipse.remote.core.RemoteServicesUtils;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.internal.lttng2.control.core.LttngProfileManager;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceSessionState;
import org.eclipse.tracecompass.internal.lttng2.control.ui.Activator;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.ControlView;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs.ISaveDialog;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs.TraceControlDialogFactory;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceSessionComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.service.LTTngControlServiceConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;


/**
 * Command handler for saving sessions
 * @author Bernd Hufmann
 *
 */
public class SaveHandler extends BaseControlViewHandler {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The list of session components the command is to be executed on.
     */
    protected List<TraceSessionComponent> fSessions = new ArrayList<>();

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

            final List<TraceSessionComponent> sessions = new ArrayList<>();
            sessions.addAll(fSessions);

            // Open dialog box for the save dialog path
            final ISaveDialog dialog = TraceControlDialogFactory.getInstance().getSaveDialog();
            if (dialog.open() != Window.OK) {
                return null;
            }

            Job job = new Job(Messages.TraceControl_SaveJob) {
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    try {
                        for (TraceSessionComponent session : sessions) {
                            session.saveSession(null, null, dialog.isForce(), monitor);

                            final IRemoteConnection connection = session.getTargetNode().getRemoteSystemProxy().getRemoteConnection();
                            SubMonitor subMonitor = SubMonitor.convert(monitor, 3);
                            // create destination directory (if necessary)
                            IRemoteProcessService processService = connection.getService(IRemoteProcessService.class);
                            IPath path = null;
                            if (processService != null) {
                                String cwd = processService.getWorkingDirectory();
                                path = RemoteServicesUtils.posixPath(cwd);
                                path = path.append(LTTngControlServiceConstants.DEFAULT_PATH);
                            }

                            if (path == null) {
                                return Status.CANCEL_STATUS;
                            }

                            // upload file
                            IRemoteFileService fileService = connection.getService(IRemoteFileService.class);
                            if (fileService == null) {
                                return Status.CANCEL_STATUS;
                            }
                            IPath dest = LttngProfileManager.getProfilePath();
                            String profileName = session.getName() + ".lttng"; //$NON-NLS-1$
                            final Path destPath = FileSystems.getDefault().getPath(dest.toString()).resolve(profileName);
                            IFileStore destFileStore = EFS.getLocalFileSystem().fromLocalFile(destPath.toFile());
                            SubMonitor childMonitor = subMonitor.newChild(1);

                            IPath remotePath = RemoteServicesUtils.posixPath(path.toString()).append(profileName);
                            IFileStore remoteResource = fileService.getResource(remotePath.toString());
                            final boolean overwrite[] = new boolean[1];
                            if (destPath.toFile().exists()) {
                                Display.getDefault().syncExec(() -> overwrite[0] = MessageDialog.openConfirm(Display.getDefault().getActiveShell(),
                                        Messages.TraceControl_ProfileAlreadyExists,
                                        NLS.bind(Messages.TraceControl_OverwriteQuery, destPath.getFileName())));

                                if (!overwrite[0]) {
                                    continue;
                                }
                            }
                            remoteResource.copy(destFileStore, EFS.OVERWRITE, childMonitor);
                        }
                    } catch (ExecutionException | CoreException e) {
                        return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.TraceControl_SaveFailure, e);
                    }
                    return Status.OK_STATUS;
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

        List<TraceSessionComponent> sessions = new ArrayList<>(0);

        // Check if one or more session are selected
        ISelection selection = page.getSelection(ControlView.ID);
        if (selection instanceof StructuredSelection) {
            StructuredSelection structered = ((StructuredSelection) selection);
            for (Iterator<?> iterator = structered.iterator(); iterator.hasNext();) {
                Object element = iterator.next();
                if (element instanceof TraceSessionComponent) {
                    // Add only TraceSessionComponents that are inactive and not destroyed
                    TraceSessionComponent session = (TraceSessionComponent) element;
                    if ((session.getSessionState() == TraceSessionState.INACTIVE) && (!session.isDestroyed())) {
                        sessions.add(session);
                    }
                }
            }
        }
        boolean isEnabled = !sessions.isEmpty();
        fLock.lock();
        try {
            fSessions = null;
            if (isEnabled) {
                fSessions = sessions;
            }
        } finally {
            fLock.unlock();
        }
        return isEnabled;
    }
}