/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.component;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.tracecompass.internal.tmf.core.request.TmfCoalescedEventRequest;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest.ExecutionType;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.signal.TmfEndSynchSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfStartSynchSignal;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfExperimentStub;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfTraceStub;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test suite for the TmfEventProvider class focusing on coalescing of experiments.
 */
public class TmfEventProviderCoalescingTest {

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
     * Test setup-up
     */
    @Before
    public void testSetup() {
        // Reset the request IDs
        TmfEventRequest.reset();
    }


    /**
     * Test clean-up
     * @throws Exception if an error occurred
     */
    @After
    public void testCleanUp() throws Exception {
        // Reset the request IDs
        TmfEventRequest.reset();
        // clear pending request
        clearPendingRequests();
    }

    // ------------------------------------------------------------------------
    // Test coalescing across providers - parent request is send first
    // ------------------------------------------------------------------------

    /**
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

        TmfCoalescedEventRequest coalescedRequest = pending.get(0);
        assertEquals("[0, 1, 2]", coalescedRequest.getSubRequestIds());
    }

    // ------------------------------------------------------------------------
    // Test coalescing across providers - children requests are send first
    // ------------------------------------------------------------------------
    /**
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

        TmfCoalescedEventRequest coalescedRequest = pending.get(0);
        assertEquals("[0, 3, 4]", coalescedRequest.getSubRequestIds());
    }

    /**
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

        TmfCoalescedEventRequest coalescedRequest = pending.get(0);
        assertEquals("[0, 5, 6, 8]", coalescedRequest.getSubRequestIds());
    }

    /**
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

        TmfCoalescedEventRequest coalescedRequest = pending.get(0);
        assertEquals("[0, 3, 2]", coalescedRequest.getSubRequestIds());
    }

    /**
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

        TmfCoalescedEventRequest coalescedRequest = pending.get(0);
        assertEquals("[0, 5, 4, 1]", coalescedRequest.getSubRequestIds());
        sendSync(false);
    }

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
        public InnerEventRequest(Class<? extends ITmfEvent> dataType, long index, int nbRequested, ExecutionType priority) {
            super(dataType, index, nbRequested, priority);
        }
    }

}
