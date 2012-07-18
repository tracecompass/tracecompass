/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial generation with CodePro tools
 *   Alexandre Montplaisir - Clean up, consolidate redundant tests
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.ctfadaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.ByteOrder;

import org.eclipse.linuxtools.ctf.core.event.types.ArrayDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.Encoding;
import org.eclipse.linuxtools.ctf.core.event.types.FloatDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.FloatDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.SequenceDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StringDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDefinition;
import org.eclipse.linuxtools.internal.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEventField;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>CtfTmfEventFieldTest</code> contains tests for the class
 * <code>{@link CtfTmfEventField}</code>.
 *
 * @author ematkho
 * @version 1.0
 */
public class CtfTmfEventFieldTest {

    private static final String ROOT = "root"; //$NON-NLS-1$
    private static final String SEQ = "seq"; //$NON-NLS-1$
    private static final String ARRAY = "array"; //$NON-NLS-1$
    private static final String STR = "str"; //$NON-NLS-1$
    private static final String FLOAT = "float"; //$NON-NLS-1$
    private static final String LEN = "len"; //$NON-NLS-1$
    private static final String INT = "int"; //$NON-NLS-1$
    private static final String NAME = "test"; //$NON-NLS-1$

    private StructDefinition fixture;

    /**
     * Launch the test.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(CtfTmfEventFieldTest.class);
    }

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        StructDeclaration sDec = new StructDeclaration(1l);
        StringDeclaration strDec = new StringDeclaration();
        IntegerDeclaration intDec = new IntegerDeclaration(8, false, 8,
                ByteOrder.BIG_ENDIAN, Encoding.NONE, null, 8);
        FloatDeclaration flDec = new FloatDeclaration(8, 24,
                ByteOrder.BIG_ENDIAN, 8);
        ArrayDeclaration arrDec = new ArrayDeclaration(2, intDec);
        SequenceDeclaration seqDec = new SequenceDeclaration(LEN, intDec);
        sDec.addField(INT, intDec);
        sDec.addField(LEN, intDec);
        sDec.addField(FLOAT, flDec);
        sDec.addField(STR, strDec);
        sDec.addField(ARRAY, arrDec);
        sDec.addField(SEQ, seqDec);
        fixture = sDec.createDefinition(fixture, ROOT);
        int capacity = 1024;
        java.nio.ByteBuffer bb = java.nio.ByteBuffer.allocateDirect(capacity);
        for (int i = 0; i < capacity; i++) {
            bb.put((byte) 2);
        }
        bb.position(20);
        bb.put((byte) 0);
        bb.position(0);
        fixture.read(new BitBuffer(bb));
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        // Add additional tear down code here
    }

    /**
     * Run the CtfTmfEventField parseField(Definition,String) method test.
     */
    @Test
    public void testParseField_float() {
        FloatDefinition fieldDef;
        fieldDef = (FloatDefinition) fixture.lookupDefinition(FLOAT);
        CtfTmfEventField result = CtfTmfEventField.parseField(fieldDef, "_"+NAME); //$NON-NLS-1$
        String result2 = CtfTmfEventField.copyFrom(result).toString();
        assertEquals( result2, "test=9.551467814359616E-38"); //$NON-NLS-1$
    }

    /**
     * Run the CtfTmfEventField parseField(Definition,String) method test.
     */
    @Test
    public void testParseField_array() {
        CtfTmfEventField result;
        result = CtfTmfEventField.parseField(fixture.lookupArray(ARRAY), NAME);
        String result2 = CtfTmfEventField.copyFrom(result).toString();
        assertEquals( result2, "test={ 2, 2}"); //$NON-NLS-1$
    }

    /**
     * Run the CtfTmfEventField parseField(Definition,String) method test.
     */
    @Test
    public void testParseField_int() {
        Definition fieldDef = fixture.lookupDefinition(INT);
        CtfTmfEventField result = CtfTmfEventField.parseField(fieldDef, NAME);
        String result2 =CtfTmfEventField.copyFrom(result).toString();
        assertEquals( result2, "test=02"); //$NON-NLS-1$
    }

    /**
     * Run the CtfTmfEventField parseField(Definition,String) method test.
     */
    @Test
    public void testParseField_sequence() {
        Definition fieldDef = fixture.lookupDefinition(SEQ);
        CtfTmfEventField result = CtfTmfEventField.parseField(fieldDef, NAME);
        String result2 =CtfTmfEventField.copyFrom(result).toString();
        assertEquals( result2, "test={ 2, 2}"); //$NON-NLS-1$
    }

    /**
     * Run the CtfTmfEventField parseField(Definition,String) method test.
     */
    @Test
    public void testParseField_string() {
        Definition fieldDef = fixture.lookupDefinition(STR);
        CtfTmfEventField result = CtfTmfEventField.parseField(fieldDef, NAME);
        String result2 =CtfTmfEventField.copyFrom(result).toString();
        assertEquals( result2, "test="); //$NON-NLS-1$
    }

    /**
     * Test the clone() method.
     */
    @Test
    public void testClone() {
        Definition fieldDef = fixture.lookupDefinition(STR);
        CtfTmfEventField result = CtfTmfEventField.parseField(fieldDef, NAME);
        assertNotNull(result.clone());
    }
}
