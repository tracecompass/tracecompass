/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
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
import org.eclipse.linuxtools.tmf.core.trace.TmfLocation;

/**
 * <b><u>TmfLocationTest</u></b>
 * <p>
 * Test suite for the TmfLocation class.
 */
@SuppressWarnings("nls")
public class TmfLocationTest extends TestCase {

	// ------------------------------------------------------------------------
	// Variables
	// ------------------------------------------------------------------------

	String       aString    = "some location";
	Long         aLong      = 12345L;
	TmfTimestamp aTimestamp = new TmfTimestamp();

	TmfLocation<String>       fLocation1;
	TmfLocation<Long>         fLocation2;
	TmfLocation<TmfTimestamp> fLocation3;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

	/**
	 * @param name the test name
	 */
	public TmfLocationTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		fLocation1 = new TmfLocation<String>(aString);
		fLocation2 = new TmfLocation<Long>(aLong);
		fLocation3 = new TmfLocation<TmfTimestamp>(aTimestamp);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

	public void testTmfLocation() {
		assertEquals("TmfLocation", aString,    fLocation1.getLocation());
		assertEquals("TmfLocation", aLong,      fLocation2.getLocation());
		assertEquals("TmfLocation", aTimestamp, fLocation3.getLocation());
	}

	public void testTmfLocationCopy() {
		TmfLocation<String> location1 = new TmfLocation<String>(fLocation1);
		TmfLocation<Long>   location2 = new TmfLocation<Long>(fLocation2);
		TmfLocation<TmfTimestamp> location3 = new TmfLocation<TmfTimestamp>(fLocation3);

		assertEquals("TmfLocation", aString,    location1.getLocation());
		assertEquals("TmfLocation", aLong,      location2.getLocation());
		assertEquals("TmfLocation", aTimestamp, location3.getLocation());
	}

	public void testTmfLocationCopy2() throws Exception {
		try {
			new TmfLocation<Long>((TmfLocation<Long>) null);
			fail("null copy");
		}
		catch (IllegalArgumentException e) {
			// Success
		}
	}

    // ------------------------------------------------------------------------
    // setLocation
    // ------------------------------------------------------------------------

	public void testSetLocation() {
		String       aString2    = "some other location";
		Long         aLong2      = 1234567L;
		TmfTimestamp aTimestamp2 = (TmfTimestamp) TmfTimestamp.BigBang;

		fLocation1.setLocation(aString2);
		fLocation2.setLocation(aLong2);
		fLocation3.setLocation(aTimestamp2);
		
		assertEquals("TmfLocation", aString2,    fLocation1.getLocation());
		assertEquals("TmfLocation", aLong2,      fLocation2.getLocation());
		assertEquals("TmfLocation", aTimestamp2, fLocation3.getLocation());
	}

    // ------------------------------------------------------------------------
    // toEquals
    // ------------------------------------------------------------------------

	public void testEqualsReflexivity() throws Exception {
		assertTrue("equals", fLocation1.equals(fLocation1));
		assertTrue("equals", fLocation2.equals(fLocation2));

		assertTrue("equals", !fLocation1.equals(fLocation2));
		assertTrue("equals", !fLocation2.equals(fLocation1));
	}
	
	public void testEqualsSymmetry() throws Exception {
		TmfLocation<String> location1 = new TmfLocation<String>(aString);
		TmfLocation<Long>   location2 = new TmfLocation<Long>(aLong);

		assertTrue("equals", location1.equals(fLocation1));
		assertTrue("equals", fLocation1.equals(location1));

		assertTrue("equals", location2.equals(fLocation2));
		assertTrue("equals", fLocation2.equals(location2));
	}
	
	public void testEqualsTransivity() throws Exception {
		TmfLocation<String> location1 = new TmfLocation<String>(aString);
		TmfLocation<String> location2 = new TmfLocation<String>(aString);
		TmfLocation<String> location3 = new TmfLocation<String>(aString);

		assertTrue("equals", location1.equals(location2));
		assertTrue("equals", location2.equals(location3));
		assertTrue("equals", location1.equals(location3));
	}
	
	public void testEqualsNull() throws Exception {
		assertTrue("equals", !fLocation1.equals(null));
		assertTrue("equals", !fLocation1.equals(null));
	}
	
	// ------------------------------------------------------------------------
	// hashCode
	// ------------------------------------------------------------------------

	public void testHashCode() throws Exception {
		TmfLocation<String> location1 = new TmfLocation<String>(aString);
		TmfLocation<Long>   location2 = new TmfLocation<Long>(aLong);

		assertTrue("hashCode", fLocation1.hashCode() == location1.hashCode());
		assertTrue("hashCode", fLocation2.hashCode() == location2.hashCode());

		assertTrue("hashCode", fLocation1.hashCode() != location2.hashCode());
		assertTrue("hashCode", fLocation2.hashCode() != location1.hashCode());
	}
	
    // ------------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------------

	public void testToString() {
		String       aString     = "some location";
		Long         aLong       = 12345L;
		TmfTimestamp aTimestamp  = new TmfTimestamp();

		TmfLocation<String>       location1 = new TmfLocation<String>(aString);
		TmfLocation<Long>         location2 = new TmfLocation<Long>(aLong);
		TmfLocation<TmfTimestamp> location3 = new TmfLocation<TmfTimestamp>(aTimestamp);

		assertEquals("TmfLocation", aString.toString(),    location1.toString());
		assertEquals("TmfLocation", aLong.toString(),      location2.toString());
		assertEquals("TmfLocation", aTimestamp.toString(), location3.toString());
	}

    // ------------------------------------------------------------------------
    // clone
    // ------------------------------------------------------------------------

	public void testClone() {
		try {
			TmfLocation<String> location1 = fLocation1.clone();
			TmfLocation<Long>   location2 = fLocation2.clone();
			TmfLocation<TmfTimestamp> location3 = fLocation3.clone();

			assertEquals("TmfLocation", aString.toString(), location1.toString());
			assertEquals("TmfLocation", aLong.toString(), location2.toString());
			assertEquals("TmfLocation", aTimestamp.toString(), location3.toString());
		}
		catch (InternalError e) {
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
			return 0;
		}
	}

	public void testCloneCloneable() {
		try {
			MyCloneableClass myClass = new MyCloneableClass("myClass");
			TmfLocation<MyCloneableClass> myLocation = new TmfLocation<MyCloneableClass>(myClass);
			TmfLocation<MyCloneableClass> location4 = myLocation.clone();

			assertEquals("TmfLocation", myClass.toString(), location4.toString());
		}
		catch (InternalError e) {
			fail("clone()");
		}
	}

	public static class MyUnCloneableClass implements Comparable<MyUnCloneableClass> {
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
			return 0;
		}
	}

	public void testCloneUnCloneable() {
		try {
			MyUnCloneableClass myClass = new MyUnCloneableClass("myClass");
			TmfLocation<MyUnCloneableClass> myLocation = new TmfLocation<MyUnCloneableClass>(myClass);
			myLocation.clone();
			fail("clone()");
		}
		catch (InternalError e) {
			// Success
		}
	}

}
