/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.synchronization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.core.synchronization.TmfConstantTransform;
import org.eclipse.tracecompass.internal.tmf.core.synchronization.TmfTimestampTransform;
import org.eclipse.tracecompass.internal.tmf.core.synchronization.TmfTimestampTransformLinear;
import org.eclipse.tracecompass.tmf.core.synchronization.ITmfTimestampTransform;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.junit.Test;

/**
 * Tests for {@link TmfTimestampTransform} and its descendants
 *
 * @author Geneviève Bastien
 */
@SuppressWarnings("nls")
public class TsTransformTest {

    private static final long ts = 1361657893526374091L;
    private static final @NonNull ITmfTimestamp oTs = TmfTimestamp.fromSeconds(ts);

    /**
     * Test the linear transform
     */
    @Test
    public void testLinearTransform() {
        /* Default constructor */
        TmfTimestampTransformLinear ttl = new TmfTimestampTransformLinear();
        assertEquals(1361657893526374091L, ttl.transform(ts));
        assertEquals(1361657893526374091L, ttl.transform(oTs).getValue());

        /* Just an offset */
        ttl = new TmfTimestampTransformLinear(BigDecimal.valueOf(1.0), BigDecimal.valueOf(3));
        assertEquals(1361657893526374094L, ttl.transform(ts));
        assertEquals(1361657893526374094L, ttl.transform(oTs).getValue());

        /* Just a slope */
        ttl = new TmfTimestampTransformLinear(BigDecimal.valueOf(2.0), BigDecimal.valueOf(0));
        assertEquals(2723315787052748182L, ttl.transform(ts));
        assertEquals(2723315787052748182L, ttl.transform(oTs).getValue());

        /* Offset and slope */
        ttl = new TmfTimestampTransformLinear(BigDecimal.valueOf(2.0), BigDecimal.valueOf(3));
        assertEquals(2723315787052748185L, ttl.transform(ts));
        assertEquals(2723315787052748185L, ttl.transform(oTs).getValue());

        /* Offset and slope */
        ttl = new TmfTimestampTransformLinear(BigDecimal.valueOf(0.5), BigDecimal.valueOf(0));
        assertEquals(680828946763187045L, ttl.transform(ts));
        assertEquals(680828946763187045L, ttl.transform(oTs).getValue());
    }

    /**
     * Test for the identity transform
     */
    @Test
    public void testIdentityTransform() {
        ITmfTimestampTransform tt = TmfTimestampTransform.IDENTITY;
        assertEquals(ts, tt.transform(ts));
        assertEquals(oTs, tt.transform(oTs));
    }

    /**
     * Test hash and equals function
     */
    @Test
    public void testEquality() {
        Map<ITmfTimestampTransform, String> map = new HashMap<>();
        ITmfTimestampTransform ttl = new TmfTimestampTransformLinear(BigDecimal.valueOf(2.0), BigDecimal.valueOf(3));
        ITmfTimestampTransform ttl2 = new TmfTimestampTransformLinear(BigDecimal.valueOf(2.0), BigDecimal.valueOf(3));
        ITmfTimestampTransform ttl3 = new TmfTimestampTransformLinear(BigDecimal.valueOf(3), BigDecimal.valueOf(3));
        assertEquals(ttl, ttl2);
        assertFalse(ttl.equals(ttl3));
        assertFalse(ttl2.equals(ttl3));

        map.put(ttl, "a");
        assertTrue(map.containsKey(ttl2));
        assertEquals("a", map.get(ttl));

        ITmfTimestampTransform ti = TmfTimestampTransform.IDENTITY;
        assertEquals(TmfTimestampTransform.IDENTITY, ti);
        assertFalse(TmfTimestampTransform.IDENTITY.equals(ttl));

        map.put(ti, "b");
        assertTrue(map.containsKey(TmfTimestampTransform.IDENTITY));
        assertEquals("b", map.get(ti));

        assertFalse(ti.equals(ttl));
        assertFalse(ttl.equals(ti));
    }

    /**
     * Test the equality of {@link TmfConstantTransform} objects.
     */
    @Test
    public void testEqualityConstantTransform() {
        ITmfTimestampTransform tt1 = new TmfConstantTransform(50L);
        ITmfTimestampTransform tt2 = new TmfConstantTransform(50L);
        ITmfTimestampTransform tt3 = new TmfConstantTransform(-10L);

        assertEquals(tt1, tt2);
        assertNotEquals(tt1, tt3);
        assertNotEquals(tt2, tt3);
    }

    /**
     * Test the transform composition function
     */
    @Test
    public void testComposition() {
        long t = 100;
        ITmfTimestampTransform ti = TmfTimestampTransform.IDENTITY;
        ITmfTimestampTransform ttl = new TmfTimestampTransformLinear(BigDecimal.valueOf(2.0), BigDecimal.valueOf(3));
        ITmfTimestampTransform ttl2 = new TmfTimestampTransformLinear(BigDecimal.valueOf(1.5), BigDecimal.valueOf(8));

        ITmfTimestampTransform tc1 = ti.composeWith(ttl);
        /* Should be ttl */
        assertEquals(ttl, tc1);
        assertEquals(203, tc1.transform(t));

        tc1 = ttl.composeWith(ti);
        /* Should be ttl also */
        assertEquals(ttl, tc1);
        assertEquals(203, tc1.transform(t));

        tc1 = ti.composeWith(ti);
        /* Should be identity */
        assertEquals(tc1, TmfTimestampTransform.IDENTITY);
        assertEquals(100, tc1.transform(t));

        tc1 = ttl.composeWith(ttl2);
        assertEquals(ttl.transform(ttl2.transform(t)), tc1.transform(t));
        assertEquals(319, tc1.transform(t));

        tc1 = ttl2.composeWith(ttl);
        assertEquals(ttl2.transform(ttl.transform(t)), tc1.transform(t));
        assertEquals(312, tc1.transform(t));

    }

}
