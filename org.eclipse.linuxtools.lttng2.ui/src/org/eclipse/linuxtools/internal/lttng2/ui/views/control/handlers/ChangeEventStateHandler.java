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
import org.eclipse.linuxtools.internal.lttng2.core.control.model.TraceEnablement;
import org.eclipse.linuxtools.internal.lttng2.ui.Activator;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.ControlView;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.messages.Messages;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceChannelComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceEventComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceSessionComponent;
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
abstract public class ChangeEventStateHandler extends BaseControlViewHandler {

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
    abstract protected TraceEnablement getNewState();

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * Change the state
     * @param channel - channel of events to be enabled
     * @param eventNames - list event names
     * @param monitor - a progress monitor
     * @throws ExecutionException If the command fails
     */
    abstract protected void changeState(TraceChannelComponent channel, List<String> eventNames, IProgressMonitor monitor) throws ExecutionException;

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
                        boolean isAll = false;
                        if (param.getChannel() != null) {
                            session = param.getChannel().getSession();
                            List<String> eventNames = new ArrayList<String>();
                            List<TraceEventComponent> events = param.getEvents();

                            for (Iterator<TraceEventComponent> iterator = events.iterator(); iterator.hasNext();) {
                                // Enable/disable all selected channels which are disabled
                                TraceEventComponent traceEvent = iterator.next();

                                // Workaround for wildcard handling in lttng-tools
                                if ("*".equals(traceEvent.getName())) { //$NON-NLS-1$
                                    isAll = true;
                                } else {
                                    eventNames.add(traceEvent.getName());
                                }
                            }
                            if (isAll) {
                                changeState(param.getChannel(), null, monitor);
                            }

                            if (!eventNames.isEmpty()) {
                                changeState(param.getChannel(), eventNames, monitor);
                            }

                            for (Iterator<TraceEventComponent> iterator = events.iterator(); iterator.hasNext();) {
                                // Enable all selected channels which are disabled
                                TraceEventComponent ev = iterator.next();
                                ev.setState(getNewState());
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
        List<TraceEventComponent> events = new ArrayList<TraceEventComponent>();

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

                    // Enable command only for events of same session, same channel and domain
                    if ((!sessionName.equals(event.getSessionName())) ||
                        (!channelName.equals(event.getChannelName())) ||
                        (channel.isKernel() != event.isKernel())) {
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
                fParam = new Parameter(channel, events);
            }
        } finally {
            fLock.unlock();
        }
        return isEnabled;
    }

    /**
     *  Class containing parameter for the command execution.
     */
    static protected class Parameter {
        /**
         * Channel component reference.
         */
        final private TraceChannelComponent fChannel;
        /**
         * The list of kernel channel components the command is to be executed on.
         */
        final private List<TraceEventComponent> fEvents = new ArrayList<TraceEventComponent>();

        /**
         * Constructor
         * @param channel - a channel component
         * @param events - a list of event components
         */
        public Parameter(TraceChannelComponent channel, List<TraceEventComponent> events) {
            fChannel = channel;
            fEvents.addAll(events);
        }

        /**
         * Copy constructor
         * @param other - a parameter to copy
         */
        public Parameter(Parameter other) {
            this(other.fChannel, other.fEvents);
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
    }
}
