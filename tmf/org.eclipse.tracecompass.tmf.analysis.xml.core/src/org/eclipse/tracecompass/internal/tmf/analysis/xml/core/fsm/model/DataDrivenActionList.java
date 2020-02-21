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

import java.util.List;

import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.runtime.DataDrivenScenarioInfo;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.IAnalysisDataContainer;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * An action that executes a series of actions
 *
 * @author Geneviève Bastien
 */
public class DataDrivenActionList implements DataDrivenAction {

    private final List<DataDrivenAction> fActions;

    /**
     * Constructor
     *
     * @param actions
     *            The list of actions to execute
     */
    public DataDrivenActionList(List<DataDrivenAction> actions) {
        fActions = actions;
    }

    @Override
    public void eventHandle(ITmfEvent event, DataDrivenScenarioInfo scenarioInfo, IAnalysisDataContainer container) {
        for (DataDrivenAction action : fActions) {
            action.eventHandle(event, scenarioInfo, container);
        }
    }

}
