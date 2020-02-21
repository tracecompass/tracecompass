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

import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.runtime.DataDrivenScenarioInfo;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.IAnalysisDataContainer;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * Base class for FSM and states
 *
 * @author Geneviève Bastien
 */
public class DataDrivenFsmState implements IDataDrivenRuntimeObject {

    private final String fId;

    /**
     * Constructor
     *
     * @param id
     *            The ID of this state
     */
    protected DataDrivenFsmState(String id) {
        fId = id;
    }

    /**
     * Get the ID of this state
     *
     * @return The ID of the state
     */
    public String getId() {
        return fId;
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
    public @Nullable DataDrivenFsmState takeTransition(ITmfEvent event, DataDrivenScenarioInfo scenarioInfo, IAnalysisDataContainer container) {
        return null;
    }

    /**
     * Return whether this state is a final state, ie has no transitions from it
     *
     * @return Whether the state is final
     */
    public boolean isFinal() {
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fId);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof DataDrivenFsmState)) {
            return false;
        }
        return Objects.equals(fId, ((DataDrivenFsmState) obj).fId);
    }

}
