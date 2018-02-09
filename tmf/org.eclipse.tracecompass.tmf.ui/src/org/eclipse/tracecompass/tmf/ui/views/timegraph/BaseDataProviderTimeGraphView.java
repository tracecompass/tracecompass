/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.timegraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph.ITimeGraphDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph.ITimeGraphRowModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph.ITimeGraphState;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph.TimeGraphEntryModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph.TimeGraphState;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderManager;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NamedTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NullTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry.Sampling;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * {@link AbstractTimeGraphView} for views with data providers.
 *
 * @author Loic Prieur-Drevon
 * @since 3.3
 */
public class BaseDataProviderTimeGraphView extends AbstractTimeGraphView {

    /**
     * Timeout between updates in the build thread in ms
     */
    protected static final long BUILD_UPDATE_TIMEOUT = 500;

    private final String fProviderId;

    /**
     * Constructs a time graph view that contains a time graph viewer.
     *
     * By default, the view uses a single default column in the name space that
     * shows the time graph entry name. To use multiple columns and/or customized
     * label texts, the subclass constructor must call
     * {@link #setTreeColumns(String[])} and/or
     * {@link #setTreeLabelProvider(TreeLabelProvider)}.
     *
     * @param id
     *            The id of the view
     * @param pres
     *            The presentation provider
     * @param providerId
     *            the ID for the {@link ITimeGraphDataProvider} to use to populate
     *            this view
     */
    public BaseDataProviderTimeGraphView(String id, TimeGraphPresentationProvider pres, String providerId) {
        super(id, pres);
        fProviderId = providerId;
    }

    @Override
    protected void buildEntryList(@NonNull ITmfTrace trace, @NonNull ITmfTrace parentTrace, @NonNull IProgressMonitor monitor) {
        ITimeGraphDataProvider<@NonNull TimeGraphEntryModel> dataProvider = DataProviderManager
                .getInstance().getDataProvider(trace, fProviderId, ITimeGraphDataProvider.class);
        if (dataProvider == null) {
            return;
        }
        boolean complete = false;
        Map<TimeGraphEntryModel, TimeGraphEntry> modelToEntryMap = new HashMap<>();
        Map<Long, TimeGraphEntry> parentLookupMap = new HashMap<>();
        while (!complete && !monitor.isCanceled()) {
            TmfModelResponse<List<TimeGraphEntryModel>> response = dataProvider.fetchTree(new TimeQueryFilter(0, Long.MAX_VALUE, 2), monitor);
            if (response.getStatus() == ITmfResponse.Status.FAILED) {
                Activator.getDefault().logError(getClass().getSimpleName() + " Data Provider failed: " + response.getStatusMessage()); //$NON-NLS-1$
                return;
            } else if (response.getStatus() == ITmfResponse.Status.CANCELLED) {
                return;
            }
            complete = response.getStatus() == ITmfResponse.Status.COMPLETED;

            List<TimeGraphEntryModel> model = response.getModel();
            if (model != null) {
                for (TimeGraphEntryModel entry : model) {
                    TimeGraphEntry uiEntry = modelToEntryMap.get(entry);
                    if (entry.getParentId() != -1) {
                        if (uiEntry == null) {
                            uiEntry = new TimeGraphEntry(entry);
                            modelToEntryMap.put(entry, uiEntry);
                            parentLookupMap.put(entry.getId(), uiEntry);

                            TimeGraphEntry parent = parentLookupMap.get(entry.getParentId());
                            if (parent != null) {
                                parent.addChild(uiEntry);
                            }
                        } else {
                            uiEntry.updateModel(entry);
                        }
                    } else {
                        setStartTime(Long.min(getStartTime(), entry.getStartTime()));
                        setEndTime(Long.max(getEndTime(), entry.getEndTime() + 1));

                        if (uiEntry != null) {
                            uiEntry.updateModel(entry);
                        } else {
                            uiEntry = new TraceEntry(entry, trace, dataProvider);
                            modelToEntryMap.put(entry, uiEntry);
                            parentLookupMap.put(entry.getId(), uiEntry);
                            addToEntryList(parentTrace, Collections.singletonList(uiEntry));
                        }
                    }
                }
                long start = getStartTime();
                long end = getEndTime();
                final long resolution = Long.max(1, (end - start) / getDisplayWidth());
                zoomEntries(modelToEntryMap.values(), start, end, resolution, monitor);
            }

            if (monitor.isCanceled()) {
                return;
            }

            if (parentTrace.equals(getTrace())) {
                synchingToTime(getTimeGraphViewer().getSelectionBegin());
                refresh();
            }
            monitor.worked(1);

            if (!complete && !monitor.isCanceled()) {
                try {
                    Thread.sleep(BUILD_UPDATE_TIMEOUT);
                } catch (InterruptedException e) {
                    Activator.getDefault().logError("Failed to wait for data provider", e); //$NON-NLS-1$
                }
            }
        }
    }

