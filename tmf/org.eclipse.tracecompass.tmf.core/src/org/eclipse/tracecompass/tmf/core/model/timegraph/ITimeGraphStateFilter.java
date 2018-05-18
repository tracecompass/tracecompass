/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/
package org.eclipse.tracecompass.tmf.core.model.timegraph;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectionTimeQueryFilter;

/**
 * Implements timegraph state filtering support. This interface provide default
 * method to provide the inputs and do the filtering.
 *
 * @author Jean-Christian Kouame
 * @since 4.0
 */
public interface ITimeGraphStateFilter {

    /**
     * Filter the time graph state and add it to the state list
     *
     * @param stateList
     *            The timegraph state list
     * @param timeGraphState
     *            The current timegraph state
     * @param key
     *            The timegraph entry model id
     * @param predicates
     *            The predicates used to filter the timegraph state. It is a map of
     *            predicate by property. The value of the property is an integer
     *            representing a bitmask associated to that property. The status of
     *            each property will be set for the timegraph state according to the
     *            associated predicate test result.
     * @param monitor
     *            The progress monitor
     */
    default void addToStateList(List<ITimeGraphState> stateList, ITimeGraphState timeGraphState, Long key, Map<Integer, Predicate<Map<String, String>>> predicates, @Nullable IProgressMonitor monitor) {

        if (!predicates.isEmpty()) {
            // Get the filter external input data
            long startTime = timeGraphState.getStartTime();
            SelectionTimeQueryFilter filter = new SelectionTimeQueryFilter(Collections.singletonList(startTime), Collections.singleton(Objects.requireNonNull(key)));
            Map<@NonNull String, @NonNull String> input = getFilterInput(filter, monitor);
            input.putAll(timeGraphState.computeData());

            // Test each predicates and set the status of the property associated to the
            // predicate
            for (Map.Entry<Integer, Predicate<Map<String, String>>> mapEntry : predicates.entrySet()) {
                Predicate<Map<String, String>> value = Objects.requireNonNull(mapEntry.getValue());
                boolean status = value.test(input);
                Integer property = Objects.requireNonNull(mapEntry.getKey());
                if (property == IFilterProperty.DIMMED || property == IFilterProperty.EXCLUDE) {
                    timeGraphState.setProperty(property, !status);
                } else {
                    timeGraphState.setProperty(property, status);
                }
            }
        }

        if (timeGraphState.isPropertyActive(IFilterProperty.EXCLUDE)) {
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
     * @param filter
     *            The selection time query filter
     * @param monitor
     *            The progress monitor
     * @return The map of input data
     */
    default Map<String, String> getFilterInput(SelectionTimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        return new HashMap<>();
    }
}
