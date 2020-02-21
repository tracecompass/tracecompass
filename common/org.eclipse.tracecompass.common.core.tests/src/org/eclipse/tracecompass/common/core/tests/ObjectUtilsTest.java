/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.common.core.tests;

import static org.eclipse.tracecompass.common.core.ObjectUtils.deepEquals;
import static org.eclipse.tracecompass.common.core.ObjectUtils.deepHashCode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.tracecompass.common.core.ObjectUtils;
import org.junit.Test;

/**
 * Unit tests for the ObjectUtils class.
 */
public class ObjectUtilsTest {

    /*
     * '1' is always equal to '2' and not equal to '3'.
     */
    private static final Object STRING1 = new String("alpha");
    private static final Object STRING2 = new String("alpha");
    private static final Object STRING3 = new String("bravo");
    private static final Object[] OBJECTARR1 = new Object[] { STRING1, STRING2, null };
    private static final Object[] OBJECTARR2 = new Object[] { STRING2, STRING1, null };
    private static final Object[] OBJECTARR3 = new Object[] { STRING1, STRING3, null };
    private static final boolean[] BOOLEANARR1 = new boolean[] { true, true };
    private static final boolean[] BOOLEANARR2 = new boolean[] { true, true };
    private static final boolean[] BOOLEANARR3 = new boolean[] { true, false };
    private static final char[] CHARARR1 = new char[] { 'a', 'b' };
    private static final char[] CHARARR2 = new char[] { 'a', 'b' };
    private static final char[] CHARARR3 = new char[] { 'a', 'c' };
    private static final byte[] BYTEARR1 = new byte[] { 1, 2 };
    private static final byte[] BYTEARR2 = new byte[] { 1, 2 };
    private static final byte[] BYTEARR3 = new byte[] { 1, 3 };
    private static final short[] SHORTARR1 = new short[] { 1, 2 };
    private static final short[] SHORTARR2 = new short[] { 1, 2 };
    private static final short[] SHORTARR3 = new short[] { 1, 3 };
    private static final int[] INTARR1 = new int[] { 1, 2 };
    private static final int[] INTARR2 = new int[] { 1, 2 };
    private static final int[] INTARR3 = new int[] { 1, 3 };
    private static final long[] LONGARR1 = new long[] { 1L, 2L };
    private static final long[] LONGARR2 = new long[] { 1L, 2L };
    private static final long[] LONGARR3 = new long[] { 1L, 3L };
    private static final float[] FLOATARR1 = new float[] { 1.0f, 2.0f };
    private static final float[] FLOATARR2 = new float[] { 1.0f, 2.0f };
    private static final float[] FLOATARR3 = new float[] { 1.0f, 3.0f };
    private static final double[] DOUBLEARR1 = new double[] { 1.0, 2.0 };
    private static final double[] DOUBLEARR2 = new double[] { 1.0, 2.0 };
    private static final double[] DOUBLEARR3 = new double[] { 1.0, 3.0 };
    private static final int[][] INTARRARR1 = new int[][] { INTARR1, INTARR2 };
    private static final int[][] INTARRARR2 = new int[][] { INTARR2, INTARR1 };
    private static final int[][] INTARRARR3 = new int[][] { INTARR1, INTARR3 };
    private static final Object[][] OBJECTARRARR1 = new Object[][] { OBJECTARR1, OBJECTARR2 };
    private static final Object[][] OBJECTARRARR2 = new Object[][] { OBJECTARR2, OBJECTARR1 };
    private static final Object[][] OBJECTARRARR3 = new Object[][] { OBJECTARR1, OBJECTARR3 };
    private static final List<?> OBJECTLIST1 = Arrays.asList(STRING1, STRING2);
    private static final List<?> OBJECTLIST2 = Arrays.asList(STRING2, STRING1);
    private static final List<?> OBJECTLIST3 = Arrays.asList(STRING1, STRING3);
    private static final List<?> OBJECTLIST4 = Arrays.asList(STRING3, STRING1);
    private static final List<?> OBJECTLIST5 = Arrays.asList(STRING1, STRING2, STRING3);
    private static final List<?> OBJECTARRLIST1 = Arrays.asList(OBJECTARR1, OBJECTARR2);
    private static final List<?> OBJECTARRLIST2 = Arrays.asList(OBJECTARR2, OBJECTARR1);
    private static final List<?> OBJECTARRLIST3 = Arrays.asList(OBJECTARR1, OBJECTARR3);
    private static final List<?> OBJECTARRLIST4 = Arrays.asList(OBJECTARR3, OBJECTARR1);
    private static final List<?> OBJECTARRLIST5 = Arrays.asList(OBJECTARR1, OBJECTARR2, OBJECTARR3);
    private static final Set<?> OBJECTSET1 = new HashSet<>(Arrays.asList(STRING1));
    private static final Set<?> OBJECTSET2 = new HashSet<>(Arrays.asList(STRING2));
    private static final Set<?> OBJECTSET3 = new HashSet<>(Arrays.asList(STRING3));
    private static final Set<?> OBJECTSET4 = new HashSet<>(Arrays.asList(STRING1, STRING3));
    private static final Set<?> OBJECTSET5 = new HashSet<>(Arrays.asList(STRING3, STRING2));
    private static final Set<?> OBJECTARRSET1 = new HashSet<>(Arrays.asList(OBJECTARR1));
    private static final Set<?> OBJECTARRSET2 = new HashSet<>(Arrays.asList(OBJECTARR2));
    private static final Set<?> OBJECTARRSET3 = new HashSet<>(Arrays.asList(OBJECTARR3));
    private static final Set<?> OBJECTARRSET4 = new HashSet<>(Arrays.asList(OBJECTARR1, OBJECTARR3));
    private static final Set<?> OBJECTARRSET5 = new HashSet<>(Arrays.asList(OBJECTARR3, OBJECTARR2));
    private static final Object[] MIXEDARR1 = new Object[] { STRING1, INTARR1, OBJECTARRLIST1, OBJECTARRSET1 };
    private static final Object[] MIXEDARR2 = new Object[] { STRING2, INTARR2, OBJECTARRLIST2, OBJECTARRSET2 };
    private static final Object[] MIXEDARR3 = new Object[] { STRING1, INTARR1, OBJECTARRLIST1, OBJECTARRSET3 };
    private static final List<?> MIXEDLIST1 = Arrays.asList(STRING1, INTARR1, OBJECTARRLIST1, OBJECTARRSET1);
    private static final List<?> MIXEDLIST2 = Arrays.asList(STRING2, INTARR2, OBJECTARRLIST2, OBJECTARRSET2);
    private static final List<?> MIXEDLIST3 = Arrays.asList(STRING1, INTARR1, OBJECTARRLIST1, OBJECTARRSET3);
    private static final Set<?> MIXEDSET1 = new HashSet<>(Arrays.asList(STRING1, INTARR1, OBJECTARRLIST1, OBJECTARRSET1));
    private static final Set<?> MIXEDSET2 = new HashSet<>(Arrays.asList(STRING2, INTARR2, OBJECTARRLIST2, OBJECTARRSET2));
    private static final Set<?> MIXEDSET3 = new HashSet<>(Arrays.asList(STRING1, INTARR1, OBJECTARRLIST1, OBJECTARRSET3));

