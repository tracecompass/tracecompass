/*******************************************************************************
 * Copyright (c) 2020 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;

/**
 * Turns the non-null intervals of given state system attributes into segments
 *
 * @author Geneviève Bastien
 */
public class IOStateSystemSegmentStore implements ISegmentStore<ISegment> {

    private final ReadWriteLock fLock = new ReentrantReadWriteLock(false);
    private final ITmfStateSystem fStateSystem;
    private final Map<Integer, Disk> fSegmentQuarks;

    Comparator<ITmfStateInterval> cmp = (x, y) -> {
        if (x.getStartTime() < y.getStartTime()) {
            return -1;
        }
        if (x.getEndTime() < y.getEndTime()) {
            return -1;
        }
        return 1;
    };


    /**
     * Constructor
     *
     * @param ss
     *            The state system from which to get the intervals
     * @param quarkToDisk
     *            A map of the quarks whose intervals are part of the segment
     *            store, with a map to the disk object they belong to
     */
    public IOStateSystemSegmentStore(ITmfStateSystem ss, Map<Integer, Disk> quarkToDisk) {
        fStateSystem = ss;
        fSegmentQuarks = quarkToDisk;
    }

    @Override
    public boolean add(@NonNull ISegment arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(@Nullable Collection<? extends @NonNull ISegment> arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(@Nullable Object arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(@Nullable Collection<?> arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public Iterator<@NonNull ISegment> iterator() {
        return new SegmentIterator();
    }

    private class SegmentIterator implements Iterator<@NonNull ISegment> {

        private final long fEndTime;
        private final @Nullable Iterator<ITmfStateInterval> fIterator;
        private @Nullable ISegment fSegment;

        public SegmentIterator() {
            fEndTime = fStateSystem.getCurrentEndTime();
            long startTime = fStateSystem.getStartTime();
            Iterator<ITmfStateInterval> iterator = null;
            try {
                Iterable<ITmfStateInterval> query2d = fStateSystem.query2D(fSegmentQuarks.keySet(), startTime, fEndTime);
                iterator = query2d.iterator();
            } catch (IndexOutOfBoundsException | TimeRangeException | StateSystemDisposedException e) {
                iterator = null;
            }
            fIterator = iterator;
        }

        @Override
        public boolean hasNext() {
            fLock.writeLock().lock();
            try {
                Iterator<ITmfStateInterval> iterator = fIterator;
                if (iterator == null) {
                    return false;
                }
                ISegment nextSegment = fSegment;
                if (nextSegment != null) {
                    return true;
                }
                while (iterator.hasNext() && nextSegment == null) {
                    ITmfStateInterval next = iterator.next();
                    nextSegment = RequestIntervalSegment.create(next, fSegmentQuarks.get(next.getAttribute()));
                }
                fSegment = nextSegment;
                return fSegment != null;
            } finally {
                fLock.writeLock().unlock();
            }
        }

        @Override
        public @NonNull ISegment next() {
            fLock.writeLock().lock();
            try {
                if (!hasNext()) {
                    throw new NoSuchElementException("No more segments"); //$NON-NLS-1$
                }
                ISegment segment = Objects.requireNonNull(fSegment, "Segment should not be null"); //$NON-NLS-1$
                fSegment = null;
                return segment;
            } finally {
                fLock.writeLock().unlock();
            }
        }

    }

    @Override
    public boolean remove(@Nullable Object arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(@Nullable Collection<?> arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(@Nullable Collection<?> arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Object @NonNull [] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T @NonNull [] toArray(T @NonNull [] arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NonNull Iterable<@NonNull ISegment> getIntersectingElements(long position) {
        return getIntersectingElements(position, position);
    }

    @Override
    public @NonNull Iterable<@NonNull ISegment> getIntersectingElements(long start, long end) {
        long startTime = Math.max(start, fStateSystem.getStartTime());
        long endTime = Math.min(end, fStateSystem.getCurrentEndTime());
        if (startTime > endTime) {
            return Collections.emptySet();
        }
        List<@NonNull ISegment> segments = new ArrayList<>();
        try {
            for (ITmfStateInterval interval : fStateSystem.query2D(fSegmentQuarks.keySet(), startTime, endTime)) {
                RequestIntervalSegment segment = RequestIntervalSegment.create(interval, fSegmentQuarks.get(interval.getAttribute()));
                if (segment != null) {
                    segments.add(segment);
                }
            }
        } catch (IndexOutOfBoundsException | TimeRangeException | StateSystemDisposedException e1) {
            // Nothing to do
        }
        return segments;
    }

    @Override
    public void dispose() {
        // State system will be disposed with its analysis, nothing to do here
    }

}
