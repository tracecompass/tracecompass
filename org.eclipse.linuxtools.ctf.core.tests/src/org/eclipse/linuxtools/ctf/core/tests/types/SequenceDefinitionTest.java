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
import static org.junit.Assert.assertTrue;

import java.nio.ByteOrder;

import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.Encoding;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.SequenceDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.SequenceDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDefinition;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

/**
 * The class <code>SequenceDefinitionTest</code> contains tests for the class
 * <code>{@link SequenceDefinition}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
@SuppressWarnings("javadoc")
public class SequenceDefinitionTest {

    private SequenceDefinition fixture;
    private final static int seqLen = 15;

    private static ImmutableList<String> wrap(String s) {
        return ImmutableList.<String> builder().add(s).build();
    }

    /**
     * Perform pre-test initialization.
     *
     * @throws CTFReaderException
     */
    @Before
    public void setUp() throws CTFReaderException {
        StructDeclaration structDec;
        StructDefinition structDef;

        IntegerDeclaration id = IntegerDeclaration.createDeclaration(8, false, 8,
                ByteOrder.LITTLE_ENDIAN, Encoding.UTF8, "", 8);
        String lengthName = "LengthName";
        structDec = new StructDeclaration(0);
        structDec.addField(lengthName, id);
        structDef = new StructDefinition(structDec, null, "x",
                wrap(lengthName),
                new Definition[] { new IntegerDefinition(id, null, lengthName, seqLen) });

        SequenceDeclaration sd = new SequenceDeclaration(lengthName, id);
        BitBuffer input = new BitBuffer(
                java.nio.ByteBuffer.allocateDirect(seqLen * 8));
        for (int i = 0; i < seqLen; i++) {
            input.putInt(i);
        }

        fixture = sd.createDefinition(structDef, "TestX", input);
        assert (fixture != null);
    }

    private static SequenceDefinition initNonString() throws CTFReaderException {
        StructDeclaration structDec;
        StructDefinition structDef;

        int len = 32;
        IntegerDeclaration id = IntegerDeclaration.createDeclaration(len, false, len,
                ByteOrder.LITTLE_ENDIAN, Encoding.UTF8, "", 8);
        String lengthName = "LengthName";
        structDec = new StructDeclaration(0);
        structDec.addField(lengthName, id);

        structDef = new StructDefinition(structDec, null, "x", wrap(lengthName), new Definition[] { new IntegerDefinition(id, null, lengthName, seqLen) });

        SequenceDeclaration sd = new SequenceDeclaration(lengthName, id);
        BitBuffer input = new BitBuffer(
                java.nio.ByteBuffer.allocateDirect(seqLen * len));
        for (int i = 0; i < seqLen; i++) {
            input.putInt(i);
        }

        SequenceDefinition ret = sd.createDefinition(structDef, "TestX", input);
        assertNotNull(ret);
        return ret;
    }

    /**
     * Run the SequenceDefinition(SequenceDeclaration,DefinitionScope,String)
     * constructor test.
     */
    @Test
    public void testSequenceDefinition() {
        assertNotNull(fixture);
    }

    /**
     * Run the SequenceDeclaration getDeclaration() method test.
     */
    @Test
    public void testGetDeclaration() {
        SequenceDeclaration result = fixture.getDeclaration();
        assertNotNull(result);
    }

    /**
     * Run the Definition getElem(int) method test.
     */
    @Test
    public void testGetElem() {
        int i = 1;
        Definition result = fixture.getElem(i);
        assertNotNull(result);
    }

    /**
     * Run the int getLength() method test.
     */
    @Test
    public void testGetLength() {
        int result = fixture.getLength();

        assertEquals(seqLen, result);
    }

    /**
     * Run the boolean isString() method test.
     */
    @Test
    public void testIsString() {
        boolean result = fixture.getDeclaration().isString();
        assertTrue(result);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString() {
        String result = fixture.toString();
        assertNotNull(result);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString_nonString() throws Exception {
        fixture = initNonString();
        String result = fixture.toString();
        assertNotNull(result);
    }
}
