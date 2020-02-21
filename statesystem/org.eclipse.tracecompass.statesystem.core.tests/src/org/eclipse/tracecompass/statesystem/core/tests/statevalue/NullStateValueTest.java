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
import static org.junit.Assert.assertTrue;

import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue.Type;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.junit.Test;

/**
 * Test the string state value class
 *
 * @author Geneviève Bastien
 */
public class NullStateValueTest extends StateValueTestBase {

    private static final TmfStateValue STATE_VALUE = TmfStateValue.nullValue();

    @Override
    protected ITmfStateValue getStateValueFixture() {
        return STATE_VALUE;
    }

    @Override
    protected Type getStateValueType() {
        return ITmfStateValue.Type.NULL;
    }

    @Override
    @Test
    public void testUnboxInt() {
        int unboxed = STATE_VALUE.unboxInt();
        assertEquals(-1, unboxed);
    }

    @Override
    @Test
    public void testUnboxLong() {
        long unboxed = STATE_VALUE.unboxLong();
        assertEquals(-1, unboxed);
    }

    @Override
    @Test
    public void testUnboxDouble() {
        double unboxed = STATE_VALUE.unboxDouble();
        assertEquals(Double.NaN, unboxed, 0.00001);
    }

    @Override
    @Test
    public void testUnboxStr() {
        String unboxed = STATE_VALUE.unboxStr();
        assertEquals("nullValue", unboxed);
    }

    @Override
    @Test
    public void testIsNull() {
        assertTrue(STATE_VALUE.isNull());
    }
}
