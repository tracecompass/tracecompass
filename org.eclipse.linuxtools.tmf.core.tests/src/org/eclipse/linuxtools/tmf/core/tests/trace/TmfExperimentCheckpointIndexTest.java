/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Alexandre Montplaisir - Port to JUnit4
 *   Patrick Tasse - Updated for ranks in experiment location
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.trace;

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
import org.eclipse.linuxtools.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfCheckpoint;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.TmfExperimentStub;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.TmfTraceStub;
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
    private static int          BLOCK_SIZE   = 1000;

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
            URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(TEST_TRACE1.getFullPath()), null);
            File test = new File(FileLocator.toFileURL(location).toURI());
            final TmfTraceStub trace1 = new TmfTraceStub(test.getPath(), 0, true);
            fTestTraces[0] = trace1;
            location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(TEST_TRACE2.getFullPath()), null);
            test = new File(FileLocator.toFileURL(location).toURI());
            final TmfTraceStub trace2 = new TmfTraceStub(test.getPath(), 0, true);
            fTestTraces[1] = trace2;
        } catch (final TmfTraceException e) {
            e.printStackTrace();
        } catch (final URISyntaxException e) {
            e.printStackTrace();
        } catch (final IOException e) {
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

        List<ITmfCheckpoint> checkpoints = fExperiment.getIndexer().getCheckpoints();
        int pageSize = fExperiment.getCacheSize();
        assertTrue("Checkpoints exist",  checkpoints != null);
        assertEquals("Checkpoints size", NB_EVENTS / BLOCK_SIZE, checkpoints.size());

        // Validate that each checkpoint points to the right event
        for (int i = 0; i < checkpoints.size(); i++) {
            ITmfCheckpoint checkpoint = checkpoints.get(i);
            ITmfLocation location = checkpoint.getLocation();
            ITmfContext context = fExperiment.seekEvent(location);
            ITmfEvent event = fExperiment.parseEvent(context);
            assertTrue(context.getRank() == i * pageSize);
            assertTrue((checkpoint.getTimestamp().compareTo(event.getTimestamp(), false) == 0));
        }
    }

    // ------------------------------------------------------------------------
    // Streaming
    // ------------------------------------------------------------------------

    @Test
    public void testGrowingIndex() {
        ITmfTrace[] testTraces = new TmfTraceStub[2];
        try {
            URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(TEST_TRACE1.getFullPath()), null);
            File test = new File(FileLocator.toFileURL(location).toURI());
            final TmfTraceStub trace1 = new TmfTraceStub(test.getPath(), 0, false);
            testTraces[0] = trace1;
            location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(TEST_TRACE2.getFullPath()), null);
            test = new File(FileLocator.toFileURL(location).toURI());
            final TmfTraceStub trace2 = new TmfTraceStub(test.getPath(), 0, false);
            testTraces[1] = trace2;
        } catch (final TmfTraceException e) {
            e.printStackTrace();
        } catch (final URISyntaxException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }

        TmfExperimentStub experiment = new TmfExperimentStub(EXPERIMENT, testTraces, BLOCK_SIZE);
        int pageSize = experiment.getCacheSize();

        // Build the first half of the index
        TmfTimeRange range = new TmfTimeRange(new TmfTimestamp(1, -3), new TmfTimestamp(NB_EVENTS / 2 - 1, -3));
        experiment.getIndexer().buildIndex(0, range, true);

        // Validate that each checkpoint points to the right event
        List<ITmfCheckpoint> checkpoints = experiment.getIndexer().getCheckpoints();
        assertTrue("Checkpoints exist",  checkpoints != null);
        assertEquals("Checkpoints size", NB_EVENTS / BLOCK_SIZE / 2, checkpoints.size());

        // Build the second half of the index
        experiment.getIndexer().buildIndex(NB_EVENTS / 2, TmfTimeRange.ETERNITY, true);

        // Validate that each checkpoint points to the right event
        assertEquals("Checkpoints size", NB_EVENTS / BLOCK_SIZE, checkpoints.size());
        for (int i = 0; i < checkpoints.size(); i++) {
            ITmfCheckpoint checkpoint = checkpoints.get(i);
            ITmfLocation location = checkpoint.getLocation();
            ITmfContext context = experiment.seekEvent(location);
            ITmfEvent event = experiment.parseEvent(context);
            assertTrue(context.getRank() == i * pageSize);
            assertTrue((checkpoint.getTimestamp().compareTo(event.getTimestamp(), false) == 0));
            assertEquals("Checkpoint value", i * pageSize + 1, checkpoint.getTimestamp().getValue());
        }

        /* Clean up (since we didn't use the class-specific fixtures) */
        experiment.dispose();
        for (ITmfTrace trace : testTraces) {
            trace.dispose();
        }
    }

}