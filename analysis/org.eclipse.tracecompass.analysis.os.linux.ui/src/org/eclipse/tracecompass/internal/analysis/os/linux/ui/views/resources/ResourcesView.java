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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.core.signals.TmfCpuSelectedSignal;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.Attributes;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.Messages;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.actions.FollowCpuAction;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.actions.UnfollowCpuAction;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.resources.ResourcesEntry.Type;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.AbstractStateSystemTimeGraphView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NullTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;

/**
 * Main implementation for the LTTng 2.0 kernel Resource view
 *
 * @author Patrick Tasse
 */
public class ResourcesView extends AbstractStateSystemTimeGraphView {

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
        TimeGraphEntry traceEntry = null;

        long startTime = ssq.getStartTime();
        long start = startTime;
        setStartTime(Math.min(getStartTime(), startTime));
        boolean complete = false;
        while (!complete) {
            if (monitor.isCanceled()) {
                return;
            }
            complete = ssq.waitUntilBuilt(BUILD_UPDATE_TIMEOUT);
            if (ssq.isCancelled()) {
                return;
            }
            long end = ssq.getCurrentEndTime();
            if (start == end && !complete) {
                // when complete execute one last time regardless of end time
                continue;
            }
            long endTime = end + 1;
            setEndTime(Math.max(getEndTime(), end));

            if (traceEntry == null) {
                traceEntry = new ResourcesEntry(trace, trace.getName(), startTime, endTime, 0);
                List<TimeGraphEntry> entryList = Collections.singletonList(traceEntry);
                addToEntryList(parentTrace, ssq, entryList);
            } else {
                traceEntry.updateEndTime(endTime);
            }
            List<Integer> cpuQuarks = ssq.getQuarks(Attributes.CPUS, "*"); //$NON-NLS-1$
            createCpuEntriesWithQuark(trace, ssq, entryMap, traceEntry, startTime, endTime, cpuQuarks);
            if (parentTrace.equals(getTrace())) {
                refresh();
            }
            final List<@NonNull TimeGraphEntry> traceEntryChildren = traceEntry.getChildren();
            final long resolution = Math.max(1, (endTime - ssq.getStartTime()) / getDisplayWidth());
            queryFullStates(ssq, ssq.getStartTime(), end, resolution, monitor, new IQueryHandler() {
                @Override
                public void handle(List<List<ITmfStateInterval>> fullStates, List<ITmfStateInterval> prevFullState) {
                    for (TimeGraphEntry child : traceEntryChildren) {
                        if (!populateEventsRecursively(fullStates, prevFullState, child).isOK()) {
                            return;
                        }
                    }
                }

                private IStatus populateEventsRecursively(@NonNull List<List<ITmfStateInterval>> fullStates, @Nullable List<ITmfStateInterval> prevFullState, @NonNull TimeGraphEntry entry) {
                    if (monitor.isCanceled()) {
                        return Status.CANCEL_STATUS;
                    }
                    List<ITimeEvent> eventList = getEventList(entry, ssq, fullStates, prevFullState, monitor);
                    if (eventList != null) {
                        /* Start a new event list on first iteration, then append to it */
                        if (prevFullState == null) {
                            entry.setEventList(eventList);
                        } else {
                            for (ITimeEvent event : eventList) {
                                entry.addEvent(event);
                            }
                        }
                    }
                    for (TimeGraphEntry child : entry.getChildren()) {
                        IStatus status = populateEventsRecursively(fullStates, prevFullState, child);
                        if (!status.isOK()) {
                            return status;
                        }
                    }
                    return Status.OK_STATUS;
                }
            });

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
            List<Integer> irqQuarks = ssq.getQuarks(Attributes.CPUS, cpuName, Attributes.IRQS, "*"); //$NON-NLS-1$
            createCpuInterruptEntryWithQuark(trace, ssq, entryMap, startTime, endTime, traceEntry, cpuEntry, irqQuarks, Type.IRQ);
            List<Integer> softIrqQuarks = ssq.getQuarks(Attributes.CPUS, cpuName, Attributes.SOFT_IRQS, "*"); //$NON-NLS-1$
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
            long startTime, long endTime,
            TimeGraphEntry traceEntry, ResourcesEntry cpuEntry,
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
    protected @Nullable List<ITimeEvent> getEventList(@NonNull TimeGraphEntry entry, ITmfStateSystem ssq,
            @NonNull List<List<ITmfStateInterval>> fullStates, @Nullable List<ITmfStateInterval> prevFullState, @NonNull IProgressMonitor monitor) {
        ResourcesEntry resourcesEntry = (ResourcesEntry) entry;
        int quark = resourcesEntry.getQuark();

        if (resourcesEntry.getType().equals(Type.CPU)) {
            return createCpuEventsList(entry, fullStates, prevFullState, monitor, quark);
        } else if ((resourcesEntry.getType().equals(Type.IRQ) || resourcesEntry.getType().equals(Type.SOFT_IRQ)) && (quark >= 0)) {
            return createIrqEventsList(entry, fullStates, prevFullState, monitor, quark);
        }

        return null;
    }

    private static List<ITimeEvent> createCpuEventsList(ITimeGraphEntry entry, List<List<ITmfStateInterval>> fullStates, List<ITmfStateInterval> prevFullState, IProgressMonitor monitor, int quark) {
        List<ITimeEvent> eventList;
        boolean isZoomThread = Thread.currentThread() instanceof ZoomThread;
        eventList = new ArrayList<>(fullStates.size());
        ITmfStateInterval lastInterval = prevFullState == null || quark >= prevFullState.size() ? null : prevFullState.get(quark);
        long lastStartTime = lastInterval == null ? -1 : lastInterval.getStartTime();
        long lastEndTime = lastInterval == null ? -1 : lastInterval.getEndTime() + 1;
        for (List<ITmfStateInterval> fullState : fullStates) {
            if (monitor.isCanceled()) {
                return null;
            }
            if (quark >= fullState.size()) {
                /* No information on this CPU (yet?), skip it for now */
                continue;
            }
            ITmfStateInterval statusInterval = fullState.get(quark);
            int status = statusInterval.getStateValue().unboxInt();
            long time = statusInterval.getStartTime();
            long duration = statusInterval.getEndTime() - time + 1;
            if (time == lastStartTime) {
                continue;
            }
            if (!statusInterval.getStateValue().isNull()) {
                if (lastEndTime != time && lastEndTime != -1) {
                    eventList.add(new TimeEvent(entry, lastEndTime, time - lastEndTime));
                }
                eventList.add(new TimeEvent(entry, time, duration, status));
            } else if (isZoomThread) {
                eventList.add(new NullTimeEvent(entry, time, duration));
            }
            lastStartTime = time;
            lastEndTime = time + duration;
        }
        return eventList;
    }

    private static List<ITimeEvent> createIrqEventsList(ITimeGraphEntry entry, List<List<ITmfStateInterval>> fullStates, List<ITmfStateInterval> prevFullState, IProgressMonitor monitor, int quark) {
        List<ITimeEvent> eventList;
        boolean isZoomThread = Thread.currentThread() instanceof ZoomThread;
        eventList = new ArrayList<>(fullStates.size());
        ITmfStateInterval lastInterval = prevFullState == null || quark >= prevFullState.size() ? null : prevFullState.get(quark);
        long lastStartTime = lastInterval == null ? -1 : lastInterval.getStartTime();
        long lastEndTime = lastInterval == null ? -1 : lastInterval.getEndTime() + 1;
        boolean lastIsNull = lastInterval == null ? false : lastInterval.getStateValue().isNull();
        for (List<ITmfStateInterval> fullState : fullStates) {
            if (monitor.isCanceled()) {
                return null;
            }
            if (quark >= fullState.size()) {
                /* No information on this IRQ (yet?), skip it for now */
                continue;
            }
            ITmfStateInterval irqInterval = fullState.get(quark);
            long time = irqInterval.getStartTime();
            long duration = irqInterval.getEndTime() - time + 1;
            if (time == lastStartTime) {
                continue;
            }
            if (!irqInterval.getStateValue().isNull()) {
                int cpu = irqInterval.getStateValue().unboxInt();
                eventList.add(new TimeEvent(entry, time, duration, cpu));
                lastIsNull = false;
            } else {
                if (lastEndTime != time && lastIsNull) {
                    /*
                     * This is a special case where we want to show IRQ_ACTIVE
                     * state but we don't know the CPU (it is between two null
                     * samples)
                     */
                    eventList.add(new TimeEvent(entry, lastEndTime, time - lastEndTime, -1));
                }
                if (isZoomThread) {
                    eventList.add(new NullTimeEvent(entry, time, duration));
                }
                lastIsNull = true;
            }
            lastStartTime = time;
            lastEndTime = time + duration;
        }
        return eventList;
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
