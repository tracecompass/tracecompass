/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.ui.views.vm.vcpuview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.KernelAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.KernelThreadInformationProvider;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.VmAttributes;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.module.VirtualMachineCpuAnalysis;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.trace.VirtualMachineExperiment;
import org.eclipse.tracecompass.internal.lttng2.kernel.ui.Activator;
import org.eclipse.tracecompass.internal.lttng2.kernel.ui.views.vm.vcpuview.VirtualMachineCommon.Type;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperimentUtils;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.AbstractTimeGraphView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NullTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;

import com.google.common.collect.Multimap;

/**
 * Main implementation for the Virtual Machine view
 *
 * @author Mohamad Gebai
 */
public class VirtualMachineView extends AbstractTimeGraphView {

    /** View ID. */
    public static final String ID = "org.eclipse.tracecompass.lttng2.analysis.vm.ui.vmview"; //$NON-NLS-1$

    private static final String[] COLUMN_NAMES = new String[] {
            Messages.VmView_stateTypeName
    };

    private static final String[] FILTER_COLUMN_NAMES = new String[] {
            Messages.VmView_stateTypeName
    };

    // Timeout between updates in the build thread in ms
    private static final long BUILD_UPDATE_TIMEOUT = 500;
    private static final int[] DEFAULT_WEIGHT = { 1, 3 };

    private Comparator<ITimeGraphEntry> fComparator = VirtualMachineViewEntry.getComparator();

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public VirtualMachineView() {
        super(ID, new VirtualMachinePresentationProvider());
        setFilterColumns(FILTER_COLUMN_NAMES);
        setTreeColumns(COLUMN_NAMES);
        setTreeLabelProvider(new VmViewTreeLabelProvider());
        setWeight(DEFAULT_WEIGHT);
        setAutoExpandLevel(2);
    }

    @Override
    protected @Nullable String getNextText() {
        return Messages.VmView_nextResourceActionNameText;
    }

    @Override
    protected @Nullable String getNextTooltip() {
        return Messages.VmView_nextResourceActionToolTipText;
    }

    @Override
    protected @Nullable String getPrevText() {
        return Messages.VmView_previousResourceActionNameText;
    }

    @Override
    protected @Nullable String getPrevTooltip() {
        return Messages.VmView_previousResourceActionToolTipText;
    }

    private static class VmViewTreeLabelProvider extends TreeLabelProvider {

        @Override
        public String getColumnText(@Nullable Object element, int columnIndex) {
            if (!(element instanceof VirtualMachineViewEntry)) {
                return ""; //$NON-NLS-1$
            }
            VirtualMachineViewEntry entry = (VirtualMachineViewEntry) element;

            if (COLUMN_NAMES[columnIndex].equals(Messages.VmView_stateTypeName)) {
                String name = entry.getName();
                return (name == null) ? "" : name; //$NON-NLS-1$
            }
            return ""; //$NON-NLS-1$
        }

    }

    // ------------------------------------------------------------------------
    // Internal
    // ------------------------------------------------------------------------

    @Override
    protected void buildEventList(ITmfTrace trace, ITmfTrace parentTrace, IProgressMonitor monitor) {
        setStartTime(Long.MAX_VALUE);
        setEndTime(Long.MIN_VALUE);

        if (!(parentTrace instanceof VirtualMachineExperiment)) {
            return;
        }
        ITmfStateSystem ssq = TmfStateSystemAnalysisModule.getStateSystem(parentTrace, VirtualMachineCpuAnalysis.ID);
        if (ssq == null) {
            return;
        }
        VirtualMachineExperiment vmExperiment = (VirtualMachineExperiment) parentTrace;
        long startTime = ssq.getStartTime();

        ArrayList<VirtualMachineViewEntry> entryList = new ArrayList<>();
        Map<String, VirtualMachineViewEntry> entryMap = new HashMap<>();

        boolean complete = false;
        VirtualMachineViewEntry groupEntry = new VirtualMachineViewEntry.VmEntryBuilder(vmExperiment.getName(), startTime, startTime, vmExperiment).build();
        entryList.add(groupEntry);
        putEntryList(parentTrace, new ArrayList<TimeGraphEntry>(entryList));

        while (!complete) {
            if (monitor.isCanceled()) {
                return;
            }
            complete = ssq.waitUntilBuilt(BUILD_UPDATE_TIMEOUT);
            if (ssq.isCancelled()) {
                return;
            }

            long endTime = ssq.getCurrentEndTime() + 1;
            groupEntry.updateEndTime(endTime);

            setStartTime(Math.min(getStartTime(), startTime));
            setEndTime(Math.max(getEndTime(), endTime));

            /*
             * Create the entries for the VMs in this experiment and their
             * respective threads
             */
            buildEntries(ssq, startTime, endTime, groupEntry, entryMap, vmExperiment);

            if (parentTrace.equals(getTrace())) {
                refresh();
            }

            /* Build event lists for each entry */
            buildEntryEventLists(entryList, ssq, startTime, endTime, monitor);
        }
    }

