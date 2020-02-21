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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;

import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.types.Encoding;
import org.eclipse.tracecompass.ctf.core.event.types.IDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StringDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StructDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StructDefinition;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>StructDeclarationTest</code> contains tests for the class
 * <code>{@link StructDeclaration}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class StructDeclarationTest {

    private StructDeclaration fixture;

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        fixture = new StructDeclaration(1L);
    }

    /**
     * Run the StructDeclaration(long) constructor test.
     */
    @Test
    public void testStructDeclaration() {
        assertNotNull(fixture);
        assertEquals(1L, fixture.getMaxAlign());

        String regex = "^\\[declaration\\] struct\\[*.\\]$";
        assertTrue(fixture.toString().matches(regex));
    }

    /**
     * Run the void addField(String,Declaration) method test.
     */
    @Test
    public void testAddField() {
        String name = "";
        IDeclaration declaration = StringDeclaration.getStringDeclaration(Encoding.UTF8);
        fixture.addField(name, declaration);
    }

    /**
     * Run the StructDefinition createDefinition(DefinitionScope,String) method
     * test.
     *
     * @throws CTFException
     *             out of bounds
     */
    @Test
    public void testCreateDefinition() throws CTFException {
        String fieldName = "";
        ByteBuffer allocate = ByteBuffer.allocate(100);
        BitBuffer bb = new BitBuffer(allocate);
        StructDefinition result = fixture.createDefinition(null, fieldName, bb);
        assertNotNull(result);
    }

    /**
     * Run the Declaration getField(String) method test.
     */
    @Test
    public void testGetField() {
        IDeclaration result = fixture.getField("test");

        assertNull(result);
    }

    /**
     * Run the List<String> getFieldsList() method test.
     */
    @Test
    public void testGetFieldsList() {
        Iterable<String> result = fixture.getFieldsList();

        assertNotNull(result);
        assertEquals(false, result.iterator().hasNext());
    }

    /**
     * Run the long getMinAlign() method test.
     */
    @Test
    public void testGetMinAlign() {
        long result = fixture.getMaxAlign();
        assertEquals(1L, result);
    }

    /**
     * Run the boolean hasField(String) method test.
     */
    @Test
    public void testHasField() {
        String name = "";
        boolean result = fixture.hasField(name);

        assertEquals(false, result);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString() {
        String result = fixture.toString();
        String trunc = result.substring(0, 21);

        assertEquals("[declaration] struct[", trunc);
    }

    /**
     * Test the hashcode
     */
    @Test
    public void hashcodeTest() {
        assertEquals(32, fixture.hashCode());
        StructDeclaration a = new StructDeclaration(8);
        fixture.addField("hello", a);
        a.addField("Time", IntegerDeclaration.INT_32B_DECL);
        StructDeclaration b = new StructDeclaration(8);
        StructDeclaration c = new StructDeclaration(8);
        b.addField("hello", c);
        c.addField("Time", IntegerDeclaration.INT_32B_DECL);
        assertEquals(b.hashCode(), fixture.hashCode());
        c.addField("Space", IntegerDeclaration.INT_32L_DECL);
        assertNotEquals(b.hashCode(), fixture.hashCode());
    }

    /**
     * Test the equals
     */
    @Test
    public void equalsTest() {
        StructDeclaration a = new StructDeclaration(8);
        StructDeclaration b = new StructDeclaration(16);
        StructDeclaration c = new StructDeclaration(8);
        StructDeclaration d = new StructDeclaration(8);
        StructDeclaration e = new StructDeclaration(8);
        StructDeclaration f = new StructDeclaration(8);
        c.addField("hi", StringDeclaration.getStringDeclaration(Encoding.UTF8));
        assertNotEquals(a, null);
        assertNotEquals(a, new Object());
        assertNotEquals(a, b);
        assertNotEquals(a, c);
        assertEquals(a, d);
        assertNotEquals(b, a);
        assertNotEquals(c, a);
        assertEquals(d, a);
        assertEquals(a, a);
        a.addField("hi", StringDeclaration.getStringDeclaration(Encoding.UTF8));
        f.addField("hi", StringDeclaration.getStringDeclaration(Encoding.UTF8));
        e.addField("hello", StringDeclaration.getStringDeclaration(Encoding.UTF8));
        assertEquals(a, c);
        assertEquals(c, a);
        assertNotEquals(a, d);
        d.addField("hi", IntegerDeclaration.INT_32B_DECL);
        assertNotEquals(a, d);
        a.addField("hello", StringDeclaration.getStringDeclaration(Encoding.UTF8));
        e.addField("hi", StringDeclaration.getStringDeclaration(Encoding.UTF8));
        f.addField("hello", IntegerDeclaration.INT_32B_DECL);
        assertNotEquals(a, e);
        assertNotEquals(a, f);
    }
}
