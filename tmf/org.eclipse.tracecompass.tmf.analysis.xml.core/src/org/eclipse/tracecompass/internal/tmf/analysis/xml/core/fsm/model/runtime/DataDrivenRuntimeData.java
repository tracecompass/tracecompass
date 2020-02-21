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
        return fFsmRuntime.computeIfAbsent(fsm, dataDrivenRuntimeFsm -> new DataDrivenRuntimeFsm());
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
