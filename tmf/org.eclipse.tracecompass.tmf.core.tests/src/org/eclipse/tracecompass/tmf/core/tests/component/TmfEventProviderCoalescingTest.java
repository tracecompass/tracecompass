/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.tracecompass.internal.tmf.core.request.TmfCoalescedEventRequest;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest.ExecutionType;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.signal.TmfEndSynchSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfStartSynchSignal;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfExperimentStub;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfTraceStub;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test suite for the TmfEventProvider class focusing on coalescing of experiments.
 */
public class TmfEventProviderCoalescingTest {

    private static final int TRACE1_NB_EVENT = 10000;
    private static final int TRACE2_NB_EVENT = 702;
    private static final int TRACE3_NB_EVENT = 10000;
    private static TmfTraceStub fTmfTrace1;
    private static TmfTraceStub fTmfTrace2;
    private static TmfTraceStub fTmfTrace3;
    private static TmfExperimentStub fExperiment;
    private static TmfExperimentStub fExperiment2;

    /**
     * Test class initialization
     * @throws Exception in case of an error
     */
    @BeforeClass
    public static void setUp() throws Exception {
        fTmfTrace1 = (TmfTraceStub)TmfTestTrace.A_TEST_10K.getTrace();
        fTmfTrace2 = (TmfTraceStub)TmfTestTrace.A_TEST_10K2.getTrace();
        fTmfTrace3 = (TmfTraceStub)TmfTestTrace.E_TEST_10K.getTrace();

        ITmfTrace[] traces2 = new ITmfTrace[1];
        traces2[0] = fTmfTrace3;
        fExperiment2 = new TmfExperimentStub("", traces2, 100);

        ITmfTrace[] traces = new ITmfTrace[3];
        traces[0] = fTmfTrace1;
        traces[1] = fTmfTrace2;
        traces[2] = fExperiment2;
        fExperiment = new TmfExperimentStub("", traces, 100);

        fExperiment.indexTrace(true);
        // Disable the coalescing timers
        setTimerFlags(false);
    }

    /**
     * Test class clean-up
     */
    @AfterClass
    public static void tearDown() {
        fExperiment.dispose();
    }

    /**
     * Test clean-up
     * @throws Exception if an error occurred
     */
    @After
    public void testCleanUp() throws Exception {
        // clear pending request
        clearPendingRequests();
    }

    /**
     * Verify coalescing across providers where a parent request is sent first
     * before the children requests. All requests are coalesced at the top
     * level parent.
     *
     * @throws Exception if an error occurred
     */
    @Test
    public void testParentFirstCoalescing() throws Exception {
        InnerEventRequest expReq = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
        InnerEventRequest trace1Req = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
        InnerEventRequest trace2Req = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
        fExperiment.sendRequest(expReq);
        fTmfTrace1.sendRequest(trace1Req);
        fTmfTrace2.sendRequest(trace2Req);

        // Verify that requests are coalesced properly with the experiment request
        List<TmfCoalescedEventRequest> pending = fExperiment.getAllPendingRequests();

        assertEquals(1, pending.size());

        assertEquals(0, fTmfTrace1.getAllPendingRequests().size());
        assertEquals(0, fTmfTrace2.getAllPendingRequests().size());

        String expectedIds = "[" + expReq.getRequestId() + ", "
                + trace1Req.getRequestId() + ", "
                + trace2Req.getRequestId() + "]";
        TmfCoalescedEventRequest coalescedRequest = pending.get(0);
        assertEquals(expectedIds, coalescedRequest.getSubRequestIds());
    }

