/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *     Simon Marchi - Initial API and implementation
 *     Marc-Andre Laperle - Add min/maximum for validation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.event.types;

import java.math.BigInteger;
import java.nio.ByteOrder;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;

/**
 * A CTF integer declaration.
 *
 * The declaration of a integer basic data type.
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @author Simon Marchi
 */
@NonNullByDefault
public class IntegerDeclaration extends Declaration implements ISimpleDatatypeDeclaration {

    // ------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------

    /**
     * unsigned int 32 bits big endian
     *
     * @since 3.0
     */
    public static final IntegerDeclaration UINT_32B_DECL = new IntegerDeclaration(32, false, ByteOrder.BIG_ENDIAN);
    /**
     * unsigned int 32 bits little endian
     *
     * @since 3.0
     */
    public static final IntegerDeclaration UINT_32L_DECL = new IntegerDeclaration(32, false, ByteOrder.LITTLE_ENDIAN);
    /**
     * signed int 32 bits big endian
     *
     * @since 3.0
     */
    public static final IntegerDeclaration INT_32B_DECL = new IntegerDeclaration(32, true, ByteOrder.BIG_ENDIAN);
    /**
     * signed int 32 bits little endian
     *
     * @since 3.0
     */
    public static final IntegerDeclaration INT_32L_DECL = new IntegerDeclaration(32, true, ByteOrder.LITTLE_ENDIAN);
    /**
     * unsigned int 32 bits big endian
     *
     * @since 3.0
     */
    public static final IntegerDeclaration UINT_64B_DECL = new IntegerDeclaration(64, false, ByteOrder.BIG_ENDIAN);
    /**
     * unsigned int 64 bits little endian
     *
     * @since 3.0
     */
    public static final IntegerDeclaration UINT_64L_DECL = new IntegerDeclaration(64, false, ByteOrder.LITTLE_ENDIAN);
    /**
     * signed int 64 bits big endian
     *
     * @since 3.0
     */
    public static final IntegerDeclaration INT_64B_DECL = new IntegerDeclaration(64, true, ByteOrder.BIG_ENDIAN);
    /**
     * signed int 64 bits little endian
     *
     * @since 3.0
     */
    public static final IntegerDeclaration INT_64L_DECL = new IntegerDeclaration(64, true, ByteOrder.LITTLE_ENDIAN);
    /**
     * unsigned 8 bit int endianness doesn't matter since it's 8 bits (byte)
     *
     * @since 3.0
     */
    public static final IntegerDeclaration UINT_8_DECL = new IntegerDeclaration(8, false, ByteOrder.BIG_ENDIAN);
    /**
     * signed 8 bit int endianness doesn't matter since it's 8 bits (char)
     *
     * @since 3.0
     */
    public static final IntegerDeclaration INT_8_DECL = new IntegerDeclaration(8, true, ByteOrder.BIG_ENDIAN);
    /**
     * Unsigned 5 bit int, used for event headers
     *
     * @since 3.1
     */
    public static final IntegerDeclaration UINT_5B_DECL = new IntegerDeclaration(5, false, ByteOrder.BIG_ENDIAN);
    /**
     * Unsigned 5 bit int, used for event headers
     *
     * @since 3.1
     */
    public static final IntegerDeclaration UINT_5L_DECL = new IntegerDeclaration(5, false, ByteOrder.BIG_ENDIAN);
    /**
     * Unsigned 5 bit int, used for event headers
     *
     * @since 3.1
     */
    public static final IntegerDeclaration UINT_27B_DECL = new IntegerDeclaration(27, false, ByteOrder.BIG_ENDIAN);
    /**
     * Unsigned 5 bit int, used for event headers
     *
     * @since 3.1
     */
    public static final IntegerDeclaration UINT_27L_DECL = new IntegerDeclaration(27, false, ByteOrder.BIG_ENDIAN);
    /**
     * Unsigned 16 bit int, used for event headers
     *
     * @since 3.1
     */
    public static final IntegerDeclaration UINT_16B_DECL = new IntegerDeclaration(16, false, ByteOrder.BIG_ENDIAN);
    /**
     * Unsigned 16 bit int, used for event headers
     *
     * @since 3.1
     */
    public static final IntegerDeclaration UINT_16L_DECL = new IntegerDeclaration(16, false, ByteOrder.LITTLE_ENDIAN);
    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final int fLength;
    private final boolean fSigned;
    private final int fBase;
    private final ByteOrder fByteOrder;
    private final Encoding fEncoding;
    private final long fAlignment;
    private final String fClock;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Factory, some common types cached
     *
     * @param len
     *            The length in bits
     * @param signed
     *            Is the integer signed? false == unsigned
     * @param base
     *            The base (10-16 are most common)
     * @param byteOrder
     *            Big-endian little-endian or other
     * @param encoding
     *            ascii, utf8 or none.
     * @param clock
     *            The clock path, can be null
     * @param alignment
     *            The minimum alignment. Should be >= 1
     * @return the integer declaration
     * @since 3.0
     */
    public static IntegerDeclaration createDeclaration(int len, boolean signed, int base,
            @Nullable ByteOrder byteOrder, Encoding encoding, String clock, long alignment) {
        if (encoding.equals(Encoding.NONE) && (alignment == 8) && (clock.equals("")) && base == 10) { //$NON-NLS-1$
            switch (len) {
            case 5:
                if (!signed) {
                    if (byteOrder != null && byteOrder.equals(ByteOrder.BIG_ENDIAN)) {
                        return UINT_5B_DECL;
                    }
                    return UINT_5L_DECL;
                }
                break;
            case 8:
                return signed ? INT_8_DECL : UINT_8_DECL;
            case 16:
                if (!signed) {
                    if (byteOrder != null && byteOrder.equals(ByteOrder.BIG_ENDIAN)) {
                        return UINT_16B_DECL;
                    }
                    return UINT_16L_DECL;
                }
                break;
            case 27:
                if (!signed) {
                    if (byteOrder != null && byteOrder.equals(ByteOrder.BIG_ENDIAN)) {
                        return UINT_27B_DECL;
                    }
                    return UINT_27L_DECL;
                }
                break;
            case 32:
                if (signed) {
                    if (byteOrder != null && byteOrder.equals(ByteOrder.BIG_ENDIAN)) {
                        return INT_32B_DECL;
                    }
                    return INT_32L_DECL;
                }
                if (byteOrder != null && byteOrder.equals(ByteOrder.BIG_ENDIAN)) {
                    return UINT_32B_DECL;
                }
                return UINT_32L_DECL;
            case 64:
                if (signed) {
                    if (byteOrder != null && byteOrder.equals(ByteOrder.BIG_ENDIAN)) {
                        return INT_64B_DECL;
                    }
                    return INT_64L_DECL;
                }
                if (byteOrder != null && byteOrder.equals(ByteOrder.BIG_ENDIAN)) {
                    return UINT_64B_DECL;
                }
                return UINT_64L_DECL;
            default:
            }
        }
        return new IntegerDeclaration(len, signed, base, byteOrder, encoding, clock, alignment);
    }

