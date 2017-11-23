/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.analysis.counters.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.analysis.counters.core.Messages;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.AbstractStateSystemAnalysisDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.TmfCommonXAxisResponseFactory;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.SelectedCounterQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.TmfTreeDataModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfCommonXAxisModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfTreeXYDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.IYModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.internal.tmf.core.model.YModel;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;

import com.google.common.collect.BiMap;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.TreeMultimap;
import com.google.common.primitives.Longs;

/**
 * This data provider will return a XY model (model is wrapped in a response)
 * based on a query filter. The model is used afterwards by any viewer to draw
 * charts. Model returned is for Counters views
 *
 * @author Mikael Ferland
 * @author Yonni Chen
 * @since 1.1
 */
@SuppressWarnings("restriction")
public class CounterDataProvider extends AbstractStateSystemAnalysisDataProvider implements ITmfTreeXYDataProvider<TmfTreeDataModel> {

    /**
     * This data provider's extension point ID
     */
    public static final String ID = "org.eclipse.tracecompass.analysis.counters.core.CounterDataProvider"; //$NON-NLS-1$

    /**
     * Chart's title
     */
    private static final String TITLE = Objects.requireNonNull(Messages.CounterDataProvider_ChartTitle);

    private static final AtomicLong COUNTER_ENTRY_ID = new AtomicLong();

