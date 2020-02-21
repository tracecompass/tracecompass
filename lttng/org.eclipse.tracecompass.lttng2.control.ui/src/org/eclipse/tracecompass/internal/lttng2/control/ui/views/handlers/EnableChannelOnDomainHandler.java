/**********************************************************************
 * Copyright (c) 2012, 2015 Ericsson
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

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceDomainType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IChannelInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceSessionState;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.ControlView;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceDomainComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceSessionComponent;
import org.eclipse.ui.IWorkbenchPage;

/**
 * <p>
 * Command handler implementation to enable a trace channel for known domain.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class EnableChannelOnDomainHandler extends BaseEnableChannelHandler {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void enableChannel(CommandParameter param, List<String> channelNames, IChannelInfo info, TraceDomainType domain, IProgressMonitor monitor) throws ExecutionException {
        if (param instanceof DomainCommandParameter) {
            ((DomainCommandParameter)param).getDomain().enableChannels(channelNames, info, monitor);
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

        // Check if one domain is selected
        ISelection selection = page.getSelection(ControlView.ID);
        if (selection instanceof StructuredSelection) {
            StructuredSelection structered = ((StructuredSelection) selection);
            for (Iterator<?> iterator = structered.iterator(); iterator.hasNext();) {
                Object element = iterator.next();
                if (element instanceof TraceDomainComponent) {
                    TraceDomainComponent tmpDomain = (TraceDomainComponent) element;
                    session = (TraceSessionComponent) tmpDomain.getParent();

                    // Add only TraceDomainComponent whose TraceSessionComponent parent is inactive and not destroyed
                    if ((session.getSessionState() == TraceSessionState.INACTIVE) && (!session.isDestroyed())) {
                        domain = tmpDomain;
                    }
                }
            }
        }

        boolean isEnabled = domain != null;

        // If it's a logger domain (JUL, LOG4J, Python), the enable channel is disabled.
        if ((domain != null) && (TraceDomainType.JUL.equals(domain.getDomain()) ||
                                 TraceDomainType.LOG4J.equals(domain.getDomain()) ||
                                 TraceDomainType.PYTHON.equals(domain.getDomain()))) {
            isEnabled = false;
        }

        fLock.lock();
        try {
            fParam = null;
            if (isEnabled) {
                fParam = new DomainCommandParameter(checkNotNull(session), checkNotNull(domain));
            }
        } finally {
            fLock.unlock();
        }

        return isEnabled;
    }

}
