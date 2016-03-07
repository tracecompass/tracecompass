/*******************************************************************************
 * Copyright (c) 2016 Ericsson, EfficiOS Inc. and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.tests.backend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.statesystem.core.backend.IStateHistoryBackend;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.interval.TmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.junit.Test;

/**
 * Abstract class to test implementations of the {@link IStateHistoryBackend} interface.
 *
 * @author Patrick Tasse
 * @author Alexandre Montplaisir
 */
public abstract class StateHistoryBackendTestBase {

    /**
     * Gets the backend to be used for building.
     *
     * @param startTime
     *            The start time of the history
     *
     * @return The backend to be used for building.
     * @throws IOException
     *             if an exception occurs
     */
    protected abstract IStateHistoryBackend getBackendForBuilding(long startTime) throws IOException;

    /**
     * Gets the backend to be used for querying. The default implementation
     * returns the backend that was used for building.
     * <p>
     * Only the returned backend should be used after calling this method. The
     * one sent in parameter might have been disposed.
     *
     * @param backend
     *            The backend that was used for building
     * @return The backend to be used for querying.
     * @throws IOException
     *             if an exception occurs
     */
    @SuppressWarnings("unused")
    protected IStateHistoryBackend getBackendForQuerying(IStateHistoryBackend backend) throws IOException {
        return backend;
    }

    /**
     * Prepares a backend to be used in tests. The specified intervals will be
     * inserted in the backend, and then the backend will be closed.
     *
     * @param startTime
     *            The start time of the history
     * @param endTime
     *            The end time at which to close the history
     * @param intervals
     *            The intervals to insert in the history backend
     * @return The backend to be used for querying.
     */
    protected final @Nullable IStateHistoryBackend prepareBackend(long startTime, long endTime,
            List<ITmfStateInterval> intervals) {

        try {
            IStateHistoryBackend backend = getBackendForBuilding(startTime);
            insertIntervals(backend, intervals);
            backend.finishedBuilding(Math.max(endTime, backend.getEndTime()));
            return getBackendForQuerying(backend);
        } catch (IOException e) {
            fail(e.getMessage());
            return null;
        }
    }

    /**
     * Insert the specified intervals in the provided backend.
     *
     * @param backend
     *            The backend to be used
     * @param intervals
     *            The intervals to insert in the history backend
     */
    protected static void insertIntervals(IStateHistoryBackend backend, List<ITmfStateInterval> intervals) {
        for (ITmfStateInterval interval : intervals) {
            backend.insertPastState(interval.getStartTime(), interval.getEndTime(), interval.getAttribute(), interval.getStateValue());
        }
    }

    /**
     * Test the integrity of a backend by first building the backend with the
     * specified intervals, closing it, and then querying at every single
     * timestamp, making sure that all returned intervals intersect with the
     * query time. The backend start and end time will be checked.
     * <p>
     * If <code>allowNull</code> is false, the specified intervals must cover
     * the full range for all attributes. The method will make sure that no null
     * intervals are returned.
     *
     * @param startTime
     *            The start time of the history
     * @param endTime
     *            The end time of the history
     * @param nbAttr
     *            The number of attributes
     * @param intervals
     *            The list of intervals to insert
     * @param allowNull
     *            True if null intervals are allowed, false otherwise
     * @return The backend to be used for querying.
     */
    protected final IStateHistoryBackend buildAndQueryFullRange(long startTime, long endTime, int nbAttr, List<ITmfStateInterval> intervals, boolean allowNull) {

        final IStateHistoryBackend backend = prepareBackend(startTime, endTime, intervals);
        assertNotNull(backend);

        try {
            /*
             * Query at every valid time stamp, making sure only the expected
             * intervals are returned.
             */
            for (long t = backend.getStartTime(); t <= backend.getEndTime(); t++) {
                List<@Nullable ITmfStateInterval> stateInfo = new ArrayList<>(nbAttr);
                for (int i = 0; i < nbAttr; i++) {
                    stateInfo.add(null);
                }
                backend.doQuery(stateInfo, t);
                for (int attr = 0; attr < stateInfo.size(); attr++) {
                    ITmfStateInterval interval = stateInfo.get(attr);
                    if (!allowNull) {
                        assertTrue("null interval at t=" + t + " for attr=" + attr, interval != null);
                    }
                    if (interval != null) {
                        assertTrue(interval + " does not intersect t=" + t, interval.intersects(t));
                    }
                }
            }

            assertEquals(startTime, backend.getStartTime());
            assertEquals(endTime, backend.getEndTime());
        } catch (StateSystemDisposedException e) {
            fail(e.getMessage());
        }
        return backend;
    }

    /**
     * Test the full query method by filling a small backend with intervals
     * placed in a "stair-like" fashion, like this:
     *
     * <pre>
     * |x----x----x---x|
     * |xx----x----x--x|
     * |x-x----x----x-x|
     * |x--x----x----xx|
     * |      ...      |
     * </pre>
     *
     * and then querying at every single timestamp, making sure all, and only,
     * the expected intervals are returned.
     */
    @Test
    public void testCascadingIntervals() {
        final int nbAttr = 10;
        final long duration = 10;
        final long startTime = 0;
        final long endTime = 1000;

        List<ITmfStateInterval> intervals = new ArrayList<>();
        for (long t = startTime + 1; t <= endTime + duration; t++) {
            intervals.add(new TmfStateInterval(
                    Math.max(startTime, t - duration),
                    Math.min(endTime, t - 1),
                    (int) t % nbAttr,
                    TmfStateValue.newValueLong(t)));
        }

        buildAndQueryFullRange(startTime, endTime, nbAttr, intervals, false);
    }

    /**
     * Test the full query method by filling a small backend with intervals that
     * take the full time range, like this:
     *
     * <pre>
     * |x-------------x|
     * |x-------------x|
     * |x-------------x|
     * |x-------------x|
     * |      ...      |
     * </pre>
     *
     * and then querying at every single timestamp, making sure all, and only,
     * the expected intervals are returned.
     */
    @Test
    public void testFullIntervals() {
        final int nbAttr = 1000;
        final long startTime = 0;
        final long endTime = 1000;

        List<ITmfStateInterval> intervals = new ArrayList<>();
        for (int attr = 0; attr < nbAttr; attr++) {
            intervals.add(new TmfStateInterval(
                    startTime,
                    endTime,
                    attr,
                    TmfStateValue.newValueLong(attr)));
        }

        buildAndQueryFullRange(startTime, endTime, nbAttr, intervals, false);
    }
}
