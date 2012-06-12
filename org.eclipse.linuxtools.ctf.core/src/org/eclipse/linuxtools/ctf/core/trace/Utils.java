/*******************************************************************************
 * Copyright (c) 2011-2012 Ericsson, Ecole Polytechnique de Montreal and others
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

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * CTF magic number. (sort of looks like CTF CTF CT)
     */
    public final static int CTF_MAGIC = 0xC1FC1FC1;

    /**
     * TSDL magic number. (sort of looks like TSDL LSDT)
     */
    public final static int TSDL_MAGIC = 0x75D11D57;

    /**
     * TSDL magic number length in bytes.
     */
    public final static int TSDL_MAGIC_LEN = 4;

    /**
     * Directory separator on the current platform.
     */
    public final static String SEPARATOR = System.getProperty("file.separator"); //$NON-NLS-1$

    /**
     * Length in bytes of a UUID value.
     */
    public final static int UUID_LEN = 16;

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Unsigned long comparison.
     *
     * @param a
     *            First operand.
     * @param b
     *            Second operand.
     * @return -1 if a < b, 1 if a > b, 0 if a == b.
     */
    public static int unsignedCompare(long a, long b) {
        boolean aLeftBit = (a & (1 << (Long.SIZE - 1))) != 0;
        boolean bLeftBit = (b & (1 << (Long.SIZE - 1))) != 0;

        if (aLeftBit && !bLeftBit) {
            return 1;
        } else if (!aLeftBit && bLeftBit) {
            return -1;
        } else {
            if (a < b) {
                return -1;
            } else if (a > b) {
                return 1;
            } else {
                return 0;
            }
        }
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
