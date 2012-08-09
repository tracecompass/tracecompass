/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.event;

import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * A basic implementation of ITmfLostEvent.
 *
 * @author Francois Chouinard
 * @version 1.0
 * @since 1.2
*/
public class TmfLostEvent extends TmfEvent implements ITmfLostEvent {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private TmfTimeRange fTimeRange;
    private long fNbLostEvents;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor which boils down to the default TmfEvent with no
     * lost event over the empty time range.
     */
    public TmfLostEvent() {
        this(null, ITmfContext.UNKNOWN_RANK, null, null, null, null, TmfTimeRange.NULL_RANGE, 0);
    }

    /**
     * Full constructor
     *
     * @param trace the parent trace
     * @param rank the event rank (in the trace)
     * @param timestamp the event timestamp
     * @param source the event source
     * @param type the event type
     * @param reference the event reference
     * @param timeRange the 'problematic' time range
     * @param nbLostEvents the number of lost events in the time range
     */
    public TmfLostEvent(final ITmfTrace<? extends ITmfEvent> trace, final long rank, final ITmfTimestamp timestamp,
            final String source, final ITmfEventType type, final String reference, final TmfTimeRange timeRange, final long nbLostEvents)
    {
        super(trace, rank, timestamp, source, type, null, reference);
        fTimeRange = timeRange;
        fNbLostEvents = nbLostEvents;
    }

    /**
     * Copy constructor
     *
     * @param event the original event
     */
    public TmfLostEvent(final ITmfLostEvent event) {
        if (event == null) {
            throw new IllegalArgumentException();
        }
        setTrace(event.getTrace());
        setRank(event.getRank());
        setTimestamp(event.getTimestamp());
        setSource(event.getSource());
        setType(event.getType());
        setContent(event.getContent());
        setReference(event.getReference());

        fTimeRange = event.getTimeRange();
        fNbLostEvents = event.getNbLostEvents();
    }

    // ------------------------------------------------------------------------
    // ITmfLostEvent
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfLostEvent#getTimeRange()
     */
    @Override
    public TmfTimeRange getTimeRange() {
        return fTimeRange;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfLostEvent#getNbLostEvents()
     */
    @Override
    public long getNbLostEvents() {
        return fNbLostEvents;
    }

    // ------------------------------------------------------------------------
    // Convenience setters
    // ------------------------------------------------------------------------

    /**
     * @param timeRange the 'problematic' time range
     */
    protected void setTimeRange(final TmfTimeRange timeRange) {
        fTimeRange = timeRange;
    }

    /**
     * @param nbLostEvents the number of lost events
     */
    protected void setNbLostEvents(final long nbLostEvents) {
        fNbLostEvents = nbLostEvents;
    }

    // ------------------------------------------------------------------------
    // Cloneable
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public TmfLostEvent clone() {
        TmfLostEvent clone = null;
        try {
            clone = (TmfLostEvent) super.clone();
            clone.fTimeRange = fTimeRange.clone();
            clone.fNbLostEvents = fNbLostEvents;
        } catch (CloneNotSupportedException e) {
        }
        return clone;
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (fNbLostEvents ^ (fNbLostEvents >>> 32));
        result = prime * result + ((fTimeRange == null) ? 0 : fTimeRange.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof TmfLostEvent)) {
            return false;
        }
        TmfLostEvent other = (TmfLostEvent) obj;
        if (fNbLostEvents != other.fNbLostEvents) {
            return false;
        }
        if (fTimeRange == null) {
            if (other.fTimeRange != null) {
                return false;
            }
        } else if (!fTimeRange.equals(other.fTimeRange)) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "TmfLostEvent [Event=" + super.toString() + ", fTimeRange=" + fTimeRange + ", fNbLostEvents=" + fNbLostEvents + "]";
    }

}
