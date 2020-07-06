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

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.analysis.timing.core.segmentstore.statistics.NumberComparator;

/**
 * Class that calculates statistics on a certain type of object. If the object
 * is not a {@link Number}, a mapper function should be passed in the constructor
 * to retrieve the numerical value to make statistics on from an object.
 *
 * @author Bernd Hufmann
 * @author Geneviève Bastien
 *
 * @param <E>
 *            The type of object to calculate statistics on
 * @since 1.3
 */
public class Statistics<@NonNull E> implements IStatistics<E> {

    private static final NumberComparator COMPARATOR = new NumberComparator();

    private final Function<E, @Nullable ? extends Number> fMapper;

    private @Nullable E fMin = null;
    private @Nullable E fMax = null;
    private Number fMinNum = Long.MAX_VALUE;
    private Number fMaxNum = Long.MIN_VALUE;
    private long fNbElements;
    private double fMean;

    /**
     * reminder, this is the variance * nb elem, as per the online algorithm
     */
    private double fVariance;
    private double fTotal;

    /**
     * Constructor
     */
    public Statistics() {
        this(e -> {
            if (!(e instanceof Long)) {
                throw new IllegalStateException("The object " + e + " is not a number"); //$NON-NLS-1$//$NON-NLS-2$
            }
            return (Long) e;
        });
    }

    /**
     * Constructor
     *
     * @param mapper
     *            A mapper function that takes an object to computes statistics
     *            for and returns the value to use for the statistics
     */
    public Statistics(Function<E, @Nullable ? extends @Nullable Number> mapper) {
        fNbElements = 0;
        fMean = 0.0;
        fVariance = 0.0;
        fTotal = 0.0;
        fMapper = mapper;
    }

    @Override
    public long getMin() {
        return fMinNum.longValue();
    }

    @Override
    public long getMax() {
        return fMaxNum.longValue();
    }

    @Override
    public @Nullable E getMinObject() {
        return fMin;
    }

    @Override
    public @Nullable E getMaxObject() {
        return fMax;
    }

    @Override
    public long getNbElements() {
        return fNbElements;
    }

    @Override
    public double getMean() {
        return fMean;
    }

    /**
     * Gets the standard deviation of the elements. It uses the online algorithm
     * shown here <a href=
     * "https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Online_algorithm">
     * Wikipedia article of dec 3 2015 </a>
     *
     * @return the standard deviation of the elements, will return NaN if there
     *         are less than 3 elements
     */
    @Override
    public double getStdDev() {
        return fNbElements > 2 ? Math.sqrt(fVariance / (fNbElements - 1)) : Double.NaN;
    }

    @Override
    public double getTotal() {
        return fTotal;
    }

    @Override
    public void update(E object) {
        Number number = fMapper.apply(object);
        if (number == null) {
            // TODO add null category?
            return;
        }
        double doubleValue = number.doubleValue();
        updateMin(object, number);
        updateMax(object, number);
        fNbElements++;
        /*
         * The running mean is not trivial, see proof in javadoc.
         *
         * TODO: Check if saturated math would be required here
         */
        double delta = doubleValue - fMean;
        fMean += delta / fNbElements;
        fVariance += delta * (doubleValue - fMean);
        fTotal += doubleValue;
    }

    private void updateMax(@Nullable E object, Number number) {
        if (object == null) {
            return;
        }
        @Nullable E max = fMax;
        if (max == null || COMPARATOR.compare(number, fMaxNum) > 0) {
            fMax = object;
            fMaxNum = number;
        }
    }

    private void updateMin(@Nullable E object, Number number) {
        if (object == null) {
            return;
        }
        @Nullable E min = fMin;
        if (min == null || COMPARATOR.compare(number, fMinNum) < 0) {
            fMin = object;
            fMinNum = number;
        }
    }

    @Override
    public void merge(IStatistics<E> o) {
        if (!(o instanceof Statistics)) {
            throw new IllegalArgumentException("Can only merge statistics of the same class"); //$NON-NLS-1$
        }
        Statistics<E> other = (Statistics<E>) o;
        if (other.fNbElements == 0) {
            return;
        } else if (fNbElements == 0) {
            copy(other);
        } else if (other.fNbElements == 1) {
            update(NonNullUtils.checkNotNull(other.getMaxObject()));
        } else if (fNbElements == 1) {
            Statistics<E> copyOther = new Statistics<>(fMapper);
            copyOther.copy(other);
            copyOther.update(NonNullUtils.checkNotNull(getMaxObject()));
            copy(copyOther);
        } else {
            internalMerge(other);
        }
    }

    private void internalMerge(Statistics<E> other) {
        /*
         * TODO: Check if saturated math would be required in this method
         *
         * Min and max are trivial, as well as number of segments
         */
        updateMin(other.getMinObject(), other.fMinNum);
        updateMax(other.getMaxObject(), other.fMaxNum);

        long oldNbSeg = fNbElements;
        double oldAverage = fMean;
        long otherSegments = other.getNbElements();
        double otherAverage = other.getMean();
        fNbElements += otherSegments;
        fTotal += other.getTotal();

        /*
         * Average is a weighted average
         */
        fMean = ((oldNbSeg * oldAverage) + (otherAverage * otherSegments)) / fNbElements;

        /*
         * This one is a bit tricky.
         *
         * The variance is the sum of the deltas from a mean squared.
         *
         * So if we add the old mean squared back to to variance and remove the
         * new mean, the standard deviation can be easily calculated.
         */
        double avg1Sq = oldAverage * oldAverage;
        double avg2sq = otherAverage * otherAverage;
        double avgtSq = fMean * fMean;
        /*
         * This is a tricky part, bear in mind that the set is not continuous
         * but discrete, Therefore, we have for n elements, n-1 intervals
         * between them. Ergo, n-1 intervals are used for divisions and
         * multiplications.
         */
        double variance1 = fVariance / (oldNbSeg - 1);
        double variance2 = other.fVariance / (otherSegments - 1);
        fVariance = ((variance1 + avg1Sq - avgtSq) * (oldNbSeg - 1) + (variance2 + avg2sq - avgtSq) * (otherSegments - 1));
    }

    private void copy(Statistics<E> copyOther) {
        fMean = copyOther.fMean;
        fMax = copyOther.fMax;
        fMin = copyOther.fMin;
        fMinNum = copyOther.fMinNum;
        fMaxNum = copyOther.fMaxNum;
        fNbElements = copyOther.fNbElements;
        fTotal = copyOther.fTotal;
        fVariance = copyOther.fVariance;
    }

    @Override
    public String toString() {
        return this.getClass() + ": Avg: " + getMean() + " on " + getNbElements() + " elements"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
