/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   France Lapointe Nguyen - Initial API and implementation
 *   Bernd Hufmann - MOve abstract class to TMF
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.table;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.ui.viewers.table.ISortingLazyContentProvider;

import com.google.common.collect.Iterables;

/**
 * Content provider for the latency table viewers.
 *
 * @author France Lapointe Nguyen
 */
public class SegmentStoreContentProvider implements ISortingLazyContentProvider {

    /**
     * Class that wraps a segment store and a time range.
     *
     * Note: this class is not thread-safe. It is not meant to be used by many
     * threads simultaneously. Many methods are synchronized, so at best, the
     * performances will be bad, but at worst, it may return false results.
     *
     * @author Geneviève Bastien
     * @param <E>
     *            The type of segment in the segment store
     */
    public static class SegmentStoreWithRange<E extends ISegment> implements Iterable<E> {
        /**
         * Constant used in the {@link #getElement(long)} method to return the
         * last element of the store
         */
        public static final long LAST = Long.MIN_VALUE;
        private final ISegmentStore<E> fSegmentStore;
        private final TmfTimeRange fRange;

        private @Nullable Comparator<ISegment> fComparator = null;
        private @Nullable Iterable<E> fIterable = null;
        private @Nullable Predicate<E> fPredicate = null;

        private long fLastReadPos = -1;
        private @Nullable Iterator<E> fIterator = null;

        /**
         * Constructor
         *
         * @param segStore
         *            The segment store
         * @param range
         *            The time range to get for this segment store
         */
        public SegmentStoreWithRange(ISegmentStore<E> segStore, TmfTimeRange range) {
            fSegmentStore = segStore;
            fRange = range;
        }

        /**
         * Constructor
         *
         * @param segStore
         *            The segment store
         * @param range
         *            The time range to get for this segment store
         * @param predicate
         *            An extra predicate to further filter the segments
         */
        public SegmentStoreWithRange(ISegmentStore<E> segStore, TmfTimeRange range, Predicate<E> predicate) {
            fSegmentStore = segStore;
            fRange = range;
            fPredicate = predicate;
        }

        @Override
        public Iterator<E> iterator() {
            return NonNullUtils.checkNotNull(getIterable().iterator());
        }

        /**
         * Set the comparator for this store
         *
         * @param comparator
         *            The comparator to use for this store
         */
        public void setComparator(Comparator<ISegment> comparator) {
            fComparator = comparator;
            fIterable = null;
            resetIterator();
        }

        /**
         * Get the iterable to iterate over this segment store
         *
         * @return The iterable object to iterate through the segment store
         */
        private Iterable<E> getIterable() {
            Iterable<E> iterable = fIterable;
            if (iterable == null) {
                Comparator<ISegment> comparator = fComparator;
                Predicate<? super E> predicate = fPredicate;
                if (comparator != null) {
                    iterable = fSegmentStore.getIntersectingElements(fRange.getStartTime().toNanos(), fRange.getEndTime().toNanos(), comparator);
                } else {
                    iterable = fSegmentStore.getIntersectingElements(fRange.getStartTime().toNanos(), fRange.getEndTime().toNanos());
                }
                if (predicate != null) {
                    iterable = Iterables.filter(iterable, input -> predicate.test(input));
                }
                fIterable = iterable;
            }
            return iterable;
        }

        private Iterator<? extends @NonNull ISegment> resetIterator() {
            Iterator<E> iterator = NonNullUtils.checkNotNull(getIterable().iterator());
            fIterator = iterator;
            fLastReadPos = -1;
            return iterator;
        }

