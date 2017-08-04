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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.core.signals.TmfCpuSelectedSignal;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.Attributes;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.Activator;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.Messages;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.actions.FollowCpuAction;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.actions.UnfollowCpuAction;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.resources.ResourcesEntry.Type;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.AbstractTimeGraphView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NullTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Table;
import com.google.common.collect.TreeMultimap;

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
    private static final String WILDCARD = "*"; //$NON-NLS-1$

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
            if (entry1.getType() == Type.NULL && entry2.getType() == Type.NULL) {
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
                if (resourcesEntry.getType().equals(ResourcesEntry.Type.CPU)) {
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
        final ITmfStateSystem ssq = TmfStateSystemAnalysisModule.getStateSystem(trace, KernelAnalysisModule.ID);
        if (ssq == null) {
            return;
        }
        Map<Integer, ResourcesEntry> entryMap = new HashMap<>();

        long startTime = ssq.getStartTime();
        long start = startTime;
        TimeGraphEntry traceEntry = new ResourcesEntry(trace, trace.getName(), startTime, ssq.getCurrentEndTime() + 1, 0);
        addToEntryList(parentTrace, Collections.singletonList(traceEntry));
        setStartTime(Long.min(getStartTime(), startTime));
        boolean complete = false;
        while (!complete && !monitor.isCanceled() && !ssq.isCancelled()) {
            complete = ssq.waitUntilBuilt(BUILD_UPDATE_TIMEOUT);
            long end = ssq.getCurrentEndTime();
            if (start == end && !complete) {
                // when complete execute one last time regardless of end time
                continue;
            }
            long endTime = end + 1;
            setEndTime(Long.max(getEndTime(), end));

            traceEntry.updateEndTime(endTime);
            List<Integer> cpuQuarks = ssq.getQuarks(Attributes.CPUS, WILDCARD);
            createCpuEntriesWithQuark(trace, ssq, entryMap, traceEntry, startTime, endTime, cpuQuarks);

            final long resolution = Long.max(1, (end - ssq.getStartTime()) / getDisplayWidth());
            /* Transform is just to change the type. */
            Iterable<TimeGraphEntry> entries = Iterables.transform(entryMap.values(), e -> (TimeGraphEntry) e);
            zoomEntries(entries, ssq.getStartTime(), end, resolution, monitor);

            if (parentTrace.equals(getTrace())) {
                refresh();
            }
            start = end;
        }
    }

    private static void createCpuEntriesWithQuark(@NonNull ITmfTrace trace, final ITmfStateSystem ssq, Map<Integer, ResourcesEntry> entryMap, TimeGraphEntry traceEntry, long startTime, long endTime, List<Integer> cpuQuarks) {
        for (Integer cpuQuark : cpuQuarks) {
            final @NonNull String cpuName = ssq.getAttributeName(cpuQuark);
            int cpu = Integer.parseInt(cpuName);
            ResourcesEntry cpuEntry = entryMap.get(cpuQuark);
            if (cpuEntry == null) {
                cpuEntry = new ResourcesEntry(cpuQuark, trace, startTime, endTime, Type.CPU, cpu);
                entryMap.put(cpuQuark, cpuEntry);
                traceEntry.addChild(cpuEntry);
            } else {
                cpuEntry.updateEndTime(endTime);
            }
            List<Integer> irqQuarks = ssq.getQuarks(Attributes.CPUS, cpuName, Attributes.IRQS, WILDCARD);
            createCpuInterruptEntryWithQuark(trace, ssq, entryMap, startTime, endTime, traceEntry, cpuEntry, irqQuarks, Type.IRQ);
            List<Integer> softIrqQuarks = ssq.getQuarks(Attributes.CPUS, cpuName, Attributes.SOFT_IRQS, WILDCARD);
            createCpuInterruptEntryWithQuark(trace, ssq, entryMap, startTime, endTime, traceEntry, cpuEntry, softIrqQuarks, Type.SOFT_IRQ);
        }
    }

    /**
     * Create and add execution contexts to a cpu entry. Also creates an
     * aggregate entry in the root trace entry. The execution context is
     * basically what the cpu is doing in its execution stack. It can be in an
     * IRQ, Soft IRQ. MCEs, NMIs, Userland and Kernel execution is not yet
     * supported.
     *
     * @param trace
     *            the trace
     * @param ssq
     *            the state system
     * @param entryMap
     *            the entry map
     * @param startTime
     *            the start time in nanoseconds
     * @param endTime
     *            the end time in nanoseconds
     * @param traceEntry
     *            the trace timegraph entry
     * @param cpuEntry
     *            the cpu timegraph entry (the entry under the trace entry
     * @param childrenQuarks
     *            the quarks to add to cpu entry
     * @param type
     *            the type of entry being added
     */
    private static void createCpuInterruptEntryWithQuark(@NonNull ITmfTrace trace,
            final ITmfStateSystem ssq, Map<Integer, ResourcesEntry> entryMap,
            long startTime, long endTime, TimeGraphEntry traceEntry, ResourcesEntry cpuEntry,
            List<Integer> childrenQuarks, Type type) {
        for (Integer quark : childrenQuarks) {
            final @NonNull String resourceName = ssq.getAttributeName(quark);
            int resourceId = Integer.parseInt(resourceName);
            ResourcesEntry interruptEntry = entryMap.get(quark);
            if (interruptEntry == null) {
                interruptEntry = new ResourcesEntry(quark, trace, startTime, endTime, type, resourceId);
                entryMap.put(quark, interruptEntry);
                cpuEntry.addChild(interruptEntry);
                boolean found = false;
                for (ITimeGraphEntry rootElem : traceEntry.getChildren()) {
                    if (rootElem instanceof AggregateResourcesEntry) {
                        AggregateResourcesEntry aggregateInterruptEntry = (AggregateResourcesEntry) rootElem;
                        if (aggregateInterruptEntry.getId() == resourceId && aggregateInterruptEntry.getType().equals(type)) {
                            found = true;
                            aggregateInterruptEntry.addContributor(interruptEntry);
                            final AggregateResourcesEntry irqCpuEntry = new AggregateResourcesEntry(trace, cpuEntry.getName(), startTime, endTime, type, cpuEntry.getId());
                            irqCpuEntry.addContributor(interruptEntry);
                            aggregateInterruptEntry.addChild(irqCpuEntry);
                            break;
                        }
                    }
                }
                if (!found) {
                    AggregateResourcesEntry aggregateInterruptEntry = new AggregateResourcesEntry(trace, startTime, endTime, type, resourceId);
                    aggregateInterruptEntry.addContributor(interruptEntry);
                    final AggregateResourcesEntry irqCpuEntry = new AggregateResourcesEntry(trace, cpuEntry.getName(), startTime, endTime, type, cpuEntry.getId());
                    irqCpuEntry.addContributor(interruptEntry);
                    aggregateInterruptEntry.addChild(irqCpuEntry);
                    traceEntry.addChild(aggregateInterruptEntry);
                }
            } else {
                interruptEntry.updateEndTime(endTime);
            }
        }
    }

    @Override
    protected void zoomEntries(Iterable<TimeGraphEntry> visible, long zoomStartTime, long zoomEndTime,
            long resolution, @NonNull IProgressMonitor monitor) {
        boolean isZoomThread = Thread.currentThread() instanceof ZoomThread;

        /* Filter the relevant entries and group them by ss */
        Table<ITmfStateSystem, Integer, ResourcesEntry> table = filterGroupEntries(visible, zoomStartTime, zoomEndTime);
        TreeMultimap<Integer, ITmfStateInterval> intervals = TreeMultimap.create(Comparator.naturalOrder(),
                Comparator.comparingLong(ITmfStateInterval::getStartTime));

        /* For each ss and its entries */
        for (Entry<ITmfStateSystem, Map<Integer, ResourcesEntry>> entry : table.rowMap().entrySet()) {
            ITmfStateSystem ss = entry.getKey();
            Map<Integer, ResourcesEntry> quarksToEntries = entry.getValue();

            long start = Long.max(zoomStartTime, ss.getStartTime());
            long end = Long.min(zoomEndTime, ss.getCurrentEndTime());
            if (start > end) {
                continue;
            }
            /* Get the time stamps for the 2D query */
            List<Long> times = StateSystemUtils.getTimes(start, end, resolution);
            try {
                /* Do the actual query */
                for (ITmfStateInterval interval : ss.query2D(quarksToEntries.keySet(), times)) {
                    if (monitor.isCanceled()) {
                        return;
                    }
                    intervals.put(interval.getAttribute(), interval);
                }
                for (Entry<Integer, Collection<ITmfStateInterval>> e : intervals.asMap().entrySet()) {
                    if (monitor.isCanceled()) {
                        return;
                    }
                    addTimeEvents(quarksToEntries.get(e.getKey()), e.getValue(), isZoomThread);
                }
            } catch (TimeRangeException e) {
                Activator.getDefault().logError("Resources zoom", e); //$NON-NLS-1$
            } catch (StateSystemDisposedException e) {
                /* State System has been disposed, no need to try again. */
            } finally {
                intervals.clear();
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
     * @return A Table of the visible entries keyed by their state system and status
     *         interval quark.
     */
    private static Table<ITmfStateSystem, Integer, ResourcesEntry> filterGroupEntries(Iterable<TimeGraphEntry> visible,
            long zoomStartTime, long zoomEndTime) {
        Table<ITmfStateSystem, Integer, ResourcesEntry> quarksToEntries = HashBasedTable.create();
        for (ResourcesEntry tge : Iterables.filter(visible, ResourcesEntry.class)) {
            if (zoomStartTime <= tge.getEndTime() && zoomEndTime >= tge.getStartTime() && tge.getQuark() >= 0) {
                ITmfStateSystem ss = TmfStateSystemAnalysisModule.getStateSystem(tge.getTrace(), KernelAnalysisModule.ID);
                if (ss != null) {
                    quarksToEntries.put(ss, tge.getQuark(), tge);
                }
            }
        }
        return quarksToEntries;
    }

    private void addTimeEvents(@Nullable ResourcesEntry resourceEntry, Collection<ITmfStateInterval> value, boolean isZoomThread) {
        if (resourceEntry == null) {
            return;
        }
        List<ITimeEvent> events = new ArrayList<>(value.size());
        ITimeEvent prev = null;
        for (ITmfStateInterval interval : value) {
            ITimeEvent event = createTimeEvent(interval, resourceEntry);
            if (prev != null) {
                long prevEnd = prev.getTime() + prev.getDuration();
                if (prevEnd < event.getTime()) {
                    // fill in the gap.
                    events.add(new TimeEvent(resourceEntry, prevEnd, event.getTime() - prevEnd));
                }
            }
            prev = event;
            events.add(event);
        }
        if (isZoomThread) {
            applyResults(() -> resourceEntry.setZoomedEventList(events));
        } else {
            resourceEntry.setEventList(events);
        }
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
    private static TimeEvent createTimeEvent(ITmfStateInterval interval, ResourcesEntry resourceEntry) {
        long startTime = interval.getStartTime();
        long duration = interval.getEndTime() - startTime + 1;
        Object status = interval.getValue();
        if (status instanceof Integer) {
            return new TimeEvent(resourceEntry, startTime, duration, (int) status);
        }
        return new NullTimeEvent(resourceEntry, startTime, duration);
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
