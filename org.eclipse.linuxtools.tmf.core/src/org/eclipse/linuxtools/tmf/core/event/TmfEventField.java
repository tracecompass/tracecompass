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

/**
 * <b><u>TmfEventField</u></b>
 * <p>
 * A basic implementation of ITmfEventField with no subfields.
 */
public class TmfEventField implements ITmfEventField {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    protected ITmfEventContent fEventContent;
    protected String fFieldId;
    protected Object fValue;
    protected ITmfEventField[] fSubFields;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    @SuppressWarnings("unused")
    private TmfEventField() {
        throw new AssertionError();
    }

    /**
     * Full constructor
     * 
     * @param content the event content (field container)
     * @param id the event field id
     * @param value the event field value
     */
    public TmfEventField(ITmfEventContent content, String id, Object value) {
        if (id == null) {
            throw new IllegalArgumentException();
        }
        fEventContent = content;
        fFieldId = id;
        fValue = value;
        fSubFields = null;
    }

    /**
     * Copy constructor
     * 
     * @param field the other event field
     */
    public TmfEventField(TmfEventField field) {
    	if (field == null)
    		throw new IllegalArgumentException();
    	fEventContent  = field.fEventContent;
    	fFieldId = field.fFieldId;
		fValue = field.fValue;
		fSubFields = field.fSubFields;
    }

    // ------------------------------------------------------------------------
    // ITmfEventField
    // ------------------------------------------------------------------------

    public ITmfEventContent getContent() {
        return fEventContent;
    }

    public String getId() {
        return fFieldId;
    }

    public Object getValue() {
        return fValue;
    }

    public ITmfEventField[] getSubFields() {
        return fSubFields;
    }

    // ------------------------------------------------------------------------
    // Convenience setters
    // ------------------------------------------------------------------------

    /**
     * @param value new field value
     */
    protected void setValue(Object value) {
        fValue = value;
    }

    // ------------------------------------------------------------------------
    // Cloneable
    // ------------------------------------------------------------------------

    @Override
    public ITmfEventField clone() {
        TmfEventField clone = null;
        try {
            clone = (TmfEventField) super.clone();
            clone.fEventContent = fEventContent;
            clone.fFieldId = new String(fFieldId);
            clone.fValue = fValue;
            clone.fSubFields = (fSubFields != null) ? fSubFields.clone() : null;
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
        result = prime * result + ((fEventContent == null) ? 0 : fEventContent.hashCode());
        result = prime * result + ((fFieldId == null) ? 0 : fFieldId.hashCode());
        result = prime * result + ((fValue == null) ? 0 : fValue.hashCode());
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
        TmfEventField other = (TmfEventField) obj;
        if (fEventContent == null) {
            if (other.fEventContent != null)
                return false;
        } else if (!fEventContent.equals(other.fEventContent))
            return false;
        if (fFieldId == null) {
            if (other.fFieldId != null)
                return false;
        } else if (!fFieldId.equals(other.fFieldId))
            return false;
        if (fValue == null) {
            if (other.fValue != null)
                return false;
        } else if (!fValue.equals(other.fValue))
            return false;
        return true;
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "TmfEventField [fFieldId=" + fFieldId + ", fValue=" + fValue + "]";
    }

}
