/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.linuxtools.lttng2.kernel.core.tests.stateprovider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import org.eclipse.linuxtools.internal.lttng2.kernel.core.stateprovider.LttngKernelStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.linuxtools.tmf.core.tests.shared.CtfTmfTestTraces;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for the {@link LttngKernelStateProvider}
 *
 * @author Alexandre Montplaisir
 */
public class LttngKernelStateProviderTest {

    private final static int TRACE_INDEX = 1;

    private static ITmfStateProvider input;

    /**
     * Set-up.
     */
    @BeforeClass
    public static void initialize() {
        assumeTrue(CtfTmfTestTraces.tracesExist());
        input = new LttngKernelStateProvider(CtfTmfTestTraces.getTestTrace(TRACE_INDEX));

    }

    /**
     * Test loading the state provider.
     */
    @Test
    public void testOpening() {
        long testStartTime;
        testStartTime = input.getStartTime();
        assertEquals(testStartTime, StateSystemTest.startTime);
    }

}
