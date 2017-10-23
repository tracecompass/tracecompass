/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.examples.histogram;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.AbstractTmfTraceDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.TmfCommonXAxisResponseFactory;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.TmfTreeDataModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfCommonXAxisModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfTreeXYDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.IYModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.internal.tmf.core.model.YModel;
import org.eclipse.tracecompass.tmf.core.statistics.ITmfStatistics;
import org.eclipse.tracecompass.tmf.core.statistics.TmfStatisticsModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.collect.ImmutableMap;

/**
 * This data provider will return a XY model (model is wrapped in a response)
 * based on a query filter. The model is used afterwards by any viewer to draw
 * charts. Model returned is for the new Histogram viewer
 *
 * @author Yonni Chen
 */
@SuppressWarnings("restriction")
public class HistogramDataProvider extends AbstractTmfTraceDataProvider implements ITmfTreeXYDataProvider<TmfTreeDataModel> {

    /**
     * Extension point ID.
     */
    public static final String ID = "org.eclipse.tracecompass.internal.examples.histogram.HistogramDataProvider"; //$NON-NLS-1$
    static final String TITLE = Objects.requireNonNull(Messages.HistogramDataProvider_Title);
    private static final AtomicLong TRACE_IDS = new AtomicLong();

    private final TmfStatisticsModule fModule;
    private final TmfModelResponse<List<TmfTreeDataModel>> fResponse;
    private final long fId;

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
        fId = TRACE_IDS.incrementAndGet();
        TmfTreeDataModel model = new TmfTreeDataModel(fId, -1, trace.getName());
        fResponse = new TmfModelResponse<>(Collections.singletonList(model), ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }

    @Override
    public @NonNull TmfModelResponse<ITmfCommonXAxisModel> fetchXY(@NonNull TimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        long[] xValues = filter.getTimesRequested();

        if (!(filter instanceof SelectionTimeQueryFilter) || !((SelectionTimeQueryFilter) filter).getSelectedItems().contains(fId)) {
            return TmfCommonXAxisResponseFactory.create(TITLE, xValues, Collections.emptyMap(), true);
        }

        int n = xValues.length;

        final ITmfStatistics stats = Objects.requireNonNull(fModule.getStatistics());
        List<Long> values = stats.histogramQuery(filter.getStart(), filter.getEnd(), n);

        double[] y = new double[n];
        Arrays.setAll(y, values::get);

        String seriesName = getTrace().getName();
        Map<String, IYModel> ySeries = ImmutableMap.of(seriesName, new YModel(seriesName, y));
        return TmfCommonXAxisResponseFactory.create(TITLE, xValues, ySeries, true);
    }

    @Override
    public TmfModelResponse<List<TmfTreeDataModel>> fetchTree(TimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        return fResponse;
    }

    @Override
    public String getId() {
        return ID;
    }
}