    private final CounterAnalysis fModule;
    private final BiMap<Long, Integer> fIdToQuark = HashBiMap.create();
    private final Map<Integer, TmfTreeDataModel> fQuarkToEntry = new LinkedHashMap<>();
    private @Nullable TmfModelResponse<List<TmfTreeDataModel>> fCached = null;

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
        super(trace);
        fModule = analysis;
    }

    @Override
    public TmfModelResponse<List<TmfTreeDataModel>> fetchTree(TimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        if (fCached != null) {
            return fCached;
        }

        fModule.waitForInitialization();
        ITmfStateSystem ss = fModule.getStateSystem();
        if (ss == null) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, CommonStatusMessage.STATE_SYSTEM_FAILED);
        }
        boolean complete = ss.waitUntilBuilt(0);

        TmfTreeDataModel trace = fQuarkToEntry.get(-1);
        if (trace == null) {
            trace = new TmfTreeDataModel(COUNTER_ENTRY_ID.getAndIncrement(), -1, getTrace().getName());
            fQuarkToEntry.put(-1, trace);
        }

        addTreeViewerBranch(ss, trace, CounterAnalysis.GROUPED_COUNTER_ASPECTS_ATTRIB);
        addTreeViewerBranch(ss, trace, CounterAnalysis.UNGROUPED_COUNTER_ASPECTS_ATTRIB);

        ArrayList<TmfTreeDataModel> model = new ArrayList<>(fQuarkToEntry.values());
        if (complete) {
            TmfModelResponse<List<TmfTreeDataModel>> response = new TmfModelResponse<>(model, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
            fCached = response;
        }
        return new TmfModelResponse<>(model, ITmfResponse.Status.RUNNING, CommonStatusMessage.RUNNING);
    }

    private void addTreeViewerBranch(ITmfStateSystem stateSystem, TmfTreeDataModel rootBranch, String branchName) {
        int quark = stateSystem.optQuarkAbsolute(branchName);
        if (quark != ITmfStateSystem.INVALID_ATTRIBUTE && !stateSystem.getSubAttributes(quark, false).isEmpty()) {
            TmfTreeDataModel branch = fQuarkToEntry.get(quark);
            if (branch == null) {
                branch = new TmfTreeDataModel(COUNTER_ENTRY_ID.getAndIncrement(), rootBranch.getId(), branchName);
                fQuarkToEntry.put(quark, branch);
            }
            addTreeViewerEntries(stateSystem, branch, quark);
        }
    }

    /**
     * Recursively add all child entries of a parent branch from the state system.
     */
    private void addTreeViewerEntries(ITmfStateSystem ss, TmfTreeDataModel parentBranch, int quark) {
        for (int childQuark : ss.getSubAttributes(quark, false)) {
            TmfTreeDataModel childBranch = fQuarkToEntry.get(childQuark);
            if (childBranch == null) {
                long id = COUNTER_ENTRY_ID.getAndIncrement();
                if (ss.getSubAttributes(childQuark, false).isEmpty()) {
                    fIdToQuark.put(id, childQuark);
                }
                childBranch = new TmfTreeDataModel(id, parentBranch.getId(), ss.getAttributeName(childQuark));
                fQuarkToEntry.put(childQuark, childBranch);
            }

            addTreeViewerEntries(ss, childBranch, childQuark);
        }
    }

    @Override
    public TmfModelResponse<ITmfCommonXAxisModel> fetchXY(TimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        if (filter instanceof SelectedCounterQueryFilter) {
            SelectedCounterQueryFilter selection = (SelectedCounterQueryFilter) filter;
            return internalFetch(getSelectedQuarks(selection), selection, monitor);
        }
        return TmfCommonXAxisResponseFactory.create(TITLE, filter.getTimesRequested(), Collections.emptyMap(), true);
    }

    private Collection<Integer> getSelectedQuarks(SelectedCounterQueryFilter filter) {
        Set<Integer> quarks = new HashSet<>();
        for (Long selectedItem : filter.getSelectedItems()) {
            Integer quark = fIdToQuark.get(selectedItem);
            if (quark != null) {
                quarks.add(quark);
            }
        }
        return quarks;
    }

    private TmfModelResponse<ITmfCommonXAxisModel> internalFetch(Collection<Integer> selectedQuarks, SelectedCounterQueryFilter filter, @Nullable IProgressMonitor monitor) {

        ITmfStateSystem ss = Objects.requireNonNull(fModule.getStateSystem());
        long stateSystemEndTime = ss.getCurrentEndTime();
        Collection<Long> times = extractRequestedTimes(ss, filter, stateSystemEndTime);
        TreeMultimap<Integer, ITmfStateInterval> countersIntervals = TreeMultimap.create(Comparator.naturalOrder(), Comparator.comparingLong(ITmfStateInterval::getStartTime));

        try {
            Iterable<@NonNull ITmfStateInterval> query2d = ss.query2D(selectedQuarks, times);
            for (ITmfStateInterval interval : query2d) {
                if (monitor != null && monitor.isCanceled()) {
                    return TmfCommonXAxisResponseFactory.createCancelledResponse(CommonStatusMessage.TASK_CANCELLED);
                }
                countersIntervals.put(interval.getAttribute(), interval);
            }

            ImmutableMap.Builder<String, IYModel> ySeries = ImmutableMap.builder();
            for (Integer quark : selectedQuarks) {
                if (monitor != null && monitor.isCanceled()) {
                    return TmfCommonXAxisResponseFactory.createCancelledResponse(CommonStatusMessage.TASK_CANCELLED);
                }
                double[] yValues = buildYValues(countersIntervals.get(quark), filter);
                String seriesName = getTrace().getName() + '/' + ss.getFullAttributePath(quark);
                ySeries.put(seriesName, new YModel(seriesName, yValues));
            }

            boolean complete = ss.waitUntilBuilt(0) || filter.getEnd() <= stateSystemEndTime;
            return TmfCommonXAxisResponseFactory.create(TITLE, filter.getTimesRequested(), ySeries.build(), complete);
        } catch (IndexOutOfBoundsException | TimeRangeException e) {
            return TmfCommonXAxisResponseFactory.createFailedResponse(e.getMessage());
        } catch (StateSystemDisposedException e) {
            /*
             * Ignore exception (can take place when closing the trace during update), and
             * continue with the other state system(s)
             */
            return TmfCommonXAxisResponseFactory.create(TITLE, filter.getTimesRequested(), Collections.emptyMap(), true);
        }
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
        Collection<Long> times = new ArrayList<>();

        long[] xValues = filter.getTimesRequested();
        long queryStart = filter.getStart();
        long stateSystemStartTime = ss.getStartTime();

        /* We only need the previous time for differential mode */
        if (!filter.isCumulative()) {
            /*
             * For differential mode, we need to get the time before query start. To do so,
             * we subtract to query start the delta between xValues[1] and query start
             */
            long prevTime = Long.max(stateSystemStartTime, 2 * queryStart - xValues[1]);
            if (prevTime <= currentEndTime) {
                times.add(prevTime);
            }
        }
        times.addAll(Collections2.filter(Longs.asList(xValues), t -> (stateSystemStartTime <= t && t <= currentEndTime)));

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

    /**
     * Get the {@link CounterDataProvider}'s TITLE
     *
     * @return the provider's title
     */
    public static String getTitle() {
        return TITLE;
    }

    @Override
    public String getId() {
        return ID;
    }
}