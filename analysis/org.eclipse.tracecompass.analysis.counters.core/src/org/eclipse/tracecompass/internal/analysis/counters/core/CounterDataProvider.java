/**********************************************************************
 * Copyright (c) 2017, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.analysis.counters.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.counters.core.CounterAnalysis;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.internal.tmf.core.model.xy.AbstractTreeCommonXDataProvider;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderParameterUtils;
import org.eclipse.tracecompass.tmf.core.model.YModel;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectedCounterQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.model.xy.IYModel;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.TreeMultimap;

/**
 * This data provider will return a XY model (model is wrapped in a response)
 * based on a query filter. The model is used afterwards by any viewer to draw
 * charts. Model returned is for Counters views
 *
 * @author Mikael Ferland
 * @author Yonni Chen
 * @since 1.1
 */
public class CounterDataProvider extends AbstractTreeCommonXDataProvider<CounterAnalysis, TmfTreeDataModel> {

    /**
     * This data provider's extension point ID
     */
    public static final String ID = "org.eclipse.tracecompass.analysis.counters.core.CounterDataProvider"; //$NON-NLS-1$

    /**
     * Cumulative key to extract isCumulative from parameters map
     */
    public static final String CUMULATIVE_COUNTER_KEY = "isCumulative"; //$NON-NLS-1$

    /**
     * Chart's title
     */
    private static final String TITLE = Objects.requireNonNull(Messages.CounterDataProvider_ChartTitle);

    /**
     * Create an instance of {@link CounterDataProvider}. Returns a null instance if
     * the analysis module is not found.
     *
     * @param trace
     *            A trace on which we are interested to fetch a model
     * @param module
     *            A CounterAnalysis instance
     * @return A {@link CounterDataProvider} instance. If analysis module is not
     *         found, it returns null
     */
    public static @Nullable CounterDataProvider create(ITmfTrace trace, @Nullable CounterAnalysis module) {
        if (trace instanceof TmfExperiment) {
            throw new UnsupportedOperationException("This data providers does not support experiment"); //$NON-NLS-1$
        }

        if (module != null) {
            module.schedule();
            return new CounterDataProvider(trace, module);
        }
        return null;
    }

    /**
     * Constructor
     */
    private CounterDataProvider(ITmfTrace trace, CounterAnalysis analysis) {
        super(trace, analysis);
    }

    /**
     * @since 1.2
     */
    @Override
    protected TmfTreeModel<TmfTreeDataModel> getTree(ITmfStateSystem ss, Map<String, Object> parameters, @Nullable IProgressMonitor monitor) {
        List<TmfTreeDataModel> entries = new ArrayList<>();
        long rootId = getId(ITmfStateSystem.ROOT_ATTRIBUTE);
        entries.add(new TmfTreeDataModel(rootId, -1, Collections.singletonList(getTrace().getName())));

        addTreeViewerBranch(ss, rootId, Collections.singletonList(CounterAnalysis.GROUPED_COUNTER_ASPECTS_ATTRIB), entries);
        addTreeViewerBranch(ss, rootId, Collections.singletonList(CounterAnalysis.UNGROUPED_COUNTER_ASPECTS_ATTRIB), entries);

        return new TmfTreeModel<>(Collections.emptyList(), entries);
    }

    private void addTreeViewerBranch(ITmfStateSystem ss, long parentId, List<String> branchName, List<TmfTreeDataModel> entries) {
        int quark = ss.optQuarkAbsolute(branchName.get(0));
        if (quark != ITmfStateSystem.INVALID_ATTRIBUTE && !ss.getSubAttributes(quark, false).isEmpty()) {
            long id = getId(quark);
            TmfTreeDataModel branch = new TmfTreeDataModel(id, parentId, branchName);
            entries.add(branch);
            addTreeViewerEntries(ss, id, quark, entries);
        }
    }

    /**
     * Recursively add all child entries of a parent branch from the state system.
     */
    private void addTreeViewerEntries(ITmfStateSystem ss, long parentId, int quark, List<TmfTreeDataModel> entries) {
        for (int childQuark : ss.getSubAttributes(quark, false)) {
            long id = getId(childQuark);
            TmfTreeDataModel childBranch = new TmfTreeDataModel(id, parentId, Collections.singletonList(ss.getAttributeName(childQuark)));
            entries.add(childBranch);
            addTreeViewerEntries(ss, id, childQuark, entries);
        }
    }

    /**
     * @since 1.2
     */
    @Override
    protected @Nullable Map<String, IYModel> getYModels(ITmfStateSystem ss, Map<String, Object> fetchParameters,
            @Nullable IProgressMonitor monitor) throws StateSystemDisposedException {
        // Check if the parameters contain timeRequested and selectedItems
        // isCumulative will be compute later
        if (FetchParametersUtils.createSelectionTimeQuery(fetchParameters) != null) {
            return internalFetch(ss, fetchParameters, monitor);
        }
        return Collections.emptyMap();
    }

