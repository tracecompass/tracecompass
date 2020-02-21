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

package org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Comparator;

/**
 * Abstract class implementing a mapper that returns double values.
 *
 * @param <T>
 *            The type of the input
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public abstract class AbstractDoubleResolver<T> implements INumericalResolver<T, Double> {

    @Override
    public Comparator<Double> getComparator() {
        return checkNotNull(Comparator.naturalOrder());
    }

    @Override
    public Double getMinValue() {
        return Double.MIN_VALUE;
    }

    @Override
    public Double getMaxValue() {
        return Double.MAX_VALUE;
    }

    @Override
    public Double getZeroValue() {
        return 0.0;
    }

}
