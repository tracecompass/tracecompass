/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
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
import java.util.Vector;

import junit.framework.TestCase;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.tmf.core.component.ITmfDataProvider;
import org.eclipse.linuxtools.tmf.core.component.TmfProviderManager;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.tests.TmfCoreTestPlugin;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfCheckpoint;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.TmfTraceStub;

/**
 * <b><u>TmfTraceTest</u></b>
 * <p>
 * Test suite for the TmfTrace class.
 */
@SuppressWarnings("nls")
public class TmfTraceTest extends TestCase {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private static final String DIRECTORY   = "testfiles";
    private static final String TEST_STREAM = "A-Test-10K";
    private static final int    BLOCK_SIZE  = 500;
    private static final int    NB_EVENTS   = 10000;
    private static TmfTraceStub fTrace      = null;

    private static byte SCALE = (byte) -3;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    public TmfTraceTest(final String name) throws Exception {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        fTrace = setupTrace(DIRECTORY + File.separator + TEST_STREAM);
        // Dummy request to force the trace indexing
        final TmfEventRequest<TmfEvent> request = new TmfEventRequest<TmfEvent>(TmfEvent.class) {
            @Override
            public void handleData(final TmfEvent event) {
                super.handleData(event);
            }
        };
        fTrace.sendRequest(request);
        request.waitForCompletion();
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

    private TmfTraceStub setupTrace(final String path) {
        if (fTrace == null)
            try {
                final URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(path), null);
                final File test = new File(FileLocator.toFileURL(location).toURI());
                fTrace = new TmfTraceStub(test.toURI().getPath(), BLOCK_SIZE, false);
            } catch (final URISyntaxException e) {
                e.printStackTrace();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        return fTrace;
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public void testTmfTraceDefault() throws Exception {
        TmfTraceStub trace = null;
        File testfile = null;
        try {
            final URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(DIRECTORY + File.separator + TEST_STREAM), null);
            testfile = new File(FileLocator.toFileURL(location).toURI());
            trace = new TmfTraceStub(testfile.toURI().getPath());
        } catch (final URISyntaxException e) {
            fail("URISyntaxException");
        } catch (final IOException e) {
            fail("IOException");
        }
        assertTrue  ("Open trace",   trace != null);
        assertEquals("getType",      TmfEvent.class, trace.getType());
        assertEquals("getPath",      testfile.toURI().getPath(), trace.getPath());
        assertEquals("getName",      TEST_STREAM, trace.getName());
        assertEquals("getCacheSize", TmfTrace.DEFAULT_INDEX_PAGE_SIZE, trace.getIndexPageSize());
    }

    public void testTmfTraceDefaultCacheSize() throws Exception {
        TmfTraceStub trace = null;
        File testfile = null;
        try {
            final URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(DIRECTORY + File.separator + TEST_STREAM), null);
            testfile = new File(FileLocator.toFileURL(location).toURI());
            trace = new TmfTraceStub(testfile.toURI().getPath(), 0);
        } catch (final URISyntaxException e) {
            fail("URISyntaxException");
        } catch (final IOException e) {
            fail("IOException");
        }
        assertTrue  ("Open trace",   trace != null);
        assertEquals("getType",      TmfEvent.class, trace.getType());
        assertEquals("getPath",      testfile.toURI().getPath(), trace.getPath());
        assertEquals("getName",      TEST_STREAM, trace.getName());
        assertEquals("getCacheSize", TmfTrace.DEFAULT_INDEX_PAGE_SIZE, trace.getIndexPageSize());
    }

    public void testTmfTrace() throws Exception {
        assertEquals("getType",      TmfEvent.class, fTrace.getType());
        assertEquals("getName",      TEST_STREAM,    fTrace.getName());
        assertEquals("getCacheSize", BLOCK_SIZE,     fTrace.getIndexPageSize());
    }

    //    public void testClone() throws Exception {
    //    	TmfTraceStub trace = fTrace.clone();
    //		assertEquals("getType",      TmfEvent.class,        trace.getType());
    //		assertEquals("getPath",      fTrace.getPath(),      trace.getPath());
    //		assertEquals("getName",      TEST_STREAM,           trace.getName());
    //		assertEquals("getCacheSize", BLOCK_SIZE,            trace.getIndexPageSize());
    //		assertEquals("getTimeRange", fTrace.getTimeRange(), trace.getTimeRange());
    //    }

    // ------------------------------------------------------------------------
    // Get/Set time range
    // ------------------------------------------------------------------------

