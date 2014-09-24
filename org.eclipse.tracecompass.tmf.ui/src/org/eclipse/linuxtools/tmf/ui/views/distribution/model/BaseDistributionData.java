/*******************************************************************************
 * Copyright (c) 2011, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Francois Chouinard - Moved from LTTng to TMF
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.distribution.model;

/**
 * Class with basic distribution data used for distribution models.
 *
 * It stores number of events (with timestamp) in buckets with a start time and a
 * certain duration. The duration is the same across all buckets.
 * Note that Timestamps are stored as long values.
 *
 * @version 1.0
 * @author Bernd Hufmann
 */
public class BaseDistributionData {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * Constant indication that bucket is not filled.
     */
    public final static int OUT_OF_RANGE_BUCKET = -1;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     *  Number of buckets
     */
    protected final int fNbBuckets;
    /**
     * Duration of each bucket
     */
    protected long fBucketDuration;
    /**
     * Bucket index of last event time
     */
    protected int fLastBucket;
    /**
     * Timestamp of the first bucket. (could be negative when analyzing events with descending time!!!)
     */
    protected long fFirstBucketTime;
    /**
     * Timestamp of the first event
     */
    protected long fFirstEventTime;
    /**
     *  Timestamp of the last event
     */
    protected long fLastEventTime;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs a base distribution data object.
     * @param nbBuckets A total number of buckets
     */
    public BaseDistributionData(int nbBuckets) {
        fNbBuckets = nbBuckets;
        clear();
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * Returns the total number of buckets.
     *
     * @return the number of buckets.
     */
    public int getNbBuckets() {
        return fNbBuckets;
    }

    /**
     * Returns the duration of buckets.
     *
     * @return bucket duration
     */
    public long getBucketDuration() {
        return fBucketDuration;
    }

    /**
     * Set the bucket duration.
     *
     * @param bucketDuration The duration to set.
     */
    public void setBucketDuration(long bucketDuration) {
        fBucketDuration = bucketDuration;
    }

    /**
     * Returns the index of the last used bucket.
     *
     * @return last bucket index.
     */
    public int getLastBucket() {
        return fLastBucket;
    }

    /**
     * Sets the index of the last bucket used.
     *
     * @param lastBucket The last bucket index to set.
     */
    public void setLastBucket(int lastBucket) {
        fLastBucket = lastBucket;
    }

    /**
     * Returns the start time of the first bucket.
     *
     * @return first bucket time.
     */
    public long getFirstBucketTime() {
        return fFirstBucketTime;
    }

    /**
     * Sets the start time of the first bucket.
     *
     * @param firstBucketTime The bucket time to ser.
     */
    public void setFirstBucketTime(long firstBucketTime) {
        fFirstBucketTime = firstBucketTime;
    }

    /**
     * Returns the start time of the last bucket used.
     *
     * @return the start time of the last bucket.
     */
    public long getLastBucketTime() {
        return getBucketStartTime(fLastBucket);
    }

    /**
     * Returns the time of the event with the lowest timestamp.
     *
     * @return first event time.
     */
    public long getFirstEventTime() {
        return fFirstEventTime;
    }

    /**
     * Sets the time of the event with the lowest timestamp.
     *
     * @param firstEventTime The first event time to set.
     */
    public void setFirstEventTime(long firstEventTime) {
        fFirstEventTime = firstEventTime;
    }

    /**
     * Returns the time of the event with the biggest timestamp.
     *
     * @return the last event time.
     */
    public long getLastEventTime() {
        return fLastEventTime;
    }

    /**
     * Sets the time of the event with the biggest timestamp.
     *
     * @param lastEventTime The last event time to set.
     */
    public void setLastEventTime(long lastEventTime) {
        fLastEventTime = lastEventTime;
    }

    /**
     * Returns the bucket start time of a given bucket index.
     *
     * @param index The bucket index.
     * @return the bucket start time of a given bucket index.
     */
    public long getBucketStartTime(int index) {
        return fFirstBucketTime + index * fBucketDuration;
    }

    /**
     * Returns the bucket end time of a given bucket index.
     *
     * @param index The bucket index.
     * @return the bucket start time of a given bucket index.
     */
    public long getBucketEndTime(int index) {
        return getBucketStartTime(index) + fBucketDuration;
    }

    /**
     * Returns the bucket index of the bucket containing a given time.
     *
     * @param time The timestamp to check.
     * @return the bucket index of the bucket containing the given time.
     */
    public int getIndex(long time) {
        return (int)((time - fFirstBucketTime) / fBucketDuration);
    }

    /**
     * Check if an index is valid.
     *
     * @param index
     *            The index to check
     * @return If it's valid, true or false.
     */
    public boolean isIndexValid(int index) {
        return ((index >= 0) && (index <= fNbBuckets - 1));
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Clears the data model to default values.
     */
    public void clear() {
        fFirstBucketTime = 0;
        fFirstEventTime = 0;
        fLastEventTime = 0;
        fLastBucket = 0;
        fBucketDuration = 1;
    }
}
