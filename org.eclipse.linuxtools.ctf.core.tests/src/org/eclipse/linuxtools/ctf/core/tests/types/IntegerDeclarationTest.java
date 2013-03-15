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

package org.eclipse.linuxtools.ctf.core.tests.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.ByteOrder;

import org.eclipse.linuxtools.ctf.core.event.types.Encoding;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDeclaration;
import org.junit.After;
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
     * Launch the test.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(IntegerDeclarationTest.class);
    }

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        fixture = new IntegerDeclaration(1, true, 1, ByteOrder.BIG_ENDIAN,
                Encoding.ASCII, null, 32);
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        // Add additional tear down code here
    }

    /**
     * Run the IntegerDeclaration(int,boolean,int,ByteOrder,Encoding)
     * constructor test.
     */
    @Test
    public void testIntegerDeclaration() {
        int len = 1;
        boolean signed = true;
        int base = 1;
        ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
        Encoding encoding = Encoding.ASCII;

        IntegerDeclaration result = new IntegerDeclaration(len, signed, base,
                byteOrder, encoding, null, 16);

        assertNotNull(result);
        assertEquals(1, result.getBase());
        assertEquals(false, result.isCharacter());
        String outputValue = "[declaration] integer[";
        assertEquals(outputValue,
                result.toString().substring(0, outputValue.length()));
        assertEquals(1, result.getLength());
        assertEquals(true, result.isSigned());
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
        IntegerDeclaration fixture8 = new IntegerDeclaration(8, true, 1,
                ByteOrder.BIG_ENDIAN, Encoding.ASCII, null, 8);

        boolean result = fixture8.isCharacter();
        assertEquals(true, result);
    }

    /**
     * Run the boolean isSigned() method test.
     */
    @Test
    public void testIsSigned_signed() {
        boolean result = fixture.isSigned();
        assertEquals(true, result);
    }

    /**
     * Run the boolean isSigned() method test.
     */
    @Test
    public void testIsSigned_unsigned() {
        IntegerDeclaration fixture_unsigned = new IntegerDeclaration(1, false,
                1, ByteOrder.BIG_ENDIAN, Encoding.ASCII, null, 8);

        boolean result = fixture_unsigned.isSigned();
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
}