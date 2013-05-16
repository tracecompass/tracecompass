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
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.handlers;

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.LogLevelType;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.TraceLogLevel;
import org.eclipse.linuxtools.internal.lttng2.ui.Activator;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.IEnableEventsDialog;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.TraceControlDialogFactory;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.messages.Messages;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TargetNodeComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceDomainComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceProviderGroup;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * <p>
 * Base command handler implementation to enable events.
 * </p>
 *
 * @author Bernd Hufmann
 */
public abstract class BaseEnableEventHandler extends BaseControlViewHandler {

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
     * Enables a list of events for given parameters.
     *
     * @param param
     *            - a parameter instance with data for the command execution
     * @param eventNames
     *            - list of event names
     * @param isKernel
     *            - true if kernel domain else false
     * @param filterExpression
     *            - a filter expression
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails for some reason
     */
    public abstract void enableEvents(CommandParameter param, List<String> eventNames, boolean isKernel, String filterExpression, IProgressMonitor monitor) throws ExecutionException;

    /**
     * Enables all syscall events.
     *
     * @param param
     *            - a parameter instance with data for the command execution
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails for some reason
     */
    public abstract void enableSyscalls(CommandParameter param, IProgressMonitor monitor) throws ExecutionException;

    /**
     * Enables a dynamic probe.
     *
     * @param param
     *            - a parameter instance with data for the command execution
     * @param eventName
     *            - a event name
     * @param isFunction
     *            - true for dynamic function entry/return probe else false
     * @param probe
     *            - a dynamic probe information
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails for some reason
     */
    public abstract void enableProbe(CommandParameter param, String eventName, boolean isFunction, String probe, IProgressMonitor monitor) throws ExecutionException;

    /**
     * Enables events using log level
     *
     * @param param
     *            - a parameter instance with data for the command execution
     * @param eventName
     *            - a event name
     * @param logLevelType
     *            - a log level type
     * @param level
     *            - a log level
     * @param filterExpression
     *            - a filter expression
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails for some reason
     */
    public abstract void enableLogLevel(CommandParameter param, String eventName, LogLevelType logLevelType, TraceLogLevel level, String filterExpression, IProgressMonitor monitor) throws ExecutionException;

    /**
     * @param param
     *            - a parameter instance with data for the command execution
     * @return returns the relevant domain (null if domain is not known)
     */
    public abstract TraceDomainComponent getDomain(CommandParameter param);

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

            TargetNodeComponent node = param.getSession().getTargetNode();
            List<ITraceControlComponent> providers = node.getChildren(TraceProviderGroup.class);

            final IEnableEventsDialog dialog = TraceControlDialogFactory.getInstance().getEnableEventsDialog();
            dialog.setTraceProviderGroup((TraceProviderGroup)providers.get(0));
            dialog.setTraceDomainComponent(getDomain(param));

            if (dialog.open() != Window.OK) {
                return null;
            }

            Job job = new Job(Messages.TraceControl_ChangeEventStateJob) {
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    Exception error = null;

                    try {
                        String filter = dialog.getFilterExpression();

                        // Enable tracepoint events
                        if (dialog.isTracepoints()) {
                            if (dialog.isAllTracePoints()) {
                                enableEvents(param, null, dialog.isKernel(), filter, monitor);
                            } else {
                                List<String> eventNames = dialog.getEventNames();
                                if (!eventNames.isEmpty()) {
                                    enableEvents(param, eventNames, dialog.isKernel(), filter, monitor);
                                }
                            }
                        }

                        // Enable syscall events
                        if (dialog.isAllSysCalls()) {
                            enableSyscalls(param, monitor);
                        }

                        // Enable dynamic probe
                        if (dialog.isDynamicProbe() && (dialog.getProbeEventName() != null) && (dialog.getProbeName() != null)) {
                            enableProbe(param, dialog.getProbeEventName(), false, dialog.getProbeName(), monitor);
                        }

                        // Enable dynamic function probe
                        if (dialog.isDynamicFunctionProbe() && (dialog.getFunctionEventName() != null) && (dialog.getFunction() != null)) {
                            enableProbe(param, dialog.getFunctionEventName(), true, dialog.getFunction(), monitor);
                        }

                        // Enable event using a wildcard
                        if (dialog.isWildcard()) {
                            List<String> eventNames = dialog.getEventNames();
                            eventNames.add(dialog.getWildcard());

                            if (!eventNames.isEmpty()) {
                                enableEvents(param, eventNames, dialog.isKernel(), filter, monitor);
                            }
                        }

                        // Enable events using log level
                        if (dialog.isLogLevel()) {
                            enableLogLevel(param, dialog.getLogLevelEventName(), dialog.getLogLevelType(), dialog.getLogLevel(), filter, monitor);
                        }

                    } catch (ExecutionException e) {
                        error = e;
                    }

                    // refresh in all cases
                    refresh(param);

                    if (error != null) {
                        return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.TraceControl_ChangeEventStateFailure, error);
                    }
                    return Status.OK_STATUS;
                }
            };
            job.setUser(true);
            job.schedule();
        } finally {
            fLock.unlock();
        }
        return null;
    }
}
