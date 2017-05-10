/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.datastore.core.serialization;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.tracecompass.datastore.core.serialization.ISafeByteBufferReader;
import org.eclipse.tracecompass.datastore.core.serialization.ISafeByteBufferWriter;
import org.eclipse.tracecompass.datastore.core.serialization.SafeByteBufferFactory;
import org.junit.Test;

/**
 * Test for the {@link SafeByteBufferWrapper} class
 *
 * @author Geneviève Bastien
 */
public class SafeByteBufferWrapperTest {

    private final ByteBuffer fMainBuffer;

    /**
     * Constructor. Prepares the main buffer and safe buffer
     */
    public SafeByteBufferWrapperTest() {
        fMainBuffer = ByteBuffer.allocate(1024);
    }

    /**
     * Test the {@link SafeByteBufferWrapper#put(byte)}
     * {@link SafeByteBufferWrapper#get()} methods
     */
    @Test
    public void testReadWriteByte() {
        byte val = Byte.MAX_VALUE;

        ISafeByteBufferWriter buffer = SafeByteBufferFactory.wrapWriter(fMainBuffer, 512);
        buffer.put(val);

        // Reset the buffer and read it again
        fMainBuffer.flip();
        ISafeByteBufferReader reader = SafeByteBufferFactory.wrapReader(fMainBuffer, 512);
        assertEquals(val, reader.get());
    }

    /**
     * Test the {@link SafeByteBufferWrapper#put(byte[])}
     * {@link SafeByteBufferWrapper#get(byte[])} methods
     */
    @Test
    public void testReadWriteByteArray() {
        byte[] val = { 0, 2, 1, 3 };

        ISafeByteBufferWriter buffer = SafeByteBufferFactory.wrapWriter(fMainBuffer, 512);
        buffer.put(val);

        // Reset the buffer and read it again
        fMainBuffer.flip();
        ISafeByteBufferReader reader = SafeByteBufferFactory.wrapReader(fMainBuffer, 512);
        byte[] ret = new byte[4];
        reader.get(ret);
        assertArrayEquals(val, ret);
    }

    /**
     * Test the {@link SafeByteBufferWrapper#putChar(char)}
     * {@link SafeByteBufferWrapper#getChar()} methods
     */
    @Test
    public void testReadWriteChar() {
        char val = 24;

        ISafeByteBufferWriter buffer = SafeByteBufferFactory.wrapWriter(fMainBuffer, 512);
        buffer.putChar(val);

        // Reset the buffer and read it again
        fMainBuffer.flip();
        ISafeByteBufferReader reader = SafeByteBufferFactory.wrapReader(fMainBuffer, 512);
        assertEquals(val, reader.getChar());
    }

    /**
     * Test the {@link SafeByteBufferWrapper#putDouble(double)}
     * {@link SafeByteBufferWrapper#getDouble()} methods
     */
    @Test
    public void testReadWriteDouble() {
        double val = Double.MAX_VALUE;

        ISafeByteBufferWriter buffer = SafeByteBufferFactory.wrapWriter(fMainBuffer, 512);
        buffer.putDouble(val);

        // Reset the buffer and read it again
        fMainBuffer.flip();
        ISafeByteBufferReader reader = SafeByteBufferFactory.wrapReader(fMainBuffer, 512);
        assertEquals(val, reader.getDouble(), 10);
    }

    /**
     * Test the {@link SafeByteBufferWrapper#putFloat(float)}
     * {@link SafeByteBufferWrapper#getFloat()} methods
     */
    @Test
    public void testReadWriteFloat() {
        float val = Float.MIN_VALUE;

        ISafeByteBufferWriter buffer = SafeByteBufferFactory.wrapWriter(fMainBuffer, 512);
        buffer.putFloat(val);

        // Reset the buffer and read it again
        fMainBuffer.flip();
        ISafeByteBufferReader reader = SafeByteBufferFactory.wrapReader(fMainBuffer, 512);
        assertEquals(val, reader.getFloat(), 10);
    }

