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
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.IAnalysisDataContainer;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCpuAspect;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * A value that resolves to the value of an event field
 *
 * @author Geneviève Bastien
 * @author Florian Wininger
 * @author Jean-Christian Kouame
 */
public class DataDrivenValueEventField extends DataDrivenValue {

    private final String fFieldName;

    /**
     * Constructor
     *
     * @param mappingGroupId
     *            The ID of the mapping group to use to map the retrieved value to
     *            another value
     * @param forcedType
     *            The desired type of the value
     * @param fieldName
     *            The name of the field to resolve to
     */
    public DataDrivenValueEventField(@Nullable String mappingGroupId, ITmfStateValue.Type forcedType, String fieldName) {
        super(mappingGroupId, forcedType);
        fFieldName = fieldName;
    }

    @Override
    protected @Nullable Object resolveValue(int baseQuark, IAnalysisDataContainer container) {
        throw new IllegalArgumentException("Event field state value should only be called with an event"); //$NON-NLS-1$
    }

    @Override
    protected @Nullable Object resolveValue(ITmfEvent event, int baseQuark, DataDrivenScenarioInfo scenarioInfo, IAnalysisDataContainer container) {
        final ITmfEventField field = event.getContent().getField(fFieldName);

        Object fieldValue = null;

        /* If the field does not exist, see if it's a special case */
        if (field == null) {
            if (fFieldName.equalsIgnoreCase(TmfXmlStrings.CPU)) {
                /* A "CPU" field will return the CPU aspect if available */
                Integer cpu = TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(), TmfCpuAspect.class, event);
                if (cpu != null) {
                    return cpu;
                }
            } else if (fFieldName.equalsIgnoreCase(TmfXmlStrings.TIMESTAMP)) {
                /*
                 * Exception also for "TIMESTAMP", returns the timestamp of this event
                 */
                return event.getTimestamp().getValue();
            } else if (fFieldName.equalsIgnoreCase(TmfXmlStrings.HOSTID)) {
                /* Return the host ID of the trace containing the event */
                return event.getTrace().getHostId();
            }
            // This will allow to use any column as input
            fieldValue = TmfTraceUtils.resolveAspectOfNameForEvent(event.getTrace(), fFieldName, event);
        } else {
            fieldValue = field.getValue();
        }

        return fieldValue;
    }

    @Override
    public String toString() {
        return "DataDrivenValueEventField: " + fFieldName; //$NON-NLS-1$
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fFieldName);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof DataDrivenValueEventField)) {
            return false;
        }
        DataDrivenValueEventField other = (DataDrivenValueEventField) obj;
        return Objects.equals(fFieldName, other.fFieldName);
    }

}
