/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.junit.Test;

/**
 * Test suite for the TmfEventField class.
 */
@SuppressWarnings({"nls", "javadoc"})
public class TmfEventFieldTest {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private final String fFieldName1 = "Field-1";
    private final String fFieldName2 = "Field-2";

    private final Object fValue1 = "Value";
    private final Object fValue2 = Integer.valueOf(10);

    private final TmfEventField fField1 = new TmfEventField(fFieldName1, fValue1);
    private final TmfEventField fField2 = new TmfEventField(fFieldName2, fValue2, null);
    private final TmfEventField fField3 = new TmfEventField(fFieldName1, fValue2, null);

    private final String fStructRootFieldName = "Root-S";
    private final String[] fStructFieldNames = new String[] { fFieldName1, fFieldName2 };
    private final TmfEventField fStructTerminalField1 = new TmfEventField(fFieldName1, null);
    private final TmfEventField fStructTerminalField2 = new TmfEventField(fFieldName2, null);
    private final TmfEventField fStructTerminalField3 = new TmfEventField(fFieldName1, null);
    private final TmfEventField fStructRootField = new TmfEventField(fStructRootFieldName,
            new ITmfEventField[] { fStructTerminalField1, fStructTerminalField2 });

    private final String fRootFieldName = "Root";
    private final String[] fFieldNames = new String[] { fFieldName1, fFieldName2 };
    private final TmfEventField fRootField = new TmfEventField(fRootFieldName,
            new ITmfEventField[] { fField1, fField2 });

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    @Test
    public void testTerminalStructConstructor() {
        assertSame("getName", fFieldName1, fStructTerminalField1.getName());
        assertNull("getValue", fStructTerminalField1.getValue());
        assertEquals("getFields", 0, fStructTerminalField1.getFields().length);
        assertNull("getField(name)", fStructTerminalField1.getField(fFieldName1));
        assertNull("getField(index)", fStructTerminalField1.getField(0));
        assertEquals("getFieldNames", 0, fStructTerminalField1.getFieldNames().length);
        assertNull("getFieldName", fStructTerminalField1.getFieldName(-1));
        assertNull("getFieldName", fStructTerminalField1.getFieldName(0));
    }

    @Test
    public void testNonTerminalStructConstructor() {
        assertSame("getName", fStructRootFieldName, fStructRootField.getName());
        assertNull("getValue", fStructRootField.getValue());
        assertEquals("getFields", 2, fStructRootField.getFields().length);
        assertSame("getField(name)", fStructTerminalField1, fStructRootField.getField(fFieldName1));
        assertSame("getField(name)", fStructTerminalField2, fStructRootField.getField(fFieldName2));
        assertSame("getField(index)", fStructTerminalField1, fStructRootField.getField(0));
        assertSame("getField(index)", fStructTerminalField2, fStructRootField.getField(1));

        final String[] names = fStructRootField.getFieldNames();
        assertEquals("getFieldNames length", 2, names.length);
        for (int i = 0; i < names.length; i++) {
            assertSame("getFieldNames", fStructFieldNames[i], names[i]);
            assertSame("getFieldName", fFieldNames[i], fStructRootField.getFieldName(i));
        }
        assertNull("getFieldName", fStructRootField.getFieldName(-1));
        assertNull("getFieldName", fStructRootField.getFieldName(names.length));
    }

    @Test
    public void testTerminalConstructor() {
        assertSame("getName", fFieldName1, fField1.getName());
        assertSame("getValue", fValue1, fField1.getValue());
        assertEquals("getFields", 0, fField1.getFields().length);
        assertNull("getField(name)", fField1.getField(fFieldName1));
        assertNull("getField(index)", fField1.getField(0));
        assertEquals("getFieldNames", 0, fField1.getFieldNames().length);
        assertNull("getFieldName", fField1.getFieldName(0));

        assertSame("getName", fFieldName2, fField2.getName());
        assertSame("getValue", fValue2, fField2.getValue());
        assertEquals("getFields", 0, fField2.getFields().length);
        assertNull("getField(name)", fField2.getField(fFieldName2));
        assertNull("getField(index)", fField2.getField(0));
        assertEquals("getFieldNames", 0, fField2.getFieldNames().length);
        assertNull("getFieldName", fField2.getFieldName(0));
    }

