/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.chart.core.tests.chart;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartData;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartSeries;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.IDataChartDescriptor;
import org.eclipse.tracecompass.tmf.chart.core.tests.stubs.StubChartProvider;
import org.eclipse.tracecompass.tmf.chart.core.tests.stubs.StubObject;
import org.junit.Test;

/**
 * Test the {@link ChartData} class
 *
 * @author Geneviève Bastien
 */
public class ChartDataTest {

    private final @NonNull StubChartProvider fProvider = new StubChartProvider();

    /**
     * Test getting the descriptors for x and y series of a chart data
     */
    @Test
    public void testGetDescriptors() {
        // Create chart data with the string descriptor as x axis and a series
        // for every other y axis
        Collection<@NonNull IDataChartDescriptor<StubObject, ?>> dataDescriptors = fProvider.getDataDescriptors();
        IDataChartDescriptor<StubObject, ?> xDesc = null;
        for (IDataChartDescriptor<StubObject, ?> desc : dataDescriptors) {
            if (desc.getName().equals(StubChartProvider.STRING_DESCRIPTOR)) {
                xDesc = desc;
                break;
            }
        }
        assertNotNull(xDesc);

        List<@NonNull ChartSeries> list = new ArrayList<>();
        for (IDataChartDescriptor<StubObject, ?> desc : dataDescriptors) {
            if (!desc.getName().equals(StubChartProvider.STRING_DESCRIPTOR)) {
                list.add(new ChartSeries(xDesc, desc));
            }
        }
        assertEquals(3, list.size());
        ChartData data = new ChartData(fProvider, list);

        // Verify that the series correspond to what has been added
        Collection<@NonNull ChartSeries> chartSeries = data.getChartSeries();
        assertEquals(3, chartSeries.size());

        assertEquals(xDesc, data.getX(0));
        assertEquals(xDesc, data.getX(1));
        assertEquals(xDesc, data.getX(2));

        assertEquals(list.get(0).getY(), data.getY(0));
        assertEquals(list.get(1).getY(), data.getY(1));
        assertEquals(list.get(2).getY(), data.getY(2));

    }

    /**
     * Test that the series cannot be modified outside the class
     */
    @Test
    public void testImmutableSeries() {
        // Create chart data with only one series
        Iterator<@NonNull IDataChartDescriptor<StubObject, ?>> dataDescriptors = fProvider.getDataDescriptors().iterator();
        IDataChartDescriptor<StubObject, ?> x = dataDescriptors.next();
        IDataChartDescriptor<StubObject, ?> y = dataDescriptors.next();
        List<@NonNull ChartSeries> list = new ArrayList<>();
        list.add(new ChartSeries(x, y));
        ChartData data = new ChartData(fProvider, list);
        assertEquals(1, data.getChartSeries().size());

        // Add a new series to the list and make sure it is not in the data
        // (which should be immutable)
        y = dataDescriptors.next();
        list.add(new ChartSeries(x, y));
        assertEquals(1, data.getChartSeries().size());

    }
}
