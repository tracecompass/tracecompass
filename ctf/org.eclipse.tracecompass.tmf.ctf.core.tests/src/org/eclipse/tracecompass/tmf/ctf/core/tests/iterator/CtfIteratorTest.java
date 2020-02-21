/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Matthew Khouzam - Initial generation with CodePro tools
 *   Alexandre Montplaisir - Clean up, consolidate redundant tests
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.tests.iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.core.trace.CTFTraceReader;
import org.eclipse.tracecompass.internal.tmf.ctf.core.trace.iterator.CtfIterator;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.ctf.core.context.CtfLocation;
import org.eclipse.tracecompass.tmf.ctf.core.context.CtfLocationInfo;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>CtfIteratorTest</code> contains tests for the class
 * <code>{@link CtfIterator}</code>.
 *
 * @author ematkho
 * @version 1.0
 */
public class CtfIteratorTest {

    private static final @NonNull CtfTestTrace testTrace = CtfTestTrace.KERNEL;

    private CtfTmfTrace fTrace;
    private CtfIterator fIterator;

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        fTrace = CtfTmfTestTraceUtils.getTrace(testTrace);
        fIterator = (CtfIterator) fTrace.createIterator();
        CtfLocation ctfLocation = new CtfLocation(new CtfLocationInfo(1, 0));
        fIterator.setLocation(ctfLocation);
        fIterator.increaseRank();
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        if (fTrace != null) {
            fTrace.dispose();
        }
        if (fIterator != null) {
            fIterator.dispose();
        }
    }

    /**
     * Run the CtfIterator(CtfTmfTrace) constructor on a non init'ed trace.
     */
    @Test
    public void testCtfIteratorNoinit() {
        try (CtfIterator result = (CtfIterator) fTrace.createIterator();) {
            assertNotNull(result);
        }
    }

    /**
     * Run the CtfIterator(CtfTmfTrace) constructor on an init'ed trace.
     */
    @Test
    public void testCtfIteratorInit() {
        fTrace.init("test");
        try (CtfIterator result = (CtfIterator) fTrace.createIterator();) {
            assertNotNull(result);
        }
    }

    /**
     * Run the CtfIterator(CtfTmfTrace,long,long) constructor test, which
     * specifies an initial position for the iterator.
     */
    @Test
    public void testCtfIteratorPosition() {
        long timestampValue = 1L;
        long rank = 1L;
        try (CtfIterator result = (CtfIterator) fTrace.createIterator(new CtfLocationInfo(timestampValue, 0), rank);) {
            assertNotNull(result);
            assertEquals("sys_socketcall", result.getCurrentEvent().getName());
        }
        timestampValue = 1332170682440133097L;
        try (CtfIterator result = (CtfIterator) fTrace.createIterator(new CtfLocationInfo(timestampValue, 0), rank);) {
            assertNotNull(result);
            assertEquals("sys_socketcall", result.getCurrentEvent().getName());
        }
    }

    /**
     * Run the boolean advance() method test.
     */
    @Test
    public void testAdvance() {
        boolean result = fIterator.advance();
        assertTrue(result);
        CtfTmfEvent currentEvent = fIterator.getCurrentEvent();
        assertEquals("exit_syscall", currentEvent.getName());
        assertEquals(Long.valueOf(4132), currentEvent.getContent().getFieldValue(Long.class, "ret"));
    }

    /**
     * Run the int compareTo(CtfIterator) method test.
     */
    @Test
    public void testCompareTo() {
        try (CtfIterator o = (CtfIterator) fTrace.createIterator();) {
            int result = fIterator.compareTo(o);
            assertEquals(1L, result);
            assertEquals(-1L, o.compareTo(fIterator));
            assertEquals(0, o.compareTo(o));
            assertEquals(0, fIterator.compareTo(fIterator));
        }
    }

    /**
     * Run the boolean equals(Object) method test. Compare with another iterator
     * on the same trace.
     *
     * @throws CTFException if the trace is corrupt
     */
    @Test
    public void testEqualsOther() throws CTFException {
        assertNotNull(fIterator);
        assertEquals(fIterator, fIterator);
        try (CtfIterator obj = (CtfIterator) fTrace.createIterator();) {
            assertNotNull(obj);
            assertNotEquals(fIterator, obj);
            CtfLocation ctfLocation1 = new CtfLocation(new CtfLocationInfo(1, 0));
            obj.setLocation(ctfLocation1);
            obj.increaseRank();

            assertEquals(fIterator, obj);
        }
        CtfTmfTrace trace = CtfTmfTestTraceUtils.getTrace(CtfTestTrace.FUNKY_TRACE);
        assertNotNull(trace);
        try (CtfIterator funky = (CtfIterator) trace.createIterator()) {
            assertNotEquals(fIterator, funky);
        }
        try (CtfIterator iter = (CtfIterator) fTrace.createIterator();) {
            CTFTrace otherTrace = new CTFTrace(fTrace.getPath());
            try (CTFTraceReader tr = new CTFTraceReader(otherTrace)) {
                assertNotEquals(iter, tr);
            }
        }
        trace.dispose();
        try (CtfIterator iter1 = (CtfIterator) fTrace.createIterator(); CtfIterator iter2 = (CtfIterator) fTrace.createIterator()) {
            assertEquals(iter1, iter2);
            iter2.setRank(2);
            assertNotEquals(iter1, iter2);
        }
    }

    /**
     * Run the boolean equals(Object) method test. Compare with an empty object.
     */
    @Test
    public void testEqualsEmpty() {
        assertNotEquals(new Object(), fIterator);
    }

    /**
     * Run the CtfTmfTrace getCtfTmfTrace() method test.
     */
    @Test
    public void testGetCtfTmfTrace() {
        CtfTmfTrace result = fIterator.getCtfTmfTrace();
        assertNotNull(result);
    }

    /**
     * Run the CtfTmfEvent getCurrentEvent() method test.
     */
    @Test
    public void testGetCurrentEvent() {
        CtfTmfEvent result = fIterator.getCurrentEvent();
        assertNotNull(result);
    }

    /**
     * Run the CtfLocation getLocation() method test.
     */
    @Test
    public void testGetLocation() {
        CtfLocation result = fIterator.getLocation();
        assertNotNull(result);
    }

    /**
     * Run the long getRank() method test.
     */
    @Test
    public void testGetRank() {
        long result = fIterator.getRank();
        assertEquals(1L, result);
    }

    /**
     * Run the boolean hasValidRank() method test.
     */
    @Test
    public void testHasValidRank() {
        boolean result = fIterator.hasValidRank();
        assertTrue(result);
    }

    /**
     * Run the int hashCode() method test.
     */
    @Test
    public void testHashCode() {
        int result = fIterator.hashCode();
        int result2 = fIterator.hashCode();
        assertEquals(result, result2);
    }

    /**
     * Run the void increaseRank() method test.
     */
    @Test
    public void testIncreaseRank() {
        long rank = fIterator.getRank();
        fIterator.increaseRank();
        assertEquals(rank + 1, fIterator.getRank());
        fIterator.setRank(ITmfContext.UNKNOWN_RANK);
        rank = fIterator.getRank();
        fIterator.advance();
        assertEquals(rank, fIterator.getRank());
    }

    /**
     * Run the void setRank() method test.
     */
    @Test
    public void testSetRank() {
        long rank = fIterator.getRank();
        fIterator.increaseRank();
        assertEquals(rank + 1, fIterator.getRank());
        fIterator.setRank(rank);
        assertEquals(rank, fIterator.getRank());
    }

    /**
     * Run the boolean seek(long) method test.
     */
    @Test
    public void testSeek() {
        // Trace 2 has duplicate time stamps
        CtfTmfTrace trace = CtfTmfTestTraceUtils.getTrace(CtfTestTrace.TRACE2);

        try (CtfIterator iterator = (CtfIterator) trace.createIterator()) {
            assertTrue(iterator.seek(1L));
            CtfTmfEvent event = iterator.getCurrentEvent();
            assertNotNull(event);
            assertEquals(1331668247314038062L, event.getTimestamp().toNanos());
            assertEquals(1331668247314038062L, iterator.getCurrentTimestamp());

            assertFalse(iterator.seek(Long.MAX_VALUE));
            assertNull(iterator.getCurrentEvent());
            assertEquals(0L, iterator.getCurrentTimestamp());
            assertFalse(iterator.advance());

            // seek to a time after trace start.
            CtfLocationInfo middleLocation = new CtfLocationInfo(1331668250328561095L, 0L);
            assertTrue(iterator.seek(middleLocation));
            event = iterator.getCurrentEvent();
            assertNotNull(event);
            assertEquals(1331668250328561095L, event.getTimestamp().toNanos());
            assertEquals(1331668250328561095L, iterator.getCurrentTimestamp());

            CtfLocationInfo middleLocationIndexOne = new CtfLocationInfo(1331668250328561095L, 1L);
            assertTrue(iterator.seek(middleLocationIndexOne));
            event = iterator.getCurrentEvent();
            assertNotNull(event);
            assertEquals(1331668250328561761L, event.getTimestamp().toNanos());
            assertEquals(1331668250328561761L, iterator.getCurrentTimestamp());
            // next event location
            assertEquals(new CtfLocationInfo(1331668250328561761L, 0L), iterator.getLocation().getLocationInfo());

            // double timestamp at 15:50:47.328921944
            CtfLocationInfo duplicateLocationIndexOne = new CtfLocationInfo(1331668247328921944L, 1L);
            assertTrue(iterator.seek(duplicateLocationIndexOne));
            event = iterator.getCurrentEvent();
            assertNotNull(event);
            assertEquals(1331668247328921944L, event.getTimestamp().toNanos());
            assertEquals(1331668247328921944L, iterator.getCurrentTimestamp());
            // test that events will be in cpu order
            assertEquals("sched_switch", event.getName());
            // next event location
            assertEquals(duplicateLocationIndexOne, iterator.getLocation().getLocationInfo());

            CtfLocationInfo duplicateLocationOutOfBounds = new CtfLocationInfo(1331668247328921944L, 4L);
            assertTrue(iterator.seek(duplicateLocationOutOfBounds));
            event = iterator.getCurrentEvent();
            assertNotNull(event);
            assertEquals(1331668247328925363L, event.getTimestamp().toNanos());
            assertEquals(1331668247328925363L, iterator.getCurrentTimestamp());
            assertEquals("sys_poll", event.getName());
            // next event location
            assertEquals(new CtfLocationInfo(1331668247328925363L, 0L), iterator.getLocation().getLocationInfo());

            CtfLocationInfo duplicateLocationIndexHuge = new CtfLocationInfo(1331668247328921944L, 9001000000L);
            assertTrue(iterator.seek(duplicateLocationIndexHuge));
            event = iterator.getCurrentEvent();
            assertNotNull(event);
            assertEquals(1331668247328925363L, event.getTimestamp().toNanos());
            assertEquals(1331668247328925363L, iterator.getCurrentTimestamp());
            assertEquals("sys_poll", event.getName());
            // next event location
            assertEquals(new CtfLocationInfo(1331668247328925363L, 0L), iterator.getLocation().getLocationInfo());

            assertFalse(iterator.seek(CtfLocation.INVALID_LOCATION));
            // last valid seek location
            assertEquals(event, iterator.getCurrentEvent());
        }
        trace.dispose();
    }

    /**
     * Run the void setLocation(ITmfLocation<?>) method test.
     */
    @Test
    public void testSetLocation() {
        CtfLocation location = new CtfLocation(new CtfLocationInfo(1, 0));
        fIterator.setLocation(location);
    }
}
