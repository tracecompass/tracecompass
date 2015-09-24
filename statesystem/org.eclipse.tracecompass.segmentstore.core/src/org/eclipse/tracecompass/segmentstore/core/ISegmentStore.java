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

package org.eclipse.tracecompass.segmentstore.core;

import java.util.Collection;

/**
 * Interface for segment-storing backends.
 *
 * @param <E>
 *            The type of {@link ISegment} element that will be stored in this
 *            database.
 *
 * @author Alexandre Montplaisir
 */
public interface ISegmentStore<E extends ISegment> extends Collection<E> {

    /**
     * Retrieve all elements that inclusively cross the given position.
     *
     * @param position
     *            The target position. This would represent a timestamp, if the
     *            tree's X axis represents time.
     * @return The intervals that cross this position
     */
    Iterable<E> getIntersectingElements(long position);

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
     * Dispose the data structure and release any system resources associated
     * with it.
     */
    void dispose();
}
