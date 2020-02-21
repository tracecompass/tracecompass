/*******************************************************************************
 * Copyright (c) 2018 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.runtime.DataDrivenScenarioInfo;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValue;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.IAnalysisDataContainer;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.pattern.DataDrivenPattern;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.segment.TmfXmlPatternSegment;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * An action that will create a segment
 *
 * @author Jean-Christian Kouamé
 * @author Geneviève Bastien
 */
public class DataDrivenActionSegment implements DataDrivenAction {

    private DataDrivenValue fType;
    private @Nullable DataDrivenValue fStart;
    private @Nullable DataDrivenValue fDuration;
    private @Nullable DataDrivenValue fEnd;
    private Map<String, DataDrivenValue> fFields;

    /**
     * Constructor
     *
     * @param type
     *            The value of the segment type name
     * @param start
     *            The value for the start of the segment
     * @param duration
     *            The value for the duration of the segment
     * @param end
     *            The value for the end of the segment
     * @param fields
     *            The list of fields for this segment
     */
    public DataDrivenActionSegment(DataDrivenValue type, @Nullable DataDrivenValue start, @Nullable DataDrivenValue duration, @Nullable DataDrivenValue end, Map<String, DataDrivenValue> fields) {
        fType = type;
        fStart = start;
        fDuration = duration;
        fEnd = end;
        fFields = fields;
    }

    @Override
    public void eventHandle(ITmfEvent event, DataDrivenScenarioInfo scenarioInfo, IAnalysisDataContainer container) {
        if (!(container instanceof DataDrivenPattern)) {
            // This action should only be run with pattern state provider
            return;
        }
        DataDrivenPattern provider = (DataDrivenPattern) container;
        // Get the default timestamp
        long start = provider.getExecutionData().getHistoryBuilder().getStartTime(container, scenarioInfo, event);
        long end = event.getTimestamp().toNanos();

        Object segmentName = fType.getValue(event, ITmfStateSystem.ROOT_ATTRIBUTE, scenarioInfo, container);

        Map<String, Object> fields = new HashMap<>();
        for (Entry<String, DataDrivenValue> field : fFields.entrySet()) {
            Object value = field.getValue().getValue(event, ITmfStateSystem.ROOT_ATTRIBUTE, scenarioInfo, container);
            // Segment content does not support null values
            if (value != null) {
                if (value instanceof ITmfStateValue) {
                    if (!((ITmfStateValue) value).isNull()) {
                        fields.put(field.getKey(), Objects.requireNonNull(((ITmfStateValue) value).unboxValue()));
                    }
                } else {
                    fields.put(field.getKey(), value);
                }
            }
        }

        // Set the start time
        if (fStart != null) {
            Object startVal = fStart.getValue(event, ITmfStateSystem.ROOT_ATTRIBUTE, scenarioInfo, container);
            if (startVal instanceof Number) {
                start = ((Number) startVal).longValue();
            }
        }

        // Set the end time
        if (fEnd != null) {
            Object endVal = fEnd.getValue(event, ITmfStateSystem.ROOT_ATTRIBUTE, scenarioInfo, container);
            if (endVal instanceof Number) {
                long endL = ((Number) endVal).longValue();
                end = endL >= start ? endL : end;
            }

        } else if (fDuration != null) {
            Object durationVal = fDuration.getValue(event, ITmfStateSystem.ROOT_ATTRIBUTE, scenarioInfo, container);
            if (durationVal instanceof Number) {
                long durationL = ((Number) durationVal).longValue();
                long endL = start + durationL;
                end = endL >= start ? endL : end;
            }
        }

        TmfXmlPatternSegment segment = new TmfXmlPatternSegment(start, end, String.valueOf(segmentName), fields);
        provider.getListener().onNewSegment(segment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fType, fStart, fDuration, fEnd, fFields);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof DataDrivenActionSegment)) {
            return false;
        }
        DataDrivenActionSegment other = (DataDrivenActionSegment) obj;
        return Objects.equals(fType, other.fType) &&
                Objects.equals(fStart, other.fStart) &&
                Objects.equals(fDuration, other.fDuration) &&
                Objects.equals(fEnd, other.fEnd) &&
                Objects.equals(fFields, other.fFields);
    }

    @Override
    public String toString() {
        return "Segment action " + fType + //$NON-NLS-1$
                (fStart == null ? StringUtils.EMPTY : " start: " + fStart + (fDuration == null ? " end: " + fEnd : " duration: " + fDuration)) + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                " fields: " + fFields; //$NON-NLS-1$
    }

}
