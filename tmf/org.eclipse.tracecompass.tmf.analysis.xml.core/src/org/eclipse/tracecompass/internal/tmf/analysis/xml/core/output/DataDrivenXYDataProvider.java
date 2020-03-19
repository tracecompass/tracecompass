/*******************************************************************************
 * Copyright (c) 2019 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.Messages;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output.DataDrivenOutputEntry.IdGetter;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output.DataDrivenOutputEntry.QuarkCallback;
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
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphEntryModel;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfTreeXYDataProvider;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfXyModel;
import org.eclipse.tracecompass.tmf.core.model.xy.IYModel;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;

/**
 * This data provider will return XY models (wrapped in a response) based on a
 * query filter. The models can be used afterwards by any viewer to draw XY
 * charts. Model returned is for XML analysis.
 *
 * TODO: There is code duplication with the time graph data provider. Share it
 * between them
 *
 * @author Geneviève Bastien
 */
public class DataDrivenXYDataProvider extends AbstractTmfTraceDataProvider
        implements ITmfTreeXYDataProvider<ITmfTreeDataModel> {

    /**
     * Data provider ID
     */
    public static final String ID = "org.eclipse.tracecompass.tmf.analysis.xml.core.module.XmlXYDataProvider"; //$NON-NLS-1$

    private static final String TITLE = Objects.requireNonNull(Messages.XmlDataProvider_DefaultXYTitle);
    private static final AtomicLong ENTRY_IDS = new AtomicLong();

    /**
     * The type of XY display
     */
    public enum DisplayType {
        /** Displays absolute value */
        ABSOLUTE,
        /** Displays the difference between current and previous value */
        DELTA
    }

    /**
     * Remember the unique mappings of state system and quark to entry ID.
     */
    private final Table<ITmfStateSystem, Integer, Long> fBaseQuarkToId = HashBasedTable.create();
    private final Map<Long, DisplayElement> fIDToDisplayQuark = new HashMap<>();
    private final Map<Long, String> fIdToTitle = new HashMap<>();
    private final IdGetter fIdGenerator = (ss, quark) -> fBaseQuarkToId.row(ss).computeIfAbsent(quark, s -> ENTRY_IDS.getAndIncrement());
    private final QuarkCallback fQuarkCallback = (id, ss, quark, displayType) -> fIDToDisplayQuark.put(id, new DisplayElement(ss, quark, displayType));

    private final List<ITmfStateSystem> fSs;
    private final List<DataDrivenOutputEntry> fEntries;
    private final String fId;

    private @Nullable TmfModelResponse<TmfTreeModel<ITmfTreeDataModel>> fCached;

    private final ReentrantReadWriteLock fLock = new ReentrantReadWriteLock(false);

    private static class DisplayElement {
        private final ITmfStateSystem fStateSystem;
        private final int fQuark;
        private final DisplayType fDisplayType;

        public DisplayElement(ITmfStateSystem stateSystem, int quark, DisplayType displayType) {
            fStateSystem = stateSystem;
            fQuark = quark;
            fDisplayType = displayType;
        }
    }

    /**
     * Constructor
     *
     * @param trace
     *            The trace this data provider is for
     * @param stateSystems
     *            The list of state systems to build it for
     * @param entries
     *            The entries
     * @param id
     *            The ID of the data provider
     */
    public DataDrivenXYDataProvider(ITmfTrace trace, List<ITmfStateSystem> stateSystems, List<DataDrivenOutputEntry> entries, @Nullable String id) {
        super(trace);
        fSs = stateSystems;
        fEntries = entries;
        fId = (id == null) ? ID : id;
    }

    @Override
    public TmfModelResponse<ITmfXyModel> fetchXY(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        TimeQueryFilter filter = FetchParametersUtils.createTimeQuery(fetchParameters);
        if (filter == null) {
            return TmfXyResponseFactory.createFailedResponse(CommonStatusMessage.INCORRECT_QUERY_PARAMETERS);
        }
        long[] xValues = filter.getTimesRequested();

        filter = FetchParametersUtils.createSelectionTimeQuery(fetchParameters);
        if (filter == null) {
            return TmfXyResponseFactory.create(TITLE, xValues, Collections.emptyMap(), true);
        }

        Map<DisplayElement, IYModel> map = initSeries(filter);
        if (map.isEmpty()) {
            return TmfXyResponseFactory.create(TITLE, xValues, Collections.emptyMap(), true);
        }

        ITmfStateSystem ss = null;
        for (DisplayElement de : map.keySet()) {
            ss = de.fStateSystem;
        }
        if (ss == null) {
            return TmfXyResponseFactory.create(TITLE, xValues, Collections.emptyMap(), true);
        }

        long currentEnd = ss.getCurrentEndTime();

        try {
            for (int i = 0; i < xValues.length; i++) {
                if (monitor != null && monitor.isCanceled()) {
                    return TmfXyResponseFactory.createCancelledResponse(CommonStatusMessage.TASK_CANCELLED);
                }
                long time = xValues[i];
                if (time > currentEnd) {
                    break;
                } else if (ss.getStartTime() <= time) {
                    List<@NonNull ITmfStateInterval> full = ss.queryFullState(time);
                    for (Entry<DisplayElement, IYModel> series : map.entrySet()) {
                        int attributeQuark = series.getKey().fQuark;
                        if (attributeQuark >= 0 && attributeQuark < full.size()) {
                            Object value = full.get(attributeQuark).getValue();
                            series.getValue().getData()[i] = extractValue(value);
                        }
                    }
                }
            }
            // Update the series value if delta is requested
            for (Entry<DisplayElement, IYModel> series : map.entrySet()) {
                if (series.getKey().fDisplayType.equals(DisplayType.DELTA)) {
                    getSeriesDelta(series.getValue().getData());
                }
            }
        } catch (StateSystemDisposedException e) {
            return TmfXyResponseFactory.createFailedResponse(e.getMessage());
        }

        boolean complete = ss.waitUntilBuilt(0) || filter.getEnd() <= currentEnd;
        return TmfXyResponseFactory.create(TITLE, xValues, Maps.uniqueIndex(map.values(), value -> Long.toString(value.getId())), complete);
    }

    private static void getSeriesDelta(double[] data) {
        double prevData = data[0];
        data[0] = 0;
        for (int i = 1; i < data.length; i++) {
            double current = data[i];
            // Update value by subtracting previous value
            data[i] = current - prevData;
            prevData = current;
        }
        data[0] = data[1];
    }

    private Map<DisplayElement, IYModel> initSeries(TimeQueryFilter filter) {
        if (!(filter instanceof SelectionTimeQueryFilter)) {
            return Collections.emptyMap();
        }
        fLock.readLock().lock();
        try {
            Map<DisplayElement, IYModel> map = new HashMap<>();
            int length = filter.getTimesRequested().length;
            for (Long id : ((SelectionTimeQueryFilter) filter).getSelectedItems()) {
                DisplayElement displayElement = fIDToDisplayQuark.get(id);
                if (displayElement != null) {
                    String name = String.valueOf(fIdToTitle.get(id));
                    map.put(displayElement, new YModel(id, name, new double[length]));
                }
            }
            return map;
        } finally {
            fLock.readLock().unlock();
        }
    }

    private static double extractValue(@Nullable Object val) {
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        }
        return 0;
    }

    @Override
    public TmfModelResponse<TmfTreeModel<ITmfTreeDataModel>> fetchTree(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        fLock.readLock().lock();
        try {
            if (fCached != null) {
                return fCached;
            }
        } finally {
            fLock.readLock().unlock();
        }
        List<ITmfTreeDataModel> entryList = new ArrayList<>();
        boolean isComplete = true;

        String traceName = String.valueOf(getTrace().getName());
        fLock.writeLock().lock();
        try {
            for (ITmfStateSystem ss : fSs) {
                isComplete &= ss.waitUntilBuilt(0);
                /* Don't query empty state system */
                if (ss.getNbAttributes() > 0 && ss.getStartTime() != Long.MIN_VALUE) {
                    long start = ss.getStartTime();
                    long end = ss.getCurrentEndTime();
                    long id = fBaseQuarkToId.row(ss).computeIfAbsent(ITmfStateSystem.ROOT_ATTRIBUTE, s -> ENTRY_IDS.getAndIncrement());
                    TimeGraphEntryModel ssEntry = new TimeGraphEntryModel(id, -1, traceName, start, end);
                    entryList.add(ssEntry);

                    for (DataDrivenOutputEntry entry : fEntries) {
                        entryList.addAll(entry.buildEntries(ss, ssEntry.getId(), getTrace(), -1, StringUtils.EMPTY, end, fIdGenerator, fQuarkCallback));
                    }
                }
            }
            fIdToTitle.clear();
            entryList.forEach(e -> fIdToTitle.put(e.getId(), e.getName()));
            if (isComplete) {
                TmfModelResponse<TmfTreeModel<ITmfTreeDataModel>> tmfModelResponse = new TmfModelResponse<>(new TmfTreeModel<>(Collections.emptyList(), entryList), ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
                fCached = tmfModelResponse;
                return tmfModelResponse;
            }
            return new TmfModelResponse<>(new TmfTreeModel<>(Collections.emptyList(), entryList), ITmfResponse.Status.RUNNING, CommonStatusMessage.RUNNING);
        } finally {
            fLock.writeLock().unlock();
        }
    }

    @Override
    public String getId() {
        return fId;
    }

}
