/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.tests.widgets.timegraph.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NullTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;
import org.junit.Test;

import com.google.common.collect.Iterators;

/**
 * Test the {@link TimeGraphEntry} class
 */
public class TimeGraphEntryTest {

    private final static String NAME = "name";

    /**
     * Test method addEvent.
     */
    @Test
    public void testAddEvent() {
        TimeGraphEntry entry = new TimeGraphEntry(NAME, SWT.DEFAULT, SWT.DEFAULT);
        ITimeEvent event1 = new TimeEvent(entry, 0, 10, 1);
        ITimeEvent event2 = new TimeEvent(entry, 10, 10, 2);
        entry.addEvent(event1);
        entry.addEvent(event2);
        assertEquals(0, entry.getStartTime());
        assertEquals(20, entry.getEndTime());
        assertIteratorsEqual(Iterators.forArray(event1, event2), entry.getTimeEventsIterator());
    }

    /**
     * Test method addEvent with replaced last event.
     */
    @Test
    public void testAddEventReplaceLast() {
        TimeGraphEntry entry = new TimeGraphEntry(NAME, SWT.DEFAULT, SWT.DEFAULT);
        ITimeEvent event1 = new TimeEvent(entry, 0, 10, 1);
        ITimeEvent event2 = new TimeEvent(entry, 10, 10, 2);
        ITimeEvent event3a = new TimeEvent(entry, 20, 5, 3);
        ITimeEvent event3 = new TimeEvent(entry, 20, 10, 3);
        entry.addEvent(event1);
        entry.addEvent(event2);
        entry.addEvent(event3a);
        // last event is replaced
        entry.addEvent(event3);
        assertEquals(0, entry.getStartTime());
        assertEquals(30, entry.getEndTime());
        assertIteratorsEqual(Iterators.forArray(event1, event2, event3), entry.getTimeEventsIterator());
    }

    /**
     * Test method addEvent with null events.
     */
    @Test
    public void testAddEventNulls() {
        TimeGraphEntry entry = new TimeGraphEntry(NAME, SWT.DEFAULT, SWT.DEFAULT);
        ITimeEvent event1 = new NullTimeEvent(entry, 0, 10);
        ITimeEvent event2 = new TimeEvent(entry, 10, 10, 2);
        ITimeEvent event3 = new NullTimeEvent(entry, 20, 10);
        // null events do not affect start and end time
        entry.addEvent(event1);
        entry.addEvent(event2);
        entry.addEvent(event3);
        assertEquals(10, entry.getStartTime());
        assertEquals(20, entry.getEndTime());
        assertIteratorsEqual(Iterators.forArray(event1, event2, event3), entry.getTimeEventsIterator());
    }
    /**
     * Test method addZoomedEvent.
     */
    @Test
    public void testaddZoomedEvent() {
        TimeGraphEntry entry = new TimeGraphEntry(NAME, SWT.DEFAULT, SWT.DEFAULT);
        ITimeEvent event1 = new TimeEvent(entry, 0, 10, 1);
        ITimeEvent event2 = new TimeEvent(entry, 10, 10, 2);
        entry.addZoomedEvent(event1);
        entry.addZoomedEvent(event2);
        assertEquals(0, entry.getStartTime());
        assertEquals(20, entry.getEndTime());
        assertIteratorsEqual(Iterators.forArray(event1, event2), entry.getTimeEventsIterator());
    }

    /**
     * Test method addZoomedEvent with duplicates.
     */
    @Test
    public void testaddZoomedEventDuplicate() {
        TimeGraphEntry entry = new TimeGraphEntry(NAME, SWT.DEFAULT, SWT.DEFAULT);
        ITimeEvent event1 = new TimeEvent(entry, 0, 10, 1);
        ITimeEvent event2 = new TimeEvent(entry, 10, 10, 2);
        ITimeEvent event3 = new TimeEvent(entry, 20, 10, 3);
        entry.addZoomedEvent(event1);
        entry.addZoomedEvent(event2);
        // duplicate events are not added twice
        entry.addZoomedEvent(event1);
        entry.addZoomedEvent(event2);
        entry.addZoomedEvent(event3);
        assertEquals(0, entry.getStartTime());
        assertEquals(30, entry.getEndTime());
        assertIteratorsEqual(Iterators.forArray(event1, event2, event3), entry.getTimeEventsIterator());
    }

