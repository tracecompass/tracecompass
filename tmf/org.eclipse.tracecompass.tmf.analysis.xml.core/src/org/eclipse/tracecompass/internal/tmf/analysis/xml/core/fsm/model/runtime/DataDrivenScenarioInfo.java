/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.runtime;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenFsm;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenFsmSimpleState;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenFsmState;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.runtime.DataDrivenScenarioHistoryBuilder.ScenarioStatusType;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.IAnalysisDataContainer;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfAttributePool;

/**
 * This class gives basic details about a scenario (quark, scenarioName, ...)
 *
 * FIXME: The original state provider analysis will use a one-state,
 * multiple-transition singleton fsm to reduce the number of potential
 * code-path. Fix this class when pattern analyses are ported to the new
 * architecture.
 *
 * @author Jean-Christian Kouame
 */
public class DataDrivenScenarioInfo {

    /**
     * A temporary dummy scenario
     */
    public static final DataDrivenScenarioInfo DUMMY_SCENARIO = new DataDrivenScenarioInfo(DataDrivenFsmSimpleState.createFinalState(StringUtils.EMPTY), ScenarioStatusType.PENDING, -1, -1, null);

    /** The string for start time */
    private static final String START_TIME = "startTime"; //$NON-NLS-1$

    private final int fQuark;
    private final int fStatusQuark;
    private DataDrivenFsmState fActiveState;
    private ScenarioStatusType fStatus;
    private final Map<@NonNull TmfAttributePool, Integer> fPoolAttributes = new HashMap<>();
    private final @Nullable DataDrivenFsm fFsm;

    /**
     * Constructor
     *
     * @param activeState
     *            The active state
     * @param status
     *            The scenario status
     * @param quark
     *            The scenario quark
     * @param statusQuark
     *            The scenario status quark
     * @param fsm
     *            The state machine this scenario info is for
     */
    public DataDrivenScenarioInfo(DataDrivenFsmState activeState, ScenarioStatusType status, int quark, int statusQuark, @Nullable DataDrivenFsm fsm) {
        fActiveState = activeState;
        fQuark = quark;
        fStatus = status;
        fStatusQuark = statusQuark;
        fFsm = fsm;
    }

    /**
     * Set the active state
     *
     * @param activeState
     *            The active state
     */
    public void setActiveState(DataDrivenFsmState activeState) {
        fActiveState = activeState;
    }

    /**
     * Set the status of this active scenario
     *
     * @param status
     *            The scenario status
     */
    public void setStatus(ScenarioStatusType status) {
        fStatus = status;
    }

    /**
     * Get the scenario quark
     *
     * @return The quark
     */
    public int getQuark() {
        return fQuark;
    }

    /**
     * Get the scenario active state
     *
     * @return The active state
     */
    public DataDrivenFsmState getActiveState() {
        return fActiveState;
    }

    /**
     * Get the active scenario status
     *
     * @return The status
     */
    public ScenarioStatusType getStatus() {
        return fStatus;
    }

    /**
     * Get the scenario status quark
     *
     * @return The quark
     */
    public int getStatusQuark() {
        return fStatusQuark;
    }


    /**
     * Get the state system quark of the attribute that was assigned to this
     * scenario from the given attribute pool. If no attribute was assigned to this
     * scenario, it will get one.
     *
     * @param pool
     *            The attribute pool from which to get an attribute for this
     *            scenario.
     * @return The quark of the attribute from a pool for the given state system
     *         path, or <code>null</code> if no attribute was assigned yet at this
     *         path.
     */
    public Integer getAttributeFromPool(TmfAttributePool pool) {
        Integer quark = fPoolAttributes.get(pool);
        if (quark == null) {
            quark = pool.getAvailable();
            fPoolAttributes.put(pool, quark);
        }
        return quark;
    }

    /**
     * Recycle all attributes taken from attribute pools
     *
     * @param ts
     *            The timestamp at which to close the attributes
     */
    public void recycleAttributes(long ts) {
        fPoolAttributes.entrySet().forEach(e -> {
            NonNullUtils.checkNotNull(e.getKey()).recycle(e.getValue(), ts);
        });
    }

    /**
     * Get the start time of a state
     *
     * FIXME: For the first iteration (no data driven patterns yet), this method
     * is here, but may move to a class responsible of scenario history, like
     * current code
     *
     * @param container
     *            The analysis data container
     * @param state
     *            The state for which to get the start time
     * @return The start time of the requested state or <code>-1L</code> if this
     *         state has not been reached
     */
    public long getStateStartTime(IAnalysisDataContainer container, String state) {
        ITmfStateSystem stateSystem = container.getStateSystem();
        int stateQuark = stateSystem.optQuarkRelative(fQuark, TmfXmlStrings.STATE, state, START_TIME);
        if (stateQuark < 0) {
            return -1L;
        }
        Object startTs = stateSystem.queryOngoing(stateQuark);
        if (startTs instanceof Long) {
            return (long) startTs;
        }
        return -1L;
    }

    /**
     * Get the ID of the FSM this scenario is part of
     *
     * FIXME: Make this @NonNull when everything is a pattern
     *
     * @return The ID of the FSM
     */
    public @Nullable DataDrivenFsm getFsm() {
        return fFsm;
    }

}