    /**
     * Verify coalescing across providers where requests from multiple children
     * are sent first before the parent request. All requests are coalesced at
     * the top level parent.
     *
     * @throws Exception if an error occurred
     */
    @Test
    public void testChildrenFirstCoalescing() throws Exception {
        InnerEventRequest expReq = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
        InnerEventRequest trace1Req = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
        InnerEventRequest trace2Req = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
        fTmfTrace1.sendRequest(trace1Req);
        fTmfTrace2.sendRequest(trace2Req);
        fExperiment.sendRequest(expReq);

        // Verify that requests are coalesced properly with the experiment request
        List<TmfCoalescedEventRequest> pending = fExperiment.getAllPendingRequests();
        assertEquals(1, pending.size());

        assertEquals(0, fTmfTrace1.getAllPendingRequests().size());
        assertEquals(0, fTmfTrace2.getAllPendingRequests().size());

        // Now trigger manually the sending of the request
        fExperiment.notifyPendingRequest(false);

        try {
            expReq.waitForCompletion();
        } catch (InterruptedException e) {
        }

        // Verify that requests only received events from the relevant traces
        assertTrue(expReq.isTraceHandled(fTmfTrace1));
        assertTrue(expReq.isTraceHandled(fTmfTrace2));

        assertTrue(trace1Req.isTraceHandled(fTmfTrace1));
        assertFalse(trace1Req.isTraceHandled(fTmfTrace2));

        assertFalse(trace2Req.isTraceHandled(fTmfTrace1));
        assertTrue(trace2Req.isTraceHandled(fTmfTrace2));
    }

    /**
     * Verify coalescing across providers (2 level deep) where requests from
     * multiple children (leafs) are sent first before the parent requests.
     * All requests are coalesced at the top level parent.
     *
     * @throws Exception if an error occurred
     */
    @Test
    public void testChildrenFirstCoalescing2() throws Exception {
        InnerEventRequest expReq = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
        InnerEventRequest exp2Req = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
        InnerEventRequest trace1Req = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
        InnerEventRequest trace2Req = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
        InnerEventRequest trace3Req = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
        fTmfTrace1.sendRequest(trace1Req);
        fTmfTrace2.sendRequest(trace2Req);
        fTmfTrace3.sendRequest(trace3Req);
        fExperiment2.sendRequest(exp2Req);
        fExperiment.sendRequest(expReq);

        // Verify that requests are coalesced properly with the experiment request
        List<TmfCoalescedEventRequest> pending = fExperiment.getAllPendingRequests();
        assertEquals(1, pending.size());

        assertEquals(0, fTmfTrace1.getAllPendingRequests().size());
        assertEquals(0, fTmfTrace2.getAllPendingRequests().size());

        // Now trigger manually the sending of the request
        fExperiment.notifyPendingRequest(false);

        try {
            expReq.waitForCompletion();
        } catch (InterruptedException e) {
        }

        // Verify that requests only received events from the relevant traces
        assertTrue(expReq.isTraceHandled(fTmfTrace1));
        assertTrue(expReq.isTraceHandled(fTmfTrace2));
        assertTrue(expReq.isTraceHandled(fTmfTrace3));

        assertFalse(exp2Req.isTraceHandled(fTmfTrace1));
        assertFalse(exp2Req.isTraceHandled(fTmfTrace2));
        assertTrue(exp2Req.isTraceHandled(fTmfTrace3));

        assertTrue(trace1Req.isTraceHandled(fTmfTrace1));
        assertFalse(trace1Req.isTraceHandled(fTmfTrace2));
        assertFalse(trace1Req.isTraceHandled(fTmfTrace3));

        assertFalse(trace2Req.isTraceHandled(fTmfTrace1));
        assertTrue(trace2Req.isTraceHandled(fTmfTrace2));
        assertFalse(trace2Req.isTraceHandled(fTmfTrace3));

        assertFalse(trace3Req.isTraceHandled(fTmfTrace1));
        assertFalse(trace3Req.isTraceHandled(fTmfTrace2));
        assertTrue(trace3Req.isTraceHandled(fTmfTrace3));
    }

