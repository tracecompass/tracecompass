/*******************************************************************************
 * Copyright (c) 2015, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.timegraph;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.log.TraceCompassLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils;
import org.eclipse.tracecompass.internal.tmf.ui.views.timegraph.TimeEventFilterDialog;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.model.timegraph.IFilterProperty;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ILinkEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.IMarkerEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry.Sampling;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

/**
 * An abstract time graph view where each entry's time event list is populated
 * from a state system. The state system full state is queried in chronological
 * order before creating the time event lists as this is optimal for state
 * system queries.
 *
 * @since 1.1
 */
public abstract class AbstractStateSystemTimeGraphView extends AbstractTimeGraphView {

    private static final @NonNull Comparator<ITmfStateInterval> INTERVALS_COMPARATOR = Comparator.comparing(ITmfStateInterval::getStartTime).thenComparing(ITmfStateInterval::getEndTime);

    private static final @NonNull Comparator<ITimeEvent> TIME_EVENT_COMPARATOR = Comparator.comparing(ITimeEvent::getTime).thenComparing(ITimeEvent::getDuration);

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final long MAX_INTERVALS = 1000000;

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    /** The state system to entry list hash map */
    private final Map<ITmfStateSystem, List<@NonNull TimeGraphEntry>> fSSEntryListMap = new HashMap<>();

    /** The trace to state system multi map */
    private final Multimap<ITmfTrace, ITmfStateSystem> fTraceSSMap = HashMultimap.create();

    private static final @NonNull Logger LOGGER = TraceCompassLog.getLogger(AbstractStateSystemTimeGraphView.class);

    private static final int DEFAULT_BUFFER_SIZE = 3;

    // ------------------------------------------------------------------------
    // Classes
    // ------------------------------------------------------------------------

    /**
     * Handler for state system queries
     */
    public interface IQueryHandler {
        /**
         * Handle a full or partial list of full states. This can be called many
         * times for the same query if the query result is split, in which case
         * the previous full state is null only the first time it is called, and
         * set to the last full state of the previous call from then on.
         *
         * @param fullStates
         *            the list of full states
         * @param prevFullState
         *            the previous full state, or null
         */
        void handle(@NonNull List<List<ITmfStateInterval>> fullStates, @Nullable List<ITmfStateInterval> prevFullState);
    }

    private class ZoomThreadByTime extends ZoomThread {
        private static final int BG_SEARCH_RESOLUTION = 1;
        private final @NonNull Collection<@NonNull TimeGraphEntry> fVisibleEntries;
        private final @NonNull List<ITmfStateSystem> fZoomSSList;
        private boolean fClearZoomedLists;

        public ZoomThreadByTime(@NonNull Collection<@NonNull TimeGraphEntry> entries, @NonNull List<ITmfStateSystem> ssList, long startTime, long endTime, long resolution, boolean restart) {
            super(startTime, endTime, resolution);
            fZoomSSList = ssList;
            fClearZoomedLists = !restart;
            fVisibleEntries = entries;
        }

        @Override
        public void doRun() {
            final List<ILinkEvent> links = new ArrayList<>();
            final List<IMarkerEvent> markers = new ArrayList<>();
            if (fClearZoomedLists) {
                clearZoomedLists();
            }
            for (ITmfStateSystem ss : fZoomSSList) {
                List<TimeGraphEntry> entryList = null;
                synchronized (fSSEntryListMap) {
                    entryList = fSSEntryListMap.get(ss);
                }
                if (entryList != null) {
                    zoomByTime(ss, entryList, links, markers, getZoomStartTime(), getZoomEndTime(), getResolution(), getMonitor());
                }
            }
            if (!getMonitor().isCanceled()) {
                /* Refresh the trace-specific markers when zooming */
                markers.addAll(getTraceMarkerList(getZoomStartTime(), getZoomEndTime(), getResolution(), getMonitor()));
                applyResults(() -> {
                    getTimeGraphViewer().setLinks(links);
                    getTimeGraphViewer().setMarkerCategories(getMarkerCategories());
                    getTimeGraphViewer().setMarkers(markers);
                });
            } else {
                TraceCompassLogUtils.traceInstant(LOGGER, Level.FINE, "TimeGraphView:ZoomThreadCanceled"); //$NON-NLS-1$
            }
        }

        @Override
        public void cancel() {
            super.cancel();
            if (fClearZoomedLists) {
                clearZoomedLists();
            }
        }

