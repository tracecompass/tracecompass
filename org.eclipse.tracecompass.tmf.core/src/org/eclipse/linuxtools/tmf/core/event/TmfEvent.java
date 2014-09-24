/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Francois Chouinard - Initial API and implementation
 *     Francois Chouinard - Updated as per TMF Event Model 1.0
 *     Alexandre Montplaisir - Made immutable
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.event;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * A basic implementation of ITmfEvent.
 *
 * @version 1.0
 * @author Francois Chouinard
 *
 * @see ITmfTimestamp
 * @see ITmfEventType
 * @see ITmfEventField
 * @see ITmfTrace
 */
public class TmfEvent extends PlatformObject implements ITmfEvent {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final ITmfTrace fTrace;
    private final long fRank;
    private final ITmfTimestamp fTimestamp;
    private final String fSource;
    private final ITmfEventType fType;
    private final ITmfEventField fContent;
    private final String fReference;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor. All fields have their default value (null) and the
     * event rank is set to TmfContext.UNKNOWN_RANK.
     */
    public TmfEvent() {
        this(null, ITmfContext.UNKNOWN_RANK, null, null, null, null, null);
    }

    /**
     * Standard constructor. The event rank will be set to TmfContext.UNKNOWN_RANK.
     *
     * @param trace the parent trace
     * @param timestamp the event timestamp
     * @param source the event source
     * @param type the event type
     * @param content the event content (payload)
     * @param reference the event reference
     * @since 2.0

     */
    public TmfEvent(final ITmfTrace trace, final ITmfTimestamp timestamp, final String source,
            final ITmfEventType type, final ITmfEventField content, final String reference)
    {
        this(trace, ITmfContext.UNKNOWN_RANK, timestamp, source, type, content, reference);
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
     * @since 2.0
     */
    public TmfEvent(final ITmfTrace trace, final long rank, final ITmfTimestamp timestamp, final String source,
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

    @Override
    public ITmfTrace getTrace() {
        return fTrace;
    }

    @Override
    public long getRank() {
        return fRank;
    }

    /**
     * @since 2.0
     */
    @Override
    public ITmfTimestamp getTimestamp() {
        return fTimestamp;
    }

    @Override
    public String getSource() {
        return fSource;
    }

    @Override
    public ITmfEventType getType() {
        return fType;
    }

    @Override
    public ITmfEventField getContent() {
        return fContent;
    }

    @Override
    public String getReference() {
        return fReference;
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

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return getClass().getSimpleName() + " [fTimestamp=" + getTimestamp()
                + ", fTrace=" + getTrace() + ", fRank=" + getRank()
                + ", fSource=" + getSource() + ", fType=" + getType()
                + ", fContent=" + getContent() + ", fReference=" + getReference()
                + "]";
    }

}
