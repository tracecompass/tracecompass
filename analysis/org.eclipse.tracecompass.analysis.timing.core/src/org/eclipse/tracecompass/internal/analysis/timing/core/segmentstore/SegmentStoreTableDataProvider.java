/*******************************************************************************
 * Copyright (c) 2022 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Kyrollos Bekhet - Initial API and implementation
 ******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.timing.core.segmentstore;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.common.core.log.TraceCompassLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils.FlowScopeLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils.FlowScopeLogBuilder;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.VirtualTableQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.table.ITmfVirtualTableDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.table.ITmfVirtualTableModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.table.TmfVirtualTableModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.table.VirtualTableCell;
import org.eclipse.tracecompass.internal.tmf.core.model.AbstractTmfTraceDataProvider;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.tmf.core.TmfStrings;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderParameterUtils;
import org.eclipse.tracecompass.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.segment.ISegmentAspect;
import org.eclipse.tracecompass.tmf.core.segment.SegmentStartTimeAspect;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * This data provider will return a virtual table model (wrapped in a response)
 * Based on a virtual table query filter. Model returned is for segment store
 * tables to do analysis.
 *
 * @author: Kyrollos Bekhet
 */

public class SegmentStoreTableDataProvider extends AbstractTmfTraceDataProvider implements ITmfVirtualTableDataProvider<TmfTreeDataModel, SegmentStoreTableLine> {
    /**
     *
     * A simple class to create checkpoints to index the segments of a segment
     * store
     */
    private static class SegmentStoreIndex {
        private long fCounter;
        private long fStartTimestamp;

        public SegmentStoreIndex(long startTimeStamp, long counter) {
            fStartTimestamp = startTimeStamp;
            fCounter = counter;
        }

        public long getStartTimestamp() {
            return fStartTimestamp;
        }

        public long getCounter() {
            return fCounter;
        }
    }

    /**
     * A predicate implementation that is used to evaluate if a segment is the
     * first of the checkpoint.
     *
     * @author Kyrollos Bekhet.
     *
     */
    private static class SegmentPredicate implements Predicate<ISegment> {
        private final long fStartTime;
        private long fCount;

        public SegmentPredicate(long startTime, long count) {
            fStartTime = startTime;
            fCount = count;
        }

        @Override
        public boolean test(ISegment segment) {
            if (segment.getStart() > fStartTime) {
                return true;
            }
            if (segment.getStart() == fStartTime) {
                if (fCount == 0) {
                    return true;
                }
                fCount--;
            }
            return false;
        }
    }

    public static final String ID = "org.eclipse.tracecompass.analysis.timing.core.segmentstore.SegmentStoreTableDataProvider";
    private static final AtomicLong fAtomicLong = new AtomicLong();
    private static BiMap<ISegmentAspect, Long> fAspectToIdMap = HashBiMap.create();
    private static final Format FORMATTER = new DecimalFormat("###,###.##");
    private static final long STEP = 1000;
    private static final Logger LOGGER = TraceCompassLog.getLogger(SegmentStoreTableDataProvider.class);

    private final Object fLock = new Object();
    private final String fId;
    private final Comparator<ISegment> fComparator;
    private List<SegmentStoreIndex> fIndexes = new ArrayList<>();
    private @Nullable ISegmentStoreProvider fSegmentProvider = null;

    /**
     * Constructor
     *
     * @param trace
     *            A trace on which we are interested to fetch a segment store
     *            table model.
     *
     * @param segmentProvider
     *            The segment provider that contains the data and from which the
     *            data will be fetched.
     *
     * @param analysisId
     *            The analysis identifier.
     */
    public SegmentStoreTableDataProvider(ITmfTrace trace, ISegmentStoreProvider segmentProvider, String analysisId) {
        super(trace);
        TraceCompassLogUtils.traceObjectCreation(LOGGER, Level.FINE, this);
        fId = analysisId;
        fComparator = (Comparator<ISegment>) SegmentStartTimeAspect.SEGMENT_START_TIME_ASPECT.getComparator();
        if (segmentProvider.getSegmentStore() != null) {
            buildIndex(segmentProvider);
        } else {
            if (segmentProvider instanceof IAnalysisModule) {
                ((IAnalysisModule) segmentProvider).schedule();
                ((IAnalysisModule) segmentProvider).waitForCompletion();
                buildIndex(segmentProvider);
            }
        }

    }

    @Override
    public void dispose() {
        TraceCompassLogUtils.traceObjectDestruction(LOGGER, Level.FINE, this, 10);
    }

