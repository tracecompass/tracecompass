/*******************************************************************************
 * Copyright (c) 2022 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.statesystem.backends.partial;

import java.io.File;
import java.io.FileInputStream;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.common.core.log.TraceCompassLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils;
import org.eclipse.tracecompass.internal.provisional.datastore.core.condition.IntegerRangeCondition;
import org.eclipse.tracecompass.internal.provisional.datastore.core.condition.TimeRangeCondition;
import org.eclipse.tracecompass.statesystem.core.backend.IPartialStateHistoryBackend;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.interval.TmfStateInterval;
import com.google.common.collect.Iterables;

/**
 * State history back-end that stores its intervals in RAM only. This back-end
 * is similar to the InMemoryBackend, except that that one saves only the intervals
 * that meet the quarks range condition and the time range condition of the query2D(),
 * this will filter the intervals then and reduce the required RAM memory. This version
 * of the InMemoryBackend has been created especially for the partial state history
 * so that we can use it to save the missing intervals between the checkpoints in
 * memory before returning them in the query2D() output.
 *
 * @author Abdellah Rahmani
 */
public class PartialInMemoryBackend implements IPartialStateHistoryBackend {

    private static final @NonNull Logger LOGGER = TraceCompassLog.getLogger(PartialInMemoryBackend.class);

    private final @NonNull String fSSID;
    private final NavigableSet<@NonNull ITmfStateInterval> fCurrentIntervals;
    private final long fStartTime;
    public static IntegerRangeCondition fRangeCondition;
    public static TimeRangeCondition fTimeCondition;
    public static boolean fIs2DQuery;
    private volatile long fLatestTime;


    /**
     * Constructor
     *
     * @param ssid
     *            The state system's ID
     * @param startTime
     *            The start time of this interval store
     */
    public PartialInMemoryBackend(@NonNull String ssid, long startTime) {
        fSSID = ssid;
        fStartTime = startTime;
        fLatestTime = startTime;
        /**
         * We need to compare the end time and the attribute, because we can
         * have 2 intervals with the same end time (for different attributes).
         * And TreeSet needs a unique "key" per element.
         */
        fCurrentIntervals = new TreeSet<>(Comparator
                .comparing(ITmfStateInterval::getEndTime)
                .thenComparing(ITmfStateInterval::getAttribute));
        fRangeCondition = null;
        fTimeCondition = null;
        fIs2DQuery = false;
    }

    @Override
    public String getSSID() {
        return fSSID;
    }

    @Override
    public long getStartTime() {
        return fStartTime;
    }

    @Override
    public long getEndTime() {
        return fLatestTime;
    }

    @Override
    public void insertPastState(long stateStartTime, long stateEndTime,
            int quark, Object value) throws TimeRangeException {

        /* Make sure the passed start/end times make sense */
        if (stateStartTime > stateEndTime || stateStartTime < fStartTime) {
            throw new TimeRangeException("Invalid timestamp caused a TimeRangeException: " + fSSID + " Interval Start:" + stateStartTime + ", Interval End:" + stateEndTime + ", Backend Start:" + fStartTime); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        ITmfStateInterval interval = new TmfStateInterval(stateStartTime, stateEndTime, quark, value);

        /*
         * Add the interval into the tree if it meets the time and quarks range
         * conditions in case of a query 2D, or add all the intervals if the
         * query is from a different type like queryFullState()
         */
        synchronized (fCurrentIntervals) {
            if ((fRangeCondition != null && fTimeCondition != null && fRangeCondition.test(interval.getAttribute())
                    && fTimeCondition.intersects(interval.getStartTime(), interval.getEndTime())) || !fIs2DQuery) {
                fCurrentIntervals.add(interval);
            }
        }

        /* Update the "latest seen time" */
        if (stateEndTime > fLatestTime) {
            fLatestTime = stateEndTime;
        }
    }

    @Override
    public void doQuery(List<ITmfStateInterval> currentStateInfo, long t)
            throws TimeRangeException {
        if (!checkValidTime(t)) {
            throw new TimeRangeException("Invalid timestamp caused a TimeRangeException: " + fSSID + " Time:" + t + ", Start:" + fStartTime + ", End:" + fLatestTime); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        /*
         * The intervals are sorted by end time, so we can binary search to get
         * the first possible interval, then only compare their start times.
         */
        synchronized (fCurrentIntervals) {
            Iterator<ITmfStateInterval> iter = searchforEndTime(fCurrentIntervals, 0, t).iterator();
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
            throw new TimeRangeException("Invalid timestamp caused a TimeRangeException: " + fSSID + " Time:" + t + ", Start:" + fStartTime + ", End:" + fLatestTime); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        /*
         * The intervals are sorted by end time, so we can binary search to get
         * the first possible interval, then only compare their start times.
         */
        synchronized (fCurrentIntervals) {
            Iterable<ITmfStateInterval> iter = searchforEndTime(fCurrentIntervals, attributeQuark, t);
            for (ITmfStateInterval entry : iter) {
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
        return (t >= fStartTime && t <= fLatestTime);
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

    private static Iterable<@NonNull ITmfStateInterval> searchforEndTime(NavigableSet<@NonNull ITmfStateInterval> tree, int quark, long time) {
        ITmfStateInterval dummyInterval = new TmfStateInterval(-1, time, quark, (Object) null);
        return tree.tailSet(dummyInterval);
    }

    @Override
    public Iterable<@NonNull ITmfStateInterval> query2D(IntegerRangeCondition quarks, TimeRangeCondition times)
            throws TimeRangeException {
        try (TraceCompassLogUtils.ScopeLog log = new TraceCompassLogUtils.ScopeLog(LOGGER, Level.FINER, "InMemoryBackend:query2D", //$NON-NLS-1$
                "ssid", getSSID(), //$NON-NLS-1$
                "quarks", quarks, //$NON-NLS-1$
                "times", times)) { //$NON-NLS-1$
            synchronized (fCurrentIntervals) {
                return Iterables.filter(searchforEndTime(fCurrentIntervals, quarks.min(), times.min()),
                        interval -> quarks.test(interval.getAttribute())
                                && times.intersects(interval.getStartTime(), interval.getEndTime()));
            }
        }
    }

    @Override
    public void updateRangeCondition(IntegerRangeCondition range) {
        fRangeCondition = range;
    }

    @Override
    public void updateTimeCondition(TimeRangeCondition timeConditionrange) {
        fTimeCondition = timeConditionrange;
    }

    @Override
    public void updateQueryType(boolean type) {
        fIs2DQuery = type;
    }

}
