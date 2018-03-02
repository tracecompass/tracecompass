/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.resourcesstatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.function.Function;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.StateValues;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelTrace;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.Attributes;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.resourcesstatus.ResourcesEntryModel.Type;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph.AbstractTimeGraphDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph.ITimeGraphArrow;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph.ITimeGraphRowModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph.ITimeGraphState;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph.TimeGraphRowModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph.TimeGraphState;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.TreeMultimap;
import com.google.common.primitives.Ints;

/**
 * Resources status data provider, used by the Resources view for example.
 *
 * @author Loic Prieur-Drevon
 */
@SuppressWarnings("restriction")
public class ResourcesStatusDataProvider extends AbstractTimeGraphDataProvider<@NonNull KernelAnalysisModule, @NonNull ResourcesEntryModel> {

    /**
     * Extension point ID.
     */
    public static final @NonNull String ID = "org.eclipse.tracecompass.internal.analysis.os.linux.core.threadstatus.ResourcesStatusDataProvider"; //$NON-NLS-1$

    private static final String WILDCARD = "*"; //$NON-NLS-1$

    /**
     * Remove the "sys_" or "syscall_entry_" or similar from what we draw in the
     * rectangle. This depends on the trace's event layout.
     */
    private final Function<@NonNull String, @NonNull String> fSyscallTrim;

    /**
     * Constructor
     *
     * @param trace
     *            The trace for which this provider will be built.
     * @param module
     *            the {@link KernelAnalysisModule} to access the underlying
     *            {@link ITmfStateSystem}
     */
    protected ResourcesStatusDataProvider(@NonNull ITmfTrace trace, @NonNull KernelAnalysisModule module) {
        super(trace, module);
        if (trace instanceof IKernelTrace) {
            IKernelAnalysisEventLayout layout = ((IKernelTrace) trace).getKernelEventLayout();
            int beginIndex = layout.eventSyscallEntryPrefix().length();
            fSyscallTrim = sysCall -> sysCall.substring(beginIndex);
        } else {
            fSyscallTrim = Function.identity();
        }
    }

    @Override
    protected @NonNull List<@NonNull ResourcesEntryModel> getTree(@NonNull ITmfStateSystem ss,
            @NonNull TimeQueryFilter filter, @Nullable IProgressMonitor monitor) throws StateSystemDisposedException {

        long start = ss.getStartTime();
        long end = ss.getCurrentEndTime();

        @NonNull List<@NonNull ResourcesEntryModel> builder = new ArrayList<>();

        long traceId = getId(ITmfStateSystem.ROOT_ATTRIBUTE);
        ResourcesEntryModel resourcesEntryModel = new ResourcesEntryModel(traceId, -1, getTrace().getName(), start, end, -1, ResourcesEntryModel.Type.TRACE);
        builder.add(resourcesEntryModel);

        for (Integer cpuQuark : ss.getQuarks(Attributes.CPUS, WILDCARD)) {
            final @NonNull String cpuName = ss.getAttributeName(cpuQuark);
            int cpu = Integer.parseInt(cpuName);
            ResourcesEntryModel cpuEntry = new ResourcesEntryModel(getId(cpuQuark), traceId, computeEntryName(Type.CPU, cpu), start, end, cpu, Type.CPU);
            builder.add(cpuEntry);

            List<Integer> irqQuarks = ss.getQuarks(cpuQuark, Attributes.IRQS, WILDCARD);
            createInterrupt(ss, start, end, cpuEntry, irqQuarks, Type.IRQ, builder);

            List<Integer> softIrqQuarks = ss.getQuarks(cpuQuark, Attributes.SOFT_IRQS, WILDCARD);
            createInterrupt(ss, start, end, cpuEntry, softIrqQuarks, Type.SOFT_IRQ, builder);
        }

        return ImmutableList.copyOf(builder);
    }

