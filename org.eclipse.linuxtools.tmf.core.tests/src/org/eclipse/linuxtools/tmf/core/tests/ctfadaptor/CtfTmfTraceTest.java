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

package org.eclipse.linuxtools.tmf.core.tests.ctfadaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfLocation;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfLocationInfo;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTimestamp;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.signal.TmfEndSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignal;
import org.eclipse.linuxtools.tmf.core.tests.shared.CtfTmfTestTrace;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>CtfTmfTraceTest</code> contains tests for the class
 * <code>{@link CtfTmfTrace}</code>.
 *
 * @author ematkho
 * @version 1.0
 */
public class CtfTmfTraceTest {

    private static final CtfTmfTestTrace testTrace = CtfTmfTestTrace.KERNEL;

    private CtfTmfTrace fixture;

    /**
     * Perform pre-test initialization.
     *
     * @throws TmfTraceException
     *             If the test trace is not found
     */
    @Before
    public void setUp() throws TmfTraceException {
        assumeTrue(testTrace.exists());
        fixture = new CtfTmfTrace();
        fixture.initTrace((IResource) null, testTrace.getPath(), CtfTmfEvent.class);
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        if (fixture != null) {
            fixture.dispose();
        }
    }

    /**
     * Run the CtfTmfTrace() constructor test.
     */
    @Test
    public void testCtfTmfTrace() {
        CtfTmfTrace result = new CtfTmfTrace();

        assertNotNull(result);
        assertNull(result.getEventType());
        assertEquals(1000, result.getCacheSize());
        assertEquals(0L, result.getNbEvents());
        assertEquals(0L, result.getStreamingInterval());
        assertNull(result.getResource());
        assertEquals(1000, result.getQueueSize());
        assertNull(result.getType());
    }

    /**
     * Test the parseEvent() method
     */
    @Test
    public void testParseEvent() {
        ITmfContext ctx = fixture.seekEvent(0);
        fixture.getNext(ctx);
        CtfTmfEvent event = fixture.parseEvent(ctx);
        assertNotNull(event);
    }

    /**
     * Run the void broadcast(TmfSignal) method test.
     */
    @Test
    public void testBroadcast() {
        TmfSignal signal = new TmfEndSynchSignal(1);
        fixture.broadcast(signal);
    }


    /**
     * Run the void dispose() method test.
     */
    @Test
    public void testDispose() {
        CtfTmfTrace emptyFixture = new CtfTmfTrace();
        emptyFixture.dispose();

    }

    /**
     * Run the int getCacheSize() method test.
     */
    @Test
    public void testGetCacheSize() {
        CtfTmfTrace emptyFixture = new CtfTmfTrace();
        int result = emptyFixture.getCacheSize();
        assertEquals(1000, result);
    }

    /**
     * Run the ITmfLocation<Comparable> getCurrentLocation() method test.
     */
    @Test
    public void testGetCurrentLocation() {
        CtfLocation result = (CtfLocation) fixture.getCurrentLocation();
        assertNull(result);
    }

    /**
     * Test the seekEvent() method with a null location.
     */
    @Test
    public void testSeekEventLoc_null() {
        CtfLocation loc = null;
        fixture.seekEvent(loc);
        assertNotNull(fixture);
    }

    /**
     * Test the seekEvent() method with a location from a timestamp.
     */
    @Test
    public void testSeekEventLoc_timetamp(){
        CtfLocation loc = new CtfLocation(new CtfTmfTimestamp(0L));
        fixture.seekEvent(loc);
        assertNotNull(fixture);
    }


    /**
     * Run the ITmfTimestamp getEndTime() method test.
     */
    @Test
    public void testGetEndTime() {
        ITmfTimestamp result = fixture.getEndTime();
        assertNotNull(result);
    }

    /**
     * Run the String getEnvironment method test.
     */
    @Test
    public void testGetEnvValue() {
        String key = "tracer_name";
        String result = fixture.getTraceProperties().get(key);
        assertEquals("\"lttng-modules\"",result);
    }

    /**
     * Run the Class<CtfTmfEvent> getEventType() method test.
     */
    @Test
    public void testGetEventType() {
        Class<ITmfEvent> result = fixture.getEventType();
        assertNotNull(result);
    }

    /**
     * Run the double getLocationRatio(ITmfLocation<?>) method test.
     */
    @Test
    public void testGetLocationRatio() {
        final CtfLocationInfo location2 = new CtfLocationInfo(1, 0);
        CtfLocation location = new CtfLocation(location2);
        double result = fixture.getLocationRatio(location);

        assertEquals(Double.NEGATIVE_INFINITY, result, 0.1);
    }

