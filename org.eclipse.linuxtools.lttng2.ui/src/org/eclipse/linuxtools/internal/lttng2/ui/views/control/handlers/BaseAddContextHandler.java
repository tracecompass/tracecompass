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

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.internal.lttng2.ui.Activator;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.IAddContextDialog;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.TraceControlDialogFactory;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.messages.Messages;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

/**
 * <p>
 * Base command handler implementation to add contexts.
 * </p>
 *
 * @author Bernd Hufmann
 */
public abstract class BaseAddContextHandler extends BaseControlViewHandler {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The command execution parameter.
     */
    protected CommandParameter fParam = null;

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Adds contexts to channel(s) and/or event(s)
     *
     * @param param
     *            - a parameter instance with data for the command execution
     * @param contextNames
     *            - list contexts to add
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If something goes wrong
     */
    public abstract void addContexts(CommandParameter param, List<String> contextNames, IProgressMonitor monitor) throws ExecutionException;

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

        if (window == null) {
            return false;
        }
        fLock.lock();
        try {
            // Make a copy for thread safety
            final CommandParameter param = fParam.clone();

            UIJob getJob = new UIJob(Messages.TraceControl_GetContextJob) {
                @Override
                public IStatus runInUIThread(IProgressMonitor monitor) {

                    try {
                        final List<String> availableContexts = param.getSession().getContextList(monitor);
                        final IAddContextDialog dialog = TraceControlDialogFactory.getInstance().getAddContextDialog();
                        dialog.setAvalibleContexts(availableContexts);

                        if ((dialog.open() != Window.OK) || (dialog.getContexts().isEmpty())) {
                            return Status.OK_STATUS;
                        }

                        Job addJob = new Job(Messages.TraceControl_AddContextJob) {
                            @Override
                            protected IStatus run(IProgressMonitor monitor2) {
                                Exception error = null;

                                try {
                                    List<String> contextNames = dialog.getContexts();
                                    addContexts(param, contextNames, monitor2);

                                } catch (ExecutionException e) {
                                    error = e;
                                }

                                // get session configuration in all cases
                                refresh(param);

                                if (error != null) {
                                    return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.TraceControl_AddContextFailure, error);
                                }
                                return Status.OK_STATUS;
                            }
                        };
                        addJob.setUser(true);
                        addJob.schedule();
                    } catch (ExecutionException e) {
                        return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.TraceControl_GetContextFailure, e);
                    }

                    return Status.OK_STATUS;
                }
            };
            getJob.setUser(false);
            getJob.schedule();

        } finally {
            fLock.unlock();
        }
        return null;
    }
}
