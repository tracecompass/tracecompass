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
import java.util.HashMap;
import java.util.Map;

/**
 * <b><u>TmfEventType</u></b>
 * <p>
 * A basic implementation of ITmfEventType.
 */
public class TmfEventType implements ITmfEventType {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    public static final String DEFAULT_CONTEXT_ID = "Context"; //$NON-NLS-1$
    public static final String DEFAULT_TYPE_ID = "Type"; //$NON-NLS-1$
	
    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

	private String fContext;
	private String fTypeId;
	private int fNbFields;
	private Map<String, Integer> fFieldMap;
	private String[] fFieldLabels;

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
	 * @param type
	 * @param format
	 */
	public TmfEventType(String context, String typeId, String[] labels) {
		if (context == null || typeId == null)
    		throw new IllegalArgumentException();
		fContext = context;
		fTypeId = typeId;
		fFieldLabels = (labels != null) ? labels : new String[] { }; 
		fNbFields = (fFieldLabels != null) ? fFieldLabels.length : 0;
		fFieldMap = new HashMap<String, Integer>();
		for (int i = 0; i < fNbFields; i++) {
		    String id = fFieldLabels[i];
			fFieldMap.put(id, i);
		}
	}

	/**
	 * Copy constructor
	 * 
	 * @param type the other type
	 */
	public TmfEventType(TmfEventType type) {
    	if (type == null)
    		throw new IllegalArgumentException();
    	fContext     = type.fContext;
		fTypeId      = type.fTypeId;
        fFieldLabels = type.fFieldLabels;
        fNbFields    = type.fNbFields;
        fFieldMap    = type.fFieldMap;
	}

    // ------------------------------------------------------------------------
    // ITmfEventType
    // ------------------------------------------------------------------------

	public String getContext() {
		return fContext;
	}

    public String getId() {
        return fTypeId;
    }

	public int getNbFields() {
		return fNbFields;
	}

    public String[] getFieldLabels() {
        return fFieldLabels;
    }

    public String getFieldLabel(int i) throws TmfNoSuchFieldException {
        if (i >= 0 && i < fNbFields)
            return fFieldLabels[i];
        throw new TmfNoSuchFieldException("Invalid index (" + i + ")"); //$NON-NLS-1$//$NON-NLS-2$
    }

    public int getFieldIndex(String fieldId) throws TmfNoSuchFieldException {
        Integer index = fFieldMap.get(fieldId);
        if (index == null)
            throw (new TmfNoSuchFieldException("Invalid field (" + fieldId + ")")); //$NON-NLS-1$//$NON-NLS-2$
        return index;
    }

    // ------------------------------------------------------------------------
    // Cloneable
    // ------------------------------------------------------------------------

    @Override
    public TmfEventType clone() {
        TmfEventType clone = null;
        try {
            clone = (TmfEventType) super.clone();
            clone.fContext = fContext;
            clone.fTypeId = fTypeId;
            // Clone the fields
            clone.fNbFields = fNbFields;
            clone.fFieldLabels = new String[fNbFields];
            clone.fFieldMap = new HashMap<String, Integer>();
            for (int i = 0; i < fNbFields; i++) {
                clone.fFieldLabels[i] = fFieldLabels[i];
                clone.fFieldMap.put(fFieldLabels[i], new Integer(i));
            }
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
        result = prime * result + Arrays.hashCode(fFieldLabels);
        result = prime * result + ((fFieldMap == null) ? 0 : fFieldMap.hashCode());
        result = prime * result + fNbFields;
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
        if (!Arrays.equals(fFieldLabels, other.fFieldLabels))
            return false;
        if (fFieldMap == null) {
            if (other.fFieldMap != null)
                return false;
        } else if (!fFieldMap.equals(other.fFieldMap))
            return false;
        if (fNbFields != other.fNbFields)
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
        return "TmfEventType [fContext=" + fContext + ", fTypeId=" + fTypeId + ", fNbFields="
                        + fNbFields + ", fFieldLabels=" + Arrays.toString(fFieldLabels) + "]";
    }

//	@Override
//    public int hashCode() {
//        return fTypeId.hashCode();
//    }
//
//	@Override
//    public boolean equals(Object other) {
//		if (!(other instanceof TmfEventType))
//			return false;
//		TmfEventType o = (TmfEventType) other;
//        return fTypeId.equals(o.fTypeId);
//    }
//
//    @Override
//    @SuppressWarnings("nls")
//    public String toString() {
//    	return "[TmfEventType:" + fTypeId + "]";
//    }

}