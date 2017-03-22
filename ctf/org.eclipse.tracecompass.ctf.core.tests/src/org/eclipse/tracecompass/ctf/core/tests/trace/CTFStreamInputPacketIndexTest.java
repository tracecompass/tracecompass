/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.tests.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.trace.ICTFPacketDescriptor;
import org.eclipse.tracecompass.internal.ctf.core.trace.StreamInputPacketIndex;
import org.eclipse.tracecompass.internal.ctf.core.trace.StreamInputPacketIndexEntry;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>StreamInputPacketIndexTest</code> contains tests for the
 * class <code>{@link StreamInputPacketIndex}</code>.
 *
 * @author Matthew Khouzam
 */
@SuppressWarnings("javadoc")
public class CTFStreamInputPacketIndexTest {

    private StreamInputPacketIndex fFixture;

    /**
     * Perform pre-test initialization.
     *
     * @throws CTFException
     */
    @Before
    public void setUp() {
        fFixture = new StreamInputPacketIndex();
    }

    @Test
    public void testStreamInputPacketIndex() {
        assertNotNull(fFixture);
        assertTrue(fFixture.append(new StreamInputPacketIndexEntry(0, 1L)));
        assertTrue(fFixture.append(new PacketStub(1, 0, 0)));
    }

    /**
     * Run the StreamInputPacketIndex() constructor test.
     */
    @Test
    public void testStreamInputPacketIndexGet() {
        assertNotNull(fFixture);
        ICTFPacketDescriptor first = new StreamInputPacketIndexEntry(0, 1L);
        ICTFPacketDescriptor second = new PacketStub(1, 0, 0);
        assertTrue(fFixture.append(first));
        assertTrue(fFixture.append(second));

        assertEquals(first, fFixture.getElement(0));
        assertEquals(second, fFixture.getElement(1));
    }

    /**
     * Test on a contiguous set
     */
    @Test
    public void testStreamInputPacketIndexContiguous() {
        assertTrue(fFixture.append(new PacketStub(0, 0, 1)));
        assertTrue(fFixture.append(new PacketStub(1, 2, 3)));
        assertTrue(fFixture.append(new PacketStub(2, 4, 5)));
        assertTrue(fFixture.append(new PacketStub(3, 6, 6)));

        assertEquals(2, fFixture.search(5));
    }

    @Test
    public void testStreamInputPacketIndexDisjoint() {
        assertTrue(fFixture.append(new PacketStub(0, 0, 1)));
        assertTrue(fFixture.append(new PacketStub(1, 2, 3)));
        assertTrue(fFixture.append(new PacketStub(2, 6, 6)));

        assertEquals(1, fFixture.search(5));
        assertEquals(1, fFixture.search(3));
    }

    @Test
    public void testStreamInputPacketIndexOverlapping() {
        assertTrue(fFixture.append(new PacketStub(0, 0, 1)));
        assertTrue(fFixture.append(new PacketStub(1, 2, 3)));
        assertTrue(fFixture.append(new PacketStub(2, 6, 6)));
        assertTrue(fFixture.append(new PacketStub(3, 6, 6)));
        assertTrue(fFixture.append(new PacketStub(4, 6, 6)));
        assertTrue(fFixture.append(new PacketStub(5, 6, 6)));
        assertTrue(fFixture.append(new PacketStub(6, 6, 6)));
        assertTrue(fFixture.append(new PacketStub(7, 6, 6)));
        assertTrue(fFixture.append(new PacketStub(8, 6, 6)));
        assertTrue(fFixture.append(new PacketStub(9, 6, 6)));

        assertEquals(1, fFixture.search(5));
        assertEquals(2, fFixture.search(6));
    }

    @Test
    public void testStreamInputPacketIndexOverlappingBothSides() {
        assertTrue(fFixture.append(new PacketStub(0, 0, 3)));
        assertTrue(fFixture.append(new PacketStub(1, 0, 3)));
        assertTrue(fFixture.append(new PacketStub(2, 0, 3)));
        assertTrue(fFixture.append(new PacketStub(3, 7, 9)));
        assertTrue(fFixture.append(new PacketStub(4, 7, 9)));
        assertTrue(fFixture.append(new PacketStub(5, 7, 9)));
        assertEquals(0, fFixture.search(3));
        assertEquals(2, fFixture.search(6));
        assertEquals(3, fFixture.search(8));
    }

    @Test
    public void testStreamInputPacketIndexLargeOverlapping() {
        assertTrue(fFixture.append(new PacketStub(0, Long.MIN_VALUE, Long.MAX_VALUE)));
        assertTrue(fFixture.append(new PacketStub(1, Long.MIN_VALUE, Long.MAX_VALUE)));
        assertTrue(fFixture.append(new PacketStub(2, Long.MIN_VALUE, Long.MAX_VALUE)));
        assertTrue(fFixture.append(new PacketStub(3, Long.MIN_VALUE, Long.MAX_VALUE)));
        assertTrue(fFixture.append(new PacketStub(4, Long.MIN_VALUE, Long.MAX_VALUE)));

        assertEquals(0, fFixture.search(5));
        assertEquals(0, fFixture.search(6));
    }

    @Test
    public void testStreamInputPacketIndexInvalidAppend() {
        fFixture = new StreamInputPacketIndex();
        assertTrue(fFixture.append(new PacketStub(0, 0, 1)));
        assertFalse("Same offset", fFixture.append(new PacketStub(0, 1, 2)));
        assertFalse("Before", fFixture.append(new PacketStub(1, -1, 0)));
        assertFalse("Empty", fFixture.append(new PacketStub(2, 3, 4) {
            @Override
            public long getContentSizeBits() {
                return 0;
            }

            @Override
            public long getPayloadStartBits() {
                return 0;
            }
        }));
    }

}