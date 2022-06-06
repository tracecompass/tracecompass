/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc., Alexandre Montplaisir
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

package org.eclipse.tracecompass.segmentstore.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.Lists;

/**
 * Interface for segment-storing backends.
 *
 * Common contract (what should not be implemented) for a segment store.
 * <ol>
 * <li>no remove</li>
 * <li>no removeAll</li>
 * <li>no retainall</li>
 * </ol>
 *
 * @param <E>
 *            The type of {@link ISegment} element that will be stored in this
 *            database.
 *
 * @author Alexandre Montplaisir
 */
public interface ISegmentStore<E extends ISegment> extends Collection<E> {

    /**
     * Sorted Iterator
     *
     * @param order
     *            The desired order for the returned iterator
     * @return An iterator over all the segments in the store in the desired
     *         order
     * @since 1.1
     */
    default Iterable<E> iterator(Comparator<ISegment> order) {
        return getIntersectingElements(0, Long.MAX_VALUE, order);
    }

    /**
     * Retrieve all elements that inclusively cross the given position.
     *
     * @param position
     *            The target position. This would represent a timestamp, if the
     *            tree's X axis represents time.
     * @return The intervals that cross this position
     */
    default Iterable<E> getIntersectingElements(long position) {
        return getIntersectingElements(position, position);
    }

    /**
     * Retrieve all elements that inclusively cross the given position, sorted
     * in the specified order.
     *
     * @param position
     *            The target position. This would represent a timestamp, if the
     *            tree's X axis represents time.
     * @param order
     *            The desired order for the returned iterator
     * @return The intervals that cross this position
     * @since 1.1
     */
    default Iterable<E> getIntersectingElements(long position, Comparator<ISegment> order) {
        return getIntersectingElements(position, position, order);
    }

    /**
     * Retrieve all elements that inclusively cross another segment. We define
     * this target segment by its start and end positions.
     *
     * This effectively means, all elements that respect *both* conditions:
     *
     * <ul>
     * <li>Their end is after the 'start' parameter</li>
     * <li>Their start is before the 'end' parameter</li>
     * </ul>
     *
     * @param start
     *            The target start position
     * @param end
     *            The target end position
     * @return The elements overlapping with this segment
     */
    Iterable<E> getIntersectingElements(long start, long end);

    /**
     * Retrieve all elements that inclusively cross another segment, sorted in
     * the specified order. We define this target segment by its start and end
     * positions.
     *
     * @param start
     *            The target start position
     * @param end
     *            The target end position
     * @param order
     *            The desired order for the returned iterator
     * @return The intervals that cross this position
     * @since 1.1
     */
    default Iterable<E> getIntersectingElements(long start, long end, Comparator<ISegment> order) {
        Iterable<E> ret = getIntersectingElements(start, end);
        List<E> list;
        if (ret instanceof ArrayList<?>) {
            /*
             * No point in copying the intersecting elements into a new
             * ArrayList if they are already in a new ArrayList.
             */
            list = (List<E>) ret;
        } else {
            list = Lists.newArrayList(ret);
        }
        list.sort(order);
        return list;
    }

    /**
     * Retrieve all elements that inclusively cross another segment, sorted in
     * the specified order. We define this target segment by a predicate that
     * tests a given segment
     *
     * @param start the target start position
     *
     * @param end the target end position
     *
     * @param order The desired order for the returned iterator
     *
     * param filter The predicate that defines the first target segment
     *
     * @return The segments that follows a specific segment inclusively
     *
     * @since 3.0
     */
    default List<E> getIntersectingElements(long start, long end, Comparator<ISegment> order, Predicate<ISegment> filter) {
        Iterable<E> segments = getIntersectingElements(start, end, order);
        List ret;
        long i = 0;
        for (E segment : segments) {
            if (filter.test(segment)) {
                break;
            }
            i++;
        }
        if (segments instanceof ArrayList<?>) {
            ret = ((ArrayList) segments).subList((int) i, ((ArrayList) segments).size());
            ret.stream().filter(filter);
        } else {
            List tmp = Lists.newArrayList(segments);
            ret = tmp.subList((int) i, tmp.size());
        }
        return ret;
    }

    /**
     * Dispose the data structure and release any system resources associated
     * with it.
     */
    void dispose();

    /**
     * Method to close off the segment store. This happens for example when we
     * are done reading an off-line trace. Implementers can use this method to
     * save the segment store on disk
     *
     * @param deleteFiles
     *            Whether to delete any file that was created while building the
     *            segment store
     */
    default void close(boolean deleteFiles) {

    }

    @Override
    default boolean remove(@Nullable Object o) {
        throw new UnsupportedOperationException("Segment stores does not support \"remove\""); //$NON-NLS-1$
    }

    @Override
    default boolean removeAll(@Nullable Collection<?> c) {
        throw new UnsupportedOperationException("Segment stores does not support \"removeAll\""); //$NON-NLS-1$
    }

    @Override
    default boolean retainAll(@Nullable Collection<?> c) {
        throw new UnsupportedOperationException("Segment stores does not support \"retainAll\""); //$NON-NLS-1$
    }
}
