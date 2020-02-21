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

package org.eclipse.tracecompass.tmf.chart.core.tests.consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.IDataChartDescriptor;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver.INumericalResolver;
import org.eclipse.tracecompass.internal.tmf.chart.core.consumer.NumericalConsumer;
import org.eclipse.tracecompass.tmf.chart.core.tests.stubs.StubChartProvider;
import org.eclipse.tracecompass.tmf.chart.core.tests.stubs.StubObject;
import org.junit.Test;

/**
 * Test the {@link NumericalConsumer} class
 *
 * @author Geneviève Bastien
 */
public class NumericalConsumerTest {

    private final @NonNull StubChartProvider fProvider = new StubChartProvider();

    /**
     * Test the numerical consumer with only the resolver. It should not accept
     * null values
     */
    @Test
    public void testResolver() {
        IDataChartDescriptor<StubObject, ?> descriptor = fProvider.getDataDescriptor(StubChartProvider.LONG_DESCRIPTOR);
        NumericalConsumer consumer = new NumericalConsumer(INumericalResolver.class.cast(descriptor.getResolver()));

        // Test with a first object
        StubObject obj = new StubObject("str", 1, 10L, 10.0);
        assertTrue(consumer.test(obj));
        consumer.accept(obj);
        assertEquals(Long.valueOf(10L), consumer.getData().get(0));

        // Test with a second object
        obj = new StubObject("str", 1, 20L, 10.0);
        assertTrue(consumer.test(obj));
        consumer.accept(obj);
        assertEquals(Long.valueOf(20L), consumer.getData().get(1));

        obj = new StubObject("str", 1, null, 10.0);
        assertFalse(consumer.test(obj));

        // Test eh minimum and maximum values
        assertEquals(Long.valueOf(10L), consumer.getMin());
        assertEquals(Long.valueOf(20L), consumer.getMax());
    }

    /**
     * Test the numerical consumer with only the resolver and predicate.
     */
    @Test
    public void testWithPredicate() {
        IDataChartDescriptor<StubObject, ?> descriptor = fProvider.getDataDescriptor(StubChartProvider.LONG_DESCRIPTOR);
        Predicate<@Nullable Number> predicate = d -> true;
        NumericalConsumer consumer = new NumericalConsumer(INumericalResolver.class.cast(descriptor.getResolver()), predicate);

        StubObject obj = new StubObject("str", 1, 10L, 10.0);
        assertTrue(consumer.test(obj));
        consumer.accept(obj);
        assertEquals(Long.valueOf(10L), consumer.getData().get(0));

        obj = new StubObject("str", 1, null, 10.0);
        assertTrue(consumer.test(obj));
        consumer.accept(obj);
        assertEquals(Long.valueOf(0L), consumer.getData().get(1));
    }

}
