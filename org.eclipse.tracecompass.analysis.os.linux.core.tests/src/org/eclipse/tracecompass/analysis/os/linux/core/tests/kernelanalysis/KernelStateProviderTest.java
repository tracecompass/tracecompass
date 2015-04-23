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
import static org.junit.Assume.assumeTrue;

import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.KernelStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTrace;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for the {@link KernelStateProvider}
 *
 * @author Alexandre Montplaisir
 */
public class KernelStateProviderTest {

    private static final CtfTmfTestTrace testTrace = CtfTmfTestTrace.TRACE2;

    private static ITmfStateProvider input;

    /**
     * Set-up.
     */
    @BeforeClass
    public static void initialize() {
        assumeTrue(testTrace.exists());
        input = new KernelStateProvider(testTrace.getTrace(), IKernelAnalysisEventLayout.DEFAULT_LAYOUT);
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
