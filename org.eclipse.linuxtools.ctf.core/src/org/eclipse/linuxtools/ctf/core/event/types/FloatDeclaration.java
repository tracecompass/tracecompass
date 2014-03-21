/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.event.types;

import java.nio.ByteOrder;

import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;

/**
 * A CTF float declaration.
 *
 * The declaration of a floating point basic data type.
 *
 * @version 1.0
 * @author Matthew Khouzam
 */
public final class FloatDeclaration extends Declaration {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final int fMantissa;
    private final int fExponent;
    private final ByteOrder fByteOrder;
    private final long fAlignement;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param exponent
     *            The exponent size in bits
     * @param mantissa
     *            The mantissa size in bits (+1 for sign) (see CTF spec)
     * @param byteOrder
     *            The byte order
     * @param alignment
     *            The alignment. Should be &ge; 1
     */
    public FloatDeclaration(int exponent, int mantissa, ByteOrder byteOrder,
            long alignment) {
        fMantissa = mantissa;
        fExponent = exponent;
        fByteOrder = byteOrder;
        fAlignement = Math.max(alignment, 1);

    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * @return the mant
     */
    public int getMantissa() {
        return fMantissa;
    }

    /**
     * @return the exp
     */
    public int getExponent() {
        return fExponent;
    }

    /**
     * @return the byteOrder
     */
    public ByteOrder getByteOrder() {
        return fByteOrder;
    }

    @Override
    public long getAlignment() {
        return fAlignement;
    }

    /**
     * @since 3.0
     */
    @Override
    public int getMaximumSize() {
        return fMantissa + fExponent + 1;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * @since 3.0
     */
    @Override
    public FloatDefinition createDefinition(IDefinitionScope definitionScope,
            String fieldName, BitBuffer input) throws CTFReaderException {
        alignRead(input);
        double value = read(input);
        return new FloatDefinition(this, definitionScope, fieldName, value);
    }

    @Override
    public String toString() {
        /* Only used for debugging */
        return "[declaration] float[" + Integer.toHexString(hashCode()) + ']'; //$NON-NLS-1$
    }

    private double read(BitBuffer input) throws CTFReaderException {
        /* Offset the buffer position wrt the current alignment */
        alignRead(input);
        final int exp = getExponent();
        final int mant = getMantissa();
        double value = Double.NaN;
        if ((exp + mant) == 32) {
            value = readRawFloat32(input, mant, exp);
        } else if ((exp + mant) == 64) {
            value = readRawFloat64(input, mant, exp);
        }
        return value;
    }

    private static double readRawFloat32(BitBuffer input, final int manBits,
            final int expBits) throws CTFReaderException {
        long temp = input.get(32, false);
        return createFloat(temp, manBits - 1, expBits);
    }

    private static double readRawFloat64(BitBuffer input, final int manBits,
            final int expBits) throws CTFReaderException {
        long temp = input.get(64, false);
        return createFloat(temp, manBits - 1, expBits);
    }

    /**
     * Create a float from the raw value, Mathematicians beware.
     *
     * @param rawValue
     *            The raw value( up to 64 bits)
     * @param manBits
     *            number of bits in the mantissa
     * @param expBits
     *            number of bits in the exponent
     */
    private static double createFloat(long rawValue, final int manBits,
            final int expBits) {
        long manShift = 1L << (manBits);
        long manMask = manShift - 1;
        long expMask = (1L << expBits) - 1;

        int exp = (int) ((rawValue >> (manBits)) & expMask) + 1;
        long man = (rawValue & manMask);
        final int offsetExponent = exp - (1 << (expBits - 1));
        double expPow = Math.pow(2.0, offsetExponent);
        double ret = man * 1.0f;
        ret /= manShift;
        ret += 1.0;
        ret *= expPow;
        return ret;
    }
}
