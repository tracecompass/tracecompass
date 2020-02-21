/*******************************************************************************
 * Copyright (c) 2015-2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
     * <p>
     * An aspect resolves a segment to data using {@link ISegmentAspect#resolve(ISegment)}. The data can be
     * {@link #CATEGORICAL} or {@link #CONTINUOUS}.
     * </p>
     * <p>
     * Not all data resolved are numbers, if they are not, they are guaranteed
     * to not be {@link #CONTINUOUS}.
     * </p>
     *
     * @since 5.2
     */
    enum SegmentType {
        /**
         * <p>
         * A <strong>categorical</strong> aspect is one that will resolve
         * characteristics that do not have mathematical meaning. An example
         * would be return codes as it would make no sense to add them up
         * however, testing equality is still valid as with all resolved data.
         * </p>
         */
        CATEGORICAL,

        /**
         * <p>
         * A <strong>continuous aspect</strong> is one that resolves data to
         * measurements. Their possible values should not be counted and should
         * be described using intervals on a real number line. Examples would be
         * the start time, end time and duration. Testing equality and comparing
         * results makes sense here, as well as performing mathematical
         * operations.
         * </p>
         * <p>
         * (e.g. the difference between start times can be measured to determine
         * the period of a regular segment source)
         * </p>
         */
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

    /**
     * Gets the {@link SegmentType} of the segment aspect, Categorical or Continuous
     *
     * @return the type of the segment aspect
     * @since 5.2
     */
    default SegmentType getType() {
        return SegmentType.CATEGORICAL;
    }
}
