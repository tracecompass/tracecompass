/*******************************************************************************
 * Copyright (c) 2012, 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Alexandre Montplaisir - Port to JUnit4
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.trace.indexer.checkpoint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.tracecompass.tmf.core.tests.trace.indexer.checkpoint.AbstractIndexTest.ITestIndexer;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.indexer.ITmfTraceIndexer;
import org.eclipse.tracecompass.tmf.core.trace.indexer.checkpoint.ITmfCheckpointIndex;
import org.eclipse.tracecompass.tmf.core.trace.indexer.checkpoint.TmfCheckpointIndexer;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfEmptyTraceStub;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfTraceStub;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for the TmfCheckpointIndexer class (events with same
 * timestamp around checkpoint).
 */
@SuppressWarnings("javadoc")
public class TmfCheckpointIndexTest2 {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private static final int       BLOCK_SIZE  = 100;
    private static final int       NB_EVENTS   = 702;
    private static TestTrace       fTrace      = null;
    private static EmptyTestTrace  fEmptyTrace = null;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    @Before
    public void setUp() {
        // Trace has 3 events at t=101 at rank 99, 100, 101
        // Trace has events with same timestamp (ts=102) for ranks 102..702 -> 2 checkpoints with same timestamp are created
        setupTrace(TmfTestTrace.A_TEST_10K2.getFullPath());
    }

    @After
    public void tearDown() {
        fTrace.dispose();
        fTrace = null;
        fEmptyTrace.dispose();
        fEmptyTrace = null;
    }

    // ------------------------------------------------------------------------
    // Helper classes
    // ------------------------------------------------------------------------

    private static class TestIndexer extends TmfCheckpointIndexer implements ITestIndexer {
        public TestIndexer(TestTrace testTrace) {
            super(testTrace, BLOCK_SIZE);
        }

        public TestIndexer(EmptyTestTrace testTrace) {
            super(testTrace, BLOCK_SIZE);
        }

        @Override
        public ITmfCheckpointIndex getCheckpoints() {
            return getTraceIndex();
        }
    }

    private class TestTrace extends TmfTraceStub {
        public TestTrace(String path, int blockSize) throws TmfTraceException {
            super(path, blockSize, false, null);
        }

        @Override
        protected ITmfTraceIndexer createIndexer(int interval) {
            return new TestIndexer(this);
        }

        @Override
        public ITestIndexer getIndexer() {
            return (ITestIndexer) super.getIndexer();
        }
    }

    private class EmptyTestTrace extends TmfEmptyTraceStub {

        public EmptyTestTrace(String path) throws TmfTraceException {
            super(path);
        }

        @Override
        protected ITmfTraceIndexer createIndexer(int interval) {
            return new TestIndexer(this);
        }

        @Override
        public ITestIndexer getIndexer() {
            return (ITestIndexer) super.getIndexer();
        }
    }

    // ------------------------------------------------------------------------
    // Helper functions
    // ------------------------------------------------------------------------

    private synchronized void setupTrace(final String path) {
        if (fTrace == null) {
            try {
                fTrace = new TestTrace(path, BLOCK_SIZE);
                fTrace.indexTrace(true);
            } catch (final TmfTraceException e) {
                e.printStackTrace();
            }
        }

        if (fEmptyTrace == null) {
            try {
                File file = File.createTempFile("empty", "txt");
                fEmptyTrace = new EmptyTestTrace(file.getAbsolutePath());
            } catch (TmfTraceException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    // ------------------------------------------------------------------------
    // Verify checkpoints
    // ------------------------------------------------------------------------

    @Test
    public void testTmfTraceMultiTimestamps() {
        assertEquals("getCacheSize",   BLOCK_SIZE, fTrace.getCacheSize());
        assertEquals("getTraceSize",   NB_EVENTS,  fTrace.getNbEvents());
        assertEquals("getRange-start", 1,          fTrace.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end",   102,        fTrace.getTimeRange().getEndTime().getValue());
        assertEquals("getStartTime",   1,          fTrace.getStartTime().getValue());
        assertEquals("getEndTime",     102,        fTrace.getEndTime().getValue());

        ITmfCheckpointIndex checkpoints = fTrace.getIndexer().getCheckpoints();
        assertTrue("Checkpoints exist",  checkpoints != null);
        assertEquals("Checkpoints size", NB_EVENTS / BLOCK_SIZE + 1, checkpoints.size());

        // Trace has 3 events with same timestamp (ts=101) at rank 99, 100, 101

        // Verify that the event at rank=99 is returned when seeking to ts=101 (first event with this timestamp)
        // and not the event at checkpoint boundary
        ITmfTimestamp seekTs = TmfTimestamp.create(101, -3);
        ITmfContext ctx = fTrace.seekEvent(seekTs);
        ITmfEvent event = fTrace.getNext(ctx);

        assertEquals(99, ctx.getRank());
        assertEquals(0, seekTs.compareTo(event.getTimestamp()));

        event = fTrace.getNext(ctx);

        assertEquals(100, ctx.getRank());
        assertEquals(0, seekTs.compareTo(event.getTimestamp()));

        event = fTrace.getNext(ctx);

        assertEquals(101, ctx.getRank());
        assertEquals(0, seekTs.compareTo(event.getTimestamp()));

        // Trace has events with same timestamp (ts=102) for ranks 102..702 -> 2 checkpoints with same timestamp are created
        // Verify that the event at rank=102 is returned when seeking to ts=102 (first event with this timestamp)
        // and not the event at checkpoint boundary
        seekTs = TmfTimestamp.create(102, -3);
        ctx = fTrace.seekEvent(seekTs);
        event = fTrace.getNext(ctx);

        assertEquals(102, ctx.getRank());
        assertEquals(0, seekTs.compareTo(event.getTimestamp()));

        // Verify seek to first checkpoint
        seekTs = TmfTimestamp.create(1, -3);
        ctx = fTrace.seekEvent(seekTs);
        event = fTrace.getNext(ctx);

        assertEquals(1, ctx.getRank());
        assertEquals(0, seekTs.compareTo(event.getTimestamp()));

        // Verify seek to timestamp before first event
        seekTs = TmfTimestamp.create(0, -3);
        ctx = fTrace.seekEvent(seekTs);
        event = fTrace.getNext(ctx);

        assertEquals(1, ctx.getRank());
        assertEquals(0, TmfTimestamp.create(1, -3).compareTo(event.getTimestamp()));

        // Verify seek to timestamp between first and second checkpoint
        seekTs = TmfTimestamp.create(50, -3);
        ctx = fTrace.seekEvent(seekTs);
        event = fTrace.getNext(ctx);

        assertEquals(50, ctx.getRank());
        assertEquals(0, seekTs.compareTo(event.getTimestamp()));

        // Verify seek to timestamp after last event in trace
        seekTs = TmfTimestamp.create(103, -3);
        ctx = fTrace.seekEvent(seekTs);
        event = fTrace.getNext(ctx);

        assertEquals(-1, ctx.getRank());
        assertNull(event);
    }
}
