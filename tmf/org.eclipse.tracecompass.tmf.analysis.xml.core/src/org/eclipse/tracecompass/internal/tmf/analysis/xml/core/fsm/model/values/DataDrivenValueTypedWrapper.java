/*******************************************************************************
 * Copyright (c) 2018 École Polytechnique de Montréal
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
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.IAnalysisDataContainer;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue.Type;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * A field state value that wraps a state value but changes its type
 *
 * @author Geneviève Bastien
 */
public class DataDrivenValueTypedWrapper extends DataDrivenValue {

    private final DataDrivenValue fValue;

    /**
     * Constructor
     *
     * @param value
     *            the wrapped value
     * @param type
     *            The expected type
     */
    public DataDrivenValueTypedWrapper(DataDrivenValue value, Type type) {
        super(null, type);
        fValue = value;
    }

    @Override
    protected @Nullable Object resolveValue(int baseQuark, IAnalysisDataContainer container) {
        return fValue.resolveValue(baseQuark, container);
    }

    @Override
    protected @Nullable Object resolveValue(ITmfEvent event, int baseQuark, DataDrivenScenarioInfo scenarioInfo, IAnalysisDataContainer container) {
        return fValue.resolveValue(event, baseQuark, scenarioInfo, container);
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return super.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fValue);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof DataDrivenValueTypedWrapper)) {
            return false;
        }
        return super.equals(obj) && Objects.equals(fValue, ((DataDrivenValueTypedWrapper) obj).fValue);
    }

}
