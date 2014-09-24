/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
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

package org.eclipse.linuxtools.tmf.ctf.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.tmf.ctf.core.CtfIterator;
import org.eclipse.linuxtools.tmf.ctf.core.CtfLocation;
import org.eclipse.linuxtools.tmf.ctf.core.CtfLocationInfo;
import org.eclipse.linuxtools.tmf.ctf.core.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.ctf.core.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.ctf.core.tests.shared.CtfTmfTestTrace;
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

    private static final CtfTmfTestTrace testTrace = CtfTmfTestTrace.KERNEL;

    private CtfTmfTrace trace;
    private CtfIterator iterator;

    /**
     * Perform pre-test initialization.
     * @throws CTFReaderException error
     */
    @Before
    public void setUp() throws CTFReaderException {
        assumeTrue(testTrace.exists());
        trace = testTrace.getTrace();
        iterator = new CtfIterator(trace);
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
     * @throws CTFReaderException error
     */
    @Test
    public void testCtfIterator_noinit() throws CTFReaderException {
        try (CtfIterator result = new CtfIterator(trace);) {
            assertNotNull(result);
        }
    }

    /**
     * Run the CtfIterator(CtfTmfTrace) constructor on an init'ed trace.
     * @throws CTFReaderException error
     */
    @Test
    public void testCtfIterator_init() throws CTFReaderException {
        trace.init("test");
        try (CtfIterator result = new CtfIterator(trace);) {
            assertNotNull(result);
        }
    }

    /**
     * Run the CtfIterator(CtfTmfTrace,long,long) constructor test, which
     * specifies an initial position for the iterator.
     * @throws CTFReaderException error
     */
    @Test
    public void testCtfIterator_position() throws CTFReaderException {
        long timestampValue = 1L;
        long rank = 1L;
        try (CtfIterator result = new CtfIterator(trace, new CtfLocationInfo(timestampValue, 0), rank);) {
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
     * @throws CTFReaderException error
     */
    @Test
    public void testCompareTo() throws CTFReaderException {
        try (CtfIterator o = new CtfIterator(trace);) {
            int result = iterator.compareTo(o);
            assertEquals(1L, result);
        }
    }

    /**
     * Run the boolean equals(Object) method test. Compare with another iterator
     * on the same trace.
     * @throws CTFReaderException error
     */
    @Test
    public void testEquals_other() throws CTFReaderException {
        try (CtfIterator obj = new CtfIterator(trace);) {
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
        try (CtfTmfTrace result = iterator.getCtfTmfTrace();) {
            assertNotNull(result);
        }
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
