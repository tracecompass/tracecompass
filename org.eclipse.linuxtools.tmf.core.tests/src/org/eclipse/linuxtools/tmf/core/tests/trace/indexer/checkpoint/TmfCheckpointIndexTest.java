/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Adapted for TMF Trace Model 1.0
 *   Alexandre Montplaisir - Port to JUnit4
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.trace.indexer.checkpoint;

import static org.junit.Assert.assertEquals;
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
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.TmfCheckpointIndexer;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.ITmfCheckpoint;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.TmfEmptyTraceStub;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.TmfTraceStub;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for the TmfCheckpointIndexTest class.
 */
@SuppressWarnings("javadoc")
public class TmfCheckpointIndexTest {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private static final String    DIRECTORY   = "testfiles";
    private static final String    TEST_STREAM = "A-Test-10K";
    private static final int       BLOCK_SIZE  = 100;
    private static final int       NB_EVENTS   = 10000;
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
            super(path, blockSize, false, null, null);
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
    public void testTmfTraceIndexing() {
        assertEquals("getCacheSize",   BLOCK_SIZE, fTrace.getCacheSize());
        assertEquals("getTraceSize",   NB_EVENTS,  fTrace.getNbEvents());
        assertEquals("getRange-start", 1,          fTrace.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end",   NB_EVENTS,  fTrace.getTimeRange().getEndTime().getValue());
        assertEquals("getStartTime",   1,          fTrace.getStartTime().getValue());
        assertEquals("getEndTime",     NB_EVENTS,  fTrace.getEndTime().getValue());

        List<ITmfCheckpoint> checkpoints = fTrace.getIndexer().getCheckpoints();
        int pageSize = fTrace.getCacheSize();
        assertTrue("Checkpoints exist",  checkpoints != null);
        assertEquals("Checkpoints size", NB_EVENTS / BLOCK_SIZE, checkpoints.size());

        // Validate that each checkpoint points to the right event
        for (int i = 0; i < checkpoints.size(); i++) {
            ITmfCheckpoint checkpoint = checkpoints.get(i);
            TmfContext context = new TmfContext(checkpoint.getLocation(), i * pageSize);
            ITmfEvent event = fTrace.parseEvent(context);
            assertTrue(context.getRank() == i * pageSize);
            assertTrue((checkpoint.getTimestamp().compareTo(event.getTimestamp(), false) == 0));
        }
    }

    @Test
    public void testEmptyTmfTraceIndexing() {
        assertEquals("getCacheSize",   ITmfTrace.DEFAULT_TRACE_CACHE_SIZE, fEmptyTrace.getCacheSize());
        assertEquals("getTraceSize",   0,  fEmptyTrace.getNbEvents());
        assertEquals("getRange-start", TmfTimestamp.BIG_BANG, fEmptyTrace.getTimeRange().getStartTime());
        assertEquals("getRange-end",   TmfTimestamp.BIG_BANG, fEmptyTrace.getTimeRange().getEndTime());
        assertEquals("getStartTime",   TmfTimestamp.BIG_BANG, fEmptyTrace.getStartTime());
        assertEquals("getEndTime",     TmfTimestamp.BIG_BANG, fEmptyTrace.getEndTime());

        List<ITmfCheckpoint> checkpoints = fEmptyTrace.getIndexer().getCheckpoints();
        int pageSize = fEmptyTrace.getCacheSize();
        assertTrue("Checkpoints exist",  checkpoints != null);
        assertEquals("Checkpoints size", 0, checkpoints.size());

        // Validate that each checkpoint points to the right event
        for (int i = 0; i < checkpoints.size(); i++) {
            ITmfCheckpoint checkpoint = checkpoints.get(i);
            TmfContext context = new TmfContext(checkpoint.getLocation(), i * pageSize);
            ITmfEvent event = fEmptyTrace.parseEvent(context);
            assertTrue(context.getRank() == i * pageSize);
            assertTrue((checkpoint.getTimestamp().compareTo(event.getTimestamp(), false) == 0));
        }
    }

}