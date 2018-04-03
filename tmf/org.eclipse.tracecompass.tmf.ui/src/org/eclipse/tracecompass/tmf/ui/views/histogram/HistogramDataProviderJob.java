/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.histogram;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.common.core.log.TraceCompassLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils;
import org.eclipse.tracecompass.internal.provisional.tmf.core.histogram.HistogramDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.TmfTreeDataModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ISeriesModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfTreeXYDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfXyModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderManager;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import com.google.common.collect.Collections2;

/**
 * Job that makes query to the histogram data provider and update the provided
 * histogram model.
 *
 * @author Simon Delisle
 * @since 3.4
 */
public class HistogramDataProviderJob extends Job {
    private static final int BUILD_UPDATE_TIMEOUT = 500;

    private static final String LOST_EVENTS_SERIES_SUFFIX = "Lost"; //$NON-NLS-1$

    private static final @NonNull Logger LOGGER = TraceCompassLog.getLogger(HistogramDataProviderJob.class);

    private final @NonNull ITmfTrace fTrace;
    private final HistogramDataModel fHistogramModel;
    private final TmfTimeRange fRange;

    /**
     * Constructor
     *
     * @param name
     *            Name of the Job
     * @param trace
     *            The trace
     * @param model
     *            Histogram model to update
     * @param range
     *            Time range to query
     */
    public HistogramDataProviderJob(String name, @NonNull ITmfTrace trace, HistogramDataModel model, TmfTimeRange range) {
        super(name);
        fTrace = trace;
        fHistogramModel = model;
        fRange = range;
    }

    @Override
    public IStatus run(IProgressMonitor monitor) {
        long startTime = fRange.getStartTime().toNanos();
        long endTime = fRange.getEndTime().toNanos();

        boolean isComplete = false;
        ITmfTreeXYDataProvider<@NonNull TmfTreeDataModel> dataProvider = DataProviderManager.getInstance().getDataProvider(fTrace, HistogramDataProvider.ID, ITmfTreeXYDataProvider.class);
        if (dataProvider == null) {
            return Status.CANCEL_STATUS;
        }

        do {
            if (monitor.isCanceled()) {
                return Status.CANCEL_STATUS;
            }

            TmfModelResponse<@NonNull List<@NonNull TmfTreeDataModel>> response = dataProvider.fetchTree(new TimeQueryFilter(0, 1, 2), monitor);
            List<@NonNull TmfTreeDataModel> model = response.getModel();
            if (model == null) {
                return Status.CANCEL_STATUS;
            }

            Map<Long, String> traces = new HashMap<>();
            Map<Integer, String> traceEntryNames = new HashMap<>();
            int index = 0;
            for (TmfTreeDataModel entry : model) {
                if (entry.getParentId() == -1) {
                    traces.put(entry.getId(), entry.getName());
                    continue;
                }
                String parentTraceName = traces.get(entry.getParentId());
                if (parentTraceName != null) {
                    traceEntryNames.put(index, parentTraceName + '/' + entry.getName());
                    index++;
                }
            }

            Collection<Long> ids = Collections2.transform(model, TmfTreeDataModel::getId);
            if (ids == null || ids.isEmpty()) {
                return Status.CANCEL_STATUS;
            }
            int nbBuckets = fHistogramModel.getNbBuckets();
            TimeQueryFilter queryFilter = new SelectionTimeQueryFilter(startTime, endTime, nbBuckets, ids);
            TmfModelResponse<@NonNull ITmfXyModel> responseXY = dataProvider.fetchXY(queryFilter, monitor);
            ITmfXyModel modelXY = responseXY.getModel();
            if (modelXY == null || monitor.isCanceled()) {
                return Status.CANCEL_STATUS;
            }

            HistogramBucket[] buckets = new HistogramBucket[nbBuckets];
            long[] lostEvents = new long[nbBuckets];
            for (Entry<Integer, String> serieEntry : traceEntryNames.entrySet()) {
                int traceIndex = serieEntry.getKey();
                String traceName = serieEntry.getValue();
                ISeriesModel seriesModel = modelXY.getData().get(traceName);
                if (seriesModel != null) {
                    double[] series = seriesModel.getData();
                    if (traceName.endsWith(LOST_EVENTS_SERIES_SUFFIX)) {
                        handleLostEvents(lostEvents, series);
                    } else {
                        handleEvents(buckets, series, traceIndex, traceEntryNames.size());
                    }
                }
            }

            long duration = (endTime - startTime) / nbBuckets;
            if (duration > 0 && !monitor.isCanceled()) {
                fHistogramModel.setSelection(startTime, startTime);
                fHistogramModel.setData(buckets, lostEvents, startTime, duration);
            }

            ITmfResponse.Status status = responseXY.getStatus();
            if (status == ITmfResponse.Status.COMPLETED) {
                isComplete = true;
            } else if (status == ITmfResponse.Status.FAILED || status == ITmfResponse.Status.CANCELLED) {
                // Error occurred, return
                TraceCompassLogUtils.traceInstant(LOGGER, Level.WARNING, responseXY.getStatusMessage());
                isComplete = true;
            } else {
                // Still running, wait and request the data provider again
                try {
                    Thread.sleep(BUILD_UPDATE_TIMEOUT);
                } catch (InterruptedException e) {
                    // Retry querying the provider
                    TraceCompassLogUtils.traceInstant(LOGGER, Level.INFO, e.getMessage());
                }
            }
        } while (!isComplete);

        return Status.OK_STATUS;
    }

    private static void handleEvents(HistogramBucket[] buckets, double[] series, int traceIndex, int nbTrace) {
        for (int i = 0; i < series.length; i++) {
            HistogramBucket bucket = buckets[i];
            if (bucket == null) {
                bucket = new HistogramBucket(nbTrace);
            }
            bucket.addEvent(traceIndex, (int) series[i]);
            buckets[i] = bucket;
        }
    }

    private static void handleLostEvents(long[] lostEventBuckets, double[] series) {
        for (int i = 0; i < series.length; i++) {
            lostEventBuckets[i] += series[i];
        }
    }

    /**
     * Get the time range used for the request
     *
     * @return The time range
     */
    public TmfTimeRange getRange() {
        return fRange;
    }
}
