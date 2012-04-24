/*******************************************************************************
 * Copyright (c) 2009, 2010, 20112 Ericsson
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
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.tests.TmfCoreTestPlugin;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfCheckpoint;
import org.eclipse.linuxtools.tmf.core.trace.TmfCheckpointIndexer;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.TmfTraceStub;

/**
 * <b><u>TmfTraceTest</u></b>
 * <p>
 * Test suite for the TmfTrace class.
 */
@SuppressWarnings("nls")
public class TmfCheckpointIndexTest extends TestCase {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private static final String DIRECTORY   = "testfiles";
    private static final String TEST_STREAM = "A-Test-10K";
    private static final int    BLOCK_SIZE  = 500;
    private static final int    NB_EVENTS   = 10000;
    private static TestTrace    fTrace      = null;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    public TmfCheckpointIndexTest(final String name) throws Exception {
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
    // Helper classes
    // ------------------------------------------------------------------------

    private class TestIndexer extends TmfCheckpointIndexer<ITmfTrace<ITmfEvent>> {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public TestIndexer(TestTrace testTrace) {
            super((ITmfTrace) testTrace);
        }
        public Vector<TmfCheckpoint> getCheckpoints() {
            return fTraceIndex;
        }
    }

    private class TestTrace extends TmfTraceStub {
        public TestTrace(String path, int blockSize) throws TmfTraceException {
            super(path, blockSize);
            fIndexer = new TestIndexer(this);
        }
        public TestIndexer getIndexer() {
            return (TestIndexer) fIndexer;
        }
    }

    // ------------------------------------------------------------------------
    // Helper functions
    // ------------------------------------------------------------------------

    private TestTrace setupTrace(final String path) {
        if (fTrace == null) {
            try {
                final URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(path), null);
                final File test = new File(FileLocator.toFileURL(location).toURI());
                fTrace = new TestTrace(test.toURI().getPath(), BLOCK_SIZE);
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
    // Verify checkpoints
    // ------------------------------------------------------------------------

    public void testTmfTraceIndexing() throws Exception {
        assertEquals("getCacheSize",   BLOCK_SIZE, fTrace.getCacheSize());
        assertEquals("getTraceSize",   NB_EVENTS,  fTrace.getNbEvents());
        assertEquals("getRange-start", 1,          fTrace.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end",   NB_EVENTS,  fTrace.getTimeRange().getEndTime().getValue());
        assertEquals("getStartTime",   1,          fTrace.getStartTime().getValue());
        assertEquals("getEndTime",     NB_EVENTS,  fTrace.getEndTime().getValue());

        Vector<TmfCheckpoint> checkpoints = fTrace.getIndexer().getCheckpoints();
        int pageSize = fTrace.getCacheSize();
        assertTrue("Checkpoints exist",  checkpoints != null);

        // Validate that each checkpoint points to the right event
        for (int i = 0; i < checkpoints.size(); i++) {
            TmfCheckpoint checkpoint = checkpoints.get(i);
            TmfContext context = new TmfContext(checkpoint.getLocation(), i * pageSize);
            ITmfEvent event = fTrace.parseEvent(context);
            assertTrue(context.getRank() == i * pageSize);
            assertTrue((checkpoint.getTimestamp().compareTo(event.getTimestamp(), false) == 0));
        }
    }

}