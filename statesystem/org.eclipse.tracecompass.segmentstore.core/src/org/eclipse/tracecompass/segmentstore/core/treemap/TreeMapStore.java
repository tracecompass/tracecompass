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

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.segmentstore.core.SegmentStoreFactory;

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
 * Removal operations are not supported.
 *
 * @param <E>
 *            The type of segment held in this store
 *
 * @author Alexandre Montplaisir
 * @deprecated Use the {@link SegmentStoreFactory} to create a new segment store
 */
@Deprecated
public class TreeMapStore<@NonNull E extends ISegment> extends org.eclipse.tracecompass.internal.segmentstore.core.treemap.TreeMapStore<E> {

    /**
     * Constructor
     */
    public TreeMapStore() {
        super();
    }

    // ------------------------------------------------------------------------
    // Methods from Collection
    // ------------------------------------------------------------------------

    @Override
    public Iterator<E> iterator() {
        return super.iterator();
    }

    @Override
    public boolean add(@Nullable E val) {
        return super.add(val);
    }

    @Override
    public int size() {
        return super.size(); // me
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty();
    }

    @Override
    public boolean contains(@Nullable Object o) {
        return super.contains(o);
    }

    @Override
    public boolean containsAll(@Nullable Collection<?> c) {
        return super.containsAll(c);
    }

    @Override
    public Object[] toArray() {
        return super.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return super.toArray(a);
    }

    @Override
    public boolean remove(@Nullable Object o) {
        return super.remove(o);
    }

    @Override
    public boolean addAll(@Nullable Collection<? extends E> c) {
        return super.addAll(c);
    }

    @Override
    public boolean removeAll(@Nullable Collection<?> c) {
        return super.removeAll(c);
    }

    @Override
    public boolean retainAll(@Nullable Collection<?> c) {
        return super.retainAll(c);
    }

    @Override
    public void clear() {
        super.clear();
    }

    // ------------------------------------------------------------------------
    // Methods added by ISegmentStore
    // ------------------------------------------------------------------------

    @Override
    public Iterable<E> getIntersectingElements(long position) {
        return super.getIntersectingElements(position);
    }

    @Override
    public Iterable<E> getIntersectingElements(long start, long end) {
        return super.getIntersectingElements(start, end);
    }

    @Override
    public void dispose() {
        super.dispose();
    }

}
