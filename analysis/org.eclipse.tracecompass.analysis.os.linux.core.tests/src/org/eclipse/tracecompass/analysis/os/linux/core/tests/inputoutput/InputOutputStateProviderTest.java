/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.tests.inputoutput;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.os.linux.core.inputoutput.InputOutputAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.LinuxTestCase;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.LinuxTestCase.PunctualInfo;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.inputoutput.IoTestFactory;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput.InputOutputStateProvider;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.tests.shared.utils.StateSystemTestUtils;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestHelper;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test suite for the {@link InputOutputStateProvider} class
 *
 * @author Geneviève Bastien
 */
@RunWith(Parameterized.class)
public class InputOutputStateProviderTest extends AbstractTestInputOutput {

    private final LinuxTestCase fTestCase;

    /**
     * Constructor
     *
     * @param testName
     *            A name for the test, to display in the header
     * @param test
     *            A test case parameter for this test
     */
    public InputOutputStateProviderTest(String testName, LinuxTestCase test) {
        super();
        fTestCase = test;
    }

    /**
     * Clean up
     */
    @After
    public void tearDown() {
        super.deleteTrace();
    }

    /**
     * @return The arrays of parameters
     */
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
                { IoTestFactory.SIMPLE_REQUESTS.getTraceFileName(), IoTestFactory.SIMPLE_REQUESTS },
                { IoTestFactory.SIMPLE_NO_STATEDUMP.getTraceFileName(), IoTestFactory.SIMPLE_NO_STATEDUMP },
                { IoTestFactory.REQUESTS_MERGE.getTraceFileName(), IoTestFactory.REQUESTS_MERGE },
                { IoTestFactory.REQUESTS_MISSING.getTraceFileName(), IoTestFactory.REQUESTS_MISSING },
                { IoTestFactory.TWO_DEVICES.getTraceFileName(), IoTestFactory.TWO_DEVICES },
                { IoTestFactory.SYSCALL_READ.getTraceFileName(), IoTestFactory.SYSCALL_READ },
                { IoTestFactory.SYSCALL_WRITE.getTraceFileName(), IoTestFactory.SYSCALL_WRITE },
                { IoTestFactory.SYSCALLS_KERNEL.getTraceFileName(), IoTestFactory.SYSCALLS_KERNEL }
        });
    }

    /**
     * Test that the analysis executes without problems
     */
    @Test
    public void testAnalysisExecution() {
        InputOutputAnalysisModule module = setUp(fTestCase.getTraceFileName());
        /* Make sure the analysis hasn't run yet */
        assertNull(module.getStateSystem());

        /* Execute the analysis */
        assertTrue(TmfTestHelper.executeAnalysis(module));
        assertNotNull(module.getStateSystem());
    }

    /**
     * Test the intervals built by the state provider
     */
    @Test
    public void testStateProviderIntervalData() {
        InputOutputAnalysisModule module = setUp(fTestCase.getTraceFileName());
        assertNotNull(module);
        TmfTestHelper.executeAnalysis(module);

        ITmfStateSystem ss = module.getStateSystem();
        assertNotNull(ss);

        StateSystemTestUtils.testIntervals(ss, fTestCase.getTestIntervals());
    }

    /**
     * Test the data of attributes at punctual times
     */
    @Test
    public void testStateProviderPunctualData() {
        InputOutputAnalysisModule module = setUp(fTestCase.getTraceFileName());
        assertNotNull(module);
        TmfTestHelper.executeAnalysis(module);

        ITmfStateSystem ss = module.getStateSystem();
        assertNotNull(ss);

        for (@NonNull PunctualInfo info : fTestCase.getPunctualTestData()) {
            StateSystemTestUtils.testValuesAtTime(ss, info.getTimestamp(), info.getValues());
        }
    }
}
