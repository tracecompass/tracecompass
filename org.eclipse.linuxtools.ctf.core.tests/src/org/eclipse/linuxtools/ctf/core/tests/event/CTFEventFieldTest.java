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

package org.eclipse.linuxtools.ctf.core.tests.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.event.types.ArrayDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.ArrayDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.Encoding;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.SequenceDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.SequenceDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StringDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StringDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDefinition;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.junit.Test;

/**
 * The class <code>CTFEventFieldTest</code> contains tests for the class
 * <code>{@link CTFEventField}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
@SuppressWarnings("javadoc")
public class CTFEventFieldTest {

    private static final String fieldName = "id";

    /**
     * Run the CTFEventField parseField(Definition,String) method test.
     * @throws CTFReaderException
     */
    @Test
    public void testParseField_complex() throws CTFReaderException {
        int len = 32;
        IntegerDeclaration id = new IntegerDeclaration(len, false, len,
                ByteOrder.LITTLE_ENDIAN, Encoding.ASCII, null, 32);
        String lengthName = "LengthName";
        StructDeclaration structDec = new StructDeclaration(0);
        structDec.addField(lengthName, id);
        StructDefinition structDef = new StructDefinition(structDec, null,
                lengthName);

        structDef.lookupInteger(lengthName).setValue(32);
        SequenceDeclaration sd = new SequenceDeclaration(lengthName, id);
        Definition fieldDef = new SequenceDefinition(sd, structDef, "TestX");
        ByteBuffer byb = ByteBuffer.allocate(1024);
        for (int i = 0; i < 1024; i++) {
            byb.put((byte) i);
        }
        BitBuffer bb = new BitBuffer(byb);
        fieldDef.read(bb);

        assertNotNull(fieldDef);
    }

    /**
     * Run the CTFEventField parseField(Definition,String) method test.
     * @throws CTFReaderException
     */
    @Test
    public void testParseField_simple() {
        final StringDeclaration elemType = new StringDeclaration();
        Definition fieldDef = elemType.createDefinition(null, fieldName);

        assertNotNull(fieldDef);
    }

    /**
     * Run the CTFEventField parseField(Definition,String) method test.
     */
    @Test
    public void testParseField_simple2() {
        IntegerDefinition fieldDef = new IntegerDefinition(
                new IntegerDeclaration(1, false, 1, ByteOrder.BIG_ENDIAN,
                        Encoding.ASCII, null, 8), null, fieldName);
        fieldDef.setValue(1L);

        assertNotNull(fieldDef);
    }

    /**
     *
     */
    @Test
    public void testParseField_simple3() {
        StringDefinition fieldDef = new StringDefinition(
                new StringDeclaration(), null, fieldName);
        fieldDef.setString(new StringBuilder("Hello World"));

        String other = "\"Hello World\"";
        assertNotNull(fieldDef);
        assertEquals(fieldDef.toString(), other);
    }

    /**
     * Run the CTFEventField parseField(Definition,String) method test.
     */
    @Test
    public void testParseField_manual() {
        Definition fieldDef = new ArrayDefinition(new ArrayDeclaration(20,
                new IntegerDeclaration(8, false, 8, null, Encoding.UTF8, null, 8)),
                null, fieldName);
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[0]).setValue('H');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[1]).setValue('e');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[2]).setValue('l');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[3]).setValue('l');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[4]).setValue('o');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[5]).setValue(' ');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[6]).setValue('W');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[7]).setValue('o');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[8]).setValue('r');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[9]).setValue('l');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[10]).setValue('d');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[11]).setValue(0);

        assertNotNull(fieldDef);
    }

    /**
     * Run the CTFEventField parseField(Definition,String) method test.
     */
    @Test
    public void testParseField_manual2() {
        Definition fieldDef = new ArrayDefinition(new ArrayDeclaration(12,
                new IntegerDeclaration(32, false, 32, null, null, null, 8)), null,
                fieldName);
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[0]).setValue('H');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[1]).setValue('e');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[2]).setValue('l');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[3]).setValue('l');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[4]).setValue('o');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[5]).setValue(' ');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[6]).setValue('W');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[7]).setValue('o');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[8]).setValue('r');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[9]).setValue('l');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[10]).setValue('d');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[11]).setValue(0);

        assertNotNull(fieldDef);
        String other = "[ 72, 101, 108, 108, 111, 32, 87, 111, 114, 108, 100, 0 ]";
        assertEquals(other, fieldDef.toString());
    }
}