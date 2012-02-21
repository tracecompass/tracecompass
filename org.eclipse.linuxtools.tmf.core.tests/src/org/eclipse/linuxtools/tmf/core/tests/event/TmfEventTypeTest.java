/*******************************************************************************
 * Copyright (c) 2009, 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Adjusted for new Event Model
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.event;

import junit.framework.TestCase;

import org.eclipse.linuxtools.tmf.core.event.ITmfEventType;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;

/**
 * <b><u>TmfEventTypeTest</u></b>
 * <p>
 * Test suite for the TmfEventType class.
 */
@SuppressWarnings("nls")
public class TmfEventTypeTest extends TestCase {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private final String fContext1 = "JUnit context 1";
    private final String fContext2 = "JUnit context 2";

    private final String fTypeId1  = "Some type";
    private final String fTypeId2 = "Some other type";

    private final String fLabel0 = "label1";
    private final String fLabel1 = "label2";
    
    private final String[] fLabels0  = new String[] { };
    private final String[] fLabels1  = new String[] { fLabel0, fLabel1 };
    private final String[] fLabels2  = new String[] { fLabel1, fLabel0, fLabel1 };

    private final TmfEventType fType0 = new TmfEventType(fContext1, fTypeId1, TmfEventField.makeRoot(fLabels0));
    private final TmfEventType fType1 = new TmfEventType(fContext1, fTypeId2, TmfEventField.makeRoot(fLabels1));
    private final TmfEventType fType2 = new TmfEventType(fContext2, fTypeId1, TmfEventField.makeRoot(fLabels2));
    private final TmfEventType fType3 = new TmfEventType(fContext2, fTypeId2, TmfEventField.makeRoot(fLabels1));

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    /**
     * @param name the test name
     */
    public TmfEventTypeTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public void testDefaultConstructor() {
        ITmfEventType type = new TmfEventType();
        assertEquals("getContext", TmfEventType.DEFAULT_CONTEXT_ID, type.getContext());
        assertEquals("getName", TmfEventType.DEFAULT_TYPE_ID, type.getName());
        assertNull("getRootField", type.getRootField());
        assertNull("getFieldNames", type.getFieldNames());
        assertNull("getFieldName", type.getFieldName(0));
    }

    public void testFullConstructor() {
        ITmfEventType type0 = new TmfEventType(fContext1, fTypeId1, TmfEventField.makeRoot(fLabels0));
        assertEquals("getContext", fContext1, type0.getContext());
        assertEquals("getName", fTypeId1, type0.getName());
        assertEquals("getRootField", TmfEventField.makeRoot(fLabels0), type0.getRootField());
        String[] labels0 = type0.getFieldNames();
        assertEquals("getFieldNames length", fLabels0.length, labels0.length);
        for (int i = 0; i < labels0.length; i++) {
            assertEquals("getFieldNames", fLabels0[i], labels0[i]);
        }
        assertNull("getFieldName", type0.getFieldName(labels0.length));

        ITmfEventType type1 = new TmfEventType(fContext1, fTypeId1, TmfEventField.makeRoot(fLabels1));
        assertEquals("getContext", fContext1, type1.getContext());
        assertEquals("getName", fTypeId1, type1.getName());
        assertEquals("getRootField", TmfEventField.makeRoot(fLabels1), type1.getRootField());
        String[] labels1 = type1.getFieldNames();
        assertEquals("getFieldNames length", fLabels1.length, labels1.length);
        for (int i = 0; i < labels1.length; i++) {
            assertEquals("getFieldNames", fLabels1[i], labels1[i]);
        }
        assertNull("getFieldName", type1.getFieldName(labels1.length));

        ITmfEventType type2 = new TmfEventType(fContext2, fTypeId2, TmfEventField.makeRoot(fLabels2));
        assertEquals("getContext", fContext2, type2.getContext());
        assertEquals("getName", fTypeId2, type2.getName());
        assertEquals("getRootField", TmfEventField.makeRoot(fLabels2), type2.getRootField());
        String[] labels2 = type2.getFieldNames();
        assertEquals("getFieldNames length", fLabels2.length, labels2.length);
        for (int i = 0; i < labels2.length; i++) {
            assertEquals("getFieldNames", fLabels2[i], labels2[i]);
        }
        assertNull("getFieldName", type2.getFieldName(labels2.length));
    }

