/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Put in shape for 1.0
 *   Patrick Tasse - Updated for removal of context clone
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.trace;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;

/**
 * The experiment context in TMF.
 * <p>
 * The experiment keeps track of the next event from each of its traces so it
 * can pick the next one in chronological order.
 * <p>
 * This implies that the "next" event from each trace has already been
 * read and that we at least know its timestamp.
 * <p>
 * The last trace refers to the trace from which the last event was "consumed"
 * at the experiment level.
 */
public final class TmfExperimentContext extends TmfContext {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * No last trace read indicator
     */
    public static final int NO_TRACE = -1;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final List<ITmfContext> fContexts;
    private final List<ITmfEvent> fEvents;
    private int fLastTraceRead;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Standard constructor
     *
     * @param nbTraces
     *            The number of traces in the experiment
     */
    public TmfExperimentContext(final int nbTraces) {
        super();
        fLastTraceRead = NO_TRACE;
        fContexts = new ArrayList<>(nbTraces);
        fEvents = new ArrayList<>(nbTraces);


        /* Initialize the arrays to the requested size */
        for (int i = 0; i < nbTraces; i++) {
            fContexts.add(null);
            fEvents.add(null);
        }
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
     * Return how many traces this experiment context tracks the contexts of
     * (a.k.a., the number of traces in the experiment).
     *
     * @return The number of traces in the experiment
     */
    public int getNbTraces() {
        return fContexts.size();
    }

    /**
     * Get the current context of a specific trace
     *
     * @param traceIndex
     *            The index of the trace in the experiment
     * @return The matching context object for that trace
     */
    @Nullable
    public ITmfContext getContext(int traceIndex) {
        return fContexts.get(traceIndex);
    }

    /**
     * Set the context of a trace
     *
     * @param traceIndex
     *            The index of the trace in the experiment
     * @param ctx
     *            The new context object for that trace
     */
    public void setContext(int traceIndex, ITmfContext ctx) {
        fContexts.set(traceIndex, ctx);
    }

    /**
     * Get the current event for a specific trace in the experiment.
     *
     * @param traceIndex
     *            The index of the trace in the experiment
     * @return The event matching the trace/context
     *
     */
    @Nullable
    public ITmfEvent getEvent(int traceIndex) {
        return fEvents.get(traceIndex);
    }

    /**
     * Set the context's event for a specific trace
     *
     * @param traceIndex
     *            The index of the trace in the experiment
     * @param event
     *            The event at the context in the trace
     */
    public void setEvent(int traceIndex, ITmfEvent event) {
        fEvents.set(traceIndex, event);
    }

    /**
     * Get the index of the trace that was last read (so the trace whose
     * current context will match this experiment's).
     *
     * @return The index of the trace
     */
    public int getLastTrace() {
        return fLastTraceRead;
    }

    /**
     * Set the last trace read index
     *
     * @param newIndex
     *            The new value to assign
     */
    public void setLastTrace(final int newIndex) {
        fLastTraceRead = newIndex;
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        int result = 17;
        for (int i = 0; i < fContexts.size(); i++) {
            result = 37 * result + fContexts.get(i).hashCode();
        }
        return result;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!super.equals(other)) {
            return false;
        }
        if (!(other instanceof TmfExperimentContext)) {
            return false;
        }
        final TmfExperimentContext o = (TmfExperimentContext) other;
        boolean isEqual = true;
        int i = 0;
        while (isEqual && (i < fContexts.size())) {
            isEqual &= fContexts.get(i).equals(o.fContexts.get(i));
            i++;
        }
        return isEqual;
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        StringBuilder sb = new StringBuilder("TmfExperimentContext [\n");
        sb.append("\tfLocation=" + getLocation() + ", fRank=" + getRank() + "\n");
        sb.append("\tfContexts=[");
        for (int i = 0; i < fContexts.size(); i++) {
            sb.append("(" + fContexts.get(i).getLocation() + "," + fContexts.get(i).getRank() + ((i < fContexts.size() - 1) ? ")," : ")]\n"));
        }
        sb.append("\tfEvents=[");
        for (int i = 0; i < fEvents.size(); i++) {
            ITmfEvent event = fEvents.get(i);
            sb.append(((event != null) ? fEvents.get(i).getTimestamp() : "(null)")  + ((i < fEvents.size() - 1) ? "," : "]\n"));
        }
        sb.append("\tfLastTraceRead=" + fLastTraceRead + "\n");
        sb.append("]");
        return sb.toString();
    }

}
