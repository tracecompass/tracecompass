/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.tests.trace.indexer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.eclipse.tracecompass.tmf.core.trace.indexer.ITmfTraceIndexer;
import org.eclipse.tracecompass.tmf.core.trace.indexer.TmfBTreeTraceIndexer;
import org.eclipse.tracecompass.tmf.core.trace.indexer.checkpoint.ITmfCheckpoint;
import org.eclipse.tracecompass.tmf.core.trace.indexer.checkpoint.ITmfCheckpointIndex;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for indexing CTF experiments.
 */
public class CtfExperimentCheckpointIndexTest {

    private static final String EXPERIMENT = "MyExperiment";
    private static final @NonNull CtfTestTrace TEST_TRACE1 = CtfTestTrace.TRACE2;
    private static final @NonNull CtfTestTrace TEST_TRACE2 = CtfTestTrace.KERNEL_VM;
    private static final int NB_EVENTS = CtfTestTrace.TRACE2.getNbEvents() + CtfTestTrace.KERNEL_VM.getNbEvents();

    private static final long START_TIME = 1331668247314038062L;
    private static final long END_TIME = 1363700770550261288L;
    private static final int BLOCK_SIZE = 100000;
    private static final int LAST_EVENT_RANK = NB_EVENTS - 1;
    private static final int LAST_CHECKPOINT_RANK = LAST_EVENT_RANK / BLOCK_SIZE;
    private static final int NB_CHECKPOINTS = LAST_CHECKPOINT_RANK + 1;

    private static ITmfTrace[] fTestTraces;
    private static TmfExperiment fExperiment;
    private static TestIndexer fIndexer;

    /**
     * Setup the test
     */
    @Before
    public void setUp() {
        deleteSupplementaryFiles();
        setUpTraces();
    }

    private static void setUpTraces() {
        fTestTraces = new ITmfTrace[2];
        fTestTraces[0] = CtfTmfTestTraceUtils.getTrace(TEST_TRACE1);
        fTestTraces[1] = CtfTmfTestTraceUtils.getTrace(TEST_TRACE2);
        fExperiment = new TmfExperiment(ITmfEvent.class, EXPERIMENT, fTestTraces, BLOCK_SIZE, null) {
            @Override
            protected ITmfTraceIndexer createIndexer(int interval) {
                fIndexer = new TestIndexer(this, interval);
                return fIndexer;
            }
        };
        fExperiment.indexTrace(true);
    }

    /**
     * Tear down the test
     */
    @After
    public void tearDown() {
        deleteSupplementaryFiles();
        disposeTraces();
    }

    private static void deleteSupplementaryFiles() {
        final String TRACE_DIRECTORY = TmfTraceManager.getTemporaryDirPath() + File.separator + EXPERIMENT;
        File supplementaryFileDir = new File(TRACE_DIRECTORY);
        if (supplementaryFileDir.exists()) {
            for (File file : supplementaryFileDir.listFiles()) {
                file.delete();
            }
        }
    }

    private static void disposeTraces() {
        fExperiment.dispose();
        fExperiment = null;
        for (ITmfTrace trace : fTestTraces) {
            trace.dispose();
        }
        fTestTraces = null;
    }

    /**
     * Test indexer to give access to checkpoints
     */
    private static class TestIndexer extends TmfBTreeTraceIndexer {

        public TestIndexer(ITmfTrace trace, int interval) {
            super(trace, interval);
        }

        public ITmfCheckpointIndex getCheckpoints() {
            return getTraceIndex();
        }
    }

    /**
     * Test the content of the index after building the full index
     */
    @Test
    public void testIndexing() {
        assertTrue(fIndexer.getCheckpoints().isCreatedFromScratch());
        verifyIndexContent();
    }

    /**
     * Test that a fully built index has the same content when reloaded from disk
     */
    @Test
    public void testReopenIndex() {
        assertTrue(fIndexer.getCheckpoints().isCreatedFromScratch());
        disposeTraces();
        setUpTraces();
        assertFalse(fIndexer.getCheckpoints().isCreatedFromScratch());
        verifyIndexContent();
    }

    private static void verifyIndexContent() {
        assertEquals("getTraceSize", NB_EVENTS, fExperiment.getNbEvents());
        assertEquals("getRange-start", START_TIME, fExperiment.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end", END_TIME, fExperiment.getTimeRange().getEndTime().getValue());
        assertEquals("getStartTime", START_TIME, fExperiment.getStartTime().getValue());
        assertEquals("getEndTime", END_TIME, fExperiment.getEndTime().getValue());

        ITmfCheckpointIndex checkpoints = fIndexer.getCheckpoints();
        assertTrue(checkpoints != null);
        assertEquals(NB_EVENTS, checkpoints.getNbEvents());
        assertEquals(NB_CHECKPOINTS, checkpoints.size());

        // Validate that each checkpoint points to the right event
        for (int i = 0; i < checkpoints.size(); i++) {
            ITmfCheckpoint checkpoint = checkpoints.get(i);
            TmfContext context = new TmfContext(checkpoint.getLocation(), i * BLOCK_SIZE);
            ITmfEvent event = fExperiment.parseEvent(context);
            assertEquals(context.getRank(), i * BLOCK_SIZE);
            assertEquals(0, (checkpoint.getTimestamp().compareTo(event.getTimestamp())));
        }

        ITmfContext context = fExperiment.seekEvent(0);
        ITmfEvent event = fExperiment.getNext(context);
        assertEquals(START_TIME, event.getTimestamp().getValue());

        context = fExperiment.seekEvent(NB_EVENTS - 1);
        event = fExperiment.getNext(context);
        assertEquals(END_TIME, event.getTimestamp().getValue());
    }
}
