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

import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.event.types.Encoding;
import org.eclipse.linuxtools.ctf.core.event.types.StringDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StringDefinition;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>StringDeclarationTest</code> contains tests for the class
 * <code>{@link StringDeclaration}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class StringDeclarationTest {

    private StringDeclaration fixture;

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        fixture = new StringDeclaration(Encoding.ASCII);
    }

    /**
     * Run the StringDeclaration() constructor test.
     */
    @Test
    public void testStringDeclaration() {
        StringDeclaration result = new StringDeclaration();

        assertNotNull(result);
        String string = "[declaration] string[";
        assertEquals(string, result.toString().substring(0, string.length()));
    }

    /**
     * Run the StringDeclaration(Encoding) constructor test.
     */
    @Test
    public void testStringDeclaration_2() {
        Encoding encoding = Encoding.ASCII;
        StringDeclaration result = new StringDeclaration(encoding);

        assertNotNull(result);
        String string = "[declaration] string[";
        assertEquals(string, result.toString().substring(0, string.length()));
    }

    /**
     * Run the StringDefinition createDefinition(DefinitionScope,String) method
     * test.
     * @throws CTFReaderException out of buffer exception
     */
    @Test
    public void testCreateDefinition() throws CTFReaderException {
        IDefinitionScope definitionScope = null;
        String fieldName = "id";
        BitBuffer bb = new BitBuffer(ByteBuffer.allocate(100));
        StringDefinition result = fixture.createDefinition(definitionScope,
                fieldName, bb);

        assertNotNull(result);
    }

    /**
     * Run the Encoding getEncoding() method test.
     */
    @Test
    public void testGetEncoding() {
        Encoding result = fixture.getEncoding();

        assertNotNull(result);
        assertEquals("ASCII", result.name());
        assertEquals("ASCII", result.toString());
        assertEquals(1, result.ordinal());
    }

     /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString() {
        String result = fixture.toString();
        String left = "[declaration] string[";
        String right = result.substring(0, left.length());

        assertEquals(left, right);
    }
}