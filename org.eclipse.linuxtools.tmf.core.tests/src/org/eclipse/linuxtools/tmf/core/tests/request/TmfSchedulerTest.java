/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Simon Delisle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.core.tests.shared.CtfTmfTestTrace;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

/**
 * Test suite for the scheduler.
 */
public class TmfSchedulerTest {

    /** Time-out tests after 60 seconds */
    @Rule
    public TestRule globalTimeout= new Timeout(60000);

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final CtfTmfTestTrace testTrace = CtfTmfTestTrace.KERNEL;
    private static final int NB_EVENTS_TRACE = 695319;
    private static final int NB_EVENTS_TIME_RANGE = 155133;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private CtfTmfTrace fixture;

    private long fStartTime;
    private long fEndTime;
    private TmfTimeRange fForegroundTimeRange;

    private final List<String> fOrderList = new ArrayList<String>();
    private int fForegroundId = 0;
    private int fBackgroundId = 0;

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
        fixture.indexTrace(true);
        fStartTime = fixture.getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        fEndTime = fixture.getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();

        long foregroundStartTime = fStartTime + ((fEndTime - fStartTime) / 4);
        long foregroundEndTime = fStartTime + ((fEndTime - fStartTime) / 2);
        fForegroundTimeRange = new TmfTimeRange(new TmfTimestamp(foregroundStartTime, ITmfTimestamp.NANOSECOND_SCALE, 0), new TmfTimestamp(foregroundEndTime, ITmfTimestamp.NANOSECOND_SCALE, 0));
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

    // ------------------------------------------------------------------------
    // Tests cases
    // ------------------------------------------------------------------------

    /**
     * Test one background request
     */
    @Test
    public void backgroundRequest() {
        BackgroundRequest background = new BackgroundRequest(TmfTimeRange.ETERNITY);
        fixture.sendRequest(background);
        try {
            background.waitForCompletion();
        } catch (InterruptedException e) {
            fail();
        }
        assertEquals(NB_EVENTS_TRACE, background.getNbEvents());
    }

    /**
     * Test one foreground request
     */
    @Test
    public void foregroundRequest() {
        ForegroundRequest foreground = new ForegroundRequest(TmfTimeRange.ETERNITY);
        fixture.sendRequest(foreground);
        try {
            foreground.waitForCompletion();
        } catch (InterruptedException e) {
            fail();
        }
        assertEquals(NB_EVENTS_TRACE, foreground.getNbEvents());
    }

    /**
     * Test one foreground and one background request for the entire trace at
     * the same time
     */
    @Test
    public void TestMultiRequest1() {
        BackgroundRequest background = new BackgroundRequest(TmfTimeRange.ETERNITY);
        ForegroundRequest foreground = new ForegroundRequest(TmfTimeRange.ETERNITY);

        fixture.sendRequest(background);
        fixture.sendRequest(foreground);
        try {
            background.waitForCompletion();
            foreground.waitForCompletion();
        } catch (InterruptedException e) {
            fail();
        }

        assertEquals(NB_EVENTS_TRACE, background.getNbEvents());
        assertEquals(NB_EVENTS_TRACE, foreground.getNbEvents());
    }

    /**
     * Test one background request for the entire trace and one foreground
     * request for smaller time range
     */
    @Test
    public void TestMultiRequest2() {
        BackgroundRequest background2 = new BackgroundRequest(TmfTimeRange.ETERNITY);
        ForegroundRequest foreground2 = new ForegroundRequest(fForegroundTimeRange);

        fixture.sendRequest(background2);
        fixture.sendRequest(foreground2);
        try {
            background2.waitForCompletion();
            foreground2.waitForCompletion();
        } catch (InterruptedException e) {
            fail();
        }

        assertEquals(NB_EVENTS_TRACE, background2.getNbEvents());
        assertEquals(NB_EVENTS_TIME_RANGE, foreground2.getNbEvents());
    }

    /**
     * Test two foreground request, one to select a time range and one to select
     * an event in this time range
     */
    @Test
    public void TestMultiRequest3() {
        ForegroundRequest foreground3 = new ForegroundRequest(TmfTimeRange.ETERNITY);
        fixture.sendRequest(foreground3);

        TmfTimeSynchSignal signal3 = new TmfTimeSynchSignal(this, new TmfTimestamp(fForegroundTimeRange.getStartTime()));
        fixture.broadcast(signal3);

        try {
            foreground3.waitForCompletion();
        } catch (InterruptedException e) {
            fail();
        }

        assertEquals(NB_EVENTS_TRACE, foreground3.getNbEvents());
    }

