/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.lttng2.kernel.core.tests.stateprovider;

import static org.junit.Assume.assumeTrue;

import org.eclipse.linuxtools.internal.lttng2.kernel.core.stateprovider.LttngKernelStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.TmfStateSystemFactory;
import org.eclipse.linuxtools.tmf.core.tests.shared.CtfTmfTestTraces;
import org.junit.BeforeClass;

/**
 * State system tests using the in-memory back-end.
 *
 * @author Alexandre Montplaisir
 */
public class StateSystemInMemoryTest extends StateSystemTest {

    /**
     * Initialization
     */
    @BeforeClass
    public static void initialize() {
        assumeTrue(CtfTmfTestTraces.tracesExist());
        input = new LttngKernelStateProvider(CtfTmfTestTraces.getTestTrace(TRACE_INDEX));
        ssq = TmfStateSystemFactory.newInMemHistory(input, true);
    }
}
