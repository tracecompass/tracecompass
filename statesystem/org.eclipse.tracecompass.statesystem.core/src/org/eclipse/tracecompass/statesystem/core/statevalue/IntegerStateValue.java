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
 * A state value containing a simple integer.
 *
 * @version 1.0
 * @author Alexandre Montplaisir
 */
final class IntegerStateValue extends TmfStateValue {

    private final int fValue;

    /**
     * Integer state value
     *
     * @param valueAsInt
     *            the value
     */
    public IntegerStateValue(int valueAsInt) {
        fValue = valueAsInt;
    }

    @Override
    public Type getType() {
        return Type.INTEGER;
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (!(object instanceof IntegerStateValue)) {
            return false;
        }
        IntegerStateValue other = (IntegerStateValue) object;
        return (this.fValue == other.fValue);
    }

    @Override
    public int hashCode() {
        return fValue;
    }

    @Override
    public @Nullable String toString() {
        return String.format("%3d", fValue); //$NON-NLS-1$
    }

    // ------------------------------------------------------------------------
    // Unboxing methods
    // ------------------------------------------------------------------------

    @Override
    public int unboxInt() {
        return fValue;
    }

    @Override
    public long unboxLong() {
        /* It's always safe to up-cast an int into a long */
        return fValue;
    }

    @Override
    public int compareTo(@Nullable ITmfStateValue other) {
        if (other == null) {
            throw new IllegalArgumentException();
        }

        switch (other.getType()) {
        case INTEGER:
            IntegerStateValue otherIntValue = (IntegerStateValue) other;
            return Integer.compare(this.fValue, otherIntValue.fValue);
        case DOUBLE:
            double otherDoubleValue = ((DoubleStateValue) other).unboxDouble();
            return Double.compare(this.fValue, otherDoubleValue);
        case LONG:
            long otherLongValue = ((LongStateValue) other).unboxLong();
            return Long.compare(this.fValue, otherLongValue);
        case NULL:
            return Integer.compare(this.fValue, other.unboxInt());
        case STRING:
            throw new StateValueTypeException("An Integer state value cannot be compared to a String state value."); //$NON-NLS-1$
        case CUSTOM:
        default:
            throw new StateValueTypeException("An Integer state value cannot be compared to the type " + other.getType()); //$NON-NLS-1$
        }

    }

    @Override
    public @Nullable Object unboxValue() {
        return fValue;
    }

}
