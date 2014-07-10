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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;

import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.junit.Test;

/**
 * Test suite for the TmfEventField class.
 */
@SuppressWarnings("javadoc")
public class TmfEventFieldTest {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private final String fFieldName1 = "Field-1";
    private final String fFieldName2 = "Field-2";

    private final Object fValue1 = "Value";
    private final Object fValue2 = Integer.valueOf(10);

    private final TmfEventField fField1 = new TmfEventField(fFieldName1, fValue1, null);
    private final TmfEventField fField2 = new TmfEventField(fFieldName2, fValue2, null);
    private final TmfEventField fField3 = new TmfEventField(fFieldName1, fValue2, null);

    private final String fStructRootFieldName = "Root-S";
    private final String[] fStructFieldNames = new String[] { fFieldName1, fFieldName2 };
    private final TmfEventField fStructTerminalField1 = new TmfEventField(fFieldName1, null, null);
    private final TmfEventField fStructTerminalField2 = new TmfEventField(fFieldName2, null, null);
    private final TmfEventField fStructTerminalField3 = new TmfEventField(fFieldName1, null, null);
    private final TmfEventField fStructRootField = new TmfEventField(fStructRootFieldName, null,
            new ITmfEventField[] { fStructTerminalField1, fStructTerminalField2 });

    private final String fRootFieldName = "Root";
    private final String[] fFieldNames = new String[] { fFieldName1, fFieldName2 };
    private final TmfEventField fRootField = new TmfEventField(fRootFieldName, null,
            new ITmfEventField[] { fField1, fField2 });

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    @Test
    public void testTerminalStructConstructor() {
        assertSame("getName", fFieldName1, fStructTerminalField1.getName());
        assertNull("getValue", fStructTerminalField1.getValue());
        assertEquals("getFields", 0, fStructTerminalField1.getFields().size());
        assertNull("getField(name)", fStructTerminalField1.getField(fFieldName1));
        assertEquals("getFieldNames", 0, fStructTerminalField1.getFieldNames().size());
    }

    @Test
    public void testNonTerminalStructConstructor() {
        assertSame("getName", fStructRootFieldName, fStructRootField.getName());
        assertNull("getValue", fStructRootField.getValue());
        assertEquals("getFields", 2, fStructRootField.getFields().size());
        assertSame("getField(name)", fStructTerminalField1, fStructRootField.getField(fFieldName1));
        assertSame("getField(name)", fStructTerminalField2, fStructRootField.getField(fFieldName2));

        final Collection<String> names = fStructRootField.getFieldNames();
        assertEquals("getFieldNames length", 2, names.size());
        assertArrayEquals(fStructFieldNames, names.toArray(new String[names.size()]));
    }

    @Test
    public void testTerminalConstructor() {
        assertSame("getName", fFieldName1, fField1.getName());
        assertSame("getValue", fValue1, fField1.getValue());
        assertEquals("getFields", 0, fField1.getFields().size());
        assertNull("getField(name)", fField1.getField(fFieldName1));
        assertEquals("getFieldNames", 0, fField1.getFieldNames().size());

        assertSame("getName", fFieldName2, fField2.getName());
        assertSame("getValue", fValue2, fField2.getValue());
        assertEquals("getFields", 0, fField2.getFields().size());
        assertNull("getField(name)", fField2.getField(fFieldName2));
    }

    @Test
    public void testNonTerminalConstructor() {
        assertSame("getName", fRootFieldName, fRootField.getName());
        assertNull("getValue", fRootField.getValue());
        assertEquals("getFields", 2, fRootField.getFields().size());
        assertSame("getField(name)", fField1, fRootField.getField(fFieldName1));
        assertSame("getField(name)", fField2, fRootField.getField(fFieldName2));

        final Collection<String> names = fRootField.getFieldNames();
        assertEquals("getFieldNames length", 2, names.size());
        assertArrayEquals(fFieldNames, names.toArray(new String[names.size()]));
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
        assertEquals("getFields", 0, copy.getFields().size());
        assertNull("getField(name)", copy.getField(fFieldName1));
        assertEquals("getFieldNames", 0, copy.getFieldNames().size());
    }

