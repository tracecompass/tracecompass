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
public class TmfExperimentContext extends TmfContext {

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

    private ITmfContext[] fContexts;
    private ITmfEvent[] fEvents;
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
        fContexts = new ITmfContext[nbTraces];
        fEvents = new ITmfEvent[nbTraces];
        fLastTraceRead = NO_TRACE;
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
     * Get the trace contexts composing this experiment context.
     *
     * @return The array of trace contexts
     */
    public ITmfContext[] getContexts() {
        return fContexts;
    }

    /**
     * Get the trace events located at this experiment context's location.
     *
     * @return The array of trace events
     */
    public ITmfEvent[] getEvents() {
        return fEvents;
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
        for (int i = 0; i < fContexts.length; i++) {
            result = 37 * result + fContexts[i].hashCode();
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
        while (isEqual && (i < fContexts.length)) {
            isEqual &= fContexts[i].equals(o.fContexts[i]);
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
        for (int i = 0; i < fContexts.length; i++) {
            sb.append("(" + fContexts[i].getLocation() + "," + fContexts[i].getRank() + ((i < fContexts.length - 1) ? ")," : ")]\n"));
        }
        sb.append("\tfEvents=[");
        for (int i = 0; i < fEvents.length; i++) {
            ITmfEvent event = fEvents[i];
            sb.append(((event != null) ? fEvents[i].getTimestamp() : "(null)")  + ((i < fEvents.length - 1) ? "," : "]\n"));
        }
        sb.append("\tfLastTraceRead=" + fLastTraceRead + "\n");
        sb.append("]");
        return sb.toString();
    }

}
