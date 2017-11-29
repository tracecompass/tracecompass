/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.threadstatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelTrace;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.Attributes;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.StateValues;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.AbstractTmfTraceDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph.ITimeGraphArrow;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph.ITimeGraphDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph.ITimeGraphRowModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph.ITimeGraphState;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph.TimeGraphArrow;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph.TimeGraphRowModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph.TimeGraphState;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils.QuarkIterator;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.interval.TmfStateInterval;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.util.Pair;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultimap;

/**
 * Thread status data provider, used by the Control Flow view for example.
 *
 * @author Simon Delisle
 */
@SuppressWarnings("restriction")
public class ThreadStatusDataProvider extends AbstractTmfTraceDataProvider implements ITimeGraphDataProvider<@NonNull ThreadEntryModel> {

    /**
     * Extension point ID.
     */
    public static final @NonNull String ID = "org.eclipse.tracecompass.internal.analysis.os.linux.core.threadstatus.ThreadStatusDataProvider"; //$NON-NLS-1$

    /**
     * CPU tooltip key.
     */
    public static final @NonNull String CPU = "cpu"; //$NON-NLS-1$

    private static final String WILDCARD = "*"; //$NON-NLS-1$
    private static final Set<Integer> ACTIVE_STATES = ImmutableSet.of(StateValues.PROCESS_STATUS_RUN_USERMODE,
            StateValues.PROCESS_STATUS_RUN_SYSCALL, StateValues.PROCESS_STATUS_INTERRUPTED);
    /**
     * Atomic Long so that every {@link ThreadEntryModel} has a unique ID.
     */
    private static final AtomicLong fAtomicLong = new AtomicLong();

    private final KernelAnalysisModule fModule;
    private final long fTraceId = fAtomicLong.getAndIncrement();

    /**
     * Map of quarks by model ID.
     */
    private final Map<Long, Integer> fQuarkMap = new HashMap<>();

    /**
     * Map of {@link ThreadEntryModel}, key is a pair [threadId, cpuId], only used
     * when building
     */
    private final Map<Pair<Integer, Integer>, ThreadEntryModel.Builder> fBuildMap = new HashMap<>();

    /**
     * Last queried time for fetch entries to avoid querying the same range twice.
     * Set to {@link Long#MAX_VALUE} when the build is complete.
     */
    private long fLastEnd = Long.MIN_VALUE;

    /**
     * Cache threadID to a {@link ThreadEntryModel} for faster lookups
     * when building link list
     */
    private final TreeMultimap<Integer, ThreadEntryModel.Builder> fTidToEntry = TreeMultimap.create(Comparator.naturalOrder(),
            Comparator.comparing(ThreadEntryModel.Builder::getStartTime));

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
     *
     */
    protected ThreadStatusDataProvider(@NonNull ITmfTrace trace, KernelAnalysisModule module) {
        super(trace);
        fModule = module;
        if (trace instanceof IKernelTrace) {
            IKernelAnalysisEventLayout layout = ((IKernelTrace) trace).getKernelEventLayout();
            int beginIndex = layout.eventSyscallEntryPrefix().length();
            fSyscallTrim = sysCall -> sysCall.substring(beginIndex);
        } else {
            fSyscallTrim = Function.identity();
        }
    }

    @Override
    public TmfModelResponse<List<ThreadEntryModel>> fetchTree(TimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        if (fLastEnd == Long.MAX_VALUE) {
            return new TmfModelResponse<>(filter(fTidToEntry, filter), ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
        }

        fModule.waitForInitialization();
        ITmfStateSystem ss = fModule.getStateSystem();
        if (ss == null) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, CommonStatusMessage.ANALYSIS_INITIALIZATION_FAILED);
        }

