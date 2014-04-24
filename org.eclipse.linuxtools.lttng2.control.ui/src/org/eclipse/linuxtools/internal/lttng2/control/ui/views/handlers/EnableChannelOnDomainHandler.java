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
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.control.ui.views.handlers;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.IChannelInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.TraceSessionState;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.ControlView;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl.TraceDomainComponent;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl.TraceSessionComponent;
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
    public void enableChannel(CommandParameter param, List<String> channelNames, IChannelInfo info, boolean isKernel, IProgressMonitor monitor) throws ExecutionException {
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

        fLock.lock();
        try {
            fParam = null;
            if (isEnabled) {
                fParam = new DomainCommandParameter(session, domain);
            }
        } finally {
            fLock.unlock();
        }

        return isEnabled;
    }

}