    /**
     * Create and add execution contexts to a cpu entry. Also creates an aggregate
     * entry in the root trace entry. The execution context is basically what the
     * cpu is doing in its execution stack. It can be in an IRQ, Soft IRQ. MCEs,
     * NMIs, Userland and Kernel execution is not yet supported.
     *
     * @param ssq
     *            the state system
     * @param startTime
     *            the start time in nanoseconds
     * @param endTime
     *            the end time in nanoseconds
     * @param cpuEntry
     *            the cpu timegraph entry (the entry under the trace entry
     * @param irqQuarks
     *            the quarks to add to cpu entry
     * @param type
     *            the type of entry being added
     * @param builder
     *            list of entries to return
     */
    private void createInterrupt(final ITmfStateSystem ssq, long startTime, long endTime,
            ResourcesEntryModel cpuEntry, List<Integer> irqQuarks, Type type, List<ResourcesEntryModel> builder) {
        for (Integer irqQuark : irqQuarks) {
            String resourceName = ssq.getAttributeName(irqQuark);
            int resourceId = Integer.parseInt(resourceName);
            long irqId = getId(irqQuark);

            builder.add(new ResourcesEntryModel(irqId, cpuEntry.getId(),
                    computeEntryName(type, resourceId), startTime, endTime, resourceId, type));

            /*
             * Search for the aggregate interrupt entry in the list. If it does not
             * exist yet, create it.
             */
            String aggregateIrqtype = type == Type.IRQ ? Attributes.IRQS : Attributes.SOFT_IRQS;
            long aggregateId = getId(ssq.optQuarkAbsolute(aggregateIrqtype, resourceName));
            if (!Iterables.any(builder, entry -> entry.getId() == aggregateId)) {
                builder.add(new ResourcesEntryModel(aggregateId, cpuEntry.getParentId(),
                        computeEntryName(type, resourceId),
                        startTime, endTime, resourceId, type));
            }

            /*
             * This reaches the limit of the contract, each entry model is supposed to have
             * a distinct ID. On the other hand, this is the same entry under a CPU and an
             * aggregate. Name this entry like a CPU but give it the IRQ type
             */
            builder.add(new ResourcesEntryModel(irqId, aggregateId,
                    computeEntryName(Type.CPU, cpuEntry.getResourceId()),
                    startTime, endTime, cpuEntry.getResourceId(), type));
        }
    }

    private static @NonNull String computeEntryName(Type type, int id) {
        if (type == Type.SOFT_IRQ) {
            return type.toString() + ' ' + id + ' ' + SoftIrqLabelProvider.getSoftIrq(id);
        }
        return type.toString() + ' ' + id;
    }

    @Override
    public List<ITimeGraphRowModel> getRowModel(ITmfStateSystem ss, SelectionTimeQueryFilter filter, @Nullable IProgressMonitor monitor)
            throws StateSystemDisposedException {

        TreeMultimap<Integer, ITmfStateInterval> intervals = TreeMultimap.create(Comparator.naturalOrder(),
                Comparator.comparing(ITmfStateInterval::getStartTime));
        Map<@NonNull Long, @NonNull Integer> entries = getSelectedEntries(filter);
        Collection<Long> times = getTimes(filter, ss.getStartTime(), ss.getCurrentEndTime());
        /* Do the actual query */
        Collection<@NonNull Integer> quarks = addThreadStatus(ss, entries.values());
        for (ITmfStateInterval interval : ss.query2D(quarks, times)) {
            if (monitor != null && monitor.isCanceled()) {
                return null;
            }
            intervals.put(interval.getAttribute(), interval);
        }

        List<ITimeGraphRowModel> rows = new ArrayList<>();

        for (Map.Entry<Long, Integer> entry : entries.entrySet()) {
            if (monitor != null && monitor.isCanceled()) {
                return null;
            }

            List<ITimeGraphState> eventList = new ArrayList<>();
            for (ITmfStateInterval interval : intervals.get(entry.getValue())) {
                long startTime = interval.getStartTime();
                long duration = interval.getEndTime() - startTime + 1;
                Object status = interval.getValue();
                if (status instanceof Integer) {
                    int s = (int) status;
                    int currentThreadQuark = ss.optQuarkRelative(interval.getAttribute(), Attributes.CURRENT_THREAD);
                    if (s == StateValues.CPU_STATUS_RUN_SYSCALL) {
                        eventList.add(getSyscall(ss, interval, intervals.get(currentThreadQuark)));
                    } else if (s == StateValues.CPU_STATUS_RUN_USERMODE) {
                        // add events for all the sampled current threads.
                        eventList.addAll(getCurrentThreads(ss, interval, intervals.get(currentThreadQuark)));
                    } else {
                        eventList.add(new TimeGraphState(startTime, duration, s));
                    }
                } else {
                    eventList.add(new TimeGraphState(startTime, duration, Integer.MIN_VALUE));
                }
            }
            rows.add(new TimeGraphRowModel(entry.getKey(), eventList));
        }
        return rows;
    }

