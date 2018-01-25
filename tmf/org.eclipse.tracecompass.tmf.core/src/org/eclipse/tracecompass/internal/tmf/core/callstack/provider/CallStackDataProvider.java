/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.callstack.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.AbstractTmfTraceDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph.ITimeGraphArrow;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph.ITimeGraphDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph.ITimeGraphRowModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph.ITimeGraphState;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph.TimeGraphRowModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph.TimeGraphState;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.ITmfResponse.Status;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.callstack.CallStackAnalysis;
import org.eclipse.tracecompass.tmf.core.symbols.ISymbolProvider;
import org.eclipse.tracecompass.tmf.core.symbols.SymbolProviderManager;
import org.eclipse.tracecompass.tmf.core.symbols.SymbolProviderUtils;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.util.Pair;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

/**
 * Call Stack Data Provider
 *
 * @author Loic Prieur-Drevon
 */
public class CallStackDataProvider extends AbstractTmfTraceDataProvider implements ITimeGraphDataProvider<@NonNull CallStackEntryModel> {

    /**
     * Extension point ID.
     */
    public static final @NonNull String ID = "org.eclipse.tracecompass.internal.tmf.core.callstack.provider.CallStackDataProvider"; //$NON-NLS-1$
    private static final int UNKNOWN_TID = -1;

    private static final AtomicLong fAtomicLong = new AtomicLong();
    private final CallStackAnalysis fModule;
    private final Map<Long, Integer> fIdToQuark = new HashMap<>();
    private final Map<Integer, Long> fQuarkToId = new HashMap<>();
    private final Map<Integer, Integer> fQuarkToPid = new HashMap<>();

    /**
     * If we have finished building the entry tree and can return a cached version.
     */
    private @Nullable TmfModelResponse<List<CallStackEntryModel>> fCached;

    private final @NonNull Collection<@NonNull ISymbolProvider> fProviders = new ArrayList<>();

    private final LoadingCache<Pair<Integer, ITmfStateInterval>, @Nullable String> fTimeEventNames = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .build(new CacheLoader<Pair<Integer, ITmfStateInterval>, @Nullable String>() {
                @Override
                public @Nullable String load(Pair<Integer, ITmfStateInterval> pidInterval) {
                    Integer pid = pidInterval.getFirst();
                    ITmfStateInterval interval = pidInterval.getSecond();

                    Object nameValue = interval.getValue();
                    Long address = null;
                    String name = null;
                    if (nameValue instanceof String) {
                        name = (String) nameValue;
                        try {
                            address = Long.parseLong(name, 16);
                        } catch (NumberFormatException e) {
                            // leave name as null
                        }
                    } else if (nameValue instanceof Integer) {
                        Integer intValue = (Integer) nameValue;
                        name = "0x" + Integer.toUnsignedString(intValue, 16); //$NON-NLS-1$
                        address = intValue.longValue();
                    } else if (nameValue instanceof Long) {
                        address = (long) nameValue;
                        name = "0x" + Long.toUnsignedString(address, 16); //$NON-NLS-1$
                    }
                    if (address != null) {
                        name = SymbolProviderUtils.getSymbolText(fProviders, pid, interval.getStartTime(), address);
                    }
                    return name;
                }
            });

    /**
     * Constructor
     *
     * @param trace
     *            underlying trace
     * @param module
     *            underlying {@link CallStackAnalysis} module
     */
    public CallStackDataProvider(@NonNull ITmfTrace trace, CallStackAnalysis module) {
        super(trace);
        fModule = module;
    }

