/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *     Marc-Andre Laperle - Add min/maximum for validation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.tests.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigInteger;
import java.nio.ByteOrder;

import org.eclipse.linuxtools.ctf.core.event.types.Encoding;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDeclaration;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>IntegerDeclarationTest</code> contains tests for the class
 * <code>{@link IntegerDeclaration}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class IntegerDeclarationTest {

    private IntegerDeclaration fixture;

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        fixture = IntegerDeclaration.createDeclaration(1, false, 1, ByteOrder.BIG_ENDIAN,
                Encoding.ASCII, "", 32);
    }

    /**
     * Run the IntegerDeclaration(int,boolean,int,ByteOrder,Encoding)
     * constructor test.
     */
    @Test
    public void testIntegerDeclaration() {
        int len = 1;
        boolean signed = false;
        int base = 1;
        ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
        Encoding encoding = Encoding.ASCII;

        IntegerDeclaration result = IntegerDeclaration.createDeclaration(len, signed, base,
                byteOrder, encoding, "", 16);

        assertNotNull(result);
        assertEquals(1, result.getBase());
        assertEquals(false, result.isCharacter());
        String outputValue = "[declaration] integer[";
        assertEquals(outputValue,
                result.toString().substring(0, outputValue.length()));
        assertEquals(1, result.getLength());
        assertEquals(false, result.isSigned());
    }

    /**
     * Test that IntegerDeclaration throws when constructing a signed 1 bit declaration
     */
    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testIntegerDeclarationIllegalArgSignedBit() {
        int len = 1;
        boolean signed = true;
        int base = 1;
        ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
        Encoding encoding = Encoding.ASCII;
        IntegerDeclaration.createDeclaration(len, signed, base, byteOrder, encoding, "", 16);
    }

    /**
     * Test that IntegerDeclaration throws when constructing a invalid length declaration
     */
    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testIntegerDeclarationIllegalArgBadLenght() {
        int len = 0;
        boolean signed = false;
        int base = 1;
        ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
        Encoding encoding = Encoding.ASCII;
        IntegerDeclaration.createDeclaration(len, signed, base, byteOrder, encoding, "", 16);
    }

    /**
     * Run the int getBase() method test.
     */
    @Test
    public void testGetBase() {
        int result = fixture.getBase();
        assertEquals(1, result);
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
     * Run the Encoding getEncoding() method test.
     */
    @Test
    public void testGetEncoding() {
        Encoding result = fixture.getEncoding();
        assertNotNull(result);
        assertEquals("ASCII", result.name());
        assertEquals("ASCII", result.toString());
        assertEquals(1, result.ordinal());
    }

    /**
     * Run the int getLength() method test.
     */
    @Test
    public void testGetLength() {
        int result = fixture.getLength();
        assertEquals(1, result);
    }

    /**
     * Run the boolean isCharacter() method test.
     */
    @Test
    public void testIsCharacter() {
        boolean result = fixture.isCharacter();
        assertEquals(false, result);
    }

    /**
     * Run the boolean isCharacter() method test.
     */
    @Test
    public void testIsCharacter_8bytes() {
        IntegerDeclaration fixture8 = IntegerDeclaration.createDeclaration(8, true, 1,
                ByteOrder.BIG_ENDIAN, Encoding.ASCII, "", 8);

        boolean result = fixture8.isCharacter();
        assertEquals(true, result);
    }

    /**
     * Run the boolean isSigned() method test.
     */
    @Test
    public void testIsSigned_signed() {
        IntegerDeclaration fixtureSigned = IntegerDeclaration.createDeclaration(2, true,
                1, ByteOrder.BIG_ENDIAN, Encoding.ASCII, "", 8);
        boolean result = fixtureSigned.isSigned();
        assertEquals(true, result);
    }

    /**
     * Run the boolean isSigned() method test.
     */
    @Test
    public void testIsSigned_unsigned() {
        boolean result = fixture.isSigned();
        assertEquals(false, result);
    }


    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString() {
        String result = fixture.toString();
        String trunc = result.substring(0, 22);
        assertEquals("[declaration] integer[", trunc);
    }

    /**
     * Run the long getMaxValue() method test.
     */
    @Test
    public void testMaxValue() {
        assertEquals(BigInteger.ONE, fixture.getMaxValue());

        IntegerDeclaration signed8bit = IntegerDeclaration.createDeclaration(8, true, 1, ByteOrder.BIG_ENDIAN, Encoding.ASCII, "", 32);
        assertEquals(BigInteger.valueOf(127), signed8bit.getMaxValue());

        IntegerDeclaration unsigned8bit = IntegerDeclaration.createDeclaration(8, false, 1, ByteOrder.BIG_ENDIAN, Encoding.ASCII, "", 32);
        assertEquals(BigInteger.valueOf(255), unsigned8bit.getMaxValue());

        IntegerDeclaration signed32bit = IntegerDeclaration.createDeclaration(32, true, 1, ByteOrder.BIG_ENDIAN, Encoding.ASCII, "", 32);
        assertEquals(BigInteger.valueOf(2147483647), signed32bit.getMaxValue());

        IntegerDeclaration unsigned32bit = IntegerDeclaration.createDeclaration(32, false, 1, ByteOrder.BIG_ENDIAN, Encoding.ASCII, "", 32);
        assertEquals(BigInteger.valueOf(4294967295l), unsigned32bit.getMaxValue());

        IntegerDeclaration signed64bit = IntegerDeclaration.createDeclaration(64, true, 1, ByteOrder.BIG_ENDIAN, Encoding.ASCII, "", 32);
        assertEquals(BigInteger.valueOf(9223372036854775807L), signed64bit.getMaxValue());

        IntegerDeclaration unsigned64bit = IntegerDeclaration.createDeclaration(64, false, 1, ByteOrder.BIG_ENDIAN, Encoding.ASCII, "", 32);
        assertEquals(BigInteger.valueOf(2).pow(64).subtract(BigInteger.ONE), unsigned64bit.getMaxValue());
    }

    /**
     * Run the long getMinValue() method test.
     */
    @Test
    public void testMinValue() {
        assertEquals(BigInteger.ZERO, fixture.getMinValue());

        IntegerDeclaration signed8bit = IntegerDeclaration.createDeclaration(8, true, 1, ByteOrder.BIG_ENDIAN, Encoding.ASCII, "", 32);
        assertEquals(BigInteger.valueOf(-128), signed8bit.getMinValue());

        IntegerDeclaration unsigned8bit = IntegerDeclaration.createDeclaration(8, false, 1, ByteOrder.BIG_ENDIAN, Encoding.ASCII, "", 32);
        assertEquals(BigInteger.ZERO, unsigned8bit.getMinValue());

        IntegerDeclaration signed32bit = IntegerDeclaration.createDeclaration(32, true, 1, ByteOrder.BIG_ENDIAN, Encoding.ASCII, "", 32);
        assertEquals(BigInteger.valueOf(-2147483648), signed32bit.getMinValue());

        IntegerDeclaration unsigned32bit = IntegerDeclaration.createDeclaration(32, false, 1, ByteOrder.BIG_ENDIAN, Encoding.ASCII, "", 32);
        assertEquals(BigInteger.ZERO, unsigned32bit.getMinValue());

        IntegerDeclaration signed64bit = IntegerDeclaration.createDeclaration(64, true, 1, ByteOrder.BIG_ENDIAN, Encoding.ASCII, "", 32);
        assertEquals(BigInteger.valueOf(-9223372036854775808L), signed64bit.getMinValue());

        IntegerDeclaration unsigned64bit = IntegerDeclaration.createDeclaration(64, false, 1, ByteOrder.BIG_ENDIAN, Encoding.ASCII, "", 32);
        assertEquals(BigInteger.ZERO, unsigned64bit.getMinValue());
    }
}