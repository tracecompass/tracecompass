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
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.tests.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.tracecompass.ctf.core.event.types.AbstractArrayDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.CompoundDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.Encoding;
import org.eclipse.tracecompass.ctf.core.event.types.IDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StringDeclaration;
import org.eclipse.tracecompass.ctf.core.tests.io.Util;
import org.eclipse.tracecompass.internal.ctf.core.event.types.ArrayDeclaration;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>ArrayDeclaration2Test</code> contains tests for the class
 * <code>{@link ArrayDeclaration}</code>.
 *
 * @author Matthew Khouzam
 * @version $Revision: 1.0 $
 */
public class ArrayDeclaration2Test {

    private ArrayDeclaration fixture;

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        fixture = new ArrayDeclaration(1, StringDeclaration.getStringDeclaration(Encoding.UTF8));
    }

    /**
     * Run the ArrayDeclaration(int,Declaration) constructor test.
     */
    @Test
    public void testArrayDeclaration() {
        int length = 1;
        IDeclaration elemType = StringDeclaration.getStringDeclaration(Encoding.UTF8);
        ArrayDeclaration result = new ArrayDeclaration(length, elemType);

        assertNotNull(result);
        String left = "[declaration] array[";
        String right = result.toString().substring(0, left.length());
        assertEquals(left, right);
        assertEquals(1, result.getLength());
    }

    /**
     * Run the ArrayDefinition createDefinition(DefinitionScope,String) method
     * test.
     *
     * @throws CTFException
     *             error in the bitbuffer
     */
    @Test
    public void testCreateDefinition() throws CTFException {
        String fieldName = "";
        IDefinitionScope definitionScope = null;
        AbstractArrayDefinition result;
        byte[] array = { 't', 'e', 's', 't', '\0', 't', 'h', 'i', 's', '\0' };
        BitBuffer bb = new BitBuffer(Util.testMemory(ByteBuffer.wrap(array)));
        result = fixture.createDefinition(definitionScope, fieldName, bb);

        assertNotNull(result);
    }

    /**
     * Run the Declaration getElementType() method test.
     */
    @Test
    public void testGetElementType() {
        IDeclaration result = fixture.getElementType();
        assertNotNull(result);
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
     * Run the boolean isString() method test.
     */
    @Test
    public void testIsString_ownDefs() {
        // it's an array of strings, not a string
        assertFalse(fixture.isString());
    }

    /**
     * Run the boolean isString() method test.
     */
    @Test
    public void testIsString_complex() {
        final IntegerDeclaration id = IntegerDeclaration.createDeclaration(8, false, 16,
                ByteOrder.LITTLE_ENDIAN, Encoding.UTF8, "", 8);
        CompoundDeclaration ad = new ArrayDeclaration(0, id);

        boolean result = ad.isString();

        assertTrue(result);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString() {
        String result = fixture.toString();
        String left = "[declaration] array[";
        String right = result.substring(0, left.length());

        assertEquals(left, right);
    }

    /**
     * Test the hashcode
     */
    @Test
    public void hashcodeTest() {
        assertEquals(2016, fixture.hashCode());
        assertEquals(new ArrayDeclaration(1, StringDeclaration.getStringDeclaration(Encoding.UTF8)).hashCode(), fixture.hashCode());
    }

    /**
     * Test the equals
     */
    @Test
    public void equalsTest() {
        ArrayDeclaration a = new ArrayDeclaration(1, IntegerDeclaration.INT_32B_DECL);
        ArrayDeclaration b = new ArrayDeclaration(2, IntegerDeclaration.INT_32B_DECL);
        ArrayDeclaration c = new ArrayDeclaration(1, StringDeclaration.getStringDeclaration(Encoding.UTF8));
        ArrayDeclaration d = new ArrayDeclaration(1, IntegerDeclaration.INT_32B_DECL);
        assertNotEquals(a, null);
        assertNotEquals(a, new Object());
        assertNotEquals(a, b);
        assertNotEquals(a, c);
        assertEquals(a, d);
        assertEquals(a, a);
        assertNotEquals(b, a);
        assertNotEquals(c, a);
        assertEquals(d, a);
        assertEquals(a, a);
        assertFalse(a.isBinaryEquivalent(b));
        assertFalse(b.isBinaryEquivalent(a));
        assertFalse(a.isBinaryEquivalent(c));
        assertFalse(c.isBinaryEquivalent(a));
        assertTrue(a.isBinaryEquivalent(d));
        assertTrue(d.isBinaryEquivalent(a));
        assertTrue(a.isBinaryEquivalent(a));
    }
}
