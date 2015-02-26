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
import static org.junit.Assert.assertNotNull;

import java.util.ListIterator;

import org.eclipse.tracecompass.ctf.core.CTFReaderException;
import org.eclipse.tracecompass.internal.ctf.core.trace.StreamInputPacketIndex;
import org.eclipse.tracecompass.internal.ctf.core.trace.StreamInputPacketIndexEntry;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>StreamInputPacketIndexTest</code> contains tests for the
 * class <code>{@link StreamInputPacketIndex}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
@SuppressWarnings("javadoc")
public class CTFStreamInputPacketIndexTest {

    private StreamInputPacketIndex fixture;
    private StreamInputPacketIndexEntry entry;

    /**
     * Perform pre-test initialization.
     *
     * @throws CTFReaderException
     */
    @Before
    public void setUp() throws CTFReaderException {
        fixture = new StreamInputPacketIndex();
        fixture.append(new StreamInputPacketIndexEntry(1L));
        entry = new StreamInputPacketIndexEntry(1L);
    }

    /**
     * Run the StreamInputPacketIndex() constructor test.
     */
    @Test
    public void testStreamInputPacketIndex() {
        assertNotNull(fixture);
    }

    /**
     * Run the void addEntry(StreamInputPacketIndexEntry) method test, by
     * specifying only 1 parameter to the entry.
     *
     * @throws CTFReaderException
     */
    @Test
    public void testAddEntry_1param() throws CTFReaderException {
        entry.setPacketSizeBits(0);
        assertNotNull(entry);
        fixture.append(entry);
    }

    /**
     * Run the void addEntry(StreamInputPacketIndexEntry) method test by
     * specifying 2 parameters to the entry.
     *
     * @throws CTFReaderException
     */
    @Test
    public void testAddEntry_2params() throws CTFReaderException {
        entry.setPacketSizeBits(1);
        entry.setContentSizeBits(0);
        assertNotNull(entry);
        fixture.append(entry);
    }

    /**
     * Run the void addEntry(StreamInputPacketIndexEntry) method test, by
     * specifying all 4 parameters to the entry.
     *
     * @throws CTFReaderException
     */
    @Test
    public void testAddEntry_4params() throws CTFReaderException {
        entry.setTimestampBegin(1L);
        entry.setPacketSizeBits(1);
        entry.setContentSizeBits(1);
        entry.setTimestampEnd(1L);
        assertNotNull(entry);
        fixture.append(entry);
    }

    /**
     * Run the ListIterator<StreamInputPacketIndexEntry> search(long) method
     * test with a valid timestamp.
     */
    @Test
    public void testSearch_valid() {
        ListIterator<StreamInputPacketIndexEntry> result = fixture.search(1L);

        assertNotNull(result);
        assertEquals(true, result.hasNext());
        assertEquals(-1, result.previousIndex());
        assertEquals(false, result.hasPrevious());
        assertEquals(0, result.nextIndex());
    }

    /**
     * Run the ListIterator<StreamInputPacketIndexEntry> search(long) method
     * test with an invalid timestamp.
     */
    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testSearch_invalid() {
        ListIterator<StreamInputPacketIndexEntry> result = fixture.search(-1L);

        assertNotNull(result);
    }
}