        /*
         * As we are caching the intermediate result, we only want a single thread to
         * update them.
         */
        synchronized (fBuildMap) {
            boolean complete = ss.waitUntilBuilt(0);
            long end = ss.getCurrentEndTime();
            fLastEnd = Long.max(fLastEnd, ss.getStartTime());

            TreeMultimap<Integer, ITmfStateInterval> execNamesPPIDs = TreeMultimap.create(Comparator.naturalOrder(),
                    Comparator.comparing(ITmfStateInterval::getStartTime));

            /*
             * Create a List with the threads' PPID and EXEC_NAME quarks for the 2D query .
             */
            List<Integer> quarks = new ArrayList<>(ss.getQuarks(Attributes.THREADS, WILDCARD, Attributes.EXEC_NAME));
            quarks.addAll(ss.getQuarks(Attributes.THREADS, WILDCARD, Attributes.PPID));
            try {
                for (ITmfStateInterval interval : ss.query2D(quarks, Long.min(fLastEnd, end), end)) {
                    if (monitor != null && monitor.isCanceled()) {
                        return new TmfModelResponse<>(null, ITmfResponse.Status.CANCELLED, CommonStatusMessage.TASK_CANCELLED);
                    }
                    execNamesPPIDs.put(interval.getAttribute(), interval);
                }
            } catch (TimeRangeException | StateSystemDisposedException e) {
                return new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, e.getClass().getName() + ':' + String.valueOf(e.getMessage()));
            }

            // update the trace Entry.
            fTidToEntry.replaceValues(Integer.MIN_VALUE, Collections.singleton(new ThreadEntryModel.Builder(fTraceId, getTrace().getName(),
                    ss.getStartTime(), end, Integer.MIN_VALUE, Integer.MIN_VALUE)));

            for (Integer threadQuark : ss.getQuarks(Attributes.THREADS, WILDCARD)) {
                String threadAttributeName = ss.getAttributeName(threadQuark);
                Pair<Integer, Integer> entryKey = Attributes.parseThreadAttributeName(threadAttributeName);
                int threadId = entryKey.getFirst();
                if (threadId < 0) {
                    // ignore the 'unknown' (-1) thread
                    continue;
                }

                int execNameQuark = ss.optQuarkRelative(threadQuark, Attributes.EXEC_NAME);
                int ppidQuark = ss.optQuarkRelative(threadQuark, Attributes.PPID);
                NavigableSet<ITmfStateInterval> ppidIntervals = execNamesPPIDs.get(ppidQuark);
                for (ITmfStateInterval execNameInterval : execNamesPPIDs.get(execNameQuark)) {
                    if (monitor != null && monitor.isCanceled()) {
                        return new TmfModelResponse<>(null, ITmfResponse.Status.CANCELLED, CommonStatusMessage.TASK_CANCELLED);
                    }
                    updateEntry(threadQuark, entryKey, ppidIntervals, execNameInterval);
                }
            }

            fLastEnd = end;

