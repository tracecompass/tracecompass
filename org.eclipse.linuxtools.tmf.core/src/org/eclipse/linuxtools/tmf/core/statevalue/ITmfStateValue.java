/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.core.statevalue;

import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;


/**
 * This is the interface for using state values and reading their contents.
 *
 * @version 1.0
 * @author Alexandre Montplaisir
 */
public interface ITmfStateValue {

    /**
     * The supported types of state values
     * @since 2.0
     */
    public enum Type {
        /** Null value, for an interval not carrying any information */
        NULL,
        /** 32-bit integer value */
        INTEGER,
        /** Variable-length string value */
        STRING,
        /** 64-bit integer value */
        LONG
    }

    /**
     * Each implementation has to define which one (among the supported types)
     * they implement. There could be more than one implementation of each type,
     * depending on the needs of the different users.
     *
     * @return The ITmfStateValue.Type enum representing the type of this value
     * @since 2.0
     */
    public Type getType();

    /**
     * Only "null values" should return true here
     *
     * @return True if this type of SV is considered "null", false if it
     *         contains a real value.
     */
    public boolean isNull();

    /**
     * Read the contained value as an 'int' primitive
     *
     * @return The integer contained in the state value
     * @throws StateValueTypeException
     *             If the contained value cannot be read as an integer
     */
    public int unboxInt() throws StateValueTypeException;

    /**
     * Read the contained value as a String
     *
     * @return The String contained in the state value
     * @throws StateValueTypeException
     *             If the contained value cannot be read as a String
     */
    public String unboxStr() throws StateValueTypeException;

    /**
     * Read the contained value as a 'long' primitive
     *
     * @return The long contained in the state value
     * @throws StateValueTypeException
     *             If the contained value cannot be read as a long
     * @since 2.0
     */
    public long unboxLong() throws StateValueTypeException;
}
