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
 *   Bernd Hufmann - Updated for support of LTTng Tools 2.1
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.control.ui.views.handlers;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.LogLevelType;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.TraceLogLevel;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.TraceSessionState;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.ControlView;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl.TraceDomainComponent;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl.TraceSessionComponent;
import org.eclipse.ui.IWorkbenchPage;

/**
 * <p>
 * Command handler implementation to enable events for a known session and default channel 'channel0'
 * (which will be created if doesn't exist).
 * </p>
 *
 * @author Bernd Hufmann
 */
public class EnableEventOnSessionHandler extends BaseEnableEventHandler {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    //-------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void enableEvents(CommandParameter param, List<String> eventNames, boolean isKernel, String filterExpression, IProgressMonitor monitor) throws ExecutionException {
        param.getSession().enableEvents(eventNames, isKernel, filterExpression, monitor);
    }

    @Override
    public void enableSyscalls(CommandParameter param, IProgressMonitor monitor) throws ExecutionException {
        param.getSession().enableSyscalls(monitor);
    }

    @Override
    public void enableProbe(CommandParameter param, String eventName, boolean isFunction, String probe, IProgressMonitor monitor) throws ExecutionException {
        param.getSession().enableProbe(eventName, isFunction, probe, monitor);
    }

    @Override
    public void enableLogLevel(CommandParameter param, String eventName, LogLevelType logLevelType, TraceLogLevel level, String filterExpression, IProgressMonitor monitor) throws ExecutionException {
        param.getSession().enableLogLevel(eventName, logLevelType, level, filterExpression, monitor);
    }

    @Override
    public TraceDomainComponent getDomain(CommandParameter param) {
        return null;
    }

    @Override
    public boolean isEnabled() {
        // Get workbench page for the Control View
        IWorkbenchPage page = getWorkbenchPage();
        if (page == null) {
            return false;
        }

        TraceSessionComponent session = null;
        // Check if one session is selected
        ISelection selection = page.getSelection(ControlView.ID);
        if (selection instanceof StructuredSelection) {
            StructuredSelection structered = ((StructuredSelection) selection);
            for (Iterator<?> iterator = structered.iterator(); iterator.hasNext();) {
                Object element = iterator.next();
                if (element instanceof TraceSessionComponent) {
                    // Add only if corresponding TraceSessionComponents is inactive and not destroyed
                    TraceSessionComponent tmpSession = (TraceSessionComponent) element;
                    if(tmpSession.getSessionState() == TraceSessionState.INACTIVE && !tmpSession.isDestroyed()) {
                        session = tmpSession;
                    }
                }
            }
        }
        boolean isEnabled = (session != null);
        fLock.lock();
        try {
            fParam = null;
            if(isEnabled) {
                fParam = new CommandParameter(session);
            }
        } finally {
            fLock.unlock();
        }
        return isEnabled;
    }
}