            List<ThreadEntryModel> list = filter(fTidToEntry, filter);
            if (complete) {
                fBuildMap.clear();
                fLastEnd = Long.MAX_VALUE;
                return new TmfModelResponse<>(list, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
            }

            return new TmfModelResponse<>(list, ITmfResponse.Status.RUNNING, CommonStatusMessage.RUNNING);
        }
    }

    private void updateEntry(Integer threadQuark, Pair<Integer, Integer> entryKey,
            NavigableSet<ITmfStateInterval> ppidIntervals, ITmfStateInterval execNameInterval) {
        Object value = execNameInterval.getValue();
        if (value == null) {
            fBuildMap.remove(entryKey);
            return;
        }

        ThreadEntryModel.Builder entry = fBuildMap.get(entryKey);
        long startTime = execNameInterval.getStartTime();
        long endTime = execNameInterval.getEndTime() + 1;
        String execName = String.valueOf(value);
        int threadId = entryKey.getFirst();
        int ppid = getPpid(ppidIntervals, endTime);

        if (entry == null) {
            long id = fAtomicLong.getAndIncrement();
            entry = new ThreadEntryModel.Builder(id, execName, startTime, endTime, threadId, ppid);
            fQuarkMap.put(id, threadQuark);
        } else {
            /*
             * Update the name of the entry to the latest execName and the parent thread id
             * to the latest ppid. We must make a copy as the Models are immutable.
             */
            entry.setEndTime(endTime);
            entry.setPpid(ppid);
            entry.setName(execName);
        }
        fBuildMap.put(entryKey, entry);
        fTidToEntry.put(threadId, entry);
    }

    /**
     * Find the parent PID for a given time from a thread's sorted PPID intervals.
     *
     * @param ppidIterator
     *            a navigable set sorted by increasing start time
     * @param t
     *            the time stamp at which we want to know the PPID
     * @return the entry's PPID or -1 if we could not find it.
     */
    private static int getPpid(NavigableSet<ITmfStateInterval> ppidIterator, long t) {
        ITmfStateInterval ppidInterval = ppidIterator.lower(new TmfStateInterval(t, t + 1, 0, 0));
        if (ppidInterval != null) {
            Object o = ppidInterval.getValue();
            if (o instanceof Integer) {
                return (Integer) o;
            }
        }
        return -1;
    }

    /**
     * Filter the threads from a {@link TreeMultimap} according to if they are
     * active or not
     *
     * @param tidToEntry
     *            Threads to filter
     * @param filter
     *            time range to query
     * @return a list of the active threads
     */
    private List<ThreadEntryModel> filter(TreeMultimap<Integer, ThreadEntryModel.Builder> tidToEntry, TimeQueryFilter filter) {
        // avoid putting everything as a child of the swapper thread.
        if (filter.getEnd() == Long.MAX_VALUE) {
            ImmutableList.Builder<ThreadEntryModel> builder = ImmutableList.builder();
            for (ThreadEntryModel.Builder entryBuilder : tidToEntry.values()) {
                builder.add(build(entryBuilder));
            }
            return builder.build();
        }
        ITmfStateSystem ss = fModule.getStateSystem();
        if (ss == null) {
            return Collections.emptyList();
        }

        long start = Long.max(filter.getStart(), ss.getStartTime());
        long end = Long.min(filter.getEnd(), ss.getCurrentEndTime());
        if (start > end) {
            return Collections.emptyList();
        }
        if (filter instanceof SelectionTimeQueryFilter) {
            Set<Long> cpus = Sets.newHashSet(((SelectionTimeQueryFilter) filter).getSelectedItems());
            List<@NonNull Integer> quarks = ss.getQuarks(Attributes.THREADS, WILDCARD, Attributes.CURRENT_CPU_RQ);
            Set<ThreadEntryModel> models = new HashSet<>();
            Map<Integer, Integer> rqToPidCache = new HashMap<>();
            try {
                for (ITmfStateInterval interval : ss.query2D(quarks, Long.max(ss.getStartTime(), start), end)) {
                    Object o = interval.getValue();
                    if (o instanceof Number && cpus.contains(((Number) o).longValue())) {
                        int attribute = interval.getAttribute();
                        int tid = rqToPidCache.computeIfAbsent(attribute, a -> Attributes.parseThreadAttributeName(ss.getAttributeName(ss.getParentAttributeQuark(a))).getFirst());
                        for (ThreadEntryModel.Builder model : tidToEntry.get(tid)) {
                            if (interval.getStartTime() <= model.getEndTime() &&
                                    model.getStartTime() <= interval.getEndTime()) {
                                models.add(build(model));
                            }
                        }
                    }
                }
            } catch (IndexOutOfBoundsException | TimeRangeException e) {
                Activator.logError("Invalid query parameters", e); //$NON-NLS-1$
            } catch (StateSystemDisposedException e) {
                return Collections.emptyList();
            }
            return Lists.newArrayList(models);
        }
        ImmutableList.Builder<ThreadEntryModel> builder = ImmutableList.builder();
        for (ThreadEntryModel.Builder thread : tidToEntry.values()) {
            Integer statusQuark = fQuarkMap.get(thread.getId());
            if (statusQuark == null) {
                continue;
            }
            QuarkIterator iterator = new QuarkIterator(ss, statusQuark, start, end);
            Iterator<Object> values = Iterators.transform(iterator, ITmfStateInterval::getValue);
            if (Iterators.any(values, ACTIVE_STATES::contains)) {
                builder.add(build(thread));
            }
        }
        return builder.build();
    }

    private ThreadEntryModel build(ThreadEntryModel.Builder entryBuilder) {
        if (entryBuilder.getId() == fTraceId) {
            return entryBuilder.build(-1);
        }
        long parentId = entryBuilder.getPpid() > 0 ? findEntry(entryBuilder.getPpid(), entryBuilder.getEndTime()) : fTraceId;
        return entryBuilder.build(parentId);
    }

    @Override
    public TmfModelResponse<List<ITimeGraphRowModel>> fetchRowModel(SelectionTimeQueryFilter filter, IProgressMonitor monitor) {
        ITmfStateSystem ss = fModule.getStateSystem();
        if (ss == null) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, CommonStatusMessage.ANALYSIS_INITIALIZATION_FAILED);
        }

        TreeMultimap<Integer, ITmfStateInterval> intervals = TreeMultimap.create(Comparator.naturalOrder(),
                Comparator.comparing(ITmfStateInterval::getStartTime));
        Collection<Integer> quarks = getQuarks(filter);
        Collection<Integer> stateAndSyscallQuarks = addSyscall(quarks, ss);
        Collection<Long> times = getTimes(ss, filter);
        try {
            /* Do the actual query */
            for (ITmfStateInterval interval : ss.query2D(stateAndSyscallQuarks, times)) {
                if (monitor != null && monitor.isCanceled()) {
                    return new TmfModelResponse<>(null, ITmfResponse.Status.CANCELLED, CommonStatusMessage.TASK_CANCELLED);
                }
                intervals.put(interval.getAttribute(), interval);
            }
        } catch (TimeRangeException | StateSystemDisposedException e) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, String.valueOf(e.getMessage()));
        }

        List<ITimeGraphRowModel> rows = new ArrayList<>();
        for (Integer quark : quarks) {
            String threadAttributeName = ss.getAttributeName(quark);
            Pair<Integer, Integer> tidCpu = Attributes.parseThreadAttributeName(threadAttributeName);
            NavigableSet<ITmfStateInterval> states = intervals.get(quark);
            NavigableSet<ITmfStateInterval> syscalls = intervals.get(ss.optQuarkRelative(quark, Attributes.SYSTEM_CALL));

            // Handle PID reuse
            for (ThreadEntryModel.Builder cfe : fTidToEntry.get(tidCpu.getFirst())) {
                if (monitor != null && monitor.isCanceled()) {
                    return new TmfModelResponse<>(null, ITmfResponse.Status.CANCELLED, CommonStatusMessage.TASK_CANCELLED);
                }

                List<ITimeGraphState> eventList = Lists.newArrayList(Iterables.transform(states, i -> createTimeGraphState(i, syscalls)));
                rows.add(new TimeGraphRowModel(cfe.getId(), eventList));
            }
        }
        return new TmfModelResponse<>(rows, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }

    private Collection<Integer> getQuarks(SelectionTimeQueryFilter filter) {
        Collection<Integer> set = new HashSet<>();
        for (Long id : filter.getSelectedItems()) {
            Integer quark = fQuarkMap.get(id);
            if (quark != null) {
                set.add(quark);
            }
        }
        return set;
    }

    private static Collection<Integer> addSyscall(Collection<Integer> quarks, ITmfStateSystem ss) {
        Collection<Integer> copy = new HashSet<>(quarks);
        for (Integer quark : quarks) {
            int syscallQuark = ss.optQuarkRelative(quark, Attributes.SYSTEM_CALL);
            if (syscallQuark != ITmfStateSystem.INVALID_ATTRIBUTE) {
                copy.add(syscallQuark);
            }
        }
        return copy;
    }

    /**
     * Filter and deduplicate the time stamps for the statesystem
     *
     * @param ss
     *            this provider's {@link ITmfStateSystem}
     * @param filter
     *            the query object
     * @return a Set of filtered timestamps that intersect the state system's time
     *         range
     */
    private static @NonNull Collection<@NonNull Long> getTimes(ITmfStateSystem ss, TimeQueryFilter filter) {
        long start = ss.getStartTime();
        // use a HashSet to deduplicate time stamps
        Collection<@NonNull Long> times = new HashSet<>();
        for (long t : filter.getTimesRequested()) {
            if (t >= start) {
                times.add(t);
            }
        }
        return times;
    }

    private ITimeGraphState createTimeGraphState(ITmfStateInterval interval, NavigableSet<ITmfStateInterval> syscalls) {
        long startTime = interval.getStartTime();
        long duration = interval.getEndTime() - startTime + 1;
        Object status = interval.getValue();
        if (status instanceof Integer) {
            int s = (int) status;
            if (s == StateValues.PROCESS_STATUS_RUN_SYSCALL) {
                // intervals are sorted by start time
                ITmfStateInterval syscall = syscalls.floor(new TmfStateInterval(startTime, startTime + 1, 0, 0));

                if (syscall != null) {
                    Object value = syscall.getValue();
                    if (value instanceof String) {
                        return new TimeGraphState(startTime, duration, s, fSyscallTrim.apply((String) value));
                    }
                }
            }
            return new TimeGraphState(startTime, duration, s);
        }
        return new TimeGraphState(startTime, duration, Integer.MIN_VALUE);
    }

    @Override
    public TmfModelResponse<List<ITimeGraphArrow>> fetchArrows(TimeQueryFilter filter, IProgressMonitor monitor) {
        ITmfStateSystem ss = fModule.getStateSystem();
        if (ss == null) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, CommonStatusMessage.ANALYSIS_INITIALIZATION_FAILED);
        }
        List<@NonNull ITimeGraphArrow> linkList = new ArrayList<>();
        /**
         * MultiMap of the current thread intervals, grouped by CPU, by increasing start
         * time.
         */
        TreeMultimap<Integer, ITmfStateInterval> currentThreadIntervalsMap = TreeMultimap.create(
                Comparator.naturalOrder(),
                Comparator.comparing(ITmfStateInterval::getStartTime));
        List<Integer> quarks = ss.getQuarks(Attributes.CPUS, WILDCARD, Attributes.CURRENT_THREAD);
        Collection<Long> times = getTimes(ss, filter);
        try {
            /* Do the actual query */
            for (ITmfStateInterval interval : ss.query2D(quarks, times)) {
                if (monitor != null && monitor.isCanceled()) {
                    return new TmfModelResponse<>(null, ITmfResponse.Status.CANCELLED, CommonStatusMessage.TASK_CANCELLED);
                }
                currentThreadIntervalsMap.put(interval.getAttribute(), interval);
            }

            /* Get the arrows. */
            for (Collection<ITmfStateInterval> currentThreadIntervals : currentThreadIntervalsMap.asMap().values()) {
                if (monitor != null && monitor.isCanceled()) {
                    return new TmfModelResponse<>(null, ITmfResponse.Status.CANCELLED, CommonStatusMessage.TASK_CANCELLED);
                }
                linkList.addAll(createCpuArrows(ss, (NavigableSet<ITmfStateInterval>) currentThreadIntervals));
            }
        } catch (TimeRangeException | StateSystemDisposedException e) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, String.valueOf(e.getMessage()));
        }
        return new TmfModelResponse<>(linkList, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }

    /**
     * Create the list of arrows to follow the current thread on a CPU
     *
     * @param trace
     *            trace displayed in the view
     * @param entryList
     *            entry list for this trace
     * @param intervals
     *            sorted collection of the current thread intervals for a CPU
     * @return the list of arrows to follow the current thread on a CPU
     * @throws StateSystemDisposedException
     *             If the query is sent after the state system has been disposed
     */
    private List<@NonNull TimeGraphArrow> createCpuArrows(ITmfStateSystem ss, NavigableSet<ITmfStateInterval> intervals)
            throws StateSystemDisposedException {
        if (intervals.isEmpty()) {
            return Collections.emptyList();
        }
        /*
         * Add the previous interval if it is the first query iteration and the first
         * interval has currentThread=0. Add the following interval if the last interval
         * has currentThread=0. These are diagonal arrows crossing the query iteration
         * range.
         */
        ITmfStateInterval first = intervals.first();
        long start = first.getStartTime() - 1;
        if (start >= ss.getStartTime() && Objects.equals(first.getValue(), 0)) {
            intervals.add(ss.querySingleState(start, first.getAttribute()));
        }
        ITmfStateInterval last = intervals.last();
        long end = last.getEndTime() + 1;
        if (end <= ss.getCurrentEndTime() && Objects.equals(last.getValue(), 0)) {
            intervals.add(ss.querySingleState(end, last.getAttribute()));
        }

        List<@NonNull TimeGraphArrow> linkList = new ArrayList<>();
        long prevEnd = 0;
        long lastEnd = 0;
        long prevEntry = -1;
        for (ITmfStateInterval currentThreadInterval : intervals) {
            long time = currentThreadInterval.getStartTime();
            if (time != lastEnd) {
                /*
                 * Don't create links where there are gaps in intervals due to the resolution
                 */
                prevEntry = -1;
                prevEnd = 0;
            }
            Integer tid = (Integer) currentThreadInterval.getValue();
            lastEnd = currentThreadInterval.getEndTime() + 1;
            long nextEntry = -1;
            if (tid != null && tid > 0) {
                nextEntry = findEntry(tid, time);
                if (prevEntry >= 0 && nextEntry >= 0) {
                    linkList.add(new TimeGraphArrow(prevEntry, nextEntry, prevEnd, time - prevEnd));
                }
                prevEntry = nextEntry;
                prevEnd = lastEnd;
            }
        }
        return linkList;
    }

    /**
     * Get the thread entry id for a given TID and time
     *
     * @param tid
     *            queried TID
     * @param time
     *            queried time stamp
     * @return the id for the desired thread or -1 if it does not exist
     */
    private long findEntry(int tid, long time) {
        /*
         * FIXME TreeMultimap values are Navigable Sets sorted by start time, find the
         * values using floor and the relevant anonymous class if ever the iteration
         * below slows down.
         */
        ThreadEntryModel.Builder entry = Iterables.find(fTidToEntry.get(tid),
                cfe -> cfe.getStartTime() <= time && time <= cfe.getEndTime(), null);
        return entry != null ? entry.getId() : fTraceId;
    }

    @Override
    public @NonNull String getId() {
        return ID;
    }

    @Override
    public TmfModelResponse<Map<String, String>> fetchTooltip(@NonNull SelectionTimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        ITmfStateSystem ss = fModule.getStateSystem();
        if (ss == null) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, CommonStatusMessage.STATE_SYSTEM_FAILED);
        }

        boolean completed = ss.waitUntilBuilt(0);
        ITmfResponse.Status status = completed ? ITmfResponse.Status.COMPLETED : ITmfResponse.Status.RUNNING;
        String statusMessage = completed ? CommonStatusMessage.COMPLETED : CommonStatusMessage.RUNNING;

        Integer quark = fQuarkMap.get(filter.getSelectedItems().iterator().next());
        if (quark == null) {
            return new TmfModelResponse<>(null, status, statusMessage);
        }

        long start = filter.getStart();
        int currentCpuRqQuark = ss.optQuarkRelative(quark, Attributes.CURRENT_CPU_RQ);
        if (currentCpuRqQuark == ITmfStateSystem.INVALID_ATTRIBUTE || start < ss.getStartTime() || start > ss.getCurrentEndTime()) {
            return new TmfModelResponse<>(null, status, statusMessage);
        }

        try {
            ITmfStateInterval interval = ss.querySingleState(start, currentCpuRqQuark);
            Object value = interval.getValue();
            if (value instanceof Integer) {
                return new TmfModelResponse<>(ImmutableMap.of(CPU, String.valueOf(value)), status, statusMessage);
            }
        } catch (StateSystemDisposedException e) {
            /* Ignored */
        }
        return new TmfModelResponse<>(null, status, statusMessage);
    }

}
