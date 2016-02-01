/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.ui.tests.view.resources;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.analysis.os.linux.ui.views.resources.AggregateEventIterator;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NullTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

/**
 * Test class to verify the main cases of the aggregate iterator
 *
 * @author Matthew Khouzam
 *
 */
@NonNullByDefault
public class AggregateIteratorTest {

    /**
     * <pre>
     * ----------X---------
     * </pre>
     */
    private final ITimeGraphEntry t1 = new StubTimeGraphEntry(ImmutableList.of(
            new NullTimeEvent(null, 0, 10),
            new TimeEvent(null, 10, 1, 1),
            new NullTimeEvent(null, 11, 9)));
    /**
     * <pre>
     * -----------X--------
     * </pre>
     */
    private final ITimeGraphEntry t2 = new StubTimeGraphEntry(ImmutableList.of(
            new NullTimeEvent(null, 0, 12),
            new TimeEvent(null, 12, 1, 1),
            new NullTimeEvent(null, 13, 7)));
    /**
     * <pre>
     * --------X-----------
     * </pre>
     */
    private final ITimeGraphEntry t3 = new StubTimeGraphEntry(ImmutableList.of(
            new NullTimeEvent(null, 0, 9),
            new TimeEvent(null, 9, 1, 1),
            new NullTimeEvent(null, 10, 10)));

    /**
     * <pre>
     * --------XXX---------
     * </pre>
     */
    private final ITimeGraphEntry t4 = new StubTimeGraphEntry(ImmutableList.of(
            new NullTimeEvent(null, 0, 9),
            new TimeEvent(null, 9, 3, 1),
            new NullTimeEvent(null, 12, 8)));

    /**
     * <pre>
     * ----------XXX-------
     * </pre>
     */
    private final ITimeGraphEntry t5 = new StubTimeGraphEntry(ImmutableList.of(
            new NullTimeEvent(null, 0, 10),
            new TimeEvent(null, 10, 3, 1),
            new NullTimeEvent(null, 13, 7)));

    /**
     * <pre>
     * ----------XXX-------
     * </pre>
     */
    private final ITimeGraphEntry t6 = new StubTimeGraphEntry(ImmutableList.of(
            new NullTimeEvent(null, 0, 10),
            new TimeEvent(null, 10, 3, 1),
            new NullTimeEvent(null, 13, 7)));

    /**
     * <pre>
     * XXXXXXXXXXXXXXXXXXXX
     * </pre>
     */
    private final ITimeGraphEntry t7 = new StubTimeGraphEntry(ImmutableList.of(
            new TimeEvent(null, 0, 20, 1)));

    /**
     * Test non-overlapping intervals
     */
    @Test
    public void testNoOverlap() {
        List<ITimeEvent> expected = ImmutableList.of(new NullTimeEvent(null, 0, 9), new TimeEvent(null, 9, 1, 1), new TimeEvent(null, 10, 1, 1), new NullTimeEvent(null, 11, 1), new TimeEvent(null, 12, 1, 1), new NullTimeEvent(null, 13, 7));
        AggregateEventIterator fixture = new AggregateEventIterator(ImmutableList.of(t1, t2, t3), COMPARATOR);
        runTest(expected, fixture);
    }

    /**
     * Test intervals with 2 events that partially overlap
     */
    @Test
    public void testPartialOverlap() {
        List<ITimeEvent> expected = ImmutableList.of(new NullTimeEvent(null, 0, 9), new TimeEvent(null, 9, 1, 1), new TimeEvent(null, 10, 2, 1), new TimeEvent(null, 12, 1, 1), new NullTimeEvent(null, 13, 7));
        AggregateEventIterator fixture = new AggregateEventIterator(ImmutableList.of(t4, t5), COMPARATOR);
        runTest(expected, fixture);
    }

    /**
     * Test two iterators that are identical, it will give the same result
     */
    @Test
    public void testFullOverlap() {
        List<ITimeEvent> expected = ImmutableList.of(new NullTimeEvent(null, 0, 10), new TimeEvent(null, 10, 3, 1), new NullTimeEvent(null, 13, 7));
        AggregateEventIterator fixture = new AggregateEventIterator(ImmutableList.of(t6, t5), COMPARATOR);
        runTest(expected, fixture);
    }

