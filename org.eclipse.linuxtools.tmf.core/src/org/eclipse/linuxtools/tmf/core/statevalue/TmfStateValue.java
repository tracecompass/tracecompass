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
 * For now the two available types are either int or String.
 *
 * @version 1.0
 * @author Alexandre Montplaisir
 */
public abstract class TmfStateValue implements ITmfStateValue {

    /**
     * Retrieve directly the value object contained within. Implementing
     * subclasses may limit the return type here.
     *
     * It's protected, since we do not want to expose this directly in the
     * public API (and require all its users to manually cast to the right
     * types). All accesses to the values should go through the "unbox-"
     * methods.
     *
     * @return The underneath object assigned to this state value.
     */
    protected abstract Object getValue();

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof TmfStateValue)) {
            return false;
        }

        /* If both types are different they're necessarily not equal */
        if (this.getType() != ((TmfStateValue) other).getType()) {
            return false;
        }

        /*
         * This checks for the case where we'd compare two null values (and so
         * avoid a NPE below)
         */
        if (this.isNull()) {
            return true;
        }

        /* The two are valid and comparable, let's compare them */
        return this.getValue().equals(((TmfStateValue) other).getValue());
    }

    @Override
    public int hashCode() {
        if (this.isNull()) {
            return 0;
        }
        return this.getValue().hashCode();
    }

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
        if (intValue == -1) {
            return nullValue();
        }
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
        if (longValue == -1) {
            return nullValue();
        }
        return new LongStateValue(longValue);
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
                "cannot be unboxed into a " + targetType + " value."; //$NON-NLS-1$ //$NON-NLS-2$
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
    public String unboxStr() throws StateValueTypeException {
        throw new StateValueTypeException(unboxErrMsg("String")); //$NON-NLS-1$
    }
}
