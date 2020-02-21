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

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.runtime.DataDrivenScenarioInfo;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.IAnalysisDataContainer;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * A value that resolves to a constant
 *
 * @author Geneviève Bastien
 */
public class DataDrivenValueConstant extends DataDrivenValue {

    private final @Nullable Object fValue;

    /**
     * Constructor
     *
     * @param mappingGroupId
     *            The ID of the mapping group to use to map the retrieved value to
     *            another value
     * @param forcedType
     *            The desired type of the value
     * @param value
     *            The constant value this value resolves to
     */
    public DataDrivenValueConstant(@Nullable String mappingGroupId, ITmfStateValue.Type forcedType, @Nullable Object value) {
        super(mappingGroupId, forcedType);
        fValue = value;
    }

    @Override
    protected @Nullable Object resolveValue(int baseQuark, IAnalysisDataContainer container) {
        return fValue;
    }

    @Override
    protected @Nullable Object resolveValue(ITmfEvent event, int baseQuark, DataDrivenScenarioInfo scenarioInfo, IAnalysisDataContainer container) {
        return fValue;
    }

    @Override
    public String toString() {
        return "DataDrivenValueConstant: " + fValue; //$NON-NLS-1$
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fValue);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof DataDrivenValueConstant)) {
            return false;
        }
        DataDrivenValueConstant other = (DataDrivenValueConstant) obj;
        return Objects.equals(fValue, other.fValue);
    }

}
