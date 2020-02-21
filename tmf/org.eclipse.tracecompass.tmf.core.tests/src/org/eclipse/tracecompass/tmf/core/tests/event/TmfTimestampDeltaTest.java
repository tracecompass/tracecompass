/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Alexandre Montplaisir - Port to JUnit4
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestampDelta;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestampFormat;
import org.junit.Test;

/**
 * Test suite for the TmfTimestampDelta class.
 */
@SuppressWarnings("javadoc")
public class TmfTimestampDeltaTest {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private final ITmfTimestamp ts0 = new TmfTimestampDelta();
    private final ITmfTimestamp ts1 = new TmfTimestampDelta(12345,  0);
    private final ITmfTimestamp ts2 = new TmfTimestampDelta(12345, -1);
    private final ITmfTimestamp ts3 = new TmfTimestampDelta(12345,  2);
    private final ITmfTimestamp ts4 = new TmfTimestampDelta(-12345,  -5);

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    @Test
    public void testDefaultConstructor() {
        assertEquals("getValue", 0, ts0.getValue());
        assertEquals("getscale", 0, ts0.getScale());
    }

    @Test
    public void testValueConstructor() {
        assertEquals("getValue", 12345, ts1.getValue());
        assertEquals("getscale", 0, ts1.getScale());
    }

    @Test
    public void testValueScaleConstructor() {
        assertEquals("getValue", 12345, ts2.getValue());
        assertEquals("getscale", -1, ts2.getScale());
    }

    @Test
    public void testFullConstructor() {
        assertEquals("getValue", 12345, ts3.getValue());
        assertEquals("getscale", 2, ts3.getScale());

        assertEquals("getValue", -12345, ts4.getValue());
        assertEquals("getscale", -5, ts4.getScale());
    }

    @Test
    public void testCopyConstructor() {
        final ITmfTimestamp ts = TmfTimestamp.create(12345, 2);
        final ITmfTimestamp copy = TmfTimestamp.create(12345, 2);

        assertEquals("getValue", ts.getValue(), copy.getValue());
        assertEquals("getscale", ts.getScale(), copy.getScale());

        assertEquals("getValue", 12345, copy.getValue());
        assertEquals("getscale", 2, copy.getScale());
    }

    // ------------------------------------------------------------------------
    // normalize
    // ------------------------------------------------------------------------

    @Test
    public void testNormalizeOffset() {
        ITmfTimestamp ts = ts0.normalize(12345, 0);
        assertTrue("instance", ts instanceof TmfTimestampDelta);
        assertEquals("getValue", 12345, ts.getValue());
        assertEquals("getscale", 0, ts.getScale());
    }

    // ------------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------------

    @Test
    public void testToStringDefault() {
        assertEquals("toString", "000.000 000 000", ts0.toString());
        assertEquals("toString", "12345.000 000 000", ts1.toString());
        assertEquals("toString", "1234.500 000 000", ts2.toString());
        assertEquals("toString", "1234500.000 000 000", ts3.toString());
        assertEquals("toString", "-000.123 450 000", ts4.toString());
    }

    @Test
    public void testToStringFormat() {
        TmfTimestampFormat format = new TmfTimestampFormat("HH:mm:ss.SSS CCC NNN");
        assertEquals("toString", "00:00:00.000 000 000", ts0.toString(format));
        assertEquals("toString", "03:25:45.000 000 000", ts1.toString(format));
        assertEquals("toString", "00:20:34.500 000 000", ts2.toString(format));
        assertEquals("toString", "06:55:00.000 000 000", ts3.toString(format));
        assertEquals("toString", "-00:00:00.123 450 000", ts4.toString(format));
    }
}
