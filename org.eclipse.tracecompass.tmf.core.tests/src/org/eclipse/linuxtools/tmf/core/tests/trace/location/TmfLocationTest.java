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
 *   Patrick Tasse - Add tests for TmfExperimentLocation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.trace.location;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;

import org.eclipse.linuxtools.internal.tmf.core.trace.TmfExperimentLocation;
import org.eclipse.linuxtools.internal.tmf.core.trace.TmfLocationArray;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.location.ITmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.location.TmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.location.TmfLongLocation;
import org.eclipse.linuxtools.tmf.core.trace.location.TmfTimestampLocation;
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

    private Long aLong = 12345L;
    private TmfTimestamp aTimestamp = new TmfTimestamp();
    private TmfLocationArray aLocationArray;

    private TmfLongLocation fLocation1;
    private TmfLongLocation fLocation2;
    private TmfTimestampLocation fLocation3;
    private TmfExperimentLocation fExpLocation;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    @Before
    public void setUp() {
        fLocation1 = new TmfLongLocation((Long) null);
        fLocation2 = new TmfLongLocation(aLong);
        fLocation3 = new TmfTimestampLocation(aTimestamp);
        aLocationArray = new TmfLocationArray(
                new ITmfLocation[] { fLocation1, fLocation2, fLocation3 },
                new long[] { 1, 2, 3 }
                );
        fExpLocation = new TmfExperimentLocation(aLocationArray);
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    @Test
    public void testTmfLocation() {
        assertNull("TmfLocation", fLocation1.getLocationInfo());
        assertEquals("TmfLocation", aLong, fLocation2.getLocationInfo());
        assertEquals("TmfLocation", aTimestamp, fLocation3.getLocationInfo());
        assertEquals("TmfLocation", aLocationArray, fExpLocation.getLocationInfo());
    }

    @Test
    public void testTmfLocationCopy() {
        TmfLongLocation location1 = new TmfLongLocation(fLocation1);
        TmfLongLocation location2 = new TmfLongLocation(fLocation2);
        TmfTimestampLocation location3 = new TmfTimestampLocation(fLocation3);
        TmfExperimentLocation expLocation = new TmfExperimentLocation(fExpLocation);

        assertNull("TmfLocation", location1.getLocationInfo());
        assertEquals("TmfLocation", aLong, location2.getLocationInfo());
        assertEquals("TmfLocation", aTimestamp, location3.getLocationInfo());
        assertEquals("TmfLocation", aLocationArray, expLocation.getLocationInfo());
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    @Test
    public void testHashCode() {
        TmfLongLocation location1 = new TmfLongLocation((Long) null);
        TmfLongLocation location2 = new TmfLongLocation(aLong);
        TmfTimestampLocation location3 = new TmfTimestampLocation(aTimestamp);
        TmfExperimentLocation expLocation = new TmfExperimentLocation(fExpLocation);
        TmfLocationArray locationArray1 = new TmfLocationArray(aLocationArray, 2, fLocation3, 5);
        TmfExperimentLocation expLocation1 = new TmfExperimentLocation(locationArray1);
        TmfLocationArray locationArray2 = new TmfLocationArray(aLocationArray, 2, fLocation2, 4);
        TmfExperimentLocation expLocation2 = new TmfExperimentLocation(locationArray2);
        TmfLocationArray locationArray3 = new TmfLocationArray(
                new ITmfLocation[] { fLocation1, fLocation2 },
                new long[] { 1, 2 }
                );
        TmfExperimentLocation expLocation3 = new TmfExperimentLocation(locationArray3);

        assertTrue("hashCode", fLocation1.hashCode() == location1.hashCode());
        assertTrue("hashCode", fLocation2.hashCode() == location2.hashCode());
        assertTrue("hashCode", fLocation3.hashCode() == location3.hashCode());
        assertTrue("hashCode", fExpLocation.hashCode() == expLocation.hashCode());

        assertTrue("hashCode", fLocation2.hashCode() != location3.hashCode());
        assertTrue("hashCode", fLocation3.hashCode() != location2.hashCode());
        assertTrue("hashCode", fExpLocation.hashCode() != expLocation1.hashCode());
        assertTrue("hashCode", fExpLocation.hashCode() != expLocation2.hashCode());
        assertTrue("hashCode", fExpLocation.hashCode() != expLocation3.hashCode());
    }

    // ------------------------------------------------------------------------
    // toEquals
    // ------------------------------------------------------------------------

    private static class TmfTestLongLocation extends TmfLocation {
        public TmfTestLongLocation(Long location) {
            super(location);
        }

        @Override
        public void serialize(ByteBuffer bufferOut) {}
    }

    private static class TmfTestLongLocation2 extends TmfTestLongLocation {
        public TmfTestLongLocation2(Long location) {
            super(location);
        }
    }

    @Test
    public void testEqualsWrongTypes() {
        ITmfLocation location1 = new TmfTestLongLocation(aLong);
        TmfTestLongLocation location2 = new TmfTestLongLocation2(aLong);

        assertFalse("equals", location1.equals(location2));
        assertFalse("equals", location2.equals(location1));
    }

    @Test
    public void testEqualsWithNulls() {
        ITmfLocation location1 = new TmfLongLocation(aLong);
        ITmfLocation location2 = new TmfLongLocation((Long) null);

        assertFalse("equals", location1.equals(location2));
        assertFalse("equals", location2.equals(location1));
    }

    @Test
    public void testEqualsReflexivity() {
        assertTrue("equals", fLocation1.equals(fLocation1));
        assertTrue("equals", fLocation2.equals(fLocation2));
        assertTrue("equals", fLocation3.equals(fLocation3));
        assertTrue("equals", fExpLocation.equals(fExpLocation));

        assertTrue("equals", !fLocation2.equals(fLocation3));
        assertTrue("equals", !fLocation3.equals(fLocation2));
        TmfLocationArray locationArray1 = new TmfLocationArray(aLocationArray, 2, fLocation3, 5);
        TmfExperimentLocation expLocation1 = new TmfExperimentLocation(locationArray1);
        TmfLocationArray locationArray2 = new TmfLocationArray(aLocationArray, 2, fLocation2, 4);
        TmfExperimentLocation expLocation2 = new TmfExperimentLocation(locationArray2);
        TmfLocationArray locationArray3 = new TmfLocationArray(
                new ITmfLocation[] { fLocation1, fLocation2, fLocation3 },
                new long[] { 1, 2 }
                );
        TmfExperimentLocation expLocation3 = new TmfExperimentLocation(locationArray3);
        assertTrue("equals", !fExpLocation.equals(expLocation1));
        assertTrue("equals", !expLocation1.equals(fExpLocation));
        assertTrue("equals", !fExpLocation.equals(expLocation2));
        assertTrue("equals", !expLocation2.equals(fExpLocation));
        assertTrue("equals", !fExpLocation.equals(expLocation3));
        assertTrue("equals", !expLocation3.equals(fExpLocation));
    }

    @Test
    public void testEqualsSymmetry() {
        TmfLongLocation location2 = new TmfLongLocation(aLong);
        TmfTimestampLocation location3 = new TmfTimestampLocation(aTimestamp);
        TmfExperimentLocation expLocation = new TmfExperimentLocation(fExpLocation);

        assertTrue("equals", location2.equals(fLocation2));
        assertTrue("equals", fLocation2.equals(location2));

        assertTrue("equals", location3.equals(fLocation3));
        assertTrue("equals", fLocation3.equals(location3));

        assertTrue("equals", expLocation.equals(fExpLocation));
        assertTrue("equals", fExpLocation.equals(expLocation));
    }

    @Test
    public void testEqualsTransivity() {
        TmfLongLocation location1 = new TmfLongLocation(aLong);
        TmfLongLocation location2 = new TmfLongLocation(aLong);
        TmfLongLocation location3 = new TmfLongLocation(aLong);

        TmfExperimentLocation expLocation1 = new TmfExperimentLocation(aLocationArray);
        TmfExperimentLocation expLocation2 = new TmfExperimentLocation(aLocationArray);
        TmfExperimentLocation expLocation3 = new TmfExperimentLocation(aLocationArray);

        assertTrue("equals", location1.equals(location2));
        assertTrue("equals", location2.equals(location3));
        assertTrue("equals", location3.equals(location1));
        assertTrue("equals", expLocation1.equals(expLocation2));
        assertTrue("equals", expLocation2.equals(expLocation3));
        assertTrue("equals", expLocation3.equals(expLocation1));
    }

    @Test
    public void testEqualsNull() {
        assertTrue("equals", !fLocation1.equals(null));
        assertTrue("equals", !fLocation2.equals(null));
        assertTrue("equals", !fLocation3.equals(null));
        assertTrue("equals", !fExpLocation.equals(null));
    }

    // ------------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------------

    @Test
    public void testToString() {
        TmfTimestamp ts = new TmfTimestamp();

        TmfLongLocation location1 = new TmfLongLocation(aLong);
        TmfTimestampLocation location2 = new TmfTimestampLocation(ts);
        TmfExperimentLocation expLocation = new TmfExperimentLocation(aLocationArray);

        String expected1 = "TmfLongLocation [fLocationInfo=" + aLong + "]";
        String expected2 = "TmfTimestampLocation [fLocationInfo=" + ts + "]";
        String expected3 = "TmfExperimentLocation [" + aLocationArray + "]";

        assertEquals("toString", expected1, location1.toString());
        assertEquals("toString", expected2, location2.toString());
        assertEquals("toString", expected3, expLocation.toString());
    }

}
