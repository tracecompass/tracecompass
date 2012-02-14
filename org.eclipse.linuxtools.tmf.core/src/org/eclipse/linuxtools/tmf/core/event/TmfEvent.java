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
 * A basic implementation of ITmfEvent.
 * 
 * Note that for performance reasons TmfEvent is NOT immutable. If a copy of
 * the event is needed, use the copy constructor.
 */
public class TmfEvent implements ITmfEvent {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    protected ITmfTrace<? extends TmfEvent> fTrace;
    protected long fRank;
    protected ITmfTimestamp fTimestamp;
    protected String fSource;
    protected ITmfEventType fType;
    protected ITmfEventField fContent;
    protected String fReference;

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
     * @param rank the event rank (in the trace)
     * @param timestamp the event timestamp
     * @param source the event source
     * @param type the event type
     * @param type the event content (payload)
     * @param reference the event reference
     */
    public TmfEvent(ITmfTrace<? extends TmfEvent> trace, long rank, ITmfTimestamp timestamp, String source,
                    TmfEventType type, ITmfEventField content, String reference)
    {
        fTrace = trace;
        fRank = rank;
        fTimestamp = timestamp;
        fSource = source;
        fType = type;
        fContent = content;
        fReference = reference;
    }

    /**
     * Constructor - no rank
     */
    public TmfEvent(ITmfTrace<? extends TmfEvent> trace, ITmfTimestamp timestamp, String source,
            TmfEventType type, ITmfEventField content, String reference)
    {
        this(trace, -1, timestamp, source, type, content, reference);
    }

    /**
     * Constructor - no rank, no content
     */
    public TmfEvent(ITmfTrace<? extends TmfEvent> trace, ITmfTimestamp timestamp, String source,
            TmfEventType type, String reference)
    {
        this(trace, -1, timestamp, source, type, null, reference);
    }

    /**
     * Constructor - no rank, no content, no trace
     */
    public TmfEvent(TmfTimestamp timestamp, String source, TmfEventType type, String reference)
    {
        this(null, -1, timestamp, source, type, null, reference);
    }

    /**
     * Copy constructor
     * 
     * @param event the original event
     */
    public TmfEvent(TmfEvent event) {
        if (event == null)
            throw new IllegalArgumentException();
        fTrace = event.fTrace;
        fRank = event.fRank;
        fTimestamp = event.fTimestamp;
        fSource = event.fSource;
        fType = event.fType;
        fContent = event.fContent;
        fReference = event.fReference;
    }

    // ------------------------------------------------------------------------
    // ITmfEvent
    // ------------------------------------------------------------------------

    public ITmfTrace<? extends TmfEvent> getTrace() {
        return fTrace;
    }

    public long getRank() {
        return fRank;
    }

    public ITmfTimestamp getTimestamp() {
        return fTimestamp;
    }

    public String getSource() {
        return fSource;
    }

    public ITmfEventType getType() {
        return fType;
    }

    public ITmfEventField getContent() {
        return fContent;
    }

    public String getReference() {
        return fReference;
    }

    // ------------------------------------------------------------------------
    // Convenience setters
    // ------------------------------------------------------------------------

    /**
     * @param source the new event source
     */
    public void setSource(String source) {
        fSource = source;
    }

    /**
     * @param timestamp the new event timestamp
     */
    public void setTimestamp(ITmfTimestamp timestamp) {
        fTimestamp = timestamp;
    }

    /**
     * @param type the new event type
     */
    public void setType(TmfEventType type) {
        fType = type;
    }

    /**
     * @param content the event new content
     */
    public void setContent(ITmfEventField content) {
        fContent = content;
    }

    /**
     * @param reference the new event reference
     */
    public void setReference(String reference) {
        fReference = reference;
    }

    // ------------------------------------------------------------------------
    // Cloneable
    // ------------------------------------------------------------------------

    @Override
    public TmfEvent clone() {
        TmfEvent clone = null;
        try {
            clone = (TmfEvent) super.clone();
            clone.fTrace = fTrace;
            clone.fRank = fRank;
            clone.fTimestamp = fTimestamp != null ? fTimestamp.clone() : null;
            clone.fSource = fSource;
            clone.fType = fType != null ? fType.clone() : null;
            clone.fContent = fContent != null ? fContent.clone() : null;
            clone.fReference = fReference;
        } catch (CloneNotSupportedException e) {
        }
        return clone;
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fTrace == null) ? 0 : fTrace.hashCode());
        result = prime * result + (int) (fRank ^ (fRank >>> 32));
        result = prime * result + ((fTimestamp == null) ? 0 : fTimestamp.hashCode());
        result = prime * result + ((fSource == null) ? 0 : fSource.hashCode());
        result = prime * result + ((fType == null) ? 0 : fType.hashCode());
        result = prime * result + ((fContent == null) ? 0 : fContent.hashCode());
        result = prime * result + ((fReference == null) ? 0 : fReference.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TmfEvent other = (TmfEvent) obj;
        if (fTrace == null) {
            if (other.fTrace != null)
                return false;
        } else if (!fTrace.equals(other.fTrace))
            return false;
        if (fRank != other.fRank)
            return false;
        if (fTimestamp == null) {
            if (other.fTimestamp != null)
                return false;
        } else if (!fTimestamp.equals(other.fTimestamp))
            return false;
        if (fSource == null) {
            if (other.fSource != null)
                return false;
        } else if (!fSource.equals(other.fSource))
            return false;
        if (fType == null) {
            if (other.fType != null)
                return false;
        } else if (!fType.equals(other.fType))
            return false;
        if (fContent == null) {
            if (other.fContent != null)
                return false;
        } else if (!fContent.equals(other.fContent))
            return false;
        if (fReference == null) {
            if (other.fReference != null)
                return false;
        } else if (!fReference.equals(other.fReference))
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
