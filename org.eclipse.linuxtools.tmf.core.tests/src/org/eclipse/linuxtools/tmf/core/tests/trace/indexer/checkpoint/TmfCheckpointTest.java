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
 *   Francois Chouinard - Adapted for TMF Trace Model 1.0
 *   Alexandre Montplaisir - Port to JUnit4
 *   Patrick Tasse - Updated for location in checkpoint
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.trace.indexer.checkpoint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.TmfCheckpoint;
import org.eclipse.linuxtools.tmf.core.trace.location.ITmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.location.TmfLongLocation;
import org.junit.Test;

/**
 * Test suite for the TmfCheckpoint class.
 */
@SuppressWarnings("javadoc")
public class TmfCheckpointTest {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private ITmfTimestamp fTimestamp1 = new TmfTimestamp();
    private ITmfTimestamp fTimestamp2 = TmfTimestamp.BIG_BANG;
    private ITmfTimestamp fTimestamp3 = TmfTimestamp.BIG_CRUNCH;

    private Long aLong1 = 12345L;
    private Long aLong2 = 23456L;
    private Long aLong3 = 34567L;
    private ITmfLocation fLocation1 = new TmfLongLocation(aLong1);
    private ITmfLocation fLocation2 = new TmfLongLocation(aLong2);
    private ITmfLocation fLocation3 = new TmfLongLocation(aLong3);

    private TmfCheckpoint fCheckpoint1 = new TmfCheckpoint(fTimestamp1, fLocation1);
    private TmfCheckpoint fCheckpoint2 = new TmfCheckpoint(fTimestamp2, fLocation2);
    private TmfCheckpoint fCheckpoint3 = new TmfCheckpoint(fTimestamp3, fLocation3);

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    @Test
    public void testTmfCheckpoint() {
        assertEquals("TmfCheckpoint", fTimestamp1, fCheckpoint1.getTimestamp());
        assertEquals("TmfCheckpoint", fLocation1,  fCheckpoint1.getLocation());
    }

    public void testTmfLocationCopy() {
        final TmfCheckpoint checkpoint = new TmfCheckpoint(fCheckpoint1);

        assertEquals("TmfCheckpoint", fTimestamp1, checkpoint.getTimestamp());
        assertEquals("TmfCheckpoint", fLocation1,  checkpoint.getLocation());
    }

    @Test
    public void testTmfLocationCopy2() {
        try {
            new TmfCheckpoint(null);
            fail("null copy");
        }
        catch (final IllegalArgumentException e) {
            // Success
        }
        catch (final Exception e) {
            fail("wrong exception");
        }
    }

    // ------------------------------------------------------------------------
    // compareTo
    // ------------------------------------------------------------------------

    @Test
    public void testCompareTo() {
        assertEquals("compareTo",  0, fCheckpoint1.compareTo(fCheckpoint1));
        assertEquals("compareTo",  1, fCheckpoint1.compareTo(fCheckpoint2));
        assertEquals("compareTo", -1, fCheckpoint1.compareTo(fCheckpoint3));

        assertEquals("compareTo", -1, fCheckpoint2.compareTo(fCheckpoint1));
        assertEquals("compareTo",  0, fCheckpoint2.compareTo(fCheckpoint2));
        assertEquals("compareTo", -1, fCheckpoint2.compareTo(fCheckpoint3));

        assertEquals("compareTo",  1, fCheckpoint3.compareTo(fCheckpoint1));
        assertEquals("compareTo",  1, fCheckpoint3.compareTo(fCheckpoint2));
        assertEquals("compareTo",  0, fCheckpoint3.compareTo(fCheckpoint3));
    }

