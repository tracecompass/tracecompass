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

package org.eclipse.tracecompass.tmf.core.tests.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.tracecompass.internal.provisional.tmf.core.model.TableColumnDescriptor;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.TableColumnDescriptor.Builder;
import org.eclipse.tracecompass.tmf.core.model.ITableColumnDescriptor;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class to verify {@link TableColumnDescriptor}
 */
public class TableColumnDescriptorTest {

    // ------------------------------------------------------------------------
    // Test data
    // ------------------------------------------------------------------------
    private static final String TO_STRING = "toString";
    private static final String HASH_CODE = "hashCode";
    private static final String EQUALS = "equals";

    private static final String COLUMN_TEXT0 = "Name";
    private static final String COLUMN_TOOLTIP0 = "Tooltip";

    private ITableColumnDescriptor fModel0;
    private ITableColumnDescriptor fModel1;

    /**
     * Test initialization
     */
    @Before
    public void setup() {
        fModel0 = createDescriptor(0);
        fModel1 = createDescriptor(1);
    }

    // ------------------------------------------------------------------------
    // Tests
    // ------------------------------------------------------------------------
    /**
     * Test @{link TableColumnDescriptor) construction using {@link Builder}.
     */
    @Test
    public void test() {
        TableColumnDescriptor.Builder builder = new TableColumnDescriptor.Builder();
        ITableColumnDescriptor desc = builder.setText(COLUMN_TEXT0).build();
        assertEquals("Column header", COLUMN_TEXT0, desc.getText());
        assertTrue("Empty tooltip", desc.getTooltip().isEmpty());

        builder = new TableColumnDescriptor.Builder();
        desc = builder.setText(COLUMN_TEXT0)
                      .setTooltip(COLUMN_TOOLTIP0)
                      .build();
        assertEquals("Column header", COLUMN_TEXT0, desc.getText());
        assertEquals("Column tooltip", COLUMN_TOOLTIP0, desc.getTooltip());

        builder = new TableColumnDescriptor.Builder();
        desc = builder.setTooltip(COLUMN_TOOLTIP0).build();
        assertTrue("Empty Text", desc.getText().isEmpty());
        assertEquals("Column tooltip", COLUMN_TOOLTIP0, desc.getTooltip());
    }

    /**
     * Test {@link TableColumnDescriptor#toString()}
     */
    @Test
    public void testToString() {
        TableColumnDescriptor.Builder builder = new TableColumnDescriptor.Builder();
        ITableColumnDescriptor desc = builder.setText(COLUMN_TEXT0)
                .setTooltip(COLUMN_TOOLTIP0)
                .build();
        assertEquals(TO_STRING, "[text=Name tooltip=Tooltip]", desc.toString());
    }

    // ------------------------------------------------------------------------
    // TableColumnDescriptor#equals()
    // ------------------------------------------------------------------------

    /**
     * Run the {@link TableColumnDescriptor#equals} method test.
     */
    @Test
    public void testEqualsReflexivity() {
        assertEquals(EQUALS, fModel0, fModel0);
        assertEquals(EQUALS, fModel1, fModel1);

        assertNotEquals(EQUALS, fModel0, fModel1);
        assertNotEquals(EQUALS, fModel1, fModel0);
    }

    /**
     * Run the {@link TableColumnDescriptor#equals} method test.
     */
    @Test
    public void testEqualsSymmetry() {
        ITableColumnDescriptor model0 = createDescriptor(0);
        ITableColumnDescriptor model1 = createDescriptor(1);

        assertEquals(EQUALS, model0, fModel0);
        assertEquals(EQUALS, fModel0, model0);

        assertEquals(EQUALS, model1, fModel1);
        assertEquals(EQUALS, fModel1, model1);
    }

    /**
     * Run the {@link TableColumnDescriptor#equals} method test.
     */
    @Test
    public void testEqualsTransivity() {
        ITableColumnDescriptor model0 = createDescriptor(0);
        ITableColumnDescriptor model1 = createDescriptor(0);
        ITableColumnDescriptor model2 = createDescriptor(0);

        assertEquals(EQUALS, model0, model1);
        assertEquals(EQUALS, model1, model2);
        assertEquals(EQUALS, model0, model2);
    }

    /**
     * Run the {@link TableColumnDescriptor#equals} method test.
     */
    @Test
    public void testEqualsNull() {
        ITableColumnDescriptor model0 = null;
        assertNotEquals(EQUALS, fModel0, model0);
        assertNotEquals(EQUALS, fModel1, model0);
    }

    // ------------------------------------------------------------------------
    // TableColumnDescriptor#hashCode
    // ------------------------------------------------------------------------

    /**
     * Run the {@link ITableColumnDescriptor#hashCode} method test.
     */
    @Test
    public void testHashCode() {
        ITableColumnDescriptor model0 = createDescriptor(0);
        ITableColumnDescriptor model1 = createDescriptor(1);

        assertEquals(HASH_CODE, fModel0.hashCode(), model0.hashCode());
        assertEquals(HASH_CODE, fModel1.hashCode(), model1.hashCode());

        assertNotEquals(HASH_CODE, fModel0.hashCode(), model1.hashCode());
        assertNotEquals(HASH_CODE, fModel1.hashCode(), model0.hashCode());
    }

    // ------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------

    private static TableColumnDescriptor createDescriptor(int i) {
        TableColumnDescriptor.Builder builder = new TableColumnDescriptor.Builder();
        return builder.setText(COLUMN_TEXT0 + String.valueOf(i)).setTooltip(COLUMN_TOOLTIP0 + String.valueOf(i)).build();
    }
}
