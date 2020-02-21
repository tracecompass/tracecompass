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

package org.eclipse.tracecompass.ctf.core.tests.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.types.Definition;
import org.eclipse.tracecompass.ctf.core.event.types.Encoding;
import org.eclipse.tracecompass.ctf.core.event.types.IDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.StringDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StringDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.StructDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StructDefinition;
import org.eclipse.tracecompass.internal.ctf.core.event.types.SequenceDeclaration;
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

    @NonNull
    private static final String fieldName = "id";

    /**
     * Run the CTFEventField parseField(Definition,String) method test.
     *
     * @throws CTFException
     */
    @Test
    public void testParseField_complex() throws CTFException {
        int len = 32;
        IntegerDeclaration id = IntegerDeclaration.createDeclaration(
                len,
                false,
                len,
                ByteOrder.LITTLE_ENDIAN,
                Encoding.ASCII,
                "",
                len);
        String lengthName = "LengthName";
        StructDeclaration structDec = new StructDeclaration(0);
        structDec.addField(lengthName, id);
        StructDefinition structDef = new StructDefinition(
                structDec,
                null,
                lengthName,
                new Definition[] {
                        new IntegerDefinition(
                                id,
                                null,
                                lengthName,
                                32)
                });

        SequenceDeclaration sd = new SequenceDeclaration(lengthName, id);
        ByteBuffer byb = testMemory(ByteBuffer.allocate(1024));
        for (int i = 0; i < 1024; i++) {
            byb.put((byte) i);
        }
        BitBuffer bb = new BitBuffer(byb);
        IDefinition fieldDef = sd.createDefinition(structDef, "fff-fffield", bb);

        assertNotNull(fieldDef);
    }

    @NonNull
    private static ByteBuffer testMemory(ByteBuffer buffer) {
        if (buffer == null) {
            throw new IllegalStateException("Failed to allocate memory");
        }
        return buffer;
    }

    /**
     * Run the CTFEventField parseField(Definition,String) method test.
     *
     * @throws CTFException
     */
    @Test
    public void testParseField_simple() throws CTFException {
        final StringDeclaration elemType = StringDeclaration.getStringDeclaration(Encoding.UTF8);
        byte[] bytes = { 'T', 'e', 's', 't', '\0' };
        ByteBuffer bb = testMemory(ByteBuffer.wrap(bytes));
        IDefinition fieldDef = elemType.createDefinition(null, fieldName, new BitBuffer(bb));

        assertNotNull(fieldDef);
    }

    /**
     * Run the CTFEventField parseField(Definition,String) method test.
     */
    @Test
    public void testParseField_simple2() {
        IntegerDefinition fieldDef = new IntegerDefinition(
                IntegerDeclaration.createDeclaration(1, false, 1, ByteOrder.BIG_ENDIAN,
                        Encoding.ASCII, "", 8), null, fieldName, 1L);

        assertNotNull(fieldDef);
    }

    /**
     *
     */
    @Test
    public void testParseField_simple3() {
        StringDefinition fieldDef = new StringDefinition(
                StringDeclaration.getStringDeclaration(Encoding.UTF8), null, fieldName, "Hello World");

        String other = "\"Hello World\"";
        assertNotNull(fieldDef);
        assertEquals(fieldDef.toString(), other);
    }

}