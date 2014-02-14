/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Bernd Hufmann - Updated equals, clone and hashCode to consider StringBuffer values
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.trace.text;

import java.util.Arrays;

import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;

/**
 * Implementation of ITmfEventField for Text Traces.
 *
 * @since 3.0
 */
public class TextTraceEventContent implements ITmfEventField, Cloneable {

    private String fName;
    private Object fValue;
    private TextTraceEventContent[] fFields;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor for a root event content
     *
     * @param fieldNames
     *            the array of field names
     */
    public TextTraceEventContent(String[] fieldNames) {
        this(ITmfEventField.ROOT_FIELD_ID);
        fFields = new TextTraceEventContent[fieldNames.length];
        for (int i = 0; i < fFields.length; i++) {
            fFields[i] = new TextTraceEventContent(fieldNames[i]);
        }
    }

    /**
     * Constructor for a subfield
     *
     * @param fieldNames
     *            the array of field names
     */
    private TextTraceEventContent(String fieldName) {
        fName = fieldName;
    }

    // ------------------------------------------------------------------------
    // ITmfEventField
    // ------------------------------------------------------------------------

    @Override
    public String getName() {
        return fName;
    }

    @Override
    public Object getValue() {
        return fValue;
    }

    @Override
    public String[] getFieldNames() {
        String[] fieldNames = new String[fFields.length];
        for (int i = 0; i < fieldNames.length; i++) {
            fieldNames[i] = fFields[i].getName();
        }
        return fieldNames;
    }

    @Override
    public String getFieldName(int index) {
        if (index >= 0 && index < fFields.length) {
            return fFields[index].getName();
        }
        return null;
    }

    @Override
    public ITmfEventField[] getFields() {
        return fFields;
    }

    @Override
    public ITmfEventField getField(String name) {
        for (int i = 0; i < fFields.length; i++) {
            if (fFields[i].getName().equals(name)) {
                return fFields[i];
            }
        }
        return null;
    }

    @Override
    public ITmfEventField getField(int index) {
        if (index >= 0 && index < fFields.length) {
            return fFields[index];
        }
        return null;
    }

    @Override
    public String getFormattedValue() {
        return fValue.toString();
    }

    @Override
    public ITmfEventField getSubField(String[] names) {
        // There are no sub fields
        if (names.length == 1) {
            return getField(names[0]);
        }
        return null;
    }

    // ------------------------------------------------------------------------
    // Convenience getters and setters
    // ------------------------------------------------------------------------

    /**
     * Get a subfield value by name
     *
     * @param name
     *            a subfield name
     * @return field value object
     */
    public Object getFieldValue(String name) {
        for (int i = 0; i < fFields.length; i++) {
            if (fFields[i].getName().equals(name)) {
                return fFields[i].getValue();
            }
        }
        return null;
    }

    /**
     * Get a subfield value by index
     *
     * @param index
     *            a subfield index
     * @return field value object
     */
    public Object getFieldValue(int index) {
        if (index >= 0 && index < fFields.length) {
            return fFields[index].getValue();
        }
        return null;
    }

    /**
     * Set the content value
     *
     * @param value
     *            the content value
     */
    public void setValue(Object value) {
        fValue = value;
    }

    /**
     * Set a subfield value by name
     *
     * @param name
     *            a subfield name
     * @param value
     *            the subfield value
     */
    public void setFieldValue(String name, Object value) {
        for (int i = 0; i < fFields.length; i++) {
            if (fFields[i].getName().equals(name)) {
                fFields[i].fValue = value;
            }
        }
    }

    /**
     * Set a subfield value by index
     *
     * @param index
     *            a subfield index
     * @param value
     *            the subfield value
     */
    public void setFieldValue(int index, Object value) {
        if (index >= 0 && index < fFields.length) {
            fFields[index].fValue = value;
        }
    }

    // ------------------------------------------------------------------------
    // Cloneable
    // ------------------------------------------------------------------------

    @Override
    public TextTraceEventContent clone() {
        TextTraceEventContent clone = null;
        try {
            clone = (TextTraceEventContent) super.clone();
            clone.fName = fName;
            if (fValue instanceof StringBuffer) {
                StringBuffer value = new StringBuffer(fValue.toString());
                clone.fValue = value;
            } else {
                clone.fValue = fValue;
            }
            clone.fFields = (fFields != null) ? fFields.clone() : null;
            if (fFields != null) {
                for (int i = 0; i < fFields.length; i++) {
                    clone.fFields[i] = fFields[i].clone();
                }
            }
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
        result = prime * result + Arrays.hashCode(fFields);
        result = prime * result + ((fName == null) ? 0 : fName.hashCode());
        int tmpHash = 0; // initialize for fValue equals null;
        if (fValue != null) {
            if (fValue instanceof StringBuffer) {
                tmpHash = fValue.toString().hashCode();
            } else {
                tmpHash = fValue.hashCode();
            }
        }
        result = prime * result + tmpHash;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TextTraceEventContent other = (TextTraceEventContent) obj;
        if (!Arrays.equals(fFields, other.fFields)) {
            return false;
        }
        if (fName == null) {
            if (other.fName != null) {
                return false;
            }
        } else if (!fName.equals(other.fName)) {
            return false;
        }
        if (fValue == null) {
            if (other.fValue != null) {
                return false;
            }
        } else {
            if ((fValue instanceof StringBuffer) && (other.fValue instanceof StringBuffer)) {
                if (!fValue.toString().equals(other.fValue.toString())) {
                    return false;
                }
            } else if (!fValue.equals(other.fValue)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (fName == ITmfEventField.ROOT_FIELD_ID) {
            for (int i = 0; i < getFields().length; i++) {
                ITmfEventField field = getFields()[i];
                if (i != 0) {
                    sb.append(", "); //$NON-NLS-1$
                }
                sb.append(field.toString());
            }
        } else {
            sb.append(fName);
            sb.append('=');
            sb.append(fValue);
        }
        return sb.toString();
    }

}
