/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.datastore.core.encoding;

import java.nio.ByteBuffer;

import org.eclipse.tracecompass.datastore.core.serialization.ISafeByteBufferReader;
import org.eclipse.tracecompass.datastore.core.serialization.ISafeByteBufferWriter;

/**
 * <em>Variable Length Long helpers.</em>
 * <p>
 * This helper class allows easier reading and writing of longs encoded in a
 * varint manner.
 * <p>
 * This allows a bit of space saving on disk.
 * <p>
 * The long will write the length of the number, then the significant portion of
 * the number.
 *
 * So long 1 would be written as 0x0801 (2 bytes) instead of 0x0000000000000001
 * (8 bytes)
 *
 * @author David Piché
 * @since 1.2
 */
public final class HTVarInt {

        /**
         *  Offset to allow better compression for values -2 and -1
         */
        private static final int OFFSET = 3;
        private static final long BYTE_MASK = 0x00000000000000FFL;
        private static final long SHORT_MASK = 0x000000000000FFFFL;
        private static final long INT_MASK = 0x00000000FFFFFFFFL;

        private HTVarInt() {
            // Do nothing
        }

        /**
         * Calculates size of the encoded value, in Bytes
         *
         * @param val
         *            The value to encode
         * @return The number of Bytes of the encoded value
         */
        public static int getEncodedLengthLong(long val) {
            long encodedVal = val + OFFSET;
            int numberOfLeadingZeros = Long.numberOfLeadingZeros(encodedVal);
            int maxBytes = Long.BYTES;

            return maxBytes - (numberOfLeadingZeros / Byte.SIZE) + Byte.BYTES;
        }

        /**
         * Writes a long to the ISafeByteBufferWriter using the VarInt encoding
         *
         * @param buffer
         *            The ISafeByteBufferWriter to write
         * @param val
         *            the value to write
         */
        public static void writeLong(ISafeByteBufferWriter buffer, long val) {

            int remainingBytes = HTVarInt.getEncodedLengthLong(val) - 1;
            long value = val + OFFSET;
            buffer.put((byte) remainingBytes);

            if (remainingBytes == Long.BYTES) {
                buffer.putLong(value);
                return;
            }
            if (remainingBytes >= Integer.BYTES) {
                buffer.putInt( (int) (value & INT_MASK) );
                value = value >> Integer.SIZE;
                remainingBytes -= Integer.BYTES;
            }
            if (remainingBytes >= Short.BYTES) {
                buffer.putShort( (short) (value & SHORT_MASK) );
                value = value >> Short.SIZE;
                remainingBytes -= Short.BYTES;
            }
            if (remainingBytes >= Byte.BYTES) {
                buffer.put( (byte) (value & BYTE_MASK) );
            }
        }

        /**
         * Writes a long to the ByteBuffer using the VarInt encoding
         *
         * @param buffer
         *            The ByteBuffer to write
         * @param val
         *            the value to write
         */
        public static void writeLong(ByteBuffer buffer, long val) {

            int remainingBytes = HTVarInt.getEncodedLengthLong(val) - 1;
            long value = val + OFFSET;
            buffer.put((byte) remainingBytes);

            if (remainingBytes == Long.BYTES) {
                buffer.putLong(value);
                return;
            }
            if (remainingBytes >= Integer.BYTES) {
                buffer.putInt( (int) (value & INT_MASK) );
                value = value >> Integer.SIZE;
                remainingBytes -= Integer.BYTES;
            }
            if (remainingBytes >= Short.BYTES) {
                buffer.putShort( (short) (value & SHORT_MASK) );
                value = value >> Short.SIZE;
                remainingBytes -= Short.BYTES;
            }
            if (remainingBytes >= Byte.BYTES) {
                buffer.put( (byte) (value & BYTE_MASK) );
            }
        }

        /**
         * Reads a encoded long from the ISafeByteBufferReader
         *
         * @param buffer
         *            the ISafeByteBufferReader to read from
         * @return the long, decoded
         */
        public static long readLong(ISafeByteBufferReader buffer) {

            int remainingBytes = buffer.get();
            int sizeLastInsert = 0;
            long retVal = 0;

            if (remainingBytes == Long.BYTES) {
                return buffer.getLong() - OFFSET;
            }
            if (remainingBytes >= Integer.BYTES) {
                retVal += Integer.toUnsignedLong(buffer.getInt());
                sizeLastInsert += Integer.SIZE;
                remainingBytes -= 4;
            }
            if (remainingBytes >= Short.BYTES) {
                short resultA = buffer.getShort();
                retVal += (Short.toUnsignedLong(resultA) << sizeLastInsert);
                sizeLastInsert += Short.SIZE;
                remainingBytes -= 2;
            }
            if (remainingBytes >= Byte.BYTES) {
                byte resultA = buffer.get();
                retVal = (Byte.toUnsignedLong(resultA) << sizeLastInsert) + retVal;
            }

            return retVal - OFFSET;
        }

        /**
         * Reads a encoded long from the ByteBuffer
         *
         * @param buffer
         *            the ByteBuffer to read from
         * @return the long, decoded
         */
        public static long readLong(ByteBuffer buffer) {

            int remainingBytes = buffer.get();
            int sizeLastInsert = 0;
            long retVal = 0;

            if (remainingBytes == Long.BYTES) {
                return buffer.getLong() - OFFSET;
            }
            if (remainingBytes >= Integer.BYTES) {
                retVal += Integer.toUnsignedLong(buffer.getInt());
                sizeLastInsert += Integer.SIZE;
                remainingBytes -= 4;
            }
            if (remainingBytes >= Short.BYTES) {
                short resultA = buffer.getShort();
                retVal += (Short.toUnsignedLong(resultA) << sizeLastInsert);
                sizeLastInsert += Short.SIZE;
                remainingBytes -= 2;
            }
            if (remainingBytes >= Byte.BYTES) {
                byte resultA = buffer.get();
                retVal = (Byte.toUnsignedLong(resultA) << sizeLastInsert) + retVal;
            }

            return retVal - OFFSET;
        }

}
