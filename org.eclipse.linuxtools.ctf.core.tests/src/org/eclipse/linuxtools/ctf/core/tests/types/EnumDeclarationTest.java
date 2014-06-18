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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.event.types.Encoding;
import org.eclipse.linuxtools.ctf.core.event.types.EnumDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.EnumDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.linuxtools.ctf.core.tests.io.Util;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>EnumDeclarationTest</code> contains tests for the class
 * <code>{@link EnumDeclaration}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class EnumDeclarationTest {

    private EnumDeclaration fixture;

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        fixture = new EnumDeclaration(IntegerDeclaration.createDeclaration(1, false, 1,
                ByteOrder.BIG_ENDIAN, Encoding.ASCII, "", 8));
    }

    /**
     * Run the EnumDeclaration(IntegerDeclaration) constructor test.
     */
    @Test
    public void testEnumDeclaration() {
        IntegerDeclaration containerType = IntegerDeclaration.createDeclaration(1, false, 1,
                ByteOrder.BIG_ENDIAN, Encoding.ASCII, "", 8);

        EnumDeclaration result = new EnumDeclaration(containerType);

        assertNotNull(result);
        String left = "[declaration] enum[";
        assertEquals(left, result.toString().substring(0, left.length()));
    }

    /**
     * Run the boolean add(long,long,String) method test.
     */
    @Test
    public void testAdd() {
        long low = 1L;
        long high = 1L;
        String label = "";

        boolean result = fixture.add(low, high, label);

        assertTrue(result);
    }

    /**
     * Run the EnumDefinition createDefinition(DefinitionScope,String) method
     * test.
     *
     * @throws CTFReaderException
     *             out of bounds error, won't happen
     */
    @Test
    public void testCreateDefinition() throws CTFReaderException {
        IDefinitionScope definitionScope = null;
        String fieldName = "";
        byte[] array = { 't', 'e', 's', 't', '\0', 't', 'h', 'i', 's', '\0' };
        BitBuffer bb = new BitBuffer(Util.testMemory(ByteBuffer.wrap(array)));

        EnumDefinition result = fixture.createDefinition(definitionScope,
                fieldName, bb);

        assertNotNull(result);
    }

    /**
     * Run the String query(long) method test.
     */
    @Test
    public void testQuery() {
        long value = 0;
        String result = fixture.query(value);

        assertNull(result);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString() {
        String result = fixture.toString();

        String left = "[declaration] enum[";
        assertEquals(left, result.substring(0, left.length()));
    }
}
