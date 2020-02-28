/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Matthew Khouzam - Initial generation with CodePro tools
 *   Alexandre Montplaisir - Clean up, consolidate redundant tests
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.tests.event;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.types.Encoding;
import org.eclipse.tracecompass.ctf.core.event.types.EnumDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.FloatDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.FloatDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.IDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StringDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StructDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StructDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.VariantDeclaration;
import org.eclipse.tracecompass.internal.ctf.core.event.types.ArrayDeclaration;
import org.eclipse.tracecompass.internal.ctf.core.event.types.SequenceDeclaration;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.ctf.core.CtfEnumPair;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEventField;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>CtfTmfEventFieldTest</code> contains tests for the class
 * <code>{@link CtfTmfEventField}</code>.
 *
 * @author Matthew Khouzam
 * @version 1.0
 */
public class CtfTmfEventFieldTest {

    private static final @NonNull String ROOT = "root";
    private static final @NonNull String SEQ = "seq";
    private static final @NonNull String ARRAY_STR = "array_str";
    private static final @NonNull String ARRAY_FLOAT = "array_float";
    private static final @NonNull String ARRAY_INT = "array_int";
    private static final @NonNull String ARRAY_STRUCT = "array_struct";
    private static final @NonNull String ARRAY_VARIANT = "array_variant";
    private static final @NonNull String ARRAY_ENUM = "array_enum";
    private static final @NonNull String STR = "str";
    private static final @NonNull String FLOAT = "float";
    private static final @NonNull String LEN = "len";
    private static final @NonNull String INT = "int";
    private static final @NonNull String NAME = "test";
    private static final @NonNull String STRUCT = "struct";
    private static final @NonNull String VARIANT = "variant";
    private static final @NonNull String ENUM = "enum";

    private static final byte TEST_NUMBER = 2;
    private static final @NonNull String TEST_STRING = "two";

    private static final int ARRAY_SIZE = 2;

    private StructDefinition fixture;

    /**
     * Perform pre-test initialization.
     *
     * @throws UnsupportedEncodingException
     *             Thrown when UTF-8 encoding is not available.
     * @throws CTFException
     *             error
     */
    @Before
    public void setUp() throws UnsupportedEncodingException, CTFException {
        final byte[] testStringBytes = TEST_STRING.getBytes("UTF-8");

        int capacity = 2048;
        ByteBuffer bb = ByteBuffer.allocateDirect(capacity);

        StructDeclaration sDec = new StructDeclaration(1l);
        StringDeclaration strDec = StringDeclaration.getStringDeclaration(Encoding.UTF8);
        IntegerDeclaration byteDec = IntegerDeclaration.UINT_8_DECL;
        IntegerDeclaration intDec = IntegerDeclaration.INT_8_DECL;
        FloatDeclaration flDec = new FloatDeclaration(8, 24,
                ByteOrder.BIG_ENDIAN, 8);
        SequenceDeclaration seqDec = new SequenceDeclaration(LEN, byteDec);
        StructDeclaration structDec = new StructDeclaration(8);
        EnumDeclaration enumDec = new EnumDeclaration(byteDec);
        VariantDeclaration varDec = new VariantDeclaration();
        ArrayDeclaration arrStrDec = new ArrayDeclaration(ARRAY_SIZE, strDec);
        ArrayDeclaration arrFloatDec = new ArrayDeclaration(ARRAY_SIZE, flDec);
        ArrayDeclaration arrIntDec = new ArrayDeclaration(ARRAY_SIZE, intDec);
        ArrayDeclaration arrStructDec = new ArrayDeclaration(ARRAY_SIZE, structDec);
        ArrayDeclaration arrVariantDec = new ArrayDeclaration(ARRAY_SIZE, varDec);
        ArrayDeclaration arrEnumDec = new ArrayDeclaration(ARRAY_SIZE, enumDec);

        sDec.addField(INT, byteDec);
        bb.put(TEST_NUMBER);

        sDec.addField(ARRAY_INT, arrIntDec);
        for (int i = 0; i < ARRAY_SIZE; ++i) {
            bb.put(TEST_NUMBER);
        }

        sDec.addField(LEN, byteDec);
        bb.put(TEST_NUMBER);

        sDec.addField(FLOAT, flDec);
        bb.putFloat(TEST_NUMBER);

        sDec.addField(ARRAY_FLOAT, arrFloatDec);
        for (int i = 0; i < ARRAY_SIZE; ++i) {
            bb.putFloat(TEST_NUMBER);
        }

        sDec.addField(STR, strDec);
        bb.put(testStringBytes);
        bb.put((byte) 0);

        sDec.addField(ARRAY_STR, arrStrDec);
        for (int i = 0; i < ARRAY_SIZE; ++i) {
            bb.put(testStringBytes);
            bb.put((byte) 0);
        }

        sDec.addField(SEQ, seqDec);
        bb.put(TEST_NUMBER);
        bb.put(TEST_NUMBER);

        structDec.addField(STR, strDec);
        structDec.addField(INT, byteDec);
        sDec.addField(STRUCT, structDec);
        bb.put(testStringBytes);
        bb.put((byte) 0);
        bb.put(TEST_NUMBER);

        sDec.addField(ARRAY_STRUCT, arrStructDec);
        for (int i = 0; i < ARRAY_SIZE; ++i) {
            bb.put(testStringBytes);
            bb.put((byte) 0);
            bb.put(TEST_NUMBER);
        }

        enumDec.add(0, 1, LEN);
        enumDec.add(2, 3, FLOAT);
        sDec.addField(ENUM, enumDec);
        bb.put(TEST_NUMBER);

        sDec.addField(ARRAY_ENUM, arrEnumDec);
        for (int i = 0; i < ARRAY_SIZE; ++i) {
            bb.put(TEST_NUMBER);
        }

        varDec.addField(LEN, byteDec);
        varDec.addField(FLOAT, flDec);
        varDec.setTag(ENUM);
        sDec.addField(VARIANT, varDec);
        bb.putFloat(TEST_NUMBER);

        sDec.addField(ARRAY_VARIANT, arrVariantDec);
        for (int i = 0; i < ARRAY_SIZE; ++i) {
            bb.putFloat(TEST_NUMBER);
        }

        fixture = sDec.createDefinition(fixture, ROOT, new BitBuffer(bb));

    }

