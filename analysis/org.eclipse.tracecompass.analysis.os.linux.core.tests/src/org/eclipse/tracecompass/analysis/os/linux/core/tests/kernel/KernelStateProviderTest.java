/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.tests.kernel;

import static org.junit.Assert.assertEquals;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.LinuxTestCase;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.kernel.KernelAnalysisTestFactory;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelTrace;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.KernelStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the {@link KernelStateProvider}
 *
 * @author Alexandre Montplaisir
 */
public class KernelStateProviderTest {

    private static final @NonNull LinuxTestCase KERNEL_TEST_CASE = KernelAnalysisTestFactory.KERNEL_SCHED;

    private IKernelTrace fTrace;
    private ITmfStateProvider fInput;

    /**
     * Set-up.
     */
    @Before
    public void initialize() {
        IKernelTrace thetrace = KERNEL_TEST_CASE.getKernelTrace();
        fTrace = thetrace;
        fInput = new KernelStateProvider(thetrace, thetrace.getKernelEventLayout());
    }

    /**
     * Class teardown
     */
    @After
    public void classTeardown() {
        if (fTrace != null) {
            fTrace.dispose();
        }
    }

    /**
     * Test loading the state provider.
     */
    @Test
    public void testOpening() {
        long testStartTime;
        testStartTime = fInput.getStartTime();
        /* Expected start time of the trace */
        assertEquals(testStartTime, 1L);
    }

}
