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

/**
 * <b><u>TmfEventType</u></b>
 * <p>
 * A basic implementation of ITmfEventType.
 */
public class TmfEventType implements ITmfEventType {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

	private String fContext;
	private String fTypeId;
	private ITmfEventField fRootField;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

	/**
	 * Default constructor
	 */
	public TmfEventType() {
		this(DEFAULT_CONTEXT_ID, DEFAULT_TYPE_ID, null);
	}

    /**
     * Full constructor
     * 
     * @param context the type context
     * @param typeId the type name
     * @param root the root field
     */
    public TmfEventType(String context, String typeId, ITmfEventField root) {
        if (context == null || typeId == null)
            throw new IllegalArgumentException();
        fContext = context;
        fTypeId = typeId;
        fRootField = root;

        // Register to the event type manager
        TmfEventTypeManager.getInstance().add(context, this);
    }

	/**
	 * Copy constructor
	 * 
	 * @param type the other type
	 */
	public TmfEventType(TmfEventType type) {
    	if (type == null)
    		throw new IllegalArgumentException();
    	fContext = type.fContext;
		fTypeId  = type.fTypeId;
		fRootField    = type.fRootField;
	}

    // ------------------------------------------------------------------------
    // ITmfEventType
    // ------------------------------------------------------------------------

	public String getContext() {
		return fContext;
	}

    public String getName() {
        return fTypeId;
    }

    public ITmfEventField getRootField() {
        return fRootField;
    }

    public String[] getFieldNames() {
        return (fRootField != null) ? fRootField.getFieldNames() : null;
    }

    public String getFieldName(int index) {
        return (fRootField != null) ? fRootField.getFieldName(index) : null;
    }

    // ------------------------------------------------------------------------
    // Cloneable
    // ------------------------------------------------------------------------

    @Override
    public ITmfEventType clone() {
        TmfEventType clone = null;
        try {
            clone = (TmfEventType) super.clone();
            clone.fContext = fContext;
            clone.fTypeId = fTypeId;
            clone.fRootField = (fRootField != null) ? fRootField.clone() : null;
        }
        catch (CloneNotSupportedException e) {
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
        result = prime * result + ((fContext == null) ? 0 : fContext.hashCode());
        result = prime * result + ((fTypeId == null) ? 0 : fTypeId.hashCode());
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
        TmfEventType other = (TmfEventType) obj;
        if (fContext == null) {
            if (other.fContext != null)
                return false;
        } else if (!fContext.equals(other.fContext))
            return false;
        if (fTypeId == null) {
            if (other.fTypeId != null)
                return false;
        } else if (!fTypeId.equals(other.fTypeId))
            return false;
        return true;
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "TmfEventType [fContext=" + fContext + ", fTypeId=" + fTypeId + "]";
    }

}