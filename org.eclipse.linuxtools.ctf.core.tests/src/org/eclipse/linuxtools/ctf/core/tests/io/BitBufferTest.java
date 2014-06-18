/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.tests.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>BitBufferTest</code> contains tests for the class
 * <code>{@link BitBuffer}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class BitBufferTest {

    private BitBuffer fixture;

    /**
     * Perform pre-test initialization.
     *
     * @throws CTFReaderException
     *             An error that cannot happen (position is under 128)
     */
    @Before
    public void setUp() throws CTFReaderException {
        fixture = new BitBuffer(Util.testMemory(ByteBuffer.allocateDirect(1)));
        fixture.setByteOrder(ByteOrder.BIG_ENDIAN);
        fixture.position(1);
    }

    /**
     * Run the BitBuffer() constructor test.
     */
    @Test
    public void testBitBuffer() {
        BitBuffer result = new BitBuffer();

        assertNotNull(result);
        assertEquals(0, result.position());
        assertNotNull(result.getByteBuffer());
    }

    /**
     * Run the BitBuffer(ByteBuffer) constructor test.
     */
    @Test
    public void testBitBuffer_fromByteBuffer() {
        BitBuffer result = new BitBuffer(Util.testMemory(ByteBuffer.allocate(0)));
        assertNotNull(result);
        assertEquals(0, result.position());
    }

    /**
     * Run the boolean canRead(int) method test.
     */
    @Test
    public void testCanRead_1param() {
        int length = 1;
        boolean result = fixture.canRead(length);

        assertEquals(true, result);
    }

    /**
     * Run the void clear() method test.
     */
    @Test
    public void testClear() {
        fixture.clear();
    }

    /**
     * Run the ByteBuffer getByteBuffer() method test.
     */
    @Test
    public void testGetByteBuffer() {
        ByteBuffer result = fixture.getByteBuffer();

        assertNotNull(result);
        assertEquals("java.nio.DirectByteBuffer[pos=0 lim=1 cap=1]", result.toString());
        assertEquals(true, result.isDirect());
        assertEquals(false, result.hasArray());
        assertEquals(1, result.limit());
        assertEquals(1, result.remaining());
        assertEquals(0, result.position());
        assertEquals(1, result.capacity());
        assertEquals(true, result.hasRemaining());
        assertEquals(false, result.isReadOnly());
    }

    /**
     * Run the ByteOrder getByteOrder() method test.
     */
    @Test
    public void testGetByteOrder() {
        ByteOrder result = fixture.getByteOrder();

        assertNotNull(result);
        assertEquals("BIG_ENDIAN", result.toString());
    }

    /**
     * Run the ByteOrder order() method test.
     */
    @Test
    public void testGetOrder() {
        ByteOrder result = fixture.getByteOrder();

        assertNotNull(result);
        assertEquals("BIG_ENDIAN", result.toString());
    }

    /**
     * Run the void order(ByteOrder) method test.
     */
    @Test
    public void testSetOrder() {
        ByteOrder order = ByteOrder.BIG_ENDIAN;

        fixture.setByteOrder(order);
    }

    /**
     * Run the int position() method test.
     */
    @Test
    public void testGetPosition() {
        long result = fixture.position();

        assertEquals(1, result);
    }

    /**
     * Run the void position(int) method test.
     *
     * @throws CTFReaderException
     *             out of bounds? won't happen
     */
    @Test
    public void testSetPosition() throws CTFReaderException {
        int newPosition = 1;
        fixture.position(newPosition);
    }

    /**
     * Run the void setByteOrder(ByteOrder) method test.
     */
    @Test
    public void testSetByteOrder() {
        ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
        fixture.setByteOrder(byteOrder);
    }

    /**
     * Test the get function
     */
    @Test
    public void testGetBytes() {
        @NonNull
        byte[] data = new byte[2];
        ByteBuffer bb = ByteBuffer.allocate(10);
        bb.put((byte) 0);
        bb.put((byte) 1);
        bb.put((byte) 1);
        bb.put((byte) 0);
        fixture = new BitBuffer(bb);
        fixture.get(data);
        assertEquals(0, data[0]);
        assertEquals(1, data[1]);
        fixture.get(data);
        assertEquals(1, data[0]);
        assertEquals(0, data[1]);
    }

    /**
     * Test the get function
     *
     * @throws CTFReaderException
     *             won't happen but we seek in a buffer
     */
    @Test
    public void testGetBytesMiddle() throws CTFReaderException {
        @NonNull
        byte[] data = new byte[5];
        // this string has been carefully selected and tested... don't change
        // the string and expect the result to be the same.
        fixture = new BitBuffer(Util.testMemory(ByteBuffer.wrap(new String("hello world").getBytes())));
        fixture.position(6 * 8);
        fixture.get(data);
        String actual = new String(data);
        assertEquals("world", actual);
    }
}