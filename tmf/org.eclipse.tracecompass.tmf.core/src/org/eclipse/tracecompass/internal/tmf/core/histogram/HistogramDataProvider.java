/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.histogram;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.core.model.AbstractTmfTraceDataProvider;
import org.eclipse.tracecompass.internal.tmf.core.model.TmfXyResponseFactory;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.tmf.core.model.YModel;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfTreeXYDataProvider;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfXyModel;
import org.eclipse.tracecompass.tmf.core.model.xy.IYModel;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.statistics.ITmfStatistics;
import org.eclipse.tracecompass.tmf.core.statistics.TmfStateStatistics.Attributes;
import org.eclipse.tracecompass.tmf.core.statistics.TmfStatisticsEventTypesModule;
import org.eclipse.tracecompass.tmf.core.statistics.TmfStatisticsModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * This data provider will return a XY model (model is wrapped in a response)
 * based on a query filter. The model is used afterwards by any viewer to draw
 * charts. Model returned is for the new Histogram viewer
 *
 * @author Yonni Chen
 * @since 4.0
 */
public class HistogramDataProvider extends AbstractTmfTraceDataProvider implements ITmfTreeXYDataProvider<TmfTreeDataModel> {

    /**
     * Extension point ID.
     */
    public static final String ID = "org.eclipse.tracecompass.internal.tmf.core.histogram.HistogramDataProvider"; //$NON-NLS-1$
    static final String TITLE = Objects.requireNonNull(Messages.HistogramDataProvider_Title);
    private static final AtomicLong TRACE_IDS = new AtomicLong();

    private final TmfStatisticsModule fModule;
    private @Nullable TmfModelResponse<TmfTreeModel<TmfTreeDataModel>> fCached = null;
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

    @Deprecated
    @Override
    public TmfModelResponse<List<TmfTreeDataModel>> fetchTree(TimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        Map<String, Object> parameters = FetchParametersUtils.timeQueryToMap(filter);
        TmfModelResponse<TmfTreeModel<TmfTreeDataModel>> response = fetchTree(parameters, monitor);
        TmfTreeModel<TmfTreeDataModel> model = response.getModel();
        List<TmfTreeDataModel> treeModel = null;
        if (model != null) {
            treeModel = model.getEntries();
        }
        return new TmfModelResponse<>(treeModel, response.getStatus(), response.getStatusMessage());
    }

    @Deprecated
    @Override
    public TmfModelResponse<ITmfXyModel> fetchXY(TimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        Map<String, Object> parameters = FetchParametersUtils.timeQueryToMap(filter);
        return fetchXY(parameters, monitor);
    }

