/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.core.tests.segmentstore.statistics;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.os.linux.core.latency.SystemCall;
import org.eclipse.tracecompass.segmentstore.core.ISegment;

/**
 * This calculates the statistics of a segment store in an offline manner to
 * validate online calculations.
 *
 * @author Matthew Khouzam
 *
 */
public class OfflineStatisticsCalculator {
    private final Collection<@NonNull SystemCall> fSs;

    /**
     * Constructor
     *
     * @param ss
     *            segment store, fully build
     */
    public OfflineStatisticsCalculator(Collection<@NonNull SystemCall> ss) {
        fSs = ss;
    }

    /**
     * Get the max value
     *
     * @return the max value
     */
    public long getMax() {
        long max = Long.MIN_VALUE;
        for (ISegment interval : fSs) {
            max = Math.max(max, interval.getLength());
        }
        return max;
    }

    /**
     * Get the min value
     *
     * @return the min value
     */
    public long getMin() {
        long min = Long.MAX_VALUE;
        for (ISegment interval : fSs) {
            min = Math.min(min, interval.getLength());
        }
        return min;
    }

    /**
     * Get the average value
     *
     * @return the average value
     */
    public double getAvg() {
        double total = 0;
        for (ISegment interval : fSs) {
            total += (double) interval.getLength() / (double) fSs.size();
        }
        return total;
    }

    /**
     * Get the standard deviation for a window.
     *
     * @return the standard deviation
     */
    public double getStdDev() {
        if (fSs.size() < 3) {
            return Double.NaN;
        }
        double mean = getAvg();

        double totalVariance = 0;
        for (ISegment interval : fSs) {
            double result = interval.getLength() - mean;
            totalVariance += result * result / (fSs.size() - 1);
        }
        return Math.sqrt(totalVariance);
    }
}
