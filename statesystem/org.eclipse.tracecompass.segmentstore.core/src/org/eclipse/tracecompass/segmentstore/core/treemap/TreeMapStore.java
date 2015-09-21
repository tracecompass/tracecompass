/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc., Alexandre Montplaisir and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.segmentstore.core.treemap;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Iterator;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.segmentstore.core.SegmentComparators;

import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultimap;

/**
 * Implementation of a {@link ISegmentStore} using in-memory {@link TreeMap}'s.
 * This relatively simple implementation holds everything in memory, and as such
 * cannot contain too much data.
 *
 * The TreeMapStore itself is Iterable, and its iteration order will be by
 * ascending order of start times. For segments with identical start times, the
 * secondary comparator will be the end time. If even those are equal, it will
 * defer to the segments' natural ordering ({@link ISegment#compareTo}).
 *
 * The store's tree maps will not accept duplicate key-value pairs, which means
 * that if you want several segments with the same start and end times, make
 * sure their compareTo() differentiates them.
 *
 * @param <T>
 *            The type of segment held in this store
 *
 * @author Alexandre Montplaisir
 */
public class TreeMapStore<T extends ISegment> implements ISegmentStore<T> {

    private final ReadWriteLock fLock = new ReentrantReadWriteLock(false);

    private final TreeMultimap<Long, T> fStartTimesIndex;
    private final TreeMultimap<Long, T> fEndTimesIndex;

    private long fSize;

    /**
     * Constructor
     */
    public TreeMapStore() {
        /*
         * For the start times index, the "key comparator" will compare the
         * start times as longs directly. This is the primary comparator for its
         * tree map.
         *
         * The secondary "value" comparator will check the end times first, and
         * in the event of a tie, defer to the ISegment's Comparable
         * implementation, a.k.a. its natural ordering.
         *
         * The same is done for the end times index, but swapping the first two
         * comparators instead.
         */
        fStartTimesIndex = checkNotNull(TreeMultimap.<Long, T> create(
                SegmentComparators.LONG_COMPARATOR,
                Ordering.from(SegmentComparators.INTERVAL_END_COMPARATOR).compound(Ordering.natural())));

        fEndTimesIndex = checkNotNull(TreeMultimap.<Long, T> create(
                SegmentComparators.LONG_COMPARATOR,
                Ordering.from(SegmentComparators.INTERVAL_START_COMPARATOR).compound(Ordering.natural())));

        fSize = 0;
    }

    /**
     * Warning, this is not thread safe, and can cause concurrent modification
     * exceptions
     */
    @Override
    public Iterator<T> iterator() {
        return checkNotNull(fStartTimesIndex.values().iterator());
    }

    @Override
    public void addElement(T val) {
        fLock.writeLock().lock();
        try {
            if (fStartTimesIndex.put(Long.valueOf(val.getStart()), val)) {
                fEndTimesIndex.put(Long.valueOf(val.getEnd()), val);
                fSize++;
            }
        } finally {
            fLock.writeLock().unlock();
        }
    }

    @Override
    public long getNbElements() {
        fLock.readLock().lock();
        try {
            return fSize;
        } finally {
            fLock.readLock().unlock();
        }
    }

    @Override
    public Iterable<T> getIntersectingElements(long position) {
        /*
         * The intervals intersecting 't' are those whose 1) start time is
         * *lower* than 't' AND 2) end time is *higher* than 't'.
         */
        fLock.readLock().lock();
        try {
            Iterable<T> matchStarts = Iterables.concat(fStartTimesIndex.asMap().headMap(position, true).values());
            Iterable<T> matchEnds = Iterables.concat(fEndTimesIndex.asMap().tailMap(position, true).values());
            return checkNotNull(Sets.intersection(Sets.newHashSet(matchStarts), Sets.newHashSet(matchEnds)));
        } finally {
            fLock.readLock().unlock();
        }
    }

    @Override
    public Iterable<T> getIntersectingElements(long start, long end) {
        fLock.readLock().lock();
        try {
            Iterable<T> matchStarts = Iterables.concat(fStartTimesIndex.asMap().headMap(end, true).values());
            Iterable<T> matchEnds = Iterables.concat(fEndTimesIndex.asMap().tailMap(start, true).values());
            return checkNotNull(Sets.intersection(Sets.newHashSet(matchStarts), Sets.newHashSet(matchEnds)));
        } finally {
            fLock.readLock().unlock();
        }
    }

    @Override
    public void dispose() {
        fLock.writeLock().lock();
        try {
            fStartTimesIndex.clear();
            fEndTimesIndex.clear();
            fSize = 0;
        } finally {
            fLock.writeLock().unlock();
        }
    }
}
