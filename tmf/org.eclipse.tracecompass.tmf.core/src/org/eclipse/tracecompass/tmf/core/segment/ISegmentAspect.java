/*******************************************************************************
 * Copyright (c) 2015-2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.segment;

import java.util.Comparator;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.segmentstore.core.ISegment;

/**
 * An aspect is a piece of information that can be extracted, directly or
 * indirectly, from a segment {@link ISegment}.
 *
 * The aspect can then be used to populate table columns, to filter
 * on to only keep certain segments, to plot XY charts, etc.
 *
 * Inspired by ITmfEventAspect implementation.
 *
 * @author Bernd Hufmann
 * @since 2.0
 */
public interface ISegmentAspect {

    /**
     * Static definition of an empty string.
     */
    String EMPTY_STRING = ""; //$NON-NLS-1$

    /**
     * @since 5.2
     */
    enum SegmentType {
            CATEGORICAL,
            CONTINUOUS,
    }

    /**
     * Get the name of this aspect. This name will be user-visible and, as such,
     * should be localized.
     *
     * @return The name of this aspect.
     */
    String getName();

    /**
     * Return a descriptive help text of what this aspect does. This could then
     * be shown in tooltip or in option dialogs for instance. It should also be
     * localized.
     *
     * @return The help text of this aspect
     */
    String getHelpText();

    /**
     * Gets the comparator to be used when comparing to segments.
     * @return the comparator to be used when comparing to segments
     */
    @Nullable Comparator<?> getComparator();

    /**
     * The "functor" representing this aspect. Basically, what to do for an
     * segment that is passed in parameter.
     *
     * Users also can (and should) provide a more specific return type than
     * Object.
     *
     * @param segment
     *            The segment to process
     * @return The resulting information for this segment.
     */
    @Nullable Object resolve(ISegment segment);

    /** Gets the type of the segment aspect, Categorical or Continuous
     *
     * @return the type of the segment aspect
     * @since 5.2
     */
    default SegmentType getType() {
        return SegmentType.CATEGORICAL;
    }
}
