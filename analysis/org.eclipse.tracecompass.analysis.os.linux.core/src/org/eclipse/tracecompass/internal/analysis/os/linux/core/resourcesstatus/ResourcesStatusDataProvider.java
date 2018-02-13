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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
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
     * Loader to load named states (EXEC_NAME or SYSCALL_NAME) from the CPU status
     * interval. Needs to find the current running thread from the CPU and resolve
     * the EXEC_NAME or SYSCALL_NAME from that thread's sub-attributes.
     */
    private final CacheLoader<ITmfStateInterval , @Nullable TimeGraphState> fLoader = new CacheLoader<ITmfStateInterval, @Nullable TimeGraphState>() {
        @Override
        public @Nullable TimeGraphState load(ITmfStateInterval interval) throws StateSystemDisposedException {
            ITmfStateSystem ss = getAnalysisModule().getStateSystem();
            if (ss == null) {
                return null;
            }

            long startTime = interval.getStartTime();
            long duration = interval.getEndTime() - startTime + 1;
            int status = interval.getStateValue().unboxInt();

            int currentThreadQuark = ss.optQuarkRelative(interval.getAttribute(), Attributes.CURRENT_THREAD);
            if (currentThreadQuark == ITmfStateSystem.INVALID_ATTRIBUTE) {
                return new TimeGraphState(startTime, duration, status);
            }

            String attribute;
            Function<String, String> trim = Function.identity();
            if (status == StateValues.CPU_STATUS_RUN_USERMODE) {
                attribute = Attributes.EXEC_NAME;
            } else if (status == StateValues.CPU_STATUS_RUN_SYSCALL) {
                attribute = Attributes.SYSTEM_CALL;
                /*
                 * Remove the "sys_" or "syscall_entry_" or similar from what we draw in the
                 * rectangle. This depends on the trace's event layout.
                 */
                trim = fSyscallTrim;
            } else {
                // no reason to be here
                return null;
            }

            long time = startTime;
            while (time < interval.getEndTime()) {
                ITmfStateInterval tidInterval = ss.querySingleState(time, currentThreadQuark);
                time = Long.max(tidInterval.getStartTime(), time);
                Object value = tidInterval.getValue();
                if (value instanceof Integer) {
                    int currentThreadId = (int) value;
                    int quark = ss.optQuarkAbsolute(Attributes.THREADS, Integer.toString(currentThreadId), attribute);
                    if (quark != ITmfStateSystem.INVALID_ATTRIBUTE) {
                        ITmfStateInterval nameInterval = ss.querySingleState(time, quark);
                        Object label = nameInterval.getValue();
                        if (label instanceof String) {
                            return new TimeGraphState(startTime, duration, status, trim.apply((String) label));
                        }
                    }
                }
                // make sure next time is at least at the next pixel
                time = tidInterval.getEndTime() + 1;
            }
            return new TimeGraphState(startTime, duration, status);
        }
    };

    private final LoadingCache<ITmfStateInterval, @Nullable TimeGraphState> fTimeEventNames = CacheBuilder.newBuilder()
            .maximumSize(1000).build(fLoader);

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
            ResourcesEntryModel cpuEntry = new ResourcesEntryModel(getId(cpuQuark), traceId, cpuName, start, end, cpu, Type.CPU);
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
                    resourceName, startTime, endTime, resourceId, type));

            /*
             * Search for the aggregate interrupt entry in the list. If it does not
             * exist yet, create it.
             */
            String aggregateIrqtype = type == Type.IRQ ? Attributes.IRQS : Attributes.SOFT_IRQS;
            long aggregateId = getId(ssq.optQuarkAbsolute(aggregateIrqtype, resourceName));
            if (!Iterables.any(builder, entry -> entry.getId() == aggregateId)) {
                builder.add(new ResourcesEntryModel(aggregateId, cpuEntry.getParentId(),
                        aggregateIrqtype, startTime, endTime, resourceId, type));
            }

            /*
             * This reaches the limit of the contract, each entry model is supposed to have
             * a distinct ID. On the other hand, this is the same entry under a CPU and an
             * aggregate.
             */
            builder.add(new ResourcesEntryModel(irqId, aggregateId, cpuEntry.getName(),
                    startTime, endTime, cpuEntry.getResourceId(), Type.CPU));
        }
    }

    @Override
    public List<ITimeGraphRowModel> getRowModel(ITmfStateSystem ss, SelectionTimeQueryFilter filter, @Nullable IProgressMonitor monitor)
            throws StateSystemDisposedException {

        TreeMultimap<Integer, ITmfStateInterval> intervals = TreeMultimap.create(Comparator.naturalOrder(),
                Comparator.comparing(ITmfStateInterval::getStartTime));
        Map<@NonNull Long, @NonNull Integer> entries = getSelectedEntries(filter);
        Collection<Long> times = getTimes(filter, ss.getStartTime(), ss.getCurrentEndTime());
        /* Do the actual query */
        for (ITmfStateInterval interval : ss.query2D(entries.values(), times)) {
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
                eventList.add(createTimeGraphState(interval));
            }
            rows.add(new TimeGraphRowModel(entry.getKey(), eventList));
        }
        if (!ss.waitUntilBuilt(0)) {
            /*
             * Avoid caching incomplete results from state system.
             */
            fTimeEventNames.invalidateAll();
        }
        return rows;
    }

    private ITimeGraphState createTimeGraphState(ITmfStateInterval interval) {
        long startTime = interval.getStartTime();
        long duration = interval.getEndTime() - startTime + 1;
        Object status = interval.getValue();
        if (status instanceof Integer) {
            int s = (int) status;
            if (s == StateValues.CPU_STATUS_RUN_USERMODE || s == StateValues.CPU_STATUS_RUN_SYSCALL) {
                return fTimeEventNames.getUnchecked(interval);
            }
            return new TimeGraphState(startTime, duration, s);
        }
        return new TimeGraphState(startTime, duration, Integer.MIN_VALUE);
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