        private void zoomByTime(final ITmfStateSystem ss, final List<TimeGraphEntry> entryList, final List<ILinkEvent> links, final List<IMarkerEvent> markers,
                long startTime, long endTime, long resolution, final @NonNull IProgressMonitor monitor) {
            final long start = Math.max(startTime, ss.getStartTime());
            final long end = Math.min(endTime, ss.getCurrentEndTime());
            final boolean fullRange = getZoomStartTime() <= getStartTime() && getZoomEndTime() >= getEndTime();
            if (end < start) {
                return;
            }

            TimeEventFilterDialog timeEventFilterDialog = getTimeEventFilterDialog();
            boolean isFilterActive = timeEventFilterDialog != null && timeEventFilterDialog.isFilterActive();
            getTimeGraphViewer().setTimeEventFilterApplied(isFilterActive);

            boolean hasSavedFilter = timeEventFilterDialog != null && timeEventFilterDialog.hasActiveSavedFilters();
            getTimeGraphViewer().setSavedFilterStatus(hasSavedFilter);

            if (fullRange) {
                redraw();
            }
            @NonNull Map<@NonNull Integer, @NonNull Predicate<@NonNull Multimap<@NonNull String, @NonNull Object>>> predicates = generateRegexPredicate();
            Sampling sampling = new Sampling(getZoomStartTime(), getZoomEndTime(), predicates, getResolution());
            Iterable<@NonNull TimeGraphEntry> incorrectSample = Iterables.filter(fVisibleEntries, entry -> !sampling.equals(entry.getSampling()));
            /* Only keep entries that are a member or child of the ss entry list */
            Iterable<@NonNull TimeGraphEntry> entries = Iterables.filter(incorrectSample, entry -> isMember(entry, entryList));
            // set gaps to null when there is no active filter
            Map<TimeGraphEntry, List<ITimeEvent>> gaps = isFilterActive ? new HashMap<>() : null;
            doZoom(ss, links, markers, resolution, monitor, start, end, sampling, entries, predicates, gaps);

            if (isFilterActive && gaps != null && !gaps.isEmpty()) {
                doBgSearch(ss, BG_SEARCH_RESOLUTION, monitor, gaps, predicates);
            }
        }

        private void doZoom(final ITmfStateSystem ss, final List<ILinkEvent> links, final List<IMarkerEvent> markers, long resolution, final @NonNull IProgressMonitor monitor, final long start, final long end, Sampling sampling,
                Iterable<@NonNull TimeGraphEntry> entries, @NonNull Map<@NonNull Integer, @NonNull Predicate<@NonNull Multimap<@NonNull String, @NonNull Object>>> predicates, Map<TimeGraphEntry, List<ITimeEvent>> gaps) {
            queryFullStates(ss, start, end, resolution, monitor, (@NonNull List<List<ITmfStateInterval>> fullStates, @Nullable List<ITmfStateInterval> prevFullState) -> {
                try (TraceCompassLogUtils.ScopeLog scope = new TraceCompassLogUtils.ScopeLog(LOGGER, Level.FINER, "ZoomThread:GettingStates");) { //$NON-NLS-1$

                    for (TimeGraphEntry entry : entries) {
                        if (!sampling.equals(entry.getSampling())) {
                            zoom(checkNotNull(entry), ss, fullStates, prevFullState, predicates, monitor, gaps);
                        }
                    }
                }
                /* Refresh the arrows when zooming */
                try (TraceCompassLogUtils.ScopeLog linksLogger1 = new TraceCompassLogUtils.ScopeLog(LOGGER, Level.FINER, "ZoomThread:GettingLinks")) { //$NON-NLS-1$
                    links.addAll(getLinkList(ss, fullStates, prevFullState, monitor));
                }
                /* Refresh the view-specific markers when zooming */
                try (TraceCompassLogUtils.ScopeLog linksLogger2 = new TraceCompassLogUtils.ScopeLog(LOGGER, Level.FINER, "ZoomThread:GettingMarkers")) { //$NON-NLS-1$
                    markers.addAll(getViewMarkerList(ss, fullStates, prevFullState, monitor));
                }
                refresh();
            });
            if (!monitor.isCanceled()) {
                applyResults(() -> entries.forEach(entry -> {
                    if (!sampling.equals(entry.getSampling())) {
                        if (!predicates.isEmpty()) {
                            /*
                             * Merge contiguous null events due to split query
                             */
                            List<ITimeEvent> eventList = Lists.newArrayList(entry.getTimeEventsIterator(getZoomStartTime(), getZoomEndTime(), getResolution()));
                            doFilterEventList(entry, eventList, predicates);
                            entry.setZoomedEventList(eventList);
                        }
                        entry.setSampling(sampling);
                    }
                }));
            }
            refresh();
        }

