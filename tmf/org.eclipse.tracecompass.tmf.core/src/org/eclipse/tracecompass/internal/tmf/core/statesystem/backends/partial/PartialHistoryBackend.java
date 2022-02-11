/*******************************************************************************
 * Copyright (c) 2013, 2016 Ericsson
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Patrick Tasse - Add message to exceptions
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.statesystem.backends.partial;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNullContents;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.datastore.core.condition.IntegerRangeCondition;
import org.eclipse.tracecompass.internal.provisional.datastore.core.condition.TimeRangeCondition;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.backend.IPartialStateHistoryBackend;
import org.eclipse.tracecompass.statesystem.core.backend.IStateHistoryBackend;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Partial state history back-end.
 *
 * This is a shim inserted between the real state system and a "real" history
 * back-end. It will keep checkpoints, every n trace events (where n is called
 * the granularity) and will only forward to the real state history the state
 * intervals that crosses at least one checkpoint. Every other interval will
 * be discarded.
 *
 * This would mean that it can only answer queries exactly at the checkpoints.
 * For any other timestamps (ie, most of the time), it will load the closest
 * earlier checkpoint, and will re-feed the state-change-input with events from
 * the trace, to restore the real state at the time that was requested.
 *
 * @author Alexandre Montplaisir
 */
public class PartialHistoryBackend implements IStateHistoryBackend {

    private final @NonNull String fSSID;

    /**
     * A partial history needs the state input plugin to re-generate state
     * between checkpoints.
     */
    private final @NonNull ITmfStateProvider fPartialInput;

    /**
     * Fake state system that is used for partially rebuilding the states (when
     * going from a checkpoint to a target query timestamp).
     */
    private final @NonNull PartialStateSystem fPartialSS;

    /** Reference to the "real" state history that is used for storage */
    private final @NonNull IStateHistoryBackend fInnerHistory;

    /** Checkpoints map, <Timestamp, Rank in the trace> */
    private final @NonNull TreeMap<Long, Long> fCheckpoints = new TreeMap<>();

    /** Latch tracking if the initial checkpoint registration is done */
    private final @NonNull CountDownLatch fCheckpointsReady = new CountDownLatch(1);

    private final long fGranularity;

    private long fLatestTime;

    private final NavigableSet<@NonNull ITmfStateInterval> fCurrentIntervals;

    private final IPartialStateHistoryBackend fBackend;

    /**
     * Constructor
     *
     * @param ssid
     *            The state system's ID
     * @param partialInput
     *            The state change input object that was used to build the
     *            upstream state system. This partial history will make its own
     *            copy (since they have different targets).
     * @param pss
     *            The partial history's inner state system. It should already be
     *            assigned to partialInput.
     * @param realBackend
     *            The real state history back-end to use. It's supposed to be
     *            modular, so it should be able to be of any type.
     * @param granularity
     *            Configuration parameter indicating how many trace events there
     *            should be between each checkpoint
     * @param backend
     *            The backend used for storage
     */
    public PartialHistoryBackend(@NonNull String ssid,
            ITmfStateProvider partialInput,
            PartialStateSystem pss,
            IStateHistoryBackend realBackend,
            long granularity,
            @NonNull IPartialStateHistoryBackend backend) {
        if (granularity <= 0 || partialInput == null || pss == null ||
                partialInput.getAssignedStateSystem() != pss) {
            throw new IllegalArgumentException();
        }

        final long startTime = realBackend.getStartTime();

        fSSID = ssid;
        fPartialInput = partialInput;
        fPartialSS = pss;

        fInnerHistory = realBackend;
        fGranularity = granularity;
        fBackend = backend;

        /**
         * We need to compare the end time and the attribute, because we can
         * have 2 intervals with the same end time (for different attributes).
         * And TreeSet needs a unique "key" per element.
         */
        this.fCurrentIntervals = new TreeSet<>(Comparator
                .comparing(ITmfStateInterval::getEndTime)
                .thenComparing(ITmfStateInterval::getAttribute));
        fLatestTime = startTime;

        registerCheckpoints();
    }

    private void registerCheckpoints() {
        ITmfEventRequest request = new CheckpointsRequest(fPartialInput, fCheckpoints);
        fPartialInput.getTrace().sendRequest(request);
        /* The request will countDown the checkpoints latch once it's finished */
    }

    @Override
    public String getSSID() {
        return fSSID;
    }

    @Override
    public long getStartTime() {
        return fInnerHistory.getStartTime();
    }

    @Override
    public long getEndTime() {
        return fLatestTime;
    }

