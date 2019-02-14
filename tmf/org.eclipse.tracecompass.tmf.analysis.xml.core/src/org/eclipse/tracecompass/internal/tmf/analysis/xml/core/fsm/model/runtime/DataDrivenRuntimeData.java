/*******************************************************************************
 * Copyright (c) 2019 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.runtime;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenFsm;

/**
 * Keep data that is useful during the execution of the analysis, but that will
 * be irrelevant once the analysis is completed.
 *
 * @author Geneviève Bastien
 */
public class DataDrivenRuntimeData {

    /**
     * A default instance that does nothing and should not be accessed directly
     */
    public static final DataDrivenRuntimeData DEFAULT = new DataDrivenRuntimeData();

    private final Map<DataDrivenFsm, DataDrivenRuntimeFsm> fFsmRuntime = new HashMap<>();
    private DataDrivenScenarioHistoryBuilder fHistoryBuilder = new DataDrivenScenarioHistoryBuilder();

    /**
     * Get the runtime FSM for a fsm. Creates one if necessary
     *
     * @param fsm
     *            The FSM to get the runtime data for
     * @return The runtime data for this FSM
     */
    public DataDrivenRuntimeFsm getRuntimeForFsm(DataDrivenFsm fsm) {
        DataDrivenRuntimeFsm runtimeFsm = fFsmRuntime.get(fsm);
        if (runtimeFsm == null) {
            runtimeFsm = new DataDrivenRuntimeFsm();
            fFsmRuntime.put(fsm, runtimeFsm);
        }
        return runtimeFsm;
    }

    /**
     * Get the history builder for this analysis execution
     *
     * @return The scenario history builder
     */
    public DataDrivenScenarioHistoryBuilder getHistoryBuilder() {
        return fHistoryBuilder;
    }

}
