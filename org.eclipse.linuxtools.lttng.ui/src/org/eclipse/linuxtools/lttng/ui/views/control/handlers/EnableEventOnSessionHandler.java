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

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.lttng.ui.views.control.ControlView;
import org.eclipse.linuxtools.lttng.ui.views.control.model.TraceSessionState;
import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.TraceSessionComponent;
import org.eclipse.ui.IWorkbenchPage;

/**
 * <b><u>EnableEventOnSessionHandler</u></b>
 * <p>
 * Command handler implementation to enable events for a known session and default channel 'channel0'
 * (which will be created if doesn't exist).
 * </p>
 */
public class EnableEventOnSessionHandler extends BaseEnableEventHandler {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    
    //-------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.handlers.BaseEnableEventHandler#enableEvents(java.util.List, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    void enableEvents(List<String> eventNames, IProgressMonitor monitor) throws ExecutionException {
        fSession.enableEvents(eventNames, true, monitor);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.handlers.BaseEnableEventHandler#enableSyscalls(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    void enableSyscalls(IProgressMonitor monitor) throws ExecutionException {
        fSession.enableSyscalls(monitor);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.handlers.BaseEnableEventHandler#enableProbe(java.lang.String, java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    void enableProbe(String eventName, String probe, IProgressMonitor monitor) throws ExecutionException {
        fSession.enableProbe(eventName, probe, monitor);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.handlers.BaseEnableEventHandler#enableFunctionProbe(java.lang.String, java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    void enableFunctionProbe(String eventName, String probe, IProgressMonitor monitor) throws ExecutionException {
        fSession.enableFunctionProbe(eventName, probe, monitor);
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

        fSession = null;

        // Check if one session is selected
        ISelection selection = page.getSelection(ControlView.ID);
        if (selection instanceof StructuredSelection) {
            StructuredSelection structered = ((StructuredSelection) selection);
            for (Iterator<?> iterator = structered.iterator(); iterator.hasNext();) {
                Object element = (Object) iterator.next();
                if (element instanceof TraceSessionComponent) {
                    // Add only TraceSessionComponents is inactive and not destroyed
                    TraceSessionComponent session = (TraceSessionComponent) element; 
                    if(session.getSessionState() == TraceSessionState.INACTIVE && !session.isDestroyed()) {
                        fSession = session;
                    }
                }
            }
        }
        return fSession != null;
    }
}
