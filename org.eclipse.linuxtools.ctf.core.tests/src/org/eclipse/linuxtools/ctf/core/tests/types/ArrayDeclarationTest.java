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

import org.eclipse.linuxtools.ctf.core.event.types.ArrayDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.ArrayDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.IDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.event.types.StringDeclaration;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>ArrayDeclarationTest</code> contains tests for the class
 * <code>{@link ArrayDeclaration}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class ArrayDeclarationTest {

    private ArrayDeclaration fixture;

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        fixture = new ArrayDeclaration(1, new StringDeclaration());
    }

    /**
     * Run the ArrayDeclaration(int,Declaration) constructor test.
     */
    @Test
    public void testArrayDeclaration() {
        int length = 1;
        IDeclaration elemType = new StringDeclaration();
        ArrayDeclaration result = new ArrayDeclaration(length, elemType);

        assertNotNull(result);
        String left = "[declaration] array[";
        String right = result.toString().substring(0, left.length());
        assertEquals(left, right);
        assertEquals(1, result.getLength());
    }

    /**
     * Run the ArrayDefinition createDefinition(DefinitionScope,String) method
     * test.
     */
    @Test
    public void testCreateDefinition() {
        String fieldName = "";
        IDefinitionScope definitionScope = null;
        ArrayDefinition result;
        result = fixture.createDefinition(definitionScope, fieldName);

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
     * Run the int getLength() method test.
     */
    @Test
    public void testGetLength() {
        int result = fixture.getLength();
        assertEquals(1, result);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString() {
        String result = fixture.toString();
        String left = "[declaration] array[";
        String right = result.substring(0, left.length());

        assertEquals(left, right);
    }
}
