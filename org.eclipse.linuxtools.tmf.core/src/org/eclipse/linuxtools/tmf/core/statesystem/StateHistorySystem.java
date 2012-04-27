/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.statesystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.statesystem.helpers.IStateHistoryBackend;

/**
 * This is the extension of the StateSystem, which will save the state intervals
 * that are created from the transient state (instead of discarding them, like a
 * simple StateSystem would do).
 * 
 * This allows the user to then run queries at past timestamps.
 * 
 * DON'T FORGET to call .closeHistory() when you are done inserting intervals,
 * or the storage backend will have no way of knowing it can close and write
 * itself to disk, and its thread will keep running.
 * 
 * @author alexmont
 * 
 */
public class StateHistorySystem extends StateSystem {

    /*
     * Inherited from StateSystem
     * 
     * protected ArrayList<AttributeTreeNode> attributeList; protected
     * TransientState transState; protected CurrentState curState;
     */

    /**
     * In addition, a state "history" system has a storage back-end from which
     * it can restore past states.
     */
    private final IStateHistoryBackend backend;

    /**
     * General constructor
     * 
     * @param backend
     *            The "state history storage" backend to use.
     * @param newFile
     *            Put true if this is a new history started from scratch. It is
     *            used to tell the state system where to get its attribute tree.
     * @throws IOException
     */
    public StateHistorySystem(IStateHistoryBackend backend, boolean newFile)
            throws IOException {
        this.backend = backend;
        transState = new TransientState(backend);

        if (newFile) {
            attributeTree = new AttributeTree(this);
        } else {
            /* We're opening an existing file */
            FileInputStream attributeTreeInput = backend.supplyAttributeTreeReader();
            this.attributeTree = new AttributeTree(this, attributeTreeInput);
            transState.setInactive();
            attributeTreeInput.close();
        }
    }

    public IStateHistoryBackend getHistoryBackend() {
        return backend;
    }

    /**
     * Method to close off the History Provider. This happens for example when
     * we are done reading an off-line trace. First we close the TransientState,
     * commit it to the Provider, mark it as inactive, then we write the
     * Attribute Tree somewhere so we can reopen it later.
     * 
     * @param endTime
     *            The requested End Time of the history, since it could be
     *            bigger than the timestamp of the last event or state change we
     *            have seen. All "ongoing" states will be extended until this
     *            'endTime'.
     * @throws TimeRangeException
     *             If the passed endTime doesn't make sense (for example, if
     *             it's earlier than the latest time) and the backend doesn't
     *             know how to handle it.
     */
    public void closeHistory(long endTime) throws TimeRangeException {
        File attributeTreeFile;
        long attributeTreeFilePos;
        long realEndTime = endTime;

        if (realEndTime < backend.getEndTime()) {
            /*
             * This can happen (empty nodes pushing the border further, etc.)
             * but shouldn't be too big of a deal.
             */
            realEndTime = backend.getEndTime();
        }
        transState.closeTransientState(realEndTime);
        backend.finishedBuilding(realEndTime);

        attributeTreeFile = backend.supplyAttributeTreeWriterFile();
        attributeTreeFilePos = backend.supplyAttributeTreeWriterFilePosition();
        if (attributeTreeFile != null) {
            /*
             * If null was returned, we simply won't save the attribute tree,
             * too bad!
             */
            attributeTree.writeSelf(attributeTreeFile, attributeTreeFilePos);
        }
    }

    /**
     * @name External query methods
     */

    /**
     * Load the complete state information at time 't' into the returned List.
     * You can then get the intervals for single attributes by using
     * List.get(n), where 'n' is the quark of the attribute.
     * 
     * On average if you need around 10 or more queries for the same timestamps,
     * use this method. If you need less than 10 (for example, running many
     * queries for the same attributes but at different timestamps), you might
     * be better using the querySingleState() methods instead.
     * 
     * @param t
     *            We will recreate the state information to what it was at time
     *            t.
     * @throws TimeRangeException
     *             If the 't' parameter is outside of the range of the state
     *             history.
     */
    public synchronized List<ITmfStateInterval> loadStateAtTime(long t)
            throws TimeRangeException {
        List<ITmfStateInterval> stateInfo = new ArrayList<ITmfStateInterval>(
                attributeTree.getNbAttributes());

        /* Bring the size of the array to the current number of attributes */
        for (int i = 0; i < attributeTree.getNbAttributes(); i++) {
            stateInfo.add(null);
        }

        /* Query the storage backend */
        backend.doQuery(stateInfo, t);

        /*
         * If we are currently building the history, also query the "ongoing"
         * states for stuff that might not yet be written to the history.
         */
        if (transState.isActive()) {
            transState.doQuery(stateInfo, t);
        }
        // We should have previously inserted an interval for every attribute
        // for all possible timestamps (and those could contain 'nullValues').
        // There should be no 'null' objects at this point.
        for (int i = 0; i < stateInfo.size(); i++) {
            if (stateInfo.get(i) == null) {
                assert (false);
            }
        }
        return stateInfo;
    }

