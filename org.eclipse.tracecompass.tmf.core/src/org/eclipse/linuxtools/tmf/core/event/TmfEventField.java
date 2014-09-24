/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Updated as per TMF Event Model 1.0
 *   Alexandre Montplaisir - Removed Cloneable, made immutable
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.event;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;

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
public class TmfEventField implements ITmfEventField {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final @NonNull String fName;
    private final @Nullable Object fValue;
    private final @NonNull ImmutableMap<String, ITmfEventField> fFields;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Full constructor
     *
     * @param name
     *            the event field id
     * @param value
     *            the event field value
     * @param fields
     *            the list of subfields
     * @throws IllegalArgumentException
     *             If 'name' is null, or if 'fields' has duplicate field names.
     */
    @SuppressWarnings("null") /* ImmutableMap methods do not return @NonNull */
    public TmfEventField(String name, @Nullable Object value, @Nullable ITmfEventField[] fields) {
        if (name == null) {
            throw new IllegalArgumentException();
        }
        fName = name;
        fValue = value;

        if (fields == null) {
            fFields = ImmutableMap.of();
        } else {
            /* Java 8 streams will make this even more simple! */
            ImmutableMap.Builder<String, ITmfEventField> mapBuilder = new ImmutableMap.Builder<>();
            for (ITmfEventField field : fields) {
                final String curName = field.getName();
                mapBuilder.put(curName, field);
            }
            fFields = mapBuilder.build();
        }
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

    /**
     * @since 3.0
     */
    @Override
    public Collection<String> getFieldNames() {
        return fFields.keySet();
    }

    /**
     * @since 3.0
     */
    @Override
    public Collection<ITmfEventField> getFields() {
        return fFields.values();
    }

    @Override
    public ITmfEventField getField(final String name) {
        return fFields.get(name);
    }

    /**
     * @since 3.0
     */
    @Override
    public ITmfEventField getSubField(final String... names) {
        ITmfEventField field = this;
        for (String name : names) {
            field = field.getField(name);
            if (field == null) {
                return null;
            }
        }
        return field;
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
            fields[i] = new TmfEventField(labels[i], null, null);
        }
        // Return a new root field;
        return new TmfEventField(ITmfEventField.ROOT_FIELD_ID, null, fields);
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        Object value = fValue;
        final int prime = 31;
        int result = 1;
        result = prime * result + fName.hashCode();
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        result = prime * result + fFields.hashCode();
        return result;
    }

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

        /* Check that 'fName' is the same */
        if (!fName.equals(other.fName)) {
            return false;
        }

        /* Check that 'fValue' is the same */
        Object value = this.fValue;
        if (value == null) {
            if (other.fValue != null) {
                return false;
            }
        } else if (!value.equals(other.fValue)) {
            return false;
        }

        /* Check that 'fFields' are the same */
        if (!fFields.equals(other.fFields)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        if (fName.equals(ITmfEventField.ROOT_FIELD_ID)) {
            /*
             * If this field is a top-level "field container", we will print its
             * sub-fields directly.
             */
            appendSubFields(ret);

        } else {
            /* The field has its own values */
            ret.append(fName);
            ret.append('=');
            ret.append(fValue);

            if (!fFields.isEmpty()) {
                /*
                 * In addition to its own name/value, this field also has
                 * sub-fields.
                 */
                ret.append(" ["); //$NON-NLS-1$
                appendSubFields(ret);
                ret.append(']');
            }
        }
        return ret.toString();
    }

    private void appendSubFields(StringBuilder sb) {
        Joiner joiner = Joiner.on(", ").skipNulls(); //$NON-NLS-1$
        sb.append(joiner.join(getFields()));
    }

    /**
     * @since 2.0
     */
    @Override
    public String getFormattedValue() {
        return getValue().toString();
    }

}
