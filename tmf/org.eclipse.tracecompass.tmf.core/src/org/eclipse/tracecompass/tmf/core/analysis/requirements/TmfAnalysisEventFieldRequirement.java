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
import java.util.HashSet;
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
public class TmfAnalysisEventFieldRequirement extends TmfAbstractAnalysisRequirement {

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
        this(eventName, fields, PriorityLevel.OPTIONAL);
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
    public TmfAnalysisEventFieldRequirement(String eventName, Collection<String> fields, PriorityLevel level) {
        super(fields, level);
        fEventName = eventName;
    }

    @Override
    public boolean test(ITmfTrace trace) {

        if (!(trace instanceof ITmfTraceWithPreDefinedEvents)) {
            return true;
        }

        Set<String> values = getValues();
        if (values.isEmpty()) {
            return true;
        }

        final Multimap<@NonNull String, @NonNull String> traceEvents = TmfEventTypeCollectionHelper.getEventFieldNames((((ITmfTraceWithPreDefinedEvents) trace).getContainedEventTypes()));

        if (fEventName.isEmpty()) {
            switch(getPriorityLevel()) {
            case ALL_OR_NOTHING:
                return traceEvents.keys().stream().allMatch(eventName -> {
                    Collection<@NonNull String> fields = new HashSet<>(traceEvents.get(eventName));
                    fields.retainAll(values);
                    return (fields.isEmpty() || fields.size() == values.size());
                });
            case AT_LEAST_ONE:
                return traceEvents.keys().stream().allMatch(eventName -> {
                    Collection<@NonNull String> fields = new HashSet<>(traceEvents.get(eventName));
                    fields.retainAll(values);
                    return !fields.isEmpty();
                });
            case MANDATORY:
                return traceEvents.keys().stream().allMatch(eventName -> {
                    Collection<@NonNull String> fields = traceEvents.get(eventName);
                    return fields.containsAll(values);
                });
            case OPTIONAL:
                return true;
            default:
                throw new IllegalStateException("Unknown value level: " + getPriorityLevel()); //$NON-NLS-1$
            }
        }

        // Check the level for required event only
        Collection<@NonNull String> fields = traceEvents.get(fEventName);
        switch(getPriorityLevel()) {
        case ALL_OR_NOTHING:
            fields.retainAll(values);
            return (fields.isEmpty() || fields.size() == values.size());
        case AT_LEAST_ONE:
            fields.retainAll(values);
            return !fields.isEmpty();
        case MANDATORY:
            return fields.containsAll(values);
        case OPTIONAL:
            return true;
        default:
            throw new IllegalStateException("Unknown value level: " + getPriorityLevel()); //$NON-NLS-1$
        }

    }

}
