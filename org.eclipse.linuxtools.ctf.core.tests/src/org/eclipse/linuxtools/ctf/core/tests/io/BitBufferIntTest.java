/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.tests.io;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.junit.Before;
import org.junit.Test;

/**
 * Part of the BitBuffer tests which test the methods to read/write integers.
 * These are separated from the main file because the fixture is different.
 *
 * @author alexmont
 */
public class BitBufferIntTest {

    private BitBuffer fixture;

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        fixture = new BitBuffer(java.nio.ByteBuffer.allocateDirect(128));
        fixture.setByteOrder(ByteOrder.BIG_ENDIAN);
        createBuffer(fixture);
    }

    private static void createBuffer(BitBuffer fixture) {
        createBuffer(fixture, 16);
    }

    private static void createBuffer(BitBuffer fixture, int j) {
        byte[] bytes = new byte[j];
        for (int i = 0; i < j; i++) {
            bytes[i] = (byte) (i % 0xff);
        }
        fixture.setByteBuffer(ByteBuffer.wrap(bytes));
        fixture.position(1);
    }

    /**
     * Run the int getInt() method test.
     */
    @Test
    public void testGetInt_base() {
        int result = fixture.getInt();
        assertEquals(0x020406, result);
    }

    /**
     * Run the int getInt(int) method test.
     */
    @Test
    public void testGetInt_pos0() {
        fixture.position(0);
        int result = fixture.getInt();
        assertEquals(0x010203, result);
    }

    /**
     * Run the int getInt(int,boolean) method test.
     */
    @Test
    public void testGetInt_pos1() {
        fixture.position(1);
        int length = 1;
        boolean signed = true;

        int result = fixture.getInt(length, signed);
        assertEquals(0, result);
    }

    /**
     * Run the int getInt(int,boolean) method test.
     */
    @Test
    public void testGetInt_pos2() {
        fixture.position(2);
        int length = 0;
        boolean signed = true;

        int result = fixture.getInt(length, signed);
        assertEquals(0, result);
    }

    /**
     * Run the int getInt(int,int,boolean) method test.
     */
    @Test
    public void testGetInt_signed() {
        fixture.position(1);
        int length = 0;
        boolean signed = true;

        int result = fixture.getInt(length, signed);
        assertEquals(0, result);
    }

    /**
     * Run the int getInt(int,int,boolean) method test.
     */
    @Test
    public void testGetInt_signed_length1() {
        fixture.position(1);
        int length = 1;
        boolean signed = true;

        int result = fixture.getInt(length, signed);
        assertEquals(0, result);
    }

    /**
     * Run the int getInt(int,int,boolean) method test with a little-endian
     * BitBuffer.
     */
    @Test
    public void testGetInt_le1() {
        BitBuffer le_fixture = new BitBuffer(
                java.nio.ByteBuffer.allocateDirect(128));
        le_fixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        createBuffer(le_fixture);
        le_fixture.position(1);
        int length = 24;
        int result = le_fixture.getInt(length, false);

        /* 0x020100 downshifted */
        assertEquals(0x810080, result);
    }

    /**
     * Run the int getInt(int,int,boolean) method test with a little-endian
     * BitBuffer.
     */
    @Test
    public void testGetInt_le2() {
        BitBuffer le_fixture = new BitBuffer(
                java.nio.ByteBuffer.allocateDirect(128));
        le_fixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        createBuffer(le_fixture);
        le_fixture.position(0);
        int length = 24;
        int result = le_fixture.getInt(length, false);
        assertEquals(0x020100, result);
    }

    /**
     * Run the int getInt(int,boolean) method test and expect an overflow.
     */
    @Test(expected = java.nio.BufferOverflowException.class)
    public void testGetInt_invalid() {
        BitBuffer small_fixture = new BitBuffer(
                java.nio.ByteBuffer.allocateDirect(128));
        small_fixture.setByteOrder(ByteOrder.BIG_ENDIAN);
        createBuffer(small_fixture, 2);
        small_fixture.position(10);
        int length = 32;
        boolean signed = true;

        int result = small_fixture.getInt(length, signed);
        assertEquals(0, result);
    }

    /**
     * Run the int getInt(int,int,boolean) method test and expect an overflow.
     */
    @Test(expected = java.nio.BufferOverflowException.class)
    public void testGetInt_invalid2() {
        BitBuffer small_fixture = new BitBuffer(
                java.nio.ByteBuffer.allocateDirect(128));
        small_fixture.setByteOrder(ByteOrder.BIG_ENDIAN);
        createBuffer(small_fixture, 2);
        small_fixture.position(1);
        int length = 64;
        boolean signed = true;

        int result = small_fixture.getInt(length, signed);
        assertEquals(0, result);
    }

    /**
     * Run the void putInt(int) method test.
     */
    @Test
    public void testPutInt() {
        int value = 1;
        fixture.position(1);
        fixture.putInt(value);
    }

    /**
     * Run the void putInt(int,int,boolean) method test.
     */
    @Test
    public void testPutInt_signed() {
        int length = 1;
        int value = 1;

        fixture.position(1);
        fixture.putInt(length, value);
    }

    /**
     * Run the void putInt(int,int,int,boolean) method test.
     */
    @Test
    public void testPutInt_length0() {
        int length = 0;
        int value = 1;

        fixture.position(1);
        fixture.putInt(length, value);
    }

    /**
     * Run the void putInt(int,int,int,boolean) method test.
     */
    @Test
    public void testPutInt_length1() {
        int length = 1;
        int value = 1;

        fixture.position(1);
        fixture.putInt(length, value);
    }

    /**
     * Run the void putInt(int) method test.
     */
    @Test
    public void testPutInt_hex() {
        final int value = 0x010203;
        int read;

        for (int i = 0; i <= 32; i++) {
            fixture.position(i);
            fixture.putInt(value);

            fixture.position(i);
            read = fixture.getInt();

            assertEquals(value, read);
        }
    }

    /**
     * Run the void putInt(int,int,int,boolean) method test.
     */
    @Test(expected = java.nio.BufferOverflowException.class)
    public void testPutInt_invalid() {
        BitBuffer fixture2;
        fixture2 = new BitBuffer(java.nio.ByteBuffer.allocateDirect(128));
        fixture2.setByteOrder(ByteOrder.BIG_ENDIAN);
        createBuffer(fixture2, 4);
        fixture2.position(1);

        int length = 32;
        int value = 1;

        fixture2.putInt(length, value);

        int read = fixture2.getInt(1, true);
        assertEquals(value, read);
    }
}
