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
public class TmfEventField {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

	private final TmfEventContent fParent;
    private final String fFieldId;
    private       Object fValue;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    @SuppressWarnings("unused")
	private TmfEventField() {
		throw new AssertionError();
    }

    /**
     * @param parent
     * @param id
     * @param value
     */
    public TmfEventField(TmfEventContent parent, String id, Object value) {
    	if (id == null) {
    		throw new IllegalArgumentException();
    	}
    	fParent  = parent;
    	fFieldId = id;
        fValue   = value;
    }

    /**
     * @param other
     */
    public TmfEventField(TmfEventField other) {
    	if (other == null)
    		throw new IllegalArgumentException();
    	fParent  = other.fParent;
    	fFieldId = other.fFieldId;
		fValue   = other.fValue;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

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
     * @param value new field value
     */
    protected void setValue(Object value) {
        fValue = value;
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
		int result = 17;
		result = 37 * result + fFieldId.hashCode();
		result = 37 * result + fValue.hashCode();
        return result;
    }

	@Override
	public boolean equals(Object other) {
    	if (!(other instanceof TmfEventField))
    		return false;
   		TmfEventField o = (TmfEventField) other;
   		return fParent.equals(o.fParent) && fFieldId.equals(o.fFieldId) && fValue.equals(o.fValue); 
    }

    @Override
	public String toString() {
        return "[TmfEventField(" + fFieldId + ":" + fValue.toString() + ")]";
    }

}