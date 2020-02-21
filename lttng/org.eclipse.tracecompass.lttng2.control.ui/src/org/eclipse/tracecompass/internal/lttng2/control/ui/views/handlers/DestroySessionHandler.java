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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceSessionState;
import org.eclipse.tracecompass.internal.lttng2.control.ui.Activator;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.ControlView;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs.IConfirmDialog;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs.TraceControlDialogFactory;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceSessionComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceSessionGroup;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * <p>
 * Command handler implementation to destroy one or more trace sessions.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class DestroySessionHandler extends BaseControlViewHandler {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The list of session components the command is to be executed on.
     */
    @NonNull private final List<TraceSessionComponent> fSessions = new ArrayList<>();

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

        if (window == null) {
            return false;
        }

        List<TraceSessionComponent> tmpSessions = new ArrayList<>();

        // Make a copy of the session list to avoid concurrent modification
        // of the list of sessions
        fLock.lock();
        try {
            tmpSessions.addAll(fSessions);
        } finally {
            fLock.unlock();
        }
        final List<TraceSessionComponent> sessions = tmpSessions;

        // Get user confirmation
        IConfirmDialog dialog = TraceControlDialogFactory.getInstance().getConfirmDialog();
        if (!dialog.openConfirm(window.getShell(),
                Messages.TraceControl_DestroyConfirmationTitle,
                Messages.TraceControl_DestroyConfirmationMessage)) {

            return null;
        }

        Job job = new Job(Messages.TraceControl_DestroySessionJob) {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    for (TraceSessionComponent session : sessions) {
                        // Destroy all selected sessions
                        TraceSessionGroup sessionGroup = (TraceSessionGroup)session.getParent();
                        sessionGroup.destroySession(session, monitor);
                    }
                } catch (ExecutionException e) {
                    return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.TraceControl_DestroySessionFailure, e);
                }
                return Status.OK_STATUS;
            }
        };
        job.setUser(true);
        job.schedule();

        return null;
    }

    @Override
    public boolean isEnabled() {
        // Get workbench page for the Control View
        IWorkbenchPage page = getWorkbenchPage();
        if (page == null) {
            return false;
        }

        List<TraceSessionComponent> sessions = new ArrayList<>(0);

        // Check if one or more session are selected
        ISelection selection = page.getSelection(ControlView.ID);
        if (selection instanceof StructuredSelection) {
            StructuredSelection structered = ((StructuredSelection) selection);
            for (Iterator<?> iterator = structered.iterator(); iterator.hasNext();) {
                Object element = iterator.next();
                if (element instanceof TraceSessionComponent) {
                    // Add only TraceSessionComponents that are inactive and not destroyed
                    TraceSessionComponent session = (TraceSessionComponent) element;
                    if ((session.getSessionState() == TraceSessionState.INACTIVE) && (!session.isDestroyed())) {
                        sessions.add((TraceSessionComponent)element);
                    }
                }
            }
        }
        boolean isEnabled = !sessions.isEmpty();
        fLock.lock();
        try {
            fSessions.clear();
            if (isEnabled) {
                fSessions.addAll(sessions);
            }
        } finally {
            fLock.unlock();
        }
        return isEnabled;
    }
}