    /**
     * Test the {@link SafeByteBufferWrapper#putInt(int)}
     * {@link SafeByteBufferWrapper#getInt()} methods
     */
    @Test
    public void testReadWriteInt() {
        int val = Integer.MAX_VALUE;

        ISafeByteBufferWriter buffer = SafeByteBufferFactory.wrapWriter(fMainBuffer, 512);
        buffer.putInt(val);

        // Reset the buffer and read it again
        fMainBuffer.flip();
        ISafeByteBufferReader reader = SafeByteBufferFactory.wrapReader(fMainBuffer, 512);
        assertEquals(val, reader.getInt());
    }

    /**
     * Test the {@link SafeByteBufferWrapper#putLong(long)}
     * {@link SafeByteBufferWrapper#getLong()} methods
     */
    @Test
    public void testReadWriteLong() {
        long val = Long.MIN_VALUE;

        ISafeByteBufferWriter buffer = SafeByteBufferFactory.wrapWriter(fMainBuffer, 512);
        buffer.putLong(val);

        // Reset the buffer and read it again
        fMainBuffer.flip();
        ISafeByteBufferReader reader = SafeByteBufferFactory.wrapReader(fMainBuffer, 512);
        assertEquals(val, reader.getLong());
    }

    /**
     * Test the {@link SafeByteBufferWrapper#putShort(short)}
     * {@link SafeByteBufferWrapper#getShort()} methods
     */
    @Test
    public void testReadWriteShort() {
        short val = Short.MIN_VALUE;

        ISafeByteBufferWriter buffer = SafeByteBufferFactory.wrapWriter(fMainBuffer, 512);
        buffer.putShort(val);

        // Reset the buffer and read it again
        fMainBuffer.flip();
        ISafeByteBufferReader reader = SafeByteBufferFactory.wrapReader(fMainBuffer, 512);
        assertEquals(val, reader.getShort());
    }

    /**
     * Test the {@link SafeByteBufferWrapper#putString(String)}
     * {@link SafeByteBufferWrapper#getString()} methods
     */
    @Test
    public void testReadWriteString() {
        String val = "abcdefg";

        ISafeByteBufferWriter buffer = SafeByteBufferFactory.wrapWriter(fMainBuffer, 512);
        buffer.putString(val);

        // Reset the buffer and read it again
        fMainBuffer.flip();
        ISafeByteBufferReader reader = SafeByteBufferFactory.wrapReader(fMainBuffer, 512);
        assertEquals(val, reader.getString());
    }

    /**
     * Test adding multiple values to the buffer, inside the limits
     */
    @Test
    public void testMultipleValues() {
        int valInt = 98;
        short valShort = 34;
        String valStr = "myString";
        long valLong = 254238908543254L;

        ISafeByteBufferWriter buffer = SafeByteBufferFactory.wrapWriter(fMainBuffer, 512);
        buffer.putInt(valInt);
        buffer.putShort(valShort);
        buffer.putString(valStr);
        buffer.putLong(valLong);

        // Reset the buffer and read it again
        fMainBuffer.flip();
        ISafeByteBufferReader reader = SafeByteBufferFactory.wrapReader(fMainBuffer, 512);
        assertEquals(valInt, reader.getInt());
        assertEquals(valShort, reader.getShort());
        assertEquals(valStr, reader.getString());
        assertEquals(valLong, reader.getLong());
    }

    /**
     * Test writing over the limit of the buffer
     */
    @Test(expected = BufferOverflowException.class)
    public void testLimit() {
        ISafeByteBufferWriter buffer = SafeByteBufferFactory.wrapWriter(fMainBuffer, 5);
        buffer.putDouble(Double.MIN_VALUE);
    }

    /**
     * Test writing to main buffer after writing to safe buffer
     */
    @Test
    public void testMainBuffer() {
        String valString = "defghi";
        long valLong = 54262542352L;
        int valInt = 2048;
        int bufferSize = Integer.BYTES + valString.length() + Long.BYTES;

        ISafeByteBufferWriter buffer = SafeByteBufferFactory.wrapWriter(fMainBuffer, bufferSize);
        buffer.putString(valString);
        buffer.putLong(valLong);
        fMainBuffer.putInt(valInt);

        // Flip the main buffer to read again
        fMainBuffer.flip();
        ISafeByteBufferReader reader = SafeByteBufferFactory.wrapReader(fMainBuffer, bufferSize);
        assertEquals(valString, reader.getString());
        assertEquals(valLong, reader.getLong());
        assertEquals(valInt, fMainBuffer.getInt());
    }

