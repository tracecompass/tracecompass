/*******************************************************************************.
 * Copyright (c) 2011-2012 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Matthew Khouzam - Initial Design and implementation + overhaul
 *  Francis Giraldeau - Initial API and implementation
 *  Philippe Proulx - Some refinement and optimization
 *  Etienne Bergeron <Etienne.Bergeron@gmail.com> - fix zero size read + cleanup
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.event.io;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;

/**
 * <b><u>BitBuffer</u></b>
 * <p>
 * A bitwise buffer capable of accessing fields with bit offsets.
 *
 * @since 2.0
 */
public final class BitBuffer {

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
    private long pos;
    private ByteOrder byteOrder;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Default constructor, makes a big-endian buffer
     */
    public BitBuffer() {
        this(null, ByteOrder.BIG_ENDIAN);
    }

    /**
     * Constructor, makes a big-endian buffer
     *
     * @param buf
     *            the bytebuffer to read
     */
    public BitBuffer(ByteBuffer buf) {
        this(buf, ByteOrder.BIG_ENDIAN);
    }

    /**
     * Constructor that is fully parameterizable
     *
     * @param buf
     *            the buffer to read
     * @param order
     *            the byte order (big-endian, little-endian, network?)
     */
    public BitBuffer(ByteBuffer buf, ByteOrder order) {
        setByteBuffer(buf);
        setByteOrder(order);
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
     * @return The int value (signed) read from the buffer
     * @throws CTFReaderException
     *             An error occurred reading the long. This exception can be
     *             raised if the buffer tries to read out of bounds
     */
    public int getInt() throws CTFReaderException {
        return getInt(BIT_INT, true);
    }

    /**
     * Relative <i>get</i> method for reading 64-bit integer.
     *
     * Reads next eight bytes from the current bit position according to current
     * byte order.
     *
     * @return The long value (signed) read from the buffer
     * @throws CTFReaderException
     *             An error occurred reading the long. This exception can be
     *             raised if the buffer tries to read out of bounds
     */
    public long getLong() throws CTFReaderException {
        return get(BIT_LONG, true);
    }

    /**
     * Relative <i>get</i> method for reading long of <i>length</i> bits.
     *
     * Reads <i>length</i> bits starting at the current position. The result is
     * signed extended if <i>signed</i> is true. The current position is
     * increased of <i>length</i> bits.
     *
     * @param length
     *            The length in bits of this integer
     * @param signed
     *            The sign extended flag
     * @return The long value read from the buffer
     * @throws CTFReaderException
     *             An error occurred reading the data. If more than 64 bits at a
     *             time are read, or the buffer is read beyond its end, this
     *             exception will be raised.
     */
    public long get(int length, boolean signed) throws CTFReaderException {
        if (length > BIT_LONG) {
            throw new CTFReaderException("Cannot read a long longer than 64 bits. Rquested: " + length); //$NON-NLS-1$
        }
        if (length > BIT_INT) {
            final int highShift = length - BIT_INT;
            long a = getInt();
            long b = getInt(highShift, false);
            long retVal;
            /* Cast the signed-extended int into a unsigned int. */
            a &= 0xFFFFFFFFL;
            b &= (1L << highShift) - 1L;

            retVal = (this.byteOrder == ByteOrder.BIG_ENDIAN) ? ((a << highShift) | b) : ((b << BIT_INT) | a);
            /* sign extend */
            if (signed) {
                int signExtendBits = BIT_LONG - length;
                retVal = (retVal << signExtendBits) >> signExtendBits;
            }
            return retVal;
        }
        long retVal = getInt(length, signed);
        return (signed ? retVal : (retVal & 0xFFFFFFFFL));
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
     * @throws CTFReaderException
     *             An error occurred reading the data. When the buffer is read
     *             beyond its end, this exception will be raised.
     */
    private int getInt(int length, boolean signed) throws CTFReaderException {

        /* Nothing to read. */
        if (length == 0) {
            return 0;
        }

        /* Validate that the buffer has enough bits. */
        if (!canRead(length)) {
            throw new CTFReaderException("Cannot read the integer, " + //$NON-NLS-1$
                    "the buffer does not have enough remaining space. " + //$NON-NLS-1$
                    "Requested:" + length); //$NON-NLS-1$
        }

        /* Get the value from the byte buffer. */
        int val = 0;
        boolean gotIt = false;

        /*
         * Try a fast read when the position is byte-aligned by using
         * java.nio.ByteBuffer's native methods
         */
        /*
         * A faster alignment detection as the compiler cannot guaranty that pos
         * is always positive.
         */
        if ((this.pos & (BitBuffer.BIT_CHAR - 1)) == 0) {
            switch (length) {
            case BitBuffer.BIT_CHAR:
                // Byte
                val = this.buf.get((int) (this.pos / 8));
                if (!signed) {
                    val = val & 0xff;
                }
                gotIt = true;
                break;

            case BitBuffer.BIT_SHORT:
                // Word
                val = this.buf.getShort((int) (this.pos / 8));
                if (!signed) {
                    val = val & 0xffff;
                }
                gotIt = true;
                break;

            case BitBuffer.BIT_INT:
                // Double word
                val = this.buf.getInt((int) (this.pos / 8));
                gotIt = true;
                break;

            default:
                break;
            }
        }

        /* When not byte-aligned, fall-back to a general decoder. */
        if (!gotIt) {
            // Nothing read yet: use longer methods
            if (this.byteOrder == ByteOrder.LITTLE_ENDIAN) {
                val = getIntLE(this.pos, length, signed);
            } else {
                val = getIntBE(this.pos, length, signed);
            }
        }
        this.pos += length;

        return val;
    }

    private int getIntBE(long index, int length, boolean signed) {
        assert ((length > 0) && (length <= BIT_INT));
        long end = index + length;
        int startByte = (int) (index / BIT_CHAR);
        int endByte = (int) ((end + (BIT_CHAR - 1)) / BIT_CHAR);
        int currByte, lshift, cshift, mask, cmask, cache;
        int value = 0;

        currByte = startByte;
        cache = this.buf.get(currByte) & 0xFF;
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
        cshift = (int) (index % BIT_CHAR);
        if (cshift > 0) {
            mask = ~((~0) << (BIT_CHAR - cshift));
            cmask = cache & mask;
            lshift = BIT_CHAR - cshift;
            value <<= lshift;
            value |= cmask;
            currByte++;
        }
        for (; currByte < (endByte - 1); currByte++) {
            value <<= BIT_CHAR;
            value |= this.buf.get(currByte) & 0xFF;
        }
        lshift = (int) (end % BIT_CHAR);
        if (lshift > 0) {
            mask = ~((~0) << lshift);
            cmask = this.buf.get(currByte) & 0xFF;
            cmask >>>= BIT_CHAR - lshift;
            cmask &= mask;
            value <<= lshift;
            value |= cmask;
        } else {
            value <<= BIT_CHAR;
            value |= this.buf.get(currByte) & 0xFF;
        }
        return value;
    }

    private int getIntLE(long index, int length, boolean signed) {
        assert ((length > 0) && (length <= BIT_INT));
        long end = index + length;
        int startByte = (int) (index / BIT_CHAR);
        int endByte = (int) ((end + (BIT_CHAR - 1)) / BIT_CHAR);
        int currByte, lshift, cshift, mask, cmask, cache, mod;
        int value = 0;

        currByte = endByte - 1;
        cache = buf.get(currByte) & 0xFF;
        mod = (int) (end % BIT_CHAR);
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
        cshift = (int) (end % BIT_CHAR);
        if (cshift > 0) {
            mask = ~((~0) << cshift);
            cmask = cache & mask;
            value <<= cshift;
            value |= cmask;
            currByte--;
        }
        for (; currByte >= (startByte + 1); currByte--) {
            value <<= BIT_CHAR;
            value |= buf.get(currByte) & 0xFF;
        }
        lshift = (int) (index % BIT_CHAR);
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
     * @throws CTFReaderException
     *             An error occurred writing the data. If the buffer is written
     *             beyond its end, this exception will be raised.
     */
    public void putInt(int value) throws CTFReaderException {
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
     * @throws CTFReaderException
     *             An error occurred writing the data. If the buffer is written
     *             beyond its end, this exception will be raised.
     */
    public void putInt(int length, int value) throws CTFReaderException {
        final long curPos = this.pos;

        if (!canRead(length)) {
            throw new CTFReaderException("Cannot write to bitbuffer, " //$NON-NLS-1$
                    + "insufficient space. Requested: " + length); //$NON-NLS-1$
        }
        if (length == 0) {
            return;
        }
        if (this.byteOrder == ByteOrder.LITTLE_ENDIAN) {
            putIntLE(curPos, length, value);
        } else {
            putIntBE(curPos, length, value);
        }
        this.pos += length;
    }

    private void putIntBE(long index, int length, int value) {
        assert ((length > 0) && (length <= BIT_INT));
        long end = index + length;
        int startByte = (int) (index / BIT_CHAR);
        int endByte = (int) ((end + (BIT_CHAR - 1)) / BIT_CHAR);
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
            lshift = (int) ((BIT_CHAR - (end % BIT_CHAR)) % BIT_CHAR);
            mask = ~((~0) << lshift);
            if ((index % BIT_CHAR) > 0) {
                mask |= (~(0)) << (BIT_CHAR - (index % BIT_CHAR));
            }
            cmask = correctedValue << lshift;
            /*
             * low bits are cleared because of left-shift and high bits are
             * already cleared
             */
            cmask &= ~mask;
            int b = this.buf.get(startByte) & 0xFF;
            this.buf.put(startByte, (byte) ((b & mask) | cmask));
            return;
        }

        /* head byte contains MSB */
        currByte = endByte - 1;
        cshift = (int) (end % BIT_CHAR);
        if (cshift > 0) {
            lshift = BIT_CHAR - cshift;
            mask = ~((~0) << lshift);
            cmask = correctedValue << lshift;
            cmask &= ~mask;
            int b = this.buf.get(currByte) & 0xFF;
            this.buf.put(currByte, (byte) ((b & mask) | cmask));
            correctedValue >>>= cshift;
            currByte--;
        }

        /* middle byte(s) */
        for (; currByte >= (startByte + 1); currByte--) {
            this.buf.put(currByte, (byte) correctedValue);
            correctedValue >>>= BIT_CHAR;
        }
        /* end byte contains LSB */
        if ((index % BIT_CHAR) > 0) {
            mask = (~0) << (BIT_CHAR - (index % BIT_CHAR));
            cmask = correctedValue & ~mask;
            int b = this.buf.get(currByte) & 0xFF;
            this.buf.put(currByte, (byte) ((b & mask) | cmask));
        } else {
            this.buf.put(currByte, (byte) correctedValue);
        }
    }

    private void putIntLE(long index, int length, int value) {
        assert ((length > 0) && (length <= BIT_INT));
        long end = index + length;
        int startByte = (int) (index / BIT_CHAR);
        int endByte = (int) ((end + (BIT_CHAR - 1)) / BIT_CHAR);
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
            lshift = (int) (index % BIT_CHAR);
            mask = ~((~0) << lshift);
            if ((end % BIT_CHAR) > 0) {
                mask |= (~(0)) << (end % BIT_CHAR);
            }
            cmask = correctedValue << lshift;
            /*
             * low bits are cleared because of left-shift and high bits are
             * already cleared
             */
            cmask &= ~mask;
            int b = this.buf.get(startByte) & 0xFF;
            this.buf.put(startByte, (byte) ((b & mask) | cmask));
            return;
        }

        /* head byte */
        currByte = startByte;
        cshift = (int) (index % BIT_CHAR);
        if (cshift > 0) {
            mask = ~((~0) << cshift);
            cmask = correctedValue << cshift;
            cmask &= ~mask;
            int b = this.buf.get(currByte) & 0xFF;
            this.buf.put(currByte, (byte) ((b & mask) | cmask));
            correctedValue >>>= BIT_CHAR - cshift;
            currByte++;
        }

        /* middle byte(s) */
        for (; currByte < (endByte - 1); currByte++) {
            this.buf.put(currByte, (byte) correctedValue);
            correctedValue >>>= BIT_CHAR;
        }
        /* end byte */
        if ((end % BIT_CHAR) > 0) {
            mask = (~0) << (end % BIT_CHAR);
            cmask = correctedValue & ~mask;
            int b = this.buf.get(currByte) & 0xFF;
            this.buf.put(currByte, (byte) ((b & mask) | cmask));
        } else {
            this.buf.put(currByte, (byte) correctedValue);
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
        if (this.buf == null) {
            return false;
        }

        if ((this.pos + length) > (((long) this.buf.capacity()) * BIT_CHAR)) {
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
    public void setByteOrder(ByteOrder order) {
        this.byteOrder = order;
        if (this.buf != null) {
            this.buf.order(order);
        }
    }

    /**
     * Sets the order of the buffer.
     *
     * @return The order of the buffer.
     */
    public ByteOrder getByteOrder() {
        return this.byteOrder;
    }

    /**
     * Sets the position in the buffer.
     *
     * @param newPosition
     *            The new position of the buffer.
     */
    public void position(long newPosition) {
        this.pos = newPosition;
    }

    /**
     *
     * Sets the position in the buffer.
     *
     * @return order The position of the buffer.
     */
    public long position() {
        return this.pos;
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
            this.buf.order(this.byteOrder);
        }
        clear();
    }

    /**
     * Gets the byte buffer
     *
     * @return The byte buffer
     */
    public ByteBuffer getByteBuffer() {
        return this.buf;
    }

    /**
     * Resets the bitbuffer.
     */
    public void clear() {
        position(0);

        if (this.buf == null) {
            return;
        }
        this.buf.clear();
    }

}