    @Override
    public void insertPastState(long stateStartTime, long stateEndTime,
            int quark, Object value) throws TimeRangeException {
        waitForCheckpoints();

        /* Update the latest time */
        if (stateEndTime > fLatestTime) {
            fLatestTime = stateEndTime;
        }

        /*
         * Check if the interval intersects the previous checkpoint. If so,
         * insert it in the real history back-end.
         *
         * FIXME since intervals are inserted in order of rank, we could avoid
         * doing a map lookup every time here (just compare with the known
         * previous one).
         */
        if (stateStartTime <= fCheckpoints.floorKey(stateEndTime)) {
            fInnerHistory.insertPastState(stateStartTime, stateEndTime, quark, value);
        }
    }

    @Override
    public void finishedBuilding(long endTime) throws TimeRangeException {
        fInnerHistory.finishedBuilding(endTime);
    }

    @Override
    public FileInputStream supplyAttributeTreeReader() {
        return fInnerHistory.supplyAttributeTreeReader();
    }

    @Override
    public File supplyAttributeTreeWriterFile() {
        return fInnerHistory.supplyAttributeTreeWriterFile();
    }

    @Override
    public long supplyAttributeTreeWriterFilePosition() {
        return fInnerHistory.supplyAttributeTreeWriterFilePosition();
    }

    @Override
    public void removeFiles() {
        fInnerHistory.removeFiles();
    }

    @Override
    public void dispose() {
        fPartialInput.dispose();
        fPartialSS.dispose();
        fInnerHistory.dispose();
    }

