/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 ******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.distribution.model;

import org.eclipse.linuxtools.lttng.ui.views.latency.model.Config;
import org.eclipse.linuxtools.tmf.ui.views.distribution.model.BaseDistributionData;

/**
 * <b><u>DistributionData</u></b>
 * <p>
 * The algorithm is based on the algorithm for the Histogram. The difference is that
 * it supports two dimensions. For more details about the model principle 
 * @see org.eclipse.linuxtools.lttng.ui.views.histogram.HistogramDataModel 
 * <p>
 */
abstract public class DistributionData extends BaseDistributionData {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * Flag to indicate if given timestamp is the first one to count 
     */
    protected boolean fIsFirst;
    
    /**
     *  reference to fBuckets
     */
    protected final int [][] fBuckets;
    
    /**
     * Time limit (current available longest time)
     */
    protected long fTimeLimit;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    public DistributionData(int[][] buckets) {
        this(Config.DEFAULT_NUMBER_OF_BUCKETS, buckets);
    }

    public DistributionData(int nbBuckets, int[][] buckets) {
        super(nbBuckets);
        fBuckets = buckets;
        clear();
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
   
    public long getTimeLimit() {
        return fTimeLimit;
    }
    
    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.distribution.model.BaseDistributionData#clear()
     */
    @Override
    public void clear() {
        super.clear();
        fIsFirst = true;
        updateEndTime();
    }

    public boolean isFirst() {
        return fIsFirst;
    }

    public int countEvent(long timestamp) {

        // Set the start/end time if not already done
        if (fIsFirst) {
            fIsFirst = false;
            fFirstBucketTime = timestamp;
            fFirstEventTime = timestamp;

            updateEndTime();
        }

        // save first event time if necessary 
        if (timestamp < fFirstEventTime) {
            fFirstEventTime = timestamp;
        }

        // save last event time if necessary
        if (fLastEventTime < timestamp) {
            fLastEventTime = timestamp;
        }

        
        if (timestamp >= fFirstBucketTime) {
            // Compact as needed
            while (timestamp >= fTimeLimit) {
                mergeBuckets();
            }

        } else {

            // Get offset for buckets adjustment
            int offset = getOffset(timestamp);

            // Compact as needed
            while (fLastBucket + offset >= fNbBuckets) {
                mergeBuckets();
                offset = getOffset(timestamp);
            }

            // Move buckets with offset (to right)
            moveBuckets(offset);

            // Adjust start/end time and index 
            fLastBucket = fLastBucket + offset;
            fFirstBucketTime = fFirstBucketTime - offset * fBucketDuration;
            updateEndTime();
        }

        // Increment the right bucket
        int index = (int) ((timestamp - fFirstBucketTime) / fBucketDuration);

        if (fLastBucket < index) {
            fLastBucket = index;
        }

        return index;
    }

    // ------------------------------------------------------------------------
    // Abstract 
    // ------------------------------------------------------------------------
    
    /**
     * Moves content of buckets with the given offset in positive direction. 
     * It has to be implemented accordingly in the relevant sub-classes for 
     * horizontal and vertical direction.  
     * 
     * @param buckets - 2-dimensional array of buckets 
     * @param offset - offset to move
     */
    abstract protected void moveBuckets(int offset);
    
    /**
     * Merges buckets if end time is exceeded. It has to be implemented 
     * accordingly in the relevant sub-classes for horizontal and 
     * vertical direction.
     * @param buckets
     */
    abstract protected void mergeBuckets();

    // ------------------------------------------------------------------------
    // Helper Functions
    // ------------------------------------------------------------------------
    protected int getOffset(long timestamp) {
        int offset = (int) ((fFirstBucketTime - timestamp) / fBucketDuration);
        if ((fFirstBucketTime - timestamp) % fBucketDuration != 0) {
            offset++;
        }
        return offset;
    }
    
    protected void updateEndTime() {
        fTimeLimit = fFirstBucketTime + fNbBuckets * fBucketDuration;
    }
}
