/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.chart.ui.aggregator;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.chart.ui.consumer.IDataConsumer;
import org.eclipse.tracecompass.internal.tmf.chart.ui.consumer.NumericalConsumer;
import org.eclipse.tracecompass.internal.tmf.chart.ui.data.ChartRange;
import org.eclipse.tracecompass.internal.tmf.chart.ui.data.ChartRangeMap;

/**
 * This class is used for processing {@link NumericalConsumer} after they are
 * done processing objects from the stream of data. Right now, it only computes
 * the total range of multiple consumers.
 *
 * FIXME: This should not be implemented as a Consumer. A consumer should be
 * stateless. This could be implemented via a andThen on the data consumers and
 * modify an object that contains the information of this class.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class NumericalConsumerAggregator implements IConsumerAggregator {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private @Nullable BigDecimal fMinimum;
    private @Nullable BigDecimal fMaximum;

    // ------------------------------------------------------------------------
    // Overriden Methods
    // ------------------------------------------------------------------------

    @Override
    public void accept(IDataConsumer obj) {
        NumericalConsumer consumer = (NumericalConsumer) obj;

        BigDecimal nextMin = new BigDecimal(consumer.getMin().toString());
        BigDecimal nextMax = new BigDecimal(consumer.getMax().toString());

        BigDecimal min = fMinimum;
        BigDecimal max = fMaximum;

        /* Set initial min and max values */
        if (min == null || max == null) {
            fMinimum = nextMin;
            fMaximum = nextMax;

            return;
        }

        /* Update min and max values */
        fMinimum = min.min(nextMin);
        fMaximum = max.max(nextMax);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Accessor that returns a range map of all the numerical consumers. The
     * internal range of the map created with default values and the external
     * range is created from the computed minimum and maximum values of this
     * aggregator.
     *
     * @return The chart range map that covers all data points
     */
    public ChartRangeMap getChartRanges() {
        BigDecimal min = fMinimum;
        BigDecimal max = fMaximum;

        if (min == null || max == null) {
            return new ChartRangeMap();
        }

        ChartRange external = new ChartRange(min, max);
        return new ChartRangeMap(external);
    }

}
