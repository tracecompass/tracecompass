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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.runtime.DataDrivenScenarioInfo;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.IAnalysisDataContainer;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * A class representing a finite state machine state with only transitions
 *
 * @author Geneviève Bastien
 * @author Jean-Christian Kouamé
 */
public class DataDrivenFsmSimpleState extends DataDrivenFsmState {

    private final List<DataDrivenFsmStateTransition> fTransitions;
    private final DataDrivenAction fOnEntry;
    private final DataDrivenAction fOnExit;

    /**
     * Create a final state (ie without transitions or actions) with the given
     * ID
     *
     * @param id
     *            The ID of the final state
     * @return The new state
     */
    public static final DataDrivenFsmSimpleState createFinalState(String id) {
        return new DataDrivenFsmSimpleState(id, Collections.emptyList(), DataDrivenAction.NO_ACTION, DataDrivenAction.NO_ACTION);
    }

    /**
     * Constructor
     *
     * @param id
     *            The ID of this state
     * @param transitions
     *            The transitions from this state
     * @param onEntry
     *            The action to execute upon entering this state
     * @param onExit
     *            The action to execute upon exiting this state
     */
    public DataDrivenFsmSimpleState(String id, List<DataDrivenFsmStateTransition> transitions, DataDrivenAction onEntry, DataDrivenAction onExit) {
        super(id);
        fTransitions = transitions;
        fOnEntry = onEntry;
        fOnExit = onExit;
    }

    @Override
    public @Nullable DataDrivenFsmState takeTransition(ITmfEvent event, DataDrivenScenarioInfo scenarioInfo, IAnalysisDataContainer container) {
        for (DataDrivenFsmStateTransition transition : fTransitions) {
            if (transition.canTake(event, scenarioInfo, container)) {
                // We have a valid transition, start by executing the onExit actions
                fOnExit.eventHandle(event, scenarioInfo, container);
                // Take the transition and return the resulting state
                String nextStateName = transition.take(event, scenarioInfo, container);
                DataDrivenFsmSimpleState nextState = Objects.requireNonNull(scenarioInfo.getFsm()).getState(nextStateName);
                // Execute the onEntry actions of nextState
                nextState.fOnEntry.eventHandle(event, scenarioInfo, container);
                return nextState;
            }
        }
        return null;
    }

    @Override
    public boolean isFinal() {
        return fTransitions.isEmpty();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fTransitions, fOnEntry, fOnExit);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof DataDrivenFsmSimpleState)) {
            return false;
        }
        DataDrivenFsmSimpleState other = (DataDrivenFsmSimpleState) obj;
        return super.equals(obj) &&
                Objects.equals(fTransitions,  other.fTransitions) &&
                Objects.equals(fOnEntry, other.fOnEntry) &&
                Objects.equals(fOnExit, other.fOnExit);
    }

    @Override
    public String toString() {
        return "State" + getId() + ':' + fTransitions + ' ' + fOnEntry + ' ' + fOnExit; //$NON-NLS-1$
    }
}
