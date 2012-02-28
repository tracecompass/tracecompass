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
import org.eclipse.linuxtools.lttng.ui.views.control.dialogs.EnableKernelEventDialog;
import org.eclipse.linuxtools.lttng.ui.views.control.dialogs.IEnableKernelEventsDialog;
import org.eclipse.linuxtools.lttng.ui.views.control.model.ITraceControlComponent;
import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.KernelProviderComponent;
import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.TargetNodeComponent;
import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.TraceProviderGroup;
import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.TraceSessionComponent;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * <b><u>EnableEventOnSessionHandler</u></b>
 * <p>
 * Command handler implementation to enable events for a known session and default channel 'channel0'
 * (which will be created if doesn't exist).
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
    
    abstract void enableEvents(List<String> eventNames, IProgressMonitor monitor) throws ExecutionException;
    abstract void enableSyscalls(IProgressMonitor monitor) throws ExecutionException;
    abstract void enableProbe(String eventName, String probe, IProgressMonitor monitor) throws ExecutionException; 
    abstract void enableFunctionProbe(String eventName, String probe, IProgressMonitor monitor) throws ExecutionException;
    
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
        List<ITraceControlComponent> kernelProvider =  providers.get(0).getChildren(KernelProviderComponent.class);

        final IEnableKernelEventsDialog dialog = new EnableKernelEventDialog(window.getShell(), (KernelProviderComponent)kernelProvider.get(0));

        if (dialog.open() != Window.OK) {
            return null;
        }

        Job job = new Job(Messages.TraceControl_ChangeEventStateJob) {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                String errorString = null;

                // Enable tracepoint events
                try {
                    if (dialog.isAllTracePoints()) {
                        enableEvents(null, monitor);
                    } else {
                        List<String> eventNames = dialog.getEventNames();
                        if (eventNames.size() > 0) {
                            enableEvents(eventNames, monitor);
                        }
                    }
                } catch (ExecutionException e) {
                    if (errorString == null) {
                        errorString = new String();
                    } 
                    errorString += e.toString() + "\n"; //$NON-NLS-1$
                }

                // Enable syscall events
                try {
                    if (dialog.isAllSysCalls()) {
                        enableSyscalls(monitor);
                    } 
                } catch (ExecutionException e) {
                    if (errorString == null) {
                        errorString = new String();
                    } 
                    errorString += e.toString() + "\n"; //$NON-NLS-1$
                }
                
                // Enable dynamic probe 
                try {
                    if ((dialog.getProbeEventName() != null && dialog.getProbeName() != null)) {
                        enableProbe(dialog.getProbeEventName(), dialog.getProbeName(), monitor);
                    } 
                } catch (ExecutionException e) {
                    if (errorString == null) {
                        errorString = new String();
                    } 
                    errorString += e.toString() + "\n"; //$NON-NLS-1$
                }
                
                // Enable dynamic function probe
                try {
                    if ((dialog.getFunctionEventName() != null) && (dialog.getFunction() != null)) {
                        fSession.enableFunctionProbe(dialog.getFunctionEventName(), dialog.getFunction(), monitor);
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