    @Override
    public TmfModelResponse<TmfTreeModel<TmfTreeDataModel>> fetchTree(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        if (fCached != null) {
            return fCached;
        }
        fModule.waitForInitialization();
        Builder<TmfTreeDataModel> builder = ImmutableList.builder();
        builder.add(new TmfTreeDataModel(fTraceId, -1, Collections.singletonList(getTrace().getName())));
        builder.add(new TmfTreeDataModel(fTotalId, fTraceId, Collections.singletonList(Objects.requireNonNull(Messages.HistogramDataProvider_Total))));
        ITmfStateSystem eventsSs = Objects.requireNonNull(fModule.getStateSystem(TmfStatisticsEventTypesModule.ID));
        if (eventsSs.optQuarkAbsolute(Attributes.LOST_EVENTS) != ITmfStateSystem.INVALID_ATTRIBUTE) {
            builder.add(new TmfTreeDataModel(fLostId, fTraceId, Collections.singletonList(Objects.requireNonNull(Messages.HistogramDataProvider_Lost))));
        }
        if (eventsSs.waitUntilBuilt(0)) {
            TmfModelResponse<TmfTreeModel<TmfTreeDataModel>> response = new TmfModelResponse<>(new TmfTreeModel<>(Collections.emptyList(), builder.build()), ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
            fCached = response;
            return response;
        }
        return new TmfModelResponse<>(new TmfTreeModel<>(Collections.emptyList(), builder.build()), ITmfResponse.Status.RUNNING, CommonStatusMessage.RUNNING);
    }

    @Override
    public @NonNull TmfModelResponse<ITmfXyModel> fetchXY(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        fModule.waitForInitialization();
        SelectionTimeQueryFilter filter = FetchParametersUtils.createSelectionTimeQuery(fetchParameters);
        long[] xValues = new long[0];
        if (filter == null) {
            return TmfXyResponseFactory.create(TITLE, xValues, Collections.emptyMap(), true);
        }
        xValues = filter.getTimesRequested();

        Collection<Long> selected = filter.getSelectedItems();
        int n = xValues.length;
        ImmutableMap.Builder<String, IYModel> builder = ImmutableMap.builder();

        final ITmfStatistics stats = Objects.requireNonNull(fModule.getStatistics());
        if (selected.contains(fTotalId)) {
            List<Long> values = stats.histogramQuery(filter.getTimesRequested());

            double[] y = new double[n];
            Arrays.setAll(y, values::get);
            String totalName = getTrace().getName() + '/' + Messages.HistogramDataProvider_Total;
            builder.put(totalName, new YModel(fTotalId, totalName, y));
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
        boolean completed = eventsSs != null ? eventsSs.waitUntilBuilt(0) || eventsSs.getCurrentEndTime() >= filter.getEnd() : false;

        return TmfXyResponseFactory.create(TITLE, xValues, builder.build(), completed);
    }

    private YModel getLostEvents(ITmfStateSystem ss, long[] times) throws StateSystemDisposedException {
        int leEndQuark = ss.optQuarkAbsolute(Attributes.LOST_EVENTS);
        int leCountQuark = ss.optQuarkAbsolute(Attributes.EVENT_TYPES, "Lost event"); //$NON-NLS-1$
        long step = (times[times.length - 1] - times[0]) / times.length;
        long lastTime = times[times.length - 1];
        long firstTime = times[0];
        double[] leY = new double[times.length];

        long t = firstTime;
        if (ss.getStartTime() <= t) {
            List<ITmfStateInterval> sortedEndIntervals = Lists.newArrayList(ss.query2D(Collections.singleton(leEndQuark), firstTime, lastTime));
            sortedEndIntervals.sort(Comparator.comparing(ITmfStateInterval::getStartTime));
            List<ITmfStateInterval> sortedCountIntervals = Lists.newArrayList(ss.query2D(Collections.singleton(leCountQuark), firstTime, lastTime));
            sortedCountIntervals.sort(Comparator.comparing(ITmfStateInterval::getStartTime));

            Iterator<ITmfStateInterval> endTimeIter = sortedEndIntervals.iterator();
            List<LostEventInterval> lostEventIntervals = new ArrayList<>();
            ITmfStateInterval endTimeInterval = endTimeIter.next();
            for (ITmfStateInterval lostCountInterval : sortedCountIntervals) {
                while (!endTimeInterval.intersects(lostCountInterval.getStartTime())) {
                    if (!endTimeIter.hasNext()) {
                        throw new IllegalStateException();
                    }
                    endTimeInterval = endTimeIter.next();
                }
                Object endTime = endTimeInterval.getValue();
                Object lostCount = lostCountInterval.getValue();
                if (endTime instanceof Number && lostCount instanceof Number) {
                    lostEventIntervals.add(new LostEventInterval(endTimeInterval.getStartTime(), ((Number) endTime).longValue(), ((Number) lostCount).longValue()));
                }
            }

            for (int i = 0; i < times.length - 2; i++) {
                int intervalIndex;
                boolean intersect = false;
                for (intervalIndex = 0; intervalIndex < lostEventIntervals.size(); intervalIndex++) {
                    if (lostEventIntervals.get(intervalIndex).intersects(times[i], times[i + 1])) {
                        intersect = true;
                        break;
                    }
                }

                if (intersect) {
                    LostEventInterval lostEventInterval = lostEventIntervals.get(intervalIndex);
                    long prevCount = 0;
                    if (intervalIndex - 1 >= 0) {
                        prevCount = lostEventIntervals.get(intervalIndex - 1).getLostEventCount();
                    }

                    long lostEventCount = lostEventInterval.getLostEventCount();
                    double yValue = step * (double) (lostEventCount - prevCount) / (lostEventInterval.getEndTime() - lostEventInterval.getStartTime());
                    if (yValue > lostEventCount) {
                        yValue = (double) lostEventCount - prevCount;
                    }
                    leY[i] = yValue;
                }
            }
        }
        String lostName = getTrace().getName() + '/' + Messages.HistogramDataProvider_Lost;
        return new YModel(fLostId, lostName, leY);
    }

    private class LostEventInterval {
        private final long fStartTime;
        private final long fEndTime;
        private final long fLostEventCount;

        public LostEventInterval(long startTime, long endTime, long lostEventCount) {
            fStartTime = startTime;
            fEndTime = endTime;
            fLostEventCount = lostEventCount;
        }

        public long getEndTime() {
            return fEndTime;
        }

        public long getStartTime() {
            return fStartTime;
        }

        public long getLostEventCount() {
            return fLostEventCount;
        }

        public boolean intersects(long start, long end) {
            return start <= fEndTime && end >= fStartTime;
        }
    }

    @Override
    public String getId() {
        return ID;
    }
}
