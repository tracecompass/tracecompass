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
 *   Bernd Hufmann - Added setter and getter and bar width support
 *   Francois Chouinard - Moved from LTTng to TMF
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.histogram;

import java.util.Arrays;

/**
 * Convenience class/struct for scaled histogram data.
 *
 * @version 1.0
 * @author Francois Chouinard
 */
public class HistogramScaledData {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * Indicator value that bucket is out of range (not filled).
     */
    public static final int OUT_OF_RANGE_BUCKET = -1;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * Width of histogram canvas (number of pixels).
     */
    public int fWidth;
    /**
     * Height of histogram canvas (number of pixels).
     */
    public int fHeight;
    /**
     * Width of one histogram bar (number of pixels).
     */
    public int fBarWidth;
    /**
     * Array of scaled values
     */
    public int[] fData;
    /**
     * The bucket duration of a scaled data bucket.
     */
    public long fBucketDuration;
    /**
     * The maximum number of events of all buckets.
     */
    public long fMaxValue;
    /**
     * The index of the current bucket.
     */
    public int fCurrentBucket;
    /**
     * The index of the last bucket.
     */
    public int fLastBucket;
    /**
     * The scaling factor used to fill the scaled data.
     */
    public double fScalingFactor;
    /**
     * Time of first bucket.
     */
    public long fFirstBucketTime;
    /**
     * The time of the first event.
     */
    public long fFirstEventTime;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     * @param width the canvas width
     * @param height the canvas height
     * @param barWidth the required bar width
     */
    public HistogramScaledData(int width, int height, int barWidth) {
        fWidth = width;
        fHeight = height;
        fBarWidth = barWidth;
        fData = new int[width/fBarWidth];
        Arrays.fill(fData, 0);
        fBucketDuration = 1;
        fMaxValue = 0;
        fCurrentBucket = 0;
        fLastBucket = 0;
        fScalingFactor = 1;
        fFirstBucketTime = 0;
    }

    /**
     * Copy constructor
     * @param other another scaled data.
     */
    public HistogramScaledData(HistogramScaledData other) {
        fWidth = other.fWidth;
        fHeight = other.fHeight;
        fBarWidth = other.fBarWidth;
        fData = Arrays.copyOf(other.fData, fWidth);
        fBucketDuration = other.fBucketDuration;
        fMaxValue = other.fMaxValue;
        fCurrentBucket = other.fCurrentBucket;
        fLastBucket = other.fLastBucket;
        fScalingFactor = other.fScalingFactor;
        fFirstBucketTime = other.fFirstBucketTime;
    }

    // ------------------------------------------------------------------------
    // Setter and Getter
    // ------------------------------------------------------------------------

    /**
     * Returns the time of the first bucket of the scaled data.
     * @return the time of the first bucket.
     */
    public long getFirstBucketTime() {
        return fFirstBucketTime;
    }

    /**
     * Set the first event time.
     * @param firstEventTime The time to set
     */
    public void setFirstBucketTime(long firstEventTime) {
        fFirstBucketTime = firstEventTime;
    }

    /**
     * Returns the time of the last bucket.
     * @return last bucket time
     */
    public long getLastBucketTime() {
        return getBucketStartTime(fLastBucket);
    }

    /**
     * Returns the time of the bucket start time for given index.
     * @param index A bucket index.
     * @return the time of the bucket start time
     */
    public long getBucketStartTime(int index) {
        return fFirstBucketTime + index * fBucketDuration;
    }

    /**
     * Returns the time of the bucket end time for given index.
     * @param index A bucket index.
     * @return the time of the bucket end time
     */
    public long getBucketEndTime(int index) {
        return getBucketStartTime(index) + fBucketDuration;
    }
}