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

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.core.filter.TmfFilterHelper;
import org.eclipse.tracecompass.tmf.core.filter.ITmfFilter;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * A common class for all filters, either event filters or regexes
 *
 * @author Geneviève Bastien
 */
@NonNullByDefault
public class TraceCompassFilter {

    private final @Nullable ITmfFilter fEventFilter;
    private final Collection<String> fRegex;

    private static final Map<ITmfTrace, TraceCompassFilter> FILTER_MAP = new WeakHashMap<>();

    private TraceCompassFilter(@Nullable ITmfFilter filter, Collection<String> regex) {
        fEventFilter = filter;
        fRegex = regex;
    }

    /**
     * Factory method to get a new filter from an event filter
     *
     * @param filter
     *            The event filter from which to create this filter
     * @param trace
     *            The trace this filter applies to
     * @return A new filter
     */
    public synchronized static TraceCompassFilter fromEventFilter(ITmfFilter filter, ITmfTrace trace) {
        TraceCompassFilter traceCompassFilter = new TraceCompassFilter(filter, Collections.singleton(TmfFilterHelper.getRegexFromFilter(filter)));
        FILTER_MAP.put(trace, traceCompassFilter);
        return traceCompassFilter;
    }

    /**
     * Factory method to get a new filter from a regex
     *
     * @param regex
     *            The regex from which to create the filter
     * @param trace
     *            The trace this filter applies to
     * @return A new filter
     */
    public synchronized static TraceCompassFilter fromRegex(Collection<String> regex, ITmfTrace trace) {
        ITmfFilter filter = TmfFilterHelper.buildFilterFromRegex(regex, trace);
        TraceCompassFilter traceCompassFilter = new TraceCompassFilter(filter, regex);
        FILTER_MAP.put(trace, traceCompassFilter);
        return traceCompassFilter;
    }

    /**
     * Get the event filter being applied. This filter should be applied on data
     * sources based on events. For other types of data sources, use the
     * {@link #getRegexes()} method to get the filter string.
     *
     * @return The filter, or <code>null</code> if filters should be removed
     */
    public @Nullable ITmfFilter getEventFilter() {
        return fEventFilter;
    }

    /**
     * Get the filter regexes, that should be used to filter data from a source
     * that is not based on event. For event-based filters, use
     * {@link #getEventFilter()} instead
     *
     * @return The regex to filter anything that is not event-based
     */
    public Collection<String> getRegexes() {
        return fRegex;
    }

    /**
     * Remove filters for the closed trace
     *
     * @param signal
     *            The trace closed signal
     */
    @TmfSignalHandler
    public synchronized static void traceClosed(final TmfTraceClosedSignal signal) {
        FILTER_MAP.remove(signal.getTrace());
    }

    /**
     * Get the filter that is active for a given trace
     *
     * @param trace
     *            The trace to get the filter for
     * @return The filter to apply, or <code>null</code> if no filter is set
     */
    public static @Nullable TraceCompassFilter getFilterForTrace(ITmfTrace trace) {
        return FILTER_MAP.get(trace);
    }

}
