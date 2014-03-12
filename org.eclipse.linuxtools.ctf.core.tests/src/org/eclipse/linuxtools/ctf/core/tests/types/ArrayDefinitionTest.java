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

import static org.junit.Assert.assertNotNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.event.types.ArrayDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.ArrayDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.Encoding;
import org.eclipse.linuxtools.ctf.core.event.types.IDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StringDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StringDefinition;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>ArrayDefinitionTest</code> contains tests for the class
 * <code>{@link ArrayDefinition}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class ArrayDefinitionTest {

    private CTFTrace trace;
    private ArrayDefinition charArrayFixture;
    private ArrayDefinition stringArrayFixture;
    private ArrayDefinition longArrayFixture;

    /**
     * Perform pre-test initialization.
     *
     * structDef shouldn't be null after parsing the CTFTraceReader object, so
     * we can ignore the warning.
     */
    @Before
    public void setUp() {
        charArrayFixture = createCharArray();
        stringArrayFixture = createStringArray();
        longArrayFixture = createLongArray();
    }

    private ArrayDefinition createLongArray() {
        IntegerDeclaration decl = new IntegerDeclaration(32, false, 10, ByteOrder.BIG_ENDIAN, Encoding.NONE, "none",8);
        IntegerDefinition[] defs = createIntDefs(10, 32);
        ArrayDefinition temp = setUpDeclaration(decl, defs);
        return temp;
    }

    private ArrayDefinition createCharArray() {
        IntegerDeclaration decl = new IntegerDeclaration(8, false, 10, ByteOrder.BIG_ENDIAN, Encoding.UTF8, "none",8);
        IntegerDefinition[] defs = createIntDefs(4,8);
        ArrayDefinition temp = setUpDeclaration(decl, defs);
        return temp;
    }

    private ArrayDefinition createStringArray() {
        StringDeclaration strDecl = new StringDeclaration();
        StringDefinition[] defs = createDefs();
        ArrayDefinition temp = setUpDeclaration(strDecl, defs);
        return temp;
    }

    private ArrayDefinition setUpDeclaration(IDeclaration decl,
            Definition[] defs) {
        ArrayDeclaration ad = new ArrayDeclaration(0, decl);
        ArrayDefinition temp = new ArrayDefinition(ad , this.trace , "Testx");
        temp.setDefinitions(defs);
        return temp;
    }


    private static IntegerDefinition[] createIntDefs(int size, int bits) {
        IntegerDefinition[] defs = new IntegerDefinition[size];
        for (int i = 0; i < size; i++) {

            String content = "test" + i;
            defs[i] = new IntegerDefinition(new IntegerDeclaration(bits, false,
                    16, ByteOrder.LITTLE_ENDIAN, Encoding.UTF8, content, 24), null, content);
            defs[i].setValue(i);
        }
        return defs;
    }

    private static StringDefinition[] createDefs() {
        int size = 4;
        StringDefinition[] defs = new StringDefinition[size];
        for (int i = 0; i < size; i++) {

            String content = "test" + i;
            defs[i] = new StringDefinition(
                    new StringDeclaration(Encoding.UTF8), null, content);
            defs[i].setValue(content);
        }
        return defs;
    }

    /**
     * Run the ArrayDefinition(ArrayDeclaration,DefinitionScope,String)
     * constructor test.
     */
    @Test
    public void testArrayDefinition_baseDeclaration() {
        ArrayDeclaration declaration = charArrayFixture.getDeclaration();
        String fieldName = "";

        ArrayDefinition result = new ArrayDefinition(declaration, this.trace, fieldName);
        assertNotNull(result);
    }

    /**
     * Run the ArrayDefinition(ArrayDeclaration,DefinitionScope,String)
     * constructor test.
     */
    @Test
    public void testArrayDefinition_newDeclaration() {
        ArrayDeclaration declaration = new ArrayDeclaration(0,
                new StringDeclaration());
        IDefinitionScope definitionScope = null;
        String fieldName = "";

        ArrayDefinition result = new ArrayDefinition(declaration, definitionScope, fieldName);
        assertNotNull(result);
    }

    /**
     * Run the ArrayDeclaration getDeclaration() method test.
     */
    @Test
    public void testGetDeclaration() {
        charArrayFixture.setDefinitions(new Definition[] {});
        ArrayDeclaration result = charArrayFixture.getDeclaration();

        assertNotNull(result);
    }

    /**
     * Run the Definition getElem(int) method test.
     */
    @Test
    public void testGetElem_noDefs() {
        int i = 0;
        Definition result = charArrayFixture.getElem(i);

        assertNotNull(result);
    }

    /**
     * Run the Definition getElem(int) method test.
     */
    @Test
    public void testGetElem_withDefs() {
        Definition defs[] = createDefs();
        charArrayFixture.setDefinitions(defs);
        int j = 1;

        Definition result = charArrayFixture.getElem(j);

        assertNotNull(result);
    }

    /**
     * Run the void read(BitBuffer) method test.
     * @throws CTFReaderException error
     */
    @Test
    public void testRead_noDefs() throws CTFReaderException {
        BitBuffer input = new BitBuffer(ByteBuffer.allocateDirect(128));

        charArrayFixture.read(input);
    }

    /**
     * Run the void read(BitBuffer) method test.
     * @throws CTFReaderException error
     */
    @Test
    public void testRead_withDefs() throws CTFReaderException {
        charArrayFixture.setDefinitions(new Definition[] {});
        BitBuffer input = new BitBuffer(java.nio.ByteBuffer.allocateDirect(128));

        charArrayFixture.read(input);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString_char() {
        String result = charArrayFixture.toString();
        assertNotNull(result);
    }
    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString_long() {
        String result = longArrayFixture.toString();
        assertNotNull(result);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString_string() {
        String result = stringArrayFixture.toString();
        assertNotNull(result);
    }
    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString_withDefs() {
        String result = charArrayFixture.toString();

        assertNotNull(result);
    }
    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToStringStringArray() {
        String result = stringArrayFixture.toString();

        assertNotNull(result);
    }
}
