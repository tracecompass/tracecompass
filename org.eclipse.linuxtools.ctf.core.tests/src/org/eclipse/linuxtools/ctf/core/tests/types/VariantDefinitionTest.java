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

import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.ctf.core.event.types.ArrayDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.ArrayDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.Encoding;
import org.eclipse.linuxtools.ctf.core.event.types.EnumDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.EnumDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.FloatDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDefinition;
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
 * The class <code>VariantDefinitionTest</code> contains tests for the class
 * <code>{@link VariantDefinition}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class VariantDefinitionTest {

    private VariantDefinition fixture;

    StructDefinition structDefinition;
    private static final String TEST_STRUCT_ID = "testStruct";

    private static final String ENUM_7 = "g";
    private static final String ENUM_6 = "f";
    private static final String ENUM_5 = "e";
    private static final String ENUM_4 = "d";
    private static final String ENUM_3 = "c";
    private static final String ENUM_2 = "b";
    private static final String ENUM_1 = "a";

    private static final String TAG_ID = "a";

//    private static final String INT_ID = "_id";
//    private static final String STRING_ID = "_args";
//    private static final String ENUM_ID = "_enumArgs";
//    private static final String SEQUENCE_ID = "_seq";

    private static final String LENGTH_SEQ = "_len";
    private static final String VAR_FIELD_NAME = "var";
    private static final String ENUM_8 = null;

    /**
     * Perform pre-test initialization.
     *
     * Not sure it needs to be that complicated, oh well...
     */
    @Before
    public void setUp() {
        StructDeclaration sDec = new StructDeclaration(12);
        StructDeclaration smallStruct = new StructDeclaration(8);
        IntegerDeclaration iDec = new IntegerDeclaration(32, false, 32, ByteOrder.BIG_ENDIAN, Encoding.NONE, null, 8);
        IntegerDeclaration lenDec = new IntegerDeclaration(8, false, 8, ByteOrder.BIG_ENDIAN, Encoding.NONE, null, 8);
        StringDeclaration strDec = new StringDeclaration();
        EnumDeclaration enDec = new EnumDeclaration(iDec);
//        SequenceDeclaration seqDec = new SequenceDeclaration(LENGTH_SEQ, iDec);
        VariantDeclaration varDec = new VariantDeclaration();
        EnumDeclaration tagDec = new EnumDeclaration(iDec);
        ArrayDeclaration arrDec = new ArrayDeclaration(2, iDec);
        FloatDeclaration fDec = new FloatDeclaration(8, 24, ByteOrder.BIG_ENDIAN, 8);
        tagDec.add(0, 1, ENUM_1);
        tagDec.add(2, 3, ENUM_2);
        tagDec.add(4, 5, ENUM_3);
        //tagDec.add(6, 7, ENUM_4); // this should not work
        tagDec.add(8, 9, ENUM_5);
        tagDec.add(10, 11, ENUM_6);
        tagDec.add(12, 13, ENUM_7);
        varDec.addField(ENUM_4, lenDec);
        varDec.addField(ENUM_7, fDec);
        varDec.addField(ENUM_6, smallStruct);
        varDec.addField(ENUM_5, enDec);
        //varDec.addField(ENUM_4, seqDec);// this should not work
        varDec.addField(ENUM_3, arrDec);
        varDec.addField(ENUM_2, iDec);
        varDec.addField(ENUM_1, strDec);

        sDec.addField(TAG_ID, tagDec);
        sDec.addField(LENGTH_SEQ, lenDec);
//        sDec.addField(SEQUENCE_ID, seqDec);

        sDec.addField(VAR_FIELD_NAME, varDec);
        varDec.setTag(TAG_ID);

        structDefinition = sDec.createDefinition(null, TEST_STRUCT_ID);
        fixture = (VariantDefinition) structDefinition.getDefinitions().get(VAR_FIELD_NAME);
    }

    /**
     * Run the VariantDefinition(VariantDeclaration,DefinitionScope,String)
     */
    @Test
    public void testVariantDefinition() {
        VariantDeclaration declaration = new VariantDeclaration();
        declaration.setTag("");
        VariantDeclaration variantDeclaration = new VariantDeclaration();
        variantDeclaration.setTag("");
        VariantDefinition variantDefinition = new VariantDefinition(
                variantDeclaration, structDefinition, "");
        IDefinitionScope definitionScope = new StructDefinition(
                new StructDeclaration(1L), variantDefinition, "");
        String fieldName = "";

        VariantDefinition result = new VariantDefinition(declaration,
                definitionScope, fieldName);
        assertNotNull(result);
    }

    /**
     * Run the Definition getCurrentField() method test.
     */
    @Test
    public void testGetCurrentField() {
        Definition result = fixture.getCurrentField();
        assertNull(result);
        fixture.setCurrentField(ENUM_1);
        result = fixture.getCurrentField();
        assertNotNull(result);
    }

    /**
     * Run the String getCurrentFieldName() method test.
     */
    @Test
    public void testGetCurrentFieldName() {
        fixture.setCurrentField(ENUM_1);
        String result = fixture.getCurrentFieldName();
        assertNotNull(result);
    }

    /**
     * Run the VariantDeclaration getDeclaration() method test.
     */
    @Test
    public void testGetDeclaration() {
        VariantDeclaration result = fixture.getDeclaration();
        assertNotNull(result);
    }

    /**
     * Run the HashMap<String, Definition> getDefinitions() method test.
     */
    @Test
    public void testGetDefinitions() {
        Map<String, Definition> result = fixture.getDefinitions();
        assertNotNull(result);
    }

    /**
     * Run the String getPath() method test.
     */
    @Test
    public void testGetPath() {
        String result = fixture.getPath();
        assertNotNull(result);
    }

    /**
     * Run the EnumDefinition getTagDefinition() method test.
     */
    @Test
    public void testGetTagDefinition() {
        EnumDefinition result = fixture.getTagDefinition();
        assertNotNull(result);
    }

    /**
     * Run the ArrayDefinition lookupArray(String) method test.
     */
    @Test
    public void testLookupArray() {
        ArrayDefinition result = fixture.lookupArray(ENUM_3);
        assertNotNull(result);
    }

    /**
     * Run the Definition lookupDefinition(String) method test.
     */
    @Test
    public void testLookupDefinition() {
        Definition result = fixture.lookupDefinition(ENUM_1);
        assertNotNull(result);
    }

    /**
     * Run the EnumDefinition lookupEnum(String) method test.
     */
    @Test
    public void testLookupEnum() {
        EnumDefinition result = fixture.lookupEnum(ENUM_5);
        assertNotNull(result);
    }

    /**
     * Run the IntegerDefinition lookupInteger(String) method test.
     */
    @Test
    public void testLookupInteger() {
        IntegerDefinition result = fixture.lookupInteger(ENUM_2);
        assertNotNull(result);
    }

    /**
     * Run the SequenceDefinition lookupSequence(String) method test.
     */
    @Test
    public void testLookupSequence_1() {
        SequenceDefinition result = fixture.lookupSequence(ENUM_4);
        assertNull(result);
    }

    /**
     * Run the StringDefinition lookupString(String) method test.
     */
    @Test
    public void testLookupString() {
        StringDefinition result = fixture.lookupString(ENUM_1);
        assertNotNull(result);
    }

    /**
     * Run the StructDefinition lookupStruct(String) method test.
     */
    @Test
    public void testLookupStruct() {
        StructDefinition result = fixture.lookupStruct(ENUM_6);
        assertNotNull(result);
    }

    /**
     * Run the VariantDefinition lookupVariant(String) method test.
     */
    @Test
    public void testLookupVariant() {
        VariantDefinition result = fixture.lookupVariant(ENUM_8);
        assertNull(result);
    }

    /**
     * Run the void setCurrentField(String) method test.
     */
    @Test
    public void testSetCurrentField() {
        fixture.setCurrentField(ENUM_1);
    }

    /**
     * Run the void setDeclaration(VariantDeclaration) method test.
     */
    @Test
    public void testSetDeclaration() {
        VariantDeclaration declaration = new VariantDeclaration();
        fixture.setDeclaration(declaration);
    }

    /**
     * Run the void setDefinitions(HashMap<String,Definition>) method test.
     */
    @Test
    public void testSetDefinitions() {
        HashMap<String, Definition> definitions = new HashMap<>();
        fixture.setDefinitions(definitions);
    }

    /**
     * Run the void setTagDefinition(EnumDefinition) method test.
     */
    @Test
    public void testSetTagDefinition(){
        VariantDeclaration vDecl;
        VariantDefinition vDef;
        StructDefinition structDef;
        EnumDefinition tagDefinition;
        String fName = "";

        vDecl = new VariantDeclaration();
        vDecl.setTag(fName);
        vDef = new VariantDefinition(vDecl, structDefinition, fName);
        structDef = new StructDefinition(new StructDeclaration(1L), vDef, fName);
        tagDefinition = new EnumDefinition(new EnumDeclaration(
                new IntegerDeclaration(1, false, 1, ByteOrder.BIG_ENDIAN,
                        Encoding.ASCII, fName, 8)), structDef, fName);

        fixture.setTagDefinition(tagDefinition);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString() {
        String result = fixture.toString();
        assertEquals("{ null = null }", result);

        fixture.setCurrentField(ENUM_2);
        result = fixture.toString();
        assertEquals("{ b = 0 }", result);
    }
}
