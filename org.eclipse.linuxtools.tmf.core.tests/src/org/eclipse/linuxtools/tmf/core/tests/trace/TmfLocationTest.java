/*******************************************************************************
 * Copyright (c) 2009, 2010, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.trace;

import junit.framework.TestCase;

import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.TmfLongLocation;
import org.eclipse.linuxtools.tmf.core.trace.TmfTimestampLocation;

/**
 * Test suite for the TmfLocation class.
 */
@SuppressWarnings({"nls","javadoc"})
public class TmfLocationTest extends TestCase {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    String aString = "some location";
    Long aLong = 12345L;
    TmfTimestamp aTimestamp = new TmfTimestamp();

    TmfStringLocation fLocation1;
    TmfStringLocation fLocation2;
    TmfLongLocation fLocation3;
    TmfTimestampLocation fLocation4;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    /**
     * @param name
     *            the test name
     */
    public TmfLocationTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        fLocation1 = new TmfStringLocation((String) null);
        fLocation2 = new TmfStringLocation(aString);
        fLocation3 = new TmfLongLocation(aLong);
        fLocation4 = new TmfTimestampLocation(aTimestamp);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public void testTmfLocation() {
        assertNull("TmfLocation", fLocation1.getLocationInfo());
        assertEquals("TmfLocation", aString, fLocation2.getLocationInfo());
        assertEquals("TmfLocation", aLong, fLocation3.getLocationInfo());
        assertEquals("TmfLocation", aTimestamp, fLocation4.getLocationInfo());
    }

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
    // clone
    // ------------------------------------------------------------------------

    public void testClone() {
        try {
            TmfStringLocation location1 = fLocation1.clone();
            TmfStringLocation location2 = fLocation2.clone();
            TmfLongLocation location3 = fLocation3.clone();
            TmfTimestampLocation location4 = fLocation4.clone();

            assertEquals("clone", fLocation1, location1);
            assertEquals("clone", fLocation2, location2);
            assertEquals("clone", fLocation3, location3);
            assertEquals("clone", fLocation4, location4);

            assertEquals("clone", fLocation1.getLocationInfo(), location1.getLocationInfo());
            assertEquals("clone", fLocation2.getLocationInfo(), location2.getLocationInfo());
            assertEquals("clone", fLocation3.getLocationInfo(), location3.getLocationInfo());
            assertEquals("clone", fLocation4.getLocationInfo(), location4.getLocationInfo());

            assertNull("clone", location1.getLocationInfo());
            assertEquals("clone", aString, location2.getLocationInfo());
            assertEquals("clone", aLong, location3.getLocationInfo());
            assertEquals("clone", aTimestamp, location4.getLocationInfo());
        } catch (InternalError e) {
            fail("clone()");
        }
    }

//    public static class MyCloneableClass implements Cloneable, Comparable<MyCloneableClass> {
//        private String fName;
//
//        public MyCloneableClass(String name) {
//            fName = name;
//        }
//
//        @Override
//        public String toString() {
//            return fName;
//        }
//
//        @Override
//        public MyCloneableClass clone() {
//            MyCloneableClass clone = null;
//            try {
//                clone = (MyCloneableClass) super.clone();
//                clone.fName = fName;
//            } catch (CloneNotSupportedException e) {
//            }
//            return clone;
//        }
//
//        @Override
//        public int compareTo(MyCloneableClass o) {
//            return fName.compareTo(o.fName);
//        }
//
//        @Override
//        public int hashCode() {
//            final int prime = 31;
//            int result = 1;
//            result = prime * result + ((fName == null) ? 0 : fName.hashCode());
//            return result;
//        }
//
//        @Override
//        public boolean equals(Object obj) {
//            if (this == obj) {
//                return true;
//            }
//            if (obj == null) {
//                return false;
//            }
//            if (!(obj instanceof MyCloneableClass)) {
//                return false;
//            }
//            MyCloneableClass other = (MyCloneableClass) obj;
//            if (fName == null) {
//                if (other.fName != null) {
//                    return false;
//                }
//            } else if (!fName.equals(other.fName)) {
//                return false;
//            }
//            return true;
//        }
//    }
//
//    public void testCloneCloneable() {
//        try {
//            MyCloneableClass myClass = new MyCloneableClass("myCloneableClass");
//            TmfLocation<MyCloneableClass> location = new TmfLocation<MyCloneableClass>(myClass);
//            TmfLocation<MyCloneableClass> clone = location.clone();
//
//            assertEquals("clone", location, clone);
//            assertEquals("clone", location.getLocationData(), clone.getLocationData());
//            assertEquals("clone", myClass, location.getLocationData());
//        } catch (InternalError e) {
//            fail("clone a cloneable class");
//        }
//    }
//
//    private static class MyUnCloneableClass implements Comparable<MyUnCloneableClass> {
//        private String fName;
//
//        public MyUnCloneableClass(String name) {
//            fName = name;
//        }
//
//        @Override
//        public String toString() {
//            return fName;
//        }
//
//        @Override
//        public Object clone() throws CloneNotSupportedException {
//            throw new CloneNotSupportedException();
//        }
//
//        @Override
//        public int compareTo(MyUnCloneableClass o) {
//            return fName.compareTo(o.fName);
//        }
//
//        @Override
//        public int hashCode() {
//            final int prime = 31;
//            int result = 1;
//            result = prime * result + ((fName == null) ? 0 : fName.hashCode());
//            return result;
//        }
//
//        @Override
//        public boolean equals(Object obj) {
//            if (this == obj) {
//                return true;
//            }
//            if (obj == null) {
//                return false;
//            }
//            if (!(obj instanceof MyUnCloneableClass)) {
//                return false;
//            }
//            MyUnCloneableClass other = (MyUnCloneableClass) obj;
//            if (fName == null) {
//                if (other.fName != null) {
//                    return false;
//                }
//            } else if (!fName.equals(other.fName)) {
//                return false;
//            }
//            return true;
//        }
//    }
//
//    public void testCloneUncloneable() {
//        try {
//            MyUnCloneableClass myClass = new MyUnCloneableClass("myUncloneableClass");
//            TmfLocation<MyUnCloneableClass> myLocation = new TmfLocation<MyUnCloneableClass>(myClass);
//            myLocation.clone();
//            fail("clone an uncloneable class");
//        } catch (InternalError e) {
//        }
//    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

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

