/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc. and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;
import org.junit.After;
import org.junit.Test;

/**
 * Tests for the {@link TmfEventField#equals} method, when using complex types
 * of values.
 */
public class TmfEventFieldValueEqualsTest {

    private static final @NonNull String FIELD_NAME = "Field1";

    private TmfEventField fField1;
    private TmfEventField fField2;

    /** Test cleanup */
    @After
    public void tearDown() {
        fField1 = null;
        fField2 = null;
    }

    private void createFields(Object value1, Object value2) {
        fField1 = new TmfEventField(FIELD_NAME, value1, null);
        fField2 = new TmfEventField(FIELD_NAME, value2, null);
    }

    /**
     * Test with values being Object[] arrays.
     */
    @Test
    public void testValueArrayObject() {
        Object object1 = new Object();
        Object object2 = new Object();
        assertNotEquals(object1, object2);

        Object[] value1 = new Object[] { object1, object2 };
        Object[] value2 = new Object[] { object1, object2 };
        assertNotEquals(value1, value2);
        assertTrue(Arrays.equals(value1, value2));

        createFields(value1, value2);
        assertEquals(fField1, fField2);
    }

    /**
     * Test with values being primitive arrays.
     */
    @Test
    public void testValueArrayPrimitives() {
        long[] value1 = new long[] { 10, 20 };
        long[] value2 = new long[] { 10, 20 };
        assertNotEquals(value1, value2);
        assertTrue(Arrays.equals(value1, value2));

        createFields(value1, value2);
        assertEquals(fField1, fField2);
    }

    /**
     * Test with values being both primitive arrays, but different ones.
     */
    @Test
    public void testValueArrayPrimitivesNotEquals() {
        long[] value1 = new long[] { 10, 20 };
        long[] value2 = new long[] { 10, 30 };

        createFields(value1, value2);
        assertNotEquals(fField1, fField2);
    }

    /**
     * Test with values being arrays of arrays of primitives.
     */
    @Test
    public void testValueArrayOfArrays() {
        long[][] value1 = new long[][] { { 10, 20 }, { 10, 30 } };
        long[][] value2 = new long[][] { { 10, 20 }, { 10, 30 } };
        assertNotEquals(value1, value2);
        assertTrue(Arrays.deepEquals(value1, value2));

        createFields(value1, value2);
        assertEquals(fField1, fField2);
    }

    /**
     * Test with values being arrays of arrays of primitives, in a case where
     * they are not equals.
     */
    @Test
    public void testValueArrayOfArraysNotEquals() {
        long[][] value1 = new long[][] { { 10, 20 }, { 10, 30 } };
        long[][] value2 = new long[][] { { 10, 20 }, { 15, 40 } };
        assertNotEquals(value1, value2);
        assertFalse(Arrays.deepEquals(value1, value2));

        createFields(value1, value2);
        assertNotEquals(fField1, fField2);
    }

    /**
     * Test with values being arrays, but of different element types.
     */
    @Test
    public void testValueArrayMismatchedTypes() {
        Object[] value1 = new Object[] { new Object(), new Object() };
        long[] value2 = new long[] { 10, 20 };

        createFields(value1, value2);
        assertNotEquals(fField1, fField2);
    }

    /**
     * Test with values being one array, and one other standard type.
     */
    @Test
    public void testValueArrayAndOther() {
        long[] value1 = new long[] { 10, 20 };
        Integer value2 = Integer.valueOf(10);

        createFields(value1, value2);
        assertNotEquals(fField1, fField2);
    }
}
