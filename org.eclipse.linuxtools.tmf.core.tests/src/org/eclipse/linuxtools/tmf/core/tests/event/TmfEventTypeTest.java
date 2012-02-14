/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.event;

import junit.framework.TestCase;

import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;

/**
 * <b><u>TmfEventTypeTest</u></b>
 * <p>
 * JUnit test suite for the TmfEventType class.
 */
@SuppressWarnings("nls")
public class TmfEventTypeTest extends TestCase {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private final String fContext = TmfEventType.DEFAULT_CONTEXT_ID;
    private final String fTypeId  = "Some type";
    private final String fTypeId2 = "Some other type";
    private final String fLabel0 = "label1";
    private final String fLabel1 = "label2";
    private final String[] fLabels1  = new String[] { fLabel0, fLabel1 };
    private final String[] fLabels2  = new String[] { fLabel1, fLabel0 };

    private final TmfEventType fType0 = new TmfEventType(fContext, fTypeId,  TmfEventField.makeRoot(fLabels1));
    private final TmfEventType fType1 = new TmfEventType(fContext, fTypeId,  TmfEventField.makeRoot(fLabels1));
    private final TmfEventType fType2 = new TmfEventType(fContext, fTypeId,  TmfEventField.makeRoot(fLabels1));
    private final TmfEventType fType3 = new TmfEventType(fContext, fTypeId2, TmfEventField.makeRoot(fLabels2));

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

    public void testTmfEventTypeDefault() {
        TmfEventType type = new TmfEventType();
        assertEquals("getTypeId", TmfEventType.DEFAULT_TYPE_ID, type.getName());
        assertNull("getLabels", type.getFieldName(0));

//            assertEquals("getFieldIndex", 0, type.getFieldIndex(fLabel0));
//            fail("getFieldIndex: no such field");
            assertNull("getLabel", type.getFieldName(0));
//            fail("getFieldIndex: no such field");
    }

    public void testTmfEventType() {
        TmfEventType type = new TmfEventType(fContext, fTypeId, TmfEventField.makeRoot(fLabels1));
        String[] expected = new String[] { fLabel0, fLabel1 };
//        try {
            assertEquals("getTypeId", fTypeId, type.getName());
//            assertEquals("getFieldIndex", 0, type.getFieldIndex(fLabel0));
//            assertEquals("getFieldIndex", 1, type.getFieldIndex(fLabel1));
            String[] labels = type.getFieldNames();
            for (int i = 0; i < labels.length; i++) {
                assertEquals("getLabels", expected[i], labels[i]);
            }
            assertEquals("getLabel", fLabel0, type.getFieldName(0));
            assertEquals("getLabel", fLabel1, type.getFieldName(1));
//        } catch (TmfNoSuchFieldException e) {
//            fail("getFieldIndex: no such field");
//        }

//        try {
//            assertEquals("getFieldIndex", 0, type.getFieldIndex("Dummy"));
//            fail("getFieldIndex: inexistant field");
//        } catch (TmfNoSuchFieldException e) {
//            // Success
//        }

//        try {
            assertNull(type.getFieldName(10));
//            fail("getLabel: inexistant field");
//        } catch (TmfNoSuchFieldException e) {
//            // Success
//        }
    }

    public void testTmfEventType2() {
        try {
            new TmfEventType(null, fTypeId, null);
            fail("TmfEventType: bad constructor");
        } catch (IllegalArgumentException e) {
            // Success
        }
    }

    public void testTmfEventType3() {
        try {
            new TmfEventType(fContext, null, null);
            fail("TmfEventType: bad constructor");
        } catch (IllegalArgumentException e) {
            // Success
        }
    }

    public void testTmfEventTypeCopy() {
        TmfEventType original = new TmfEventType(fContext, fTypeId, TmfEventField.makeRoot(fLabels1));
        TmfEventType copy = new TmfEventType(original);
        String[] expected = new String[] { fLabel0, fLabel1 };

        assertEquals("getTypeId", fTypeId, copy.getName());
        String[] labels = copy.getFieldNames();
        for (int i = 0; i < labels.length; i++) {
            assertEquals("getLabels", expected[i], labels[i]);
        }
    }

    public void testTmfEventSourceCopy2() {
        try {
            @SuppressWarnings("unused")
            TmfEventType type = new TmfEventType(null);
            fail("null copy");
        } catch (IllegalArgumentException e) {
            // Success
        }
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
        assertTrue("equals", fType0.equals(fType1));
        assertTrue("equals", fType1.equals(fType0));

        assertTrue("equals", !fType0.equals(fType3));
        assertTrue("equals", !fType3.equals(fType0));
    }

    public void testEqualsTransivity() throws Exception {
        assertTrue("equals", fType0.equals(fType1));
        assertTrue("equals", fType1.equals(fType2));
        assertTrue("equals", fType0.equals(fType2));
    }

    public void testEqualsNull() throws Exception {
        assertTrue("equals", !fType0.equals(null));
        assertTrue("equals", !fType3.equals(null));
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    public void testHashCode() throws Exception {
        assertTrue("hashCode", fType0.hashCode() == fType1.hashCode());
        assertTrue("hashCode", fType0.hashCode() != fType3.hashCode());
    }

    // ------------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------------

    public void testToString() {
        String expected1 = "TmfEventType [fContext=" + TmfEventType.DEFAULT_CONTEXT_ID +
               ", fTypeId=" + TmfEventType.DEFAULT_TYPE_ID + "]";
        TmfEventType type1 = new TmfEventType();
        assertEquals("toString", expected1, type1.toString());

        String expected2 = "TmfEventType [fContext=" + fContext + ", fTypeId=" + fTypeId + "]";
        TmfEventType type2 = new TmfEventType(fContext, fTypeId, TmfEventField.makeRoot(fLabels1));
        assertEquals("toString", expected2, type2.toString());
    }

}
