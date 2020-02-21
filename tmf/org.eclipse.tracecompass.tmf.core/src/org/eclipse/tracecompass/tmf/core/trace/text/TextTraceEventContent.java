/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Bernd Hufmann - Updated equals, clone and hashCode to consider StringBuffer values
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.trace.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;

/**
 * Implementation of ITmfEventField for Text Traces.
 */
public class TextTraceEventContent implements ITmfEventField {

    private final @NonNull String fName;
    private final @NonNull List<TextTraceEventContent> fFields;

    private @Nullable Object fValue;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor for a root event content. Subfields with the specified field
     * names are created and initialized with a null value.
     *
     * @param fieldNames
     *            the array of non-null field names
     * @throws IllegalArgumentException
     *             if any one of the field names is null
     */
    public TextTraceEventContent(String @NonNull [] fieldNames) {
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
     * Constructor for an initial capacity. This should be the expected number
     * of fields.
     *
     * @param initialCapacity
     *            the initial capacity of the field list
     * @since 1.0
     */
    public TextTraceEventContent(int initialCapacity) {
        fName = ITmfEventField.ROOT_FIELD_ID;
        fValue = null;
        fFields = new ArrayList<>(initialCapacity);
    }

    /**
     * Constructor for a subfield
     *
     * @param fieldName
     *            the subfield name
     */
    private TextTraceEventContent(@NonNull String fieldName) {
        fName = fieldName;
        fValue = null;
        fFields = Collections.emptyList();
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
    public ITmfEventField getField(String... path) {
        if (path.length == 0) {
            return this;
        }
        // There are no sub fields
        if (path.length == 1) {
            for (TextTraceEventContent field : fFields) {
                if (field.getName().equals(path[0])) {
                    return field;
                }
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

    // ------------------------------------------------------------------------
    // Convenience getters and setters
    // ------------------------------------------------------------------------

    /**
     * Get a field name by index.
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
     * Get a field by index.
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
     * Get a subfield value by name.
     *
     * @param name
     *            a subfield name
     * @return field value object
     */
    public Object getFieldValue(@NonNull String name) {
        for (int i = 0; i < fFields.size(); i++) {
            if (fFields.get(i).getName().equals(name)) {
                return fFields.get(i).getValue();
            }
        }
        return null;
    }

    /**
     * Get a subfield value by index.
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
     * Set the content value.
     *
     * @param value
     *            the content value
     */
    public void setValue(Object value) {
        fValue = value;
    }

    /**
     * Set a subfield value by name. Adds the subfield if it is new.
     *
     * @param name
     *            a subfield name
     * @param value
     *            the subfield value
     */
    public void setFieldValue(@NonNull String name, Object value) {
        TextTraceEventContent field = null;
        for (int i = 0; i < fFields.size(); i++) {
            if (fFields.get(i).getName().equals(name)) {
                field = fFields.get(i);
                field.setValue(value);
            }
        }
        if (field == null) {
            field = new TextTraceEventContent(name);
            field.setValue(value);
            fFields.add(field);
        }
    }

    /**
     * Set a subfield value by index.
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

    /**
     * Add a new subfield unconditionally and set its value. Note: This can
     * create a duplicate subfield. If the subfield already exists, use
     * {@link #setFieldValue(String, Object)} instead.
     *
     * @param name
     *            a subfield name
     * @param value
     *            the subfield value
     * @since 1.0
     */
    public void addField(@NonNull String name, Object value) {
        TextTraceEventContent field = new TextTraceEventContent(name);
        field.setValue(value);
        fFields.add(field);
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
