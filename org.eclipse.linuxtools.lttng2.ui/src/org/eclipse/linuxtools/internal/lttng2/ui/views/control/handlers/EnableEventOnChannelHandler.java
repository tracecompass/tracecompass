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

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.ControlView;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.LogLevelType;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.TraceLogLevel;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceChannelComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceDomainComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceSessionComponent;
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

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.handlers.BaseEnableEventHandler#enableEvents(org.eclipse.linuxtools.internal.lttng2.ui.views.control.handlers.CommandParameter, java.util.List, boolean, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void enableEvents(CommandParameter param, List<String> eventNames, boolean isKernel, IProgressMonitor monitor) throws ExecutionException {
        if (param instanceof ChannelCommandParameter) {
            ((ChannelCommandParameter)param).getChannel().enableEvents(eventNames, monitor);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.handlers.BaseEnableEventHandler#enableSyscalls(org.eclipse.linuxtools.internal.lttng2.ui.views.control.handlers.CommandParameter, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void enableSyscalls(CommandParameter param, IProgressMonitor monitor) throws ExecutionException {
        if (param instanceof ChannelCommandParameter) {
            ((ChannelCommandParameter)param).getChannel().enableSyscalls(monitor);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.handlers.BaseEnableEventHandler#enableProbe(org.eclipse.linuxtools.internal.lttng2.ui.views.control.handlers.CommandParameter, java.lang.String, boolean, java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void enableProbe(CommandParameter param, String eventName, boolean isFunction, String probe, IProgressMonitor monitor) throws ExecutionException {
        if (param instanceof ChannelCommandParameter) {
            ((ChannelCommandParameter)param).getChannel().enableProbe(eventName, isFunction, probe, monitor);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.handlers.BaseEnableEventHandler#enableLogLevel(org.eclipse.linuxtools.internal.lttng2.ui.views.control.handlers.CommandParameter, java.lang.String, org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.LogLevelType, org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.TraceLogLevel, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void enableLogLevel(CommandParameter param, String eventName, LogLevelType logLevelType, TraceLogLevel level, IProgressMonitor monitor) throws ExecutionException {
        if (param instanceof ChannelCommandParameter) {
            ((ChannelCommandParameter)param).getChannel().enableLogLevel(eventName, logLevelType, level, monitor);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.handlers.BaseEnableEventHandler#getDomain(org.eclipse.linuxtools.internal.lttng2.ui.views.control.handlers.CommandParameter)
     */
    @Override
    public TraceDomainComponent getDomain(CommandParameter param) {
        if (param instanceof ChannelCommandParameter) {
            return (TraceDomainComponent) ((ChannelCommandParameter)param).getChannel().getParent();
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

        TraceChannelComponent channel = null;
        TraceSessionComponent session = null;
        ISelection selection = page.getSelection(ControlView.ID);
        if (selection instanceof StructuredSelection) {
            StructuredSelection structered = ((StructuredSelection) selection);
            for (Iterator<?> iterator = structered.iterator(); iterator.hasNext();) {
                Object element = (Object) iterator.next();
                if (element instanceof TraceChannelComponent) {
                    // Add only if corresponding TraceSessionComponents is inactive and not destroyed
                    TraceChannelComponent tmpChannel = (TraceChannelComponent) element; 
                    session = tmpChannel.getSession();
                    if(!session.isDestroyed()) {
                        channel = tmpChannel;
                    }
                }
            }
        }
        
        boolean isEnabled = (channel != null);
        fLock.lock();
        try {
            fParam = null;
            if(isEnabled) {
                fParam = new ChannelCommandParameter(session, channel);
            }
        } finally {
            fLock.unlock();
        }
        return isEnabled;
    }
}

