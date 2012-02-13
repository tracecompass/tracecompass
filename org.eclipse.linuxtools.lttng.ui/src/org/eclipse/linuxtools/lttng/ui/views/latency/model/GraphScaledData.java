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
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.latency.model;

/**
 * <b><u>GraphScaledData</u></b>
 * Convenience class for scaled distribution data.  
 * <p>
 */
import java.util.Arrays;

import org.eclipse.linuxtools.lttng.ui.views.distribution.model.BaseDistributionData;

public class GraphScaledData {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private int fWidth;
    private int fHeight;
    private int fBarWidth;
    private int[][] fData;
    private BaseDistributionData fHorDistributionData;
    private BaseDistributionData fVerDistributionData;
    private long fCurrentEventTime;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    public GraphScaledData(int width, int height, int barWidth) {
        fWidth = width;
        fHeight = height;
        fBarWidth = barWidth;
        int horNbBuckets = (int)width/barWidth;
        int verNbBuckets = (int)height/barWidth; 
        fData = new int[horNbBuckets][verNbBuckets];
        for (int[] row : fData) {
            Arrays.fill(row, 0);    
        }
        fHorDistributionData = new BaseDistributionData(horNbBuckets);
        fHorDistributionData.clear();
        
        fVerDistributionData = new BaseDistributionData(verNbBuckets);
        fVerDistributionData.clear();
        
        fCurrentEventTime = Config.INVALID_EVENT_TIME;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    public int getWidth() {
        return fWidth;
    }

    public int getHeight() {
        return fHeight;
    }

    public int getBarWidth() {
        return fBarWidth;
    }

    public int[][] getData() {
        return fData;
    }

    public int getHorNbBuckets() {
        return fHorDistributionData.getNbBuckets();
    }
    
    public int getVerNbBuckets() {
        return fVerDistributionData.getNbBuckets();
    }

    public long getHorFirstBucketTime() {
        return fHorDistributionData.getFirstBucketTime();
    }

    public long getVerFirstBucketTime() {
        return fVerDistributionData.getFirstBucketTime();
    }

    public long getHorLastBucketTime() {
        return fHorDistributionData.getLastBucketTime();
    }

    public long getVerLastBucketTime() {
        return fVerDistributionData.getLastBucketTime();
    }
    
    public long getHorFirstEventTime() {
        return fHorDistributionData.getFirstEventTime();
    }

    public long getVerFirstEventTime() {
        return fVerDistributionData.getFirstEventTime();
    }

    public long getHorLastEventTime() {
        return fHorDistributionData.getLastEventTime();
    }

    public long getVerLastEventTime() {
        return fVerDistributionData.getLastEventTime();
    }
    
    public long getHorBucketDuration() {
        return fHorDistributionData.getBucketDuration();
    }

    public long getVerBucketDuration() {
        return fVerDistributionData.getBucketDuration();
    }

    public int getHorLastBucket() {
        return fHorDistributionData.getLastBucket();
    }

    public int getVerLastBucket() {
        return fVerDistributionData.getLastBucket();
    }

    public long getHorBucketStartTime(int index) {
        return  fHorDistributionData.getBucketStartTime(index);
    }
    
    public long getHorBucketEndTime(int index) {
        return fHorDistributionData.getBucketEndTime(index);
    }
    
    public long getVerBucketStartTime(int index) {
        return  fVerDistributionData.getBucketStartTime(index);
    }
    
    public long getVerBucketEndTime(int index) {
        return fVerDistributionData.getBucketEndTime(index);
    }
    
    public int getEventCount(int horIndex, int verIndex) {
        return fData[horIndex][verIndex];
    }
    
    public long getCurrentEventTime() {
        return fCurrentEventTime;
    }

    public boolean isCurrentEventTimeValid() {
        if (fCurrentEventTime == Config.INVALID_EVENT_TIME || fCurrentEventTime < getHorFirstEventTime() || fCurrentEventTime > getHorLastEventTime()) {
            return false;
        }
        return true;
    }
    
    public int getHorBucketIndex(long time) {
         return fHorDistributionData.getIndex(time);
    }
    
    public int getVerBucketIndex(long time) {
        return fVerDistributionData.getIndex(time);
    }
    
    public boolean isHorIndexValid(int index) {
        return fHorDistributionData.isIndexValid(index);
    }
    
    public boolean isVerIndexValid(int index) {
        return fVerDistributionData.isIndexValid(index);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    public void setWidth(int width) {
        fWidth = width;
    }

    public void setHeight(int height) {
        fHeight = height;
    }
    
    public void setBarWidth(int barWidth) {
        fBarWidth = barWidth;
    }

    public void setData(int[][] data) {
        fData = data;
    }

    public void setHorFirstBucketTime(long firstBucketTime) {
        fHorDistributionData.setFirstBucketTime(firstBucketTime);
    }

    public void setVerFirstBucketTime(long firstBucketTime) {
        fVerDistributionData.setFirstBucketTime(firstBucketTime);
    }

    public void setHorFirstEventTime(long firstEventTime) {
        fHorDistributionData.setFirstEventTime(firstEventTime);
    }

    public void setVerFirstEventTime(long firstEventTime) {
        fVerDistributionData.setFirstEventTime(firstEventTime);
    }

    public void setHorLastEventTime(long lastEventTime) {
        fHorDistributionData.setLastEventTime(lastEventTime);
    }

    public void setVerLastEventTime(long lastEventTime) {
        fVerDistributionData.setLastEventTime(lastEventTime);
    }

    public void setHorBucketDuration(long bucketDuration) {
        fHorDistributionData.setBucketDuration(bucketDuration);
    }

    public void setVerBucketDuration(long bucketDuration) {
        fVerDistributionData.setBucketDuration(bucketDuration);
    }

    public void setHorLastBucket(int lastBucket) {
        fHorDistributionData.setLastBucket(lastBucket);
    }

    public void setVerLastBucket(int lastBucket) {
        fVerDistributionData.setLastBucket(lastBucket);
    }

    public void setCurrentEventTime(long currentEventTime) {
        fCurrentEventTime = currentEventTime;
    }
}
