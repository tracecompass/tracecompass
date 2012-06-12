/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.trace;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.tmf.core.trace.TmfExperimentContext;
import org.eclipse.linuxtools.internal.tmf.core.trace.TmfExperimentLocation;
import org.eclipse.linuxtools.internal.tmf.core.trace.TmfLocationArray;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.tests.TmfCoreTestPlugin;
import org.eclipse.linuxtools.tmf.core.trace.ITmfCheckpoint;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.TmfExperimentStub;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.TmfTraceStub;

/**
 * Test suite for the TmfCheckpointIndexTest class.
 */
@SuppressWarnings({ "nls" })
public class TmfExperimentCheckpointIndexTest extends TestCase {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private static final String DIRECTORY    = "testfiles";
    private static final String TEST_STREAM1 = "O-Test-10K";
    private static final String TEST_STREAM2 = "E-Test-10K";
    private static final String EXPERIMENT   = "MyExperiment";
    private static int          NB_EVENTS    = 20000;
    private static int          BLOCK_SIZE   = 1000;

    private static ITmfTrace<TmfEvent>[] fTestTraces;
    private static TmfExperimentStub<TmfEvent> fExperiment;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    public TmfExperimentCheckpointIndexTest(final String name) throws Exception {
        super(name);
    }

    @Override
    protected synchronized void setUp() throws Exception {
        super.setUp();
        if (fExperiment == null) {
            setupTrace(DIRECTORY + File.separator + TEST_STREAM1, DIRECTORY + File.separator + TEST_STREAM2);
            fExperiment = new TmfExperimentStub<TmfEvent>(EXPERIMENT, fTestTraces, BLOCK_SIZE);
            fExperiment.getIndexer().buildIndex(0, TmfTimeRange.ETERNITY, true);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        fExperiment.dispose();
        fExperiment = null;
        fTestTraces = null;
    }

    @SuppressWarnings("unchecked")
    private synchronized static ITmfTrace<?>[] setupTrace(final String path1, final String path2) {
        if (fTestTraces == null) {
            fTestTraces = new ITmfTrace[2];
            try {
                URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(path1), null);
                File test = new File(FileLocator.toFileURL(location).toURI());
                final TmfTraceStub trace1 = new TmfTraceStub(test.getPath(), 0, true);
                fTestTraces[0] = trace1;
                location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(path2), null);
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
        return fTestTraces;
    }

    // ------------------------------------------------------------------------
    // Verify checkpoints
    // ------------------------------------------------------------------------

    public void testTmfTraceIndexing() throws Exception {
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
            TmfExperimentLocation expLocation = (TmfExperimentLocation) checkpoint.getLocation();
            TmfLocationArray locations = expLocation.getLocation();
            ITmfContext[] trcContexts = new ITmfContext[2];
            trcContexts[0] = new TmfContext(locations.getLocations()[0], (i * pageSize) / 2);
            trcContexts[1] = new TmfContext(locations.getLocations()[1], (i * pageSize) / 2);
            TmfExperimentContext expContext = new TmfExperimentContext(trcContexts);
            expContext.getEvents()[0] = fTestTraces[0].getNext(fTestTraces[0].seekEvent((i * pageSize) / 2));
            expContext.getEvents()[1] = fTestTraces[1].getNext(fTestTraces[1].seekEvent((i * pageSize) / 2));
            ITmfEvent event = fExperiment.parseEvent(expContext);
            assertTrue(expContext.getRank() == i * pageSize);
            assertTrue((checkpoint.getTimestamp().compareTo(event.getTimestamp(), false) == 0));
        }
    }

    // ------------------------------------------------------------------------
    // Streaming
    // ------------------------------------------------------------------------

    public void testGrowingIndex() throws Exception {

        ITmfTrace<TmfEvent>[] testTraces = new TmfTraceStub[2];
        try {
            URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(DIRECTORY + File.separator + TEST_STREAM1), null);
            File test = new File(FileLocator.toFileURL(location).toURI());
            final TmfTraceStub trace1 = new TmfTraceStub(test.getPath(), 0, false);
            testTraces[0] = trace1;
            location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(DIRECTORY + File.separator + TEST_STREAM2), null);
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

        TmfExperimentStub<TmfEvent> experiment = new TmfExperimentStub<TmfEvent>(EXPERIMENT, testTraces, BLOCK_SIZE);
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
            TmfExperimentLocation expLocation = (TmfExperimentLocation) checkpoint.getLocation();
            TmfLocationArray locations = expLocation.getLocation();
            ITmfContext[] trcContexts = new ITmfContext[2];
            trcContexts[0] = new TmfContext(locations.getLocations()[0], (i * pageSize) / 2);
            trcContexts[1] = new TmfContext(locations.getLocations()[1], (i * pageSize) / 2);
            TmfExperimentContext expContext = new TmfExperimentContext(trcContexts);
            expContext.getEvents()[0] = testTraces[0].getNext(testTraces[0].seekEvent((i * pageSize) / 2));
            expContext.getEvents()[1] = testTraces[1].getNext(testTraces[1].seekEvent((i * pageSize) / 2));
            ITmfEvent event = experiment.parseEvent(expContext);
            assertTrue(expContext.getRank() == i * pageSize);
            assertTrue((checkpoint.getTimestamp().compareTo(event.getTimestamp(), false) == 0));
            assertEquals("Checkpoint value", i * pageSize + 1, checkpoint.getTimestamp().getValue());
        }
    }

}