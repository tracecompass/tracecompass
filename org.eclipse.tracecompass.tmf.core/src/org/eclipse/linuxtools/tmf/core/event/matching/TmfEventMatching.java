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

package org.eclipse.linuxtools.tmf.core.event.matching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

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

    /**
     * The array of traces to match
     */
    private final Collection<ITmfTrace> fTraces;

    /**
     * The class to call once a match is found
     */
    private final IMatchProcessingUnit fMatches;

    private static final Map<MatchingType, List<ITmfMatchEventDefinition>> fMatchDefinitions = new HashMap<>();

    private final Map<ITmfTrace, ITmfMatchEventDefinition> fMatchMap = new HashMap<>();

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
        fTraces = traces;
        fMatches = tmfEventMatches;
    }

    /**
     * Returns the traces to process
     *
     * @return The traces
     */
    protected Collection<? extends ITmfTrace> getTraces() {
        return fTraces;
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
     * Returns the match event definition corresponding to the trace
     *
     * @param trace
     *            The trace
     * @return The match event definition object
     */
    protected ITmfMatchEventDefinition getEventDefinition(ITmfTrace trace) {
        return fMatchMap.get(trace);
    }

    /**
     * Method that initializes any data structure for the event matching. It
     * also assigns to each trace an event matching definition instance that
     * applies to the trace
     */
    protected void initMatching() {
        fMatches.init(fTraces);
        List<ITmfMatchEventDefinition> deflist = fMatchDefinitions.get(getMatchingType());
        if (deflist == null) {
            return;
        }
        for (ITmfTrace trace : fTraces) {
            for (ITmfMatchEventDefinition def : deflist) {
                if (def.canMatchTrace(trace)) {
                    fMatchMap.put(trace, def);
                    break;
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
            if (!fMatchDefinitions.containsKey(type)) {
                fMatchDefinitions.put(type, new ArrayList<ITmfMatchEventDefinition>());
            }
            fMatchDefinitions.get(type).add(match);
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