    /**
     * Constructor
     *
     * @param len
     *            The length in bits
     * @param signed
     *            Is the integer signed? false == unsigned
     * @param base
     *            The base (10-16 are most common)
     * @param byteOrder
     *            Big-endian little-endian or other
     * @param encoding
     *            ascii, utf8 or none.
     * @param clock
     *            The clock path, can be null
     * @param alignment
     *            The minimum alignment. Should be &ge; 1
     */
    private IntegerDeclaration(int len, boolean signed, int base,
            @Nullable ByteOrder byteOrder, Encoding encoding, String clock, long alignment) {
        if (len <= 0 || len == 1 && signed) {
            throw new IllegalArgumentException();
        }

        fLength = len;
        fSigned = signed;
        fBase = base;

        @SuppressWarnings("null")
        @NonNull
        ByteOrder actualByteOrder = (byteOrder == null ? ByteOrder.nativeOrder() : byteOrder);
        fByteOrder = actualByteOrder;

        fEncoding = encoding;
        fClock = clock;
        fAlignment = Math.max(alignment, 1);
    }

    private IntegerDeclaration(int len, boolean signed, @Nullable ByteOrder byteOrder) {
        this(len, signed, 10, byteOrder, Encoding.NONE, "", 8); //$NON-NLS-1$
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * Is the integer signed?
     *
     * @return the is the integer signed
     */
    public boolean isSigned() {
        return fSigned;
    }

    /**
     * Get the integer base commonly decimal or hex
     *
     * @return the integer base
     */
    public int getBase() {
        return fBase;
    }

    /**
     * Get the byte order
     *
     * @return the byte order
     */
    public ByteOrder getByteOrder() {
        return fByteOrder;
    }

    /**
     * Get encoding, chars are 8 bit ints
     *
     * @return the encoding
     */
    public Encoding getEncoding() {
        return fEncoding;
    }

    /**
     * Is the integer a character (8 bits and encoded?)
     *
     * @return is the integer a char
     */
    public boolean isCharacter() {
        return (fLength == 8) && (fEncoding != Encoding.NONE);
    }

    /**
     * Is the integer an unsigned byte (8 bits and no sign)?
     *
     * @return is the integer an unsigned byte
     * @since 3.1
     */
    public boolean isUnsignedByte() {
        return (fLength == 8) && (!fSigned);
    }

    /**
     * Get the length in bits for this integer
     *
     * @return the length of the integer
     */
    public int getLength() {
        return fLength;
    }

    @Override
    public long getAlignment() {
        return fAlignment;
    }

    /**
     * The integer's clock, since timestamps are stored in ints
     *
     * @return the integer's clock, can be null. (most often it is)
     */
    public String getClock() {
        return fClock;
    }

    /**
     * @since 3.0
     */
    @Override
    public int getMaximumSize() {
        return fLength;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * @since 3.0
     */
    @Override
    public IntegerDefinition createDefinition(@Nullable IDefinitionScope definitionScope,
            String fieldName, BitBuffer input) throws CTFReaderException {
        ByteOrder byteOrder = input.getByteOrder();
        input.setByteOrder(fByteOrder);
        long value = read(input);
        input.setByteOrder(byteOrder);
        return new IntegerDefinition(this, definitionScope, fieldName, value);
    }

    @Override
    public String toString() {
        /* Only used for debugging */
        return "[declaration] integer[" + Integer.toHexString(hashCode()) + ']'; //$NON-NLS-1$
    }

    /**
     * Get the maximum value for this integer declaration.
     *
     * @return The maximum value for this integer declaration
     * @since 2.0
     */
    public BigInteger getMaxValue() {
        /*
         * Compute the number of bits able to represent an unsigned number,
         * ignoring sign bit.
         */
        int significantBits = fLength - (fSigned ? 1 : 0);
        /*
         * For a given N significant bits, compute the maximal value which is (1
         * << N) - 1.
         */

        @SuppressWarnings("null")
        @NonNull
        BigInteger ret = BigInteger.ONE.shiftLeft(significantBits).subtract(BigInteger.ONE);
        return ret;
    }

    /**
     * Get the minimum value for this integer declaration.
     *
     * @return The minimum value for this integer declaration
     * @since 2.0
     */
    public BigInteger getMinValue() {
        if (!fSigned) {
            @SuppressWarnings("null")
            @NonNull
            BigInteger ret = BigInteger.ZERO;
            return ret;
        }

        /*
         * Compute the number of bits able to represent an unsigned number,
         * without the sign bit.
         */
        int significantBits = fLength - 1;
        /*
         * For a given N significant bits, compute the minimal value which is -
         * (1 << N).
         */
        @SuppressWarnings("null")
        @NonNull
        BigInteger ret = BigInteger.ONE.shiftLeft(significantBits).negate();
        return ret;
    }

    private long read(BitBuffer input) throws CTFReaderException {
        /* Offset the buffer position wrt the current alignment */
        alignRead(input);

        boolean signed = isSigned();
        int length = getLength();
        long bits = 0;

        /*
         * Is the endianness of this field the same as the endianness of the
         * input buffer? If not, then temporarily set the buffer's endianness to
         * this field's just to read the data
         */
        ByteOrder previousByteOrder = input.getByteOrder();
        if ((getByteOrder() != input.getByteOrder())) {
            input.setByteOrder(getByteOrder());
        }

        if (length > 64) {
            throw new CTFReaderException("Cannot read an integer with over 64 bits. Length given: " + length); //$NON-NLS-1$
        }

        bits = input.get(length, signed);

        /*
         * Put the input buffer's endianness back to original if it was changed
         */
        if (previousByteOrder != input.getByteOrder()) {
            input.setByteOrder(previousByteOrder);
        }

        return bits;
    }

}
