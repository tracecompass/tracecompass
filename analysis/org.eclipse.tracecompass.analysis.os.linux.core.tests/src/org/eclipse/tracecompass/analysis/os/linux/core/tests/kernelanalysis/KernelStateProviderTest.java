/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.tests.kernelanalysis;

import static org.junit.Assert.assertEquals;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.KernelStateProvider;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for the {@link KernelStateProvider}
 *
 * @author Alexandre Montplaisir
 */
public class KernelStateProviderTest {

    private static final @NonNull CtfTestTrace testTrace = CtfTestTrace.TRACE2;

    private static CtfTmfTrace trace;
    private static ITmfStateProvider input;

    /**
     * Set-up.
     */
    @BeforeClass
    public static void initialize() {
        CtfTmfTrace thetrace = CtfTmfTestTraceUtils.getTrace(testTrace);
        trace = thetrace;
        input = new KernelStateProvider(thetrace, IKernelAnalysisEventLayout.DEFAULT_LAYOUT);
    }

    /**
     * Class teardown
     */
    @AfterClass
    public static void classTeardown() {
        if (trace != null) {
            trace.dispose();
        }
    }

    /**
     * Test loading the state provider.
     */
    @Test
    public void testOpening() {
        long testStartTime;
        testStartTime = input.getStartTime();
        /* Expected start time of "trace2" */
        assertEquals(testStartTime, 1331668247314038062L);
    }

}
