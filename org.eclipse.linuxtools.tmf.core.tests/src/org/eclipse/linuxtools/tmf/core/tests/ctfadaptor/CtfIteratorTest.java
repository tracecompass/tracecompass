/*******************************************************************************
 * Copyright (c) 2012 Ericsson
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

package org.eclipse.linuxtools.tmf.core.tests.ctfadaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfIterator;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfLocation;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfLocationInfo;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
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

    private CtfIterator fixture;

    /**
     * Perform pre-test initialization.
     *
     * @throws TmfTraceException
     *             If the test trace is not found
     */
    @Before
    public void setUp() throws TmfTraceException {
        fixture = new CtfIterator(createTrace());
        CtfLocation ctfLocation = new CtfLocation(new CtfLocationInfo(1, 0));
        fixture.setLocation(ctfLocation);
        fixture.increaseRank();
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        fixture.dispose();
    }


    private static CtfTmfTrace createTrace() throws TmfTraceException {
        return TestParams.createTrace();
    }

    /**
     * Run the CtfIterator(CtfTmfTrace) constructor on a non init'ed trace.
     *
     * @throws TmfTraceException
     *             If the test trace is not found
     */
    @Test
    public void testCtfIterator_noinit() throws TmfTraceException {
        CtfTmfTrace trace = createTrace();
        CtfIterator result = new CtfIterator(trace);
        assertNotNull(result);
    }

    /**
     * Run the CtfIterator(CtfTmfTrace) constructor on an init'ed trace.
     *
     * @throws TmfTraceException
     *             If the test trace is not found
     */
    @Test
    public void testCtfIterator_init() throws TmfTraceException {
        CtfTmfTrace trace = createTrace();
        trace.init("test"); //$NON-NLS-1$
        CtfIterator result = new CtfIterator(trace);

        assertNotNull(result);
    }

    /**
     * Run the CtfIterator(CtfTmfTrace,long,long) constructor test, which
     * specifies an initial position for the iterator.
     *
     * @throws TmfTraceException
     *             If the test trace is not found
     */
    @Test
    public void testCtfIterator_position() throws TmfTraceException {
        CtfTmfTrace trace = createTrace();
        long timestampValue = 1L;
        long rank = 1L;
        CtfIterator result = new CtfIterator(trace, new CtfLocationInfo(timestampValue, 0), rank);

        assertNotNull(result);
    }


    /**
     * Run the boolean advance() method test.
     */
    @Test
    public void testAdvance() {
        boolean result = fixture.advance();
        assertTrue(result);
    }

    /**
     * Run the CtfIterator clone() method test.
     */
    @Test
    public void testClone() {
        CtfIterator result = fixture.clone();
        assertNotNull(result);
    }

    /**
     * Run the int compareTo(CtfIterator) method test.
     *
     * @throws TmfTraceException
     *             If the test trace is not found
     */
    @Test
    public void testCompareTo() throws TmfTraceException {
        CtfIterator o = new CtfIterator(createTrace());
        int result = fixture.compareTo(o);

        assertEquals(1L, result);
    }

    /**
     * Run the boolean equals(Object) method test. Compare with another iterator
     * on the same trace.
     *
     * @throws TmfTraceException
     *             If the test trace is not found
     */
    @Test
    public void testEquals_other() throws TmfTraceException {
        CtfIterator obj = new CtfIterator(createTrace());
        CtfLocation ctfLocation1 = new CtfLocation(new CtfLocationInfo(1, 0));
        obj.setLocation(ctfLocation1);
        obj.increaseRank();

        boolean result = fixture.equals(obj);
        assertTrue(result);
    }

    /**
     * Run the boolean equals(Object) method test. Compare with an empty object.
     */
    @Test
    public void testEquals_empty() {
        Object obj = new Object();
        boolean result = fixture.equals(obj);

        assertFalse(result);
    }

    /**
     * Run the CtfTmfTrace getCtfTmfTrace() method test.
     */
    @Test
    public void testGetCtfTmfTrace() {
        CtfTmfTrace result = fixture.getCtfTmfTrace();
        assertNotNull(result);
    }

    /**
     * Run the CtfTmfEvent getCurrentEvent() method test.
     */
    @Test
    public void testGetCurrentEvent() {
        CtfTmfEvent result = fixture.getCurrentEvent();
        assertNotNull(result);
    }

    /**
     * Run the CtfLocation getLocation() method test.
     */
    @Test
    public void testGetLocation() {
        CtfLocation result = fixture.getLocation();
        assertNotNull(result);
    }

    /**
     * Run the long getRank() method test.
     */
    @Test
    public void testGetRank() {
        long result = fixture.getRank();
        assertEquals(1L, result);
    }

    /**
     * Run the boolean hasValidRank() method test.
     */
    @Test
    public void testHasValidRank() {
        boolean result = fixture.hasValidRank();
        assertTrue(result);
    }

    /**
     * Run the int hashCode() method test.
     */
    @Test
    public void testHashCode() {
        int result = fixture.hashCode();
        int result2 = fixture.hashCode();
        assertEquals(result, result2);
    }

    /**
     * Run the void increaseRank() method test.
     */
    @Test
    public void testIncreaseRank() {
        fixture.increaseRank();
    }

    /**
     * Run the boolean seek(long) method test.
     */
    @Test
    public void testSeek() {
        long timestamp = 1L;
        boolean result = fixture.seek(timestamp);
        assertTrue(result);
    }

    /**
     * Run the void setLocation(ITmfLocation<?>) method test.
     */
    @Test
    public void testSetLocation() {
        CtfLocation location = new CtfLocation(new CtfLocationInfo(1, 0));
        fixture.setLocation(location);
    }
}
