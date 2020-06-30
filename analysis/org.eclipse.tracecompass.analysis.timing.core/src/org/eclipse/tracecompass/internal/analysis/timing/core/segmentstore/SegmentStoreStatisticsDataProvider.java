/**********************************************************************
 * Copyright (c) 2018, 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.internal.analysis.timing.core.segmentstore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.SegmentStoreStatisticsModel;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.statistics.AbstractSegmentStatisticsAnalysis;
import org.eclipse.tracecompass.analysis.timing.core.statistics.IStatistics;
import org.eclipse.tracecompass.analysis.timing.core.statistics.IStatisticsAnalysis;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.TableCellDescriptor;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.TableColumnDescriptor;
import org.eclipse.tracecompass.internal.tmf.core.model.AbstractTmfTraceDataProvider;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderParameterUtils;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataType;
import org.eclipse.tracecompass.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.tmf.core.model.ITableCellDescriptor;
import org.eclipse.tracecompass.tmf.core.model.ITableColumnDescriptor;
import org.eclipse.tracecompass.tmf.core.model.filters.FilterTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse.Status;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.collect.ImmutableList;

/**
 * This data provider will supply a statistics tree for an
 * {@link AbstractSegmentStatisticsAnalysis}. Passing a
 * {@link FilterTimeQueryFilter} will also return the statistics from the
 * selected range.
 *
 * @author Loic Prieur-Drevon
 * @since 4.0
 */
