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
 *   Francois Chouinard - Adapted for TMF Trace Model 1.0
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.trace;

import junit.framework.TestCase;

import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfLongLocation;
import org.eclipse.linuxtools.tmf.core.trace.TmfTimestampLocation;

/**
 * Test suite for the TmfContext class.
 */
@SuppressWarnings({"nls","javadoc"})
public class TmfContextTest extends TestCase {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    final String aString = "some location";
    final Long aLong = 12345L;
    final TmfTimestamp aTimestamp = new TmfTimestamp();

    final TmfStringLocation fLocation1 = new TmfStringLocation(aString);
    final TmfLongLocation fLocation2 = new TmfLongLocation(aLong);
    final TmfTimestampLocation fLocation3 = new TmfTimestampLocation(aTimestamp);

    final long fRank1 = 1;
    final long fRank2 = 2;
    final long fRank3 = 3;

    final TmfContext fContext1 = new TmfContext(fLocation1, fRank1);
    final TmfContext fContext2 = new TmfContext(fLocation2, fRank2);
    final TmfContext fContext3 = new TmfContext(fLocation3, fRank3);

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    /**
     * @param name the test name
     */
    public TmfContextTest(final String name) {
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

    public void testTmfContextDefault() {
        final TmfContext context = new TmfContext();
        assertEquals("getLocation", null, context.getLocation());
        assertEquals("getRank", ITmfContext.UNKNOWN_RANK, context.getRank());
    }

    public void testTmfContextNoRank() {
        final TmfContext context1 = new TmfContext(fLocation1);
        final TmfContext context2 = new TmfContext(fLocation2);
        final TmfContext context3 = new TmfContext(fLocation3);

        assertEquals("getLocation", fLocation1, context1.getLocation());
        assertEquals("getLocation", fLocation2, context2.getLocation());
        assertEquals("getLocation", fLocation3, context3.getLocation());

        assertEquals("getRank", ITmfContext.UNKNOWN_RANK, context1.getRank());
        assertEquals("getRank", ITmfContext.UNKNOWN_RANK, context2.getRank());
        assertEquals("getRank", ITmfContext.UNKNOWN_RANK, context3.getRank());
    }

    public void testTmfContext() {
        assertEquals("getLocation", fLocation1, fContext1.getLocation());
        assertEquals("getLocation", fLocation2, fContext2.getLocation());
        assertEquals("getLocation", fLocation3, fContext3.getLocation());

        assertEquals("getRank", fRank1, fContext1.getRank());
        assertEquals("getRank", fRank2, fContext2.getRank());
        assertEquals("getRank", fRank3, fContext3.getRank());
    }

    public void testTmfContextCopy() {
        final TmfContext context1 = new TmfContext(fContext1);
        final TmfContext context2 = new TmfContext(fContext2);
        final TmfContext context3 = new TmfContext(fContext3);

        assertEquals("getLocation", fLocation1, context1.getLocation());
        assertEquals("getLocation", fLocation2, context2.getLocation());
        assertEquals("getLocation", fLocation3, context3.getLocation());

        assertEquals("getRank", fRank1, context1.getRank());
        assertEquals("getRank", fRank2, context2.getRank());
        assertEquals("getRank", fRank3, context3.getRank());
    }

    public void testTmfContextCopy2() {
        try {
            new TmfContext((TmfContext) null);
            fail("Copy constructor: no exception");
        }
        catch (final IllegalArgumentException e) {
            // pass
        }
        catch (final Exception e) {
            fail("Copy constructor: wrong exception");
        }
    }

    // ------------------------------------------------------------------------
    // equals
    // ------------------------------------------------------------------------

    public void testEqualsReflexivity() {
        assertTrue("equals", fContext1.equals(fContext1));
        assertTrue("equals", fContext2.equals(fContext2));

        assertFalse("equals", fContext1.equals(fContext2));
        assertFalse("equals", fContext2.equals(fContext1));
    }

    public void testEqualsSymmetry() {
        final TmfContext context1 = new TmfContext(fContext1);
        final TmfContext context2 = new TmfContext(fContext2);

        assertTrue("equals", context1.equals(fContext1));
        assertTrue("equals", fContext1.equals(context1));

        assertTrue("equals", context2.equals(fContext2));
        assertTrue("equals", fContext2.equals(context2));
    }

    public void testEqualsTransivity() {
        final TmfContext context1 = new TmfContext(fContext1);
        final TmfContext context2 = new TmfContext(context1);
        final TmfContext context3 = new TmfContext(context2);

        assertTrue("equals", context1.equals(context2));
        assertTrue("equals", context2.equals(context3));
        assertTrue("equals", context1.equals(context3));
    }

    public void testEqualsNull() {
        assertFalse("equals", fContext1.equals(null));
        assertFalse("equals", fContext2.equals(null));
    }

    private static class MyContext extends TmfContext {
    }

    public void testNonEquals() {

        // Different classes
        final MyContext myContext = new MyContext();
        assertFalse("equals", fContext1.equals(myContext));
        assertFalse("equals", myContext.equals(fContext1));

        // Different locations
        TmfContext context1 = new TmfContext(fContext1);
        TmfContext context2 = new TmfContext(fContext1);
        context1.setLocation(null);
        context2.setLocation(null);

        assertFalse("equals", fContext1.equals(context1));
        assertFalse("equals", context1.equals(fContext1));
        assertTrue("equals", context1.equals(context2));

        // Different ranks
        context1 = new TmfContext(fContext1);
        context2 = new TmfContext(fContext1);
        context1.setRank(fContext1.getRank() + 1);
        context2.setRank(fContext1.getRank() + 2);

        assertFalse("equals", fContext1.equals(context1));
        assertFalse("equals", context1.equals(fContext1));
        assertFalse("equals", context1.equals(context2));
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    public void testHashCode() {
        final TmfContext context1 = new TmfContext(fContext1);
        final TmfContext context2 = new TmfContext(fContext2);

        assertEquals("hashCode", fContext1.hashCode(), context1.hashCode());
        assertEquals("hashCode", fContext2.hashCode(), context2.hashCode());

        assertFalse("hashCode", fContext1.hashCode() == context2.hashCode());
        assertFalse("hashCode", fContext2.hashCode() == context1.hashCode());

        final TmfContext nullContext1 = new TmfContext();
        final TmfContext nullContext2 = new TmfContext(nullContext1);
        assertEquals("hashCode", nullContext1.hashCode(), nullContext2.hashCode());
    }

    // ------------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------------

    public void testToString() {
        final String expected1 = "TmfContext [fLocation=" + fLocation1 + ", fRank=" + 1 + "]";
        final String expected2 = "TmfContext [fLocation=" + fLocation2 + ", fRank=" + 2 + "]";
        final String expected3 = "TmfContext [fLocation=" + fLocation3 + ", fRank=" + 3 + "]";

        assertEquals("toString", expected1, fContext1.toString());
        assertEquals("toString", expected2, fContext2.toString());
        assertEquals("toString", expected3, fContext3.toString());
    }

    // ------------------------------------------------------------------------
    // clone
    // ------------------------------------------------------------------------

    public void testClone() {
        try {
            final TmfContext context1 = fContext1.clone();
            final TmfContext context2 = fContext2.clone();
            final TmfContext context3 = fContext3.clone();

            assertEquals("clone", context1, fContext1);
            assertEquals("clone", context2, fContext2);
            assertEquals("clone", context3, fContext3);

            context1.setLocation(null);
            final TmfContext context4 = context1.clone();
            assertEquals("clone", context1, context4);
            assertNull(context1.getLocation());
            assertNull(context4.getLocation());

        } catch (final InternalError e) {
            fail("clone()");
        }
    }

    // ------------------------------------------------------------------------
    // setLocation, setRank, updateRank
    // ------------------------------------------------------------------------

    public void testSetLocation() {
        final TmfContext context1 = new TmfContext(fContext1);
        context1.setLocation(fContext2.getLocation());

        assertEquals("getLocation", fLocation2, context1.getLocation());
        assertEquals("getRank", 1, context1.getRank());
    }

    public void testSetRank() {
        final TmfContext context1 = new TmfContext(fContext1);
        context1.setRank(fContext2.getRank());

        assertEquals("getLocation", fLocation1, context1.getLocation());
        assertEquals("getRank", fRank2, context1.getRank());
    }

    public void testIncreaseRank() {
        final TmfContext context1 = new TmfContext(fContext1);

        context1.increaseRank();
        assertEquals("getRank", fRank1 + 1, context1.getRank());
        context1.increaseRank();
        assertEquals("getRank", fRank1 + 2, context1.getRank());

        context1.setRank(ITmfContext.UNKNOWN_RANK);
        context1.increaseRank();
        assertEquals("getRank", ITmfContext.UNKNOWN_RANK, context1.getRank());
        context1.increaseRank();
        assertEquals("getRank", ITmfContext.UNKNOWN_RANK, context1.getRank());
    }

}
