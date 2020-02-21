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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceDomainType;
import org.eclipse.tracecompass.internal.lttng2.control.ui.Activator;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.ControlView;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs.GetLoggerInfoDialog;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs.TraceControlDialogFactory;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.BaseLoggerComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TargetNodeComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceSessionComponent;
import org.eclipse.ui.IWorkbenchPage;

/**
 * Command handler implementation to assign loggers to a session and enable/configure them.
 * This is done on the trace provider level.
 *
 * @author Bruno Roy
 */
public class AssignLoggerHandler extends BaseControlViewHandler {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The command execution parameter.
     */
    private Parameter fParam;

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        // Make a copy for thread safety
        Parameter tmpParam = null;
        fLock.lock();
        try {
            tmpParam = fParam;
            if (tmpParam == null) {
                return null;
            }
            tmpParam = new Parameter(tmpParam);
        } finally {
            fLock.unlock();
        }
        final Parameter param = tmpParam;

        // Open dialog box to retrieve the session and channel where the events should be enabled in.
        final GetLoggerInfoDialog dialog = TraceControlDialogFactory.getInstance().getGetLoggerInfoDialog();
        dialog.setSessions(param.getSessions());
        dialog.setLoggerDomain(param.getLoggerDomain());

        if (dialog.open() != Window.OK) {
            return null;
        }

        Job job = new Job(Messages.TraceControl_EnableLoggersDialogTitle) {
            @Override
            protected IStatus run(IProgressMonitor monitor) {

                Exception error = null;
                TraceSessionComponent session = dialog.getSession();
                try {
                    List<String> loggerNames = new ArrayList<>();
                    List<BaseLoggerComponent> loggers = param.getLoggers();
                    // Create list of event names
                    for (BaseLoggerComponent logger : loggers) {
                        loggerNames.add(logger.getName());
                    }

                    // enable events on default channel
                    if (dialog.getLogLevel() != null) {
                        session.enableLogLevel(loggerNames, dialog.getLogLevelType(), dialog.getLogLevel(), null, param.getLoggerDomain(), monitor);
                    } else {
                        session.enableEvents(loggerNames, param.getLoggerDomain(), null, null, monitor);
                    }

                } catch (ExecutionException e) {
                    error = e;
                }

                // refresh in all cases
                refresh(new CommandParameter(session));

                if (error != null) {
                    return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.TraceControl_EnableEventsFailure, error);
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
        @NonNull
        ArrayList<@NonNull BaseLoggerComponent> loggers = new ArrayList<>();
        @NonNull
        TraceSessionComponent[] sessions = null;
        TraceDomainType domain = null;

        // Get workbench page for the Control View
        IWorkbenchPage page = getWorkbenchPage();
        if (page == null) {
            return false;
        }

        // Check if one or more session are selected
        ISelection selection = page.getSelection(ControlView.ID);
        if (selection instanceof StructuredSelection) {

            StructuredSelection structered = ((StructuredSelection) selection);
            for (Iterator<?> iterator = structered.iterator(); iterator.hasNext();) {
                Object element = iterator.next();
                if (element instanceof BaseLoggerComponent) {
                    BaseLoggerComponent logger = (BaseLoggerComponent) element;

                    // The loggers have to be the same domain (multiple selection)
                    if (domain == null) {
                        domain = logger.getDomain();
                    } else if (!domain.equals(logger.getDomain())){
                        loggers.clear();
                        break;
                    }

                    // Add BaseLoggerComponents
                    loggers.add(logger);

                    if (sessions == null) {
                        TargetNodeComponent root = (TargetNodeComponent) logger.getParent().getParent().getParent();
                        sessions = root.getSessions();
                    }
                }
            }
        }

        boolean isEnabled = ((!loggers.isEmpty()) && (sessions != null) && (sessions.length > 0));

        if (domain == null) {
            return false;
        }

        fLock.lock();
        try {
            fParam = null;
            if (isEnabled) {
                fParam = new Parameter(NonNullUtils.checkNotNull(sessions), loggers, domain);
            }
        } finally {
            fLock.unlock();
        }
        return isEnabled;
    }

    /**
     * Class containing parameter for the command execution.
     */
    @NonNullByDefault
    private static final class Parameter {

        /**
         * The list of logger components the command is to be executed on.
         */
        private final List<BaseLoggerComponent> fLoggers;

        /**
         * The list of available sessions.
         */
        private final @NonNull TraceSessionComponent[] fSessions;

        /**
         * The domain type ({@link TraceDomainType})
         */
        private final TraceDomainType fDomain;

        /**
         * Constructor
         *
         * @param sessions
         *            a array of trace sessions
         * @param loggers
         *            a lists of loggers to enable
         * @param domain
         *            domain type ({@link TraceDomainType})
         */
        public Parameter(@NonNull TraceSessionComponent[] sessions, List<BaseLoggerComponent> loggers, TraceDomainType domain) {
            fSessions = NonNullUtils.checkNotNull(Arrays.copyOf(sessions, sessions.length));
            fLoggers = new ArrayList<>();
            fLoggers.addAll(loggers);
            fDomain = domain;
        }

        /**
         * Copy constructor
         *
         * @param other
         *            a parameter to copy
         */
        public Parameter(Parameter other) {
            this(other.fSessions, other.fLoggers, other.fDomain);
        }

        /**
         * Return an array of session component
         *
         * @return array of session component
         */
        public TraceSessionComponent[] getSessions() {
            return fSessions;
        }

        /**
         * Return a list of logger component
         *
         * @return list of base logger component
         */
        public List<BaseLoggerComponent> getLoggers() {
            return fLoggers;
        }

        /**
         * Return the logger domain ({@link TraceDomainType})
         *
         * @return - the logger domain ({@link TraceDomainType})
         */
        public TraceDomainType getLoggerDomain() {
            return fDomain;
        }
    }
}
