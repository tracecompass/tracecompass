/**********************************************************************
 * Copyright (c) 2012, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Bruno Roy - Bug 486658: Support for enabling disabled events of types:
 *      kernel dynamic probe, function probe, ust loglevel/loglevel-only
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
import org.eclipse.tracecompass.internal.lttng2.control.core.model.LogLevelType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceEnablement;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceEventType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceLogLevel;
import org.eclipse.tracecompass.internal.lttng2.control.ui.Activator;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.ControlView;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceChannelComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceEventComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceProbeEventComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceSessionComponent;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * <p>
 * Base Command handler implementation to enable or disabling a trace channel.
 * </p>
 *
 * @author Bernd Hufmann
 */
public abstract class ChangeEventStateHandler extends BaseControlViewHandler {

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
     * @param channel - channel of events to be enabled
     * @param eventNames - list event names
     * @param logLevel - the log level
     * @param logLevelType - the log level type
     * @param eventType  - the event type ({@link TraceEventType})
     * @param probe - The address or symbol of the probe or function
     * @param monitor - a progress monitor
     * @throws ExecutionException If the command fails
     */
    protected abstract void changeState(TraceChannelComponent channel, List<String> eventNames, TraceLogLevel logLevel, LogLevelType logLevelType,  TraceEventType eventType, String probe, IProgressMonitor monitor) throws ExecutionException;

    private void changeState(final Parameter param, List<String> eventNames, IProgressMonitor monitor) throws ExecutionException {
        changeState(param.getChannel(), eventNames, param.getLogLevel(), param.getLogLevelType(), param.getEventType(), param.getProbe(), monitor);
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

        if (window == null) {
            return false;
        }

        fLock.lock();
        try {

            final Parameter param = new Parameter(fParam);

            Job job = new Job(Messages.TraceControl_ChangeChannelStateJob) {
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    Exception error = null;

                    TraceSessionComponent session = null;

                    try {
                        if (param.getChannel() != null) {
                            session = param.getChannel().getSession();
                            List<String> eventNames = new ArrayList<>();
                            List<TraceEventComponent> events = param.getEvents();

                            for (TraceEventComponent traceEvent : events) {
                                if ("*".equals(traceEvent.getName())) { //$NON-NLS-1$
                                    changeState(param, null, monitor);
                                } else {
                                    eventNames.add(traceEvent.getName());
                                }
                            }

                            if (!eventNames.isEmpty()) {
                                changeState(param, eventNames, monitor);
                            }

                            for (TraceEventComponent traceEvent : events) {
                                traceEvent.setState(getNewState());
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

    @Override
    public boolean isEnabled() {
        // Get workbench page for the Control View
        IWorkbenchPage page = getWorkbenchPage();
        if (page == null) {
            return false;
        }

        // Check if one or more session are selected
        ISelection selection = page.getSelection(ControlView.ID);

        TraceChannelComponent channel = null;
        TraceLogLevel logLevel = null;
        LogLevelType logLevelType = null;
        List<TraceEventComponent> events = new ArrayList<>();
        TraceEventType eventType = null;
        String probe = null;

        if (selection instanceof StructuredSelection) {
            StructuredSelection structered = ((StructuredSelection) selection);
            String sessionName = null;
            String channelName = null;

            for (Iterator<?> iterator = structered.iterator(); iterator.hasNext();) {
                Object element = iterator.next();

                if (element instanceof TraceEventComponent) {

                    TraceEventComponent event = (TraceEventComponent) element;
                    if (sessionName == null) {
                        sessionName = String.valueOf(event.getSessionName());
                    }

                    if (channel == null) {
                        channel = (TraceChannelComponent)event.getParent();
                    }

                    if (channelName == null) {
                        channelName = event.getChannelName();
                    }

                    if (logLevel == null) {
                        logLevel = event.getLogLevel();
                    }

                    if (logLevelType == null) {
                        logLevelType = event.getLogLevelType();
                    }

                    // The events have to be the same type
                    if (eventType == null) {
                        eventType = event.getEventType();
                    } else if (!eventType.equals(event.getEventType())) {
                        events.clear();
                        break;
                    }

                    // The probe or address
                    if (probe == null) {
                        if (event instanceof TraceProbeEventComponent) {
                            probe = ((TraceProbeEventComponent) event).getProbeString();
                        }
                    } else {
                        events.clear();
                        break;
                    }

                    // Enable command only for events of same session, same channel and domain
                    if ((!sessionName.equals(event.getSessionName())) ||
                        (!channelName.equals(event.getChannelName())) ||
                        (!channel.getDomain().equals(event.getDomain()))) {
                        events.clear();
                        break;
                    }

                    // Enable command only for events of same loglevel and loglevel type
                    // (not applicable if the loglevel type is none)
                    if ((!event.getLogLevelType().equals(LogLevelType.LOGLEVEL_NONE)) &&
                        ((!logLevelType.equals(event.getLogLevelType())) ||
                        (!logLevel.equals(event.getLogLevel())))) {
                        events.clear();
                        break;
                    }

                    if ((event.getState() != getNewState())) {
                        events.add(event);
                    }
                }
            }
        }
        boolean isEnabled = !events.isEmpty();

        fLock.lock();
        try {
            fParam = null;
            if (isEnabled) {
                fParam = new Parameter(channel, events, logLevel, logLevelType, eventType, probe);
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
         * Channel component reference.
         */
        private final TraceChannelComponent fChannel;
        /**
         * The list of kernel channel components the command is to be executed on.
         */
        private final List<TraceEventComponent> fEvents = new ArrayList<>();
        /**
         * The log level.
         */
        private final TraceLogLevel fLogLevel;
        /**
         * The log level type.
         */
        private final LogLevelType fLogLevelType;
        /**
         * The event type.
         */
        private final TraceEventType fEventType;
        /**
         * The probe or address.
         */
        private final String fProbe;

        /**
         * Constructor
         * @param channel - a channel component
         * @param events - a list of event components
         * @param logLevel - the log level
         * @param logLevelType - the log level type
         * @param eventType - the event type
         * @param probe - the probe r address
         */
        public Parameter(TraceChannelComponent channel, List<TraceEventComponent> events, TraceLogLevel logLevel, LogLevelType logLevelType, TraceEventType eventType, String probe) {
            fChannel = channel;
            fEvents.addAll(events);
            fLogLevel = logLevel;
            fLogLevelType = logLevelType;
            fEventType = eventType;
            fProbe = probe;
        }

        /**
         * Copy constructor
         * @param other - a parameter to copy
         */
        public Parameter(Parameter other) {
            this(other.fChannel, other.fEvents, other.fLogLevel, other.fLogLevelType, other.fEventType, other.fProbe);
        }

        /**
         * @return the trace channel component.
         */
        public TraceChannelComponent getChannel() {
            return fChannel;
        }

        /**
         * @return a list of trace event components.
         */
        public List<TraceEventComponent> getEvents() {
            return fEvents;
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
        public TraceLogLevel getLogLevel() {
            return fLogLevel;
        }

        /**
         * @return the event type.
         */
        public TraceEventType getEventType() {
            return fEventType;
        }

        /**
         * @return the probe or address.
         */
        public String getProbe() {
            return fProbe;
        }
    }
}
