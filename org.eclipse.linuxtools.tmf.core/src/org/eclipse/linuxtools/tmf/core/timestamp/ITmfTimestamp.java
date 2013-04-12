/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.timestamp;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;

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
 * @version 2.0
 * @since 2.0
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
    public static final int SECOND_SCALE = 0;

    /**
     * The millisecond scale factor (10e-3)
     */
    public static final int MILLISECOND_SCALE = -3;

    /**
     * The microsecond scale factor (10e-6)
     */
    public static final int MICROSECOND_SCALE = -6;

    /**
     * The nanosecond scale factor (10e-9)
     */
    public static final int NANOSECOND_SCALE = -9;

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * @return the timestamp value (magnitude)
     */
    public long getValue();

    /**
     * @return the timestamp scale (exponent)
     */
    public int getScale();

    /**
     * @return the timestamp precision (measurement tolerance)
     */
    public int getPrecision();

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
    public ITmfTimestamp normalize(long offset, int scale);

    /**
     * Compares [this] and [ts] within timestamp precision
     *
     * @param ts the other timestamp
     * @param withinPrecision consider the precision when testing for equality
     * @return -1, 0 or 1 (less than, equals, greater than)
     */
    public int compareTo(ITmfTimestamp ts, boolean withinPrecision);

    /**
     * Returns the difference between [this] and [ts] as a timestamp
     *
     * @param ts the other timestamp
     * @return the time difference (this - other) as an ITmfTimestamp
     */
    public ITmfTimestamp getDelta(ITmfTimestamp ts);

    /**
     * Returns if this timestamp intersects the given time range. Borders are
     * inclusive (for more fine-grained behavior, you can use
     * {@link #compareTo(ITmfTimestamp)}.
     *
     * @param range
     *            The time range to compare to
     * @return True if this timestamp is part of the time range, false if not
     */
    public boolean intersects(TmfTimeRange range);

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
     * @since 2.0
     */
    public String toString(final TmfTimestampFormat format);

}
