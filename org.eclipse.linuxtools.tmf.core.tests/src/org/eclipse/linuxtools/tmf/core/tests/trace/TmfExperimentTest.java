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
 *   Francois Chouinard - Adjusted for new Trace Model
 *   Alexandre Montplaisir - Port to JUnit4
 *   Patrick Tasse - Updated for rank in experiment location
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.tmf.core.trace.TmfExperimentContext;
import org.eclipse.linuxtools.internal.tmf.core.trace.TmfExperimentLocation;
import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisModule;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest.ExecutionType;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.statistics.ITmfStatistics;
import org.eclipse.linuxtools.tmf.core.tests.TmfCoreTestPlugin;
import org.eclipse.linuxtools.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.core.trace.location.ITmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.location.TmfLongLocation;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.TmfExperimentStub;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.TmfTraceStub;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for the TmfExperiment class (single trace).
 */
@SuppressWarnings("javadoc")
public class TmfExperimentTest {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private static final String EXPERIMENT  = "MyExperiment";
    private static int          NB_EVENTS   = 10000;
    private static int          BLOCK_SIZE  = 1000;

    private static final double DELTA = 1e-15;

    private ITmfTrace[] fTestTraces;
    private TmfExperimentStub fExperiment;

    private static byte SCALE = (byte) -3;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    private synchronized ITmfTrace[] setupTrace(final String path) {
        if (fTestTraces == null) {
            fTestTraces = new ITmfTrace[1];
            try {
                final URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(path), null);
                final File test = new File(FileLocator.toFileURL(location).toURI());
                final TmfTraceStub trace = new TmfTraceStub(test.getPath(), 0, true, null);
                fTestTraces[0] = trace;
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

    private synchronized void setupExperiment() {
        if (fExperiment == null) {
            fExperiment = new TmfExperimentStub(EXPERIMENT, fTestTraces, BLOCK_SIZE);
            fExperiment.getIndexer().buildIndex(0, TmfTimeRange.ETERNITY, true);
        }
    }

    @Before
    public void setUp() {
        setupTrace(TmfTestTrace.A_TEST_10K.getFullPath());
        setupExperiment();
    }

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    @Test
    public void testSimpleTmfExperimentConstructor() {
        TmfExperiment experiment = new TmfExperiment(ITmfEvent.class, EXPERIMENT, fTestTraces);
        assertEquals("GetId", EXPERIMENT, experiment.getName());
        assertEquals("GetCacheSize", TmfExperiment.DEFAULT_INDEX_PAGE_SIZE, experiment.getCacheSize());
        experiment.dispose();

        experiment = new TmfExperiment(ITmfEvent.class, EXPERIMENT, null);
        experiment.dispose();
    }

    @Test
    public void testNormalTmfExperimentConstructor() {
        assertEquals("GetId", EXPERIMENT, fExperiment.getName());
        assertEquals("GetNbEvents", NB_EVENTS, fExperiment.getNbEvents());

        final long nbExperimentEvents = fExperiment.getNbEvents();
        assertEquals("GetNbEvents", NB_EVENTS, nbExperimentEvents);

        final long nbTraceEvents = fExperiment.getTraces()[0].getNbEvents();
        assertEquals("GetNbEvents", NB_EVENTS, nbTraceEvents);

        final TmfTimeRange timeRange = fExperiment.getTimeRange();
        assertEquals("getStartTime", 1, timeRange.getStartTime().getValue());
        assertEquals("getEndTime", NB_EVENTS, timeRange.getEndTime().getValue());
    }

    // ------------------------------------------------------------------------
    // getTimestamp
    // ------------------------------------------------------------------------

    @Test
    public void testGetTimestamp() {
        assertEquals("getTimestamp", new TmfTimestamp(    1, (byte) -3), fExperiment.getTimestamp(   0));
        assertEquals("getTimestamp", new TmfTimestamp(    2, (byte) -3), fExperiment.getTimestamp(   1));
        assertEquals("getTimestamp", new TmfTimestamp(   11, (byte) -3), fExperiment.getTimestamp(  10));
        assertEquals("getTimestamp", new TmfTimestamp(  101, (byte) -3), fExperiment.getTimestamp( 100));
        assertEquals("getTimestamp", new TmfTimestamp( 1001, (byte) -3), fExperiment.getTimestamp(1000));
        assertEquals("getTimestamp", new TmfTimestamp( 2001, (byte) -3), fExperiment.getTimestamp(2000));
        assertEquals("getTimestamp", new TmfTimestamp( 2501, (byte) -3), fExperiment.getTimestamp(2500));
        assertEquals("getTimestamp", new TmfTimestamp(10000, (byte) -3), fExperiment.getTimestamp(9999));
        assertNull("getTimestamp", fExperiment.getTimestamp(10000));
    }

    // ------------------------------------------------------------------------
    // Bookmarks file handling
    // ------------------------------------------------------------------------

    @Test
    public void testBookmarks() {
        assertNull("GetBookmarksFile", fExperiment.getBookmarksFile());
        IFile bookmarks = (IFile) fTestTraces[0].getResource();
        fExperiment.setBookmarksFile(bookmarks);
        assertEquals("GetBookmarksFile", bookmarks, fExperiment.getBookmarksFile());
    }

    // ------------------------------------------------------------------------
    // State system, statistics and modules methods
    // ------------------------------------------------------------------------

    @Test
    public void testGetStatistics() {
        /* There should not be any experiment-specific statistics */
        ITmfStatistics stats = fExperiment.getStatistics();
        assertNull(stats);
    }

    @Test
    public void testGetAnalysisModules() {
        /* There should not be any modules at this point */
        Map<String, IAnalysisModule> modules = fExperiment.getAnalysisModules();
        assertTrue(modules.isEmpty());
    }

    // ------------------------------------------------------------------------
    // seekEvent by location
    // ------------------------------------------------------------------------

    @Test
    public void testSeekBadLocation() {
        ITmfContext context = fExperiment.seekEvent(new TmfLongLocation(0L));
        assertNull("seekEvent", context);
    }

    @Test
    public void testSeekNoTrace() {
        TmfExperiment experiment = new TmfExperiment(ITmfEvent.class, EXPERIMENT, null);
        ITmfContext context = experiment.seekEvent((TmfExperimentLocation) null);
        assertNull("seekEvent", context);
        experiment.dispose();
    }

    // ------------------------------------------------------------------------
    // seekEvent on ratio
    // ------------------------------------------------------------------------

    @Test
    public void testSeekEventOnRatio() {
        // First event
        ITmfContext context = fExperiment.seekEvent(0.0);
        assertEquals("Context rank", 0, context.getRank());
        ITmfEvent event = fExperiment.parseEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Context rank", 0, context.getRank());

        // Middle event
        int midTrace = NB_EVENTS / 2;
        context = fExperiment.seekEvent(0.5);
        assertEquals("Context rank", midTrace, context.getRank());
        event = fExperiment.parseEvent(context);
        assertEquals("Event timestamp", midTrace + 1, event.getTimestamp().getValue());
        assertEquals("Context rank", midTrace, context.getRank());

        // Last event
        context = fExperiment.seekEvent(1.0);
        assertEquals("Context rank", NB_EVENTS, context.getRank());
        event = fExperiment.parseEvent(context);
        assertNull("Event timestamp", event);
        assertEquals("Context rank", NB_EVENTS, context.getRank());

        // Beyond last event
        context = fExperiment.seekEvent(1.1);
        assertEquals("Context rank", NB_EVENTS, context.getRank());
        event = fExperiment.parseEvent(context);
        assertNull("Event timestamp", event);
        assertEquals("Context rank", NB_EVENTS, context.getRank());

        // Negative ratio
        context = fExperiment.seekEvent(-0.5);
        assertEquals("Context rank", 0, context.getRank());
        event = fExperiment.parseEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Context rank", 0, context.getRank());
    }

    @Test
    public void testGetLocationRatio() {
        // First event
        ITmfContext context = fExperiment.seekEvent((ITmfLocation) null);
        double ratio = fExperiment.getLocationRatio(context.getLocation());
        assertEquals("getLocationRatio", 0.0, ratio, DELTA);

        // Middle event
        context = fExperiment.seekEvent(NB_EVENTS / 2);
        ratio = fExperiment.getLocationRatio(context.getLocation());
        assertEquals("getLocationRatio", (double) (NB_EVENTS / 2) / NB_EVENTS, ratio, DELTA);

        // Last event
        context = fExperiment.seekEvent(NB_EVENTS - 1);
        ratio = fExperiment.getLocationRatio(context.getLocation());
        assertEquals("getLocationRatio", (double) (NB_EVENTS - 1) / NB_EVENTS, ratio, DELTA);
    }

//    @SuppressWarnings("rawtypes")
//    public void testGetCurrentLocation() {
//        ITmfContext context = fExperiment.seekEvent((ITmfLocation) null);
//        ITmfLocation location = fExperiment.getCurrentLocation();
//        assertEquals("getCurrentLocation", location, context.getLocation());
//    }

    // ------------------------------------------------------------------------
    // seekEvent on rank
    // ------------------------------------------------------------------------

    @Test
    public void testSeekRankOnCacheBoundary() {
        long cacheSize = fExperiment.getCacheSize();

        // On lower bound, returns the first event (TS = 1)
        ITmfContext context = fExperiment.seekEvent(0);
        assertEquals("Context rank", 0, context.getRank());

        ITmfEvent event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Context rank", 1, context.getRank());

        // Position trace at event rank [cacheSize]
        context = fExperiment.seekEvent(cacheSize);
        assertEquals("Context rank", cacheSize, context.getRank());

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", cacheSize + 1, event.getTimestamp().getValue());
        assertEquals("Context rank", cacheSize + 1, context.getRank());

        // Position trace at event rank [4 * cacheSize]
        context = fExperiment.seekEvent(4 * cacheSize);
        assertEquals("Context rank", 4 * cacheSize, context.getRank());

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 4 * cacheSize + 1, event.getTimestamp().getValue());
        assertEquals("Context rank", 4 * cacheSize + 1, context.getRank());
    }

    @Test
    public void testSeekRankNotOnCacheBoundary() {
        long cacheSize = fExperiment.getCacheSize();

        // Position trace at event rank 9
        ITmfContext context = fExperiment.seekEvent(9);
        assertEquals("Context rank", 9, context.getRank());

        ITmfEvent event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 10, event.getTimestamp().getValue());
        assertEquals("Context rank", 10, context.getRank());

        // Position trace at event rank [cacheSize - 1]
        context = fExperiment.seekEvent(cacheSize - 1);
        assertEquals("Context rank", cacheSize - 1, context.getRank());

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", cacheSize, event.getTimestamp().getValue());
        assertEquals("Context rank", cacheSize, context.getRank());

        // Position trace at event rank [cacheSize + 1]
        context = fExperiment.seekEvent(cacheSize + 1);
        assertEquals("Context rank", cacheSize + 1, context.getRank());

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", cacheSize + 2, event.getTimestamp().getValue());
        assertEquals("Context rank", cacheSize + 2, context.getRank());

        // Position trace at event rank 4500
        context = fExperiment.seekEvent(4500);
        assertEquals("Context rank", 4500, context.getRank());

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 4501, event.getTimestamp().getValue());
        assertEquals("Context rank", 4501, context.getRank());
    }

    @Test
    public void testSeekRankOutOfScope() {
        // Position trace at beginning
        ITmfContext context = fExperiment.seekEvent(-1);
        assertEquals("Event rank", 0, context.getRank());

        ITmfEvent event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Context rank", 1, context.getRank());

        // Position trace at event passed the end
        context = fExperiment.seekEvent(NB_EVENTS);
        assertEquals("Context rank", NB_EVENTS, context.getRank());

        event = fExperiment.getNext(context);
        assertNull("Event", event);
        assertEquals("Context rank", NB_EVENTS, context.getRank());
    }

    // ------------------------------------------------------------------------
    // seekEvent on timestamp
    // ------------------------------------------------------------------------

    @Test
    public void testSeekTimestampOnCacheBoundary() {
        long cacheSize = fExperiment.getCacheSize();

        // Position trace at event rank 0
        ITmfContext context = fExperiment.seekEvent(new TmfTimestamp(1, SCALE, 0));
        assertEquals("Context rank", 0, context.getRank());

        ITmfEvent event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Context rank", 1, context.getRank());

        // Position trace at event rank [cacheSize]
        context = fExperiment.seekEvent(new TmfTimestamp(cacheSize + 1, SCALE, 0));
        assertEquals("Event rank", cacheSize, context.getRank());

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", cacheSize + 1, event.getTimestamp().getValue());
        assertEquals("Context rank", cacheSize + 1, context.getRank());

        // Position trace at event rank [4 * cacheSize]
        context = fExperiment.seekEvent(new TmfTimestamp(4 * cacheSize + 1, SCALE, 0));
        assertEquals("Context rank", 4 * cacheSize, context.getRank());

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 4 * cacheSize + 1, event.getTimestamp().getValue());
        assertEquals("Context rank", 4 * cacheSize + 1, context.getRank());
    }

    @Test
    public void testSeekTimestampNotOnCacheBoundary() {
        // Position trace at event rank 1 (TS = 2)
        ITmfContext context = fExperiment.seekEvent(new TmfTimestamp(2, SCALE, 0));
        assertEquals("Context rank", 1, context.getRank());

        ITmfEvent event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 2, event.getTimestamp().getValue());
        assertEquals("Context rank", 2, context.getRank());

        // Position trace at event rank 9 (TS = 10)
        context = fExperiment.seekEvent(new TmfTimestamp(10, SCALE, 0));
        assertEquals("Context rank", 9, context.getRank());

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 10, event.getTimestamp().getValue());
        assertEquals("Context rank", 10, context.getRank());

        // Position trace at event rank 999 (TS = 1000)
        context = fExperiment.seekEvent(new TmfTimestamp(1000, SCALE, 0));
        assertEquals("Context rank", 999, context.getRank());

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 1000, event.getTimestamp().getValue());
        assertEquals("Context rank", 1000, context.getRank());

        // Position trace at event rank 1001 (TS = 1002)
        context = fExperiment.seekEvent(new TmfTimestamp(1002, SCALE, 0));
        assertEquals("Context rank", 1001, context.getRank());

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 1002, event.getTimestamp().getValue());
        assertEquals("Context rank", 1002, context.getRank());

        // Position trace at event rank 4500 (TS = 4501)
        context = fExperiment.seekEvent(new TmfTimestamp(4501, SCALE, 0));
        assertEquals("Context rank", 4500, context.getRank());

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 4501, event.getTimestamp().getValue());
        assertEquals("Context rank", 4501, context.getRank());
    }

    @Test
    public void testSeekTimestampOutOfScope() {
        // Position trace at beginning
        ITmfContext context = fExperiment.seekEvent(new TmfTimestamp(-1, SCALE, 0));
        assertEquals("Event rank", 0, context.getRank());

        ITmfEvent event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 1, context.getRank());

        // Position trace at event passed the end
        context = fExperiment.seekEvent(new TmfTimestamp(NB_EVENTS + 1, SCALE, 0));
        event = fExperiment.getNext(context);
        assertNull("Event location", event);
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());
    }

    // ------------------------------------------------------------------------
    // seekEvent by location (context rank is undefined)
    // ------------------------------------------------------------------------

    @Test
    public void testSeekLocationOnCacheBoundary() {
        long cacheSize = fExperiment.getCacheSize();

        // Position trace at event rank 0
        ITmfContext tmpContext = fExperiment.seekEvent(0);
        ITmfContext context = fExperiment.seekEvent(tmpContext.getLocation());

        ITmfEvent event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 2, event.getTimestamp().getValue());

        // Position trace at event rank 'cacheSize'
        tmpContext = fExperiment.seekEvent(cacheSize);
        context = fExperiment.seekEvent(tmpContext.getLocation());

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", cacheSize + 1, event.getTimestamp().getValue());

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", cacheSize + 2, event.getTimestamp().getValue());

        // Position trace at event rank 4 * 'cacheSize'
        tmpContext = fExperiment.seekEvent(4 * cacheSize);
        context = fExperiment.seekEvent(tmpContext.getLocation());

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 4 * cacheSize + 1, event.getTimestamp().getValue());

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 4 * cacheSize + 2, event.getTimestamp().getValue());
    }

    @Test
    public void testSeekLocationNotOnCacheBoundary() {
        long cacheSize = fExperiment.getCacheSize();

        // Position trace at event 'cacheSize' - 1
        ITmfContext tmpContext = fExperiment.seekEvent(cacheSize - 1);
        ITmfContext context = fExperiment.seekEvent(tmpContext.getLocation());

        ITmfEvent event = fExperiment.getNext(context);
        assertEquals("Event timestamp", cacheSize, event.getTimestamp().getValue());

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", cacheSize + 1, event.getTimestamp().getValue());

        // Position trace at event rank 2 * 'cacheSize' - 1
        tmpContext = fExperiment.seekEvent(2 * cacheSize - 1);
        context = fExperiment.seekEvent(tmpContext.getLocation());
        context = fExperiment.seekEvent(2 * cacheSize - 1);

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 2 * cacheSize, event.getTimestamp().getValue());

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 2 * cacheSize + 1, event.getTimestamp().getValue());

        // Position trace at event rank 4500
        tmpContext = fExperiment.seekEvent(4500);
        context = fExperiment.seekEvent(tmpContext.getLocation());

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 4501, event.getTimestamp().getValue());

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 4502, event.getTimestamp().getValue());
    }

    @Test
    public void testSeekLocationOutOfScope() {
        // Position trace at beginning
        ITmfContext context = fExperiment.seekEvent((ITmfLocation) null);

        ITmfEvent event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
    }

    // ------------------------------------------------------------------------
    // getNext - updates the context
    // ------------------------------------------------------------------------

    private static void validateContextRanks(ITmfContext context) {
        assertTrue("Experiment context type", context instanceof TmfExperimentContext);
        TmfExperimentContext ctx = (TmfExperimentContext) context;

        int nbTraces = ctx.getContexts().length;

        // expRank = sum(trace ranks) - nbTraces + 1 (if lastTraceRead != NO_TRACE)
        long expRank = -nbTraces + ((ctx.getLastTrace() != TmfExperimentContext.NO_TRACE) ? 1 : 0);
        for (int i = 0; i < nbTraces; i++) {
            long rank = ctx.getContexts()[i].getRank();
            if (rank == -1) {
                expRank = -1;
                break;
            }
            expRank += rank;
        }
        assertEquals("Experiment context rank", expRank, ctx.getRank());
    }

    @Test
    public void testGetNextAfteSeekingOnTS_1() {

        final long INITIAL_TS = 1;
        final int NB_READS = 20;

        // On lower bound, returns the first event (ts = 1)
        final ITmfContext context = fExperiment.seekEvent(new TmfTimestamp(INITIAL_TS, SCALE, 0));

        validateContextRanks(context);

        // Read NB_EVENTS
        ITmfEvent event;
        for (int i = 0; i < NB_READS; i++) {
            event = fExperiment.getNext(context);
            assertEquals("Event timestamp", INITIAL_TS + i, event.getTimestamp().getValue());
            assertEquals("Event rank", INITIAL_TS + i, context.getRank());
        }

        // Make sure we stay positioned
        event = fExperiment.parseEvent(context);
        assertEquals("Event timestamp", INITIAL_TS + NB_READS, event.getTimestamp().getValue());
        assertEquals("Event rank", INITIAL_TS + NB_READS - 1, context.getRank());

        validateContextRanks(context);
    }

    @Test
    public void testGetNextAfteSeekingOnTS_2() {
        final long INITIAL_TS = 2;
        final int NB_READS = 20;

        // On lower bound, returns the first event (ts = 2)
        final ITmfContext context = fExperiment.seekEvent(new TmfTimestamp(INITIAL_TS, SCALE, 0));

        validateContextRanks(context);

        // Read NB_EVENTS
        ITmfEvent event;
        for (int i = 0; i < NB_READS; i++) {
            event = fExperiment.getNext(context);
            assertEquals("Event timestamp", INITIAL_TS + i, event.getTimestamp().getValue());
            assertEquals("Event rank", INITIAL_TS + i, context.getRank());
        }

        // Make sure we stay positioned
        event = fExperiment.parseEvent(context);
        assertEquals("Event timestamp", INITIAL_TS + NB_READS, event.getTimestamp().getValue());
        assertEquals("Event rank", INITIAL_TS + NB_READS - 1, context.getRank());

        validateContextRanks(context);
    }

    @Test
    public void testGetNextAfteSeekingOnTS_3() {

        final long INITIAL_TS = 500;
        final int NB_READS = 20;

        // On lower bound, returns the first event (ts = 500)
        final ITmfContext context = fExperiment.seekEvent(new TmfTimestamp(INITIAL_TS, SCALE, 0));

        validateContextRanks(context);

        // Read NB_EVENTS
        ITmfEvent event;
        for (int i = 0; i < NB_READS; i++) {
            event = fExperiment.getNext(context);
            assertEquals("Event timestamp", INITIAL_TS + i, event.getTimestamp().getValue());
            assertEquals("Event rank", INITIAL_TS + i, context.getRank());
        }

        // Make sure we stay positioned
        event = fExperiment.parseEvent(context);
        assertEquals("Event timestamp", INITIAL_TS + NB_READS, event.getTimestamp().getValue());
        assertEquals("Event rank", INITIAL_TS + NB_READS - 1, context.getRank());

        validateContextRanks(context);
    }

    @Test
    public void testGetNextAfterSeekingOnRank_1() {
        final long INITIAL_RANK = 0L;
        final int NB_READS = 20;

        // On lower bound, returns the first event (rank = 0)
        final ITmfContext context = fExperiment.seekEvent(INITIAL_RANK);

        validateContextRanks(context);

        // Read NB_EVENTS
        ITmfEvent event;
        for (int i = 0; i < NB_READS; i++) {
            event = fExperiment.getNext(context);
            assertEquals("Event timestamp", INITIAL_RANK + i + 1, event.getTimestamp().getValue());
            assertEquals("Event rank", INITIAL_RANK + i + 1, context.getRank());
        }

        // Make sure we stay positioned
        event = fExperiment.parseEvent(context);
        assertEquals("Event timestamp", INITIAL_RANK + NB_READS + 1, event.getTimestamp().getValue());
        assertEquals("Event rank", INITIAL_RANK + NB_READS, context.getRank());

        validateContextRanks(context);
    }

    @Test
    public void testGetNextAfterSeekingOnRank_2() {
        final long INITIAL_RANK = 1L;
        final int NB_READS = 20;

        // On lower bound, returns the first event (rank = 0)
        final ITmfContext context = fExperiment.seekEvent(INITIAL_RANK);

        validateContextRanks(context);

        // Read NB_EVENTS
        ITmfEvent event;
        for (int i = 0; i < NB_READS; i++) {
            event = fExperiment.getNext(context);
            assertEquals("Event timestamp", INITIAL_RANK + i + 1, event.getTimestamp().getValue());
            assertEquals("Event rank", INITIAL_RANK + i + 1, context.getRank());
        }

        // Make sure we stay positioned
        event = fExperiment.parseEvent(context);
        assertEquals("Event timestamp", INITIAL_RANK + NB_READS + 1, event.getTimestamp().getValue());
        assertEquals("Event rank", INITIAL_RANK + NB_READS, context.getRank());

        validateContextRanks(context);
    }

    @Test
    public void testGetNextAfterSeekingOnRank_3() {
        final long INITIAL_RANK = 500L;
        final int NB_READS = 20;

        // On lower bound, returns the first event (rank = 0)
        final ITmfContext context = fExperiment.seekEvent(INITIAL_RANK);

        validateContextRanks(context);

        // Read NB_EVENTS
        ITmfEvent event;
        for (int i = 0; i < NB_READS; i++) {
            event = fExperiment.getNext(context);
            assertEquals("Event timestamp", INITIAL_RANK + i + 1, event.getTimestamp().getValue());
            assertEquals("Event rank", INITIAL_RANK + i + 1, context.getRank());
        }

        // Make sure we stay positioned
        event = fExperiment.parseEvent(context);
        assertEquals("Event timestamp", INITIAL_RANK + NB_READS + 1, event.getTimestamp().getValue());
        assertEquals("Event rank", INITIAL_RANK + NB_READS, context.getRank());

        validateContextRanks(context);
    }

    @Test
    public void testGetNextAfterSeekingOnLocation_1() {
        final ITmfLocation INITIAL_LOC = null;
        final long INITIAL_TS = 1;
        final int NB_READS = 20;

        // On lower bound, returns the first event (ts = 1)
        final ITmfContext context = fExperiment.seekEvent(INITIAL_LOC);

        validateContextRanks(context);

        // Read NB_EVENTS
        ITmfEvent event;
        for (int i = 0; i < NB_READS; i++) {
            event = fExperiment.getNext(context);
            assertEquals("Event timestamp", INITIAL_TS + i, event.getTimestamp().getValue());
            assertEquals("Event rank", INITIAL_TS + i, context.getRank());
        }

        // Make sure we stay positioned
        event = fExperiment.parseEvent(context);
        assertEquals("Event timestamp", INITIAL_TS + NB_READS, event.getTimestamp().getValue());
        assertEquals("Event rank", INITIAL_TS + NB_READS - 1, context.getRank());

        validateContextRanks(context);
    }

    @Test
    public void testGetNextAfterSeekingOnLocation_2() {
        final ITmfLocation INITIAL_LOC = fExperiment.seekEvent(1L).getLocation();
        final long INITIAL_TS = 2;
        final int NB_READS = 20;

        // On lower bound, returns the first event (ts = 2)
        final ITmfContext context = fExperiment.seekEvent(INITIAL_LOC);

        validateContextRanks(context);

        // Read NB_EVENTS
        ITmfEvent event;
        for (int i = 0; i < NB_READS; i++) {
            event = fExperiment.getNext(context);
            assertEquals("Event timestamp", INITIAL_TS + i, event.getTimestamp().getValue());
        }

        // Make sure we stay positioned
        event = fExperiment.parseEvent(context);
        assertEquals("Event timestamp", INITIAL_TS + NB_READS, event.getTimestamp().getValue());

        validateContextRanks(context);
    }

    @Test
    public void testGetNextAfterSeekingOnLocation_3() {
        final ITmfLocation INITIAL_LOC = fExperiment.seekEvent(500L).getLocation();
        final long INITIAL_TS = 501;
        final int NB_READS = 20;

        // On lower bound, returns the first event (ts = 501)
        final ITmfContext context = fExperiment.seekEvent(INITIAL_LOC);

        validateContextRanks(context);

        // Read NB_EVENTS
        ITmfEvent event;
        for (int i = 0; i < NB_READS; i++) {
            event = fExperiment.getNext(context);
            assertEquals("Event timestamp", INITIAL_TS + i, event.getTimestamp().getValue());
        }

        // Make sure we stay positioned
        event = fExperiment.parseEvent(context);
        assertEquals("Event timestamp", INITIAL_TS + NB_READS, event.getTimestamp().getValue());

        validateContextRanks(context);
    }

    @Test
    public void testGetNextLocation() {
        ITmfContext context1 = fExperiment.seekEvent(0);
        fExperiment.getNext(context1);
        ITmfLocation location = context1.getLocation();
        ITmfEvent event1 = fExperiment.getNext(context1);
        ITmfContext context2 = fExperiment.seekEvent(location);
        ITmfEvent event2 = fExperiment.getNext(context2);
        assertEquals("Event timestamp", event1.getTimestamp().getValue(), event2.getTimestamp().getValue());
    }

    @Test
    public void testGetNextEndLocation() {
        ITmfContext context1 = fExperiment.seekEvent(fExperiment.getNbEvents() - 1);
        fExperiment.getNext(context1);
        ITmfLocation location = context1.getLocation();
        ITmfContext context2 = fExperiment.seekEvent(location);
        ITmfEvent event = fExperiment.getNext(context2);
        assertNull("Event", event);
    }

    // ------------------------------------------------------------------------
    // processRequest
    // ------------------------------------------------------------------------

    @Test
    public void testProcessRequestForNbEvents() throws InterruptedException {
        final int nbEvents  = 1000;
        final Vector<ITmfEvent> requestedEvents = new Vector<ITmfEvent>();

        final TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BIG_BANG, TmfTimestamp.BIG_CRUNCH);
        final TmfEventRequest request = new TmfEventRequest(ITmfEvent.class,
                range, 0, nbEvents, ExecutionType.FOREGROUND) {
            @Override
            public void handleData(final ITmfEvent event) {
                super.handleData(event);
                requestedEvents.add(event);
            }
        };
        fExperiment.sendRequest(request);
        request.waitForCompletion();

        assertEquals("nbEvents", nbEvents, requestedEvents.size());
        assertTrue("isCompleted",  request.isCompleted());
        assertFalse("isCancelled", request.isCancelled());

        // Ensure that we have distinct events.
        // Don't go overboard: we are not validating the stub!
        for (int i = 0; i < nbEvents; i++) {
            assertEquals("Distinct events", i+1, requestedEvents.get(i).getTimestamp().getValue());
        }
    }

    @Test
    public void testProcessRequestForAllEvents() throws InterruptedException {
        final int nbEvents  = TmfEventRequest.ALL_DATA;
        final Vector<ITmfEvent> requestedEvents = new Vector<ITmfEvent>();
        final long nbExpectedEvents = NB_EVENTS;

        final TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BIG_BANG, TmfTimestamp.BIG_CRUNCH);
        final TmfEventRequest request = new TmfEventRequest(ITmfEvent.class,
                range, 0, nbEvents, ExecutionType.FOREGROUND) {
            @Override
            public void handleData(final ITmfEvent event) {
                super.handleData(event);
                requestedEvents.add(event);
            }
        };
        fExperiment.sendRequest(request);
        request.waitForCompletion();

        assertEquals("nbEvents", nbExpectedEvents, requestedEvents.size());
        assertTrue("isCompleted",  request.isCompleted());
        assertFalse("isCancelled", request.isCancelled());

        // Ensure that we have distinct events.
        // Don't go overboard: we are not validating the stub!
        for (int i = 0; i < nbExpectedEvents; i++) {
            assertEquals("Distinct events", i+1, requestedEvents.get(i).getTimestamp().getValue());
        }
    }

    // ------------------------------------------------------------------------
    // cancel
    // ------------------------------------------------------------------------

    @Test
    public void testCancel() throws InterruptedException {
        final int nbEvents = NB_EVENTS;
        final int limit = BLOCK_SIZE;
        final Vector<ITmfEvent> requestedEvents = new Vector<ITmfEvent>();

        final TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BIG_BANG, TmfTimestamp.BIG_CRUNCH);
        final TmfEventRequest request = new TmfEventRequest(ITmfEvent.class,
                range, 0, nbEvents, ExecutionType.FOREGROUND) {
            int nbRead = 0;

            @Override
            public void handleData(final ITmfEvent event) {
                super.handleData(event);
                requestedEvents.add(event);
                if (++nbRead == limit) {
                    cancel();
                }
            }

            @Override
            public void handleCancel() {
                if (requestedEvents.size() < limit) {
                    System.out.println("aie");
                }
            }
        };
        fExperiment.sendRequest(request);
        request.waitForCompletion();

        assertEquals("nbEvents",  limit, requestedEvents.size());
        assertTrue("isCompleted", request.isCompleted());
        assertTrue("isCancelled", request.isCancelled());
    }

}
