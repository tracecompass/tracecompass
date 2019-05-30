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
import java.util.Objects;

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
    Comparator<Long> LONG_COMPARATOR = Objects.requireNonNull(Long::compare);

    /**
     * Start time comparator
     */
    Comparator<ISegment> INTERVAL_START_COMPARATOR = Objects.requireNonNull(Comparator.comparingLong(ISegment::getStart));

    /**
     * End time comparator
     */
    Comparator<ISegment> INTERVAL_END_COMPARATOR = Objects.requireNonNull(Comparator.comparingLong(ISegment::getEnd));

    /**
     * Length comparator
     */
    Comparator<ISegment> INTERVAL_LENGTH_COMPARATOR = Objects.requireNonNull(Comparator.comparingLong(ISegment::getLength));

}
