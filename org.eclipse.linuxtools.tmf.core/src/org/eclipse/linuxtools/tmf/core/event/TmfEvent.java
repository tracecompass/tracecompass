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
 * A basic implementation of ITmfEvent.
 * <p>
 * Note that for performance reasons TmfEvent is NOT immutable. If a shallow
 * copy of the event is needed, use the copy constructor. Otherwise (deep copy)
 * use clone().
 * 
 * @since 1.0
 * @version 1.0
 * @author Francois Chouinard
 * @see ITmfTimestamp
 * @see ITmfEventType
 * @see ITmfEventField
 * @see ITmfTrace
*/
public class TmfEvent implements ITmfEvent {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private ITmfTrace<? extends ITmfEvent> fTrace;
    private long fRank;
    private ITmfTimestamp fTimestamp;
    private String fSource;
    private ITmfEventType fType;
    private ITmfEventField fContent;
    private String fReference;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public TmfEvent() {
        this(null, -1, null, null, null, null, null);
    }

    /**
     * Constructor - no rank
     */
    public TmfEvent(final ITmfTrace<? extends ITmfEvent> trace, final ITmfTimestamp timestamp, final String source,
            final ITmfEventType type, final ITmfEventField content, final String reference)
    {
        this(trace, -1, timestamp, source, type, content, reference);
    }

    /**
     * Constructor - no rank, no content
     */
    public TmfEvent(final ITmfTrace<? extends ITmfEvent> trace, final ITmfTimestamp timestamp, final String source,
            final ITmfEventType type, final String reference)
    {
        this(trace, -1, timestamp, source, type, null, reference);
    }

    /**
     * Constructor - no rank, no content, no trace
     */
    public TmfEvent(final ITmfTimestamp timestamp, final String source, final ITmfEventType type, final String reference)
    {
        this(null, -1, timestamp, source, type, null, reference);
    }

    /**
     * Full constructor
     * 
     * @param trace the parent trace
     * @param rank the event rank (in the trace)
     * @param timestamp the event timestamp
     * @param source the event source
     * @param type the event type
     * @param content the event content (payload)
     * @param reference the event reference
     */
    public TmfEvent(final ITmfTrace<? extends ITmfEvent> trace, final long rank, final ITmfTimestamp timestamp, final String source,
            final ITmfEventType type, final ITmfEventField content, final String reference)
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
     * Copy constructor
     * 
     * @param event the original event
     */
    public TmfEvent(final ITmfEvent event) {
        if (event == null) {
            throw new IllegalArgumentException();
        }
        fTrace = event.getTrace();
        fRank = event.getRank();
        fTimestamp = event.getTimestamp();
        fSource = event.getSource();
        fType = event.getType();
        fContent = event.getContent();
        fReference = event.getReference();
    }

    // ------------------------------------------------------------------------
    // ITmfEvent
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfEvent#getTrace()
     */
    @Override
    public ITmfTrace<? extends ITmfEvent> getTrace() {
        return fTrace;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfEvent#getRank()
     */
    @Override
    public long getRank() {
        return fRank;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfEvent#getTimestamp()
     */
    @Override
    public ITmfTimestamp getTimestamp() {
        return fTimestamp;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfEvent#getSource()
     */
    @Override
    public String getSource() {
        return fSource;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfEvent#getType()
     */
    @Override
    public ITmfEventType getType() {
        return fType;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfEvent#getContent()
     */
    @Override
    public ITmfEventField getContent() {
        return fContent;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfEvent#getReference()
     */
    @Override
    public String getReference() {
        return fReference;
    }

    // ------------------------------------------------------------------------
    // Convenience setters
    // ------------------------------------------------------------------------

    /**
     * @param trace the new event trace
     */
    protected void setTrace(final ITmfTrace<? extends ITmfEvent> trace) {
        fTrace = trace;
    }

    /**
     * @param rank the new event rank
     */
    protected void setRank(final long rank) {
        fRank = rank;
    }

    /**
     * @param timestamp the new event timestamp
     */
    protected void setTimestamp(final ITmfTimestamp timestamp) {
        fTimestamp = timestamp;
    }

    /**
     * @param source the new event source
     */
    protected void setSource(final String source) {
        fSource = source;
    }

    /**
     * @param type the new event type
     */
    protected void setType(final ITmfEventType type) {
        fType = type;
    }

    /**
     * @param content the event new content
     */
    protected void setContent(final ITmfEventField content) {
        fContent = content;
    }

    /**
     * @param reference the new event reference
     */
    protected void setReference(final String reference) {
        fReference = reference;
    }

    // ------------------------------------------------------------------------
    // Cloneable
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
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
        } catch (final CloneNotSupportedException e) {
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

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TmfEvent)) {
            return false;
        }
        final TmfEvent other = (TmfEvent) obj;
        if (fTrace == null) {
            if (other.fTrace != null) {
                return false;
            }
        } else if (!fTrace.equals(other.fTrace)) {
            return false;
        }
        if (fRank != other.fRank) {
            return false;
        }
        if (fTimestamp == null) {
            if (other.fTimestamp != null) {
                return false;
            }
        } else if (!fTimestamp.equals(other.fTimestamp)) {
            return false;
        }
        if (fSource == null) {
            if (other.fSource != null) {
                return false;
            }
        } else if (!fSource.equals(other.fSource)) {
            return false;
        }
        if (fType == null) {
            if (other.fType != null) {
                return false;
            }
        } else if (!fType.equals(other.fType)) {
            return false;
        }
        if (fContent == null) {
            if (other.fContent != null) {
                return false;
            }
        } else if (!fContent.equals(other.fContent)) {
            return false;
        }
        if (fReference == null) {
            if (other.fReference != null) {
                return false;
            }
        } else if (!fReference.equals(other.fReference)) {
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
        return "TmfEvent [fTimestamp=" + fTimestamp + ", fTrace=" + fTrace + ", fRank=" + fRank
                + ", fSource=" + fSource + ", fType=" + fType + ", fContent=" + fContent
                + ", fReference=" + fReference + "]";
    }

}
