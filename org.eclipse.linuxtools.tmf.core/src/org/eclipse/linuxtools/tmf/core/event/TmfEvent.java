/*******************************************************************************
 * Copyright (c) 2009, 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Updated as per TMF Event Model 1.0
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.event;

import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * <b><u>TmfEvent</u></b>
 * <p>
 * The basic event structure in the TMF. In its canonical form, an event has:
 * <ul>
 * <li>a normalized timestamp
 * <li>a source (reporter)
 * <li>a type
 * <li>a content
 * </ul>
 * For convenience, a free-form reference field is also provided. It could be
 * used as e.g. a location marker in the event stream to distinguish between
 * otherwise identical events.
 * 
 * Notice that for performance reasons TmfEvent is NOT immutable. If a copy of
 * the event is needed, use the copy constructor.
 */
public class TmfEvent extends TmfDataEvent implements Cloneable {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    protected ITmfTimestamp fTimestamp;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public TmfEvent() {
    }

    /**
     * Full constructor
     * 
     * @param trace the parent trace
     * @param rank the vent rank (in the trace)
     * @param timestamp the event timestamp
     * @param source the event source
     * @param type the event type
     * @param reference the event reference
     */
    public TmfEvent(ITmfTrace<?> trace, long rank, ITmfTimestamp timestamp, String source,
                    TmfEventType type, String reference)
    {
        super(trace, rank, source, type, null, reference);
        fTimestamp = timestamp;
    }

    /**
     * Constructor - no rank
     */
    public TmfEvent(ITmfTrace<?> parentTrace, ITmfTimestamp timestamp, String source,
                    TmfEventType type, String reference)
    {
        this(parentTrace, -1, timestamp, source, type, reference);
    }

    /**
     * Constructor - no trace, no rank
     */
    public TmfEvent(ITmfTimestamp timestamp, String source, TmfEventType type, String reference) {
        this(null, -1, timestamp, source, type, reference);
    }

    /**
     * Copy constructor
     * 
     * @param event the original event
     */
    public TmfEvent(TmfEvent event) {
        super(event);
        fTimestamp = event.fTimestamp != null ? event.fTimestamp.clone() : null;
    }

    // ------------------------------------------------------------------------
    // ITmfEvent
    // ------------------------------------------------------------------------

    /**
     * @return the effective event timestamp
     */
    public ITmfTimestamp getTimestamp() {
        return fTimestamp;
    }

    // ------------------------------------------------------------------------
    // Cloneable
    // ------------------------------------------------------------------------

    @Override
    public TmfEvent clone() {
        TmfEvent clone = null;
        clone = (TmfEvent) super.clone();
        clone.fTimestamp = fTimestamp != null ? fTimestamp.clone() : null;
        return clone;
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((fTimestamp == null) ? 0 : fTimestamp.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        TmfEvent other = (TmfEvent) obj;
        if (fTimestamp == null) {
            if (other.fTimestamp != null)
                return false;
        } else if (!fTimestamp.equals(other.fTimestamp))
            return false;
        return true;
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "TmfEvent [fTimestamp=" + fTimestamp + ", fTrace=" + fTrace + ", fRank=" + fRank
                        + ", fSource=" + fSource + ", fType=" + fType + ", fContent=" + fContent
                        + ", fReference=" + fReference + "]";
    }

}
