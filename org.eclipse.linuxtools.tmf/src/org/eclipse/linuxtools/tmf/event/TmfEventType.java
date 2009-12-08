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

import java.util.HashMap;
import java.util.Map;

/**
 * <b><u>TmfEventType</u></b>
 * <p>
 * The event type.
 */
public class TmfEventType implements Cloneable {

    // ========================================================================
    // Constants
    // ========================================================================

	public static final String DEFAULT_TYPE_ID  = "TMF Default Type";
	public static final String[] DEFAULT_LABELS = new String[] { "Content" };
	
    // ========================================================================
    // Attributes
    // ========================================================================

	private final String   fTypeId;
	private final String[] fFieldLabels;
	private final int      fNbFields;
	private final Map<String, Integer> fFieldMap;

	// ========================================================================
    // Constructors
    // ========================================================================

	/**
	 * 
	 */
	public TmfEventType() {
		this(DEFAULT_TYPE_ID, DEFAULT_LABELS);
	}

	/**
	 * @param type
	 * @param format
	 */
	public TmfEventType(String typeId, String[] labels) {
		assert(typeId != null);
		assert(labels != null);
		fTypeId      = typeId;
		fFieldLabels = labels;
		fNbFields    = fFieldLabels.length;
		fFieldMap    = new HashMap<String, Integer>();
		for (int i = 0; i < fNbFields; i++) {
			fFieldMap.put(fFieldLabels[i], i);
		}
	}

	/**
	 * @param other
	 */
	public TmfEventType(TmfEventType other) {
		assert(other != null);
		fTypeId      = other.fTypeId;
		fFieldLabels = other.fFieldLabels;
		fNbFields    = other.fNbFields;
		fFieldMap    = other.fFieldMap;
	}

    // ========================================================================
    // Accessors
    // ========================================================================

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
	public String getLabel(int i) {
		if (i >= 0 && i < fNbFields)
			return fFieldLabels[i];
		return null;
	}

    // ========================================================================
    // Operators
    // ========================================================================

    @Override
    public TmfEventType clone() {
    	return new TmfEventType(this);
    }

    @Override
    public String toString() {
    	return "[TmfEventType:" + fTypeId + "]";
    }

}