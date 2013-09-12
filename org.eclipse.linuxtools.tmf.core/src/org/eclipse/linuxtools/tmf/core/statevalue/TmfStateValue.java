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
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.core.statevalue;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;

/**
 * This is the wrapper class that exposes the different types of 'state values'
 * available to use in the State System.
 *
 * This also defines how these values are to be stored in the History Tree. For
 * example, we can save numerical values as integers instead of arrays of
 * 1-digit characters.
 *
 * The available types are Int, Long, Double and String.
 *
 * @version 1.0
 * @author Alexandre Montplaisir
 */
public abstract class TmfStateValue implements ITmfStateValue {
    // ------------------------------------------------------------------------
    // Factory methods to instantiate new state values
    // ------------------------------------------------------------------------

    /*
     * Since all "null state values" are the same, we only need one copy in
     * memory.
     */
    private static TmfStateValue nullValue = new NullStateValue();

    /**
     * Return an instance of a "null" value. Only one copy exists in memory.
     *
     * @return A null value
     */
    public static final TmfStateValue nullValue() {
        return nullValue;
    }

    /**
     * Factory constructor for Integer state values
     *
     * @param intValue
     *            The integer value to contain
     * @return The newly-created TmfStateValue object
     */
    public static TmfStateValue newValueInt(int intValue) {
        return new IntegerStateValue(intValue);
    }

    /**
     * Factory constructor for Long state values
     *
     * @param longValue
     *            The long value to contain
     * @return The newly-created TmfStateValue object
     * @since 2.0
     */
    public static TmfStateValue newValueLong(long longValue) {
        return new LongStateValue(longValue);
    }

    /**
     * Factory constructor for Double state values
     *
     * @param value
     *            The double value to contain
     * @return The newly-created TmfStateValue object
     */
    public static TmfStateValue newValueDouble(double value) {
        return new DoubleStateValue(value);
    }

    /**
     * Factory constructor for String state values
     *
     * @param strValue
     *            The string value to contain
     * @return The newly-created TmfStateValue object
     */
    public static TmfStateValue newValueString(@Nullable String strValue) {
        if (strValue == null) {
            return nullValue();
        }
        return new StringStateValue(strValue);
    }

    // ------------------------------------------------------------------------
    // Default unboxing methods.
    // Subclasses can override those for the types they support.
    // ------------------------------------------------------------------------

    private String unboxErrMsg(String targetType) {
        return "Type " + getClass().getSimpleName() + //$NON-NLS-1$
                " cannot be unboxed into a " + targetType + " value."; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public int unboxInt() throws StateValueTypeException {
        throw new StateValueTypeException(unboxErrMsg("Int")); //$NON-NLS-1$
    }

    @Override
    public long unboxLong() throws StateValueTypeException {
        throw new StateValueTypeException(unboxErrMsg("Long")); //$NON-NLS-1$
    }

    @Override
    public double unboxDouble() throws StateValueTypeException {
        throw new StateValueTypeException(unboxErrMsg("Double")); //$NON-NLS-1$
    }

    @Override
    public String unboxStr() throws StateValueTypeException {
        throw new StateValueTypeException(unboxErrMsg("String")); //$NON-NLS-1$
    }
}
