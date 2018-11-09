/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.analysis.timing.core.segmentstore;

import java.util.Collections;
import java.util.List;

import org.eclipse.tracecompass.analysis.timing.core.statistics.IStatistics;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeDataModel;

/**
 * A {@link TmfTreeDataModel} extended with the getters from {@link IStatistics}
 *
 * @author Loic Prieur-Drevon
 * @since 4.0
 */
public class SegmentStoreStatisticsModel extends TmfTreeDataModel {

    private final long fMin;
    private final long fMax;
    private final long fNbElements;
    private final double fMean;
    private final double fStdDev;
    private final double fTotal;
    private final long fMinStart;
    private final long fMinEnd;
    private final long fMaxStart;
    private final long fMaxEnd;

    /**
     * Constructor
     *
     * @param id
     *            The id of the model
     * @param parentId
     *            The parent id of this model. If it has none, give <code>-1</code>.
     * @param name
     *            The name of this model
     * @param statistics
     *            the {@link IStatistics} who's values will be copied into this
     *            model.
     */
    public SegmentStoreStatisticsModel(long id, long parentId, String name, IStatistics<ISegment> statistics) {
        this(id, parentId, Collections.singletonList(name), statistics);
    }

    /**
     * Constructor
     *
     * @param id
     *            The id of the model
     * @param parentId
     *            The parent id of this model. If it has none, give <code>-1</code>.
     * @param labels
     *            The labels of this model
     * @param statistics
     *            the {@link IStatistics} who's values will be copied into this
     *            model.
     * @since 4.2
     */
    public SegmentStoreStatisticsModel(long id, long parentId, List<String> labels, IStatistics<ISegment> statistics) {
        super(id, parentId, labels);
        fMin = statistics.getMin();
        fMax = statistics.getMax();
        fNbElements = statistics.getNbElements();
        fMean = statistics.getMean();
        fStdDev = statistics.getStdDev();
        fTotal = statistics.getTotal();

        ISegment max = statistics.getMaxObject();
        if (max != null) {
            fMaxStart = max.getStart();
            fMaxEnd = max.getEnd();
        } else {
            fMaxStart = 0;
            fMaxEnd = 0;
        }

        ISegment min = statistics.getMinObject();
        if (min != null) {
            fMinStart = min.getStart();
            fMinEnd = min.getEnd();
        } else {
            fMinStart = 0;
            fMinEnd = 0;
        }
    }

    /**
     * Get the minimum value from the statistics
     *
     * @return mininum value.
     */
    public long getMin() {
        return fMin;
    }

    /**
     * Get the maximum value from the statistics
     *
     * @return maximum value.
     */
    public long getMax() {
        return fMax;
    }

    /**
     * Get the number of elements in the statistics
     *
     * @return the number of elements.
     */
    public long getNbElements() {
        return fNbElements;
    }

    /**
     * Get the mean value from the statistics
     *
     * @return mean value.
     */
    public double getMean() {
        return fMean;
    }

    /**
     * Get the standard deviation from the statistics
     *
     * @return standard deviation.
     */
    public double getStdDev() {
        return fStdDev;
    }

    /**
     * Get the total value from the statistics
     *
     * @return total value.
     */
    public double getTotal() {
        return fTotal;
    }

    /**
     * Get the minimum's start time from the statistics, if there was one, else
     * {@code 0}
     *
     * @return minimum's start time.
     */
    public long getMinStart() {
        return fMinStart;
    }

    /**
     * Get the minimum's end time from the statistics, if there was one, else
     * {@code 0}
     *
     * @return minimum's end time.
     */
    public long getMinEnd() {
        return fMinEnd;
    }

    /**
     * Get the maximum's start time from the statistics, if there was one, else
     * {@code 0}
     *
     * @return maximum's start time.
     */
    public long getMaxStart() {
        return fMaxStart;
    }

    /**
     * Get the maximum's end time from the statistics, if there was one, else
     * {@code 0}
     *
     * @return maximum's end time.
     */
    public long getMaxEnd() {
        return fMaxEnd;
    }

}