    @Override
    public void doQuery(List<@Nullable ITmfStateInterval> currentStateInfo, long t)
            throws TimeRangeException, StateSystemDisposedException {
        /* Wait for required steps to be done */
        waitForCheckpoints();
        fPartialSS.getUpstreamSS().waitUntilBuilt();

        if (!checkValidTime(t)) {
            throw new TimeRangeException("Invalid timestamp caused a TimeRangeException: " + fSSID + " Time:" + t + ", Start:" + getStartTime() + ", End:" + getEndTime()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        /* Reload the previous checkpoint */
        long checkpointTime1 = fCheckpoints.floorKey(t);
        fInnerHistory.doQuery(currentStateInfo, checkpointTime1);
        if (checkpointTime1 == t) {
            /* We have all the intervals we need at the checkpoint */
            return;
        }

        /*
         * Set the initial contents of the partial state system (which is the
         * contents of the query at the checkpoint).
         */
        List<@NonNull ITmfStateInterval> filledStateInfo =
                checkNotNullContents(currentStateInfo.stream()).collect(Collectors.toList());

        fPartialSS.takeQueryLock();
        fPartialSS.replaceOngoingState(filledStateInfo);

        /*
         * Send an event request to update the state system to the target time. We will
         * continue reading the trace until the checkpoint succeeding t so that we get
         * the end times of the intervals that end after t
         */

        /* Reload the next checkpoint */
        long checkpointTime2 = 0;
        if (fCheckpoints.ceilingKey(t) != null) {
            checkpointTime2 = fCheckpoints.ceilingKey(t);
        }
        else {
            /*
             * In case there is no other checkpoint because we are close to the
             * end of the trace
             */
            checkpointTime2 = fPartialInput.getTrace().getEndTime().toNanos();
        }

        /*
         * Send an event request to update the state system.
         */
        TmfTimeRange range = new TmfTimeRange(
                /*
                 * The state at the checkpoint already includes any state change
                 * caused by the event(s) happening exactly at 'checkpointTime',
                 * if any. We must not include those events in the query.
                 */
                TmfTimestamp.fromNanos(checkpointTime1 + 1),
                TmfTimestamp.fromNanos(checkpointTime2));
        ITmfEventRequest request = new PartialStateSystemRequest(fPartialInput, range);
        fPartialInput.getTrace().sendRequest(request);

        try {
            request.waitForCompletion();
        } catch (InterruptedException e) {
            // Do nothing
        }

        /*
         * Now, we have the intervals with their real end times written to the
         * backend, we should be able to get them from there
         */
        List<ITmfStateInterval> intervalsList = ((ITmfStateSystem) fPartialSS).queryFullState(t);

        for (int i = 0; i < currentStateInfo.size(); i++) {
            ITmfStateInterval interval = currentStateInfo.get(i);
            if (interval != null) {
                if (interval.getEndTime() < intervalsList.get(i).getEndTime()) {
                    currentStateInfo.set(i, intervalsList.get(i));
                }
            }
        }

        /*
         * Querying the partial history at the UpperCheckpoint in order to add
         * the intervals that end a this moment. we need to check first that the
         * upper checkpoint exists
         */
        if (fCheckpoints.ceilingKey(t) != null) {
            intervalsList = prepareIntervalList(((ITmfStateSystem) fPartialSS).getNbAttributes());
            filledStateInfo.clear();
            try {
                fInnerHistory.doQuery(intervalsList, checkpointTime2);
            } catch (StateSystemDisposedException e) {
                e.printStackTrace();
            }

            filledStateInfo = checkNotNullContents(intervalsList.stream()).collect(Collectors.toList());

            /*
             * Adding the intervals that intersect the timestamp t
             */

            for (int i = 0; i < currentStateInfo.size(); i++) {
                ITmfStateInterval actualInterval = currentStateInfo.get(i);
                ITmfStateInterval newInterval = filledStateInfo.get(i);
                if (actualInterval != null) {
                    if (actualInterval.getEndTime() < newInterval.getEndTime() && newInterval.intersects(t)) {
                        currentStateInfo.set(i, newInterval);
                    }
                }

            }
        }

        fPartialSS.releaseQueryLock();
    }

    private static List<@Nullable ITmfStateInterval> prepareIntervalList(int nbAttrib) {
        List<@Nullable ITmfStateInterval> intervalsList = new ArrayList<ITmfStateInterval>(Collections.nCopies(nbAttrib, null));
        return intervalsList;
    }

    @Override
    public ITmfStateInterval doSingularQuery(long t, int attributeQuark) throws TimeRangeException, StateSystemDisposedException {

        waitForCheckpoints();
        fPartialSS.getUpstreamSS().waitUntilBuilt();

        if (!checkValidTime(t)) {
            throw new TimeRangeException("Invalid timestamp caused a TimeRangeException: " + fSSID + " Time:" + t + ", Start:" + getStartTime() + ", End:" + getEndTime()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        /* Reload the previous checkpoint */
        long checkpointTime = fCheckpoints.floorKey(t);
        int nbAtributes = ((ITmfStateSystem)fPartialSS).getNbAttributes();
        List<ITmfStateInterval> intervalsList = prepareIntervalList(nbAtributes);

        /* Checking if the interval was stored in the real backend */
        fInnerHistory.doQuery(intervalsList, checkpointTime);
        ITmfStateInterval ret = intervalsList.get(attributeQuark);

        if (ret == null || !ret.intersects(t)) {
            /* The interval was not stored on disk, read it from the trace then */
            intervalsList = prepareIntervalList(nbAtributes);
            this.doQuery(intervalsList, t);
            ret = intervalsList.get(attributeQuark);
        }
        return ret;
    }

    @Override
    public Iterable<@NonNull ITmfStateInterval> query2D(IntegerRangeCondition quarks, TimeRangeCondition times)
            throws TimeRangeException {
        /* Clearing the old intervals*/
        fCurrentIntervals.clear();

        waitForCheckpoints();
        fPartialSS.getUpstreamSS().waitUntilBuilt();

        /* Updating the backend with the quarks and time ranges */
        synchronized (fBackend) {
            fBackend.updateRangeCondition(quarks);
            fBackend.updateTimeCondition(times);
            /*
             * Tell the backend that the request is a 2D in order to filter the
             * intervals to write
             */
            fBackend.updateQueryType(true);

        }

        /*
         * Getting the lower and upper checkpoint timestamps that bound the time
         * range condition
         */
        long min = times.min();
        long max = times.max();
        long lowerCheckpoint = fCheckpoints.floorKey(min);
        long upperCheckpoint = max;
        if (fCheckpoints.ceilingKey(max) != null) {
            upperCheckpoint = fCheckpoints.ceilingKey(max);
        }

        /* Querying the partial history at the lowerCheckpoint */
        List<@Nullable ITmfStateInterval> currentStateInfo = prepareIntervalList(((ITmfStateSystem)fPartialSS).getNbAttributes());
        try {
            fInnerHistory.doQuery(currentStateInfo, lowerCheckpoint);
        } catch (StateSystemDisposedException e) {
            e.printStackTrace();
        }

        List<@NonNull ITmfStateInterval> filledStateInfo =
                checkNotNullContents(currentStateInfo.stream()).collect(Collectors.toList());

        /*
         * Adding the intervals that satisfy the query condition to its output
         */
        for (ITmfStateInterval interval : filledStateInfo) {
            if (quarks.test(interval.getAttribute()) && times.intersects(interval.getStartTime(), interval.getEndTime())) {
                fCurrentIntervals.add(interval);
            }
        }
        try {

            fPartialSS.takeQueryLock();
            fPartialSS.replaceOngoingState(filledStateInfo);

            /*
             * Reading the trace updating the state until the upperCheckpoint to
             * get the missing intervals
             */
            TmfTimeRange range = new TmfTimeRange(TmfTimestamp.fromNanos(lowerCheckpoint), TmfTimestamp.fromNanos(upperCheckpoint));

            ITmfEventRequest request = new PartialStateSystemRequest(fPartialInput, range);
            fPartialInput.getTrace().sendRequest(request);

            try {
                request.waitForCompletion();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            /* Converting the quarks condition into a collection of quarks */
            List<Integer> quarksColletion = new ArrayList<>();
            for (int value = quarks.min(); value <= quarks.max(); value++) {
                if (quarks.test(value)) {
                    quarksColletion.add(value);
                }
            }

            /*
             * Querying the intervals read from the trace and adding them to the
             * output
             */
            Logger logger = Logger.getAnonymousLogger();
            try {
                synchronized (fPartialSS) {
                    for (ITmfStateInterval interval : ((ITmfStateSystem) fPartialSS).query2D(quarksColletion, min, max)) {
                        fCurrentIntervals.add(interval);
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                logger.log(Level.SEVERE,"An IndexOutOfBoundsException exception occurred", e);
            } catch (StateSystemDisposedException e) {
                logger.log(Level.SEVERE,"A StateSystemDisposedException exception occurred", e);
            }
        } finally {
            fPartialSS.releaseQueryLock();
        }

        /*
         * Querying the partial history at the UpperCheckpoint in order to add
         * the intervals that end a this moment. we need to check first that the
         * upper checkpoint exists
         */
        if (fCheckpoints.ceilingKey(max) != null) {
            currentStateInfo.clear();
            filledStateInfo.clear();
            try {
                fInnerHistory.doQuery(currentStateInfo, upperCheckpoint);
            } catch (StateSystemDisposedException e) {
                e.printStackTrace();
            }

            filledStateInfo = checkNotNullContents(currentStateInfo.stream()).collect(Collectors.toList());

            /*
             * Adding the intervals that satisfy the query condition to its
             * output
             */
            for (ITmfStateInterval interval : filledStateInfo) {
                if (quarks.test(interval.getAttribute()) && times.intersects(interval.getStartTime(), interval.getEndTime())) {
                    fCurrentIntervals.add(interval);
                }
            }
        }
        try {
            return fCurrentIntervals;

        } finally {

            synchronized (fBackend) {
                fBackend.updateQueryType(false);
            }
        }
    }

    private boolean checkValidTime(long t) {
        return (t >= getStartTime() && t <= getEndTime());
    }

    private void waitForCheckpoints() {
        try {
            fCheckpointsReady.await();
        } catch (InterruptedException e) {
            // Do nothing
        }
    }

    // ------------------------------------------------------------------------
    // Event requests types
    // ------------------------------------------------------------------------

    private class CheckpointsRequest extends TmfEventRequest {
        private final ITmfTrace trace;
        private final Map<Long, Long> checkpts;
        private long eventCount;
        private long lastCheckpointAt;

        public CheckpointsRequest(ITmfStateProvider input, Map<Long, Long> checkpoints) {
            super(ITmfEvent.class,
                    TmfTimeRange.ETERNITY,
                    0,
                    ITmfEventRequest.ALL_DATA,
                    ITmfEventRequest.ExecutionType.FOREGROUND);
            checkpoints.clear();
            this.trace = input.getTrace();
            this.checkpts = checkpoints;
            eventCount = 0;
            lastCheckpointAt = 0;

            /* Insert a checkpoint at the start of the trace */
            checkpoints.put(input.getStartTime(), 0L);
        }

        @Override
        public void handleData(final ITmfEvent event) {
            super.handleData(event);
            if (event.getTrace() == trace) {
                eventCount++;

                /* Check if we need to register a new checkpoint */
                if (eventCount >= lastCheckpointAt + fGranularity) {
                    checkpts.put(event.getTimestamp().getValue(), eventCount);
                    lastCheckpointAt = eventCount;
                }
            }
        }

        @Override
        public void handleCompleted() {
            super.handleCompleted();
            fCheckpointsReady.countDown();
        }
    }

    private class PartialStateSystemRequest extends TmfEventRequest {
        private final ITmfStateProvider sci;
        private final ITmfTrace trace;

        PartialStateSystemRequest(ITmfStateProvider sci, TmfTimeRange range) {
            super(ITmfEvent.class,
                    range,
                    0,
                    ITmfEventRequest.ALL_DATA,
                    ITmfEventRequest.ExecutionType.BACKGROUND);
            this.sci = sci;
            this.trace = sci.getTrace();
        }

        @Override
        public void handleData(final ITmfEvent event) {
            super.handleData(event);
            if (event.getTrace() == trace) {
                sci.processEvent(event);
            }
        }

        @Override
        public void handleCompleted() {
            /*
             * If we're using a threaded state provider, we need to make sure
             * all events have been handled by the state system before doing
             * queries on it.
             */
            if (fPartialInput instanceof AbstractTmfStateProvider) {
                ((AbstractTmfStateProvider) fPartialInput).waitForEmptyQueue();
            }
            super.handleCompleted();
        }

    }
}
