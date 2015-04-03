/**********************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick-Jeffrey Pollo Guilbert - Added headers, exporting .lttng profiles
 *   William Enright - Added ProfileHandler implementation
 *   William Tri-Khiem Truong - Completed documentation
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.handlers;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteFileService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.internal.lttng2.control.ui.Activator;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.ControlView;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs.ProfileDialog;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TargetNodeComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceSessionGroup;
import org.eclipse.tracecompass.tmf.remote.core.proxy.RemoteSystemProxy;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * <p>
 * Profile Handler to select and load session profiles for tracing purposes
 * </p>
 *
 * @author William Tri-Khiem Truong, William Enright, Patrick-Jeffrey Pollo Guilbert
 */
public class ProfileHandler extends BaseControlViewHandler {

    /**
     * Id of the parameter for the remote services id.
     *
     * @see NewConnectionHandler
     * @see IRemoteConnectionType#getId()
     */
    public static final String PARAMETER_REMOTE_SERVICES_ID = "org.eclipse.linuxtools.lttng2.control.ui.remoteServicesIdParameter"; //$NON-NLS-1$

    /**
     * (INewConnectionDialog) wd).getConnection() Id of the parameter for the
     * name of the remote connection.
     *
     * @see NewConnectionHandler
     * @see IRemoteConnectionType#getName()
     */
    public static final String PARAMETER_CONNECTION_NAME = "org.eclipse.linuxtools.lttng2.control.ui.connectionNameParameter"; //$NON-NLS-1$

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        Shell s = new Shell(SWT.CENTER);

        Rectangle screenSize = Display.getCurrent().getPrimaryMonitor().getBounds();
        s.setLocation((screenSize.width - s.getBounds().width) / 2, (screenSize.height - s.getBounds().height) / 2);
        s.setMinimumSize(500, 500);

        IWorkbenchPage page = getWorkbenchPage();
        if (page == null) {
            return false;
        }

        TargetNodeComponent elementParent = null;

        // Check if the session group project is selected
        ISelection selection = page.getSelection(ControlView.ID);
        if (selection instanceof StructuredSelection) {
            Object element = ((StructuredSelection) selection).getFirstElement();
            final TraceSessionGroup sessionGroup = (element instanceof TraceSessionGroup) ? (TraceSessionGroup) element : null;
            if (sessionGroup != null)
            {
                if (sessionGroup.getParent() instanceof TargetNodeComponent) {
                    elementParent = (TargetNodeComponent) sessionGroup.getParent();
                    final ProfileDialog btd = new ProfileDialog(s);
                    if (btd.open() != Window.OK) {
                        return null;
                    }
                    final String remotePath = sessionGroup.getTargetNode().getRemoteSystemProxy().getRemoteConnection().getProperty("user.home") + "/.lttng/sessions";  //$NON-NLS-1$//$NON-NLS-2$

                    RemoteSystemProxy proxy = elementParent.getRemoteSystemProxy();
                    IRemoteFileService fsss = proxy.getRemoteConnection().getService(IRemoteFileService.class);
                    ArrayList<File> checkedFiles = btd.getCheckedFiles();

                    for (File file : checkedFiles) {
                        final IFileStore remoteFolder = fsss.getResource(remotePath);
                        final IFileStore remoteFile = remoteFolder.getFileStore(new Path(file.getName()));
                        try {
                            try (OutputStream out = remoteFile.openOutputStream(EFS.NONE, new NullProgressMonitor()))
                            {
                                Files.copy(file.toPath(), out);
                            }
                        } catch (CoreException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        Job job = new Job(Messages.TraceControl_LoadSessionJob) {
                            @Override
                            protected IStatus run(IProgressMonitor monitor) {
                                try {
                                    sessionGroup.loadSession(remotePath + "/" + remoteFile.getName(), monitor); //$NON-NLS-1$
                                } catch (ExecutionException e) {
                                    return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.TraceControl_LoadSessionFailure, e);
                                }
                                return Status.OK_STATUS;
                            }
                        };

                        job.setUser(true);
                        job.schedule();
                    }

                }

            }
        }
        // Refreshing the sessions in the control view by calling the refresh
        // command

        IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);

        IHandlerService service = part.getSite().getService(IHandlerService.class);

        try {
            service.executeCommand("org.eclipse.linuxtools.internal.lttng2.ui.commands.control.refresh", null); //$NON-NLS-1$
        } catch (NotDefinedException e) {

        } catch (NotEnabledException e) {

        } catch (NotHandledException e) {

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
            if (isEnabled) {
            }
        } finally {
            fLock.unlock();
        }
        return isEnabled;
    }
}