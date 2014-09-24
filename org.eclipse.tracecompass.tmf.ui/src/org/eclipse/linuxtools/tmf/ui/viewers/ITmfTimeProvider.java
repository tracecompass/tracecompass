/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation in ITmfChartTimeProvider
 *   Geneviève Bastien - Moved methods from ITmfChartTimeProvider to this interface
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers;

/**
 * Interface for providing and updating time information. This is typically
 * implemented by a viewer that is displaying trace data over time.
 *
 * @author Bernd Hufmann
 * @author Geneviève Bastien
 * @since 3.0
 */
public interface ITmfTimeProvider extends ITmfViewer {

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
     * Method to notify about a change of the current selected time.
     *
     * @param currentBeginTime
     *            The current selection begin time
     * @param currentEndTime
     *            The current selection end time
     */
    void updateSelectionRange(long currentBeginTime, long currentEndTime);

    /**
     * Updates the current time range window.
     *
     * @param windowStartTime
     *            The window start time
     * @param windowEndTime
     *            The window end time.
     */
    void updateWindow(long windowStartTime, long windowEndTime);

}