    /**
     * Verify coalescing across providers where requests from the parent and
     * children are sent in mixed order. All requests are coalesced at the
     * top level parent.
     *
     * @throws Exception if an error occurred
     */
    @Test
    public void testMixedOrderCoalescing() throws Exception {
        InnerEventRequest expReq = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
        InnerEventRequest trace1Req = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
        InnerEventRequest trace2Req = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
        fTmfTrace1.sendRequest(trace1Req);
        fExperiment.sendRequest(expReq);
        fTmfTrace2.sendRequest(trace2Req);

        // Verify that requests are coalesced properly with the experiment request
        List<TmfCoalescedEventRequest> pending = fExperiment.getAllPendingRequests();
        assertEquals(1, pending.size());

        assertEquals(0, fTmfTrace1.getAllPendingRequests().size());
        assertEquals(0, fTmfTrace2.getAllPendingRequests().size());

        // Now trigger manually the sending of the request
        fExperiment.notifyPendingRequest(false);

        try {
            expReq.waitForCompletion();
        } catch (InterruptedException e) {
        }

        // Verify that requests only received events from the relevant traces
        assertTrue(expReq.isTraceHandled(fTmfTrace1));
        assertTrue(expReq.isTraceHandled(fTmfTrace2));

        assertTrue(trace1Req.isTraceHandled(fTmfTrace1));
        assertFalse(trace1Req.isTraceHandled(fTmfTrace2));

        assertFalse(trace2Req.isTraceHandled(fTmfTrace1));
        assertTrue(trace2Req.isTraceHandled(fTmfTrace2));
    }

    /**
     * Verify coalescing across multi-level providers where requests from the
     * parent and children are sent in mixed order. All requests are coalesced
     * at the top level parent.
     *
     * @throws Exception if an error occurred
     */
    @Test
    public void testMixedCoalescing2() throws Exception {
        InnerEventRequest expReq = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
        InnerEventRequest exp2Req = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
        InnerEventRequest trace1Req = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
        InnerEventRequest trace2Req = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
        InnerEventRequest trace3Req = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
        fExperiment2.sendRequest(exp2Req);
        fExperiment.sendRequest(expReq);
        fTmfTrace1.sendRequest(trace1Req);
        fTmfTrace2.sendRequest(trace2Req);
        fTmfTrace3.sendRequest(trace3Req);

        // Verify that requests are coalesced properly with the experiment request
        List<TmfCoalescedEventRequest> pending = fExperiment.getAllPendingRequests();
        assertEquals(1, pending.size());

        assertEquals(0, fTmfTrace1.getAllPendingRequests().size());
        assertEquals(0, fTmfTrace2.getAllPendingRequests().size());
        assertEquals(0, fTmfTrace3.getAllPendingRequests().size());

        // Now trigger manually the sending of the request
        fExperiment.notifyPendingRequest(false);

        try {
            expReq.waitForCompletion();
        } catch (InterruptedException e) {
        }

        // Verify that requests only received events from the relevant traces
        assertTrue(expReq.isTraceHandled(fTmfTrace1));
        assertTrue(expReq.isTraceHandled(fTmfTrace2));
        assertTrue(expReq.isTraceHandled(fTmfTrace3));

        assertFalse(exp2Req.isTraceHandled(fTmfTrace1));
        assertFalse(exp2Req.isTraceHandled(fTmfTrace2));
        assertTrue(exp2Req.isTraceHandled(fTmfTrace3));

        assertTrue(trace1Req.isTraceHandled(fTmfTrace1));
        assertFalse(trace1Req.isTraceHandled(fTmfTrace2));
        assertFalse(trace1Req.isTraceHandled(fTmfTrace3));

        assertFalse(trace2Req.isTraceHandled(fTmfTrace1));
        assertTrue(trace2Req.isTraceHandled(fTmfTrace2));
        assertFalse(trace2Req.isTraceHandled(fTmfTrace3));

        assertFalse(trace3Req.isTraceHandled(fTmfTrace1));
        assertFalse(trace3Req.isTraceHandled(fTmfTrace2));
        assertTrue(trace3Req.isTraceHandled(fTmfTrace3));
    }

