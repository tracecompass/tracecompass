/*******************************************************************************
 * Copyright (c) 2012 Ericsson
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

import org.eclipse.linuxtools.internal.lttng2.kernel.core.stateprovider.CtfKernelStateInput;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.statesystem.IStateChangeInput;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for the LTTng 2.0 kernel state provider
 *
 * @author Alexandre Montplaisir
 */
public class CtfKernelStateInputTest {

    static IStateChangeInput input;

    /**
     * Set-up.
     *
     * @throws TmfTraceException
     *             If we can't find the test traces
     */
    @BeforeClass
    public static void initialize() throws TmfTraceException {
        input = new CtfKernelStateInput(CtfTestFiles.getTestTrace());

    }

    /**
     * Test loading the state provider.
     */
    @Test
    public void testOpening() {
        long testStartTime;
        testStartTime = input.getStartTime();
        assertEquals(testStartTime, CtfTestFiles.startTime);
    }

}
