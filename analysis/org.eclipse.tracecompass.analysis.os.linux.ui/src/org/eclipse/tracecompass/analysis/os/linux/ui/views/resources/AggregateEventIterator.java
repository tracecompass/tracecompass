/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.ui.views.resources;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;

/**
 * Aggregate TimeEvent iterator, this takes multiple streams of events and
 * merges them into one single time event stream
 *
 * @author Matthew Khouzam
 * @since 2.0
 */
public class AggregateEventIterator implements Iterator<@NonNull ITimeEvent> {

    private final List<@NonNull CachingIterator> fIterators = new ArrayList<>();

    private final Comparator<ITimeEvent> fComparator;

    /**
     * Constructor
     *
     * @param contributors
     *            the entries to aggregate
     * @param comparator
     *            The comparator to sort time events
     */
    public AggregateEventIterator(@NonNull List<ITimeGraphEntry> contributors, Comparator<ITimeEvent> comparator) {
        this(contributors, Long.MIN_VALUE, Long.MAX_VALUE, 1, comparator);
    }

    /**
     * Constructor with a time range
     *
     * @param contributors
     *            the entries to aggregate
     * @param startTime
     *            start time in nanoseconds
     * @param endTime
     *            stop time in nanoseconds
     * @param duration
     *            duration of one pixel in nanoseconds
     * @param comparator
     *            The comparator to sort time events
     */
    public AggregateEventIterator(@NonNull List<ITimeGraphEntry> contributors, long startTime, long endTime, long duration, Comparator<ITimeEvent> comparator) {
        fComparator = comparator;
        contributors.forEach(timeGraphEntry -> {
            final Iterator<@NonNull ITimeEvent> timeEventsIterator = timeGraphEntry.getTimeEventsIterator(startTime, endTime, duration);
            if (timeEventsIterator != null) {
                CachingIterator iterator = new CachingIterator(timeEventsIterator, comparator);
                if (iterator.hasNext()) {
                    fIterators.add(iterator);
                }
            }
        });
    }

    @Override
    public boolean hasNext() {
        return !fIterators.isEmpty();
    }

    @Override
    public @NonNull ITimeEvent next() {

        final List<@NonNull CachingIterator> iterators = fIterators;
        if (iterators.isEmpty()) {
            throw new NoSuchElementException("Aggregate iterator is empty"); //$NON-NLS-1$
        }

        ITimeEvent winner = iterators.get(0).peek();
        long trimTime = winner.getTime() + winner.getDuration();
        for (int i = 1; i < iterators.size(); i++) {
            CachingIterator iterator = iterators.get(i);
            ITimeEvent candidate = iterator.peek();
            if (candidate.getTime() < winner.getTime()) {
                trimTime = Math.min(winner.getTime(), candidate.getTime() + candidate.getDuration());
                winner = candidate;
            } else if (candidate.getTime() == winner.getTime()) {
                trimTime = Math.min(trimTime, candidate.getTime() + candidate.getDuration());
                if (fComparator.compare(candidate, winner) < 0) {
                    winner = candidate;
                }
            } else {
                trimTime = Math.min(trimTime, candidate.getTime());
            }
        }

        /* Trim the next event before the trim time, if necessary. */
        final ITimeEvent next = (trimTime < (winner.getDuration() + winner.getTime())) ? winner.splitBefore(trimTime) : winner;

        /* Trim all remaining events after the trim time, if necessary. */
        Iterator<CachingIterator> iteratorIterator = iterators.iterator();
        while (iteratorIterator.hasNext()) {
            CachingIterator iterator = iteratorIterator.next();
            iterator.trim(trimTime);
            /* Remove empty iterators from the list */
            if (!iterator.hasNext()) {
                iteratorIterator.remove();
            }
        }

        return checkNotNull(next);
    }
}