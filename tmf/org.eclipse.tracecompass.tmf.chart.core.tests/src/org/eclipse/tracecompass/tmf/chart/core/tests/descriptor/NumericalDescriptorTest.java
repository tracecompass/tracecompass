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

package org.eclipse.tracecompass.tmf.chart.core.tests.descriptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.DataChartNumericalDescriptor;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver.AbstractLongResolver;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver.INumericalResolver;
import org.eclipse.tracecompass.tmf.chart.core.tests.stubs.StubObject;
import org.junit.Test;

/**
 * Test the {@link DataChartNumericalDescriptor} class
 *
 * @author Geneviève Bastien
 */
public class NumericalDescriptorTest {

    private static final @NonNull String DESC_NAME = "test";

    private final @NonNull INumericalResolver<StubObject, @NonNull Long> fResolver = new AbstractLongResolver<StubObject>() {

        @Override
        public @NonNull Function<StubObject, @Nullable Long> getMapper() {
            return o -> o.getLong();
        }
    };

    /**
     * Test the {@link DataChartNumericalDescriptor#DataChartNumericalDescriptor(String, INumericalResolver)} constructor
     */
    @Test
    public void testConstructor() {
        DataChartNumericalDescriptor<StubObject, @NonNull Long> desc = new DataChartNumericalDescriptor<>(DESC_NAME, fResolver);
        assertEquals(DESC_NAME, desc.getName());
        assertNull(desc.getUnit());
        assertEquals(DESC_NAME, desc.getLabel());
    }

    /**
     * Test the {@link DataChartNumericalDescriptor#DataChartNumericalDescriptor(String, INumericalResolver, String)} constructor
     */
    @Test
    public void testConstructorWithUnit() {
        String unit = "bla";
        DataChartNumericalDescriptor<StubObject, @NonNull Long> desc = new DataChartNumericalDescriptor<>(DESC_NAME, fResolver, unit);
        assertEquals(DESC_NAME, desc.getName());
        assertEquals(unit, desc.getUnit());
        assertEquals(DESC_NAME + " (" + unit + ')', desc.getLabel());
    }

}
