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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;

import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.event.types.ArrayDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.Encoding;
import org.eclipse.linuxtools.ctf.core.event.types.EnumDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.EnumDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.SequenceDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.SequenceDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StringDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StringDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.VariantDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.VariantDefinition;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>StructDefinitionTest</code> contains tests for the class
 * <code>{@link StructDefinition}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class StructDefinitionTest {

    private static final String TEST_STRUCT_ID = "testStruct";
    private static final String ENUM_2 = "y";
    private static final String ENUM_1 = "x";
    private static final String TAG_ID = "Tag";
    private static final String INT_ID = "_id";
    private static final String STRING_ID = "_args";
    private static final String ENUM_ID = "_enumArgs";
    private static final String SEQUENCE_ID = "_seq";
    private static final String LENGTH_SEQ = "_len";

    private StructDefinition fixture;
    private StructDefinition emptyStruct;
    private StructDefinition simpleStruct;
    private static final String VAR_FIELD_NAME = "SomeVariant";

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        StructDeclaration sDec = new StructDeclaration(12);
        IntegerDeclaration id = new IntegerDeclaration(32, false, 32, ByteOrder.BIG_ENDIAN, Encoding.NONE, null, 8);
        IntegerDeclaration lenDec = new IntegerDeclaration(8, false, 8, ByteOrder.BIG_ENDIAN, Encoding.NONE, null, 8);
        StringDeclaration sd = new StringDeclaration();
        EnumDeclaration ed = new EnumDeclaration(id);
        SequenceDeclaration seqDec = new SequenceDeclaration(LENGTH_SEQ, id);
        VariantDeclaration varDec = new VariantDeclaration();
        EnumDeclaration tagDec = new EnumDeclaration(id);
        tagDec.add(0, 1, ENUM_1);
        tagDec.add(2, 3, ENUM_2);
        varDec.addField(ENUM_2, id);
        varDec.addField(ENUM_1, sd);
        varDec.setTag(TAG_ID);
        sDec.addField(INT_ID, id);
        sDec.addField(STRING_ID, sd);
        sDec.addField(ENUM_ID, ed);
        sDec.addField(TAG_ID, tagDec);
        sDec.addField(LENGTH_SEQ, lenDec);
        sDec.addField(SEQUENCE_ID, seqDec);
        sDec.addField(VAR_FIELD_NAME, varDec);
        fixture = sDec.createDefinition(null, TEST_STRUCT_ID);
        EnumDefinition eDef = tagDec.createDefinition(fixture, TAG_ID);
        VariantDefinition vd = varDec.createDefinition(fixture,VAR_FIELD_NAME );
        vd.setTagDefinition(eDef);

        // Create an empty struct
        StructDeclaration esDec = new StructDeclaration(32);
        emptyStruct = esDec.createDefinition(null, TEST_STRUCT_ID);

        // Create a simple struct with two items
        StructDeclaration ssDec = new StructDeclaration(32);
        ssDec.addField(INT_ID, id);
        ssDec.addField(STRING_ID, sd);
        simpleStruct = ssDec.createDefinition(null, TEST_STRUCT_ID);
    }

    /**
     * Run the StructDeclaration getDeclaration() method test.
     */
    @Test
    public void testGetDeclaration() {
        StructDeclaration result = fixture.getDeclaration();
        assertNotNull(result);
    }

    /**
     * Run the HashMap<String, Definition> getDefinitions() method test.
     */
    @Test
    public void testGetDefinitions_1() {
        Map<String, Definition> result = fixture.getDefinitions();
        assertNotNull(result);
    }

    /**
     * Run the ArrayDefinition lookupArray(String) method test.
     */
    @Test
    public void testLookupArray() {
        String name = INT_ID;
        ArrayDefinition result = fixture.lookupArray(name);

        assertNull(result);
    }

    /**
     * Run the Definition lookupDefinition(String) method test.
     */
    @Test
    public void testLookupDefinition() {
        String lookupPath = "args";
        Definition result = fixture.lookupDefinition(lookupPath);

        assertNotNull(result);
    }

    /**
     * Run the EnumDefinition lookupEnum(String) method test.
     */
    @Test
    public void testLookupEnum() {
        String name = ENUM_ID;
        EnumDefinition result = fixture.lookupEnum(name);
        assertNotNull(result);
    }

    /**
     * Run the IntegerDefinition lookupInteger(String) method test.
     */
    @Test
    public void testLookupInteger_1() {
        String name = "_id";
        IntegerDefinition result = fixture.lookupInteger(name);
        assertNotNull(result);
    }

    /**
     * Run the IntegerDefinition lookupInteger(String) method test.
     */
    @Test
    public void testLookupInteger_2() {
        String name = VAR_FIELD_NAME;
        IntegerDefinition result = fixture.lookupInteger(name);
        assertNull(result);
    }

    /**
     * Run the SequenceDefinition lookupSequence(String) method test.
     */
    @Test
    public void testLookupSequence() {
        String name = SEQUENCE_ID;
        SequenceDefinition result = fixture.lookupSequence(name);
        assertNotNull(result);
    }

    /**
     * Run the StringDefinition lookupString(String) method test.
     */
    @Test
    public void testLookupString() {
        String name = VAR_FIELD_NAME;
        StringDefinition result = fixture.lookupString(name);

        assertNull(result);
    }

    /**
     * Run the StructDefinition lookupStruct(String) method test.
     */
    @Test
    public void testLookupStruct() {
        String name = VAR_FIELD_NAME;
        StructDefinition result = fixture.lookupStruct(name);

        assertNull(result);
    }

    /**
     * Run the VariantDefinition lookupVariant(String) method test.
     */
    @Test
    public void testLookupVariant() {
        String name = VAR_FIELD_NAME;
        VariantDefinition result = fixture.lookupVariant(name);

        assertNotNull(result);
    }

    /**
     * Run the void read(BitBuffer) method test.
     */
    @Test
    public void testRead_() {
        ByteBuffer bb = ByteBuffer.allocateDirect(128);
        bb.put((byte) 20);
        BitBuffer input = new BitBuffer(bb);

        fixture.read(input);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString() {
        String result = fixture.toString();
        assertNotNull(result);

        result = emptyStruct.toString();
        assertEquals("{  }", result);

        result = simpleStruct.toString();
        assertEquals("{ _id = 0, _args = \"\" }", result);
    }
}