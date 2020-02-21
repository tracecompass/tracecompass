/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.tests.statevalue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.junit.Test;

/**
 * Base class for state value tests.
 *
 * By default, it is assumed the state value is *not* null, and throws an
 * exception for every unbox*() method. Subclasses should override the
 * appropriate tests to represent their actual behaviour.
 *
 * @author Alexandre Montplaisir
 */
public abstract class StateValueTestBase {

    /**
     * @return The state value fixture
     */
    protected abstract ITmfStateValue getStateValueFixture();

    /**
     * @return The expected type of the state value
     */
    protected abstract ITmfStateValue.Type getStateValueType();

    /**
     * Test the {@link TmfStateValue#getType()} method
     */
    @Test
    public final void testGetType() {
        assertEquals(getStateValueType(), getStateValueFixture().getType());
    }

    /**
     * Test the {@link TmfStateValue#unboxInt()} method
     */
    @Test(expected=StateValueTypeException.class)
    public void testUnboxInt() {
        getStateValueFixture().unboxInt();
    }

    /**
     * Test the {@link TmfStateValue#unboxLong()} method
     */
    @Test(expected=StateValueTypeException.class)
    public void testUnboxLong() {
        getStateValueFixture().unboxLong();
    }

    /**
     * Test the {@link TmfStateValue#unboxDouble()} method
     */
    @Test(expected=StateValueTypeException.class)
    public void testUnboxDouble() {
        getStateValueFixture().unboxDouble();
    }

    /**
     * Test the {@link TmfStateValue#unboxStr()} method
     */
    @Test(expected=StateValueTypeException.class)
    public void testUnboxStr() {
        getStateValueFixture().unboxStr();
    }

    /**
     * Test the {@link TmfStateValue#isNull()} method
     */
    @Test
    public void testIsNull() {
        assertFalse(getStateValueFixture().isNull());
    }
}
