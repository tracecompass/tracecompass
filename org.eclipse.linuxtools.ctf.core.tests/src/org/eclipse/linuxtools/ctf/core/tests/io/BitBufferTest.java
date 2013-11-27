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
     * @throws CTFReaderException An error that cannot happen (position is under 128)
     */
    @Before
    public void setUp() throws CTFReaderException {
        fixture = new BitBuffer(java.nio.ByteBuffer.allocateDirect(128));
        fixture.setByteOrder(ByteOrder.BIG_ENDIAN);
        fixture.setByteBuffer(ByteBuffer.allocate(0));
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
        assertEquals(null, result.getByteBuffer());
    }

    /**
     * Run the BitBuffer(ByteBuffer) constructor test.
     */
    @Test
    public void testBitBuffer_fromByteBuffer() {
        ByteBuffer buf = ByteBuffer.allocate(0);
        BitBuffer result = new BitBuffer(buf);

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

        assertEquals(false, result);
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
        assertEquals("java.nio.HeapByteBuffer[pos=0 lim=0 cap=0]", result.toString());
        assertEquals(false, result.isDirect());
        assertEquals(true, result.hasArray());
        assertEquals(0, result.arrayOffset());
        assertEquals(0, result.limit());
        assertEquals(0, result.remaining());
        assertEquals(0, result.position());
        assertEquals(0, result.capacity());
        assertEquals(false, result.hasRemaining());
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
     * Run the void setByteBuffer(ByteBuffer) method test.
     */
    @Test
    public void testSetByteBuffer() {
        ByteBuffer buf = ByteBuffer.allocate(0);
        fixture.setByteBuffer(buf);
    }

    /**
     * Run the void setByteBuffer(ByteBuffer) method test.
     */
    @Test
    public void testSetByteBuffer_null() {
        ByteBuffer buf = null;
        fixture.setByteBuffer(buf);
    }

    /**
     * Run the void setByteOrder(ByteOrder) method test.
     */
    @Test
    public void testSetByteOrder() {
        ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
        fixture.setByteOrder(byteOrder);
    }
}