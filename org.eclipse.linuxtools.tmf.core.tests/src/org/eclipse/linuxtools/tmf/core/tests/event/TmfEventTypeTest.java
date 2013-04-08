/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Adjusted for new Event Model
 *   Alexandre Montplaisir - Port to JUnit4
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.linuxtools.tmf.core.event.ITmfEventType;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;
import org.junit.Test;

/**
 * Test suite for the TmfEventType class.
 */
@SuppressWarnings("javadoc")
public class TmfEventTypeTest {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private final String fContext1 = "JUnit context 1";
    private final String fContext2 = "JUnit context 2";

    private final String fTypeId1 = "Some type";
    private final String fTypeId2 = "Some other type";

    private final String fLabel0 = "label1";
    private final String fLabel1 = "label2";

    private final String[] fLabels0 = new String[] { };
    private final String[] fLabels1 = new String[] { fLabel0, fLabel1 };
    private final String[] fLabels2 = new String[] { fLabel1, fLabel0, fLabel1 };

    private final ITmfEventType fType0 = new TmfEventType(fContext1, fTypeId1, TmfEventField.makeRoot(fLabels0));
    private final ITmfEventType fType1 = new TmfEventType(fContext1, fTypeId2, TmfEventField.makeRoot(fLabels1));
    private final ITmfEventType fType2 = new TmfEventType(fContext2, fTypeId1, TmfEventField.makeRoot(fLabels2));
    private final ITmfEventType fType3 = new TmfEventType(fContext2, fTypeId2, TmfEventField.makeRoot(fLabels1));

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    @Test
    public void testDefaultConstructor() {
        final ITmfEventType type = new TmfEventType();
        assertEquals("getContext", ITmfEventType.DEFAULT_CONTEXT_ID, type.getContext());
        assertEquals("getName", ITmfEventType.DEFAULT_TYPE_ID, type.getName());
        assertNull("getRootField", type.getRootField());
        assertEquals("getFieldNames", 0, type.getFieldNames().length);
        assertNull("getFieldName", type.getFieldName(0));
    }

    @Test
    public void testFullConstructor() {
        final ITmfEventType type0 = new TmfEventType(fContext1, fTypeId1, TmfEventField.makeRoot(fLabels0));
        assertEquals("getContext", fContext1, type0.getContext());
        assertEquals("getName", fTypeId1, type0.getName());
        assertEquals("getRootField", TmfEventField.makeRoot(fLabels0), type0.getRootField());
        final String[] labels0 = type0.getFieldNames();
        assertEquals("getFieldNames length", fLabels0.length, labels0.length);
        for (int i = 0; i < labels0.length; i++) {
            assertEquals("getFieldNames", fLabels0[i], labels0[i]);
        }
        assertNull("getFieldName", type0.getFieldName(labels0.length));

        final ITmfEventType type1 = new TmfEventType(fContext1, fTypeId1, TmfEventField.makeRoot(fLabels1));
        assertEquals("getContext", fContext1, type1.getContext());
        assertEquals("getName", fTypeId1, type1.getName());
        assertEquals("getRootField", TmfEventField.makeRoot(fLabels1), type1.getRootField());
        final String[] labels1 = type1.getFieldNames();
        assertEquals("getFieldNames length", fLabels1.length, labels1.length);
        for (int i = 0; i < labels1.length; i++) {
            assertEquals("getFieldNames", fLabels1[i], labels1[i]);
        }
        assertNull("getFieldName", type1.getFieldName(labels1.length));

        final ITmfEventType type2 = new TmfEventType(fContext2, fTypeId2, TmfEventField.makeRoot(fLabels2));
        assertEquals("getContext", fContext2, type2.getContext());
        assertEquals("getName", fTypeId2, type2.getName());
        assertEquals("getRootField", TmfEventField.makeRoot(fLabels2), type2.getRootField());
        final String[] labels2 = type2.getFieldNames();
        assertEquals("getFieldNames length", fLabels2.length, labels2.length);
        for (int i = 0; i < labels2.length; i++) {
            assertEquals("getFieldNames", fLabels2[i], labels2[i]);
        }
        assertNull("getFieldName", type2.getFieldName(labels2.length));
    }

    @Test
    public void testConstructorCornerCases() {
        try {
            new TmfEventType(null, fTypeId1, null);
            fail("TmfEventType: null context");
        } catch (final IllegalArgumentException e) {
        }

        try {
            new TmfEventType(fContext1, null, null);
            fail("TmfEventType: null type");
        } catch (final IllegalArgumentException e) {
        }
    }

