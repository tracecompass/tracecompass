/*******************************************************************************
 * Copyright (c) 2013, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Matthew Khouzam - Modified to use a TreeSet
 *   Patrick Tasse - Add message to exceptions
 ******************************************************************************/

package org.eclipse.tracecompass.internal.statesystem.core.backend;

import java.io.File;
import java.io.FileInputStream;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.statesystem.core.backend.IStateHistoryBackend;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.interval.TmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;

/**
 * State history back-end that stores its intervals in RAM only. It cannot be
 * saved to disk, which means we need to rebuild it every time we re-open a
 * trace. But it's relatively quick to build, so this shouldn't be a problem in
 * most cases.
 *
 * This should only be used with very small state histories (and/or, very small
 * traces). Since it's stored in standard Collections, it's limited to 2^31
 * intervals.
 *
 * @author Alexandre Montplaisir
 */
public class InMemoryBackend implements IStateHistoryBackend {

    /**
     * We need to compare the end time and the attribute, because we can have 2
     * intervals with the same end time (for different attributes). And TreeSet
     * needs a unique "key" per element.
     */
    private static final Comparator<ITmfStateInterval> END_COMPARATOR =
            new Comparator<ITmfStateInterval>() {
                @Override
                public int compare(ITmfStateInterval o1, ITmfStateInterval o2) {
                    final long e1 = o1.getEndTime();
                    final long e2 = o2.getEndTime();
                    final int a1 = o1.getAttribute();
                    final int a2 = o2.getAttribute();
                    if (e1 < e2) {
                        return -1;
                    } else if (e1 > e2) {
                        return 1;
                    } else if (a1 < a2) {
                        return -1;
                    } else if (a1 > a2) {
                        return 1;
                    } else {
                        return 0;
                    }
                }

            };

    private final @NonNull String ssid;
    private final NavigableSet<ITmfStateInterval> intervals;
    private final long startTime;

    private volatile long latestTime;

    /**
     * Constructor
     *
     * @param ssid
     *            The state system's ID
     * @param startTime
     *            The start time of this interval store
     */
    public InMemoryBackend(@NonNull String ssid, long startTime) {
        this.ssid = ssid;
        this.startTime = startTime;
        this.latestTime = startTime;
        this.intervals = new TreeSet<>(END_COMPARATOR);
    }

    @Override
    public String getSSID() {
        return ssid;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public long getEndTime() {
        return latestTime;
    }

    @Override
    public void insertPastState(long stateStartTime, long stateEndTime,
            int quark, ITmfStateValue value) throws TimeRangeException {
        /* Make sure the passed start/end times make sense */
        if (stateStartTime > stateEndTime || stateStartTime < startTime) {
            throw new TimeRangeException(ssid + " Interval Start:" + stateStartTime + ", Interval End:" + stateEndTime + ", Backend Start:" + startTime); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        ITmfStateInterval interval = new TmfStateInterval(stateStartTime, stateEndTime, quark, value);

        /* Add the interval into the tree */
        synchronized (intervals) {
            intervals.add(interval);
        }

        /* Update the "latest seen time" */
        if (stateEndTime > latestTime) {
            latestTime = stateEndTime;
        }
    }

    @Override
    public void doQuery(List<ITmfStateInterval> currentStateInfo, long t)
            throws TimeRangeException {
        if (!checkValidTime(t)) {
            throw new TimeRangeException(ssid + " Time:" + t + ", Start:" + startTime + ", End:" + latestTime); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        /*
         * The intervals are sorted by end time, so we can binary search to get
         * the first possible interval, then only compare their start times.
         */
        synchronized (intervals) {
            Iterator<ITmfStateInterval> iter = searchforEndTime(intervals, t);
            for (int modCount = 0; iter.hasNext() && modCount < currentStateInfo.size();) {
                ITmfStateInterval entry = iter.next();
                final long entryStartTime = entry.getStartTime();
                if (entryStartTime <= t) {
                    /* Add this interval to the returned values */
                    currentStateInfo.set(entry.getAttribute(), entry);
                    modCount++;
                }
            }
        }
    }

    @Override
    public ITmfStateInterval doSingularQuery(long t, int attributeQuark)
            throws TimeRangeException {
        if (!checkValidTime(t)) {
            throw new TimeRangeException(ssid + " Time:" + t + ", Start:" + startTime + ", End:" + latestTime); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        /*
         * The intervals are sorted by end time, so we can binary search to get
         * the first possible interval, then only compare their start times.
         */
        synchronized (intervals) {
            Iterator<ITmfStateInterval> iter = searchforEndTime(intervals, t);
            while (iter.hasNext()) {
                ITmfStateInterval entry = iter.next();
                final boolean attributeMatches = (entry.getAttribute() == attributeQuark);
                final long entryStartTime = entry.getStartTime();
                if (attributeMatches) {
                    if (entryStartTime <= t) {
                        /* This is the droid we are looking for */
                        return entry;
                    }
                }
            }
        }
        return null;
    }

    private boolean checkValidTime(long t) {
        if (t >= startTime && t <= latestTime) {
            return true;
        }
        return false;
    }

    @Override
    public void finishedBuilding(long endTime) throws TimeRangeException {
        /* Nothing to do */
    }

    @Override
    public FileInputStream supplyAttributeTreeReader() {
        /* Saving to disk not supported */
        return null;
    }

    @Override
    public File supplyAttributeTreeWriterFile() {
        /* Saving to disk not supported */
        return null;
    }

    @Override
    public long supplyAttributeTreeWriterFilePosition() {
        /* Saving to disk not supported */
        return -1;
    }

    @Override
    public void removeFiles() {
        /* Nothing to do */
    }

    @Override
    public void dispose() {
        /* Nothing to do */
    }

    private static Iterator<ITmfStateInterval> searchforEndTime(NavigableSet<ITmfStateInterval> tree, long time) {
        ITmfStateInterval dummyInterval = new TmfStateInterval(-1, time, -1, TmfStateValue.nullValue());
        ITmfStateInterval myInterval = tree.lower(dummyInterval);
        if (myInterval == null) {
            return tree.iterator();
        }
        final SortedSet<ITmfStateInterval> tailSet = tree.tailSet(myInterval);
        Iterator<ITmfStateInterval> retVal = tailSet.iterator();
        retVal.next();
        return retVal;
    }

}