    /**
     * Test method for {@link ObjectUtils#deepEquals(Object, Object)}
     */
    @Test
    public void testDeepEquals() {
        assertTrue(deepEquals(null, null));
        assertFalse(deepEquals(null, STRING1));
        assertFalse(deepEquals(STRING1, null));
        assertTrue(deepEquals(STRING1, STRING1));
        assertTrue(deepEquals(STRING1, STRING2));
        assertTrue(deepEquals(STRING2, STRING1));
        assertFalse(deepEquals(STRING1, STRING3));
        assertFalse(deepEquals(STRING3, STRING1));

        assertTrue(deepEquals(OBJECTARR1, OBJECTARR2));
        assertTrue(deepEquals(OBJECTARR2, OBJECTARR1));
        assertFalse(deepEquals(OBJECTARR1, OBJECTARR3));
        assertFalse(deepEquals(OBJECTARR3, OBJECTARR1));
        assertTrue(deepEquals(BOOLEANARR1, BOOLEANARR2));
        assertTrue(deepEquals(BOOLEANARR2, BOOLEANARR1));
        assertFalse(deepEquals(BOOLEANARR1, BOOLEANARR3));
        assertFalse(deepEquals(BOOLEANARR3, BOOLEANARR1));
        assertTrue(deepEquals(CHARARR1, CHARARR2));
        assertTrue(deepEquals(CHARARR2, CHARARR1));
        assertFalse(deepEquals(CHARARR1, CHARARR3));
        assertFalse(deepEquals(CHARARR3, CHARARR1));
        assertTrue(deepEquals(BYTEARR1, BYTEARR2));
        assertTrue(deepEquals(BYTEARR2, BYTEARR1));
        assertFalse(deepEquals(BYTEARR1, BYTEARR3));
        assertFalse(deepEquals(BYTEARR3, BYTEARR1));
        assertTrue(deepEquals(SHORTARR1, SHORTARR2));
        assertTrue(deepEquals(SHORTARR2, SHORTARR1));
        assertFalse(deepEquals(SHORTARR1, SHORTARR3));
        assertFalse(deepEquals(SHORTARR3, SHORTARR1));
        assertTrue(deepEquals(INTARR1, INTARR2));
        assertTrue(deepEquals(INTARR2, INTARR2));
        assertFalse(deepEquals(INTARR1, INTARR3));
        assertFalse(deepEquals(INTARR3, INTARR1));
        assertTrue(deepEquals(LONGARR1, LONGARR2));
        assertTrue(deepEquals(LONGARR2, LONGARR1));
        assertFalse(deepEquals(LONGARR1, LONGARR3));
        assertFalse(deepEquals(LONGARR3, LONGARR1));
        assertTrue(deepEquals(FLOATARR1, FLOATARR2));
        assertTrue(deepEquals(FLOATARR2, FLOATARR1));
        assertFalse(deepEquals(FLOATARR1, FLOATARR3));
        assertFalse(deepEquals(FLOATARR3, FLOATARR1));
        assertTrue(deepEquals(DOUBLEARR1, DOUBLEARR2));
        assertTrue(deepEquals(DOUBLEARR2, DOUBLEARR1));
        assertFalse(deepEquals(DOUBLEARR1, DOUBLEARR3));
        assertFalse(deepEquals(DOUBLEARR3, DOUBLEARR1));
        assertTrue(deepEquals(INTARRARR1, INTARRARR2));
        assertTrue(deepEquals(INTARRARR2, INTARRARR1));
        assertFalse(deepEquals(INTARRARR1, INTARRARR3));
        assertFalse(deepEquals(INTARRARR3, INTARRARR1));
        assertTrue(deepEquals(OBJECTARRARR1, OBJECTARRARR2));
        assertTrue(deepEquals(OBJECTARRARR2, OBJECTARRARR1));
        assertFalse(deepEquals(OBJECTARRARR1, OBJECTARRARR3));
        assertFalse(deepEquals(OBJECTARRARR3, OBJECTARRARR1));

        assertTrue(deepEquals(OBJECTLIST1, OBJECTLIST2));
        assertTrue(deepEquals(OBJECTLIST2, OBJECTLIST1));
        assertFalse(deepEquals(OBJECTLIST1, OBJECTLIST3));
        assertFalse(deepEquals(OBJECTLIST3, OBJECTLIST1));
        assertFalse(deepEquals(OBJECTLIST1, OBJECTLIST4));
        assertFalse(deepEquals(OBJECTLIST4, OBJECTLIST1));
        assertFalse(deepEquals(OBJECTLIST3, OBJECTLIST4));
        assertFalse(deepEquals(OBJECTLIST4, OBJECTLIST3));
        assertFalse(deepEquals(OBJECTLIST1, OBJECTLIST5));
        assertFalse(deepEquals(OBJECTLIST5, OBJECTLIST1));
        assertTrue(deepEquals(OBJECTARRLIST1, OBJECTARRLIST2));
        assertTrue(deepEquals(OBJECTARRLIST2, OBJECTARRLIST1));
        assertFalse(deepEquals(OBJECTARRLIST1, OBJECTARRLIST3));
        assertFalse(deepEquals(OBJECTARRLIST3, OBJECTARRLIST1));
        assertFalse(deepEquals(OBJECTARRLIST1, OBJECTARRLIST4));
        assertFalse(deepEquals(OBJECTARRLIST4, OBJECTARRLIST1));
        assertFalse(deepEquals(OBJECTARRLIST3, OBJECTARRLIST4));
        assertFalse(deepEquals(OBJECTARRLIST4, OBJECTARRLIST3));
        assertFalse(deepEquals(OBJECTARRLIST1, OBJECTARRLIST5));
        assertFalse(deepEquals(OBJECTARRLIST5, OBJECTARRLIST1));

        assertTrue(deepEquals(OBJECTSET1, OBJECTSET2));
        assertTrue(deepEquals(OBJECTSET2, OBJECTSET1));
        assertFalse(deepEquals(OBJECTSET1, OBJECTSET3));
        assertFalse(deepEquals(OBJECTSET3, OBJECTSET1));
        assertFalse(deepEquals(OBJECTSET1, OBJECTSET4));
        assertFalse(deepEquals(OBJECTSET4, OBJECTSET1));
        assertTrue(deepEquals(OBJECTSET4, OBJECTSET5));
        assertTrue(deepEquals(OBJECTSET5, OBJECTSET4));
        assertTrue(deepEquals(OBJECTARRSET1, OBJECTARRSET2));
        assertTrue(deepEquals(OBJECTARRSET2, OBJECTARRSET1));
        assertFalse(deepEquals(OBJECTARRSET1, OBJECTARRSET3));
        assertFalse(deepEquals(OBJECTARRSET3, OBJECTARRSET1));
        assertFalse(deepEquals(OBJECTARRSET1, OBJECTARRSET4));
        assertFalse(deepEquals(OBJECTARRSET4, OBJECTARRSET1));
        assertTrue(deepEquals(OBJECTARRSET4, OBJECTARRSET5));
        assertTrue(deepEquals(OBJECTARRSET5, OBJECTARRSET4));

        assertTrue(deepEquals(MIXEDARR1, MIXEDARR2));
        assertTrue(deepEquals(MIXEDARR2, MIXEDARR1));
        assertFalse(deepEquals(MIXEDARR1, MIXEDARR3));
        assertFalse(deepEquals(MIXEDARR3, MIXEDARR1));
        assertTrue(deepEquals(MIXEDLIST1, MIXEDLIST2));
        assertTrue(deepEquals(MIXEDLIST2, MIXEDLIST1));
        assertFalse(deepEquals(MIXEDLIST1, MIXEDLIST3));
        assertFalse(deepEquals(MIXEDLIST3, MIXEDLIST1));
        assertTrue(deepEquals(MIXEDSET1, MIXEDSET2));
        assertTrue(deepEquals(MIXEDSET2, MIXEDSET1));
        assertFalse(deepEquals(MIXEDSET1, MIXEDSET3));
        assertFalse(deepEquals(MIXEDSET3, MIXEDSET1));
    }

