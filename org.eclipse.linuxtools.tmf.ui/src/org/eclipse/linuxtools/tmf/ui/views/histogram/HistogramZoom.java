/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Moved from LTTng to TMF
 *   Patrick Tasse - Update for mouse wheel zoom
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.histogram;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;

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

    private long fAbsoluteStartTime;
    private long fAbsoluteEndTime;
    private final long fMinWindowSize;

    private long fRangeStartTime;
    private long fRangeDuration;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Standard constructor.
     *
     * @param histogram
     *            The parent histogram object
     * @param start
     *            The start time of the zoom area
     * @param end
     *            The end time of the zoom area
     * @since 2.0
     */
    public HistogramZoom(Histogram histogram, long start, long end) {
        fHistogram = histogram;
        fAbsoluteStartTime = start;
        fAbsoluteEndTime = end;
        fMinWindowSize = 0;

        fRangeStartTime = fAbsoluteStartTime;
        fRangeDuration = fAbsoluteStartTime + fMinWindowSize;
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
        long realStart = startTime;

        if (realStart < fAbsoluteStartTime) {
            realStart = fAbsoluteStartTime;
        }

        long endTime = realStart + duration;
        if (endTime > fAbsoluteEndTime) {
            endTime = fAbsoluteEndTime;
            if (endTime - duration > fAbsoluteStartTime) {
                realStart = endTime - duration;
            } else {
                realStart = fAbsoluteStartTime;
            }
        }

        fRangeStartTime = realStart;
        fRangeDuration = endTime - realStart;
    }

    // ------------------------------------------------------------------------
    // MouseWheelListener
    // ------------------------------------------------------------------------

    @Override
    public synchronized void mouseScrolled(MouseEvent event) {
        zoom(event.count);
    }

    private synchronized void zoom(int nbClicks) {
        // Compute the new time range
        long requestedRange = (nbClicks > 0) ? Math.round(ZOOM_FACTOR * fRangeDuration) : (long) Math.ceil(fRangeDuration * (1.0 / ZOOM_FACTOR));

        // Distribute delta and adjust for boundaries
        long requestedStart = validateStart(fRangeStartTime + (fRangeDuration - requestedRange) / 2);
        long requestedEnd = validateEnd(requestedStart, requestedStart + requestedRange);
        requestedStart = validateStart(requestedEnd - requestedRange);

        fHistogram.updateTimeRange(requestedStart, requestedEnd);
    }

    private long validateStart(long start) {
        long realStart = start;

        if (realStart < fAbsoluteStartTime) {
            realStart = fAbsoluteStartTime;
        }
        if (realStart > fAbsoluteEndTime) {
            realStart = fAbsoluteEndTime - fMinWindowSize;
        }
        return realStart;
    }

    private long validateEnd(long start, long end) {
        long realEnd = end;

        if (realEnd > fAbsoluteEndTime) {
            realEnd = fAbsoluteEndTime;
        }
        if (realEnd < start + fMinWindowSize) {
            realEnd = start + fMinWindowSize;
        }
        return realEnd;
    }
}
