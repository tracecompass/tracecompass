/*******************************************************************************
 * Copyright (c) 2012, 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Alexandre Montplaisir - Port to JUnit4
 *   Patrick Tasse - Updated for ranks in experiment location
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.trace.indexer.checkpoint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.indexer.checkpoint.ITmfCheckpoint;
import org.eclipse.tracecompass.tmf.core.trace.indexer.checkpoint.ITmfCheckpointIndex;
import org.eclipse.tracecompass.tmf.core.trace.location.ITmfLocation;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfExperimentStub;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfTraceStub;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for the TmfCheckpointIndexTest class.
 */
@SuppressWarnings("javadoc")
public class TmfExperimentCheckpointIndexTest {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private static final String EXPERIMENT   = "MyExperiment";
    private static final TmfTestTrace TEST_TRACE1   = TmfTestTrace.O_TEST_10K;
    private static final TmfTestTrace TEST_TRACE2   = TmfTestTrace.E_TEST_10K;
    private static int          NB_EVENTS    = 20000;
    private static int          BLOCK_SIZE   = 2000;
    private static int          LAST_EVENT_RANK    = NB_EVENTS - 1;
    private static int          LAST_CHECKPOINT_RANK = LAST_EVENT_RANK / BLOCK_SIZE;
    private static int          NB_CHECKPOINTS    =  LAST_CHECKPOINT_RANK + 1;

    private static ITmfTrace[] fTestTraces;
    private static TmfExperimentStub fExperiment;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    @Before
    public void setUp() {
        setupTraces();
        fExperiment = new TmfExperimentStub(EXPERIMENT, fTestTraces, BLOCK_SIZE);
        fExperiment.getIndexer().buildIndex(0, TmfTimeRange.ETERNITY, true);
    }

    @After
    public void tearDown() {
        fExperiment.dispose();
        fExperiment = null;
        for (ITmfTrace trace : fTestTraces) {
            trace.dispose();
        }
        fTestTraces = null;
    }

    private static void setupTraces() {

        fTestTraces = new ITmfTrace[2];
        try {
            final TmfTraceStub trace1 = new TmfTraceStub(TEST_TRACE1.getFullPath(), 0, true, null);
            fTestTraces[0] = trace1;
            final TmfTraceStub trace2 = new TmfTraceStub(TEST_TRACE2.getFullPath(), 0, true, null);
            fTestTraces[1] = trace2;
        } catch (final TmfTraceException e) {
            e.printStackTrace();
        }
    }

    // ------------------------------------------------------------------------
    // Verify checkpoints
    // ------------------------------------------------------------------------

    @Test
    public void testTmfTraceIndexing() {
        assertEquals("getCacheSize",   BLOCK_SIZE, fExperiment.getCacheSize());
        assertEquals("getTraceSize",   NB_EVENTS,  fExperiment.getNbEvents());
        assertEquals("getRange-start", 1,          fExperiment.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end",   NB_EVENTS,  fExperiment.getTimeRange().getEndTime().getValue());
        assertEquals("getStartTime",   1,          fExperiment.getStartTime().getValue());
        assertEquals("getEndTime",     NB_EVENTS,  fExperiment.getEndTime().getValue());

        ITmfCheckpointIndex checkpoints = fExperiment.getIndexer().getCheckpoints();
        int pageSize = fExperiment.getCacheSize();
        assertTrue("Checkpoints exist",  checkpoints != null);
        assertEquals("Checkpoints size", NB_CHECKPOINTS, checkpoints.size());

        // Validate that each checkpoint points to the right event
        for (int i = 0; i < checkpoints.size(); i++) {
            ITmfCheckpoint checkpoint = checkpoints.get(i);
            ITmfLocation location = checkpoint.getLocation();
            ITmfContext context = fExperiment.seekEvent(location);
            ITmfEvent event = fExperiment.parseEvent(context);
            assertTrue(context.getRank() == i * pageSize);
            assertTrue((checkpoint.getTimestamp().compareTo(event.getTimestamp()) == 0));
        }
    }

    // ------------------------------------------------------------------------
    // Streaming
    // ------------------------------------------------------------------------

    @Test
    public void testGrowingIndex() {
        ITmfTrace[] testTraces = new TmfTraceStub[2];
        try {
            final TmfTraceStub trace1 = new TmfTraceStub(TEST_TRACE1.getFullPath(), 0, false, null);
            testTraces[0] = trace1;
            final TmfTraceStub trace2 = new TmfTraceStub(TEST_TRACE2.getFullPath(), 0, false, null);
            testTraces[1] = trace2;
        } catch (final TmfTraceException e) {
            e.printStackTrace();
        }

        TmfExperimentStub experiment = new TmfExperimentStub(EXPERIMENT, testTraces, BLOCK_SIZE);
        int pageSize = experiment.getCacheSize();

        // Build the first half of the index
        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.create(1, -3), TmfTimestamp.create(NB_EVENTS / 2 - 1, -3));
        experiment.getIndexer().buildIndex(0, range, true);

        // Validate that each checkpoint points to the right event
        ITmfCheckpointIndex checkpoints = experiment.getIndexer().getCheckpoints();
        assertTrue("Checkpoints exist",  checkpoints != null);
        assertEquals("Checkpoints size", NB_CHECKPOINTS / 2, checkpoints.size());

        // Build the second half of the index
        experiment.getIndexer().buildIndex(NB_EVENTS / 2, TmfTimeRange.ETERNITY, true);

        // Validate that each checkpoint points to the right event
        assertEquals("Checkpoints size", NB_CHECKPOINTS, checkpoints.size());
        for (int i = 0; i < checkpoints.size(); i++) {
            ITmfCheckpoint checkpoint = checkpoints.get(i);
            ITmfLocation location = checkpoint.getLocation();
            ITmfContext context = experiment.seekEvent(location);
            ITmfEvent event = experiment.parseEvent(context);
            assertTrue(context.getRank() == i * pageSize);
            assertTrue((checkpoint.getTimestamp().compareTo(event.getTimestamp()) == 0));
            assertEquals("Checkpoint value", i * pageSize + 1, checkpoint.getTimestamp().getValue());
        }

        /* Clean up (since we didn't use the class-specific fixtures) */
        experiment.dispose();
        for (ITmfTrace trace : testTraces) {
            trace.dispose();
        }
    }

}