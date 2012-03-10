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
 * <b><u>HistogramZoom</u></b>
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

    public HistogramZoom(Histogram histogram, Canvas canvas, long start, long end) {
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

    public synchronized long getStartTime() {
        return fRangeStartTime;
    }

    public synchronized long getEndTime() {
        return fRangeStartTime + fRangeDuration;
    }

    public synchronized long getDuration() {
        return fRangeDuration;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    public synchronized void stop() {
        if (fScrollCounter != null) {
            fScrollCounter.interrupt();
            fScrollCounter = null;
        }
    }

    public synchronized void setFullRange(long startTime, long endTime) {
        fAbsoluteStartTime = startTime;
        fAbsoluteEndTime = endTime;
    }

    public synchronized void setNewRange(long startTime, long duration) {
        if (startTime < fAbsoluteStartTime)
            startTime = fAbsoluteStartTime;

        long endTime = startTime + duration;
        if (endTime > fAbsoluteEndTime) {
            endTime = fAbsoluteEndTime;
            if (endTime - duration > fAbsoluteStartTime)
                startTime = endTime - duration;
            else {
                startTime = fAbsoluteStartTime;
            }
        }

        fRangeStartTime = startTime;
        fRangeDuration = endTime - startTime;
    }

    // ------------------------------------------------------------------------
    // MouseWheelListener
    // ------------------------------------------------------------------------

    private long fMouseTimestamp = 0;

    @Override
    public synchronized void mouseScrolled(MouseEvent event) {
        if (fScrollCounter == null) {
            fScrollCounter = new MouseScrollCounter(this);
            fScrollCounter.start();
            fMouseTimestamp = fHistogram.getTimestamp(event.x);
        }
        fScrollCounter.incrementMouseScroll(event.count);
    }

    private synchronized void zoom(int nbClicks) {
        // The job is finished
        fScrollCounter = null;

        // Compute the new time range
        long requestedRange = (nbClicks > 0) ? Math.round(ZOOM_FACTOR * fRangeDuration) : (long) Math.ceil(fRangeDuration * (1.0 / ZOOM_FACTOR));

        // Perform a proportional zoom wrt the mouse position
        double ratio = ((double) (fMouseTimestamp - fRangeStartTime)) / fRangeDuration;
        long requestedStart = validateStart(fRangeStartTime + (long) ((fRangeDuration - requestedRange) * ratio));
        long requestedEnd = validateEnd(requestedStart, requestedStart + requestedRange);
        requestedStart = validateStart(requestedEnd - requestedRange);

        fHistogram.updateTimeRange(requestedStart, requestedEnd);
    }

    private long validateStart(long start) {
        if (start < fAbsoluteStartTime)
            start = fAbsoluteStartTime;
        if (start > fAbsoluteEndTime)
            start = fAbsoluteEndTime - fMinWindowSize;
        return start;
    }

    private long validateEnd(long start, long end) {
        if (end > fAbsoluteEndTime)
            end = fAbsoluteEndTime;
        if (end < start + fMinWindowSize)
            end = start + fMinWindowSize;
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

        public MouseScrollCounter(HistogramZoom zoom) {
            fZoom = zoom;
            fLastPoolTime = System.currentTimeMillis();
        }

        // --------------------------------------------------------------------
        // Operation
        // --------------------------------------------------------------------

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
            if (!isInterrupted())
                fZoom.zoom(nbScrollClick);
        }
    }

}
