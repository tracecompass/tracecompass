/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.analysis.timing.core.segmentstore.statistics;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.collect.ImmutableList;

/**
 * Abstract analysis to build statistics data for a segment store
 *
 * @author Jean-Christian Kouame
 */
public abstract class AbstractSegmentStatisticsAnalysis extends TmfAbstractAnalysisModule {

    private @Nullable IAnalysisModule fSegmentStoreProviderModule;

    private @Nullable SegmentStoreStatistics fTotalStats;

    private @Nullable Map<String, SegmentStoreStatistics> fPerSegmentTypeStats;

    @Override
    protected Iterable<IAnalysisModule> getDependentAnalyses() {
        ITmfTrace trace = getTrace();
        if (trace != null) {
            ISegmentStoreProvider provider = getSegmentProviderAnalysis(trace);
            if (provider instanceof IAnalysisModule) {
                fSegmentStoreProviderModule = (IAnalysisModule) provider;
                return ImmutableList.of((IAnalysisModule) provider);
            }
        }
        return super.getDependentAnalyses();
    }

    @Override
    protected boolean executeAnalysis(IProgressMonitor monitor) throws TmfAnalysisException {
        IAnalysisModule segmentStoreProviderModule = fSegmentStoreProviderModule;
        ITmfTrace trace = getTrace();
        if (!(segmentStoreProviderModule instanceof ISegmentStoreProvider) || (trace == null)) {
            return false;
        }
        segmentStoreProviderModule.waitForCompletion();

        ISegmentStore<ISegment> segStore = ((ISegmentStoreProvider) segmentStoreProviderModule).getSegmentStore();

        if (segStore != null) {

            boolean result = calculateTotalManual(segStore, monitor);

            if (!result) {
                return false;
            }

            result = calculateTotalPerType(segStore, monitor);
            if (!result) {
                return false;
            }
        }
        return true;
    }

    private boolean calculateTotalManual(ISegmentStore<ISegment> store, IProgressMonitor monitor) {
        SegmentStoreStatistics total = new SegmentStoreStatistics();
        Iterator<ISegment> iter = store.iterator();
        while (iter.hasNext()) {
            if (monitor.isCanceled()) {
                return false;
            }
            ISegment segment = iter.next();
            total.update(checkNotNull(segment));
        }
        fTotalStats = total;
        return true;
    }

    private boolean calculateTotalPerType(ISegmentStore<ISegment> store, IProgressMonitor monitor) {
        Map<String, SegmentStoreStatistics> perSegmentTypeStats = new HashMap<>();

        Iterator<ISegment> iter = store.iterator();
        while (iter.hasNext()) {
            if (monitor.isCanceled()) {
                return false;
            }
            ISegment segment = iter.next();
            String segmentType = getSegmentType(segment);
            if (segmentType != null) {
                SegmentStoreStatistics values = perSegmentTypeStats.get(segmentType);
                if (values == null) {
                    values = new SegmentStoreStatistics();
                }
                values.update(segment);
                perSegmentTypeStats.put(segmentType, values);
            }
        }
        fPerSegmentTypeStats = perSegmentTypeStats;
        return true;
    }

    /**
     * Get the type of a segment. Statistics per type will use this type as a
     * key
     *
     * @param segment
     *            the segment for which to get the type
     * @return The type of the segment
     */
    protected abstract @Nullable String getSegmentType(ISegment segment);

    /**
     * Find the segment store provider used for this analysis
     *
     * @param trace
     *            The active trace
     *
     * @return The segment store provider
     */
    protected abstract @Nullable ISegmentStoreProvider getSegmentProviderAnalysis(ITmfTrace trace);

    @Override
    protected void canceling() {
    }

    /**
     * The total statistics
     *
     * @return the total statistics
     */
    public @Nullable SegmentStoreStatistics getTotalStats() {
        return fTotalStats;
    }

    /**
     * The per syscall statistics
     *
     * @return the per syscall statistics
     */
    public @Nullable Map<String, SegmentStoreStatistics> getPerSegmentTypeStats() {
        return fPerSegmentTypeStats;
    }

}
