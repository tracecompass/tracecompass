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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.TargetResource;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.TraceResource;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.TraceResource.TraceState;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.config.TraceConfig;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.service.ILttControllerService;
import org.eclipse.linuxtools.internal.lttng.ui.Activator;
import org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.Messages;
import org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.TraceControlConstants;
import org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.dialogs.NewTraceDialog;
import org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.subsystems.TraceSubSystem;
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
 * <b><u>CreateNewTrace</u></b>
 * <p>
 * Action implementation to create a new trace.
 * </p>
 */
public class CreateNewTrace implements IObjectActionDelegate, IWorkbenchWindowActionDelegate, IViewActionDelegate {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final List<TargetResource> fSelectedFiles;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor for CreateNewTrace.
     */
    public CreateNewTrace() {
        fSelectedFiles = new ArrayList<TargetResource>();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.
     * action.IAction, org.eclipse.ui.IWorkbenchPart)
     */
    @Override
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    }

    /**
     * Returns the first selected target resource.
     * 
     * @return first selected target resource
     */
    protected TargetResource getFirstSelectedTarget() {
        if (fSelectedFiles.size() > 0) {
            return fSelectedFiles.get(0);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @Override
    public void run(IAction action) {
        Shell shell = getShell();
        final TargetResource targetResource = getFirstSelectedTarget();
        TraceSubSystem subSystem = (TraceSubSystem) targetResource.getSubSystem();
        NewTraceDialog dialog = new NewTraceDialog(shell, subSystem, targetResource);

        final TraceConfig traceConfig = dialog.open();

        if (traceConfig == null) {
            return;
        }

        try {
            final ILttControllerService service = subSystem.getControllerService();

            TraceResource trace = new TraceResource(targetResource.getSubSystem(), service);
            trace.setName(traceConfig.getTraceName());
            trace.setParent(targetResource);
            trace.setTraceConfig(traceConfig);

            if (targetResource.isUst()) {
                boolean ok = setupUstLocation(service, targetResource, traceConfig);
                if (!ok) {
                    return;
                }
            }

            trace.setupTrace();

            if (!targetResource.isUst()) {

                // Enable all channels by default
                trace.setChannelEnable(TraceControlConstants.Lttng_Control_AllChannels, true);

                // Set overwrite mode for all channels according to user
                // selection (true for flight recorder, false for normal)
                trace.setChannelOverwrite(TraceControlConstants.Lttng_Control_AllChannels, traceConfig.getMode() == TraceConfig.FLIGHT_RECORDER_MODE);

                // Set channel timer for all channels
                final long period = 1000;
                trace.setChannelTimer(TraceControlConstants.Lttng_Control_AllChannels, period);

                // Set subbuffer size for all channels
                final long subbufSize = 16384;
                trace.setChannelSubbufSize(TraceControlConstants.Lttng_Control_AllChannels, subbufSize);

                // Set number of subbuffers for all channels
                final long subbufNum = 2;
                trace.setChannelSubbufNum(TraceControlConstants.Lttng_Control_AllChannels, subbufNum);
            }

            if (traceConfig.isNetworkTrace()) {

                File newDir = new File(traceConfig.getTracePath());
                if (!newDir.exists()) {
                    boolean created = newDir.mkdirs();
                    if (!created) {
                        throw new Exception(Messages.Lttng_Control_ErrorCreateTracePath + ": " + traceConfig.getTracePath()); //$NON-NLS-1$
                    }
                }
            }

            if (trace.isUst()) {
                // in UST the tracing is started after setupTrace!!
                trace.setTraceState(TraceState.STARTED);
            } else {
                trace.setTraceState(TraceState.CONFIGURED);
            }

            targetResource.addTrace(trace);

            ISystemRegistry registry = SystemStartHere.getSystemRegistry();
            registry.fireRemoteResourceChangeEvent(ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_CREATED, trace, targetResource, subSystem, null);

        } catch (Exception e) {
            SystemMessageException sysExp;
            if (e instanceof SystemMessageException) {
                sysExp = (SystemMessageException) e;
            } else {
                sysExp = new SystemMessageException(Activator.getDefault().getMessage(e));
            }
            SystemBasePlugin.logError(Messages.Lttng_Control_ErrorNewTrace + " (" + //$NON-NLS-1$
                    Messages.Lttng_Resource_Trace + ": " + traceConfig.getTraceName() + ")", sysExp); //$NON-NLS-1$ //$NON-NLS-2$

            return;
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action
     * .IAction, org.eclipse.jface.viewers.ISelection)
     */
    @Override
    @SuppressWarnings("unchecked")
    public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            fSelectedFiles.clear();
            // store the selected targets to be used when running
            Iterator<IStructuredSelection> theSet = ((IStructuredSelection) selection).iterator();
            while (theSet.hasNext()) {
                Object obj = theSet.next();
                if (obj instanceof TargetResource) {
                    fSelectedFiles.add((TargetResource) obj);
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
     * 
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.
     * IWorkbenchWindow)
     */
    @Override
    public void init(IWorkbenchWindow window) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
     */
    @Override
    public void dispose() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    @Override
    public void init(IViewPart view) {
    }

    /*
     * Setup the trace location for UST.
     */
    private boolean setupUstLocation(final ILttControllerService service, final TargetResource targetResource, final TraceConfig traceConfig)
            throws Exception {
        if (traceConfig.isNetworkTrace()) {
            File localDir = new File(traceConfig.getTracePath());
            if (!localDir.exists()) {
                boolean success = localDir.mkdirs();
                if (!success) {
                    return false;
                }
            }

            // Create future task
            boolean ok = new TCFTask<Boolean>() {
                @Override
                public void run() {

                    // Setup trace location using Lttng controller service proxy
                    service.writeTraceNetwork(targetResource.getParent().getName(), targetResource.getName(), traceConfig.getTracePath(), traceConfig.getTraceName(),
                            traceConfig.getNumChannel(), traceConfig.getIsAppend(), traceConfig.getMode() == TraceConfig.FLIGHT_RECORDER_MODE,
                            traceConfig.getMode() == TraceConfig.NORMAL_MODE, new ILttControllerService.DoneWriteTraceNetwork() {

                                @Override
                                public void doneWriteTraceNetwork(IToken token, Exception error, Object str) {
                                    if (error != null) {
                                        // Notify with error
                                        error(error);
                                        return;
                                    }

                                    // Notify about success
                                    done(true);
                                }
                            });
                }
            }.get(TraceControlConstants.DEFAULT_TCF_TASK_TIMEOUT, TimeUnit.SECONDS);
            return ok;
        } else {
            // Create future task
            boolean ok = new TCFTask<Boolean>() {
                @Override
                public void run() {

                    // Setup trace location using Lttng controller service proxy
                    service.writeTraceLocal(targetResource.getParent().getName(), targetResource.getName(), traceConfig.getTraceName(),
                            traceConfig.getTracePath(), traceConfig.getNumChannel(), traceConfig.getIsAppend(),
                            traceConfig.getMode() == TraceConfig.NORMAL_MODE, traceConfig.getMode() == TraceConfig.FLIGHT_RECORDER_MODE,
                            new ILttControllerService.DoneWriteTraceLocal() {

                                @Override
                                public void doneWriteTraceLocal(IToken token, Exception error, Object str) {
                                    if (error != null) {
                                        // Notify with error
                                        error(error);
                                        return;
                                    }

                                    // Notify about success
                                    done(true);
                                }
                            });
                }
            }.get(TraceControlConstants.DEFAULT_TCF_TASK_TIMEOUT, TimeUnit.SECONDS);
            return ok;
        }
    }

}
