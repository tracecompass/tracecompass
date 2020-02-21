/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.tests.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.types.Encoding;
import org.eclipse.tracecompass.ctf.core.event.types.EnumDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StringDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StructDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StructDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.VariantDeclaration;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEventField;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Add some unit tests specific for variant fields
 *
 * @author Geneviève Bastien
 */
public class CtfTmfEventFieldVariantTest {

    /*
     * Same string content, but not the same object, as the any variant selected
     * string
     */
    private static final @NonNull String ENUM_ANY = new String(CtfTmfEventField.FIELD_VARIANT_SELECTED);
    private static final @NonNull String ENUM_NAME2 = "choice2";
    private static final @NonNull String ENUM_NAME3 = "choice3";
    private static final int ENUM_VAL1 = 0;
    private static final int ENUM_VAL2 = 1;
    private static final int ENUM_VAL3 = 2;
    private static final @NonNull String ENUM = "enum";

    private static final @NonNull String FIELD1 = "Fiedl1";
    private static final @NonNull String FIELD2 = "Field2";
    private static final @NonNull String FIELD3 = "Field3";

    private static final @NonNull String VARIANT = "variant";
    private static final @NonNull String ROOT = "root";

    private static StructDeclaration fDeclaration;

    /**
     * Create the declaration for the variant field
     */
    @BeforeClass
    public static void createDeclaration() {
        /**
         * Create a variant field based on the following metadata
         *
         * <pre>
         * enum : integer { size = 8; } {
         *     "_Any" = 0,
         *     "_var2" = 1,
         *     "_var3" = 2,
         * } _tag;
         * variant <_tag> {
         *     struct {
         *        integer { size = 8; } _field1;
         *        integer { size = 8; } _field2;
         *     } _Any;
         *     struct {
         *         integer { size = 8; } _field1;
         *         string _field2;
         *         integer { size = 8; } _field3;
         *     } _var2;
         *     integer { size = 8; } _var3;
         * } _mainVariant;
         * </pre>
         */
        StructDeclaration sDec = new StructDeclaration(0);

        /* Create enum field */
        EnumDeclaration enumDec = new EnumDeclaration(IntegerDeclaration.INT_8_DECL);
        enumDec.add(ENUM_VAL1, ENUM_VAL1, ENUM_ANY);
        enumDec.add(ENUM_VAL2, ENUM_VAL2, ENUM_NAME2);
        enumDec.add(ENUM_VAL3, ENUM_VAL3, ENUM_NAME3);
        sDec.addField(ENUM, enumDec);

        /* Create structure for first choice */
        StructDeclaration choice1Dec = new StructDeclaration(0);
        choice1Dec.addField(FIELD1, IntegerDeclaration.INT_8_DECL);
        choice1Dec.addField(FIELD2, IntegerDeclaration.INT_8_DECL);

        /* Create structure for second choice */
        StructDeclaration choice2Dec = new StructDeclaration(0);
        choice2Dec.addField(FIELD1, IntegerDeclaration.INT_8_DECL);
        choice2Dec.addField(FIELD2, StringDeclaration.getStringDeclaration(Encoding.UTF8));
        choice2Dec.addField(FIELD3, IntegerDeclaration.INT_8_DECL);

        /* Create variant field */
        VariantDeclaration varDec = new VariantDeclaration();
        varDec.setTag(ENUM);
        varDec.addField(ENUM_ANY, choice1Dec);
        varDec.addField(ENUM_NAME2, choice2Dec);
        varDec.addField(ENUM_NAME3, IntegerDeclaration.INT_8_DECL);

        sDec.addField(VARIANT, varDec);
        fDeclaration = sDec;
    }

