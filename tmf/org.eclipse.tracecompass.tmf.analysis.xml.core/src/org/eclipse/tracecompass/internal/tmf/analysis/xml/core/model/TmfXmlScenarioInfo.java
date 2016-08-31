/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.TmfXmlScenarioHistoryBuilder.ScenarioStatusType;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfAttributePool;

/**
 * This class gives basic details about a scenario (quark, scenarioName, ...)
 *
 * @author Jean-Christian Kouame
 */
public class TmfXmlScenarioInfo {

    private final int fQuark;
    private final TmfXmlFsm fFsm;
    private final int fStatusQuark;
    private String fActiveState;
    private ScenarioStatusType fStatus;
    private final Map<@NonNull TmfAttributePool, Integer> fPoolAttributes = new HashMap<>();

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
     *            The FSM this scenario is part of
     */
    public TmfXmlScenarioInfo(String activeState, ScenarioStatusType status, int quark, int statusQuark, TmfXmlFsm fsm) {
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
    public void setActiveState(String activeState) {
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
    public String getActiveState() {
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
     * Get the ID of the FSM this scenario is part of
     *
     * @return The ID of the FSM
     */
    public String getFsmId() {
        return fFsm.getId();
    }

    /**
     * Get the state system quark of the attribute that was assigned to this
     * scenario from the given attribute pool. If no attribute was assigned to
     * this scenario, it will get one.
     *
     * @param pool
     *            The attribute pool from which to get an attribute for this
     *            scenario.
     * @return The quark of the attribute from a pool for the given state system
     *         path, or <code>null</code> if no attribute was assigned yet at
     *         this path.
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

}
