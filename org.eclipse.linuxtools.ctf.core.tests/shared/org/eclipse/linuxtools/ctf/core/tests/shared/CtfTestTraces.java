/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.tests.shared;

import java.io.File;

import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;

/**
 * Here are the definitions common to all the CTF parser tests.
 *
 * @author alexmont
 */
public abstract class CtfTestTraces {

    /*
     * Path to test traces. Make sure you run the traces/get-traces.sh script
     * first!
     */
    private static final String[] testTracePaths = {
        "../org.eclipse.linuxtools.ctf.core.tests/traces/kernel",
        "../org.eclipse.linuxtools.ctf.core.tests/traces/trace2"
    };

    private static CTFTrace[] testTraces = new CTFTrace[testTracePaths.length];
    private static CTFTrace[] testTracesFromFile = new CTFTrace[testTracePaths.length];

    private static final File testTraceFile1 = new File(testTracePaths[0] + "/channel0_0");

    private static final File emptyFile = new File("");
    private static CTFTrace emptyTrace = null;

    /**
     * Return an empty file (new File("");)
     *
     * @return An empty file
     */
    public static File getEmptyFile() {
        return emptyFile;
    }

    /**
     * Return a file in test trace #1 (channel0_0).
     *
     * Make sure {@link #tracesExist()} before calling this!
     *
     * @return A file in a test trace
     */
    public static File getTraceFile(){
        return testTraceFile1;
    }

    /**
     * Return a trace out of an empty file (new CTFTrace("");)
     *
     * @return An empty trace
     */
    public static CTFTrace getEmptyTrace() {
        if (emptyTrace == null) {
            try {
                emptyTrace = new CTFTrace("");
            } catch (CTFReaderException e) {
                /* Should always work... */
                throw new RuntimeException(e);
            }
        }
        return emptyTrace;
    }

    /**
     * Get a CTFTrace reference to the given test trace.
     *
     * Make sure {@link #tracesExist()} before calling this!
     *
     * @param idx
     *            The index of the trace among all the available ones
     * @return Reference to test trace #1
     * @throws CTFReaderException
     *             If the trace cannot be found
     */
    public static CTFTrace getTestTrace(int idx) throws CTFReaderException {
        if (testTraces[idx] == null) {
            testTraces[idx] = new CTFTrace(testTracePaths[idx]);
        }
        return testTraces[idx];
    }

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
        return testTracePaths[idx];
    }

    /**
     * Same as {@link #getTestTrace}, except the CTFTrace is create from the
     * File object and not the path.
     *
     * Make sure {@link #tracesExist()} before calling this!
     *
     * @param idx
     *            The index of the trace among all the available ones
     * @return Reference to test trace #1
     */
    public static CTFTrace getTestTraceFromFile(int idx) {
        if (testTracesFromFile[idx] == null) {
            try {
                testTracesFromFile[idx] = new CTFTrace(new File(testTracePaths[idx]));
            } catch (CTFReaderException e) {
                /* This trace should exist */
                throw new RuntimeException(e);
            }
        }
        return testTracesFromFile[idx];
    }

    /**
     * Check if the test traces are present in the tree. If not, you can get
     * them by running traces/get-traces.sh or traces/get-traces.xml
     *
     * @return True if *all* the test files could be found, false otherwise.
     */
    public static boolean tracesExist() {
        for (int i = 0; i < testTracePaths.length; i++) {
            if (!traceExists(i)) {
                return false;
            }
        }
        return true;
    }

    private static boolean traceExists(int idx) {
        if (testTraces[idx] != null) {
            return true;
        }
        try {
            getTestTrace(idx);
        } catch (CTFReaderException e) {
            return false;
        }
        return true;
    }
}