    /**
     * Test two iterators that are identical, it will give the same result
     */
    @Test
    public void testSameStartOverlap() {
        List<ITimeEvent> expected = ImmutableList.of(new NullTimeEvent(null, 0, 9), new TimeEvent(null, 9, 1, 1), new TimeEvent(null, 10, 2, 1), new NullTimeEvent(null, 12, 8));
        AggregateEventIterator fixture = new AggregateEventIterator(ImmutableList.of(t3, t4), COMPARATOR);
        runTest(expected, fixture);
    }

    /**
     * Test two iterators that are identical, it will give the same result
     */
    @Test
    public void testSameEndOverlap() {
        List<ITimeEvent> expected = ImmutableList.of(new NullTimeEvent(null, 0, 10), new TimeEvent(null, 10, 2, 1), new TimeEvent(null, 12, 1, 1), new NullTimeEvent(null, 13, 7));
        AggregateEventIterator fixture = new AggregateEventIterator(ImmutableList.of(t5, t2), COMPARATOR);
        runTest(expected, fixture);
    }

    /**
     * Test two iterators where one only has one HUGE event
     */
    @Test
    public void testOverlapEnglobing() {
        List<ITimeEvent> expected = ImmutableList.of(new TimeEvent(null, 0, 10, 1), new TimeEvent(null, 10, 1, 1), new TimeEvent(null, 11, 9, 1));
        AggregateEventIterator fixture = new AggregateEventIterator(ImmutableList.of(t1, t7), COMPARATOR);
        runTest(expected, fixture);
    }

    private static void runTest(List<ITimeEvent> expected, AggregateEventIterator fixture) {
        List<ITimeEvent> results = new ArrayList<>();
        Iterators.addAll(results, fixture);
        assertEquals(expected.size(), results.size());
        for (int i = 0; i < expected.size(); i++) {
            final @NonNull TimeEvent actual = (@NonNull TimeEvent) results.get(i);
            final @NonNull TimeEvent expected2 = (@NonNull TimeEvent) expected.get(i);
            final @NonNull String name = Integer.toString(i);
            assertEquals(name, expected2.getClass(), actual.getClass());
            assertEquals(name, expected2.getDuration(), actual.getDuration());
            assertEquals(name, expected2.getTime(), actual.getTime());
            assertEquals(name, expected2.getEntry(), actual.getEntry());
            assertEquals(name, expected2.getValue(), actual.getValue());
        }
    }

    static class StubTimeGraphEntry extends TimeGraphEntry {

        private final Iterable<@NonNull ITimeEvent> fEvents;

        public StubTimeGraphEntry(Iterable<ITimeEvent> events) {
            super("stub", Long.MIN_VALUE, Long.MAX_VALUE);
            fEvents = events;
        }

        @Override
        public boolean hasTimeEvents() {
            return !Iterables.isEmpty(fEvents);
        }

        // for a stub, not worth making it work.
        @SuppressWarnings("null")
        @Override
        public Iterator<@NonNull ITimeEvent> getTimeEventsIterator() {
            return fEvents.iterator();
        }

        @Override
        public Iterator<@NonNull ITimeEvent> getTimeEventsIterator(long startTime, long stopTime, long visibleDuration) {
            if (startTime != Long.MIN_VALUE || stopTime != Long.MAX_VALUE) {
                throw new IllegalArgumentException("startTime must be Long.MIN_VALUE, stopTime must be Long.MAX_VALUE");
            }
            return getTimeEventsIterator();
        }

    }

    /**
     * Same as in AggregateResourcesEntry but we want a snapshot and to not
     * update the tests at every new mode of resources view
     */
    private static final Comparator<ITimeEvent> COMPARATOR = new Comparator<ITimeEvent>() {
        @Override
        public int compare(ITimeEvent o1, ITimeEvent o2) {
            // largest value
            return Integer.compare(getValue(o2), getValue(o1));
        }

        private int getValue(ITimeEvent element) {
            return (element instanceof TimeEvent) ? ((TimeEvent) element).getValue() : Integer.MIN_VALUE;
        }
    };

}
