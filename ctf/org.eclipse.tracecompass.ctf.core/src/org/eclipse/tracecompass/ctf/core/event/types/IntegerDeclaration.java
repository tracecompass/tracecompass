/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *     Simon Marchi - Initial API and implementation
 *     Marc-Andre Laperle - Add min/maximum for validation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.event.types;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.math.BigInteger;
import java.nio.ByteOrder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.scope.IDefinitionScope;

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
public final class IntegerDeclaration extends Declaration implements ISimpleDatatypeDeclaration {

    // ------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------

    private static final int SIZE_64 = 64;
    private static final int SIZE_32 = 32;
    private static final int SIZE_27 = 27;
    private static final int SIZE_16 = 16;
    private static final int SIZE_8 = 8;
    private static final int SIZE_5 = 5;
    private static final int BYTE_ALIGN = 8;
    private static final int BASE_10 = 10;
    /**
     * unsigned int 32 bits big endian
     */
    public static final IntegerDeclaration UINT_32B_DECL = new IntegerDeclaration(32, false, ByteOrder.BIG_ENDIAN);
    /**
     * unsigned int 32 bits little endian
     */
    public static final IntegerDeclaration UINT_32L_DECL = new IntegerDeclaration(32, false, ByteOrder.LITTLE_ENDIAN);
    /**
     * signed int 32 bits big endian
     */
    public static final IntegerDeclaration INT_32B_DECL = new IntegerDeclaration(32, true, ByteOrder.BIG_ENDIAN);
    /**
     * signed int 32 bits little endian
     */
    public static final IntegerDeclaration INT_32L_DECL = new IntegerDeclaration(32, true, ByteOrder.LITTLE_ENDIAN);
    /**
     * unsigned int 32 bits big endian
     */
    public static final IntegerDeclaration UINT_64B_DECL = new IntegerDeclaration(64, false, ByteOrder.BIG_ENDIAN);
    /**
     * unsigned int 64 bits little endian
     */
    public static final IntegerDeclaration UINT_64L_DECL = new IntegerDeclaration(64, false, ByteOrder.LITTLE_ENDIAN);
    /**
     * signed int 64 bits big endian
     */
    public static final IntegerDeclaration INT_64B_DECL = new IntegerDeclaration(64, true, ByteOrder.BIG_ENDIAN);
    /**
     * signed int 64 bits little endian
     */
    public static final IntegerDeclaration INT_64L_DECL = new IntegerDeclaration(64, true, ByteOrder.LITTLE_ENDIAN);
    /**
     * unsigned 8 bit int endianness doesn't matter since it's 8 bits (byte)
     */
    public static final IntegerDeclaration UINT_8_DECL = new IntegerDeclaration(8, false, ByteOrder.BIG_ENDIAN);
    /**
     * signed 8 bit int endianness doesn't matter since it's 8 bits (char)
     */
    public static final IntegerDeclaration INT_8_DECL = new IntegerDeclaration(8, true, ByteOrder.BIG_ENDIAN);
    /**
     * Unsigned 5 bit int, used for event headers
     */
    public static final IntegerDeclaration UINT_5B_DECL = new IntegerDeclaration(5, false, 10, ByteOrder.BIG_ENDIAN, Encoding.NONE, "", 1); //$NON-NLS-1$
    /**
     * Unsigned 5 bit int, used for event headers
     */
    public static final IntegerDeclaration UINT_5L_DECL = new IntegerDeclaration(5, false, 10, ByteOrder.LITTLE_ENDIAN, Encoding.NONE, "", 1); //$NON-NLS-1$
    /**
     * Unsigned 5 bit int, used for event headers
     */
    public static final IntegerDeclaration UINT_27B_DECL = new IntegerDeclaration(27, false, 10, ByteOrder.BIG_ENDIAN, Encoding.NONE, "", 1); //$NON-NLS-1$
    /**
     * Unsigned 5 bit int, used for event headers
     */
    public static final IntegerDeclaration UINT_27L_DECL = new IntegerDeclaration(27, false, 10, ByteOrder.LITTLE_ENDIAN, Encoding.NONE, "", 1); //$NON-NLS-1$
    /**
     * Unsigned 16 bit int, used for event headers
     */
    public static final IntegerDeclaration UINT_16B_DECL = new IntegerDeclaration(16, false, ByteOrder.BIG_ENDIAN);
    /**
     * Unsigned 16 bit int, used for event headers
     */
    public static final IntegerDeclaration UINT_16L_DECL = new IntegerDeclaration(16, false, ByteOrder.LITTLE_ENDIAN);
    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final int fLength;
    private final boolean fSigned;
    private final int fBase;
    private final boolean fIsByteOrderSet;
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
     */
    public static IntegerDeclaration createDeclaration(int len, boolean signed, int base,
            @Nullable ByteOrder byteOrder, Encoding encoding, String clock, long alignment) {
        if (encoding.equals(Encoding.NONE) && (clock.equals("")) && base == BASE_10 && byteOrder != null) { //$NON-NLS-1$
            if (alignment == BYTE_ALIGN) {
                switch (len) {
                case SIZE_8:
                    return signed ? INT_8_DECL : UINT_8_DECL;
                case SIZE_16:
                    if (!signed) {
                        if (isBigEndian(byteOrder)) {
                            return UINT_16B_DECL;
                        }
                        return UINT_16L_DECL;
                    }
                    break;
                case SIZE_32:
                    if (signed) {
                        if (isBigEndian(byteOrder)) {
                            return INT_32B_DECL;
                        }
                        return INT_32L_DECL;
                    }
                    if (isBigEndian(byteOrder)) {
                        return UINT_32B_DECL;
                    }
                    return UINT_32L_DECL;
                case SIZE_64:
                    if (signed) {
                        if (isBigEndian(byteOrder)) {
                            return INT_64B_DECL;
                        }
                        return INT_64L_DECL;
                    }
                    if (isBigEndian(byteOrder)) {
                        return UINT_64B_DECL;
                    }
                    return UINT_64L_DECL;

                default:

                }

            } else if (alignment == 1) {
                switch (len) {
                case SIZE_5:
                    if (!signed) {
                        if (isBigEndian(byteOrder)) {
                            return UINT_5B_DECL;
                        }
                        return UINT_5L_DECL;
                    }
                    break;
                case SIZE_27:
                    if (!signed) {
                        if (isBigEndian(byteOrder)) {
                            return UINT_27B_DECL;
                        }
                        return UINT_27L_DECL;
                    }
                    break;
                default:
                    break;
                }
            }
        }
        return new IntegerDeclaration(len, signed, base, byteOrder, encoding, clock, alignment);
    }

