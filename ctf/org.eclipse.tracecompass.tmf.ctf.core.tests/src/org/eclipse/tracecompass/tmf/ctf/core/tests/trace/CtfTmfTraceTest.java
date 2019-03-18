/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial generation with CodePro tools
 *   Alexandre Montplaisir - Clean up, consolidate redundant tests
 *   Patrick Tasse - Fix location ratio
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.tests.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventType;
import org.eclipse.tracecompass.tmf.core.signal.TmfEndSynchSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfEventTypeCollectionHelper;
import org.eclipse.tracecompass.tmf.ctf.core.context.CtfLocation;
import org.eclipse.tracecompass.tmf.ctf.core.context.CtfLocationInfo;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * The class <code>CtfTmfTraceTest</code> contains tests for the class
 * <code>{@link CtfTmfTrace}</code>.
 *
 * @author ematkho
 * @version 1.0
 */
public class CtfTmfTraceTest {

    private static final @NonNull CtfTestTrace testTrace = CtfTestTrace.KERNEL;

    private static CtfTmfTrace fixture;

    /**
     * Perform pre-test initialization.
     */
    @BeforeClass
    public static void setUp() {
        fixture = CtfTmfTestTraceUtils.getTrace(testTrace);
    }

    /**
     * Perform post-test clean-up.
     */
    @AfterClass
    public static void tearDown() {
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
        assertEquals(1000, result.getCacheSize());
        assertEquals(0L, result.getNbEvents());
        assertEquals(0L, result.getStreamingInterval());
        assertNull(result.getResource());
        assertNull(result.getEventType());

        result.dispose();
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
        ctx.dispose();
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

        emptyFixture.dispose();
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
        CtfLocation loc = new CtfLocation(TmfTimestamp.fromNanos(0L));
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
        String result = fixture.getProperties().get(key);
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
        Set<? extends ITmfEventType> result = fixture.getContainedEventTypes();
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Run the double getLocationRatio(ITmfLocation<?>) method test.
     */
    @Test
    public void testGetLocationRatio() {
        ITmfContext context = fixture.seekEvent(0);
        long t1 = ((CtfLocationInfo) context.getLocation().getLocationInfo()).getTimestamp();
        fixture.getNext(context);
        long t2 = ((CtfLocationInfo) context.getLocation().getLocationInfo()).getTimestamp();
        fixture.getNext(context);
        long t3 = ((CtfLocationInfo) context.getLocation().getLocationInfo()).getTimestamp();
        fixture.getNext(context);
        context.dispose();
        double ratio1 = fixture.getLocationRatio(new CtfLocation(t1, 0));
        assertEquals(0.0, ratio1, 0.01);
        double ratio2 = fixture.getLocationRatio(new CtfLocation(t2, 0));
        assertEquals((double) (t2 - t1) / (t3 - t1), ratio2, 0.01);
        double ratio3 = fixture.getLocationRatio(new CtfLocation(t3, 0));
        assertEquals(1.0, ratio3, 0.01);
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
        int result = fixture.getProperties().size();
        assertEquals(10, result);
        assertEquals(String.valueOf(1332166405241713987L), fixture.getProperties().get("clock_offset"));
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
        context.dispose();
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
        context.dispose();
    }

    /**
     * Run the ITmfContext seekEvent(double) method test.
     */
    @Test
    public void testSeekEvent_ratio() {
        ITmfContext context = fixture.seekEvent(0);
        long t1 = ((CtfLocationInfo) context.getLocation().getLocationInfo()).getTimestamp();
        fixture.getNext(context);
        long t2 = ((CtfLocationInfo) context.getLocation().getLocationInfo()).getTimestamp();
        fixture.getNext(context);
        long t3 = ((CtfLocationInfo) context.getLocation().getLocationInfo()).getTimestamp();
        fixture.getNext(context);
        context.dispose();
        context = fixture.seekEvent(0.0);
        assertEquals(t1, ((CtfLocationInfo) context.getLocation().getLocationInfo()).getTimestamp());
        context.dispose();
        context = fixture.seekEvent(0.5);
        assertEquals(t2, ((CtfLocationInfo) context.getLocation().getLocationInfo()).getTimestamp());
        context.dispose();
        context = fixture.seekEvent(1.0);
        assertEquals(t3, ((CtfLocationInfo) context.getLocation().getLocationInfo()).getTimestamp());
        context.dispose();
    }

    /**
     * Run the ITmfContext seekEvent(long) method test.
     */
    @Test
    public void testSeekEvent_rank() {
        long rank = 1L;
        ITmfContext result = fixture.seekEvent(rank);
        assertNotNull(result);
        result.dispose();
    }

    /**
     * Run the ITmfContext seekEvent(ITmfTimestamp) method test.
     */
    @Test
    public void testSeekEvent_timestamp() {
        ITmfTimestamp timestamp = TmfTimestamp.create(0, ITmfTimestamp.SECOND_SCALE);
        ITmfContext result = fixture.seekEvent(timestamp);
        assertNotNull(result);
        result.dispose();
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
        result.dispose();
    }

    /**
     * Run the ITmfContext seekEvent(ITmfLocation<?>) method test with invalid location.
     */
    @Test
    public void testSeekEventInvalidLocation() {
        CtfLocation ctfLocation = new CtfLocation(CtfLocation.INVALID_LOCATION);
        ITmfContext result = fixture.seekEvent(ctfLocation);
        assertNull(fixture.getNext(result));
        assertEquals(CtfLocation.INVALID_LOCATION, result.getLocation().getLocationInfo());
        result.dispose();

        // Not using CtfLocation.INVALID_LOCATION directly on purpose, to make sure CtfLocationInfo.equals is properly used
        CtfLocationInfo invalidLocation = new CtfLocationInfo(CtfLocation.INVALID_LOCATION.getTimestamp(), CtfLocation.INVALID_LOCATION.getIndex());
        ctfLocation = new CtfLocation(invalidLocation);
        result = fixture.seekEvent(ctfLocation);
        assertNull(fixture.getNext(result));
        assertEquals(CtfLocation.INVALID_LOCATION, result.getLocation().getLocationInfo());
        result.dispose();
    }

    /**
     * Run the boolean validate(IProject,String) method test.
     */
    @Test
    public void testValidate() {
        IProject project = null;
        IStatus result = fixture.validate(project, fixture.getPath());
        assertTrue(result.isOK());
    }

    /**
     * Run the boolean hasEvent(final String) method test
     */
    @Test
    public void testEventLookup() {
        Set<@NonNull ? extends ITmfEventType> eventTypes = fixture.getContainedEventTypes();
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