    /**
     * Test writing to main buffer after writing to safe buffer but not
     * completely
     */
    @Test
    public void testMainBuffer2() {
        String valString = "defghi";
        long valLong = 54262542352L;
        int valInt = 2048;
        int bufferSize = Integer.BYTES + valString.length() + Long.BYTES + Long.BYTES;

        ISafeByteBufferWriter buffer = SafeByteBufferFactory.wrapWriter(fMainBuffer, bufferSize);
        buffer.putString(valString);
        buffer.putLong(valLong);

        // Assert the main buffer's position is after the safe buffer, even
        // though it is not completely written
        assertEquals(bufferSize, fMainBuffer.position());
        fMainBuffer.putInt(valInt);

        // Write the extra long at the end of the safe buffer
        buffer.putLong(valLong);

        // Start reading again
        fMainBuffer.flip();
        ISafeByteBufferReader reader = SafeByteBufferFactory.wrapReader(fMainBuffer, bufferSize);
        assertEquals(valString, reader.getString());
        assertEquals(valLong, reader.getLong());
        assertEquals(valLong, reader.getLong());
        assertEquals(valInt, fMainBuffer.getInt());
    }

    /**
     * Test writing to main buffer before writing to safe buffer
     */
    @Test
    public void testMainBuffer3() {
        String valString = "defghi";
        long valLong = 54262542352L;
        int valInt = 2048;
        int bufferSize = Integer.BYTES + valString.length() + Long.BYTES;

        fMainBuffer.putLong(valLong);
        fMainBuffer.putInt(valInt);

        ISafeByteBufferWriter buffer = SafeByteBufferFactory.wrapWriter(fMainBuffer, bufferSize);
        buffer.putString(valString);
        buffer.putLong(valLong);

        fMainBuffer.flip();
        assertEquals(valLong, fMainBuffer.getLong());
        assertEquals(valInt, fMainBuffer.getInt());

        // Flip the main buffer
        ISafeByteBufferReader reader = SafeByteBufferFactory.wrapReader(fMainBuffer, bufferSize);
        assertEquals(valString, reader.getString());
        assertEquals(valLong, reader.getLong());
    }

    /**
     * Test writing in buffers with different endianness
     */
    @Test
    public void testEndianness() {
        long valLong = 54262542352L;
        int valInt = 2048;
        int bufferSize = Integer.BYTES + Long.BYTES;

        // Change the order of the buffer
        ByteOrder order = fMainBuffer.order();
        ByteOrder newOrder = (order == ByteOrder.BIG_ENDIAN ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
        fMainBuffer.order(newOrder);

        ByteBuffer otherBuffer = ByteBuffer.allocate(fMainBuffer.capacity());
        otherBuffer.order(newOrder);

        // Wrap one of the buffer in a safe buffer
        ISafeByteBufferWriter buffer = SafeByteBufferFactory.wrapWriter(fMainBuffer, bufferSize);

        // Write the same data in both buffer and make sure they are equal
        buffer.putLong(valLong);
        otherBuffer.putLong(valLong);
        buffer.putInt(valInt);
        otherBuffer.putInt(valInt);

        fMainBuffer.flip();
        otherBuffer.flip();

        byte[] expected = new byte[bufferSize];
        byte[] actual = new byte[bufferSize];

        fMainBuffer.get(actual, 0, bufferSize);
        otherBuffer.get(expected, 0, bufferSize);

        assertArrayEquals(expected, actual);

        fMainBuffer.flip();
        otherBuffer.flip();

        // Read the safe byte buffer and make sure the results are the same
        ISafeByteBufferReader reader = SafeByteBufferFactory.wrapReader(fMainBuffer, bufferSize);
        assertEquals(otherBuffer.getLong(), reader.getLong());
        assertEquals(otherBuffer.getInt(), reader.getInt());
    }

}
