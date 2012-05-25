/*******************************************************************************
 * Copyright (c) 2009, 2010, 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Adjusted for new Trace Model
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.trace;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Vector;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.tmf.core.trace.TmfExperimentLocation;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.tests.TmfCoreTestPlugin;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.core.trace.TmfLocation;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.TmfExperimentStub;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.TmfTraceStub;

/**
 * Test suite for the TmfExperiment class (single trace).
 */
@SuppressWarnings({ "nls", "restriction" })
public class TmfExperimentTest extends TestCase {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private static final String DIRECTORY   = "testfiles";
    private static final String TEST_STREAM = "A-Test-10K";
    private static final String EXPERIMENT  = "MyExperiment";
    private static int          NB_EVENTS   = 10000;
    private static int          BLOCK_SIZE  = 1000;

    private ITmfTrace<TmfEvent>[] fTestTraces;
    private TmfExperimentStub<ITmfEvent> fExperiment;

    private static byte SCALE = (byte) -3;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private synchronized ITmfTrace<?>[] setupTrace(final String path) {
        if (fTestTraces == null) {
            fTestTraces = new ITmfTrace[1];
            try {
                final URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(path), null);
                final File test = new File(FileLocator.toFileURL(location).toURI());
                final TmfTraceStub trace = new TmfTraceStub(test.getPath(), 0, true);
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
            fExperiment = new TmfExperimentStub<ITmfEvent>(EXPERIMENT, fTestTraces, BLOCK_SIZE);
            fExperiment.getIndexer().buildIndex(0, TmfTimeRange.ETERNITY, true);
        }
    }

