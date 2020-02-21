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

package org.eclipse.tracecompass.internal.tmf.chart.ui.consumer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartSeries;
import org.eclipse.tracecompass.internal.tmf.chart.core.consumer.IDataConsumer;
import org.eclipse.tracecompass.internal.tmf.chart.core.consumer.ISeriesConsumer;

import com.google.common.collect.ImmutableList;

/**
 * This class implements a {@link ISeriesConsumer} for XY series. Such series
 * have two data consumers: one for the X axis and the other for the Y axis.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class XYSeriesConsumer implements ISeriesConsumer {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private final IDataConsumer fXConsumer;
    private final IDataConsumer fYConsumer;
    private final ChartSeries fChartSeries;
    private final List<Object> fConsumedElements = new ArrayList<>();

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructors.
     *
     * @param series
     *            The chart series related to this consumer
     * @param xConsumer
     *            The consumer for the X axis
     * @param yConsumer
     *            The consumer for the Y axis
     */
    public XYSeriesConsumer(ChartSeries series, IDataConsumer xConsumer, IDataConsumer yConsumer) {
        fChartSeries = series;
        fXConsumer = xConsumer;
        fYConsumer = yConsumer;
    }

    // ------------------------------------------------------------------------
    // Overriden methods
    // ------------------------------------------------------------------------

    @Override
    public void accept(Object obj) {
        /* Make sure every consumer can consume their value */
        if (!fXConsumer.test(obj) || !fYConsumer.test(obj)) {
            return;
        }

        /* Consume the value for each consumer */
        fXConsumer.accept(obj);
        fYConsumer.accept(obj);

        /* Add the object to the list of consumed objects */
        // FIXME: The object is kept for the signals to work correctly, but this
        // may cause OutOfMemoryExceptions if we keep all the consumed objects.
        fConsumedElements.add(obj);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Accessor that returns the chart series related to this consumer.
     *
     * FIXME: See if we can avoid exposing the series here
     *
     * @return The chart series
     */
    public ChartSeries getSeries() {
        return fChartSeries;
    }

    /**
     * Accessor that returns the list of valid consumed objects.
     *
     * @return The list of consumed objects
     */
    public Collection<Object> getConsumedElements() {
        return ImmutableList.copyOf(fConsumedElements);
    }

    /**
     * Accessor that returns the X consumer.
     *
     * TODO: Once consumer aggregators have been refactored, this method may not
     * be necessary
     *
     * @return The X consumer
     */
    public IDataConsumer getXConsumer() {
        return fXConsumer;
    }

    /**
     * Accessor that returns the Y consumer.
     *
     * TODO: Once consumer aggregators have been refactored, this method may not
     * be necessary
     *
     * @return The Y consumer
     */
    public IDataConsumer getYConsumer() {
        return fYConsumer;
    }

}
