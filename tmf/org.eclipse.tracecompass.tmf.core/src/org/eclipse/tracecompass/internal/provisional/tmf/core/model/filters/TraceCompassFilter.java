/*******************************************************************************
 * Copyright (c) 2018 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.internal.tmf.core.filter.TmfFilterHelper;
import org.eclipse.tracecompass.tmf.core.filter.ITmfFilter;

/**
 * A common class for all filters, either event filters or regexes
 *
 * @author Geneviève Bastien
 */
@NonNullByDefault
public class TraceCompassFilter {

    private final ITmfFilter fEventFilter;
    private final String fRegex;

    private TraceCompassFilter(ITmfFilter filter, String regex) {
        fEventFilter = filter;
        fRegex = regex;
    }

    /**
     * Factory method to get a new filter from an event filter
     *
     * @param filter
     *            The event filter from which to create this filter
     * @return A new filter
     */
    public static TraceCompassFilter fromEventFilter(ITmfFilter filter) {
        return new TraceCompassFilter(filter, TmfFilterHelper.getRegexFromFilter(filter));
    }

    /**
     * Factory method to get a new filter from a regex
     *
     * @param regex
     *            The regex from which to create the filter
     * @return A new filter
     */
    public static TraceCompassFilter fromRegex(String regex) {
        return new TraceCompassFilter(TmfFilterHelper.buildFilterFromRegex(regex), regex);
    }

    /**
     * Get the event filter being applied. This filter should be applied on data
     * sources based on events. For other types of data sources, use the
     * {@link #getRegex()} method to get the filter string.
     *
     * @return The filter
     */
    public ITmfFilter getEventFilter() {
        return fEventFilter;
    }

    /**
     * Get the filter regex, that should be used to filter data from a source
     * that is not based on event. For event-based filters, use
     * {@link #getEventFilter()} instead
     *
     * @return The regex to filter anything that is not event-based
     */
    public String getRegex() {
        return fRegex;
    }

}