    /**
     * Add the quarks for the thread status attribute, for all the CPU quarks in
     * values
     *
     * @param ss
     *            backing state system
     * @param values
     *            original quarks
     * @return set containing values, and Current Thread attributes for all the
     *         quarks
     */
    private static @NonNull Set<@NonNull Integer> addThreadStatus(@NonNull ITmfStateSystem ss, @NonNull Collection<@NonNull Integer> values) {
        Set<@NonNull Integer> set = new HashSet<>(values);
        for (Integer quark : values) {
            int parentAttributeQuark = ss.getParentAttributeQuark(quark);
            if (ss.getAttributeName(parentAttributeQuark).equals(Attributes.CPUS)) {
                int threadStatus = ss.optQuarkRelative(quark, Attributes.CURRENT_THREAD);
                if (threadStatus != ITmfStateSystem.INVALID_ATTRIBUTE) {
                    set.add(threadStatus);
                }
            }
        }
        return set;
    }

    /**
     * Get a list of all the current threads over the duration of the current
     * usermode interval, as several threads can be scheduled over that interval
     *
     * @param ss
     *            backing state system
     * @param userModeInterval
     *            interval representing the CPUs current user mode state.
     * @param currentThreadIntervals
     *            Current Threads intervals for the same CPU with the time query
     *            filter sampling
     * @return a List of intervals with the current thread name label.
     */
    private static List<TimeGraphState> getCurrentThreads(@NonNull ITmfStateSystem ss, ITmfStateInterval userModeInterval,
            @NonNull NavigableSet<ITmfStateInterval> currentThreadIntervals) throws StateSystemDisposedException {
        List<TimeGraphState> list = new ArrayList<>();
        for (ITmfStateInterval currentThread : currentThreadIntervals) {
            // filter the current thread intervals which overlap the usermode interval
            if (currentThread.getStartTime() <= userModeInterval.getEndTime()
                    && currentThread.getEndTime() >= userModeInterval.getStartTime()) {
                long start = Long.max(userModeInterval.getStartTime(), currentThread.getStartTime());
                long end = Long.min(userModeInterval.getEndTime(), currentThread.getEndTime());
                long duration = end - start + 1;
                Object tid = currentThread.getValue();
                if (tid instanceof Integer) {
                    int execNameQuark = ss.optQuarkAbsolute(Attributes.THREADS, String.valueOf(tid), Attributes.EXEC_NAME);
                    if (execNameQuark != ITmfStateSystem.INVALID_ATTRIBUTE) {
                        Object currentThreadName = ss.querySingleState(currentThread.getEndTime(), execNameQuark).getValue();
                        if (currentThreadName instanceof String) {
                            list.add(new TimeGraphState(start, duration, StateValues.CPU_STATUS_RUN_USERMODE, (String) currentThreadName));
                            continue;
                        }
                    }
                }
                list.add(new TimeGraphState(start, duration, StateValues.CPU_STATUS_RUN_USERMODE));
            }
        }
        return list;
    }

    /**
     * Get a {@link TimeGraphState} with the syscall name.
     *
     * @param ss
     *            backing state system
     * @param interval
     *            current userMode interval
     * @param currentThreadIntervals
     *            sampled current thread intervals for the CPU
     * @return a {@link TimeGraphState} with the System Call name if we found it
     */
    private ITimeGraphState getSyscall(@NonNull ITmfStateSystem ss, ITmfStateInterval interval,
            @NonNull NavigableSet<ITmfStateInterval> currentThreadIntervals) throws StateSystemDisposedException {
        long startTime = interval.getStartTime();
        long duration = interval.getEndTime() - startTime + 1;
        int status = StateValues.CPU_STATUS_RUN_SYSCALL;

        ITmfStateInterval tidInterval = currentThreadIntervals.floor(interval);
        if (tidInterval != null) {
            Object value = tidInterval.getValue();
            if (value instanceof Integer) {
                int currentThreadId = (int) value;
                int quark = ss.optQuarkAbsolute(Attributes.THREADS, Integer.toString(currentThreadId), Attributes.SYSTEM_CALL);
                if (quark != ITmfStateSystem.INVALID_ATTRIBUTE) {
                    ITmfStateInterval nameInterval = ss.querySingleState(startTime, quark);
                    Object syscallName = nameInterval.getValue();
                    if (syscallName instanceof String) {
                        String label = fSyscallTrim.apply((String) syscallName);
                        return new TimeGraphState(startTime, duration, status, label);
                    }
                }
            }
        }
        return new TimeGraphState(startTime, duration, status);
    }

