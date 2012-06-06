/*******************************************************************************.
 * Copyright (c) 2011-2012 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial Design and implementation
 * Contributors: Francis Giraldeau - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.ctf.core.event.io;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * <b><u>BitBuffer</u></b>
 * <p>
 * A bitwise buffer capable of accessing fields with bit offsets.
 */
public class BitBuffer {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /* default bit width */
    /** 8 bits to a char */
    public static final int BIT_CHAR = 8;
    /** 16 bits to a short */
    public static final int BIT_SHORT = 16;
    /** 32 bits to an int */
    public static final int BIT_INT = 32;
    /** 32 bits to a float */
    public static final int BIT_FLOAT = 32;
    /** 64 bits to a long */
    public static final int BIT_LONG = 64;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private ByteBuffer buf;
    private int pos;
    private ByteOrder byteOrder;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Default constructor, makes a bigendian buffer
     */
    public BitBuffer() {
        this(null, ByteOrder.BIG_ENDIAN);
    }

    /**
     * Constructor, makes a bigendian buffer
     *
     * @param buf
     *            the bytebuffer to read
     */
    public BitBuffer(ByteBuffer buf) {
        this(buf, ByteOrder.BIG_ENDIAN);
    }

    /**
     * Constructor that is fully parametrisable
     *
     * @param buf
     *            the buffer to read
     * @param order
     *            the byte order (big endian, little endian, network?)
     */
    public BitBuffer(ByteBuffer buf, ByteOrder order) {
        setByteBuffer(buf);
        order(order);
        position(0);
    }

    // ------------------------------------------------------------------------
    // 'Get' operations on buffer
    // ------------------------------------------------------------------------

    /**
     * Relative <i>get</i> method for reading 32-bit integer.
     *
     * Reads next four bytes from the current bit position according to current
     * byte order.
     *
     * @return The int value read from the buffer
     */
    public int getInt() {
        int val = getInt(BIT_INT, true);
        pos += BIT_INT;
        return val;
    }

