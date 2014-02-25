/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.trace;

import java.util.UUID;

/**
 * Various utilities.
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @author Simon Marchi
 */
public class Utils {

    private Utils() {}

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * CTF magic number. (sort of looks like CTF CTF CT)
     */
    public static final int CTF_MAGIC = 0xC1FC1FC1;

    /**
     * TSDL magic number. (sort of looks like TSDL LSDT)
     */
    public static final int TSDL_MAGIC = 0x75D11D57;

    /**
     * TSDL magic number length in bytes.
     */
    public static final int TSDL_MAGIC_LEN = 4;

    /**
     * Directory separator on the current platform.
     */
    public static final String SEPARATOR = System.getProperty("file.separator"); //$NON-NLS-1$

    /**
     * Length in bytes of a UUID value.
     */
    public static final int UUID_LEN = 16;

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Performs an unsigned long comparison on two unsigned long numbers.
     *
     * @note As Java does not support unsigned types and arithmetic, parameters
     *       are received encoded as a signed long (two-complement) but the
     *       operation is an unsigned comparator.
     *
     * @param left
     *            Left operand of the comparator.
     * @param right
     *            Right operand of the comparator.
     * @return -1 if left < right, 1 if left > right, 0 if left == right.
     */
    public static int unsignedCompare(long left, long right) {
        /*
         * This method assumes that the arithmetic overflow on signed
         * integer wrap on a circular domain (modulo arithmetic in
         * two-complement), which is the defined behavior in Java.
         *
         * This idea is to rotate the domain by the length of the negative
         * space, and then use the signed operator.
         */
        final long a = left + Long.MIN_VALUE;
        final long b = right + Long.MIN_VALUE;
        if (a < b) {
            return -1;
        } else if (a > b) {
            return 1;
        }
        return 0;
    }

    /**
     * Creates a UUID object from an array of 16 bytes.
     *
     * @param bytes
     *            Array of 16 bytes.
     * @return A UUID object.
     */
    public static UUID makeUUID(byte bytes[]) {
        long high = 0;
        long low = 0;

        assert (bytes.length == Utils.UUID_LEN);

        for (int i = 0; i < 8; i++) {
            low = (low << 8) | (bytes[i + 8] & 0xFF);
            high = (high << 8) | (bytes[i] & 0xFF);
        }

        UUID uuid = new UUID(high, low);

        return uuid;
    }

}
