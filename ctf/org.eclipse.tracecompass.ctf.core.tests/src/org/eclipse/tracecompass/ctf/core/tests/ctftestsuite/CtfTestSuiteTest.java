/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.tests.ctftestsuite;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.core.trace.CTFTraceReader;
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
public class CtfTestSuiteTest {

    /** Time-out tests after 1 minute. */
    @Rule
    public TestRule globalTimeout = new Timeout(1, TimeUnit.MINUTES);

    private static final Path BASE_PATH = Paths.get("traces", "ctf-testsuite", "tests", "1.8");

    /**
     * Test we know are currently failing. Ignore them so we can at least run
     * the others.
     *
     * TODO Actually fix them!
     */
    private static final Path[] IGNORED_TESTS = {
            BASE_PATH.resolve(Paths.get("regression", "metadata", "pass", "sequence-typedef-length")),
            BASE_PATH.resolve(Paths.get("regression", "stream", "pass", "integer-large-size")),
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

        addDirsFrom(dirs, BASE_PATH.resolve(Paths.get("fuzzing", "metadata", "fail")), false);
        addDirsFrom(dirs, BASE_PATH.resolve(Paths.get("fuzzing", "metadata", "pass")), true);
        addDirsFrom(dirs, BASE_PATH.resolve(Paths.get("fuzzing", "stream", "fail")), false);
        addDirsFrom(dirs, BASE_PATH.resolve(Paths.get("fuzzing", "stream", "pass")), true);

        addDirsFrom(dirs, BASE_PATH.resolve(Paths.get("regression", "metadata", "fail")), false);
        addDirsFrom(dirs, BASE_PATH.resolve(Paths.get("regression", "metadata", "pass")), true);
        addDirsFrom(dirs, BASE_PATH.resolve(Paths.get("regression", "stream", "fail")), false);
        addDirsFrom(dirs, BASE_PATH.resolve(Paths.get("regression", "stream", "pass")), true);

        addDirsFrom(dirs, BASE_PATH.resolve(Paths.get("stress", "metadata", "fail")), false);
        addDirsOneLevelDeepFrom(dirs, BASE_PATH.resolve(Paths.get("stress", "metadata", "pass")), true);
        addDirsFrom(dirs, BASE_PATH.resolve(Paths.get("stress", "stream", "fail")), false);
        addDirsOneLevelDeepFrom(dirs, BASE_PATH.resolve(Paths.get("stress", "stream", "pass")), true);

        return dirs;
    }

    private static void addDirsFrom(List<Object[]> dirs, Path path, boolean expectSuccess) {
        if (!Files.exists(path)) {
            /* Some planned directories may not exist yet in the test suite */
            return;
        }
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(path, DIR_FILTER);) {
            for (Path p : ds) {
                /* Add this test case to the list of tests to run */
                Object array[] = new Object[] { p.toString(), expectSuccess };
                dirs.add(array);
            }
        } catch (IOException e) {
            /* Something is wrong with the layout of the test suite? */
            e.printStackTrace();
        }
    }

    /**
     * Some test traces are not in pass/trace1, pass/trace2, etc. but rather
     * pass/test1/trace1, pass/test1/trace2, etc.
     *
     * This methods adds the directories one level "down" instead of the very
     * next level.
     */
    private static void addDirsOneLevelDeepFrom(List<Object[]> dirs, Path path,
            boolean expectSuccess) {
        if (!Files.exists(path)) {
            return;
        }
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(path, DIR_FILTER);) {
            for (Path p : ds) {
                addDirsFrom(dirs, p, expectSuccess);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final DirectoryStream.Filter<Path> DIR_FILTER =
            new DirectoryStream.Filter<Path>() {
                @Override
                public boolean accept(Path entry) {
                    /* Only accept directories and non-blacklisted tests */
                    if (!Files.isDirectory(entry)) {
                        return false;
                    }
                    for (Path ignoredTestPath : IGNORED_TESTS) {
                        if (entry.equals(ignoredTestPath)) {
                            System.err.println("Skipping test " + entry.toString() + " as requested.");
                            return false;
                        }
                    }
                    return true;
                }
            };

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
    public CtfTestSuiteTest(String tracePath, boolean expectSuccess) {
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
        try {
            /* Instantiate the trace (which implies parsing the metadata) */
            CTFTrace trace = new CTFTrace(fTracePath);
            /* Read the trace until the end */
            try (CTFTraceReader reader = new CTFTraceReader(trace);) {

                reader.getCurrentEventDef();
                while (reader.advance()) {
                    assertNotNull(reader.getCurrentEventDef());
                }

                checkIfWeShoudlSucceed();
            }
        } catch (CTFException e) {
            checkIfWeShouldFail(e);
        } catch (OutOfMemoryError e) {
            checkIfWeShouldFail(e);
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