    @Override
    public TmfModelResponse<List<ITimeGraphArrow>> fetchArrows(TimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        return new TmfModelResponse<>(null, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }

    @Override
    public @NonNull String getId() {
        return ID;
    }

    @Override
    public TmfModelResponse<Map<String, String>> fetchTooltip(@NonNull SelectionTimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        ITmfStateSystem ss = getAnalysisModule().getStateSystem();
        Set<@NonNull Integer> quarks = getSelectedQuarks(filter);
        long start = filter.getStart();
        if (ss == null || quarks.size() != 1 || !getAnalysisModule().isQueryable(start)) {
            /*
             * We need the ss to query, we should only be querying one attribute and the
             * query times should be valid.
             */
            return new TmfModelResponse<>(null, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
        }
        int quark = quarks.iterator().next();

        // assert that it is a CPU quark
        String attributeName = ss.getAttributeName(quark);
        Integer cpuNumber = Ints.tryParse(attributeName);
        String parent = ss.getAttributeName(ss.getParentAttributeQuark(quark));
        if (!parent.equals(Attributes.CPUS) || cpuNumber == null) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
        }

        try {
            Map<String, String> retMap = new LinkedHashMap<>(1);
            List<ITmfStateInterval> full = ss.queryFullState(start);
            Object object = full.get(quark).getValue();
            if (object instanceof Integer) {
                int status = (int) object;
                if (status == StateValues.CPU_STATUS_IRQ) {
                    putIrq(ss, attributeName, retMap, full, Attributes.IRQS);
                } else if (status == StateValues.CPU_STATUS_SOFTIRQ) {
                    putIrq(ss, attributeName, retMap, full, Attributes.SOFT_IRQS);
                } else if (status == StateValues.CPU_STATUS_RUN_USERMODE || status == StateValues.CPU_STATUS_RUN_SYSCALL) {
                    putCpuTooltip(ss, attributeName, retMap, full, status);
                }
            }
            return new TmfModelResponse<>(retMap, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
        } catch (StateSystemDisposedException e) {
        }

        return new TmfModelResponse<>(null, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }

    private static void putIrq(ITmfStateSystem ss, String attributeName,
            Map<String, String> retMap, List<ITmfStateInterval> full, String irqs) {
        for (int irqQuark : ss.getQuarks(Attributes.CPUS, attributeName, irqs, "*")) { //$NON-NLS-1$
            ITmfStateInterval interval = full.get(irqQuark);
            if (interval.getValue() != null) {
                String irq = ss.getAttributeName(irqQuark);
                retMap.put(irqs, irq);
                return;
            }
        }
    }

    private static void putCpuTooltip(ITmfStateSystem ss, String attributeName,
            Map<String, String> retMap, List<ITmfStateInterval> full, int status) {
        int currentThreadQuark = ss.optQuarkAbsolute(Attributes.CPUS, attributeName, Attributes.CURRENT_THREAD);
        if (currentThreadQuark == ITmfStateSystem.INVALID_ATTRIBUTE) {
            return;
        }

        Object currentThreadObject = full.get(currentThreadQuark).getValue();
        if (currentThreadObject instanceof Number) {
            String currentThread = currentThreadObject.toString();
            retMap.put(Attributes.CURRENT_THREAD, currentThread);

            int execNameQuark = ss.optQuarkAbsolute(Attributes.THREADS, currentThread, Attributes.EXEC_NAME);
            if (execNameQuark != ITmfStateSystem.INVALID_ATTRIBUTE) {
                Object processName = full.get(execNameQuark).getValue();
                if (processName instanceof String) {
                    retMap.put(Attributes.EXEC_NAME, (String) processName);
                }
            }

            int syscallQuark = ss.optQuarkAbsolute(Attributes.THREADS, currentThread, Attributes.SYSTEM_CALL);
            if (status == StateValues.CPU_STATUS_RUN_SYSCALL
                    && syscallQuark != ITmfStateSystem.INVALID_ATTRIBUTE) {
                Object syscall = full.get(syscallQuark).getValue();
                if (syscall instanceof String) {
                    retMap.put(Attributes.SYSTEM_CALL, (String) syscall);
                }
            }
        }
    }

    @Override
    protected boolean isCacheable() {
        return true;
    }

}
