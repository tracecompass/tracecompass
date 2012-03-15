/**********************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng.ui.views.control.handlers;

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
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.internal.lttng.ui.Activator;
import org.eclipse.linuxtools.internal.lttng.ui.views.control.ControlView;
import org.eclipse.linuxtools.internal.lttng.ui.views.control.Messages;
import org.eclipse.linuxtools.internal.lttng.ui.views.control.dialogs.IGetEventInfoDialog;
import org.eclipse.linuxtools.internal.lttng.ui.views.control.dialogs.TraceControlDialogFactory;
import org.eclipse.linuxtools.internal.lttng.ui.views.control.model.ITraceControlComponent;
import org.eclipse.linuxtools.internal.lttng.ui.views.control.model.impl.BaseEventComponent;
import org.eclipse.linuxtools.internal.lttng.ui.views.control.model.impl.KernelProviderComponent;
import org.eclipse.linuxtools.internal.lttng.ui.views.control.model.impl.TargetNodeComponent;
import org.eclipse.linuxtools.internal.lttng.ui.views.control.model.impl.TraceChannelComponent;
import org.eclipse.linuxtools.internal.lttng.ui.views.control.model.impl.TraceSessionComponent;
import org.eclipse.linuxtools.internal.lttng.ui.views.control.model.impl.UstProviderComponent;
import org.eclipse.ui.IWorkbenchPage;

/**
 * <b><u>EnableEventHandler</u></b>
 * <p>
 * Command handler implementation to assign events to a session and channel and enable/configure them.
 * This is done on the trace provider level.
 * </p>
 */
public class AssignEventHandler extends BaseControlViewHandler {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The list of event components the command is to be executed on. 
     */
    private List<BaseEventComponent> fEvents = new ArrayList<BaseEventComponent>();
    
    /**
     * The list of available sessions.
     */
    private TraceSessionComponent[] fSessions;
    
    /**
     * Flag for indicating Kernel or UST.
     */
    Boolean fIsKernel = null;
    
    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        // Open dialog box to retrieve the session and channel where the events should be enabled in.
        final IGetEventInfoDialog dialog = TraceControlDialogFactory.getInstance().getGetEventInfoDialog();
        dialog.setIsKernel(fIsKernel);
        dialog.setSessions(fSessions);

        if (dialog.open() != Window.OK) {
            return null;
        }

        Job job = new Job(Messages.TraceControl_EnableEventsJob) {
            @Override
            protected IStatus run(IProgressMonitor monitor) {

                String errorString = null;
                try {
                    List<String> eventNames = new ArrayList<String>();
                    // Create list of event names
                    for (Iterator<BaseEventComponent> iterator = fEvents.iterator(); iterator.hasNext();) {
                        BaseEventComponent event = (BaseEventComponent) iterator.next();
                        eventNames.add(event.getName());
                    }

                    TraceChannelComponent channel = dialog.getChannel();
                    if (channel == null) {
                        // enable events on default channel (which will be created by lttng-tools)
                        dialog.getSession().enableEvents(eventNames, fIsKernel, monitor);
                    } else {
                        channel.enableEvents(eventNames, monitor);
                    }

                } catch (ExecutionException e) {
                    errorString = e.toString() + "\n"; //$NON-NLS-1$
                }

                // get session configuration in all cases
                try {
                    dialog.getSession().getConfigurationFromNode(monitor);
                } catch (ExecutionException e) {
                    if (errorString == null) {
                        errorString = new String();
                    }
                    errorString += Messages.TraceControl_ListSessionFailure + ": " + e.toString();  //$NON-NLS-1$ 
                } 

                if (errorString != null) {
                    return new Status(Status.ERROR, Activator.PLUGIN_ID, errorString);
                }
                return Status.OK_STATUS;
            }
        };
        job.setUser(true);
        job.schedule();
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.core.commands.AbstractHandler#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        fEvents.clear();
        fSessions = null;
        fIsKernel = null;

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
                Object element = (Object) iterator.next();
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
                    if (fIsKernel == null) {
                        fIsKernel = Boolean.valueOf(temp);
                    } else {
                        // don't mix events from Kernel and UST provider
                        if (fIsKernel.booleanValue() != temp) {
                            return false;
                        }
                    }

                    // Add BaseEventComponents
                    fEvents.add(event);
                    
                    if (fSessions == null) {
                        TargetNodeComponent  root = (TargetNodeComponent)event.getParent().getParent().getParent();
                        fSessions = root.getSessions();
                    }
                }
            }
        }
        return ((fEvents.size() > 0) && (fSessions != null) && (fSessions.length > 0));
    }
}
