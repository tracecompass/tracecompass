/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.chart.ui.consumer;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.chart.core.aggregator.IConsumerAggregator;
import org.eclipse.tracecompass.internal.tmf.chart.core.consumer.IChartConsumer;
import org.eclipse.tracecompass.internal.tmf.chart.core.consumer.IDataConsumer;

import com.google.common.collect.ImmutableList;

/**
 * This class implements a {@link IChartConsumer} for a XY chart. It offers
 * optional aggregation of X and Y {@link IDataConsumer} that are called after
 * all the objects have been processed and allow to execute extra operations on
 * the whole dataset.
 *
 * TODO: This approach with aggregator forces us to have stateful data consumers
 * and then consume those consumers. Ideally, the consumer aggregator should
 * rather be additional consumers called in a andThen method.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class XYChartConsumer implements IChartConsumer {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private final List<XYSeriesConsumer> fSeries;
    private final @Nullable IConsumerAggregator fXAggregator;
    private final @Nullable IConsumerAggregator fYAggregator;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     *
     * @param series
     *            The list of XY series consumer
     * @param xAggregator
     *            An optional aggregator for the X consumers
     * @param yAggregator
     *            An optional aggregator for the Y consumers
     */
    public XYChartConsumer(List<XYSeriesConsumer> series,
            @Nullable IConsumerAggregator xAggregator,
            @Nullable IConsumerAggregator yAggregator) {
        fSeries = ImmutableList.copyOf(series);
        fXAggregator = xAggregator;
        fYAggregator = yAggregator;
    }

    // ------------------------------------------------------------------------
    // Overriden methods
    // ------------------------------------------------------------------------

    @Override
    public void accept(Object obj) {
        fSeries.forEach(consumer -> consumer.accept(obj));
    }

    @Override
    public void finish() {
        /* Aggregate X consumer if needed */
        IConsumerAggregator aggregatorX = fXAggregator;
        if (aggregatorX != null) {
            fSeries.forEach(s -> aggregatorX.accept(s.getXConsumer()));
        }

        /* Aggregate Y consumer if needed */
        IConsumerAggregator aggregatorY = fYAggregator;
        if (aggregatorY != null) {
            fSeries.forEach(s -> aggregatorY.accept(s.getYConsumer()));
        }
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Accessor that returns a collection of consumer, one for each series in
     * the chart.
     *
     * @return The list of series consumer
     */
    public Collection<XYSeriesConsumer> getSeries() {
        return fSeries;
    }

    /**
     * Accessor that return the X consumers aggregator, if present.
     *
     * @return The X consumers aggregator
     */
    public @Nullable IConsumerAggregator getXAggregator() {
        return fXAggregator;
    }

    /**
     * Accessor that return the Y consumers aggregator, if present.
     *
     * @return The Y consumers aggregator
     */
    public @Nullable IConsumerAggregator getYAggregator() {
        return fYAggregator;
    }

}
