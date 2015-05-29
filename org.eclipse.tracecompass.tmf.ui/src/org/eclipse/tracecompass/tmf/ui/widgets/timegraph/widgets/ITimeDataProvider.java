/*****************************************************************************
 * Copyright (c) 2007, 2015 Intel Corporation, Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Intel Corporation - Initial API and implementation
 *   Ruslan A. Scherbakov, Intel - Initial API and implementation
 *   Alvaro Sanchez-Leon - Updated for TMF
 *   Geneviève Bastien - Added methods to save a time range selection
 *   Patrick Tasse - Refactoring, support for range selection
 *****************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets;

import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.Utils.TimeFormat;

/**
 * Time data provider interface, for use in the timegraph widget.
 *
 * @author Alvaro Sanchez-Leon
 * @author Patrick Tasse
 * @author Xavier Raynaud
 */
public interface ITimeDataProvider {

    /**
     * Updates the selection begin and end time and notifies any registered
     * listeners about the new time range (if necessary)
     *
     * @param beginTime the selection begin time
     * @param endTime the selection end time
     */
    void setSelectionRangeNotify(long beginTime, long endTime);

    /**
     * Updates the selection begin and end time
     *
     * @param beginTime the selection begin time
     * @param endTime the selection end time
     */
    void setSelectionRange(long beginTime, long endTime);

    /**
     * @return The begin time of the current selection
     */
    long getSelectionBegin();

    /**
     * @return The end time of the current selection
     */
    long getSelectionEnd();

    /**
     * Get the user-specified bounds begin time. May be set to SWT.DEFAULT. For
     * the actual bound use {@link #getMinTime()}.
     *
     * @return The user-specified begin time, or SWT.DEFAULT if input bound used
     */
    long getBeginTime();

    /**
     * Get the user-specified bounds end time. May be set to SWT.DEFAULT. For
     * the actual bound use {@link #getMaxTime()}.
     *
     * @return The user-specified end time, or SWT.DEFAULT if input bound used
     */
    long getEndTime();

    /**
     * @return The bounds minimum time
     */
    long getMinTime();

    /**
     * @return The bounds maximum time
     */
    long getMaxTime();

    /**
     * @return The current window start time
     */
    long getTime0();

    /**
     * @return The current window end time
     */
    long getTime1();

    /**
     * @return The minimal time interval
     */
    long getMinTimeInterval();

    /**
     * Updates the time range and notify registered listeners
     *
     * @param time0
     * @param time1
     */
    void setStartFinishTimeNotify(long time0, long time1);

    /**
     * Update the time range but do not trigger event notification
     *
     * @param time0
     * @param time1
     */
    void setStartFinishTime(long time0, long time1);

    /**
     * Notify registered listeners without updating the time range
     */
    void notifyStartFinishTime();

    /**
     * Updates the selected time, adjusts the time range if necessary and
     * notifies any registered listeners about the new selected time and new
     * range (if necessary)
     *
     * @param time
     *            A Time to set
     * @param ensureVisible
     *            Ensure visibility of new time (will adjust time range if
     *            necessary)
     */
    void setSelectedTimeNotify(long time, boolean ensureVisible);

    /**
     * Updates the selected time and adjusts the time range if necessary without
     * notifying registered listeners.
     *
     * @param time
     *            A Time to set
     * @param ensureVisible
     *            Ensure visibility of new time (will adjust time range if
     *            necessary)
     */
    void setSelectedTime(long time, boolean ensureVisible);

    /**
     * Reset the start and end times and notify the registered listeners
     */
    void resetStartFinishTime();

    /**
     * @return The names' width
     */
    int getNameSpace();

    /**
     * Set the names' width
     *
     * @param width
     */
    void setNameSpace(int width);

    /**
     * @return The width for timestamps
     */
    int getTimeSpace();

    /**
     * @return the time format, one of:
     * <ul>
     *   <li>{@link TimeFormat#CALENDAR} absolute time, displayed as year/month/day/hours/minutes/seconds/ms/us/ns
     *   <li>{@link TimeFormat#RELATIVE} relative time, displayed as seconds/ms/us/ns
     *   <li>{@link TimeFormat#NUMBER}   number, displayed as long values.
     * </ul>
     */
    TimeFormat getTimeFormat();
}