    /**
     * Class to encapsulate a {@link TimeGraphEntryModel} for the trace level and
     * the relevant data provider
     *
     * @author Loic Prieur-Drevon
     * @since 3.3
     */
    protected static class TraceEntry extends TimeGraphEntry {
        private final @NonNull ITmfTrace fTrace;
        private final @NonNull ITimeGraphDataProvider<? extends TimeGraphEntryModel> fProvider;

        /**
         * Constructor
         *
         * @param model
         *            trace level model
         * @param trace
         *            The trace corresponding to this trace entry.
         * @param provider
         *            reference to the provider for this trace and view
         */
        public TraceEntry(TimeGraphEntryModel model, @NonNull ITmfTrace trace,
                @NonNull ITimeGraphDataProvider<? extends TimeGraphEntryModel> provider) {
            super(model);
            fTrace = trace;
            fProvider = provider;
        }

        @Override
        public boolean hasTimeEvents() {
            return false;
        }

        /**
         * Getter for this trace entry's trace
         *
         * @return the trace for this trace entry and its children
         */
        public @NonNull ITmfTrace getTrace() {
            return fTrace;
        }

        /**
         * Getter for the data provider for this {@link TraceEntry}
         *
         * @return this entry's {@link ITimeGraphDataProvider}
         */
        public @NonNull ITimeGraphDataProvider<? extends TimeGraphEntryModel> getProvider() {
            return fProvider;
        }
    }

    /**
     * Get the {@link ITmfTrace} from a {@link TimeGraphEntry}'s parent.
     *
     * @param entry
     *            queried {@link TimeGraphEntry}.
     * @return the {@link ITmfTrace}
     * @since 3.3
     */
    public static @NonNull ITmfTrace getTrace(TimeGraphEntry entry) {
        return getTraceEntry(entry).getTrace();
    }

    /**
     * Get the {@link ITimeGraphDataProvider} from a {@link TimeGraphEntry}'s
     * parent.
     *
     * @param entry
     *            queried {@link TimeGraphEntry}.
     * @return the {@link ITimeGraphDataProvider}
     * @since 3.3
     */
    public static ITimeGraphDataProvider<? extends TimeGraphEntryModel> getProvider(TimeGraphEntry entry) {
        return getTraceEntry(entry).getProvider();
    }

    private static TraceEntry getTraceEntry(TimeGraphEntry entry) {
        ITimeGraphEntry parent = entry;
        while (parent != null) {
            if (parent instanceof TraceEntry) {
                return ((TraceEntry) parent);
            }
            parent = parent.getParent();
        }
        throw new IllegalStateException(entry + " should have a TraceEntry parent"); //$NON-NLS-1$
    }

    @Override
    protected void zoomEntries(@NonNull Iterable<@NonNull TimeGraphEntry> entries, long zoomStartTime, long zoomEndTime, long resolution, @NonNull IProgressMonitor monitor) {
        if (resolution < 0) {
            // StateSystemUtils.getTimes would throw an illegal argument exception.
            return;
        }

        long start = Long.min(zoomStartTime, zoomEndTime);
        long end = Long.max(zoomStartTime, zoomEndTime);
        List<@NonNull Long> times = StateSystemUtils.getTimes(start, end, resolution);
        Sampling sampling = new Sampling(start, end, resolution);
        Map<ITimeGraphDataProvider<? extends TimeGraphEntryModel>, Multimap<Long, TimeGraphEntry>> groupedEntries = filterGroupEntries(entries, zoomStartTime, zoomEndTime);
        SubMonitor subMonitor = SubMonitor.convert(monitor, getClass().getSimpleName() + "#zoomEntries", groupedEntries.size()); //$NON-NLS-1$

        for (Entry<ITimeGraphDataProvider<? extends TimeGraphEntryModel>, Multimap<Long, TimeGraphEntry>> entry : groupedEntries.entrySet()) {
            ITimeGraphDataProvider<? extends TimeGraphEntryModel> dataProvider = entry.getKey();
            Multimap<Long, TimeGraphEntry> map = entry.getValue();
            SelectionTimeQueryFilter filter = new SelectionTimeQueryFilter(times, map.keySet());
            TmfModelResponse<List<ITimeGraphRowModel>> response = dataProvider.fetchRowModel(filter, monitor);

            List<ITimeGraphRowModel> model = response.getModel();
            if (model != null) {
                zoomEntries(map, model, response.getStatus() == ITmfResponse.Status.COMPLETED, sampling);
            }
            subMonitor.worked(1);
        }
    }

