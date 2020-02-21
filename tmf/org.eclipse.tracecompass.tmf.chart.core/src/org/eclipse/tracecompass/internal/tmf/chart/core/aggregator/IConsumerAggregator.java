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
