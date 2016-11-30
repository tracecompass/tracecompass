/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
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

package org.eclipse.tracecompass.tmf.core.tests.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.tracecompass.internal.tmf.core.component.TmfProviderManager;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.component.ITmfEventProvider;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest.ExecutionType;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.tests.TmfCoreTestPlugin;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.location.ITmfLocation;
import org.eclipse.tracecompass.tmf.tests.stubs.analysis.TestAnalysis;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfTraceStub;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

/**
 * Test suite for the TmfTrace class.
 */
@SuppressWarnings("javadoc")
public class TmfTraceTest {

    /** Time-out tests after 1 minute. */
    @Rule
    public TestRule globalTimeout = new Timeout(1, TimeUnit.MINUTES);

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private static final TmfTestTrace TEST_TRACE = TmfTestTrace.A_TEST_10K;
    private static final long   DEFAULT_INITIAL_OFFSET_VALUE = (1L * 100 * 1000 * 1000); // .1sec
    private static final int    NB_EVENTS   = 10000;
    private TmfTraceStub fTrace      = null;

    private static int SCALE = -3;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    @Before
    public void setUp() {
        try {
            final URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(TEST_TRACE.getFullPath()), null);
            final File test = new File(FileLocator.toFileURL(location).toURI());
            fTrace = new TmfTraceStub(test.toURI().getPath(), ITmfTrace.DEFAULT_TRACE_CACHE_SIZE, false, null);
            TmfSignalManager.deregister(fTrace);
            fTrace.indexTrace(true);
        } catch (final TmfTraceException e) {
            e.printStackTrace();
        } catch (final URISyntaxException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() {
        fTrace.dispose();
        fTrace = null;
        assertEquals(0, TmfProviderManager.getProviders(ITmfEvent.class).length);
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    @Test
    public void testFullConstructor() throws TmfTraceException {
        try {
            final URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(TEST_TRACE.getFullPath()), null);
            File testfile = new File(FileLocator.toFileURL(location).toURI());
            TmfTraceStub trace = new TmfTraceStub(testfile.toURI().getPath(), ITmfTrace.DEFAULT_TRACE_CACHE_SIZE, false, null);
            trace.indexTrace(true);

            assertEquals("getEventType", ITmfEvent.class, trace.getEventType());
            assertNull("getResource", trace.getResource());
            assertEquals("getPath", testfile.toURI().getPath(), trace.getPath());
            assertEquals("getCacheSize", ITmfTrace.DEFAULT_TRACE_CACHE_SIZE, trace.getCacheSize());
            assertEquals("getStreamingInterval", 0, trace.getStreamingInterval());
            assertEquals("getName", TEST_TRACE.getPath(), trace.getName());

            assertEquals("getNbEvents", NB_EVENTS, trace.getNbEvents());
            assertEquals("getRange-start", 1, trace.getTimeRange().getStartTime().getValue());
            assertEquals("getRange-end", NB_EVENTS, trace.getTimeRange().getEndTime().getValue());
            assertEquals("getStartTime", 1, trace.getStartTime().getValue());
            assertEquals("getEndTime", NB_EVENTS, trace.getEndTime().getValue());

            trace.dispose();
        } catch (final URISyntaxException e) {
            fail("URISyntaxException");
        } catch (final IOException e) {
            fail("IOException");
        }
    }

    @Test
    public void testLiveTraceConstructor() throws TmfTraceException {
        final long interval = 100;
        try {
            final URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(TEST_TRACE.getFullPath()), null);
            File testfile = new File(FileLocator.toFileURL(location).toURI());
            TmfTraceStub trace = new TmfTraceStub(testfile.toURI().getPath(), ITmfTrace.DEFAULT_TRACE_CACHE_SIZE, interval);
            trace.indexTrace(true);

            assertEquals("getEventType", ITmfEvent.class, trace.getEventType());
            assertNull("getResource", trace.getResource());
            assertEquals("getPath", testfile.toURI().getPath(), trace.getPath());
            assertEquals("getCacheSize", ITmfTrace.DEFAULT_TRACE_CACHE_SIZE, trace.getCacheSize());
            assertEquals("getStreamingInterval", interval, trace.getStreamingInterval());
            assertEquals("getName", TEST_TRACE.getPath(), trace.getName());

            assertEquals("getNbEvents", NB_EVENTS, trace.getNbEvents());
            assertEquals("getRange-start", 1, trace.getTimeRange().getStartTime().getValue());
            assertEquals("getRange-end", NB_EVENTS, trace.getTimeRange().getEndTime().getValue());
            assertEquals("getStartTime", 1, trace.getStartTime().getValue());
            assertEquals("getEndTime", NB_EVENTS, trace.getEndTime().getValue());

            trace.dispose();
        } catch (final URISyntaxException e) {
            fail("URISyntaxException");
        } catch (final IOException e) {
            fail("IOException");
        }
    }

