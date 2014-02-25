/**********************************************************************
 * Copyright (c) 2013, 2014 Ericsson
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.TraceSessionState;
import org.eclipse.linuxtools.internal.lttng2.ui.Activator;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.ControlView;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.messages.Messages;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceSessionComponent;
import org.eclipse.ui.IWorkbenchPage;

/**
 * <p>
 * Command handler implementation to record a snapshot.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class SnaphshotHandler extends BaseControlViewHandler {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The list of session components the command is to be executed on.
     */
    protected List<TraceSessionComponent> fSessions = new ArrayList<>();

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        fLock.lock();
        try {
            // Make a copy for thread safety
            final List<TraceSessionComponent> sessions = new ArrayList<>();
            sessions.addAll(fSessions);

            Job job = new Job(Messages.TraceControl_RecordSnapshotJob) {
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    MultiStatus status = new MultiStatus(Activator.PLUGIN_ID, IStatus.OK, null, null);
                    for (Iterator<TraceSessionComponent> iterator = sessions.iterator(); iterator.hasNext();) {
                        try {
                            // record snapshot for all selected sessions sequentially
                            TraceSessionComponent session = iterator.next();
                            session.recordSnapshot(monitor);
                            if (monitor.isCanceled()) {
                                status.add(Status.CANCEL_STATUS);
                                break;
                            }
                        } catch (ExecutionException e) {
                            status.add(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.TraceControl_RecordSnapshotFailure, e));
                        }
                    }
                    return status;
                }
            };
            job.setUser(true);
            job.schedule();
        } finally {
            fLock.unlock();
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

        List<TraceSessionComponent> sessions = new ArrayList<>(0);

        // Check if one session is selected
        ISelection selection = page.getSelection(ControlView.ID);
        if (selection instanceof StructuredSelection) {
            StructuredSelection structered = ((StructuredSelection) selection);
            for (Iterator<?> iterator = structered.iterator(); iterator.hasNext();) {
                Object element = iterator.next();
                if (element instanceof TraceSessionComponent) {
                    // Add only if corresponding TraceSessionComponent is an active snapshot session and not destroyed
                    TraceSessionComponent session = (TraceSessionComponent) element;
                    if(session.isSnapshotSession() &&
                            session.getSessionState() == TraceSessionState.ACTIVE &&
                            !session.isDestroyed()) {
                        sessions.add(session);
                    }
                }
            }
        }
        boolean isEnabled = !sessions.isEmpty();
        fLock.lock();
        try {
            fSessions = null;
            if (isEnabled) {
                fSessions = sessions;
            }
        } finally {
            fLock.unlock();
        }
        return isEnabled;
    }
}