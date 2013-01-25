/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.ctfadaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfLocationData;
import org.junit.Before;
import org.junit.Test;

/**
 * Collection of tests for the {@link CtfLocationData}
 *
 * @author alexmont
 */
public class CtfLocationDataTest {

    private CtfLocationData fixture;

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        fixture = new CtfLocationData(1, 0);
    }

    /**
     * Test for the .getTimestamp() and .getIndex() methods
     */
    @Test
    public void testGetters() {
        long timestamp = fixture.getTimestamp();
        long index = fixture.getIndex();

        assertEquals(1, timestamp);
        assertEquals(0, index);
    }

    /**
     * Test for the .hashCode() method
     */
    @Test
    public void testHashCode() {
        int code = fixture.hashCode();
        assertEquals(962, code);
    }

    /**
     * Test for the .equals() method
     */
    @Test
    public void testEquals() {
        CtfLocationData same = new CtfLocationData(1, 0);
        CtfLocationData diff1 = new CtfLocationData(100, 0);
        CtfLocationData diff2 = new CtfLocationData(1, 10);

        assertTrue(fixture.equals(same));
        assertFalse(fixture.equals(diff1));
        assertFalse(fixture.equals(diff2));
    }

    /**
     * Test for the .compareTo() method
     */
    @Test
    public void testCompareTo() {
        CtfLocationData same = new CtfLocationData(1, 0);
        CtfLocationData smaller = new CtfLocationData(0, 0);
        CtfLocationData bigger1 = new CtfLocationData(1000, 500);
        CtfLocationData bigger2 = new CtfLocationData(1, 1);

        assertEquals(0, same.compareTo(fixture));
        assertEquals(-1, smaller.compareTo(fixture));
        assertEquals(1, bigger1.compareTo(fixture));
        assertEquals(1, bigger2.compareTo(fixture));
    }

    /**
     * Test for the .toString() method
     */
    @Test
    public void testToString() {
        String expected = "Element [1/0]"; //$NON-NLS-1$
        assertEquals(expected, fixture.toString());
    }
}
