/*******************************************************************************
 * Copyright (c) 2013, 2020 Ericsson, École Polytechnique de Montréal
 *
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

import java.nio.ByteOrder;

import org.eclipse.tracecompass.ctf.core.event.types.Encoding;
import org.eclipse.tracecompass.ctf.core.event.types.EnumDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.EnumDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDefinition;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>EnumDefinitionTest</code> contains tests for the class
 * <code>{@link EnumDefinition}</code>.
 *
 * @author Matthew Khouzam
 * @version $Revision: 1.0 $
 */
public class EnumDefinitionTest {

    private EnumDefinition fixtureA;
    private EnumDefinition fixtureB;

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        IntegerDeclaration integerDeclaration = IntegerDeclaration.createDeclaration(1, false, 1, ByteOrder.BIG_ENDIAN,
                Encoding.ASCII, "", 8);
        EnumDeclaration declaration = new EnumDeclaration(
                integerDeclaration);
        declaration.add(0, 10, "a");
        declaration.add(11, 20, "b");
        String fieldName = "";

        fixtureA = new EnumDefinition(declaration, null, fieldName, new IntegerDefinition(integerDeclaration, null, fieldName, 4));
        fixtureB = new EnumDefinition(declaration, null, fieldName, new IntegerDefinition(integerDeclaration, null, fieldName, 12));
    }

    /**
     * Run the EnumDefinition(EnumDeclaration,DefinitionScope,String)
     * constructor test.
     */
    @Test
    public void testEnumDefinition() {
        assertNotNull(fixtureA);
        assertNotNull(fixtureB);
    }

    /**
     * Run the String getValue() method test.
     */
    @Test
    public void testGetValue() {
        String result = fixtureA.getValue();

        assertNotNull(result);
        assertEquals("a", result);
    }

    /**
     * Run the long getIntegerValue() method test.
     */
    @Test
    public void testGetIntegerValue_one() {
        long result = fixtureA.getIntegerValue();
        assertEquals(4L, result);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString() {
        String result = fixtureB.toString();

        assertEquals("{ value = b, container = 12 }", result);
    }

    /**
     * Test results of an unknown enumeration value
     */
    @Test
    public void testUnknownEnum() {
        IntegerDeclaration integerDeclaration = IntegerDeclaration.createDeclaration(1, false, 1, ByteOrder.BIG_ENDIAN,
                Encoding.ASCII, "", 8);
        EnumDeclaration declaration = new EnumDeclaration(
                integerDeclaration);
        declaration.add(0, 10, "a");
        declaration.add(11, 20, "b");
        String fieldName = "unknownValue";

        // Test with a value of 0, where 0 is not part of the enumeration
        EnumDefinition fixture = new EnumDefinition(declaration, null, fieldName, new IntegerDefinition(integerDeclaration, null, fieldName, 22));

        assertEquals("<unknown> (22)", fixture.getValue());
        assertEquals("{ value = <unknown> (22), container = 22 }", fixture.toString());
    }

}