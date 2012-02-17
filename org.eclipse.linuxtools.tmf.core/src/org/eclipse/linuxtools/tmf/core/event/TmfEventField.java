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

import java.util.HashMap;
import java.util.Map;

/**
 * <b><u>TmfEventField</u></b>
 * <p>
 * A basic implementation of ITmfEventField.
 */
public class TmfEventField implements ITmfEventField {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private String fName;
    private Object fValue;
    private ITmfEventField[] fFields;

    private String[] fFieldNames;
    private Map<String, ITmfEventField> fNameMapping;
    
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
     * Constructor for a terminal field (i.e. no subfields)
     * 
     * @param name the event field id
     * @param value the event field value
     */
    public TmfEventField(String name, Object value) {
        this(name, value, new ITmfEventField[0]);
    }

    /**
     * Constructor for a non-valued field (for structural purposes)
     * 
     * @param name the event field id
     * @param subfields the list of subfields
     */
    public TmfEventField(String name, ITmfEventField[] fields) {
        this(name, null, fields);
    }

    /**
     * Full constructor
     * 
     * @param name the event field id
     * @param value the event field value
     * @param subfields the list of subfields
     */
    public TmfEventField(String name, Object value, ITmfEventField[] fields) {
        if (name == null) {
            throw new IllegalArgumentException();
        }
        fName = name;
        fValue = value;
        fFields = fields;
        populateStructs();
    }

    /**
     * Copy constructor
     * 
     * @param field the other event field
     */
    public TmfEventField(TmfEventField field) {
    	if (field == null)
    		throw new IllegalArgumentException();
    	fName = field.fName;
		fValue = field.fValue;
		fFields = field.fFields;
		fFieldNames = field.fFieldNames;
    }

    // ------------------------------------------------------------------------
    // ITmfEventField
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfEventField#getName()
     */
    @Override
    public String getName() {
        return fName;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfEventField#getValue()
     */
    @Override
    public Object getValue() {
        return fValue;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfEventField#getFieldNames()
     */
    @Override
    public String[] getFieldNames() {
        return fFieldNames;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfEventField#getFieldName(int)
     */
    @Override
    public String getFieldName(int index) {
        ITmfEventField field = getField(index);
        if (field != null) {
            return field.getName();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfEventField#getFields()
     */
    @Override
    public ITmfEventField[] getFields() {
        return fFields;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfEventField#getField(java.lang.String)
     */
    @Override
    public ITmfEventField getField(String name) {
        // FIXME: Add treatment for 'special' fields 
        return fNameMapping.get(name);
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfEventField#getField(int)
     */
    @Override
    public ITmfEventField getField(int index) {
        if (index >= 0 && index < fFields.length)
            return fFields[index];
        return null;
    }

    // ------------------------------------------------------------------------
    // Convenience setters
    // ------------------------------------------------------------------------

    /**
     * @param value new field raw value
     * @param fields the corresponding fields
     */
    protected void setValue(Object value, ITmfEventField[] fields) {
        fValue = value;
        fFields = fields;
        populateStructs();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Create a root field from a list of labels.
     * 
     * @param labels the list of labels
     * @return the (flat) root list
     */
    public final static ITmfEventField makeRoot(String[] labels) {
        ITmfEventField[] fields = new ITmfEventField[labels.length];
        for (int i = 0; i < labels.length; i++) {
            fields[i] = new TmfEventField(labels[i], null);
        }
        ITmfEventField rootField = new TmfEventField(ITmfEventField.ROOT_ID, fields);
        return rootField;
    }

    /*
     * Populate the subfield names and the name map
     */
    private void populateStructs() {
        int nbFields = (fFields != null) ? fFields.length : 0;
        fFieldNames = new String[nbFields];
        fNameMapping = new HashMap<String, ITmfEventField>();
        for (int i = 0; i < nbFields; i++) {
            String name = fFields[i].getName();
            fFieldNames[i] = name;
            fNameMapping.put(name, fFields[i]);
        }
    }

    // ------------------------------------------------------------------------
    // Cloneable
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public ITmfEventField clone() {
        TmfEventField clone = null;
        try {
            clone = (TmfEventField) super.clone();
            clone.fName = fName;
            clone.fValue = fValue;
            clone.fFields = (fFields != null) ? fFields.clone() : null;
            clone.populateStructs();
        } catch (CloneNotSupportedException e) {
        }
        return clone;
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fName == null) ? 0 : fName.hashCode());
        result = prime * result + ((fValue == null) ? 0 : fValue.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TmfEventField other = (TmfEventField) obj;
        if (fName == null) {
            if (other.fName != null)
                return false;
        } else if (!fName.equals(other.fName))
            return false;
        if (fValue == null) {
            if (other.fValue != null)
                return false;
        } else if (!fValue.equals(other.fValue))
            return false;
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "TmfEventField [fFieldId=" + fName + ", fValue=" + fValue + "]";
    }

}