    /**
     * Test method addZoomedEvent with replaced last event.
     */
    @Test
    public void testaddZoomedEventReplaceLast() {
        TimeGraphEntry entry = new TimeGraphEntry(NAME, SWT.DEFAULT, SWT.DEFAULT);
        ITimeEvent event1 = new TimeEvent(entry, 0, 10, 1);
        ITimeEvent event2 = new TimeEvent(entry, 10, 10, 2);
        ITimeEvent event3a = new TimeEvent(entry, 20, 5, 3);
        ITimeEvent event3 = new TimeEvent(entry, 20, 10, 3);
        entry.addZoomedEvent(event1);
        entry.addZoomedEvent(event2);
        entry.addZoomedEvent(event3a);
        entry.addZoomedEvent(event1);
        entry.addZoomedEvent(event2);
        // last event is replaced
        entry.addZoomedEvent(event3);
        assertEquals(0, entry.getStartTime());
        assertEquals(30, entry.getEndTime());
        assertIteratorsEqual(Iterators.forArray(event1, event2, event3), entry.getTimeEventsIterator());
    }

    /**
     * Test method addZoomedEvent with null events.
     */
    @Test
    public void testAddZoomedEventNulls() {
        TimeGraphEntry entry = new TimeGraphEntry(NAME, SWT.DEFAULT, SWT.DEFAULT);
        ITimeEvent event1 = new NullTimeEvent(entry, 0, 10);
        ITimeEvent event2 = new TimeEvent(entry, 10, 10, 2);
        ITimeEvent event3 = new NullTimeEvent(entry, 20, 10);
        // null events do not affect start and end time
        entry.addZoomedEvent(event1);
        entry.addZoomedEvent(event2);
        entry.addZoomedEvent(event3);
        assertEquals(10, entry.getStartTime());
        assertEquals(20, entry.getEndTime());
        assertIteratorsEqual(Iterators.forArray(event1, event2, event3), entry.getTimeEventsIterator());
    }

    /**
     * Test method addZoomedEvent with partial list restarted.
     */
    @Test
    public void testaddZoomedEventPartialRestart() {
        TimeGraphEntry entry = new TimeGraphEntry(NAME, SWT.DEFAULT, SWT.DEFAULT);
        ITimeEvent event1 = new TimeEvent(entry, 0, 10, 1);
        ITimeEvent event2 = new TimeEvent(entry, 10, 10, 2);
        ITimeEvent event3 = new TimeEvent(entry, 20, 10, 3);
        entry.addZoomedEvent(event2);
        entry.addZoomedEvent(event3);
        // zoomed list is cleared and restarted
        entry.addZoomedEvent(event1);
        entry.addZoomedEvent(event2);
        entry.addZoomedEvent(event3);
        assertEquals(0, entry.getStartTime());
        assertEquals(30, entry.getEndTime());
        assertIteratorsEqual(Iterators.forArray(event1, event2, event3), entry.getTimeEventsIterator());
    }

    /**
     * Test method getTimeEventsIterator with event list and zoomed list.
     */
    @Test
    public void testGetTimeEventsIteratorMixed() {
        TimeGraphEntry entry = new TimeGraphEntry(NAME, SWT.DEFAULT, SWT.DEFAULT);
        ITimeEvent event1 = new TimeEvent(entry, 0, 10, 1);
        ITimeEvent event2 = new TimeEvent(entry, 10, 10, 2);
        ITimeEvent event3 = new TimeEvent(entry, 20, 10, 3);
        ITimeEvent event2a = new TimeEvent(entry, 10, 5, 4);
        ITimeEvent event2b = new TimeEvent(entry, 15, 5, 5);
        entry.addEvent(event1);
        entry.addEvent(event2);
        entry.addEvent(event3);
        // zoomed events override normal events they overlap completely
        entry.addZoomedEvent(event2a);
        entry.addZoomedEvent(event2b);
        assertEquals(0, entry.getStartTime());
        assertEquals(30, entry.getEndTime());
        assertIteratorsEqual(Iterators.forArray(event1, event2a, event2b, event3), entry.getTimeEventsIterator());
    }

