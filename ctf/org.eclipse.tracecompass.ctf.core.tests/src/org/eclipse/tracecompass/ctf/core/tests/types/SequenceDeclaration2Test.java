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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.types.AbstractArrayDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.Definition;
import org.eclipse.tracecompass.ctf.core.event.types.Encoding;
import org.eclipse.tracecompass.ctf.core.event.types.IDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.StringDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StructDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StructDefinition;
import org.eclipse.tracecompass.internal.ctf.core.event.types.SequenceDeclaration;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>SequenceDeclarationTest</code> contains tests for the class
 * <code>{@link SequenceDeclaration}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
@SuppressWarnings("javadoc")
public class SequenceDeclaration2Test {

    @NonNull
    private static final String FIELD_NAME = "LengthName";

    private SequenceDeclaration fixture;
    @NonNull
    private BitBuffer input = new BitBuffer();

    @Before
    public void setUp() {
        fixture = new SequenceDeclaration(FIELD_NAME, StringDeclaration.getStringDeclaration(Encoding.UTF8));
        byte array[] = { 't', 'e', 's', 't', '\0', 't', 'h', 'i', 's', '\0' };
        ByteBuffer byb = ByteBuffer.wrap(array);
        input = new BitBuffer(byb);
    }

    /**
     * Run the SequenceDeclaration(String,Declaration) constructor test.
     */
    @Test
    public void testSequenceDeclaration() {
        String lengthName = "";
        IDeclaration elemType = StringDeclaration.getStringDeclaration(Encoding.UTF8);

        SequenceDeclaration result = new SequenceDeclaration(lengthName, elemType);
        assertNotNull(result);
        String string = "[declaration] sequence[";
        assertEquals(string, result.toString().substring(0, string.length()));
    }

    /**
     * Run the SequenceDefinition createDefinition(DefinitionScope,String)
     * method test.
     *
     * @throws CTFException
     *             an error in the bitbuffer
     */
    @Test
    public void testCreateDefinition() throws CTFException {
        long seqLen = 2;
        IntegerDeclaration id = IntegerDeclaration.createDeclaration(8, false, 8,
                ByteOrder.LITTLE_ENDIAN, Encoding.UTF8, "", 32);
        StructDeclaration structDec = new StructDeclaration(0);
        structDec.addField(FIELD_NAME, id);
        StructDefinition structDef = new StructDefinition(
                structDec,
                null,
                "x",
                new Definition[] {
                        new IntegerDefinition(
                                id,
                                null,
                                FIELD_NAME,
                                seqLen)
                });
        AbstractArrayDefinition result = fixture.createDefinition(structDef, FIELD_NAME, input);
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
     * Run the String toString() method test.
     */
    @Test
    public void testToString() {
        String result = fixture.toString();
        String left = "[declaration] sequence[";
        assertEquals(left, result.substring(0, left.length()));
    }

    /**
     * Test the hashcode
     */
    @Test
    public void hashcodeTest() {
        assertEquals(-1140774256, fixture.hashCode());
        SequenceDeclaration a = new SequenceDeclaration("Hi", IntegerDeclaration.INT_32B_DECL);
        SequenceDeclaration b = new SequenceDeclaration("Hello", IntegerDeclaration.INT_32B_DECL);
        SequenceDeclaration c = new SequenceDeclaration("Hi", StringDeclaration.getStringDeclaration(Encoding.UTF8));
        SequenceDeclaration d = new SequenceDeclaration("Hi", IntegerDeclaration.INT_32B_DECL);
        assertNotEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a.hashCode(), c.hashCode());
        assertEquals(a.hashCode(), d.hashCode());
    }

    /**
     * Test the equals
     */
    @Test
    public void equalsTest() {
        SequenceDeclaration a = new SequenceDeclaration("Hi", IntegerDeclaration.INT_32B_DECL);
        SequenceDeclaration b = new SequenceDeclaration("Hello", IntegerDeclaration.INT_32B_DECL);
        SequenceDeclaration c = new SequenceDeclaration("Hi", StringDeclaration.getStringDeclaration(Encoding.UTF8));
        SequenceDeclaration d = new SequenceDeclaration("Hi", IntegerDeclaration.INT_32B_DECL);
        assertNotEquals(a, null);
        assertNotEquals(a, new Object());
        assertNotEquals(a, b);
        assertNotEquals(a, c);
        assertEquals(a, d);
        assertNotEquals(b, a);
        assertNotEquals(c, a);
        assertEquals(d, a);
        assertEquals(a, a);
        assertFalse(a.isBinaryEquivalent(b));
        assertFalse(a.isBinaryEquivalent(c));
        assertTrue(a.isBinaryEquivalent(d));
        assertFalse(b.isBinaryEquivalent(a));
        assertFalse(c.isBinaryEquivalent(a));
        assertTrue(d.isBinaryEquivalent(a));
        assertTrue(a.isBinaryEquivalent(a));
    }

}
