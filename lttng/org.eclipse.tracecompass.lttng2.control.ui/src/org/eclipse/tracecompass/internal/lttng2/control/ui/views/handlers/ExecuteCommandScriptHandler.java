/**********************************************************************
 * Copyright (c) 2015 Ericsson
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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.tracecompass.internal.lttng2.control.ui.Activator;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.ControlView;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs.ISelectCommandScriptDialog;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs.TraceControlDialogFactory;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceSessionGroup;
import org.eclipse.ui.IWorkbenchPage;

/**
 * <p>
 * Command handler implementation to execute commands of a command script.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class ExecuteCommandScriptHandler extends BaseControlViewHandler {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The trace session group the command is to be executed on.
     */
    @Nullable private TraceSessionGroup fSessionGroup = null;

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        TraceSessionGroup tmpGroup = null;

        fLock.lock();
        try {
            tmpGroup = fSessionGroup;
        } finally {
            fLock.unlock();
        }

        final TraceSessionGroup sessionGroup = tmpGroup;
        if (sessionGroup == null) {
            return null;
        }

        // Open dialog box for the node name and address
        final ISelectCommandScriptDialog dialog = TraceControlDialogFactory.getInstance().getCommandScriptDialog();

        if (dialog.open() != Window.OK) {
            return null;
        }

        Job job = new Job(Messages.TraceControl_ExecuteScriptJob) {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    sessionGroup.executeCommands(monitor, dialog.getCommands());
                } catch (ExecutionException e) {
                    return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.TraceControl_CreateSessionFailure, e);
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

        TraceSessionGroup sessionGroup = null;

        // Check if the session group project is selected
        ISelection selection = page.getSelection(ControlView.ID);
        if (selection instanceof StructuredSelection) {
            Object element = ((StructuredSelection) selection).getFirstElement();
            sessionGroup = (element instanceof TraceSessionGroup) ? (TraceSessionGroup) element : null;
        }

        boolean isEnabled = sessionGroup != null;
        fLock.lock();
        try {
            fSessionGroup = null;
            if(isEnabled) {
                fSessionGroup = sessionGroup;
            }
        } finally {
            fLock.unlock();
        }
        return isEnabled;
    }
}
