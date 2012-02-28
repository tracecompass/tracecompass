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
import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.TraceChannelComponent;
import org.eclipse.ui.IWorkbenchPage;

/**
 * <b><u>EnableEventOnChannelHandler</u></b>
 * <p>
 * Command handler implementation to enable events for a known channel.
 * </p>
 */
public class EnableEventOnChannelHandler extends BaseEnableEventHandler {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The channel component the command is to be executed on. 
     */
    private TraceChannelComponent fChannel = null;

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.handlers.BaseEnableEventHandler#enableEvents(java.util.List, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    void enableEvents(List<String> eventNames, IProgressMonitor monitor) throws ExecutionException {
        fChannel.enableEvents(eventNames, monitor);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.handlers.BaseEnableEventHandler#enableSyscalls(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    void enableSyscalls(IProgressMonitor monitor) throws ExecutionException {
        fChannel.enableSyscalls(monitor);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.handlers.BaseEnableEventHandler#enableProbe(java.lang.String, java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    void enableProbe(String eventName, String probe, IProgressMonitor monitor) throws ExecutionException {
        fChannel.enableProbe(eventName, probe, monitor);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.handlers.BaseEnableEventHandler#enableFunctionProbe(java.lang.String, java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    void enableFunctionProbe(String eventName, String probe, IProgressMonitor monitor) throws ExecutionException {
        fChannel.enableFunctionProbe(eventName, probe, monitor);
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

        fChannel = null;
        ISelection selection = page.getSelection(ControlView.ID);
        if (selection instanceof StructuredSelection) {
            StructuredSelection structered = ((StructuredSelection) selection);
            for (Iterator<?> iterator = structered.iterator(); iterator.hasNext();) {
                Object element = (Object) iterator.next();
                if (element instanceof TraceChannelComponent) {
                    fChannel = (TraceChannelComponent) element;
                    fSession = fChannel.getSession();
                }
            }
        }
        return fChannel != null;
    }

}
