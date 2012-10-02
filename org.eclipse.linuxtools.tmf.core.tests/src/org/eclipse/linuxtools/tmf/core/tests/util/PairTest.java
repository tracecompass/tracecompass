/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Bernd Hufmann - Initial design and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.util;

import junit.framework.TestCase;

import org.eclipse.linuxtools.tmf.core.util.Pair;

/**
 * Test case for Pair class.
 *
 * @author Bernd Hufmann
 */
@SuppressWarnings({ "nls", "javadoc" })
public class PairTest extends TestCase {

    // ------------------------------------------------------------------------
    // Field(s)
    // ------------------------------------------------------------------------
    Pair<String, Long> fPair1 = new Pair<String, Long>("String 1", 1L);
    Pair<String, Long> fPair2 = new Pair<String, Long>("String 2", 2L);

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    // ------------------------------------------------------------------------
    // to String
    // ------------------------------------------------------------------------

    public void testToString() {
        String result = fPair1.toString();
        assertEquals("(String 1, 1)", result);
    }

    // ------------------------------------------------------------------------
    // Setters/Getters
    // ------------------------------------------------------------------------

    public void testAccessors() {
        Pair<String, Long> myPair = new Pair<String, Long>("String 1", 1L);
        assertEquals("String 1", myPair.getFirst());
        assertEquals(Long.valueOf(1L), myPair.getSecond());

        myPair.setFirst("Hello");
        assertEquals("Hello", myPair.getFirst());

        myPair.setSecond(123L);
        assertEquals(Long.valueOf(123L), myPair.getSecond());
    }

    // ------------------------------------------------------------------------
    // equals
    // ------------------------------------------------------------------------

    public void testEqualsReflexivity() {
        assertTrue("equals", fPair1.equals(fPair1));
        assertTrue("equals", fPair2.equals(fPair2));

        assertTrue("equals", !fPair1.equals(fPair2));
        assertTrue("equals", !fPair2.equals(fPair1));
    }

    public void testEqualsSymmetry() {
        Pair<String, Long> info1 = new Pair<String, Long>(fPair1.getFirst(), fPair1.getSecond().longValue());
        Pair<String, Long> info2 = new Pair<String, Long>(fPair2.getFirst(), fPair2.getSecond().longValue());

        assertTrue("equals", info1.equals(fPair1));
        assertTrue("equals", fPair1.equals(info1));

        assertTrue("equals", info2.equals(fPair2));
        assertTrue("equals", fPair2.equals(info2));
    }

    public void testEqualsTransivity() {
        Pair<String, Long> info1 = new Pair<String, Long>(fPair1.getFirst(), fPair1.getSecond().longValue());
        Pair<String, Long> info2 = new Pair<String, Long>(fPair1.getFirst(), fPair1.getSecond().longValue());
        Pair<String, Long> info3 = new Pair<String, Long>(fPair1.getFirst(), fPair1.getSecond().longValue());

        assertTrue("equals", info1.equals(info2));
        assertTrue("equals", info2.equals(info3));
        assertTrue("equals", info1.equals(info3));
    }

    public void testEqualsNull() {
        assertTrue("equals", !fPair1.equals(null));
        assertTrue("equals", !fPair2.equals(null));
    }

    public void testEqualsDifferentObj() {
        Pair<Long, String> info = new Pair<Long, String>(1L, "String1");
        assertTrue("equals", !fPair1.equals(info));
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    public void testHashCode() {
        Pair<String, Long> info1 = new Pair<String, Long>(fPair1.getFirst(), fPair1.getSecond().longValue());
        Pair<String, Long> info2 = new Pair<String, Long>(fPair2.getFirst(), fPair2.getSecond().longValue());

        assertTrue("hashCode", fPair1.hashCode() == info1.hashCode());
        assertTrue("hashCode", fPair2.hashCode() == info2.hashCode());

        assertTrue("hashCode", fPair1.hashCode() != info2.hashCode());
        assertTrue("hashCode", fPair2.hashCode() != info1.hashCode());
    }
}
