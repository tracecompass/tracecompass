/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - IInitial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.tests.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.trace.TraceValidationStatus;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTraceValidationStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * The class <code>CtfTmfTraceValidateTest</code> contains tests for trace
 * validation
 * <code>{@link CtfTmfTrace#validate(org.eclipse.core.resources.IProject, String)}</code>
 * .
 *
 * @author Bernd Hufmann
 */
@RunWith(Parameterized.class)
public class CtfTmfTraceValidateTest {

    private static final Path BASE_PATH = Paths.get("../org.eclipse.tracecompass.ctf.core.tests", "traces");
    private static final Path CTF_SUITE_BASE_PATH = Paths.get("../org.eclipse.tracecompass.ctf.core.tests", "traces", "ctf-testsuite", "tests", "1.8");

    private String fTrace;
    private int fServerity;
    private int fConfidence;
    private boolean fHasException;

    /**
     * Gets a list of test case parameters.
     *
     * @return The list of CTF traces (directories) to test
     * @throws Exception in case of error
     */
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> getTracePaths() throws Exception {
        final List<Object[]> dirs = new LinkedList<>();
        // text-only metadata, valid CTF trace (lttle-endian)
        addDirsFrom(dirs, CTF_SUITE_BASE_PATH.resolve(Paths.get("regression", "metadata", "pass", "literal-integers")), IStatus.OK, 10, false);
        // packet-based metadata, valid CTF trace (lttle-endian)
        URI tracePath = FileLocator.toFileURL(CtfTestTrace.KERNEL.getTraceURL()).toURI();
        addDirsFrom(dirs, Paths.get(tracePath), IStatus.OK, 10, false);
        // text-only metadata, but invalid
        addDirsFrom(dirs, CTF_SUITE_BASE_PATH.resolve(Paths.get("regression", "metadata", "fail", "enum-empty")), IStatus.WARNING, 1, true);
        // packet-based metadata, but invalid
        addDirsFrom(dirs, CTF_SUITE_BASE_PATH.resolve(Paths.get("regression", "metadata", "fail", "lttng-modules-2.0-pre1")), IStatus.WARNING, 1, true);
        // pass file instead of directory
        addDirsFrom(dirs, BASE_PATH.resolve(Paths.get("synctraces.tar.gz")), IStatus.ERROR, 1, false);

        return dirs;
    }

    private static void addDirsFrom(List<Object[]> dirs, Path path, int severity, int confidence, boolean hasException) {
        if (!Files.exists(path)) {
            /* Some planned directories may not exist yet in the test suite */
            return;
        }

        Object array[] = new Object[] { path.toString(), severity, confidence, hasException };
        dirs.add(array);
    }

    /**
     * @param trace
     *            a trace path
     * @param severity
     *            severity of validation status expected
     * @param confidence
     *            confidence of validation status expected
     * @param hasException
     *            flag whether validation status should contain exception
     */
    public CtfTmfTraceValidateTest(String trace, int severity, int confidence, boolean hasException) {
        fTrace = trace;
        fServerity = severity;
        fConfidence = confidence;
        fHasException = hasException;
    }

    /**
     * Main test cases
     */
    @Test
    public void testValidate() {
        CtfTmfTrace trace = new CtfTmfTrace();
        IStatus status = trace.validate(null, fTrace);
        assertEquals(toString(), fServerity, status.getSeverity());

        if (fHasException) {
            assertNotNull(toString(), status.getException());
        }
        switch (status.getSeverity()) {
        case IStatus.OK: {
            assertTrue(status instanceof CtfTraceValidationStatus);
            CtfTraceValidationStatus ctfStatus = (CtfTraceValidationStatus) status;
            assertEquals(toString(), fConfidence, ctfStatus.getConfidence());
            assertNotNull(ctfStatus.getEnvironment());
            break;
        }
        case IStatus.WARNING: {
            assertTrue(status instanceof TraceValidationStatus);
            TraceValidationStatus ctfStatus = (TraceValidationStatus) status;
            assertEquals(fConfidence, ctfStatus.getConfidence());
            break;
        }
        case IStatus.ERROR: {
            // nothing else to check here
            break;
        }
        default:
            // no other severity should be returned
            fail();
            break;
        }
        assertEquals(fServerity, status.getSeverity());
        trace.dispose();
    }

}