    /**
     * Run the CtfTmfEventField parseField(Definition,String) method test.
     */
    @Test
    public void testParseField_float() {
        FloatDefinition fieldDef = (FloatDefinition) fixture.lookupDefinition(FLOAT);
        CtfTmfEventField result = CtfTmfEventField.parseField(fieldDef, "_" + NAME);
        assertEquals("test=2.0", result.toString());
    }

    /**
     * Run the CtfTmfEventField parseField(Definition,String) method test for an
     * array of floats field.
     */
    @Test
    public void testParseField_array_float() {
        IDefinition fieldDef = fixture.lookupArrayDefinition(ARRAY_FLOAT);
        CtfTmfEventField result = CtfTmfEventField.parseField(fieldDef, NAME);
        assertEquals("test=[2.0, 2.0]", result.toString());
    }

    /**
     * Run the CtfTmfEventField parseField(Definition,String) method test.
     */
    @Test
    public void testParseField_int() {
        IDefinition fieldDef = fixture.lookupDefinition(INT);
        CtfTmfEventField result = CtfTmfEventField.parseField(fieldDef, NAME);
        assertEquals("test=2", result.toString());
    }

    /**
     * Run the CtfTmfEventField parseField(Definition,String) method test for an
     * array of integers field.
     */
    @Test
    public void testParseField_array_int() {
        IDefinition fieldDef = fixture.lookupArrayDefinition(ARRAY_INT);
        CtfTmfEventField result = CtfTmfEventField.parseField(fieldDef, NAME);
        assertEquals("test=[2, 2]", result.toString());
    }

    /**
     * Run the CtfTmfEventField parseField(Definition,String) method test.
     */
    @Test
    public void testParseField_sequence() {
        IDefinition fieldDef = fixture.lookupDefinition(SEQ);
        CtfTmfEventField result = CtfTmfEventField.parseField(fieldDef, NAME);
        assertEquals("test=[2, 2]", result.toString());
    }

