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
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.event.types.Encoding;
import org.eclipse.linuxtools.ctf.core.event.types.EnumDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.EnumDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>EnumDefinitionTest</code> contains tests for the class
 * <code>{@link EnumDefinition}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class EnumDefinitionTest {

    private EnumDefinition fixture;

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        EnumDeclaration declaration = new EnumDeclaration(
                new IntegerDeclaration(1, false, 1, ByteOrder.BIG_ENDIAN,
                        Encoding.ASCII, null, 8));
        declaration.add(0, 10, "a");
        declaration.add(11, 20, "b");
        String fieldName = "";

        fixture = new EnumDefinition(declaration, null, fieldName);
    }

    /**
     * Run the EnumDefinition(EnumDeclaration,DefinitionScope,String)
     * constructor test.
     */
    @Test
    public void testEnumDefinition() {
        assertNotNull(fixture);
    }

    /**
     * Run the String getValue() method test.
     */
    @Test
    public void testGetValue() {
        String result = fixture.getValue();

        assertNotNull(result);
    }

    /**
     * Run the long getIntegerValue() method test.
     */
    @Test
    public void testGetIntegerValue_one() {
        fixture.setIntegerValue(1L);
        long result = fixture.getIntegerValue();

        assertEquals(1L, result);
    }

    /**
     * Run the String getValue() method test.
     */
    @Test
    public void testGetIntegerValue_zero() {
        fixture.setIntegerValue(0);
        long result = fixture.getIntegerValue();

        assertTrue(0 == result);
    }

    /**
     * Run the void read(BitBuffer) method test.
     * @throws CTFReaderException error
     */
    @Test
    public void testRead() throws CTFReaderException {
        fixture.setIntegerValue(1L);
        BitBuffer input = new BitBuffer(ByteBuffer.allocateDirect(128));

        fixture.read(input);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString() {
        fixture.setIntegerValue(16);
        String result = fixture.toString();

        assertEquals("{ value = b, container = 16 }", result);
    }
}