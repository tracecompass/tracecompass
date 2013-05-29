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
    public boolean equals(Object other) {
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
     * @param intValue The integer value to contain
     * @return The newly-created TmfStateValue object
     */
    public static TmfStateValue newValueInt(int intValue) {
        if (intValue == -1) {
            return nullValue();
        }
        return new IntegerStateValue(intValue);
    }

    /**
     * Factory constructor for String state values
     *
     * @param strValue The string value to contain
     * @return The newly-create TmfStateValue object
     */
    public static TmfStateValue newValueString(String strValue) {
        if (strValue == null) {
            return nullValue();
        }
        return new StringStateValue(strValue);
    }

    /**
     * Factory constructor for Long state values
     *
     * @param longValue The long value to contain
     * @return The newly-create TmfStateValue object
     * @since 2.0
     */
    public static TmfStateValue newValueLong(long longValue) {
        if (longValue == -1) {
            return nullValue();
        }
        return new LongStateValue(longValue);
    }

    @Override
    public int unboxInt() throws StateValueTypeException {
        if (this.isNull()) {
            /* Int value expected, return "-1" instead */
            return -1;
        }

        if (this.getType() != Type.INTEGER) {
            throw new StateValueTypeException();
        }
        return (Integer) this.getValue();
    }

    @Override
    public String unboxStr() throws StateValueTypeException {
        if (this.isNull()) {
            /* String value expected, return "nullValue" instead */
            return "nullValue"; //$NON-NLS-1$
        }

        if (this.getType() != Type.STRING) {
            throw new StateValueTypeException();
        }
        return (String) this.getValue();
    }

    /**
     * @since 2.0
     */
    @Override
    public long unboxLong() throws StateValueTypeException {
        if (this.isNull()) {
            /* Long value expected, return "-1" instead */
            return -1;
        }

        if (this.getType() != Type.LONG) {
            throw new StateValueTypeException();
        }
        return (Long) this.getValue();
    }
}
