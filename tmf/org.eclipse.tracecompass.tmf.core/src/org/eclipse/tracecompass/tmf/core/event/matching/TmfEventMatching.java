/*******************************************************************************
 * Copyright (c) 2013, 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation and API
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.event.matching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.matching.TmfEventDependency.DependencyEvent;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

/**
 * Abstract class to extend to match certain type of events in a trace
 *
 * @author Geneviève Bastien
 */
public class TmfEventMatching implements ITmfEventMatching {

    private static final Set<ITmfMatchEventDefinition> MATCH_DEFINITIONS = new HashSet<>();

    /**
     * The array of traces to match
     */
    private final @NonNull Collection<@NonNull ITmfTrace> fTraces;

    private final @NonNull Collection<@NonNull ITmfTrace> fIndividualTraces;
    private final @NonNull Collection<@NonNull String> fDistinctHosts;

    /**
     * The class to call once a match is found
     */
    private final IMatchProcessingUnit fMatches;

    private final Multimap<ITmfTrace, ITmfMatchEventDefinition> fMatchMap = HashMultimap.create();

    /**
     * Hashtables for unmatches incoming events
     */
    private final Table<ITmfTrace, IEventMatchingKey, DependencyEvent> fUnmatchedIn = HashBasedTable.create();

    /**
     * Hashtables for unmatches outgoing events
     */
    private final Table<ITmfTrace, IEventMatchingKey, DependencyEvent> fUnmatchedOut = HashBasedTable.create();

    /**
     * Hash tables matching the latest match between 2 hosts (sender, receiver) by
     * key class
     */
    private final Map<Class<? extends IEventMatchingKey>, Table<String, String, TmfEventDependency>> fLastMatches = new HashMap<>();

    /**
     * Enum for cause and effect types of event
     * @since 1.0
     */
    public enum Direction {
        /**
         * The event is the first event of the match
         */
        CAUSE,
        /**
         * The event is the second event, the one that matches with the cause
         */
        EFFECT,
    }

    /**
     * Constructor with multiple traces
     *
     * @param traces
     *            The set of traces for which to match events
     * @since 1.0
     */
    public TmfEventMatching(Collection<@NonNull ITmfTrace> traces) {
        this(traces, new TmfEventMatches());
    }

    /**
     * Constructor with multiple traces and a match processing object
     *
     * @param traces
     *            The set of traces for which to match events
     * @param tmfEventMatches
     *            The match processing class
     */
    public TmfEventMatching(Collection<@NonNull ITmfTrace> traces, IMatchProcessingUnit tmfEventMatches) {
        if (tmfEventMatches == null) {
            throw new IllegalArgumentException();
        }
        fTraces = new HashSet<>(traces);
        fMatches = tmfEventMatches;
        Set<@NonNull ITmfTrace> individualTraces = new HashSet<>();
        for (ITmfTrace trace : traces) {
            individualTraces.addAll(TmfTraceManager.getTraceSet(trace));
        }
        fIndividualTraces = individualTraces;
        fDistinctHosts = individualTraces.stream()
                .map(ITmfTrace::getHostId)
                .collect(Collectors.toSet());
    }

    /**
     * Returns the traces to synchronize. These are the traces that were
     * specified in the constructor, they may contain either traces or
     * experiment.
     *
     * @return The traces to synchronize
     */
    protected Collection<ITmfTrace> getTraces() {
        return new HashSet<>(fTraces);
    }

    /**
     * Returns the individual traces to process. If some of the traces specified
     * to synchronize in the constructor were experiments, only the traces
     * contained in this experiment will be returned. No {@link TmfExperiment}
     * are returned by this method.
     *
     * @return The individual traces to synchronize, no experiments
     */
    protected Collection<ITmfTrace> getIndividualTraces() {
        return fIndividualTraces;
    }

    /**
     * Returns the match processing unit
     *
     * @return The match processing unit
     */
    protected IMatchProcessingUnit getProcessingUnit() {
        return fMatches;
    }

