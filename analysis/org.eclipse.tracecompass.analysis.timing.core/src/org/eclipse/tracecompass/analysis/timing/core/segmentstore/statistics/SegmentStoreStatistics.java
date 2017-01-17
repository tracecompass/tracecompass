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

import org.eclipse.tracecompass.analysis.timing.core.statistics.IStatistics;
import org.eclipse.tracecompass.analysis.timing.core.statistics.Statistics;
import org.eclipse.tracecompass.segmentstore.core.BasicSegment;
import org.eclipse.tracecompass.segmentstore.core.ISegment;

/**
 * Class to calculate simple segment store statistics (min, max, average)
 *
 * @author Bernd Hufmann
 * @deprecated Use {@link IStatistics} instead
 */
@Deprecated
public class SegmentStoreStatistics {

    private static final ISegment MIN_SEGMENT = new BasicSegment(0, Long.MAX_VALUE);
    private static final ISegment MAX_SEGMENT = new BasicSegment(Long.MIN_VALUE, 0);

    private final IStatistics<ISegment> fStatistics;

    /**
     * Constructor
     */
    public SegmentStoreStatistics() {
        fStatistics = new Statistics<>(s -> s.getLength());
    }

    /**
     * Constructor
     *
     * @param stats The statistics object
     * @since 1.3
     */
    public SegmentStoreStatistics(IStatistics<ISegment> stats) {
        fStatistics = stats;
    }

    /**
     * Get minimum value
     *
     * @return minimum value
     * @since 1.2
     */
    public long getMin() {
        return fStatistics.getMin();
    }

    /**
     * Get maximum value
     *
     * @return maximum value
     */
    public long getMax() {
        return fStatistics.getMax();
    }

    /**
     * Get segment with minimum length
     *
     * @return segment with minimum length
     */
    public ISegment getMinSegment() {
        ISegment minObject = fStatistics.getMinObject();
        return minObject == null ? MIN_SEGMENT : minObject;
    }

    /**
     * Get segment with maximum length
     *
     * @return segment with maximum length
     */
    public ISegment getMaxSegment() {
        ISegment maxObject = fStatistics.getMaxObject();
        return maxObject == null ? MAX_SEGMENT : maxObject;
    }

    /**
     * Get number of segments analyzed
     *
     * @return number of segments analyzed
     */
    public long getNbSegments() {
        return fStatistics.getNbElements();
    }

    /**
     * Gets the arithmetic average
     *
     * @return arithmetic average
     */
    public double getAverage() {
        return fStatistics.getMean();
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
        return fStatistics.getStdDev();
    }

    /**
     * Get total value
     *
     * @return total value
     * @since 1.1
     */
    public double getTotal() {
        return fStatistics.getTotal();
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
        fStatistics.update(segment);
    }

    /**
     * Merge two statistics sets. If the pools are large, there may be a slight
     * approximation error (empirically, the error is at most 0.001 but usually
     * around 1e-5 for the standard deviation as this uses pooled variance.
     *
     * @param other
     *            The other segment store statistics
     * @since 1.2
     */
    public void merge(SegmentStoreStatistics other) {
        fStatistics.merge(other.getStatObject());
    }

    private IStatistics<ISegment> getStatObject() {
        return fStatistics;
    }

}
