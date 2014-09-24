/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.tests.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.trace.ITmfEventParser;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;
import org.eclipse.linuxtools.tmf.core.trace.indexer.TmfBTreeTraceIndexer;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.ITmfCheckpoint;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.ITmfCheckpointIndex;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Common test code for custom trace indexes
 *
 * @author Marc-Andre Laperle
 */
public abstract class AbstractCustomTraceIndexTest {

    /**
     * Time format use for event creation
     */
    protected static final String TIMESTAMP_FORMAT = "dd/MM/yyyy HH:mm:ss:SSS";
    /**
     * Block size used for the indexer
     */
    protected static final int BLOCK_SIZE = 100;
    /**
     * The total number of events in the generated trace
     */
    protected static final int NB_EVENTS = 10000;
    private TestTrace fTrace = null;

    /**
     * A common test indexer for custom trace index tests
     */
    protected static class TestIndexer extends TmfBTreeTraceIndexer {

        /**
         * Constructs a new test indexer
         *
         * @param trace the trace
         * @param interval the checkpoint interval
         */
        public TestIndexer(ITmfTrace trace, int interval) {
            super(trace, interval);
        }

        /**
         * Get the index
         *
         * @return the index
         */
        public ITmfCheckpointIndex getCheckpoints() {
            return getTraceIndex();
        }
    }

    interface TestTrace extends ITmfTrace {
        TestIndexer getIndexer();
    }

    /**
     * Setup the test
     *
     * @throws Exception when error occurs
     */
    @Before
    public void setUp() throws Exception {
        setupTrace();
    }

    private synchronized void setupTrace() throws Exception {
        File traceDirectory = new File(getTraceDirectory());
        if (traceDirectory.exists()) {
            traceDirectory.delete();
        }
        traceDirectory.mkdir();
        if (fTrace == null) {
            fTrace = createTrace();
            fTrace.indexTrace(true);
        }
    }

    /**
     * Create a test trace, varies between tests
     *
     * @return the test trace
     * @throws Exception when error occurs
     */
    abstract protected TestTrace createTrace() throws Exception;
    /**
     * Return the trace directory for the generated trace
     *
     * @return the trace directory for the generated trace
     */
    abstract protected String getTraceDirectory();

    /**
     * Tear down the test
     */
    @After
    public void tearDown() {
        String directory = TmfTraceManager.getSupplementaryFileDir(fTrace);
        try {
            fTrace.dispose();
            fTrace = null;
        } finally {
            File dir = new File(directory);
            if (dir.exists()) {
                File[] files = dir.listFiles();
                for (File file : files) {
                    file.delete();
                }
                dir.delete();
            }

            File trace = new File(getTraceDirectory());
            if (trace.exists()) {
                trace.delete();
            }
        }

    }

    /**
     * Test the content of the index after building the full index
     */
    @Test
    public void testTmfTraceIndexing() {
        verifyIndexContent();
    }

    private void verifyIndexContent() {
        assertEquals("getCacheSize", BLOCK_SIZE, fTrace.getCacheSize());
        assertEquals("getTraceSize", NB_EVENTS, fTrace.getNbEvents());
        assertEquals("getRange-start", 0, fTrace.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end", NB_EVENTS - 1, fTrace.getTimeRange().getEndTime().getValue());
        assertEquals("getStartTime", 0, fTrace.getStartTime().getValue());
        assertEquals("getEndTime", NB_EVENTS - 1, fTrace.getEndTime().getValue());

        ITmfCheckpointIndex checkpoints = fTrace.getIndexer().getCheckpoints();
        int pageSize = fTrace.getCacheSize();
        assertTrue("Checkpoints exist", checkpoints != null);
        assertEquals("Checkpoints size", NB_EVENTS / BLOCK_SIZE, checkpoints.size());

        // Validate that each checkpoint points to the right event
        for (int i = 0; i < checkpoints.size(); i++) {
            ITmfCheckpoint checkpoint = checkpoints.get(i);
            TmfContext context = new TmfContext(checkpoint.getLocation(), i * pageSize);
            ITmfEvent event = ((ITmfEventParser)fTrace).parseEvent(context);
            assertTrue(context.getRank() == i * pageSize);
            assertTrue((checkpoint.getTimestamp().compareTo(event.getTimestamp(), false) == 0));
        }
    }

    /**
     * Test that a fully built index has the same content when reloaded from disk
     *
     * @throws Exception when error occurs
     */
    @Test
    public void testReopenIndex() throws Exception {
        fTrace.dispose();
        fTrace = createTrace();
        assertFalse(fTrace.getIndexer().getCheckpoints().isCreatedFromScratch());
        fTrace.indexTrace(true);

        verifyIndexContent();
    }
}