    /**
     * Test two foreground request, one to select a time range and one to select
     * an event before this time range
     */
    @Test
    public void TestMultiRequest4() {
        ForegroundRequest foreground4 = new ForegroundRequest(fForegroundTimeRange);
        fixture.sendRequest(foreground4);
        TmfTimeSynchSignal signal4 = new TmfTimeSynchSignal(this, new TmfTimestamp(fStartTime + ((fEndTime - fStartTime) / 8)));
        fixture.broadcast(signal4);

        try {
            foreground4.waitForCompletion();
        } catch (InterruptedException e) {
            fail();
        }

        assertEquals(NB_EVENTS_TIME_RANGE, foreground4.getNbEvents());
    }

    /**
     * Test two foreground request, one to select a time range and one to select
     * an event after this time range
     */
    @Test
    public void TestMultiRequest5() {
        ForegroundRequest foreground5 = new ForegroundRequest(fForegroundTimeRange);
        fixture.sendRequest(foreground5);
        TmfTimeSynchSignal signal5 = new TmfTimeSynchSignal(this, new TmfTimestamp(fEndTime - ((fEndTime - fStartTime) / 4)));
        fixture.broadcast(signal5);

        try {
            foreground5.waitForCompletion();
        } catch (InterruptedException e) {
            fail();
        }

        assertEquals(NB_EVENTS_TIME_RANGE, foreground5.getNbEvents());
    }

    /**
     * Test one background and one foreground request for the entire trace and
     * one foreground request to select an event
     */
    @Test
    public void TestMultiRequest6() {
        BackgroundRequest background6 = new BackgroundRequest(TmfTimeRange.ETERNITY);
        ForegroundRequest foreground6 = new ForegroundRequest(TmfTimeRange.ETERNITY);

        fixture.sendRequest(background6);
        fixture.sendRequest(foreground6);

        TmfTimeSynchSignal signal6 = new TmfTimeSynchSignal(this, new TmfTimestamp(fStartTime + ((fEndTime - fStartTime) / 8)));
        fixture.broadcast(signal6);

        try {
            background6.waitForCompletion();
            foreground6.waitForCompletion();
        } catch (InterruptedException e) {
            fail();
        }

        assertEquals(NB_EVENTS_TRACE, background6.getNbEvents());
        assertEquals(NB_EVENTS_TRACE, foreground6.getNbEvents());
    }

    /**
     * Four request, two foreground and two background
     */
    @Test
    public void TestMultiRequest7() {
        ForegroundRequest foreground7 = new ForegroundRequest(TmfTimeRange.ETERNITY);
        ForegroundRequest foreground8 = new ForegroundRequest(fForegroundTimeRange);
        BackgroundRequest background7 = new BackgroundRequest(TmfTimeRange.ETERNITY);
        BackgroundRequest background8 = new BackgroundRequest(TmfTimeRange.ETERNITY);
        fixture.sendRequest(foreground7);
        fixture.sendRequest(foreground8);
        fixture.sendRequest(background7);
        fixture.sendRequest(background8);
        try {
            foreground7.waitForCompletion();
            foreground8.waitForCompletion();
            background7.waitForCompletion();
            background8.waitForCompletion();
        } catch (InterruptedException e) {
            fail();
        }
        assertEquals(NB_EVENTS_TRACE, foreground7.getNbEvents());
        assertEquals(NB_EVENTS_TIME_RANGE, foreground8.getNbEvents());
        assertEquals(NB_EVENTS_TRACE, background7.getNbEvents());
        assertEquals(NB_EVENTS_TRACE, background8.getNbEvents());
    }

    /**
     * One long foreground request and one short foreground request, the short
     * one should finish first
     */
    @Test
    public void preemptedForegroundRequest() {
        ForegroundRequest foreground9 = new ForegroundRequest(TmfTimeRange.ETERNITY);
        TmfTimeRange shortTimeRange = new TmfTimeRange(new TmfTimestamp(fStartTime, ITmfTimestamp.NANOSECOND_SCALE, 0), new TmfTimestamp(fStartTime + ((fEndTime - fStartTime) / 16), ITmfTimestamp.NANOSECOND_SCALE, 0));
        ForegroundRequest shortForeground = new ForegroundRequest(shortTimeRange);
        fixture.sendRequest(foreground9);
        fixture.sendRequest(shortForeground);
        try {
            shortForeground.waitForCompletion();
        } catch (InterruptedException e) {
            fail();
        }
        assertFalse(foreground9.isCompleted());
    }

