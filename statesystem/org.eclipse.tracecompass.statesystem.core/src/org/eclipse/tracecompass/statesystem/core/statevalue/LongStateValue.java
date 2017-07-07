/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html

 * Contributors:
 *   François Rajotte - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.statevalue;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;

/**
 * A state value containing a long integer (8 bytes).
 *
 * @version 1.0
 * @author François Rajotte
 */
final class LongStateValue extends TmfStateValue {

    private final long fValue;

    /**
     * Long value constructor
     *
     * @param valueAsLong
     *            the value
     */
    public LongStateValue(long valueAsLong) {
        fValue = valueAsLong;
    }

    @Override
    public Type getType() {
        return Type.LONG;
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (!(object instanceof LongStateValue)) {
            return false;
        }
        LongStateValue other = (LongStateValue) object;
        return (this.fValue == other.fValue);
    }

    @Override
    public int hashCode() {
        return ((int) fValue) ^ ((int) (fValue >>> 32));
    }

    @Override
    public @Nullable String toString() {
        return String.format("%3d", fValue); //$NON-NLS-1$
    }

    // ------------------------------------------------------------------------
    // Unboxing methods
    // ------------------------------------------------------------------------

    @Override
    public long unboxLong() {
        return fValue;
    }

    @Override
    public int compareTo(@Nullable ITmfStateValue other) {
        if (other == null) {
            throw new IllegalArgumentException();
        }

        switch (other.getType()) {
        case INTEGER:
            long otherLongValue = ((IntegerStateValue) other).unboxInt();
            return Long.compare(this.fValue, otherLongValue);
        case DOUBLE:
            double otherDoubleValue = ((DoubleStateValue) other).unboxDouble();
            return Double.compare(this.fValue, otherDoubleValue);
        case LONG:
            otherLongValue = ((LongStateValue) other).unboxLong();
            return Long.compare(this.fValue, otherLongValue);
        case NULL:
            return Long.compare(this.fValue, other.unboxLong());
        case STRING:
            throw new StateValueTypeException("A Long state value cannot be compared to a String state value."); //$NON-NLS-1$
        case CUSTOM:
        default:
            throw new StateValueTypeException("A Long state value cannot be compared to the type " + other.getType()); //$NON-NLS-1$
        }

    }

    @Override
    public @Nullable Object unboxValue() {
        return fValue;
    }

}
