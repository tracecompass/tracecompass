/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
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
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfLocationInfo;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
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
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        fixture = new CtfLocation(new CtfLocationInfo(1, 0));
    }

    /**
     * Run the CtfLocation(Long) constructor test.
     */
    @Test
    public void testCtfLocation_long() {
        CtfLocationInfo location = new CtfLocationInfo(1, 0);
        CtfLocation result = new CtfLocation(location);

        assertNotNull(result);
        assertEquals(Long.valueOf(1), (Long)result.getLocationInfo().getTimestamp());
    }

    /**
     * Run the CtfLocation(ITmfTimestamp) constructor test.
     */
    @Test
    public void testCtfLocation_timestamp() {
        ITmfTimestamp timestamp = new TmfTimestamp();
        CtfLocation result = new CtfLocation(timestamp);

        assertNotNull(result);
        assertEquals(new Long(0L), (Long)result.getLocationInfo().getTimestamp());
    }

    /**
     * Run the Long getLocation() method test.
     */
    @Test
    public void testGetLocation() {
        CtfLocationInfo location = fixture.getLocationInfo();
        Long result = location.getTimestamp();
        assertNotNull(result);
        assertEquals("1", result.toString());
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
        CtfLocationInfo location = new CtfLocationInfo(1337, 7331);
        fixture = new CtfLocation(location);
    }

    /**
     * Test the toString() method with a valid location.
     */
    @Test
    public void testToString_valid(){
        CtfLocation fixture2 = new CtfLocation(new CtfLocationInfo(1337, 7331));
        assertEquals("CtfLocation [fLocationInfo=Element [1337/7331]]", fixture2.toString());
    }

    /**
     * Test the toString() method with an invalid location.
     */
    @Test
    public void testToString_invalid(){
        CtfLocation fixture2 = new CtfLocation(new CtfLocationInfo(-1, -1));
        assertEquals("CtfLocation [INVALID]", fixture2.toString());
    }
}
