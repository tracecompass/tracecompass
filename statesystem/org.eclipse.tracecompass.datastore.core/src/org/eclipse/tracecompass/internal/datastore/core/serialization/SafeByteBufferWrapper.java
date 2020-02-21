/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.datastore.core.serialization;

import java.nio.ByteBuffer;

import org.eclipse.tracecompass.datastore.core.serialization.ISafeByteBufferReader;
import org.eclipse.tracecompass.datastore.core.serialization.ISafeByteBufferWriter;

/**
 * This class is a wrapper around a ByteBuffer. The size to read/write to the
 * buffer must be known from the beginning. It will not overflow onto the main
 * buffer.
 *
 * This class may be used to wrap a small part of a bigger ByteBuffer in such a
 * way that it limits the number of bytes to read/write. It will not overflow
 * over or below the allowed positions in the big ByteBuffer while not requiring
 * extra copies of byte arrays.
 *
 * This allows sequential read and write operations but does not allow resizes or
 * seeks.
 *
 * @author Geneviève Bastien
 */
public class SafeByteBufferWrapper implements ISafeByteBufferReader, ISafeByteBufferWriter {

    private final ByteBuffer fBuffer;

    /**
     * Constructor.
     *
     * @param buffer
     *            The big ByteBuffer to safely wrap
     */
    public SafeByteBufferWrapper(ByteBuffer buffer) {
        fBuffer = buffer;
    }

    @Override
    public byte get() {
        return fBuffer.get();
    }

    @Override
    public void get(byte[] dst) {
        fBuffer.get(dst);
    }

    @Override
    public char getChar() {
        return fBuffer.getChar();
    }

    @Override
    public double getDouble() {
        return fBuffer.getDouble();
    }

    @Override
    public float getFloat() {
        return fBuffer.getFloat();
    }

    @Override
    public int getInt() {
        return fBuffer.getInt();
    }

    @Override
    public long getLong() {
        return fBuffer.getLong();
    }

    @Override
    public short getShort() {
        return fBuffer.getShort();
    }

    @Override
    public String getString() {
        int strSize = fBuffer.getShort();
        byte[] array = new byte[strSize];
        fBuffer.get(array);
        return new String(array);
    }

    @Override
    public void put(byte value) {
        fBuffer.put(value);
    }

    @Override
    public void put(byte[] src) {
        fBuffer.put(src);
    }

    @Override
    public void putChar(char value) {
        fBuffer.putChar(value);
    }

    @Override
    public void putDouble(double value) {
        fBuffer.putDouble(value);
    }

    @Override
    public void putFloat(float value) {
        fBuffer.putFloat(value);
    }

    @Override
    public void putInt(int value) {
        fBuffer.putInt(value);
    }

    @Override
    public void putLong(long value) {
        fBuffer.putLong(value);
    }

    @Override
    public void putShort(short value) {
        fBuffer.putShort(value);
    }

    @Override
    public void putString(String value) {
        String toWrite = value;
        if (value.length() > Short.MAX_VALUE) {
            toWrite = toWrite.substring(0, Short.MAX_VALUE);
        }
        fBuffer.putShort((short) value.length());
        fBuffer.put(toWrite.getBytes());
    }

    /**
     * Return the serialized size of the string in this byte buffer. The maximum
     * size is {@link Short#MAX_VALUE}. A string with larger size will be
     * truncated.
     *
     * @param string
     *            The string to serialize
     * @return The size of the serialized string
     */
    public static int getStringSizeInBuffer(String string) {
        return Short.BYTES + Math.min(Short.MAX_VALUE, string.length());
    }

}
