/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexandre Montplaisir - Extracted from BitBufferTest, cleanup
 *     Matthew Khouzam - Additional tests
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.tests.io;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.junit.Before;
import org.junit.Test;

/**
 * Part of the {@link BitBuffer} tests which test the methods to read/write
 * integers. These are separated from the main file because the fixture is
 * different.
 *
 * @author Alexandre Montplaisir
 */
public class BitBufferIntTest {

    private BitBuffer fixture;

    /**
     * Perform pre-test initialization.
     *
     * @throws CTFException
     *             Out of bounds, won't happen
     */
    @Before
    public void setUp() throws CTFException {
        ByteBuffer allocateDirect = ByteBuffer.allocateDirect(128);
        fixture = new BitBuffer(allocateDirect);
        fixture.setByteOrder(ByteOrder.BIG_ENDIAN);
        fixture = createBuffer();
    }

    private static BitBuffer createBuffer() throws CTFException {
        return createBuffer(16);
    }

    private static BitBuffer createBuffer(int j) throws CTFException {
        final byte[] bytes = new byte[j];
        for (int i = 0; i < j; i++) {
            bytes[i] = (byte) (i % 0xff);
        }
        ByteBuffer wrap = ByteBuffer.wrap(bytes);
        BitBuffer fixture = new BitBuffer(wrap);
        fixture.position(1);
        return fixture;
    }

    /**
     * Test {@link BitBuffer#getInt} with a basic value
     *
     * @throws CTFException
     *             Not expected
     */
    @Test
    public void testGetInt_base() throws CTFException {
        int result = fixture.getInt();
        assertEquals(0x020406, result);
    }

    /**
     * Test {@link BitBuffer#getInt} with explicit seek at pos 0.
     *
     * @throws CTFException
     *             Not expected
     */
    @Test
    public void testGetInt_pos0() throws CTFException {
        fixture.position(0);
        int result = fixture.getInt();
        assertEquals(0x010203, result);
    }

    /**
     * Test {@link BitBuffer#get} with seek at pos 1.
     *
     * @throws CTFException
     *             Not expected
     */
    @Test
    public void testGetInt_pos1() throws CTFException {
        fixture.position(1);

        long result = fixture.get(1, true);
        assertEquals(0, result);
    }

    /**
     * Test {@link BitBuffer#get} with seek at pos 2.
     *
     * @throws CTFException
     *             Not expected
     */
    @Test
    public void testGetInt_pos2() throws CTFException {
        fixture.position(2);

        long result = fixture.get(0, true);
        assertEquals(0, result);
    }

    /**
     * Test {@link BitBuffer#get} with explicit little-endian reading.
     *
     * @throws CTFException
     *             Not expected
     */
    @Test
    public void testGetInt_le2() throws CTFException {
        BitBuffer leFixture = createBuffer(128);
        leFixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        leFixture.position(0);
        long result = leFixture.get(24, false);
        assertEquals(0x020100, result);
    }

    /**
     * Test {@link BitBuffer#get} with explicit little-endian reading, with an
     * offset.
     *
     * @throws CTFException
     *             Not expected
     */
    @Test
    public void testGetInt_le1() throws CTFException {
        BitBuffer leFixture = createBuffer(128);
        leFixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        leFixture.position(1);
        long result = leFixture.get(24, false);
        assertEquals(0x810080, result); /* 0x020100 down-shifted */
    }

    /**
     * Test {@link BitBuffer#get} with a 32-bit out-of-bounds read. Should throw
     * an exception.
     *
     * @throws CTFException
     *             Expected
     */
    @Test(expected = CTFException.class)
    public void testGetInt_invalid() throws CTFException {
        BitBuffer smallFixture = createBuffer(2);
        smallFixture.setByteOrder(ByteOrder.BIG_ENDIAN);

        smallFixture.position(10);

        /* This will attempt to read past the buffer's end. */
        smallFixture.get(32, true);
    }

    /**
     * Test {@link BitBuffer#get} with a 64-bit out-of-bounds read. Should throw
     * an exception.
     *
     * @throws CTFException
     *             Expected
     */
    @Test(expected = CTFException.class)
    public void testGetInt_invalid2() throws CTFException {
        BitBuffer smallFixture = createBuffer(2);
        smallFixture.setByteOrder(ByteOrder.BIG_ENDIAN);

        smallFixture.position(1);

        /* This will attempt to read past the buffer's end. */
        smallFixture.get(64, true);
    }

