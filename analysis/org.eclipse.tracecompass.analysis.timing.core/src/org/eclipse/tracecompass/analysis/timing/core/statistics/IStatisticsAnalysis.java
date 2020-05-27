/*******************************************************************************
 * Copyright (c) 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.analysis.timing.core.statistics;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Statistics data provider. Gives statistics that are globally accumulated as
 * well as per-type.
 *
 * @param <E>
 *            The element to evaluate
 *
 * @author Matthew Khouzam
 * @since 5.2
 */
public interface IStatisticsAnalysis<E> {

    /**
     * Get the total statistics for a specific range. If the range start is
     * TmfTimeRange.ETERNITY.getStartTime().toNanos() and the range end is
     * TmfTimeRange.ETERNITY.getEndTime().toNanos(), it will return the
     * statistics for the whole trace.
     *
     * @param start
     *            The start time of the range
     * @param end
     *            The end time of the range
     * @param monitor
     *            The progress monitor
     * @return The total statistics, or <code>null</code> if data source is
     *         invalid or if the request is canceled
     */
    @Nullable
    IStatistics<@NonNull E> getStatsForRange(long start, long end, IProgressMonitor monitor);

    /**
     * Get the per type statistics for a specific range. If the range start is
     * TmfTimeRange.ETERNITY.getStartTime().toNanos() and the range end is
     * TmfTimeRange.ETERNITY.getEndTime().toNanos(), it will return the
     * statistics for the whole trace.
     *
     * @param start
     *            The start time of the range
     * @param end
     *            The end time of the range
     * @param monitor
     *            The progress monitor
     * @return The per type statistics, or <code>null</code> if data source is
     *         invalid or if the request is canceled
     */
    Map<@NonNull String, IStatistics<@NonNull E>> getStatsPerTypeForRange(long start, long end, IProgressMonitor monitor);

    /**
     * Get the statistics for the time range, all categories aggregated
     *
     * @return The complete statistics
     */
    @Nullable
    IStatistics<@NonNull E> getStatsTotal();

    /**
     * Get the statistics for each category in the data source
     *
     * @return the map of statistics per type
     */
    Map<String, IStatistics<@NonNull E>> getStatsPerType();

}