    private void buildEntries(ITmfStateSystem ssq, long startTime, long endTime, VirtualMachineViewEntry groupEntry,
            Map<@NonNull String, @NonNull VirtualMachineViewEntry> entryMap, VirtualMachineExperiment vmExperiment) {
        try {
            List<Integer> vmQuarks = ssq.getQuarks(VmAttributes.VIRTUAL_MACHINES, "*"); //$NON-NLS-1$
            /* For each virtual machine in the analysis */
            for (Integer vmQuark : vmQuarks) {

                /* Display an entry for the virtual machine */
                String vmHostId = ssq.getAttributeName(vmQuark);
                ITmfStateInterval vmNameInterval = StateSystemUtils.queryUntilNonNullValue(ssq, vmQuark, startTime, endTime);
                String vmName = vmHostId;
                if (vmNameInterval != null) {
                    vmName = vmNameInterval.getStateValue().unboxStr();
                }

                VirtualMachineViewEntry vmEntry = entryMap.get(vmHostId);
                if (vmEntry == null) {
                    vmEntry = new VirtualMachineViewEntry.VmEntryBuilder(vmName, startTime, endTime, vmExperiment).setId(vmHostId).setVmName(vmName).setNumericId(vmQuark).setType(Type.VM).build();
                    vmEntry.sortChildren(fComparator);

                    groupEntry.addChild(vmEntry);
                    entryMap.put(vmHostId, vmEntry);
                } else {
                    vmEntry.updateEndTime(endTime);
                }

                /* Display an entry for each of its CPUs */
                for (Integer vCpuQuark : ssq.getSubAttributes(vmQuark, false)) {
                    String vcpuId = ssq.getAttributeName(vCpuQuark);
                    VirtualMachineViewEntry vcpuEntry = entryMap.get(vmHostId + vcpuId);
                    if (vcpuEntry == null) {
                        vcpuEntry = new VirtualMachineViewEntry.VmEntryBuilder(vcpuId, startTime, endTime, vmExperiment).setId(vcpuId).setVmName(vmName).setNumericId(vCpuQuark).setType(Type.VCPU).build();

                        vmEntry.addChild(vcpuEntry);
                        entryMap.put(vmHostId + vcpuId, vcpuEntry);
                    } else {
                        vcpuEntry.updateEndTime(endTime);
                    }

                }

                /* Add the entries for the threads */
                buildThreadEntries(vmEntry, entryMap, startTime, endTime);
            }
        } catch (AttributeNotFoundException e) {
            /*
             * The attribute may not exist yet if the state system is being
             * built
             */
        } catch (TimeRangeException | StateValueTypeException e) {
            Activator.getDefault().logError("VirtualMachineView: error building event list", e); //$NON-NLS-1$
        }
    }

    private static void buildThreadEntries(VirtualMachineViewEntry vmEntry, Map<String, VirtualMachineViewEntry> entryMap, long startTime, long endTime) {
        String vmHostId = vmEntry.getId();
        VirtualMachineExperiment vmExperiment = vmEntry.getExperiment();

        /*
         * Get the LTTng Kernel analysis module from the corresponding trace
         */
        KernelAnalysisModule kernelModule = TmfExperimentUtils.getAnalysisModuleOfClassForHost(vmExperiment, vmHostId, KernelAnalysisModule.class);
        if (kernelModule == null) {
            return;
        }

        VirtualMachineViewEntry threadEntry = entryMap.get(vmHostId + NonNullUtils.nullToEmptyString(Messages.VmView_threads));
        if (threadEntry == null) {
            threadEntry = new VirtualMachineViewEntry.VmEntryBuilder(NonNullUtils.nullToEmptyString(Messages.VmView_threads), startTime, endTime, vmExperiment).build();
            entryMap.put(vmHostId + NonNullUtils.nullToEmptyString(Messages.VmView_threads), threadEntry);
            vmEntry.addChild(threadEntry);
        } else {
            threadEntry.updateEndTime(endTime);
        }

        String vmName = vmEntry.getVmName();
        if (vmName == null) {
            return;
        }

        /*
         * Display an entry for each thread.
         *
         * For each interval that is in a running status, intersect with the
         * status of the virtual CPU it is currently running on
         */
        Collection<Integer> threadIds = KernelThreadInformationProvider.getThreadIds(kernelModule);
        for (Integer threadId : threadIds) {
            if (threadId == -1) {
                continue;
            }
            VirtualMachineViewEntry oneThreadEntry = entryMap.get(vmHostId + ':' + threadId);
            if (oneThreadEntry != null) {
                oneThreadEntry.updateEndTime(endTime);
                continue;
            }
            /*
             * FIXME: Only add threads that are active during the trace
             */
            String threadName = KernelThreadInformationProvider.getExecutableName(kernelModule, threadId);
            String tidString = threadId.toString();
            threadName = (threadName != null) ? tidString + ':' + ' ' + threadName : tidString;
            oneThreadEntry = new VirtualMachineViewEntry.VmEntryBuilder(threadName, startTime, endTime, vmExperiment).setId(threadName).setVmName(vmName).setNumericId(threadId).setType(Type.THREAD).build();

            threadEntry.addChild(oneThreadEntry);
            entryMap.put(vmHostId + ':' + threadId, oneThreadEntry);
        }

    }

