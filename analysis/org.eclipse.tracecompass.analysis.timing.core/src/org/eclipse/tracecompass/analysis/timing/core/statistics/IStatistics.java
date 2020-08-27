/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.core.statistics;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Interface for classes implementing statistics. These statistics take a
 * generic object type, so that they can keep information on the objects that
 * have the minimum and maximum value. Implementations will need to be told how
 * to transform the objects of generic type into a Number that can be used for
 * statistics calculations.
 *
 * @author Geneviève Bastien
 * @param <E>
 *            The type of object to calculate statistics on
 * @since 1.3
 */
public interface IStatistics<@NonNull E> {

    /**
     * Get minimum value
     *
     * @return minimum value
     */
    long getMin();

    /**
     * Get maximum value
     *
     * @return maximum value
     */
    long getMax();

    /**
     * Get minimum number
     *
     * @return minimum number
     * @since 5.2
     */
    default Number getMinNumber() {
        return getMin();
    }

    /**
     * Get maximum number
     *
     * @return maximum number
     * @since 5.2
     */
    default Number getMaxNumber() {
        return getMax();
    }

    /**
     * Get element with minimum value, or <code>null</code> if
     * {@link #getNbElements()} is 0.
     *
     * @return element with minimum value
     */
    @Nullable E getMinObject();

    /**
     * Get element with maximum value, or <code>null</code> if
     * {@link #getNbElements()} is 0.
     *
     * @return element with maximum value
     */
    @Nullable E getMaxObject();

    /**
     * Get number of elements analyzed
     *
     * @return number of elements analyzed
     */
    long getNbElements();

    /**
     * Gets the arithmetic mean
     *
     * @return arithmetic mean
     */
    double getMean();

    /**
     * Gets the standard deviation of the values
     *
     * @return the standard deviation of the segment store, will return NaN if
     *         there are less than 3 elements
     */
    double getStdDev();

    /**
     * Get total value
     *
     * @return total value
     */
    double getTotal();

    /**
     * Update the statistics based on a given object
     * <p>
     * This is an online algorithm and must retain a complexity of O(1)
     *
     * @param object
     *            the object used for the update
     */
    void update(E object);

    /**
     * Merge 2 statistics classes for the same object type
     *
     * @param other
     *            The other statistics object
     */
    void merge(IStatistics<E> other);

}
