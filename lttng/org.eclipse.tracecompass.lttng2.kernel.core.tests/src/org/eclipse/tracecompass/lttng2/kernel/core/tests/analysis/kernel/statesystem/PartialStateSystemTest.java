/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson
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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelAnalysisModule;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * State system tests using a partial history.
 *
 * @author Alexandre Montplaisir
 */

public class PartialStateSystemTest extends StateSystemTest {

    private static final @NonNull String TEST_FILE_NAME = "test-partial";

    private static CtfTmfTrace fTrace;
    private static File fStateFile;
    private static TestLttngKernelAnalysisModule fModule;

    /**
     * Test class setup
     */
    @BeforeClass
    public static void initialize() {
        fTrace = CtfTmfTestTraceUtils.getTrace(testTrace);

        fStateFile = new File(TmfTraceManager.getSupplementaryFileDir(fTrace) + TEST_FILE_NAME);
        if (fStateFile.exists()) {
            fStateFile.delete();
        }

        fModule = new TestLttngKernelAnalysisModule(TEST_FILE_NAME);
        try {
            assertTrue(fModule.setTrace(fTrace));
        } catch (TmfAnalysisException e) {
            fail();
        }
        fModule.schedule();
        assertTrue(fModule.waitForCompletion());

        fixture = fModule.getStateSystem();
    }

    /**
     * Class clean-up
     */
    @AfterClass
    public static void cleanup() {
        if (fModule != null) {
            fModule.dispose();
        }
        if (fStateFile != null) {
            fStateFile.delete();
        }
        if (fixture != null) {
            fixture.dispose();
        }
        if (fTrace != null) {
            fTrace.dispose();
        }
        fModule = null;
        fixture = null;
        fTrace = null;
    }

    @Override
    @Test
    @Ignore /*
             * Test passes but takes many minutes to complete (~10 min) since it
             * does many singular "slow" queries, remove this Ignore and the
             * time out rule in StateSystemTest if you can wait
             */
    public void testRangeQuery1() {
        super.testRangeQuery1();
    }

    @Override
    @Test
    @Ignore /*
             * Test passes but takes many minutes (~3 min) to complete since it
             * does many singular "slow" queries, remove this Ignore and the
             * time out rule in StateSystemTest if you can wait
             */
    public void testRangeQuery3() {
        super.testRangeQuery3();
    }

    @Override
    @Test
    public void testFullQueryThorough() {
        super.testFullQueryThorough();
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
            return StateSystemBackendType.PARTIAL;
        }

        @Override
        protected String getSsFileName() {
            return htFileName;
        }

    }
}