    @Test
    public void testCopyConstructor() throws TmfTraceException {
        try {
            final URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(TEST_TRACE.getFullPath()), null);
            File testfile = new File(FileLocator.toFileURL(location).toURI());
            TmfTraceStub original = new TmfTraceStub(testfile.toURI().getPath(), ITmfTrace.DEFAULT_TRACE_CACHE_SIZE, false, null);
            TmfTraceStub trace = new TmfTraceStub(original);
            trace.indexTrace(true);

            assertEquals("getEventType", ITmfEvent.class, trace.getEventType());
            assertNull("getResource", trace.getResource());
            assertEquals("getPath", testfile.toURI().getPath(), trace.getPath());
            assertEquals("getCacheSize", ITmfTrace.DEFAULT_TRACE_CACHE_SIZE, trace.getCacheSize());
            assertEquals("getStreamingInterval", 0, trace.getStreamingInterval());
            assertEquals("getName", TEST_TRACE.getPath(), trace.getName());

            assertEquals("getNbEvents", NB_EVENTS, trace.getNbEvents());
            assertEquals("getRange-start", 1, trace.getTimeRange().getStartTime().getValue());
            assertEquals("getRange-end", NB_EVENTS, trace.getTimeRange().getEndTime().getValue());
            assertEquals("getStartTime", 1, trace.getStartTime().getValue());
            assertEquals("getEndTime", NB_EVENTS, trace.getEndTime().getValue());

            original.dispose();
            trace.dispose();
        } catch (final URISyntaxException e) {
            fail("URISyntaxException");
        } catch (final IOException e) {
            fail("IOException");
        }

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

    @Test
    public void testInitializeNullPath() {
        // Instantiate an "empty" trace
        final TmfTraceStub trace = new TmfTraceStub();

        try {
            trace.initialize(null, null, ITmfEvent.class);
            fail("TmfTrace.initialize() - no exception thrown");
        } catch (TmfTraceException e) {
            // Success
            trace.dispose();
        } catch (Exception e) {
            fail("TmfTrace.initialize() - wrong exception thrown");
        }
    }

