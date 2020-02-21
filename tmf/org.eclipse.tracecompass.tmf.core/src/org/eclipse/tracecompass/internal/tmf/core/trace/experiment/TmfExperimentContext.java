/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Put in shape for 1.0
 *   Patrick Tasse - Updated for removal of context clone
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.trace.experiment;

import java.util.Arrays;
import java.util.PriorityQueue;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfContext;

import com.google.common.annotations.VisibleForTesting;

/**
 * The experiment context in TMF.
 * <p>
 * The experiment keeps track of the next event from each of its traces so it
 * can pick the next one in chronological order.
 * <p>
 * This implies that the "next" event from each trace has already been read and
 * that we at least know its timestamp.
 * <p>
 * The last trace refers to the trace from which the last event was "consumed"
 * at the experiment level.
 */
public final class TmfExperimentContext extends TmfContext {

    /**
     * Inline class describing the contexts, containing the index of the trace
     * in the experiment, its content and current event.
     */
    public class ContextTuple implements Comparable<ContextTuple>{

        private final int fIndex;
        private final @NonNull ITmfEvent fEvent;
        private final @NonNull ITmfContext fContext;

        private ContextTuple(int index, @NonNull ITmfEvent event, @NonNull ITmfContext context) {
            fIndex = index;
            fEvent = event;
            fContext = context;
        }

        /**
         * Getter for the experiment index.
         *
         * @return this ContextTuple's index in the enclosing experiment.
         */
        public int getIndex() {
            return fIndex;
        }

        /**
         * Getter for the current event
         *
         * @return this ContextTuple's current event
         */
        public @NonNull ITmfEvent getEvent() {
            return fEvent;
        }

        /**
         * Getter for the context
         *
         * @return this ContextTuple's context field
         */
        public @NonNull ITmfContext getContext() {
            return fContext;
        }

        @Override
        public int compareTo(ContextTuple o) {
            int timeStampComparison = fEvent.getTimestamp().compareTo(o.fEvent.getTimestamp());
            if (timeStampComparison != 0) {
                return timeStampComparison;
            }
            /**
             * if two context tuple's events have the same timestamp, compare
             * their index in the context to resolve indetermination.
             */
            return Integer.compare(fIndex, o.fIndex);
        }
    }

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final ITmfContext[] fContexts;
    /**
     * fPriority is a PriorityQueue of initial capacity fContexts.length,
     * ordered by increasing timestamps of the ContextTuple events.
     */
    private final PriorityQueue<ContextTuple> fPriority;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Standard constructor
     *
     * @param nbTraces
     *            The number of traces in the experiment
     */
    public TmfExperimentContext(int nbTraces) {
        super();
        if (nbTraces < 0) {
            throw new IllegalArgumentException("TmfExperimentContext size cannot be negative"); //$NON-NLS-1$
        }
        fContexts = new ITmfContext[nbTraces];
        fPriority = new PriorityQueue<>(Math.max(1, nbTraces), null);
    }

    @Override
    public void dispose() {
        for (ITmfContext context : fContexts) {
            context.dispose();
        }
        super.dispose();
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Set the context and event of a trace
     *
     * @param traceIndex
     *            The index of the trace in the experiment
     * @param ctx
     *            The new context object for that trace
     * @param event
     *            The event at the context in the trace
     */
    public void setContent(int traceIndex, ITmfContext ctx, ITmfEvent event) {
        fContexts[traceIndex] = ctx;
        if (event != null && ctx != null) {
            fPriority.add(new ContextTuple(traceIndex, event, ctx));
        }
    }

    /**
     * Get the tuple containing the index, context and event of the for the
     * trace with the earliest event, and the trace with the lowest index in the
     * Experiment in case of indetermination.
     *
     * @return The information concerning the next trace in the context's
     *         priority queue.
     */
    public ContextTuple getNext() {
        return fPriority.poll();
    }

    /**
     * Getter for the sub contexts of this experiment context.
     *
     * @return a copy of the array of contexts
     */
    @VisibleForTesting
    public ITmfContext[] getContexts() {
        return Arrays.copyOf(fContexts, fContexts.length);
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        return Arrays.hashCode(fContexts);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!super.equals(other)) {
            return false;
        }
        if (!(other instanceof TmfExperimentContext)) {
            return false;
        }
        TmfExperimentContext o = (TmfExperimentContext) other;
        return Arrays.equals(fContexts, o.fContexts);
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        StringBuilder sb = new StringBuilder("TmfExperimentContext [\n");
        sb.append("\tfLocation=" + getLocation() + ", fRank=" + getRank() + "\n");
        sb.append("\tfContexts=[");
        for (int i = 0; i < fContexts.length; i++) {
            sb.append("(" + fContexts[i].getLocation() + "," + fContexts[i].getRank() + ((i < fContexts.length - 1) ? ")," : ")]\n"));
        }
        sb.append("]");
        return sb.toString();
    }

}
