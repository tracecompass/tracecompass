/**********************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.viewers.xycharts;

import org.eclipse.linuxtools.tmf.ui.viewers.ITmfViewer;

/**
 * Interface for providing and updating time information. This is typically
 * implemented by a chart viewer that is displaying trace data over time where
 * the time is shown on the x-axis.
 *
 * @author Bernd Hufmann
 * @since 3.0
 */
public interface ITmfChartTimeProvider extends ITmfViewer {
    /**
     * Gets the start time of trace
     *
     * @return start time of trace
     */
    long getStartTime();

    /**
     * Gets the end time of trace
     *
     * @return End time of trace
     */
    long getEndTime();

    /**
     * Gets the start time of current time range displayed
     *
     * @return start time of current time range
     */
    long getWindowStartTime();

    /**
     * Gets the end time of current time range displayed
     *
     * @return End time of current time range
     */
    long getWindowEndTime();

    /**
     * Gets the duration of the current time range displayed
     *
     * @return duration of current time range
     */
    long getWindowDuration();

    /**
     * Gets the begin time of the selected range
     *
     * @return the begin time of the selected range
     */
    long getSelectionBeginTime();

    /**
     * Gets the end time of the selected range
     *
     * @return end time of the selected range
     */
    long getSelectionEndTime();

    /**
     * Returns a constant time offset that is used to normalize the time values
     * to a range of 0..53 bits to avoid loss of precision when converting
     * long <-> double.
     *
     * Time values are stored in TMF as long values. The SWT chart library
     * uses values of type double (on x and y axis). To avoid loss of
     * precision when converting long <-> double the values need to fit
     * within 53 bits.
     *
     * Subtract the offset when using time values provided externally for
     * internal usage in SWT chart. Add the offset when using time values
     * provided by SWT chart (e.g. for display purposes) and when broadcasting
     * them externally (e.g. time synchronization signals).
     *
     * For example the offset can be calculated as the time of the first
     * time value in the current time range to be displayed in the chart.
     * Add +1 to avoid 0 when using logarithmic scale.
     *
     * t0=10000, t2=20000, tn=N -> timeOffset=t0-1
     * -> t0'=1, t1'=10001, tn'=N-timeOffset
     *
     * where t0 ... tn are times used externally and t0' ... tn' are times
     * used internally by the SWT chart.
     *
     * @return the time offset
     */
    long getTimeOffset();

}
