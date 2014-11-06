/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;

/**
 * Abstract class to extend to match certain type of events in a trace
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public abstract class TmfEventMatching implements ITmfEventMatching {

    /**
     * The matching type
     *
     * FIXME Not the best place to put this. Have an array of match types as a
     * parameter of each trace?
     */
    public enum MatchingType {
        /**
         * NETWORK, match network events
         */
        NETWORK
    }

    private static final Multimap<MatchingType, ITmfMatchEventDefinition> MATCH_DEFINITIONS = HashMultimap.create();

    /**
     * The array of traces to match
     */
    private final Collection<ITmfTrace> fTraces;

    /**
     * The class to call once a match is found
     */
    private final IMatchProcessingUnit fMatches;

    private final Multimap<ITmfTrace, ITmfMatchEventDefinition> fMatchMap = HashMultimap.create();

    /**
     * Constructor with multiple traces and a match processing object
     *
     * @param traces
     *            The set of traces for which to match events
     * @param tmfEventMatches
     *            The match processing class
     */
    public TmfEventMatching(Collection<ITmfTrace> traces, IMatchProcessingUnit tmfEventMatches) {
        if (tmfEventMatches == null) {
            throw new IllegalArgumentException();
        }
        fTraces = new HashSet<>(traces);
        fMatches = tmfEventMatches;
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
        Set<ITmfTrace> traces = new HashSet<>();
        for (ITmfTrace trace : fTraces) {
            traces.addAll(TmfTraceManager.getTraceSet(trace));
        }
        return traces;
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
     */
    protected void initMatching() {
        fMatches.init(fTraces);
        Collection<ITmfMatchEventDefinition> deflist = MATCH_DEFINITIONS.get(getMatchingType());
        if (deflist == null) {
            return;
        }
        for (ITmfTrace trace : getIndividualTraces()) {
            for (ITmfMatchEventDefinition def : deflist) {
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
    @SuppressWarnings("nls")
    @Override
    public String toString() {
        return getClass().getSimpleName() + " [ " + fMatches + " ]";
    }

    /**
     * Matches one event
     *
     * @param event
     *            The event to match
     * @param trace
     *            The trace to which this event belongs
     */
    protected abstract void matchEvent(ITmfEvent event, ITmfTrace trace);

    /**
     * Returns the matching type this class implements
     *
     * @return A matching type
     */
    protected abstract MatchingType getMatchingType();

    /**
     * Method that start the process of matching events
     *
     * @return Whether the match was completed correctly or not
     */
    @Override
    public boolean matchEvents() {

        /* Are there traces to match? If no, return false */
        if (!(fTraces.size() > 0)) {
            return false;
        }

        // TODO Start a new thread here?
        initMatching();

        /**
         * For each trace, get the events and for each event, call the
         * MatchEvent method
         *
         * FIXME This would use a lot of memory if the traces are big, because
         * all involved events from first trace will have to be kept before a
         * first match is possible with second trace.
         *
         * <pre>
         * Other possible matching strategy:
         * Incremental:
         * Sliding window:
         * Other strategy: start with the shortest trace, take a few events
         * at the beginning and at the end
         * Experiment strategy: have the experiment do the request, then events will
         * come from both traces chronologically, but then instead of ITmfTrace[], it
         * would be preferable to have experiment
         * </pre>
         */
        for (ITmfTrace trace : fTraces) {
            EventMatchingBuildRequest request = new EventMatchingBuildRequest(this, trace);

            /*
             * Send the request to the trace here, since there is probably no
             * experiment.
             */
            trace.sendRequest(request);
            try {
                request.waitForCompletion();
            } catch (InterruptedException e) {
                Activator.logInfo(e.getMessage());
            }
        }

        finalizeMatching();

        return true;
    }

    /**
     * Registers an event match definition to be used for a certain match type
     *
     * @param match
     *            The event matching definition
     */
    public static void registerMatchObject(ITmfMatchEventDefinition match) {
        for (MatchingType type : match.getApplicableMatchingTypes()) {
            MATCH_DEFINITIONS.put(type, match);
        }
    }

}

class EventMatchingBuildRequest extends TmfEventRequest {

    private final TmfEventMatching matching;
    private final ITmfTrace trace;

    EventMatchingBuildRequest(TmfEventMatching matching, ITmfTrace trace) {
        super(ITmfEvent.class,
                TmfTimeRange.ETERNITY,
                0,
                ITmfEventRequest.ALL_DATA,
                ITmfEventRequest.ExecutionType.FOREGROUND);
        this.matching = matching;
        this.trace = trace;
    }

    @Override
    public void handleData(final ITmfEvent event) {
        super.handleData(event);
        matching.matchEvent(event, trace);
    }

    @Override
    public void handleSuccess() {
        super.handleSuccess();
    }

    @Override
    public void handleCancel() {
        super.handleCancel();
    }

    @Override
    public void handleFailure() {
        super.handleFailure();
    }
}
