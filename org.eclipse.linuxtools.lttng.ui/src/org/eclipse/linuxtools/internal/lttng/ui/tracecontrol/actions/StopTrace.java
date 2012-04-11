/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Polytechnique Montréal - Initial API and implementation
 *   Bernd Hufmann - Productification, enhancements and fixes
 *   
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.TraceResource;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.TraceResource.TraceState;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.config.TraceConfig;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.service.ILttControllerService;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.utility.LiveTraceManager;
import org.eclipse.linuxtools.internal.lttng.ui.Activator;
import org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.Messages;
import org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.TraceControlConstants;
import org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.subsystems.TraceSubSystem;
import org.eclipse.rse.core.events.ISystemRemoteChangeEvents;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.model.SystemStartHere;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.util.TCFTask;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * <b><u>StopTrace</u></b>
 * <p>
 * Action implementation to stop a trace which deallocates all resources on the remote system.
 * </p>
 */
public class StopTrace implements IObjectActionDelegate, IWorkbenchWindowActionDelegate, IViewActionDelegate {
    
    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private List<TraceResource> fSelectedTraces;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    
    public StopTrace() {
        fSelectedTraces = new ArrayList<TraceResource>();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
     */
    @Override
    public void setActivePart(IAction arg0, IWorkbenchPart arg1) {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @Override
    public void run(IAction arg0) {
        int size = fSelectedTraces.size();
        for (int i = 0; i < size; i++) {

            final TraceResource trace = (TraceResource) fSelectedTraces.get(i);
            TraceSubSystem subSystem = (TraceSubSystem)trace.getSubSystem();

            try {
                final ILttControllerService service = subSystem.getControllerService();
                
                TraceConfig traceConfig = trace.getTraceConfig();
                if (traceConfig != null && traceConfig.getMode() == TraceConfig.FLIGHT_RECORDER_MODE) {
                	setupLocation(service, trace, traceConfig);
                }
                
                // Create future task
                @SuppressWarnings("unused")
                Boolean success = new TCFTask<Boolean>() {
                    @Override
                    public void run() {

                        // Setup trace  using Lttng controller service proxy
                        service.destroyTrace(trace.getParent().getParent().getName(), trace.getParent().getName(), trace.getName(), new ILttControllerService.DoneDestroyTrace() {

                            @Override
                            public void doneDestroyTrace(IToken token, Exception error, Object str) {
                                if (error != null) {
                                    // Notify with error
                                    error(error);
                                    return;
                                }

                                // Notify about success
                                done(Boolean.valueOf(true));
                            }
                        });
                    }}.get(TraceControlConstants.DEFAULT_TCF_TASK_TIMEOUT, TimeUnit.SECONDS);
                    
                    trace.setTraceState(TraceState.STOPPED);

                    if (trace.getTraceConfig() != null) {
                        LiveTraceManager.setLiveTrace(trace.getTraceConfig().getTracePath(), false);
                    }

                    ISystemRegistry registry = SystemStartHere.getSystemRegistry();
                    registry.fireRemoteResourceChangeEvent(ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_CHANGED, trace, trace.getParent(), subSystem, null);

            } catch (Exception e) {
                SystemMessageException sysExp;
                if (e instanceof SystemMessageException) {
                    sysExp = (SystemMessageException)e;
                } else {
                    sysExp = new SystemMessageException(Activator.getDefault().getMessage(e));    
                }
                SystemBasePlugin.logError(Messages.Lttng_Control_ErrorStop + " (" +  //$NON-NLS-1$
                        Messages.Lttng_Resource_Trace + ": "  + trace.getName() + ")", sysExp); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        
        if (selection instanceof IStructuredSelection) {
            fSelectedTraces.clear();

            // store the selected targets to be used when running
            Iterator<IStructuredSelection> theSet = ((IStructuredSelection) selection).iterator();
            while (theSet.hasNext()) {
                Object obj = theSet.next();
                if (obj instanceof TraceResource) {
                    fSelectedTraces.add((TraceResource)obj);
                }
            }
        }

    }

    /**
     * Returns the active workbench shell of this plug-in.
     * 
     * @return active workbench shell.
     */
    protected Shell getShell() {
        return SystemBasePlugin.getActiveWorkbenchShell();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
     */
    @Override
    public void init(IWorkbenchWindow window) {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
     */
    @Override
    public void dispose() {
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    @Override
    public void init(IViewPart view) {
    }
    
    /*
     * Setup the trace location. Only flight recorder channels are written when trace is stopped.
     */
    private void setupLocation(final ILttControllerService service, final TraceResource trace, final TraceConfig traceConfig) throws Exception {
    	boolean success = false;
    	
        if (traceConfig.isNetworkTrace()) {

            // Create future task
            success = new TCFTask<Boolean>() {
                @Override
                public void run() {

                    // Setup trace transport using Lttng controller service proxy
                    service.writeTraceNetwork(trace.getParent().getParent().getName(), 
                            trace.getParent().getName(), 
                            traceConfig.getTraceName(), 
                            traceConfig.getTracePath(), 
                            traceConfig.getNumChannel(), 
                            traceConfig.getIsAppend(), 
                            true, // write only flight recorder channels 
                            false, 
                            new ILttControllerService.DoneWriteTraceNetwork() {

                        @Override
                        public void doneWriteTraceNetwork(IToken token, Exception error, Object str) {
                            if (error != null) {
                                // Notify with error
                                error(error);
                                return;
                            }

                            // Notify about success
                            done(Boolean.valueOf(true));
                        }
                    });
                }}.get(TraceControlConstants.DEFAULT_TCF_TASK_TIMEOUT, TimeUnit.SECONDS);
            
        } else {
            
            // Create future task
            success = new TCFTask<Boolean>() {
                @Override
                public void run() {

                    // Setup trace transport using Lttng controller service proxy
                    service.writeTraceLocal(trace.getParent().getParent().getName(), 
                            trace.getParent().getName(), 
                            traceConfig.getTraceName(), 
                            traceConfig.getTracePath(), 
                            traceConfig.getNumChannel(),
                            traceConfig.getIsAppend(), 
                            true, // write only flight recorder channels 
                            false, 
                            new ILttControllerService.DoneWriteTraceLocal() {

                        @Override
                        public void doneWriteTraceLocal(IToken token, Exception error, Object str) {
                            if (error != null) {
                                // Notify with error
                                error(error);
                                return;
                            }

                            // Notify about success
                            done(Boolean.valueOf(true));
                        }
                    });
                }}.get(TraceControlConstants.DEFAULT_TCF_TASK_TIMEOUT, TimeUnit.SECONDS);
        }
        
        if (success) {
            //FIXME: wait 2 seconds to allow time for channels to be written before destroying the trace
            try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
    }


}
