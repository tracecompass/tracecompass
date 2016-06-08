/**********************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bruno Roy - Initial API and implementation
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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceEnablement;
import org.eclipse.tracecompass.internal.lttng2.control.ui.Activator;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.ControlView;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceDomainComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceLoggerComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceSessionComponent;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Base Command handler implementation to enable or disabling a trace logger.
 *
 * @author Bruno Roy
 */
public abstract class ChangeLoggerStateHandler extends BaseControlViewHandler {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The command execution parameter.
     */
    protected Parameter fParam;

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * @return the new state to set
     */
    protected abstract TraceEnablement getNewState();

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * Change the state
     *
     * @param domain
     *            domain of events to be enabled
     * @param loggerNames
     *            list logger names
     * @param monitor
     *            a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    protected abstract void changeState(TraceDomainComponent domain, List<String> loggerNames, IProgressMonitor monitor) throws ExecutionException;

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

        if (window == null) {
            return false;
        }

        fLock.lock();
        try {

            final Parameter param = new Parameter(fParam);

            Job job = new Job(Messages.TraceControl_ChangeLoggerStateJob) {
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    Exception error = null;

                    TraceSessionComponent session = null;

                    try {
                        boolean isAll = false;
                        if (param.getLoggers() != null) {
                            session = param.getDomain().getSession();
                            List<String> loggerNames = new ArrayList<>();
                            List<TraceLoggerComponent> loggers = param.getLoggers();

                            for (Iterator<TraceLoggerComponent> iterator = loggers.iterator(); iterator.hasNext();) {
                                // Enable/disable all selected channels which are disabled
                                TraceLoggerComponent traceLogger = iterator.next();

                                // Workaround for wildcard handling in lttng-tools
                                if ("*".equals(traceLogger.getName())) { //$NON-NLS-1$
                                    isAll = true;
                                } else {
                                    loggerNames.add(traceLogger.getName());
                                }
                            }
                            if (isAll) {
                                changeState(param.getDomain(), null, monitor);
                            }

                            if (!loggerNames.isEmpty()) {
                                changeState(param.getDomain(), loggerNames, monitor);
                            }

                            for (Iterator<TraceLoggerComponent> iterator = loggers.iterator(); iterator.hasNext();) {
                                // Enable all selected channels which are disabled
                                TraceLoggerComponent lg = iterator.next();
                                lg.setState(getNewState());
                            }
                        }
                    } catch (ExecutionException e) {
                        error = e;
                    }

                    if (session != null) {
                        // In all cases notify listeners
                        session.fireComponentChanged(session);
                    }

                    if (error != null) {
                        return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.TraceControl_ChangeLoggerStateFailure, error);
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

    @Override
    public boolean isEnabled() {
        // Get workbench page for the Control View
        IWorkbenchPage page = getWorkbenchPage();
        if (page == null) {
            return false;
        }

        // Check if one or more session are selected
        ISelection selection = page.getSelection(ControlView.ID);

        TraceDomainComponent domain = null;
        List<TraceLoggerComponent> loggers = new ArrayList<>();

        if (selection instanceof StructuredSelection) {
            StructuredSelection structered = ((StructuredSelection) selection);
            String sessionName = null;
            String domainName = null;

            for (Iterator<?> iterator = structered.iterator(); iterator.hasNext();) {
                Object element = iterator.next();

                if (element instanceof TraceLoggerComponent) {

                    TraceLoggerComponent logger = (TraceLoggerComponent) element;
                    if (sessionName == null) {
                        sessionName = String.valueOf(logger.getSessionName());
                    }

                    if (domain == null) {
                        domain = (TraceDomainComponent) logger.getParent();
                    }

                    if (domainName == null) {
                        domainName = logger.getDomain().name();
                    }

                    // Enable command only for loggers of same session, same channel and domain
                    if ((!sessionName.equals(logger.getSessionName())) ||
                        (!domain.getName().equals(logger.getDomain().name()))) {
                        loggers.clear();
                        break;
                    }

                    if ((logger.getState() != getNewState())) {
                        loggers.add(logger);
                    }
                }
            }
        }
        boolean isEnabled = !loggers.isEmpty();

        fLock.lock();
        try {
            fParam = null;
            if (isEnabled) {
                fParam = new Parameter(domain, loggers);
            }
        } finally {
            fLock.unlock();
        }
        return isEnabled;
    }

    /**
     *  Class containing parameter for the command execution.
     */
    protected static class Parameter {
        /**
         * Domain component reference.
         */
        private final TraceDomainComponent fDomain;

        /**
         * The list of kernel channel components the command is to be executed on.
         */
        private final List<TraceLoggerComponent> fLoggers = new ArrayList<>();

        /**
         * Constructor
         *
         * @param domain
         *            a domain component
         * @param loggers
         *            a list of logger components
         */
        public Parameter(TraceDomainComponent domain, List<TraceLoggerComponent> loggers) {
            fDomain = domain;
            fLoggers.addAll(loggers);
        }

        /**
         * Copy constructor
         *
         * @param other
         *            a parameter to copy
         */
        public Parameter(Parameter other) {
            this(other.fDomain, other.fLoggers);
        }

        /**
         * @return the trace domain component.
         */
        public TraceDomainComponent getDomain() {
            return fDomain;
        }

        /**
         * @return a list of trace logger components.
         */
        public List<TraceLoggerComponent> getLoggers() {
            return fLoggers;
        }
    }
}
