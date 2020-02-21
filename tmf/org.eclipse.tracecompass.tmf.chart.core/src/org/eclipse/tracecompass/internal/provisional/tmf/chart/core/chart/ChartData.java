/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart;

import java.util.Collection;
import java.util.List;

import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.IDataChartDescriptor;
import org.eclipse.tracecompass.tmf.chart.core.model.IDataChartProvider;

import com.google.common.collect.ImmutableList;

/**
 * This class contains the data used to populate a chart.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class ChartData {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private final IDataChartProvider<?> fProvider;
    private final List<ChartSeries> fSeries;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     *
     * @param provider
     *            The provider of data
     * @param series
     *            The list of series to plot
     */
    public ChartData(IDataChartProvider<?> provider, List<ChartSeries> series) {
        fProvider = provider;
        fSeries = ImmutableList.copyOf(series);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Accessor that returns the data provider.
     *
     * @return The data provider
     */
    public IDataChartProvider<?> getDataProvider() {
        return fProvider;
    }

    /**
     * Accessor that returns a read-only list of the series.
     *
     * @return The list of series
     */
    public Collection<ChartSeries> getChartSeries() {
        return fSeries;
    }

    /**
     * Accessor that returns a specific X descriptor from a series.
     *
     * @param index
     *            The index of the series
     * @return The data descriptor
     */
    public IDataChartDescriptor<?, ?> getX(int index) {
        return fSeries.get(index).getX();
    }

    /**
     * Accessor that returns a specific Y descriptor from a series.
     *
     * @param index
     *            The index of the series
     * @return The data descriptor
     */
    public IDataChartDescriptor<?, ?> getY(int index) {
        return fSeries.get(index).getY();
    }

}
