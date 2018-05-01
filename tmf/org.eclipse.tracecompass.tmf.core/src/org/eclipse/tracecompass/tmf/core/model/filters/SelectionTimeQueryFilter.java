/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.model.filters;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Standardized query filter to query XY data providers for a Collection of
 * entries. The selected items are the unique keys of the selected entries.
 *
 * @author Loic Prieur-Drevon
 * @since 4.0
 */
public class SelectionTimeQueryFilter extends TimeQueryFilter
        implements IMultipleSelectionQueryFilter<Collection<Long>> {

    private final Collection<Long> fItems;

    /**
     * Build a XYTimeQueryFilter
     *
     * @param start
     *            The starting value
     * @param end
     *            The ending value
     * @param n
     *            The number of entries
     * @param items
     *            The unique keys of the selected entries
     */
    public SelectionTimeQueryFilter(long start, long end, int n, Collection<Long> items) {
        super(start, end, n);
        fItems = ImmutableList.copyOf(items);
    }

    /**
     * Create a {@link SelectionTimeQueryFilter} from a sorted list of times.
     *
     * @param times
     *            sorted list of times to query.
     * @param items
     *            The unique keys of the selected entries.
     */
    public SelectionTimeQueryFilter(List<Long> times, Collection<Long> items) {
        super(times);
        fItems = ImmutableList.copyOf(items);
    }

    @Override
    public Collection<Long> getSelectedItems() {
        return fItems;
    }

}