    private void buildEntryEventLists(List<@NonNull VirtualMachineViewEntry> entryList, ITmfStateSystem ssq, long startTime, long endTime, IProgressMonitor monitor) {
        for (VirtualMachineViewEntry entry : entryList) {
            if (monitor.isCanceled()) {
                return;
            }
            buildEntryEventList(entry, ssq, startTime, endTime, monitor);
        }
    }

    private void buildEntryEventList(TimeGraphEntry entry, ITmfStateSystem ssq, long start, long end, IProgressMonitor monitor) {
        if (start < entry.getEndTime() && end > entry.getStartTime()) {

            long startTime = Math.max(start, entry.getStartTime());
            long endTime = Math.min(end + 1, entry.getEndTime());
            long resolution = Math.max(1, (end - ssq.getStartTime()) / getDisplayWidth());
            List<ITimeEvent> eventList = getEventList(entry, startTime, endTime, resolution, monitor);
            entry.setEventList(eventList);
            redraw();
            for (ITimeGraphEntry child : entry.getChildren()) {
                if (!(child instanceof TimeGraphEntry)) {
                    continue;
                }
                if (monitor.isCanceled()) {
                    return;
                }
                buildEntryEventList((TimeGraphEntry) child, ssq, start, end, monitor);
            }
        }
    }

    @Override
    protected @Nullable List<ITimeEvent> getEventList(TimeGraphEntry entry,
            long startTime, long endTime, long resolution,
            IProgressMonitor monitor) {
        if (!(entry instanceof VirtualMachineViewEntry)) {
            return null;
        }
        if (monitor.isCanceled()) {
            return null;
        }

        VirtualMachineViewEntry vmEntry = (VirtualMachineViewEntry) entry;

        switch (vmEntry.getType()) {
        case THREAD: {
            return getThreadEventList(vmEntry, endTime, monitor);
        }
        case VCPU: {
            return getVcpuEventList(vmEntry, startTime, endTime, resolution, monitor);
        }
        case VM: {
            VirtualMachineExperiment experiment = vmEntry.getExperiment();
            VirtualMachineCpuAnalysis vmAnalysis = TmfTraceUtils.getAnalysisModuleOfClass(experiment, VirtualMachineCpuAnalysis.class, VirtualMachineCpuAnalysis.ID);
            if (vmAnalysis == null) {
                break;
            }
            Multimap<Integer, ITmfStateInterval> updatedThreadIntervals = vmAnalysis.getUpdatedThreadIntervals(vmEntry.getNumericId(), startTime, endTime, resolution, monitor);
            vmEntry.setThreadIntervals(updatedThreadIntervals);
        }
            break;
        case NULL:
            /* These entry types are not used in this view */
            break;
        default:
            break;
        }

        return null;
    }

