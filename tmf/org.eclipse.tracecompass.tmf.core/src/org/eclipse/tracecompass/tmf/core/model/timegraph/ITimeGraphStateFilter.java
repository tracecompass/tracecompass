/**********************************************************************
 * Copyright (c) 2018, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
package org.eclipse.tracecompass.tmf.core.model.timegraph;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.model.CoreFilterProperty;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

/**
 * Implements timegraph state filtering support. This interface provide default
 * method to provide the inputs and do the filtering.
 *
 * @author Jean-Christian Kouame
 * @since 4.0
 */
public interface ITimeGraphStateFilter {

    /**
     * Filter the time graph state and add it to the state list.
     *
     * @param stateList
     *            The timegraph state list
     * @param timeGraphState
     *            The current timegraph state
     * @param entryId
     *            The timegraph entry model id
     * @param predicates
     *            The predicates used to filter the timegraph state. It is a map
     *            of predicate by property. The value of the property is an
     *            integer representing a bitmask associated to that property.
     *            The status of each property will be set for the timegraph
     *            state according to the associated predicate test result.
     * @param monitor
     *            The progress monitor
     * @since 5.0
     */
    default void applyFilterAndAddState(List<ITimeGraphState> stateList, ITimeGraphState timeGraphState, Long entryId, Map<Integer, Predicate<Multimap<String, Object>>> predicates, @Nullable IProgressMonitor monitor) {
        applyFilterAndAddState(stateList, timeGraphState, entryId, predicates, null, monitor);
    }

    /**
     * Filter the time graph state and add it to the state list.
     * <p>
     * If a list of times is specified, all states that intersect a time in the
     * list are included. States that do not intersect any time (e.g. in gap)
     * are only included if they are neither dimmed nor excluded, and a maximum
     * of one state per gap is included. The method must be called in
     * chronological order of states, otherwise the result is unspecified.
     *
     * @param stateList
     *            The timegraph state list
     * @param timeGraphState
     *            The current timegraph state
     * @param entryId
     *            The timegraph entry model id
     * @param predicates
     *            The predicates used to filter the timegraph state. It is a map
     *            of predicate by property. The value of the property is an
     *            integer representing a bitmask associated to that property.
     *            The status of each property will be set for the timegraph
     *            state according to the associated predicate test result.
     * @param times
     *            The list of times that should be included in the state list.
     *            If set to <code>null</code>, all matching states should be
     *            included.
     * @param monitor
     *            The progress monitor
     * @since 5.2
     */
    default void applyFilterAndAddState(List<ITimeGraphState> stateList, ITimeGraphState timeGraphState, Long entryId, Map<Integer, Predicate<Multimap<String, Object>>> predicates, @Nullable List<Long> times, @Nullable IProgressMonitor monitor) {

        long startTime = timeGraphState.getStartTime();
        long endTime = timeGraphState.getStartTime() + timeGraphState.getDuration();

        // Discard all additional states in same gap after first match
        if (times != null && !stateList.isEmpty()) {
            ITimeGraphState lastState = Iterables.getLast(stateList);
            long lastStart = lastState.getStartTime();
            long lastEnd = lastStart + lastState.getDuration();
            int index = Collections.binarySearch(times, lastStart);
            if (index < 0) {
                index = -index - 1;
                if (index >= times.size()) {
                    // Skip all after the last query time
                    return;
                }
                if (lastEnd < times.get(index) && endTime <= times.get(index)) {
                    // If last state was in gap, skip all others in same gap
                    return;
                }
            }
        }

        if (!predicates.isEmpty()) {
            // Get the filter external input data
            Multimap<@NonNull String, @NonNull Object> input = HashMultimap.create();
            input.putAll(getFilterData(entryId, startTime, monitor));
            input.putAll(timeGraphState.getMetadata());

            // Test each predicates and set the status of the property
            // associated to the predicate
            for (Map.Entry<Integer, Predicate<Multimap<String, Object>>> mapEntry : predicates.entrySet()) {
                Predicate<Multimap<String, Object>> value = Objects.requireNonNull(mapEntry.getValue());
                boolean status = value.test(input);
                Integer property = Objects.requireNonNull(mapEntry.getKey());
                if (property == CoreFilterProperty.DIMMED || property == CoreFilterProperty.EXCLUDE) {
                    timeGraphState.setProperty(property, !status);
                } else {
                    timeGraphState.setProperty(property, status);
                }
            }
        }

        if (times != null && (timeGraphState.getActiveProperties() & (CoreFilterProperty.DIMMED | CoreFilterProperty.EXCLUDE)) != 0) {
            // Do not include state in gap if it is dimmed or excluded
            int index = Collections.binarySearch(times, startTime);
            if (index < 0) {
                index = -index - 1;
                if (index >= times.size() || (endTime < times.get(index))) {
                    return;
                }
            }
        }

        if (timeGraphState.isPropertyActive(CoreFilterProperty.EXCLUDE)) {
            // Replace excluded state with a null state
            TimeGraphState timeGraphState2 = new TimeGraphState(timeGraphState.getStartTime(), timeGraphState.getDuration(), Integer.MIN_VALUE);
            timeGraphState2.setActiveProperties(timeGraphState.getActiveProperties());
            stateList.add(timeGraphState2);
        } else {
            stateList.add(timeGraphState);
        }
    }

    /**
     * Get input data used for filtering
     *
     * @param entryId
     *            The ID of the entry
     * @param time
     *            The time at which to get data
     * @param monitor
     *            The progress monitor
     * @return The map of input data
     * @since 5.0
     */
    default Multimap<String, Object> getFilterData(long entryId, long time, @Nullable IProgressMonitor monitor) {
        return ImmutableMultimap.of();
    }

    /**
     * Merge multiple multimaps into one. The resulting map is not immutable so
     * callers can fill it with additional data.
     *
     * @param maps
     *            The maps to merge
     * @return A multimap that is the result of the merge of the maps in
     *         arguments
     * @since 5.0
     */
    @SafeVarargs
    static Multimap<String, Object> mergeMultimaps(Multimap<String, Object>... maps) {
        Multimap<@NonNull String, @NonNull Object> data = HashMultimap.create();
        for (Multimap<String, Object> multimap : maps) {
            data.putAll(multimap);
        }
        return data;
    }
}
