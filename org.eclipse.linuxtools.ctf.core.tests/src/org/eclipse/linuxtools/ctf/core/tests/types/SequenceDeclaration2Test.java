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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.event.types.AbstractArrayDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.Encoding;
import org.eclipse.linuxtools.ctf.core.event.types.IDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StringDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDefinition;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.internal.ctf.core.event.types.SequenceDeclaration;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

/**
 * The class <code>SequenceDeclarationTest</code> contains tests for the class
 * <code>{@link SequenceDeclaration}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
@SuppressWarnings("javadoc")
public class SequenceDeclaration2Test {

    @NonNull private static final String FIELD_NAME = "LengthName";

    private SequenceDeclaration fixture;
    @NonNull private BitBuffer input = new BitBuffer();

    @Before
    public void setUp() {
        fixture = new SequenceDeclaration(FIELD_NAME, new StringDeclaration());
        byte array[] = { 't', 'e', 's', 't', '\0', 't', 'h', 'i', 's', '\0' };
        ByteBuffer byb = ByteBuffer.wrap(array);
        if( byb == null){
            throw new IllegalStateException("Failed to allocate memory");
        }
        input = new BitBuffer(byb);
    }

    /**
     * Run the SequenceDeclaration(String,Declaration) constructor test.
     */
    @Test
    public void testSequenceDeclaration() {
        String lengthName = "";
        IDeclaration elemType = new StringDeclaration();

        SequenceDeclaration result = new SequenceDeclaration(lengthName, elemType);
        assertNotNull(result);
        String string = "[declaration] sequence[";
        assertEquals(string, result.toString().substring(0, string.length()));
    }

    /**
     * Run the SequenceDefinition createDefinition(DefinitionScope,String)
     * method test.
     *
     * @throws CTFReaderException
     *             an error in the bitbuffer
     */
    @Test
    public void testCreateDefinition() throws CTFReaderException {
        long seqLen = 2;
        IntegerDeclaration id = IntegerDeclaration.createDeclaration(8, false, 8,
                ByteOrder.LITTLE_ENDIAN, Encoding.UTF8, "", 32);
        StructDeclaration structDec = new StructDeclaration(0);
        structDec.addField(FIELD_NAME, id);
        StructDefinition structDef = new StructDefinition(
                structDec,
                null,
                "x",
                ImmutableList.of(FIELD_NAME),
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
}
