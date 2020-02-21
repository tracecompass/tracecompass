/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Alexandre Montplaisir - Removed concept of precision
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.timestamp;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * The fundamental time reference in the TMF.
 * <p>
 * It defines a generic timestamp interface in its most basic form:
 * <ul>
 * <li>timestamp = [value] * 10**[scale] +/- [precision]
 * </ul>
 * Where:
 * <ul>
 * <li>[value] is an unstructured integer value
 * <li>[scale] is the magnitude of the value wrt some application-specific
 * base unit (e.g. the second)
 * <li>[precision] indicates the error on the value (useful for comparing
 * timestamps in different scales). Default: 0.
 * </ul>
 *
 * @author Francois Chouinard
 *
 * @see ITmfEvent
 * @see TmfTimeRange
 */
public interface ITmfTimestamp extends Comparable<ITmfTimestamp> {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * The millisecond scale factor (10e0)
     */
    int SECOND_SCALE = 0;

    /**
     * The millisecond scale factor (10e-3)
     */
    int MILLISECOND_SCALE = -3;

    /**
     * The microsecond scale factor (10e-6)
     */
    int MICROSECOND_SCALE = -6;

    /**
     * The nanosecond scale factor (10e-9)
     */
    int NANOSECOND_SCALE = -9;

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * @return the timestamp value (magnitude)
     */
    long getValue();

    /**
     * @return the timestamp scale (exponent)
     */
    int getScale();

    /**
     * Gets the timestamp converted to nanoseconds, if the timestamp is larger
     * than {@link Long#MAX_VALUE} or smaller than {@link Long#MIN_VALUE} it
     * will be clamped to those values.
     *
     * @return the timestamp converted to a long value of nanoseconds
     * @since 2.0
     */
    default long toNanos() {
        if (getScale() == NANOSECOND_SCALE) {
            return getValue();
        }
        return normalize(0L, NANOSECOND_SCALE).getValue();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Normalize (adjust scale and offset) of the timestamp
     *
     * @param offset the offset to apply to the timestamp value (after scaling)
     * @param scale the new timestamp scale
     * @return a new 'adjusted' ITmfTimestamp
     */
    @NonNull ITmfTimestamp normalize(long offset, int scale);

    /**
     * Returns the difference between [this] and [ts] as a timestamp
     *
     * @param ts the other timestamp
     * @return the time difference (this - other) as an ITmfTimestamp
     */
    @NonNull ITmfTimestamp getDelta(ITmfTimestamp ts);

    /**
     * Returns if this timestamp intersects the given time range. Borders are
     * inclusive (for more fine-grained behavior, you can use
     * {@link #compareTo(ITmfTimestamp)}.
     *
     * @param range
     *            The time range to compare to
     * @return True if this timestamp is part of the time range, false if not
     */
    boolean intersects(TmfTimeRange range);

    // ------------------------------------------------------------------------
    // Comparable
    // ------------------------------------------------------------------------

    @Override
    int compareTo(ITmfTimestamp ts);

    /**
     * Format the timestamp as per the format provided
     *
     * @param format the timestamp formatter
     * @return the formatted timestamp
     */
    String toString(final TmfTimestampFormat format);

}
