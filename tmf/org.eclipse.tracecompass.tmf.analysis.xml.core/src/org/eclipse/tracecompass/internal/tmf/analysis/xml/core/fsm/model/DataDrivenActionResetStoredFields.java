/*******************************************************************************
 * Copyright (c) 2016 Ecole Polytechnique de Montreal, Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model;

import java.util.Map.Entry;

import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.runtime.DataDrivenScenarioInfo;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.IAnalysisDataContainer;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.pattern.DataDrivenPattern;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * This action will reset the value of each stored values to a null
 * {@link ITmfStateValue} in the state system
 */
public final class DataDrivenActionResetStoredFields implements DataDrivenAction {

    private static final DataDrivenAction INSTANCE = new DataDrivenActionResetStoredFields();

    /**
     * Get the instance of this action
     *
     * @return The action
     */
    public static DataDrivenAction getInstance() {
        return INSTANCE;
    }

    /**
     * Constructor
     */
    private DataDrivenActionResetStoredFields() {
        // Nothing to do
    }

    @Override
    public void eventHandle(ITmfEvent event, DataDrivenScenarioInfo scenarioInfo, IAnalysisDataContainer container) {
        if (container instanceof DataDrivenPattern) {
            DataDrivenPattern provider = (DataDrivenPattern) container;
            for (Entry<String, String> entry : provider.getStoredFields().entrySet()) {
                provider.getExecutionData().getHistoryBuilder().resetStoredFields(provider, entry.getValue(), scenarioInfo, event);
            }
        }
    }
}
