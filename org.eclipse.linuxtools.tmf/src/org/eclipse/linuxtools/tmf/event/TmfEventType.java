/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
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

import java.util.HashMap;
import java.util.Map;

/**
 * <b><u>TmfEventType</u></b>
 * <p>
 * The event type.
 */
public class TmfEventType {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

	public static final String DEFAULT_TYPE_ID  = "TMF Default Type";
	public static final String[] DEFAULT_LABELS = new String[] { "Content" };
	
    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

	private final String   fTypeId;
	private final String[] fFieldLabels;
	private final int      fNbFields;
	private final Map<String, Integer> fFieldMap;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

	/**
	 * Default constructor
	 */
	public TmfEventType() {
		this(DEFAULT_TYPE_ID, DEFAULT_LABELS);
	}

	/**
	 * @param type
	 * @param format
	 */
	public TmfEventType(String typeId, String[] labels) {
		if (typeId == null || labels == null)
    		throw new IllegalArgumentException();
		fTypeId      = typeId;
		fFieldLabels = labels;
		fNbFields    = fFieldLabels.length;
		fFieldMap    = new HashMap<String, Integer>();
		for (int i = 0; i < fNbFields; i++) {
			fFieldMap.put(fFieldLabels[i], i);
		}
	}

	/**
	 * Copy constructor
	 * @param other
	 */
	public TmfEventType(TmfEventType other) {
    	if (other == null)
    		throw new IllegalArgumentException();
		fTypeId      = other.fTypeId;
		fFieldLabels = other.fFieldLabels;
		fNbFields    = other.fNbFields;
		fFieldMap    = other.fFieldMap;
	}

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

	/**
	 * @return
	 */
	public String getTypeId() {
		return fTypeId;
	}

	/**
	 * @return
	 */
	public int getNbFields() {
		return fNbFields;
	}

	/**
	 * @return
	 */
	public int getFieldIndex(String id) throws TmfNoSuchFieldException {
		Integer index = fFieldMap.get(id);
		if (index == null)
			throw(new TmfNoSuchFieldException(id));
		return index;
	}

	/**
	 * @return
	 */
	public String[] getLabels() {
		return fFieldLabels;
	}

	/**
	 * @return
	 */
	public String getLabel(int i) throws TmfNoSuchFieldException {
		if (i >= 0 && i < fNbFields)
			return fFieldLabels[i];
		throw new TmfNoSuchFieldException("Bad index (" + i + ")");
	}

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

	@Override
    public int hashCode() {
        return fTypeId.hashCode();
    }

	@Override
    public boolean equals(Object other) {
		if (!(other instanceof TmfEventType))
			return false;
		TmfEventType o = (TmfEventType) other;
        return fTypeId.equals(o.fTypeId);
    }

    @Override
    public String toString() {
    	return "[TmfEventType:" + fTypeId + "]";
    }

}