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
import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * This represents a specialized query filter used by data some providers. In
 * addition to base query filters, it encapsulated the selected thread and
 * selected CPUs.
 *
 * @author Yonni Chen
 * @since 4.0
 */
public class SelectedCpuQueryFilter extends SelectionTimeQueryFilter {

    private final Set<Integer> fCpus;

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
     * @param selectedThreads
     *            The selected threads
     * @param cpu
     *            The set of CPU
     */
    public SelectedCpuQueryFilter(long start, long end, int n, Collection<Long> selectedThreads, Set<Integer> cpu) {
        super(start, end, n, selectedThreads);
        fCpus = ImmutableSet.copyOf(cpu);
    }

    /**
     * Constructor.
     *
     * @param times
     *            Sorted list of times to query.
     * @param selectedThreads
     *            The selected threads
     * @param cpu
     *            The set of CPU
     * @since 5.0
     */
    public SelectedCpuQueryFilter(List<Long> times, Collection<Long> selectedThreads, Set<Integer> cpu) {
        super(times, selectedThreads);
        fCpus = ImmutableSet.copyOf(cpu);
    }

    /**
     * Gets a set of selected CPUs
     *
     * @return A set of cpu id
     */
    public Set<Integer> getSelectedCpus() {
        return fCpus;
    }
}
