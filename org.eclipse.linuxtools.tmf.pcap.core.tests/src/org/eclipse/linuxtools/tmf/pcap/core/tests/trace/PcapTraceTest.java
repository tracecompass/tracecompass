/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Vincent Perot - Initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.pcap.core.tests.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.signal.TmfEndSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignal;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.location.TmfLongLocation;
import org.eclipse.linuxtools.tmf.pcap.core.event.PcapEvent;
import org.eclipse.linuxtools.tmf.pcap.core.tests.shared.PcapTmfTestTrace;
import org.eclipse.linuxtools.tmf.pcap.core.trace.PcapTrace;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit that test the PcapTrace class.
 *
 * @author Vincent Perot
 */
public class PcapTraceTest {

    private static final PcapTmfTestTrace TEST_TRACE = PcapTmfTestTrace.MOSTLY_TCP;

    private PcapTrace fFixture;

    /**
     * Perform pre-test initialization.
     *
     * @throws TmfTraceException
     *             If the test trace is not found
     */
    @Before
    public void setUp() throws TmfTraceException {
        assumeTrue(TEST_TRACE.exists());
        fFixture = new PcapTrace();
        fFixture.initTrace((IResource) null, TEST_TRACE.getPath(), PcapEvent.class);
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        if (fFixture != null) {
            fFixture.dispose();
        }
    }

    /**
     * Run the PcapTrace() constructor test.
     */
    @Test
    public void testPcapTrace() {
        try (PcapTrace result = new PcapTrace();) {
            assertNotNull(result);
            assertEquals(1000, result.getCacheSize());
            assertEquals(0L, result.getNbEvents());
            assertEquals(0L, result.getStreamingInterval());
            assertNull(result.getResource());
            assertNull(result.getType());
        }
    }

    /**
     * Test the parseEvent() method
     */
    @Test
    public void testParseEvent() {
        ITmfContext ctx = fFixture.seekEvent(0);
        fFixture.getNext(ctx);
        PcapEvent event = fFixture.parseEvent(ctx);
        assertNotNull(event);
    }

    /**
     * Run the void broadcast(TmfSignal) method test.
     */
    @Test
    public void testBroadcast() {
        TmfSignal signal = new TmfEndSynchSignal(1);
        fFixture.broadcast(signal);
    }

    /**
     * Run the void dispose() method test.
     */
    @Test
    public void testClose() {
        try (PcapTrace emptyFixture = new PcapTrace();) {
        }
    }

    /**
     * Run the int getCacheSize() method test.
     */
    @Test
    public void testGetCacheSize() {
        try (PcapTrace emptyFixture = new PcapTrace();) {
            int result = emptyFixture.getCacheSize();
            assertEquals(1000, result);
        }
    }

    /**
     * Run the ITmfLocation<Comparable> getCurrentLocation() method test.
     */
    @Test
    public void testGetCurrentLocation() {
        TmfLongLocation result = (TmfLongLocation) fFixture.getCurrentLocation();
        assertEquals(new TmfLongLocation(0), result);
    }

    /**
     * Test the seekEvent() method with a null location.
     */
    @Test
    public void testSeekEventLoc_null() {
        TmfLongLocation loc = null;
        fFixture.seekEvent(loc);
        assertNotNull(fFixture);
    }

    /**
     * Test the seekEvent() method with a normal location.
     */
    @Test
    public void testSeekEventLoc_normal() {
        TmfLongLocation loc = new TmfLongLocation(3L);
        fFixture.seekEvent(loc);
        assertNotNull(fFixture);
    }

    /**
     * Run the ITmfTimestamp getEndTime() method test.
     */
    @Test
    public void testGetEndTime() {
        ITmfTimestamp result = fFixture.getEndTime();
        assertNotNull(result);
    }

    /**
     * Test the {@link PcapTrace#getEventType()} method.
     */
    @Test
    public void testGetEventType() {
        Class<?> result = fFixture.getEventType();
        assertNotNull(result);
        assertEquals(PcapEvent.class, result);
    }

    /**
     * Run the double getLocationRatio(ITmfLocation<?>) method test.
     */
    @Test
    public void testGetLocationRatio() {
        TmfLongLocation location = new TmfLongLocation(20L);
        double result = fFixture.getLocationRatio(location);

        assertEquals(20.0 / 43.0, result, 0.01);
    }

    /**
     * Run the String getName() method test.
     */
    @Test
    public void testGetName() {
        String result = fFixture.getName();
        assertNotNull(result);
    }

    /**
     * Run the getTraceProperties() method test.
     */
    @Test
    public void testGetTraceProperties() {
        int result = fFixture.getTraceProperties().size();
        assertEquals(6, result);
    }

    /**
     * Run the long getNbEvents() method test.
     */
    @Test
    public void testGetNbEvents() {
        long result = fFixture.getNbEvents();
        assertEquals(0, result);
    }

    /**
     * Run the String getPath() method test.
     */
    @Test
    public void testGetPath() {
        String result = fFixture.getPath();
        assertNotNull(result);
    }

    /**
     * Run the IResource getResource() method test.
     */
    @Test
    public void testGetResource() {
        IResource result = fFixture.getResource();
        assertNull(result);
    }

    /**
     * Run the ITmfTimestamp getStartTime() method test.
     */
    @Test
    public void testGetStartTime() {
        ITmfTimestamp result = fFixture.getStartTime();
        assertNotNull(result);
    }

    /**
     * Run the long getStreamingInterval() method test.
     */
    @Test
    public void testGetStreamingInterval() {
        long result = fFixture.getStreamingInterval();
        assertEquals(0L, result);
    }

    /**
     * Run the TmfTimeRange getTimeRange() method test.
     */
    @Test
    public void testGetTimeRange() {
        TmfTimeRange result = fFixture.getTimeRange();
        assertNotNull(result);
    }

    /**
     * Run the ITmfContext seekEvent(double) method test.
     */
    @Test
    public void testSeekEvent_ratio() {
        double ratio = 21.0 / 43.0;
        ITmfContext result = fFixture.seekEvent(ratio);
        assertNotNull(result);
    }

    /**
     * Run the ITmfContext seekEvent(long) method test.
     */
    @Test
    public void testSeekEvent_rank() {
        long rank = 1L;
        ITmfContext result = fFixture.seekEvent(rank);
        assertNotNull(result);
    }

    /**
     * Run the ITmfContext seekEvent(ITmfLocation<?>) method test.
     */
    @Test
    public void testSeekEvent_location() {
        TmfLongLocation pcapLocation = new TmfLongLocation(10L);
        ITmfContext result = fFixture.seekEvent(pcapLocation);
        assertNotNull(result);
    }

    /**
     * Run the boolean validate(IProject,String) method test.
     */
    @Test
    public void testValidate() {
        IProject project = null;
        IStatus result = fFixture.validate(project, TEST_TRACE.getPath());
        assertTrue(result.isOK());
    }

    /**
     * Run the String getHostId() method test
     */
    @Test
    public void getSource() {
        String a = fFixture.getHostId();
        assertEquals("mostlyTCP.pcap", a);
    }

}
