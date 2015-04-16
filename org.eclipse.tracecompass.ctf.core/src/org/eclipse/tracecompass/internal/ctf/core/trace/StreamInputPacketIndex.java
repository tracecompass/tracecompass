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

package org.eclipse.tracecompass.internal.ctf.core.trace;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.TreeSet;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.trace.ICTFPacketDescriptor;

/**
 * <b><u>StreamInputPacketIndex</u></b>
 * <p>
 * This is a data structure containing entries, you may append to this and read
 * it. It is not thread safe.
 */
public class StreamInputPacketIndex {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * Entries of the index. They are sorted by increasing begin timestamp.
     * index builder.
     */
    private final List<ICTFPacketDescriptor> fEntries = new ArrayList<>();

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Returns the number of elements in this data structure. If this data
     * structure contains more than {@code Integer.MAX_VALUE} elements, returns
     * {@code Integer.MAX_VALUE}.
     *
     * @return the number of elements in this data structure
     */
    public int size() {
        return fEntries.size();
    }

    /**
     * Returns {@code true} if this data structure contains no elements.
     *
     * @return {@code true} if this data structure contains no elements
     */
    public boolean isEmpty() {
        return fEntries.isEmpty();
    }

    /**
     * Adds a collection of entries to the index, the entries must be sorted.
     *
     * @param preParsedIndex
     *            the pre-parsed index file
     *
     * @throws CTFException
     *             If there was a problem reading the entry
     */
    public void appendAll(Collection<ICTFPacketDescriptor> preParsedIndex)
            throws CTFException {
        for (ICTFPacketDescriptor sipie : preParsedIndex) {
            append(checkNotNull(sipie));
        }
    }

    /**
     * Appends the specified element to the end of this data structure
     *
     * @param entry
     *            element to be appended to this index, cannot be null
     * @return {@code true} (as specified by {@link Collection#add})
     * @throws CTFException
     *             If there was a problem reading the entry
     */
    public boolean append(@NonNull ICTFPacketDescriptor entry)
            throws CTFException {

        /* Validate consistent entry. */
        if (entry.getTimestampBegin() > entry.getTimestampEnd()) {
            throw new CTFException("Packet begin timestamp is after end timestamp"); //$NON-NLS-1$
        }

        /*
         * Validate entries are inserted in monotonic increasing timestamp
         * order.
         */
        if (!fEntries.isEmpty() && (entry.getTimestampBegin() < lastElement().getTimestampBegin())) {
            throw new CTFException("Packets begin timestamp decreasing"); //$NON-NLS-1$
        }

        fEntries.add(entry);
        return true;
    }

    /**
     * Returns the first PacketIndexEntry that could include the timestamp, that
     * is the last packet with a begin timestamp smaller than the given
     * timestamp.
     *
     * @param timestamp
     *            The timestamp to look for.
     * @return The StreamInputPacketEntry that corresponds to the packet that
     *         includes the given timestamp.
     */
    public ListIterator<ICTFPacketDescriptor> search(final long timestamp) {
        /*
         * Start with min and max covering all the elements.
         */
        int max = fEntries.size() - 1;
        int min = 0;

        int guessI;
        ICTFPacketDescriptor guessEntry = null;

        /*
         * If the index is empty, return the iterator at the very beginning.
         */
        if (isEmpty()) {
            return fEntries.listIterator();
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
            guessEntry = fEntries.get(guessI);

            /*
             * If we reached the point where we focus on a single packet, our
             * search is done.
             */
            if (min == max) {
                break;
            }

            if (timestamp <= guessEntry.getTimestampEnd()) {
                /*
                 * If the timestamp is lower or equal to the end of the guess
                 * packet, then the guess packet becomes the new inclusive max.
                 */
                max = guessI;
            } else {
                /*
                 * If the timestamp is greater than the end of the guess packet,
                 * then the new inclusive min is the packet after the guess
                 * packet.
                 */
                min = guessI + 1;
            }
        }

        return fEntries.listIterator(guessI);
    }

    /**
     * Get the last element of the index
     *
     * @return the last element in the index
     */
    public ICTFPacketDescriptor lastElement() {
        return fEntries.get(fEntries.size() - 1);
    }

    /**
     * Returns the element at the specified position in this data structure.
     *
     * @param index
     *            index of the element to return
     * @return the element at the specified position in this data structure
     * @throws IndexOutOfBoundsException
     *             if the index is out of range (
     *             {@code index < 0 || index >= size()})
     */
    public ICTFPacketDescriptor getElement(int index) {
        return fEntries.get(index);
    }

    /**
     * Returns the index of the first occurrence of the specified element in
     * this data structure, or -1 if this data structure does not contain the
     * element. More formally, returns the lowest index {@code i} such that, for
     * an entry {@code o}, {@code (o==null ? get(i)==null : o.equals(get(i)))},
     * or {@code -1} if there is no such index. This will work in log(n) time
     * since the data structure contains elements in a non-repeating increasing
     * manner.
     *
     * @param element
     *            element to search for
     * @return the index of the first occurrence of the specified element in
     *         this data structure, or -1 if this data structure does not
     *         contain the element
     * @throws ClassCastException
     *             if the type of the specified element is incompatible with
     *             this data structure (<a
     *             href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException
     *             if the specified element is null and this data structure does
     *             not permit null elements (<a
     *             href="Collection.html#optional-restrictions">optional</a>)
     */
    public int indexOf(ICTFPacketDescriptor element) {
        int indexOf = -1;
        if (element != null) {
            indexOf = Collections.binarySearch(fEntries, element, new MonotonicComparator());
        }
        return (indexOf < 0) ? -1 : indexOf;
    }

    /**
     * Ordering comparator for entering entries into a data structure sorted by
     * timestamp.
     */
    private static class MonotonicComparator implements Comparator<ICTFPacketDescriptor>, Serializable {
        /**
         * For {@link Serializable}, that way if we migrate to a {@link TreeSet}
         * the comparator is serializable too.
         */
        private static final long serialVersionUID = -5693064068367242076L;

        @Override
        public int compare(ICTFPacketDescriptor left, ICTFPacketDescriptor right) {
            if (left.getTimestampBegin() > right.getTimestampBegin()) {
                return 1;
            }
            if (left.getTimestampBegin() < right.getTimestampBegin()) {
                return -1;
            }
            if (left.getTimestampEnd() > right.getTimestampEnd()) {
                return 1;
            }
            if (left.getTimestampEnd() < right.getTimestampEnd()) {
                return -1;
            }
            return 0;
        }
    }

}