    /**
     * Singular query method. This one does not update the whole stateInfo
     * vector, like loadStateAtTimes() does. It only searches for one specific
     * entry in the state history.
     * 
     * It should be used when you only want very few entries, instead of the
     * whole state (or many entries, but all at different timestamps). If you do
     * request many entries all at the same time, you should use the
     * conventional loadStateAtTime() + List.get() method.
     * 
     * @param t
     *            The timestamp at which we want the state
     * @param attributeQuark
     *            Which attribute we want to get the state of
     * @return The StateInterval representing the state
     * @throws TimeRangeException
     *             If 't' is invalid
     * @throws AttributeNotFoundException
     *             If the requested quark does not exist in the model
     */
    public ITmfStateInterval querySingleState(long t, int attributeQuark)
            throws AttributeNotFoundException, TimeRangeException {
        ITmfStateInterval ret;

        if (transState.hasInfoAboutStateOf(t, attributeQuark)) {
            ret = transState.getOngoingInterval(attributeQuark);
        } else {
            ret = backend.doSingularQuery(t, attributeQuark);
        }
        assert (ret != null);
        return ret;
    }

    /**
     * Return a list of state intervals, containing the "history" of a given
     * attribute between timestamps t1 and t2. The list will be ordered by
     * ascending time.
     * 
     * Note that contrary to loadStateAtTime(), the returned list here is in the
     * "direction" of time (and not in the direction of attributes, as is the
     * case with loadStateAtTime()).
     * 
     * @param attributeQuark
     *            Which attribute this query is interested in
     * @param t1
     *            Start time of the range query
     * @param t2
     *            End time of the query
     * @return The List of state intervals that happened between t1 and t2
     * @throws TimeRangeException
     *             If either t1 or t2 is invalid.
     * @throws AttributeNotFoundException
     *             If the requested quark does not exist in the model.
     */
    public List<ITmfStateInterval> queryHistoryRange(int attributeQuark,
            long t1, long t2) throws TimeRangeException,
            AttributeNotFoundException {
        List<ITmfStateInterval> intervals;
        ITmfStateInterval currentInterval;
        long ts;

        checkValidTimeRange(t1, t2);

        /* Get the initial state at time T1 */
        intervals = new ArrayList<ITmfStateInterval>();
        currentInterval = querySingleState(t1, attributeQuark);
        intervals.add(currentInterval);

        /* Get the following state changes */
        ts = currentInterval.getEndTime();
        while (ts != -1 && ts <= t2) {
            ts++; /* To "jump over" to the next state in the history */
            currentInterval = querySingleState(ts, attributeQuark);
            intervals.add(currentInterval);
            ts = currentInterval.getEndTime();
        }
        return intervals;
    }

    /**
     * Return the state history of a given attribute, but with at most one
     * update per "resolution". This can be useful for populating views (where
     * it's useless to have more than one query per pixel, for example).
     * 
     * @param attributeQuark
     *            Which attribute this query is interested in
     * @param t1
     *            Start time of the range query
     * @param t2
     *            End time of the query
     * @param resolution
     *            The "step" of this query
     * @return The List of states that happened between t1 and t2
     * @throws TimeRangeException
     *             If one of the timestamps is invalid
     * @throws AttributeNotFoundException
     *             If the attribute doesn't exist
     */
    public List<ITmfStateInterval> queryHistoryRange(int attributeQuark,
            long t1, long t2, long resolution) throws TimeRangeException,
            AttributeNotFoundException {
        List<ITmfStateInterval> intervals;
        ITmfStateInterval currentInterval;
        long ts;

        checkValidTimeRange(t1, t2);

        /* Get the initial state at time T1 */
        intervals = new ArrayList<ITmfStateInterval>();
        currentInterval = querySingleState(t1, attributeQuark);
        intervals.add(currentInterval);

        /*
         * Iterate over the "resolution points". We skip unneeded queries in the
         * case the current interval is longer than the resolution.
         */
        for (ts = currentInterval.getStartTime(); (currentInterval.getEndTime() != -1)
                && (ts <= t2); ts += resolution) {
            if (ts <= currentInterval.getEndTime()) {
                continue;
            }
            currentInterval = querySingleState(ts, attributeQuark);
            intervals.add(currentInterval);
        }
        return intervals;
    }

    /**
     * Makes sure the range [t1, t2] is valid for the current state history.
     */
    private void checkValidTimeRange(long t1, long t2)
            throws TimeRangeException {
        if (!(backend.checkValidTime(t1) && backend.checkValidTime(t2))) {
            throw new TimeRangeException();
        }
    }

    /**
     * @name Debugging methods
     */

    /**
     * Print out the contents of the inner structures to the selected
     * PrintWriter.
     */
    @Override
    public synchronized void debugPrint(PrintWriter writer) {
        /* Only used for debugging, shouldn't be externalized */
        writer.println("------------------------------"); //$NON-NLS-1$

        /* Print the other inner containers */
        super.debugPrint(writer);
        backend.debugPrint(writer);
    }
}
