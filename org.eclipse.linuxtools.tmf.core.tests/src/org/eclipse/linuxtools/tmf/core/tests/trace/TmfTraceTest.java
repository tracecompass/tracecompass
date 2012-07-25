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
 *   Francois Chouinard - Adapted for TMF Trace Model 1.0
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.trace;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Vector;

import junit.framework.TestCase;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.tmf.core.component.TmfProviderManager;
import org.eclipse.linuxtools.tmf.core.component.ITmfDataProvider;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.tests.TmfCoreTestPlugin;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfCheckpointIndexer;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.TmfTraceStub;

/**
 * Test suite for the TmfTrace class.
 */
@SuppressWarnings({"nls","javadoc"})
public class TmfTraceTest extends TestCase {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private static final String DIRECTORY   = "testfiles";
    private static final String TEST_STREAM = "A-Test-10K";
    private static final int    BLOCK_SIZE  = 500;
    private static final int    NB_EVENTS   = 10000;
    private static TmfTraceStub fTrace      = null;

    private static int SCALE = -3;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    /**
     * @param name the test name
     */
    public TmfTraceTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        fTrace = setupTrace(DIRECTORY + File.separator + TEST_STREAM);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        fTrace.dispose();
        fTrace = null;
    }

    // ------------------------------------------------------------------------
    // Helper functions
    // ------------------------------------------------------------------------

    private static TmfTraceStub setupTrace(final String path) {
        if (fTrace == null) {
            try {
                final URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(path), null);
                final File test = new File(FileLocator.toFileURL(location).toURI());
                fTrace = new TmfTraceStub(test.toURI().getPath(), BLOCK_SIZE);
                fTrace.indexTrace();
            } catch (final TmfTraceException e) {
                e.printStackTrace();
            } catch (final URISyntaxException e) {
                e.printStackTrace();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        return fTrace;
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    @SuppressWarnings("null")
    public void testStandardConstructor() throws TmfTraceException {
        TmfTraceStub trace = null;
        File testfile = null;
        try {
            final URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(DIRECTORY + File.separator + TEST_STREAM), null);
            testfile = new File(FileLocator.toFileURL(location).toURI());
            trace = new TmfTraceStub(testfile.toURI().getPath());
            trace.indexTrace();
        } catch (final URISyntaxException e) {
            fail("URISyntaxException");
        } catch (final IOException e) {
            fail("IOException");
        }

        assertFalse ("Open trace", trace == null);
        assertEquals("getType",  TmfEvent.class, trace.getType());
        assertNull  ("getResource", trace.getResource());
        assertEquals("getPath", testfile.toURI().getPath(), trace.getPath());
        assertEquals("getCacheSize", ITmfTrace.DEFAULT_TRACE_CACHE_SIZE, trace.getCacheSize());
        assertEquals("getStreamingInterval", 0, trace.getStreamingInterval());
        assertEquals("getName", TEST_STREAM, trace.getName());

        assertEquals("getNbEvents",    NB_EVENTS, trace.getNbEvents());
        assertEquals("getRange-start", 1,         trace.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end",   NB_EVENTS, trace.getTimeRange().getEndTime().getValue());
        assertEquals("getStartTime",   1,         trace.getStartTime().getValue());
        assertEquals("getEndTime",     NB_EVENTS, trace.getEndTime().getValue());
    }

    @SuppressWarnings("null")
    public void testStandardConstructorCacheSize() throws TmfTraceException {
        TmfTraceStub trace = null;
        File testfile = null;
        try {
            final URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(DIRECTORY + File.separator + TEST_STREAM), null);
            testfile = new File(FileLocator.toFileURL(location).toURI());
            trace = new TmfTraceStub(testfile.toURI().getPath(), 0);
            trace.indexTrace();
        } catch (final URISyntaxException e) {
            fail("URISyntaxException");
        } catch (final IOException e) {
            fail("IOException");
        }

        assertFalse ("Open trace", trace == null);
        assertEquals("getType",  TmfEvent.class, trace.getType());
        assertNull  ("getResource", trace.getResource());
        assertEquals("getPath", testfile.toURI().getPath(), trace.getPath());
        assertEquals("getCacheSize", ITmfTrace.DEFAULT_TRACE_CACHE_SIZE, trace.getCacheSize());
        assertEquals("getStreamingInterval", 0, trace.getStreamingInterval());
        assertEquals("getName", TEST_STREAM, trace.getName());

        assertEquals("getNbEvents",    NB_EVENTS, trace.getNbEvents());
        assertEquals("getRange-start", 1,         trace.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end",   NB_EVENTS, trace.getTimeRange().getEndTime().getValue());
        assertEquals("getStartTime",   1,         trace.getStartTime().getValue());
        assertEquals("getEndTime",     NB_EVENTS, trace.getEndTime().getValue());

        try {
            final URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(DIRECTORY + File.separator + TEST_STREAM), null);
            testfile = new File(FileLocator.toFileURL(location).toURI());
            trace = new TmfTraceStub(testfile.toURI().getPath(), BLOCK_SIZE);
            trace.indexTrace();
        } catch (final URISyntaxException e) {
            fail("URISyntaxException");
        } catch (final IOException e) {
            fail("IOException");
        }

        assertEquals("getType",  TmfEvent.class, trace.getType());
        assertNull  ("getResource", trace.getResource());
        assertEquals("getPath", testfile.toURI().getPath(), trace.getPath());
        assertEquals("getCacheSize", BLOCK_SIZE, trace.getCacheSize());
        assertEquals("getStreamingInterval", 0, trace.getStreamingInterval());
        assertEquals("getName", TEST_STREAM, trace.getName());

        assertEquals("getNbEvents",    NB_EVENTS, trace.getNbEvents());
        assertEquals("getRange-start", 1,         trace.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end",   NB_EVENTS, trace.getTimeRange().getEndTime().getValue());
        assertEquals("getStartTime",   1,         trace.getStartTime().getValue());
        assertEquals("getEndTime",     NB_EVENTS, trace.getEndTime().getValue());
    }

    @SuppressWarnings("null")
    public void testFullConstructor() throws TmfTraceException {
        TmfTraceStub trace = null;
        File testfile = null;
        try {
            final URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(DIRECTORY + File.separator + TEST_STREAM), null);
            testfile = new File(FileLocator.toFileURL(location).toURI());
            trace = new TmfTraceStub(testfile.toURI().getPath(), BLOCK_SIZE, null);
            trace.indexTrace();
        } catch (final URISyntaxException e) {
            fail("URISyntaxException");
        } catch (final IOException e) {
            fail("IOException");
        }

        assertFalse ("Open trace", trace == null);
        assertEquals("getType",  TmfEvent.class, trace.getType());
        assertNull  ("getResource", trace.getResource());
        assertEquals("getPath", testfile.toURI().getPath(), trace.getPath());
        assertEquals("getCacheSize", BLOCK_SIZE, trace.getCacheSize());
        assertEquals("getStreamingInterval", 0, trace.getStreamingInterval());
        assertEquals("getName", TEST_STREAM, trace.getName());

        assertEquals("getNbEvents",    NB_EVENTS, trace.getNbEvents());
        assertEquals("getRange-start", 1,         trace.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end",   NB_EVENTS, trace.getTimeRange().getEndTime().getValue());
        assertEquals("getStartTime",   1,         trace.getStartTime().getValue());
        assertEquals("getEndTime",     NB_EVENTS, trace.getEndTime().getValue());
    }

    @SuppressWarnings("null")
    public void testLiveTraceConstructor() throws TmfTraceException {
        TmfTraceStub trace = null;
        File testfile = null;
        final long interval = 100;
        try {
            final URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(DIRECTORY + File.separator + TEST_STREAM), null);
            testfile = new File(FileLocator.toFileURL(location).toURI());
            trace = new TmfTraceStub(testfile.toURI().getPath(), BLOCK_SIZE, interval);
            trace.indexTrace();
        } catch (final URISyntaxException e) {
            fail("URISyntaxException");
        } catch (final IOException e) {
            fail("IOException");
        }

        assertFalse ("Open trace", trace == null);
        assertEquals("getType",  TmfEvent.class, trace.getType());
        assertNull  ("getResource", trace.getResource());
        assertEquals("getPath", testfile.toURI().getPath(), trace.getPath());
        assertEquals("getCacheSize", BLOCK_SIZE, trace.getCacheSize());
        assertEquals("getStreamingInterval", interval, trace.getStreamingInterval());
        assertEquals("getName", TEST_STREAM, trace.getName());

        assertEquals("getNbEvents",    NB_EVENTS, trace.getNbEvents());
        assertEquals("getRange-start", 1,         trace.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end",   NB_EVENTS, trace.getTimeRange().getEndTime().getValue());
        assertEquals("getStartTime",   1,         trace.getStartTime().getValue());
        assertEquals("getEndTime",     NB_EVENTS, trace.getEndTime().getValue());
    }

    @SuppressWarnings({ "unchecked", "rawtypes", "null" })
    public void testCopyConstructor() throws TmfTraceException {
        TmfTraceStub original = null;
        TmfTraceStub trace = null;
        File testfile = null;
        try {
            final URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(DIRECTORY + File.separator + TEST_STREAM), null);
            testfile = new File(FileLocator.toFileURL(location).toURI());
            original = new TmfTraceStub(testfile.toURI().getPath(), BLOCK_SIZE, new TmfCheckpointIndexer(null));
            trace = new TmfTraceStub(original);
            trace.indexTrace();
        } catch (final URISyntaxException e) {
            fail("URISyntaxException");
        } catch (final IOException e) {
            fail("IOException");
        }

        assertFalse ("Open trace", trace == null);
        assertEquals("getType",  TmfEvent.class, trace.getType());
        assertNull  ("getResource", trace.getResource());
        assertEquals("getPath", testfile.toURI().getPath(), trace.getPath());
        assertEquals("getCacheSize", BLOCK_SIZE, trace.getCacheSize());
        assertEquals("getStreamingInterval", 0, trace.getStreamingInterval());
        assertEquals("getName", TEST_STREAM, trace.getName());

        assertEquals("getNbEvents",    NB_EVENTS, trace.getNbEvents());
        assertEquals("getRange-start", 1,         trace.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end",   NB_EVENTS, trace.getTimeRange().getEndTime().getValue());
        assertEquals("getStartTime",   1,         trace.getStartTime().getValue());
        assertEquals("getEndTime",     NB_EVENTS, trace.getEndTime().getValue());

        // Test the copy of a null trace
        try {
            new TmfTraceStub((TmfTraceStub) null);
            fail("Missing exception");
        } catch (final IllegalArgumentException e) {
            // test passed
        } catch (final Exception e) {
            fail("Unexpected exception");
        }
    }

    // ------------------------------------------------------------------------
    // Trace initialization
    // ------------------------------------------------------------------------

    public void testInitializeNullPath() {

        // Instantiate an "empty" trace
        final TmfTraceStub trace = new TmfTraceStub();

        try {
            trace.initialize(null, null, TmfEvent.class);
            fail("TmfTrace.initialize() - no exception thrown");
        } catch (TmfTraceException e) {
            // Success
        } catch (Exception e) {
            fail("TmfTrace.initialize() - wrong exception thrown");
        }
    }

    public void testInitializeSimplePath() {

        // Instantiate an "empty" trace
        final TmfTraceStub trace = new TmfTraceStub();

        // Path == trace name
        String path = "TraceName";
        try {
            trace.initialize(null, path, TmfEvent.class);
        } catch (Exception e) {
            fail("TmfTrace.initialize() - Exception thrown");
        }

        assertEquals("getType", TmfEvent.class, trace.getType());
        assertNull  ("getResource", trace.getResource());
        assertEquals("getPath", path, trace.getPath());
        assertEquals("getCacheSize", ITmfTrace.DEFAULT_TRACE_CACHE_SIZE, trace.getCacheSize());
        assertEquals("getStreamingInterval", 0, trace.getStreamingInterval());
        assertEquals("getName", path, trace.getName());

        assertEquals("getNbEvents",    0, trace.getNbEvents());
        assertEquals("getRange-start", Long.MAX_VALUE, trace.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end",   Long.MIN_VALUE, trace.getTimeRange().getEndTime().getValue());
        assertEquals("getStartTime",   Long.MAX_VALUE, trace.getStartTime().getValue());
        assertEquals("getEndTime",     Long.MIN_VALUE, trace.getEndTime().getValue());
    }

    public void testInitializeNormalPath() {

        // Instantiate an "empty" trace
        final TmfTraceStub trace = new TmfTraceStub();

        // Path == trace name
        String name = "TraceName";
        String path = "/my/trace/path/" + name;
        try {
            trace.initialize(null, path, TmfEvent.class);
        } catch (Exception e) {
            fail("TmfTrace.initialize() - Exception thrown");
        }

        assertEquals("getType", TmfEvent.class, trace.getType());
        assertNull  ("getResource", trace.getResource());
        assertEquals("getPath", path, trace.getPath());
        assertEquals("getCacheSize", ITmfTrace.DEFAULT_TRACE_CACHE_SIZE, trace.getCacheSize());
        assertEquals("getStreamingInterval", 0, trace.getStreamingInterval());
        assertEquals("getName", name, trace.getName());

        assertEquals("getNbEvents",    0, trace.getNbEvents());
        assertEquals("getRange-start", Long.MAX_VALUE, trace.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end",   Long.MIN_VALUE, trace.getTimeRange().getEndTime().getValue());
        assertEquals("getStartTime",   Long.MAX_VALUE, trace.getStartTime().getValue());
        assertEquals("getEndTime",     Long.MIN_VALUE, trace.getEndTime().getValue());
    }

    public void testInitTrace() throws URISyntaxException, IOException, TmfTraceException, InterruptedException {

        // Instantiate an "empty" trace
        final TmfTraceStub trace = new TmfTraceStub();

        assertNull  ("getType",  trace.getType());
        assertNull  ("getResource", trace.getResource());
        assertEquals("getCacheSize", ITmfTrace.DEFAULT_TRACE_CACHE_SIZE, trace.getCacheSize());
        assertEquals("getStreamingInterval", 0, trace.getStreamingInterval());
        assertEquals("getName", "", trace.getName());

        assertEquals("getNbEvents",    0, trace.getNbEvents());
        assertEquals("getRange-start", Long.MAX_VALUE, trace.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end",   Long.MIN_VALUE, trace.getTimeRange().getEndTime().getValue());
        assertEquals("getStartTime",   Long.MAX_VALUE, trace.getStartTime().getValue());
        assertEquals("getEndTime",     Long.MIN_VALUE, trace.getEndTime().getValue());

        // Validate
        final URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(DIRECTORY + File.separator + TEST_STREAM), null);
        final File testfile = new File(FileLocator.toFileURL(location).toURI());
        assertTrue("validate", trace.validate(null, testfile.getPath()));

        // InitTrace and wait for indexing completion...
        trace.initTrace(null, testfile.toURI().getPath(), TmfEvent.class);
        trace.indexTrace();
        int nbSecs = 0;
        while (trace.getNbEvents() < NB_EVENTS && nbSecs < 10) {
            Thread.sleep(1000);
            nbSecs++;
        }
        if (trace.getNbEvents() < NB_EVENTS) {
            fail("indexing");
        }

        assertEquals("getType",  TmfEvent.class, trace.getType());
        assertNull  ("getResource", trace.getResource());
        assertEquals("getCacheSize", ITmfTrace.DEFAULT_TRACE_CACHE_SIZE, trace.getCacheSize());
        assertEquals("getStreamingInterval", 0, trace.getStreamingInterval());
        assertEquals("getName", TEST_STREAM, trace.getName());

        assertEquals("getNbEvents",    NB_EVENTS, trace.getNbEvents());
        assertEquals("getRange-start", 1,         trace.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end",   NB_EVENTS, trace.getTimeRange().getEndTime().getValue());
        assertEquals("getStartTime",   1,         trace.getStartTime().getValue());
        assertEquals("getEndTime",     NB_EVENTS, trace.getEndTime().getValue());
    }

    // ------------------------------------------------------------------------
    // Set/Get streaming interval
    // ------------------------------------------------------------------------

    public void testSetStreamingInterval() throws TmfTraceException {
        final TmfTraceStub trace = new TmfTraceStub(fTrace);

        long interval = 0;
        assertEquals("getStreamingInterval", interval, trace.getStreamingInterval());

        interval = 100;
        trace.setStreamingInterval(interval);
        assertEquals("getStreamingInterval", interval, trace.getStreamingInterval());

        interval = -1;
        trace.setStreamingInterval(interval);
        assertEquals("getStreamingInterval", 0, trace.getStreamingInterval());

        interval = 0;
        trace.setStreamingInterval(interval);
        assertEquals("getStreamingInterval", interval, trace.getStreamingInterval());

        trace.dispose();
    }

    // ------------------------------------------------------------------------
    // Set/Get time range
    // ------------------------------------------------------------------------

    public void testSetTimeRange() throws TmfTraceException {
        final TmfTraceStub trace = new TmfTraceStub(fTrace);
        trace.indexTrace();

        assertEquals("getRange-start", 1,         trace.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end",   NB_EVENTS, trace.getTimeRange().getEndTime().getValue());
        assertEquals("getStartTime",   1,         trace.getStartTime().getValue());
        assertEquals("getEndTime",     NB_EVENTS, trace.getEndTime().getValue());

        trace.setTimeRange(new TmfTimeRange(new TmfTimestamp(100), new TmfTimestamp(200)));
        assertEquals("setTimeRange",   100, trace.getTimeRange().getStartTime().getValue());
        assertEquals("setTimeRange",   200, trace.getTimeRange().getEndTime().getValue());
        assertEquals("setTimeRange",   100, trace.getStartTime().getValue());
        assertEquals("setTimeRange",   200, trace.getEndTime().getValue());

        trace.dispose();
    }

    public void testSetStartTime() throws TmfTraceException {
        final TmfTraceStub trace = new TmfTraceStub(fTrace);
        trace.indexTrace();

        assertEquals("getRange-start", 1,         trace.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end",   NB_EVENTS, trace.getTimeRange().getEndTime().getValue());
        assertEquals("getStartTime",   1,         trace.getStartTime().getValue());
        assertEquals("getEndTime",     NB_EVENTS, trace.getEndTime().getValue());

        trace.setStartTime(new TmfTimestamp(100));
        assertEquals("setStartTime",   100,       trace.getTimeRange().getStartTime().getValue());
        assertEquals("setStartTime",   NB_EVENTS, trace.getTimeRange().getEndTime().getValue());
        assertEquals("setStartTime",   100,       trace.getStartTime().getValue());
        assertEquals("setStartTime",   NB_EVENTS, trace.getEndTime().getValue());

        trace.dispose();
    }

    public void testSetEndTime() throws TmfTraceException {
        final TmfTraceStub trace = new TmfTraceStub(fTrace);
        trace.indexTrace();

        assertEquals("getRange-start", 1,         trace.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end",   NB_EVENTS, trace.getTimeRange().getEndTime().getValue());
        assertEquals("getStartTime",   1,         trace.getStartTime().getValue());
        assertEquals("getEndTime",     NB_EVENTS, trace.getEndTime().getValue());

        trace.setEndTime(new TmfTimestamp(100));
        assertEquals("setEndTime",     1,   trace.getTimeRange().getStartTime().getValue());
        assertEquals("setEndTime",     100, trace.getTimeRange().getEndTime().getValue());
        assertEquals("setEndTime",     1,   trace.getStartTime().getValue());
        assertEquals("setEndTime",     100, trace.getEndTime().getValue());

        trace.dispose();
    }

    public void testSetNbEvents() throws TmfTraceException {
        final TmfTraceStub trace = new TmfTraceStub(fTrace);
        trace.indexTrace();

        assertEquals("getNbEvents", NB_EVENTS, trace.getNbEvents());

        trace.setNbEvents(0);
        assertEquals("getNbEvents", 0, trace.getNbEvents());

        trace.setNbEvents(-1);
        assertEquals("getNbEvents", 0, trace.getNbEvents());

        trace.setNbEvents(NB_EVENTS + 1);
        assertEquals("getNbEvents", NB_EVENTS + 1, trace.getNbEvents());

        trace.setNbEvents(NB_EVENTS);
        assertEquals("getNbEvents", NB_EVENTS, trace.getNbEvents());

        trace.dispose();
    }

    // ------------------------------------------------------------------------
    // seekEvent on location (note: does not reliably set the rank)
    // ------------------------------------------------------------------------

    public void testSeekEventOnCacheBoundary() {

        // Position trace at event rank 0
        ITmfContext context = fTrace.seekEvent(0);
        ITmfEvent event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 0, context.getRank());

        context = fTrace.seekEvent(context.getLocation());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.getNext(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        // Position trace at event rank 1000
        ITmfContext tmpContext = fTrace.seekEvent(new TmfTimestamp(1001, SCALE, 0));
        context = fTrace.seekEvent(tmpContext.getLocation());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1001, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.getNext(context);
        assertEquals("Event timestamp", 1001, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        // Position trace at event rank 4000
        tmpContext = fTrace.seekEvent(new TmfTimestamp(4001, SCALE, 0));
        context = fTrace.seekEvent(tmpContext.getLocation());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 4001, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.getNext(context);
        assertEquals("Event timestamp", 4001, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());
    }

    public void testSeekEventNotOnCacheBoundary() {

        // Position trace at event rank 9
        ITmfContext tmpContext = fTrace.seekEvent(new TmfTimestamp(10, SCALE, 0));
        TmfContext context = fTrace.seekEvent(tmpContext.getLocation());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        ITmfEvent event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 10, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.getNext(context);
        assertEquals("Event timestamp", 10, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        // Position trace at event rank 999
        tmpContext = fTrace.seekEvent(new TmfTimestamp(1000, SCALE, 0));
        context = fTrace.seekEvent(tmpContext.getLocation());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1000, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.getNext(context);
        assertEquals("Event timestamp", 1000, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        // Position trace at event rank 1001
        tmpContext = fTrace.seekEvent(new TmfTimestamp(1002, SCALE, 0));
        context = fTrace.seekEvent(tmpContext.getLocation());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1002, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.getNext(context);
        assertEquals("Event timestamp", 1002, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        // Position trace at event rank 4500
        tmpContext = fTrace.seekEvent(new TmfTimestamp(4501, SCALE, 0));
        context = fTrace.seekEvent(tmpContext.getLocation());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 4501, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.getNext(context);
        assertEquals("Event timestamp", 4501, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());
    }

    public void testSeekEventOutOfScope() {

        // Position trace at beginning
        ITmfContext tmpContext = fTrace.seekEvent(0);
        ITmfContext context = fTrace.seekEvent(tmpContext.getLocation());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        ITmfEvent event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.getNext(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        // Position trace at event passed the end
        context = fTrace.seekEvent(new TmfTimestamp(NB_EVENTS + 1, SCALE, 0));
        assertNull("Event timestamp", context.getLocation());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.getNext(context);
        assertNull("Event", event);
    }

    // ------------------------------------------------------------------------
    // seekEvent on timestamp (note: does not reliably set the rank)
    // ------------------------------------------------------------------------

    public void testSeekEventOnNullTimestamp() {

        // Position trace at event rank 0
        ITmfContext context = fTrace.seekEvent((ITmfTimestamp) null);
        assertEquals("Event rank", 0, context.getRank());

        ITmfEvent event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 0, context.getRank());
    }

    public void testSeekEventOnTimestampOnCacheBoundary() {

        // Position trace at event rank 0
        ITmfContext context = fTrace.seekEvent(new TmfTimestamp(1, SCALE, 0));
        assertEquals("Event rank", 0, context.getRank());

        ITmfEvent event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 0, context.getRank());

        event = fTrace.getNext(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 1, context.getRank());

        // Position trace at event rank 1000
        context = fTrace.seekEvent(new TmfTimestamp(1001, SCALE, 0));
        assertEquals("Event rank", 1000, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1001, event.getTimestamp().getValue());
        assertEquals("Event rank", 1000, context.getRank());

        event = fTrace.getNext(context);
        assertEquals("Event timestamp", 1001, event.getTimestamp().getValue());
        assertEquals("Event rank", 1001, context.getRank());

        // Position trace at event rank 4000
        context = fTrace.seekEvent(new TmfTimestamp(4001, SCALE, 0));
        assertEquals("Event rank", 4000, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 4001, event.getTimestamp().getValue());
        assertEquals("Event rank", 4000, context.getRank());

        event = fTrace.getNext(context);
        assertEquals("Event timestamp", 4001, event.getTimestamp().getValue());
        assertEquals("Event rank", 4001, context.getRank());
    }

    public void testSeekEventOnTimestampNotOnCacheBoundary() {

        // Position trace at event rank 1
        ITmfContext context = fTrace.seekEvent(new TmfTimestamp(2, SCALE, 0));
        assertEquals("Event rank", 1, context.getRank());

        ITmfEvent event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 2, event.getTimestamp().getValue());
        assertEquals("Event rank", 1, context.getRank());

        event = fTrace.getNext(context);
        assertEquals("Event timestamp", 2, event.getTimestamp().getValue());
        assertEquals("Event rank", 2, context.getRank());

        // Position trace at event rank 9
        context = fTrace.seekEvent(new TmfTimestamp(10, SCALE, 0));
        assertEquals("Event rank", 9, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 10, event.getTimestamp().getValue());
        assertEquals("Event rank", 9, context.getRank());

        event = fTrace.getNext(context);
        assertEquals("Event timestamp", 10, event.getTimestamp().getValue());
        assertEquals("Event rank", 10, context.getRank());

        // Position trace at event rank 999
        context = fTrace.seekEvent(new TmfTimestamp(1000, SCALE, 0));
        assertEquals("Event rank", 999, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1000, event.getTimestamp().getValue());
        assertEquals("Event rank", 999, context.getRank());

        event = fTrace.getNext(context);
        assertEquals("Event timestamp", 1000, event.getTimestamp().getValue());
        assertEquals("Event rank", 1000, context.getRank());

        // Position trace at event rank 1001
        context = fTrace.seekEvent(new TmfTimestamp(1002, SCALE, 0));
        assertEquals("Event rank", 1001, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1002, event.getTimestamp().getValue());
        assertEquals("Event rank", 1001, context.getRank());

        event = fTrace.getNext(context);
        assertEquals("Event timestamp", 1002, event.getTimestamp().getValue());
        assertEquals("Event rank", 1002, context.getRank());

        // Position trace at event rank 4500
        context = fTrace.seekEvent(new TmfTimestamp(4501, SCALE, 0));
        assertEquals("Event rank", 4500, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 4501, event.getTimestamp().getValue());
        assertEquals("Event rank", 4500, context.getRank());

        event = fTrace.getNext(context);
        assertEquals("Event timestamp", 4501, event.getTimestamp().getValue());
        assertEquals("Event rank", 4501, context.getRank());
    }

    public void testSeekEventOnTimestampOutOfScope() {

        // Position trace at beginning
        ITmfContext context = fTrace.seekEvent(new TmfTimestamp(-1, SCALE, 0));
        assertEquals("Event rank", 0, context.getRank());

        ITmfEvent event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 0, context.getRank());

        event = fTrace.getNext(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 1, context.getRank());

        // Position trace at event passed the end
        context = fTrace.seekEvent(new TmfTimestamp(NB_EVENTS + 1, SCALE, 0));
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", null, event);
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.getNext(context);
        assertEquals("Event timestamp", null, event);
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());
    }

    // ------------------------------------------------------------------------
    // seekEvent on rank
    // ------------------------------------------------------------------------

    public void testSeekEventOnNegativeRank() {

        // Position trace at event rank 0
        ITmfContext context = fTrace.seekEvent(-1);
        assertEquals("Event rank", 0, context.getRank());

        ITmfEvent event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 0, context.getRank());
    }

    public void testSeekOnRankOnCacheBoundary() {

        // On lower bound, returns the first event (ts = 1)
        ITmfContext context = fTrace.seekEvent(0);
        assertEquals("Event rank", 0, context.getRank());

        ITmfEvent event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 0, context.getRank());

        event = fTrace.getNext(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 1, context.getRank());

        // Position trace at event rank 1000
        context = fTrace.seekEvent(1000);
        assertEquals("Event rank", 1000, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1001, event.getTimestamp().getValue());
        assertEquals("Event rank", 1000, context.getRank());

        event = fTrace.getNext(context);
        assertEquals("Event timestamp", 1001, event.getTimestamp().getValue());
        assertEquals("Event rank", 1001, context.getRank());

        // Position trace at event rank 4000
        context = fTrace.seekEvent(4000);
        assertEquals("Event rank", 4000, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 4001, event.getTimestamp().getValue());
        assertEquals("Event rank", 4000, context.getRank());

        event = fTrace.getNext(context);
        assertEquals("Event timestamp", 4001, event.getTimestamp().getValue());
        assertEquals("Event rank", 4001, context.getRank());
    }

    public void testSeekOnRankNotOnCacheBoundary() {

        // Position trace at event rank 9
        ITmfContext context = fTrace.seekEvent(9);
        assertEquals("Event rank", 9, context.getRank());

        ITmfEvent event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 10, event.getTimestamp().getValue());
        assertEquals("Event rank", 9, context.getRank());

        event = fTrace.getNext(context);
        assertEquals("Event timestamp", 10, event.getTimestamp().getValue());
        assertEquals("Event rank", 10, context.getRank());

        // Position trace at event rank 999
        context = fTrace.seekEvent(999);
        assertEquals("Event rank", 999, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1000, event.getTimestamp().getValue());
        assertEquals("Event rank", 999, context.getRank());

        event = fTrace.getNext(context);
        assertEquals("Event timestamp", 1000, event.getTimestamp().getValue());
        assertEquals("Event rank", 1000, context.getRank());

        // Position trace at event rank 1001
        context = fTrace.seekEvent(1001);
        assertEquals("Event rank", 1001, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1002, event.getTimestamp().getValue());
        assertEquals("Event rank", 1001, context.getRank());

        event = fTrace.getNext(context);
        assertEquals("Event timestamp", 1002, event.getTimestamp().getValue());
        assertEquals("Event rank", 1002, context.getRank());

        // Position trace at event rank 4500
        context = fTrace.seekEvent(4500);
        assertEquals("Event rank", 4500, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 4501, event.getTimestamp().getValue());
        assertEquals("Event rank", 4500, context.getRank());

        event = fTrace.getNext(context);
        assertEquals("Event timestamp", 4501, event.getTimestamp().getValue());
        assertEquals("Event rank", 4501, context.getRank());
    }

    public void testSeekEventOnRankOutOfScope() {

        // Position trace at beginning
        ITmfContext context = fTrace.seekEvent(-1);
        assertEquals("Event rank", 0, context.getRank());

        ITmfEvent event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 0, context.getRank());

        event = fTrace.getNext(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 1, context.getRank());

        // Position trace at event passed the end
        context = fTrace.seekEvent(NB_EVENTS);
        assertEquals("Event rank", NB_EVENTS, context.getRank());

        event = fTrace.parseEvent(context);
        assertNull("Event", event);
        assertEquals("Event rank", NB_EVENTS, context.getRank());

        event = fTrace.getNext(context);
        assertNull("Event", event);
        assertEquals("Event rank", NB_EVENTS, context.getRank());
    }

    // ------------------------------------------------------------------------
    // parseEvent - make sure parseEvent doesn't update the context
    // ------------------------------------------------------------------------

    public void testParseEvent() {

        final int NB_READS = 20;

        // On lower bound, returns the first event (ts = 0)
        final TmfContext context = (TmfContext) fTrace.seekEvent(new TmfTimestamp(0, SCALE, 0));
        TmfContext svContext = new TmfContext(context);

        ITmfEvent event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 0, context.getRank());
        assertTrue("parseEvent", context.equals(svContext));

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 0, context.getRank());
        assertTrue("parseEvent", context.equals(svContext));

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 0, context.getRank());
        assertTrue("parseEvent", context.equals(svContext));

        // Position the trace at event NB_READS
        for (int i = 1; i < NB_READS; i++) {
            event = fTrace.getNext(context);
            assertEquals("Event timestamp", i, event.getTimestamp().getValue());
        }

        svContext = new TmfContext(context);
        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", NB_READS, event.getTimestamp().getValue());
        assertEquals("Event rank", NB_READS -1 , context.getRank());
        assertTrue("parseEvent", context.equals(svContext));

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", NB_READS, event.getTimestamp().getValue());
        assertEquals("Event rank", NB_READS - 1, context.getRank());
        assertTrue("parseEvent", context.equals(svContext));
    }

    // ------------------------------------------------------------------------
    // getNext - updates the context
    // ------------------------------------------------------------------------

    public void testGetNextAfteSeekingOnTS_1() {

        final long INITIAL_TS = 1;
        final int NB_READS = 20;

        // On lower bound, returns the first event (ts = 1)
        final ITmfContext context = fTrace.seekEvent(new TmfTimestamp(INITIAL_TS, SCALE, 0));

        // Read NB_EVENTS
        ITmfEvent event;
        for (int i = 0; i < NB_READS; i++) {
            event = fTrace.getNext(context);
            assertEquals("Event timestamp", INITIAL_TS + i, event.getTimestamp().getValue());
            assertEquals("Event rank", INITIAL_TS + i, context.getRank());
        }

        // Make sure we stay positioned
        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", INITIAL_TS + NB_READS, event.getTimestamp().getValue());
        assertEquals("Event rank", INITIAL_TS + NB_READS - 1, context.getRank());
    }

    public void testGetNextAfteSeekingOnTS_2() {

        final long INITIAL_TS = 2;
        final int NB_READS = 20;

        // On lower bound, returns the first event (ts = 500)
        final ITmfContext context = fTrace.seekEvent(new TmfTimestamp(INITIAL_TS, SCALE, 0));

        // Read NB_EVENTS
        ITmfEvent event;
        for (int i = 0; i < NB_READS; i++) {
            event = fTrace.getNext(context);
            assertEquals("Event timestamp", INITIAL_TS + i, event.getTimestamp().getValue());
            assertEquals("Event rank", INITIAL_TS + i, context.getRank());
        }

        // Make sure we stay positioned
        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", INITIAL_TS + NB_READS, event.getTimestamp().getValue());
        assertEquals("Event rank", INITIAL_TS + NB_READS - 1, context.getRank());
    }

    public void testGetNextAfteSeekingOnTS_3() {

        final long INITIAL_TS = 500;
        final int NB_READS = 20;

        // On lower bound, returns the first event (ts = 500)
        final ITmfContext context = fTrace.seekEvent(new TmfTimestamp(INITIAL_TS, SCALE, 0));

        // Read NB_EVENTS
        ITmfEvent event;
        for (int i = 0; i < NB_READS; i++) {
            event = fTrace.getNext(context);
            assertEquals("Event timestamp", INITIAL_TS + i, event.getTimestamp().getValue());
            assertEquals("Event rank", INITIAL_TS + i, context.getRank());
        }

        // Make sure we stay positioned
        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", INITIAL_TS + NB_READS, event.getTimestamp().getValue());
        assertEquals("Event rank", INITIAL_TS + NB_READS - 1, context.getRank());
    }

    public void testGetNextAfterSeekingOnRank_1() {

        final long INITIAL_RANK = 0L;
        final int NB_READS = 20;

        // On lower bound, returns the first event (rank = 0)
        final ITmfContext context = fTrace.seekEvent(INITIAL_RANK);

        // Read NB_EVENTS
        ITmfEvent event;
        for (int i = 0; i < NB_READS; i++) {
            event = fTrace.getNext(context);
            assertEquals("Event timestamp", INITIAL_RANK + i + 1, event.getTimestamp().getValue());
            assertEquals("Event rank", INITIAL_RANK + i + 1, context.getRank());
        }

        // Make sure we stay positioned
        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", INITIAL_RANK + NB_READS + 1, event.getTimestamp().getValue());
        assertEquals("Event rank", INITIAL_RANK + NB_READS, context.getRank());
    }

    public void testGetNextAfterSeekingOnRank_2() {

        final long INITIAL_RANK = 1L;
        final int NB_READS = 20;

        // On lower bound, returns the first event (rank = 0)
        final ITmfContext context = fTrace.seekEvent(INITIAL_RANK);

        // Read NB_EVENTS
        ITmfEvent event;
        for (int i = 0; i < NB_READS; i++) {
            event = fTrace.getNext(context);
            assertEquals("Event timestamp", INITIAL_RANK + i + 1, event.getTimestamp().getValue());
            assertEquals("Event rank", INITIAL_RANK + i + 1, context.getRank());
        }

        // Make sure we stay positioned
        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", INITIAL_RANK + NB_READS + 1, event.getTimestamp().getValue());
        assertEquals("Event rank", INITIAL_RANK + NB_READS, context.getRank());
    }

    public void testGetNextAfterSeekingOnRank_3() {

        final long INITIAL_RANK = 500L;
        final int NB_READS = 20;

        // On lower bound, returns the first event (rank = 0)
        final ITmfContext context = fTrace.seekEvent(INITIAL_RANK);

        // Read NB_EVENTS
        ITmfEvent event;
        for (int i = 0; i < NB_READS; i++) {
            event = fTrace.getNext(context);
            assertEquals("Event timestamp", INITIAL_RANK + i + 1, event.getTimestamp().getValue());
            assertEquals("Event rank", INITIAL_RANK + i + 1, context.getRank());
        }

        // Make sure we stay positioned
        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", INITIAL_RANK + NB_READS + 1, event.getTimestamp().getValue());
        assertEquals("Event rank", INITIAL_RANK + NB_READS, context.getRank());
    }

    public void testGetNextAfterSeekingOnLocation_1() {

        final ITmfLocation<?> INITIAL_LOC = null;
        final long INITIAL_TS = 1;
        final int NB_READS = 20;

        // On lower bound, returns the first event (ts = 1)
        final ITmfContext context = fTrace.seekEvent(INITIAL_LOC);

        // Read NB_EVENTS
        ITmfEvent event;
        for (int i = 0; i < NB_READS; i++) {
            event = fTrace.getNext(context);
            assertEquals("Event timestamp", INITIAL_TS + i, event.getTimestamp().getValue());
            assertEquals("Event rank", INITIAL_TS + i, context.getRank());
        }

        // Make sure we stay positioned
        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", INITIAL_TS + NB_READS, event.getTimestamp().getValue());
        assertEquals("Event rank", INITIAL_TS + NB_READS - 1, context.getRank());
    }

    public void testGetNextAfterSeekingOnLocation_2() {

        final ITmfLocation<?> INITIAL_LOC = fTrace.seekEvent(1L).getLocation();
        final long INITIAL_TS = 2;
        final int NB_READS = 20;

        // On lower bound, returns the first event (ts = 501)
        final ITmfContext context = fTrace.seekEvent(INITIAL_LOC);

        // Read NB_EVENTS
        ITmfEvent event;
        for (int i = 0; i < NB_READS; i++) {
            event = fTrace.getNext(context);
            assertEquals("Event timestamp", INITIAL_TS + i, event.getTimestamp().getValue());
        }

        // Make sure we stay positioned
        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", INITIAL_TS + NB_READS, event.getTimestamp().getValue());
    }

    public void testGetNextAfterSeekingOnLocation_3() {

        final ITmfLocation<?> INITIAL_LOC = fTrace.seekEvent(500L).getLocation();
        final long INITIAL_TS = 501;
        final int NB_READS = 20;

        // On lower bound, returns the first event (ts = 501)
        final ITmfContext context = fTrace.seekEvent(INITIAL_LOC);

        // Read NB_EVENTS
        ITmfEvent event;
        for (int i = 0; i < NB_READS; i++) {
            event = fTrace.getNext(context);
            assertEquals("Event timestamp", INITIAL_TS + i, event.getTimestamp().getValue());
        }

        // Make sure we stay positioned
        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", INITIAL_TS + NB_READS, event.getTimestamp().getValue());
    }

    public void testGetNextLocation() {
        ITmfContext context1 = fTrace.seekEvent(0);
        fTrace.getNext(context1);
        ITmfLocation<?> location = context1.getLocation().clone();
        ITmfEvent event1 = fTrace.getNext(context1);
        ITmfContext context2 = fTrace.seekEvent(location);
        ITmfEvent event2 = fTrace.getNext(context2);
        assertEquals("Event timestamp", event1.getTimestamp().getValue(), event2.getTimestamp().getValue());
    }

    public void testGetNextEndLocation() {
        ITmfContext context1 = fTrace.seekEvent(fTrace.getNbEvents() - 1);
        fTrace.getNext(context1);
        ITmfLocation<?> location = context1.getLocation().clone();
        ITmfContext context2 = fTrace.seekEvent(location);
        ITmfEvent event = fTrace.getNext(context2);
        assertNull("Event", event);
    }

    // ------------------------------------------------------------------------
    // processRequest
    // ------------------------------------------------------------------------

    @SuppressWarnings("hiding")
    public void testProcessEventRequestForAllEvents() throws InterruptedException {
        final int BLOCK_SIZE =  1;
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();

        final TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BIG_BANG, TmfTimestamp.BIG_CRUNCH);
        final TmfEventRequest<TmfEvent> request = new TmfEventRequest<TmfEvent>(TmfEvent.class, range, NB_EVENTS, BLOCK_SIZE) {
            @Override
            public void handleData(final TmfEvent event) {
                super.handleData(event);
                requestedEvents.add(event);
            }
        };
        final ITmfDataProvider<TmfEvent>[] providers = (ITmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TmfTraceStub.class);
        providers[0].sendRequest(request);
        request.waitForCompletion();

        assertEquals("nbEvents", NB_EVENTS, requestedEvents.size());
        assertTrue("isCompleted",  request.isCompleted());
        assertFalse("isCancelled", request.isCancelled());

        // Ensure that we have distinct events.
        // Don't go overboard: we are not validating the stub!
        for (int i = 0; i < NB_EVENTS; i++) {
            assertEquals("Distinct events", i + 1, requestedEvents.get(i).getTimestamp().getValue());
        }
    }

    @SuppressWarnings("hiding")
    public void testProcessEventRequestForNbEvents() throws InterruptedException {
        final int BLOCK_SIZE = 100;
        final int NB_EVENTS  = 1000;
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();

        final TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BIG_BANG, TmfTimestamp.BIG_CRUNCH);
        final TmfEventRequest<TmfEvent> request = new TmfEventRequest<TmfEvent>(TmfEvent.class, range, NB_EVENTS, BLOCK_SIZE) {
            @Override
            public void handleData(final TmfEvent event) {
                super.handleData(event);
                requestedEvents.add(event);
            }
        };
        final ITmfDataProvider<TmfEvent>[] providers = (ITmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TmfTraceStub.class);
        providers[0].sendRequest(request);
        request.waitForCompletion();

        assertEquals("nbEvents", NB_EVENTS, requestedEvents.size());
        assertTrue("isCompleted",  request.isCompleted());
        assertFalse("isCancelled", request.isCancelled());

        // Ensure that we have distinct events.
        // Don't go overboard: we are not validating the stub!
        for (int i = 0; i < NB_EVENTS; i++) {
            assertEquals("Distinct events", i + 1, requestedEvents.get(i).getTimestamp().getValue());
        }
    }

    @SuppressWarnings("hiding")
    public void testProcessEventRequestForSomeEvents() throws InterruptedException {
        final int BLOCK_SIZE =  1;
        final long startTime = 100;
        final int NB_EVENTS  = 1000;
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();

        final TmfTimeRange range = new TmfTimeRange(new TmfTimestamp(startTime, SCALE), TmfTimestamp.BIG_CRUNCH);
        final TmfEventRequest<TmfEvent> request = new TmfEventRequest<TmfEvent>(TmfEvent.class, range, NB_EVENTS, BLOCK_SIZE) {
            @Override
            public void handleData(final TmfEvent event) {
                super.handleData(event);
                requestedEvents.add(event);
            }
        };
        final ITmfDataProvider<TmfEvent>[] providers = (ITmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TmfTraceStub.class);
        providers[0].sendRequest(request);
        request.waitForCompletion();

        assertEquals("nbEvents", NB_EVENTS, requestedEvents.size());
        assertTrue("isCompleted",  request.isCompleted());
        assertFalse("isCancelled", request.isCancelled());

        // Ensure that we have distinct events.
        // Don't go overboard: we are not validating the stub!
        for (int i = 0; i < NB_EVENTS; i++) {
            assertEquals("Distinct events", startTime + i, requestedEvents.get(i).getTimestamp().getValue());
        }
    }

    @SuppressWarnings("hiding")
    public void testProcessEventRequestForOtherEvents() throws InterruptedException {
        final int BLOCK_SIZE =  1;
        final int startIndex = 99;
        final long startTime = 100;
        final int NB_EVENTS  = 1000;
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();

        final TmfTimeRange range = new TmfTimeRange(new TmfTimestamp(startTime, SCALE), TmfTimestamp.BIG_CRUNCH);
        final TmfEventRequest<TmfEvent> request = new TmfEventRequest<TmfEvent>(TmfEvent.class, range, startIndex, NB_EVENTS, BLOCK_SIZE) {
            @Override
            public void handleData(final TmfEvent event) {
                super.handleData(event);
                requestedEvents.add(event);
            }
        };
        final ITmfDataProvider<TmfEvent>[] providers = (ITmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TmfTraceStub.class);
        providers[0].sendRequest(request);
        request.waitForCompletion();

        assertEquals("nbEvents", NB_EVENTS, requestedEvents.size());
        assertTrue("isCompleted",  request.isCompleted());
        assertFalse("isCancelled", request.isCancelled());

        // Ensure that we have distinct events.
        // Don't go overboard: we are not validating the stub!
        for (int i = 0; i < NB_EVENTS; i++) {
            assertEquals("Distinct events", startTime + i, requestedEvents.get(i).getTimestamp().getValue());
        }
    }

    @SuppressWarnings("hiding")
    public void testProcessDataRequestForSomeEvents() throws InterruptedException {
        final int startIndex = 100;
        final int NB_EVENTS  = 1000;
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();

        final TmfDataRequest<TmfEvent> request = new TmfDataRequest<TmfEvent>(TmfEvent.class, startIndex, NB_EVENTS) {
            @Override
            public void handleData(final TmfEvent event) {
                super.handleData(event);
                requestedEvents.add(event);
            }
        };
        final ITmfDataProvider<TmfEvent>[] providers = (ITmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TmfTraceStub.class);
        providers[0].sendRequest(request);
        request.waitForCompletion();

        assertEquals("nbEvents", NB_EVENTS, requestedEvents.size());
        assertTrue("isCompleted",  request.isCompleted());
        assertFalse("isCancelled", request.isCancelled());

        // Ensure that we have distinct events.
        // Don't go overboard: we are not validating the stub!
        for (int i = 0; i < NB_EVENTS; i++) {
            assertEquals("Distinct events", startIndex + 1 + i, requestedEvents.get(i).getTimestamp().getValue());
        }
    }

    // ------------------------------------------------------------------------
    // cancel
    // ------------------------------------------------------------------------

    public void testCancel() throws InterruptedException {
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();

        final TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BIG_BANG, TmfTimestamp.BIG_CRUNCH);
        final TmfEventRequest<TmfEvent> request = new TmfEventRequest<TmfEvent>(TmfEvent.class, range, NB_EVENTS, BLOCK_SIZE) {
            int nbRead = 0;
            @Override
            public void handleData(final TmfEvent event) {
                super.handleData(event);
                requestedEvents.add(event);
                if (++nbRead == BLOCK_SIZE) {
                    cancel();
                }
            }
        };
        final ITmfDataProvider<TmfEvent>[] providers = (ITmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TmfTraceStub.class);
        providers[0].sendRequest(request);
        request.waitForCompletion();

        assertEquals("nbEvents",  BLOCK_SIZE, requestedEvents.size());
        assertTrue("isCompleted", request.isCompleted());
        assertTrue("isCancelled", request.isCancelled());
    }

    // ------------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------------

    public void testDefaultTmfTraceStub() {
        assertFalse ("Open trace", fTrace == null);
        assertEquals("getType",  TmfEvent.class, fTrace.getType());
        assertNull  ("getResource", fTrace.getResource());
        assertEquals("getCacheSize", BLOCK_SIZE, fTrace.getCacheSize());
        assertEquals("getStreamingInterval", 0, fTrace.getStreamingInterval());
        assertEquals("getName", TEST_STREAM, fTrace.getName());

        assertEquals("getNbEvents",    NB_EVENTS, fTrace.getNbEvents());
        assertEquals("getRange-start", 1,         fTrace.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end",   NB_EVENTS, fTrace.getTimeRange().getEndTime().getValue());
        assertEquals("getStartTime",   1,         fTrace.getStartTime().getValue());
        assertEquals("getEndTime",     NB_EVENTS, fTrace.getEndTime().getValue());

        String expected = "TmfTrace [fPath=" + fTrace.getPath() + ", fCacheSize=" + fTrace.getCacheSize() +
                ", fNbEvents=" + fTrace.getNbEvents() + ", fStartTime=" + fTrace.getStartTime() +
                ", fEndTime=" + fTrace.getEndTime() + ", fStreamingInterval=" + fTrace.getStreamingInterval() +
                "]";
        assertEquals("toString", expected, fTrace.toString());
    }

}
