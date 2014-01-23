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

package org.eclipse.linuxtools.ctf.core.tests.ctftestsuite;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Parameterized test class running the CTF Test Suite
 *
 * (from https://github.com/efficios/ctf-testsuite).
 *
 * @author Alexandre Montplaisir
 */
@RunWith(Parameterized.class)
public class CtfTestSuiteTests {

    /** Time-out tests after 10 seconds. */
    @Rule
    public TestRule globalTimeout = new Timeout(10000);

    private static final String BASE_PATH = "traces/ctf-testsuite/tests/1.8/";

    /**
     * Test we know are currently failing. Ignore them so we can at least run
     * the others.
     *
     * TODO Actually fix them!
     */
    private static final String[] IGNORED_TESTS = {
            "regression/metadata/pass/sequence-typedef-length",
            "regression/metadata/pass/array-of-struct"
    };

    private final String fTracePath;
    private final boolean fExpectSuccess;

    // ------------------------------------------------------------------------
    // Methods for the Parametrized runner
    // ------------------------------------------------------------------------

    /**
     * Get the existing trace paths in the CTF-Testsuite git tree.
     *
     * @return The list of CTF traces (directories) to test
     */
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> getTracePaths() {
        final List<Object[]> dirs = new LinkedList<>();

        addDirsFrom(dirs, BASE_PATH + "fuzzing/metadata/fail", false);
        addDirsFrom(dirs, BASE_PATH + "fuzzing/metadata/pass", true);
        addDirsFrom(dirs, BASE_PATH + "fuzzing/stream/fail", false);
        addDirsFrom(dirs, BASE_PATH + "fuzzing/stream/pass", true);

        addDirsFrom(dirs, BASE_PATH + "regression/metadata/fail", false);
        addDirsFrom(dirs, BASE_PATH + "regression/metadata/pass", true);
        addDirsFrom(dirs, BASE_PATH + "regression/stream/fail", false);
        addDirsFrom(dirs, BASE_PATH + "regression/stream/pass", true);

        addDirsFrom(dirs, BASE_PATH + "stress/metadata/fail", false);
        addDirsFrom(dirs, BASE_PATH + "stress/metadata/pass", true);
        addDirsFrom(dirs, BASE_PATH + "stress/stream/fail", false);
        addDirsFrom(dirs, BASE_PATH + "stress/stream/pass", true);

        return dirs;
    }

    private static void addDirsFrom(List<Object[]> dirs, String path, boolean expectSuccess) {
        File[] traceDirs = (new File(path)).listFiles();
        if (traceDirs == null) {
            return;
        }
        for (File traceDir : traceDirs) {
            /* Skip the "run.sh" files and blacklisted tests */
            if (!traceDir.isDirectory() || testIsBlacklisted(traceDir.getPath())) {
                continue;
            }

            /* Add this test case to the list of tests to run */
            Object array[] = new Object[] { traceDir.getPath(), expectSuccess };
            dirs.add(array);
        }
    }

    private static boolean testIsBlacklisted(String fullPath) {
        for (String ignoredTest : IGNORED_TESTS) {
            if (fullPath.contains(ignoredTest)) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------------
    // Test constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor for the parametrized tests
     *
     * @param tracePath
     *            The complete path to the trace to test
     * @param expectSuccess
     *            Should this trace parse successfully, or not.
     */
    public CtfTestSuiteTests(String tracePath, boolean expectSuccess) {
        fTracePath = tracePath;
        fExpectSuccess = expectSuccess;
    }

    // ------------------------------------------------------------------------
    // Test methods
    // ------------------------------------------------------------------------

    /**
     * Test opening and reading the trace
     */
    @Test
    public void testTrace() {
        CTFTrace trace = null;
        CTFTraceReader reader = null;
        try {
            /* Instantiate the trace object (which implies parsing the metadata) */
            trace = new CTFTrace(fTracePath);

            /* Read the trace until the end */
            reader = new CTFTraceReader(trace);
            reader.getCurrentEventDef();
            while (reader.advance()) {
                assertNotNull(reader.getCurrentEventDef());
            }

            checkIfWeShoudlSucceed();
        } catch (CTFReaderException e) {
            checkIfWeShouldFail(e);
        } catch (OutOfMemoryError e) {
            checkIfWeShouldFail(e);
        } finally {
            if (reader != null) {
                reader.dispose();
            }
            if (trace != null) {
                trace.dispose();
            }

        }
    }

    private void checkIfWeShoudlSucceed() {
        if (!fExpectSuccess) {
            fail("Trace was expected to fail parsing: " + fTracePath);
        }
    }

    private void checkIfWeShouldFail(Throwable e) {
        if (fExpectSuccess) {
            fail("Trace was expected to succeed, but failed parsing: " +
                    fTracePath + " (" + e.getMessage() + ")");
        }
    }
}