    public void testConstructorCornerCases() {
        try {
            new TmfEventType(null, fTypeId1, null);
            fail("TmfEventType: null context");
        } catch (IllegalArgumentException e) {
        }

        try {
            new TmfEventType(fContext1, null, null);
            fail("TmfEventType: null type");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testCopyConstructor() {
        TmfEventType original = new TmfEventType(fContext1, fTypeId1, TmfEventField.makeRoot(fLabels1));
        TmfEventType copy = new TmfEventType(original);

        assertEquals("getContext", fContext1, copy.getContext());
        assertEquals("getName", fTypeId1, copy.getName());
        assertEquals("getRootField", TmfEventField.makeRoot(fLabels1), copy.getRootField());
        String[] labels1 = copy.getFieldNames();
        assertEquals("getFieldNames length", fLabels1.length, labels1.length);
        for (int i = 0; i < labels1.length; i++) {
            assertEquals("getFieldNames", fLabels1[i], labels1[i]);
        }
        assertNull("getFieldName", copy.getFieldName(labels1.length));
    }

    public void testCopyConstructorCornerCases() {
        try {
            new TmfEventType(null);
            fail("TmfEventType: null argument");
        } catch (IllegalArgumentException e) {
        }
    }

    // ------------------------------------------------------------------------
    // clone
    // ------------------------------------------------------------------------

    public class MyEventType extends TmfEventType {

        @Override
        public boolean equals(Object other) {
            return super.equals(other);
        }

        @Override
        public MyEventType clone() {
            return (MyEventType) super.clone();
        }
    }

    public void testClone() throws Exception {
        ITmfEventType clone = fType1.clone();
        assertEquals("clone", fType1, clone);
    }

    public void testClone2() throws Exception {
        ITmfEventType type = new TmfEventType();
        ITmfEventType clone = type.clone();
        assertEquals("clone", clone, type);
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    public void testHashCode() throws Exception {
        TmfEventType copy1 = new TmfEventType(fType0);

        assertTrue("hashCode", fType0.hashCode() == copy1.hashCode());
        assertTrue("hashCode", fType0.hashCode() != fType3.hashCode());
    }

    // ------------------------------------------------------------------------
    // equals
    // ------------------------------------------------------------------------

    public void testEqualsReflexivity() throws Exception {
        assertTrue("equals", fType0.equals(fType0));
        assertTrue("equals", fType3.equals(fType3));

        assertTrue("equals", !fType0.equals(fType3));
        assertTrue("equals", !fType3.equals(fType0));
    }

    public void testEqualsSymmetry() throws Exception {
        TmfEventType copy0 = new TmfEventType(fType0);
        assertTrue("equals", fType0.equals(copy0));
        assertTrue("equals", copy0.equals(fType0));
        
        TmfEventType copy1 = new TmfEventType(fType1);
        assertTrue("equals", fType1.equals(copy1));
        assertTrue("equals", copy1.equals(fType1));
        
        TmfEventType copy2 = new TmfEventType(fType2);
        assertTrue("equals", fType2.equals(copy2));
        assertTrue("equals", copy2.equals(fType2));
    }

    public void testEqualsTransivity() throws Exception {
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

    public void testEqualsNull() throws Exception {
        assertFalse("equals", fType0.equals(null));
        assertFalse("equals", fType3.equals(null));
    }

    public void testNonEquals() throws Exception {
        assertFalse("equals", fType0.equals(fType1));
        assertFalse("equals", fType1.equals(fType2));
        assertFalse("equals", fType2.equals(fType3));
        assertFalse("equals", fType3.equals(fType0));
    }
    
    public void testEqualsNonType() throws Exception {
        assertFalse("equals", fType1.equals(fLabels1));
    }
    
    // ------------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------------

    public void testToString() {
        String expected1 = "TmfEventType [fContext=" + TmfEventType.DEFAULT_CONTEXT_ID +
               ", fTypeId=" + TmfEventType.DEFAULT_TYPE_ID + "]";
        TmfEventType type1 = new TmfEventType();
        assertEquals("toString", expected1, type1.toString());

        String expected2 = "TmfEventType [fContext=" + fContext1 + ", fTypeId=" + fTypeId1 + "]";
        TmfEventType type2 = new TmfEventType(fContext1, fTypeId1, TmfEventField.makeRoot(fLabels1));
        assertEquals("toString", expected2, type2.toString());
    }

}