public class SegmentStoreStatisticsDataProvider extends AbstractTmfTraceDataProvider
        implements ITmfTreeDataProvider<SegmentStoreStatisticsModel> {

    /**
     * Base {@link SegmentStoreStatisticsDataProvider} prefix
     */
    public static final String ID = "org.eclipse.tracecompass.analysis.timing.core.segmentstore.SegmentStoreStatisticsDataProvider"; //$NON-NLS-1$

    private static final String STATISTICS_SUFFIX = ".statistics"; //$NON-NLS-1$
    private static final String TOTAL_PREFIX = "Total_"; //$NON-NLS-1$
    private static final String SELECTION_PREFIX = "Selection_"; //$NON-NLS-1$
    private static final Map<IStatisticsAnalysis, SegmentStoreStatisticsDataProvider> PROVIDER_MAP = new WeakHashMap<>();
    private static final AtomicLong ENTRY_ID = new AtomicLong();

    private final IStatisticsAnalysis fProvider;
    private final String fId;

    private final Map<String, Long> fIdToType = new HashMap<>();
    private final long fTraceId = ENTRY_ID.getAndIncrement();

    private final @Nullable IAnalysisModule fModule;

    /**
     * Get an instance of {@link SegmentStoreStatisticsDataProvider} for a trace and
     * provider. Returns a null instance if the ISegmentStoreProvider is null. If
     * the provider is an instance of {@link IAnalysisModule}, analysis is also
     * scheduled.
     *
     * @param trace
     *            A trace on which we are interested to fetch a model
     * @param module
     *            the ID of the analysis to generate this provider from.
     * @return An instance of SegmentStoreDataProvider. Returns a null if the
     *         ISegmentStoreProvider is null.
     */
    public static synchronized @Nullable SegmentStoreStatisticsDataProvider getOrCreate(ITmfTrace trace, AbstractSegmentStatisticsAnalysis module) {
        // TODO experiment support.
        return PROVIDER_MAP.computeIfAbsent(module, p -> new SegmentStoreStatisticsDataProvider(trace, p, module.getId() + STATISTICS_SUFFIX));
    }

    /**
     * Constructor
     *
     * @param trace
     *            the trace for which this provider will supply info
     * @param provider
     *            the segment statistics module from which to get data
     * @param id
     *            the extension point ID
     */
    public SegmentStoreStatisticsDataProvider(ITmfTrace trace, IStatisticsAnalysis provider, String id) {
        super(trace);
        fId = id;
        fProvider = provider;
        fModule = provider instanceof IAnalysisModule ? (IAnalysisModule) provider : null;
    }

    @Override
    public TmfModelResponse<TmfTreeModel<SegmentStoreStatisticsModel>> fetchTree(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        IAnalysisModule module = fModule;
        if (module != null) {
            if (monitor != null) {
                module.waitForCompletion(monitor);
                if (monitor.isCanceled()) {
                    return new TmfModelResponse<>(null, Status.CANCELLED, CommonStatusMessage.TASK_CANCELLED);
                }
            } else {
                module.waitForCompletion();
            }
        }

        IStatistics<ISegment> statsTotal = fProvider.getStatsTotal();
        if (statsTotal == null) {
            return new TmfModelResponse<>(null, Status.FAILED, CommonStatusMessage.ANALYSIS_INITIALIZATION_FAILED);
        }

        List<SegmentStoreStatisticsModel> list = new ArrayList<>();
        list.add(new SegmentStoreStatisticsModel(fTraceId, -1, getCellLabels(NonNullUtils.nullToEmptyString(getTrace().getName()), statsTotal), getCellDescriptors(), statsTotal));

        /*
         * Add statistics for full duration.
         */
        long totalId = getUniqueId(TOTAL_PREFIX);
        list.add(new SegmentStoreStatisticsModel(totalId, fTraceId, getCellLabels(Objects.requireNonNull(Messages.SegmentStoreStatisticsDataProvider_Total), statsTotal), statsTotal));
        Map<String, IStatistics<ISegment>> totalStats = fProvider.getStatsPerType();
        for (Entry<String, IStatistics<ISegment>> entry : totalStats.entrySet()) {
            IStatistics<ISegment> statistics = entry.getValue();
            list.add(new SegmentStoreStatisticsModel(getUniqueId(TOTAL_PREFIX + entry.getKey()), totalId, getCellLabels(entry.getKey(), statistics), statistics));
        }

        /*
         * Add statistics for selection if any.
         */
        TimeQueryFilter filter = FetchParametersUtils.createTimeQuery(fetchParameters);
        Boolean isFiltered = DataProviderParameterUtils.extractIsFiltered(fetchParameters);
        if (filter != null && isFiltered != null && isFiltered) {
            long start = filter.getStart();
            long end = filter.getEnd();

            IProgressMonitor nonNullMonitor = monitor != null ? monitor : new NullProgressMonitor();
            IStatistics<ISegment> statsForRange = fProvider.getStatsForRange(start, end, nonNullMonitor);
            if (statsForRange == null) {
                return new TmfModelResponse<>(null, Status.CANCELLED, CommonStatusMessage.TASK_CANCELLED);
            }

            long selectionId = getUniqueId(SELECTION_PREFIX);
            list.add(new SegmentStoreStatisticsModel(selectionId, fTraceId, Collections.singletonList(Objects.requireNonNull(Messages.SegmentStoreStatisticsDataProvider_Selection)), statsForRange));
            Map<String, IStatistics<ISegment>> selectionStats = fProvider.getStatsPerTypeForRange(start, end, nonNullMonitor);
            for (Entry<String, IStatistics<ISegment>> entry : selectionStats.entrySet()) {
                IStatistics<ISegment> statistics = entry.getValue();
                list.add(new SegmentStoreStatisticsModel(getUniqueId(SELECTION_PREFIX + entry.getKey()), selectionId, getCellLabels(entry.getKey(), statistics), statistics));
            }
        }
        TmfTreeModel.Builder<SegmentStoreStatisticsModel> treeModelBuilder = new TmfTreeModel.Builder<>();
        treeModelBuilder.setColumnDescriptors(getColumnDescriptors());
        treeModelBuilder.setEntries(Collections.unmodifiableList(list));
        return new TmfModelResponse<>(treeModelBuilder.build(), Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }

    /**
     * Gets the list of cell descriptors.
     *
     * Overriding this method allows to have different table column descriptors.
     *
     * Note that {@link SegmentStoreStatisticsDataProvider#getColumnDescriptors()},
     * {@link SegmentStoreStatisticsDataProvider#getCellDescriptors()} and
     * {@link SegmentStoreStatisticsDataProvider#getCellLabels(String, IStatistics)}
     * have to be consistent and provide a list with the same length.
     *
     * @return list of cell descriptors
     */
    protected List<ITableColumnDescriptor> getColumnDescriptors() {
        ImmutableList.Builder<ITableColumnDescriptor> headers = new ImmutableList.Builder<>();
        TableColumnDescriptor.Builder builder = new TableColumnDescriptor.Builder();
        builder.setText(Objects.requireNonNull(Messages.SegmentStoreStatistics_Label));
        headers.add(builder.build());

        builder = new TableColumnDescriptor.Builder();
        builder.setText(Objects.requireNonNull(Messages.SegmentStoreStatistics_MinLabel));
        headers.add(builder.build());

        builder = new TableColumnDescriptor.Builder();
        builder.setText(Objects.requireNonNull(Messages.SegmentStoreStatistics_MaxLabel));
        headers.add(builder.build());

        builder = new TableColumnDescriptor.Builder();
        builder.setText(Objects.requireNonNull(Messages.SegmentStoreStatistics_AverageLabel));
        headers.add(builder.build());

        builder = new TableColumnDescriptor.Builder();
        builder.setText(Objects.requireNonNull(Messages.SegmentStoreStatistics_StandardDeviationLabel));
        headers.add(builder.build());

        builder = new TableColumnDescriptor.Builder();
        builder.setText(Objects.requireNonNull(Messages.SegmentStoreStatistics_CountLabel));
        headers.add(builder.build());

        builder = new TableColumnDescriptor.Builder();
        builder.setText(Objects.requireNonNull(Messages.SegmentStoreStatistics_TotalLabel));
        headers.add(builder.build());

        builder = new TableColumnDescriptor.Builder();
        builder.setText(Objects.requireNonNull(Messages.SegmentStoreStatistics_MinStartLabel));
        headers.add(builder.build());

        builder = new TableColumnDescriptor.Builder();
        builder.setText(Objects.requireNonNull(Messages.SegmentStoreStatistics_MinEndLabel));
        headers.add(builder.build());

        builder = new TableColumnDescriptor.Builder();
        builder.setText(Objects.requireNonNull(Messages.SegmentStoreStatistics_MaxStartLabel));
        headers.add(builder.build());

        builder = new TableColumnDescriptor.Builder();
        builder.setText(Objects.requireNonNull(Messages.SegmentStoreStatistics_MaxEndLabel));
        headers.add(builder.build());
        return headers.build();
    }

    /**
     * Gets the list of cell descriptors.
     *
     * Overriding this method allows for providing custom cell descriptors.
     *
     * Note that {@link SegmentStoreStatisticsDataProvider#getColumnDescriptors()},
     * {@link SegmentStoreStatisticsDataProvider#getCellDescriptors()} and
     * {@link SegmentStoreStatisticsDataProvider#getCellLabels(String, IStatistics)}
     * have to be consistent and provide a list with the same length.
     *
     * @return list of cell descriptors
     */
    protected List<ITableCellDescriptor> getCellDescriptors() {
        ImmutableList.Builder<ITableCellDescriptor> headers = new ImmutableList.Builder<>();
        // Header text
        headers.add(new TableCellDescriptor.Builder().setDataType(DataType.STRING).build());
        // Minimum duration
        headers.add(new TableCellDescriptor.Builder().setDataType(DataType.DURATION).build());
        // Maximum duration
        headers.add(new TableCellDescriptor.Builder().setDataType(DataType.DURATION).build());
        // Average duration
        headers.add(new TableCellDescriptor.Builder().setDataType(DataType.DURATION).build());
        // Standard Deviation
        headers.add(new TableCellDescriptor.Builder().setDataType(DataType.DURATION).build());
        // Number of elements (count)
        headers.add(new TableCellDescriptor.Builder().setDataType(DataType.NUMBER).build());
        // Total duration
        headers.add(new TableCellDescriptor.Builder().setDataType(DataType.DURATION).build());
        // Start time of minimum duration
        headers.add(new TableCellDescriptor.Builder().setDataType(DataType.TIMESTAMP).build());
        // End time of minimum duration
        headers.add(new TableCellDescriptor.Builder().setDataType(DataType.TIMESTAMP).build());
        // Start time of maximum duration
        headers.add(new TableCellDescriptor.Builder().setDataType(DataType.TIMESTAMP).build());
        // End time of maximum duration
        headers.add(new TableCellDescriptor.Builder().setDataType(DataType.TIMESTAMP).build());
        return headers.build();
    }

    /**
     * Returns a list of cell labels.
     *
     * Overriding this method allows for providing custom cell labels.
     *
     * Note that {@link SegmentStoreStatisticsDataProvider#getColumnDescriptors()},
     * {@link SegmentStoreStatisticsDataProvider#getCellDescriptors()} and
     * {@link SegmentStoreStatisticsDataProvider#getCellLabels(String, IStatistics)}
     * have to be consistent and provide a list with the same length.
     *
     * @param name
     *            the name value of the label column per row. Use as is.
     * @param statistics
     *            the {@link IStatistics} implementation to get the cell statistics labels from
     * @return the list of cell label
     */
    protected List<String> getCellLabels(String name, IStatistics<ISegment> statistics) {
        ImmutableList.Builder<String> labels = new ImmutableList.Builder<>();

        ISegment min = statistics.getMinObject();
        long minStart = 0;
        long minEnd = 0;
        if (min != null) {
            minStart = min.getStart();
            minEnd = min.getEnd();
        }
        ISegment max = statistics.getMaxObject();
        long maxStart = 0;
        long maxEnd = 0;
        if (max != null) {
            maxStart = max.getStart();
            maxEnd = max.getEnd();
        }
        labels.add(name)
        .add(String.valueOf(statistics.getMin()))
        .add(String.valueOf(statistics.getMax()))
        .add(String.valueOf(statistics.getMean()))
        .add(String.valueOf(statistics.getStdDev()))
        .add(String.valueOf(statistics.getTotal()))
        .add(String.valueOf(statistics.getNbElements()))
        .add(String.valueOf(minStart))
        .add(String.valueOf(minEnd))
        .add(String.valueOf(maxStart))
        .add(String.valueOf(maxEnd));
        return labels.build();
    }

    private long getUniqueId(String name) {
        synchronized (fIdToType) {
            return fIdToType.computeIfAbsent(name, n -> ENTRY_ID.getAndIncrement());
        }
    }

    @Override
    public String getId() {
        return fId;
    }

    @Override
    public void dispose() {
        synchronized (fIdToType) {
            fIdToType.clear();
        }
        if (fModule != null) {
            fModule.dispose();
        }
    }
}
