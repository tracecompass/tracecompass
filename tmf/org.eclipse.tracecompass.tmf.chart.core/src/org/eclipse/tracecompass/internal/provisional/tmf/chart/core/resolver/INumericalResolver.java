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

import java.util.Comparator;

/**
 * This interface is used for mapping numbers inside a stream of object. It
 * provides a {@link Comparator} for comparing consumed numbers. The zero value
 * of the returned type is also provided, this allows us to compare a number
 * with a zero easily.
 * <p>
 * FIXME: We support any kind of number. The {@link Number} class doesn't
 * provide methods that return important values that are used by the chart
 * plugin. If there is a better way of doind this, we could remove
 * {@link #getMinValue()}, {@link #getMaxValue()} and {@link #getZeroValue()}.
 *
 * @param <T>
 *            The type of the input
 * @param <R>
 *            The type of the number when the input is resolved
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public interface INumericalResolver<T, R extends Number> extends IDataResolver<T, R> {

    /**
     * This method returns the comparator used for comparing numbers of the
     * specified type.
     *
     * @return The comparator
     */
    Comparator<R> getComparator();

    /**
     * This method returns the minimum value supported by the specified type.
     *
     * @return The minimum value
     */
    R getMinValue();

    /**
     * This method returns the maximum value supported by the specified type.
     *
     * @return The maximum value
     */
    R getMaxValue();

    /**
     * This method returns the value of the specified type that is equivalent to
     * zero.
     *
     * @return The value equivalent to zero
     */
    R getZeroValue();

}
