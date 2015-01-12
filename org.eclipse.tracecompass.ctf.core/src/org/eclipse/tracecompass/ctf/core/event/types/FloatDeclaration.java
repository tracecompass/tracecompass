/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.event.types;

import java.nio.ByteOrder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.tracecompass.ctf.core.trace.CTFReaderException;

/**
 * A CTF float declaration.
 *
 * The declaration of a floating point basic data type.
 *
 * @version 1.0
 * @author Matthew Khouzam
 */
@NonNullByDefault
public final class FloatDeclaration extends Declaration implements ISimpleDatatypeDeclaration {

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
    public FloatDeclaration(int exponent, int mantissa, @Nullable ByteOrder byteOrder,
            long alignment) {
        fMantissa = mantissa;
        fExponent = exponent;
        ByteOrder byteOrder2 = (byteOrder == null) ? ByteOrder.nativeOrder() : byteOrder;
        if (byteOrder2 == null) {
            throw new IllegalStateException("ByteOrder cannot be null"); //$NON-NLS-1$
        }
        fByteOrder = byteOrder2;
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
    public FloatDefinition createDefinition(@Nullable IDefinitionScope definitionScope,
            String fieldName, BitBuffer input) throws CTFReaderException {
        ByteOrder byteOrder = input.getByteOrder();
        input.setByteOrder(fByteOrder);
        double value = read(input);
        input.setByteOrder(byteOrder);
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (fAlignement ^ (fAlignement >>> 32));
        result = prime * result + fByteOrder.toString().hashCode(); // don't evaluate object but string
        result = prime * result + fExponent;
        result = prime * result + fMantissa;
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FloatDeclaration other = (FloatDeclaration) obj;
        if (fAlignement != other.fAlignement) {
            return false;
        }
        if (!fByteOrder.equals(other.fByteOrder)) {
            return false;
        }
        if (fExponent != other.fExponent) {
            return false;
        }
        if (fMantissa != other.fMantissa) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isBinaryEquivalent(@Nullable IDeclaration obj) {
        return equals(obj);
    }
}
