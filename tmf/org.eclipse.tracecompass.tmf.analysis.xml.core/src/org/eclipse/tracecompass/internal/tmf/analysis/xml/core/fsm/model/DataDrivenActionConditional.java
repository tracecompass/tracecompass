/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.IAnalysisDataContainer;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * An action that will execute another action depending on the result of a
 * condition.
 *
 * @author Geneviève Bastien
 */
public class DataDrivenActionConditional implements DataDrivenAction {

    private final DataDrivenCondition fCondition;
    private final DataDrivenAction fThenChange;
    private final @Nullable DataDrivenAction fElseChange;

    /**
     * Constructor
     *
     * @param condition
     *            The condition to verify
     * @param thenChange
     *            The action to execute if the condition is true
     * @param elseChange
     *            The optional action to execute if the condition is false. This
     *            action can be <code>null</code>
     */
    public DataDrivenActionConditional(DataDrivenCondition condition, DataDrivenAction thenChange, @Nullable DataDrivenAction elseChange) {
        fCondition = condition;
        fThenChange = thenChange;
        fElseChange = elseChange;
    }

    @Override
    public void eventHandle(ITmfEvent event, DataDrivenScenarioInfo scenarioInfo, IAnalysisDataContainer container) {
        if (fCondition.test(event, scenarioInfo, container)) {
            fThenChange.eventHandle(event, scenarioInfo, container);
        } else if (fElseChange != null) {
            fElseChange.eventHandle(event, scenarioInfo, container);
        }
    }

}
