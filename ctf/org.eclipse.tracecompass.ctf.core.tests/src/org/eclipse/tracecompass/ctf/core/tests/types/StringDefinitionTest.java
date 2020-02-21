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

package org.eclipse.tracecompass.ctf.core.tests.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.ByteBuffer;

import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.tracecompass.ctf.core.event.types.Encoding;
import org.eclipse.tracecompass.ctf.core.event.types.StringDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StringDefinition;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>StringDefinitionTest</code> contains tests for the class
 * <code>{@link StringDefinition}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class StringDefinitionTest {

    private StringDefinition fixture;
    private String testString;

    /**
     * Perform pre-test initialization.
     *
     * @throws CTFException
     *             won't happen
     */
    @Before
    public void setUp() throws CTFException {
        String name = "testString";
        StringDeclaration stringDec = StringDeclaration.getStringDeclaration(Encoding.UTF8);
        ByteBuffer byteBuffer = ByteBuffer.allocate(100);
        BitBuffer bb = new BitBuffer(byteBuffer);
        byteBuffer.mark();
        testString = new String("testString");
        byteBuffer.put(testString.getBytes());
        byteBuffer.reset();
        fixture = stringDec.createDefinition(null, name, bb);
    }

    /**
     * Run the StringDefinition(StringDeclaration,DefinitionScope,String)
     * constructor test.
     */
    @Test
    public void testStringDefinition() {
        StringDeclaration declaration = StringDeclaration.getStringDeclaration(Encoding.UTF8);
        IDefinitionScope definitionScope = null;
        String fieldName = "";

        StringDefinition result = new StringDefinition(declaration,
                definitionScope, fieldName, "");

        assertNotNull(result);
    }

    /**
     * Run the StringDeclaration getDeclaration() method test.
     */
    @Test
    public void testGetDeclaration() {
        StringDeclaration result = fixture.getDeclaration();
        assertNotNull(result);
    }

    /**
     * Run the String getValue() method test.
     */
    @Test
    public void testGetValue() {
        String result = fixture.getValue();
        assertNotNull(result);
    }

    /**
     * Run the String setValue() method test.
     */
    @Test
    public void testSetValue() {

        String result = fixture.getValue();
        assertNotNull(result);
        assertEquals("testString", result);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString() {
        String result = fixture.toString();
        assertNotNull(result);
    }
}