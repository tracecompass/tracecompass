/*******************************************************************************
 * Copyright (c) 2019 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.runtime.DataDrivenRuntimeData;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.runtime.DataDrivenRuntimeFsm;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.runtime.DataDrivenScenario;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.runtime.DataDrivenScenarioInfo;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.IAnalysisDataContainer;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * A class representing a data driven finite state machine
 *
 * @author Geneviève Bastien
 * @author Jean-Christian Kouamé
 */
public class DataDrivenFsm extends DataDrivenFsmState {

    private final DataDrivenFsmState fInitial;
    private final Map<String, DataDrivenFsmSimpleState> fStates;
    private final DataDrivenCondition fPrecondition;
    private final boolean fConsuming;
    private final boolean fMultipleInstances;

    /**
     * Constructor
     *
     * @param id
     *            The ID of this FSM
     * @param initial
     *            The initial state
     * @param states
     *            The map of states for this state machine
     * @param preCondition
     *            The precondition that should be matched for this state machine
     *            to be considered
     * @param consuming
     *            Whether the state machine is consuming, ie if a transition is
     *            taken from any one scenario, then the other scenarios will not
     *            be considered
     * @param multipleInstances
     *            Whether multiple scenarios are allowed for this state machine
     */
    public DataDrivenFsm(String id, DataDrivenFsmState initial, Map<String, DataDrivenFsmSimpleState> states, DataDrivenCondition preCondition, boolean consuming, boolean multipleInstances) {
        super(id);
        fInitial = initial;
        fStates = states;
        fPrecondition = preCondition;
        fConsuming = consuming;
        fMultipleInstances = multipleInstances;
    }

    /**
     * Create a new scenario for this FSM
     *
     * @param event
     *            The event that marks the start of the scenario
     * @param force
     *            Whether to force the creation or create only if allowed
     * @param executionData
     *            The execution data
     * @param container
     *            The parent analysis container
     */
    public void createScenario(ITmfEvent event, boolean force, DataDrivenRuntimeData executionData, IAnalysisDataContainer container) {
        DataDrivenRuntimeFsm runtimeFsm = executionData.getRuntimeForFsm(this);
        if (force || isNewScenarioAllowed(runtimeFsm)) {
            runtimeFsm.addPendingScenario(new DataDrivenScenario(event, this, fInitial, container, executionData));
        }
    }

    /**
     * Return whether this finite state machine can support an additional
     * scenario
     *
     * @param runtimeFsm
     *            The runtime FSM data
     * @return <code>true</code> if this state machine can have a new scenario
     */
    public synchronized boolean isNewScenarioAllowed(DataDrivenRuntimeFsm runtimeFsm) {
        return runtimeFsm.getScenarioCount() > 0 && fMultipleInstances
                && runtimeFsm.getPendingScenario() == null;
    }

    /**
     * Let the FSM try to handle this event
     *
     * @param event
     *            The event to handle
     * @param executionData
     *            The execution data for run
     * @param container
     *            The analysis data container
     */
    public void handleEvent(ITmfEvent event, DataDrivenRuntimeData executionData, IAnalysisDataContainer container) {
        // First validate the precondition
        // Preconditions should be stateless, so we don't need specific scenario
        // infos
        if (!fPrecondition.test(event, DataDrivenScenarioInfo.DUMMY_SCENARIO, container)) {
            return;
        }

        DataDrivenRuntimeFsm runtimeFsm = executionData.getRuntimeForFsm(this);
        boolean eventConsumed = false;

        // First handle the active scenarios
        eventConsumed = handleActiveScenarios(event, runtimeFsm, container);
        // Then handle the pending scenario
        handlePendingScenario(event, eventConsumed, runtimeFsm, container);
    }

    private void handlePendingScenario(ITmfEvent event, boolean eventConsumed, DataDrivenRuntimeFsm runtimeFsm, IAnalysisDataContainer container) {
        if (fConsuming && eventConsumed) {
            return;
        }

        DataDrivenScenario scenario = runtimeFsm.getPendingScenario();
        if (scenario != null) {
            scenario.handleEvent(event, container);
            if (!scenario.isPending()) {
                runtimeFsm.activatePending();
            }
        }
    }

    /**
     * Process the active scenario with the ongoing event
     *
     * @param event
     *            The ongoing event
     * @param runtimeFsm
     *            The map of transition
     * @param container
     *            The data container
     * @return True if the event has been consumed by the active scenarios
     */
    private boolean handleActiveScenarios(ITmfEvent event, DataDrivenRuntimeFsm runtimeFsm, IAnalysisDataContainer container) {

        boolean eventConsumed = false;
        List<DataDrivenScenario> toRemove = new ArrayList<>();
        for (DataDrivenScenario scenario : runtimeFsm.getActiveScenarios()) {
            // Remove inactive scenarios or handle the active ones.
            if (!scenario.isActive()) {
                toRemove.add(scenario);
            } else {
                if (scenario.isActive() || scenario.isPending()) {
                    eventConsumed |= scenario.handleEvent(event, container);
                    if (fConsuming && eventConsumed) {
                        break;
                    }
                }
            }
        }
        // Remove scenarios set to be removed
        for (DataDrivenScenario scenario : toRemove) {
            runtimeFsm.removeScenario(scenario);
        }

        return eventConsumed;

    }

    /**
     * Cancel all the still active scenarios
     *
     * @param executionData
     *            The execution data
     */
    public void dispose(DataDrivenRuntimeData executionData) {
        for (DataDrivenScenario scenario : executionData.getRuntimeForFsm(this).getActiveScenarios()) {
            if (scenario.isActive()) {
                scenario.cancel();
            }
        }
    }

    /**
     * Get the state of a given name. The state should exist and this method
     * will throw a <code>NullPointerException</code> if it doesn't.
     *
     * @param stateName
     *            The name of the state to get
     * @return The requested state
     */
    public DataDrivenFsmSimpleState getState(String stateName) {
        return Objects.requireNonNull(fStates.get(stateName), "The requested state should exist"); //$NON-NLS-1$
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fInitial, fStates, fPrecondition, fConsuming, fMultipleInstances);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof DataDrivenFsm)) {
            return false;
        }
        DataDrivenFsm other = (DataDrivenFsm) obj;
        return super.equals(obj) &&
                Objects.equals(fInitial,  other.fInitial) &&
                Objects.equals(fPrecondition, other.fPrecondition) &&
                Objects.equals(fConsuming, other.fConsuming) &&
                Objects.equals(fMultipleInstances, other.fMultipleInstances) &&
                Objects.equals(fStates, other.fStates);
    }

    @Override
    public String toString() {
        return "FSM " + getId() + ':' + fInitial + ' ' + fPrecondition + ' ' + fConsuming + ' ' + fMultipleInstances + //$NON-NLS-1$
                ' ' + fStates;
    }

}
