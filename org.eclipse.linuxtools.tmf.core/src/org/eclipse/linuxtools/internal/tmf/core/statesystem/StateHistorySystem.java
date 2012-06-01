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

package org.eclipse.linuxtools.internal.tmf.core.statesystem;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.internal.tmf.core.Tracer;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.interval.TmfStateInterval;
import org.eclipse.linuxtools.tmf.core.statesystem.IStateSystemBuilder;
import org.eclipse.linuxtools.tmf.core.statevalue.TmfStateValue;

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
public class StateHistorySystem extends StateSystem implements
        IStateSystemBuilder {

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
            this.attributeTree = new AttributeTree(this, backend.supplyAttributeTreeReader());
            transState.setInactive();
        }
    }

    @Override
    public long getStartTime() {
        return backend.getStartTime();
    }

    @Override
    public long getCurrentEndTime() {
        return backend.getEndTime();
    }

    @Override
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

    @Override
    public synchronized List<ITmfStateInterval> queryFullState(long t)
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

        /*
         * We should have previously inserted an interval for every attribute.
         * If we do happen do see a 'null' object here, just replace it with a a
         * dummy internal with a null value, to avoid NPE's further up.
         */
        for (int i = 0; i < stateInfo.size(); i++) {
            if (stateInfo.get(i) == null) {
                //logMissingInterval(i, t);
                stateInfo.set(i, new TmfStateInterval(t, t, i, TmfStateValue.nullValue()));
            }
        }
        return stateInfo;
    }

    @Override
    public ITmfStateInterval querySingleState(long t, int attributeQuark)
            throws AttributeNotFoundException, TimeRangeException {
        ITmfStateInterval ret;

        if (transState.hasInfoAboutStateOf(t, attributeQuark)) {
            ret = transState.getOngoingInterval(attributeQuark);
        } else {
            ret = backend.doSingularQuery(t, attributeQuark);
        }

        /*
         * Return a fake interval if we could not find anything in the history.
         * We do NOT want to return 'null' here.
         */
        if (ret == null) {
            //logMissingInterval(attributeQuark, t);
            return new TmfStateInterval(t, this.getCurrentEndTime(),
                    attributeQuark, TmfStateValue.nullValue());
        }
        return ret;
    }

    @Override
    public List<ITmfStateInterval> queryHistoryRange(int attributeQuark,
            long t1, long t2) throws TimeRangeException,
            AttributeNotFoundException {
        List<ITmfStateInterval> intervals;
        ITmfStateInterval currentInterval;
        long ts, tEnd;

        /* Make sure the time range makes sense */
        if (t2 <= t1) {
            throw new TimeRangeException();
        }

        /* Set the actual, valid end time of the range query */
        if (t2 > this.getCurrentEndTime()) {
            tEnd = this.getCurrentEndTime();
        } else {
            tEnd = t2;
        }

        /* Get the initial state at time T1 */
        intervals = new ArrayList<ITmfStateInterval>();
        currentInterval = querySingleState(t1, attributeQuark);
        intervals.add(currentInterval);

        /* Get the following state changes */
        ts = currentInterval.getEndTime();
        while (ts != -1 && ts < tEnd) {
            ts++; /* To "jump over" to the next state in the history */
            currentInterval = querySingleState(ts, attributeQuark);
            intervals.add(currentInterval);
            ts = currentInterval.getEndTime();
        }
        return intervals;
    }

    @Override
    public List<ITmfStateInterval> queryHistoryRange(int attributeQuark,
            long t1, long t2, long resolution) throws TimeRangeException,
            AttributeNotFoundException {
        List<ITmfStateInterval> intervals;
        ITmfStateInterval currentInterval;
        long ts, tEnd;

        /* Make sure the time range makes sense */
        if (t2 < t1 || resolution <= 0) {
            throw new TimeRangeException();
        }

        /* Set the actual, valid end time of the range query */
        if (t2 > this.getCurrentEndTime()) {
            tEnd = this.getCurrentEndTime();
        } else {
            tEnd = t2;
        }

        /* Get the initial state at time T1 */
        intervals = new ArrayList<ITmfStateInterval>();
        currentInterval = querySingleState(t1, attributeQuark);
        intervals.add(currentInterval);

        /*
         * Iterate over the "resolution points". We skip unneeded queries in the
         * case the current interval is longer than the resolution.
         */
        for (ts = t1; (currentInterval.getEndTime() != -1) && (ts < tEnd);
                ts += resolution) {
            if (ts <= currentInterval.getEndTime()) {
                continue;
            }
            currentInterval = querySingleState(ts, attributeQuark);
            intervals.add(currentInterval);
        }

        /* Add the interval at t2, if it wasn't included already. */
        if (currentInterval.getEndTime() < tEnd) {
            currentInterval = querySingleState(tEnd, attributeQuark);
            intervals.add(currentInterval);
        } 
        return intervals;
    }

    static void logMissingInterval(int attribute, long timestamp) {
        Tracer.traceInfo("No data found in history for attribute " + //$NON-NLS-1$
                attribute + " at time " + timestamp + //$NON-NLS-1$
                ", returning dummy interval"); //$NON-NLS-1$
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
