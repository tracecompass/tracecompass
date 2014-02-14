/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.trace.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;

import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.core.trace.location.ITmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.location.TmfLongLocation;
import org.eclipse.linuxtools.tmf.core.trace.text.TextTraceContext;
import org.junit.Test;

/**
 * Test suite for the {@link TextTraceContext} class.
 */
@SuppressWarnings("javadoc")
public class TextTraceContextTest {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private final Long aLong1 = 12345L;
    private final Long aLong2 = 12346L;
    private final Long aLong3 = 12347L;

    private final TmfLongLocation fLocation1 = new TmfLongLocation(aLong1);
    private final TmfLongLocation fLocation2 = new TmfLongLocation(aLong2);

    private final long fRank1 = 1;
    private final long fRank2 = 2;

    private final TextTraceContext fContext1 = new TextTraceContext(fLocation1, fRank1);
    private final TextTraceContext fContext2 = new TextTraceContext(fLocation1, fRank1);

    private final Pattern pattern1 = Pattern.compile("\\s*.*");
    private final Pattern pattern2 = Pattern.compile("\\s*.*");

    private final String line1 = "line1";
    private final String line2 = "line2";

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public TextTraceContextTest () {
        fContext1.firstLine = line1;
        fContext1.firstLineMatcher = pattern1.matcher(line1);
        fContext1.nextLineLocation = aLong2;

        fContext2.firstLine = line2;
        fContext2.firstLineMatcher = pattern2.matcher(line2);
        fContext2.nextLineLocation = aLong3;
    }

    @Test
    public void testTmfContextDefault() {
        final TextTraceContext context = new TextTraceContext(fLocation1, fRank1);
        assertEquals("getLocation", fLocation1, context.getLocation());
        assertEquals("getRank", fRank1, context.getRank());
        assertNull(context.firstLine);
        assertNull(context.firstLineMatcher);
        assertEquals(0, context.nextLineLocation);
    }

    // ------------------------------------------------------------------------
    // equals
    // ------------------------------------------------------------------------

    @Test
    public void testEqualsReflexivity() {
        assertTrue("equals", fContext1.equals(fContext1));
        assertTrue("equals", fContext2.equals(fContext2));

        assertFalse("equals", fContext1.equals(fContext2));
        assertFalse("equals", fContext2.equals(fContext1));
    }

    @Test
    public void testEqualsSymmetry() {
        final TextTraceContext context1 = new TextTraceContext(fContext1);
        final TextTraceContext context2 = new TextTraceContext(fContext2);

        assertTrue("equals", context1.equals(fContext1));
        assertTrue("equals", fContext1.equals(context1));

        assertTrue("equals", context2.equals(fContext2));
        assertTrue("equals", fContext2.equals(context2));
    }

    @Test
    public void testEqualsTransivity() {

        final TextTraceContext context1 = new TextTraceContext(fContext1);
        final TextTraceContext context2 = new TextTraceContext(fContext1);
        final TextTraceContext context3 = new TextTraceContext(fContext1);

        assertTrue("equals", context1.equals(context2));
        assertTrue("equals", context2.equals(context3));
        assertTrue("equals", context1.equals(context3));
    }

    @Test
    public void testEqualsNull() {
        assertFalse("equals", fContext1.equals(null));
        assertFalse("equals", fContext2.equals(null));
    }

    private static class MyContext extends TextTraceContext {

        public MyContext(ITmfLocation location, long rank) {
            super(location, rank);
        }
    }

    @Test
    public void testNonEquals() {

        // Different classes
        final MyContext myContext = new MyContext(fLocation1, fRank1);
        assertFalse("equals", fContext1.equals(myContext));
        assertFalse("equals", myContext.equals(fContext1));

        // Different locations
        TextTraceContext context1 = new TextTraceContext(fLocation1, fRank1);
        TextTraceContext context2 = new TextTraceContext(fLocation2, fRank1);
        assertFalse("equals", context1.equals(context2));

        // Different ranks
        context1 = new TextTraceContext(fLocation1, fRank1);
        context2 = new TextTraceContext(fLocation1, fRank2);
        assertFalse("equals", context1.equals(context2));

        // Different firstLine
        context1 = new TextTraceContext(fLocation1, fRank1);
        context1.firstLine = line1;
        context2 = new TextTraceContext(fLocation1, fRank1);
        context2.firstLine = line2;
        assertFalse("equals", context1.equals(context2));

        // Different firstLineMatcher
        context1 = new TextTraceContext(fLocation1, fRank1);
        context1.firstLine = line1;
        context1.firstLineMatcher = fContext1.firstLineMatcher;
        context2 = new TextTraceContext(fLocation1, fRank1);
        context2.firstLine = line1;
        context2.firstLineMatcher = fContext2.firstLineMatcher;
        assertFalse("equals", context1.equals(context2));

        // Different nextLineLocation
        context1 = new TextTraceContext(fLocation1, fRank1);
        context1.firstLine = line1;
        context1.firstLineMatcher = fContext1.firstLineMatcher;
        context1.nextLineLocation = aLong2;
        context2 = new TextTraceContext(fLocation1, fRank1);
        context2.firstLine = line1;
        context2.firstLineMatcher = fContext1.firstLineMatcher;
        context2.nextLineLocation = aLong3;
        assertFalse("equals", context1.equals(context2));

    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    @Test
    public void testHashCode() {
        final TextTraceContext context1 = new TextTraceContext(fContext1);
        final TextTraceContext context2 = new TextTraceContext(fContext2);

        assertEquals("hashCode", fContext1.hashCode(), context1.hashCode());
        assertEquals("hashCode", fContext2.hashCode(), context2.hashCode());

        assertFalse("hashCode", fContext1.hashCode() == context2.hashCode());
        assertFalse("hashCode", fContext2.hashCode() == context1.hashCode());

        final TmfContext nullContext1 = new TmfContext();
        final TmfContext nullContext2 = new TmfContext(nullContext1);
        assertEquals("hashCode", nullContext1.hashCode(), nullContext2.hashCode());
    }

    // ------------------------------------------------------------------------
    // setLocation, setRank, updateRank
    // ------------------------------------------------------------------------

    @Test
    public void testSetLocation() {
        final TextTraceContext context1 = new TextTraceContext(fLocation1, fRank1);
        context1.setLocation(fLocation2);

        assertEquals("getLocation", fLocation2, context1.getLocation());
        assertEquals("getRank", fRank1, context1.getRank());
    }

    @Test
    public void testSetRank() {
        final TextTraceContext context1 = new TextTraceContext(fContext1);
        context1.setRank(fContext2.getRank());

        assertEquals("getLocation", fContext1.getLocation(), context1.getLocation());
        assertEquals("getRank", fContext2.getRank(), context1.getRank());
    }

    @Test
    public void testIncreaseRank() {
        final TextTraceContext context1 = new TextTraceContext(fLocation1, fRank1);

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

