/*****************************************************************************
 * Copyright (c) 2007, 2016 Intel Corporation, Ericsson
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

import org.eclipse.tracecompass.tmf.ui.views.ITimeReset;
import org.eclipse.tracecompass.tmf.ui.views.FormatTimeUtils;
import org.eclipse.tracecompass.tmf.ui.views.FormatTimeUtils.TimeFormat;

/**
 * Time data provider interface, for use in the timegraph widget.
 *
 * @author Alvaro Sanchez-Leon
 * @author Patrick Tasse
 * @author Xavier Raynaud
 */
public interface ITimeDataProvider extends ITimeReset {

    /**
     * Updates the selection begin and end time and notifies the selection
     * listeners about the new selection range (if it has changed).
     * <p>
     * If ensureVisible is true, the window range will be centered either on the
     * selection begin time (if it has changed) or otherwise on the selection
     * end time, if and only if that time is outside of the current window. If
     * the window range is modified, the range listeners will be notified.
     *
     * @param beginTime
     *            the selection begin time
     * @param endTime
     *            the selection end time
     * @param ensureVisible
     *            if true, ensure visibility of the new selection range boundary
     * @since 2.0
     */
    void setSelectionRangeNotify(long beginTime, long endTime, boolean ensureVisible);

    /**
     * Updates the selection begin and end time.
     * <p>
     * If ensureVisible is true, the window range will be centered either on the
     * selection begin time (if it has changed) or otherwise on the selection
     * end time, if and only if that time is outside of the current window. If
     * the window range is modified, the range listeners will be notified.
     *
     * @param beginTime
     *            the selection begin time
     * @param endTime
     *            the selection end time
     * @param ensureVisible
     *            if true, ensure visibility of the new selection range boundary
     * @since 2.0
     */
    void setSelectionRange(long beginTime, long endTime, boolean ensureVisible);

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
     * Updates the window range and notify the range listeners.
     *
     * @param time0
     *            the window start time
     * @param time1
     *            the window end time
     */
    void setStartFinishTimeNotify(long time0, long time1);

    /**
     * Update the window range but do not notify the range listeners.
     *
     * @param time0
     *            the window start time
     * @param time1
     *            the window end time
     */
    void setStartFinishTime(long time0, long time1);

    /**
     * Notify the range listeners without updating the time range.
     */
    void notifyStartFinishTime();

    /**
     * Updates the selection time and notifies the selection listeners about the
     * new selection time (if it has changed).
     * <p>
     * If ensureVisible is true, the window range will be centered on the
     * selection time, if and only if that time is outside of the current
     * window. If the window range is modified, the range listeners will be
     * notified.
     *
     * @param time
     *            the selection time
     * @param ensureVisible
     *            if true, ensure visibility of the new selection time
     */
    void setSelectedTimeNotify(long time, boolean ensureVisible);

    /**
     * Updates the selection time.
     * <p>
     * If ensureVisible is true, the window range will be centered on the
     * selection time, if and only if that time is outside of the current
     * window. If the window range is modified, the range listeners will be
     * notified.
     *
     * @param time
     *            the selection time
     * @param ensureVisible
     *            if true, ensure visibility of the new selection time
     */
    void setSelectedTime(long time, boolean ensureVisible);

    /**
     * Reset the start and end times.
     *
     * @param notify
     *            if true, notify the registered listeners
     * @since 2.0
     */
    @Override
    default void resetStartFinishTime(boolean notify) {
        if (notify) {
            setStartFinishTimeNotify(getMinTime(), getMaxTime());
        } else {
            setStartFinishTime(getMinTime(), getMaxTime());
        }
    }

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
     *   <li>{@link TimeFormat#CYCLES}   cycles, displayed as long values.
     * </ul>
     * @since 3.3
     */
    default TimeFormat getTimeFormat2() {
        return FormatTimeUtils.TimeFormat.values()[getTimeFormat().ordinal()];
    }

    /**
     * @return the time format, one of:
     * <ul>
     *   <li>{@link TimeFormat#CALENDAR} absolute time, displayed as year/month/day/hours/minutes/seconds/ms/us/ns
     *   <li>{@link TimeFormat#RELATIVE} relative time, displayed as seconds/ms/us/ns
     *   <li>{@link TimeFormat#NUMBER}   number, displayed as long values.
     *   <li>{@link TimeFormat#CYCLES}   cycles, displayed as long values.
     * </ul>
     * @deprecated As of 3.3 use {@link #getTimeFormat2()}
     */
    @Deprecated
    Utils.TimeFormat getTimeFormat();
}
