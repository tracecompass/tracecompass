/**********************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal, Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick-Jeffrey Pollo Guilbert - Added headers, exporting .lttng profiles
 *   William Enright - Added ProfileHandler implementation
 *   William Tri-Khiem Truong - Completed documentation
 *   Bernd Hufmann - Renamed from ProfileHandler and redesign
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.handlers;

import java.util.ArrayList;
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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteFileService;
import org.eclipse.remote.core.IRemoteProcessService;
import org.eclipse.remote.core.RemoteServicesUtils;
import org.eclipse.tracecompass.internal.lttng2.control.ui.Activator;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.ControlView;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs.ILoadDialog;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs.TraceControlDialogFactory;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceSessionGroup;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.service.ILttngControlService;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.service.LTTngControlServiceConstants;
import org.eclipse.ui.IWorkbenchPage;

/**
 * Command handler implementation to execute load command.
 *
 * @author Bernd Hufmann
 * @author Patrick-Jeffrey Pollo Guilbert
 * @author William Enright
 * @author William Tri-Khiem Truong
 */
public class LoadHandler extends BaseControlViewHandler {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The trace session group the command is to be executed on.
     */
    private TraceSessionGroup fSessionGroup = null;

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        final TraceSessionGroup sessionGroup;
        final IRemoteConnection connection;

        fLock.lock();
        try {
             sessionGroup = fSessionGroup;
             if (sessionGroup == null) {
                 return null;
             }
             connection = sessionGroup.getTargetNode().getRemoteSystemProxy().getRemoteConnection();
        } finally {
            fLock.unlock();
        }

        // Open dialog box for the session input path
        final ILoadDialog dialog = TraceControlDialogFactory.getInstance().getLoadDialog();
        dialog.initialize(connection);
        if (dialog.open() != Window.OK) {
            return null;
        }

        Job job = new Job(Messages.TraceControl_LoadJob) {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
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

                    ILttngControlService service = sessionGroup.getControlService();
                    List<String> commands = new ArrayList<>();
                    commands.add("mkdir -p " + path.toString()); //$NON-NLS-1$
                    service.runCommands(subMonitor.newChild(1), commands);
                    // upload files
                    IRemoteFileService fileService = connection.getService(IRemoteFileService.class);
                    if (fileService == null) {
                        return Status.CANCEL_STATUS;
                    }

                    List<IFileStore> localFiles = dialog.getLocalResources();
                    List<IFileStore> remoteResources;
                    if (localFiles != null) {
                        remoteResources = new ArrayList<>();
                        SubMonitor childMonitor = subMonitor.newChild(1);
                        for (IFileStore local : localFiles) {
                            IPath remotePath = RemoteServicesUtils.posixPath(path.toString()).append(local.getName());
                            IFileStore remoteResource = fileService.getResource(remotePath.toString());
                            local.copy(remoteResource, EFS.OVERWRITE, childMonitor);
                            remoteResources.add(remoteResource);
                        }
                    } else {
                        subMonitor.newChild(1);
                        remoteResources = dialog.getRemoteResources();
                    }
                    loadRemoteProfile(sessionGroup, subMonitor.newChild(1), remoteResources, dialog.isForce());
                } catch (ExecutionException | CoreException e) {
                    return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.TraceControl_LoadFailure, e);
                } catch (InterruptedException e) {
                    return Status.CANCEL_STATUS;
                }
                monitor.done();
                return Status.OK_STATUS;
            }

        };
        job.setUser(true);
        job.schedule();

        return null;
    }

    @Override
    public boolean isEnabled() {

        // Get workbench page for the Control View
        IWorkbenchPage page = getWorkbenchPage();
        if (page == null) {
            return false;
        }

        TraceSessionGroup sessionGroup = null;

        // Check if the session group project is selected
        ISelection selection = page.getSelection(ControlView.ID);
        if (selection instanceof StructuredSelection) {
            Object element = ((StructuredSelection) selection).getFirstElement();
            sessionGroup = (element instanceof TraceSessionGroup) ? (TraceSessionGroup) element : null;
        }

        boolean isEnabled = sessionGroup != null;
        fLock.lock();
        try {
            fSessionGroup = null;
            if(isEnabled) {
                fSessionGroup = sessionGroup;
            }
        } finally {
            fLock.unlock();
        }
        return isEnabled;
    }

    private static void loadRemoteProfile(final TraceSessionGroup sessionGroup, IProgressMonitor monitor, List<IFileStore> files, boolean isForce) throws ExecutionException, InterruptedException  {
        SubMonitor subMonitor = SubMonitor.convert(monitor, files.size());
            for (IFileStore file : files) {
                // Check if operation was cancelled.
                if (subMonitor.isCanceled()) {
                    throw new InterruptedException();
                }
                subMonitor.beginTask(NLS.bind(Messages.TraceControl_LoadTask, file.getName()), 1);
                sessionGroup.loadSession(file.toURI().getPath(), isForce, subMonitor);
                subMonitor.done();
            }
    }
}
