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
package org.eclipse.linuxtools.lttng.ui.tracecontrol.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.lttng.core.tracecontrol.model.TraceResource;
import org.eclipse.linuxtools.lttng.core.tracecontrol.model.TraceResource.TraceState;
import org.eclipse.linuxtools.lttng.core.tracecontrol.service.ILttControllerService;
import org.eclipse.linuxtools.lttng.ui.LTTngUiPlugin;
import org.eclipse.linuxtools.lttng.ui.tracecontrol.Messages;
import org.eclipse.linuxtools.lttng.ui.tracecontrol.TraceControlConstants;
import org.eclipse.linuxtools.lttng.ui.tracecontrol.subsystems.TraceSubSystem;
import org.eclipse.rse.core.events.ISystemRemoteChangeEvents;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.model.SystemStartHere;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.util.TCFTask;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * <b><u>PauseTrace</u></b>
 * <p>
 * Action implementation to pause a trace.
 * </p>
 */
public class PauseTrace implements IObjectActionDelegate, IWorkbenchWindowActionDelegate, IViewActionDelegate {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private List<TraceResource> fSelectedTraces;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public PauseTrace() {
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
        for (int i = 0; i < fSelectedTraces.size(); i++) {

            final TraceResource trace = (TraceResource) fSelectedTraces.get(i);
            TraceSubSystem subSystem = (TraceSubSystem)trace.getSubSystem();
            
            try {
                final ILttControllerService service = subSystem.getControllerService();

                // Create future task
                @SuppressWarnings("unused")
                Boolean success = new TCFTask<Boolean>() {
                    @Override
                    public void run() {

                        // Setup trace  using Lttng controller service proxy
                        service.pauseTrace(trace.getParent().getParent().getName(), trace.getParent().getName(), trace.getName(), new ILttControllerService.DonePauseTrace() {

                            @Override
                            public void donePauseTrace(IToken token, Exception error, Object str) {
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

                    trace.setTraceState(TraceState.PAUSED);
                    
                    ISystemRegistry registry = SystemStartHere.getSystemRegistry();
                    registry.fireRemoteResourceChangeEvent(ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_CHANGED, trace, trace.getParent(), subSystem, null);
                         
            } catch (Exception e) {
                SystemMessageException sysExp;
                if (e instanceof SystemMessageException) {
                    sysExp = (SystemMessageException)e;
                } else {
                    sysExp = new SystemMessageException(LTTngUiPlugin.getDefault().getMessage(e));    
                }
                SystemBasePlugin.logError(Messages.Lttng_Control_ErrorPause + " (" +  //$NON-NLS-1$
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
     * Set selected traces
     * @param traces
     */
    public void setSelectedTraces(List<TraceResource> traces) {
        fSelectedTraces = traces; 
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
}
