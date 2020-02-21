/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values;

import java.util.Objects;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenStateSystemPath;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.runtime.DataDrivenScenarioInfo;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.IAnalysisDataContainer;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * A value that resolves to the value at a given path in the state system
 *
 * @author Geneviève Bastien
 * @author Florian Wininger
 */
public class DataDrivenValueQuery extends DataDrivenValue {

    private final DataDrivenStateSystemPath fQuery;

    /**
     * Constructor
     *
     * @param mappingGroupId
     *            The ID of the mapping group to use to map the retrieved value to
     *            another value
     * @param forcedType
     *            The desired type of the value
     * @param query
     *            The path of the query in the state system
     */
    public DataDrivenValueQuery(@Nullable String mappingGroupId, ITmfStateValue.Type forcedType, DataDrivenStateSystemPath query) {
        super(mappingGroupId, forcedType);
        fQuery = query;
    }

    @Override
    protected @Nullable Object resolveValue(int quark, IAnalysisDataContainer container) {
        return executeQuery(() -> fQuery.getQuark(null, ITmfStateSystem.ROOT_ATTRIBUTE, null, container), container);
    }

    @Override
    protected @Nullable Object resolveValue(ITmfEvent event, int quark, DataDrivenScenarioInfo scenarioInfo, IAnalysisDataContainer container) {
        return executeQuery(() -> fQuery.getQuark(event, ITmfStateSystem.ROOT_ATTRIBUTE, scenarioInfo, container), container);
    }

    private static @Nullable Object executeQuery(Supplier<Integer> function, IAnalysisDataContainer container) {
        /* Query the state system for the value */
        Object value = null;
        ITmfStateSystem ss = container.getStateSystem();

        @SuppressWarnings("null")
        int quarkQuery = function.get();
        /*
         * the query can fail : for example, if a value is requested but has not been
         * set yet
         */
        if (quarkQuery >= 0) {
            value = ss.queryOngoing(quarkQuery);
        }
        return value;
    }

    @Override
    public String toString() {
        return "DataDrivenValueQuery: " + fQuery; //$NON-NLS-1$
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fQuery);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof DataDrivenValueQuery)) {
            return false;
        }
        DataDrivenValueQuery other = (DataDrivenValueQuery) obj;
        return Objects.equals(fQuery, other.fQuery);
    }

}