    /**
     * Test variant field getters
     *
     * @throws CTFException
     *             exception occurring when reading a field
     */
    @Test
    public void testVariantGetField() throws CTFException {
        StructDeclaration decl = fDeclaration;
        StructDefinition sDef = null;

        /* Obtain a field with the first variant choice */
        ByteBuffer bb = ByteBuffer.allocateDirect(1024);
        bb.put((byte) ENUM_VAL1);
        bb.put((byte) 3);
        bb.put((byte) 4);

        sDef = decl.createDefinition(null, ROOT, new BitBuffer(bb));
        IDefinition definition = sDef.getDefinition(VARIANT);
        CtfTmfEventField result = CtfTmfEventField.parseField(definition, VARIANT);

        /** Assert it returns a value only for the correct enum */
        assertNotNull(result.getField(ENUM_ANY));
        assertNull(result.getField(ENUM_NAME2));
        assertNull(result.getField(ENUM_NAME3));

        /**
         * Assert the subfields can be reached through the current enum and have
         * the right value
         */
        assertNotNull(result.getField(ENUM_ANY, FIELD1));
        assertEquals(3L, result.getField(ENUM_ANY, FIELD1).getValue());
        assertNotNull(result.getField(ENUM_ANY, FIELD2));
        assertEquals(4L, result.getField(ENUM_ANY, FIELD2).getValue());
        assertNull(result.getField(ENUM_ANY, FIELD3));

        /**
         * Assert the subfields can be reached through the
         * {@link CtfTmfEventField#FIELD_VARIANT_SELECTED} constant and have the
         * right value
         */
        assertNotNull(result.getField(CtfTmfEventField.FIELD_VARIANT_SELECTED, FIELD1));
        assertEquals(3L, result.getField(CtfTmfEventField.FIELD_VARIANT_SELECTED, FIELD1).getValue());
        assertNotNull(result.getField(CtfTmfEventField.FIELD_VARIANT_SELECTED, FIELD2));
        assertEquals(4L, result.getField(CtfTmfEventField.FIELD_VARIANT_SELECTED, FIELD2).getValue());
        assertNull(result.getField(CtfTmfEventField.FIELD_VARIANT_SELECTED, FIELD3));

        /* Obtain a field with the second variant choice */
        String testString = "My Test string";
        bb = ByteBuffer.allocateDirect(1024);
        bb.put((byte) ENUM_VAL2);
        bb.put((byte) 3);
        bb.put(testString.getBytes());
        bb.put((byte) 0);
        bb.put((byte) 4);

        sDef = decl.createDefinition(null, ROOT, new BitBuffer(bb));
        definition = sDef.getDefinition(VARIANT);
        result = CtfTmfEventField.parseField(definition, VARIANT);

        /** Assert it returns a value only for the correct enum */
        assertNull(result.getField(ENUM_ANY));
        assertNotNull(result.getField(ENUM_NAME2));
        assertNull(result.getField(ENUM_NAME3));

        /**
         * Assert the subfields can be reached through the current enum and have
         * the right value
         */
        assertNotNull(result.getField(ENUM_NAME2, FIELD1));
        assertEquals(3L, result.getField(ENUM_NAME2, FIELD1).getValue());
        assertNotNull(result.getField(ENUM_NAME2, FIELD2));
        assertEquals(testString, result.getField(ENUM_NAME2, FIELD2).getValue());
        assertNotNull(result.getField(ENUM_NAME2, FIELD3));
        assertEquals(4L, result.getField(ENUM_NAME2, FIELD3).getValue());

        /**
         * Assert the subfields can be reached through the
         * {@link CtfTmfEventField#FIELD_VARIANT_SELECTED} constant and have the
         * right value
         */
        assertNotNull(result.getField(CtfTmfEventField.FIELD_VARIANT_SELECTED, FIELD1));
        assertEquals(3L, result.getField(CtfTmfEventField.FIELD_VARIANT_SELECTED, FIELD1).getValue());
        assertNotNull(result.getField(CtfTmfEventField.FIELD_VARIANT_SELECTED, FIELD2));
        assertEquals(testString, result.getField(CtfTmfEventField.FIELD_VARIANT_SELECTED, FIELD2).getValue());
        assertNotNull(result.getField(CtfTmfEventField.FIELD_VARIANT_SELECTED, FIELD3));
        assertEquals(4L, result.getField(CtfTmfEventField.FIELD_VARIANT_SELECTED, FIELD3).getValue());

        /**
         * Assert that trying to reach a field of this choice with the same name
         * as the current selection field will not return the field
         */
        assertNull(result.getField(ENUM_ANY, FIELD1));

        /* Obtain a field with the third variant choice */
        bb = ByteBuffer.allocateDirect(1024);
        bb.put((byte) ENUM_VAL3);
        bb.put((byte) 3);

        sDef = decl.createDefinition(null, ROOT, new BitBuffer(bb));
        definition = sDef.getDefinition(VARIANT);
        result = CtfTmfEventField.parseField(definition, VARIANT);

        /** Assert it returns a value only for the correct enum */
        assertNull(result.getField(ENUM_ANY));
        assertNull(result.getField(ENUM_NAME2));
        assertNotNull(result.getField(ENUM_NAME3));

        /**
         * Assert the subfields return null
         */
        assertNull(result.getField(ENUM_NAME3, FIELD1));
        assertNull(result.getField(ENUM_NAME3, FIELD2));
        assertEquals(3L, result.getField(ENUM_NAME3).getValue());

        /**
         * Assert the subfields can be reached through the
         * {@link CtfTmfEventField#FIELD_VARIANT_SELECTED} constant and have the
         * right value
         */
        assertNotNull(result.getField(CtfTmfEventField.FIELD_VARIANT_SELECTED));
        assertEquals(3L, result.getField(CtfTmfEventField.FIELD_VARIANT_SELECTED).getValue());
        assertNull(result.getField(CtfTmfEventField.FIELD_VARIANT_SELECTED, FIELD1));
    }

}
