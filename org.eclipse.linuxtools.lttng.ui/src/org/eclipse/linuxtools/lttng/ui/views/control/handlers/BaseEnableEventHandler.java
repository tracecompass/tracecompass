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
package org.eclipse.linuxtools.lttng.ui.views.control.handlers;

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.lttng.ui.LTTngUiPlugin;
import org.eclipse.linuxtools.lttng.ui.views.control.Messages;
import org.eclipse.linuxtools.lttng.ui.views.control.dialogs.IEnableEventsDialog;
import org.eclipse.linuxtools.lttng.ui.views.control.dialogs.TraceControlDialogFactory;
import org.eclipse.linuxtools.lttng.ui.views.control.model.ITraceControlComponent;
import org.eclipse.linuxtools.lttng.ui.views.control.model.LogLevelType;
import org.eclipse.linuxtools.lttng.ui.views.control.model.TraceLogLevel;
import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.TargetNodeComponent;
import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.TraceDomainComponent;
import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.TraceProviderGroup;
import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.TraceSessionComponent;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * <b><u>EnableEventOnSessionHandler</u></b>
 * <p>
 * Base command handler implementation to enable events.
 * </p>
 */
abstract public class BaseEnableEventHandler extends BaseControlViewHandler {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The session component the command is to be executed on. 
     */
    protected TraceSessionComponent fSession = null;

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * Enables a list of events for given parameters.
     * @param eventNames - list of event names
     * @param isKernel - true if kernel domain else false
     * @param monitor - a progress monitor
     * @throws ExecutionException
     */
    abstract public void enableEvents(List<String> eventNames, boolean isKernel, IProgressMonitor monitor) throws ExecutionException;
    /**
     * Enables all syscall events.
     * @param monitor - a progress monitor
     * @throws ExecutionException
     */
    abstract public void enableSyscalls(IProgressMonitor monitor) throws ExecutionException;
    
    /**
     * Enables a dynamic probe.
     * @param eventName - a event name
     * @param isFunction - true for dynamic function entry/return probe else false
     * @param probe - a dynamic probe information
     * @param monitor - a progress monitor
     * @throws ExecutionException
     */
    abstract public void enableProbe(String eventName, boolean isFunction, String probe, IProgressMonitor monitor) throws ExecutionException;
    
    /**
     * Enables events using log level
     * @param eventName - a event name
     * @param logLevelType - a log level type 
     * @param level - a log level 
     * @param monitor - a progress monitor  
     * @throws ExecutionException
     */    
    abstract public void enableLogLevel(String eventName, LogLevelType logLevelType, TraceLogLevel level, IProgressMonitor monitor) throws ExecutionException;
    
    /**
     * @return returns the relevant domain (null if domain is not known)
     */
    abstract TraceDomainComponent getDomain();

    /*
     * (non-Javadoc)
     * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

        if (window == null) {
            return false;
        }

        TargetNodeComponent node = fSession.getTargetNode();
        List<ITraceControlComponent> providers = node.getChildren(TraceProviderGroup.class);

        final IEnableEventsDialog dialog = TraceControlDialogFactory.getInstance().getEnableEventsDialog();
        dialog.setTraceProviderGroup((TraceProviderGroup)providers.get(0));
        dialog.setTraceDomainComponent(getDomain());

        if (dialog.open() != Window.OK) {
            return null;
        }

        Job job = new Job(Messages.TraceControl_ChangeEventStateJob) {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                String errorString = null;

                try {
                    // Enable tracepoint events
                    if (dialog.isTracepoints()) {
                        if (dialog.isAllTracePoints()) {
                            enableEvents(null, dialog.isKernel(), monitor);
                        } else {
                            List<String> eventNames = dialog.getEventNames();
                            if (eventNames.size() > 0) {
                                enableEvents(eventNames, dialog.isKernel(), monitor);
                            }
                        }
                    }

                    // Enable syscall events
                    if (dialog.isAllSysCalls()) {
                        if (dialog.isAllSysCalls()) {
                            enableSyscalls(monitor);
                        } 
                    }

                    // Enable dynamic probe
                    if (dialog.isDynamicProbe()) {
                        if ((dialog.getProbeEventName() != null && dialog.getProbeName() != null)) {
                            enableProbe(dialog.getProbeEventName(), false, dialog.getProbeName(), monitor);
                        } 
                    }

                    // Enable dynamic function probe
                    if (dialog.isDynamicFunctionProbe()) {
                        if ((dialog.getFunctionEventName() != null) && (dialog.getFunction() != null)) {
                            enableProbe(dialog.getFunctionEventName(), true, dialog.getFunction(), monitor);
                        } 
                    }

                    // Enable event using a wildcard
                    if (dialog.isWildcard()) {
                        List<String> eventNames = dialog.getEventNames();
                        eventNames.add(dialog.getWildcard());

                        if (eventNames.size() > 0) {
                            enableEvents(eventNames, dialog.isKernel(), monitor);
                        }
                    }
                    
                    // Enable events using log level
                    if (dialog.isLogLevel()) {
                        enableLogLevel(dialog.getLogLevelEventName(), dialog.getLogLevelType(), dialog.getLogLevel(), monitor);
                    }

                } catch (ExecutionException e) {
                    if (errorString == null) {
                        errorString = new String();
                    } 
                    errorString += e.toString() + "\n"; //$NON-NLS-1$
                }

                // get session configuration in all cases
                try {
                    fSession.getConfigurationFromNode(monitor);
                } catch (ExecutionException e) {
                    if (errorString == null) {
                        errorString = new String();
                    }
                    errorString += Messages.TraceControl_ListSessionFailure + ": " + e.toString();  //$NON-NLS-1$ 
                } 

                if (errorString != null) {
                    return new Status(Status.ERROR, LTTngUiPlugin.PLUGIN_ID, errorString);
                }
                return Status.OK_STATUS;
            }
        };
        job.setUser(true);
        job.schedule();

        return null;
    }
}
