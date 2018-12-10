/*******************************************************************************
 * Copyright (c) 2016, 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.core.component.TmfProviderManager;
import org.eclipse.tracecompass.internal.tmf.core.request.TmfCoalescedEventRequest;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest.ExecutionType;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.tests.stubs.request.TmfEventRequestStub;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfTraceStub;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

/**
 * Test how requests behave
 *
 * @author Geneviève Bastien
 */
public class TmfEventRequestIntegrationTest {

    /**
     * The test should timeout after a few seconds, that would mean a deadlock
     * may have happened
     */
     @Rule
     public TestRule globalTimeout = new Timeout(2, TimeUnit.MINUTES);

    private static final TmfTestTrace TEST_TRACE = TmfTestTrace.A_TEST_10K;

    // Initialize the test trace
    private static TmfTraceStub fTrace = null;

    /**
     * Test class setup
     */
    @BeforeClass
    public static void setUp() {
        try {
            fTrace = new TmfTraceStub(TEST_TRACE.getFullPath(), ITmfTrace.DEFAULT_TRACE_CACHE_SIZE, false, null);
            TmfSignalManager.deregister(fTrace);
            fTrace.indexTrace(true);
        } catch (final TmfTraceException e) {
            e.printStackTrace();
            fail("Error setting up test trace");
        }
    }

    /**
     * Test class clean-up
     */
    @AfterClass
    public static void tearDown() {
        if (fTrace != null) {
            fTrace.dispose();
            fTrace = null;
            assertEquals(0, TmfProviderManager.getProviders(ITmfEvent.class).length);
        }
    }

    /**
     * Test the behavior of a failed request
     *
     * @throws InterruptedException
     *             The test timed out
     */
    @Test
    public void testSingleRequestException() throws InterruptedException {
        TmfTrace trace = fTrace;

        TmfEventRequest requestFail = new TmfEventRequestStub(ITmfEvent.class, TmfTimeRange.ETERNITY, 2, 0, ExecutionType.BACKGROUND, 0) {

            @Override
            public void handleData(@NonNull ITmfEvent data) {
                throw new IllegalArgumentException();
            }

        };
        trace.sendRequest(requestFail);
        requestFail.waitForCompletion();

        assertTrue(requestFail.isCompleted());
        assertFalse(requestFail.isCancelled());
        assertTrue(requestFail.isFailed());
    }

    /**
     * Test the behavior of multiple coalesced requests when one fails
     *
     * @throws InterruptedException
     *             The test timed out
     */
    @Test
    public void testRequestException() throws InterruptedException {
        TmfTrace trace = fTrace;

        TmfCoalescedEventRequest allRequests = new TmfCoalescedEventRequest(ITmfEvent.class, TmfTimeRange.ETERNITY, 2, 0, ExecutionType.BACKGROUND, 0);
        TmfEventRequest requestOk = new TmfEventRequestStub(ITmfEvent.class, TmfTimeRange.ETERNITY, 2, 0, ExecutionType.BACKGROUND, 0);
        requestOk.setProviderFilter(trace);
        allRequests.addRequest(requestOk);
        TmfEventRequest requestFail = new TmfEventRequestStub(ITmfEvent.class, TmfTimeRange.ETERNITY, 2, 0, ExecutionType.BACKGROUND, 0) {

            @Override
            public void handleData(@NonNull ITmfEvent data) {
                throw new IllegalArgumentException();
            }

        };
        requestFail.setProviderFilter(trace);
        allRequests.addRequest(requestFail);

        trace.sendRequest(allRequests);

        requestOk.waitForCompletion();
        requestFail.waitForCompletion();

        assertTrue(requestOk.isCompleted());
        assertFalse(requestOk.isCancelled());
        assertFalse(requestOk.isFailed());

        assertTrue(requestFail.isCompleted());
        assertFalse(requestFail.isCancelled());
        assertTrue(requestFail.isFailed());
    }

}
