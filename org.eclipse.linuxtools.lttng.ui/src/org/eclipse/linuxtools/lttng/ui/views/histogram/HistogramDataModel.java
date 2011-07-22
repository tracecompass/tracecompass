/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.ui.views.histogram;

import java.util.Arrays;

import org.eclipse.linuxtools.lttng.exceptions.EventOutOfSequenceException;
import org.eclipse.linuxtools.lttng.ui.LTTngUILogger;
import org.eclipse.linuxtools.lttng.ui.views.histogram.HistogramScaledData;

/**
 * <b><u>HistogramDataModel</u></b>
 * <p>
 * Histogram-independent data model with the following characteristics:
 * <ul>
 * <li>The <i>basetime</i> is the timestamp of the first event
 * <li>There is a fixed number (<i>n</i>) of buckets of uniform duration
 * (<i>d</i>)
 * <li>The <i>timespan</i> of the model is thus: <i>n</i> * <i>d</i> time units
 * <li>Bucket <i>i</i> holds the number of events that occurred in time range:
 * [<i>basetime</i> + <i>i</i> * <i>d</i>, <i>basetime</i> + (<i>i</i> + 1) *
 * <i>d</i>)
 * </ul>
 * Initially, the bucket durations is set to 1ns. As the events are read, they
 * are tallied (using <i>countEvent()</i>) in the appropriate bucket (relative
 * to the <i>basetime</i>).
 * <p>
 * Eventually, an event will have a timestamp that exceeds the <i>timespan</i>
 * high end (determined by <i>n</i>, the number of buckets, and <i>d</i>, the
 * bucket duration). At this point, the histogram needs to be compacted. This is
 * done by simply merging adjacent buckets by pair, in effect doubling the
 * <i>timespan</i> (<i>timespan'</i> = <i>n</i> * <i>d'</i>, where <i>d'</i> =
 * 2<i>d</i>). This compaction happens as needed as the trace is read.
 * <p>
 * The mapping from the model to the UI is performed by the <i>scaleTo()</i>
 * method. By keeping the number of buckets <i>n</i> relatively large with
 * respect to to the number of pixels in the actual histogram, we should achieve
 * a nice result when visualizing the histogram.
 * <p>
 * TODO: Add filter support for more refined event counting (e.g. by trace,
 * event type, etc.)
 * <p>
 * TODO: Cut-off eccentric values?
 * TODO: Support for going back in time?
 */
public class HistogramDataModel {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    // The default number of buckets
    public static final int DEFAULT_NUMBER_OF_BUCKETS = 16 * 1000;

//    // The ratio where an eccentric value will be truncated
//    private static final int MAX_TO_AVERAGE_CUTOFF_RATIO = 5;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // Bucket management
    private final int fNbBuckets;
    private final long[] fBuckets;
    private long fBucketDuration;
    private long fNbEvents;
    private int fLastBucket;

    // Timestamps
    private long fFirstEventTime;
    private long fLastEventTime;
    private long fCurrentEventTime;
    private long fTimeLimit;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public HistogramDataModel() {
        this(DEFAULT_NUMBER_OF_BUCKETS);
    }

    public HistogramDataModel(int nbBuckets) {
        fNbBuckets = nbBuckets;
        fBuckets = new long[nbBuckets];
        clear();
    }