        /**
         * Get the element at position index. If the index is {@link #LAST}, it
         * will return the last element from the end of the iterator, if a
         * comparator was specified, otherwise it will return the first event.
         * This method with {@link #LAST} is meant to be used with sorted
         * stores.
         *
         * @param index
         *            The index of the requested element. If index is
         *            {@link #LAST} it will return the last element, at the end
         *            of the iterator.
         * @return The segment at position index or <code>null</code> if it is
         *         not available
         */
        public @Nullable ISegment getElement(long index) {
            long idx = index;
            // Special code path if we are looking for an element from the end there is a comparator
            if (index == LAST) {
                Comparator<@NonNull ISegment> comparator = fComparator;
                if (comparator != null) {
                    return getLastElement(comparator);
                }
                // No comparator, so impossible the easily get last element,
                // just return the first as it is random anyway
                idx = 0;
            }
            Iterable<? extends @NonNull ISegment> iterable = fIterable;
            if (iterable instanceof List<?> && idx <= Integer.MAX_VALUE) {
                return Iterables.get(iterable, (int) idx, null);
            }
            Iterator<? extends @NonNull ISegment> iterator = fIterator;
            if (iterator == null || idx <= fLastReadPos) {
                iterator = resetIterator();
            }
            ISegment segment = null;
            while (fLastReadPos < idx && iterator.hasNext()) {
                fLastReadPos++;
                segment = NonNullUtils.checkNotNull(iterator.next());
            }
            if (fLastReadPos == idx) {
                return segment;
            }
            return null;
        }

        private @Nullable ISegment getLastElement(Comparator<@NonNull ISegment> comparator) {
            Iterable<? extends ISegment> baseIterable = fIterable;
            if (baseIterable instanceof List<?>) {
                return Iterables.getLast(baseIterable, null);
            }
            // Not a trivial get, so get an iterable for the reverse comparator
            // and fetch first element
            Predicate<? super E> predicate = fPredicate;
            Iterable<E> iterable = fSegmentStore.getIntersectingElements(fRange.getStartTime().toNanos(), fRange.getEndTime().toNanos(), comparator.reversed());
            if (predicate != null) {
                iterable = Iterables.filter(iterable, input -> predicate.test(input));
            }
            // FIXME: The cast turns an error into a warning for this null
            // value, but it is completely unnecessary otherwise
            return Iterables.getFirst((Iterable<? extends ISegment>) iterable, null);
        }

        /**
         * Get the number of segments
         *
         * TODO: Try to live without this method, this is not lazy enough
         *
         * @return The number of segment in the current iterable
         */
        public long getSegmentCount() {
            return Iterables.size(getIterable());
        }
    }

    /**
     * Table viewer of the latency table viewer
     */
    private @Nullable TableViewer fTableViewer = null;

    /**
     * Segment comparator
     */
    private @Nullable Comparator<ISegment> fComparator = null;
    private @Nullable SegmentStoreWithRange<?> fStore;

    @Override
    public void updateElement(int index) {
        final TableViewer tableViewer = fTableViewer;
        SegmentStoreWithRange<?> store = fStore;
        if (tableViewer == null || store == null) {
            return;
        }
        tableViewer.replace(store.getElement(index), index);
    }

    @Override
    public void dispose() {
        fStore = null;
        fTableViewer = null;
        fComparator = null;
    }

    @Override
    public void inputChanged(@Nullable Viewer viewer, @Nullable Object oldInput, @Nullable Object newInput) {
        fTableViewer = (TableViewer) viewer;
        if (newInput instanceof SegmentStoreWithRange) {
            SegmentStoreWithRange<?> sswr = (SegmentStoreWithRange<?>) newInput;
            Comparator<ISegment> comparator = fComparator;
            if (comparator != null) {
                sswr.setComparator(comparator);
            }
            fStore = sswr;
        } else if (newInput instanceof ISegmentStore<?>) {
            ISegmentStore<? extends ISegment> segmentStore = (ISegmentStore<?>) newInput;
            SegmentStoreWithRange<?> sswr = new SegmentStoreWithRange<>(segmentStore, TmfTimeRange.ETERNITY);
            Comparator<ISegment> comparator = fComparator;

            if (comparator != null) {
                sswr.setComparator(comparator);
            }
            fStore = sswr;
        } else {
            fStore = null;
        }
    }

    @Override
    public void setSortOrder(@Nullable Comparator<?> comparator) {
        SegmentStoreWithRange<?> store = fStore;
        final TableViewer tableViewer = fTableViewer;
        if (comparator == null || store == null || tableViewer == null) {
            return;
        }
        @SuppressWarnings("unchecked")
        Comparator<ISegment> comp = (Comparator<ISegment>) comparator;
        fComparator = comp;
        store.setComparator(comp);
        tableViewer.refresh();
    }

    /**
     * Get the segment count
     *
     * @return the segment count
     */
    public long getSegmentCount() {
        SegmentStoreWithRange<?> store = fStore;
        return (store == null ? 0 : store.getSegmentCount());
    }
}
