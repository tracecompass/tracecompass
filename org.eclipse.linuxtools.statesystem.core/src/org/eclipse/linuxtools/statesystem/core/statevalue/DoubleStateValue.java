/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html

 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.statesystem.core.statevalue;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A state value containing a double primitive.
 *
 * @author Alexandre Montplaisir
 */
final class DoubleStateValue extends TmfStateValue {

    private final double value;

    public DoubleStateValue(double value) {
        this.value = value;
    }

    @Override
    public Type getType() {
        return Type.DOUBLE;
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (!(object instanceof DoubleStateValue)) {
            return false;
        }
        DoubleStateValue other = (DoubleStateValue) object;
        return (Double.compare(this.value, other.value) == 0);
    }

    @Override
    public int hashCode() {
        long bits = Double.doubleToLongBits(value);
        return ((int) bits) ^ ((int) (bits >>> 32));
    }

    @Override
    public @Nullable String toString() {
        return String.format("%3f", value); //$NON-NLS-1$
    }

    // ------------------------------------------------------------------------
    // Unboxing methods
    // ------------------------------------------------------------------------

    @Override
    public double unboxDouble() {
        return value;
    }
}
