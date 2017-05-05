/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API
 ******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.statevalue;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;

/**
 * This is the interface for using state values and reading their contents.
 *
 * @author Alexandre Montplaisir
 */
public interface ITmfStateValue extends Comparable<ITmfStateValue> {

    /**
     * The supported types of state values
     */
    public enum Type {
        /** Null value, for an interval not carrying any information */
        NULL,
        /** 32-bit integer value */
        INTEGER,
        /** 64-bit integer value */
        LONG,
        /** IEEE 754 double precision number */
        DOUBLE,
        /** Variable-length string value */
        STRING,
        /** Custom state value type
         * @since 2.0 */
        CUSTOM;
    }

    /**
     * Each implementation has to define which one (among the supported types)
     * they implement. There could be more than one implementation of each type,
     * depending on the needs of the different users.
     *
     * @return The ITmfStateValue.Type enum representing the type of this value
     */
    Type getType();

    /**
     * Only "null values" should return true here
     *
     * @return True if this type of SV is considered "null", false if it
     *         contains a real value.
     */
    boolean isNull();

    /**
     * Read the contained value as an 'int' primitive
     *
     * @return The integer contained in the state value
     * @throws StateValueTypeException
     *             If the contained value cannot be read as an integer
     */
    int unboxInt();

    /**
     * Read the contained value as a 'long' primitive
     *
     * @return The long contained in the state value
     * @throws StateValueTypeException
     *             If the contained value cannot be read as a long
     */
    long unboxLong();

    /**
     * Read the contained value as a 'double' primitive
     *
     * @return The double contained in the state value
     * @throws StateValueTypeException
     *             If the contained value cannot be read as a double
     */
    double unboxDouble();

    /**
     * Read the contained value as a String
     *
     * @return The String contained in the state value
     * @throws StateValueTypeException
     *             If the contained value cannot be read as a String
     */
    String unboxStr();

    /**
     * Getter for this {@link ITmfStateValue}'s Object
     *
     * @return the object in this {@link ITmfStateValue}
     * @since 3.0
     */
    default @Nullable Object unboxValue() {
        return null;
    }

}
