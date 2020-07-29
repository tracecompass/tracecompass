/**********************************************************************
 * Copyright (c) 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
package org.eclipse.tracecompass.tmf.core.tests.model.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeDataModel;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit Test class to test {@link TmfTreeDataModel}
 */
public class TmfTreeDataModelTest {
    // ------------------------------------------------------------------------
    // Test data
    // ------------------------------------------------------------------------
    private static final String TO_STRING = "toString";
    private static final String HASH_CODE = "hashCode";
    private static final String EQUALS = "equals";

    private static final @NonNull List<@NonNull String> LABELS0 = Arrays.asList("label1, label2, label3");
    private static final long ID0 = 0L;
    private static final long PARENT_ID0 = -1L;
    private static final OutputElementStyle STYLE0 = null;
    private static final @NonNull List<@NonNull String> LABELS1 = Arrays.asList("label4, label5, label6", "label7");
    private static final long ID1 = 1L;
    private static final long PARENT_ID1 = 0L;
    private static final boolean HAS_MODEL1 = false;
    private static final @NonNull OutputElementStyle STYLE1 = new OutputElementStyle("1");

    private static final long ID2 = 0L;
    private static final long PARENT_ID2 = -1L;
    private static final OutputElementStyle STYLE3 = null;
    private static final String NAME = "Name";

    private TmfTreeDataModel fModel0 = null;
    private TmfTreeDataModel fModel1 = null;

    // ------------------------------------------------------------------------
    // Test setup
    // ------------------------------------------------------------------------
    /**
     * Test initialization.
     */
    @Before
    public void setUp() {
        fModel0 = createModel(0);
        fModel1 = createModel(1);
    }

    // ------------------------------------------------------------------------
    // Tests
    // ------------------------------------------------------------------------

    /**
     * Test constructors and getter/setters.
     */
    @Test
    public void testConstructors() {
        TmfTreeDataModel model0 = createModel(0);
        assertEquals(ID0, model0.getId());
        assertEquals(PARENT_ID0, model0.getParentId());
        assertEquals(LABELS0.get(0), model0.getName());
        assertEquals(LABELS0, model0.getLabels());
        assertTrue(model0.hasRowModel());
        assertEquals(STYLE0, model0.getStyle());

        TmfTreeDataModel model1 = createModel(1);
        assertEquals(ID1, model1.getId());
        assertEquals(PARENT_ID1, model1.getParentId());
        assertEquals(LABELS1.get(0), model1.getName());
        assertEquals(LABELS1, model1.getLabels());
        assertFalse(model1.hasRowModel());
        assertEquals(STYLE1, model1.getStyle());

        TmfTreeDataModel model2 = createModel(2);
        assertEquals(ID2, model2.getId());
        assertEquals(PARENT_ID2, model2.getParentId());
        assertEquals(NAME, model2.getName());
        assertEquals(1, model2.getLabels().size());
        assertEquals(NAME, model2.getLabels().get(0));
        assertTrue(model2.hasRowModel());
        assertEquals(STYLE3, model2.getStyle());

    }

    // ------------------------------------------------------------------------
    // TmfTreeDataModel#equals()
    // ------------------------------------------------------------------------

    /**
     * Run the {@link TmfTreeDataModel#equals} method test.
     */
    @Test
    public void testEqualsReflexivity() {
        assertTrue(EQUALS, fModel0.equals(fModel0));
        assertTrue(EQUALS, fModel1.equals(fModel1));

        assertTrue(EQUALS, !fModel0.equals(fModel1));
        assertTrue(EQUALS, !fModel1.equals(fModel0));
    }

    /**
     * Run the {@link TmfTreeDataModel#equals} method test.
     */
    @Test
    public void testEqualsSymmetry() {
        TmfTreeDataModel model0 = createModel(0);
        TmfTreeDataModel model1 = createModel(1);

        assertTrue(EQUALS, model0.equals(fModel0));
        assertTrue(EQUALS, fModel0.equals(model0));

        assertTrue(EQUALS, model1.equals(fModel1));
        assertTrue(EQUALS, fModel1.equals(model1));
    }

    /**
     * Run the {@link TmfTreeDataModel#equals} method test.
     */
    @Test
    public void testEqualsTransivity() {
        TmfTreeDataModel model0 = createModel(0);
        TmfTreeDataModel model1 = createModel(0);
        TmfTreeDataModel model2 = createModel(0);

        assertTrue(EQUALS, model0.equals(model1));
        assertTrue(EQUALS, model1.equals(model2));
        assertTrue(EQUALS, model0.equals(model2));
    }

    /**
     * Run the {@link TmfTreeDataModel#equals} method test.
     */
    @Test
    public void testEqualsNull() {
        TmfTreeDataModel model0 = null;
        assertFalse(EQUALS, fModel0.equals(model0));
        assertFalse(EQUALS, fModel1.equals(model0));
    }

    // ------------------------------------------------------------------------
    // TmfTreeDataModel#hashCode
    // ------------------------------------------------------------------------

    /**
     * Run the {@link TmfTreeDataModel#hashCode} method test.
     */
    @Test
    public void testHashCode() {
        TmfTreeDataModel model0 = createModel(0);
        TmfTreeDataModel model1 = createModel(1);

        assertTrue(HASH_CODE, fModel0.hashCode() == model0.hashCode());
        assertTrue(HASH_CODE, fModel1.hashCode() == model1.hashCode());

        assertTrue(HASH_CODE, fModel0.hashCode() != model1.hashCode());
        assertTrue(HASH_CODE, fModel1.hashCode() != model0.hashCode());
    }

    /**
     * Test {@link TmfTreeDataModel#toString()}
     */
    @Test
    public void testToString() {
        assertEquals(TO_STRING, "<name=[label1, label2, label3] id=0 parentId=-1 style=null hasRowModel=true>", fModel0.toString());
        assertEquals(TO_STRING, "<name=[label4, label5, label6, label7] id=1 parentId=0 style=Style [1, {}] hasRowModel=false>", fModel1.toString());
        TmfTreeDataModel model2 = createModel(2);
        assertEquals(TO_STRING, "<name=[Name] id=0 parentId=-1 style=null hasRowModel=true>", model2.toString());
    }

    // ------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------

    private static TmfTreeDataModel createModel(int i) {
        switch (i) {
        case 0:
            return new TmfTreeDataModel(ID0, PARENT_ID0, LABELS0);
        case 1:
            return new TmfTreeDataModel(ID1, PARENT_ID1, LABELS1, HAS_MODEL1, STYLE1);
        case 2:
            return new TmfTreeDataModel(ID2, PARENT_ID2, NAME);
        default:
            return new TmfTreeDataModel(ID0, PARENT_ID0, LABELS0);
        }
    }
}
