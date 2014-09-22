/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.tests.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.linuxtools.internal.ctf.core.trace.StreamInputPacketIndexEntry;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>StreamInputPacketIndexEntryTest</code> contains tests for the
 * class <code>{@link StreamInputPacketIndexEntry}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class CTFStreamInputPacketIndexEntryTest {

    private StreamInputPacketIndexEntry fixture;

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        fixture = new StreamInputPacketIndexEntry(1L);
    }

    /**
     * Run the StreamInputPacketIndexEntry(long) constructor test.
     */
    @Test
    public void testStreamInputPacketIndexEntry_1() {
        String expectedResult = "StreamInputPacketIndexEntry [offsetBytes=1, " +
                "timestampBegin=0, timestampEnd=0]";

        assertNotNull(fixture);
        assertEquals(expectedResult, fixture.toString());
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString() {
        String expectedResult = "StreamInputPacketIndexEntry [offsetBytes=1,"+
                " timestampBegin=1, timestampEnd=1]";


        fixture.setContentSizeBits(1);
        fixture.setDataOffsetBits(1);
        fixture.setTimestampEnd(1L);
        fixture.setPacketSizeBits(1);
        fixture.setTimestampBegin(1L);

        assertEquals(expectedResult, fixture.toString());
    }
}