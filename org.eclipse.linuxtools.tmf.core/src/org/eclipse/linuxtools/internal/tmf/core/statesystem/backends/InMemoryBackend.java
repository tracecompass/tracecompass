/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.statesystem.backends;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.interval.TmfIntervalEndComparator;
import org.eclipse.linuxtools.tmf.core.interval.TmfStateInterval;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;

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

    private static final Comparator<ITmfStateInterval> END_COMPARATOR =
            new TmfIntervalEndComparator();

    private final List<ITmfStateInterval> intervals;
    private final long startTime;
    private long latestTime;

    /**
     * Constructor
     *
     * @param startTime
     *            The start time of this interval store
     */
    public InMemoryBackend(long startTime) {
        this.startTime = startTime;
        this.latestTime = startTime;
        this.intervals = new ArrayList<ITmfStateInterval>();
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
            throw new TimeRangeException();
        }

        ITmfStateInterval interval = new TmfStateInterval(stateStartTime, stateEndTime, quark, value);

        /* Update the "latest seen time" */
        if (stateEndTime > latestTime) {
            latestTime = stateEndTime;
        }

        /* Add the interval into the-array */
        intervals.add(interval);
    }


    @Override
    public void doQuery(List<ITmfStateInterval> currentStateInfo, long t)
            throws TimeRangeException {
        if (!checkValidTime(t)) {
            throw new TimeRangeException();
        }

        /*
         * The intervals are sorted by end time, so we can binary search to get
         * the first possible interval, then only compare their start times.
         */
        ITmfStateInterval entry;
        for (int i = binarySearchEndTime(intervals, t); i < intervals.size(); i++) {
            entry = intervals.get(i);
            if (entry.getStartTime() <= t) {
                /* Add this interval to the returned values */
                currentStateInfo.set(entry.getAttribute(), entry);
            }
        }
    }

    @Override
    public ITmfStateInterval doSingularQuery(long t, int attributeQuark)
            throws TimeRangeException, AttributeNotFoundException {
        if (!checkValidTime(t)) {
            throw new TimeRangeException();
        }

        /*
         * The intervals are sorted by end time, so we can binary search to get
         * the first possible interval, then only compare their start times.
         */
        ITmfStateInterval entry;
        for (int i = binarySearchEndTime(intervals, t); i < intervals.size(); i++) {
            entry = intervals.get(i);
            if (entry.getStartTime() <= t && entry.getAttribute() == attributeQuark) {
                    /* This is the droid we are looking for */
                    return entry;
                }
        }
        throw new AttributeNotFoundException();
    }

    @Override
    public boolean checkValidTime(long t) {
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

    @Override
    public void debugPrint(PrintWriter writer) {
        writer.println(intervals.toString());
    }

    private static int binarySearchEndTime(List<ITmfStateInterval> list, long time) {
        ITmfStateInterval dummyInterval = new TmfStateInterval(-1, time, -1, null);
        int mid = Collections.binarySearch(list, dummyInterval, END_COMPARATOR);

        /* The returned value is < 0 if the exact key was not found. */
        if (mid < 0) {
            mid = -mid - 1;
        }

        /*
         * Collections.binarySearch doesn't guarantee which element is returned
         * if it falls on one of many equal ones. So make sure we are at the
         * first one provided.
         */
        while ((mid > 0) &&
                (list.get(mid).getEndTime() == list.get(mid-1).getEndTime())) {
            mid--;
        }
        return mid;
    }

}
