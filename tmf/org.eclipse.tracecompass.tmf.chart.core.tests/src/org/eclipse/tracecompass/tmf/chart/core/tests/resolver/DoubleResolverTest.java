/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.chart.core.tests.resolver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver.AbstractDoubleResolver;
import org.eclipse.tracecompass.tmf.chart.core.tests.stubs.StubObject;
import org.junit.Test;

/**
 * Test the {@link AbstractDoubleResolver} class
 *
 * @author Geneviève Bastien
 */
public class DoubleResolverTest {

    private static final AbstractDoubleResolver<StubObject> DOUBLE_RESOLVER = new AbstractDoubleResolver<StubObject>() {
        @Override
        public @NonNull Function<StubObject, @Nullable Double> getMapper() {
            return o -> o.getDbl();
        }
    };

    /**
     * Test the limit values of the long resolver
     */
    @Test
    public void testLimitValues() {
        assertEquals(Double.MIN_VALUE, DOUBLE_RESOLVER.getMinValue(), 0.000001);
        assertEquals(Double.MAX_VALUE, DOUBLE_RESOLVER.getMaxValue(), 0.000001);
        assertEquals(0.0, DOUBLE_RESOLVER.getZeroValue(), 0.000001);
    }

    /**
     * Test the {@link AbstractDoubleResolver#getComparator()} method
     */
    @Test
    public void testComparator() {
        List<@NonNull Double> list = new ArrayList<>();
        // Add a few items not ordered naturally
        list.add(0.0);
        list.add(1234.1234);
        list.add(-3.45);
        Collections.sort(list, DOUBLE_RESOLVER.getComparator());
        assertEquals(Double.valueOf(-3.45), list.get(0), 0.000001);
        assertEquals(0.0, list.get(1), 0.000001);
        assertEquals(1234.1234, list.get(2), 0.000001);
    }

    /**
     * Test the {@link AbstractDoubleResolver#getMapper()} method
     */
    @Test
    public void testMapper() {
        Function<StubObject, @Nullable Double> mapper = DOUBLE_RESOLVER.getMapper();

        StubObject obj = new StubObject("str", 1, 0l, -3.45);
        Double dbl = mapper.apply(obj);
        assertNotNull(dbl);
        assertEquals(Double.valueOf(-3.45), dbl, 0.000001);

        obj = new StubObject("str", 1, 0l, 0.0);
        dbl = mapper.apply(obj);
        assertNotNull(dbl);
        assertEquals(0.0, dbl, 0.000001);

        obj = new StubObject("str", 1, 1234l, 1234.1234);
        dbl = mapper.apply(obj);
        assertNotNull(dbl);
        assertEquals(Double.valueOf(1234.1234), dbl, 0.000001);

    }

}
