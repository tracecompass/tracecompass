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
 *   Alexandre Montplaisir - Port to JUnit4
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.TmfLongLocation;
import org.eclipse.linuxtools.tmf.core.trace.TmfTimestampLocation;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for the TmfLocation class.
 */
@SuppressWarnings("javadoc")
public class TmfLocationTest {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private String aString = "some location";
    private Long aLong = 12345L;
    private TmfTimestamp aTimestamp = new TmfTimestamp();

    private TmfStringLocation fLocation1;
    private TmfStringLocation fLocation2;
    private TmfLongLocation fLocation3;
    private TmfTimestampLocation fLocation4;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    @Before
    public void setUp() {
        fLocation1 = new TmfStringLocation((String) null);
        fLocation2 = new TmfStringLocation(aString);
        fLocation3 = new TmfLongLocation(aLong);
        fLocation4 = new TmfTimestampLocation(aTimestamp);
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    @Test
    public void testTmfLocation() {
        assertNull("TmfLocation", fLocation1.getLocationInfo());
        assertEquals("TmfLocation", aString, fLocation2.getLocationInfo());
        assertEquals("TmfLocation", aLong, fLocation3.getLocationInfo());
        assertEquals("TmfLocation", aTimestamp, fLocation4.getLocationInfo());
    }

    @Test
    public void testTmfLocationCopy() {
        TmfStringLocation location1 = new TmfStringLocation(fLocation1);
        TmfStringLocation location2 = new TmfStringLocation(fLocation2);
        TmfLongLocation location3 = new TmfLongLocation(fLocation3);
        TmfTimestampLocation location4 = new TmfTimestampLocation(fLocation4);

        assertNull("TmfLocation", location1.getLocationInfo());
        assertEquals("TmfLocation", aString, location2.getLocationInfo());
        assertEquals("TmfLocation", aLong, location3.getLocationInfo());
        assertEquals("TmfLocation", aTimestamp, location4.getLocationInfo());
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    @Test
    public void testHashCode() {
        TmfStringLocation location1 = new TmfStringLocation((String) null);
        TmfStringLocation location2 = new TmfStringLocation(aString);
        TmfLongLocation location3 = new TmfLongLocation(aLong);

        assertTrue("hashCode", fLocation1.hashCode() == location1.hashCode());
        assertTrue("hashCode", fLocation2.hashCode() == location2.hashCode());
        assertTrue("hashCode", fLocation3.hashCode() == location3.hashCode());

        assertTrue("hashCode", fLocation2.hashCode() != location3.hashCode());
        assertTrue("hashCode", fLocation3.hashCode() != location2.hashCode());
    }

    // ------------------------------------------------------------------------
    // toEquals
    // ------------------------------------------------------------------------

    private static class TmfLocation2 extends TmfStringLocation {
        public TmfLocation2(String location) {
            super(location);
        }
    }

    @Test
    public void testEqualsWrongTypes() {
        ITmfLocation location1 = new TmfStringLocation(aString);
        TmfLocation2 location2 = new TmfLocation2(aString);

        assertFalse("equals", location1.equals(location2));
        assertFalse("equals", location2.equals(location1));
    }

    @Test
    public void testEqualsWithNulls() {
        TmfStringLocation location1 = new TmfStringLocation(aString);
        TmfStringLocation location2 = new TmfStringLocation((String) null);

        assertFalse("equals", location1.equals(location2));
        assertFalse("equals", location2.equals(location1));
    }

    @Test
    public void testEqualsReflexivity() {
        assertTrue("equals", fLocation2.equals(fLocation2));
        assertTrue("equals", fLocation3.equals(fLocation3));

        assertTrue("equals", !fLocation2.equals(fLocation3));
        assertTrue("equals", !fLocation3.equals(fLocation2));
    }

    @Test
    public void testEqualsSymmetry() {
        TmfStringLocation location2 = new TmfStringLocation(aString);
        TmfLongLocation location3 = new TmfLongLocation(aLong);

        assertTrue("equals", location2.equals(fLocation2));
        assertTrue("equals", fLocation2.equals(location2));

        assertTrue("equals", location3.equals(fLocation3));
        assertTrue("equals", fLocation3.equals(location3));
    }

    @Test
    public void testEqualsTransivity() {
        TmfStringLocation location1 = new TmfStringLocation(aString);
        TmfStringLocation location2 = new TmfStringLocation(aString);
        TmfStringLocation location3 = new TmfStringLocation(aString);

        assertTrue("equals", location1.equals(location2));
        assertTrue("equals", location2.equals(location3));
        assertTrue("equals", location3.equals(location1));
    }

    @Test
    public void testEqualsNull() {
        assertTrue("equals", !fLocation2.equals(null));
        assertTrue("equals", !fLocation2.equals(null));
    }

    // ------------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------------

    @Test
    public void testToString() {
        String str = "some location";
        Long lng = 12345L;
        TmfTimestamp ts = new TmfTimestamp();

        TmfStringLocation location1 = new TmfStringLocation(str);
        TmfLongLocation location2 = new TmfLongLocation(lng);
        TmfTimestampLocation location3 = new TmfTimestampLocation(ts);

        String expected1 = "TmfStringLocation [fLocationInfo=" + str + "]";
        String expected2 = "TmfLongLocation [fLocationInfo=" + lng + "]";
        String expected3 = "TmfTimestampLocation [fLocationInfo=" + ts + "]";

        assertEquals("toString", expected1, location1.toString());
        assertEquals("toString", expected2, location2.toString());
        assertEquals("toString", expected3, location3.toString());
    }

}
