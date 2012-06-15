/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial generation with CodePro tools
 *   Alexandre Montplaisir - Clean up, consolidate redundant tests
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.ctfadaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTimestamp;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTimestamp.TimestampType;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>CtfTmfTimestampTest</code> contains tests for the class
 * <code>{@link CtfTmfTimestamp}</code>.
 *
 * @author ematkho
 * @version 1.0
 */
public class CtfTmfTimestampTest {

    private CtfTmfTimestamp fixture;

    /**
     * Launch the test.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(CtfTmfTimestampTest.class);
    }

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        fixture = new CtfTmfTimestamp(1L);
        fixture.setType(CtfTmfTimestamp.TimestampType.DAY);
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        // Add additional tear down code here
    }


    /**
     * Run the CtfTmfTimestamp(long) constructor test.
     */
    @Test
    public void testCtfTmfTimestamp() {
        long timestamp = 1L;

        CtfTmfTimestamp result = new CtfTmfTimestamp(timestamp);
        result.setType(TimestampType.NANOS);

        assertNotNull(result);
        assertEquals("1 ns", result.toString()); //$NON-NLS-1$
        assertEquals(0, result.getPrecision());
        assertEquals(-9, result.getScale());
        assertEquals(1L, result.getValue());
    }


    /**
     * Run the boolean equals(Object) method test with another identical object.
     */
    @Test
    public void testEquals_same() {
        CtfTmfTimestamp obj = new CtfTmfTimestamp(1L);
        obj.setType(CtfTmfTimestamp.TimestampType.DAY);

        boolean result = fixture.equals(obj);
        assertTrue(result);
    }

    /**
     * Run the boolean equals(Object) method test, with an empty object.
     */
    @Test
    public void testEquals_empty() {
        Object obj = new Object();

        boolean result = fixture.equals(obj);
        assertFalse(result);
    }

    /**
     * Run the ITmfTimestamp getDelta(ITmfTimestamp) method test.
     */
    @Test
    public void testGetDelta() {
        ITmfTimestamp ts = new TmfTimestamp();
        ITmfTimestamp result = fixture.getDelta(ts);

        assertNotNull(result);
        assertEquals(0, result.getPrecision());
        assertEquals(-9, result.getScale());
        assertEquals(1L, result.getValue());
    }

    /**
     * Run the CtfTmfTimestamp.TimestampType getType() method test.
     */
    @Test
    public void testGetType() {
        CtfTmfTimestamp.TimestampType result = fixture.getType();

        assertNotNull(result);
        assertEquals("DAY", result.name()); //$NON-NLS-1$
        assertEquals("DAY", result.toString()); //$NON-NLS-1$
        assertEquals(1, result.ordinal());
    }

    /**
     * Run the int hashCode() method test.
     */
    @Test
    public void testHashCode() {
        int result = fixture.hashCode();
        assertEquals(1012115, result);
    }

    /**
     * Run the int hashCode() method test.
     */
    @Test
    public void testHashCode_nullType() {
        fixture.setType(null);
        int result = fixture.hashCode();
        assertEquals(944663, result);
    }

    /**
     * Run the void setType(TimestampType) method test.
     */
    @Test
    public void testSetType() {
        CtfTmfTimestamp.TimestampType value = CtfTmfTimestamp.TimestampType.DAY;
        fixture.setType(value);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString_ns() {
        fixture.setType(CtfTmfTimestamp.TimestampType.NANOS);
        String result = fixture.toString();
        assertEquals("1 ns", result); //$NON-NLS-1$
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString_s() {
        fixture.setType(CtfTmfTimestamp.TimestampType.SECONDS);
        String result = fixture.toString();
        assertEquals("1.0E-9 s", result); //$NON-NLS-1$
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString_day() {
        String result = fixture.toString();
        assertEquals("19:00:00.000000001", result); //$NON-NLS-1$
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString_full() {
        fixture.setType(CtfTmfTimestamp.TimestampType.FULL_DATE);
        String result = fixture.toString();
        assertEquals("1969-12-31 19:00:00.000000001", result); //$NON-NLS-1$
    }
}