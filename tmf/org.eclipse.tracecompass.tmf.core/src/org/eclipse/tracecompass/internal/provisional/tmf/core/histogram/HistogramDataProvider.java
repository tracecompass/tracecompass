/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.histogram;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.AbstractTmfTraceDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.TmfXyResponseFactory;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.TmfTreeDataModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfTreeXYDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfXyModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.IYModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.internal.tmf.core.model.YModel;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.statistics.ITmfStatistics;
import org.eclipse.tracecompass.tmf.core.statistics.TmfStateStatistics.Attributes;
import org.eclipse.tracecompass.tmf.core.statistics.TmfStatisticsEventTypesModule;
import org.eclipse.tracecompass.tmf.core.statistics.TmfStatisticsModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;

/**
 * This data provider will return a XY model (model is wrapped in a response)
 * based on a query filter. The model is used afterwards by any viewer to draw
 * charts. Model returned is for the new Histogram viewer
 *
 * @author Yonni Chen
 */
public class HistogramDataProvider extends AbstractTmfTraceDataProvider implements ITmfTreeXYDataProvider<TmfTreeDataModel> {

    /**
     * Extension point ID.
     */
    public static final String ID = "org.eclipse.tracecompass.internal.provisional.tmf.core.histogram.HistogramDataProvider"; //$NON-NLS-1$
    static final String TITLE = Objects.requireNonNull(Messages.HistogramDataProvider_Title);
    private static final AtomicLong TRACE_IDS = new AtomicLong();

    private final TmfStatisticsModule fModule;
    private @Nullable TmfModelResponse<List<TmfTreeDataModel>> fCached = null;
    private final long fTraceId = TRACE_IDS.getAndIncrement();
    private final long fTotalId = TRACE_IDS.getAndIncrement();
    private final long fLostId = TRACE_IDS.getAndIncrement();

    /**
     * Constructor
     *
     * @param trace
     *            A trace on which we are interested to fetch a model
     * @param module
     *            the {@link TmfStatisticsModule} for the trace.
     */
    public HistogramDataProvider(ITmfTrace trace, TmfStatisticsModule module) {
        super(trace);
        fModule = module;
    }

    @Override
    public TmfModelResponse<List<TmfTreeDataModel>> fetchTree(TimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        if (fCached != null) {
            return fCached;
        }
        fModule.waitForInitialization();
        Builder<TmfTreeDataModel> builder = ImmutableList.builder();
        builder.add(new TmfTreeDataModel(fTraceId, -1, getTrace().getName()));
        builder.add(new TmfTreeDataModel(fTotalId, fTraceId, Objects.requireNonNull(Messages.HistogramDataProvider_Total)));
        ITmfStateSystem eventsSs = Objects.requireNonNull(fModule.getStateSystem(TmfStatisticsEventTypesModule.ID));
        if (eventsSs.optQuarkAbsolute(Attributes.LOST_EVENTS) != ITmfStateSystem.INVALID_ATTRIBUTE) {
            builder.add(new TmfTreeDataModel(fLostId, fTraceId, Objects.requireNonNull(Messages.HistogramDataProvider_Lost)));
        }
        if (eventsSs.waitUntilBuilt(0)) {
            TmfModelResponse<List<TmfTreeDataModel>> response = new TmfModelResponse<>(builder.build(), ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
            fCached = response;
            return response;
        }
        return new TmfModelResponse<>(builder.build(), ITmfResponse.Status.RUNNING, CommonStatusMessage.RUNNING);
    }

    @Override
    public @NonNull TmfModelResponse<ITmfXyModel> fetchXY(@NonNull TimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        long[] xValues = filter.getTimesRequested();

        if (!(filter instanceof SelectionTimeQueryFilter)) {
            return TmfXyResponseFactory.create(TITLE, xValues, Collections.emptyMap(), true);
        }
        Collection<Long> selected = ((SelectionTimeQueryFilter) filter).getSelectedItems();
        int n = xValues.length;
        ImmutableMap.Builder<String, IYModel> builder = ImmutableMap.builder();

        final ITmfStatistics stats = Objects.requireNonNull(fModule.getStatistics());
        if (selected.contains(fTotalId)) {
            List<Long> values = stats.histogramQuery(filter.getStart(), filter.getEnd(), n);

            double[] y = new double[n];
            Arrays.setAll(y, values::get);
            String totalName = getTrace().getName() + '/' + Messages.HistogramDataProvider_Total;
            builder.put(totalName, new YModel(totalName, y));
        }

        ITmfStateSystem eventsSs = fModule.getStateSystem(TmfStatisticsEventTypesModule.ID);
        if (selected.contains(fLostId) && eventsSs != null) {
            try {
                YModel series = getLostEvents(eventsSs, xValues);
                builder.put(series.getName(), series);
            } catch (StateSystemDisposedException e) {
                return TmfXyResponseFactory.createFailedResponse(CommonStatusMessage.STATE_SYSTEM_FAILED);
            }
        }

        return TmfXyResponseFactory.create(TITLE, xValues, builder.build(), true);
    }

    private YModel getLostEvents(ITmfStateSystem ss, long[] times) throws StateSystemDisposedException {
        int leEndQuark = ss.optQuarkAbsolute(Attributes.LOST_EVENTS);
        int leCountQuark = ss.optQuarkAbsolute(Attributes.EVENT_TYPES, "Lost event"); //$NON-NLS-1$
        long step = (times[times.length - 1] - times[0]) / times.length;
        double[] leY = new double[times.length];
        ITmfStateInterval lePrevValueInterval = null;
        ITmfStateInterval leEndInterval = null;
        ITmfStateInterval leCountInterval = null;
        long prevValue = 0l;
        /*
         * FIXME : in the worst case scenario, filling in one value for the lost events
         * requires querying THREE intervals. Fixing this would require modifying the
         * state system for the desired value to be directly accessible.
         */
        for (int i = 0; i < times.length; i++) {
            long t = times[i];
            if (t > ss.getCurrentEndTime()) {
                break;
            } else if (ss.getStartTime() <= t) {
                // try to reuse the end and count intervals.
                if (leEndInterval == null || !leEndInterval.intersects(t)) {
                    leEndInterval = ss.querySingleState(t, leEndQuark);
                }

                if (leCountInterval == null || !leCountInterval.intersects(t)) {
                    leCountInterval = ss.querySingleState(t, leCountQuark);
                }
                Object endValue = leEndInterval.getValue();
                Object countValue = leCountInterval.getValue();

                long prevValueTime = leEndInterval.getStartTime() - 1;
                if (prevValueTime >= ss.getStartTime()) {
                    // try to reuse the previous count intervals.
                    if (lePrevValueInterval == null || lePrevValueInterval.getEndTime() != prevValueTime) {
                        lePrevValueInterval = ss.querySingleState(prevValueTime, leCountQuark);
                        Object prevValueObject = lePrevValueInterval.getValue();
                        if (prevValueObject instanceof Number) {
                            prevValue = ((Number) prevValueObject).longValue();
                        }
                    }
                } else {
                    prevValue = 0;
                }

                if (endValue instanceof Number && countValue instanceof Number) {
                    long end = ((Number) endValue).longValue();
                    double count = ((Number) countValue).doubleValue();
                    if (end >= t) {
                        leY[i] = step * (count - prevValue) / (end - leEndInterval.getStartTime());
                    }
                }
            }
        }
        String lostName = getTrace().getName() + '/' + Messages.HistogramDataProvider_Lost;
        return new YModel(lostName, leY);
    }

    @Override
    public String getId() {
        return ID;
    }
}
