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
 * Selection query filter implementation with a given regex to filter the data.
 *
 * @author Jean-Christian Kouame
 *
 */
public class SelectionTimeQueryRegexFilter extends SelectionTimeQueryFilter implements IRegexQuery {

    private final Multimap<@NonNull Integer, @NonNull String> fRegexes;

    /**
     * Build a XYTimeQueryFilter
     *
     * @param start
     *                    The starting value
     * @param end
     *                    The ending value
     * @param n
     *                    The number of entries
     * @param items
     *                    The unique keys of the selected entries
     * @param regexes
     *                    The regexes use to filter the queried data. It is a
     *                    multimap of filter strings by property. The data provider
     *                    will use the filter strings to determine whether the
     *                    property should be activated or not. See
     *                    {@link IFilterProperty} for supported properties.
     */
    public SelectionTimeQueryRegexFilter(long start, long end, int n, Collection<Long> items, Multimap<@NonNull Integer, @NonNull String> regexes) {
        super(start, end, n, items);
        fRegexes = regexes;
    }

    /**
     * Create a {@link SelectionTimeQueryFilter} from a sorted list of times.
     *
     * @param times
     *                    sorted list of times to query.
     * @param items
     *                    The unique keys of the selected entries.
     * @param regexes
     *                    The regexes use to filter the queried data. It is a
     *                    multimap of filter strings by property. The data provider
     *                    will use the filter strings to determine whether the
     *                    property should be activated or not. See
     *                    {@link IFilterProperty} for supported properties.
     */
    public SelectionTimeQueryRegexFilter(List<Long> times, Collection<Long> items, Multimap<@NonNull Integer, @NonNull String> regexes) {
        super(times, items);
        fRegexes = regexes;
    }

    @Override
    public Multimap<Integer, String> getRegexes() {
        return fRegexes;
    }

}
