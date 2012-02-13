/*******************************************************************************
 * Copyright (c) 2009, 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Updated as per TMF Event Model 1.0
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.event;

import java.util.Arrays;

/**
 * <b><u>TmfEventContent</u></b>
 * <p>
 * A basic implementation of ITmfEventContent where the raw content is a String.
 */
public class TmfEventContent implements ITmfEventContent {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

	// Default field IDs
	public static final String FIELD_ID_TIMESTAMP = ":timestamp:"; //$NON-NLS-1$
	public static final String FIELD_ID_SOURCE    = ":source:";    //$NON-NLS-1$
	public static final String FIELD_ID_TYPE      = ":type:";      //$NON-NLS-1$
	public static final String FIELD_ID_REFERENCE = ":reference:"; //$NON-NLS-1$
	public static final String FIELD_ID_CONTENT   = ":content:";   //$NON-NLS-1$
	
    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

	protected TmfEvent fParentEvent;
	protected String fRawContent;
	protected TmfEventField[] fFields;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
	
    /**
     * Default constructor
     */
    @SuppressWarnings("unused")
    private TmfEventContent() {
        throw new AssertionError();
    }

	/**
	 * Full constructor
	 * 
	 * @param parent the parent event (owner)
	 * @param content the raw content as a byte[]
	 */
	public TmfEventContent(TmfEvent parent, String content) {
		fParentEvent = parent;
		fRawContent  = content;
	}

    /**
     * Copy constructor
     * 
     * @param content the original event content
     */
    public TmfEventContent(TmfEventContent content) {
    	if (content == null)
    		throw new IllegalArgumentException();
    	fParentEvent = content.fParentEvent;
		fRawContent  = content.fRawContent;
		fFields      = content.fFields;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

	/**
	 * @return the parent (containing) event
	 */
	public TmfEvent getEvent() {
		return fParentEvent;
	}

	/**
	 * @return the event type
	 */
	public ITmfEventType getType() {
		return fParentEvent.getType();
	}

	/**
	 * @return the raw content
	 */
	public Object getRawContent() {
		return fRawContent;
	}

    /**
     * @return the serialized content
     */
    public String getFmtContent() {
        return fRawContent;
    }

	/**
	 * Returns the list of fields in the same order as TmfEventType.getLabels()
	 * 
	 * @return the ordered set of fields (optional fields might be null)
	 */
	public ITmfEventField[] getFields() {
		if (fFields == null) {
			parseContent();
		}
		return fFields;
	}

	/**
	 * @param id the field id
	 * @return the corresponding field
	 * @throws TmfNoSuchFieldException
	 */
    public ITmfEventField getField(String id) throws TmfNoSuchFieldException {
        if (fFields == null) {
            parseContent();
        }
        try {
            return fFields[getType().getFieldIndex(id)];
        } catch (TmfNoSuchFieldException e) {
            // Required for filtering from default TmfEventsTable columns
            if (id.equals(FIELD_ID_CONTENT)) {
                return new TmfEventField(this, FIELD_ID_CONTENT, toString());
            } else if (id.equals(FIELD_ID_TIMESTAMP)) {
                return new TmfEventField(this, FIELD_ID_TIMESTAMP, fParentEvent.getTimestamp().toString());
            } else if (id.equals(FIELD_ID_SOURCE)) {
                return new TmfEventField(this, FIELD_ID_SOURCE, fParentEvent.getSource());
            } else if (id.equals(FIELD_ID_TYPE)) {
                return new TmfEventField(this, FIELD_ID_TYPE, fParentEvent.getType().getId());
            } else if (id.equals(FIELD_ID_REFERENCE)) {
                return new TmfEventField(this, FIELD_ID_REFERENCE, fParentEvent.getReference());
            }
            throw e;
        }
    }

	/**
	 * @param n the field index as per TmfEventType.getLabels()
	 * @return the corresponding field (null if non-existing)
	 */
	public ITmfEventField getField(int n) {
		if (fFields == null) {
			parseContent();
		}
		if (n >= 0 && n < fFields.length)
			return fFields[n];

		return null;
	}

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

	/**
	 * Parse the content into fields. By default, a single field (the raw
	 * content) is returned.
	 * 
	 * Should be overridden.
	 */
	protected void parseContent() {
		fFields = new TmfEventField[1];
		fFields[0] = new TmfEventField(this, FIELD_ID_CONTENT, fRawContent);
	}
	
    // ------------------------------------------------------------------------
    // Cloneable
    // ------------------------------------------------------------------------

    @Override
    public ITmfEventContent clone() {
        TmfEventContent clone = null;
        try {
            clone = (TmfEventContent) super.clone();
            clone.fParentEvent = fParentEvent;
            clone.fRawContent = fRawContent;
            clone.fFields = fFields;
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
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
        result = prime * result + Arrays.hashCode(fFields);
        result = prime * result + ((fRawContent == null) ? 0 : fRawContent.hashCode());
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
        TmfEventContent other = (TmfEventContent) obj;
        if (!Arrays.equals(fFields, other.fFields))
            return false;
        if (fRawContent == null) {
            if (other.fRawContent != null)
                return false;
        } else if (!fRawContent.equals(other.fRawContent))
            return false;
        return true;
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "TmfEventContent [fRawContent=" + fRawContent + ", fFields=" + Arrays.toString(fFields) + "]";
    }

}
