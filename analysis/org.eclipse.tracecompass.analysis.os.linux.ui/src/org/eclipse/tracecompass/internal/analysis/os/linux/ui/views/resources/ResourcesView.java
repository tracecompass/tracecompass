/*******************************************************************************
 * Copyright (c) 2012, 2017 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Geneviève Bastien - Move code to provide base classes for time graph views
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tracecompass.analysis.os.linux.core.signals.TmfCpuSelectedSignal;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.resourcesstatus.ResourcesEntryModel;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.resourcesstatus.ResourcesEntryModel.Type;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.resourcesstatus.ResourcesStatusDataProvider;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.Activator;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.Messages;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.actions.FollowCpuAction;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.actions.UnfollowCpuAction;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph.ITimeGraphRowModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph.ITimeGraphState;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.AbstractTimeGraphView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NamedTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NullTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry.Sampling;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

/**
 * Main implementation for the LTTng 2.0 kernel Resource view
 *
 * @author Patrick Tasse
 */
public class ResourcesView extends AbstractTimeGraphView {

    /** View ID. */
    public static final @NonNull String ID = "org.eclipse.tracecompass.analysis.os.linux.views.resources"; //$NON-NLS-1$

    /** ID of the followed CPU in the map data in {@link TmfTraceContext} */
    public static final @NonNull String RESOURCES_FOLLOW_CPU = ID + ".FOLLOW_CPU"; //$NON-NLS-1$

    private static final String[] FILTER_COLUMN_NAMES = new String[] {
            Messages.ResourcesView_stateTypeName
    };

    // Timeout between updates in the build thread in ms
    private static final long BUILD_UPDATE_TIMEOUT = 500;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public ResourcesView() {
        super(ID, new ResourcesPresentationProvider());
        setFilterColumns(FILTER_COLUMN_NAMES);
        setFilterLabelProvider(new ResourcesFilterLabelProvider());
        setEntryComparator(new ResourcesEntryComparator());
        setAutoExpandLevel(1);
    }

    private static class ResourcesEntryComparator implements Comparator<ITimeGraphEntry> {
        @Override
        public int compare(ITimeGraphEntry o1, ITimeGraphEntry o2) {
            ResourcesEntry entry1 = (ResourcesEntry) o1;
            ResourcesEntry entry2 = (ResourcesEntry) o2;
            if (entry1.getType() == Type.TRACE && entry2.getType() == Type.TRACE) {
                /* sort trace entries alphabetically */
                return entry1.getName().compareTo(entry2.getName());
            }
            /* sort resource entries by their defined order */
            return entry1.compareTo(entry2);
        }
    }