    /**
     * One long background request and one short foreground request, the
     * foreground request should finish first
     */
    @Test
    public void preemptedBackgroundRequest() {
        BackgroundRequest background9 = new BackgroundRequest(TmfTimeRange.ETERNITY);
        ForegroundRequest foreground10 = new ForegroundRequest(fForegroundTimeRange);
        fixture.sendRequest(background9);
        fixture.sendRequest(foreground10);
        try {
            foreground10.waitForCompletion();
        } catch (InterruptedException e) {
            fail();
        }
        assertTrue(foreground10.isCompleted());
        assertFalse(background9.isCompleted());
    }

    /**
     * Test if the scheduler is working as expected
     */
    @Test
    public void executionOrder() {
        List<String> expectedOrder = new LinkedList<String>();
        expectedOrder.add("FOREGROUND1");
        expectedOrder.add("FOREGROUND2");
        expectedOrder.add("FOREGROUND3");
        expectedOrder.add("FOREGROUND4");
        expectedOrder.add("BACKGROUND1");
        expectedOrder.add("FOREGROUND1");
        expectedOrder.add("FOREGROUND2");
        expectedOrder.add("FOREGROUND3");
        expectedOrder.add("FOREGROUND4");
        expectedOrder.add("BACKGROUND2");

        fOrderList.clear();
        fForegroundId = 0;
        fBackgroundId = 0;

        BackgroundRequest background1 = new BackgroundRequest(TmfTimeRange.ETERNITY);
        BackgroundRequest background2 = new BackgroundRequest(TmfTimeRange.ETERNITY);

        ForegroundRequest foreground1 = new ForegroundRequest(TmfTimeRange.ETERNITY);
        ForegroundRequest foreground2 = new ForegroundRequest(TmfTimeRange.ETERNITY);
        ForegroundRequest foreground3 = new ForegroundRequest(TmfTimeRange.ETERNITY);
        ForegroundRequest foreground4 = new ForegroundRequest(TmfTimeRange.ETERNITY);

        fixture.sendRequest(foreground1);
        fixture.sendRequest(foreground2);
        fixture.sendRequest(foreground3);
        fixture.sendRequest(foreground4);
        fixture.sendRequest(background1);
        fixture.sendRequest(background2);
        try {
            foreground1.waitForCompletion();
            foreground2.waitForCompletion();
            foreground3.waitForCompletion();
            foreground4.waitForCompletion();
            background1.waitForCompletion();
            background2.waitForCompletion();
        } catch (InterruptedException e) {
            fail();
        }
        assertEquals(expectedOrder, fOrderList.subList(0, expectedOrder.size()));
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------

    private class BackgroundRequest extends TmfEventRequest {
        private int nbEvents = 0;
        private String backgroundName;

        BackgroundRequest(TmfTimeRange timeRange) {
            super(fixture.getEventType(),
                    timeRange,
                    0,
                    ITmfEventRequest.ALL_DATA,
                    ExecutionType.BACKGROUND);
            backgroundName = getExecType().toString() + ++fBackgroundId;
        }

        @Override
        public void handleData(final ITmfEvent event) {
            super.handleData(event);
            synchronized (fOrderList) {
                if (fOrderList.isEmpty() || !fOrderList.get(fOrderList.size() - 1).equals(backgroundName)) {
                    fOrderList.add(backgroundName);
                }
            }
            ++nbEvents;
        }

        public int getNbEvents() {
            return nbEvents;
        }
    }

    private class ForegroundRequest extends TmfEventRequest {
        private int nbEvents = 0;
        private String foregroundName;

        ForegroundRequest(TmfTimeRange timeRange) {
            super(fixture.getEventType(),
                    timeRange,
                    0,
                    ITmfEventRequest.ALL_DATA,
                    ExecutionType.FOREGROUND);
            foregroundName = getExecType().toString() + ++fForegroundId;
        }

        @Override
        public void handleData(final ITmfEvent event) {
            super.handleData(event);
            synchronized (fOrderList) {
                if (fOrderList.isEmpty() || !fOrderList.get(fOrderList.size() - 1).equals(foregroundName)) {
                    fOrderList.add(foregroundName);
                }
            }
            ++nbEvents;
        }

        public int getNbEvents() {
            return nbEvents;
        }
    }
}