/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Comparator;

/**
 * Abstract class implementing a mapper that returns long values.
 *
 * @param <T>
 *            The type of the input
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public abstract class AbstractLongResolver<T> implements INumericalResolver<T, Long> {

    @Override
    public Comparator<Long> getComparator() {
        return checkNotNull(Comparator.naturalOrder());
    }

    @Override
    public Long getMinValue() {
        return Long.MIN_VALUE;
    }

    @Override
    public Long getMaxValue() {
        return Long.MAX_VALUE;
    }

    @Override
    public Long getZeroValue() {
        return 0l;
    }

}
