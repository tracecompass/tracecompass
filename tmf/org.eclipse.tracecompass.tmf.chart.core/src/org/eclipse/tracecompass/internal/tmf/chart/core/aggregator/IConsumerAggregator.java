/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.chart.core.aggregator;

import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.chart.core.consumer.IDataConsumer;
import org.eclipse.tracecompass.internal.tmf.chart.core.consumer.NumericalConsumer;

/**
 * This interface is used for performing operations on multiple
 * {@link IDataConsumer}. As of right now, it is used for computing total range
 * of multiple {@link NumericalConsumer}. Normally, it should process the data
 * consumers only when they are done processing individual object from the
 * stream of data.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public interface IConsumerAggregator extends Consumer<@NonNull IDataConsumer> {

}