    /**
     * Test {@link BitBuffer#getLong}.
     *
     * @throws CTFException
     *             error
     */
    @Test
    public void testGetLong_pos0() throws CTFException {
        fixture.position(0);
        long result = fixture.getLong();
        assertEquals(0x01020304050607L, result);
    }

    /**
     * Test {@link BitBuffer#getLong} with an offset of 7.
     *
     * @throws CTFException
     *             error
     */
    @Test
    public void testGetLong_pos7() throws CTFException {
        fixture.position(7);
        long result = fixture.getLong();
        assertEquals(0x81018202830384L, result);
    }

    /**
     * Test {@link BitBuffer#getLong} with an offset of 8.
     *
     * @throws CTFException
     *             error
     */
    @Test
    public void testGetLong_pos8() throws CTFException {
        fixture.position(8);
        long result = fixture.getLong();
        assertEquals(0x0102030405060708L, result);
    }

    /**
     * Test {@link BitBuffer#getLong} with a little-endian buffer.
     *
     * @throws CTFException
     *             error
     */
    @Test
    public void testGetLong_pos0LE() throws CTFException {
        fixture.position(0);
        fixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        long result = fixture.getLong();
        assertEquals(0x0706050403020100L, result);
    }

    /**
     * Test {@link BitBuffer#getLong} with a little-endian buffer at pos 7.
     *
     * @throws CTFException
     *             error
     */
    @Test
    public void testGetLong_pos7LE() throws CTFException {
        fixture.position(7);
        fixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        long result = fixture.getLong();
        assertEquals(0x100e0c0a08060402L, result);
    }

    /**
     * Test {@link BitBuffer#getLong} with a little-endian buffer at pos 8.
     *
     * @throws CTFException
     *             error
     */
    @Test
    public void testGetLong_pos8LE() throws CTFException {
        fixture.position(8);
        fixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        long result = fixture.getLong();
        assertEquals(0x0807060504030201L, result);
    }

    /**
     * Test {@link BitBuffer#get} for >32 bits in length.
     *
     * @throws CTFException
     *             error
     */
    @Test
    public void testGet35_pos0BE() throws CTFException {
        fixture.position(0);
        long result = fixture.get(35, false);
        assertEquals(0x081018L, result);
    }

    /**
     * Test {@link BitBuffer#get} for >32 bits in length at an offset position.
     *
     * @throws CTFException
     *             error
     */
    @Test
    public void testGet35_pos8BE() throws CTFException {
        fixture.position(8);
        long result = fixture.get(35, false);
        assertEquals(0x08101820L, result);
    }

    /**
     * Test {@link BitBuffer#get} for >32 bits in length in little-endian.
     *
     * @throws CTFException
     *             error
     */
    @Test
    public void testGet35_pos0LE() throws CTFException {
        fixture.position(0);
        fixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        long result = fixture.get(35, false);
        assertEquals(0x0403020100L, result);
    }

    /**
     * Test {@link BitBuffer#get} for >32 bits in length, in little-endian, at
     * position 7.
     *
     * @throws CTFException
     *             error
     */
    @Test
    public void testGetLong35_pos7LE() throws CTFException {
        fixture.position(7);
        fixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        long result = fixture.get(35, false);
        assertEquals(0x0208060402L, result);
    }

    /**
     * Test {@link BitBuffer#get} for >32 bits in length, in little-endian, at
     * position 8.
     *
     * @throws CTFException
     *             error
     */
    @Test
    public void testGetLong35_pos8LE() throws CTFException {
        fixture.position(8);
        fixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        long result = fixture.get(35, false);
        assertEquals(0x0504030201L, result);
    }

    /**
     * Test {@link BitBuffer#get} for >32 bits in length, in little-endian, for
     * a signed value.
     *
     * @throws CTFException
     *             error
     */
    @Test
    public void testGetLong35s_pos0LE() throws CTFException {
        fixture.position(0);
        fixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        long result = fixture.get(35, true);
        assertEquals(0xfffffffc03020100L, result);
    }

    /**
     * Test {@link BitBuffer#get} for >32 bits in length, in little-endian, for
     * a signed value, at position 7.
     *
     * @throws CTFException
     *             error
     */
    @Test
    public void testGetLong35s_pos7LE() throws CTFException {
        fixture.position(7);
        fixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        long result = fixture.get(35, true);
        assertEquals(0x0208060402L, result);
    }

