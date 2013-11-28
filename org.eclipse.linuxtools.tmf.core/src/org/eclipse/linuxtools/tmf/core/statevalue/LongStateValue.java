/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html

 * Contributors:
 *   François Rajotte - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.statevalue;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A state value containing a long integer (8 bytes).
 *
 * @version 1.0
 * @author François Rajotte
 */
final class LongStateValue extends TmfStateValue {

    private final long value;

    public LongStateValue(long valueAsLong) {
        this.value = valueAsLong;
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
        return (this.value == other.value);
    }

    @Override
    public int hashCode() {
        return ((int) value) ^ ((int) (value >>> 32));
    }

    @Override
    public @Nullable String toString() {
        return String.format("%3d", value); //$NON-NLS-1$
    }

    // ------------------------------------------------------------------------
    // Unboxing methods
    // ------------------------------------------------------------------------

    @Override
    public long unboxLong() {
        return value;
    }
}
