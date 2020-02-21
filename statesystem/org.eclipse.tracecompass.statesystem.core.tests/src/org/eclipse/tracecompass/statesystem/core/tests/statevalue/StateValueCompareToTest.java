/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Naser Ezzati - Initial API and implementation
 ******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.tests.statevalue;

import static org.junit.Assert.assertTrue;

import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.junit.Test;

/**
 * Unit test for the {@link ITmfStateValue#compareTo(ITmfStateValue)} method
 *
 * @author Naser Ezzati
 */
public class StateValueCompareToTest {

    // ------------------------------------------------------------------------
    // Static fields
    // ------------------------------------------------------------------------

    /* State values that will be used */
    private static final ITmfStateValue BASE_INT_VALUE = TmfStateValue.newValueInt(10);
    private static final ITmfStateValue BIGGER_INT_VALUE = TmfStateValue.newValueInt(20);
    private static final ITmfStateValue SMALLER_INT_VALUE = TmfStateValue.newValueInt(6);

    private static final ITmfStateValue BASE_LONG_VALUE = TmfStateValue.newValueLong(10);
    private static final ITmfStateValue BIGGER_LONG_VALUE = TmfStateValue.newValueLong(20);
    private static final ITmfStateValue SMALLER_LONG_VALUE = TmfStateValue.newValueLong(6);
    private static final ITmfStateValue MIN_LONG_VALUE = TmfStateValue.newValueLong(Long.MIN_VALUE);
    private static final ITmfStateValue MAX_LONG_VALUE = TmfStateValue.newValueLong(Long.MAX_VALUE);

    private static final ITmfStateValue BASE_DOUBLE_VALUE = TmfStateValue.newValueDouble(10.00);
    private static final ITmfStateValue BIGGER_DOUBLE_VALUE1 = TmfStateValue.newValueDouble(20.00);
    private static final ITmfStateValue BIGGER_DOUBLE_VALUE2 = TmfStateValue.newValueDouble(10.03);
    private static final ITmfStateValue SMALLER_DOUBLE_VALUE1 = TmfStateValue.newValueDouble(6.00);
    private static final ITmfStateValue SMALLER_DOUBLE_VALUE2 = TmfStateValue.newValueDouble(9.99);
    private static final ITmfStateValue MIN_DOUBLE_VALUE = TmfStateValue.newValueDouble(Double.MIN_VALUE);
    private static final ITmfStateValue MAX_DOUBLE_VALUE = TmfStateValue.newValueDouble(Double.MAX_VALUE);
    private static final ITmfStateValue POSITIVE_INFINITY = TmfStateValue.newValueDouble(Double.POSITIVE_INFINITY);
    private static final ITmfStateValue NEGATIVE_INFINITY = TmfStateValue.newValueDouble(Double.NEGATIVE_INFINITY);

    private static final ITmfStateValue BASE_STRING_VALUE = TmfStateValue.newValueString("D");
    private static final ITmfStateValue BIGGER_STRING_VALUE = TmfStateValue.newValueString("Z");
    private static final ITmfStateValue SMALLER_STRING_VALUE = TmfStateValue.newValueString("A");

    private static final ITmfStateValue NULL_VALUE = TmfStateValue.nullValue();

    // ------------------------------------------------------------------------
    // Comparisons of Integer state values
    // ------------------------------------------------------------------------

    /**
     * Compare Integer state values together
     */
    @Test
    public void compareIntWithInt() {
        assertTrue(BASE_INT_VALUE.compareTo(BASE_INT_VALUE) == 0);
        assertTrue(BASE_INT_VALUE.compareTo(BIGGER_INT_VALUE) < 0);
        assertTrue(BASE_INT_VALUE.compareTo(SMALLER_INT_VALUE) > 0);
    }

    /**
     * Compare Integer with Long state values
     */
    @Test
    public void compareIntWithLong() {
        assertTrue(BASE_INT_VALUE.compareTo(BASE_LONG_VALUE) == 0);
        assertTrue(BASE_INT_VALUE.compareTo(BIGGER_LONG_VALUE) < 0);
        assertTrue(BASE_INT_VALUE.compareTo(MAX_LONG_VALUE) < 0);

        assertTrue(BASE_INT_VALUE.compareTo(SMALLER_LONG_VALUE) > 0);
        assertTrue(BASE_INT_VALUE.compareTo(MIN_LONG_VALUE) > 0);
    }