        private void zoom(@NonNull TimeGraphEntry entry, ITmfStateSystem ss, @NonNull List<List<ITmfStateInterval>> fullStates, @Nullable List<ITmfStateInterval> prevFullState,
                @NonNull Map<@NonNull Integer, @NonNull Predicate<@NonNull Multimap<@NonNull String, @NonNull Object>>> predicates, @NonNull IProgressMonitor monitor, Map<TimeGraphEntry, List<ITimeEvent>> gaps) {
            List<ITimeEvent> eventList = getEventList(entry, ss, fullStates, prevFullState, monitor);

            if (gaps != null) {
                // gaps != null means that there is active filter
                getEnventListGaps(entry, eventList, gaps);
            }

            if (eventList != null && !monitor.isCanceled()) {
                doFilterEventList(entry, eventList, predicates);
                applyResults(() -> {
                    for (ITimeEvent event : eventList) {
                            entry.addZoomedEvent(event);
                    }
                });
            }
        }

        private void doBgSearch(ITmfStateSystem ss, int resolution, @NonNull IProgressMonitor monitor, Map<TimeGraphEntry, List<ITimeEvent>> gaps,
                @NonNull Map<@NonNull Integer, @NonNull Predicate<@NonNull Multimap<@NonNull String, @NonNull Object>>> predicates) {
            try (TraceCompassLogUtils.ScopeLog poc = new TraceCompassLogUtils.ScopeLog(LOGGER, Level.FINE, "TimegraphBgSearch")) { //$NON-NLS-1$

                TimeEventFilterDialog timeEventFilterDialog = getTimeEventFilterDialog();
                boolean hasActiveSavedFilters = timeEventFilterDialog.hasActiveSavedFilters();

                //Regroup the gaps by overlapping gaps
                Table<ITimeGraphEntry, @NonNull Pair<Long, Long>, List<ITimeEvent>> gapsTable = regroupOverlappingGaps(gaps);

                //Get the quarks per entry once
                Map<ITimeGraphEntry, Collection<Integer>> quarksPerEntry = new HashMap<>();
                for (ITimeGraphEntry entry : gapsTable.rowKeySet()) {
                    Collection<Integer> quarks = getQuarksForEntry(entry, ss);
                    quarksPerEntry.put(entry, quarks);
                }

                for (@NonNull Entry<Pair<Long, Long>, Map<ITimeGraphEntry, List<ITimeEvent>>> tableEntry : gapsTable.columnMap().entrySet()) {
                    Map<ITimeGraphEntry, List<ITimeEvent>> row = tableEntry.getValue();
                    List<Integer> quarks = new ArrayList<>();

                    //Get the need quarks for this slot
                    for (ITimeGraphEntry entry : row.keySet()) {
                        Collection<Integer> entryQuarks = quarksPerEntry.get(entry);
                        if (entryQuarks != null) {
                            entryQuarks.forEach(quark -> quarks.add(quark));
                        }
                    }

                    queryRangeStates(ss, tableEntry.getKey().getLeft(), tableEntry.getKey().getRight(), quarks, resolution, monitor, (@NonNull List<List<ITmfStateInterval>> fullStates, @Nullable List<ITmfStateInterval> prevFullState) -> {
                        for (Entry<ITimeGraphEntry, List<ITimeEvent>> entryGap : row.entrySet()) {
                            List<ITimeEvent> gapEvents = entryGap.getValue();
                            TimeGraphEntry entry = (TimeGraphEntry) entryGap.getKey();
                            List<ITimeEvent> eventList = getEventList(Objects.requireNonNull(entry), ss, fullStates, prevFullState, monitor);

                            if (eventList != null && !eventList.isEmpty() && !monitor.isCanceled()) {
                                doFilterEventList(entry, eventList, predicates);
                                for (ITimeEvent event : eventList) {
                                    int pos = Collections.binarySearch(gapEvents, event, TimeGraphEntry.WITHIN_COMPARATOR);
                                    // If the event is within a gap
                                    if (pos >= 0) {
                                        ITimeEvent gap = gapEvents.get(pos);
                                        if (!monitor.isCanceled()) {
                                            // if any underlying event is not dimmed,
                                            // then set the related gap event dimmed
                                            // property to false
                                            boolean dimmed = gap.isPropertyActive(IFilterProperty.DIMMED);
                                            if (dimmed && !event.isPropertyActive(IFilterProperty.DIMMED)) {
                                                gap.setProperty(IFilterProperty.DIMMED, false);
                                            }

                                            // if any underlying event is not excluded,
                                            // then set the related gap event exclude
                                            // status property to false and add the gap
                                            // back to the zoom event list
                                            if (hasActiveSavedFilters && !event.isPropertyActive(IFilterProperty.EXCLUDE)) {
                                                gap.setProperty(IFilterProperty.EXCLUDE, false);
                                                applyResults(() -> {
                                                    entry.updateZoomedEvent(gap);
                                                });
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    });
                    refresh();
                }
            }
        }

        private void clearZoomedLists() {
            for (ITmfStateSystem ss : fZoomSSList) {
                List<TimeGraphEntry> entryList = null;
                synchronized (fSSEntryListMap) {
                    entryList = fSSEntryListMap.get(ss);
                }
                if (entryList != null) {
                    for (TimeGraphEntry entry : entryList) {
                        clearZoomedList(entry);
                    }
                }
            }
            fClearZoomedLists = false;
        }

        private void clearZoomedList(TimeGraphEntry entry) {
            entry.setZoomedEventList(null);
            entry.setSampling(null);
            for (TimeGraphEntry child : entry.getChildren()) {
                clearZoomedList(child);
            }
        }

        private boolean isMember(TimeGraphEntry entry, List<TimeGraphEntry> ssEntryList) {
            TimeGraphEntry ssEntry = entry;
            while (ssEntry != null) {
                if (ssEntryList.contains(ssEntry)) {
                    return true;
                }
                ssEntry = ssEntry.getParent();
            }
            return false;
        }
    }

    private static @NonNull Table<ITimeGraphEntry, @NonNull Pair<Long, Long>, List<ITimeEvent>> regroupOverlappingGaps(Map<? extends ITimeGraphEntry, List<ITimeEvent>> gapsMap) {

        Table<ITimeGraphEntry, @NonNull Pair<Long, Long>, List<ITimeEvent>> gapsTable = HashBasedTable.create();
        PriorityQueue<ITimeEvent> queue = new PriorityQueue<>(TIME_EVENT_COMPARATOR);
        gapsMap.values().forEach(events -> queue.addAll(events));
        Pair<Long, Long> previous = null;
        while(!queue.isEmpty()) {
            ITimeEvent event = Objects.requireNonNull(queue.poll());

            ITimeGraphEntry entry = event.getEntry();

            Pair<Long, Long> result = previous != null && event.getTime() < previous.getRight() && event.getTime() + event.getDuration() > previous.getLeft() ? previous : null;
            if (result != null) {
                Pair<Long, Long> merged = mergeInterval(result, event);
                Map<ITimeGraphEntry, List<ITimeEvent>> column = new HashMap<>(gapsTable.column(result));
                if (!result.equals(merged)) {
                    gapsTable.columnKeySet().remove(result);
                    result = merged;
                    for (Entry<ITimeGraphEntry, List<ITimeEvent>> row : column.entrySet()) {
                        gapsTable.put(row.getKey(), result, row.getValue());
                    }
                }
            } else {
                result = Objects.requireNonNull(Pair.of(event.getTime(), event.getTime() + event.getDuration()));
            }

            previous = result;
            List<ITimeEvent> events = gapsTable.get(entry, result);
            if (events == null) {
                events = new ArrayList<>();
                gapsTable.put(entry, result, events);
            }
            events.add(event);

        }

        return gapsTable;
    }

    private static @NonNull Pair<Long, Long> mergeInterval(Pair<Long, Long> source, ITimeEvent event) {
        long left = Math.min(source.getLeft(), event.getTime());
        long right = Math.max(source.getRight(), event.getTime() + event.getDuration());
        return Objects.requireNonNull(Pair.of(left, right));
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs a time graph view that contains either a time graph viewer or
     * a time graph combo.
     *
     * By default, the view uses a time graph viewer. To use a time graph combo,
     * the subclass constructor must call {@link #setTreeColumns(String[])} and
     * {@link #setTreeLabelProvider(TreeLabelProvider)}.
     *
     * @param id
     *            The id of the view
     * @param pres
     *            The presentation provider
     */
    public AbstractStateSystemTimeGraphView(String id, TimeGraphPresentationProvider pres) {
        super(id, pres);
    }

    // ------------------------------------------------------------------------
    // Internal
    // ------------------------------------------------------------------------

    /**
     *
     * Gather the events list's gaps and add them to the map of gaps by entry.
     * The default implementation do nothing and views that need to do
     * background search need to provide their own implementation.
     *
     * @param entry
     *            The entry to find gaps
     * @param eventList
     *            The event list
     * @param gaps
     *            The map of gaps by entry
     * @since 4.3
     */
    protected void getEnventListGaps(@NonNull TimeGraphEntry entry, List<ITimeEvent> eventList, Map<TimeGraphEntry, List<ITimeEvent>> gaps) {
        // Do nothing here. Must be implemented by subclasses that need this behavior
    }

    /**
     * Get the list of attributes in given state system needed to build this
     * specific entry
     *
     * @param entry
     *            The entry to build
     * @param ss
     *            The related state system
     * @return The list of attributes needed to build the entry
     * @since 4.3
     */
    protected @NonNull Collection<@NonNull Integer> getQuarksForEntry(@NonNull ITimeGraphEntry entry, ITmfStateSystem ss) {
        return IntStream.range(0, ss.getNbAttributes()).boxed().collect(Collectors.toList());
    }

    /**
     * Gets the entry list for a state system. These entries, and their
     * recursive children, will have their event lists computed using the
     * provided state system.
     *
     * @param ss
     *            the state system
     *
     * @return the entry list
     */
    protected @Nullable List<@NonNull TimeGraphEntry> getEntryList(ITmfStateSystem ss) {
        synchronized (fSSEntryListMap) {
            return fSSEntryListMap.get(ss);
        }
    }

    /**
     * Sets the entry list for a state system. These entries, and their
     * recursive children, will have their event lists computed using the
     * provided state system. The root of each entry (or the entry itself if it
     * is a root) will be added to the parent trace's entry list.
     *
     * @param trace
     *            the parent trace
     * @param ss
     *            the state system
     * @param list
     *            the list of time graph entries
     */
    protected void putEntryList(ITmfTrace trace, ITmfStateSystem ss, List<@NonNull TimeGraphEntry> list) {
        super.addToEntryList(trace, getRootEntries(list));
        synchronized (fSSEntryListMap) {
            fSSEntryListMap.put(ss, new CopyOnWriteArrayList<>(list));
            fTraceSSMap.put(trace, ss);
        }
    }

    /**
     * Adds a list of entries to the entry list for a state system. These
     * entries, and their recursive children, will have their event lists
     * computed using the provided state system. The root of each entry (or the
     * entry itself if it is a root) will be added to the parent trace's entry
     * list.
     *
     * @param trace
     *            the parent trace
     * @param ss
     *            the state system
     * @param list
     *            the list of time graph entries to add
     */
    protected void addToEntryList(ITmfTrace trace, ITmfStateSystem ss, List<@NonNull TimeGraphEntry> list) {
        super.addToEntryList(trace, getRootEntries(list));
        synchronized (fSSEntryListMap) {
            List<@NonNull TimeGraphEntry> entryList = fSSEntryListMap.get(ss);
            if (entryList == null) {
                fSSEntryListMap.put(ss, new CopyOnWriteArrayList<>(list));
            } else {
                entryList.addAll(list);
            }
            fTraceSSMap.put(trace, ss);
        }
    }

    /**
     * Removes a list of entries from the entry list for a state system. These
     * entries, and their recursive children, will no longer have their event
     * lists computed using the provided state system. Each entry that is itself
     * a root will be removed from the parent trace's entry list.
     *
     * @param trace
     *            the parent trace
     * @param ss
     *            the state system
     * @param list
     *            the list of time graph entries to remove
     */
    protected void removeFromEntryList(ITmfTrace trace, ITmfStateSystem ss, List<TimeGraphEntry> list) {
        super.removeFromEntryList(trace, list);
        synchronized (fSSEntryListMap) {
            List<TimeGraphEntry> entryList = fSSEntryListMap.get(ss);
            if (entryList != null) {
                entryList.removeAll(list);
                if (entryList.isEmpty()) {
                    fTraceSSMap.remove(trace, ss);
                }
            }
        }
    }

    private static List<@NonNull TimeGraphEntry> getRootEntries(List<@NonNull TimeGraphEntry> list) {
        Set<@NonNull TimeGraphEntry> roots = new LinkedHashSet<>();
        for (@NonNull TimeGraphEntry entry : list) {
            TimeGraphEntry root = entry;
            while (root.getParent() != null) {
                root = root.getParent();
            }
            roots.add(root);
        }
        return new ArrayList<>(roots);
    }

    @Override
    protected @Nullable ZoomThread createZoomThread(long startTime, long endTime, long resolution, boolean restart) {
        List<ITmfStateSystem> ssList = null;
        synchronized (fSSEntryListMap) {
            ssList = new ArrayList<>(fTraceSSMap.get(getTrace()));
        }
        if (ssList.isEmpty()) {
            return null;
        }
        return new ZoomThreadByTime(getVisibleItems(DEFAULT_BUFFER_SIZE), ssList, startTime, endTime, resolution, restart);
    }

    /**
     * Query the state system full state for the given time range.
     *
     * @param ss
     *            The state system
     * @param start
     *            The start time
     * @param end
     *            The end time
     * @param resolution
     *            The resolution
     * @param monitor
     *            The progress monitor
     * @param handler
     *            The query handler
     */
    protected void queryFullStates(ITmfStateSystem ss, long start, long end, long resolution,
            @NonNull IProgressMonitor monitor, @NonNull IQueryHandler handler) {
        if (end < start) {
            /* We have an empty trace, the state system will be empty: nothing to do here. */
            return;
        }
        List<List<ITmfStateInterval>> fullStates = new ArrayList<>();
        List<ITmfStateInterval> prevFullState = null;
        try {
            long time = start;
            while (true) {
                if (monitor.isCanceled()) {
                    break;
                }
                List<ITmfStateInterval> fullState = ss.queryFullState(time);
                fullStates.add(fullState);
                if (fullStates.size() * fullState.size() > MAX_INTERVALS) {
                    handler.handle(fullStates, prevFullState);
                    prevFullState = fullStates.get(fullStates.size() - 1);
                    fullStates.clear();
                }
                if (time >= end) {
                    break;
                }
                time = Math.min(end, time + resolution);
            }
            if (!fullStates.isEmpty()) {
                handler.handle(fullStates, prevFullState);
            }
        } catch (StateSystemDisposedException e) {
            /* Ignored */
        }
    }

    /**
     * Query the state system for a list of specific state and for the given
     * time range.
     *
     * @param ss
     *            The state system
     * @param start
     *            The start time
     * @param end
     *            The end time
     * @param quarks
     *            The list of quarks/state to query
     * @param resolution
     *            The resolution
     * @param monitor
     *            The progress monitor
     * @param handler
     *            The query handler
     *
     * @since 4.3
     */
    protected void queryRangeStates(ITmfStateSystem ss, long start, long end, @NonNull Collection<@NonNull Integer> quarks, long resolution,
            @NonNull IProgressMonitor monitor, @NonNull IQueryHandler handler) {
        if (end < start) {
            /*
             * We have an empty trace, the state system will be empty: nothing
             * to do here.
             */
            return;
        }

        try {
            Iterable<@NonNull ITmfStateInterval> intervals = ss.query2D(quarks, start, end);
            Map<Integer, Integer> ptr = new HashMap<>();
            Map<Integer, List<ITmfStateInterval>> itvs = new HashMap<>();
            intervals.forEach(itv -> {
                int key = itv.getAttribute();
                List<ITmfStateInterval> set = itvs.get(key);
                if (set == null) {
                    set = new ArrayList<>();
                    itvs.put(key, set);
                    ptr.put(key, 0);
                }
                set.add(itv);
            });

            // Sort the intervals
            for (Entry<Integer, List<ITmfStateInterval>> entry : itvs.entrySet()) {
                entry.getValue().sort(INTERVALS_COMPARATOR);
            }

            List<List<ITmfStateInterval>> fullStates = new ArrayList<>();
            List<ITmfStateInterval> prevFullState = null;

            long time = start;
            while (true) {
                if (monitor.isCanceled()) {
                    break;
                }
                int nbAttributes = ss.getNbAttributes();
                List<ITmfStateInterval> fullState = getFullStateForTime(time, itvs, ptr, nbAttributes);
                fullStates.add(fullState);
                if (fullStates.size() * fullState.size() > MAX_INTERVALS) {
                    handler.handle(fullStates, prevFullState);
                    prevFullState = fullStates.get(fullStates.size() - 1);
                    fullStates.clear();
                }
                if (time >= end) {
                    break;
                }
                time = Math.min(end, time + resolution);
            }
            if (!fullStates.isEmpty()) {
                handler.handle(fullStates, prevFullState);
            }
        } catch (StateSystemDisposedException e1) {
            // Do nothing
        }
    }

    private static List<ITmfStateInterval> getFullStateForTime(long time, @NonNull Map<@NonNull Integer, @NonNull List<@NonNull ITmfStateInterval>> itvs, @NonNull Map<@NonNull Integer, @NonNull Integer> ptr, int nbAttributes) {
        List<ITmfStateInterval> fullState = new ArrayList<>();
        for (int key = 0; key < nbAttributes; key++) {
            fullState.add(null);
        }

        for (Entry<Integer, List<ITmfStateInterval>> entry : itvs.entrySet()) {
            List<ITmfStateInterval> intervals = entry.getValue();

            int key = Objects.requireNonNull(entry.getKey());
            int index = Objects.requireNonNull(ptr.get(key));

            ITmfStateInterval interval = Objects.requireNonNull(intervals.get(index));
            while(!isBounded(time, interval) && ++index < intervals.size()) {
                interval = Objects.requireNonNull(intervals.get(index));
                ptr.put(key, index);
            }

            if (index < intervals.size()) {
                fullState.set(key, interval);
            }
        }
        return fullState;
    }

    private static boolean isBounded(long time, ITmfStateInterval interval) {
        return interval.getStartTime() <= time && interval.getEndTime() >= time;
    }

    /**
     * Gets the list of events for an entry for a given list of full states.
     * <p>
     * Called from the ZoomThread for every entry to update the zoomed event
     * list. Can be an empty implementation if the view does not support zoomed
     * event lists. Can also be used to compute the full event list.
     *
     * @param tgentry
     *            The time graph entry
     * @param ss
     *            The state system
     * @param fullStates
     *            A list of full states
     * @param prevFullState
     *            The previous full state, or null
     * @param monitor
     *            A progress monitor
     * @return The list of time graph events
     */
    protected abstract @Nullable List<ITimeEvent> getEventList(@NonNull TimeGraphEntry tgentry, ITmfStateSystem ss,
            @NonNull List<List<ITmfStateInterval>> fullStates, @Nullable List<ITmfStateInterval> prevFullState, @NonNull IProgressMonitor monitor);

    /**
     * Gets the list of links (displayed as arrows) for a given list of full
     * states. The default implementation returns an empty list.
     *
     * @param ss
     *            The state system
     * @param fullStates
     *            A list of full states
     * @param prevFullState
     *            The previous full state, or null
     * @param monitor
     *            A progress monitor
     * @return The list of link events
     * @since 2.0
     */
    protected @NonNull List<ILinkEvent> getLinkList(ITmfStateSystem ss,
            @NonNull List<List<ITmfStateInterval>> fullStates, @Nullable List<ITmfStateInterval> prevFullState, @NonNull IProgressMonitor monitor) {
        return new ArrayList<>();
    }

    /**
     * Gets the list of markers for a given list of full states. The default
     * implementation returns an empty list.
     *
     * @param ss
     *            The state system
     * @param fullStates
     *            A list of full states
     * @param prevFullState
     *            The previous full state, or null
     * @param monitor
     *            A progress monitor
     * @return The list of marker events
     * @since 2.0
     */
    protected @NonNull List<IMarkerEvent> getViewMarkerList(ITmfStateSystem ss,
            @NonNull List<List<ITmfStateInterval>> fullStates, @Nullable List<ITmfStateInterval> prevFullState, @NonNull IProgressMonitor monitor) {
        return new ArrayList<>();
    }

    @Override
    protected void resetView(ITmfTrace viewTrace) {
        // Don't remove super call
        super.resetView(viewTrace);
        synchronized (fSSEntryListMap) {
            for (ITmfStateSystem ss : fTraceSSMap.removeAll(viewTrace)) {
                fSSEntryListMap.remove(ss);
            }
        }
    }
}