    private static boolean isBigEndian(@Nullable ByteOrder byteOrder) {
        return (byteOrder != null) && byteOrder.equals(ByteOrder.BIG_ENDIAN);
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
        fLength = len;
        fSigned = signed;
        fBase = base;
        fIsByteOrderSet = byteOrder != null;
        fByteOrder = (byteOrder == null) ? ByteOrder.nativeOrder() : byteOrder;
        fEncoding = encoding;
        fClock = clock;
        fAlignment = Math.max(alignment, 1);
    }

    private IntegerDeclaration(int len, boolean signed, @Nullable ByteOrder byteOrder) {
        this(len, signed, BASE_10, byteOrder, Encoding.NONE, "", BYTE_ALIGN); //$NON-NLS-1$
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
     * @since 2.0
     */
    @Override
    public boolean isByteOrderSet() {
        return fIsByteOrderSet;
    }

    @Override
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
        return (fLength == SIZE_8) && (fEncoding != Encoding.NONE);
    }

    /**
     * Is the integer an unsigned byte (8 bits and no sign)?
     *
     * @return is the integer an unsigned byte
     */
    public boolean isUnsignedByte() {
        return (fLength == SIZE_8) && (!fSigned);
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

    @Override
    public int getMaximumSize() {
        return fLength;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public IntegerDefinition createDefinition(@Nullable IDefinitionScope definitionScope,
            String fieldName, BitBuffer input) throws CTFException {
        ByteOrder byteOrder = input.getByteOrder();
        input.setByteOrder(fByteOrder);
        long value = read(input);
        input.setByteOrder(byteOrder);
        return new IntegerDefinition(this, definitionScope, fieldName, value);
    }

    @Override
    public String toString() {
        return "[declaration] integer[length:" + fLength + (fSigned ? " " : " un") + "signed" + " base:" + fBase + " byteOrder:" + fByteOrder + " encoding:" + fEncoding + " alignment:" + fAlignment + "  clock:" + fClock + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
    }

    /**
     * Get the maximum value for this integer declaration.
     *
     * @return The maximum value for this integer declaration
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
        return checkNotNull(BigInteger.ONE.shiftLeft(significantBits).subtract(BigInteger.ONE));
    }

    /**
     * Get the minimum value for this integer declaration.
     *
     * @return The minimum value for this integer declaration
     */
    public BigInteger getMinValue() {
        if (!fSigned) {
            return checkNotNull(BigInteger.ZERO);
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
        return checkNotNull(BigInteger.ONE.shiftLeft(significantBits).negate());
    }

    private long read(BitBuffer input) throws CTFException {
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

        if (length > SIZE_64) {
            throw new CTFException("Cannot read an integer with over 64 bits. Length given: " + length); //$NON-NLS-1$
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (fAlignment ^ (fAlignment >>> 32));
        result = prime * result + fBase;
        result = prime * result + fByteOrder.toString().hashCode();
        result = prime * result + fClock.hashCode();
        result = prime * result + fEncoding.hashCode();
        result = prime * result + fLength;
        result = prime * result + (fSigned ? 1231 : 1237);
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
        IntegerDeclaration other = (IntegerDeclaration) obj;
        if (!isBinaryEquivalent(other)) {
            return false;
        }
        if (!fByteOrder.equals(other.fByteOrder)) {
            return false;
        }
        if (!fClock.equals(other.fClock)) {
            return false;
        }
        if (fEncoding != other.fEncoding) {
            return false;
        }
        return (fBase == other.fBase);
    }

    @Override
    public boolean isBinaryEquivalent(@Nullable IDeclaration obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        IntegerDeclaration other = (IntegerDeclaration) obj;
        return isBinaryEquivalent(other);
    }

    private boolean isBinaryEquivalent(IntegerDeclaration other) {
        if (fAlignment != other.fAlignment) {
            return false;
        }
        if (fLength != other.fLength) {
            return false;
        }
        if (fSigned != other.fSigned) {
            return false;
        }
        // no need for base
        // no need for encoding
        // no need for clock
        // byte inversion is ok on byte order if the element is one byte long
        return !((fLength != BYTE_ALIGN) && !fByteOrder.equals(other.fByteOrder));
    }

}
