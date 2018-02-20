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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.core.model.timegraph.AbstractTimeGraphDataProvider;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.callstack.CallStackAnalysis;
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

/**
 * Call Stack Data Provider
 *
 * @author Loic Prieur-Drevon
 */
public class CallStackDataProvider extends AbstractTimeGraphDataProvider<@NonNull CallStackAnalysis, @NonNull CallStackEntryModel> {

    /**
     * Extension point ID.
     */
    public static final @NonNull String ID = "org.eclipse.tracecompass.internal.tmf.core.callstack.provider.CallStackDataProvider"; //$NON-NLS-1$
    private static final int UNKNOWN_TID = -1;

    private final Map<Integer, Integer> fQuarkToPid = new HashMap<>();

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
    public CallStackDataProvider(@NonNull ITmfTrace trace, @NonNull CallStackAnalysis module) {
        super(trace, module);
    }

    @Override
    protected List<CallStackEntryModel> getTree(ITmfStateSystem ss, TimeQueryFilter filter,
            @Nullable IProgressMonitor monitor) throws StateSystemDisposedException {
        long start = ss.getStartTime();
        long end = ss.getCurrentEndTime();
        String[] callStackPath = getAnalysisModule().getCallStackPath();

        ImmutableList.Builder<CallStackEntryModel> builder = ImmutableList.builder();
        long traceId = getId(ITmfStateSystem.ROOT_ATTRIBUTE);
        builder.add(new CallStackEntryModel(traceId, -1, getTrace().getName(), start, end, CallStackEntryModel.TRACE, UNKNOWN_TID));

        List<Integer> processQuarks = ss.getQuarks(getAnalysisModule().getProcessesPattern());
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
                threadParentId = getId(processQuark);
                String processName = ss.getAttributeName(processQuark);
                Object processValue = fullEnd.get(processQuark).getValue();
                pid = getThreadProcessId(processName, processValue);
                builder.add(new CallStackEntryModel(threadParentId, traceId, processName, start, end,
                        CallStackEntryModel.PROCESS, pid));
            }

            /* Create the threads under the process */
            List<Integer> threadQuarks = ss.getQuarks(processQuark, getAnalysisModule().getThreadsPattern());
            for (int threadQuark : threadQuarks) {
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
                    CallStackEntryModel thread = createThread(ss, start, end, threadQuark, threadParentId, callStackQuark,
                            fullStart, fullEnd);
                    callStackParent = thread.getId();
                    builder.add(thread);
                }
                List<Integer> callStackAttributes = ss.getSubAttributes(callStackQuark, false);
                createStackEntries(callStackAttributes, start, end, pid, threadName, callStackParent, builder);
            }
            subMonitor.worked(1);
        }

        return builder.build();
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
        return new CallStackEntryModel(getId(threadQuark), processId, threadName, threadStart, threadEnd, CallStackEntryModel.THREAD, threadId);
    }

    private void createStackEntries(List<Integer> callStackAttributes, long start, long end, int pid,
            String threadName, long callStackParent, ImmutableList.Builder<CallStackEntryModel> builder) {
        int level = 1;
        for (int stackLevelQuark : callStackAttributes) {
            long id = getId(stackLevelQuark);
            builder.add(new CallStackEntryModel(id, callStackParent, threadName, start, end, level, pid));
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

    @Override
    protected List<ITimeGraphRowModel> getRowModel(ITmfStateSystem ss, SelectionTimeQueryFilter filter, @Nullable IProgressMonitor monitor)
            throws StateSystemDisposedException {
        Map<@NonNull Long, @NonNull Integer> entries = getSelectedEntries(filter);
        if (entries.size() == 1 && filter.getTimesRequested().length == 2) {
            // this is a request for a follow event.
            Entry<@NonNull Long, @NonNull Integer> entry = entries.entrySet().iterator().next();
            if (filter.getStart() == Long.MIN_VALUE) {
                return getFollowEvent(ss, entry, filter.getEnd(), false);
            } else if (filter.getEnd() == Long.MAX_VALUE) {
                return getFollowEvent(ss, entry, filter.getStart(), true);
            }
        }

        SubMonitor subMonitor = SubMonitor.convert(monitor, "CallStackDataProvider#fetchRowModel", 2); //$NON-NLS-1$

        ArrayListMultimap<Integer, ITmfStateInterval> intervals = ArrayListMultimap.create();
        Collection<Long> times = getTimes(filter, ss.getStartTime(), ss.getCurrentEndTime());
        /* Do the actual query */
        for (ITmfStateInterval interval : ss.query2D(entries.values(), times)) {
            if (subMonitor.isCanceled()) {
                return null;
            }
            intervals.put(interval.getAttribute(), interval);
        }
        subMonitor.worked(1);

        List<ITimeGraphRowModel> rows = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : entries.entrySet()) {
            if (subMonitor.isCanceled()) {
                return null;
            }
            Collection<ITmfStateInterval> states = intervals.get(entry.getValue());
            List<ITimeGraphState> eventList = new ArrayList<>(states.size());
            states.forEach(state -> eventList.add(createTimeGraphState(state)));
            eventList.sort(Comparator.comparingLong(ITimeGraphState::getStartTime));
            rows.add(new TimeGraphRowModel(entry.getKey(), eventList));
        }
        subMonitor.worked(1);
        return rows;
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
     * @param ss
     *            this data provider's state system
     * @param entry
     *            whose key is the ID and value is the quark for the entry whose
     *            next / previous state we are searching for
     * @param time
     *            selection start time
     * @param forward
     *            if going to next or previous
     * @return the next / previous state encapsulated in a row if it exists, else
     *         null
     * @throws StateSystemDisposedException
     */
    private static List<ITimeGraphRowModel> getFollowEvent(ITmfStateSystem ss, Entry<Long, Integer> entry, long time, boolean forward) throws StateSystemDisposedException {
        int parentQuark = ss.getParentAttributeQuark(entry.getValue());
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
                int value = ((Number) object).intValue();
                TimeGraphState state = new TimeGraphState(interval.getStartTime(), interval.getEndTime() - interval.getStartTime(), value);
                TimeGraphRowModel row = new TimeGraphRowModel(entry.getKey(), Collections.singletonList(state));
                return Collections.singletonList(row);
            }
        }
        return null;
    }

    @Override
    public TmfModelResponse<Map<String, String>> fetchTooltip(SelectionTimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        return new TmfModelResponse<>(Collections.emptyMap(), ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }

    @Override
    protected boolean isCacheable() {
        return true;
    }

}