    @Test
    public void testCompareToNull() {
        final TmfCheckpoint checkpoint1 = new TmfCheckpoint(null, fLocation1);
        final TmfCheckpoint checkpoint2 = new TmfCheckpoint(null, fLocation2);
        final TmfCheckpoint checkpoint3 = new TmfCheckpoint(null, fLocation3);
        final TmfCheckpoint checkpoint4 = new TmfCheckpoint(null, fLocation1);

        // Test the various 'null' vs. '!null' combinations
        assertEquals("compareTo",  0, checkpoint1.compareTo(fCheckpoint1));
        assertEquals("compareTo",  0, fCheckpoint1.compareTo(checkpoint1));
        assertEquals("compareTo", -1, checkpoint1.compareTo(fCheckpoint2));
        assertEquals("compareTo",  1, fCheckpoint2.compareTo(checkpoint1));
        assertEquals("compareTo", -1, checkpoint1.compareTo(fCheckpoint3));
        assertEquals("compareTo",  1, fCheckpoint3.compareTo(checkpoint1));

        // Test the 'null' vs. 'null' combinations
        assertEquals("compareTo",  0, checkpoint1.compareTo(checkpoint4));
        assertEquals("compareTo",  0, checkpoint4.compareTo(checkpoint1));
        assertEquals("compareTo", -1, checkpoint1.compareTo(checkpoint2));
        assertEquals("compareTo",  1, checkpoint2.compareTo(checkpoint1));
        assertEquals("compareTo", -1, checkpoint1.compareTo(checkpoint3));
        assertEquals("compareTo",  1, checkpoint3.compareTo(checkpoint1));
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    @Test
    public void testHashCode() {
        final TmfCheckpoint checkpoint1 = new TmfCheckpoint(fCheckpoint1);
        final TmfCheckpoint checkpoint2 = new TmfCheckpoint(fCheckpoint2);

        assertTrue("hashCode", fCheckpoint1.hashCode() == checkpoint1.hashCode());
        assertTrue("hashCode", fCheckpoint2.hashCode() == checkpoint2.hashCode());

        assertTrue("hashCode", fCheckpoint1.hashCode() != checkpoint2.hashCode());
        assertTrue("hashCode", fCheckpoint2.hashCode() != checkpoint1.hashCode());
    }

    @Test
    public void testHashCodeNull() {
        final TmfCheckpoint checkpoint1 = new TmfCheckpoint(null, fLocation1);
        final TmfCheckpoint checkpoint2 = new TmfCheckpoint(fTimestamp1, null);
        final TmfCheckpoint checkpoint3 = new TmfCheckpoint(checkpoint1);
        final TmfCheckpoint checkpoint4 = new TmfCheckpoint(checkpoint2);

        assertTrue("hashCode", fCheckpoint1.hashCode() != checkpoint1.hashCode());
        assertTrue("hashCode", fCheckpoint1.hashCode() != checkpoint2.hashCode());

        assertTrue("hashCode", checkpoint1.hashCode() == checkpoint3.hashCode());
        assertTrue("hashCode", checkpoint2.hashCode() == checkpoint4.hashCode());
    }

    // ------------------------------------------------------------------------
    // equals
    // ------------------------------------------------------------------------

    @Test
    public void testEqualsReflexivity() {
        assertTrue("equals", fCheckpoint1.equals(fCheckpoint1));
        assertTrue("equals", fCheckpoint2.equals(fCheckpoint2));

        assertTrue("equals", !fCheckpoint1.equals(fCheckpoint2));
        assertTrue("equals", !fCheckpoint2.equals(fCheckpoint1));
    }

    @Test
    public void testEqualsSymmetry() {
        final TmfCheckpoint checkpoint1 = new TmfCheckpoint(fCheckpoint1);
        final TmfCheckpoint checkpoint2 = new TmfCheckpoint(fCheckpoint2);

        assertTrue("equals", checkpoint1.equals(fCheckpoint1));
        assertTrue("equals", fCheckpoint1.equals(checkpoint1));

        assertTrue("equals", checkpoint2.equals(fCheckpoint2));
        assertTrue("equals", fCheckpoint2.equals(checkpoint2));
    }

    @Test
    public void testEqualsTransivity() {
        final TmfCheckpoint checkpoint1 = new TmfCheckpoint(fCheckpoint1);
        final TmfCheckpoint checkpoint2 = new TmfCheckpoint(checkpoint1);
        final TmfCheckpoint checkpoint3 = new TmfCheckpoint(checkpoint2);

        assertTrue("equals", checkpoint1.equals(checkpoint2));
        assertTrue("equals", checkpoint2.equals(checkpoint3));
        assertTrue("equals", checkpoint1.equals(checkpoint3));
    }

    @Test
    public void testNotEqual() {
        // Various checkpoints
        final TmfCheckpoint checkpoint1 = new TmfCheckpoint(fTimestamp1, fLocation1);
        final TmfCheckpoint checkpoint2 = new TmfCheckpoint(fTimestamp2, fLocation1);
        final TmfCheckpoint checkpoint3 = new TmfCheckpoint(fTimestamp1, fLocation2);
        final TmfCheckpoint checkpoint4 = new TmfCheckpoint(fTimestamp1, null);
        final TmfCheckpoint checkpoint5 = new TmfCheckpoint(null, fLocation1);

        // Null check
        assertFalse("equals", checkpoint1.equals(null));

        // Different types
        assertFalse("equals", checkpoint1.equals(new TmfTimestamp()));

        // Null locations/location
        assertFalse("equals", checkpoint1.equals(checkpoint4));
        assertFalse("equals", checkpoint1.equals(checkpoint5));
        assertFalse("equals", checkpoint4.equals(checkpoint1));
        assertFalse("equals", checkpoint5.equals(checkpoint1));

        // Different locations/location
        assertFalse("equals", checkpoint1.equals(checkpoint2));
        assertFalse("equals", checkpoint1.equals(checkpoint3));
    }

    // ------------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------------

    @Test
    public void testToString() {
        final String expected1 = "TmfCheckpoint [fLocation=" + fCheckpoint1.getLocation() +
                ", fTimestamp=" + fCheckpoint1.getTimestamp() + "]";
        final String expected2 = "TmfCheckpoint [fLocation=" + fCheckpoint2.getLocation() +
                ", fTimestamp=" + fCheckpoint2.getTimestamp() + "]";
        final String expected3 = "TmfCheckpoint [fLocation=" + fCheckpoint3.getLocation() +
                ", fTimestamp=" + fCheckpoint3.getTimestamp() + "]";

        assertEquals("toString", expected1, fCheckpoint1.toString());
        assertEquals("toString", expected2, fCheckpoint2.toString());
        assertEquals("toString", expected3, fCheckpoint3.toString());
    }

}
