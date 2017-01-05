/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.chart.core.tests.resolver;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver.AbstractLongResolver;
import org.eclipse.tracecompass.tmf.chart.core.tests.stubs.StubObject;
import org.junit.Test;

/**
 * Test the {@link AbstractLongResolver} class
 *
 * @author Geneviève Bastien
 */
public class LongResolverTest {

    private static final AbstractLongResolver<StubObject> LONG_RESOLVER = new AbstractLongResolver<StubObject>() {
        @Override
        public @NonNull Function<StubObject, @Nullable Long> getMapper() {
            return o -> o.getLong();
        }
    };

    /**
     * Test the limit values of the long resolver
     */
    @Test
    public void testLimitValues() {
        assertEquals((Long) Long.MIN_VALUE, LONG_RESOLVER.getMinValue());
        assertEquals((Long) Long.MAX_VALUE, LONG_RESOLVER.getMaxValue());
        assertEquals((Long) 0l, LONG_RESOLVER.getZeroValue());
    }

    /**
     * Test the {@link AbstractLongResolver#getComparator()} method
     */
    @Test
    public void testComparator() {
        List<@NonNull Long> list = new ArrayList<>();
        // Add a few items not ordered naturally
        list.add(0l);
        list.add(1234l);
        list.add(-1234l);
        Collections.sort(list, LONG_RESOLVER.getComparator());
        assertEquals(Long.valueOf(-1234l), list.get(0));
        assertEquals((Long) 0l, list.get(1));
        assertEquals((Long) 1234l, list.get(2));
    }

    /**
     * Test the {@link AbstractLongResolver#getMapper()} method
     */
    @Test
    public void testMapper() {
        Function<StubObject, @Nullable Long> mapper = LONG_RESOLVER.getMapper();

        StubObject obj = new StubObject("str", 1, -1234l, 0.0);
        assertEquals(Long.valueOf(-1234l), mapper.apply(obj));

        obj = new StubObject("str", 1, 0l, 0.0);
        assertEquals(Long.valueOf(0l), mapper.apply(obj));

        obj = new StubObject("str", 1, 1234l, 0.0);
        assertEquals(Long.valueOf(1234l), mapper.apply(obj));

    }

}
