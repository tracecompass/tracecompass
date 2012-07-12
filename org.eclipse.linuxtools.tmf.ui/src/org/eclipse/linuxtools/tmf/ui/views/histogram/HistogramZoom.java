/*******************************************************************************
 * Copyright (c) 2011, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Moved from LTTng to TMF
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.histogram;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.widgets.Canvas;

/**
 * Class to handle zooming within histogram windows..
 *
 * @version 1.0
 * @author Francois Chouinard
 * <p>
 */
public class HistogramZoom implements MouseWheelListener {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private final static double ZOOM_FACTOR = 0.8;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final Histogram fHistogram;
    private final Canvas fCanvas;

    private long fAbsoluteStartTime;
    private long fAbsoluteEndTime;
    private final long fMinWindowSize;

    private long fRangeStartTime;
    private long fRangeDuration;

    private MouseScrollCounter fScrollCounter;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Standard constructor.
     *
     * @param histogram
     *            The parent histogram object
     * @param canvas
     *            The canvas
     * @param start
     *            The start time of the zoom area
     * @param end
     *            The end time of the zoom area
     */
    public HistogramZoom(Histogram histogram, Canvas canvas, long start,
            long end) {
        fHistogram = histogram;
        fCanvas = canvas;
        fAbsoluteStartTime = start;
        fAbsoluteEndTime = end;
        fMinWindowSize = fCanvas.getBounds().x;

        fRangeStartTime = fAbsoluteStartTime;
        fRangeDuration = fAbsoluteStartTime + fMinWindowSize;

        canvas.addMouseWheelListener(this);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Get start time of the zoom window.
     * @return the start time.
     */
    public synchronized long getStartTime() {
        return fRangeStartTime;
    }

    /**
     * Get the end time of the zoom window.
     * @return the end time
     */
    public synchronized long getEndTime() {
        return fRangeStartTime + fRangeDuration;
    }

    /**
     * Get the duration of the zoom window.
     * @return the duration of the zoom window.
     */
    public synchronized long getDuration() {
        return fRangeDuration;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Stops the zooming (multiple consecutive execution)
     */
    public synchronized void stop() {
        if (fScrollCounter != null) {
            fScrollCounter.interrupt();
            fScrollCounter = null;
        }
    }

    /**
     * The the full time range of the histogram
     *
     * @param startTime the start time the histogram
     * @param endTime the end time of the histogram
     */
    public synchronized void setFullRange(long startTime, long endTime) {
        fAbsoluteStartTime = startTime;
        fAbsoluteEndTime = endTime;
    }

    /**
     * Sets the new zoom window
     * @param startTime the start time
     * @param duration the duration
     */
    public synchronized void setNewRange(long startTime, long duration) {
        if (startTime < fAbsoluteStartTime) {
            startTime = fAbsoluteStartTime;
        }

        long endTime = startTime + duration;
        if (endTime > fAbsoluteEndTime) {
            endTime = fAbsoluteEndTime;
            if (endTime - duration > fAbsoluteStartTime) {
                startTime = endTime - duration;
            } else {
                startTime = fAbsoluteStartTime;
            }
        }

        fRangeStartTime = startTime;
        fRangeDuration = endTime - startTime;
    }

    // ------------------------------------------------------------------------
    // MouseWheelListener
    // ------------------------------------------------------------------------

    @Override
    public synchronized void mouseScrolled(MouseEvent event) {
        if (fScrollCounter == null) {
            fScrollCounter = new MouseScrollCounter(this);
            fScrollCounter.start();
        }
        fScrollCounter.incrementMouseScroll(event.count);
    }

    private synchronized void zoom(int nbClicks) {
        // The job is finished
        fScrollCounter = null;

        // Compute the new time range
        long requestedRange = (nbClicks > 0) ? Math.round(ZOOM_FACTOR * fRangeDuration) : (long) Math.ceil(fRangeDuration * (1.0 / ZOOM_FACTOR));

        // Distribute delta and adjust for boundaries
        long requestedStart = validateStart(fRangeStartTime + (long) ((fRangeDuration - requestedRange) / 2));
        long requestedEnd = validateEnd(requestedStart, requestedStart + requestedRange);
        requestedStart = validateStart(requestedEnd - requestedRange);

        fHistogram.updateTimeRange(requestedStart, requestedEnd);
    }

    private long validateStart(long start) {
        if (start < fAbsoluteStartTime) {
            start = fAbsoluteStartTime;
        }
        if (start > fAbsoluteEndTime) {
            start = fAbsoluteEndTime - fMinWindowSize;
        }
        return start;
    }

    private long validateEnd(long start, long end) {
        if (end > fAbsoluteEndTime) {
            end = fAbsoluteEndTime;
        }
        if (end < start + fMinWindowSize) {
            end = start + fMinWindowSize;
        }
        return end;
    }

    // ------------------------------------------------------------------------
    // DelayedMouseScroll
    // ------------------------------------------------------------------------

    private static class MouseScrollCounter extends Thread {

        // --------------------------------------------------------------------
        // Constants
        // --------------------------------------------------------------------

        private final static long QUIET_TIME = 100L;
        private final static long POLLING_INTERVAL = 10L;

        // --------------------------------------------------------------------
        // Attributes
        // --------------------------------------------------------------------

        private HistogramZoom fZoom = null;

        private long fLastPoolTime = 0L;
        private int nbScrollClick = 0;

        // --------------------------------------------------------------------
        // Constructors
        // --------------------------------------------------------------------

        /**
         * Constructor of inner class to handle consecutive scrolls of mouse wheel.
         * @param zoom the histogram zoom reference
         */
        public MouseScrollCounter(HistogramZoom zoom) {
            fZoom = zoom;
            fLastPoolTime = System.currentTimeMillis();
        }

        // --------------------------------------------------------------------
        // Operation
        // --------------------------------------------------------------------

        /**
         * Increments the number of scroll clicks.
         * @param nbScrolls the number to add to the current value
         */
        public void incrementMouseScroll(int nbScrolls) {
            fLastPoolTime = System.currentTimeMillis();
            nbScrollClick += nbScrolls;
        }

        // --------------------------------------------------------------------
        // Thread
        // --------------------------------------------------------------------

        @Override
        public void run() {
            while ((System.currentTimeMillis() - fLastPoolTime) < QUIET_TIME) {
                try {
                    Thread.sleep(POLLING_INTERVAL);
                } catch (Exception e) {
                    return;
                }
            }
            // Done waiting. Notify the histogram.
            if (!isInterrupted()) {
                fZoom.zoom(nbScrollClick);
            }
        }
    }

}
