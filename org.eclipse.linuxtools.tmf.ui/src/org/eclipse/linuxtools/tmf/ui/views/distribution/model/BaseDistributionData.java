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
 * <b><u>BaseDistributionData</u></b>
 * <p>
 */
public class BaseDistributionData {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    public final static int OUT_OF_RANGE_BUCKET = -1;

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

    public BaseDistributionData(int nbBuckets) {
        fNbBuckets = nbBuckets;
        clear();
    }
    
    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    
    public int getNbBuckets() {
        return fNbBuckets;
    }

    public long getBucketDuration() {
        return fBucketDuration;
    }

    public void setBucketDuration(long bucketDuration) {
        fBucketDuration = bucketDuration;
    }

    public int getLastBucket() {
        return fLastBucket;
    }

    public void setLastBucket(int lastBucket) {
        fLastBucket = lastBucket;
    }

    public long getFirstBucketTime() {
        return fFirstBucketTime;
    }

    public void setFirstBucketTime(long firstBucketTime) {
        fFirstBucketTime = firstBucketTime;
    }

    public long getLastBucketTime() {
        return getBucketStartTime(fLastBucket);
    }

    public long getFirstEventTime() {
        return fFirstEventTime;
    }

    public void setFirstEventTime(long firstEventTime) {
        fFirstEventTime = firstEventTime;
    }

    public long getLastEventTime() {
        return fLastEventTime;
    }
    
    public void setLastEventTime(long lastEventTime) {
        fLastEventTime = lastEventTime;
    }
    
    public long getBucketStartTime(int index) {
        return fFirstBucketTime + index * fBucketDuration;
    }
    
    public long getBucketEndTime(int index) {
        return getBucketStartTime(index) + fBucketDuration;
    }
    
    public int getIndex(long time) {
        return (int)((time - fFirstBucketTime) / fBucketDuration); 
    }
    
    public boolean isIndexValid(int index) {
        return ((index >= 0) && (index <= fNbBuckets - 1));
    }
    
    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    public void clear() {
        fFirstBucketTime = 0;
        fFirstEventTime = 0;
        fLastEventTime = 0;
        fLastBucket = 0;
        fBucketDuration = 1; // 1ns
    }

}
