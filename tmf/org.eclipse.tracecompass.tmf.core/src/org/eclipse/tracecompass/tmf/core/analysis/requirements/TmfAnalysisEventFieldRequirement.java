/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.analysis.requirements;

import java.util.Collection;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTraceWithPreDefinedEvents;
import org.eclipse.tracecompass.tmf.core.trace.TmfEventTypeCollectionHelper;

import com.google.common.collect.Multimap;

/**
 * An analysis requirement for event fields of a given event name.
 *
 * @author Bernd Hufmann
 * @since 2.0
 */
public class TmfAnalysisEventFieldRequirement extends TmfAnalysisRequirement {

    /** The type of requirement */
    private static final String TYPE_EVENT_FIELD = "field"; //$NON-NLS-1$

    /** The event name of the event containing the mandatory fields */
    private String fEventName;

    /**
     * Constructor
     *
     * @param eventName
     *            The event name of the event containing the mandatory fields.
     *            Empty string will indicate that all events need to have the
     *            fields.
     * @param fields
     *            The list of event field names the trace must have
     */
    public TmfAnalysisEventFieldRequirement(String eventName, Collection<String> fields) {
        this(eventName, fields, ValuePriorityLevel.OPTIONAL);
    }

    /**
     * Constructor. Instantiate a requirement object with a list of values of
     * the same level
     *
     * @param eventName
     *            The event name of the event containing the mandatory fields.
     *            Empty string will indicate that all events need to have the
     *            fields.
     * @param fields
     *            The list of event field names the trace must have
     * @param level
     *            A level associated with all the values
     * @throws IllegalArgumentException if no event names are provided
     */
    public TmfAnalysisEventFieldRequirement(String eventName, Collection<String> fields, ValuePriorityLevel level) {
        super(TYPE_EVENT_FIELD, fields, level);
        fEventName = eventName;
    }

    @Override
    public boolean test(ITmfTrace trace) {
        if ((trace instanceof ITmfTraceWithPreDefinedEvents)) {
            Set<String> mandatoryValues = getValues(ValuePriorityLevel.MANDATORY);
            if (mandatoryValues.isEmpty()) {
                return true;
            }
            Multimap<@NonNull String, @NonNull String> traceEvents =
                    TmfEventTypeCollectionHelper.getEventFieldNames((((ITmfTraceWithPreDefinedEvents) trace).getContainedEventTypes()));

            if (fEventName.isEmpty()) {
                return traceEvents.keys().stream().allMatch(eventName -> {
                    Collection<@NonNull String> fields = traceEvents.get(fEventName);
                    return fields.containsAll(mandatoryValues);
                });
            }
            Collection<@NonNull String> fields = traceEvents.get(fEventName);
            return fields.containsAll(mandatoryValues);
        }
        return true;
    }

}
