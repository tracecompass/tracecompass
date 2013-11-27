/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexandre Montplaisir - Extracted from BitBufferTest, cleanup
 *     Matthew Khouzam - Additional tests
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.tests.io;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
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
     * @throws CTFReaderException
     *             Out of bounds, won't happen
     */
    @Before
    public void setUp() throws CTFReaderException {
        fixture = new BitBuffer(ByteBuffer.allocateDirect(128));
        fixture.setByteOrder(ByteOrder.BIG_ENDIAN);
        createBuffer(fixture);
    }

    private static void createBuffer(BitBuffer fixture) throws CTFReaderException {
        createBuffer(fixture, 16);
    }

    private static void createBuffer(BitBuffer fixture, int j) throws CTFReaderException {
        final byte[] bytes = new byte[j];
        for (int i = 0; i < j; i++) {
            bytes[i] = (byte) (i % 0xff);
        }
        fixture.setByteBuffer(ByteBuffer.wrap(bytes));
        fixture.position(1);
    }

    /**
     * Test {@link BitBuffer#getInt} with a basic value
     *
     * @throws CTFReaderException
     *             Not expected
     */
    @Test
    public void testGetInt_base() throws CTFReaderException {
        int result = fixture.getInt();
        assertEquals(0x020406, result);
    }

    /**
     * Test {@link BitBuffer#getInt} with explicit seek at pos 0.
     *
     * @throws CTFReaderException
     *             Not expected
     */
    @Test
    public void testGetInt_pos0() throws CTFReaderException {
        fixture.position(0);
        int result = fixture.getInt();
        assertEquals(0x010203, result);
    }

    /**
     * Test {@link BitBuffer#get} with seek at pos 1.
     *
     * @throws CTFReaderException
     *             Not expected
     */
    @Test
    public void testGetInt_pos1() throws CTFReaderException {
        fixture.position(1);

        long result = fixture.get(1, true);
        assertEquals(0, result);
    }

    /**
     * Test {@link BitBuffer#get} with seek at pos 2.
     *
     * @throws CTFReaderException
     *             Not expected
     */
    @Test
    public void testGetInt_pos2() throws CTFReaderException {
        fixture.position(2);

        long result = fixture.get(0, true);
        assertEquals(0, result);
    }


    /**
     * Test {@link BitBuffer#get} with explicit little-endian reading.
     *
     * @throws CTFReaderException
     *             Not expected
     */
    @Test
    public void testGetInt_le2() throws CTFReaderException {
        BitBuffer leFixture = new BitBuffer(ByteBuffer.allocateDirect(128));
        leFixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        createBuffer(leFixture);
        leFixture.position(0);

        long result = leFixture.get(24, false);
        assertEquals(0x020100, result);
    }

    /**
     * Test {@link BitBuffer#get} with explicit little-endian reading, with an
     * offset.
     *
     * @throws CTFReaderException
     *             Not expected
     */
    @Test
    public void testGetInt_le1() throws CTFReaderException {
        BitBuffer leFixture = new BitBuffer(ByteBuffer.allocateDirect(128));
        leFixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        createBuffer(leFixture);
        leFixture.position(1);

        long result = leFixture.get(24, false);
        assertEquals(0x810080, result);  /* 0x020100 down-shifted */
    }


    /**
     * Test {@link BitBuffer#get} with a 32-bit out-of-bounds read. Should throw
     * an exception.
     *
     * @throws CTFReaderException
     *             Expected
     */
    @Test(expected = CTFReaderException.class)
    public void testGetInt_invalid() throws CTFReaderException {
        BitBuffer smallFixture = new BitBuffer(ByteBuffer.allocateDirect(128));
        smallFixture.setByteOrder(ByteOrder.BIG_ENDIAN);
        createBuffer(smallFixture, 2);
        smallFixture.position(10);

        /* This will attempt to read past the buffer's end. */
        smallFixture.get(32, true);
    }

    /**
     * Test {@link BitBuffer#get} with a 64-bit out-of-bounds read. Should throw
     * an exception.
     *
     * @throws CTFReaderException
     *             Expected
     */
    @Test(expected = CTFReaderException.class)
    public void testGetInt_invalid2() throws CTFReaderException {
        BitBuffer smallFixture = new BitBuffer(ByteBuffer.allocateDirect(128));
        smallFixture.setByteOrder(ByteOrder.BIG_ENDIAN);
        createBuffer(smallFixture, 2);
        smallFixture.position(1);

        /* This will attempt to read past the buffer's end. */
        smallFixture.get(64, true);
    }

    /**
     * Test {@link BitBuffer#getLong}.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGetLong_pos0() throws CTFReaderException {
        fixture.position(0);
        long result = fixture.getLong();
        assertEquals(0x01020304050607L, result);
    }

    /**
     * Test {@link BitBuffer#getLong} with an offset of 7.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGetLong_pos7() throws CTFReaderException {
        fixture.position(7);
        long result = fixture.getLong();
        assertEquals(0x81018202830384L, result);
    }

    /**
     * Test {@link BitBuffer#getLong} with an offset of 8.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGetLong_pos8() throws CTFReaderException {
        fixture.position(8);
        long result = fixture.getLong();
        assertEquals(0x0102030405060708L, result);
    }

    /**
     * Test {@link BitBuffer#getLong} with a little-endian buffer.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGetLong_pos0LE() throws CTFReaderException {
        fixture.position(0);
        fixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        long result = fixture.getLong();
        assertEquals(0x0706050403020100L, result);
    }

    /**
     * Test {@link BitBuffer#getLong} with a little-endian buffer at pos 7.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGetLong_pos7LE() throws CTFReaderException {
        fixture.position(7);
        fixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        long result = fixture.getLong();
        assertEquals(0x100e0c0a08060402L, result);
    }

    /**
     * Test {@link BitBuffer#getLong} with a little-endian buffer at pos 8.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGetLong_pos8LE() throws CTFReaderException {
        fixture.position(8);
        fixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        long result = fixture.getLong();
        assertEquals(0x0807060504030201L, result);
    }

    /**
     * Test {@link BitBuffer#get} for >32 bits in length.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGet35_pos0BE() throws CTFReaderException {
        fixture.position(0);
        long result = fixture.get(35, false);
        assertEquals(0x081018L, result);
    }

    /**
     * Test {@link BitBuffer#get} for >32 bits in length at an offset position.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGet35_pos8BE() throws CTFReaderException {
        fixture.position(8);
        long result = fixture.get(35, false);
        assertEquals(0x08101820L, result);
    }

    /**
     * Test {@link BitBuffer#get} for >32 bits in length in little-endian.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGet35_pos0LE() throws CTFReaderException {
        fixture.position(0);
        fixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        long result = fixture.get(35, false);
        assertEquals(0x0403020100L, result);
    }

    /**
     * Test {@link BitBuffer#get} for >32 bits in length, in little-endian, at
     * position 7.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGetLong35_pos7LE() throws CTFReaderException {
        fixture.position(7);
        fixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        long result = fixture.get(35, false);
        assertEquals(0x0208060402L, result);
    }

    /**
     * Test {@link BitBuffer#get} for >32 bits in length, in little-endian, at
     * position 8.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGetLong35_pos8LE() throws CTFReaderException {
        fixture.position(8);
        fixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        long result = fixture.get(35, false);
        assertEquals(0x0504030201L, result);
    }

    /**
     * Test {@link BitBuffer#get} for >32 bits in length, in little-endian, for
     * a signed value.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGetLong35s_pos0LE() throws CTFReaderException {
        fixture.position(0);
        fixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        long result = fixture.get(35, true);
        assertEquals(0xfffffffc03020100L, result);
    }

    /**
     * Test {@link BitBuffer#get} for >32 bits in length, in little-endian, for
     * a signed value, at position 7.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGetLong35s_pos7LE() throws CTFReaderException {
        fixture.position(7);
        fixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        long result = fixture.get(35, true);
        assertEquals(0x0208060402L, result);
    }

    /**
     * Test {@link BitBuffer#get} for >32 bits in length, in little-endian, for
     * a signed value, at position 8.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGetLong35s_pos8LE() throws CTFReaderException {
        fixture.position(8);
        fixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        long result = fixture.get(35, true);
        assertEquals(0xfffffffd04030201L, result);
    }

    /**
     * Test reading negative values as signed values.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGetSigned() throws CTFReaderException {
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
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGetUnsigned() throws CTFReaderException {
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
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGet24Signed() throws CTFReaderException {
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
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGet24Unsigned() throws CTFReaderException {
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
     * @throws CTFReaderException
     *             Not expected
     */
    @Test
    public void testPutInt() throws CTFReaderException {
        fixture.position(1);
        fixture.putInt(1);
    }

    /**
     * Test {@link BitBuffer#putInt(int, int)}
     *
     * @throws CTFReaderException
     *             Not expected
     */
    @Test
    public void testPutInt_length1() throws CTFReaderException {
        fixture.position(1);
        fixture.putInt(1, 1);
    }

    /**
     * Test {@link BitBuffer#putInt(int, int)} with length = 0.
     *
     * @throws CTFReaderException
     *             Not expected
     */
    @Test
    public void testPutInt_length0() throws CTFReaderException {
        fixture.position(1);
        fixture.putInt(0, 1);
    }

    /**
     * Test writing and reading a value defined in hex format.
     *
     * @throws CTFReaderException
     *             Not expected
     */
    @Test
    public void testPutInt_hex() throws CTFReaderException {
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
     * @throws CTFReaderException
     *             Expected
     */
    @Test(expected = CTFReaderException.class)
    public void testPutInt_invalid() throws CTFReaderException {
        BitBuffer fixture2;
        fixture2 = new BitBuffer(ByteBuffer.allocateDirect(128));
        fixture2.setByteOrder(ByteOrder.BIG_ENDIAN);
        createBuffer(fixture2, 4);
        fixture2.position(1);

        /* This will try writing past the buffer's end */
        fixture2.putInt(32, 1);
    }
}
