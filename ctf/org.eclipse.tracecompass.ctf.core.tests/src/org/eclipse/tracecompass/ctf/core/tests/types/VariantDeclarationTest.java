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

import java.nio.ByteBuffer;

import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.tracecompass.ctf.core.event.types.Definition;
import org.eclipse.tracecompass.ctf.core.event.types.Encoding;
import org.eclipse.tracecompass.ctf.core.event.types.EnumDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.EnumDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.IDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.StringDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StringDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.StructDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StructDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.VariantDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.VariantDefinition;
import org.eclipse.tracecompass.ctf.core.tests.shared.CtfTestTraceUtils;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>VariantDeclarationTest</code> contains tests for the class
 * <code>{@link VariantDeclaration}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class VariantDeclarationTest {

    private static final CtfTestTrace testTrace = CtfTestTrace.KERNEL;

    private VariantDeclaration fixture;

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        fixture = new VariantDeclaration();
    }

    private static IDefinitionScope createDefinitionScope() throws CTFException {
        StructDeclaration declaration = new StructDeclaration(8);
        VariantDeclaration variantDeclaration = new VariantDeclaration();
        variantDeclaration.addField("a", IntegerDeclaration.INT_32B_DECL);
        variantDeclaration.addField("b", IntegerDeclaration.INT_32L_DECL);
        variantDeclaration.setTag("a");

        EnumDeclaration enumDeclaration = new EnumDeclaration(IntegerDeclaration.UINT_8_DECL);
        enumDeclaration.add(0, 1, "a");
        enumDeclaration.add(2, 2, "b");
        declaration.addField("tag", enumDeclaration);
        declaration.addField("variant", variantDeclaration);
        EnumDefinition tagDef = new EnumDefinition(
                enumDeclaration,
                null,
                "tag",
                new IntegerDefinition(
                        IntegerDeclaration.UINT_8_DECL,
                        null,
                        "test",
                        0)
                );
        VariantDefinition variantDefinition = new VariantDefinition(
                variantDeclaration,
                CtfTestTraceUtils.getTrace(testTrace),
                tagDef,
                "tag",
                "tag",
                new StringDefinition(
                        StringDeclaration.getStringDeclaration(Encoding.UTF8),
                        null,
                        "f",
                        "tag"
                ));

        IDefinitionScope definitionScope = new StructDefinition(
                declaration,
                variantDefinition,
                "",
                new Definition[] { tagDef, variantDefinition }
                );

        return definitionScope;
    }

    /**
     * Run the VariantDeclaration() constructor test.
     */
    @Test
    public void testVariantDeclaration() {
        assertNotNull(fixture);
        assertEquals(false, fixture.isTagged());
        String left = "[declaration] variant[";
        assertEquals(left, fixture.toString().substring(0, left.length()));
    }

    /**
     * Run the void addField(String,Declaration) method test.
     */
    @Test
    public void testAddField() {
        fixture.setTag("");
        String tag = "";
        IDeclaration declaration = StringDeclaration.getStringDeclaration(Encoding.UTF8);
        fixture.addField(tag, declaration);
    }

    /**
     * Run the VariantDefinition createDefinition(DefinitionScope,String) method
     * test.
     *
     * @throws CTFException
     *             Should not happen
     */
    @Test
    public void testCreateDefinition() throws CTFException {
        fixture.setTag("tag");
        fixture.addField("a", IntegerDeclaration.UINT_64B_DECL);
        IDefinitionScope definitionScope = createDefinitionScope();
        String fieldName = "";
        ByteBuffer allocate = ByteBuffer.allocate(100);
        BitBuffer bb = new BitBuffer(allocate);
        VariantDefinition result = fixture.createDefinition(definitionScope, fieldName, bb);

        assertNotNull(result);
    }

    /**
     * Run the boolean hasField(String) method test.
     */
    @Test
    public void testHasField() {
        fixture.setTag("");
        String tag = "";
        boolean result = fixture.hasField(tag);

        assertEquals(false, result);
    }

    /**
     * Run the boolean isTagged() method test.
     */
    @Test
    public void testIsTagged() {
        fixture.setTag("");
        boolean result = fixture.isTagged();

        assertEquals(true, result);
    }

    /**
     * Run the boolean isTagged() method test.
     */
    @Test
    public void testIsTagged_null() {
        fixture.setTag((String) null);
        boolean result = fixture.isTagged();

        assertEquals(false, result);
    }

    /**
     * Run the void setTag(String) method test.
     */
    @Test
    public void testSetTag() {
        fixture.setTag("");
        String tag = "";
        fixture.setTag(tag);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString() {
        fixture.setTag("");
        String result = fixture.toString();
        String left = "[declaration] variant[";
        String right = result.substring(0, left.length());

        assertEquals(left, right);
    }

    /**
     * Test the hashcode
     */
    @Test
    public void hashcodeTest() {
        VariantDeclaration a = new VariantDeclaration();
        assertEquals(fixture.hashCode(), a.hashCode());

        VariantDeclaration b = new VariantDeclaration();
        b.addField("hi", StringDeclaration.getStringDeclaration(Encoding.UTF8));
        VariantDeclaration c = new VariantDeclaration();
        c.addField("hi", StringDeclaration.getStringDeclaration(Encoding.UTF8));
        assertEquals(b.hashCode(), c.hashCode());
    }

    /**
     * Test the equals
     */
    @Test
    public void equalsTest() {
        VariantDeclaration a = new VariantDeclaration();
        VariantDeclaration b = new VariantDeclaration();
        b.addField("hi", StringDeclaration.getStringDeclaration(Encoding.UTF8));
        VariantDeclaration c = new VariantDeclaration();
        c.addField("hi", StringDeclaration.getStringDeclaration(Encoding.UTF8));
        VariantDeclaration d = new VariantDeclaration();
        assertNotEquals(a, null);
        assertNotEquals(a, new Object());
        assertNotEquals(a, b);
        assertNotEquals(a, c);
        assertEquals(a, d);
        assertEquals(a, a);
        assertEquals(b, c);
        assertNotEquals(b, a);
        assertNotEquals(c, a);
        assertEquals(d, a);
        assertEquals(c, b);
        b.setTag("hi");
        assertNotEquals(b, c);
        c.setTag("Hello");
        assertNotEquals(b, c);
        c.setTag("hi");
        assertEquals(b, c);
        b.addField("hello", IntegerDeclaration.INT_32B_DECL);
        d.addField("hello", IntegerDeclaration.INT_32B_DECL);
        d.addField("hi", StringDeclaration.getStringDeclaration(Encoding.UTF8));
        d.setTag("hi");
        assertEquals(b, d);
        assertEquals(d, b);
    }

    /**
     * Test the equals out of order
     */
    @Test
    public void equalsOutOfOrderTest() {
        VariantDeclaration a = new VariantDeclaration();
        VariantDeclaration b = new VariantDeclaration();
        b.addField("hi", StringDeclaration.getStringDeclaration(Encoding.UTF8));
        b.addField("hello", new VariantDeclaration());
        a.addField("hello", new VariantDeclaration());
        a.addField("hi", StringDeclaration.getStringDeclaration(Encoding.UTF8));
        assertEquals(b, a);
    }

    /**
     * Test the equals out of order
     */
    @Test
    public void equalsAddTwiceTest() {
        VariantDeclaration a = new VariantDeclaration();
        VariantDeclaration b = new VariantDeclaration();
        b.addField("hi", StringDeclaration.getStringDeclaration(Encoding.UTF8));
        a.addField("hi", StringDeclaration.getStringDeclaration(Encoding.UTF8));
        assertEquals(b, a);
        b.addField("hi", new VariantDeclaration());
        assertNotEquals(b, a);
    }

}