/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alvaro Sanchez-Leon (alvsan09@gmail.com) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng.core.state.experiment;

import org.eclipse.linuxtools.internal.lttng.core.Activator;
import org.eclipse.linuxtools.internal.lttng.core.TraceDebug;
import org.eclipse.linuxtools.internal.lttng.core.model.LTTngTreeNode;
import org.eclipse.linuxtools.internal.lttng.core.state.LttngStateException;
import org.eclipse.linuxtools.internal.lttng.core.state.trace.IStateTraceManager;
import org.eclipse.linuxtools.internal.lttng.core.state.trace.StateTraceManager;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;

/**
 * @author alvaro
 *
 */
public class StateManagerFactory {
    // ========================================================================
    // Data
    // =======================================================================

    private static IStateExperimentManager experimentManager = null;
    /**
     * Allows to modify the check point interval for every new instance of trace manager
     */
    private static Long ftraceCheckPointInterval = null;

    static {
        initCheck();
    }

    // ========================================================================
    // Methods
    // =======================================================================

    /**
     * @param traceUniqueId
     * @param experiment
     * @return
     */
    public static LTTngTreeNode getManager(ITmfTrace rtrace, LTTngTreeNode experiment) {

        // Validate
        if (rtrace == null) {
            return null;
        }

        String traceUniqueId = rtrace.getName();
        if (traceUniqueId == null) {
            return null;
        }

        LTTngTreeNode managerNode = null;
        managerNode = experiment.getChildByName(traceUniqueId);

        if (managerNode != null && managerNode instanceof IStateTraceManager) {
            return managerNode;
        }

//		LttngTraceState traceModel =
//		StateModelFactory.getStateEntryInstance();
        StateTraceManager manager = null;

        // catch potential construction problems
        try {
            manager = new StateTraceManager(experiment.getNextUniqueId(), experiment, traceUniqueId, rtrace);

            // Allow the possibility to configure the trace state check point
            // interval at creation time
            if (ftraceCheckPointInterval != null) {
                manager.setCheckPointInterval(ftraceCheckPointInterval);
            }

        } catch (LttngStateException e) {
            Activator.getDefault().logError("Unexpected Error", e);  //$NON-NLS-1$
        }

        experiment.addChild(manager);
        return manager;
    }

    /**
     * Provide the State trace set manager
     *
     * @return
     */
    public static IStateExperimentManager getExperimentManager() {
        return experimentManager;
    }

    /**
     * Remove previously registered managers
     *
     * @param traceUniqueId
     */
    public static void removeManager(ITmfTrace rtrace, LTTngTreeNode rexperiment) {
        if (rtrace ==  null || rexperiment == null) {
            return;
        }
        if (rexperiment.getValue() instanceof TmfExperiment) {
            LTTngTreeNode childToremove = rexperiment.getChildByName(rtrace.getName());
            if (childToremove != null) {
                rexperiment.removeChild(childToremove);
            }
        } else {
            TraceDebug.debug("Invalid arguments to remove manager for trace: " //$NON-NLS-1$
                    + rtrace.getName());
        }
    }

    /**
     * initialization of factory
     */
    private static void initCheck() {
        if (experimentManager == null) {
            Long id = 0L; // unique id
            String name = "StateExperimentManager"; // name //$NON-NLS-1$
            experimentManager = new StateExperimentManager(id, name);
        }
    }

    /**
     * Clea up resources
     */
    public static void dispose() {
        if (experimentManager != null) {
            experimentManager = null;
        }
    }

    /**
     * @return the traceCheckPointInterval
     */
    public static Long getTraceCheckPointInterval() {
        return ftraceCheckPointInterval;
    }

    /**
     * @param traceCheckPointInterval
     *            the traceCheckPointInterval to set
     */
    public static void setTraceCheckPointInterval(Long traceCheckPointInterval) {
        StateManagerFactory.ftraceCheckPointInterval = traceCheckPointInterval;
    }
}
