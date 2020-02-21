/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc., Alexandre Montplaisir and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.segmentstore.core.treemap;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.segmentstore.core.BasicSegment;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.TreeMultimap;

/**
 * Implementation of a {@link ISegmentStore} using an in-memory {@link TreeMultimap}s.
 * This relatively simple implementation holds everything in memory, and as such
 * cannot contain too much data.
 *
 * The TreeMapStore itself is Iterable, and its iteration order will be by
 * ascending order of start times. For segments with identical start times, the
 * secondary comparator will be the end time. If even those are equal, it will
 * defer to the segments' natural ordering ({@link ISegment#compareTo}).
 *
 * The store's tree map will not accept duplicate key-value pairs, which means
 * that if you want several segments with the same start and end times, make
 * sure their compareTo() differentiates them.
 *
 * Removal operations are not supported.
 *
 * @param <E>
 *            The type of segment held in this store
 *
 * @author Alexandre Montplaisir
 */
public class TreeMapStore<@NonNull E extends ISegment> implements ISegmentStore<E> {

    private final ReadWriteLock fLock = new ReentrantReadWriteLock(false);

    private final TreeMultimap<Long, E> fStartTimesIndex;

    private volatile int fSize;
    private volatile long fStart = Long.MAX_VALUE;
    private volatile long fEnd = Long.MIN_VALUE;

    private @Nullable transient Iterable<E> fLastSnapshot = null;

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
         */
        fStartTimesIndex = TreeMultimap.create(Comparator.<Long>naturalOrder(),
                Comparator.comparingLong(E::getEnd).thenComparing(Function.identity()));

        fSize = 0;
    }

    // ------------------------------------------------------------------------
    // Methods from Collection
    // ------------------------------------------------------------------------

    @Override
    public Iterator<E> iterator() {
        fLock.readLock().lock();
        try {
            Iterable<E> lastSnapshot = fLastSnapshot;
            if (lastSnapshot == null) {
                lastSnapshot = ImmutableList.copyOf(fStartTimesIndex.values());
                fLastSnapshot = lastSnapshot;
            }
            return checkNotNull(lastSnapshot.iterator());
        } finally {
            fLock.readLock().unlock();
        }
    }

    @Override
    public boolean add(@Nullable E val) {
        if (val == null) {
            throw new IllegalArgumentException();
        }

        fLock.writeLock().lock();
        try {
            boolean put = fStartTimesIndex.put(val.getStart(), val);
            if (put) {
                fSize++;
                fStart = Math.min(fStart, val.getStart());
                fEnd = Math.max(fEnd, val.getEnd());
                fLastSnapshot = null;
            }
            return put;
        } finally {
            fLock.writeLock().unlock();
        }
    }

    @Override
    public int size() {
        return fSize;
    }

    @Override
    public boolean isEmpty() {
        return (fSize == 0);
    }

    @Override
    public boolean contains(@Nullable Object o) {
        if (o == null || !(o instanceof ISegment)) {
            return false;
        }
        fLock.readLock().lock();
        try {
            /* Narrow down the search */
            ISegment seg = (ISegment) o;
            return fStartTimesIndex.get(seg.getStart()).contains(o);
        } finally {
            fLock.readLock().unlock();
        }
    }

    @Override
    public boolean containsAll(@Nullable Collection<?> c) {
        fLock.readLock().lock();
        try {
            return fStartTimesIndex.values().containsAll(c);
        } finally {
            fLock.readLock().unlock();
        }
    }

    @Override
    public Object[] toArray() {
        fLock.readLock().lock();
        try {
            return fStartTimesIndex.values().toArray();
        } finally {
            fLock.readLock().unlock();
        }
    }

    @Override
    public <T> T[] toArray(T[] a) {
        fLock.readLock().lock();
        try {
            return fStartTimesIndex.values().toArray(a);
        } finally {
            fLock.readLock().unlock();
        }
    }

    @Override
    public boolean addAll(@Nullable Collection<? extends E> c) {
        if (c == null) {
            throw new IllegalArgumentException();
        }

        fLock.writeLock().lock();
        try {
            boolean changed = false;
            for (E elem : c) {
                if (add(elem)) {
                    changed = true;
                }
            }
            return changed;
        } finally {
            fLock.writeLock().unlock();
        }
    }

    @Override
    public void clear() {
        fLock.writeLock().lock();
        try {
            fSize = 0;
            fStart = Long.MAX_VALUE;
            fEnd = Long.MIN_VALUE;
            fStartTimesIndex.clear();
        } finally {
            fLock.writeLock().unlock();
        }
    }

    // ------------------------------------------------------------------------
    // Methods added by ISegmentStore
    // ------------------------------------------------------------------------

    @Override
    public Iterable<E> getIntersectingElements(long start, long end) {
        fLock.readLock().lock();
        try {
            if (start <= fStart && end >= fEnd) {
                if (fLastSnapshot == null) {
                    fLastSnapshot = ImmutableList.copyOf(fStartTimesIndex.values());
                }
                return checkNotNull(fLastSnapshot);
            }
            List<E> iterable = new ArrayList<>();
            /**
             * fromElement is used to search the navigable sets of the
             * TreeMultiMap for Segments that end after start query time.
             */
            E fromElement = (E) new BasicSegment(Long.MIN_VALUE, start);
            /* Get the sets of segments for startTimes <= end */
            for (Collection<E> col : fStartTimesIndex.asMap().headMap(end, true).values()) {
                /*
                 * The collections of segments are NavigableSets for
                 * TreeMultimap, add elements from the tailSet: which will have
                 * endTimes >= start.
                 */
                NavigableSet<E> nav = (NavigableSet<E>) col;
                iterable.addAll(nav.tailSet(fromElement, true));
            }
            return iterable;
        } finally {
            fLock.readLock().unlock();
        }
    }

    @Override
    public void dispose() {
        clear();
    }
}
