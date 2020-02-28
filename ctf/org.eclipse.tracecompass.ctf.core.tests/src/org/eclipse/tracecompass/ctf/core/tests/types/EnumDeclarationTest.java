/*******************************************************************************
 * Copyright (c) 2013, 2020 Ericsson, École Polytechnique de Montréal
 *
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.tracecompass.ctf.core.event.types.Encoding;
import org.eclipse.tracecompass.ctf.core.event.types.EnumDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.EnumDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.tracecompass.ctf.core.tests.io.Util;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>EnumDeclarationTest</code> contains tests for the class
 * <code>{@link EnumDeclaration}</code>.
 *
 * @author Matthew Khouzam
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

        assertTrue(fixture.add(low, high, label));
        assertEquals("", fixture.query(1));
    }

    /**
     * Run the boolean add(long,long,String) method test several times out of
     * order.
     */
    @Test
    public void testAddMany() {
        assertTrue(fixture.add(00, 01, "fork"));
        assertTrue(fixture.add(02, 03, "tork"));
        assertTrue(fixture.add(04, 07, "mork"));
        assertTrue(fixture.add(10, 20, "zork"));
        assertTrue(fixture.add(22, 27, "york"));
        assertTrue(fixture.add(21, 21, "bork"));
        assertTrue(fixture.add(28, 50, "dork"));
        assertEquals("fork", fixture.query(0));
        assertEquals("fork", fixture.query(1));
        assertEquals("tork", fixture.query(2));
        assertEquals("tork", fixture.query(3));
        assertEquals("mork", fixture.query(4));
        assertEquals("mork", fixture.query(5));
        assertEquals("mork", fixture.query(6));
        assertEquals("zork", fixture.query(10));
        assertEquals("zork", fixture.query(19));
        assertEquals("bork", fixture.query(21));
        assertEquals("york", fixture.query(22));
    }

    /**
     * Tests adding two of the same elements, this is allowed in the ctf spec
     */
    @Test
    public void testDubs() {
        assertTrue(fixture.add(00, 01, "fork"));
        assertTrue(fixture.add(02, 03, "fork"));
        assertNull(fixture.query(-1));
        assertEquals("fork", fixture.query(0));
        assertEquals("fork", fixture.query(1));
        assertEquals("fork", fixture.query(2));
        assertEquals("fork", fixture.query(3));
        assertNull(fixture.query(5));
    }

    /**
     * Tests adding two of the same elements
     */
    @Test
    public void testOverlap1() {
        assertTrue(fixture.add(00, 01, "fork"));
        assertFalse(fixture.add(01, 03, "zork"));
    }

    /**
     * Tests adding two of the same elements
     */
    @Test
    public void testOverlap2() {
        assertTrue(fixture.add(00, 02, "fork"));
        assertFalse(fixture.add(01, 03, "zork"));
    }

    /**
     * Tests adding two of the same elements
     */
    @Test
    public void testOverlap3() {
        assertTrue(fixture.add(00, 03, "fork"));
        assertFalse(fixture.add(01, 02, "zork"));
    }

    /**
     * Tests adding two of the same elements
     */
    @Test
    public void testOverlap4() {
        assertTrue(fixture.add(01, 03, "fork"));
        assertFalse(fixture.add(00, 02, "zork"));
    }

    /**
     * Run the EnumDefinition createDefinition(DefinitionScope,String) method
     * test.
     *
     * @throws CTFException
     *             out of bounds error, won't happen
     */
    @Test
    public void testCreateDefinition() throws CTFException {
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
     * Test that values that are not present in the enum but whose bits have a
     * value are returned as ORed strings
     */
    @Test
    public void testBitFlagEnum() {
        assertTrue(fixture.add(1 << 0, 1 << 0, "flag1"));
        assertTrue(fixture.add(1 << 1, 1 << 1, "flag2"));
        assertTrue(fixture.add(1 << 2, 1 << 2, "flag3"));
        assertTrue(fixture.add(1 << 3, 1 << 3, "flag4"));
        assertTrue(fixture.add(1 << 4, 1 << 4, "flag5"));
        assertTrue(fixture.add(1 << 5, 1 << 5, "flag6"));
        assertTrue(fixture.add(1 << 6, (1 << 6) + 3, "range"));
        // Test a value with bit flag set
        assertEquals("flag1 | flag2 | flag3", fixture.query((1 << 0) + (1 << 1) + (1 << 2)));
        // Test a value where one bit is a range
        assertNull(fixture.query((1 << 1) + (1 << 4) + (1 << 6)));
        // Test a normal value that is included in the range
        assertEquals("range", fixture.query((1 << 6) + 1));
        // Test a value with one bit not set anywhere
        assertNull(fixture.query((1 << 1) + (1 << 4) + (1 << 8)));
        // Test 0
        assertNull(fixture.query(0));
        // Test a negative value
        assertNull(fixture.query(-1));

        // Add the 0 to the fixtures, values are still flags
        assertTrue(fixture.add(0, 0, "noflag"));
        assertEquals("noflag", fixture.query(0));

        // Add a negative range
        assertTrue(fixture.add(-4, -1, "negativeRange"));
        // Bit flag still works
        assertEquals("flag1 | flag2 | flag3", fixture.query((1 << 0) + (1 << 1) + (1 << 2)));
        // Negative range OK
        assertEquals("negativeRange", fixture.query(-1));
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

    /**
     * Test the hashcode
     */
    @Test
    public void hashcodeTest() {
        EnumDeclaration b = new EnumDeclaration(IntegerDeclaration.createDeclaration(1, false, 1,
                ByteOrder.BIG_ENDIAN, Encoding.ASCII, "", 8));
        assertEquals(b.hashCode(), fixture.hashCode());
        fixture.add(0, 1, "hello");
        fixture.add(2, 3, "kitty");
        b.add(0, 1, "hello");
        b.add(2, 3, "kitty");
        assertEquals(fixture.hashCode(), b.hashCode());
    }

    /**
     * Test the equals
     */
    @Test
    public void equalsTest() {
        EnumDeclaration a = new EnumDeclaration(IntegerDeclaration.INT_8_DECL);
        EnumDeclaration b = new EnumDeclaration(IntegerDeclaration.INT_8_DECL);
        b.add(2, 19, "hi");
        EnumDeclaration c = new EnumDeclaration(IntegerDeclaration.INT_32B_DECL);
        EnumDeclaration d = new EnumDeclaration(IntegerDeclaration.INT_8_DECL);
        assertNotEquals(a, null);
        assertNotEquals(a, new Object());
        assertNotEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(b, c);
        assertEquals(a, d);
        assertNotEquals(b, a);
        assertNotEquals(c, a);
        assertNotEquals(c, b);
        assertEquals(d, a);
        a.add(2, 19, "hi");
        assertEquals(a, a);
        assertEquals(a, b);
        assertEquals(b, a);
        assertNotEquals(a, d);
        assertNotEquals(d, a);
        d.add(2, 22, "hi");
        assertNotEquals(a, d);
        assertNotEquals(d, a);
    }

    /**
     * Test the isBinaryEquivalent
     */
    @Test
    public void binaryEquivalentTest() {
        EnumDeclaration a = new EnumDeclaration(IntegerDeclaration.INT_8_DECL);
        EnumDeclaration b = new EnumDeclaration(IntegerDeclaration.INT_8_DECL);
        b.add(2, 19, "hi");
        EnumDeclaration c = new EnumDeclaration(IntegerDeclaration.INT_32B_DECL);
        EnumDeclaration d = new EnumDeclaration(IntegerDeclaration.INT_8_DECL);
        assertFalse(a.isBinaryEquivalent(null));
        assertFalse(a.isBinaryEquivalent(IntegerDeclaration.INT_32B_DECL));
        assertFalse(a.isBinaryEquivalent(b));
        assertFalse(a.isBinaryEquivalent(c));
        assertFalse(b.isBinaryEquivalent(c));
        assertTrue(a.isBinaryEquivalent(d));
        assertFalse(b.isBinaryEquivalent(a));
        assertFalse(c.isBinaryEquivalent(a));
        assertFalse(c.isBinaryEquivalent(b));
        assertTrue(d.isBinaryEquivalent(a));
        a.add(2, 19, "hi");
        assertTrue(a.isBinaryEquivalent(a));
        assertTrue(a.isBinaryEquivalent(b));
        assertTrue(b.isBinaryEquivalent(a));
        assertFalse(a.isBinaryEquivalent(d));
        assertFalse(d.isBinaryEquivalent(a));
        d.add(2, 22, "hi");
        assertFalse(a.isBinaryEquivalent(d));
        assertFalse(d.isBinaryEquivalent(a));
    }

}
