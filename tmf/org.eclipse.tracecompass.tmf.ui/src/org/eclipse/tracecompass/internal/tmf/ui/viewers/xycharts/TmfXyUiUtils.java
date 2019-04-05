/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.viewers.xycharts;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.ITmfChartTimeProvider;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.Range;

/**
 * Utility class providing XY charts utility methods, for example for zooming
 * and scrolling. For scrolling, it will move the zoom window to another
 * position while maintaining the window size. It also notifies the viewer about
 * a change of range.
 *
 * @author Bernd Hufmann
 */
public class TmfXyUiUtils {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    private static final long MIN_WINDOW_SIZE = 1;
    private static final double ZOOM_FACTOR_AT_X_POSITION = 0.8;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    private TmfXyUiUtils() {
    }

    /**
     * Perform horizontal scrolling for a given viewer.
     *
     * @param viewer
     *            the chart time provider to use for scrolling
     * @param chart
     *            the SwtChart reference to use the scrolling
     * @param left
     *            true to scroll left else scroll right
     *
     */
    public static void horizontalScroll(ITmfChartTimeProvider viewer, Chart chart, boolean left) {
        IAxis xAxis = chart.getAxisSet().getXAxis(0);
        if (xAxis == null) {
            return;
        }
        long windowStartTime = viewer.getWindowStartTime();
        long windowsEndTime = viewer.getWindowEndTime();

        long startTime = viewer.getStartTime();
        long endTime = viewer.getEndTime();

        long range = windowsEndTime - windowStartTime;
        if (range <= 0) {
            return;
        }
        long increment = Math.max(1, range / 2);
        if (left) {
            windowStartTime = Math.max(windowStartTime - increment, startTime);
            windowsEndTime = windowStartTime + range;
        } else {
            windowsEndTime = Math.min(windowsEndTime + increment, endTime);
            windowStartTime = windowsEndTime - range;
        }
        viewer.updateWindow(windowStartTime, windowsEndTime);
        xAxis.setRange(new Range(windowStartTime - viewer.getTimeOffset(), windowsEndTime - viewer.getTimeOffset()));
    }

    /**
     * Provides horizontal zooming for a given viewer at given position.
     *
     * @param viewer
     *            the chart time provider to use for zooming
     * @param chart
     *            the SwtChart reference to use the zooming
     * @param zoomIn
     *            true to zoomIn else zoomOut
     * @param x
     *            x location to center the zoom
     */
    public static void zoom(ITmfChartTimeProvider viewer, Chart chart, boolean zoomIn, final int x) {
        IAxis xAxis = chart.getAxisSet().getXAxis(0);
        if (xAxis == null) {
            return;
        }
        // Compute the new time range
        long newDuration = viewer.getWindowDuration();
        if (newDuration == 0) {
            return;
        }
        double ratio = 1.0;
        if (zoomIn) {
            ratio = ZOOM_FACTOR_AT_X_POSITION;
            newDuration = Math.round(ZOOM_FACTOR_AT_X_POSITION * newDuration);
        } else {
            ratio = 1.0 / ZOOM_FACTOR_AT_X_POSITION;
            newDuration = (long) Math.ceil(newDuration * ratio);
        }
        newDuration = Math.max(MIN_WINDOW_SIZE, newDuration);

        // Center the zoom on mouse position, distribute new duration and adjust
        // for boundaries.
        long timeAtXPos = limitXDataCoordinate(viewer, xAxis.getDataCoordinate(x)) + viewer.getTimeOffset();
        // Note: ratio = newDuration/oldDuration
        long newWindowStartTime = timeAtXPos - Math.round(ratio * (timeAtXPos - viewer.getWindowStartTime()));
        long newWindowEndTime = validateWindowEndTime(viewer, newWindowStartTime, newWindowStartTime + newDuration);
        newWindowStartTime = validateWindowStartTime(viewer, newWindowStartTime);
        viewer.updateWindow(newWindowStartTime, newWindowEndTime);
        xAxis.setRange(new Range(newWindowStartTime - viewer.getTimeOffset(),
                newWindowEndTime - viewer.getTimeOffset()));
    }

    /**
     * Limits x data coordinate to window start and window end range for a given
     * viewer
     *
     * @param viewer
     *            the chart time provider to use
     * @param x
     *            x to limit
     * @return x if x >= begin && x <= end begin if x < begin end if x > end
     */
    public static long limitXDataCoordinate(@Nullable ITmfChartTimeProvider viewer, double x) {
        if (viewer != null) {
            long windowStartTime = viewer.getWindowStartTime() - viewer.getTimeOffset();
            long windowEndTime = viewer.getWindowEndTime() - viewer.getTimeOffset();

            if (x < windowStartTime) {
                return windowStartTime;
            }

            if (x > windowEndTime) {
                return windowEndTime;
            }
        }

        return (long) x;
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------
    private static long validateWindowStartTime(ITmfChartTimeProvider viewer, long start) {
        long realStart = start;

        long startTime = viewer.getStartTime();
        long endTime = viewer.getEndTime();

        if (realStart < startTime) {
            realStart = startTime;
        }
        if (realStart > endTime) {
            realStart = endTime;
        }
        return realStart;
    }

    private static long validateWindowEndTime(ITmfChartTimeProvider viewer, long start, long end) {
        long realEnd = end;
        long endTime = viewer.getEndTime();

        if (realEnd > endTime) {
            realEnd = endTime;
        }
        if (realEnd < start + MIN_WINDOW_SIZE) {
            realEnd = start + MIN_WINDOW_SIZE;
        }
        return realEnd;
    }
}
