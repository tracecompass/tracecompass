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
package org.eclipse.linuxtools.lttng.ui.views.control.handlers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.lttng.ui.LTTngUiPlugin;
import org.eclipse.linuxtools.lttng.ui.views.control.ControlView;
import org.eclipse.linuxtools.lttng.ui.views.control.Messages;
import org.eclipse.linuxtools.lttng.ui.views.control.model.TraceSessionState;
import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.TraceSessionComponent;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * <b><u>ChangeSessionStateHandler</u></b>
 * <p>
 * Abstract command handler implementation to start or stop one or more trace sessions.
 * </p>
 */
abstract public class ChangeSessionStateHandler extends AbstractHandler {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The list of session components the command is to be executed on. 
     */
    protected List<TraceSessionComponent> fSessions = new ArrayList<TraceSessionComponent>();
    
    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @return new required state.
     */
    abstract TraceSessionState getNewState();
    
    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    
    /**
     * Performs the state change on given session. 
     * @param session - a session which state is to be changed
     * @param monitor - a progress monitor
     */
    abstract public void changeState(TraceSessionComponent session, IProgressMonitor monitor) throws ExecutionException;

    /*
     * (non-Javadoc)
     * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

        if (window == null) {
            return false;
        }

        Job job = new Job(Messages.TraceControl_StartSessionJob) {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    for (Iterator<TraceSessionComponent> iterator = fSessions.iterator(); iterator.hasNext();) {

                        // Start all selected sessions
                        TraceSessionComponent session = (TraceSessionComponent) iterator.next();
                        changeState(session, monitor);

                        // Set Session state
                        session.setSessionState(getNewState());
                        session.fireComponentChanged(session);
                    }
                } catch (ExecutionException e) {
                    return new Status(Status.ERROR, LTTngUiPlugin.PLUGIN_ID, e.toString());
                }  
                return Status.OK_STATUS;
            }
        };
        job.setUser(true);
        job.schedule();
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.core.commands.AbstractHandler#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        fSessions.clear();

        // Check if we are closing down
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return false;
        }

        // Check if we are in the Project View
        IWorkbenchPage page = window.getActivePage();
        if (page == null) {
            return false;
        }

        IWorkbenchPart part = page.getActivePart();
        if (!(part instanceof ControlView)) {
            return false;
        }

        // Check if one or more session are selected
        ISelection selection = page.getSelection(ControlView.ID);
        if (selection instanceof StructuredSelection) {
            StructuredSelection structered = ((StructuredSelection) selection);
            for (Iterator<?> iterator = structered.iterator(); iterator.hasNext();) {
                Object element = (Object) iterator.next();
                if (element instanceof TraceSessionComponent) {
                    // Add only TraceSessionComponents that are inactive and not destroyed
                    TraceSessionComponent session = (TraceSessionComponent) element;
                    if ((session.getSessionState() != getNewState()) && (!session.isDestroyed())) {
                        fSessions.add((TraceSessionComponent)element);
                    }
                }
            }
        }
        return fSessions.size() > 0;
    }
}