    /**
     * Test method for {@link ObjectUtils#deepHashCode(Object)}
     */
    @Test
    public void testDeepHashCode() {
        assertEquals(deepHashCode(null), deepHashCode(null));
        assertEquals(deepHashCode(STRING1), deepHashCode(STRING1));
        assertEquals(deepHashCode(STRING1), deepHashCode(STRING2));

        assertEquals(deepHashCode(OBJECTARR1), deepHashCode(OBJECTARR2));
        assertEquals(deepHashCode(BOOLEANARR1), deepHashCode(BOOLEANARR2));
        assertEquals(deepHashCode(CHARARR1), deepHashCode(CHARARR2));
        assertEquals(deepHashCode(BYTEARR1), deepHashCode(BYTEARR2));
        assertEquals(deepHashCode(SHORTARR1), deepHashCode(SHORTARR2));
        assertEquals(deepHashCode(INTARR1), deepHashCode(INTARR2));
        assertEquals(deepHashCode(LONGARR1), deepHashCode(LONGARR2));
        assertEquals(deepHashCode(FLOATARR1), deepHashCode(FLOATARR2));
        assertEquals(deepHashCode(DOUBLEARR1), deepHashCode(DOUBLEARR2));
        assertEquals(deepHashCode(INTARRARR1), deepHashCode(INTARRARR2));
        assertEquals(deepHashCode(OBJECTARRARR1), deepHashCode(OBJECTARRARR2));

        assertEquals(deepHashCode(OBJECTLIST1), deepHashCode(OBJECTLIST2));
        assertEquals(deepHashCode(OBJECTARRLIST1), deepHashCode(OBJECTARRLIST2));

        assertEquals(deepHashCode(OBJECTSET1), deepHashCode(OBJECTSET2));
        assertEquals(deepHashCode(OBJECTARRSET1), deepHashCode(OBJECTARRSET2));

        assertEquals(deepHashCode(MIXEDARR1), deepHashCode(MIXEDARR2));
        assertEquals(deepHashCode(MIXEDLIST1), deepHashCode(MIXEDLIST2));
        assertEquals(deepHashCode(MIXEDSET1), deepHashCode(MIXEDSET2));
    }
}
