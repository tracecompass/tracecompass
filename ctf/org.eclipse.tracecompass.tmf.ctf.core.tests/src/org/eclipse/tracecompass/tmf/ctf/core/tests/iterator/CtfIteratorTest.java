/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial generation with CodePro tools
 *   Alexandre Montplaisir - Clean up, consolidate redundant tests
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.tests.iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.ctf.core.trace.iterator.CtfIterator;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
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

    private CtfTmfTrace trace;
    private CtfIterator iterator;

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        trace = CtfTmfTestTraceUtils.getTrace(testTrace);
        iterator = (CtfIterator) trace.createIterator();
        CtfLocation ctfLocation = new CtfLocation(new CtfLocationInfo(1, 0));
        iterator.setLocation(ctfLocation);
        iterator.increaseRank();
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        if (trace != null) {
            trace.dispose();
        }
        if (iterator != null) {
            iterator.dispose();
        }
    }

    /**
     * Run the CtfIterator(CtfTmfTrace) constructor on a non init'ed trace.
     */
    @Test
    public void testCtfIterator_noinit() {
        try (CtfIterator result = (CtfIterator) trace.createIterator();) {
            assertNotNull(result);
        }
    }

    /**
     * Run the CtfIterator(CtfTmfTrace) constructor on an init'ed trace.
     */
    @Test
    public void testCtfIterator_init() {
        trace.init("test");
        try (CtfIterator result = (CtfIterator) trace.createIterator();) {
            assertNotNull(result);
        }
    }

    /**
     * Run the CtfIterator(CtfTmfTrace,long,long) constructor test, which
     * specifies an initial position for the iterator.
     */
    @Test
    public void testCtfIterator_position() {
        long timestampValue = 1L;
        long rank = 1L;
        try (CtfIterator result = (CtfIterator) trace.createIterator(new CtfLocationInfo(timestampValue, 0), rank);) {
            assertNotNull(result);
        }
    }


    /**
     * Run the boolean advance() method test.
     */
    @Test
    public void testAdvance() {
        boolean result = iterator.advance();
        assertTrue(result);
    }

    /**
     * Run the int compareTo(CtfIterator) method test.
     */
    @Test
    public void testCompareTo() {
        try (CtfIterator o = (CtfIterator) trace.createIterator();) {
            int result = iterator.compareTo(o);
            assertEquals(1L, result);
        }
    }

    /**
     * Run the boolean equals(Object) method test. Compare with another iterator
     * on the same trace.
     */
    @Test
    public void testEquals_other() {
        try (CtfIterator obj = (CtfIterator) trace.createIterator();) {
            assertNotNull(obj);
            CtfLocation ctfLocation1 = new CtfLocation(new CtfLocationInfo(1, 0));
            obj.setLocation(ctfLocation1);
            obj.increaseRank();

            boolean result = iterator.equals(obj);
            assertTrue(result);
        }
    }

    /**
     * Run the boolean equals(Object) method test. Compare with an empty object.
     */
    @Test
    public void testEquals_empty() {
        Object obj = new Object();
        boolean result = iterator.equals(obj);

        assertFalse(result);
    }

    /**
     * Run the CtfTmfTrace getCtfTmfTrace() method test.
     */
    @Test
    public void testGetCtfTmfTrace() {
        CtfTmfTrace result = iterator.getCtfTmfTrace();
        assertNotNull(result);
    }

    /**
     * Run the CtfTmfEvent getCurrentEvent() method test.
     */
    @Test
    public void testGetCurrentEvent() {
        CtfTmfEvent result = iterator.getCurrentEvent();
        assertNotNull(result);
    }

    /**
     * Run the CtfLocation getLocation() method test.
     */
    @Test
    public void testGetLocation() {
        CtfLocation result = iterator.getLocation();
        assertNotNull(result);
    }

    /**
     * Run the long getRank() method test.
     */
    @Test
    public void testGetRank() {
        long result = iterator.getRank();
        assertEquals(1L, result);
    }

    /**
     * Run the boolean hasValidRank() method test.
     */
    @Test
    public void testHasValidRank() {
        boolean result = iterator.hasValidRank();
        assertTrue(result);
    }

    /**
     * Run the int hashCode() method test.
     */
    @Test
    public void testHashCode() {
        int result = iterator.hashCode();
        int result2 = iterator.hashCode();
        assertEquals(result, result2);
    }

    /**
     * Run the void increaseRank() method test.
     */
    @Test
    public void testIncreaseRank() {
        iterator.increaseRank();
    }

    /**
     * Run the boolean seek(long) method test.
     */
    @Test
    public void testSeek() {
        long timestamp = 1L;
        boolean result = iterator.seek(timestamp);
        assertTrue(result);
    }

    /**
     * Run the void setLocation(ITmfLocation<?>) method test.
     */
    @Test
    public void testSetLocation() {
        CtfLocation location = new CtfLocation(new CtfLocationInfo(1, 0));
        iterator.setLocation(location);
    }
}
