/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.shared;

import java.io.File;

import org.eclipse.linuxtools.ctf.core.tests.shared.CtfTestTraces;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;

/**
 * Definitions used by all tests using CTF-TMF trace files.
 *
 * To run these tests, you will first need to run the "get-traces.sh" script
 * located under lttng/org.eclipse.linuxtools.ctf.core.tests/traces/ .
 *
 * @author Alexandre Montplaisir
 */
public final class CtfTmfTestTraces {

    private CtfTmfTestTraces() {}

    private static final File emptyFile = new File("");
    private static CtfTmfTrace emptyTrace = new CtfTmfTrace();

    private static CtfTmfTrace[] testTraces = new CtfTmfTrace[3];

    /**
     * Get an empty File (new File("");)
     *
     * @return An empty file
     */
    public static File getEmptyFile() {
        return emptyFile;
    }

    /**
     * Get an empty CtfTmfTrace (new CtfTmfTrace();)
     *
     * @return An empty trace
     */
    public static CtfTmfTrace getEmptyTrace() {
        return emptyTrace;
    }

    /**
     * Get a reference to the test trace used for the kernel event handler unit
     * tests.
     *
     * Make sure you call {@link #tracesExist()} before calling this!
     *
     * @param idx
     *            The index of the test trace you want
     * @return A CtfTmfTrace reference to the test trace
     */
    public synchronized static CtfTmfTrace getTestTrace(int idx) {
        if (testTraces[idx] == null) {
            String tracePath = CtfTestTraces.getTestTracePath(idx);
            testTraces[idx] = new CtfTmfTrace();
            try {
                testTraces[idx].initTrace(null, tracePath, CtfTmfEvent.class);
            } catch (TmfTraceException e) {
                /* Should not happen if tracesExist() passed */
                testTraces[idx] = null;
                throw new RuntimeException(e);
            }
        }
        return testTraces[idx];
    }

    // ------------------------------------------------------------------------
    // Wrappers around direct CtfTestTraces methods
    // ------------------------------------------------------------------------

    /**
     * Get the (string) path to a given test trace.
     *
     * You should call {@link #tracesExist()} before calling this if you are
     * going to use this trace for real.
     *
     * @param idx
     *            The index of the trace among all the available ones
     * @return The path to the test trace
     */
    public static String getTestTracePath(int idx) {
        return CtfTestTraces.getTestTracePath(idx);
    }

    /**
     * Check if the test traces are present before trying to open them.
     *
     * This should be called in unit tests within a asssumeTrue() call, to skip
     * the test/test-class if the traces are not available.
     *
     * @return True if the trace is available, false if not
     */
    public static boolean tracesExist() {
        return CtfTestTraces.tracesExist();
    }

}
