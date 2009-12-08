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
 * <b><u>TmfEventField</u></b>
 * <p>
 * A basic event field.
 * 
 * TODO: Add support for field hierarchy.
 */
public class TmfEventField implements Cloneable {

    // ========================================================================
    // Attributes
    // ========================================================================

	private final TmfEventContent fParent;
    private final String fFieldId;
    private       Object fValue;

    // ========================================================================
    // Constructors
    // ========================================================================

    /**
     * @param parent
     * @param id
     * @param value
     */
    public TmfEventField(TmfEventContent parent, String id, Object value) {
    	fParent  = parent;
    	fFieldId = id;
        fValue   = value;
    }

    /**
     * @param other
     */
    public TmfEventField(TmfEventField other) {
    	assert(other != null);
    	fParent  = other.fParent;
    	fFieldId = other.fFieldId;
		fValue   = other.fValue;
    }

    @SuppressWarnings("unused")
	private TmfEventField() {
    	fParent  = null;
    	fFieldId = null;
        fValue   = null;
    }

    // ========================================================================
    // Accessors
    // ========================================================================

    /**
     * @return
     */
    public TmfEventContent getParent() {
        return fParent;
    }

    /**
     * @return
     */
    public String getId() {
        return fFieldId;
    }

    /**
     * @return
     */
    public Object getValue() {
        return fValue;
    }

    /**
     * @param value
     */
    protected void setValue(Object value) {
        fValue = value;
    }

    // ========================================================================
    // Operators
    // ========================================================================

	/**
	 * Clone: shallow copy by default; override for deep copy.
	 */
    @Override
    public TmfEventField clone() {
    	return new TmfEventField(this);
    }

    @Override
	public String toString() {
        return "[TmfEventField(" + fFieldId + ":" + fValue.toString() + ")]";
    }

}