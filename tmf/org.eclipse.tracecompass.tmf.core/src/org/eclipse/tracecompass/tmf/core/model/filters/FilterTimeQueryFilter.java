/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.model.filters;

/**
 * A {@link TimeQueryFilter} with an additional field to tell the provider to
 * filter its data or not.
 *
 * @author Loic Prieur-Drevon
 * @since 4.0
 */
public class FilterTimeQueryFilter extends TimeQueryFilter {

    private final boolean fFiltered;

    /**
     * Constructor. Given a start value, end value and n entries, this constructor
     * will set its property to an array of n entries uniformly distributed and
     * ordered ascendingly.
     *
     * @param start
     *            The starting value
     * @param end
     *            The ending value
     * @param n
     *            The number of entries
     * @param filtered
     *            if the provider should filter the returned data.
     **/
    public FilterTimeQueryFilter(long start, long end, int n, boolean filtered) {
        super(start, end, n);
        fFiltered = filtered;
    }

    /**
     * Get the filtering status
     *
     * @return if the provider should filter the returned data.
     */
    public boolean isFiltered() {
        return fFiltered;
    }

}
