/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.segmentstore.core;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Comparator;

import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.Ordering;

/**
 * Basic implementation of {@link ISegment}.
 *
 * @author Alexandre Montplaisir
 */
public class BasicSegment implements ISegment {

    private static final long serialVersionUID = -3257452887960883177L;

    private static final Comparator<ISegment> COMPARATOR = checkNotNull(Ordering
            .from(SegmentComparators.INTERVAL_START_COMPARATOR)
            .compound(SegmentComparators.INTERVAL_END_COMPARATOR));

    private final long fStart;
    private final long fEnd;

    /**
     * Create a new segment.
     *
     * The end position should be equal to or greater than the start position.
     *
     * @param start
     *            Start position of the segment
     * @param end
     *            End position of the segment
     */
    public BasicSegment(long start, long end) {
        if (end < start) {
            throw new IllegalArgumentException();
        }
        fStart = start;
        fEnd = end;
    }

    @Override
    public long getStart() {
        return fStart;
    }

    @Override
    public long getEnd() {
        return fEnd;
    }

    @Override
    public int compareTo(@Nullable ISegment o) {
        if (o == null) {
            throw new IllegalArgumentException();
        }
        return COMPARATOR.compare(this, o);
    }

    @Override
    public String toString() {
        return new String('[' + String.valueOf(fStart) + ", " + String.valueOf(fEnd) + ']'); //$NON-NLS-1$
    }
}
