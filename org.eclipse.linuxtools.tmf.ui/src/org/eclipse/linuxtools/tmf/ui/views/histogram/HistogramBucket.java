/*******************************************************************************
 * Copyright (c) 2014 Kalray
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Xavier Raynaud - Initial API and implementation
 *   Patrick Tasse - Fix concurrency issue
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.histogram;

import java.util.Arrays;

/**
 * This class counts events for a particular time range, taking into account origin of the event.
 * @author Xavier Raynaud
 * @since 3.0
 */
public class HistogramBucket {

    private int fNbEvents = 0;
    private int fEvents[];

    /**
     * Constructor
     * @param traceCount number of traces of the experiment.
     */
    public HistogramBucket(int traceCount) {
        fEvents = new int[traceCount];
    }

    /**
     * Constructor
     * @param values list of values
     */
    public HistogramBucket(int... values) {
        fEvents = values;
        for (int i : fEvents) {
            fNbEvents += i;
        }
    }

    /**
     * Copy Constructor
     * @param b a HistogramBucket to copy
     */
    public HistogramBucket(HistogramBucket b) {
        add(b);
    }

    /**
     * Merge Constructor
     * @param b1 a HistogramBucket
     * @param b2 another HistogramBucket
     */
    public HistogramBucket(HistogramBucket b1, HistogramBucket b2) {
        add(b1);
        add(b2);
    }

    /**
     * @return the number of events in this bucket
     */
    public int getNbEvents() {
        return fNbEvents;
    }

    /**
     * Add an event in this bucket
     * @param traceIndex a trace index - see {@link HistogramDataModel#setTrace}.
     */
    public synchronized void addEvent(int traceIndex) {
        ensureCapacity(traceIndex + 1);
        fEvents[traceIndex]++;
        fNbEvents++;
    }

    private void ensureCapacity(int len) {
        if (fEvents == null) {
            fEvents = new int[len];
        } else if (fEvents.length < len) {
            int[] oldArray = fEvents;
            fEvents = new int[len];
            System.arraycopy(oldArray, 0, fEvents, 0, oldArray.length);
        }
    }

    /**
     * Gets the number of event in this bucket belonging to given trace
     * @param traceIndex a trace index
     * @return the number of events in this bucket belonging to the given trace
     */
    public int getNbEvent(int traceIndex) {
        if (fEvents == null || fEvents.length<= traceIndex) {
            return 0;
        }
        return fEvents[traceIndex];
    }

    /**
     * @return the number of traces in this bucket
     */
    public int getNbTraces() {
        if (fEvents == null) {
            return 0;
        }
        return fEvents.length;
    }

    /**
     * Merge the given bucket in this one.
     * @param histogramBucket a bucket to merge in this one.
     */
    public synchronized void add(HistogramBucket histogramBucket) {
        if (histogramBucket != null && histogramBucket.fNbEvents != 0) {
            int len = histogramBucket.fEvents.length;
            ensureCapacity(len);
            for (int i = 0; i < len; i++) {
                int nbEvents = histogramBucket.fEvents[i];
                fEvents[i] += nbEvents;
                fNbEvents += nbEvents;
            }
        }
    }

    /**
     * @return <code>true</code> if this bucket contains no event, <code>false</code> otherwise.
     */
    public boolean isEmpty() {
        return fNbEvents == 0;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(fEvents);
        result = prime * result + fNbEvents;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        HistogramBucket other = (HistogramBucket) obj;
        if (fNbEvents != other.fNbEvents) {
            return false;
        }
        if (fNbEvents != 0 && !Arrays.equals(fEvents, other.fEvents)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(fNbEvents);
        sb.append(": "); //$NON-NLS-1$
        sb.append(Arrays.toString(fEvents));
        return sb.toString();
    }

}