    public TmfExperimentTest(final String name) throws Exception {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setupTrace(DIRECTORY + File.separator + TEST_STREAM);
        setupExperiment();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    public void testSimpleTmfExperimentConstructor() {

        TmfExperiment<TmfEvent> experiment = new TmfExperiment<TmfEvent>(TmfEvent.class, EXPERIMENT, fTestTraces);
        assertEquals("GetId", EXPERIMENT, experiment.getName());
        assertEquals("GetCacheSize", TmfExperiment.DEFAULT_INDEX_PAGE_SIZE, experiment.getCacheSize());
        experiment.dispose();

        experiment = new TmfExperiment<TmfEvent>(TmfEvent.class, EXPERIMENT, null);
        experiment.dispose();
    }

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

    @SuppressWarnings("static-access")
    public void testSetCurrentExperiment() {

        TmfExperiment<TmfEvent> experiment = new TmfExperiment<TmfEvent>(TmfEvent.class, EXPERIMENT, fTestTraces);
        experiment.setCurrentExperiment(experiment);
        assertEquals("getCurrentExperiment", experiment, experiment.getCurrentExperiment());

        TmfExperiment<TmfEvent> experiment2 = new TmfExperiment<TmfEvent>(TmfEvent.class, EXPERIMENT, null);
        experiment.setCurrentExperiment(experiment2);
        assertEquals("getCurrentExperiment", experiment2, experiment.getCurrentExperiment());

        experiment.dispose();
        experiment2.dispose();
    }

    // ------------------------------------------------------------------------
    // getTimestamp
    // ------------------------------------------------------------------------

    public void testGetTimestamp() throws Exception {
        assertTrue("getTimestamp", fExperiment.getTimestamp(    0).equals(new TmfTimestamp(   1, (byte) -3)));
        assertTrue("getTimestamp", fExperiment.getTimestamp(   10).equals(new TmfTimestamp(  11, (byte) -3)));
        assertTrue("getTimestamp", fExperiment.getTimestamp(  100).equals(new TmfTimestamp( 101, (byte) -3)));
        assertTrue("getTimestamp", fExperiment.getTimestamp( 1000).equals(new TmfTimestamp(1001, (byte) -3)));
        assertTrue("getTimestamp", fExperiment.getTimestamp( 2000).equals(new TmfTimestamp(2001, (byte) -3)));
        assertTrue("getTimestamp", fExperiment.getTimestamp( 2500).equals(new TmfTimestamp(2501, (byte) -3)));
        assertNull("getTimestamp", fExperiment.getTimestamp(10000));
    }

    // ------------------------------------------------------------------------
    // Bookmarks file handling
    // ------------------------------------------------------------------------

    public void testBookmarks() throws Exception {
        assertNull("GetBookmarksFile", fExperiment.getBookmarksFile());
        IFile bookmarks = (IFile) fTestTraces[0].getResource();
        fExperiment.setBookmarksFile(bookmarks);
        assertEquals("GetBookmarksFile", bookmarks, fExperiment.getBookmarksFile());
    }

    // ------------------------------------------------------------------------
    // seekEvent by location
    // ------------------------------------------------------------------------

    public void testSeekBadLocation() throws Exception {
        ITmfContext context = fExperiment.seekEvent((ITmfLocation<?>) new TmfLocation<Long>(0L));
        assertNull("seekEvent", context);
    }

    public void testSeekNoTrace() throws Exception {
        TmfExperiment<TmfEvent> experiment = new TmfExperiment<TmfEvent>(TmfEvent.class, EXPERIMENT, null);
        ITmfContext context = experiment.seekEvent((TmfExperimentLocation) null);
        assertNull("seekEvent", context);
        experiment.dispose();
    }

    // ------------------------------------------------------------------------
    // seekEvent on ratio
    // ------------------------------------------------------------------------

    public void testSeekEventOnRatio() throws Exception {

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

    @SuppressWarnings("rawtypes")
    public void testGetLocationRatio() throws Exception {

        // First event
        ITmfContext context = fExperiment.seekEvent((ITmfLocation) null);
        double ratio = fExperiment.getLocationRatio(context.getLocation());
        context = fExperiment.seekEvent(ratio);
        double ratio2 = fExperiment.getLocationRatio(context.getLocation());
        assertEquals("getLocationRatio", ratio, ratio2);

        // Middle event
        context = fExperiment.seekEvent(NB_EVENTS / 2);
        ratio = fExperiment.getLocationRatio(context.getLocation());
        context = fExperiment.seekEvent(ratio);
        ratio2 = fExperiment.getLocationRatio(context.getLocation());
        assertEquals("getLocationRatio", ratio, ratio2);

        // Last event
        context = fExperiment.seekEvent(NB_EVENTS - 1);
        ratio = fExperiment.getLocationRatio(context.getLocation());
        context = fExperiment.seekEvent(ratio);
        ratio2 = fExperiment.getLocationRatio(context.getLocation());
        assertEquals("getLocationRatio", ratio, ratio2);
    }

//    @SuppressWarnings({ "unchecked", "rawtypes" })
//    public void testGetCurrentLocation() throws Exception {
//        ITmfContext context = fExperiment.seekEvent((ITmfLocation) null);
//        ITmfLocation location = fExperiment.getCurrentLocation();
//        assertEquals("getCurrentLocation", location, context.getLocation());
//    }

    // ------------------------------------------------------------------------
    // seekEvent on rank
    // ------------------------------------------------------------------------

    public void testSeekRankOnCacheBoundary() throws Exception {

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

    public void testSeekRankNotOnCacheBoundary() throws Exception {

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

    public void testSeekRankOutOfScope() throws Exception {

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

    public void testSeekTimestampOnCacheBoundary() throws Exception {

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

    public void testSeekTimestampNotOnCacheBoundary() throws Exception {

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

    public void testSeekTimestampOutOfScope() throws Exception {

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

    public void testSeekLocationOnCacheBoundary() throws Exception {
        
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

    public void testSeekLocationNotOnCacheBoundary() throws Exception {

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

    public void testSeekLocationOutOfScope() throws Exception {

        // Position trace at beginning
        ITmfContext context = fExperiment.seekEvent((ITmfLocation<?>) null);

        ITmfEvent event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
    }

    // ------------------------------------------------------------------------
    // readtNextEvent - updates the context
    // ------------------------------------------------------------------------

    public void testReadNextEvent() throws Exception {

        // On lower bound, returns the first event (ts = 0)
        final ITmfContext context = fExperiment.seekEvent(0);
        ITmfEvent event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());

        for (int i = 2; i < 20; i++) {
            event = fExperiment.getNext(context);
            assertEquals("Event timestamp", i, event.getTimestamp().getValue());
        }
    }

    // ------------------------------------------------------------------------
    // processRequest
    // ------------------------------------------------------------------------

    public void testProcessRequestForNbEvents() throws Exception {

        final int blockSize = 100;
        final int nbEvents  = 1000;
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();

        final TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BIG_BANG, TmfTimestamp.BIG_CRUNCH);
        final TmfEventRequest<TmfEvent> request = new TmfEventRequest<TmfEvent>(TmfEvent.class, range, nbEvents, blockSize) {
            @Override
            public void handleData(final TmfEvent event) {
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

    public void testProcessRequestForNbEvents2() throws Exception {

        final int blockSize = 2 * NB_EVENTS;
        final int nbEvents = 1000;
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();

        final TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BIG_BANG, TmfTimestamp.BIG_CRUNCH);
        final TmfEventRequest<TmfEvent> request = new TmfEventRequest<TmfEvent>(TmfEvent.class, range, nbEvents, blockSize) {
            @Override
            public void handleData(final TmfEvent event) {
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

    public void testProcessRequestForAllEvents() throws Exception {

        final int nbEvents  = TmfEventRequest.ALL_DATA;
        final int blockSize =  1;
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();
        final long nbExpectedEvents = NB_EVENTS;

        final TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BIG_BANG, TmfTimestamp.BIG_CRUNCH);
        final TmfEventRequest<TmfEvent> request = new TmfEventRequest<TmfEvent>(TmfEvent.class, range, nbEvents, blockSize) {
            @Override
            public void handleData(final TmfEvent event) {
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

    public void testCancel() throws Exception {

        final int nbEvents  = NB_EVENTS;
        final int blockSize = BLOCK_SIZE;
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();

        final TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BIG_BANG, TmfTimestamp.BIG_CRUNCH);
        final TmfEventRequest<TmfEvent> request = new TmfEventRequest<TmfEvent>(TmfEvent.class, range, nbEvents, blockSize) {
            int nbRead = 0;
            @Override
            public void handleData(final TmfEvent event) {
                super.handleData(event);
                requestedEvents.add(event);
                if (++nbRead == blockSize) {
                    cancel();
                }
            }
            @Override
            public void handleCancel() {
                if (requestedEvents.size() < blockSize) {
                    System.out.println("aie");
                }
            }
        };
        fExperiment.sendRequest(request);
        request.waitForCompletion();

        assertEquals("nbEvents",  blockSize, requestedEvents.size());
        assertTrue("isCompleted", request.isCompleted());
        assertTrue("isCancelled", request.isCancelled());
    }

}