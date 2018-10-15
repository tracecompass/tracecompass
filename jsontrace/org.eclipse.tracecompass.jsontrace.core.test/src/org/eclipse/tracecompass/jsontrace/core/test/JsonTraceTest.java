/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.jsontrace.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.jsontrace.core.test.stub.JsonStubTrace;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.junit.Test;

/**
 * Test generic Json trace
 *
 * @author Simon Delisle
 */
public class JsonTraceTest {

    /**
     * Test the unsorted json trace
     *
     * @throws TmfTraceException
     *             If there is a problem while initializing the trace
     */
    @Test
    public void testSortedTrace() throws TmfTraceException {
        String path = "traces/sortedTrace.json"; //$NON-NLS-1$
        long nbEvents = 5;
        ITmfTimestamp startTime = TmfTimestamp.fromNanos(1);
        ITmfTimestamp endTime = TmfTimestamp.fromNanos(5);
        testJsonTrace(path, nbEvents, startTime, endTime);
    }

    /**
     * Test the unsorted json trace
     *
     * @throws TmfTraceException
     *             If there is a problem while initializing the trace
     */
    @Test
    public void testUnsortedTrace() throws TmfTraceException {
        String path = "traces/unsortedTrace.json"; //$NON-NLS-1$
        long nbEvents = 5;
        ITmfTimestamp startTime = TmfTimestamp.fromNanos(1);
        ITmfTimestamp endTime = TmfTimestamp.fromNanos(5);
        testJsonTrace(path, nbEvents, startTime, endTime);
    }

    private void testJsonTrace(String path, long expectedNbEvents, ITmfTimestamp startTime, ITmfTimestamp endTime)
            throws TmfTraceException {
        ITmfTrace trace = new JsonStubTrace();
        try {
            IStatus validate = trace.validate(null, path);
            assertTrue(validate.getMessage(), validate.isOK());
            trace.initTrace(null, path, ITmfEvent.class);
            ITmfContext context = trace.seekEvent(0.0);
            ITmfEvent event = trace.getNext(context);
            long count = 0;
            long prevTs = -1;
            while (event != null) {
                count++;
                @NonNull
                ITmfTimestamp currentTime = event.getTimestamp();
                assertNotNull(currentTime);
                // Make sure that the event are ordered
                assertTrue(currentTime.toNanos() >= prevTs);
                prevTs = currentTime.toNanos();
                event = trace.getNext(context);
            }
            assertEquals(expectedNbEvents, count);
            assertEquals(expectedNbEvents, trace.getNbEvents());
            assertEquals(startTime.toNanos(), trace.getStartTime().toNanos());
            assertEquals(endTime.toNanos(), trace.getEndTime().toNanos());
        } finally {
            trace.dispose();
        }
    }

}
