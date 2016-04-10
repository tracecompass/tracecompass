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

/**
 * A state value that contains no particular value. It is sometimes needed over
 * a "null" reference, since we avoid NPE's this way.
 *
 * It can also be read either as a String ("nullValue") or an Integer (-1).
 *
 * @version 1.0
 * @author Alexandre Montplaisir
 */
final class NullStateValue extends TmfStateValue {

    private static final byte[] EMPTY_ARRAY = new byte[0];
    private static final String STR_VALUE = "nullValue"; //$NON-NLS-1$

    @Override
    public Type getType() {
        return Type.NULL;
    }

    @Override
    public boolean isNull() {
        return true;
    }

    @Override
    public boolean equals(@Nullable Object object) {
        return (object instanceof NullStateValue);
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String toString() {
        return STR_VALUE;
    }

    @Override
    public byte[] serialize() {
        return EMPTY_ARRAY;
    }

    // ------------------------------------------------------------------------
    // Unboxing methods. Null values can be unboxed into any type.
    // ------------------------------------------------------------------------

    @Override
    public int unboxInt() {
        return -1;
    }

    @Override
    public long unboxLong() {
        return -1;
    }

    @Override
    public double unboxDouble() {
        return Double.NaN;
    }

    @Override
    public String unboxStr() {
        return STR_VALUE;
    }

    @Override
    public int compareTo(@Nullable ITmfStateValue other) {
        if (other == null) {
            throw new IllegalArgumentException();
        }
        if (other instanceof NullStateValue) {
            return 0;
        }
        /*
         * For every other state value type, we defer to how that type wants to
         * be compared against null values.
         */
        return -(other.compareTo(this));
    }
}
