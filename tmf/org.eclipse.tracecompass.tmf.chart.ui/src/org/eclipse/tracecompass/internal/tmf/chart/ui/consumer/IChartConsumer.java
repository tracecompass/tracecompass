/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.chart.ui.consumer;

import java.util.function.Consumer;

/**
 * This interface is the entry point for consuming data in the chart plugin.
 * Each chart type should implement this interface. When an object needs to be
 * consumed, it is first sent to this consumer which passes the object all its
 * {@link ISeriesConsumer}. When all the data has been consumed,
 * {@link #finish()} is called.
 * <p>
 * Consider a XY chart with <i>n</i> series where both axes are numerical. Thus,
 * the implemented class should have <i>n</i> {@link ISeriesConsumer}. This
 * consumer first receives an object from the stream of data and passes it to
 * all its series consumer. Since both axes are numerical, the series consumer
 * test and send the object to their X and Y {@link IDataConsumer}. This last
 * consumer contains the final series that can be plotted. When all the data has
 * been processed, the {@link #finish()} method is called. In the case of an XY
 * chart with numerical axes, it is used for computing the total range of the
 * all the series.
 *
 * @see ISeriesConsumer
 * @see IDataConsumer
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public interface IChartConsumer extends Consumer<Object> {

    /**
     * This method is called when all the data has been processed. It can be
     * used by the implemented chart consumer or not.
     */
    void finish();

}