    @Override
    public TmfModelResponse<List<CallStackEntryModel>> fetchTree(TimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        TmfModelResponse<List<CallStackEntryModel>> cached = fCached;
        if (cached != null) {
            return cached;
        }

        fModule.waitForInitialization();
        ITmfStateSystem ss = fModule.getStateSystem();
        if (ss == null) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, CommonStatusMessage.ANALYSIS_INITIALIZATION_FAILED);
        }

        boolean complete = ss.waitUntilBuilt(0);
        long start = ss.getStartTime();
        long end = ss.getCurrentEndTime();
        String[] callStackPath = fModule.getCallStackPath();

        ImmutableList.Builder<CallStackEntryModel> builder = ImmutableList.builder();
        long traceId = getIdForQuark(ITmfStateSystem.ROOT_ATTRIBUTE);
        builder.add(new CallStackEntryModel(traceId, -1, getTrace().getName(), start, end, CallStackEntryModel.TRACE, UNKNOWN_TID));

        try {
            List<Integer> processQuarks = ss.getQuarks(fModule.getProcessesPattern());
            SubMonitor subMonitor = SubMonitor.convert(monitor, "CallStackDataProvider#fetchTree", processQuarks.size()); //$NON-NLS-1$
            List<@NonNull ITmfStateInterval> fullStart = ss.queryFullState(start);
            List<@NonNull ITmfStateInterval> fullEnd = ss.queryFullState(end);
            for (int processQuark : processQuarks) {

                /*
                 * Default to trace entry, overwrite if a process entry exists.
                 */
                long threadParentId = traceId;
                int pid = UNKNOWN_TID;
                if (processQuark != ITmfStateSystem.ROOT_ATTRIBUTE) {
                    threadParentId = getIdForQuark(processQuark);
                    String processName = ss.getAttributeName(processQuark);
                    Object processValue = fullEnd.get(processQuark).getValue();
                    pid = getThreadProcessId(processName, processValue);
                    builder.add(new CallStackEntryModel(threadParentId, traceId, processName, start, end,
                            CallStackEntryModel.PROCESS, pid));
                }

                /* Create the threads under the process */
                List<Integer> threadQuarks = ss.getQuarks(processQuark, fModule.getThreadsPattern());
                for (int threadQuark : threadQuarks) {
                    if (subMonitor.isCanceled()) {
                        new TmfModelResponse<>(null, ITmfResponse.Status.CANCELLED, CommonStatusMessage.TASK_CANCELLED);
                    }

                    int callStackQuark = ss.optQuarkRelative(threadQuark, callStackPath);
                    if (callStackQuark == ITmfStateSystem.INVALID_ATTRIBUTE) {
                        continue;
                    }
                    String threadName = ss.getAttributeName(threadQuark);
                    /*
                     * Default to process/trace entry, overwrite if a thread entry exists.
                     */
                    long callStackParent = threadParentId;
                    if (threadQuark != processQuark) {
                        CallStackEntryModel thread = createThread(ss, start, end, threadQuark, threadParentId, callStackQuark, fullStart, fullEnd);
                        callStackParent = thread.getId();
                        builder.add(thread);
                    }
                    List<Integer> callStackAttributes = ss.getSubAttributes(callStackQuark, false);
                    createStackEntries(callStackAttributes, start, end, pid, threadName, callStackParent, builder);
                }
                subMonitor.worked(1);
            }
        } catch (StateSystemDisposedException e) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, CommonStatusMessage.STATE_SYSTEM_FAILED);
        }

        if (complete) {
            fCached = new TmfModelResponse<>(builder.build(), ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
            return fCached;
        }

        return new TmfModelResponse<>(builder.build(), ITmfResponse.Status.RUNNING, CommonStatusMessage.RUNNING);
    }

    private CallStackEntryModel createThread(ITmfStateSystem ss, long start, long end, int threadQuark, long processId, int callStackQuark,
            List<ITmfStateInterval> fullStart, List<ITmfStateInterval> fullEnd) {
        String threadName = ss.getAttributeName(threadQuark);
        long threadEnd = end + 1;
        ITmfStateInterval endInterval = fullEnd.get(callStackQuark);
        if (endInterval.getValue() == null && endInterval.getStartTime() != ss.getStartTime()) {
            threadEnd = endInterval.getStartTime();
        }
        Object threadStateValue = fullEnd.get(threadQuark).getValue();
        int threadId = getThreadProcessId(threadName, threadStateValue);
        ITmfStateInterval startInterval = fullStart.get(callStackQuark);
        long threadStart = startInterval.getValue() == null ? Long.min(startInterval.getEndTime() + 1, end) : start;
        return new CallStackEntryModel(getIdForQuark(threadQuark), processId, threadName, threadStart, threadEnd, CallStackEntryModel.THREAD, threadId);
    }

    private void createStackEntries(List<Integer> callStackAttributes, long start, long end, int pid,
            String threadName, long callStackParent, ImmutableList.Builder<CallStackEntryModel> builder) {
        int level = 1;
        for (int stackLevelQuark : callStackAttributes) {
            long id = getIdForQuark(stackLevelQuark);
            builder.add(new CallStackEntryModel(id, callStackParent, threadName, start, end, level, pid));
            fIdToQuark.put(id, stackLevelQuark);
            fQuarkToPid.put(stackLevelQuark, pid);
            level++;
        }
    }

    private static int getThreadProcessId(String name, @Nullable Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(name);
        } catch (NumberFormatException e) {
            return UNKNOWN_TID;
        }
    }

    private long getIdForQuark(int quark) {
        synchronized (fQuarkToId) {
            return fQuarkToId.computeIfAbsent(quark, q -> fAtomicLong.getAndIncrement());
        }
    }

    @Override
    public TmfModelResponse<List<ITimeGraphRowModel>> fetchRowModel(SelectionTimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        ITmfStateSystem ss = fModule.getStateSystem();
        if (ss == null) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, CommonStatusMessage.STATE_SYSTEM_FAILED);
        }

        if (filter.getSelectedItems().size() == 1 && filter.getTimesRequested().length == 2) {
            // this is a request for a follow event.
            Long id = filter.getSelectedItems().iterator().next();
            if (filter.getStart() == Long.MIN_VALUE) {
                return getFollowEvent(ss, id, filter.getEnd(), false);
            } else if (filter.getEnd() == Long.MAX_VALUE) {
                return getFollowEvent(ss, id, filter.getStart(), true);
            }
        }

        SubMonitor subMonitor = SubMonitor.convert(monitor, "CallStackDataProvider#fetchRowModel", 2); //$NON-NLS-1$

        ArrayListMultimap<Integer, ITmfStateInterval> intervals = ArrayListMultimap.create();
        Map<Long, Integer> idToQuark = Maps.filterKeys(fIdToQuark, k -> filter.getSelectedItems().contains(k));
        Collection<Long> times = getTimes(ss.getStartTime(), filter);
        boolean completed = ss.waitUntilBuilt(0);
        try {
            /* Do the actual query */
            for (ITmfStateInterval interval : ss.query2D(idToQuark.values(), times)) {
                if (subMonitor.isCanceled()) {
                    return new TmfModelResponse<>(null, ITmfResponse.Status.CANCELLED, CommonStatusMessage.TASK_CANCELLED);
                }
                intervals.put(interval.getAttribute(), interval);
            }
        } catch (TimeRangeException | StateSystemDisposedException e) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, String.valueOf(e.getMessage()));
        }
        subMonitor.worked(1);

        List<ITimeGraphRowModel> rows = new ArrayList<>();
        for (Entry<Long, Integer> idQuark : idToQuark.entrySet()) {
            if (subMonitor.isCanceled()) {
                return new TmfModelResponse<>(null, ITmfResponse.Status.CANCELLED, CommonStatusMessage.TASK_CANCELLED);
            }
            List<ITmfStateInterval> states = intervals.get(idQuark.getValue());
            List<ITimeGraphState> eventList = new ArrayList<>(states.size());
            states.forEach(state -> eventList.add(createTimeGraphState(state)));
            eventList.sort(Comparator.comparingLong(ITimeGraphState::getStartTime));
            rows.add(new TimeGraphRowModel(idQuark.getKey(), eventList));
        }
        subMonitor.worked(1);
        if (completed) {
            return new TmfModelResponse<>(rows, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
        }
        return new TmfModelResponse<>(rows, ITmfResponse.Status.RUNNING, CommonStatusMessage.RUNNING);
    }

    private ITimeGraphState createTimeGraphState(ITmfStateInterval interval) {
        long startTime = interval.getStartTime();
        long duration = interval.getEndTime() - startTime + 1;
        Object value = interval.getValue();
        Integer pid = fQuarkToPid.get(interval.getAttribute());
        if (value != null && pid != null) {
            String name = fTimeEventNames.getUnchecked(new Pair<>(pid, interval));
            return new TimeGraphState(startTime, duration, value.hashCode(), name);
        }
        return new TimeGraphState(startTime, duration, Integer.MIN_VALUE);
    }

    private static @NonNull Collection<@NonNull Long> getTimes(long start, TimeQueryFilter filter) {
        Collection<@NonNull Long> times = new HashSet<>();
        for (long t : filter.getTimesRequested()) {
            if (t >= start) {
                times.add(t);
            }
        }
        return times;
    }

    @Override
    public TmfModelResponse<List<ITimeGraphArrow>> fetchArrows(TimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        return new TmfModelResponse<>(null, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }

    @Override
    public String getId() {
        return ID;
    }

    /**
     * Clear the symbol provider cache. Load the symbol provider for the current
     * trace.
     *
     * @param monitor
     *            progress monitor
     */
    public void resetFunctionNames(@Nullable IProgressMonitor monitor) {
        fTimeEventNames.invalidateAll();
        synchronized (fProviders) {
            Collection<@NonNull ISymbolProvider> symbolProviders = SymbolProviderManager.getInstance().getSymbolProviders(getTrace());
            SubMonitor sub = SubMonitor.convert(monitor, "CallStackDataProvider#resetFunctionNames", symbolProviders.size()); //$NON-NLS-1$
            fProviders.clear();
            for (ISymbolProvider symbolProvider : symbolProviders) {
                fProviders.add(symbolProvider);
                symbolProvider.loadConfiguration(sub);
                sub.worked(1);
            }
        }
    }

    /**
     * Get the next or previous interval for a call stack entry ID, time and
     * direction
     *
     * @param id
     *            call stack entry ID
     * @param time
     *            selection start time
     * @param forward
     *            if going to next or previous
     * @return the next / previous interval if it exists, encapsulated in a
     *         response.
     */
    private @NonNull TmfModelResponse<List<ITimeGraphRowModel>> getFollowEvent(ITmfStateSystem ss, long id, long time, boolean forward) {
        Integer quark = fIdToQuark.get(id);
        if (quark == null) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
        }
        int parentQuark = ss.getParentAttributeQuark(quark);
        try {
            ITmfStateInterval current = ss.querySingleState(Long.max(ss.getStartTime(), Long.min(time, ss.getCurrentEndTime())), parentQuark);
            ITmfStateInterval interval = null;
            if (forward && current.getEndTime() + 1 <= ss.getCurrentEndTime()) {
                interval = ss.querySingleState(current.getEndTime() + 1, parentQuark);
            } else if (!forward && current.getStartTime() - 1 >= ss.getStartTime()) {
                interval = ss.querySingleState(current.getStartTime() - 1, parentQuark);
            }
            if (interval != null && interval.getValue() instanceof Number) {
                Object object = interval.getValue();
                if (object instanceof Number) {
                    long value = ((Number) object).longValue();
                    TimeGraphState state = new TimeGraphState(interval.getStartTime(), interval.getEndTime() - interval.getStartTime(), value);
                    TimeGraphRowModel row = new TimeGraphRowModel(id, Collections.singletonList(state));
                    return new TmfModelResponse<>(Collections.singletonList(row), Status.COMPLETED, CommonStatusMessage.COMPLETED);
                }
            }
        } catch (StateSystemDisposedException | TimeRangeException e) {
        }
        return new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, CommonStatusMessage.STATE_SYSTEM_FAILED);
    }

    @Override
    public TmfModelResponse<Map<String, String>> fetchTooltip(SelectionTimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        return new TmfModelResponse<>(Collections.emptyMap(), ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }

}
