/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Bernd Hufmann - Use state system analysis module instead of factory
 ******************************************************************************/

package org.eclipse.linuxtools.lttng2.kernel.core.tests.stateprovider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.File;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.stateprovider.LttngKernelStateProvider;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;
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

    private static File stateFile;
    private static File stateFileBenchmark;

    private static final String TEST_FILE_NAME = "test.ht";
    private static final String BENCHMARK_FILE_NAME = "test.benchmark.ht";

    /**
     * Initialize the test cases (build the history file once for all tests).
     */
    @BeforeClass
    public static void initialize() {
        assumeTrue(testTrace.exists());
        stateFile = createStateFile(TEST_FILE_NAME);
        stateFileBenchmark = createStateFile(BENCHMARK_FILE_NAME);

        TestLttngKernelAnalysisModule module = new TestLttngKernelAnalysisModule(TEST_FILE_NAME);
        try {
            module.setTrace(testTrace.getTrace());
        } catch (TmfAnalysisException e) {
            fail();
        }
        module.schedule();
        assertTrue(module.waitForCompletion(new NullProgressMonitor()));
        ssq = module.getStateSystem();

        assertNotNull(ssq);
    }

    /**
     * Clean-up
     */
    @AfterClass
    public static void tearDownClass() {
        stateFile.delete();
        stateFileBenchmark.delete();
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
            module2.setTrace(testTrace.getTrace());
        } catch (TmfAnalysisException e) {
            fail();
        }
        module2.schedule();
        assertTrue(module2.waitForCompletion(new NullProgressMonitor()));
        ITmfStateSystem ssb2 = module2.getStateSystem();

        assertNotNull(ssb2);
        assertEquals(startTime, ssb2.getStartTime());
        assertEquals(endTime, ssb2.getCurrentEndTime());
    }

    /**
     * Test re-opening the existing file.
     */
    @Test
    public void testOpenExistingStateFile() {
        /* 'newStateFile' should have already been created */
        TestLttngKernelAnalysisModule module2 = new TestLttngKernelAnalysisModule(TEST_FILE_NAME);
        try {
            module2.setTrace(testTrace.getTrace());
        } catch (TmfAnalysisException e) {
            fail();
        }
        module2.schedule();
        assertTrue(module2.waitForCompletion(new NullProgressMonitor()));
        ITmfStateSystem ssb2 = module2.getStateSystem();

        assertNotNull(ssb2);
        assertEquals(startTime, ssb2.getStartTime());
        assertEquals(endTime, ssb2.getCurrentEndTime());
    }

    private static class TestLttngKernelAnalysisModule extends TmfStateSystemAnalysisModule {

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
        public void setTrace(ITmfTrace trace) throws TmfAnalysisException {
            if (!(trace instanceof CtfTmfTrace)) {
                throw new IllegalStateException("TestLttngKernelAnalysisModule: trace should be of type CtfTmfTrace"); //$NON-NLS-1$
            }
            super.setTrace(trace);
        }

        @Override
        protected ITmfStateProvider createStateProvider() {
            return new LttngKernelStateProvider((CtfTmfTrace) getTrace());
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
        File file = new File(TmfTraceManager.getSupplementaryFileDir(testTrace.getTrace()) + name);
        if (file.exists()) {
            file.delete();
        }
        return file;
    }

}
