/**********************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.internal.lttng2.ui.Activator;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.ControlView;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.Messages;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.ICreateSessionDialog;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.TraceControlDialogFactory;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceSessionGroup;
import org.eclipse.ui.IWorkbenchPage;

/**
 * <b><u>CreateSessionHandler</u></b>
 * <p>
 * Command handler implementation to create a trace session.
 * </p>
 */
public class CreateSessionHandler extends BaseControlViewHandler {

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
    /*
     * (non-Javadoc)
     * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        fLock.lock();
        try {
            final TraceSessionGroup sessionGroup = fSessionGroup; 
                    
            // Open dialog box for the node name and address
            ICreateSessionDialog dialog = TraceControlDialogFactory.getInstance().getCreateSessionDialog();
            dialog.setTraceSessionGroup(sessionGroup);

            if (dialog.open() != Window.OK) {
                return null;
            }

            final String sessionName = dialog.getSessionName();
            final String sessionPath = dialog.isDefaultSessionPath() ? null : dialog.getSessionPath();

            Job job = new Job(Messages.TraceControl_CreateSessionJob) {
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    try {
                        sessionGroup.createSession(sessionName, sessionPath, monitor);
                    } catch (ExecutionException e) {
                        return new Status(Status.ERROR, Activator.PLUGIN_ID, e.toString());
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

    /*
     * (non-Javadoc)
     * @see org.eclipse.core.commands.AbstractHandler#isEnabled()
     */
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
}