    /**
     * Build the indexes which will act like checkpoints for the data provider.
     *
     * @param segmentProvider
     *            The segment provider to use.
     */
    private void buildIndex(ISegmentStoreProvider segmentProvider) {
        TraceCompassLogUtils.traceAsyncStart(LOGGER, Level.FINE, "SegmentStoreTableDataProvider#buildIndex", fId, 0);
        fSegmentProvider = segmentProvider;
        long i = 0;
        if (fSegmentProvider != null) {
            ISegmentStore<ISegment> segStore = fSegmentProvider.getSegmentStore();
            if (segStore != null) {
                synchronized (fLock) {
                    try (FlowScopeLog scope = new FlowScopeLogBuilder(LOGGER, Level.FINE, "SegmentStoreTableDataProvider#buildIndex.buildingIndexes").build()) {
                        TraceCompassLogUtils.traceObjectCreation(LOGGER, Level.FINE, fLock);
                        Iterable<ISegment> sortedSegmentStore = segStore.iterator(fComparator);
                        long counter = 0;
                        long previousTimestamp = Long.MAX_VALUE;
                        for (ISegment segment : sortedSegmentStore) {
                            if (segment.getStart() == previousTimestamp) {
                                counter++;
                            } else {
                                previousTimestamp = segment.getStart();
                                counter = 0;
                            }
                            if (i % STEP == 0) {
                                fIndexes.add(new SegmentStoreIndex(segment.getStart(), counter));
                            }
                            i++;
                        }
                    } catch (Exception ex) {
                        TraceCompassLogUtils.traceInstant(LOGGER, Level.SEVERE, "error build index", ex.getMessage());
                    } finally {
                        TraceCompassLogUtils.traceObjectDestruction(LOGGER, Level.FINE, fLock);
                    }
                }
            }
        }
    }

    @Override
    public String getId() {
        return fId;
    }