    /**
     * Test method getTimeEventsIterator with event list and zoomed list partially overlapping.
     */
    @Test
    public void testGetTimeEventsIteratorMixedSplit() {
        TimeGraphEntry entry = new TimeGraphEntry(NAME, SWT.DEFAULT, SWT.DEFAULT);
        ITimeEvent event1 = new TimeEvent(entry, 0, 10, 1);
        ITimeEvent event2 = new TimeEvent(entry, 10, 10, 2);
        ITimeEvent event3 = new TimeEvent(entry, 20, 10, 3);
        ITimeEvent event2a = new TimeEvent(entry, 5, 10, 4);
        ITimeEvent event2b = new TimeEvent(entry, 15, 10, 5);
        ITimeEvent event1s = new TimeEvent(entry, 0, 5, 1);
        ITimeEvent event3s = new TimeEvent(entry, 25, 5, 3);
        entry.addEvent(event1);
        entry.addEvent(event2);
        entry.addEvent(event3);
        // zoomed events split and override normal events they overlap partially
        entry.addZoomedEvent(event2a);
        entry.addZoomedEvent(event2b);
        assertEquals(0, entry.getStartTime());
        assertEquals(30, entry.getEndTime());
        assertIteratorsEqual(Iterators.forArray(event1s, event2a, event2b, event3s), entry.getTimeEventsIterator());
    }

    /**
     * Test method addChild.
     */
    @Test
    public void testAddChild() {
        TimeGraphEntry parent = new TimeGraphEntry("parent", SWT.DEFAULT, SWT.DEFAULT);
        TimeGraphEntry child = new TimeGraphEntry("child", SWT.DEFAULT, SWT.DEFAULT);
        parent.addChild(child);
        assertEquals(null, parent.getParent());
        assertEquals(Arrays.asList(child), parent.getChildren());
        assertEquals(parent, child.getParent());
        assertEquals(Arrays.asList(), child.getChildren());
    }

    /**
     * Test method addChild at specific position.
     */
    @Test
    public void testAddChildAtPosition() {
        TimeGraphEntry parent = new TimeGraphEntry("parent", SWT.DEFAULT, SWT.DEFAULT);
        TimeGraphEntry child1 = new TimeGraphEntry("child1", SWT.DEFAULT, SWT.DEFAULT);
        TimeGraphEntry child2 = new TimeGraphEntry("child2", SWT.DEFAULT, SWT.DEFAULT);
        parent.addChild(0, child1);
        parent.addChild(0, child2);
        assertEquals(null, parent.getParent());
        assertEquals(Arrays.asList(child2, child1), parent.getChildren());
        assertEquals(parent, child1.getParent());
        assertEquals(Arrays.asList(), child1.getChildren());
        assertEquals(parent, child2.getParent());
        assertEquals(Arrays.asList(), child2.getChildren());
    }

    /**
     * Test method addChild to same parent.
     */
    @Test
    public void testAddChildSameParent() {
        TimeGraphEntry parent = new TimeGraphEntry("parent", SWT.DEFAULT, SWT.DEFAULT);
        TimeGraphEntry child = new TimeGraphEntry("child", SWT.DEFAULT, SWT.DEFAULT);
        parent.addChild(child);
        parent.addChild(child);
        assertEquals(null, parent.getParent());
        assertEquals(Arrays.asList(child), parent.getChildren());
        assertEquals(parent, child.getParent());
        assertEquals(Arrays.asList(), child.getChildren());
    }

    /**
     * Test method addChild to an other parent.
     */
    @Test
    public void testAddChildOtherParent() {
        TimeGraphEntry parent1 = new TimeGraphEntry("parent1", SWT.DEFAULT, SWT.DEFAULT);
        TimeGraphEntry parent2 = new TimeGraphEntry("parent2", SWT.DEFAULT, SWT.DEFAULT);
        TimeGraphEntry child = new TimeGraphEntry("child", SWT.DEFAULT, SWT.DEFAULT);
        parent1.addChild(child);
        parent2.addChild(child);
        assertEquals(null, parent1.getParent());
        assertEquals(Arrays.asList(), parent1.getChildren());
        assertEquals(null, parent2.getParent());
        assertEquals(Arrays.asList(child), parent2.getChildren());
        assertEquals(parent2, child.getParent());
        assertEquals(Arrays.asList(), child.getChildren());
    }

