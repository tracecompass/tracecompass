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

package org.eclipse.linuxtools.tmf.core.statevalue;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A state value containing a double primitive.
 *
 * @author Alexandre Montplaisir
 */
final class DoubleStateValue extends TmfStateValue {

    private final Double valueDouble;

    public DoubleStateValue(double value) {
        valueDouble = new Double(value);
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
    public Double getValue() {
        return valueDouble;
    }

    @Override
    public @Nullable String toString() {
        return String.format("%3f", valueDouble); //$NON-NLS-1$
    }

    // ------------------------------------------------------------------------
    // Unboxing methods
    // ------------------------------------------------------------------------

    @Override
    public double unboxDouble() {
        return valueDouble;
    }
}