    /**
     * Run the String getName() method test.
     */
    @Test
    public void testGetName() {
        String result = fixture.getName();
        assertNotNull(result);
    }

    /**
     * Run the int getNbEnvVars() method test.
     */
    @Test
    public void testGetNbEnvVars() {
        int result = fixture.getTraceProperties().size();
        assertEquals(8, result);
    }

    /**
     * Run the long getNbEvents() method test.
     */
    @Test
    public void testGetNbEvents() {
        long result = fixture.getNbEvents();
        assertEquals(1L, result);
    }

    /**
     * Run the CtfTmfEvent getNext(ITmfContext) method test.
     */
    @Test
    public void testGetNext() {
        ITmfContext context = fixture.seekEvent(0);
        CtfTmfEvent result = fixture.getNext(context);
        assertNotNull(result);
    }

    /**
     * Run the String getPath() method test.
     */
    @Test
    public void testGetPath() {
        String result = fixture.getPath();
        assertNotNull(result);
    }

    /**
     * Run the IResource getResource() method test.
     */
    @Test
    public void testGetResource() {
        IResource result = fixture.getResource();
        assertNull(result);
    }

    /**
     * Run the ITmfTimestamp getStartTime() method test.
     */
    @Test
    public void testGetStartTime() {
        ITmfTimestamp result = fixture.getStartTime();
        assertNotNull(result);
    }

    /**
     * Run the long getStreamingInterval() method test.
     */
    @Test
    public void testGetStreamingInterval() {
        long result = fixture.getStreamingInterval();
        assertEquals(0L, result);
    }

    /**
     * Run the TmfTimeRange getTimeRange() method test.
     */
    @Test
    public void testGetTimeRange() {
        TmfTimeRange result = fixture.getTimeRange();
        assertNotNull(result);
    }

    /**
     * Run the CtfTmfEvent readNextEvent(ITmfContext) method test.
     */
    @Test
    public void testReadNextEvent() {
        ITmfContext context = fixture.seekEvent(0);
        CtfTmfEvent result = fixture.getNext(context);
        assertNotNull(result);
    }

    /**
     * Run the ITmfContext seekEvent(double) method test.
     */
    @Test
    public void testSeekEvent_ratio() {
        double ratio = 0.99;
        ITmfContext result = fixture.seekEvent(ratio);
        assertNotNull(result);
    }

    /**
     * Run the ITmfContext seekEvent(long) method test.
     */
    @Test
    public void testSeekEvent_rank() {
        long rank = 1L;
        ITmfContext result = fixture.seekEvent(rank);
        assertNotNull(result);
    }

    /**
     * Run the ITmfContext seekEvent(ITmfTimestamp) method test.
     */
    @Test
    public void testSeekEvent_timestamp() {
        ITmfTimestamp timestamp = new TmfTimestamp();
        ITmfContext result = fixture.seekEvent(timestamp);
        assertNotNull(result);
    }

    /**
     * Run the ITmfContext seekEvent(ITmfLocation<?>) method test.
     */
    @Test
    public void testSeekEvent_location() {
        final CtfLocationInfo location2 = new CtfLocationInfo(1L, 0L);
        CtfLocation ctfLocation = new CtfLocation(location2);
        ITmfContext result = fixture.seekEvent(ctfLocation);
        assertNotNull(result);
    }

    /**
     * Run the boolean validate(IProject,String) method test.
     */
    @Test
    public void testValidate() {
        IProject project = null;
        IStatus result = fixture.validate(project, testTrace.getPath());
        assertTrue(result.isOK());
    }

    /**
     * Run the boolean hasEvent(final String) method test
     */
    @Test
    public void testEventLookup() {
        assertTrue(fixture.hasEvent("sched_switch"));
        assertFalse(fixture.hasEvent("Sched_switch"));
        String[] events = { "sched_switch", "sched_wakeup", "timer_init" };
        assertTrue(fixture.hasAllEvents(events));
        assertTrue(fixture.hasAtLeastOneOfEvents(events));
        String[] names = { "inexistent", "sched_switch", "SomeThing" };
        assertTrue(fixture.hasAtLeastOneOfEvents(names));
        assertFalse(fixture.hasAllEvents(names));
    }

    /**
     * Run the String getHostId() method test
     */
    @Test
    public void testCtfHostId() {
        String a = fixture.getHostId();
        assertEquals("\"84db105b-b3f4-4821-b662-efc51455106a\"", a);
    }

}
