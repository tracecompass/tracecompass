/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/
package org.eclipse.tracecompass.internal.tmf.core.model.filters;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.timegraph.IFilterProperty;

import com.google.common.collect.Multimap;

/**
 * Standardized query filter to query time graph data providers for a Collection
 * of entries and filter the data using the given regex.
 *
 * @author Jean-Christian Kouame
 * @since 4.0
 *
 */
public class TimeGraphStateQueryFilter extends SelectionTimeQueryFilter {

    private Multimap<@NonNull Integer, @NonNull String> fRegexes;

    /**
     * Constructor
     *
     * @param times
     *            sorted list of times to query.
     * @param items
     *            The unique keys of the selected entries.
     * @param regexes
     *            The regexes use to filter the queried data. It is a multimap of
     *            filter strings by property. The data provider will use the filter
     *            strings to determine whether the property should be activated or
     *            not. See {@link IFilterProperty} for supported properties.
     */
    public TimeGraphStateQueryFilter(List<Long> times, Collection<Long> items, Multimap<@NonNull Integer, @NonNull String> regexes) {
        super(times, items);
        fRegexes = regexes;
    }

    /**
     * Constructor
     *
     * @param start
     *            The starting value
     * @param end
     *            The ending value
     * @param n
     *            The number of entries
     * @param items
     *            The unique keys of the selected entries
     * @param regexes
     *            The regexes use to filter the queried data. It is a multimap of
     *            filter strings by property. The data provider will use the filter
     *            strings to determine whether the property should be activated or
     *            not.
     */
    public TimeGraphStateQueryFilter(long start, long end, int n, Collection<Long> items, Multimap<@NonNull Integer, @NonNull String> regexes) {
        super(start, end, n, items);
        fRegexes = regexes;
    }

    /**
     * Get the regexes use to filter the queried data. It is a multimap of filter
     * strings by property. The data provider will use the filter strings to
     * determine whether the property should be activated or not.
     *
     * @return The multimap of regexes by property.
     */
    public Multimap<@NonNull Integer, @NonNull String> getRegexes() {
        return fRegexes;
    }

}
