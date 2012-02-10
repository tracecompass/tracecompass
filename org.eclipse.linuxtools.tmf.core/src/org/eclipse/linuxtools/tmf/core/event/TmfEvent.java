/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.event;

import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * <b><u>TmfEvent</u></b>
 * <p>
 * The basic event structure in the TMF. In its canonical form, an event has:
 * <ul>
 * <li> a normalized timestamp
 * <li> a source (reporter)
 * <li> a type
 * <li> a content
 * </ul>
 * For convenience, a free-form reference field is also provided. It could be
 * used as e.g. a location marker in the event stream to distinguish between
 * otherwise identical events.
 * 
 * Notice that for performance reasons TmfEvent is NOT immutable. If a copy
 * of the event is needed, use the copy constructor.
 */
public class TmfEvent extends TmfData implements Cloneable {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    public static final TmfEvent NullEvent = new TmfEvent();

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

	protected ITmfTrace<?>      fParentTrace;
	protected long              fEventRank;
	protected TmfTimestamp      fEffectiveTimestamp;
	protected TmfTimestamp      fOriginalTimestamp;
	protected TmfEventSource    fSource;
	protected TmfEventType      fType;
	protected TmfEventReference fReference;

    // Content requires a reference to the parent event so it is initialized
    // using setContent()
    protected TmfEventContent fContent;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * @param trace the parent trace
     * @param originalTS the original timestamp
     * @param effectiveTS the effective timestamp
     * @param source the event source (generator)
     * @param type the event type
     * @param reference a free-form reference field
     */
    public TmfEvent(ITmfTrace<?> trace, long rank, TmfTimestamp originalTS, TmfTimestamp effectiveTS,
            TmfEventSource source, TmfEventType type, TmfEventReference reference)
    {
        fParentTrace        = trace;
        fEventRank          = rank;
        fOriginalTimestamp  = originalTS;
        fEffectiveTimestamp = effectiveTS;
        fSource             = source;
        fType               = type;
        fReference          = reference;
    }

	/**
	 * @param originalTS the original timestamp
	 * @param effectiveTS the effective timestamp
	 * @param source the event source (generator)
	 * @param type the event type
	 * @param reference a free-form reference field
	 */
	public TmfEvent(TmfTimestamp originalTS, TmfTimestamp effectiveTS,
			TmfEventSource source, TmfEventType type, TmfEventReference reference)
	{
        this(null, -1, originalTS, effectiveTS, source, type, reference);
	}

    /**
     * @param trace the parent trace
     * @param timestamp the effective timestamp
     * @param source the event source (generator)
     * @param type the event type
     * @param reference a free-form reference field
     */
    public TmfEvent(ITmfTrace<?> parentTrace, TmfTimestamp timestamp, TmfEventSource source,
            TmfEventType type, TmfEventReference reference)
    {
        this(parentTrace, -1, timestamp, timestamp, source, type, reference);
    }

	/**
	 * @param timestamp the effective timestamp
	 * @param source the event source (generator)
	 * @param type the event type
	 * @param reference a free-form reference field
	 */
	public TmfEvent(TmfTimestamp timestamp, TmfEventSource source,
			TmfEventType type, TmfEventReference reference)
	{
		this(null, -1, timestamp, timestamp, source, type, reference);
	}

	/**
	 * Copy constructor
	 * 
	 * @param other the original event
	 */
	public TmfEvent(TmfEvent other) {
    	if (other == null)
    		throw new IllegalArgumentException();
        fParentTrace        = other.fParentTrace;
        fEventRank          = other.fEventRank;
		fOriginalTimestamp  = other.fOriginalTimestamp != null ? new TmfTimestamp(other.fOriginalTimestamp) : null;
		fEffectiveTimestamp = other.fEffectiveTimestamp != null ? new TmfTimestamp(other.fEffectiveTimestamp) : null;
		fSource    			= other.fSource != null ? new TmfEventSource(other.fSource) : null;
		fType      			= other.fType != null ? new TmfEventType(other.fType) : null;
		fContent   			= other.fContent != null ? new TmfEventContent(other.fContent) : null;
		fReference			= other.fReference != null ? new TmfEventReference(other.fReference) : null;
	}

    public TmfEvent() {
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @return the parent trace
     */
    public ITmfTrace<?> getParentTrace() {
        return fParentTrace;
    }

    /**
     * @return the event rank
     */
    public long getEventRank() {
        return fEventRank;
    }

    /**
     * @return the effective event timestamp
     */
    public TmfTimestamp getTimestamp() {
        return fEffectiveTimestamp;
    }

    /**
     * @return the original event timestamp
     */
    public TmfTimestamp getOriginalTimestamp() {
        return fOriginalTimestamp;
    }

    /**
     * @return the event source
     */
    public TmfEventSource getSource() {
        return fSource;
    }

    /**
     * @return the event type
     */
    public TmfEventType getType() {
        return fType;
    }

    /**
     * @return the event content
     */
    public TmfEventContent getContent() {
        return fContent;
    }

    /**
     * @return the event reference
     */
    public TmfEventReference getReference() {
        return fReference;
    }

    /**
     * @param content
     *            the new event content
     */
    public void setContent(TmfEventContent content) {
        fContent = content;
    }

    /**
     * @return the event raw text
     */
    public String getRawText() {
        if (fContent != null && fContent.getContent() != null) {
            return fContent.getContent().toString();
        }
        return ""; //$NON-NLS-1$
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + (fSource != null ? fSource.hashCode() : 0);
        result = 37 * result + (fType != null ? fType.hashCode() : 0);
        result = 37 * result + (fEffectiveTimestamp != null ? fEffectiveTimestamp.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof TmfEvent))
            return false;
        TmfEvent o = (TmfEvent) other;
        if (fEffectiveTimestamp == null) {
            if (o.fEffectiveTimestamp != null) {
                return false;
            }
        } else {
            if (!fEffectiveTimestamp.equals(o.fEffectiveTimestamp)) {
                return false;
            }
        }
        if (fSource == null) {
            if (o.fSource != null) {
                return false;
            }
        } else {
            if (!fSource.equals(o.fSource)) {
                return false;
            }
        }
        if (fType == null) {
            if (o.fType != null) {
                return false;
            }
        } else {
            if (!fType.equals(o.fType)) {
                return false;
            }
        }
        if (fContent == null) {
            if (o.fContent != null) {
                return false;
            }
        } else {
            if (!fContent.equals(o.fContent)) {
                return false;
            }
        }
        return true;
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "[TmfEvent(" + fEffectiveTimestamp + "," + fSource + "," + fType + "," + fContent + ")]";
    }

	@Override
	public TmfEvent clone() {
		TmfEvent clone = null;
		try {
			clone = (TmfEvent) super.clone();
			clone.fParentTrace        = fParentTrace;
			clone.fEventRank          = fEventRank;
			clone.fOriginalTimestamp  = fOriginalTimestamp != null ? fOriginalTimestamp.clone() : null;
			clone.fEffectiveTimestamp = fEffectiveTimestamp != null ? fEffectiveTimestamp.clone() : null;
			clone.fSource             = fSource != null ? fSource.clone() : null;
			clone.fType               = fType != null ? fType.clone() : null;
			clone.fReference          = fReference != null ? fReference.clone() : null;
			clone.fContent            = fContent != null ? fContent.clone() : null;
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

        return clone;
    }

}