    /**
     * Run the CtfTmfEventField parseField(Definition,String) method test.
     */
    @Test
    public void testParseField_sequence_value() {
        IDefinition fieldDef = fixture.lookupDefinition(SEQ);
        CtfTmfEventField result = CtfTmfEventField.parseField(fieldDef, NAME);
        long[] values = (long[]) result.getValue();
        long[] expected = new long[] { 2, 2 };
        assertArrayEquals(expected, values);
    }

    /**
     * Run the CtfTmfEventField parseField(Definition,String) method test.
     */
    @Test
    public void testParseField_string() {
        IDefinition fieldDef = fixture.lookupDefinition(STR);
        CtfTmfEventField result = CtfTmfEventField.parseField(fieldDef, NAME);
        assertEquals("test=two", result.toString());
    }

    /**
     * Run the CtfTmfEventField parseField(Definition,String) method test for an
     * array of strings field.
     */
    @Test
    public void testParseField_array_string() {
        IDefinition fieldDef = fixture.lookupArrayDefinition(ARRAY_STR);
        CtfTmfEventField result = CtfTmfEventField.parseField(fieldDef, NAME);
        assertEquals("test=[two, two]", result.toString());
    }

    /**
     * Run the CtfTmfEventField parseField(Definition,String) method test.
     */
    @Test
    public void testParseField_struct() {
        IDefinition fieldDef = fixture.lookupDefinition(STRUCT);
        CtfTmfEventField result = CtfTmfEventField.parseField(fieldDef, NAME);
        assertEquals("test=[str=two, int=2]", result.toString());
    }

    /**
     * Run the CtfTmfEventField parseField(Definition,String) method test for an
     * array of structs field.
     */
    @Test
    public void testParseField_array_struct() {
        IDefinition fieldDef = fixture.lookupArrayDefinition(ARRAY_STRUCT);
        CtfTmfEventField result = CtfTmfEventField.parseField(fieldDef, NAME);
        assertEquals("test=[[str=two, int=2], [str=two, int=2]]", result.toString());
    }

    /**
     * Run the CtfTmfEventField parseField(Definition,String) method test.
     *
     * Also test the {@link ITmfEventField#getFieldValue(Class)} for this
     * field type
     */
    @Test
    public void testParseField_enum() {
        IDefinition fieldDef = fixture.lookupDefinition(ENUM);
        CtfTmfEventField result = CtfTmfEventField.parseField(fieldDef, NAME);
        assertEquals("test=float", result.toString());

        // Test the value conversion
        assertEquals(new CtfEnumPair(FLOAT, (long) TEST_NUMBER), result.getFieldValue(CtfEnumPair.class));
        assertEquals(Long.valueOf(TEST_NUMBER), result.getFieldValue(Long.class));
        assertEquals(Integer.valueOf(TEST_NUMBER), result.getFieldValue(Integer.class));
        assertEquals(Double.valueOf(TEST_NUMBER), result.getFieldValue(Double.class));
        assertEquals(FLOAT, result.getFieldValue(String.class));
        assertNull(result.getFieldValue(IDefinition.class));
    }

    /**
     * Run the CtfTmfEventField parseField(Definition,String) method test for an
     * array of enums field.
     */
    @Test
    public void testParseField_array_enum() {
        IDefinition fieldDef = fixture.lookupArrayDefinition(ARRAY_ENUM);
        CtfTmfEventField result = CtfTmfEventField.parseField(fieldDef, NAME);
        assertEquals("test=[float, float]", result.toString());
    }

    /**
     * Run the CtfTmfEventField parseField(Definition,String) method test.
     */
    @Test
    public void testParseField_variant() {
        IDefinition fieldDef = fixture.lookupDefinition(VARIANT);
        CtfTmfEventField result = CtfTmfEventField.parseField(fieldDef, NAME);
        assertEquals("test=float=2.0", result.toString());
    }

    /**
     * Run the CtfTmfEventField parseField(Definition,String) method test for an
     * array of variants field.
     */
    @Test
    public void testParseField_array_variant() {
        IDefinition fieldDef = fixture.lookupArrayDefinition(ARRAY_VARIANT);
        CtfTmfEventField result = CtfTmfEventField.parseField(fieldDef, NAME);
        assertEquals("test=[float=2.0, float=2.0]", result.toString());
    }
}
