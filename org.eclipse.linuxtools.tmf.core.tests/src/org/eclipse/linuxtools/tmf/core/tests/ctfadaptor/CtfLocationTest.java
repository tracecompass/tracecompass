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
import static org.junit.Assert.assertNotNull;

import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfLocation;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfLocationData;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>CtfLocationTest</code> contains tests for the class
 * <code>{@link CtfLocation}</code>.
 *
 * @author ematkho
 * @version 1.0
 */
public class CtfLocationTest {

    private CtfLocation fixture;

    /**
     * Launch the test.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(CtfLocationTest.class);
    }

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        fixture = new CtfLocation(new CtfLocationData(1, 0));
        fixture.setLocation(new CtfLocationData(1, 0));
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        // Add additional tear down code here
    }


    /**
     * Run the CtfLocation(Long) constructor test.
     */
    @Test
    public void testCtfLocation_long() {
        CtfLocationData location = new CtfLocationData(1, 0);
        CtfLocation result = new CtfLocation(location);

        assertNotNull(result);
        assertEquals(Long.valueOf(1), (Long)result.getLocationData().getTimestamp());
    }

    /**
     * Run the CtfLocation(ITmfTimestamp) constructor test.
     */
    @Test
    public void testCtfLocation_timestamp() {
        ITmfTimestamp timestamp = new TmfTimestamp();
        CtfLocation result = new CtfLocation(timestamp);

        assertNotNull(result);
        assertEquals(new Long(0L), (Long)result.getLocationData().getTimestamp());
    }

    /**
     * Run the CtfLocation clone() method test.
     */
    @Test
    public void testClone() {
        CtfLocation result = fixture.clone();

        assertNotNull(result);
        assertEquals(Long.valueOf(1), (Long)result.getLocationData().getTimestamp());
    }

    /**
     * Run the Long getLocation() method test.
     */
    @Test
    public void testGetLocation() {
        CtfLocationData location = fixture.getLocationData();
        Long result = location.getTimestamp();
        assertNotNull(result);
        assertEquals("1", result.toString()); //$NON-NLS-1$
        assertEquals((byte) 1, result.byteValue());
        assertEquals((short) 1, result.shortValue());
        assertEquals(1, result.intValue());
        assertEquals(1L, result.longValue());
        assertEquals(1.0f, result.floatValue(), 1.0f);
        assertEquals(1.0, result.doubleValue(), 1.0);
    }

    /**
     * Run the void setLocation(Long) method test.
     */
    @Test
    public void testSetLocation() {
        CtfLocationData location = new CtfLocationData(1337, 7331);
        fixture.setLocation(location);
    }

    /**
     * Test the toString() method with a valid location.
     */
    @Test
    public void testToString_valid(){
        CtfLocation fixture2 = new CtfLocation(new CtfLocationData(1337, 7331));
        assertEquals("CtfLocation: Element [1337/7331]",fixture2.toString()); //$NON-NLS-1$
    }

    /**
     * Test the toString() method with an invalid location.
     */
    @Test
    public void testToString_invalid(){
        CtfLocation fixture2 = new CtfLocation(new CtfLocationData(-1, -1));
        assertEquals("CtfLocation: INVALID",fixture2.toString()); //$NON-NLS-1$
    }
}