    public HistogramDataModel(HistogramDataModel other) {
        fNbBuckets = other.fNbBuckets;
        fBuckets = Arrays.copyOf(other.fBuckets, fNbBuckets);
        fBucketDuration = other.fBucketDuration;
        fNbEvents = other.fNbEvents;
        fLastBucket = other.fLastBucket;
        fFirstEventTime = other.fFirstEventTime;
        fLastEventTime = other.fLastEventTime;
        fCurrentEventTime = other.fCurrentEventTime;
        fTimeLimit = other.fTimeLimit;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    public long getNbEvents() {
        return fNbEvents;
    }

    public int getNbBuckets() {
        return fNbBuckets;
    }

    public long getBucketDuration() {
        return fBucketDuration;
    }

    public long getStartTime() {
        return fFirstEventTime;
    }

    public long getEndTime() {
        return fLastEventTime;
    }

    public long getCurrentEventTime() {
        return fCurrentEventTime;
    }

    public long getTimeLimit() {
        return fTimeLimit;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Clear the histogram model.
     */
    public void clear() {
        Arrays.fill(fBuckets, 0);
        fNbEvents = 0;
        fFirstEventTime = 0;
        fLastEventTime = 0;
        fCurrentEventTime = 0;
        fLastBucket = 0;
        fBucketDuration = 1; // 1ns
        updateEndTime();
    }

    /**
     * Sets the current event time
     * 
     * @param timestamp
     */
    public void setCurrentEvent(long timestamp) {
        fCurrentEventTime = timestamp;
    }

    /**
     * Add event to the correct bucket, compacting the if needed.
     * 
     * @param timestamp the timestamp of the event to count
     */
    public void countEvent(long timestamp) {
        // Set the start/end time if not already done
        if (fLastBucket == 0 && fBuckets[0] == 0 && timestamp > 0) {
            fFirstEventTime = timestamp;
            updateEndTime();
        }
        if (fLastEventTime < timestamp) {
            fLastEventTime = timestamp;
        }

        // Compact as needed
        while (timestamp >= fTimeLimit) {
            mergeBuckets();
        }

        // Validate
        if (timestamp < fFirstEventTime) {
            String message = "Out of order timestamp. Going back in time?"; //$NON-NLS-1$
            EventOutOfSequenceException exception = new EventOutOfSequenceException(message);
            LTTngUILogger.logError(message, exception);
            return;
        }

        // Increment the right bucket
        int index = (int) ((timestamp - fFirstEventTime) / fBucketDuration);
        fBuckets[index]++;
        fNbEvents++;
        if (fLastBucket < index)
            fLastBucket = index;
    }

    /**
     * Scale the model data to the width and height requested.
     * 
     * @param width
     * @param height
     * @return the result array of size [width] and where the highest value
     *         doesn't exceed [height]
     */
    public HistogramScaledData scaleTo(int width, int height) {
        // Basic validation
        assert width > 0 && height > 0;

        // The result structure
        HistogramScaledData result = new HistogramScaledData(width, height);

        // Scale horizontally
        int bucketsPerBar = fLastBucket / width + 1;
        result.fBucketDuration = bucketsPerBar * fBucketDuration;
        for (int i = 0; i < width; i++) {
            int count = 0;
            for (int j = i * bucketsPerBar; j < (i + 1) * bucketsPerBar; j++) {
                if (fNbBuckets <= j)
                    break;
                count += fBuckets[j];
            }
            result.fData[i] = count;
            result.fLastBucket = i;
            if (result.fMaxValue < count)
                result.fMaxValue = count;
        }

        // Scale vertically
        if (result.fMaxValue > 0) {
            result.fScalingFactor = (double) height / result.fMaxValue;
        }

        // Set the current event index in the scaled histogram
        if (fCurrentEventTime >= fFirstEventTime && fCurrentEventTime <= fLastEventTime)
            result.fCurrentBucket = (int) ((fCurrentEventTime - fFirstEventTime) / fBucketDuration) / bucketsPerBar;
        else
            result.fCurrentBucket = HistogramScaledData.OUT_OF_RANGE_BUCKET;

        return result;
    }

    // ------------------------------------------------------------------------
    // Helper functions
    // ------------------------------------------------------------------------

    private void updateEndTime() {
        fTimeLimit = fFirstEventTime + fNbBuckets * fBucketDuration;
    }

    private void mergeBuckets() {
        for (int i = 0; i < fNbBuckets / 2; i++) {
            fBuckets[i] = fBuckets[2 * i] + fBuckets[2 * i + 1];
        }
        Arrays.fill(fBuckets, fNbBuckets / 2, fNbBuckets, 0);
        fBucketDuration *= 2;
        updateEndTime();
        fLastBucket = fNbBuckets / 2 - 1;
    }

}