    private @Nullable Map<String, IYModel> internalFetch(ITmfStateSystem ss, Map<String, Object> fetchParameters,
            @Nullable IProgressMonitor monitor) throws StateSystemDisposedException {
        SelectedCounterQueryFilter filter = createCounterQuery(fetchParameters);
        if (filter == null) {
            return null;
        }
        long stateSystemEndTime = ss.getCurrentEndTime();
        Collection<Long> times = extractRequestedTimes(ss, filter, stateSystemEndTime);

        Map<Long, Integer> entries = Maps.filterValues(getSelectedEntries(filter), q -> ss.getSubAttributes(q, false).isEmpty());

        TreeMultimap<Integer, ITmfStateInterval> countersIntervals = TreeMultimap.create(Comparator.naturalOrder(),
                Comparator.comparingLong(ITmfStateInterval::getStartTime));

        Iterable<@NonNull ITmfStateInterval> query2d = ss.query2D(entries.values(), times);
        for (ITmfStateInterval interval : query2d) {
            if (monitor != null && monitor.isCanceled()) {
                return null;
            }
            countersIntervals.put(interval.getAttribute(), interval);
        }

        ImmutableMap.Builder<String, IYModel> ySeries = ImmutableMap.builder();
        for (Entry<Long, Integer> entry : entries.entrySet()) {
            if (monitor != null && monitor.isCanceled()) {
                return null;
            }
            int quark = entry.getValue();
            double[] yValues = buildYValues(countersIntervals.get(quark), filter);
            String seriesName = getTrace().getName() + '/' + ss.getFullAttributePath(quark);
            ySeries.put(seriesName, new YModel(entry.getKey(), seriesName, yValues));
        }

        return ySeries.build();
    }

    private static @Nullable SelectedCounterQueryFilter createCounterQuery(Map<String, Object> parameters) {
        List<Long> timeRequested = DataProviderParameterUtils.extractTimeRequested(parameters);
        List<Long> selectedItems = DataProviderParameterUtils.extractSelectedItems(parameters);

        if (timeRequested == null || selectedItems == null) {
            return null;
        }

        Boolean isCumulativeParameter = DataProviderParameterUtils.extractBoolean(parameters, CUMULATIVE_COUNTER_KEY);
        // If the cumulative parameter is not present in the parameters use
        // "false" as default value
        boolean isCumulative = isCumulativeParameter != null && isCumulativeParameter;
        return new SelectedCounterQueryFilter(timeRequested, selectedItems, isCumulative);
    }

    /**
     * Extracts an array of times that will be used for a 2D query. It extracts the
     * times based on the state system bounds, the requested time of the query
     * filter and the current end time
     *
     * @param ss
     *            The state system
     * @param filter
     *            The query filter
     * @param currentEndTime
     *            We want to make sure that current end time is consistent
     *            throughout the query even if the analysis progresses
     * @return A collection of time
     */
    private static Collection<Long> extractRequestedTimes(ITmfStateSystem ss, SelectedCounterQueryFilter filter, long currentEndTime) {
        Collection<Long> times = getTimes(filter, ss.getStartTime(), currentEndTime);

        long[] xValues = filter.getTimesRequested();
        long queryStart = filter.getStart();
        long stateSystemStartTime = ss.getStartTime();

        /* We only need the previous time for differential mode */
        if (!filter.isCumulative() && xValues.length > 1) {
            /*
             * For differential mode, we need to get the time before query start. To do so,
             * we subtract to query start the delta between xValues[1] and query start
             */
            long prevTime = Long.max(stateSystemStartTime, 2 * queryStart - xValues[1]);
            if (prevTime <= currentEndTime) {
                times.add(prevTime);
            }
        }

        return times;
    }

    private static double[] buildYValues(NavigableSet<ITmfStateInterval> countersIntervals, SelectedCounterQueryFilter filter) {

        long[] times = filter.getTimesRequested();
        boolean isCumulative = filter.isCumulative();

        double[] yValues = new double[times.length];
        long prevValue = 0L;
        if (!countersIntervals.isEmpty()) {
            Object value = countersIntervals.first().getValue();
            if (value instanceof Number) {
                prevValue = ((Number) value).longValue();
            }
        }
        int to = 0;

        for (ITmfStateInterval interval : countersIntervals) {
            int from = Arrays.binarySearch(times, interval.getStartTime());
            from = (from >= 0) ? from : -1 - from;
            Number value = (Number) interval.getValue();
            long l = value != null ? value.longValue() : 0l;
            if (isCumulative) {
                /* Fill in all the time stamps that the interval overlaps */
                to = Arrays.binarySearch(times, interval.getEndTime());
                to = (to >= 0) ? to + 1 : -1 - to;
                Arrays.fill(yValues, from, to, l);
            } else {
                yValues[from] = (l - prevValue);
            }
            prevValue = l;
        }

        /* Fill the time stamps after the state system, if any. */
        if (isCumulative) {
            Arrays.fill(yValues, to, yValues.length, prevValue);
        }

        return yValues;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public String getId() {
        return ID;
    }

    /**
     * @since 1.2
     */
    @Override
    protected boolean isCacheable() {
        return true;
    }
}