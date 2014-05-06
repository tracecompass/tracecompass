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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;

/**
 * Implementation of ITmfEventField for Text Traces.
 *
 * @since 3.0
 */
public class TextTraceEventContent implements ITmfEventField {

    private final @NonNull String fName;
    private final @NonNull List<TextTraceEventContent> fFields;

    private @Nullable Object fValue;

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
        fName = ITmfEventField.ROOT_FIELD_ID;
        fValue = null;
        fFields = new ArrayList<>(fieldNames.length);
        for (String fieldName : fieldNames) {
            if (fieldName == null) {
                throw new IllegalArgumentException("Null field name not allowed"); //$NON-NLS-1$
            }
            fFields.add(new TextTraceEventContent(fieldName));
        }
    }

    /**
     * Constructor for a subfield
     *
     * @param fieldNames
     *            the array of field names
     */
    private TextTraceEventContent(@NonNull String fieldName) {
        fName = fieldName;
        fValue = null;
        @SuppressWarnings("null")
        @NonNull List<TextTraceEventContent> fields = Collections.EMPTY_LIST;
        fFields = fields;
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
    public List<String> getFieldNames() {
        List<String> fieldNames = new ArrayList<>(fFields.size());
        for (TextTraceEventContent field : fFields) {
            fieldNames.add(field.getName());
        }
        return fieldNames;
    }

    @Override
    public List<TextTraceEventContent> getFields() {
        return new ArrayList<>(fFields);
    }

    @Override
    public ITmfEventField getField(String name) {
        for (TextTraceEventContent field : fFields) {
            if (field.getName().equals(name)) {
                return field;
            }
        }
        return null;
    }

    @Override
    public String getFormattedValue() {
        Object value = fValue;
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    @Override
    public ITmfEventField getSubField(String... names) {
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
     * Get a field name by index
     *
     * @param index
     *            The index of the field
     * @return The name of the field at that index
     */
    public String getFieldName(int index) {
        if (index >= 0 && index < fFields.size()) {
            return fFields.get(index).getName();
        }
        return null;
    }

    /**
     * Get a field by index
     *
     * @param index
     *            The index of the field
     * @return The field object at the requested index
     */
    public ITmfEventField getField(int index) {
        if (index >= 0 && index < fFields.size()) {
            return fFields.get(index);
        }
        return null;
    }

    /**
     * Get a subfield value by name
     *
     * @param name
     *            a subfield name
     * @return field value object
     */
    public Object getFieldValue(String name) {
        for (int i = 0; i < fFields.size(); i++) {
            if (fFields.get(i).getName().equals(name)) {
                return fFields.get(i).getValue();
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
        if (index >= 0 && index < fFields.size()) {
            return fFields.get(index).getValue();
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
        for (int i = 0; i < fFields.size(); i++) {
            if (fFields.get(i).getName().equals(name)) {
                fFields.get(i).fValue = value;
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
        if (index >= 0 && index < fFields.size()) {
            fFields.get(index).fValue = value;
        }
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + fFields.hashCode();
        result = prime * result + fName.hashCode();
        int tmpHash = 0; // initialize for fValue equals null;
        Object value = fValue;
        if (value != null) {
            if (value instanceof StringBuffer) {
                tmpHash = value.toString().hashCode();
            } else {
                tmpHash = value.hashCode();
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
        if (!fFields.equals(other.fFields)) {
            return false;
        }
        if (!fName.equals(other.fName)) {
            return false;
        }

        Object value = fValue;
        if (value == null) {
            if (other.fValue != null) {
                return false;
            }
        } else {
            if ((value instanceof StringBuffer) && (other.fValue instanceof StringBuffer)) {
                Object otherValue = other.getValue();
                if (otherValue == null) {
                    return false;
                }
                if (!value.toString().equals(otherValue.toString())) {
                    return false;
                }
            } else if (!value.equals(other.fValue)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (fName == ITmfEventField.ROOT_FIELD_ID) {
            for (int i = 0; i < getFields().size(); i++) {
                ITmfEventField field = getFields().get(i);
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
