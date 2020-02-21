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

package org.eclipse.tracecompass.internal.tmf.chart.core.consumer;

import java.util.function.Consumer;

/**
 * This interface consumes any data that comes from a {@link IChartConsumer}.
 * Every object that it receives is sent to its own {@link IDataConsumer}. The
 * main benefit of this consumer is that it can easily reject (x, y) couple that
 * are invalid for plotting. An object can be tested with
 * {@link IDataConsumer#test(Object)} before being consumed into an (x, y)
 * couple.
 * <p>
 * For example, a XY chart should have a series consumer for each of the plotted
 * series. There would have two consumer in a XY chart that plot the following
 * series: "start VS name" and "end VS name".
 *
 * @see IChartConsumer
 * @see IDataConsumer
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public interface ISeriesConsumer extends Consumer<Object> {

}
