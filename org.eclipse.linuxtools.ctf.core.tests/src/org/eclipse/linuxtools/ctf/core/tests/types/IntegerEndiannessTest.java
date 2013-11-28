/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal, Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *   Alexandre Montplaisir - Split out in separate class
 *   Matthew Khouzam - update api (exceptions)
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.tests.types;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.event.types.Encoding;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDefinition;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.junit.Before;
import org.junit.Test;

/**
 * Endianness test for {@link IntegerDefinition}.
 *
 * @author Geneviève Bastien
 */
public class IntegerEndiannessTest {

    private static String name = "testInt";
    private static String clockName = "clock";

    private ByteBuffer bb;
    private BitBuffer input;

    /**
     * Set up the bit-buffer to be used
     */
    @Before
    public void setUp() {
        bb = java.nio.ByteBuffer.allocateDirect(8);
        bb.put((byte) 0xab);
        bb.put((byte) 0xcd);
        bb.put((byte) 0xef);
        bb.put((byte) 0x12);
        bb.put((byte) 0x34);
        bb.put((byte) 0x56);
        bb.put((byte) 0x78);
        bb.put((byte) 0x9a);
        input = new BitBuffer(bb);
    }

    /**
     * Read 32-bits BE
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void test32BE() throws CTFReaderException {
        IntegerDeclaration be = new IntegerDeclaration(32, true, 1, ByteOrder.BIG_ENDIAN, Encoding.NONE, clockName, 8);
        IntegerDefinition fixture_be = be.createDefinition(null, name);
        fixture_be.read(input);
        assertEquals(0xabcdef12, fixture_be.getValue());
    }

    /**
     * Read 64-bits BE
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void test64BE() throws CTFReaderException {
        IntegerDeclaration be = new IntegerDeclaration(64, true, 1, ByteOrder.BIG_ENDIAN, Encoding.NONE, clockName, 8);
        IntegerDefinition fixture_be = be.createDefinition(null, name);
        fixture_be.read(input);
        assertEquals(0xabcdef123456789aL, fixture_be.getValue());
    }

    /**
     * Read 32-bits LE
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void test32LE() throws CTFReaderException {
        IntegerDeclaration le = new IntegerDeclaration(32, true, 1, ByteOrder.LITTLE_ENDIAN, Encoding.NONE, clockName, 8);
        IntegerDefinition fixture_le = le.createDefinition(null, name);
        fixture_le.read(input);
        assertEquals(0x12efcdab, fixture_le.getValue());
    }

    /**
     * Read 64-bits LE
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void test64LE() throws CTFReaderException {
        IntegerDeclaration le = new IntegerDeclaration(64, true, 1, ByteOrder.LITTLE_ENDIAN, Encoding.NONE, clockName, 8);
        IntegerDefinition fixture_le = le.createDefinition(null, name);
        fixture_le.read(input);
        assertEquals(0x9a78563412efcdabL, fixture_le.getValue());
    }
}
