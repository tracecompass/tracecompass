/*******************************************************************************
 * Copyright (c) 2012, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API
 ******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.interval;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;

/**
 * This is the basic interface for accessing state intervals. See
 * StateInterval.java for a basic implementation.
 *
 * A StateInterval is meant to be immutable. All implementing (non-abstract)
 * classes should ideally be marked as 'final'.
 *
 * @author Alexandre Montplaisir
 */
public interface ITmfStateInterval {

    /**
     * Retrieve the start time of the interval
     *
     * @return the start time of the interval
     */
    long getStartTime();

    /**
     * Retrieve the end time of the interval
     *
     * @return the end time of the interval
     */
    long getEndTime();

    /**
     * Retrieve the quark of the attribute this state interval refers to
     *
     * @return the quark of the attribute this state interval refers to
     */
    int getAttribute();

    /**
     * Retrieve the state value represented by this interval
     *
     * @return the state value represented by this interval
     */
    @NonNull ITmfStateValue getStateValue();

    /**
     * Test if this interval intersects another timestamp, inclusively.
     *
     * @param timestamp
     *            The target timestamp
     * @return True if the interval and timestamp intersect, false if they don't
     */
    boolean intersects(long timestamp);

    /**
     * Retrieve this state interval's value
     *
     * @return the Object contained by this interval
     * @since 3.1
     */
    default @Nullable Object getValue() {
        return getStateValue().unboxValue();
    }

    /**
     * Retrieve this state interval's value as an int, or 0 if the value is
     * null.
     *
     * @return the int value contained by this interval
     * @throws ClassCastException
     *             if the value is not an int
     * @since 4.1
     */
    default int getValueInt() {
        Object value = getValue();
        if (value == null) {
            return 0;
        }
        return (int) value;
    }

    /**
     * Retrieve this state interval's value as a long, or 0L if the value is
     * null.
     *
     * @return the long value contained by this interval
     * @throws ClassCastException
     *             if the value is not a long
     * @since 4.1
     */
    default long getValueLong() {
        Object value = getValue();
        if (value == null) {
            return 0;
        }
        return (long) value;
    }

    /**
     * Retrieve this state interval's value as a double, or 0.0 if the value is
     * null.
     *
     * @return the double value contained by this interval
     * @throws ClassCastException
     *             if the value is not a double
     * @since 4.1
     */
    default double getValueDouble() {
        Object value = getValue();
        if (value == null) {
            return 0.0;
        }
        return (double) value;
    }

    /**
     * Retrieve this state interval's value as a String.
     *
     * @return the String value contained by this interval
     * @throws ClassCastException
     *             if the value is not a String
     * @since 4.1
     */
    default String getValueString() {
        Object value = getValue();
        return (String) value;
    }
}
