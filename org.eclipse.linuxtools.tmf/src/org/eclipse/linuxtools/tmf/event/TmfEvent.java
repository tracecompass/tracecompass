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

package org.eclipse.linuxtools.tmf.event;

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
public class TmfEvent extends TmfData {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

	protected TmfTimestamp      fEffectiveTimestamp;
	protected TmfTimestamp      fOriginalTimestamp;
	protected TmfEventSource    fSource;
	protected TmfEventType      fType;
	protected TmfEventReference fReference;

	// Content requires a reference to the parent event so it is initialized
	// using setContent()
	protected TmfEventContent   fContent;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

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
		if (source == null || type == null || effectiveTS == null) {
    		throw new IllegalArgumentException();
		}
		fOriginalTimestamp  = originalTS;
		fEffectiveTimestamp = effectiveTS;
		fSource             = source;
		fType               = type;
		fReference          = reference;
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
		this(timestamp, timestamp, source, type, reference);
	}

	/**
	 * Copy constructor
	 * 
	 * @param other the original event
	 */
	public TmfEvent(TmfEvent other) {
    	if (other == null)
    		throw new IllegalArgumentException();
		fOriginalTimestamp  = new TmfTimestamp(other.fOriginalTimestamp);
		fEffectiveTimestamp = new TmfTimestamp(other.fEffectiveTimestamp);
		fSource    			= new TmfEventSource(other.fSource);
		fType      			= new TmfEventType(other.fType);
		fContent   			= new TmfEventContent(other.fContent);
		fReference			= new TmfEventReference(other.fReference);
	}

	@SuppressWarnings("unused")
	private TmfEvent() {
		throw new AssertionError();
	}

	// ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

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
	 * @param content the new event content
	 */
	public void setContent(TmfEventContent content) {
		fContent = content;
	}

	// ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

	@Override
    public int hashCode() {
		int result = 17;
		result = 37 * result + fSource.hashCode();
		result = 37 * result + fType.hashCode();
		result = 37 * result + fEffectiveTimestamp.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof TmfEvent))
        	return false;
        TmfEvent o = (TmfEvent) other;
        return fEffectiveTimestamp.equals(o.fEffectiveTimestamp) &&
               fSource.equals(o.fSource) &&
               fType.equals(o.fType) &&
               fContent.equals(o.fContent);
    }

	@Override
	public String toString() {
		return "[TmfEvent(" + fEffectiveTimestamp + "," + fSource + "," + fType + "," + fContent + ")]";
	}

}
