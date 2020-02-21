/**********************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.tracecompass.internal.lttng2.control.core.model.ITraceLogLevel;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.LogLevelType;
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
     * @param logLevel
     *            the log level
     * @param logLevelType
     *            the log level type
     * @param monitor
     *            a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    protected abstract void changeState(TraceDomainComponent domain, List<String> loggerNames, ITraceLogLevel logLevel, LogLevelType logLevelType, IProgressMonitor monitor) throws ExecutionException;

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
                        if (param.getLoggers() != null) {
                            session = param.getDomain().getSession();
                            List<String> loggerNames = new ArrayList<>();
                            List<TraceLoggerComponent> loggers = param.getLoggers();

                            for (TraceLoggerComponent logger : loggers) {
                                if ("*".equals(logger.getName())) { //$NON-NLS-1$
                                    changeState(param.getDomain(), null, param.getLogLevel(), param.getLogLevelType(), monitor);
                                } else {
                                    loggerNames.add(logger.getName());
                                }
                            }

                            if (!loggerNames.isEmpty()) {
                                changeState(param.getDomain(), loggerNames, param.getLogLevel(), param.getLogLevelType(), monitor);
                            }

                            for (TraceLoggerComponent logger : loggers) {
                                // Enable all selected channels which are disabled
                                logger.setState(getNewState());
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
        ITraceLogLevel logLevel = null;
        LogLevelType logLevelType = null;
        List<TraceLoggerComponent> loggers = new ArrayList<>();

        if (selection instanceof StructuredSelection) {
            StructuredSelection structered = ((StructuredSelection) selection);
            String sessionName = null;
            String domainName = null;

            for (Iterator<?> iterator = structered.iterator(); iterator.hasNext();) {
                Object element = iterator.next();

                if (element instanceof TraceLoggerComponent) {

                    TraceLoggerComponent logger = (TraceLoggerComponent) element;

                    // This is a work-around the a bug that destroys all the sessions, this bug was fixed in LTTng 2.8.1
                    // https://github.com/lttng/lttng-tools/pull/75/commits/aae621cf9d9a078f40415495a77e07079690fea1
                    if (logger.getName().equals("*") && !logger.getTargetNode().isVersionSupported("2.8.1")) { //$NON-NLS-1$ //$NON-NLS-2$
                        return false;
                    }

                    if (sessionName == null) {
                        sessionName = String.valueOf(logger.getSessionName());
                    }

                    if (domain == null) {
                        domain = (TraceDomainComponent) logger.getParent();
                    }

                    if (domainName == null) {
                        domainName = logger.getDomain().name();
                    }

                    if (logLevel == null) {
                        logLevel = logger.getLogLevel();
                    }

                    if (logLevelType == null) {
                        logLevelType = logger.getLogLevelType();
                    }

                    // Enable command only for loggers of same session and domain.
                    // This is because when using the lttng enable-event command to re-enable disabled
                    // loggers we need to pass all the options of the logger, this means that if two loggers are from
                    // different session or domain they need to be enabled in two different lttng commands. At this moment,
                    // it is simpler to disable the context menu. This issue will be addressed later as an enhancement.
                    if ((!sessionName.equals(logger.getSessionName())) ||
                        (!domain.getName().equalsIgnoreCase(logger.getDomain().name()))) {
                        loggers.clear();
                        break;
                    }

                    // Enable command only for loggers of same loglevel and loglevel type
                    // Same reason as explained above.
                    if ((!logLevel.equals(logger.getLogLevel())) ||
                        (!logLevelType.equals(logger.getLogLevelType()))) {
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
                fParam = new Parameter(domain, loggers, logLevel, logLevelType);
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
         * The log level.
         */
        private final ITraceLogLevel fLogLevel;
        /**
         * The log level type.
         */
        private final LogLevelType fLogLevelType;

        /**
         * Constructor
         *
         * @param domain
         *            a domain component
         * @param loggers
         *            a list of logger components
         * @param logLevel
         *            the log level
         * @param logLevelType
         *            the log level type
         */
        public Parameter(TraceDomainComponent domain, List<TraceLoggerComponent> loggers, ITraceLogLevel logLevel, LogLevelType logLevelType) {
            fDomain = domain;
            fLoggers.addAll(loggers);
            fLogLevel = logLevel;
            fLogLevelType = logLevelType;
        }

        /**
         * Copy constructor
         *
         * @param other
         *            a parameter to copy
         */
        public Parameter(Parameter other) {
            this(other.fDomain, other.fLoggers, other.fLogLevel, other.fLogLevelType);
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

        /**
         * @return the log level type.
         */
        public LogLevelType getLogLevelType() {
            return fLogLevelType;
        }

        /**
         * @return the log level.
         */
        public ITraceLogLevel getLogLevel() {
            return fLogLevel;
        }
    }
}
