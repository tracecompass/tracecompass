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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.trace.ICTFPacketDescriptor;
import org.eclipse.tracecompass.internal.ctf.core.Activator;

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
     */
    public void appendAll(Collection<ICTFPacketDescriptor> preParsedIndex) {
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
     */
    public synchronized boolean append(@NonNull ICTFPacketDescriptor entry) {
        ICTFPacketDescriptor entryToAdd = entry;
        /* Validate consistent entry. */
        if (entryToAdd.getTimestampBegin() > entryToAdd.getTimestampEnd()) {
            Activator.log(IStatus.WARNING, "Packet at offset " + entryToAdd.getOffsetBytes() + //$NON-NLS-1$
                    " begin timestamp is after end timestamp"); //$NON-NLS-1$
            entryToAdd = new StreamInputPacketIndexEntry(entryToAdd, Long.MAX_VALUE);
        }

        /*
         * Validate entries are inserted in monotonic increasing timestamp
         * order.
         */
        if (!fEntries.isEmpty() && ((entryToAdd.getContentSizeBits() <= entryToAdd.getPayloadStartBits()) ||
                (entryToAdd.getTimestampBegin() < lastElement().getTimestampBegin() ||
                        entryToAdd.getOffsetBytes() <= lastElement().getOffsetBytes()))) {
            return false;
        }

        fEntries.add(entryToAdd);
        return true;
    }

    /**
     * Returns the first packet that could include the timestamp, that is the
     * first packet that includes the given timestamp, or if none exist, first
     * packet that begins before the given timestamp
     *
     * @param timestamp
     *            The timestamp to look for.
     * @return The index of the desired packet
     */
    public int search(final long timestamp) {
        /*
         * Search using binary search.
         *
         * As the entries in fEntries are IndexEntries, the key to search for
         * needs to be one too. We are looking for a timestamp though, so we use
         * the dataOffset which is a long and use it as a timestamp holder.
         */
        int index = Collections.binarySearch(fEntries, new StreamInputPacketIndexEntry(timestamp, 0), new FindTimestamp());
        if (index < 0) {
            index = -index - 1;
        }
        if (index >= fEntries.size()) {
            return fEntries.size() - 1;
        }
        for (int i = index; i > 0; i--) {
            ICTFPacketDescriptor entry = fEntries.get(i);
            ICTFPacketDescriptor nextEntry = fEntries.get(i - 1);
            if (entry.getTimestampEnd() >= timestamp && nextEntry.getTimestampEnd() < timestamp) {
                if (entry.getTimestampBegin() <= timestamp) {
                    return i;
                }
                return i - 1;
            }
        }
        return 0;

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
     * or {@code -1} if there is no such index. This will typically work in
     * log(n) time since the data structure contains elements in a non-repeating
     * increasing manner.
     *
     * Only the offset is checked in this case as there cannot be more than one
     * packet with a given offset.
     *
     * @param element
     *            element to search for
     * @return the index of the first occurrence of the specified element in
     *         this data structure, or -1 if this data structure does not
     *         contain the element
     * @throws ClassCastException
     *             if the type of the specified element is incompatible with
     *             this data structure (
     *             <a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException
     *             if the specified element is null and this data structure does
     *             not permit null elements (
     *             <a href="Collection.html#optional-restrictions">optional</a>)
     */
    public int indexOf(ICTFPacketDescriptor element) {
        int indexOf = -1;
        if (element != null) {
            indexOf = Collections.binarySearch(fEntries, element, Comparator.comparingLong(ICTFPacketDescriptor::getOffsetBytes));
        }
        return (indexOf < 0) ? -1 : indexOf;
    }

    /**
     * Used for search, assumes that the second argument in the comparison is
     * always the key
     */
    private static class FindTimestamp implements Comparator<ICTFPacketDescriptor>, Serializable {

        /**
         * UID
         */
        private static final long serialVersionUID = 7235997205945550341L;

        @Override
        public int compare(ICTFPacketDescriptor value, ICTFPacketDescriptor key) {
            /*
             * It is assumed that the second packet descriptor is the key, a
             * wrapped timestamp in a PacketDescriptor. So we need to extract
             * the timestamp. Then we have 3 choices, the if the timestamp is in
             * the interval, we return 0 or found. If the timestamp is before or
             * after, we just need to compare it to a value in the segment (like
             * start) to know if it is greater or lesser to the current packet.
             */
            long ts = key.getOffsetBits();
            if (value.includes(ts)) {
                return 0;
            }
            return Long.compare(value.getTimestampBegin(), ts);
        }

    }

}
