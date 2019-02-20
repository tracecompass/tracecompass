/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values;

import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenScenarioInfo;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenStateSystemPath;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.DataDrivenException;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.IAnalysisDataContainer;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * A value that resolves to the peek of a stack at a given path
 *
 * @author Geneviève Bastien
 */
public class DataDrivenValueStackPeek extends DataDrivenValue {

    private final DataDrivenStateSystemPath fPath;

    /**
     * Constructor
     *
     * @param mappingGroupId
     *            The ID of the mapping group to use to map the retrieved value to
     *            another value
     * @param forcedType
     *            The desired type of the value
     * @param path
     *            The path in the state system to use to reach the stack. This can
     *            be empty if the queried quark already points to the right
     *            location.
     */
    public DataDrivenValueStackPeek(@Nullable String mappingGroupId, ITmfStateValue.Type forcedType, DataDrivenStateSystemPath path) {
        super(mappingGroupId, forcedType);
        fPath = path;
    }

    @Override
    protected @Nullable Object resolveValue(int baseQuark, IAnalysisDataContainer container) {
        return null;
    }

    @Override
    protected @Nullable Object resolveValue(ITmfEvent event, int baseQuark, DataDrivenScenarioInfo scenarioInfo, IAnalysisDataContainer container) {
        final long ts = event.getTimestamp().toNanos();
        /* Query the state system for the value */
        Object value = null;
        ITmfStateSystem ss = container.getStateSystem();

        int quarkQuery = fPath.getQuark(event, baseQuark, scenarioInfo, container);
        /*
         * the query can fail : for example, if a value is requested but has not been
         * set yet
         */
        if (quarkQuery >= 0) {
            try {
                @Nullable
                ITmfStateInterval stackTopInterval = StateSystemUtils.querySingleStackTop(ss, ts, quarkQuery);
                return (stackTopInterval != null ? stackTopInterval.getStateValue().unboxValue() : null);
            } catch (AttributeNotFoundException | StateSystemDisposedException e) {
                throw new DataDrivenException("Resolving stack peek: " + e.getMessage(), event); //$NON-NLS-1$
            }
        }
        return value;
    }

    @Override
    public String toString() {
        return "DataDrivenValueStackPeek"; //$NON-NLS-1$
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fPath);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof DataDrivenValueStackPeek)) {
            return false;
        }
        DataDrivenValueStackPeek other = (DataDrivenValueStackPeek) obj;
        return Objects.equals(fPath, other.fPath);
    }

}
