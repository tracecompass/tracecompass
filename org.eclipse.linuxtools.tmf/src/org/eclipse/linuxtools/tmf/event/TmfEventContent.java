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
 * <b><u>TmfEventContent</u></b>
 * <p>
 * The event content.
 */
public class TmfEventContent implements Cloneable {

    // ========================================================================
    // Attributes
    // ========================================================================

	protected TmfEvent fParentEvent = null;
	protected Object   fRawContent  = null;
	protected Object[] fFields      = null;

    // ========================================================================
    // Constructors
    // ========================================================================

	/**
	 * @param parent
	 * @param content
	 */
	public TmfEventContent(TmfEvent parent, Object content) {
		fParentEvent = parent;
		fRawContent  = content;
	}

    /**
     * @param other
     */
    public TmfEventContent(TmfEventContent other) {
    	assert(other != null);
    	fParentEvent = other.fParentEvent;
		fRawContent  = other.fRawContent;
		fFields      = other.fFields;
    }

    @SuppressWarnings("unused")
	private TmfEventContent() {
    }

    // ========================================================================
    // Accessors
    // ========================================================================

	/**
	 * @return the parent (containing) event
	 */
	public TmfEvent getEvent() {
		return fParentEvent;
	}

	/**
	 * @return the event type
	 */
	public TmfEventType getType() {
		return fParentEvent.getType();
	}

	/**
	 * @return the raw content
	 */
	public Object getContent() {
		return fRawContent;
	}

	/**
	 * Returns the list of fields in the same order as TmfEventType.getLabels()
	 * 
	 * @return the ordered set of fields (optional fields might be null)
	 */
	public Object[] getFields() {
		if (fFields == null) {
			parseContent();
		}
		return fFields;
	}

	/**
	 * @param id
	 * @return
	 */
	public Object getField(String id) throws TmfNoSuchFieldException {
		if (fFields == null) {
			parseContent();
		}
		return fFields[getType().getFieldIndex(id)];
	}

	/**
	 * @param n
	 * @return
	 */
	public Object getField(int n) {
		if (fFields == null) {
			parseContent();
		}
		if (n >= 0 && n < fFields.length)
			return fFields[n];
		return null;
	}

    // ========================================================================
    // Operators
    // ========================================================================

	/**
	 * Should be overridden (all fields are null by default)
	 */
	protected void parseContent() {
		fFields = new Object[1];
		fFields[0] = fRawContent;
	}
	
	/**
	 * Clone: shallow copy by default; override for deep copy.
	 */
    @Override
    public TmfEventContent clone() {
		return new TmfEventContent(this);
    }

    @Override
	public String toString() {
    	Object[] fields = getFields();
    	String result = "[TmfEventContent(";
    	for (int i = 0; i < fields.length; i++) {
    		result += fields[i].toString() + ",";
    	}
    	result += ")]";

    	return result;
    }

}
