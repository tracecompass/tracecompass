/*******************************************************************************
 * Copyright (c) 2015, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.timegraph;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.log.TraceCompassLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ILinkEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.IMarkerEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * An abstract time graph view where each entry's time event list is populated
 * from a state system. The state system full state is queried in chronological
 * order before creating the time event lists as this is optimal for state
 * system queries.
 *
 * @since 1.1
 */
public abstract class AbstractStateSystemTimeGraphView extends AbstractTimeGraphView {

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
        private final @NonNull List<ITmfStateSystem> fZoomSSList;
        private boolean fClearZoomedLists;

        public ZoomThreadByTime(@NonNull List<ITmfStateSystem> ssList, long startTime, long endTime, long resolution, boolean restart) {
            super(startTime, endTime, resolution);
            fZoomSSList = ssList;
            fClearZoomedLists = !restart;
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
            if (fullRange) {
                redraw();
            }
            queryFullStates(ss, start, end, resolution, monitor, new IQueryHandler() {
                @Override
                public void handle(@NonNull List<List<ITmfStateInterval>> fullStates, @Nullable List<ITmfStateInterval> prevFullState) {
                    try (TraceCompassLogUtils.ScopeLog scope = new TraceCompassLogUtils.ScopeLog(LOGGER, Level.FINER, "ZoomThread:GettingStates");) { //$NON-NLS-1$
                        if (!fullRange) {
                            for (TimeGraphEntry entry : entryList) {
                                zoom(checkNotNull(entry), ss, fullStates, prevFullState, monitor);
                            }
                        }
                    }
                    /* Refresh the arrows when zooming */
                    try (TraceCompassLogUtils.ScopeLog linksLogger = new TraceCompassLogUtils.ScopeLog(LOGGER, Level.FINER, "ZoomThread:GettingLinks")) { //$NON-NLS-1$
                        links.addAll(getLinkList(ss, fullStates, prevFullState, monitor));
                    }
                    /* Refresh the view-specific markers when zooming */
                    try (TraceCompassLogUtils.ScopeLog linksLogger = new TraceCompassLogUtils.ScopeLog(LOGGER, Level.FINER, "ZoomThread:GettingMarkers")) { //$NON-NLS-1$
                        markers.addAll(getViewMarkerList(ss, fullStates, prevFullState, monitor));
                    }
                }
            });
            refresh();
        }

        private void zoom(@NonNull TimeGraphEntry entry, ITmfStateSystem ss, @NonNull List<List<ITmfStateInterval>> fullStates, @Nullable List<ITmfStateInterval> prevFullState, @NonNull IProgressMonitor monitor) {
            List<ITimeEvent> eventList = getEventList(entry, ss, fullStates, prevFullState, monitor);
            if (eventList != null) {
                applyResults(() -> {
                    for (ITimeEvent event : eventList) {
                        if (monitor.isCanceled()) {
                            return;
                        }
                        entry.addZoomedEvent(event);
                    }
                });
            }
            for (TimeGraphEntry child : entry.getChildren()) {
                if (monitor.isCanceled()) {
                    TraceCompassLogUtils.traceInstant(LOGGER, Level.FINE, "TimeGraphView:ZoomThreadCanceled"); //$NON-NLS-1$
                    return;
                }
                zoom(child, ss, fullStates, prevFullState, monitor);
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
            for (TimeGraphEntry child : entry.getChildren()) {
                clearZoomedList(child);
            }
        }
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
     * Gets the entry list for a state system
     *
     * @param ss
     *            the state system
     *
     * @return the entry list map
     */
    protected List<@NonNull TimeGraphEntry> getEntryList(ITmfStateSystem ss) {
        synchronized (fSSEntryListMap) {
            return fSSEntryListMap.get(ss);
        }
    }

    /**
     * Adds a trace entry list to the entry list map
     *
     * @param trace
     *            the trace
     * @param ss
     *            the state system
     * @param list
     *            the list of time graph entries
     */
    protected void putEntryList(ITmfTrace trace, ITmfStateSystem ss, List<@NonNull TimeGraphEntry> list) {
        super.putEntryList(trace, list);
        synchronized (fSSEntryListMap) {
            fSSEntryListMap.put(ss, new CopyOnWriteArrayList<>(list));
            fTraceSSMap.put(trace, ss);
        }
    }

    /**
     * Adds a list of entries to a trace's entry list
     *
     * @param trace
     *            the trace
     * @param ss
     *            the state system
     * @param list
     *            the list of time graph entries to add
     */
    protected void addToEntryList(ITmfTrace trace, ITmfStateSystem ss, List<@NonNull TimeGraphEntry> list) {
        super.addToEntryList(trace, list);
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
     * Removes a list of entries from a trace's entry list
     *
     * @param trace
     *            the trace
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

    @Override
    protected @Nullable ZoomThread createZoomThread(long startTime, long endTime, long resolution, boolean restart) {
        List<ITmfStateSystem> ssList = null;
        synchronized (fSSEntryListMap) {
            ssList = new ArrayList<>(fTraceSSMap.get(getTrace()));
        }
        if (ssList.isEmpty()) {
            return null;
        }
        return new ZoomThreadByTime(ssList, startTime, endTime, resolution, restart);
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
            if (fullStates.size() > 0) {
                handler.handle(fullStates, prevFullState);
            }
        } catch (StateSystemDisposedException e) {
            /* Ignored */
        }
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

    /**
     * @deprecated The subclass should call
     *             {@link #getEntryList(ITmfStateSystem)} instead.
     */
    @Deprecated
    @Override
    protected final List<TimeGraphEntry> getEntryList(ITmfTrace trace) {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated The subclass should call
     *             {@link #addToEntryList(ITmfTrace, ITmfStateSystem, List)}
     *             instead.
     */
    @Deprecated
    @Override
    protected final void addToEntryList(ITmfTrace trace, List<TimeGraphEntry> list) {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated The subclass should call
     *             {@link #putEntryList(ITmfTrace, ITmfStateSystem, List)}
     *             instead.
     */
    @Deprecated
    @Override
    protected final void putEntryList(ITmfTrace trace, List<TimeGraphEntry> list) {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated The subclass should call
     *             {@link #removeFromEntryList(ITmfTrace, ITmfStateSystem, List)}
     *             instead.
     */
    @Deprecated
    @Override
    protected final void removeFromEntryList(ITmfTrace trace, List<TimeGraphEntry> list) {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated The subclass should implement
     *             {@link #getEventList(TimeGraphEntry, ITmfStateSystem, List, List, IProgressMonitor)}
     *             instead.
     */
    @Deprecated
    @Override
    protected final List<ITimeEvent> getEventList(TimeGraphEntry entry, long startTime, long endTime, long resolution, IProgressMonitor monitor) {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated The subclass should implement
     *             {@link #getLinkList(ITmfStateSystem, List, List, IProgressMonitor)}
     *             instead.
     */
    @Deprecated
    @Override
    protected final List<ILinkEvent> getLinkList(long startTime, long endTime, long resolution, IProgressMonitor monitor) {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated The subclass should implement
     *             {@link #getViewMarkerList(ITmfStateSystem, List, List, IProgressMonitor)}
     *             instead.
     */
    @Deprecated
    @Override
    protected final List<IMarkerEvent> getViewMarkerList(long startTime, long endTime, long resolution, IProgressMonitor monitor) {
        throw new UnsupportedOperationException();
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
