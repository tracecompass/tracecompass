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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;

import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.event.types.IDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StringDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDefinition;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>StructDeclarationTest</code> contains tests for the class
 * <code>{@link StructDeclaration}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class StructDeclarationTest {

    private StructDeclaration fixture;

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        fixture = new StructDeclaration(1L);
    }

    /**
     * Run the StructDeclaration(long) constructor test.
     */
    @Test
    public void testStructDeclaration() {
        assertNotNull(fixture);
        assertEquals(1L, fixture.getMaxAlign());

        String regex = "^\\[declaration\\] struct\\[[0-9a-f]{1,8}\\]$";
        assertTrue(fixture.toString().matches(regex));
    }

    /**
     * Run the void addField(String,Declaration) method test.
     */
    @Test
    public void testAddField() {
        String name = "";
        IDeclaration declaration = new StringDeclaration();
        fixture.addField(name, declaration);
    }

    /**
     * Run the StructDefinition createDefinition(DefinitionScope,String) method
     * test.
     *
     * @throws CTFReaderException
     *             out of bounds
     */
    @Test
    public void testCreateDefinition() throws CTFReaderException {
        String fieldName = "";
        ByteBuffer allocate = ByteBuffer.allocate(100);
        if( allocate == null){
            throw new IllegalStateException("Failed to allocate memory");
        }
        BitBuffer bb = new BitBuffer(allocate);
        StructDefinition result = fixture.createDefinition(null, fieldName, bb);
        assertNotNull(result);
    }

    /**
     * Run the Declaration getField(String) method test.
     */
    @Test
    public void testGetField() {
        IDeclaration result = fixture.getField("test");

        assertNull(result);
    }

    /**
     * Run the List<String> getFieldsList() method test.
     */
    @Test
    public void testGetFieldsList() {
        Iterable<String> result = fixture.getFieldsList();

        assertNotNull(result);
        assertEquals(false, result.iterator().hasNext());
    }

    /**
     * Run the long getMinAlign() method test.
     */
    @Test
    public void testGetMinAlign() {
        long result = fixture.getMaxAlign();
        assertEquals(1L, result);
    }

    /**
     * Run the boolean hasField(String) method test.
     */
    @Test
    public void testHasField() {
        String name = "";
        boolean result = fixture.hasField(name);

        assertEquals(false, result);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString() {
        String result = fixture.toString();
        String trunc = result.substring(0, 21);

        assertEquals("[declaration] struct[", trunc);
    }
}
