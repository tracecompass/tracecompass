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
 *   Marc-Andre Laperle - Extracted to a common class from TmfCheckpointIndexTest
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.trace.indexer.checkpoint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.tests.TmfCoreTestPlugin;
import org.eclipse.linuxtools.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.core.trace.indexer.ITmfTraceIndexer;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.ITmfCheckpoint;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.ITmfCheckpointIndex;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.TmfCheckpointIndexer;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.TmfEmptyTraceStub;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.TmfTraceStub;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Common code for index testing
 *
 * @author Marc-Andre Laperle
 */
public abstract class AbstractIndexTest {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    /**
     *
     */
    protected static final int BLOCK_SIZE = 100;
    private static final int NB_EVENTS = 10000;
    /**
     * The trace being tested
     */
    protected static TestTrace fTrace = null;
    private static EmptyTestTrace fEmptyTrace = null;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    /**
     * Setup the test
     */
    @Before
    public void setUp() {
        setupTrace(getTracePath());
    }

    /**
     * Get the trace path
     *
     * @return the trace path
     */
    protected String getTracePath() {
        return TmfTestTrace.A_TEST_10K.getFullPath();
    }

    /**
     * Tear down the test
     */
    @After
    public void tearDown() {
        fTrace.dispose();
        fTrace = null;
        fEmptyTrace.dispose();
        fEmptyTrace = null;
    }

    interface TestIndexerInterface extends ITmfTraceIndexer {
        ITmfCheckpointIndex getCheckpoints();
    }

    // ------------------------------------------------------------------------
    // Helper classes
    // ------------------------------------------------------------------------

    /**
     * A test indexer
     */
    protected static class TestIndexer extends TmfCheckpointIndexer implements TestIndexerInterface {
        /**
         * Constructs the test indexer for a normal test trace
         *
         * @param testTrace
         *            the test trace
         */
        public TestIndexer(ITmfTrace testTrace) {
            super(testTrace, BLOCK_SIZE);
        }

        @Override
        public ITmfCheckpointIndex getCheckpoints() {
            return getTraceIndex();
        }
    }

    /**
     * Create the indexer for testing
     *
     * @param trace
     *            the trace
     * @return the indexer for testing
     */
    protected TestIndexerInterface createTestIndexer(TestTrace trace) {
        return new TestIndexer(trace);
    }

    /**
     * A test trace
     */
    protected class TestTrace extends TmfTraceStub {
        /**
         *
         * @param path
         *            the path
         * @param blockSize
         *            the block size
         * @throws TmfTraceException
         *             when error occurs
         */
        public TestTrace(String path, int blockSize) throws TmfTraceException {
            super(path, blockSize, false, null, null);
            setIndexer(createTestIndexer(this));
        }

        @Override
        public TestIndexerInterface getIndexer() {
            return (TestIndexerInterface) super.getIndexer();
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

    /**
     * Creates the trace for the specified path
     *
     * @param path
     *            the path
     * @return the created trace
     * @throws URISyntaxException
     *             when error occurs
     * @throws IOException
     *             when error occurs
     * @throws TmfTraceException
     *             when error occurs
     */
    protected TestTrace createTrace(final String path) throws URISyntaxException, IOException, TmfTraceException {
        final URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(path), null);
        final File test = new File(FileLocator.toFileURL(location).toURI());
        TestTrace trace = new TestTrace(test.toURI().getPath(), BLOCK_SIZE);
        trace.indexTrace(true);
        return trace;
    }

    private synchronized void setupTrace(final String path) {
        if (fTrace == null) {
            try {
                fTrace = createTrace(path);
            } catch (final TmfTraceException e) {
                fail(e.getMessage());
            } catch (final URISyntaxException e) {
                fail(e.getMessage());
            } catch (final IOException e) {
                fail(e.getMessage());
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

    /**
     * Test the content of the index after building the full index
     */
    @Test
    public void testTmfTraceIndexing() {
        verifyIndexContent();
    }

    /**
     * Verify the content of the index
     */
    protected static void verifyIndexContent() {
        assertEquals(BLOCK_SIZE, fTrace.getCacheSize());
        assertEquals(NB_EVENTS, fTrace.getNbEvents());
        assertEquals(1, fTrace.getTimeRange().getStartTime().getValue());
        assertEquals(NB_EVENTS, fTrace.getTimeRange().getEndTime().getValue());
        assertEquals(1, fTrace.getStartTime().getValue());
        assertEquals(NB_EVENTS, fTrace.getEndTime().getValue());

        ITmfCheckpointIndex checkpoints = fTrace.getIndexer().getCheckpoints();
        int pageSize = fTrace.getCacheSize();
        assertTrue(checkpoints != null);
        assertEquals(NB_EVENTS / BLOCK_SIZE, checkpoints.size());

        // Validate that each checkpoint points to the right event
        for (int i = 0; i < checkpoints.size(); i++) {
            ITmfCheckpoint checkpoint = checkpoints.get(i);
            TmfContext context = new TmfContext(checkpoint.getLocation(), i * pageSize);
            ITmfEvent event = fTrace.parseEvent(context);
            assertEquals(context.getRank(), i * pageSize);
            assertTrue((checkpoint.getTimestamp().compareTo(event.getTimestamp(), false) == 0));
        }
    }

    /**
     * Test that a empty trace has the correct content
     */
    @Test
    public void testEmptyTmfTraceIndexing() {
        assertEquals(ITmfTrace.DEFAULT_TRACE_CACHE_SIZE, fEmptyTrace.getCacheSize());
        assertEquals(0, fEmptyTrace.getNbEvents());
        assertEquals(TmfTimestamp.BIG_BANG, fEmptyTrace.getTimeRange().getStartTime());
        assertEquals(TmfTimestamp.BIG_BANG, fEmptyTrace.getTimeRange().getEndTime());
        assertEquals(TmfTimestamp.BIG_BANG, fEmptyTrace.getStartTime());
        assertEquals(TmfTimestamp.BIG_BANG, fEmptyTrace.getEndTime());

        ITmfCheckpointIndex checkpoints = fEmptyTrace.getIndexer().getCheckpoints();
        assertTrue(checkpoints != null);
        assertEquals(0, checkpoints.size());
    }
}
