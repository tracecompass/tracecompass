/*******************************************************************************
 * Copyright (c) 2012, 2016 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.statesystem.core.backend.historytree;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.common.core.collect.BufferedBlockingQueue;
import org.eclipse.tracecompass.common.core.log.TraceCompassLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils;
import org.eclipse.tracecompass.internal.provisional.datastore.core.condition.IntegerRangeCondition;
import org.eclipse.tracecompass.internal.provisional.datastore.core.condition.TimeRangeCondition;
import org.eclipse.tracecompass.internal.statesystem.core.Activator;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;

import com.google.common.collect.Iterables;

/**
 * Variant of the HistoryTreeBackend which runs all the interval-insertion logic
 * in a separate thread.
 *
 * @author Alexandre Montplaisir
 */
public final class ThreadedHistoryTreeBackend extends HistoryTreeBackend
        implements Runnable {

    private static final @NonNull Logger LOGGER = TraceCompassLog.getLogger(ThreadedHistoryTreeBackend.class);

    private static final int CHUNK_SIZE = 127;
    private final @NonNull BufferedBlockingQueue<HTInterval> intervalQueue;
    private final @NonNull Thread shtThread;
    /**
     * The backend tracks its end time separately from the tree, to take into
     * consideration intervals in the queue.
     */
    private long fEndTime;

    /**
     * New state history constructor
     *
     * Note that it usually doesn't make sense to use a Threaded HT if you're
     * opening an existing state-file, but you know what you're doing...
     *
     * @param ssid
     *            The state system's id
     * @param newStateFile
     *            The name of the history file that will be created. Should end
     *            in ".ht"
     * @param providerVersion
     *            Version of of the state provider. We will only try to reopen
     *            existing files if this version matches the one in the
     *            framework.
     * @param startTime
     *            The earliest timestamp stored in the history
     * @param queueSize
     *            The size of the interval insertion queue. 2000 - 10000 usually
     *            works well
     * @param blockSize
     *            The size of the blocks in the file
     * @param maxChildren
     *            The maximum number of children allowed for each core node
     * @throws IOException
     *             If there was a problem opening the history file for writing
     */
    public ThreadedHistoryTreeBackend(@NonNull String ssid,
            File newStateFile,
            int providerVersion,
            long startTime,
            int queueSize,
            int blockSize,
            int maxChildren)
                    throws IOException {
        super(ssid, newStateFile, providerVersion, startTime, blockSize, maxChildren);
        fEndTime = startTime;

        intervalQueue = new BufferedBlockingQueue<>(queueSize / CHUNK_SIZE, CHUNK_SIZE);
        shtThread = new Thread(this, "History Tree Thread"); //$NON-NLS-1$
        shtThread.start();
    }

    /**
     * New State History constructor. This version provides default values for
     * blockSize and maxChildren.
     *
     * @param ssid
     *            The state system's id
     * @param newStateFile
     *            The name of the history file that will be created. Should end
     *            in ".ht"
     * @param providerVersion
     *            Version of of the state provider. We will only try to reopen
     *            existing files if this version matches the one in the
     *            framework.
     * @param startTime
     *            The earliest timestamp stored in the history
     * @param queueSize
     *            The size of the interval insertion queue. 2000 - 10000 usually
     *            works well
     * @throws IOException
     *             If there was a problem opening the history file for writing
     */
    public ThreadedHistoryTreeBackend(@NonNull String ssid,
            File newStateFile,
            int providerVersion,
            long startTime,
            int queueSize)
                    throws IOException {
        super(ssid, newStateFile, providerVersion, startTime);
        fEndTime = startTime;

        intervalQueue = new BufferedBlockingQueue<>(queueSize / CHUNK_SIZE, CHUNK_SIZE);
        shtThread = new Thread(this, "History Tree Thread"); //$NON-NLS-1$
        shtThread.start();
    }

    /*
     * The Threaded version does not specify an "existing file" constructor,
     * since the history is already built (and we only use the other thread
     * during building). Just use a plain HistoryTreeProvider in this case.
     *
     * TODO but what about streaming??
     */

    @Deprecated
    @Override
    public void insertPastState(long stateStartTime, long stateEndTime,
            int quark, ITmfStateValue value) throws TimeRangeException {
        /*
         * Here, instead of directly inserting the elements in the History Tree
         * underneath, we'll put them in the Queue. They will then be taken and
         * processed by the other thread executing the run() method.
         */
        insertPastState(stateStartTime, stateEndTime, quark, value.unboxValue());
    }

    @Override
    public void insertPastState(long stateStartTime, long stateEndTime,
            int quark, Object value) throws TimeRangeException {
        /*
         * Here, instead of directly inserting the elements in the History Tree
         * underneath, we'll put them in the Queue. They will then be taken and
         * processed by the other thread executing the run() method.
         */
        HTInterval interval = new HTInterval(stateStartTime, stateEndTime,
                quark, value);
        intervalQueue.put(interval);
        fEndTime = Math.max(fEndTime, stateEndTime);
    }

    @Override
    public long getEndTime() {
        return fEndTime;
    }

    @Override
    public void finishedBuilding(long endTime) {
        /*
         * We need to commit everything in the History Tree and stop the
         * standalone thread before returning to the StateHistorySystem. (SHS
         * will then write the Attribute Tree to the file, that must not happen
         * at the same time we are writing the last nodes!)
         */

        stopRunningThread(endTime);
        setFinishedBuilding(true);
        return;
    }

    @Override
    public void dispose() {
        if (!isFinishedBuilding()) {
            stopRunningThread(Long.MAX_VALUE);
        }
        /*
         * isFinishedBuilding remains false, so the superclass will ask the
         * back-end to delete the file.
         */
        super.dispose();
    }

    private void stopRunningThread(long endTime) {
        if (!shtThread.isAlive()) {
            return;
        }

        /*
         * Send a "poison pill" in the queue, then wait for the HT to finish its
         * closeTree()
         */
        try {
            HTInterval pill = new HTInterval(Long.MIN_VALUE, endTime, -1, TmfStateValue.nullValue());
            intervalQueue.put(pill);
            intervalQueue.flushInputBuffer();
            shtThread.join();
        } catch (TimeRangeException e) {
            Activator.getDefault().logError("Error closing state system", e); //$NON-NLS-1$
        } catch (InterruptedException e) {
            Activator.getDefault().logError("State system interrupted", e); //$NON-NLS-1$
        }
    }

    @Override
    public void run() {
        try {
            HTInterval currentInterval = intervalQueue.blockingPeek();
            while (currentInterval.getStartTime() != Long.MIN_VALUE) {
                /* Send the interval to the History Tree */
                getSHT().insertInterval(currentInterval);
                /* Actually remove the interval from the queue */
                // FIXME Replace with remove() once it is implemented.
                intervalQueue.take();
                currentInterval = intervalQueue.blockingPeek();
            }
            if (currentInterval.getAttribute() != -1) {
                /* Make sure this is the "poison pill" we are waiting for */
                throw new IllegalStateException();
            }
            /*
             * We've been told we're done, let's write down everything and quit.
             * The end time of this "signal interval" is actually correct.
             */
            getSHT().closeTree(currentInterval.getEndTime());
        } catch (TimeRangeException e) {
            /* This should not happen */
            Activator.getDefault().logError("Error starting the state system", e); //$NON-NLS-1$
        }
    }

    // ------------------------------------------------------------------------
    // Query methods
    // ------------------------------------------------------------------------

    @Override
    public void doQuery(List<ITmfStateInterval> currentStateInfo, long t)
            throws TimeRangeException, StateSystemDisposedException {
        super.doQuery(currentStateInfo, t);

        if (isFinishedBuilding()) {
            /*
             * The history tree is the only place to look for intervals once
             * construction is finished.
             */
            return;
        }

        /*
         * It is possible we may have missed some intervals due to them being in
         * the queue while the query was ongoing. Go over the results to see if
         * we missed any.
         */
        for (int i = 0; i < currentStateInfo.size(); i++) {
            if (currentStateInfo.get(i) == null) {
                /* Query the missing interval via "unicast" */
                ITmfStateInterval interval = doSingularQuery(t, i);
                currentStateInfo.set(i, interval);
            }
        }
    }

    @Override
    public ITmfStateInterval doSingularQuery(long t, int attributeQuark)
            throws TimeRangeException, StateSystemDisposedException {
        ITmfStateInterval ret = super.doSingularQuery(t, attributeQuark);
        if (ret != null) {
            return ret;
        }

        /*
         * We couldn't find the interval in the history tree. It's possible that
         * it is currently in the intervalQueue. Look for it there. Note that
         * BufferedBlockingQueue's iterator() is thread-safe (no need to lock
         * the queue).
         */
        for (ITmfStateInterval interval : intervalQueue) {
            if (interval.getAttribute() == attributeQuark && interval.intersects(t)) {
                return interval;
            }
        }

        /*
         * If we missed it again, it's because it got inserted in the tree
         * *while we were iterating* on the queue. One last pass in the tree
         * should find it.
         *
         * This case is really rare, which is why we do a second pass at the end
         * if needed, instead of systematically checking in the queue first
         * (which is slow).
         */
        return super.doSingularQuery(t, attributeQuark);
    }

    @Override
    public Iterable<@NonNull ITmfStateInterval> query2D(IntegerRangeCondition quarks, TimeRangeCondition times)
            throws TimeRangeException {
        try (TraceCompassLogUtils.ScopeLog log = new TraceCompassLogUtils.ScopeLog(LOGGER, Level.FINEST, "ThreadedHistoryTreeBackend:query2D", //$NON-NLS-1$
                "ssid", getSSID(), //$NON-NLS-1$
                "quarks", quarks, //$NON-NLS-1$
                "timeCondition", times)) { //$NON-NLS-1$
            /*
             * There can still be intervals in the queue, search the
             * HistoryTreeBackend, then the queue for the intervals we need.
             * Iterables will lazily evaluate the BBQ only once the
             * HistoryTreeBackend is consumed and if the construction still
             * isn't done.
             */
            Iterable<@NonNull HTInterval> queuedIntervals = Iterables.filter(intervalQueue,
                    interval -> !isFinishedBuilding() && quarks.test(interval.getAttribute())
                            && times.intersects(interval.getStartTime(), interval.getEndTime()));
            return Iterables.concat(super.query2D(quarks, times), queuedIntervals);
        }
    }
}
