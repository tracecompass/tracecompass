/**********************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Alexandre Montplaisir - Port to JUnit4
 **********************************************************************/

package org.eclipse.linuxtools.lttng2.control.core.tests.model.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.linuxtools.internal.lttng2.control.core.model.ITraceInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.impl.TraceInfo;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>TraceInfoTest</code> contains test for the class
 * <code>{@link TraceInfo}</code>.
 */
public class TraceInfoTest {

    // ------------------------------------------------------------------------
    // Test data
    // ------------------------------------------------------------------------

    private ITraceInfo fTraceInfo1 = null;
    private ITraceInfo fTraceInfo2 = null;


    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        fTraceInfo1 = new TraceInfo("event1");
        fTraceInfo2 = new TraceInfo("event2");
    }

    // ------------------------------------------------------------------------
    // Tests
    // ------------------------------------------------------------------------

    /**
     * Run the BaseEventInfo() constructor test.
     */
    @Test
    public void testTraceInfo() {
        TraceInfo fixture = new TraceInfo("event");
        assertNotNull(fixture);

        assertEquals("event", fixture.getName());
    }

    /**
     * Test Copy Constructor
     */
    @Test
    public void testTraceInfo2() {
        try {
            String name = null;
            new TraceInfo(name);
            fail("null name in custructor");
        }
        catch (IllegalArgumentException e) {
            // Success
        }
    }

    /**
     * Test Copy Constructor
     */
    @Test
    public void testTraceInfoCopy() {
        TraceInfo info = new TraceInfo((TraceInfo)fTraceInfo1);

        assertEquals(fTraceInfo1.getName(), info.getName());
    }

    /**
     * Test Copy Constructor
     */
    @Test
    public void testTraceCopy2() {
        try {
            TraceInfo info = null;
            new TraceInfo(info);
            fail("null copy");
        }
        catch (IllegalArgumentException e) {
            // Success
        }
    }

    /**
     * Run the void setEventType(String) method test.
     */
    @Test
    public void testSetName() {
        TraceInfo fixture = new TraceInfo("event");
        fixture.setName("newName");
        assertEquals("newName", fixture.getName());
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString_1() {
        String result = fTraceInfo1.toString();

        // add additional test code here
        assertEquals("[TraceInfo(Name=event1)]", result);
    }

    // ------------------------------------------------------------------------
    // equals
    // ------------------------------------------------------------------------

    /**
     * Run the equals() method test.
     */
    @Test
    public void testEqualsReflexivity() {
        assertTrue("equals", fTraceInfo1.equals(fTraceInfo1));
        assertTrue("equals", fTraceInfo2.equals(fTraceInfo2));

        assertTrue("equals", !fTraceInfo1.equals(fTraceInfo2));
        assertTrue("equals", !fTraceInfo2.equals(fTraceInfo1));
    }

    /**
     * Run the equals() method test.
     */
    @Test
    public void testEqualsSymmetry() {
        TraceInfo info1 = new TraceInfo((TraceInfo)fTraceInfo1);
        TraceInfo info2 = new TraceInfo((TraceInfo)fTraceInfo2);

        assertTrue("equals", info1.equals(fTraceInfo1));
        assertTrue("equals", fTraceInfo1.equals(info1));

        assertTrue("equals", info2.equals(fTraceInfo2));
        assertTrue("equals", fTraceInfo2.equals(info2));
    }
    /**
     * Run the equals() method test.
     */
    @Test
    public void testEqualsTransivity() {
        TraceInfo info1 = new TraceInfo((TraceInfo)fTraceInfo1);
        TraceInfo info2 = new TraceInfo((TraceInfo)fTraceInfo1);
        TraceInfo info3 = new TraceInfo((TraceInfo)fTraceInfo1);

        assertTrue("equals", info1.equals(info2));
        assertTrue("equals", info2.equals(info3));
        assertTrue("equals", info1.equals(info3));
    }
    /**
     * Run the equals() method test.
     */
    @Test
    public void testEqualsNull() {
        assertTrue("equals", !fTraceInfo1.equals(null));
        assertTrue("equals", !fTraceInfo2.equals(null));
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    /**
     * Run the hashCode() method test.
     */
    @Test
    public void testHashCode() {
        TraceInfo info1 = new TraceInfo((TraceInfo)fTraceInfo1);
        TraceInfo info2 = new TraceInfo((TraceInfo)fTraceInfo2);

        assertTrue("hashCode", fTraceInfo1.hashCode() == info1.hashCode());
        assertTrue("hashCode", fTraceInfo2.hashCode() == info2.hashCode());

        assertTrue("hashCode", fTraceInfo1.hashCode() != info2.hashCode());
        assertTrue("hashCode", fTraceInfo2.hashCode() != info1.hashCode());
    }
}