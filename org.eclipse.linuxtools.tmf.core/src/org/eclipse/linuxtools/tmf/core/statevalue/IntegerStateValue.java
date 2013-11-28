/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.statevalue;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A state value containing a simple integer.
 *
 * @version 1.0
 * @author Alexandre Montplaisir
 */
final class IntegerStateValue extends TmfStateValue {

    private final int value;

    public IntegerStateValue(int valueAsInt) {
        this.value = valueAsInt;
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
        return (this.value == other.value);
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public @Nullable String toString() {
        return String.format("%3d", value); //$NON-NLS-1$
    }

    // ------------------------------------------------------------------------
    // Unboxing methods
    // ------------------------------------------------------------------------

    @Override
    public int unboxInt() {
        return value;
    }

    @Override
    public long unboxLong() {
        /* It's always safe to up-cast a int into a long */
        return value;
    }
}
