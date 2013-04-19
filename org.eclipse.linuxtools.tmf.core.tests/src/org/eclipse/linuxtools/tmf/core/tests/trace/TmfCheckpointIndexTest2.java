/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
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

package org.eclipse.linuxtools.tmf.core.tests.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.tests.TmfCoreTestPlugin;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfCheckpoint;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfCheckpointIndexer;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.TmfEmptyTraceStub;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.TmfTraceStub;
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

    private static final String    DIRECTORY   = "testfiles";
    // Trace has 3 events at t=101 at rank 99, 100, 101
    // Trace has events with same timestamp (ts=102) for ranks 102..702 -> 2 checkpoints with same timestamp are created
    private static final String    TEST_STREAM = "A-Test-10K-2";
    private static final int       BLOCK_SIZE  = 100;
    private static final int       NB_EVENTS   = 702;
    private static TestTrace       fTrace      = null;
    private static EmptyTestTrace  fEmptyTrace = null;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    @Before
    public void setUp() {
        setupTrace(DIRECTORY + File.separator + TEST_STREAM);
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

    private static class TestIndexer extends TmfCheckpointIndexer {
        @SuppressWarnings({ })
        public TestIndexer(TestTrace testTrace) {
            super(testTrace, BLOCK_SIZE);
        }
        @SuppressWarnings({ })
        public TestIndexer(EmptyTestTrace testTrace) {
            super(testTrace, BLOCK_SIZE);
        }
        public List<ITmfCheckpoint> getCheckpoints() {
            return getTraceIndex();
        }
    }

    private class TestTrace extends TmfTraceStub {
        public TestTrace(String path, int blockSize) throws TmfTraceException {
            super(path, blockSize);
            setIndexer(new TestIndexer(this));
        }
        @Override
        public TestIndexer getIndexer() {
            return (TestIndexer) super.getIndexer();
        }
    }

    private class EmptyTestTrace extends TmfEmptyTraceStub {
        public EmptyTestTrace() {
            super();
            setIndexer(new TestIndexer(this));
        }
        @Override
        public TestIndexer getIndexer() {
            return (TestIndexer) super.getIndexer();
        }
    }

    // ------------------------------------------------------------------------
    // Helper functions
    // ------------------------------------------------------------------------

    private synchronized void setupTrace(final String path) {
        if (fTrace == null) {
            try {
                final URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(path), null);
                final File test = new File(FileLocator.toFileURL(location).toURI());
                fTrace = new TestTrace(test.toURI().getPath(), BLOCK_SIZE);
                fTrace.indexTrace(true);
            } catch (final TmfTraceException e) {
                e.printStackTrace();
            } catch (final URISyntaxException e) {
                e.printStackTrace();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

        if (fEmptyTrace == null) {
            fEmptyTrace = new EmptyTestTrace();
            fEmptyTrace.indexTrace(true);
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

        List<ITmfCheckpoint> checkpoints = fTrace.getIndexer().getCheckpoints();
        assertTrue("Checkpoints exist",  checkpoints != null);
        assertEquals("Checkpoints size", NB_EVENTS / BLOCK_SIZE + 1, checkpoints.size());

        // Trace has 3 events with same timestamp (ts=101) at rank 99, 100, 101

        // Verify that the event at rank=99 is returned when seeking to ts=101 (first event with this timestamp)
        // and not the event at checkpoint boundary
        TmfTimestamp seekTs = new TmfTimestamp(101, -3, 0);
        ITmfContext ctx = fTrace.seekEvent(seekTs);
        ITmfEvent event = fTrace.getNext(ctx);

        assertEquals(99, ctx.getRank());
        assertEquals(0, seekTs.compareTo(event.getTimestamp(), false));

        event = fTrace.getNext(ctx);

        assertEquals(100, ctx.getRank());
        assertEquals(0, seekTs.compareTo(event.getTimestamp(), false));

        event = fTrace.getNext(ctx);

        assertEquals(101, ctx.getRank());
        assertEquals(0, seekTs.compareTo(event.getTimestamp(), false));

        // Trace has events with same timestamp (ts=102) for ranks 102..702 -> 2 checkpoints with same timestamp are created
        // Verify that the event at rank=102 is returned when seeking to ts=102 (first event with this timestamp)
        // and not the event at checkpoint boundary
        seekTs = new TmfTimestamp(102, -3, 0);
        ctx = fTrace.seekEvent(seekTs);
        event = fTrace.getNext(ctx);

        assertEquals(102, ctx.getRank());
        assertEquals(0, seekTs.compareTo(event.getTimestamp(), false));

        // Verify seek to first checkpoint
        seekTs = new TmfTimestamp(1, -3, 0);
        ctx = fTrace.seekEvent(seekTs);
        event = fTrace.getNext(ctx);

        assertEquals(1, ctx.getRank());
        assertEquals(0, seekTs.compareTo(event.getTimestamp(), false));

        // Verify seek to timestamp before first event
        seekTs = new TmfTimestamp(0, -3, 0);
        ctx = fTrace.seekEvent(seekTs);
        event = fTrace.getNext(ctx);

        assertEquals(1, ctx.getRank());
        assertEquals(0, new TmfTimestamp(1, -3, 0).compareTo(event.getTimestamp(), false));

        // Verify seek to timestamp between first and second checkpoint
        seekTs = new TmfTimestamp(50, -3, 0);
        ctx = fTrace.seekEvent(seekTs);
        event = fTrace.getNext(ctx);

        assertEquals(50, ctx.getRank());
        assertEquals(0, seekTs.compareTo(event.getTimestamp(), false));

        // Verify seek to timestamp after last event in trace
        seekTs = new TmfTimestamp(103, -3, 0);
        ctx = fTrace.seekEvent(seekTs);
        event = fTrace.getNext(ctx);

        assertEquals(-1, ctx.getRank());
        assertNull(event);
    }
}