    /**
     * Verify coalescing across multi-level providers where requests from the
     * parent and children are sent in mixed order. Each provider sends
     * multiple requests. All requests are coalesced at the top level parent.
     *
     * @throws Exception if an error occurred
     */
    @Test
    public void testMultipleRequestsCoalescing() throws Exception {
        InnerEventRequest expReq = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.FOREGROUND);
        InnerEventRequest expReq2 = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.FOREGROUND);
        InnerEventRequest trace1Req = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.FOREGROUND);
        InnerEventRequest trace1Req2 = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.FOREGROUND);
        InnerEventRequest trace2Req = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.FOREGROUND);
        sendSync(true);
        fTmfTrace1.sendRequest(trace1Req);
        fTmfTrace1.sendRequest(trace1Req2);
        fExperiment.sendRequest(expReq);
        fTmfTrace2.sendRequest(trace2Req);
        fExperiment.sendRequest(expReq2);

        // Verify that requests are coalesced properly with the experiment request
        List<TmfCoalescedEventRequest> pending = fExperiment.getAllPendingRequests();
        assertEquals(1, pending.size());

        assertEquals(0, fTmfTrace1.getAllPendingRequests().size());
        assertEquals(0, fTmfTrace2.getAllPendingRequests().size());
        assertEquals(0, fTmfTrace3.getAllPendingRequests().size());
        assertEquals(0, fExperiment2.getAllPendingRequests().size());

        sendSync(false);

        try {
            expReq.waitForCompletion();
        } catch (InterruptedException e) {
        }

        assertTrue(expReq.isTraceHandled(fTmfTrace1));
        assertTrue(expReq.isTraceHandled(fTmfTrace2));
        assertTrue(expReq.isTraceHandled(fTmfTrace3));

        assertTrue(expReq2.isTraceHandled(fTmfTrace1));
        assertTrue(expReq2.isTraceHandled(fTmfTrace2));
        assertTrue(expReq2.isTraceHandled(fTmfTrace3));

        assertTrue(trace1Req.isTraceHandled(fTmfTrace1));
        assertFalse(trace1Req.isTraceHandled(fTmfTrace2));
        assertFalse(trace1Req.isTraceHandled(fTmfTrace3));

        assertFalse(trace2Req.isTraceHandled(fTmfTrace1));
        assertTrue(trace2Req.isTraceHandled(fTmfTrace2));
        assertFalse(trace2Req.isTraceHandled(fTmfTrace3));
    }

    /**
     * Verify coalescing for a trace with different dependency level. Requests
     * on the same dependency level are coalesced together.
     *
     * @throws Exception
     *             if an error occurred
     */
    @Test
    public void testTraceDependencyBackground() throws Exception {
        InnerEventRequest traceReqLevel0 = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
        InnerEventRequest traceReq2Level0 = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
        InnerEventRequest traceReqLevel1 = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND, 1);
        InnerEventRequest traceReq2Level1 = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND, 1);
        fTmfTrace1.sendRequest(traceReqLevel0);
        fTmfTrace1.sendRequest(traceReq2Level0);
        fTmfTrace1.sendRequest(traceReqLevel1);
        fTmfTrace1.sendRequest(traceReq2Level1);

        // Verify that requests are coalesced properly with the experiment
        // request
        List<TmfCoalescedEventRequest> pending = fTmfTrace1.getAllPendingRequests();
        assertEquals(2, pending.size());

        // Now trigger manually the sending of the request
        fTmfTrace1.notifyPendingRequest(false);

        /*
         * traceReqLevel0 and trace1Req are coalesced together
         */
        String expectedIds = "[" + traceReqLevel0.getRequestId() + ", "
                + traceReq2Level0.getRequestId() + "]";
        TmfCoalescedEventRequest coalescedRequest = pending.get(0);
        assertEquals(expectedIds, coalescedRequest.getSubRequestIds());

        /*
         * traceReqLevel1 and traceReq2Level1 are coalesced together
         */
        expectedIds = "[" + traceReqLevel1.getRequestId() + ", "
                + traceReq2Level1.getRequestId() + "]";
        coalescedRequest = pending.get(1);
        assertEquals(expectedIds, coalescedRequest.getSubRequestIds());

        traceReqLevel0.waitForCompletion();
        traceReq2Level0.waitForCompletion();
        traceReqLevel1.waitForCompletion();
        traceReq2Level1.waitForCompletion();

        assertEquals(TRACE1_NB_EVENT, traceReqLevel0.getNbRead());
        assertEquals(TRACE1_NB_EVENT, traceReq2Level0.getNbRead());
        assertEquals(TRACE1_NB_EVENT, traceReqLevel1.getNbRead());
        assertEquals(TRACE1_NB_EVENT, traceReq2Level1.getNbRead());
    }

    /**
     * Verify coalescing for a trace with different dependency level. Requests
     * on the same dependency level are coalesced together.
     *
     * @throws Exception
     *             if an error occurred
     */
    @Test
    public void testTraceDependencyForeground() throws Exception {
        InnerEventRequest traceReqLevel0 = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.FOREGROUND);
        InnerEventRequest traceReq2Level0 = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.FOREGROUND);
        InnerEventRequest traceReqLevel1 = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.FOREGROUND, 1);
        InnerEventRequest traceReq2Level1 = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.FOREGROUND, 1);
        sendSync(true);
        fTmfTrace1.sendRequest(traceReqLevel0);
        fTmfTrace1.sendRequest(traceReq2Level0);
        fTmfTrace1.sendRequest(traceReqLevel1);
        fTmfTrace1.sendRequest(traceReq2Level1);

        // Verify that requests are coalesced properly with the experiment
        // request
        List<TmfCoalescedEventRequest> pending = fTmfTrace1.getAllPendingRequests();
        assertEquals(2, pending.size());

        sendSync(false);
        /*
         * traceReqLevel0 and trace1Req are coalesced together
         */
        String expectedIds = "[" + traceReqLevel0.getRequestId() + ", "
                + traceReq2Level0.getRequestId() + "]";
        TmfCoalescedEventRequest coalescedRequest = pending.get(0);
        assertEquals(expectedIds, coalescedRequest.getSubRequestIds());

        /*
         * traceReqLevel1 and traceReq2Level1 are coalesced together
         */
        expectedIds = "[" + traceReqLevel1.getRequestId() + ", "
                + traceReq2Level1.getRequestId() + "]";
        coalescedRequest = pending.get(1);
        assertEquals(expectedIds, coalescedRequest.getSubRequestIds());

        traceReqLevel0.waitForCompletion();
        traceReq2Level0.waitForCompletion();
        traceReqLevel1.waitForCompletion();
        traceReq2Level1.waitForCompletion();

        assertEquals(TRACE1_NB_EVENT, traceReqLevel0.getNbRead());
        assertEquals(TRACE1_NB_EVENT, traceReq2Level0.getNbRead());
        assertEquals(TRACE1_NB_EVENT, traceReqLevel1.getNbRead());
        assertEquals(TRACE1_NB_EVENT, traceReq2Level1.getNbRead());
    }

    /**
     * Verify coalescing across providers where a parent request is sent first
     * before the children requests. One child request has a dependency on the
     * other child so that it is not coalesced with the other requests.
     *
     * @throws Exception if an error occurred
     */
    @Test
    public void testParentDependencyBackground() throws Exception {
        InnerEventRequest expReq = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
        InnerEventRequest expReq2 = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND, 1);
        InnerEventRequest trace1Req = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
        InnerEventRequest trace2Req = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND, 1);

        fExperiment.sendRequest(expReq);
        fTmfTrace1.sendRequest(trace1Req);
        fExperiment.sendRequest(expReq2);
        fTmfTrace2.sendRequest(trace2Req);

        // Verify that requests are coalesced properly with the experiment request
        List<TmfCoalescedEventRequest> pending = fExperiment.getAllPendingRequests();
        assertEquals(2, pending.size());

        assertEquals(0, fTmfTrace1.getAllPendingRequests().size());
        assertEquals(0, fTmfTrace2.getAllPendingRequests().size());

        // Now trigger manually the sending of the request
        fExperiment.notifyPendingRequest(false);

        /*
         * expReq and trace1Req are coalesced together
         */
        String expectedIds = "[" + expReq.getRequestId() + ", "
                + trace1Req.getRequestId() + "]";
        TmfCoalescedEventRequest coalescedRequest = pending.get(0);
        assertEquals(expectedIds, coalescedRequest.getSubRequestIds());

        /*
         * expReq2 and trace2Req are coalesced together
         */
        expectedIds = "[" + expReq2.getRequestId() + ", "
                + trace2Req.getRequestId() + "]";
        coalescedRequest = pending.get(1);
        assertEquals(expectedIds, coalescedRequest.getSubRequestIds());

        expReq.waitForCompletion();
        expReq2.waitForCompletion();
        trace1Req.waitForCompletion();
        trace2Req.waitForCompletion();

        assertTrue(expReq.isTraceHandled(fTmfTrace1));
        assertTrue(expReq.isTraceHandled(fTmfTrace2));
        assertTrue(expReq.isTraceHandled(fTmfTrace3));

        assertTrue(trace1Req.isTraceHandled(fTmfTrace1));
        assertFalse(trace1Req.isTraceHandled(fTmfTrace2));
        assertFalse(trace1Req.isTraceHandled(fTmfTrace3));

        assertTrue(expReq2.isTraceHandled(fTmfTrace1));
        assertTrue(expReq2.isTraceHandled(fTmfTrace2));
        assertTrue(expReq2.isTraceHandled(fTmfTrace3));

        assertFalse(trace2Req.isTraceHandled(fTmfTrace1));
        assertTrue(trace2Req.isTraceHandled(fTmfTrace2));
        assertFalse(trace2Req.isTraceHandled(fTmfTrace3));

        assertEquals(TRACE1_NB_EVENT + TRACE2_NB_EVENT + TRACE3_NB_EVENT, expReq.getNbRead());
        assertEquals(TRACE1_NB_EVENT, trace1Req.getNbRead());
        assertEquals(TRACE2_NB_EVENT, trace2Req.getNbRead());
    }

    /**
     * Verify coalescing across providers where a child request is sent first
     * before the children requests. One child request is deferred so that it is
     * not coalesced at the top level parent.
     *
     * @throws Exception
     *             if an error occurred
     */
    @Test
    public void testChildFirstDependencyBackground() throws Exception {
        InnerEventRequest expReq = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
        InnerEventRequest expReq2 = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND, 1);
        InnerEventRequest trace1Req = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
        InnerEventRequest trace2Req = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND, 1);

        fTmfTrace1.sendRequest(trace1Req);
        fExperiment.sendRequest(expReq);
        fTmfTrace2.sendRequest(trace2Req);
        fExperiment.sendRequest(expReq2);

        // Verify that requests are coalesced properly with the experiment
        // request
        List<TmfCoalescedEventRequest> pending = fExperiment.getAllPendingRequests();
        assertEquals(2, pending.size());

        assertEquals(0, fTmfTrace1.getAllPendingRequests().size());
        assertEquals(0, fTmfTrace2.getAllPendingRequests().size());

        // Now trigger manually the sending of the request
        fExperiment.notifyPendingRequest(false);
        fTmfTrace1.notifyPendingRequest(false);
        fTmfTrace2.notifyPendingRequest(false);

        /*
         * expReq and trace1Req are coalesced together
         */
        String expectedIds = "[" + expReq.getRequestId() + ", "
                + trace1Req.getRequestId() + "]";
        TmfCoalescedEventRequest coalescedRequest = pending.get(0);
        assertEquals(expectedIds, coalescedRequest.getSubRequestIds());

        /*
         * expReq2 and trace2Req are coalesced together
         */
        expectedIds = "[" + expReq2.getRequestId() + ", "
                + trace2Req.getRequestId() + "]";
        coalescedRequest = pending.get(1);
        assertEquals(expectedIds, coalescedRequest.getSubRequestIds());

        expReq.waitForCompletion();
        expReq2.waitForCompletion();
        trace1Req.waitForCompletion();
        trace2Req.waitForCompletion();

        assertTrue(expReq.isTraceHandled(fTmfTrace1));
        assertTrue(expReq.isTraceHandled(fTmfTrace2));
        assertTrue(expReq.isTraceHandled(fTmfTrace3));

        assertTrue(trace1Req.isTraceHandled(fTmfTrace1));
        assertFalse(trace1Req.isTraceHandled(fTmfTrace2));
        assertFalse(trace1Req.isTraceHandled(fTmfTrace3));

        assertTrue(expReq2.isTraceHandled(fTmfTrace1));
        assertTrue(expReq2.isTraceHandled(fTmfTrace2));
        assertTrue(expReq2.isTraceHandled(fTmfTrace3));

        assertFalse(trace2Req.isTraceHandled(fTmfTrace1));
        assertTrue(trace2Req.isTraceHandled(fTmfTrace2));
        assertFalse(trace2Req.isTraceHandled(fTmfTrace3));

        assertEquals(TRACE1_NB_EVENT + TRACE2_NB_EVENT + TRACE3_NB_EVENT, expReq.getNbRead());
        assertEquals(TRACE1_NB_EVENT, trace1Req.getNbRead());
        assertEquals(TRACE2_NB_EVENT, trace2Req.getNbRead());
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------
    private static void sendSync(boolean isStart) {
        if (isStart) {
            TmfStartSynchSignal signal = new TmfStartSynchSignal(0);
            fTmfTrace1.startSynch(signal);
            fTmfTrace1.startSynch(signal);
            fExperiment.startSynch(signal);
            fTmfTrace2.startSynch(signal);
            fExperiment.startSynch(signal);

        } else {
            TmfEndSynchSignal signal = new TmfEndSynchSignal(0);
            fTmfTrace1.endSynch(signal);
            fTmfTrace1.endSynch(signal);
            fExperiment.endSynch(signal);
            fTmfTrace2.endSynch(signal);
            fExperiment.endSynch(signal);
        }
    }

    private static void setTimerFlags(boolean flag) throws Exception {
        fExperiment.setTimerEnabledFlag(flag);
        fExperiment2.setTimerEnabledFlag(flag);
        fTmfTrace1.setTimerEnabledFlag(flag);
        fTmfTrace2.setTimerEnabledFlag(flag);
        fTmfTrace3.setTimerEnabledFlag(flag);
    }

    private static void clearPendingRequests() throws Exception {
        fExperiment.clearAllPendingRequests();
        fExperiment2.clearAllPendingRequests();
        fTmfTrace1.clearAllPendingRequests();
        fTmfTrace2.clearAllPendingRequests();
        fTmfTrace3.clearAllPendingRequests();
    }

    private static class InnerEventRequest extends TmfEventRequest {
        private Set<String> traces = new HashSet<>();

        public InnerEventRequest(Class<? extends ITmfEvent> dataType, long index, int nbRequested, ExecutionType priority) {
            super(dataType, index, nbRequested, priority);
        }

        public InnerEventRequest(Class<? extends ITmfEvent> dataType, long index, int nbRequested, ExecutionType priority, int dependency) {
            super(dataType, TmfTimeRange.ETERNITY, index, nbRequested, priority, dependency);
        }

        @Override
        public void handleData(ITmfEvent event) {
            super.handleData(event);
            if (!traces.contains(event.getTrace().getName())) {
                traces.add(event.getTrace().getName());
            }
        }

        public boolean isTraceHandled(ITmfTrace trace) {
            return traces.contains(trace.getName());
        }
    }

}