    /**
     * Returns the match event definitions corresponding to the trace
     *
     * @param trace
     *            The trace
     * @return The match event definition object
     */
    protected Collection<ITmfMatchEventDefinition> getEventDefinitions(ITmfTrace trace) {
        return ImmutableList.copyOf(fMatchMap.get(trace));
    }

    /**
     * Method that initializes any data structure for the event matching. It
     * also assigns to each trace an event matching definition instance that
     * applies to the trace
     *
     * @since 1.0
     */
    public void initMatching() {
        // Initialize the matching infrastructure (unmatched event lists)
        fUnmatchedIn.clear();
        fUnmatchedOut.clear();

        fMatches.init(fTraces);
        for (ITmfTrace trace : getIndividualTraces()) {
            for (ITmfMatchEventDefinition def : MATCH_DEFINITIONS) {
                if (def.canMatchTrace(trace)) {
                    fMatchMap.put(trace, def);
                }
            }
        }
    }

    /**
     * Calls any post matching methods of the processing class
     */
    protected void finalizeMatching() {
        fMatches.matchingEnded();
    }

    /**
     * Prints stats from the matching
     *
     * @return string of statistics
     */
    @Override
    public String toString() {
        final String cr = System.getProperty("line.separator"); //$NON-NLS-1$
        StringBuilder b = new StringBuilder();
        b.append(getProcessingUnit());
        int i = 0;
        for (ITmfTrace trace : getIndividualTraces()) {
            b.append("Trace " + i++ + ":" + cr + //$NON-NLS-1$ //$NON-NLS-2$
                    "  " + fUnmatchedIn.row(trace).size() + " unmatched incoming events" + cr + //$NON-NLS-1$ //$NON-NLS-2$
                    "  " + fUnmatchedOut.row(trace).size() + " unmatched outgoing events" + cr); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return b.toString();
    }

    /**
     * Matches one event
     *
     * @param event
     *            The event to match
     * @param trace
     *            The trace to which this event belongs
     * @param monitor
     *            The monitor for the synchronization job
     * @since 1.0
     */
    public void matchEvent(ITmfEvent event, ITmfTrace trace, @NonNull IProgressMonitor monitor) {
        ITmfMatchEventDefinition def = null;
        Direction evType = null;
        IEventMatchingKey eventKey = null;
        for (ITmfMatchEventDefinition oneDef : getEventDefinitions(event.getTrace())) {
            def = oneDef;
            evType = def.getDirection(event);
            if (evType != null) {
                /*
                 * Make sure this definition generates an event key, maybe
                 * another definition does
                 */
                eventKey = def.getEventKey(event);
                if (eventKey != null) {
                    break;
                }
            }

        }

        if (def == null || evType == null || eventKey == null) {
            return;
        }

        Table<ITmfTrace, IEventMatchingKey, DependencyEvent> unmatchedTbl, companionTbl;

        /* Point to the appropriate table */
        switch (evType) {
        case EFFECT:
            unmatchedTbl = fUnmatchedIn;
            companionTbl = fUnmatchedOut;
            break;
        case CAUSE:
            unmatchedTbl = fUnmatchedOut;
            companionTbl = fUnmatchedIn;
            break;
        default:
            return;
        }

        TmfEventDependency dep = null;
        DependencyEvent depEvent = new DependencyEvent(event);
        /* Search for the event in the companion table */
        for (ITmfTrace mTrace : getIndividualTraces()) {
            if (companionTbl.contains(mTrace, eventKey)) {
                DependencyEvent companionEvent = companionTbl.remove(mTrace, eventKey);

                /* Create the dependency object */
                switch (evType) {
                case EFFECT:
                    dep = new TmfEventDependency(companionEvent, depEvent);
                    break;
                case CAUSE:
                    /*
                     * If the companionEvent is from the same host, ignore this
                     * match to respect causality. Put it back in the list, so
                     * that it is available again for another match.
                     *
                     * FIXME: This happens because a packet may go through
                     * several network interfaces in a machine before being
                     * finally sent by the physical interface. With virtual
                     * interfaces, sending and reception are not punctual
                     * events, but has a duration. We should follow the event
                     * through all its interfaces and maybe have a virtual event
                     * to encompass the whole duration. More investigation needed
                     */
                    if (!companionEvent.getTrace().getHostId().equals(depEvent.getTrace().getHostId())) {
                        dep = new TmfEventDependency(depEvent, companionEvent);
                    } else {
                        companionTbl.put(mTrace,  eventKey, companionEvent);
                    }
                    break;
                default:
                    break;

                }
            }
        }

        /*
         * If no companion was found, add the event to the appropriate unMatched
         * lists
         */
        if (dep != null) {
            processDependency(eventKey, dep);
            monitor.subTask(NLS.bind(Messages.TmfEventMatching_MatchesFound, getProcessingUnit().countMatches()));
        } else {
            /*
             * If an event is already associated with this key, do not add it
             * again, we keep the first event chronologically, so if its match
             * is eventually found, it is associated with the first send or
             * receive event. At best, it is a good guess, at worst, the match
             * will be too far off to be accurate. Too bad!
             *
             * TODO: maybe instead of just one event, we could have a list of
             * events as value for the unmatched table. Not necessary right now
             * though
             */
            if (!unmatchedTbl.contains(event.getTrace(), eventKey)) {
                unmatchedTbl.put(event.getTrace(), eventKey, depEvent);
            }
        }
    }

    private void processDependency(@NonNull IEventMatchingKey eventKey, @NonNull TmfEventDependency dep) {
        getProcessingUnit().addMatch(eventKey, dep);
        String sourceHost = dep.getSource().getTrace().getHostId();
        String destHost = dep.getDestination().getTrace().getHostId();
        Table<String, String, TmfEventDependency> lastMatches = getLastMatchTable(eventKey);
        lastMatches.put(sourceHost, destHost, dep);

        // Do some cleanup of events waiting to be matched
        cleanupList(eventKey, lastMatches.row(sourceHost), dep.getSource(), evDep -> evDep.getSource().getTimestamp().toNanos(), fUnmatchedOut);
        cleanupList(eventKey, lastMatches.column(destHost), dep.getDestination(), evDep -> evDep.getDestination().getTimestamp().toNanos(), fUnmatchedIn);
    }

    private Table<String, String, TmfEventDependency> getLastMatchTable(@NonNull IEventMatchingKey eventKey) {
        return fLastMatches.computeIfAbsent(eventKey.getClass(), k -> HashBasedTable.create());
    }

    private void cleanupList(@NonNull IEventMatchingKey eventKey, Map<String, TmfEventDependency> lastMatches, DependencyEvent lastDep, ToLongFunction<TmfEventDependency> mapToTime, Table<ITmfTrace, IEventMatchingKey, DependencyEvent> toClean) {
     // Is there a match with all other hosts
        long otherHosts = lastMatches.keySet().stream().filter(s -> !s.equals(lastDep.getTrace().getHostId())).count();
        if (otherHosts == fDistinctHosts.size() - 1) {
            // A match has been found with all hosts, cleanup the previously sent packets for this trace
            long earliest = lastMatches.values().stream()
                    .mapToLong(mapToTime)
                    .min()
                    .orElse(0L);
            if (earliest > 0) {
                List<IEventMatchingKey> toRemove = new ArrayList<>();
                for (Entry<IEventMatchingKey, DependencyEvent> entry : toClean.row(lastDep.getTrace()).entrySet()) {
                    if (entry.getValue().getTimestamp().toNanos() < earliest && entry.getKey().getClass().isAssignableFrom(eventKey.getClass())) {
                        toRemove.add(entry.getKey());
                    }
                }
                toRemove.forEach(m -> toClean.remove(lastDep.getTrace(), m));
            }
        }
    }

    /**
     * Method that start the process of matching events
     *
     * @return Whether the match was completed correctly or not
     */
    @Override
    public boolean matchEvents() {

        /* Are there traces to match? If no, return false */
        if (fTraces.isEmpty()) {
            return false;
        }

        initMatching();

        /*
         * Actual analysis will be run on a separate thread
         */
        Job job = new Job(Messages.TmfEventMatching_MatchingEvents) {
            @Override
            protected IStatus run(final IProgressMonitor monitor) {
                /**
                 * FIXME For now, we use the experiment strategy: the trace that
                 * is asked to be matched is actually an experiment and the
                 * experiment does the request. But depending on how divergent
                 * the traces' times are and how long it takes to get the first
                 * match, it can use a lot of memory.
                 *
                 * Some strategies can help limit the memory usage of this
                 * algorithm:
                 *
                 * <pre>
                 * Other possible matching strategy:
                 * * start with the shortest trace
                 * * take a few events at the beginning and at the end and try
                 *   to match them
                 * </pre>
                 */
                for (ITmfTrace trace : fTraces) {
                    monitor.beginTask(NLS.bind(Messages.TmfEventMatching_LookingEventsFrom, trace.getName()), IProgressMonitor.UNKNOWN);
                    setName(NLS.bind(Messages.TmfEventMatching_RequestingEventsFrom, trace.getName()));

                    /* Send the request to the trace */
                    EventMatchingBuildRequest request = new EventMatchingBuildRequest(TmfEventMatching.this, trace, monitor);
                    trace.sendRequest(request);
                    try {
                        request.waitForCompletion();
                    } catch (InterruptedException e) {
                        Activator.logInfo(e.getMessage());
                    }
                    if (monitor.isCanceled()) {
                        return Status.CANCEL_STATUS;
                    }
                }
                return Status.OK_STATUS;
            }
        };
        job.schedule();
        try {
            job.join();
        } catch (InterruptedException e) {

        }

        finalizeMatching();

        return true;
    }

    /**
     * Registers an event match definition
     *
     * @param match
     *            The event matching definition
     */
    public static void registerMatchObject(ITmfMatchEventDefinition match) {
        MATCH_DEFINITIONS.add(match);
    }

    /**
     * Get the table of unmatched effect events (incoming)
     *
     * @return The table of unmatched incoming events
     * @since 3.3
     */
    @VisibleForTesting
    protected Table<ITmfTrace, IEventMatchingKey, DependencyEvent> getUnmatchedIn() {
        return fUnmatchedIn;
    }

    /**
     * Get the table of unmatched cause events (outgoing)
     *
     * @return The table of unmatched outgoing events
     * @since 3.3
     */
    @VisibleForTesting
    protected Table<ITmfTrace, IEventMatchingKey, DependencyEvent> getUnmatchedOut() {
        return fUnmatchedOut;
    }

}

class EventMatchingBuildRequest extends TmfEventRequest {

    private final TmfEventMatching matching;
    private final ITmfTrace trace;
    private final @NonNull IProgressMonitor fMonitor;

    EventMatchingBuildRequest(TmfEventMatching matching, ITmfTrace trace, IProgressMonitor monitor) {
        super(ITmfEvent.class,
                TmfTimeRange.ETERNITY,
                0,
                ITmfEventRequest.ALL_DATA,
                ITmfEventRequest.ExecutionType.FOREGROUND);
        this.matching = matching;
        this.trace = trace;
        if (monitor == null) {
            fMonitor = new NullProgressMonitor();
        } else {
            fMonitor = monitor;
        }
    }

    @Override
    public void handleData(final ITmfEvent event) {
        super.handleData(event);
        if (fMonitor.isCanceled()) {
            this.cancel();
        }
        matching.matchEvent(event, trace, fMonitor);
    }
}