    @Test
    public void testNonTerminalConstructor() {
        assertSame("getName", fRootFieldName, fRootField.getName());
        assertNull("getValue", fRootField.getValue());
        assertEquals("getFields", 2, fRootField.getFields().length);
        assertSame("getField(name)", fField1, fRootField.getField(fFieldName1));
        assertSame("getField(name)", fField2, fRootField.getField(fFieldName2));
        assertSame("getField(index)", fField1, fRootField.getField(0));
        assertSame("getField(index)", fField2, fRootField.getField(1));

        final String[] names = fRootField.getFieldNames();
        assertEquals("getFieldNames length", 2, names.length);
        for (int i = 0; i < names.length; i++) {
            assertSame("getFieldNames", fFieldNames[i], names[i]);
            assertSame("getFieldName", fFieldNames[i], fRootField.getFieldName(i));
        }
        assertNull("getFieldName", fRootField.getFieldName(-1));
        assertNull("getFieldName", fRootField.getFieldName(names.length));
    }

    @Test
    public void testConstructorBadArg() {
        try {
            new TmfEventField(null, fValue1, null);
            fail("Invalid (null) field name");
        } catch (final IllegalArgumentException e) {
        }
    }

    @Test
    public void testTerminalCopyConstructor() {
        final TmfEventField copy = new TmfEventField(fField1);
        assertSame("getName", fFieldName1, copy.getName());
        assertSame("getValue", fValue1, copy.getValue());
        assertEquals("getFields", 0, copy.getFields().length);
        assertNull("getField(name)", copy.getField(fFieldName1));
        assertNull("getField(index)", copy.getField(0));
        assertEquals("getFieldNames", 0, copy.getFieldNames().length);
        assertNull("getFieldName", copy.getFieldName(0));
    }

    @Test
    public void testNonTerminalCopyConstructor() {
        assertSame("getName", fRootFieldName, fRootField.getName());
        assertNull("getValue", fRootField.getValue());
        assertEquals("getFields", 2, fRootField.getFields().length);
        assertSame("getField(name)", fField1, fRootField.getField(fFieldName1));
        assertSame("getField(name)", fField2, fRootField.getField(fFieldName2));
        assertSame("getField(index)", fField1, fRootField.getField(0));
        assertSame("getField(index)", fField2, fRootField.getField(1));

        final String[] names = fRootField.getFieldNames();
        assertEquals("getFieldNames length", 2, names.length);
        for (int i = 0; i < names.length; i++) {
            assertSame("getFieldNames", fFieldNames[i], names[i]);
            assertSame("getFieldName", fFieldNames[i], fRootField.getFieldName(i));
        }
        assertNull("getFieldName", fRootField.getFieldName(names.length));
    }

