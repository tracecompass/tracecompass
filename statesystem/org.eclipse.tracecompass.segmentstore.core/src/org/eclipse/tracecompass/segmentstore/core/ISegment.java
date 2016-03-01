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

import java.io.Serializable;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Generic interface for any segment (like a time range) that can be used in the
 * segment store.
 *
 * @author Alexandre Montplaisir
 */
public interface ISegment extends Serializable, Comparable<@NonNull ISegment> {

    /**
     * The start position/time of the segment.
     *
     * @return The start position
     */
    long getStart();

    /**
     * The end position/time of the segment
     *
     * @return The end position
     */
    long getEnd();

    @Override
    default int compareTo(@NonNull ISegment arg0) {
        return SegmentComparators.INTERVAL_START_COMPARATOR
                .thenComparing(SegmentComparators.INTERVAL_END_COMPARATOR)
                .compare(this, arg0);
    }

    /**
     * The length/duration of the segment.
     *
     * @return The duration
     */
    default long getLength() {
        return getEnd() - getStart();
    }
}
