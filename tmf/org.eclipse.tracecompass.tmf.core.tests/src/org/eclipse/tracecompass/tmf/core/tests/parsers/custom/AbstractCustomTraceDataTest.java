/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.parsers.custom;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Abstract class to test custom traces
 *
 * @author Geneviève Bastien
 */
public abstract class AbstractCustomTraceDataTest {

    /**
     * Time format use for event creation
     */
    protected static final String TIMESTAMP_FORMAT = "dd/MM/yyyy HH:mm:ss:SSS";

    /**
     * Block size used for the indexer
     */
    protected static final int BLOCK_SIZE = 100;

    /** The trace directory */
    protected static final String TRACE_DIRECTORY = TmfTraceManager.getTemporaryDirPath() + File.separator + "dummyTrace";


    /**
     * Interface to be implemented by concrete test cases to get the necessary
     * test data
     *
     * @author Geneviève Bastien
     */
    protected static interface ICustomTestData {
        /**
         * Get the trace for this test case
         *
         * @return The initialized trace
         * @throws IOException
         *             If an exception occurred while getting the trace
         * @throws TmfTraceException
         *             If an exception occurred while getting the trace
         */
        public ITmfTrace getTrace() throws IOException, TmfTraceException;

        /**
         * Validate the event for this test case. This method should contain the
         * necessary asserts.
         *
         * @param event
         *            The event to validate
         */
        public void validateEvent(ITmfEvent event);

        /**
         * Validate the event count. This method will be called after reading
         * the whole trace.
         *
         * @param eventCount
         *            The event count to validate
         */
        public void validateEventCount(int eventCount);
    }

    private final @NonNull ICustomTestData fTestData;
    private ITmfTrace fTrace;

    /**
     * Constructor
     *
     * @param data
     *            The custom trace test data
     */
    public AbstractCustomTraceDataTest(@NonNull ICustomTestData data) {
        fTestData = data;
    }

    /**
     * Setup the test
     * @throws Exception Exceptions that occurred during setup
     */
    @Before
    public void setUp() throws Exception {
        setupTrace();
    }

    private synchronized void setupTrace() throws Exception {
        File traceDirectory = new File(TRACE_DIRECTORY);
        if (traceDirectory.exists()) {
            traceDirectory.delete();
        }
        traceDirectory.mkdir();
        if (fTrace == null) {
            fTrace = fTestData.getTrace();
        }
    }

    /**
     * Tear down the test
     */
    @After
    public void tearDown() {
        String directory = TmfTraceManager.getSupplementaryFileDir(fTrace);
        try {
            fTrace.dispose();
            fTrace = null;
        } finally {
            File dir = new File(directory);
            if (dir.exists()) {
                File[] files = dir.listFiles();
                for (File file : files) {
                    file.delete();
                }
                dir.delete();
            }

            File trace = new File(TRACE_DIRECTORY);
            if (trace.exists()) {
                trace.delete();
            }
        }

    }

    /**
     * Test reading the events of the trace
     */
    @Test
    public void testReadingEvents() {
        ITmfTrace trace = fTrace;

        ITmfContext ctx = trace.seekEvent(0L);
        int eventCount = 0;
        ITmfEvent event = trace.getNext(ctx);
        while (event != null) {
            fTestData.validateEvent(event);
            eventCount++;
            event = trace.getNext(ctx);
        }
        fTestData.validateEventCount(eventCount);
    }

    /**
     * Test that the fast bound reading method returns the correct time stamps.
     */
    @Test
    public void testReadingBounds() {
        /* First, read the bounds without indexing. */
        ITmfTimestamp start = fTrace.readStart();
        ITmfTimestamp end = fTrace.readEnd();

        /*
         * Index the trace so that getStartTime and getEndTime return the
         * correct time stamps.
         */
        fTrace.indexTrace(true);

        /* Compare the TmfTrace bounds to the fast read bounds. */
        assertEquals(fTrace.getStartTime(), start);
        assertEquals(fTrace.getEndTime(), end);
    }

}