    public void testEqualsWrongTypes() {
        ITmfLocation location1 = new TmfStringLocation(aString);
        TmfLocation2 location2 = new TmfLocation2(aString);

        assertFalse("equals", location1.equals(location2));
        assertFalse("equals", location2.equals(location1));
    }

    public void testEqualsWithNulls() {
        TmfStringLocation location1 = new TmfStringLocation(aString);
        TmfStringLocation location2 = new TmfStringLocation((String) null);

        assertFalse("equals", location1.equals(location2));
        assertFalse("equals", location2.equals(location1));
    }

    public void testEqualsReflexivity() {
        assertTrue("equals", fLocation2.equals(fLocation2));
        assertTrue("equals", fLocation3.equals(fLocation3));

        assertTrue("equals", !fLocation2.equals(fLocation3));
        assertTrue("equals", !fLocation3.equals(fLocation2));
    }

    public void testEqualsSymmetry() {
        TmfStringLocation location2 = new TmfStringLocation(aString);
        TmfLongLocation location3 = new TmfLongLocation(aLong);

        assertTrue("equals", location2.equals(fLocation2));
        assertTrue("equals", fLocation2.equals(location2));

        assertTrue("equals", location3.equals(fLocation3));
        assertTrue("equals", fLocation3.equals(location3));
    }

    public void testEqualsTransivity() {
        TmfStringLocation location1 = new TmfStringLocation(aString);
        TmfStringLocation location2 = new TmfStringLocation(aString);
        TmfStringLocation location3 = new TmfStringLocation(aString);

        assertTrue("equals", location1.equals(location2));
        assertTrue("equals", location2.equals(location3));
        assertTrue("equals", location3.equals(location1));
    }

    public void testEqualsNull() {
        assertTrue("equals", !fLocation2.equals(null));
        assertTrue("equals", !fLocation2.equals(null));
    }

    // ------------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------------

    @SuppressWarnings("hiding")
    public void testToString() {
        String aString = "some location";
        Long aLong = 12345L;
        TmfTimestamp aTimestamp = new TmfTimestamp();

        TmfStringLocation location1 = new TmfStringLocation(aString);
        TmfLongLocation location2 = new TmfLongLocation(aLong);
        TmfTimestampLocation location3 = new TmfTimestampLocation(aTimestamp);

        String expected1 = "TmfLocation [fLocation=" + aString + "]";
        String expected2 = "TmfLocation [fLocation=" + aLong + "]";
        String expected3 = "TmfLocation [fLocation=" + aTimestamp + "]";

        assertEquals("toString", expected1, location1.toString());
        assertEquals("toString", expected2, location2.toString());
        assertEquals("toString", expected3, location3.toString());
    }

}
