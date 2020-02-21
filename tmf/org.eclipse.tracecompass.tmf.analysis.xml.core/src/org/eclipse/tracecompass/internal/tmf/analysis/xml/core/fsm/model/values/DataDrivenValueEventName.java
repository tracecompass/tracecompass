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

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.runtime.DataDrivenScenarioInfo;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.IAnalysisDataContainer;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * A value that resolves to the event name
 *
 * @author Geneviève Bastien
 */
public final class DataDrivenValueEventName extends DataDrivenValue {

    /**
     * Constructor
     *
     * @param mappingGroupId
     *            The ID of the mapping group to use to map the retrieved value to
     *            another value
     */
    public DataDrivenValueEventName(@Nullable String mappingGroupId) {
        super(mappingGroupId, ITmfStateValue.Type.STRING);
    }

    @Override
    protected @Nullable Object resolveValue(int baseQuark, IAnalysisDataContainer container) {
        throw new IllegalArgumentException("Event field name value should only be called with an event"); //$NON-NLS-1$
    }

    @Override
    protected @Nullable Object resolveValue(ITmfEvent event, int baseQuark, DataDrivenScenarioInfo scenarioInfo, IAnalysisDataContainer container) {
        return event.getName();
    }

    @Override
    public String toString() {
        return "DataDrivenValueEventName"; //$NON-NLS-1$
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof DataDrivenValueEventName)) {
            return false;
        }
        return super.equals(obj);
    }
}
