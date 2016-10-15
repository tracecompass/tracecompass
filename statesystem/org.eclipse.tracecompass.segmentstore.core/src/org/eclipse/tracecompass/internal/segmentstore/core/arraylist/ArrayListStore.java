/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.segmentstore.core.arraylist;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.segmentstore.core.BasicSegment;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.segmentstore.core.SegmentComparators;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

/**
 * Implementation of an {@link ISegmentStore} using one in-memory
 * {@link ArrayList}. This relatively simple implementation holds everything in
 * memory, and as such cannot contain too much data.
 *
 * The ArrayListStore itself is {@link Iterable}, and its iteration order will
 * be by ascending order of start times. For segments with identical start
 * times, the secondary comparator will be the end time. If even those are
 * equal, it will defer to the segments' natural ordering (
 * {@link ISegment#compareTo}).
 *
 * The store's tree maps will not accept duplicate key-value pairs, which means
 * that if you want several segments with the same start and end times, make
 * sure their compareTo() differentiates them.
 *
 * Removal operations are not supported.
 *
 * @param <E>
 *            The type of segment held in this store
 *
 * @author Matthew Khouzam
 */
public class ArrayListStore<@NonNull E extends ISegment> implements ISegmentStore<E> {

    private final Comparator<E> COMPARATOR = Ordering.from(SegmentComparators.INTERVAL_START_COMPARATOR)
            .compound(SegmentComparators.INTERVAL_END_COMPARATOR);

    private final ReadWriteLock fLock = new ReentrantReadWriteLock(false);

    private final List<E> fStore;

    private @Nullable transient Iterable<E> fLastSnapshot = null;

    /**
     * Constructor
     */
    public ArrayListStore() {
        fStore = new ArrayList<>();
    }

    /**
     * Constructor
     *
     * @param array
     *            an array of elements to wrap in the segment store
     *
     */
    public ArrayListStore(Object[] array) {
        fStore = new ArrayList<>();
        for (int i = 0; i < array.length; i++) {
            if (array[i] instanceof ISegment) {
                fStore.add((E) array[i]);
            }
        }
        fStore.sort(COMPARATOR);
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
                lastSnapshot = ImmutableList.copyOf(fStore);
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
            throw new IllegalArgumentException("Cannot add null value"); //$NON-NLS-1$
        }

        fLock.writeLock().lock();
        try {
            int insertPoint = Collections.binarySearch(fStore, val);
            insertPoint = insertPoint >= 0 ? insertPoint : -insertPoint - 1;
            fStore.add(insertPoint, val);
            fLastSnapshot = null;
            return true;
        } finally {
            fLock.writeLock().unlock();
        }
    }

    @Override
    public int size() {
        fLock.readLock().lock();
        try {
            return fStore.size();
        } finally {
            fLock.readLock().unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        fLock.readLock().lock();
        try {
            return fStore.isEmpty();
        } finally {
            fLock.readLock().unlock();
        }
    }

    @Override
    public boolean contains(@Nullable Object o) {
        fLock.readLock().lock();
        try {
            return fStore.contains(o);
        } finally {
            fLock.readLock().unlock();
        }
    }

    @Override
    public boolean containsAll(@Nullable Collection<?> c) {
        fLock.readLock().lock();
        try {
            return fStore.containsAll(c);
        } finally {
            fLock.readLock().unlock();
        }
    }

    @Override
    public Object[] toArray() {
        fLock.readLock().lock();
        try {
            return fStore.toArray();
        } finally {
            fLock.readLock().unlock();
        }
    }

    @Override
    public <T> T[] toArray(T[] a) {
        fLock.readLock().lock();
        try {
            return fStore.toArray(a);
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
                if (this.add(elem)) {
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
            fStore.clear();
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
            int index = Collections.binarySearch(fStore, new BasicSegment(end, Long.MAX_VALUE));
            index = (index >= 0) ? index : -index - 1;
            return fStore.subList(0, index).stream().filter(element -> !(start > element.getEnd() || end < element.getStart())).collect(Collectors.toList());
        } finally {
            fLock.readLock().unlock();
        }
    }

    @Override
    public void dispose() {
        fLock.writeLock().lock();
        try {
            fStore.clear();
        } finally {
            fLock.writeLock().unlock();
        }
    }
}
