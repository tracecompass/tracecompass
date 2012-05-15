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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A basic implementation of ITmfEventField.
 * <p>
 * Non-value fields are structural (i.e. used to represent the event structure
 * including optional fields) while the valued fields are actual event fields.
 * 
 * @version 1.0
 * @author Francois Chouinard
 * 
 * @see ITmfEvent
 * @see ITmfEventType
 */
public class TmfEventField implements ITmfEventField, Cloneable {

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
    }

    /**
     * Constructor for a structural field
     * 
     * @param name the event field id
     * @param fields the list of subfields
     */
    public TmfEventField(final String name, final ITmfEventField[] fields) {
        this(name, null, fields);
    }

    /**
     * Constructor for a terminal field (no subfields)
     * 
     * @param name the event field id
     * @param value the event field value
     */
    public TmfEventField(final String name, final Object value) {
        this(name, value, null);
    }

    /**
     * Full constructor
     * 
     * @param name the event field id
     * @param value the event field value
     * @param fields the list of subfields
     */
    public TmfEventField(final String name, final Object value, final ITmfEventField[] fields) {
        if (name == null) {
            throw new IllegalArgumentException();
        }
        fName = name;
        fValue = value;
        fFields = (fields != null) ? Arrays.copyOf(fields, fields.length) : null;
        populateStructs();
    }

    /**
     * Copy constructor
     * 
     * @param field the other event field
     */
    public TmfEventField(final TmfEventField field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        fName = field.fName;
        fValue = field.fValue;
        fFields = field.fFields;
        fFieldNames = field.fFieldNames;
        populateStructs();
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
        return Arrays.copyOf(fFieldNames, fFieldNames.length);
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfEventField#getFieldName(int)
     */
    @Override
    public String getFieldName(final int index) {
        final ITmfEventField field = getField(index);
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
        return (fFields != null) ? Arrays.copyOf(fFields, fFields.length) : null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfEventField#getField(java.lang.String)
     */
    @Override
    public ITmfEventField getField(final String name) {
        return fNameMapping.get(name);
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfEventField#getField(int)
     */
    @Override
    public ITmfEventField getField(final int index) {
        if (fFields != null && index >= 0 && index < fFields.length) {
            return fFields[index];
        }
        return null;
    }

    // ------------------------------------------------------------------------
    // Convenience setters
    // ------------------------------------------------------------------------

    /**
     * @param value new field raw value
     * @param fields the corresponding fields
     */
    protected void setValue(final Object value, final ITmfEventField[] fields) {
        fValue = value;
        fFields = (fields != null) ? Arrays.copyOf(fields, fields.length) : null;
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
    public final static ITmfEventField makeRoot(final String[] labels) {
        final ITmfEventField[] fields = new ITmfEventField[labels.length];
        for (int i = 0; i < labels.length; i++) {
            fields[i] = new TmfEventField(labels[i], null);
        }
        // Return a new root field;
        return new TmfEventField(ITmfEventField.ROOT_FIELD_ID, fields);
    }

    /*
     * Populate the subfield names and the name map
     */
    private void populateStructs() {
        final int nbFields = (fFields != null) ? fFields.length : 0;
        fFieldNames = new String[nbFields];
        fNameMapping = new HashMap<String, ITmfEventField>();
        for (int i = 0; i < nbFields; i++) {
            final String name = fFields[i].getName();
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
    public TmfEventField clone() {
        TmfEventField clone = null;
        try {
            clone = (TmfEventField) super.clone();
            clone.fName = fName;
            clone.fValue = fValue;
            clone.fFields = (fFields != null) ? fFields.clone() : null;
            clone.populateStructs();
        } catch (final CloneNotSupportedException e) {
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
        result = prime * result + fName.hashCode();
        result = prime * result + ((fValue == null) ? 0 : fValue.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TmfEventField)) {
            return false;
        }
        final TmfEventField other = (TmfEventField) obj;
        if (!fName.equals(other.fName)) {
            return false;
        }
        if (fValue == null) {
            if (other.fValue != null) {
                return false;
            }
        } else if (!fValue.equals(other.fValue)) {
            return false;
        }
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
