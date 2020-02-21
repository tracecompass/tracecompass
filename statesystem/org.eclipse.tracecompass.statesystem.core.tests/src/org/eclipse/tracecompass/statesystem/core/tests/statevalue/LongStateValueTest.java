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

package org.eclipse.tracecompass.statesystem.core.tests.statevalue;

import static org.junit.Assert.assertEquals;

import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue.Type;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.junit.Test;

/**
 * Test the long state value class
 *
 * @author Geneviève Bastien
 */
public class LongStateValueTest extends StateValueTestBase {

    private static final long UNBOXED_VALUE = 34L;
    private static final TmfStateValue STATE_VALUE = TmfStateValue.newValueLong(UNBOXED_VALUE);

    @Override
    protected ITmfStateValue getStateValueFixture() {
        return STATE_VALUE;
    }

    @Override
    protected Type getStateValueType() {
        return ITmfStateValue.Type.LONG;
    }

    @Override
    @Test
    public void testUnboxLong() {
        long unboxed = STATE_VALUE.unboxLong();
        assertEquals(UNBOXED_VALUE, unboxed);
    }
}
