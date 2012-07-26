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

import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.TmfLocation;

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

    TmfLocation<String> fLocation1;
    TmfLocation<String> fLocation2;
    TmfLocation<Long> fLocation3;
    TmfLocation<ITmfTimestamp> fLocation4;

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
        fLocation1 = new TmfLocation<String>((String) null);
        fLocation2 = new TmfLocation<String>(aString);
        fLocation3 = new TmfLocation<Long>(aLong);
        fLocation4 = new TmfLocation<ITmfTimestamp>(aTimestamp);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public void testTmfLocation() {
        assertNull("TmfLocation", fLocation1.getLocation());
        assertEquals("TmfLocation", aString, fLocation2.getLocation());
        assertEquals("TmfLocation", aLong, fLocation3.getLocation());
        assertEquals("TmfLocation", aTimestamp, fLocation4.getLocation());
    }

    public void testTmfLocationCopy() {
        TmfLocation<String> location1 = new TmfLocation<String>(fLocation1);
        TmfLocation<String> location2 = new TmfLocation<String>(fLocation2);
        TmfLocation<Long> location3 = new TmfLocation<Long>(fLocation3);
        TmfLocation<ITmfTimestamp> location4 = new TmfLocation<ITmfTimestamp>(fLocation4);

        assertNull("TmfLocation", location1.getLocation());
        assertEquals("TmfLocation", aString, location2.getLocation());
        assertEquals("TmfLocation", aLong, location3.getLocation());
        assertEquals("TmfLocation", aTimestamp, location4.getLocation());
    }

    // ------------------------------------------------------------------------
    // clone
    // ------------------------------------------------------------------------

    public void testClone() {
        try {
            TmfLocation<String> location1 = fLocation1.clone();
            TmfLocation<String> location2 = fLocation2.clone();
            TmfLocation<Long> location3 = fLocation3.clone();
            TmfLocation<ITmfTimestamp> location4 = fLocation4.clone();

            assertEquals("clone", fLocation1, location1);
            assertEquals("clone", fLocation2, location2);
            assertEquals("clone", fLocation3, location3);
            assertEquals("clone", fLocation4, location4);

            assertEquals("clone", fLocation1.getLocation(), location1.getLocation());
            assertEquals("clone", fLocation2.getLocation(), location2.getLocation());
            assertEquals("clone", fLocation3.getLocation(), location3.getLocation());
            assertEquals("clone", fLocation4.getLocation(), location4.getLocation());

            assertNull("clone", location1.getLocation());
            assertEquals("clone", aString, location2.getLocation());
            assertEquals("clone", aLong, location3.getLocation());
            assertEquals("clone", aTimestamp, location4.getLocation());
        } catch (InternalError e) {
            fail("clone()");
        }
    }

    public static class MyCloneableClass implements Cloneable, Comparable<MyCloneableClass> {
        private String fName;

        public MyCloneableClass(String name) {
            fName = name;
        }

        @Override
        public String toString() {
            return fName;
        }

        @Override
        public MyCloneableClass clone() {
            MyCloneableClass clone = null;
            try {
                clone = (MyCloneableClass) super.clone();
                clone.fName = fName;
            } catch (CloneNotSupportedException e) {
            }
            return clone;
        }

        @Override
        public int compareTo(MyCloneableClass o) {
            return fName.compareTo(o.fName);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((fName == null) ? 0 : fName.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof MyCloneableClass)) {
                return false;
            }
            MyCloneableClass other = (MyCloneableClass) obj;
            if (fName == null) {
                if (other.fName != null) {
                    return false;
                }
            } else if (!fName.equals(other.fName)) {
                return false;
            }
            return true;
        }
    }

    public void testCloneCloneable() {
        try {
            MyCloneableClass myClass = new MyCloneableClass("myCloneableClass");
            TmfLocation<MyCloneableClass> location = new TmfLocation<MyCloneableClass>(myClass);
            TmfLocation<MyCloneableClass> clone = location.clone();

            assertEquals("clone", location, clone);
            assertEquals("clone", location.getLocation(), clone.getLocation());
            assertEquals("clone", myClass, location.getLocation());
        } catch (InternalError e) {
            fail("clone a cloneable class");
        }
    }

    private static class MyUnCloneableClass implements Comparable<MyUnCloneableClass> {
        private String fName;

        public MyUnCloneableClass(String name) {
            fName = name;
        }

        @Override
        public String toString() {
            return fName;
        }

        @Override
        public Object clone() throws CloneNotSupportedException {
            throw new CloneNotSupportedException();
        }

        @Override
        public int compareTo(MyUnCloneableClass o) {
            return fName.compareTo(o.fName);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((fName == null) ? 0 : fName.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof MyUnCloneableClass)) {
                return false;
            }
            MyUnCloneableClass other = (MyUnCloneableClass) obj;
            if (fName == null) {
                if (other.fName != null) {
                    return false;
                }
            } else if (!fName.equals(other.fName)) {
                return false;
            }
            return true;
        }
    }

    public void testCloneUncloneable() {
        try {
            MyUnCloneableClass myClass = new MyUnCloneableClass("myUncloneableClass");
            TmfLocation<MyUnCloneableClass> myLocation = new TmfLocation<MyUnCloneableClass>(myClass);
            myLocation.clone();
            fail("clone an uncloneable class");
        } catch (InternalError e) {
        }
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    public void testHashCode() {
        TmfLocation<String> location1 = new TmfLocation<String>((String) null);
        TmfLocation<String> location2 = new TmfLocation<String>(aString);
        TmfLocation<Long> location3 = new TmfLocation<Long>(aLong);

        assertTrue("hashCode", fLocation1.hashCode() == location1.hashCode());
        assertTrue("hashCode", fLocation2.hashCode() == location2.hashCode());
        assertTrue("hashCode", fLocation3.hashCode() == location3.hashCode());

        assertTrue("hashCode", fLocation2.hashCode() != location3.hashCode());
        assertTrue("hashCode", fLocation3.hashCode() != location2.hashCode());
    }

    // ------------------------------------------------------------------------
    // toEquals
    // ------------------------------------------------------------------------

    private static class TmfLocation2 extends TmfLocation<String> {
        public TmfLocation2(String location) {
            super(location);
        }
    }

    public void testEqualsWrongTypes() {
        TmfLocation<String> location1 = new TmfLocation<String>(aString);
        TmfLocation2 location2 = new TmfLocation2(aString);

        assertFalse("equals", location1.equals(location2));
        assertFalse("equals", location2.equals(location1));
    }

    public void testEqualsWithNulls() {
        TmfLocation<String> location1 = new TmfLocation<String>(aString);
        TmfLocation<String> location2 = new TmfLocation<String>((String) null);

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
        TmfLocation<String> location2 = new TmfLocation<String>(aString);
        TmfLocation<Long> location3 = new TmfLocation<Long>(aLong);

        assertTrue("equals", location2.equals(fLocation2));
        assertTrue("equals", fLocation2.equals(location2));

        assertTrue("equals", location3.equals(fLocation3));
        assertTrue("equals", fLocation3.equals(location3));
    }

    public void testEqualsTransivity() {
        TmfLocation<String> location1 = new TmfLocation<String>(aString);
        TmfLocation<String> location2 = new TmfLocation<String>(aString);
        TmfLocation<String> location3 = new TmfLocation<String>(aString);

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

        TmfLocation<String> location1 = new TmfLocation<String>(aString);
        TmfLocation<Long> location2 = new TmfLocation<Long>(aLong);
        TmfLocation<ITmfTimestamp> location3 = new TmfLocation<ITmfTimestamp>(aTimestamp);

        String expected1 = "TmfLocation [fLocation=" + aString + "]";
        String expected2 = "TmfLocation [fLocation=" + aLong + "]";
        String expected3 = "TmfLocation [fLocation=" + aTimestamp + "]";

        assertEquals("toString", expected1, location1.toString());
        assertEquals("toString", expected2, location2.toString());
        assertEquals("toString", expected3, location3.toString());
    }

}