    /**
     * @since 2.0
     */
    @Override
    protected void fillTimeGraphEntryContextMenu(@NonNull IMenuManager menuManager) {
        ISelection selection = getSite().getSelectionProvider().getSelection();
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection sSel = (IStructuredSelection) selection;
            if (sSel.getFirstElement() instanceof ResourcesEntry) {
                ResourcesEntry resourcesEntry = (ResourcesEntry) sSel.getFirstElement();
                if (resourcesEntry.getType().equals(Type.CPU)) {
                    TmfTraceContext ctx = TmfTraceManager.getInstance().getCurrentTraceContext();
                    Integer data = (Integer) ctx.getData(RESOURCES_FOLLOW_CPU);
                    int cpu = data != null ? data.intValue() : -1;
                    if (cpu >= 0) {
                        menuManager.add(new UnfollowCpuAction(ResourcesView.this, resourcesEntry.getId(), resourcesEntry.getTrace()));
                    } else {
                        menuManager.add(new FollowCpuAction(ResourcesView.this, resourcesEntry.getId(), resourcesEntry.getTrace()));
                    }
                }
            }
        }
    }

    private static class ResourcesFilterLabelProvider extends TreeLabelProvider {
        @Override
        public String getColumnText(Object element, int columnIndex) {
            ResourcesEntry entry = (ResourcesEntry) element;
            if (columnIndex == 0) {
                return entry.getName();
            }
            return ""; //$NON-NLS-1$
        }

    }

    // ------------------------------------------------------------------------
    // Internal
    // ------------------------------------------------------------------------

    @Override
    protected String getNextText() {
        return Messages.ResourcesView_nextResourceActionNameText;
    }

    @Override
    protected String getNextTooltip() {
        return Messages.ResourcesView_nextResourceActionToolTipText;
    }

    @Override
    protected String getPrevText() {
        return Messages.ResourcesView_previousResourceActionNameText;
    }

    @Override
    protected String getPrevTooltip() {
        return Messages.ResourcesView_previousResourceActionToolTipText;
    }

    @Override
    protected void buildEntryList(ITmfTrace trace, ITmfTrace parentTrace, final IProgressMonitor monitor) {
        ResourcesStatusDataProvider dataProvider = DataProviderManager.getInstance()
                .getDataProvider(trace, ResourcesStatusDataProvider.ID, ResourcesStatusDataProvider.class);
        if (dataProvider == null) {
            return;
        }

        SubMonitor subMonitor = SubMonitor.convert(monitor);
        boolean complete = false;
        ResourcesEntry traceEntry = null;
        Map<Long, TimeGraphEntry> map = new HashMap<>();
        while (!complete && !subMonitor.isCanceled()) {
            TmfModelResponse<List<ResourcesEntryModel>> response = dataProvider.fetchTree(new TimeQueryFilter(0, Long.MAX_VALUE, 2), subMonitor);
            if (response.getStatus() == ITmfResponse.Status.FAILED) {
                Activator.getDefault().logError("Resources Data Provider failed: " + response.getStatusMessage()); //$NON-NLS-1$
                return;
            } else if (response.getStatus() == ITmfResponse.Status.CANCELLED) {
                return;
            }
            complete = response.getStatus() == ITmfResponse.Status.COMPLETED;

            List<ResourcesEntryModel> model = response.getModel();
            if (model != null) {
                for (ResourcesEntryModel entry : model) {
                    if (entry.getType() != ResourcesEntryModel.Type.TRACE) {
                        TimeGraphEntry uiEntry = map.get(entry.getId());
                        if (uiEntry == null) {
                            uiEntry = new ResourcesEntry(entry, trace);
                            map.put(entry.getId(), uiEntry);

                            TimeGraphEntry parent = map.getOrDefault(entry.getParentId(), traceEntry);
                            parent.addChild(uiEntry);
                        } else {
                            uiEntry.updateModel(entry);
                        }
                    } else {
                        setStartTime(Long.min(getStartTime(), entry.getStartTime()));
                        setEndTime(Long.max(getEndTime(), entry.getEndTime() + 1));

                        if (traceEntry != null) {
                            traceEntry.updateModel(entry);
                        } else {
                            traceEntry = new ResourcesEntry(entry, trace);
                            addToEntryList(parentTrace, Collections.singletonList(traceEntry));
                        }
                    }
                }
                Objects.requireNonNull(traceEntry);
                long start = traceEntry.getStartTime();
                long end = traceEntry.getEndTime();
                final long resolution = Long.max(1, (end - start) / getDisplayWidth());
                zoomEntries(map.values(), start, end, resolution, subMonitor);
            }

            if (subMonitor.isCanceled()) {
                return;
            }

            if (parentTrace.equals(getTrace())) {
                refresh();
            }
            subMonitor.worked(1);

            if (!complete && !subMonitor.isCanceled()) {
                try {
                    Thread.sleep(BUILD_UPDATE_TIMEOUT);
                } catch (InterruptedException e) {
                    Activator.getDefault().logError("Failed to wait for data provider", e); //$NON-NLS-1$
                }
            }
        }
    }

    @Override
    protected void zoomEntries(@NonNull Iterable<@NonNull TimeGraphEntry> entries, long zoomStartTime, long zoomEndTime,
            long resolution, @NonNull IProgressMonitor monitor) {
        if (resolution < 0) {
            // StateSystemUtils.getTimes would throw an illegal argument exception.
            return;
        }

        long start = Long.min(zoomStartTime, zoomEndTime);
        long end = Long.max(zoomStartTime, zoomEndTime);
        List<@NonNull Long> times = StateSystemUtils.getTimes(start, end, resolution);
        Sampling sampling = new Sampling(start, end, resolution);
        Map<ResourcesStatusDataProvider, Multimap<Long, ResourcesEntry>> resourceEntries = filterGroupEntries(entries, zoomStartTime, zoomEndTime);
        SubMonitor subMonitor = SubMonitor.convert(monitor, "ResourcesView#zoomEntries", resourceEntries.size()); //$NON-NLS-1$

        for (Entry<ResourcesStatusDataProvider, Multimap<Long, ResourcesEntry>> entry : resourceEntries.entrySet()) {
            ResourcesStatusDataProvider dataProvider = entry.getKey();
            Multimap<Long, ResourcesEntry> map = entry.getValue();
            SelectionTimeQueryFilter filter = new SelectionTimeQueryFilter(times, map.keySet());
            TmfModelResponse<List<ITimeGraphRowModel>> response = dataProvider.fetchRowModel(filter, monitor);

            List<ITimeGraphRowModel> model = response.getModel();
            if (model != null) {
                zoomEntries(map, model, response.getStatus() == ITmfResponse.Status.COMPLETED, sampling);
            }
            subMonitor.worked(1);
        }
    }

    private void zoomEntries(Multimap<Long, ResourcesEntry> map, List<ITimeGraphRowModel> model, boolean completed, Sampling sampling) {
        boolean isZoomThread = Thread.currentThread() instanceof ZoomThread;
        for (ITimeGraphRowModel rowModel : model) {
            Collection<ResourcesEntry> resourceEntries = map.get(rowModel.getEntryID());

            for (ResourcesEntry resourceEntry : resourceEntries) {
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
     * Filter the entries to return only the Non Null {@link ResourcesEntry} which
     * intersect the time range.
     *
     * @param visible
     *            the input list of visible entries
     * @param zoomStartTime
     *            the leftmost time bound of the view
     * @param zoomEndTime
     *            the rightmost time bound of the view
     * @return A Map of the visible entries keyed by their state system and grouped
     *         by their data provider.
     */
    private static Map<ResourcesStatusDataProvider, Multimap<Long, ResourcesEntry>> filterGroupEntries(Iterable<TimeGraphEntry> visible,
            long zoomStartTime, long zoomEndTime) {
        DataProviderManager manager = DataProviderManager.getInstance();
        Iterable<ResourcesEntry> resourceEntries = Iterables.filter(visible, ResourcesEntry.class);
        Map<ResourcesStatusDataProvider, Multimap<Long, ResourcesEntry>> quarksToEntries = new HashMap<>();
        for (ResourcesEntry entry : resourceEntries) {
            if (zoomStartTime <= entry.getEndTime() && zoomEndTime >= entry.getStartTime()) {
                ResourcesStatusDataProvider provider = manager.getDataProvider(entry.getTrace(), ResourcesStatusDataProvider.ID, ResourcesStatusDataProvider.class);
                if (provider != null) {
                    Multimap<Long, ResourcesEntry> multimap = quarksToEntries.computeIfAbsent(provider, p -> HashMultimap.create());
                    multimap.put(entry.getModel().getId(), entry);
                }
            }
        }
        return quarksToEntries;
    }

    private static List<ITimeEvent> createTimeEvents(ResourcesEntry resourceEntry, List<ITimeGraphState> value) {
        List<ITimeEvent> events = new ArrayList<>(value.size());
        ITimeEvent prev = null;
        for (ITimeGraphState interval : value) {
            ITimeEvent event = createTimeEvent(interval, resourceEntry);
            if (prev != null) {
                long prevEnd = prev.getTime() + prev.getDuration();
                if (prevEnd < event.getTime()) {
                    // fill in the gap.
                    events.add(new TimeEvent(resourceEntry, prevEnd, event.getTime() - prevEnd, -1));
                }
            }
            prev = event;
            events.add(event);
        }
        return events;
    }

    /**
     * Insert a {@link TimeEvent} from an {@link ITmfStateInterval} into a
     * {@link }.
     *
     * @param resourceEntry
     *            resource entry which receives the new entry.
     * @param interval
     *            state interval which will generate the new event
     */
    private static TimeEvent createTimeEvent(ITimeGraphState state, ResourcesEntry resourceEntry) {
        if (state.getValue() == Integer.MIN_VALUE) {
            return new NullTimeEvent(resourceEntry, state.getStartTime(), state.getDuration());
        }
        String label = state.getLabel();
        if (label != null) {
            return new NamedTimeEvent(resourceEntry, state.getStartTime(), state.getDuration(), (int) state.getValue(), label);
        }
        return new TimeEvent(resourceEntry, state.getStartTime(), state.getDuration(), (int) state.getValue());
    }

    /**
     * Signal handler for a cpu selected signal.
     *
     * @param signal
     *            the cpu selected signal
     * @since 2.0
     */
    @TmfSignalHandler
    public void listenToCpu(TmfCpuSelectedSignal signal) {
        int data = signal.getCore() >= 0 ? signal.getCore() : -1;
        ITmfTrace trace = getTrace();
        if (trace == null) {
            return;
        }
        TmfTraceManager.getInstance().updateTraceContext(trace,
                builder -> builder.setData(RESOURCES_FOLLOW_CPU, data));
    }

}
