/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc., Alexandre Montplaisir
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

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultimap;

/**
 * Implementation of a {@link ISegmentStore} using in-memory {@link TreeMap}'s.
 * This relatively simple implementation holds everything in memory, and as such
 * cannot contain too much data.
 *
 * @param <T>
 *            The time of time range held
 *
 * @author Alexandre Montplaisir
 */
public class TreeMapStore<T extends ISegment> implements ISegmentStore<T> {

    private final TreeMultimap<Long, T> fStartTimesIndex;
    private final TreeMultimap<Long, T> fEndTimesIndex;

    private final Map<Long, T> fPositionMap;

    private volatile long fSize;

    /**
     *Constructor
     */
    public TreeMapStore() {
        fStartTimesIndex = checkNotNull(TreeMultimap.<Long, T> create(LONG_COMPARATOR, INTERVAL_START_COMPARATOR));
        fEndTimesIndex = checkNotNull(TreeMultimap.<Long, T> create(LONG_COMPARATOR, INTERVAL_END_COMPARATOR));
        fPositionMap = new HashMap<>();
        fSize = 0;
    }

    @Override
    public Iterator<T> iterator() {
        return checkNotNull(fStartTimesIndex.values().iterator());
    }

    @Override
    public synchronized void addElement(T val) {
        fStartTimesIndex.put(Long.valueOf(val.getStart()), val);
        fEndTimesIndex.put(Long.valueOf(val.getEnd()), val);
        fPositionMap.put(fSize, val);
        fSize++;
    }

    @Override
    public long getNbElements() {
        return fSize;
    }

    @Override
    public T getElementAtIndex(long index) {
        return checkNotNull(fPositionMap.get(Long.valueOf(index)));
    }

    @Override
    public Iterable<T> getIntersectingElements(long position) {
        /*
         * The intervals intersecting 't' are those whose 1) start time is
         * *lower* than 't' AND 2) end time is *higher* than 't'.
         */
        Iterable<T> matchStarts = Iterables.concat(fStartTimesIndex.asMap().headMap(position, true).values());
        Iterable<T> matchEnds = Iterables.concat(fEndTimesIndex.asMap().tailMap(position, true).values());

        return checkNotNull(Sets.intersection(Sets.newHashSet(matchStarts), Sets.newHashSet(matchEnds)));
    }

    @Override
    public Iterable<T> getIntersectingElements(long start, long end) {
        Iterable<T> matchStarts = Iterables.concat(fStartTimesIndex.asMap().headMap(end, true).values());
        Iterable<T> matchEnds = Iterables.concat(fEndTimesIndex.asMap().tailMap(start, true).values());

        return checkNotNull(Sets.intersection(Sets.newHashSet(matchStarts), Sets.newHashSet(matchEnds)));
    }

    @Override
    public synchronized void dispose() {
        fStartTimesIndex.clear();
        fEndTimesIndex.clear();
        fPositionMap.clear();
        fSize = 0;
    }

    // ------------------------------------------------------------------------
    // Comparators, used for the tree maps
    // ------------------------------------------------------------------------

    private static final Comparator<Long> LONG_COMPARATOR = new Comparator<Long>() {
        @Override
        public int compare(@Nullable Long o1, @Nullable Long o2) {
            if (o1 == null || o2 == null) {
                throw new IllegalArgumentException();
            }
            return o1.compareTo(o2);
        }
    };

    private static final Comparator<ISegment> INTERVAL_START_COMPARATOR = new Comparator<ISegment>() {
        @Override
        public int compare(@Nullable ISegment o1, @Nullable ISegment o2) {
            if (o1 == null || o2 == null) {
                throw new IllegalArgumentException();
            }
            return Long.compare(o1.getStart(), o2.getStart());
        }
    };

    private static final Comparator<ISegment> INTERVAL_END_COMPARATOR = new Comparator<ISegment>() {
        @Override
        public int compare(@Nullable ISegment o1, @Nullable ISegment o2) {
            if (o1 == null || o2 == null) {
                throw new IllegalArgumentException();
            }
            return Long.compare(o1.getEnd(), o2.getEnd());
        }
    };


}
