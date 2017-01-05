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

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartSeries;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.IDataChartDescriptor;
import org.eclipse.tracecompass.tmf.chart.core.tests.stubs.StubChartProvider;
import org.eclipse.tracecompass.tmf.chart.core.tests.stubs.StubObject;
import org.junit.Test;

/**
 * Test the {@link ChartSeries} class
 *
 * @author Geneviève Bastien
 */
public class ChartSeriesTest {

    private final StubChartProvider fProvider = new StubChartProvider();

    /**
     * Test the constructor
     */
    @Test
    public void testConstructor() {
        Collection<@NonNull IDataChartDescriptor<StubObject, ?>> dataDescriptors = fProvider.getDataDescriptors();
        IDataChartDescriptor<StubObject, ?> x = dataDescriptors.iterator().next();
        IDataChartDescriptor<StubObject, ?> y = dataDescriptors.iterator().next();
        ChartSeries series = new ChartSeries(x, y);
        assertEquals(x, series.getX());
        assertEquals(y, series.getY());
    }
}
