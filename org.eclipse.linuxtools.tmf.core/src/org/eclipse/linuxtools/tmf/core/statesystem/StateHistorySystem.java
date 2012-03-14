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

    private final IStateHistoryBackend backend;

    /*
     * This the container that will hold the result of full queries obtained
     * with loadStateAtTime()
     */
    private ArrayList<ITmfStateInterval> currentStateInfo;

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
        currentStateInfo = new ArrayList<ITmfStateInterval>();

        if (newFile) {
            attributeTree = new AttributeTree(this);
        } else {
            /* We're opening an existing file */
            FileInputStream attributeTreeInput = backend.supplyAttributeTreeReader();
            this.attributeTree = new AttributeTree(this, attributeTreeInput);
            transState.setInactive();
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
     * Load the state information at time 't' as the Current State. You can then
     * use the queryState() method to query the value of different attributes,
     * as they were at time 't'.
     * 
     * On average if you need around 10 or more queries for the same timestamps,
     * use this. If you need less than 10 (for example, running many queries for
     * the same attributes but at different timestamps), you might be better
     * using the querySingleState() methods instead.
     * 
     * @param t
     *            We will recreate the state information to what it was at time
     *            t.
     * @throws TimeRangeException
     */
    public synchronized void loadStateAtTime(long t) throws TimeRangeException {
        /* Empty the currentStateInfo */
        currentStateInfo = new ArrayList<ITmfStateInterval>(
                attributeTree.getNbAttributes());
        for (int i = 0; i < attributeTree.getNbAttributes(); i++) {
            currentStateInfo.add(null);
        }

        backend.doQuery(currentStateInfo, t);

        if (transState.isActive()) {
            transState.doQuery(currentStateInfo, t);
        }
        // We should have previously inserted an interval for every attribute
        // for all possible timestamps (and those could contain 'nullValues').
        // There should be no 'null' objects at this point.
        for (ITmfStateInterval interval : currentStateInfo) {
            assert (interval != null);
        }
    }

    /**
     * Once we have set up the "current state" using loadStateAtTime(), we can
     * now run queries to get the state of individual attributes at the
     * previously loaded timestamp.
     * 
     * @param attributeQuark
     *            The quark of attribute for which we want the state.
     * @return The StateInterval object matching this timestamp/attribute pair.
     * @throws AttributeNotFoundException
     */
    public ITmfStateInterval queryState(int attributeQuark) {
        return currentStateInfo.get(attributeQuark);
    }

    /**
     * Alternative, singular version of the "queryState" method. This one does
     * not update the whole stateInfo vector, like loadStateAtTimes does. It
     * only searches for one specific entry in the state history.
     * 
     * It should be used when you only want very few entries, instead of the
     * whole state (or many entries, but all at different timestamps). If you do
     * request many entries all at the same time, you should use the
     * conventional loadStateAtTime() + queryState() method.
     * 
     * @param t
     *            The timestamp at which we want the state
     * @param attributeQuark
     *            Which attribute we want to get the state of
     * @return The StateInterval representing the state
     * @throws TimeRangeException
     * @throws AttributeNotFoundException
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
     * @param attributeQuark
     *            Which attribute this query is interested in
     * @param t1
     *            Start time of the range query
     * @param t2
     *            End time of the query
     * @return The List of state intervals that happened between t1 and t2
     * @throws TimeRangeException
     * @throws AttributeNotFoundException
     */
    public List<ITmfStateInterval> queryHistoryRange(int attributeQuark, long t1,
            long t2) throws TimeRangeException, AttributeNotFoundException {

        List<ITmfStateInterval> intervals = new ArrayList<ITmfStateInterval>();
        ITmfStateInterval currentInterval;
        long ts;

        /* Get the initial state at time T1 */
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
     * @name Debugging methods
     */

    /**
     * Print out the contents of the inner structures to the selected
     * PrintWriter.
     */
    @Override
    public void debugPrint(PrintWriter writer) {
        /* Only used for debugging, shouldn't be externalized */
        writer.println("------------------------------"); //$NON-NLS-1$
        writer.println("Current State Info vector:\n"); //$NON-NLS-1$
        for (int i = 0; i < currentStateInfo.size(); i++) {
            writer.print(i + "\t\t"); //$NON-NLS-1$
            if (currentStateInfo.get(i) == null) {
                writer.println("null"); //$NON-NLS-1$
            } else {
                writer.println(currentStateInfo.get(i).toString());
            }
        }
        writer.println('\n');

        /* Print the other inner containers */
        super.debugPrint(writer);
        backend.debugPrint(writer);
    }
}