    /**
     * Compare Integer with Double state values
     */
    @Test
    public void compareIntWithDouble() {
        assertTrue(BASE_INT_VALUE.compareTo(BASE_DOUBLE_VALUE) == 0);
        assertTrue(BASE_INT_VALUE.compareTo(BIGGER_DOUBLE_VALUE1) < 0);
        assertTrue(BASE_INT_VALUE.compareTo(BIGGER_DOUBLE_VALUE2) < 0);
        assertTrue(BASE_INT_VALUE.compareTo(MAX_DOUBLE_VALUE) < 0);
        assertTrue(BASE_INT_VALUE.compareTo(POSITIVE_INFINITY) < 0);
        assertTrue(BASE_INT_VALUE.compareTo(SMALLER_DOUBLE_VALUE1) > 0);
        assertTrue(BASE_INT_VALUE.compareTo(SMALLER_DOUBLE_VALUE2) > 0);
        assertTrue(BASE_INT_VALUE.compareTo(MIN_DOUBLE_VALUE) > 0);
        assertTrue(BASE_INT_VALUE.compareTo(NEGATIVE_INFINITY) > 0);
    }

    /**
     * Compare Integer with Null state values
     */
    @Test
    public void compareIntWithNull() {
        assertTrue(BASE_INT_VALUE.compareTo(NULL_VALUE) > 0);
    }

    /**
     * Compare Integer with String state values (should fail)
     */
    @Test(expected = StateValueTypeException.class)
    public void tcompareIntWithString() {
        BASE_INT_VALUE.compareTo(BASE_STRING_VALUE);
    }

    // ------------------------------------------------------------------------
    // Comparisons of Long state values
    // ------------------------------------------------------------------------

    /**
     * Compare Long with Integer state values
     */
    @Test
    public void compareLongWithInt() {
        // with Integer
        assertTrue(BASE_LONG_VALUE.compareTo(BASE_INT_VALUE) == 0);
        assertTrue(BASE_LONG_VALUE.compareTo(BIGGER_INT_VALUE) < 0);
        assertTrue(BASE_LONG_VALUE.compareTo(SMALLER_INT_VALUE) > 0);
    }

    /**
     * Compare Long state values together
     */
    @Test
    public void compareLongWithLong() {
        assertTrue(BASE_LONG_VALUE.compareTo(BASE_LONG_VALUE) == 0);
        assertTrue(BASE_LONG_VALUE.compareTo(BIGGER_LONG_VALUE) < 0);
        assertTrue(BASE_LONG_VALUE.compareTo(MAX_LONG_VALUE) < 0);
        assertTrue(BASE_LONG_VALUE.compareTo(SMALLER_LONG_VALUE) > 0);
        assertTrue(BASE_LONG_VALUE.compareTo(MIN_LONG_VALUE) > 0);
    }

    /**
     * Compare Long with Double state values
     */
    @Test
    public void compareLongWithDouble() {
        assertTrue(BASE_LONG_VALUE.compareTo(BASE_DOUBLE_VALUE) == 0);
        assertTrue(BASE_LONG_VALUE.compareTo(BIGGER_DOUBLE_VALUE1) < 0);
        assertTrue(BASE_LONG_VALUE.compareTo(BIGGER_DOUBLE_VALUE2) < 0);
        assertTrue(BASE_LONG_VALUE.compareTo(MAX_DOUBLE_VALUE) < 0);
        assertTrue(BASE_LONG_VALUE.compareTo(POSITIVE_INFINITY) < 0);
        assertTrue(BASE_LONG_VALUE.compareTo(SMALLER_DOUBLE_VALUE1) > 0);
        assertTrue(BASE_LONG_VALUE.compareTo(SMALLER_DOUBLE_VALUE2) > 0);
        assertTrue(BASE_LONG_VALUE.compareTo(MIN_DOUBLE_VALUE) > 0);
        assertTrue(BASE_LONG_VALUE.compareTo(NEGATIVE_INFINITY) > 0);
    }

    /**
     * Compare Long with Null state values
     */
    @Test
    public void compareLongWithNull() {
        assertTrue(BASE_LONG_VALUE.compareTo(NULL_VALUE) > 0);
    }

    /**
     * Compare Long with String state values (should fail)
     */
    @Test(expected = StateValueTypeException.class)
    public void compareLongWithString() {
        BASE_LONG_VALUE.compareTo(BASE_STRING_VALUE);
    }

    // ------------------------------------------------------------------------
    // Comparisons of Double state values
    // ------------------------------------------------------------------------

    /**
     * Compare Double with Integer state values
     */
    @Test
    public void compareDoubleWithInt() {
        assertTrue(BASE_DOUBLE_VALUE.compareTo(BASE_INT_VALUE) == 0);
        assertTrue(BASE_DOUBLE_VALUE.compareTo(BIGGER_INT_VALUE) < 0);
        assertTrue(BASE_DOUBLE_VALUE.compareTo(SMALLER_INT_VALUE) > 0);
    }