    /**
     * Test method removeChild.
     */
    @Test
    public void testRemoveChild() {
        TimeGraphEntry parent = new TimeGraphEntry("parent", SWT.DEFAULT, SWT.DEFAULT);
        TimeGraphEntry child = new TimeGraphEntry("child", SWT.DEFAULT, SWT.DEFAULT);
        parent.addChild(child);
        parent.removeChild(child);
        assertEquals(null, parent.getParent());
        assertEquals(Arrays.asList(), parent.getChildren());
        assertEquals(null, child.getParent());
        assertEquals(Arrays.asList(), child.getChildren());
    }

    /**
     * Test method removeChild with child that has no parent.
     */
    @Test
    public void testRemoveChildNoParent() {
        TimeGraphEntry parent = new TimeGraphEntry("parent", SWT.DEFAULT, SWT.DEFAULT);
        TimeGraphEntry child = new TimeGraphEntry("child", SWT.DEFAULT, SWT.DEFAULT);
        parent.removeChild(child);
        assertEquals(null, parent.getParent());
        assertEquals(Arrays.asList(), parent.getChildren());
        assertEquals(null, child.getParent());
        assertEquals(Arrays.asList(), child.getChildren());
    }

    /**
     * Test method removeChild with child that has an other parent.
     */
    @Test
    public void testRemoveChildOtherParent() {
        TimeGraphEntry parent1 = new TimeGraphEntry("parent1", SWT.DEFAULT, SWT.DEFAULT);
        TimeGraphEntry parent2 = new TimeGraphEntry("parent2", SWT.DEFAULT, SWT.DEFAULT);
        TimeGraphEntry child = new TimeGraphEntry("child", SWT.DEFAULT, SWT.DEFAULT);
        parent1.addChild(child);
        parent2.removeChild(child);
        assertEquals(null, parent1.getParent());
        assertEquals(Arrays.asList(child), parent1.getChildren());
        assertEquals(null, parent2.getParent());
        assertEquals(Arrays.asList(), parent2.getChildren());
        assertEquals(parent1, child.getParent());
        assertEquals(Arrays.asList(), child.getChildren());
    }

    /**
     * Test method sortChildren.
     */
    @Test
    public void testSortChildren() {
        TimeGraphEntry parent = new TimeGraphEntry("parent", SWT.DEFAULT, SWT.DEFAULT);
        TimeGraphEntry child1 = new TimeGraphEntry("child1", 2, 2);
        TimeGraphEntry child2 = new TimeGraphEntry("child2", 1, 1);
        parent.addChild(child1);
        parent.addChild(child2);
        parent.sortChildren(Comparator.comparingLong(entry -> entry.getStartTime()));
        assertEquals(null, parent.getParent());
        assertEquals(Arrays.asList(child2, child1), parent.getChildren());
        assertEquals(parent, child1.getParent());
        assertEquals(Arrays.asList(), child1.getChildren());
        assertEquals(parent, child2.getParent());
        assertEquals(Arrays.asList(), child2.getChildren());
    }

    /**
     * Test method sortChildren followed by addChild.
     */
    @Test
    public void testSortChildrenThenAdd() {
        TimeGraphEntry parent = new TimeGraphEntry("parent", SWT.DEFAULT, SWT.DEFAULT);
        TimeGraphEntry child1 = new TimeGraphEntry("child1", 2, 2);
        TimeGraphEntry child2 = new TimeGraphEntry("child2", 1, 1);
        parent.sortChildren(Comparator.comparingLong(entry -> entry.getStartTime()));
        parent.addChild(child1);
        parent.addChild(child2);
        assertEquals(null, parent.getParent());
        assertEquals(Arrays.asList(child2, child1), parent.getChildren());
        assertEquals(parent, child1.getParent());
        assertEquals(Arrays.asList(), child1.getChildren());
        assertEquals(parent, child2.getParent());
        assertEquals(Arrays.asList(), child2.getChildren());
    }

    private static void assertIteratorsEqual(Iterator<ITimeEvent> expected, Iterator<ITimeEvent> actual) {
        int i = 0;
        while (expected.hasNext()) {
            assertTrue("missing event at position " + i, actual.hasNext());
            ITimeEvent e1 = expected.next();
            ITimeEvent e2 = actual.next();
            assertEquals("not equal events at position " + i, e1, e2);
            i++;
        }
        assertFalse("extra event at position " + i, actual.hasNext());
    }
}