    @Test
    public void testInitializeSimplePath() {
        // Instantiate an "empty" trace
        final TmfTraceStub trace = new TmfTraceStub();

        // Path == trace name
        String path = "TraceName";
        try {
            trace.initialize(null, path, ITmfEvent.class);
        } catch (Exception e) {
            fail("TmfTrace.initialize() - Exception thrown");
        }

        assertEquals("getEventType", ITmfEvent.class, trace.getEventType());
        assertNull  ("getResource", trace.getResource());
        assertEquals("getPath", path, trace.getPath());
        assertEquals("getCacheSize", ITmfTrace.DEFAULT_TRACE_CACHE_SIZE, trace.getCacheSize());
        assertEquals("getStreamingInterval", 0, trace.getStreamingInterval());
        assertEquals("getName", path, trace.getName());

        assertEquals("getNbEvents",    0, trace.getNbEvents());
        assertEquals("getRange-start", Long.MIN_VALUE, trace.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end",   Long.MIN_VALUE, trace.getTimeRange().getEndTime().getValue());
        assertEquals("getStartTime",   Long.MIN_VALUE, trace.getStartTime().getValue());
        assertEquals("getEndTime",     Long.MIN_VALUE, trace.getEndTime().getValue());

        trace.dispose();
    }

    @Test
    public void testInitializeNormalPath() {
        // Instantiate an "empty" trace
        final TmfTraceStub trace = new TmfTraceStub();

        // Path == trace name
        String name = "TraceName";
        String path = "/my/trace/path/" + name;
        try {
            trace.initialize(null, path, ITmfEvent.class);
        } catch (Exception e) {
            fail("TmfTrace.initialize() - Exception thrown");
        }

        assertEquals("getEventType", ITmfEvent.class, trace.getEventType());
        assertNull  ("getResource", trace.getResource());
        assertEquals("getPath", path, trace.getPath());
        assertEquals("getCacheSize", ITmfTrace.DEFAULT_TRACE_CACHE_SIZE, trace.getCacheSize());
        assertEquals("getStreamingInterval", 0, trace.getStreamingInterval());
        assertEquals("getName", name, trace.getName());

        assertEquals("getNbEvents",    0, trace.getNbEvents());
        assertEquals("getRange-start", Long.MIN_VALUE, trace.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end",   Long.MIN_VALUE, trace.getTimeRange().getEndTime().getValue());
        assertEquals("getStartTime",   Long.MIN_VALUE, trace.getStartTime().getValue());
        assertEquals("getEndTime",     Long.MIN_VALUE, trace.getEndTime().getValue());

        trace.dispose();
    }

    @Test
    public void testInitTrace() throws URISyntaxException, IOException, TmfTraceException, InterruptedException {
        // Instantiate an "empty" trace
        final TmfTraceStub trace = new TmfTraceStub();

        assertNull  ("getEventType",  trace.getEventType());
        assertNull  ("getResource", trace.getResource());
        assertEquals("getCacheSize", ITmfTrace.DEFAULT_TRACE_CACHE_SIZE, trace.getCacheSize());
        assertEquals("getStreamingInterval", 0, trace.getStreamingInterval());
        assertEquals("getName", "", trace.getName());

        assertEquals("getNbEvents",    0, trace.getNbEvents());
        assertEquals("getRange-start", Long.MIN_VALUE, trace.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end",   Long.MIN_VALUE, trace.getTimeRange().getEndTime().getValue());
        assertEquals("getStartTime",   Long.MIN_VALUE, trace.getStartTime().getValue());
        assertEquals("getEndTime",     Long.MIN_VALUE, trace.getEndTime().getValue());

        // Validate
        final URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(TEST_TRACE.getFullPath()), null);
        final File testfile = new File(FileLocator.toFileURL(location).toURI());
        assertTrue("validate", trace.validate(null, testfile.getPath()).isOK());

        // InitTrace and wait for indexing completion...
        trace.initTrace(null, testfile.toURI().getPath(), ITmfEvent.class);
        trace.indexTrace(true);
        int nbSecs = 0;
        while (trace.getNbEvents() < NB_EVENTS && nbSecs < 10) {
            Thread.sleep(1000);
            nbSecs++;
        }
        if (trace.getNbEvents() < NB_EVENTS) {
            fail("indexing");
        }

        assertEquals("getEventType", ITmfEvent.class, trace.getEventType());
        assertNull  ("getResource", trace.getResource());
        assertEquals("getCacheSize", ITmfTrace.DEFAULT_TRACE_CACHE_SIZE, trace.getCacheSize());
        assertEquals("getStreamingInterval", 0, trace.getStreamingInterval());
        assertEquals("getName", TEST_TRACE.getPath(), trace.getName());

        assertEquals("getNbEvents",    NB_EVENTS, trace.getNbEvents());
        assertEquals("getRange-start", 1,         trace.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end",   NB_EVENTS, trace.getTimeRange().getEndTime().getValue());
        assertEquals("getStartTime",   1,         trace.getStartTime().getValue());
        assertEquals("getEndTime",     NB_EVENTS, trace.getEndTime().getValue());

        trace.dispose();
    }

    // ------------------------------------------------------------------------
    // Set/Get streaming interval
    // ------------------------------------------------------------------------

    @Test
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

    @Test
    public void testSetTimeRange() throws TmfTraceException {
        final TmfTraceStub trace = new TmfTraceStub(fTrace);
        trace.indexTrace(true);

        assertEquals("getRange-start", 1,         trace.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end",   NB_EVENTS, trace.getTimeRange().getEndTime().getValue());
        assertEquals("getStartTime",   1,         trace.getStartTime().getValue());
        assertEquals("getEndTime",     NB_EVENTS, trace.getEndTime().getValue());

        trace.setTimeRange(new TmfTimeRange(TmfTimestamp.fromSeconds(100), TmfTimestamp.fromSeconds(200)));
        assertEquals("setTimeRange",   100, trace.getTimeRange().getStartTime().getValue());
        assertEquals("setTimeRange",   200, trace.getTimeRange().getEndTime().getValue());
        assertEquals("setTimeRange",   100, trace.getStartTime().getValue());
        assertEquals("setTimeRange",   200, trace.getEndTime().getValue());

        trace.dispose();
    }

    @Test
    public void testSetStartTime() throws TmfTraceException {
        final TmfTraceStub trace = new TmfTraceStub(fTrace);
        trace.indexTrace(true);

        assertEquals("getRange-start", 1,         trace.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end",   NB_EVENTS, trace.getTimeRange().getEndTime().getValue());
        assertEquals("getStartTime",   1,         trace.getStartTime().getValue());
        assertEquals("getEndTime",     NB_EVENTS, trace.getEndTime().getValue());

        trace.setStartTime(TmfTimestamp.fromSeconds(100));
        assertEquals("setStartTime",   100,       trace.getTimeRange().getStartTime().getValue());
        assertEquals("setStartTime",   NB_EVENTS, trace.getTimeRange().getEndTime().getValue());
        assertEquals("setStartTime",   100,       trace.getStartTime().getValue());
        assertEquals("setStartTime",   NB_EVENTS, trace.getEndTime().getValue());

        trace.dispose();
    }

    @Test
    public void testSetEndTime() throws TmfTraceException {
        final TmfTraceStub trace = new TmfTraceStub(fTrace);
        trace.indexTrace(true);

        assertEquals("getRange-start", 1,         trace.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end",   NB_EVENTS, trace.getTimeRange().getEndTime().getValue());
        assertEquals("getStartTime",   1,         trace.getStartTime().getValue());
        assertEquals("getEndTime",     NB_EVENTS, trace.getEndTime().getValue());

        trace.setEndTime(TmfTimestamp.fromSeconds(100));
        assertEquals("setEndTime",     1,   trace.getTimeRange().getStartTime().getValue());
        assertEquals("setEndTime",     100, trace.getTimeRange().getEndTime().getValue());
        assertEquals("setEndTime",     1,   trace.getStartTime().getValue());
        assertEquals("setEndTime",     100, trace.getEndTime().getValue());

        trace.dispose();
    }

    @Test
    public void testSetNbEvents() throws TmfTraceException {
        final TmfTraceStub trace = new TmfTraceStub(fTrace);
        trace.indexTrace(true);

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
    // State system, statistics and modules methods
    // ------------------------------------------------------------------------

    @Test
    public void testGetModules() {
        /* There should not be any modules at this point */
        Iterable<IAnalysisModule> modules = fTrace.getAnalysisModules();
        assertFalse(modules.iterator().hasNext());

        /* Open the trace, the modules should be populated */
        fTrace.traceOpened(new TmfTraceOpenedSignal(this, fTrace, null));

        modules = fTrace.getAnalysisModules();
        assertTrue(modules.iterator().hasNext());

        /*
         * Make sure all modules of type TestAnalysis are returned in the second
         * call
         */
        int count = 0;
        for (IAnalysisModule module : modules) {
            if (module instanceof TestAnalysis) {
                count++;
                IAnalysisModule otherModule = fTrace.getAnalysisModule(module.getId());
                assertNotNull(otherModule);
                assertEquals(otherModule, module);
            }
        }
        /*
         * FIXME: The exact count depends on the context the test is run (full
         * test suite or this file only), but there must be at least 2 modules
         */
        assertTrue(count >= 2);
    }

    // ------------------------------------------------------------------------
    // seekEvent on location (note: does not reliably set the rank)
    // ------------------------------------------------------------------------

    @Test
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
        ITmfContext tmpContext = fTrace.seekEvent(TmfTimestamp.create(1001, SCALE));
        context = fTrace.seekEvent(tmpContext.getLocation());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1001, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.getNext(context);
        assertEquals("Event timestamp", 1001, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        // Position trace at event rank 4000
        tmpContext = fTrace.seekEvent(TmfTimestamp.create(4001, SCALE));
        context = fTrace.seekEvent(tmpContext.getLocation());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 4001, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.getNext(context);
        assertEquals("Event timestamp", 4001, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());
    }

    @Test
    public void testSeekEventNotOnCacheBoundary() {
        // Position trace at event rank 9
        ITmfContext tmpContext = fTrace.seekEvent(TmfTimestamp.create(10, SCALE));
        TmfContext context = fTrace.seekEvent(tmpContext.getLocation());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        ITmfEvent event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 10, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.getNext(context);
        assertEquals("Event timestamp", 10, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        // Position trace at event rank 999
        tmpContext = fTrace.seekEvent(TmfTimestamp.create(1000, SCALE));
        context = fTrace.seekEvent(tmpContext.getLocation());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1000, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.getNext(context);
        assertEquals("Event timestamp", 1000, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        // Position trace at event rank 1001
        tmpContext = fTrace.seekEvent(TmfTimestamp.create(1002, SCALE));
        context = fTrace.seekEvent(tmpContext.getLocation());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1002, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.getNext(context);
        assertEquals("Event timestamp", 1002, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        // Position trace at event rank 4500
        tmpContext = fTrace.seekEvent(TmfTimestamp.create(4501, SCALE));
        context = fTrace.seekEvent(tmpContext.getLocation());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 4501, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.getNext(context);
        assertEquals("Event timestamp", 4501, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());
    }

    @Test
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
        context = fTrace.seekEvent(TmfTimestamp.create(NB_EVENTS + 1, SCALE));
        assertNull("Event timestamp", context.getLocation());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.getNext(context);
        assertNull("Event", event);
    }

    // ------------------------------------------------------------------------
    // seekEvent on timestamp (note: does not reliably set the rank)
    // ------------------------------------------------------------------------

    @Test
    public void testSeekEventOnNullTimestamp() {
        // Position trace at event rank 0
        ITmfContext context = fTrace.seekEvent((ITmfTimestamp) null);
        assertEquals("Event rank", 0, context.getRank());

        ITmfEvent event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 0, context.getRank());
    }

    @Test
    public void testSeekEventOnTimestampOnCacheBoundary() {
        // Position trace at event rank 0
        ITmfContext context = fTrace.seekEvent(TmfTimestamp.create(1, SCALE));
        assertEquals("Event rank", 0, context.getRank());

        ITmfEvent event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 0, context.getRank());

        event = fTrace.getNext(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 1, context.getRank());

        // Position trace at event rank 1000
        context = fTrace.seekEvent(TmfTimestamp.create(1001, SCALE));
        assertEquals("Event rank", 1000, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1001, event.getTimestamp().getValue());
        assertEquals("Event rank", 1000, context.getRank());

        event = fTrace.getNext(context);
        assertEquals("Event timestamp", 1001, event.getTimestamp().getValue());
        assertEquals("Event rank", 1001, context.getRank());

        // Position trace at event rank 4000
        context = fTrace.seekEvent(TmfTimestamp.create(4001, SCALE));
        assertEquals("Event rank", 4000, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 4001, event.getTimestamp().getValue());
        assertEquals("Event rank", 4000, context.getRank());

        event = fTrace.getNext(context);
        assertEquals("Event timestamp", 4001, event.getTimestamp().getValue());
        assertEquals("Event rank", 4001, context.getRank());
    }

    @Test
    public void testSeekEventOnTimestampNotOnCacheBoundary() {
        // Position trace at event rank 1
        ITmfContext context = fTrace.seekEvent(TmfTimestamp.create(2, SCALE));
        assertEquals("Event rank", 1, context.getRank());

        ITmfEvent event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 2, event.getTimestamp().getValue());
        assertEquals("Event rank", 1, context.getRank());

        event = fTrace.getNext(context);
        assertEquals("Event timestamp", 2, event.getTimestamp().getValue());
        assertEquals("Event rank", 2, context.getRank());

        // Position trace at event rank 9
        context = fTrace.seekEvent(TmfTimestamp.create(10, SCALE));
        assertEquals("Event rank", 9, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 10, event.getTimestamp().getValue());
        assertEquals("Event rank", 9, context.getRank());

        event = fTrace.getNext(context);
        assertEquals("Event timestamp", 10, event.getTimestamp().getValue());
        assertEquals("Event rank", 10, context.getRank());

        // Position trace at event rank 999
        context = fTrace.seekEvent(TmfTimestamp.create(1000, SCALE));
        assertEquals("Event rank", 999, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1000, event.getTimestamp().getValue());
        assertEquals("Event rank", 999, context.getRank());

        event = fTrace.getNext(context);
        assertEquals("Event timestamp", 1000, event.getTimestamp().getValue());
        assertEquals("Event rank", 1000, context.getRank());

        // Position trace at event rank 1001
        context = fTrace.seekEvent(TmfTimestamp.create(1002, SCALE));
        assertEquals("Event rank", 1001, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1002, event.getTimestamp().getValue());
        assertEquals("Event rank", 1001, context.getRank());

        event = fTrace.getNext(context);
        assertEquals("Event timestamp", 1002, event.getTimestamp().getValue());
        assertEquals("Event rank", 1002, context.getRank());

        // Position trace at event rank 4500
        context = fTrace.seekEvent(TmfTimestamp.create(4501, SCALE));
        assertEquals("Event rank", 4500, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 4501, event.getTimestamp().getValue());
        assertEquals("Event rank", 4500, context.getRank());

        event = fTrace.getNext(context);
        assertEquals("Event timestamp", 4501, event.getTimestamp().getValue());
        assertEquals("Event rank", 4501, context.getRank());
    }

    @Test
    public void testSeekEventOnTimestampOutOfScope() {
        // Position trace at beginning
        ITmfContext context = fTrace.seekEvent(TmfTimestamp.create(-1, SCALE));
        assertEquals("Event rank", 0, context.getRank());

        ITmfEvent event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 0, context.getRank());

        event = fTrace.getNext(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 1, context.getRank());

        // Position trace at event passed the end
        context = fTrace.seekEvent(TmfTimestamp.create(NB_EVENTS + 1, SCALE));
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

    @Test
    public void testSeekEventOnNegativeRank() {
        // Position trace at event rank 0
        ITmfContext context = fTrace.seekEvent(-1);
        assertEquals("Event rank", 0, context.getRank());

        ITmfEvent event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 0, context.getRank());
    }

    @Test
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

    @Test
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

    @Test
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

    @Test
    public void testParseEvent() {
        final int NB_READS = 20;

        // On lower bound, returns the first event (ts = 0)
        final TmfContext context = (TmfContext) fTrace.seekEvent(TmfTimestamp.create(0, SCALE));
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

    @Test
    public void testGetNextAfteSeekingOnTS_1() {
        final long INITIAL_TS = 1;
        final int NB_READS = 20;

        // On lower bound, returns the first event (ts = 1)
        final ITmfContext context = fTrace.seekEvent(TmfTimestamp.create(INITIAL_TS, SCALE));

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

    @Test
    public void testGetNextAfteSeekingOnTS_2() {
        final long INITIAL_TS = 2;
        final int NB_READS = 20;

        // On lower bound, returns the first event (ts = 500)
        final ITmfContext context = fTrace.seekEvent(TmfTimestamp.create(INITIAL_TS, SCALE));

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

    @Test
    public void testGetNextAfteSeekingOnTS_3() {
        final long INITIAL_TS = 500;
        final int NB_READS = 20;

        // On lower bound, returns the first event (ts = 500)
        final ITmfContext context = fTrace.seekEvent(TmfTimestamp.create(INITIAL_TS, SCALE));

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

    @Test
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

    @Test
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

    @Test
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

    @Test
    public void testGetNextAfterSeekingOnLocation_1() {
        final ITmfLocation INITIAL_LOC = null;
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

    @Test
    public void testGetNextAfterSeekingOnLocation_2() {
        final ITmfLocation INITIAL_LOC = fTrace.seekEvent(1L).getLocation();
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

    @Test
    public void testGetNextAfterSeekingOnLocation_3() {
        final ITmfLocation INITIAL_LOC = fTrace.seekEvent(500L).getLocation();
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

    @Test
    public void testGetNextLocation() {
        ITmfContext context1 = fTrace.seekEvent(0);
        fTrace.getNext(context1);
        ITmfLocation location = context1.getLocation();
        ITmfEvent event1 = fTrace.getNext(context1);
        ITmfContext context2 = fTrace.seekEvent(location);
        ITmfEvent event2 = fTrace.getNext(context2);
        assertEquals("Event timestamp", event1.getTimestamp().getValue(), event2.getTimestamp().getValue());
    }

    @Test
    public void testGetNextEndLocation() {
        ITmfContext context1 = fTrace.seekEvent(fTrace.getNbEvents() - 1);
        fTrace.getNext(context1);
        ITmfLocation location = context1.getLocation();
        ITmfContext context2 = fTrace.seekEvent(location);
        ITmfEvent event = fTrace.getNext(context2);
        assertNull("Event", event);
    }

    // ------------------------------------------------------------------------
    // processRequest
    // ------------------------------------------------------------------------

    @Test
    public void testProcessEventRequestForAllEvents() throws InterruptedException {
        final Vector<ITmfEvent> requestedEvents = new Vector<>();

        final TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BIG_BANG, TmfTimestamp.BIG_CRUNCH);
        final TmfEventRequest request = new TmfEventRequest(ITmfEvent.class,
                range, 0, NB_EVENTS, ExecutionType.FOREGROUND) {
            @Override
            public void handleData(final ITmfEvent event) {
                super.handleData(event);
                requestedEvents.add(event);
            }
        };
        final ITmfEventProvider[] providers = TmfProviderManager.getProviders(ITmfEvent.class, TmfTraceStub.class);
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

    @Test
    public void testProcessEventRequestForNbEvents() throws InterruptedException {
        final int nbEvents  = 1000;
        final Vector<ITmfEvent> requestedEvents = new Vector<>();

        final TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BIG_BANG, TmfTimestamp.BIG_CRUNCH);
        final TmfEventRequest request = new TmfEventRequest(ITmfEvent.class,
                range, 0, nbEvents, ExecutionType.FOREGROUND) {
            @Override
            public void handleData(final ITmfEvent event) {
                super.handleData(event);
                requestedEvents.add(event);
            }
        };
        final ITmfEventProvider[] providers = TmfProviderManager.getProviders(ITmfEvent.class, TmfTraceStub.class);
        providers[0].sendRequest(request);
        request.waitForCompletion();

        assertEquals("nbEvents", nbEvents, requestedEvents.size());
        assertTrue("isCompleted",  request.isCompleted());
        assertFalse("isCancelled", request.isCancelled());

        // Ensure that we have distinct events.
        // Don't go overboard: we are not validating the stub!
        for (int i = 0; i < nbEvents; i++) {
            assertEquals("Distinct events", i + 1, requestedEvents.get(i).getTimestamp().getValue());
        }
    }

    @Test
    public void testProcessEventRequestForSomeEvents() throws InterruptedException {
        final long startTime = 100;
        final int nbEvents  = 1000;
        final Vector<ITmfEvent> requestedEvents = new Vector<>();

        final TmfTimeRange range = new TmfTimeRange(TmfTimestamp.create(startTime, SCALE), TmfTimestamp.BIG_CRUNCH);
        final TmfEventRequest request = new TmfEventRequest(ITmfEvent.class,
                range, 0, nbEvents, ExecutionType.FOREGROUND) {
            @Override
            public void handleData(final ITmfEvent event) {
                super.handleData(event);
                requestedEvents.add(event);
            }
        };
        final ITmfEventProvider[] providers = TmfProviderManager.getProviders(ITmfEvent.class, TmfTraceStub.class);
        providers[0].sendRequest(request);
        request.waitForCompletion();

        assertEquals("nbEvents", nbEvents, requestedEvents.size());
        assertTrue("isCompleted",  request.isCompleted());
        assertFalse("isCancelled", request.isCancelled());

        // Ensure that we have distinct events.
        // Don't go overboard: we are not validating the stub!
        for (int i = 0; i < nbEvents; i++) {
            assertEquals("Distinct events", startTime + i, requestedEvents.get(i).getTimestamp().getValue());
        }
    }

    @Test
    public void testProcessEventRequestForOtherEvents() throws InterruptedException {
        final int startIndex = 99;
        final long startTime = 100;
        final int nbEvents  = 1000;
        final Vector<ITmfEvent> requestedEvents = new Vector<>();

        final TmfTimeRange range = new TmfTimeRange(TmfTimestamp.create(startTime, SCALE), TmfTimestamp.BIG_CRUNCH);
        final TmfEventRequest request = new TmfEventRequest(ITmfEvent.class,
                range, startIndex, nbEvents, ExecutionType.FOREGROUND) {
            @Override
            public void handleData(final ITmfEvent event) {
                super.handleData(event);
                requestedEvents.add(event);
            }
        };
        final ITmfEventProvider[] providers = TmfProviderManager.getProviders(ITmfEvent.class, TmfTraceStub.class);
        providers[0].sendRequest(request);
        request.waitForCompletion();

        assertEquals("nbEvents", nbEvents, requestedEvents.size());
        assertTrue("isCompleted",  request.isCompleted());
        assertFalse("isCancelled", request.isCancelled());

        // Ensure that we have distinct events.
        // Don't go overboard: we are not validating the stub!
        for (int i = 0; i < nbEvents; i++) {
            assertEquals("Distinct events", startTime + i, requestedEvents.get(i).getTimestamp().getValue());
        }
    }

    @Test
    public void testProcessDataRequestForSomeEvents() throws InterruptedException {
        final int startIndex = 100;
        final int nbEvents  = 1000;
        final Vector<ITmfEvent> requestedEvents = new Vector<>();

        final TmfEventRequest request = new TmfEventRequest(ITmfEvent.class,
                TmfTimeRange.ETERNITY,
                startIndex,
                nbEvents,
                TmfEventRequest.ExecutionType.FOREGROUND) {
            @Override
            public void handleData(final ITmfEvent event) {
                super.handleData(event);
                requestedEvents.add(event);
            }
        };
        final ITmfEventProvider[] providers = TmfProviderManager.getProviders(ITmfEvent.class, TmfTraceStub.class);
        providers[0].sendRequest(request);
        request.waitForCompletion();

        assertEquals("nbEvents", nbEvents, requestedEvents.size());
        assertTrue("isCompleted",  request.isCompleted());
        assertFalse("isCancelled", request.isCancelled());

        // Ensure that we have distinct events.
        // Don't go overboard: we are not validating the stub!
        for (int i = 0; i < nbEvents; i++) {
            assertEquals("Distinct events", startIndex + 1 + i, requestedEvents.get(i).getTimestamp().getValue());
        }
    }

    // ------------------------------------------------------------------------
    // cancel
    // ------------------------------------------------------------------------

    @Test
    public void testCancel() throws InterruptedException {
        final int limit = 500;
        final Vector<ITmfEvent> requestedEvents = new Vector<>();

        final TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BIG_BANG, TmfTimestamp.BIG_CRUNCH);
        final TmfEventRequest request = new TmfEventRequest(ITmfEvent.class,
                range, 0, NB_EVENTS, ExecutionType.FOREGROUND) {
            int nbRead = 0;

            @Override
            public void handleData(final ITmfEvent event) {
                super.handleData(event);
                requestedEvents.add(event);
                if (++nbRead == limit) {
                    cancel();
                }
            }
        };
        final ITmfEventProvider[] providers = TmfProviderManager.getProviders(ITmfEvent.class, TmfTraceStub.class);
        providers[0].sendRequest(request);
        request.waitForCompletion();

        assertEquals("nbEvents",  limit, requestedEvents.size());
        assertTrue("isCompleted", request.isCompleted());
        assertTrue("isCancelled", request.isCancelled());
    }

    // ------------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------------

    @Test
    public void testDefaultTmfTraceStub() {
        assertFalse ("Open trace", fTrace == null);
        assertEquals("getEventType",  ITmfEvent.class, fTrace.getEventType());
        assertNull  ("getResource", fTrace.getResource());
        assertEquals("getStreamingInterval", 0, fTrace.getStreamingInterval());
        assertEquals("getName", TEST_TRACE.getPath(), fTrace.getName());

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

    // ------------------------------------------------------------------------
    // getInitialRangeOffset, getCurrentRange, getCurrentTime
    // ------------------------------------------------------------------------

    @Test
    public void testCurrentTimeValues() throws TmfTraceException {

        TmfTraceStub trace = null;
        File testfile = null;
        try {
            final URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(TEST_TRACE.getFullPath()), null);
            testfile = new File(FileLocator.toFileURL(location).toURI());
            trace = new TmfTraceStub(testfile.toURI().getPath(), ITmfTrace.DEFAULT_TRACE_CACHE_SIZE, false, null);
            // verify initial values
            ITmfTimestamp defaultInitRange = TmfTimestamp.fromNanos(DEFAULT_INITIAL_OFFSET_VALUE);
            assertEquals("getInitialRangeOffset", defaultInitRange, trace.getInitialRangeOffset());
            trace.setInitialRangeOffset(TmfTimestamp.fromMillis(5));
            trace.indexTrace(true);

            ITmfTimestamp initRange = TmfTimestamp.fromMillis(5);
            assertEquals("getInitialRangeOffset", initRange, trace.getInitialRangeOffset());

            trace.dispose();
        } catch (final URISyntaxException e) {
            fail("URISyntaxException");
        } catch (final IOException e) {
            fail("IOException");
        }
    }

    // ------------------------------------------------------------------------
    // Other getters
    // ------------------------------------------------------------------------

    /**
     * Run the String getHostId() method test
     */
    @Test
    public void testTraceHostId() {
        String a = fTrace.getHostId();
        assertEquals("A-Test-10K", a);
    }

    /**
     * Test the {@link TmfTrace#getEventAspects} method.
     */
    @Test
    public void testGetEventAspects() {
        Iterable<ITmfEventAspect<?>> aspects = fTrace.getEventAspects();
        for (ITmfEventAspect<?> aspect : aspects) {
            assertTrue(TmfTrace.BASE_ASPECTS.contains(aspect));
        }
    }
}