    /**
     * Filter the entries to return only the Non Null {@link TimeGraphEntry} which
     * intersect the time range.
     *
     * @param visible
     *            the input list of visible entries
     * @param zoomStartTime
     *            the leftmost time bound of the view
     * @param zoomEndTime
     *            the rightmost time bound of the view
     * @return A Map of the visible entries keyed by their model ID and grouped by
     *         their data provider.
     */
    private static Map<ITimeGraphDataProvider<? extends TimeGraphEntryModel>, Multimap<Long, TimeGraphEntry>> filterGroupEntries(Iterable<TimeGraphEntry> visible,
            long zoomStartTime, long zoomEndTime) {
        Map<ITimeGraphDataProvider<? extends TimeGraphEntryModel>, Multimap<Long, TimeGraphEntry>> quarksToEntries = new HashMap<>();
        for (TimeGraphEntry entry : visible) {
            if (zoomStartTime <= entry.getEndTime() && zoomEndTime >= entry.getStartTime() && entry.hasTimeEvents()) {
                ITimeGraphDataProvider<? extends TimeGraphEntryModel> provider = getProvider(entry);
                if (provider != null) {
                    Multimap<Long, TimeGraphEntry> multimap = quarksToEntries.computeIfAbsent(provider, p -> HashMultimap.create());
                    multimap.put(entry.getModel().getId(), entry);
                }
            }
        }
        return quarksToEntries;
    }

    private void zoomEntries(Multimap<Long, TimeGraphEntry> map, List<ITimeGraphRowModel> model, boolean completed, Sampling sampling) {
        boolean isZoomThread = Thread.currentThread() instanceof ZoomThread;
        for (ITimeGraphRowModel rowModel : model) {
            Collection<TimeGraphEntry> resourceEntries = map.get(rowModel.getEntryID());

            for (TimeGraphEntry resourceEntry : resourceEntries) {
                List<ITimeEvent> events = createTimeEvents(resourceEntry, rowModel.getStates());
                if (isZoomThread) {
                    applyResults(() -> {
                        resourceEntry.setZoomedEventList(events);
                        if (completed) {
                            resourceEntry.setSampling(sampling);
                        }
                    });
                } else {
                    resourceEntry.setEventList(events);
                }
            }
        }
    }

    /**
     * Create {@link ITimeEvent}s for an entry from the list of
     * {@link ITimeGraphState}s, filling in the gaps.
     *
     * @param entry
     *            the {@link TimeGraphEntry} on which we are working
     * @param values
     *            the list of {@link ITimeGraphState}s from the
     *            {@link ITimeGraphDataProvider}.
     * @return a contiguous List of {@link ITimeEvent}s
     */
    protected List<ITimeEvent> createTimeEvents(TimeGraphEntry entry, List<ITimeGraphState> values) {
        List<ITimeEvent> events = new ArrayList<>(values.size());
        ITimeEvent prev = null;
        for (ITimeGraphState state : values) {
            ITimeEvent event = createTimeEvent(entry, state);
            if (prev != null) {
                long prevEnd = prev.getTime() + prev.getDuration();
                if (prevEnd < event.getTime()) {
                    // fill in the gap.
                    events.add(new TimeEvent(entry, prevEnd, event.getTime() - prevEnd));
                }
            }
            prev = event;
            events.add(event);
        }
        return events;
    }

    /**
     * Create a {@link TimeEvent} for a {@link TimeGraphEntry} and a
     * {@link TimeGraphState}
     *
     * @param entry
     *            {@link TimeGraphEntry} for which we create a state
     * @param state
     *            {@link ITimeGraphState} from the data provider
     * @return a new {@link TimeEvent} for these arguments
     *
     * @since 3.3
     */
    protected TimeEvent createTimeEvent(TimeGraphEntry entry, ITimeGraphState state) {
        if (state.getValue() == Integer.MIN_VALUE) {
            return new NullTimeEvent(entry, state.getStartTime(), state.getDuration());
        }
        String label = state.getLabel();
        if (label != null) {
            return new NamedTimeEvent(entry, state.getStartTime(), state.getDuration(), (int) state.getValue(), label);
        }
        return new TimeEvent(entry, state.getStartTime(), state.getDuration(), (int) state.getValue());
    }

}
