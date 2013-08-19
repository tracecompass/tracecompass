/**********************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/

package org.eclipse.linuxtools.lttng2.core.tests.control.model.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.linuxtools.internal.lttng2.core.control.model.ISnapshotInfo;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.impl.SnapshotInfo;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.impl.TraceInfo;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>SnapshotInfoTest</code> contains test for the class
 * <code>{@link SnapshotInfo}</code>.
 */
public class SnapshotInfoTest {

    // ------------------------------------------------------------------------
    // Test data
    // ------------------------------------------------------------------------

    private ISnapshotInfo fSnapshotInfo1 = null;
    private ISnapshotInfo fSnapshotInfo2 = null;


    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        ModelImplFactory factory = new ModelImplFactory();
        fSnapshotInfo1 = factory.getSnapshotInfo1();
        fSnapshotInfo2 = factory.getSnapshotInfo2();
    }

    // ------------------------------------------------------------------------
    // Tests
    // ------------------------------------------------------------------------

    /**
     * Constructor test.
     */
    @Test
    public void testSnapshotInfo() {
        SnapshotInfo fixture = new SnapshotInfo("event");
        assertNotNull(fixture);
        assertEquals("event", fixture.getName());
    }

    /**
     * Test Copy Constructor
     */
    @Test
    public void testSnapshotInfo2() {
        try {
            String name = null;
            new SnapshotInfo(name);
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
    public void testSnapshotCopy() {
        SnapshotInfo info = new SnapshotInfo((SnapshotInfo)fSnapshotInfo1);

        assertEquals(fSnapshotInfo1.getName(), info.getName());
    }

    /**
     * Test Copy Constructor
     */
    @Test
    public void testTraceCopy2() {
        try {
            SnapshotInfo info = null;
            new SnapshotInfo(info);
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
        SnapshotInfo fixture = new SnapshotInfo("event");
        fixture.setName("newName");
        assertEquals("newName", fixture.getName());
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString_1() {
        String result = fSnapshotInfo1.toString();

        // add additional test code here
        assertEquals("[SnapshotInfo([TraceInfo(Name=snapshot-1)],snapshotPath=/home/user/lttng-trace/mysession/,ID=1,isStreamedSnapshot=false)]", result);
    }

    // ------------------------------------------------------------------------
    // equals
    // ------------------------------------------------------------------------

    /**
     * Run the equals() method test.
     */
    @Test
    public void testEqualsReflexivity() {
        assertTrue("equals", fSnapshotInfo1.equals(fSnapshotInfo1));
        assertTrue("equals", fSnapshotInfo2.equals(fSnapshotInfo2));

        assertTrue("equals", !fSnapshotInfo1.equals(fSnapshotInfo2));
        assertTrue("equals", !fSnapshotInfo2.equals(fSnapshotInfo1));
    }

    /**
     * Run the equals() method test.
     */
    @Test
    public void testEqualsSymmetry() {
        SnapshotInfo info1 = new SnapshotInfo((SnapshotInfo)fSnapshotInfo1);
        SnapshotInfo info2 = new SnapshotInfo((SnapshotInfo)fSnapshotInfo2);

        assertTrue("equals", info1.equals(fSnapshotInfo1));
        assertTrue("equals", fSnapshotInfo1.equals(info1));

        assertTrue("equals", info2.equals(fSnapshotInfo2));
        assertTrue("equals", fSnapshotInfo2.equals(info2));
    }
    /**
     * Run the equals() method test.
     */
    @Test
    public void testEqualsTransivity() {
        SnapshotInfo info1 = new SnapshotInfo((SnapshotInfo)fSnapshotInfo1);
        SnapshotInfo info2 = new SnapshotInfo((SnapshotInfo)fSnapshotInfo1);
        SnapshotInfo info3 = new SnapshotInfo((SnapshotInfo)fSnapshotInfo1);

        assertTrue("equals", info1.equals(info2));
        assertTrue("equals", info2.equals(info3));
        assertTrue("equals", info1.equals(info3));
    }
    /**
     * Run the equals() method test.
     */
    @Test
    public void testEqualsNull() {
        assertTrue("equals", !fSnapshotInfo1.equals(null));
        assertTrue("equals", !fSnapshotInfo2.equals(null));

        SnapshotInfo info = new SnapshotInfo("snapshot-1");
        assertTrue("equals", !fSnapshotInfo1.equals(info));

        info.setSnapshotPath(null);
        assertTrue("equals", !fSnapshotInfo1.equals(info));

        info.setId(fSnapshotInfo1.getId());
        assertTrue("equals", !info.equals(fSnapshotInfo1));

        info.setSnapshotPath("/home/user/lttng-trace/mysession/");
        assertTrue("equals", fSnapshotInfo1.equals(info));
        info.setId(2);
        assertTrue("equals", !fSnapshotInfo1.equals(info));

        info.setId(fSnapshotInfo1.getId());
        info.setStreamedSnapshot(true);
        assertTrue("equals", !fSnapshotInfo1.equals(info));

        assertTrue("equals", !fSnapshotInfo1.equals(new TraceInfo(fSnapshotInfo1.getName())));
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    /**
     * Run the hashCode() method test.
     */
    @Test
    public void testHashCode() {
        SnapshotInfo info1 = new SnapshotInfo((SnapshotInfo)fSnapshotInfo1);
        SnapshotInfo info2 = new SnapshotInfo((SnapshotInfo)fSnapshotInfo2);

        assertTrue("hashCode", fSnapshotInfo1.hashCode() == info1.hashCode());
        assertTrue("hashCode", fSnapshotInfo2.hashCode() == info2.hashCode());

        assertTrue("hashCode", fSnapshotInfo1.hashCode() != info2.hashCode());
        assertTrue("hashCode", fSnapshotInfo2.hashCode() != info1.hashCode());

        // null values
        SnapshotInfo info3 = new SnapshotInfo("snapshot-1");
        assertTrue("hashCode", fSnapshotInfo1.hashCode() != info3.hashCode());

        info3.setSnapshotPath(null);
        assertTrue("hashCode", fSnapshotInfo1.hashCode() != info3.hashCode());

        info3.setSnapshotPath("/home/user/lttng-trace/mysession/");
        assertTrue("hashCode", fSnapshotInfo1.hashCode() != info3.hashCode());
    }
}