    @Test
    public void testNonTerminalCopyConstructor() {
        assertSame("getName", fRootFieldName, fRootField.getName());
        assertNull("getValue", fRootField.getValue());
        assertEquals("getFields", 2, fRootField.getFields().size());
        assertSame("getField(name)", fField1, fRootField.getField(fFieldName1));
        assertSame("getField(name)", fField2, fRootField.getField(fFieldName2));

        final Collection<String> names = fRootField.getFieldNames();
        assertEquals("getFieldNames length", 2, names.size());
        assertArrayEquals(fFieldNames, names.toArray(new String[names.size()]));
    }

    @Test
    public void testCopyConstructorBadArg() {
        try {
            new TmfEventField(null);
            fail("TmfEventField: null arguemnt");
        } catch (final IllegalArgumentException e) {
        }
    }

    /**
     * Test that we correctly fail to create a field with subfields having the
     * same name.
     */
    @Test
    public void testDuplicateFieldNames() {
        ITmfEventField[] fields = {
                new TmfEventField("samename", null, null),
                new TmfEventField("samename", null, null)
        };

        try {
            new TmfEventField("field", null, fields);
            fail("TmfEventField: Duplicate field names");
        } catch (IllegalArgumentException e) {
            /* Expected exception */
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
        final TmfEventField copy1 = new TmfEventField(fFieldName1, fValue1, null);
        TmfEventField copy2 = new TmfEventField(fFieldName1, fValue1, null);
        assertTrue("equals", copy1.equals(copy2));
        assertTrue("equals", copy2.equals(copy1));

        copy2 = new TmfEventField(fFieldName1, fValue2, null);
        assertFalse("equals", copy1.equals(copy2));
        assertFalse("equals", copy2.equals(copy1));

        copy2 = new TmfEventField(fFieldName1, null, null);
        assertFalse("equals", copy1.equals(copy2));
        assertFalse("equals", copy2.equals(copy1));
    }

    @Test
    public void testNonEquals() {
        assertFalse("equals", fField1.equals(fField2));
        assertFalse("equals", fField2.equals(fField1));

        assertFalse("equals", fField1.equals(fStructTerminalField1));
    }

    /**
     * Test with same fields, but different values (should not be equal)
     */
    @Test
    public void testNonEqualsValue() {
        final String fieldName = "myfield";
        final Object value1 = new String("test-string");
        final Object value2 = new TmfEvent();
        final TmfEventField[] fields = { fField1, fField2 };

        final TmfEventField field1 = new TmfEventField(fieldName, value1, fields);
        final TmfEventField field2 = new TmfEventField(fieldName, value2, fields);

        assertNotEquals(field1, field2);
        assertNotEquals(field2, field1);
    }

    /**
     * Test with same value, but different fields (should not be equal)
     */
    @Test
    public void testNonEqualsFields() {
        final String fieldName = "myfield";
        final Object value = new String("test-string");
        final TmfEventField[] fields1 = { fField1, fField2 };
        final TmfEventField[] fields2 = { fField2, fField3 };

        final TmfEventField field1 = new TmfEventField(fieldName, value, fields1);
        final TmfEventField field2 = new TmfEventField(fieldName, value, fields2);

        assertNotEquals(field1, field2);
        assertNotEquals(field2, field1);
    }

    /**
     * Test with same field and values (should be equals)
     */
    @Test
    public void testEqualsEverything() {
        final String fieldName = "myfield";
        final Object value = new String("test-string");
        final TmfEventField[] fields = { fField1, fField2 };

        final TmfEventField field1 = new TmfEventField(fieldName, value, fields);
        final TmfEventField field2 = new TmfEventField(fieldName, value, fields);

        assertEquals(field1, field2);
        assertEquals(field2, field1);
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
        Collection<String> names = root.getFieldNames();
        assertEquals("getFieldNames length", 2, names.size());
        assertArrayEquals(fStructFieldNames, names.toArray(new String[names.size()]));

        root = TmfEventField.makeRoot(fFieldNames);
        names = root.getFieldNames();
        assertEquals("getFieldNames length", 2, names.size());
        assertArrayEquals(fFieldNames, names.toArray(new String[names.size()]));
    }
}