    @Override
    public TmfModelResponse<TmfTreeModel<TmfTreeDataModel>> fetchTree(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        List<TmfTreeDataModel> model = new ArrayList<>();
        for (final ISegmentAspect aspect : ISegmentStoreProvider.getBaseSegmentAspects()) {
            synchronized (fAspectToIdMap) {
                long id = fAspectToIdMap.computeIfAbsent(aspect, a -> fAtomicLong.getAndIncrement());
                model.add(new TmfTreeDataModel(id, -1, Collections.singletonList(aspect.getName())));
            }
        }
        if (fSegmentProvider != null) {
            synchronized (fAspectToIdMap) {
                for (final ISegmentAspect aspect : fSegmentProvider.getSegmentAspects()) {
                    long id = fAspectToIdMap.computeIfAbsent(aspect, a -> fAtomicLong.getAndIncrement());
                    model.add(new TmfTreeDataModel(id, -1, Collections.singletonList(aspect.getName())));
                }
            }
        }
        return new TmfModelResponse<>(new TmfTreeModel<>(Collections.emptyList(), model), ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }

    @Override
    public TmfModelResponse<ITmfVirtualTableModel<SegmentStoreTableLine>> fetchLines(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        TraceCompassLogUtils.traceAsyncStart(LOGGER, Level.FINE, "SegmentStoreTableDataProvider#fetchLines", fId, 2);
        if (!fetchParameters.containsKey(DataProviderParameterUtils.REQUESTED_COLUMN_IDS_KEY)) {
            fetchParameters.put(DataProviderParameterUtils.REQUESTED_COLUMN_IDS_KEY, Collections.emptyList());
        }
        VirtualTableQueryFilter queryFilter = FetchParametersUtils.createVirtualTableQueryFilter(fetchParameters);
        if (queryFilter == null) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, CommonStatusMessage.INCORRECT_QUERY_PARAMETERS);
        }
        Map<Long, ISegmentAspect> aspects = getAspectsFromColumnId(queryFilter.getColumnsId());
        if (aspects.isEmpty()) {
            return new TmfModelResponse<>(new TmfVirtualTableModel<>(Collections.emptyList(), Collections.emptyList(), queryFilter.getIndex(), 0), ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
        }
        List<Long> columnIds = new ArrayList<>(aspects.keySet());
        if (fSegmentProvider != null) {
            ISegmentStore<ISegment> segStore = fSegmentProvider.getSegmentStore();
            if (segStore != null) {
                if (segStore.isEmpty()) {
                    return new TmfModelResponse<>(new TmfVirtualTableModel<>(columnIds, Collections.emptyList(), queryFilter.getIndex(), 0), ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
                }
                if (queryFilter.getIndex() > segStore.size()) {
                    return new TmfModelResponse<>(null, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
                }
                synchronized (fLock) {
                    try (FlowScopeLog scope = new FlowScopeLogBuilder(LOGGER, Level.FINE, "SegmentStoreTableDataProvider#fetchLines").build()) {
                        TraceCompassLogUtils.traceObjectCreation(LOGGER, Level.FINE, fLock);
                        List<SegmentStoreTableLine> lines = new ArrayList<>();
                        final int startIndexRank = (int) (queryFilter.getIndex() / STEP);
                        final int actualStartQueryIndex = (int) (queryFilter.getIndex() % STEP);
                        SegmentStoreIndex segIndex = fIndexes.get(startIndexRank);
                        long start = segIndex.getStartTimestamp();
                        int endIndexRank = (int) ((queryFilter.getIndex() + queryFilter.getCount() + STEP - 1) / STEP);
                        long end;
                        if (endIndexRank < fIndexes.size()) {
                            end = fIndexes.get(endIndexRank).getStartTimestamp();
                        } else {
                            end = Long.MAX_VALUE;
                        }
                        SegmentPredicate filter = new SegmentPredicate(start, segIndex.getCounter());
                        List<ISegment> newSegStore = segStore.getIntersectingElements(start, end, fComparator, filter);
                        for (int i = actualStartQueryIndex; i < newSegStore.size(); i++) {
                            if (queryFilter.getCount() == lines.size()) {
                                break;
                            }
                            long lineNumber = queryFilter.getIndex() + lines.size();
                            SegmentStoreTableLine newLine = buildSegmentStoreTableLine(aspects, newSegStore.get(i), lineNumber);
                            lines.add(newLine);
                        }
                        return new TmfModelResponse<>(new TmfVirtualTableModel<>(columnIds, lines, queryFilter.getIndex(), segStore.size()), ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
                    } catch (Exception ex) {
                        TraceCompassLogUtils.traceInstant(LOGGER, Level.SEVERE, "error fetching lines ", ex.getMessage());
                    } finally {
                        TraceCompassLogUtils.traceObjectDestruction(LOGGER, Level.FINE, fLock);
                    }
                }
            }
        }
        return new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, CommonStatusMessage.INCORRECT_QUERY_PARAMETERS);
    }

    /**
     * Builds the table line.
     *
     * @param aspects
     *            The aspects to resolve.
     *
     * @param segment
     *            The segment that contains the data that will fill the line.
     *
     * @param lineNumber
     *            The line number that will be assigned to the line that will be
     *            built.
     *
     * @return Returns a SegmentStoreTableLine that contains the data which will
     *         be displayed by the client.
     */
    private static SegmentStoreTableLine buildSegmentStoreTableLine(Map<Long, ISegmentAspect> aspects, ISegment segment, long lineNumber) {
        List<VirtualTableCell> entry = new ArrayList<>(aspects.size());
        for (Entry<Long, ISegmentAspect> aspectEntry : aspects.entrySet()) {
            Object aspectResolved = aspectEntry.getValue().resolve(segment);
            String cellContent;
            if (aspectEntry.getValue().getName().equals(TmfStrings.duration())) {
                cellContent = NonNullUtils.nullToEmptyString(FORMATTER.format(aspectResolved));
            } else if (aspectEntry.getValue().getName().equals(TmfStrings.startTime()) || aspectEntry.getValue().getName().equals(TmfStrings.endTime())) {
                cellContent = TmfTimestamp.fromNanos((Long) Objects.requireNonNull(aspectResolved)).toString();
            } else {
                cellContent = aspectResolved == null ? StringUtils.EMPTY : String.valueOf(aspectResolved);
            }
            entry.add(new VirtualTableCell(cellContent));
        }
        SegmentStoreTableLine tableLine = new SegmentStoreTableLine(entry, lineNumber);
        return tableLine;
    }

    /**
     * @param desiredColumns
     *            The list of desired column ids that we want to retreive
     *
     * @return The list of {@link ISegmentAspect} that matches the desired
     *         columns ids
     */
    private static Map<Long, ISegmentAspect> getAspectsFromColumnId(List<Long> desiredColumns) {
        Map<Long, ISegmentAspect> aspects = new LinkedHashMap<>();
        if (!desiredColumns.isEmpty()) {
            for (Long columnId : desiredColumns) {
                ISegmentAspect aspect = fAspectToIdMap.inverse().get(columnId);
                if (aspect != null) {
                    aspects.put(columnId, aspect);
                }
            }
            return aspects;
        }
        return Objects.requireNonNull(fAspectToIdMap.inverse());
    }

}