    /**
     * Test {@link BitBuffer#get} for >32 bits in length, in little-endian, for
     * a signed value, at position 8.
     *
     * @throws CTFException
     *             error
     */
    @Test
    public void testGetLong35s_pos8LE() throws CTFException {
        fixture.position(8);
        fixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        long result = fixture.get(35, true);
        assertEquals(0xfffffffd04030201L, result);
    }

    /**
     * Test reading negative values as signed values.
     *
     * @throws CTFException
     *             error
     */
    @Test
    public void testGetSigned() throws CTFException {
        fixture.position(0);
        fixture.putInt(-1);
        fixture.putInt(-1);
        fixture.position(0);
        fixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);

        long result = fixture.get(32, true);
        assertEquals(-1L, result);
    }

    /**
     * Test reading negative values as unsigned values.
     *
     * @throws CTFException
     *             error
     */
    @Test
    public void testGetUnsigned() throws CTFException {
        fixture.position(0);
        fixture.putInt(-1);
        fixture.putInt(-1);
        fixture.position(0);
        fixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);

        long result = fixture.get(32, false);
        assertEquals(0xFFFFFFFFL, result);
    }

    /**
     * Test reading 24 bits of a 32-bit negative value as a signed value.
     *
     * @throws CTFException
     *             error
     */
    @Test
    public void testGet24Signed() throws CTFException {
        fixture.position(0);
        fixture.putInt(-1);
        fixture.putInt(-1);
        fixture.position(0);
        fixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);

        long result = fixture.get(24, true);
        assertEquals(-1L, result);
    }

    /**
     * Test reading 24 bits of a 32-bit negative value as an unsigned value.
     *
     * @throws CTFException
     *             error
     */
    @Test
    public void testGet24Unsigned() throws CTFException {
        fixture.position(0);
        fixture.putInt(-1);
        fixture.putInt(-1);
        fixture.position(0);
        fixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);

        long result = fixture.get(24, false);
        assertEquals(0xFFFFFFL, result);
    }

    /**
     * Test {@link BitBuffer#putInt(int)}
     *
     * @throws CTFException
     *             Not expected
     */
    @Test
    public void testPutInt() throws CTFException {
        fixture.position(1);
        fixture.putInt(1);
    }

    /**
     * Test {@link BitBuffer#putInt(int, int)}
     *
     * @throws CTFException
     *             Not expected
     */
    @Test
    public void testPutInt_length1() throws CTFException {
        fixture.position(1);
        fixture.putInt(1, 1);
    }

    /**
     * Test {@link BitBuffer#putInt(int, int)} with length = 0.
     *
     * @throws CTFException
     *             Not expected
     */
    @Test
    public void testPutInt_length0() throws CTFException {
        fixture.position(1);
        fixture.putInt(0, 1);
    }

    /**
     * Test {@link BitBuffer#putInt(int)} Little endian
     *
     * @throws CTFException
     *             Not expected
     */
    @Test
    public void testPutIntLe() throws CTFException {
        fixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        fixture.position(1);
        fixture.putInt(1);
    }

    /**
     * Test {@link BitBuffer#putInt(int, int)} Little endian
     *
     * @throws CTFException
     *             Not expected
     */
    @Test
    public void testPutIntLe_length1() throws CTFException {
        fixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        fixture.position(1);
        fixture.putInt(1, 1);
    }

    /**
     * Test {@link BitBuffer#putInt(int, int)} with length = 0. Little endian
     *
     * @throws CTFException
     *             Not expected
     */
    @Test
    public void testPutIntLe_length0() throws CTFException {
        fixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        fixture.position(1);
        fixture.putInt(0, 1);
    }

    /**
     * Test writing and reading a value defined in hex format.
     *
     * @throws CTFException
     *             Not expected
     */
    @Test
    public void testPutInt_hex() throws CTFException {
        final int value = 0x010203;

        for (int i = 0; i <= 32; i++) {
            fixture.position(i);
            fixture.putInt(value);

            fixture.position(i);
            int read = fixture.getInt();

            assertEquals(value, read);
        }
    }

    /**
     * Test {@link BitBuffer#putInt} with an out-of-bounds length. An exception
     * should be thrown.
     *
     * @throws CTFException
     *             Expected
     */
    @Test(expected = CTFException.class)
    public void testPutInt_invalid() throws CTFException {
        BitBuffer fixture2;
        fixture2 = createBuffer(4);
        fixture2.setByteOrder(ByteOrder.BIG_ENDIAN);
        fixture2.position(1);

        /* This will try writing past the buffer's end */
        fixture2.putInt(32, 1);
    }
}
