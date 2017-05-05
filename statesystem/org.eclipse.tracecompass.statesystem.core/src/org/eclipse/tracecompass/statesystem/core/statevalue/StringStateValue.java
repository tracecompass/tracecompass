/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.statevalue;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;

/**
 * A state value containing a variable-sized string
 *
 * @version 1.0
 * @author Alexandre Montplaisir
 */
final class StringStateValue extends TmfStateValue {

    private final String value;

    public StringStateValue(String valueAsString) {
        this.value = valueAsString;
    }

    @Override
    public Type getType() {
        return Type.STRING;
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (!(object instanceof StringStateValue)) {
            return false;
        }
        StringStateValue other = (StringStateValue) object;
        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }

    // ------------------------------------------------------------------------
    // Unboxing methods
    // ------------------------------------------------------------------------

    @Override
    public String unboxStr() {
        return value;
    }

    @Override
    public int compareTo(@Nullable ITmfStateValue other) {
        if (other == null) {
            throw new IllegalArgumentException();
        }

        switch (other.getType()) {
        case DOUBLE:
            throw new StateValueTypeException("A String state value cannot be compared to a Double state value."); //$NON-NLS-1$
        case INTEGER:
            throw new StateValueTypeException("A String state value cannot be compared to an Integer state value."); //$NON-NLS-1$
        case LONG:
            throw new StateValueTypeException("A String state value cannot be compared to a Long state value."); //$NON-NLS-1$
        case NULL:
            /*
             * We assume that every string state value is greater than a null
             * state value.
             */
            return 1;
        case STRING:
            StringStateValue otherStringValue = (StringStateValue) other;
            return value.compareTo(otherStringValue.value);
        case CUSTOM:
        default:
            throw new StateValueTypeException("A String state value cannot be compared to the type " + other.getType()); //$NON-NLS-1$
        }

    }

    @Override
    public @Nullable Object unboxValue() {
        return value;
    }

}
