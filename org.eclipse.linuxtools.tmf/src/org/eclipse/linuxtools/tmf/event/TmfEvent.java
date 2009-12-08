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
 * of the event is needed, use clone().
 */
public class TmfEvent extends TmfData implements Cloneable {

    // ========================================================================
    // Attributes
    // ========================================================================

	protected TmfTimestamp      fEffectiveTimestamp;
	protected TmfTimestamp      fOriginalTimestamp;
	protected TmfEventSource    fSource;
	protected TmfEventType      fType;
	protected TmfEventReference fReference;

	// Content requires a reference to the parent event so it is initialized
	// using setContent()
	protected TmfEventContent   fContent;

	// ========================================================================
    // Constructors
    // ========================================================================

	/**
	 * @param originalTS
	 * @param effectiveTS
	 * @param source
	 * @param type
	 * @param reference
	 */
	public TmfEvent(TmfTimestamp originalTS, TmfTimestamp effectiveTS,
			TmfEventSource source, TmfEventType type, TmfEventReference reference)
	{
		fOriginalTimestamp  = originalTS;
		fEffectiveTimestamp = effectiveTS;
		fSource             = source;
		fType               = type;
		fReference          = reference;
	}

	/**
	 * @param timestamp
	 * @param source
	 * @param type
	 * @param reference
	 */
	public TmfEvent(TmfTimestamp timestamp, TmfEventSource source,
			TmfEventType type, TmfEventReference reference)
	{
		this(timestamp, timestamp, source, type, reference);
	}

	/**
	 * Copy constructor (shallow)
	 * 
	 * @param other
	 */
	public TmfEvent(TmfEvent other) {
		assert(other != null);
		fOriginalTimestamp  = other.fOriginalTimestamp;
		fEffectiveTimestamp = other.fEffectiveTimestamp;
		fSource    			= other.fSource;
		fType      			= other.fType;
		fContent   			= other.fContent;
		fReference			= other.fReference;
	}

	@SuppressWarnings("unused")
	private TmfEvent() {
	}

    // ========================================================================
    // Accessors
    // ========================================================================

	/**
	 * @return
	 */
	public TmfTimestamp getTimestamp() {
		return fEffectiveTimestamp;
	}

	/**
	 * @return
	 */
	public TmfTimestamp getOriginalTimestamp() {
		return fOriginalTimestamp;
	}

	/**
	 * @return
	 */
	public TmfEventSource getSource() {
		return fSource;
	}

	/**
	 * @return
	 */
	public TmfEventType getType() {
		return fType;
	}

	/**
	 * @return
	 */
	public void setContent(TmfEventContent content) {
		fContent = content;
	}

	/**
	 * @return
	 */
	public TmfEventContent getContent() {
		return fContent;
	}

	/**
	 * @return
	 */
	public TmfEventReference getReference() {
		return fReference;
	}

    // ========================================================================
    // Operators
    // ========================================================================

	@Override
	public TmfEvent clone() {
		TmfEvent event = new TmfEvent(
			fOriginalTimestamp.clone(),
			fEffectiveTimestamp.clone(),
		    fSource.clone(),
		    fType.clone(),
		    fReference.clone());
		TmfEventContent content = fContent.clone();
		event.setContent(content);
		return event;
	}

	// TODO: Design a proper format...
	@Override
	public String toString() {
		return fEffectiveTimestamp.toString();
	}
}