    @Test
    public void testCopyConstructorBadArg() {
        try {
            new TmfEventField(null);
            fail("TmfEventField: null arguemnt");
        } catch (final IllegalArgumentException e) {
        }
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    @Test
    public void testHashCode() {
        TmfEventField copy = new TmfEventField(fField1);
        assertTrue("hashCode", fField1.hashCode() == copy.hashCode());
        assertTrue("hashCode", fField1.hashCode() != fField2.hashCode());

        copy = new TmfEventField(fStructTerminalField1);
        assertTrue("hashCode", fStructTerminalField1.hashCode() == copy.hashCode());
        assertTrue("hashCode", fStructTerminalField1.hashCode() != fStructTerminalField2.hashCode());
    }

    // ------------------------------------------------------------------------
    // equals
    // ------------------------------------------------------------------------

    @Test
    public void testEqualsReflexivity() {
        assertTrue("equals", fField1.equals(fField1));
        assertTrue("equals", fField2.equals(fField2));

        assertFalse("equals", fField1.equals(fField2));
        assertFalse("equals", fField2.equals(fField1));

        assertTrue("equals", fStructTerminalField1.equals(fStructTerminalField1));
        assertTrue("equals", fStructTerminalField2.equals(fStructTerminalField2));

        assertFalse("equals", fStructTerminalField1.equals(fStructTerminalField2));
        assertFalse("equals", fStructTerminalField2.equals(fStructTerminalField1));
    }

    @Test
    public void testEqualsSymmetry() {
        final TmfEventField copy0 = new TmfEventField(fField1);
        assertTrue("equals", fField1.equals(copy0));
        assertTrue("equals", copy0.equals(fField1));

        final TmfEventField copy3 = new TmfEventField(fField2);
        assertTrue("equals", fField2.equals(copy3));
        assertTrue("equals", copy3.equals(fField2));
    }

    @Test
    public void testEqualsTransivity() {
        TmfEventField copy1 = new TmfEventField(fField1);
        TmfEventField copy2 = new TmfEventField(copy1);
        assertTrue("equals", fField1.equals(copy1));
        assertTrue("equals", copy1.equals(copy2));
        assertTrue("equals", fField1.equals(copy2));

        copy1 = new TmfEventField(fField2);
        copy2 = new TmfEventField(copy1);
        assertTrue("equals", fField2.equals(copy1));
        assertTrue("equals", copy1.equals(copy2));
        assertTrue("equals", fField2.equals(copy2));
    }

    @Test
    public void testEquals() {
        assertTrue("equals", fStructTerminalField1.equals(fStructTerminalField3));
        assertTrue("equals", fStructTerminalField3.equals(fStructTerminalField1));

        assertFalse("equals", fStructTerminalField1.equals(fField3));
        assertFalse("equals", fField3.equals(fStructTerminalField1));
    }

    @Test
    public void testEqualsNull() {
        assertFalse("equals", fField1.equals(null));
        assertFalse("equals", fField2.equals(null));
    }

    @Test
    public void testNonEqualClasses() {
        assertFalse("equals", fField1.equals(fStructTerminalField1));
        assertFalse("equals", fField1.equals(fValue1));
    }

    @Test
    public void testNonEqualValues() {
        final TmfEventField copy1 = new TmfEventField(fFieldName1, fValue1);
        TmfEventField copy2 = new TmfEventField(fFieldName1, fValue1);
        assertTrue("equals", copy1.equals(copy2));
        assertTrue("equals", copy2.equals(copy1));

        copy2 = new TmfEventField(fFieldName1, fValue2);
        assertFalse("equals", copy1.equals(copy2));
        assertFalse("equals", copy2.equals(copy1));

        copy2 = new TmfEventField(fFieldName1, null);
        assertFalse("equals", copy1.equals(copy2));
        assertFalse("equals", copy2.equals(copy1));
    }

    @Test
    public void testNonEquals() {
        assertFalse("equals", fField1.equals(fField2));
        assertFalse("equals", fField2.equals(fField1));

        assertFalse("equals", fField1.equals(fStructTerminalField1));
    }

    // ------------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------------

    @Test
    public void testToString() {
        final String expected1 = fFieldName1 + "=" + fValue1.toString();
        TmfEventField field = new TmfEventField(fFieldName1, fValue1, null);
        assertEquals("toString", expected1, field.toString());

        final String expected2 = fFieldName1 + "=" + fValue2.toString();
        field = new TmfEventField(fFieldName1, fValue2, null);
        assertEquals("toString", expected2, field.toString());
    }

    // ------------------------------------------------------------------------
    // makeRoot
    // ------------------------------------------------------------------------

    @Test
    public void testMakeRoot() {
        ITmfEventField root = TmfEventField.makeRoot(fStructFieldNames);
        String[] names = root.getFieldNames();
        assertEquals("getFieldNames length", 2, names.length);
        for (int i = 0; i < names.length; i++) {
            assertSame("getFieldNames", fStructFieldNames[i], names[i]);
            assertSame("getFieldName", fStructFieldNames[i], root.getFieldName(i));
            assertNull("getValue", root.getField(i).getValue());
        }
        assertNull("getFieldName", root.getFieldName(-1));
        assertNull("getFieldName", root.getFieldName(names.length));

        root = TmfEventField.makeRoot(fFieldNames);
        names = root.getFieldNames();
        assertEquals("getFieldNames length", 2, names.length);
        for (int i = 0; i < names.length; i++) {
            assertSame("getFieldNames", fFieldNames[i], names[i]);
            assertSame("getFieldName", fFieldNames[i], root.getFieldName(i));
            assertNull("getValue", root.getField(i).getValue());
        }
        assertNull("getFieldName", root.getFieldName(-1));
        assertNull("getFieldName", root.getFieldName(names.length));
    }

}
