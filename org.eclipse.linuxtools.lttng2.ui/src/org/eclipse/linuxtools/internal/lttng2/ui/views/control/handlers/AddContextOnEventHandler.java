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
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.handlers;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.TraceSessionState;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.ControlView;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceEventComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceSessionComponent;
import org.eclipse.ui.IWorkbenchPage;

/**
 * <p>
 * Command handler implementation to add contexts to a given event.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class AddContextOnEventHandler extends BaseAddContextHandler {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void addContexts(CommandParameter param, List<String> contextNames, IProgressMonitor monitor) throws ExecutionException {
        if (param instanceof EventCommandParameter) {
            TraceEventComponent event = ((EventCommandParameter)param).getEvent();
            event.addContexts(contextNames, monitor);
        }
    }

    @Override
    public boolean isEnabled() {
        // Get workbench page for the Control View
        IWorkbenchPage page = getWorkbenchPage();
        if (page == null) {
            return false;
        }

        TraceEventComponent event = null;
        TraceSessionComponent session = null;
        ISelection selection = page.getSelection(ControlView.ID);
        if (selection instanceof StructuredSelection) {
            StructuredSelection structered = ((StructuredSelection) selection);
            for (Iterator<?> iterator = structered.iterator(); iterator.hasNext();) {
                Object element = iterator.next();
                if (element instanceof TraceEventComponent) {
                    // Add only if corresponding TraceSessionComponents is inactive and not destroyed
                    TraceEventComponent tmpEvent = (TraceEventComponent) element;
                    session = tmpEvent.getSession();
                    if(session.getSessionState() == TraceSessionState.INACTIVE && !session.isDestroyed()) {
                        event = tmpEvent;
                    }
                }
            }
        }

        boolean isEnabled = (event != null);
        fLock.lock();
        try {
            fParam = null;
            if(isEnabled) {
                fParam = new EventCommandParameter(session, event);
            }
        } finally {
            fLock.unlock();
        }
        return isEnabled;
    }
}