    /**
     * Compare Double with Long state values
     */
    @Test
    public void compareDoubleWithLong() {
        assertTrue(BASE_DOUBLE_VALUE.compareTo(BASE_LONG_VALUE) == 0);
        assertTrue(BASE_DOUBLE_VALUE.compareTo(BIGGER_LONG_VALUE) < 0);
        assertTrue(SMALLER_DOUBLE_VALUE2.compareTo(BASE_LONG_VALUE) < 0);
        assertTrue(BASE_DOUBLE_VALUE.compareTo(MAX_LONG_VALUE) < 0);
        assertTrue(BIGGER_DOUBLE_VALUE1.compareTo(SMALLER_LONG_VALUE) > 0);
        assertTrue(BIGGER_DOUBLE_VALUE2.compareTo(BASE_LONG_VALUE) > 0);
        assertTrue(BASE_DOUBLE_VALUE.compareTo(MIN_LONG_VALUE) > 0);
    }

    /**
     * Compare Double state values together
     */
    @Test
    public void compareDoubleWithDouble() {
        assertTrue(BASE_DOUBLE_VALUE.compareTo(BASE_DOUBLE_VALUE) == 0);
        assertTrue(BASE_DOUBLE_VALUE.compareTo(BIGGER_DOUBLE_VALUE2) < 0);
        assertTrue(BASE_DOUBLE_VALUE.compareTo(MAX_DOUBLE_VALUE) < 0);
        assertTrue(BASE_DOUBLE_VALUE.compareTo(SMALLER_DOUBLE_VALUE2) > 0);
        assertTrue(BASE_DOUBLE_VALUE.compareTo(MIN_DOUBLE_VALUE) > 0);
    }

    /**
     * Compare Double with Null state values
     */
    @Test
    public void compareDoubleWithNull() {
        /* NullValue.unboxDouble returns NaN */
        assertTrue(BASE_DOUBLE_VALUE.compareTo(NULL_VALUE) < 0);
    }

    /**
     * Compare Double with String state values (should fail)
     */
    @Test(expected = StateValueTypeException.class)
    public void compareDoubleWithString() {
        BASE_DOUBLE_VALUE.compareTo(BASE_STRING_VALUE);
    }

    // ------------------------------------------------------------------------
    // Comparisons of String state values
    // ------------------------------------------------------------------------

    /**
     * Compare String with Integer state values (should fail)
     */
    @Test(expected = StateValueTypeException.class)
    public void compareStringWithInt() {
        BASE_STRING_VALUE.compareTo(BASE_INT_VALUE);
    }

    /**
     * Compare String with Long state values (should fail)
     */
    @Test(expected = StateValueTypeException.class)
    public void compareStringWithLong() {
        BASE_STRING_VALUE.compareTo(BASE_LONG_VALUE);
    }

    /**
     * Compare String with Double state values (should fail)
     */
    @Test(expected = StateValueTypeException.class)
    public void compareStringWithDouble() {
        BASE_STRING_VALUE.compareTo(BASE_DOUBLE_VALUE);
    }

    /**
     * Compare String state values together
     */
    @Test
    public void compareStringWithString() {
        assertTrue(BASE_STRING_VALUE.compareTo(BASE_STRING_VALUE) == 0);
        assertTrue(BASE_STRING_VALUE.compareTo(SMALLER_STRING_VALUE) > 0);
        assertTrue(BASE_STRING_VALUE.compareTo(BIGGER_STRING_VALUE) < 0);
    }

    /**
     * Compare String with Null state values
     */
    @Test
    public void compareStringWithNull() {
        assertTrue(BASE_STRING_VALUE.compareTo(NULL_VALUE) > 0);
    }

    // ------------------------------------------------------------------------
    // Comparisons of Null state values
    // ------------------------------------------------------------------------

    /**
     * Compare Null with Integer state values
     */
    @Test
    public void compareNullWithInt() {
        assertTrue(NULL_VALUE.compareTo(BASE_INT_VALUE) < 0);
    }

    /**
     * Compare Null with Long state values
     */
    @Test
    public void compareNullWithLong() {
        assertTrue(NULL_VALUE.compareTo(BASE_LONG_VALUE) < 0);
    }

    /**
     * Compare Null with Double state values
     */
    @Test
    public void compareNullWithDouble() {
        /* NullValue.unboxDouble returns NaN */
        assertTrue(NULL_VALUE.compareTo(BASE_DOUBLE_VALUE) > 0);
    }

    /**
     * Compare Null with String state values
     */
    @Test
    public void compareNullWithString() {
        assertTrue(NULL_VALUE.compareTo(BASE_STRING_VALUE) < 0);
    }

    /**
     * Compare Null state values together
     */
    @Test
    public void compareNullWithNull() {
        assertTrue(NULL_VALUE.compareTo(NULL_VALUE) == 0);
    }

}
