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

package org.eclipse.linuxtools.tmf.ctf.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventType;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.signal.TmfEndSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignal;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfEventTypeCollectionHelper;
import org.eclipse.linuxtools.tmf.ctf.core.CtfLocation;
import org.eclipse.linuxtools.tmf.ctf.core.CtfLocationInfo;
import org.eclipse.linuxtools.tmf.ctf.core.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.ctf.core.CtfTmfTimestamp;
import org.eclipse.linuxtools.tmf.ctf.core.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.ctf.core.tests.shared.CtfTmfTestTrace;
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
        try (CtfTmfTrace result = new CtfTmfTrace();) {
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
    public void testClose() {
        try (CtfTmfTrace emptyFixture = new CtfTmfTrace();) {
        }
    }

    /**
     * Run the int getCacheSize() method test.
     */
    @Test
    public void testGetCacheSize() {
        try (CtfTmfTrace emptyFixture = new CtfTmfTrace();) {
            int result = emptyFixture.getCacheSize();
            assertEquals(1000, result);
        }
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
    public void testSeekEventLoc_timetamp() {
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
        assertEquals("\"lttng-modules\"", result);
    }

    /**
     * Test the {@link CtfTmfTrace#getEventType()} method.
     */
    @Test
    public void testGetEventType() {
        Class<?> result = fixture.getEventType();
        assertNotNull(result);
        assertEquals(CtfTmfEvent.class, result);
    }

    /**
     * Run the Class<CtfTmfEvent> getContainedEventTypes() method test.
     */
    @Test
    public void testGetContainedEventTypes() {
        Set<ITmfEventType> result = fixture.getContainedEventTypes();
        assertNotNull(result);
        assertFalse(result.isEmpty());
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
     * Run the getTraceProperties() method test.
     */
    @Test
    public void testGetTraceProperties() {
        int result = fixture.getTraceProperties().size();
        assertEquals(9, result);
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
        Set<ITmfEventType> eventTypes = fixture.getContainedEventTypes();
        Set<String> eventNames = TmfEventTypeCollectionHelper.getEventNames(eventTypes);
        assertTrue(eventNames.contains("sched_switch"));
        assertFalse(eventNames.contains("Sched_switch"));
        String[] events = { "sched_switch", "sched_wakeup", "timer_init" };
        assertTrue(eventNames.containsAll(Arrays.asList(events)));
        Set<String> copy = new HashSet<>(eventNames);
        copy.retainAll(Arrays.asList(events));
        assertFalse(copy.isEmpty());
        String[] names = { "inexistent", "sched_switch", "SomeThing" };
        copy = new HashSet<>(eventNames);
        copy.retainAll(Arrays.asList(names));
        assertTrue(!copy.isEmpty());
        assertFalse(eventNames.containsAll(Arrays.asList(names)));
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
