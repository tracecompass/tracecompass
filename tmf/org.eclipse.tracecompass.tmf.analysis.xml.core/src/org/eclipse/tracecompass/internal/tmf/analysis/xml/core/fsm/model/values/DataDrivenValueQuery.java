/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values;

import java.util.List;
import java.util.function.Function;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenScenarioInfo;
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

    private final List<DataDrivenValue> fQuery;

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
    public DataDrivenValueQuery(@Nullable String mappingGroupId, ITmfStateValue.Type forcedType, List<DataDrivenValue> query) {
        super(mappingGroupId, forcedType);
        fQuery = query;
    }

    @Override
    protected @Nullable Object resolveValue(int quark, IAnalysisDataContainer container) {
        return executeQuery(sv -> sv.resolveValue(quark, container), container);
    }

    @Override
    protected @Nullable Object resolveValue(ITmfEvent event, int quark, DataDrivenScenarioInfo scenarioInfo, IAnalysisDataContainer container) {
        return executeQuery(sv -> sv.resolveValue(event, quark, scenarioInfo, container), container);
    }

    private @Nullable Object executeQuery(Function<DataDrivenValue, @Nullable Object> function, IAnalysisDataContainer container) {
        /* Query the state system for the value */
        Object value = null;
        int quarkQuery = ITmfStateSystem.ROOT_ATTRIBUTE;
        ITmfStateSystem ss = container.getStateSystem();

        for (DataDrivenValue path : fQuery) {
            Object attribVal = function.apply(path);
            if (attribVal == null) {
                quarkQuery = IAnalysisDataContainer.ERROR_QUARK;
                break;
            }
            quarkQuery = container.getQuarkRelativeAndAdd(quarkQuery, String.valueOf(attribVal));
            if (quarkQuery < 0) {
                /* the query is not valid, we stop the state change */
                break;
            }
        }
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

}
