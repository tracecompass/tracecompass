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
import static org.junit.Assume.assumeTrue;

import org.eclipse.linuxtools.ctf.core.event.types.IDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.event.types.StringDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.VariantDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.VariantDefinition;
import org.eclipse.linuxtools.ctf.core.tests.shared.CtfTestTrace;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>VariantDeclarationTest</code> contains tests for the class
 * <code>{@link VariantDeclaration}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class VariantDeclarationTest {

    private static final CtfTestTrace testTrace = CtfTestTrace.KERNEL;

    private VariantDeclaration fixture;

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        fixture = new VariantDeclaration();
    }

    /**
     * Run the VariantDeclaration() constructor test.
     */
    @Test
    public void testVariantDeclaration() {
        assertNotNull(fixture);
        assertEquals(false, fixture.isTagged());
        String left = "[declaration] variant[";
        assertEquals(left, fixture.toString().substring(0, left.length()));
    }

    /**
     * Run the void addField(String,Declaration) method test.
     */
    @Test
    public void testAddField() {
        fixture.setTag("");
        String tag = "";
        IDeclaration declaration = new StringDeclaration();
        fixture.addField(tag, declaration);
    }

    /**
     * Run the VariantDefinition createDefinition(DefinitionScope,String) method
     * test.
     *
     * @throws CTFReaderException Should not happen
     */
    @Test
    public void testCreateDefinition() throws CTFReaderException {
        fixture.setTag("");
        IDefinitionScope definitionScope = createDefinitionScope();
        String fieldName = "";
        VariantDefinition result = fixture.createDefinition(definitionScope, fieldName);

        assertNotNull(result);
    }

    private static IDefinitionScope createDefinitionScope() throws CTFReaderException {
        assumeTrue(testTrace.exists());
        VariantDeclaration declaration = new VariantDeclaration();
        declaration.setTag("");
        VariantDeclaration variantDeclaration = new VariantDeclaration();
        variantDeclaration.setTag("");
        VariantDefinition variantDefinition = new VariantDefinition(
                variantDeclaration, testTrace.getTrace(), "");
        IDefinitionScope definitionScope = new StructDefinition(
                new StructDeclaration(1L), variantDefinition, "");
        String fieldName = "";

        VariantDefinition result = new VariantDefinition(declaration,
                definitionScope, fieldName);
        return result;
    }

    /**
     * Run the boolean hasField(String) method test.
     */
    @Test
    public void testHasField() {
        fixture.setTag("");
        String tag = "";
        boolean result = fixture.hasField(tag);

        assertEquals(false, result);
    }

    /**
     * Run the boolean isTagged() method test.
     */
    @Test
    public void testIsTagged() {
        fixture.setTag("");
        boolean result = fixture.isTagged();

        assertEquals(true, result);
    }

    /**
     * Run the boolean isTagged() method test.
     */
    @Test
    public void testIsTagged_null() {
        fixture.setTag((String) null);
        boolean result = fixture.isTagged();

        assertEquals(false, result);
    }

    /**
     * Run the void setTag(String) method test.
     */
    @Test
    public void testSetTag() {
        fixture.setTag("");
        String tag = "";
        fixture.setTag(tag);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString() {
        fixture.setTag("");
        String result = fixture.toString();
        String left = "[declaration] variant[";
        String right = result.substring(0, left.length());

        assertEquals(left, right);
    }
}