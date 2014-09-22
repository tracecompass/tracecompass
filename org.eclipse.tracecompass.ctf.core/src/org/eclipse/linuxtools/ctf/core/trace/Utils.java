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

import org.eclipse.linuxtools.ctf.core.event.types.AbstractArrayDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.CompoundDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDefinition;

/**
 * Various utilities.
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @author Simon Marchi
 */
public final class Utils {

    private Utils() {
    }

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
     * <strong> As Java does not support unsigned types and arithmetic,
     * parameters are received encoded as a signed long (two-complement) but the
     * operation is an unsigned comparator.</strong>
     *
     * @param left
     *            Left operand of the comparator.
     * @param right
     *            Right operand of the comparator.
     * @return -1 if left &lt; right, 1 if left &gt; right, 0 if left == right.
     */
    public static int unsignedCompare(long left, long right) {
        /*
         * This method assumes that the arithmetic overflow on signed integer
         * wrap on a circular domain (modulo arithmetic in two-complement),
         * which is the defined behavior in Java.
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
     * Gets a UUID from an array defintion
     *
     * @param uuidDef
     *            the array defintions, must contain integer bytes
     * @return the UUID
     * @throws CTFReaderException
     *             if the definition contains less than 16 elements
     * @since 3.1
     */
    public static UUID getUUIDfromDefinition(AbstractArrayDefinition uuidDef) throws CTFReaderException {
        byte[] uuidArray = new byte[16];
        IDeclaration declaration = uuidDef.getDeclaration();
        if (!(declaration instanceof CompoundDeclaration)) {
            throw new CTFReaderException("UUID must be a sequence of unsigned bytes"); //$NON-NLS-1$
        }
        CompoundDeclaration uuidDec = (CompoundDeclaration) declaration;

        IDeclaration uuidElem = uuidDec.getElementType();
        if (!(uuidElem instanceof IntegerDeclaration)) {
            throw new CTFReaderException("UUID must be a sequence of unsigned bytes"); //$NON-NLS-1$
        }
        IntegerDeclaration intUuidElem = (IntegerDeclaration) uuidElem;
        if (!intUuidElem.isUnsignedByte()) {
            throw new CTFReaderException("UUID must be a sequence of unsigned bytes"); //$NON-NLS-1$
        }
        return getUUID(uuidDef, uuidArray);
    }

    private static UUID getUUID(AbstractArrayDefinition uuidDef, byte[] uuidArray) throws CTFReaderException {
        for (int i = 0; i < uuidArray.length; i++) {
            IntegerDefinition uuidByteDef = (IntegerDefinition) uuidDef.getDefinitions().get(i);
            if (uuidByteDef == null) {
                throw new CTFReaderException("UUID incomplete, only " + i + " bytes available"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            uuidArray[i] = (byte) uuidByteDef.getValue();
        }

        UUID uuid = Utils.makeUUID(uuidArray);
        return uuid;
    }

    /**
     * Gets a UUID from an array defintion
     *
     * @param uuidDef
     *            the array defintions, must contain integer bytes
     * @return the UUID
     * @throws CTFReaderException
     *             if the definition contains less than 16 elements
     * @since 3.1
     * @deprecated use
     *             {@link Utils#getUUIDfromDefinition(AbstractArrayDefinition uuidDef)}
     */
    @Deprecated
    public static UUID getUUIDfromDefinition(org.eclipse.linuxtools.ctf.core.event.types.ArrayDefinition uuidDef) throws CTFReaderException {
        byte[] uuidArray = new byte[16];
        return getUUID(uuidDef, uuidArray);
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
