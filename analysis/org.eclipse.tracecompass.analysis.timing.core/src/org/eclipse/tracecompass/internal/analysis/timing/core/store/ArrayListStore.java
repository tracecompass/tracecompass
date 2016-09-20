/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.timing.core.store;

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

import com.google.common.collect.ImmutableList;

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

    private final Comparator<E> COMPARATOR = (o1, o2) -> {
        int ret = Long.compare(o1.getStart(), o2.getStart());
        if (ret == 0) {
            return Long.compare(o1.getEnd(), o2.getEnd());
        }
        return ret;
    };

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
        return fStore.size();
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
    public boolean remove(@Nullable Object o) {
        throw new UnsupportedOperationException();
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
    public boolean removeAll(@Nullable Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(@Nullable Collection<?> c) {
        throw new UnsupportedOperationException();
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
    public Iterable<E> getIntersectingElements(long position) {
        /*
         * The intervals intersecting 't' are those whose 1) start time is
         * *lower* than 't' AND 2) end time is *higher* than 't'.
         */
        fLock.readLock().lock();
        try {
            /*
             * as fStore is sorted by start then end times, restrict sub array
             * to elements whose start times <= t as stream.filter won't do it.
             */
            int index = Collections.binarySearch(fStore, new BasicSegment(position, Long.MAX_VALUE));
            index = (index >= 0) ? index : -index - 1;
            return fStore.subList(0, index).stream().filter(element -> position >= element.getStart() && position <= element.getEnd()).collect(Collectors.toList());
        } finally {
            fLock.readLock().unlock();
        }
    }

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
