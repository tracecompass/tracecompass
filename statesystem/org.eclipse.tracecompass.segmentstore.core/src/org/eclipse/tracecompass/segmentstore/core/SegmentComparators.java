/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.segmentstore.core;

import java.util.Comparator;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Segments comparators. These do not allow for null arguments.
 *
 * @author Alexandre Montplaisir
 * @noimplement This interface only contains static definitions.
 */
public interface SegmentComparators {

    /**
     * Basic long comparator
     */
    Comparator<Long> LONG_COMPARATOR = new Comparator<Long>() {
        @Override
        public int compare(@Nullable Long o1, @Nullable Long o2) {
            if (o1 == null || o2 == null) {
                throw new IllegalArgumentException();
            }
            return o1.compareTo(o2);
        }
    };

    /**
     * Start time comparator
     */
    Comparator<ISegment> INTERVAL_START_COMPARATOR = new Comparator<ISegment>() {
        @Override
        public int compare(@Nullable ISegment o1, @Nullable ISegment o2) {
            if (o1 == null || o2 == null) {
                throw new IllegalArgumentException();
            }
            return Long.compare(o1.getStart(), o2.getStart());
        }
    };

    /**
     * End time comparator
     */
    Comparator<ISegment> INTERVAL_END_COMPARATOR = new Comparator<ISegment>() {
        @Override
        public int compare(@Nullable ISegment o1, @Nullable ISegment o2) {
            if (o1 == null || o2 == null) {
                throw new IllegalArgumentException();
            }
            return Long.compare(o1.getEnd(), o2.getEnd());
        }
    };

    /**
     * Length comparator
     */
    Comparator<ISegment> INTERVAL_LENGTH_COMPARATOR = new Comparator<ISegment>() {
        @Override
        public int compare(@Nullable ISegment o1, @Nullable ISegment o2) {
            if (o1 == null || o2 == null) {
                throw new IllegalArgumentException();
            }
            return Long.compare(o1.getLength(), o2.getLength());
        }
    };

}
