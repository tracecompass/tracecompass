/*******************************************************************************
 * Copyright (c) 2018 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.resourcesstatus;

import java.text.FieldPosition;
import java.text.Format;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
import org.eclipse.osgi.util.NLS;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.StateValues;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelTrace;
import org.eclipse.tracecompass.common.core.format.DecimalUnitFormat;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.Attributes;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.resourcesstatus.ResourcesEntryModel.Type;
import org.eclipse.tracecompass.internal.tmf.core.model.timegraph.AbstractTimeGraphDataProvider;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphArrow;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphRowModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphState;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphRowModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphState;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.TreeMultimap;
import com.google.common.primitives.Ints;

/**
 * Resources status data provider, used by the Resources view for example.
 *
 * @author Loic Prieur-Drevon
 */
public class ResourcesStatusDataProvider extends AbstractTimeGraphDataProvider<@NonNull KernelAnalysisModule, @NonNull ResourcesEntryModel> {

    /**
     * Extension point ID.
     */
    public static final @NonNull String ID = "org.eclipse.tracecompass.internal.analysis.os.linux.core.threadstatus.ResourcesStatusDataProvider"; //$NON-NLS-1$

    private static final String WILDCARD = "*"; //$NON-NLS-1$

    private static final @NonNull String SEPARATOR = ""; //$NON-NLS-1$
    private static final @NonNull Format FREQUENCY_FORMATTER = new DecimalUnitFormat() {

        /**
         *
         */
        private static final long serialVersionUID = 2101980732073309988L;

        @Override
        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            return super.format(obj, toAppendTo, pos).append("Hz"); //$NON-NLS-1$
        }


    };
    private static final long FREQUENCY_MULTIPLIER = 1000;

    /**
     * Remove the "sys_" or "syscall_entry_" or similar from what we draw in the
     * rectangle. This depends on the trace's event layout.
     */
    private final Function<@NonNull String, @NonNull String> fSyscallTrim;

    /** Map of attribute quark to its ResourcesEntryModel type */
    private final HashMap<Integer, Type> fEntryModelTypes = new HashMap<>();

    /** Map of CPU number to its separator entry's model id */
    private final Map<Integer, Long> fSeparatorIds = new HashMap<>();

    /**
     * BiMap of IRQ/SoftIRQ twin model id to quark, the key is the model id of the
     * CPU entry under an aggregate IRQ/SoftIRQ entry, the value is the quark of its
     * twin IRQ/SoftIRQ entry under a CPU. These entries share the same states.
     */
    private final BiMap<Long, Integer> fTwinIdsToQuark = HashBiMap.create();

    private static final Comparator<ITmfStateInterval> CACHE_COMPARATOR = (a, b) -> {
        if (a.getEndTime() < b.getStartTime()) {
            return -1;
        } else if (a.getStartTime() > b.getEndTime()) {
            return 1;
        }
        return 0;
    };

    /** Map of thread id to Exec_name intervals */
    private final TreeMultimap<Integer, ITmfStateInterval> fExecNamesCache = TreeMultimap.create(Integer::compare, CACHE_COMPARATOR);

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

        List<@NonNull ResourcesEntryModel> builder = new ArrayList<>();

        long traceId = getId(ITmfStateSystem.ROOT_ATTRIBUTE);
        ResourcesEntryModel resourcesEntryModel = new ResourcesEntryModel(traceId, -1, getTrace().getName(), start, end, -1, Type.GROUP);
        builder.add(resourcesEntryModel);

        for (Integer cpuQuark : ss.getQuarks(Attributes.CPUS, WILDCARD)) {
            final @NonNull String cpuName = ss.getAttributeName(cpuQuark);
            int cpu = Integer.parseInt(cpuName);

            Integer currentThreadQuark = ss.optQuarkRelative(cpuQuark, Attributes.CURRENT_THREAD);
            if (currentThreadQuark != ITmfStateSystem.INVALID_ATTRIBUTE) {
                ResourcesEntryModel currentThreadEntry = new ResourcesEntryModel(getId(currentThreadQuark), traceId, computeEntryName(Type.CURRENT_THREAD, cpu), start, end, cpu, Type.CURRENT_THREAD);
                builder.add(currentThreadEntry);
                fEntryModelTypes.put(currentThreadQuark, Type.CURRENT_THREAD);
            }

            ResourcesEntryModel cpuEntry = new ResourcesEntryModel(getId(cpuQuark), traceId, computeEntryName(Type.CPU, cpu), start, end, cpu, Type.CPU);
            builder.add(cpuEntry);
            fEntryModelTypes.put(cpuQuark, Type.CPU);

            // Add a line for the frequency if available
            Integer currentFreqQuark = ss.optQuarkRelative(cpuQuark, Attributes.CURRENT_FREQUENCY);
            if (currentFreqQuark != ITmfStateSystem.INVALID_ATTRIBUTE) {
                // Get the minimum and maximum frequencies of the CPU, if avaible. If not, these values won't be used anyway
                long minFrequency = getCpuFrequency(ss, cpuQuark, Attributes.MIN_FREQUENCY);
                long maxFrequency = getCpuFrequency(ss, cpuQuark, Attributes.MAX_FREQUENCY);
                ResourcesEntryModel currentFreqEntry = new ResourcesEntryModelWeighted(getId(currentFreqQuark), traceId, computeEntryName(Type.FREQUENCY, cpu), start, end, cpu, Type.FREQUENCY, minFrequency, maxFrequency);
                builder.add(currentFreqEntry);
                fEntryModelTypes.put(currentFreqQuark, Type.FREQUENCY);
            }

            // Add a separator entry after each CPU entry
            long id = fSeparatorIds.computeIfAbsent(cpu, key -> getEntryId());
            builder.add(new ResourcesEntryModel(id, traceId, SEPARATOR, start, end, cpu, Type.GROUP));

            List<Integer> irqQuarks = ss.getQuarks(cpuQuark, Attributes.IRQS, WILDCARD);
            createInterrupt(ss, start, end, cpuEntry, irqQuarks, Type.IRQ, builder);

            List<Integer> softIrqQuarks = ss.getQuarks(cpuQuark, Attributes.SOFT_IRQS, WILDCARD);
            createInterrupt(ss, start, end, cpuEntry, softIrqQuarks, Type.SOFT_IRQ, builder);
        }

        return ImmutableList.copyOf(builder);
    }

    private static long getCpuFrequency(@NonNull ITmfStateSystem ss, int cpuQuark, @NonNull String freqAttribute) throws StateSystemDisposedException {
        int quark = ss.optQuarkRelative(cpuQuark, freqAttribute);
        if (quark == ITmfStateSystem.INVALID_ATTRIBUTE) {
            // This value will not be used if unavailable, so just return something
            return 1;
        }
        Object value = ss.querySingleState(ss.getStartTime(), quark).getValue();
        // The frequency needs to fit in an int, so divide by 1000
        return value instanceof Long ? ((Long) value).longValue() / FREQUENCY_MULTIPLIER : 1;
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

            fEntryModelTypes.put(irqQuark, type);

            /*
             * Search for the aggregate interrupt entry in the list. If it does not exist
             * yet, create it.
             */
            String aggregateIrqtype = type == Type.IRQ ? Attributes.IRQS : Attributes.SOFT_IRQS;
            int aggregateQuark = ssq.optQuarkAbsolute(aggregateIrqtype, resourceName);
            if (aggregateQuark == ITmfStateSystem.INVALID_ATTRIBUTE) {
                continue;
            }
            long aggregateId = getId(aggregateQuark);
            if (!Iterables.any(builder, entry -> entry.getId() == aggregateId)) {
                builder.add(new ResourcesEntryModel(aggregateId, cpuEntry.getParentId(),
                        computeEntryName(type, resourceId),
                        startTime, endTime, resourceId, type));
            }
            fEntryModelTypes.put(aggregateQuark, type);

            /*
             * Create an IRQ or SOFT_IRQ entry under the aggregate interrupt entry, but name
             * it like a CPU entry. Add the mapping to its twin IRQ/SOFT_IRQ entry's quark
             * under the CPU entry. The twin entries share the same quark.
             */
            long id = fTwinIdsToQuark.inverse().computeIfAbsent(irqQuark, key -> getEntryId());
            builder.add(new ResourcesEntryModel(id, aggregateId,
                    computeEntryName(Type.CPU, cpuEntry.getResourceId()),
                    startTime, endTime, cpuEntry.getResourceId(), type));
        }
    }

    private static @NonNull String computeEntryName(Type type, int id) {
        if (type == Type.SOFT_IRQ) {
            return type.toString() + ' ' + id + ' ' + SoftIrqLabelProvider.getSoftIrq(id);
        } else if (type == Type.CURRENT_THREAD) {
            String threadEntryName = NLS.bind(Messages.ThreadEntry, id);
            if (threadEntryName != null) {
                return threadEntryName;
            }
        } else if (type == Type.CPU) {
            String cpuEntryName = NLS.bind(Messages.CpuEntry, id);
            if (cpuEntryName != null) {
                return cpuEntryName;
            }
        } else if (type == Type.FREQUENCY) {
            String cpuEntryName = NLS.bind(Messages.FrequencyEntry, id);
            if (cpuEntryName != null) {
                return cpuEntryName;
            }
        }
        return type.toString() + ' ' + id;
    }

    @Override
    public List<ITimeGraphRowModel> getRowModel(ITmfStateSystem ss, SelectionTimeQueryFilter filter, @Nullable IProgressMonitor monitor)
            throws StateSystemDisposedException {

        TreeMultimap<Integer, ITmfStateInterval> intervals = TreeMultimap.create(Comparator.naturalOrder(),
                Comparator.comparing(ITmfStateInterval::getStartTime));
        Map<@NonNull Long, @NonNull Integer> idsToQuark = getSelectedEntries(filter);
        /* Add the mapping for twin entries as they are not in the parent class BiMap */
        addTwinIrqIds(filter, idsToQuark);
        Collection<Long> times = getTimes(filter, ss.getStartTime(), ss.getCurrentEndTime());
        /* Do the actual query */
        Collection<@NonNull Integer> quarks = addThreadStatus(ss, idsToQuark.values());
        for (ITmfStateInterval interval : ss.query2D(quarks, times)) {
            if (monitor != null && monitor.isCanceled()) {
                return null;
            }
            intervals.put(interval.getAttribute(), interval);
        }

        List<ITimeGraphRowModel> rows = new ArrayList<>();

        for (Map.Entry<Long, Integer> idToQuark : idsToQuark.entrySet()) {
            if (monitor != null && monitor.isCanceled()) {
                return null;
            }

            List<ITimeGraphState> eventList = new ArrayList<>();
            for (ITmfStateInterval interval : intervals.get(idToQuark.getValue())) {
                long startTime = interval.getStartTime();
                long duration = interval.getEndTime() - startTime + 1;
                Object status = interval.getValue();
                Type type = fEntryModelTypes.get(interval.getAttribute());
                if (status instanceof Integer) {
                    int s = (int) status;
                    int currentThreadQuark = ss.optQuarkRelative(interval.getAttribute(), Attributes.CURRENT_THREAD);
                    if (type == Type.CPU && s == StateValues.CPU_STATUS_RUN_SYSCALL) {
                        eventList.add(getSyscall(ss, interval, intervals.get(currentThreadQuark)));
                    } else if (type == Type.CPU && s == StateValues.CPU_STATUS_RUN_USERMODE) {
                        // add events for all the sampled current threads.
                        eventList.addAll(getCurrentThreads(ss, interval, intervals.get(currentThreadQuark)));
                    } else if (type == Type.CURRENT_THREAD && s != 0) {
                        String execName = null;
                        synchronized (fExecNamesCache) {
                            if (fExecNamesCache.containsEntry(status, interval)) {
                                NavigableSet<ITmfStateInterval> intervalSet = fExecNamesCache.get(s);
                                ITmfStateInterval execNameInterval = intervalSet.ceiling(interval);
                                if (execNameInterval != null && CACHE_COMPARATOR.compare(execNameInterval, interval) == 0) {
                                    execName = (String) execNameInterval.getValue();
                                }
                            } else {
                                int quark = ss.optQuarkAbsolute(Attributes.THREADS, Integer.toString(s), Attributes.EXEC_NAME);
                                if (quark != ITmfStateSystem.INVALID_ATTRIBUTE) {
                                    ITmfStateInterval namedInterval = ss.querySingleState(interval.getEndTime(), quark);
                                    fExecNamesCache.put(s, namedInterval);
                                    execName = (String) namedInterval.getValue();
                                }
                            }
                        }
                        eventList.add(new TimeGraphState(startTime, duration, s, execName != null ? execName + ' ' + '(' + String.valueOf(s) + ')' : String.valueOf(s)));
                    } else if (type == Type.CURRENT_THREAD) {
                        // add null state when current thread is 0
                        eventList.add(new TimeGraphState(startTime, duration, Integer.MIN_VALUE));
                    } else {
                        eventList.add(new TimeGraphState(startTime, duration, s));
                    }
                } else if ((status instanceof Long) && (type == Type.FREQUENCY)) {
                    long s = (long) status;
                    // The value needs to fit in an integer
                    eventList.add(new TimeGraphState(startTime, duration, s/FREQUENCY_MULTIPLIER, String.valueOf(FREQUENCY_FORMATTER.format(s))));
                } else {
                    eventList.add(new TimeGraphState(startTime, duration, Integer.MIN_VALUE));
                }
            }
            rows.add(new TimeGraphRowModel(idToQuark.getKey(), eventList));
        }
        synchronized (fExecNamesCache) {
            fExecNamesCache.clear();
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

    private void addTwinIrqIds(SelectionTimeQueryFilter filter, Map<@NonNull Long, @NonNull Integer> idsToQuark) {
        for (Long id : filter.getSelectedItems()) {
            Integer quark = fTwinIdsToQuark.get(id);
            if (quark != null) {
                /*
                 * The selected id is a twin id. Add it to the idsToQuark map, using its
                 * corresponding twin entry's quark.
                 */
                idsToQuark.put(id, quark);
            }
        }
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
        Collection<@NonNull Integer> quarks = getSelectedEntries(filter).values();

        boolean isACopy = false;
        if (quarks.size() != 1) {
            Map<Long, Integer> selectedEntries = new HashMap<>();
            for (Long selectedItem : filter.getSelectedItems()) {
                Integer quark = fTwinIdsToQuark.get(selectedItem);
                if (quark != null && quark >= 0) {
                    selectedEntries.put(selectedItem, quark);
                    isACopy = true;
                }
            }
            quarks = selectedEntries.values();
        }

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

        if (cpuNumber == null && !(attributeName.equals(Attributes.CURRENT_THREAD) || attributeName.equals(Attributes.CURRENT_FREQUENCY))) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
        }

        try {
            Map<String, String> retMap = new LinkedHashMap<>(1);
            List<ITmfStateInterval> full = ss.queryFullState(start);
            Object object = full.get(quark).getValue();
            if (object instanceof Integer) {
                int status = (int) object;
                if (isACopy) {
                    retMap.put(parent.equals(Attributes.IRQS) ? Messages.ResourcesStatusDataProvider_attributeIrqName :
                                                                Messages.ResourcesStatusDataProvider_attributeSoftIrqName,
                               attributeName);
                } else if (parent.equals(Attributes.IRQS) || parent.equals(Attributes.SOFT_IRQS)) {
                    putCpus(ss, quark, retMap, full, parent, status);
                } else if (status == StateValues.CPU_STATUS_IRQ) {
                    putIrq(ss, attributeName, retMap, full, Attributes.IRQS, status);
                } else if (status == StateValues.CPU_STATUS_SOFTIRQ) {
                    putIrq(ss, attributeName, retMap, full, Attributes.SOFT_IRQS, status);
                } else if (status == StateValues.CPU_STATUS_RUN_USERMODE || status == StateValues.CPU_STATUS_RUN_SYSCALL) {
                    putCpuTooltip(ss, attributeName, retMap, full, status);
                } else if (attributeName.equals(Attributes.CURRENT_THREAD)) {
                    putCurrentThreadTooltip(ss, retMap, full, status);
                }
            } else if (object instanceof Long && attributeName.equals(Attributes.CURRENT_FREQUENCY)) {
                retMap.put("Frequency", FREQUENCY_FORMATTER.format(object)); //$NON-NLS-1$
            }
            return new TmfModelResponse<>(retMap, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
        } catch (StateSystemDisposedException e) {
        }

        return new TmfModelResponse<>(null, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }

    private static void putCpus(ITmfStateSystem ss, int quark, Map<String, String> retMap,
            List<ITmfStateInterval> full, String irqs, int status) {
        List<@NonNull String> cpuList = new ArrayList<>();

        int grandParentAttribute = ss.getParentAttributeQuark(ss.getParentAttributeQuark(quark));
        if (grandParentAttribute != org.eclipse.tracecompass.statesystem.core.ITmfStateSystem.ROOT_ATTRIBUTE
                && ss.getAttributeName(ss.getParentAttributeQuark(ss.getParentAttributeQuark(ss.getParentAttributeQuark(quark)))).equals(Attributes.CPUS)) {
            cpuList.add(ss.getAttributeName(grandParentAttribute));
        } else {
            for (int cpuQuark : ss.getQuarks(Attributes.CPUS, "*", irqs, ss.getAttributeName(quark))) { //$NON-NLS-1$
                ITmfStateInterval interval = full.get(cpuQuark);
                if (interval.getValue() != null) {
                    Object object = full.get(cpuQuark).getValue();
                    if (object instanceof Integer) {
                        int objectStatus = (int) object;
                        if (objectStatus == status) {
                            String cpu = ss.getAttributeName(ss.getParentAttributeQuark(ss.getParentAttributeQuark(cpuQuark)));
                            cpuList.add(cpu);
                        }
                    }
                }
            }
        }

        if (!cpuList.isEmpty()) {
            Collections.sort(cpuList, (s1, s2) -> Integer.compare(Integer.parseInt(s1), Integer.parseInt(s2)));
            retMap.put(Messages.ResourcesStatusDataProvider_attributeCpuName, String.join(", ", cpuList)); //$NON-NLS-1$
        }
    }

    private static void putIrq(ITmfStateSystem ss, String attributeName,
            Map<String, String> retMap, List<ITmfStateInterval> full, String irqs, int status) {

        for (int irqQuark : ss.getQuarks(Attributes.CPUS, attributeName, irqs, "*")) { //$NON-NLS-1$
            ITmfStateInterval interval = full.get(irqQuark);
            if (interval.getValue() != null) {
                Object object = full.get(irqQuark).getValue();
                if (object instanceof Integer) {
                    int objectStatus = (int) object;
                    if (objectStatus == status) {
                        retMap.put(status == StateValues.CPU_STATUS_IRQ ? Messages.ResourcesStatusDataProvider_attributeIrqName :
                                                                          Messages.ResourcesStatusDataProvider_attributeSoftIrqName,
                                   ss.getAttributeName(irqQuark));
                        return;
                    }
                }
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
            retMap.put(Messages.ResourcesStatusDataProvider_attributeTidName, currentThread);

            int execNameQuark = ss.optQuarkAbsolute(Attributes.THREADS, currentThread, Attributes.EXEC_NAME);
            if (execNameQuark != ITmfStateSystem.INVALID_ATTRIBUTE) {
                Object processName = full.get(execNameQuark).getValue();
                if (processName instanceof String) {
                    retMap.put(Messages.ResourcesStatusDataProvider_attributeProcessName, (String) processName);
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

    private static void putCurrentThreadTooltip(ITmfStateSystem ss,
            Map<String, String> retMap, List<ITmfStateInterval> full, int tid) {
        String currentThread = String.valueOf(tid);
        retMap.put(Attributes.CURRENT_THREAD, currentThread);

        int execNameQuark = ss.optQuarkAbsolute(Attributes.THREADS, currentThread, Attributes.EXEC_NAME);
        if (execNameQuark != ITmfStateSystem.INVALID_ATTRIBUTE) {
            Object processName = full.get(execNameQuark).getValue();
            if (processName instanceof String) {
                retMap.put(Attributes.EXEC_NAME, (String) processName);
            }
        }
    }

    @Override
    protected boolean isCacheable() {
        return true;
    }

}