    /**
     * Relative <i>get</i> method for reading integer of <i>length</i> bits.
     *
     * Reads <i>length</i> bits starting at the current position. The result is
     * signed extended if <i>signed</i> is true. The current position is
     * increased of <i>length</i> bits.
     *
     * @param length
     *            The length in bits of this integer
     * @param signed
     *            The sign extended flag
     * @return The int value read from the buffer
     */
    public int getInt(int length, boolean signed) {
        int val;
        if (!canRead(pos, length)) {
            throw new BufferOverflowException();
        }
        if (length == 0) {
            val = 0;
        }
        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            val = getIntLE(pos, length, signed);
        } else {
            val = getIntBE(pos, length, signed);
        }
        pos += length;
        return val;
    }

    /**
     * Absolute <i>get</i> method for reading integer of <i>length</i> bits.
     *
     * Reads <i>length</i> bits starting from position <i>index</i>. The result
     * is signed extended if <i>signed</i> is true. The current position is
     * increased of <i>length</i> bits.
     *
     * @param index
     *            The start index in bits
     * @param length
     *            The length in bits to read
     * @param signed
     *            The sign extended flag
     * @return The int value read from the buffer
     */
    public int getInt(int index, int length, boolean signed) {
        if (!canRead(index, length)) {
            throw new BufferOverflowException();
        }
        if (length == 0) {
            return 0;
        }
        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            return getIntLE(index, length, signed);
        }
        return getIntBE(index, length, signed);
    }

    private int getIntBE(int index, int length, boolean signed) {
        assert ((length > 0) && (length <= BIT_INT));
        int end = index + length;
        int startByte = index / BIT_CHAR;
        int endByte = (end + (BIT_CHAR - 1)) / BIT_CHAR;
        int currByte, lshift, cshift, mask, cmask, cache;
        int value = 0;

        currByte = startByte;
        cache = buf.get(currByte) & 0xFF;
        boolean isNeg = (cache & (1 << (BIT_CHAR - (index % BIT_CHAR) - 1))) != 0;
        if (signed && isNeg) {
            value = ~0;
        }
        if (startByte == (endByte - 1)) {
            cmask = cache >>> ((BIT_CHAR - (end % BIT_CHAR)) % BIT_CHAR);
            if (((length) % BIT_CHAR) > 0) {
                mask = ~((~0) << length);
                cmask &= mask;
            }
            value <<= length;
            value |= cmask;
            return value;
        }
        cshift = index % BIT_CHAR;
        if (cshift > 0) {
            mask = ~((~0) << (BIT_CHAR - cshift));
            cmask = cache & mask;
            lshift = BIT_CHAR - cshift;
            value <<= lshift;
            value |= cmask;
            // index += lshift;
            currByte++;
        }
        for (; currByte < (endByte - 1); currByte++) {
            value <<= BIT_CHAR;
            value |= buf.get(currByte) & 0xFF;
        }
        lshift = end % BIT_CHAR;
        if (lshift > 0) {
            mask = ~((~0) << lshift);
            cmask = buf.get(currByte) & 0xFF;
            cmask >>>= BIT_CHAR - lshift;
            cmask &= mask;
            value <<= lshift;
            value |= cmask;
        } else {
            value <<= BIT_CHAR;
            value |= buf.get(currByte) & 0xFF;
        }
        return value;
    }

    private int getIntLE(int index, int length, boolean signed) {
        assert ((length > 0) && (length <= BIT_INT));
        int end = index + length;
        int startByte = index / BIT_CHAR;
        int endByte = (end + (BIT_CHAR - 1)) / BIT_CHAR;
        int currByte, lshift, cshift, mask, cmask, cache, mod;
        int value = 0;

        currByte = endByte - 1;
        cache = buf.get(currByte) & 0xFF;
        mod = end % BIT_CHAR;
        lshift = (mod > 0) ? mod : BIT_CHAR;
        boolean isNeg = (cache & (1 << (lshift - 1))) != 0;
        if (signed && isNeg) {
            value = ~0;
        }
        if (startByte == (endByte - 1)) {
            cmask = cache >>> (index % BIT_CHAR);
            if (((length) % BIT_CHAR) > 0) {
                mask = ~((~0) << length);
                cmask &= mask;
            }
            value <<= length;
            value |= cmask;
            return value;
        }
        cshift = end % BIT_CHAR;
        if (cshift > 0) {
            mask = ~((~0) << cshift);
            cmask = cache & mask;
            value <<= cshift;
            value |= cmask;
            // end -= cshift;
            currByte--;
        }
        for (; currByte >= (startByte + 1); currByte--) {
            value <<= BIT_CHAR;
            value |= buf.get(currByte) & 0xFF;
        }
        lshift = index % BIT_CHAR;
        if (lshift > 0) {
            mask = ~((~0) << (BIT_CHAR - lshift));
            cmask = buf.get(currByte) & 0xFF;
            cmask >>>= lshift;
            cmask &= mask;
            value <<= (BIT_CHAR - lshift);
            value |= cmask;
        } else {
            value <<= BIT_CHAR;
            value |= buf.get(currByte) & 0xFF;
        }
        return value;
    }

    // ------------------------------------------------------------------------
    // 'Put' operations on buffer
    // ------------------------------------------------------------------------

    /**
     * Relative <i>put</i> method to write signed 32-bit integer.
     *
     * Write four bytes starting from current bit position in the buffer
     * according to the current byte order. The current position is increased of
     * <i>length</i> bits.
     *
     * @param value
     *            The int value to write
     */
    public void putInt(int value) {
        putInt(BIT_INT, value);
    }

    /**
     * Relative <i>put</i> method to write <i>length</i> bits integer.
     *
     * Writes <i>length</i> lower-order bits from the provided <i>value</i>,
     * starting from current bit position in the buffer. Sequential bytes are
     * written according to the current byte order. The sign bit is carried to
     * the MSB if signed is true. The sign bit is included in <i>length</i>. The
     * current position is increased of <i>length</i>.
     *
     * @param length
     *            The number of bits to write
     * @param value
     *            The value to write
     */
    public void putInt(int length, int value) {
        putInt(this.pos, length, value);
    }

    /**
     * Absolute <i>put</i> method to write <i>length</i> bits integer.
     *
     * Writes <i>length</i> lower-order bits from the provided <i>value</i>,
     * starting from <i>index</i> position in the buffer. Sequential bytes are
     * written according to the current byte order. The sign bit is carried to
     * the MSB if signed is true. The sign bit is included in <i>length</i>. The
     * current position is increased of <i>length</i>.
     *
     * @param index
     *            The start position to write the value
     * @param value
     *            The value to write
     * @param length
     *            The number of bits to write
     */
    public void putInt(int index, int length, int value) {
        if (!canRead(index, length)) {
            throw new BufferOverflowException();
        }
        if (length == 0) {
            return;
        }
        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            putIntLE(index, length, value);
        } else {
            putIntBE(index, length, value);
        }
    }

    private void putIntBE(int index, int length, int value) {
        assert ((length > 0) && (length <= BIT_INT));
        int end = index + length;
        int startByte = index / BIT_CHAR;
        int endByte = (end + (BIT_CHAR - 1)) / BIT_CHAR;
        int currByte, lshift, cshift, mask, cmask;
        int correctedValue = value;

        /*
         * mask v high bits. Works for unsigned and two complement signed
         * numbers which value do not overflow on length bits.
         */

        if (length < BIT_INT) {
            correctedValue &= ~(~0 << length);
        }

        /* sub byte */
        if (startByte == (endByte - 1)) {
            lshift = (BIT_CHAR - (end % BIT_CHAR)) % BIT_CHAR;
            mask = ~((~0) << lshift);
            if ((index % BIT_CHAR) > 0) {
                mask |= (~(0)) << (BIT_CHAR - (index % BIT_CHAR));
            }
            cmask = correctedValue << lshift;
            /*
             * low bits are cleared because of lshift and high bits are already
             * cleared
             */
            cmask &= ~mask;
            int b = buf.get(startByte) & 0xFF;
            buf.put(startByte, (byte) ((b & mask) | cmask));
            return;
        }

        /* head byte contains MSB */
        currByte = endByte - 1;
        cshift = end % BIT_CHAR;
        if (cshift > 0) {
            lshift = BIT_CHAR - cshift;
            mask = ~((~0) << lshift);
            cmask = correctedValue << lshift;
            cmask &= ~mask;
            int b = buf.get(currByte) & 0xFF;
            buf.put(currByte, (byte) ((b & mask) | cmask));
            correctedValue >>>= cshift;
            // end -= cshift;
            currByte--;
        }

        /* middle byte(s) */
        for (; currByte >= (startByte + 1); currByte--) {
            buf.put(currByte, (byte) correctedValue);
            correctedValue >>>= BIT_CHAR;
        }
        /* end byte contains LSB */
        if ((index % BIT_CHAR) > 0) {
            mask = (~0) << (BIT_CHAR - (index % BIT_CHAR));
            cmask = correctedValue & ~mask;
            int b = buf.get(currByte) & 0xFF;
            buf.put(currByte, (byte) ((b & mask) | cmask));
        } else {
            buf.put(currByte, (byte) correctedValue);
        }
    }

    private void putIntLE(int index, int length, int value) {
        assert ((length > 0) && (length <= BIT_INT));
        int end = index + length;
        int startByte = index / BIT_CHAR;
        int endByte = (end + (BIT_CHAR - 1)) / BIT_CHAR;
        int currByte, lshift, cshift, mask, cmask;
        int correctedValue = value;

        /*
         * mask v high bits. Works for unsigned and two complement signed
         * numbers which value do not overflow on length bits.
         */

        if (length < BIT_INT) {
            correctedValue &= ~(~0 << length);
        }

        /* sub byte */
        if (startByte == (endByte - 1)) {
            lshift = index % BIT_CHAR;
            mask = ~((~0) << lshift);
            if ((end % BIT_CHAR) > 0) {
                mask |= (~(0)) << (end % BIT_CHAR);
            }
            cmask = correctedValue << lshift;
            /*
             * low bits are cleared because of lshift and high bits are already
             * cleared
             */
            cmask &= ~mask;
            int b = buf.get(startByte) & 0xFF;
            buf.put(startByte, (byte) ((b & mask) | cmask));
            return;
        }

        /* head byte */
        currByte = startByte;
        cshift = index % BIT_CHAR;
        if (cshift > 0) {
            mask = ~((~0) << cshift);
            cmask = correctedValue << cshift;
            cmask &= ~mask;
            int b = buf.get(currByte) & 0xFF;
            buf.put(currByte, (byte) ((b & mask) | cmask));
            correctedValue >>>= BIT_CHAR - cshift;
            // index += BIT_CHAR - cshift;
            currByte++;
        }

        /* middle byte(s) */
        for (; currByte < (endByte - 1); currByte++) {
            buf.put(currByte, (byte) correctedValue);
            correctedValue >>>= BIT_CHAR;
        }
        /* end byte */
        if ((end % BIT_CHAR) > 0) {
            mask = (~0) << (end % BIT_CHAR);
            cmask = correctedValue & ~mask;
            int b = buf.get(currByte) & 0xFF;
            buf.put(currByte, (byte) ((b & mask) | cmask));
        } else {
            buf.put(currByte, (byte) correctedValue);
        }
    }

    // ------------------------------------------------------------------------
    // Buffer attributes handling
    // ------------------------------------------------------------------------

    /**
     * Can this buffer be read for thus amount of bits?
     *
     * @param length
     *            the length in bits to read
     * @return does the buffer have enough room to read the next "length"
     */
    public boolean canRead(int length) {
        return canRead(pos, length);
    }

    /**
     * Can this buffer be read for thus amount of bits?
     *
     * @param index
     *            the position in the buffer to read
     * @param length
     *            the length in bits to read
     * @return does the buffer have enough room to read the next "length"
     */
    public boolean canRead(int index, int length) {
        if (buf == null) {
            return false;
        }

        if ((index + length) > (buf.capacity() * BIT_CHAR)) {
            return false;
        }
        return true;
    }

    /**
     * Sets the order of the buffer.
     *
     * @param order
     *            The order of the buffer.
     */
    public void order(ByteOrder order) {
        this.byteOrder = order;
        if (buf != null) {
            buf.order(order);
        }
    }

    /**
     * Sets the order of the buffer.
     *
     * @return The order of the buffer.
     */
    public ByteOrder order() {
        return byteOrder;
    }

    /**
     * Sets the position in the buffer.
     *
     * @param order
     *            The position of the buffer.
     */
    public void position(int newPosition) {
        this.pos = newPosition;
    }

    /**
     *
     * Sets the position in the buffer.
     *
     * @return order The position of the buffer.
     */
    public int position() {
        return pos;
    }

    /**
     * Sets the byte buffer
     *
     * @param buf
     *            the byte buffer
     */
    public void setByteBuffer(ByteBuffer buf) {
        this.buf = buf;
        if (buf != null) {
            this.buf.order(byteOrder);
        }
        clear();
    }

    /**
     * Gets the byte buffer
     *
     * @return The byte buffer
     */
    public ByteBuffer getByteBuffer() {
        return buf;
    }

    /**
     * Sets the byte order
     *
     * @param byteOrder
     *            The byte order
     */
    public void setByteOrder(ByteOrder byteOrder) {
        this.byteOrder = byteOrder;
    }

    /**
     * Gets the byte order
     *
     * @return The byte order
     */
    public ByteOrder getByteOrder() {
        return byteOrder;
    }

    /**
     * resets the bitbuffer.
     */
    public void clear() {
        position(0);

        if (buf == null) {
            return;
        }
        buf.clear();
    }

}
