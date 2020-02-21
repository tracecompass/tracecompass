/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson
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
 *   Bernd Hufmann - Use state system analysis module instead of factory
 ******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.core.tests.analysis.kernel.statesystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelAnalysisModule;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * State system tests using a full history back-end and the LTTng kernel state
 * input.
 *
 * @author Alexandre Montplaisir
 */
public class StateSystemFullHistoryTest extends StateSystemTest {

    private static final @NonNull String TEST_FILE_NAME = "test.ht";
    private static final @NonNull String BENCHMARK_FILE_NAME = "test.benchmark.ht";

    private static CtfTmfTrace trace;
    private static File stateFile;
    private static File stateFileBenchmark;
    private static TestLttngKernelAnalysisModule module;

    /**
     * Test class setup
     */
    @BeforeClass
    public static void initialize() {
        trace = CtfTmfTestTraceUtils.getTrace(testTrace);

        stateFile = createStateFile(TEST_FILE_NAME);
        stateFileBenchmark = createStateFile(BENCHMARK_FILE_NAME);

        module = new TestLttngKernelAnalysisModule(TEST_FILE_NAME);
        try {
            assertTrue(module.setTrace(trace));
        } catch (TmfAnalysisException e) {
            fail();
        }
        module.schedule();
        assertTrue(module.waitForCompletion());

        fixture = module.getStateSystem();
    }

    /**
     * Clean-up
     */
    @AfterClass
    public static void cleanup() {
        if (module != null) {
            module.dispose();
        }
        if (stateFile != null) {
            stateFile.delete();
        }
        if (stateFileBenchmark != null) {
            stateFileBenchmark.delete();
        }
        if (fixture != null) {
            fixture.dispose();
        }
        if (trace != null) {
            trace.dispose();
        }
        module = null;
        fixture = null;
        trace = null;
    }

    // ------------------------------------------------------------------------
    // Tests specific to a full-history
    // ------------------------------------------------------------------------

    /**
     * Rebuild independently so we can benchmark it. Too bad JUnit doesn't allow
     * us to @Test the @BeforeClass...
     */
    @Test
    public void testBuild() {
        TestLttngKernelAnalysisModule module2 = new TestLttngKernelAnalysisModule(BENCHMARK_FILE_NAME);
        try {
            assertTrue(module2.setTrace(trace));
        } catch (TmfAnalysisException e) {
            module2.dispose();
            fail();
        }
        module2.schedule();
        assertTrue(module2.waitForCompletion());
        ITmfStateSystem ssb2 = module2.getStateSystem();

        assertNotNull(ssb2);
        assertEquals(startTime, ssb2.getStartTime());
        assertEquals(endTime, ssb2.getCurrentEndTime());

        module2.dispose();
    }

    /**
     * Test re-opening the existing file.
     */
    @Test
    public void testOpenExistingStateFile() {
        /* 'newStateFile' should have already been created */
        TestLttngKernelAnalysisModule module2 = new TestLttngKernelAnalysisModule(TEST_FILE_NAME);
        try {
            assertTrue(module2.setTrace(trace));
        } catch (TmfAnalysisException e) {
            module2.dispose();
            fail();
        }
        module2.schedule();
        assertTrue(module2.waitForCompletion());
        ITmfStateSystem ssb2 = module2.getStateSystem();

        assertNotNull(ssb2);
        assertEquals(startTime, ssb2.getStartTime());
        assertEquals(endTime, ssb2.getCurrentEndTime());

        module2.dispose();
    }

    @NonNullByDefault
    private static class TestLttngKernelAnalysisModule extends KernelAnalysisModule {

        private final String htFileName;

        /**
         * Constructor adding the views to the analysis
         * @param htFileName
         *      The History File Name
         */
        public TestLttngKernelAnalysisModule(String htFileName) {
            super();
            this.htFileName = htFileName;
        }

        @Override
        public boolean setTrace(@Nullable ITmfTrace trace) throws TmfAnalysisException {
            if (!(trace instanceof CtfTmfTrace)) {
                return false;
            }
            return super.setTrace(trace);
        }

        @Override
        protected StateSystemBackendType getBackendType() {
            return StateSystemBackendType.FULL;
        }

        @Override
        protected String getSsFileName() {
            return htFileName;
        }
    }

    private static File createStateFile(String name) {
        File file = new File(TmfTraceManager.getSupplementaryFileDir(trace) + name);
        if (file.exists()) {
            file.delete();
        }
        return file;
    }

}
