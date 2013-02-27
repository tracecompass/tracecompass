/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
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

import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
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
     * @since 2.0
     */
    public TmfLostEvent(final ITmfTrace trace, final long rank, final ITmfTimestamp timestamp,
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
        super(  event.getTrace(),
                event.getRank(),
                event.getTimestamp(),
                event.getSource(),
                event.getType(),
                event.getContent(),
                event.getReference());
        fTimeRange = event.getTimeRange();
        fNbLostEvents = event.getNbLostEvents();
    }

    // ------------------------------------------------------------------------
    // ITmfLostEvent
    // ------------------------------------------------------------------------

    /**
     * @since 2.0
     */
    @Override
    public TmfTimeRange getTimeRange() {
        return fTimeRange;
    }

    @Override
    public long getNbLostEvents() {
        return fNbLostEvents;
    }

    // ------------------------------------------------------------------------
    // Convenience setters
    // ------------------------------------------------------------------------

    /**
     * @param timeRange the 'problematic' time range
     * @since 2.0
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
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (fNbLostEvents ^ (fNbLostEvents >>> 32));
        result = prime * result + ((fTimeRange == null) ? 0 : fTimeRange.hashCode());
        return result;
    }

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

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "TmfLostEvent [Event=" + super.toString() + ", fTimeRange=" + fTimeRange + ", fNbLostEvents=" + fNbLostEvents + "]";
    }

}
