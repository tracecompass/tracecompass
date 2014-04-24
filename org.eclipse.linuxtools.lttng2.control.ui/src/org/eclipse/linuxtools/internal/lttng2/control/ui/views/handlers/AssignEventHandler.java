/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
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
package org.eclipse.linuxtools.internal.lttng2.control.ui.views.handlers;

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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.internal.lttng2.control.ui.Activator;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.ControlView;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.dialogs.IGetEventInfoDialog;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.dialogs.TraceControlDialogFactory;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.ITraceControlComponent;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl.BaseEventComponent;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl.KernelProviderComponent;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl.TargetNodeComponent;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl.TraceChannelComponent;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl.TraceSessionComponent;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl.UstProviderComponent;
import org.eclipse.ui.IWorkbenchPage;

/**
 * <p>
 * Command handler implementation to assign events to a session and channel and enable/configure them.
 * This is done on the trace provider level.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class AssignEventHandler extends BaseControlViewHandler {

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

        fLock.lock();
        try {
            // Make a copy for thread safety
            final Parameter param = new Parameter(fParam);

            // Open dialog box to retrieve the session and channel where the events should be enabled in.
            final IGetEventInfoDialog dialog = TraceControlDialogFactory.getInstance().getGetEventInfoDialog();
            dialog.setIsKernel(param.isKernel());
            dialog.setSessions(param.getSessions());

            if (dialog.open() != Window.OK) {
                return null;
            }

            Job job = new Job(Messages.TraceControl_EnableEventsJob) {
                @Override
                protected IStatus run(IProgressMonitor monitor) {

                    Exception error = null;

                    try {
                        List<String> eventNames = new ArrayList<>();
                        List<BaseEventComponent> events = param.getEvents();
                        // Create list of event names
                        for (Iterator<BaseEventComponent> iterator = events.iterator(); iterator.hasNext();) {
                            BaseEventComponent baseEvent = iterator.next();
                            eventNames.add(baseEvent.getName());
                        }

                        TraceChannelComponent channel = dialog.getChannel();
                        if (channel == null) {
                            // enable events on default channel (which will be created by lttng-tools)
                            dialog.getSession().enableEvents(eventNames, param.isKernel(), dialog.getFilterExpression(), monitor);
                        } else {
                            channel.enableEvents(eventNames, dialog.getFilterExpression(), monitor);
                        }

                    } catch (ExecutionException e) {
                        error = e;
                    }

                    // refresh in all cases
                    refresh(new CommandParameter(dialog.getSession()));

                    if (error != null) {
                        return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.TraceControl_EnableEventsFailure, error);
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
        ArrayList<BaseEventComponent> events = new ArrayList<>();
        TraceSessionComponent[] sessions = null;
        Boolean isKernel = null;

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
                if (element instanceof BaseEventComponent) {
                    BaseEventComponent event = (BaseEventComponent) element;
                    ITraceControlComponent provider = event.getParent();

                    // check for kernel or UST provider
                    boolean temp = false;
                    if (provider instanceof KernelProviderComponent) {
                        temp = true;
                    } else if (provider instanceof UstProviderComponent) {
                        temp = false;
                    } else {
                        return false;
                    }
                    if (isKernel == null) {
                        isKernel = Boolean.valueOf(temp);
                    } else {
                        // don't mix events from Kernel and UST provider
                        if (isKernel.booleanValue() != temp) {
                            return false;
                        }
                    }

                    // Add BaseEventComponents
                    events.add(event);

                    if (sessions == null) {
                        TargetNodeComponent  root = (TargetNodeComponent)event.getParent().getParent().getParent();
                        sessions = root.getSessions();
                    }
                }
            }
        }

        boolean isEnabled = ((!events.isEmpty()) && (sessions != null) && (sessions.length > 0));

        // To avoid compiler warnings check for null even if isKernel is always not null when used below
        if (isKernel == null) {
            return false;
        }

        fLock.lock();
        try {
            fParam = null;
            if(isEnabled) {
                fParam = new Parameter(sessions, events, isKernel);
            }
        } finally {
            fLock.unlock();
        }
        return isEnabled;
    }

    /**
     *  Class containing parameter for the command execution.
     */
    private static final class Parameter {

        /**
         * The list of event components the command is to be executed on.
         */
        private final List<BaseEventComponent> fEvents;

        /**
         * The list of available sessions.
         */
        final private TraceSessionComponent[] fSessions;

        /**
         * Flag for indicating Kernel or UST.
         */
        private final boolean fIsKernel;

        /**
         * Constructor
         *
         * @param sessions - a array of trace sessions
         * @param events - a lists of events to enable
         * @param isKernel - domain (true for kernel or UST)
         */
        public Parameter(TraceSessionComponent[] sessions, List<BaseEventComponent> events, boolean isKernel) {
            fSessions = Arrays.copyOf(sessions, sessions.length);
            fEvents = new ArrayList<>();
            fEvents.addAll(events);
            fIsKernel = isKernel;
        }

        /**
         * Copy constructor
         * @param other - a parameter to copy
         */
        public Parameter(Parameter other) {
            this(other.fSessions, other.fEvents, other.fIsKernel);
        }

        public TraceSessionComponent[] getSessions() {
            return fSessions;
        }

        public List<BaseEventComponent> getEvents() {
            return fEvents;
        }

        public boolean isKernel() {
            return fIsKernel;
        }
    }
}
