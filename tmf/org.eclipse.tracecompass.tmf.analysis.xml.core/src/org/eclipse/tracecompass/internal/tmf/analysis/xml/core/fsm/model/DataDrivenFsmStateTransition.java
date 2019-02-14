/*******************************************************************************
 * Copyright (c) 2019 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.runtime.DataDrivenScenarioInfo;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.IAnalysisDataContainer;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * A transition from a state machine
 *
 * @author Geneviève Bastien
 * @author Jean-Christian Kouamé
 */
public class DataDrivenFsmStateTransition implements IDataDrivenRuntimeObject {

    private final DataDrivenCondition fEvents;
    private final String fTarget;
    private final DataDrivenCondition fCondition;
    private final List<DataDrivenAction> fActions;

    /**
     * Constructor
     *
     * @param eventCondition
     *            The condition for the events
     * @param dataDrivenCondition
     *            The additional conditions for this transition to be taken
     * @param target
     *            The name of the target of the transition
     * @param actions
     *            The actions to execute on success
     */
    public DataDrivenFsmStateTransition(DataDrivenCondition eventCondition, DataDrivenCondition dataDrivenCondition, String target, List<DataDrivenAction> actions) {
        fEvents = eventCondition;
        fCondition = dataDrivenCondition;
        fTarget = target;
        fActions = actions;
    }

    /**
     * Get the transition that can be taken out of this state for the event
     *
     * @param event
     *            The current event to handle
     * @param scenarioInfo
     *            The scenario info
     * @param container
     *            The container
     * @return The first transition that can be taken out of this state, or
     *         <code>null</code> if no transition can be taken
     */
    public boolean canTake(ITmfEvent event, DataDrivenScenarioInfo scenarioInfo, IAnalysisDataContainer container) {
        return fEvents.test(event, scenarioInfo, container) && fCondition.test(event, scenarioInfo, container);
    }

    /**
     * Take this transition and return the next state
     *
     * @param event
     *            The current event to handle
     * @param scenarioInfo
     *            The scenario info
     * @param container
     *            The container
     * @return The target state
     */
    public String take(ITmfEvent event, DataDrivenScenarioInfo scenarioInfo, IAnalysisDataContainer container) {
        for (DataDrivenAction action : fActions) {
            action.eventHandle(event, scenarioInfo, container);
        }
        return fTarget;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fEvents, fTarget, fCondition, fActions);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof DataDrivenFsmStateTransition)) {
            return false;
        }
        DataDrivenFsmStateTransition other = (DataDrivenFsmStateTransition) obj;
        return Objects.equals(fEvents,  other.fEvents) &&
                Objects.equals(fTarget, other.fTarget) &&
                Objects.equals(fCondition, other.fCondition) &&
                Objects.equals(fActions, other.fActions);
    }

    @Override
    public String toString() {
        return "--> " + fTarget + ':' + fEvents + ' ' + fCondition + ' ' + fActions; //$NON-NLS-1$
    }
}