    private static @Nullable List<@NonNull ITimeEvent> getVcpuEventList(VirtualMachineViewEntry vmEntry, long startTime, long endTime, long resolution, IProgressMonitor monitor) {
        List<ITimeEvent> eventList = null;
        try {
            int quark = vmEntry.getNumericId();

            ITmfStateSystem ssq = TmfStateSystemAnalysisModule.getStateSystem(vmEntry.getExperiment(), VirtualMachineCpuAnalysis.ID);
            if (ssq == null) {
                return Collections.EMPTY_LIST;
            }
            final long realStart = Math.max(startTime, ssq.getStartTime());
            final long realEnd = Math.min(endTime, ssq.getCurrentEndTime() + 1);
            if (realEnd <= realStart) {
                return Collections.EMPTY_LIST;
            }
            quark = ssq.getQuarkRelative(quark, VmAttributes.STATUS);
            List<ITmfStateInterval> statusIntervals = StateSystemUtils.queryHistoryRange(ssq, quark, realStart, realEnd - 1, resolution, monitor);

            eventList = parseIntervalsForEvents(vmEntry, statusIntervals, endTime, monitor);
        } catch (AttributeNotFoundException | TimeRangeException | StateValueTypeException e) {
            Activator.getDefault().logError("Error getting event list", e); //$NON-NLS-1$
        } catch (StateSystemDisposedException e) {
            /* Ignored */
        }
        return eventList;
    }

    private static @Nullable List<@NonNull ITimeEvent> getThreadEventList(VirtualMachineViewEntry vmEntry, long endTime, IProgressMonitor monitor) {
        List<ITimeEvent> eventList = null;
        Collection<ITmfStateInterval> threadIntervals = getThreadIntervalsForEntry(vmEntry);

        if (threadIntervals != null) {
            /*
             * FIXME: I think the key for the alpha bug when alpha overlaps
             * multiple events is around here
             *
             * Hint by Patrick: "The problem is that the thread intervals
             * are sorted by start time, and drawn in that order.
             *
             * Given the intervals: Blue [0,10] Alpha [5,15] Red [10,20]
             *
             * Blue is drawn, then Alpha makes DarkBlue from [5,10] and
             * DarkBackground from [10,15], then Red is drawn over [10,20],
             * overwriting the DarkBackground. There is no DarkRed.
             *
             * For this to work you would have to draw all real states
             * first, then all alpha states second.
             *
             * I think this would also have the side-effect that the find
             * item used for tool tips would always find the real event and
             * never the alpha event. This might be what we want. Right now
             * the tool tip has State: (multiple).
             *
             * But using the Next Event button, we would skip to the next
             * real event and not at the preemption event. Maybe not what we
             * want.
             *
             * Maybe what we need is separate thread interval events:
             *
             * Blue [0,5] Preempted Blue [5,10] Preempted Red [10,15] Red
             * [15,20]...
             *
             * The preempted events would have the real state value, but
             * with a flag for alpha to be used in the postDrawEvent."
             */
            eventList = parseIntervalsForEvents(vmEntry, threadIntervals, endTime, monitor);
        }
        return eventList;
    }

    private static @Nullable List<@NonNull ITimeEvent> parseIntervalsForEvents(VirtualMachineViewEntry vmEntry, Collection<@NonNull ITmfStateInterval> intervals, long endTime, IProgressMonitor monitor) {
        List<ITimeEvent> eventList = new ArrayList<>(intervals.size());
        long lastEndTime = -1;
        for (ITmfStateInterval interval : intervals) {
            if (monitor.isCanceled()) {
                return null;
            }

            long time = interval.getStartTime();
            long duration = interval.getEndTime() - time + 1;
            if (!interval.getStateValue().isNull()) {
                int status = interval.getStateValue().unboxInt();
                if (lastEndTime != time && lastEndTime != -1) {
                    eventList.add(new TimeEvent(vmEntry, lastEndTime, time - lastEndTime));
                }
                eventList.add(new TimeEvent(vmEntry, time, duration, status));
            } else if (lastEndTime == -1 || time + duration >= endTime) {
                /* add null event if it intersects the start or end time */
                eventList.add(new NullTimeEvent(vmEntry, time, duration));
            }
            lastEndTime = time + duration;
        }

        return eventList;
    }

    private static @Nullable Collection<@NonNull ITmfStateInterval> getThreadIntervalsForEntry(VirtualMachineViewEntry vmEntry) {
        Collection<ITmfStateInterval> threadIntervals = null;

        /*
         * The parent VM entry will contain the thread intervals for all
         * threads. Just take the list from there
         */
        ITimeGraphEntry parent = vmEntry.getParent();
        while (threadIntervals == null && parent != null) {
            if (parent instanceof VirtualMachineViewEntry) {
                threadIntervals = ((VirtualMachineViewEntry) parent).getThreadIntervals(vmEntry.getNumericId());
            }
            if (parent instanceof TimeGraphEntry) {
                parent = ((TimeGraphEntry) parent).getParent();
            }
        }
        return threadIntervals;
    }

    @Override
    protected Iterable<ITmfTrace> getTracesToBuild(@Nullable ITmfTrace trace) {
        if (trace == null) {
            return Collections.EMPTY_SET;
        }
        return Collections.singleton(trace);
    }

}
