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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.event.scope.LexicalScope;
import org.eclipse.linuxtools.ctf.core.event.types.CompoundDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.Encoding;
import org.eclipse.linuxtools.ctf.core.event.types.IDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StringDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StringDefinition;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.eclipse.linuxtools.internal.ctf.core.event.types.ArrayDeclaration;
import org.eclipse.linuxtools.internal.ctf.core.event.types.ArrayDefinition;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>ArrayDefinition2Test</code> contains tests for the class
 * <code>{@link ArrayDefinition}</code>.
 *
 */
public class ArrayDefinition2Test {

    @NonNull private CTFTrace trace = new CTFTrace();
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
        IntegerDeclaration decl = IntegerDeclaration.createDeclaration(32, false, 10, ByteOrder.BIG_ENDIAN, Encoding.NONE, "none", 8);
        List<Definition> defs = createIntDefs(10, 32);
        ArrayDefinition temp = setUpDeclaration(decl, defs);
        return temp;
    }

    private ArrayDefinition createCharArray() {
        IntegerDeclaration decl = IntegerDeclaration.createDeclaration(8, false, 10, ByteOrder.BIG_ENDIAN, Encoding.UTF8, "none", 8);
        List<Definition> defs = createIntDefs(4, 8);
        ArrayDefinition temp = setUpDeclaration(decl, defs);
        return temp;
    }

    private ArrayDefinition createStringArray() {
        StringDeclaration strDecl = new StringDeclaration();
        List<Definition> defs = createDefs();
        ArrayDefinition temp = setUpDeclaration(strDecl, defs);
        return temp;
    }

    private ArrayDefinition setUpDeclaration(IDeclaration decl,
            @NonNull List<Definition> defs) {
        ArrayDeclaration ad = new ArrayDeclaration(0, decl);
        ArrayDefinition temp = new ArrayDefinition(ad, this.trace, "Testx", defs);
        return temp;
    }

    private @NonNull
    static List<Definition> createIntDefs(int size, int bits) {
        List<Definition> defs = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            String content = "test" + i;
            defs.add(new IntegerDefinition(IntegerDeclaration.createDeclaration(bits, false,
                    16, ByteOrder.LITTLE_ENDIAN, Encoding.UTF8, content, 24), null, content, i));
        }
        return defs;
    }

    private @NonNull
    static List<Definition> createDefs() {
        int size = 4;
        List<Definition> defs = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            String content = "test" + i;
            defs.add(new StringDefinition(
                    new StringDeclaration(Encoding.UTF8), null, content, content));
        }
        return defs;
    }

    /**
     * Run the ArrayDefinition(ArrayDeclaration,DefinitionScope,String)
     * constructor test.
     */
    @Test
    public void testArrayDefinition_baseDeclaration() {
        ArrayDeclaration declaration = (ArrayDeclaration) charArrayFixture.getDeclaration();
        String fieldName = "";

        @SuppressWarnings("null")
        ArrayDefinition result = new ArrayDefinition(declaration, this.trace, fieldName, Arrays.asList(new Definition[0]));
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
        IDefinitionScope definitionScope = getDefinitionScope();

        String fieldName = "";
        @SuppressWarnings("null")
        ArrayDefinition result = new ArrayDefinition(declaration, definitionScope, fieldName , Arrays.asList(new Definition[0]));
        assertNotNull(result);
    }

    /**
     * Run the ArrayDeclaration getDeclaration() method test.
     */
    @Test
    public void testGetDeclaration() {
        ArrayDeclaration result = (ArrayDeclaration) charArrayFixture.getDeclaration();

        assertNotNull(result);
    }

    /**
     * Run the Definition getDefinitions().get(int) method test.
     */
    @Test
    public void testgetElem_noDefs() {
        int i = 0;
        Definition result = charArrayFixture.getDefinitions().get(i);

        assertNotNull(result);
    }

    /**
     * Run the Definition getDefinitions().get(int) method test.
     */
    @Test
    public void testgetElem_withDefs() {
        List<Definition> defs = createDefs();
        IDefinitionScope definitionScope = getDefinitionScope();
        ArrayDefinition ad = new ArrayDefinition((CompoundDeclaration) charArrayFixture.getDeclaration(), definitionScope, "test", defs);
        int j = 1;

        Definition result = ad.getDefinitions().get(j);

        assertNotNull(result);
    }

    @NonNull private static IDefinitionScope getDefinitionScope() {
        return new IDefinitionScope() {

            @Override
            public Definition lookupDefinition(String lookupPath) {
                return null;
            }

            @Override
            public LexicalScope getScopePath() {
                return null;
            }
        };
    }

    /**
     * Run the void read(BitBuffer) method test.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testRead_noDefs() throws CTFReaderException {
        BitBuffer input = new BitBuffer(ByteBuffer.allocateDirect(128));
        charArrayFixture.getDeclaration().createDefinition(null, "test", input);
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
