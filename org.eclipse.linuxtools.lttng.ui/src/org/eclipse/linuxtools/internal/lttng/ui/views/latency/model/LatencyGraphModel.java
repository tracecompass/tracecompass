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

package org.eclipse.linuxtools.internal.lttng.ui.views.latency.model;

import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.linuxtools.internal.lttng.ui.views.distribution.model.DistributionData;
import org.eclipse.linuxtools.internal.lttng.ui.views.distribution.model.HorDistributionData;
import org.eclipse.linuxtools.internal.lttng.ui.views.distribution.model.VerDistributionData;

/**
 * <b><u>LatencyGraphModel</u></b>
 * <p>
 */
public class LatencyGraphModel implements IGraphDataModel {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private final int fNbBuckets;
    private final int [][] fBuckets;
    private final DistributionData fHorDistributionData;
    private final DistributionData fVerDistributionData;
    private long fCurrentEventTime;
    
    // private listener lists
    private final ListenerList fModelListeners;

    private final ReentrantLock fLock;
    
    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    public LatencyGraphModel() {
        this(Config.DEFAULT_NUMBER_OF_BUCKETS);
    }

    public LatencyGraphModel(int nbBuckets) {
        fNbBuckets = nbBuckets;
        fBuckets = new int[nbBuckets][nbBuckets];
        fHorDistributionData = new HorDistributionData(nbBuckets, fBuckets);
        fVerDistributionData = new VerDistributionData(nbBuckets, fBuckets);
        fCurrentEventTime = Config.INVALID_EVENT_TIME;
       
        fModelListeners = new ListenerList();
        fLock = new ReentrantLock();
        clear();
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    
    public int getNbBuckets() {
        return fNbBuckets;
    }
    
    public long getHorBucketDuration() {
        fLock.lock(); 
        try {
            return fHorDistributionData.getBucketDuration();
        } finally {
            fLock.unlock();
        }
    }

    public long getVerBucketDuration() {
        fLock.lock(); 
        try {
            return fVerDistributionData.getBucketDuration();
        } finally {
            fLock.unlock();
        }
    }
    
    public long getHorFirstBucketTime() {
        fLock.lock(); 
        try {
            return fHorDistributionData.getFirstBucketTime();
        } finally {
            fLock.unlock();
        }
    }

    public long getVerFirstBucketTime() {
        fLock.lock(); 
        try {
            return fVerDistributionData.getFirstBucketTime();
        } finally {
            fLock.unlock();
        }
    }

    public long getHorFirstEventTime() {
        fLock.lock(); 
        try {
            return fHorDistributionData.getFirstEventTime();
        } finally {
            fLock.unlock();
        }
    }

    public long getVerFirstEventTime() {
        fLock.lock(); 
        try {
            return fVerDistributionData.getFirstEventTime();
        } finally {
            fLock.unlock();
        }
    }

    public long getHorLastEventTime() {
        fLock.lock(); 
        try {
            return fHorDistributionData.getLastEventTime();
        } finally {
            fLock.unlock();
        }
    }

    public long getVerLastEventTime() {
        fLock.lock(); 
        try {
            return fVerDistributionData.getLastEventTime();
        } finally {
            fLock.unlock();
        }
    }

    public long getHorTimeLimit() {
        fLock.lock(); 
        try {
            return fHorDistributionData.getTimeLimit();
        } finally {
            fLock.unlock();
        }
    }

    public long getVerTimeLimit() {
        fLock.lock(); 
        try {
            return fVerDistributionData.getTimeLimit();
        } finally {
            fLock.unlock();
        }
    }
    
    public int getHorLastBucket() {
        fLock.lock(); 
        try {
            return fHorDistributionData.getLastBucket();
        } finally {
            fLock.unlock();
        }
    }

    public int getVerLastBucket() {
        fLock.lock(); 
        try {
            return fVerDistributionData.getLastBucket();
        } finally {
            fLock.unlock();
        }
    }
    
    public long getCurrentEventTime() {
        fLock.lock();
        try {
            return fCurrentEventTime;
        } finally {
            fLock.unlock();
        }
    }

    // ------------------------------------------------------------------------
    // Listener interface
    // ------------------------------------------------------------------------
    public void addGraphModelListener(IGraphModelListener listener) {
        fModelListeners.add(listener);        
    }
    
    public void removeGraphModelListener(IGraphModelListener listener) {
        fModelListeners.remove(listener);
    }
 
    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.distribution.model.IBaseDataModel#clear()
     */
    @Override
    public void clear() {
        fLock.lock();
        try {
            for (int[] row : fBuckets) {
                Arrays.fill(row, 0, fNbBuckets, 0);
            }
            fHorDistributionData.clear();
            fVerDistributionData.clear();
        } finally {
            fLock.unlock();
        }
        fireModelUpdateNotification();
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.latency.model.IGraphDataModel#countEvent(int, long, long)
     */
    @Override
    public void countEvent(int eventCount, long timestamp, long time) {
        fLock.lock();
        try {
            int horIndex = fHorDistributionData.countEvent(timestamp);
            int verIndex = fVerDistributionData.countEvent(time);

            fBuckets[horIndex][verIndex]++;
        } finally {
            fLock.unlock();
        }

        fireModelUpdateNotification(eventCount);
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.latency.model.IGraphDataModel#scaleTo(int, int, int)
     */
    @Override
    public GraphScaledData scaleTo(int width, int height, int barWidth) {
        GraphScaledData scaledData = new GraphScaledData(width, height, barWidth);
        fLock.lock();
        try {
            if (!fHorDistributionData.isFirst() && !fVerDistributionData.isFirst() ) {

                // Basic validation
                if (width <= 0 ||  height <= 0 || barWidth <= 0)
                    throw new AssertionError("Invalid histogram dimensions (" + width + "x" + height + ", barWidth=" + barWidth + ")");   //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$

                // Scale horizontally
                int nbBars = width / barWidth;
                int bucketsPerBar = fHorDistributionData.getLastBucket() / nbBars + 1;

                int horData[][] = new int[nbBars][fNbBuckets];  
                for (int y = 0; y < fNbBuckets; y++) {
                    for (int i = 0; i < nbBars; i++) {
                        int count = 0;
                        for (int j = i * bucketsPerBar; j < (i + 1) * bucketsPerBar; j++) {
                            if (fNbBuckets <= j)
                                break;
                            count += fBuckets[j][y];
                        }
                        horData[i][y] = count;
                    }
                }

                // Scale vertically
                int nbVerBars = height / barWidth;
                int bucketsPerVerBar = fVerDistributionData.getLastBucket() / nbVerBars + 1;

                int verData[][] = new int[nbBars][nbVerBars];  
                for (int x = 0; x < nbBars; x++) {
                    for (int i = 0; i < nbVerBars; i++) {
                        int count = 0;
                        for (int j = i * bucketsPerVerBar; j < (i + 1) * bucketsPerVerBar; j++) {
                            if (fNbBuckets <= j)
                                break;
                            count += horData[x][j];
                        }
                        verData[x][i] = count;
                    }
                }

                scaledData.setData(verData);
                scaledData.setHorFirstBucketTime(fHorDistributionData.getFirstBucketTime());
                scaledData.setVerFirstBucketTime(fVerDistributionData.getFirstBucketTime());
                scaledData.setHorFirstEventTime(fHorDistributionData.getFirstEventTime());
                scaledData.setVerFirstEventTime(fVerDistributionData.getFirstEventTime());
                scaledData.setHorLastEventTime(fHorDistributionData.getLastEventTime());
                scaledData.setVerLastEventTime(fVerDistributionData.getLastEventTime());
                scaledData.setHorBucketDuration(bucketsPerBar * fHorDistributionData.getBucketDuration());
                scaledData.setVerBucketDuration(bucketsPerVerBar * fVerDistributionData.getBucketDuration());
                scaledData.setHorLastBucket(fHorDistributionData.getLastBucket() / bucketsPerBar);
                scaledData.setVerLastBucket(fVerDistributionData.getLastBucket() / bucketsPerVerBar);
                scaledData.setCurrentEventTime(fCurrentEventTime);
            }
        } finally {
            fLock.unlock();
        }

        return scaledData;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.distribution.model.IBaseDataModel#complete()
     */
    @Override
    public void complete() {
        fireModelUpdateNotification();
    }

    /**
     * Sets the current event time but don't notify listeners.
     * 
     * @param timestamp
     */
    public void setCurrentEvent(long timestamp) {
        fLock.lock();
        try {
            fCurrentEventTime = timestamp;
        } finally {
            fLock.unlock();
        }   
    }

    /**
     * Sets the current event time and notify listeners.
     * 
     * @param timestamp
     */
    public void setCurrentEventNotifyListeners(long timestamp) {
        fLock.lock();
        try {
            fCurrentEventTime = timestamp;
        } finally {
            fLock.unlock();
        }
        fireCurrentEventUpdateNotification();
    }
   
    // ------------------------------------------------------------------------
    // Helper functions
    // ------------------------------------------------------------------------

    /*
     * Notify listeners immediately
     */
    private void fireModelUpdateNotification() {
        fireModelUpdateNotification(0);
    }
    
    /*
     * Notify listeners with certain refresh rate.
     */
    private void fireModelUpdateNotification(int count) {
        if (count % Config.POINT_BUFFER_SIZE == 0) {
            Object[] listeners = fModelListeners.getListeners();
            for (int i = 0; i < listeners.length; i++) {
                IGraphModelListener listener = (IGraphModelListener) listeners[i];
                listener.graphModelUpdated();
            }
        }
    }

    /*
     * Notify listeners immediately
     */
    private void fireCurrentEventUpdateNotification() {
        Object[] listeners = fModelListeners.getListeners();
        for (int i = 0; i < listeners.length; i++) {
            IGraphModelListener listener = (IGraphModelListener) listeners[i];
            listener.currentEventUpdated(fCurrentEventTime);
        }
    }
}
