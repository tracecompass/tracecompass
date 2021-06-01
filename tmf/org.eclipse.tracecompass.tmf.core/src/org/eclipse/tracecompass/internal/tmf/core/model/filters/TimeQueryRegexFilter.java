/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
package org.eclipse.tracecompass.internal.tmf.core.model.filters;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;

import com.google.common.collect.Multimap;

/**
 * This represents a time query filter with a regex filter on the data.
 *
 * @author Jean-Christian Kouame
 *
 */
public class TimeQueryRegexFilter extends TimeQueryFilter implements IRegexQuery {

    private final Multimap<@NonNull Integer, @NonNull String> fRegexes;

    /**
     * Constructor
     *
     * @param start
     *                    The starting value
     * @param end
     *                    The ending value
     * @param n
     *                    The number of entries
     * @param regexes
     *                    The regexes use to filter the queried data. It is a
     *                    multimap of filter strings by property. The data provider
     *                    will use the filter strings to determine whether the
     *                    property should be activated or not. See
     *                    {@link IFilterProperty} for supported properties.
     */
    public TimeQueryRegexFilter(long start, long end, int n, Multimap<@NonNull Integer, @NonNull String> regexes) {
        super(start, end, n);
        fRegexes = regexes;
    }

    /**
     * Constructor
     *
     * @param times
     *                    sorted list of times to query.
     * @param regexes
     *                    The regexes use to filter the queried data. It is a
     *                    multimap of filter strings by property. The data provider
     *                    will use the filter strings to determine whether the
     *                    property should be activated or not. See
     *                    {@link IFilterProperty} for supported properties.
     */
    public TimeQueryRegexFilter(List<Long> times, Multimap<@NonNull Integer, @NonNull String> regexes) {
        super(times);
        fRegexes = regexes;
    }

    @Override
    public Multimap<@NonNull Integer, @NonNull String> getRegexes() {
        return fRegexes;
    }
}
