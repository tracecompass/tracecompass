/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.examples.histogram;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.AbstractTmfTraceDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.TmfCommonXAxisResponseFactory;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfCommonXAxisResponse;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfXYDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.IYModel;
import org.eclipse.tracecompass.internal.tmf.core.model.YModel;
import org.eclipse.tracecompass.tmf.core.statistics.ITmfStatistics;
import org.eclipse.tracecompass.tmf.core.statistics.TmfStatisticsModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

import com.google.common.collect.ImmutableMap;

/**
 * This data provider will return a XY model (model is wrapped in a response)
 * based on a query filter. The model is used afterwards by any viewer to draw
 * charts. Model returned is for the new Histogram viewer
 *
 * @author Yonni Chen
 */
@SuppressWarnings("restriction")
public class HistogramDataProvider extends AbstractTmfTraceDataProvider implements ITmfXYDataProvider {

    /**
     * Constructor
     *
     * @param trace
     *            A trace on which we are interested to fetch a model
     */
    public HistogramDataProvider(ITmfTrace trace) {
        super(trace);
    }

    @Override
    public @NonNull ITmfCommonXAxisResponse fetchXY(@NonNull TimeQueryFilter filter, @Nullable IProgressMonitor monitor) {

        long[] xValues = filter.getTimesRequested();
        int n = xValues.length;
        double[] y = new double[n];
        long[] yLong = new long[n];

        /* Add the values for each trace */
        for (ITmfTrace trace : TmfTraceManager.getTraceSet(getTrace())) {
            /* Retrieve the statistics object */
            final TmfStatisticsModule statsMod = TmfTraceUtils.getAnalysisModuleOfClass(trace, TmfStatisticsModule.class, TmfStatisticsModule.ID);

            if (statsMod == null) {
                /* No statistics module available for this trace */
                continue;
            }
            statsMod.waitForInitialization();
            final ITmfStatistics stats = Objects.requireNonNull(statsMod.getStatistics());
            List<Long> values = stats.histogramQuery(filter.getStart(), filter.getEnd(), n);

            for (int i = 0; i < n; i++) {
                yLong[i] += values.get(i);
            }
        }

        for (int i = 0; i < n; i++) {
            y[i] = yLong[i]; /* casting from long to double */
        }

        String title = Objects.requireNonNull(Messages.HistogramDataProvider_Title);
        String seriesName = Objects.requireNonNull(Messages.HistogramDataProvider_NumberOfEvent);
        Map<String, IYModel> ySeries = ImmutableMap.of(seriesName, new YModel(seriesName, y));
        return TmfCommonXAxisResponseFactory.create(title, xValues, ySeries, filter.getEnd(), true);
    }
}
