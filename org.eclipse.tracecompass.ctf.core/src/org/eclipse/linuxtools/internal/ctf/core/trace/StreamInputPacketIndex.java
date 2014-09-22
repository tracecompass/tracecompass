/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 * Contributors: Etienne Bergeron <etienne.bergeron@gmail.com>
 * Contributors: Mathieu Desnoyers <mathieu.desnoyers@efficios.com>
 *******************************************************************************/

package org.eclipse.linuxtools.internal.ctf.core.trace;

import java.util.ListIterator;
import java.util.Vector;

import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;

/**
 * <b><u>StreamInputPacketIndex</u></b>
 * <p>
 * TODO Implement me. Please.
 */
public class StreamInputPacketIndex {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * Entries of the index. They are sorted by increasing begin timestamp.
     * index builder.
     */
    private final Vector<StreamInputPacketIndexEntry> entries = new Vector<>();

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * Gets the entries
     *
     * @return the entries
     */
    public Vector<StreamInputPacketIndexEntry> getEntries() {
        return this.entries;
    }

    /**
     * Gets an iterator to the entries
     *
     * @return an iterator to the entries
     */
    public ListIterator<StreamInputPacketIndexEntry> listIterator() {
        return this.entries.listIterator();
    }

    /**
     * Gets an iterator to the entries at a given position
     *
     * @param n
     *            the position to get
     * @return the iterator
     */
    public ListIterator<StreamInputPacketIndexEntry> listIterator(int n) {
        return this.entries.listIterator(n);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Adds an entry to the index.
     *
     * @param entry
     *            The entry to add
     * @throws CTFReaderException
     *             If there was a problem reading the entry
     */
    public void addEntry(StreamInputPacketIndexEntry entry)
            throws CTFReaderException {
        assert (entry.getContentSizeBits() != 0);

        /* Validate consistent entry. */
        if (entry.getTimestampBegin() > entry.getTimestampEnd()) {
            throw new CTFReaderException("Packet begin timestamp is after end timestamp"); //$NON-NLS-1$
        }

        /* Validate entries are inserted in monotonic increasing timestamp order. */
        if (!this.entries.isEmpty()) {
            if (entry.getTimestampBegin() < this.entries.lastElement()
                    .getTimestampBegin()) {
                throw new CTFReaderException("Packets begin timestamp decreasing"); //$NON-NLS-1$
            }
        }
        this.entries.add(entry);
    }

    /**
     * Returns the first PacketIndexEntry that could include the timestamp,
     * that is the last packet with a begin timestamp smaller than the given timestamp.
     *
     * @param timestamp
     *            The timestamp to look for.
     * @return The StreamInputPacketEntry that corresponds to the packet that
     *         includes the given timestamp.
     */
    public ListIterator<StreamInputPacketIndexEntry> search(final long timestamp) {
        /*
         * Start with min and max covering all the elements.
         */
        int max = this.entries.size() - 1;
        int min = 0;

        int guessI;
        StreamInputPacketIndexEntry guessEntry = null;

        /*
         * If the index is empty, return the iterator at the very beginning.
         */
        if (this.getEntries().isEmpty()) {
            return this.getEntries().listIterator();
        }

        if (timestamp < 0) {
            throw new IllegalArgumentException("timestamp is negative"); //$NON-NLS-1$
        }

        /* Binary search */
        for (;;) {
            /*
             * Guess in the middle of min and max.
             */
            guessI = min + ((max - min) / 2);
            guessEntry = this.entries.get(guessI);

            /*
             * If we reached the point where we focus on a single packet, our
             * search is done.
             */
            if (min == max) {
                break;
            }

            if (timestamp <= guessEntry.getTimestampEnd()) {
                /*
                 * If the timestamp is lower or equal to the end of the guess packet,
                 * then the guess packet becomes the new inclusive max.
                 */
                max = guessI;
            } else {
                /*
                 * If the timestamp is greater than the end of the guess packet, then
                 * the new inclusive min is the packet after the guess packet.
                 */
                min = guessI + 1;
            }
        }

        return this.entries.listIterator(guessI);
    }
}