    public void testSetTimeRange() throws Exception {
        final TmfTraceStub trace = new TmfTraceStub(fTrace);

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

    public void testSetStartTime() throws Exception {
        final TmfTraceStub trace = new TmfTraceStub(fTrace);

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

    public void testSetEndTime() throws Exception {
        final TmfTraceStub trace = new TmfTraceStub(fTrace);

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

    // ------------------------------------------------------------------------
    // Verify checkpoints
    // ------------------------------------------------------------------------

    public void testTmfTraceIndexing() throws Exception {
        assertEquals("getCacheSize",   BLOCK_SIZE, fTrace.getIndexPageSize());
        assertEquals("getTraceSize",   NB_EVENTS,  fTrace.getNbEvents());
        assertEquals("getRange-start", 1,          fTrace.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end",   NB_EVENTS,  fTrace.getTimeRange().getEndTime().getValue());
        assertEquals("getStartTime",   1,          fTrace.getStartTime().getValue());
        assertEquals("getEndTime",     NB_EVENTS,  fTrace.getEndTime().getValue());

        final Vector<TmfCheckpoint> checkpoints = fTrace.getCheckpoints();
        final int pageSize = fTrace.getIndexPageSize();
        assertTrue("Checkpoints exist",  checkpoints != null);

        // Validate that each checkpoint points to the right event
        for (int i = 0; i < checkpoints.size(); i++) {
            final TmfCheckpoint checkpoint = checkpoints.get(i);
            final TmfContext context = new TmfContext(checkpoint.getLocation(), i * pageSize);
            final ITmfEvent event = fTrace.parseEvent(context);
            assertTrue(context.getRank() == i * pageSize);
            assertTrue((checkpoint.getTimestamp().compareTo(event.getTimestamp(), false) == 0));
        }
    }

    // ------------------------------------------------------------------------
    // parseEvent - make sure parseEvent doesn't update the context
    // ------------------------------------------------------------------------

    public void testParseEvent() throws Exception {

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
            event = fTrace.getNextEvent(context);
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
    // getNextEvent - updates the context
    // ------------------------------------------------------------------------

    public void testGetNextEvent() throws Exception {

        final int NB_READS = 20;

        // On lower bound, returns the first event (ts = 1)
        final ITmfContext context = fTrace.seekEvent(new TmfTimestamp(0, SCALE, 0));

        // Read NB_EVENTS
        ITmfEvent event;
        for (int i = 0; i < NB_READS; i++) {
            event = fTrace.getNextEvent(context);
            assertEquals("Event timestamp", i + 1, event.getTimestamp().getValue());
            assertEquals("Event rank", i + 1, context.getRank());
        }

        // Make sure we stay positioned
        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", NB_READS + 1, event.getTimestamp().getValue());
        assertEquals("Event rank", NB_READS, context.getRank());
    }

    // ------------------------------------------------------------------------
    // seekLocation
    // Note: seekLocation() does not reliably set the rank
    // ------------------------------------------------------------------------

    public void testSeekLocationOnCacheBoundary() throws Exception {

        // Position trace at event rank 0
        TmfContext context = fTrace.seekLocation(null);
        ITmfEvent event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 0, context.getRank());

        context = fTrace.seekLocation(context.getLocation());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        // Position trace at event rank 1000
        ITmfContext tmpContext = fTrace.seekEvent(new TmfTimestamp(1001, SCALE, 0));
        context = fTrace.seekLocation(tmpContext.getLocation());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1001, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1001, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        // Position trace at event rank 4000
        tmpContext = fTrace.seekEvent(new TmfTimestamp(4001, SCALE, 0));
        context = fTrace.seekLocation(tmpContext.getLocation());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 4001, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 4001, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());
    }

    public void testSeekLocationNotOnCacheBoundary() throws Exception {

        // Position trace at event rank 9
        ITmfContext tmpContext = fTrace.seekEvent(new TmfTimestamp(10, SCALE, 0));
        TmfContext context = fTrace.seekLocation(tmpContext.getLocation());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        ITmfEvent event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 10, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 10, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        // Position trace at event rank 999
        tmpContext = fTrace.seekEvent(new TmfTimestamp(1000, SCALE, 0));
        context = fTrace.seekLocation(tmpContext.getLocation());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1000, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1000, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        // Position trace at event rank 1001
        tmpContext = fTrace.seekEvent(new TmfTimestamp(1002, SCALE, 0));
        context = fTrace.seekLocation(tmpContext.getLocation());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1002, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1002, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        // Position trace at event rank 4500
        tmpContext = fTrace.seekEvent(new TmfTimestamp(4501, SCALE, 0));
        context = fTrace.seekLocation(tmpContext.getLocation());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 4501, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 4501, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());
    }

    public void testSeekLocationOutOfScope() throws Exception {

        // Position trace at beginning
        ITmfContext tmpContext = fTrace.seekLocation(null);
        ITmfContext context = fTrace.seekLocation(tmpContext.getLocation());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        ITmfEvent event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        // Position trace at event passed the end
        tmpContext = fTrace.seekEvent(new TmfTimestamp(NB_EVENTS + 1, SCALE, 0));
        context = fTrace.seekLocation(tmpContext.getLocation());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", null, event);
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

        event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", null, event);
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());
    }

    // ------------------------------------------------------------------------
    // seekEvent on timestamp
    // ------------------------------------------------------------------------

    public void testSeekEventOnTimestampOnCacheBoundary() throws Exception {

        // Position trace at event rank 0
        ITmfContext context = fTrace.seekEvent(new TmfTimestamp(1, SCALE, 0));
        assertEquals("Event rank", 0, context.getRank());

        ITmfEvent event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 0, context.getRank());

        event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 1, context.getRank());

