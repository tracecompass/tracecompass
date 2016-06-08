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
 *   Bernd Hufmann - Updated for support of LTTng Tools 2.1
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.handlers;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceDomainType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.ITraceLogLevel;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.LogLevelType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceSessionState;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.ControlView;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceDomainComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceSessionComponent;
import org.eclipse.ui.IWorkbenchPage;

/**
 * <p>
 * Command handler implementation to enable events for a known domain and default channel 'channel0'
 * (which will be created if doesn't exist).
 * </p>
 *
 * @author Bernd Hufmann
 */
public class EnableEventOnDomainHandler extends BaseEnableEventHandler {

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void enableEvents(CommandParameter param, List<String> eventNames, TraceDomainType domain, String filterExression, List<String> excludedEvents, IProgressMonitor monitor) throws ExecutionException {
        if (param instanceof DomainCommandParameter) {
            ((DomainCommandParameter)param).getDomain().enableEvents(eventNames, filterExression, excludedEvents, monitor);
        }
    }

    @Override
    public void enableSyscalls(CommandParameter param, IProgressMonitor monitor) throws ExecutionException {
        if (param instanceof DomainCommandParameter) {
            ((DomainCommandParameter)param).getDomain().enableSyscalls(monitor);
        }
    }

    @Override
    public void enableProbe(CommandParameter param, String eventName, boolean isFunction, String probe, IProgressMonitor monitor) throws ExecutionException {
        if (param instanceof DomainCommandParameter) {
            ((DomainCommandParameter)param).getDomain().enableProbe(eventName, isFunction, probe, monitor);
        }
    }

    @Override
    public void enableLogLevel(CommandParameter param, List<String> eventNames, LogLevelType logLevelType, ITraceLogLevel level, String filterExression, TraceDomainType domain, IProgressMonitor monitor) throws ExecutionException {
        if (param instanceof DomainCommandParameter) {
            ((DomainCommandParameter)param).getDomain().enableLogLevel(eventNames, logLevelType, level, filterExression, domain, monitor);
        }
    }

    @Override
    public TraceDomainComponent getDomain(CommandParameter param) {
        if (param instanceof DomainCommandParameter) {
            return ((DomainCommandParameter)param).getDomain();
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

        TraceDomainComponent domain = null;
        TraceSessionComponent session = null;
        ISelection selection = page.getSelection(ControlView.ID);
        if (selection instanceof StructuredSelection) {
            StructuredSelection structered = ((StructuredSelection) selection);
            for (Iterator<?> iterator = structered.iterator(); iterator.hasNext();) {
                Object element = iterator.next();
                if (element instanceof TraceDomainComponent) {
                    // Add only if corresponding TraceSessionComponents is inactive and not destroyed
                    TraceDomainComponent tmpDomain = (TraceDomainComponent) element;
                    session = tmpDomain.getSession();
                    if(session.getSessionState() == TraceSessionState.INACTIVE && !session.isDestroyed()) {
                        domain = tmpDomain;
                    }
                }
            }
        }

        boolean isEnabled = (domain != null);
        fLock.lock();
        try {
            fParam = null;
            if(isEnabled) {
                fParam = new DomainCommandParameter(checkNotNull(session), checkNotNull(domain));
            }
        } finally {
            fLock.unlock();
        }
        return isEnabled;
    }
}
