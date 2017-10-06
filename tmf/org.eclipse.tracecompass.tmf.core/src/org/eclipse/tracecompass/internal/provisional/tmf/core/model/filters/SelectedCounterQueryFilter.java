/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters;

import java.util.Collection;

import com.google.common.collect.ImmutableList;

/**
 * This represents a specialized query filter used by some data providers. In
 * addition to base query filters, it encapsulated the selected quarks
 *
 * @author Mikael Ferland
 * @author Yonni Chen
 * @since 3.1
 */
public class SelectedCounterQueryFilter extends TimeQueryFilter implements ICumulativeQueryFilter, IMultipleSelectionQueryFilter<Collection<Long>> {

    /*
     * The set of selected quarks would be applied to a certain trace. We use
     * trace's UUID to identify trace
     */
    private final Collection<Long> fSelectedIds;
    private final boolean fIsCumulative;

    /**
     * Constructor. Given a start value, end value and n entries, this constructor
     * will set x values property to an array of n entries uniformly distributed and
     * ordered ascendingly.
     *
     * @param start
     *            The starting value
     * @param end
     *            The ending value
     * @param n
     *            The number of entries
     * @param ids
     *            The selected IDs
     * @param isCumulative
     *            To know if we want to fetch model as cumulative or differential.
     *            Give true if cumulative, false either
     */
    public SelectedCounterQueryFilter(long start, long end, int n, Collection<Long> ids, boolean isCumulative) {
        super(start, end, n);
        fSelectedIds = ImmutableList.copyOf(ids);
        fIsCumulative = isCumulative;
    }

    /**
     * Gets a set of selected quarks
     *
     * @return A set of quarks
     */
    @Override
    public Collection<Long> getSelectedItems() {
        return fSelectedIds;
    }

    @Override
    public boolean isCumulative() {
        return fIsCumulative;
    }
}
