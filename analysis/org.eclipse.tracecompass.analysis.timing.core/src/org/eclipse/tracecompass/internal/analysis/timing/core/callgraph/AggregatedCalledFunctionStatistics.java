/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.timing.core.callgraph;

import org.eclipse.tracecompass.analysis.timing.core.segmentstore.statistics.SegmentStoreStatistics;
import org.eclipse.tracecompass.segmentstore.core.ISegment;

/**
 * Class to calculate statistics for an aggregated function.
 *
 * @author Sonia Farrah
 */
public class AggregatedCalledFunctionStatistics extends SegmentStoreStatistics {

    /**
     * The maximum self time
     */
    private long fMaxSelfTime;
    /**
     * The minimum duration
     */
    private long fMinSelfTime;
    /**
     * The average self time
     */
    private double fSelfTimeAverage;
    /**
     * The variance of the self time
     */
    private double fVariance;
    /**
     * The segment with the longest duration
     */
    private ISegment fMaxSegment;
    /**
     * The segment with the shortest duration
     */
    private ISegment fMinSegment;

    /**
     * Constructor
     *
     * @param duration
     *            The function's duration
     * @param selfTime
     *            The function's self time
     */
    public AggregatedCalledFunctionStatistics(ISegment duration, ISegment selfTime) {
        fMaxSelfTime = selfTime.getLength();
        fMinSelfTime = selfTime.getLength();
        fSelfTimeAverage = selfTime.getLength();
        fVariance = 0.0;
        fMinSegment = duration;
        fMaxSegment = duration;
        update(duration);
    }

    /**
     * Update the statistics, this is used while merging nodes for the
     * aggregation tree.
     *
     * @param statisticsNode
     *            The statistics node to be merged
     * @param duration
     *            The function to be merged duration
     * @param selfTime
     *            The function to be merged self time
     */
    public void update(SegmentStoreStatistics statisticsNode, long duration, long selfTime) {
        merge(statisticsNode);
        if (fMaxSelfTime < selfTime) {
            fMaxSelfTime = selfTime;
        }
        if (fMinSelfTime > selfTime) {
            fMinSelfTime = selfTime;
        }
        double delta = selfTime - fSelfTimeAverage;
        fSelfTimeAverage += delta / getNbSegments();
        fVariance += delta * (selfTime - fSelfTimeAverage);
    }

    /**
     * Get the maximum self time
     *
     * @return The maximum self time
     */
    public long getMaxSelfTime() {
        return fMaxSelfTime;
    }

    /**
     * Get the minimum self time
     *
     * @return The minimum self time
     */
    public long getMinSelfTime() {
        return fMinSelfTime;
    }

    /**
     * Get the average self time
     *
     * @return The average self time
     */
    public double getAverageSelfTime() {
        return fSelfTimeAverage;
    }

    /**
     * Get the standard deviation of the self time
     *
     * @return The standard deviation of the self time
     */
    public double getStdDevSelfTime() {
        long nbSegments = getNbSegments();
        return nbSegments > 2 ? Math.sqrt(fVariance / (nbSegments - 1)) : Double.NaN;
    }

    /**
     * Initialize the maximum and minimum self time
     *
     * @param selfTime
     *            Self time
     */
    public void initializeMaxMinSelfTime(long selfTime) {
        fMaxSelfTime = selfTime;
        fMinSelfTime = selfTime;
        fSelfTimeAverage = selfTime;
    }

    @Override
    public ISegment getMaxSegment() {
        return fMaxSegment;
    }

    @Override
    public ISegment getMinSegment() {
        return fMinSegment;
    }
}