        // Position trace at event rank 1000
        context = fTrace.seekEvent(new TmfTimestamp(1001, SCALE, 0));
        assertEquals("Event rank", 1000, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1001, event.getTimestamp().getValue());
        assertEquals("Event rank", 1000, context.getRank());

        event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1001, event.getTimestamp().getValue());
        assertEquals("Event rank", 1001, context.getRank());

        // Position trace at event rank 4000
        context = fTrace.seekEvent(new TmfTimestamp(4001, SCALE, 0));
        assertEquals("Event rank", 4000, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 4001, event.getTimestamp().getValue());
        assertEquals("Event rank", 4000, context.getRank());

        event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 4001, event.getTimestamp().getValue());
        assertEquals("Event rank", 4001, context.getRank());
    }

    public void testSeekEventOnTimestampNotOnCacheBoundary() throws Exception {

        // Position trace at event rank 1
        ITmfContext context = fTrace.seekEvent(new TmfTimestamp(2, SCALE, 0));
        assertEquals("Event rank", 1, context.getRank());

        ITmfEvent event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 2, event.getTimestamp().getValue());
        assertEquals("Event rank", 1, context.getRank());

        event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 2, event.getTimestamp().getValue());
        assertEquals("Event rank", 2, context.getRank());

        // Position trace at event rank 9
        context = fTrace.seekEvent(new TmfTimestamp(10, SCALE, 0));
        assertEquals("Event rank", 9, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 10, event.getTimestamp().getValue());
        assertEquals("Event rank", 9, context.getRank());

        event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 10, event.getTimestamp().getValue());
        assertEquals("Event rank", 10, context.getRank());

        // Position trace at event rank 999
        context = fTrace.seekEvent(new TmfTimestamp(1000, SCALE, 0));
        assertEquals("Event rank", 999, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1000, event.getTimestamp().getValue());
        assertEquals("Event rank", 999, context.getRank());

        event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1000, event.getTimestamp().getValue());
        assertEquals("Event rank", 1000, context.getRank());

        // Position trace at event rank 1001
        context = fTrace.seekEvent(new TmfTimestamp(1002, SCALE, 0));
        assertEquals("Event rank", 1001, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1002, event.getTimestamp().getValue());
        assertEquals("Event rank", 1001, context.getRank());

        event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1002, event.getTimestamp().getValue());
        assertEquals("Event rank", 1002, context.getRank());

        // Position trace at event rank 4500
        context = fTrace.seekEvent(new TmfTimestamp(4501, SCALE, 0));
        assertEquals("Event rank", 4500, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 4501, event.getTimestamp().getValue());
        assertEquals("Event rank", 4500, context.getRank());

        event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 4501, event.getTimestamp().getValue());
        assertEquals("Event rank", 4501, context.getRank());
    }

    public void testSeekEventOnTimestampOutOfScope() throws Exception {

        // Position trace at beginning
        ITmfContext context = fTrace.seekEvent(new TmfTimestamp(-1, SCALE, 0));
        assertEquals("Event rank", 0, context.getRank());

        ITmfEvent event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 0, context.getRank());

        event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 1, context.getRank());

        // Position trace at event passed the end
        context = fTrace.seekEvent(new TmfTimestamp(NB_EVENTS + 1, SCALE, 0));
        assertEquals("Event rank", NB_EVENTS, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", null, event);
        assertEquals("Event rank", NB_EVENTS, context.getRank());

        event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", null, event);
        assertEquals("Event rank", NB_EVENTS, context.getRank());
    }

    // ------------------------------------------------------------------------
    // seekEvent on rank
    // ------------------------------------------------------------------------

    public void testSeekOnRankOnCacheBoundary() throws Exception {

        // On lower bound, returns the first event (ts = 1)
        ITmfContext context = fTrace.seekEvent(0);
        assertEquals("Event rank", 0, context.getRank());

        ITmfEvent event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 0, context.getRank());

        event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 1, context.getRank());

        // Position trace at event rank 1000
        context = fTrace.seekEvent(1000);
        assertEquals("Event rank", 1000, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1001, event.getTimestamp().getValue());
        assertEquals("Event rank", 1000, context.getRank());

        event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1001, event.getTimestamp().getValue());
        assertEquals("Event rank", 1001, context.getRank());

        // Position trace at event rank 4000
        context = fTrace.seekEvent(4000);
        assertEquals("Event rank", 4000, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 4001, event.getTimestamp().getValue());
        assertEquals("Event rank", 4000, context.getRank());

        event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 4001, event.getTimestamp().getValue());
        assertEquals("Event rank", 4001, context.getRank());
    }

    public void testSeekOnRankNotOnCacheBoundary() throws Exception {

        // Position trace at event rank 9
        ITmfContext context = fTrace.seekEvent(9);
        assertEquals("Event rank", 9, context.getRank());

        ITmfEvent event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 10, event.getTimestamp().getValue());
        assertEquals("Event rank", 9, context.getRank());

        event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 10, event.getTimestamp().getValue());
        assertEquals("Event rank", 10, context.getRank());

        // Position trace at event rank 999
        context = fTrace.seekEvent(999);
        assertEquals("Event rank", 999, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1000, event.getTimestamp().getValue());
        assertEquals("Event rank", 999, context.getRank());

        event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1000, event.getTimestamp().getValue());
        assertEquals("Event rank", 1000, context.getRank());

        // Position trace at event rank 1001
        context = fTrace.seekEvent(1001);
        assertEquals("Event rank", 1001, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1002, event.getTimestamp().getValue());
        assertEquals("Event rank", 1001, context.getRank());

        event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1002, event.getTimestamp().getValue());
        assertEquals("Event rank", 1002, context.getRank());

        // Position trace at event rank 4500
        context = fTrace.seekEvent(4500);
        assertEquals("Event rank", 4500, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 4501, event.getTimestamp().getValue());
        assertEquals("Event rank", 4500, context.getRank());

        event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 4501, event.getTimestamp().getValue());
        assertEquals("Event rank", 4501, context.getRank());
    }

    public void testSeekEventOnRankOfScope() throws Exception {

        // Position trace at beginning
        ITmfContext context = fTrace.seekEvent(-1);
        assertEquals("Event rank", 0, context.getRank());

        ITmfEvent event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 0, context.getRank());

        event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 1, context.getRank());

        // Position trace at event passed the end
        context = fTrace.seekEvent(NB_EVENTS);
        assertEquals("Event rank", NB_EVENTS, context.getRank());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", null, event);
        assertEquals("Event rank", NB_EVENTS, context.getRank());

        event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", null, event);
        assertEquals("Event rank", NB_EVENTS, context.getRank());
    }

    // ------------------------------------------------------------------------
    // processRequest
    // ------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public void testProcessRequestForNbEvents() throws Exception {
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
        for (int i = 0; i < NB_EVENTS; i++)
            assertEquals("Distinct events", i+1, requestedEvents.get(i).getTimestamp().getValue());
    }

    @SuppressWarnings("unchecked")
    public void testProcessRequestForAllEvents() throws Exception {
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
        for (int i = 0; i < NB_EVENTS; i++)
            assertEquals("Distinct events", i+1, requestedEvents.get(i).getTimestamp().getValue());
    }

    // ------------------------------------------------------------------------
    // cancel
    // ------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public void testCancel() throws Exception {
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();

        final TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BIG_BANG, TmfTimestamp.BIG_CRUNCH);
        final TmfEventRequest<TmfEvent> request = new TmfEventRequest<TmfEvent>(TmfEvent.class, range, NB_EVENTS, BLOCK_SIZE) {
            int nbRead = 0;
            @Override
            public void handleData(final TmfEvent event) {
                super.handleData(event);
                requestedEvents.add(event);
                if (++nbRead == BLOCK_SIZE)
                    cancel();
            }
        };
        final ITmfDataProvider<TmfEvent>[] providers = (ITmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TmfTraceStub.class);
        providers[0].sendRequest(request);
        request.waitForCompletion();

        assertEquals("nbEvents",  BLOCK_SIZE, requestedEvents.size());
        assertTrue("isCompleted", request.isCompleted());
        assertTrue("isCancelled", request.isCancelled());
    }

}