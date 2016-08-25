/*******************************************************************************
 * Copyright (c) 2015, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.analysis.timing.core.segmentstore.statistics;

import org.eclipse.tracecompass.segmentstore.core.BasicSegment;
import org.eclipse.tracecompass.segmentstore.core.ISegment;

/**
 * Class to calculate simple segment store statistics (min, max, average)
 *
 * @author Bernd Hufmann
 */
public class SegmentStoreStatistics {
    private ISegment fMin;
    private ISegment fMax;
    private long fNbSegments;
    private double fAverage;
    /**
     * reminder, this is the variance * nb elem, as per the online algorithm
     */
    private double fVariance;
    private double fTotal;

    /**
     * Constructor
     */
    public SegmentStoreStatistics() {
        fMin = new BasicSegment(0, Long.MAX_VALUE);
        fMax = new BasicSegment(Long.MIN_VALUE, 0);
        fNbSegments = 0;
        fAverage = 0.0;
        fVariance = 0.0;
        fTotal = 0.0;
    }

    /**
     * Get minimum value
     *
     * @return minimum value
     */
    public long getMin() {
        return fMin.getLength();
    }

    /**
     * Get maximum value
     *
     * @return maximum value
     */
    public long getMax() {
        return fMax.getLength();
    }

    /**
     * Get segment with minimum length
     *
     * @return segment with minimum length
     */
    public ISegment getMinSegment() {
        return fMin;
    }

    /**
     * Get segment with maximum length
     *
     * @return segment with maximum length
     */
    public ISegment getMaxSegment() {
        return fMax;
    }

    /**
     * Get number of segments analyzed
     *
     * @return number of segments analyzed
     */
    public long getNbSegments() {
        return fNbSegments;
    }

    /**
     * Gets the arithmetic average
     *
     * @return arithmetic average
     */
    public double getAverage() {
        return fAverage;
    }

    /**
     * Gets the standard deviation of the segments, uses the online algorithm
     * shown here <a href=
     * "https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Online_algorithm">
     * Wikipedia article of dec 3 2015 </a>
     *
     * @return the standard deviation of the segment store, will return NaN if
     *         there are less than 3 elements
     */
    public double getStdDev() {
        return fNbSegments > 2 ? Math.sqrt(fVariance / (fNbSegments - 1)) : Double.NaN;
    }

    /**
     * Get total value
     *
     * @return total value
     * @since 1.1
     */
    public double getTotal() {
        return fTotal;
    }

    /**
     * Update the statistics based on a given segment
     * <p>
     * This is an online algorithm and must retain a complexity of O(1)
     *
     * @param segment
     *            the segment used for the update
     */
    public void update(ISegment segment) {
        long value = segment.getLength();
        /*
         * Min and max are trivial, as well as number of segments
         */
        long min = fMin.getLength();
        long max = fMax.getLength();
        fMin = min <= value ? fMin : segment;
        fMax = max >= value ? fMax : segment;

        fNbSegments++;
        /*
         * The running mean is not trivial, see proof in javadoc.
         */
        double delta = value - fAverage;
        fAverage += delta / fNbSegments;
        fVariance += delta * (value - fAverage);
        fTotal += value;
    }

    /**
     * Merge two statistics sets. If the pools are large, there may be a slight
     * approximation error (empirically, the error is at most 0.001 but usually
     * around 1e-5 for the standard deviation as this uses pooled variance.
     *
     * @param other
     *            The other segment store statistics
     * @since 1.1
     */
    public void merge(SegmentStoreStatistics other) {
        if (other.fNbSegments == 0) {
            return;
        } else if (fNbSegments == 0) {
            copy(other);
        } else if (other.fNbSegments == 1) {
            update(other.fMax);
        } else if (fNbSegments == 1) {
            SegmentStoreStatistics copyOther = new SegmentStoreStatistics();
            copyOther.copy(other);
            copyOther.update(fMax);
            copy(copyOther);
        } else {
            internalMerge(other);
        }
    }

    private void internalMerge(SegmentStoreStatistics other) {
        /*
         * Min and max are trivial, as well as number of segments
         */
        long min = fMin.getLength();
        long max = fMax.getLength();
        fMin = min <= other.getMin() ? fMin : other.getMinSegment();
        fMax = max >= other.getMax() ? fMax : other.getMaxSegment();

        long oldNbSeg = fNbSegments;
        double oldAverage = fAverage;
        long otherSegments = other.getNbSegments();
        double otherAverage = other.getAverage();
        fNbSegments += otherSegments;
        fTotal += other.getTotal();

        /*
         * Average is a weighted average
         */
        fAverage = ((oldNbSeg * oldAverage) + (otherAverage * otherSegments)) / fNbSegments;

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
        double avgtSq = fAverage * fAverage;
        /*
         * This is a tricky part, bear in mind that the set is not continuous but discrete,
         * Therefore, we have for n elements, n-1 intervals between them.
         * Ergo, n-1 intervals are used for divisions and multiplications.
         */
        double variance1 = fVariance / (oldNbSeg - 1);
        double variance2 = other.fVariance / (otherSegments - 1);
        fVariance = ((variance1 + avg1Sq - avgtSq) * (oldNbSeg - 1) + (variance2 + avg2sq - avgtSq) * (otherSegments - 1));
    }

    private void copy(SegmentStoreStatistics copyOther) {
        fAverage = copyOther.fAverage;
        fMax = copyOther.fMax;
        fMin = copyOther.fMin;
        fNbSegments = copyOther.fNbSegments;
        fTotal = copyOther.fTotal;
        fVariance = copyOther.fVariance;
    }
}