    @Test
    public void testCopyConstructor() {
        final TmfEventType original = new TmfEventType(fContext1, fTypeId1, TmfEventField.makeRoot(fLabels1));
        final TmfEventType copy = new TmfEventType(original);

        assertEquals("getContext", fContext1, copy.getContext());
        assertEquals("getName", fTypeId1, copy.getName());
        assertEquals("getRootField", TmfEventField.makeRoot(fLabels1), copy.getRootField());
        final String[] labels1 = copy.getFieldNames();
        assertEquals("getFieldNames length", fLabels1.length, labels1.length);
        for (int i = 0; i < labels1.length; i++) {
            assertEquals("getFieldNames", fLabels1[i], labels1[i]);
        }
        assertNull("getFieldName", copy.getFieldName(labels1.length));
    }

    @Test
    public void testCopyConstructorCornerCases() {
        try {
            new TmfEventType(null);
            fail("TmfEventType: null argument");
        } catch (final IllegalArgumentException e) {
        }
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    @Test
    public void testHashCode() {
        final TmfEventType copy1 = new TmfEventType(fType0);

        assertTrue("hashCode", fType0.hashCode() == copy1.hashCode());
        assertTrue("hashCode", fType0.hashCode() != fType3.hashCode());
    }

    // ------------------------------------------------------------------------
    // equals
    // ------------------------------------------------------------------------

    @Test
    public void testEqualsReflexivity() {
        assertTrue("equals", fType0.equals(fType0));
        assertTrue("equals", fType3.equals(fType3));

        assertFalse("equals", fType0.equals(fType3));
        assertFalse("equals", fType3.equals(fType0));
    }

    @Test
    public void testEqualsSymmetry() {
        final TmfEventType copy0 = new TmfEventType(fType0);
        assertTrue("equals", fType0.equals(copy0));
        assertTrue("equals", copy0.equals(fType0));

        final TmfEventType copy1 = new TmfEventType(fType1);
        assertTrue("equals", fType1.equals(copy1));
        assertTrue("equals", copy1.equals(fType1));

        final TmfEventType copy2 = new TmfEventType(fType2);
        assertTrue("equals", fType2.equals(copy2));
        assertTrue("equals", copy2.equals(fType2));
    }

    @Test
    public void testEqualsTransivity() {
        TmfEventType copy1 = new TmfEventType(fType1);
        TmfEventType copy2 = new TmfEventType(copy1);
        assertTrue("equals", fType1.equals(copy1));
        assertTrue("equals", copy1.equals(copy2));
        assertTrue("equals", fType1.equals(copy2));

        copy1 = new TmfEventType(fType2);
        copy2 = new TmfEventType(copy1);
        assertTrue("equals", fType2.equals(copy1));
        assertTrue("equals", copy1.equals(copy2));
        assertTrue("equals", fType2.equals(copy2));

        copy1 = new TmfEventType(fType3);
        copy2 = new TmfEventType(copy1);
        assertTrue("equals", fType3.equals(copy1));
        assertTrue("equals", copy1.equals(copy2));
        assertTrue("equals", fType3.equals(copy2));
    }

    @Test
    public void testEqualsNull() {
        assertFalse("equals", fType0.equals(null));
        assertFalse("equals", fType3.equals(null));
    }

    @Test
    public void testNonEquals() {
        assertFalse("equals", fType0.equals(fType1));
        assertFalse("equals", fType1.equals(fType2));
        assertFalse("equals", fType2.equals(fType3));
        assertFalse("equals", fType3.equals(fType0));
    }

    @Test
    public void testNonEqualsClasses() {
        assertFalse("equals", fType1.equals(fLabels1));
    }

    // ------------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------------

    @Test
    public void testToString() {
        final String expected1 = "TmfEventType [fContext=" + ITmfEventType.DEFAULT_CONTEXT_ID +
                ", fTypeId=" + ITmfEventType.DEFAULT_TYPE_ID + "]";
        final TmfEventType type1 = new TmfEventType();
        assertEquals("toString", expected1, type1.toString());

        final String expected2 = "TmfEventType [fContext=" + fContext1 + ", fTypeId=" + fTypeId1 + "]";
        final TmfEventType type2 = new TmfEventType(fContext1, fTypeId1, TmfEventField.makeRoot(fLabels1));
        assertEquals("toString", expected2, type2.toString());
    }

}
