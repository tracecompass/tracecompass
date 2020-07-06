/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.core.tests.statistics;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.timing.core.statistics.IStatistics;
import org.eclipse.tracecompass.common.core.NonNullUtils;

/**
 * This calculates the statistics of a segment store in an offline manner to
 * validate online calculations.
 *
 * @author Matthew Khouzam
 * @param <E>
 *            The type of object to calculate statistics on
 */
public class OfflineStatisticsCalculator<@NonNull E> implements IStatistics<E> {

    private final Collection<E> fElements;
    private final @NonNull Function<E, @NonNull Long> fMapper;

    /**
     * Constructor
     *
     * @param elements
     *            collection of elements
     * @param mapper
     *            A mapper function that takes an object to computes statistics
     *            for and returns the value to use for the statistics
     */
    public OfflineStatisticsCalculator(Collection<E> elements, @NonNull Function<E, @NonNull Long> mapper) {
        fElements = elements;
        fMapper = mapper;
    }

    /**
     * Get the max value
     *
     * @return the max value
     */
    @Override
    public long getMax() {
        long max = Long.MIN_VALUE;
        for (E element : fElements) {
            max = Math.max(max, fMapper.apply(element));
        }
        return max;
    }

    /**
     * Get the min value
     *
     * @return the min value
     */
    @Override
    public long getMin() {
        long min = Long.MAX_VALUE;
        for (E element : fElements) {
            min = Math.min(min, fMapper.apply(element));
        }
        return min;
    }

    @Override
    public @NonNull E getMinObject() {
        @Nullable E obj = null;
        for (E element : fElements) {
            if (obj == null) {
                obj = element;
                continue;
            }
            Long value = fMapper.apply(element);
            if (value < fMapper.apply(obj)) {
                obj = element;
            }
        }
        if (obj == null) {
            throw new NoSuchElementException("There are no elements in the collection");
        }
        return obj;
    }

    @Override
    public @NonNull E getMaxObject() {
        @Nullable E obj = null;
        for (E element : fElements) {
            if (obj == null) {
                obj = element;
                continue;
            }
            Long value = fMapper.apply(element);
            if (value > fMapper.apply(obj)) {
                obj = element;
            }
        }
        if (obj == null) {
            throw new NoSuchElementException("There are no elements in the collection");
        }
        return obj;
    }

    /**
     * Get the average value
     *
     * @return the average value
     */
    @Override
    public double getMean() {
        double total = 0;
        for (E element : fElements) {
            total += (double) NonNullUtils.checkNotNull(fMapper.apply(element)) / (double) fElements.size();
        }
        return total;
    }

    /**
     * Get the standard deviation.
     *
     * @return the standard deviation
     */
    @Override
    public double getStdDev() {
        if (fElements.size() < 3) {
            return Double.NaN;
        }
        double mean = getMean();

        double totalVariance = 0;
        for (E element : fElements) {
            double result = NonNullUtils.checkNotNull(fMapper.apply(element)) - mean;
            totalVariance += result * result / (fElements.size() - 1);
        }
        return Math.sqrt(totalVariance);
    }

    /**
     * Get the total
     *
     * @return the total
     */
    @Override
    public double getTotal() {
        double total = 0.0;
        for (E element : fElements) {
            total += fMapper.apply(element);
        }
        return total;
    }

    @Override
    public long getNbElements() {
        return fElements.size();
    }

    @Override
    public void update(@NonNull E object) {
        throw new UnsupportedOperationException("Offline statistics should not update"); //$NON-NLS-1$
    }

    @Override
    public void merge(@NonNull IStatistics<@NonNull E> other) {
        throw new UnsupportedOperationException("Offline statistics should not merge"); //$NON-NLS-1$
    }

}
