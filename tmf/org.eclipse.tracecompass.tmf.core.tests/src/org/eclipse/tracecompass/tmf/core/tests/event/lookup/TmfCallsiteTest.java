/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.event.lookup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.lookup.ITmfCallsite;
import org.eclipse.tracecompass.tmf.core.event.lookup.TmfCallsite;
import org.junit.Test;

/**
 * Test suite for the TmfCallsite class.
 */
@SuppressWarnings("javadoc")
public class TmfCallsiteTest {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private final static @NonNull String fFileName1 = "filename1";
    private final static Long fLine1 = 10L;

    private final static @NonNull String fFileName2 = "filename2";
    private final static Long fLine2 = 25L;

    private final static @NonNull String fFileName3 = "filename3";
    private final static Long fLine3 = 123L;

    private final static @NonNull String fFileName4 = "filename1";
    private final static Long fLine4 = 11L;

    private final static @NonNull ITmfCallsite fCallsite1 = new TmfCallsite(fFileName1, fLine1);
    private final static @NonNull ITmfCallsite fCallsite2 = new TmfCallsite(fFileName2, fLine2);
    private final static @NonNull ITmfCallsite fCallsite3 = new TmfCallsite(fFileName3, fLine3);
    private final static @NonNull ITmfCallsite fCallsite4 = new TmfCallsite(fFileName4, fLine4);

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    @Test
    public void testDefaultConstructor() {
        assertEquals(fFileName1, fCallsite1.getFileName());
        assertEquals(fLine1, fCallsite1.getLineNo());
    }

    @Test
    public void testCallsiteCopy() {
        TmfCallsite copy = new TmfCallsite(fCallsite1);

        assertEquals(fFileName1, copy.getFileName());
        assertEquals(fLine1, copy.getLineNo());
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    @Test
    public void testHashCode() {
        final ITmfCallsite callsite1b = new TmfCallsite(fCallsite1);
        final ITmfCallsite callsite2b = new TmfCallsite(fCallsite2);
        final ITmfCallsite callsite3b = new TmfCallsite(fCallsite3);

        assertEquals("hashCode", fCallsite1.hashCode(), callsite1b.hashCode());
        assertEquals("hashCode", fCallsite2.hashCode(), callsite2b.hashCode());
        assertEquals("hashCode", fCallsite3.hashCode(), callsite3b.hashCode());
    }

    // ------------------------------------------------------------------------
    // equals
    // ------------------------------------------------------------------------

    @Test
    public void testEqualsReflexivity() {
        assertTrue("equals", fCallsite1.equals(fCallsite1));
        assertTrue("equals", fCallsite2.equals(fCallsite2));

        assertFalse("equals", fCallsite1.equals(fCallsite2));
        assertFalse("equals", fCallsite2.equals(fCallsite1));
        assertFalse("equals", fCallsite1.equals(fCallsite4));
        assertFalse("equals", fCallsite4.equals(fCallsite1));
    }

    @Test
    public void testEqualsSymmetry() {
        final ITmfCallsite callsite1 = new TmfCallsite(fCallsite1);
        final ITmfCallsite callsite2 = new TmfCallsite(fCallsite2);

        assertTrue("equals", callsite1.equals(fCallsite1));
        assertTrue("equals", fCallsite1.equals(callsite1));

        assertTrue("equals", callsite2.equals(fCallsite2));
        assertTrue("equals", fCallsite2.equals(callsite2));
    }

    @Test
    public void testEqualsTransivity() {
        final ITmfCallsite callsite1 = new TmfCallsite(fCallsite1);
        final ITmfCallsite callsite2 = new TmfCallsite(fCallsite1);
        final ITmfCallsite callsite3 = new TmfCallsite(fCallsite1);

        assertTrue("equals", callsite1.equals(callsite2));
        assertTrue("equals", callsite2.equals(callsite3));
        assertTrue("equals", callsite1.equals(callsite3));
    }

    @Test
    public void testEqualsNull() {
        assertFalse("equals", fCallsite1.equals(null));
        assertFalse("equals", fCallsite2.equals(null));
        assertFalse("equals", fCallsite3.equals(null));
    }

    @Test
    public void testNonEqualClasses() {
        assertFalse("equals", fCallsite1.equals(fCallsite1.getFileName()));
    }

    // ------------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------------

    @Test
    public void testToString() {
        assertEquals("toString", "filename1:10", fCallsite1.toString());
        assertEquals("toString", "filename2:25", fCallsite2.toString());
        assertEquals("toString", "filename3:123", fCallsite3.toString());
    }
}
