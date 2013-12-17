/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.historytree;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.statevalue.TmfStateValue;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;

/**
 * Variant of the HistoryTreeBackend which runs all the interval-insertion logic
 * in a separate thread.
 *
 * @author alexmont
 *
 */
public final class ThreadedHistoryTreeBackend extends HistoryTreeBackend
        implements Runnable {

    private BlockingQueue<HTInterval> intervalQueue;
    private final Thread shtThread;

    /**
     * New state history constructor
     *
     * Note that it usually doesn't make sense to use a Threaded HT if you're
     * opening an existing state-file, but you know what you're doing...
     *
     * @param newStateFile
     *            The name of the history file that will be created. Should end
     *            in ".ht"
     * @param blockSize
     *            The size of the blocks in the file
     * @param maxChildren
     *            The maximum number of children allowed for each core node
     * @param startTime
     *            The earliest timestamp stored in the history
     * @param providerVersion
     *            Version of of the state provider. We will only try to reopen
     *            existing files if this version matches the one in the
     *            framework.
     * @param queueSize
     *            The size of the interval insertion queue. 2000 - 10000 usually
     *            works well
     * @throws IOException
     *             If there was a problem opening the history file for writing
     */
    public ThreadedHistoryTreeBackend(File newStateFile, int blockSize,
            int maxChildren, long startTime, int providerVersion, int queueSize)
                    throws IOException {
        super(newStateFile, blockSize, maxChildren, providerVersion, startTime);

        intervalQueue = new ArrayBlockingQueue<>(queueSize);
        shtThread = new Thread(this, "History Tree Thread"); //$NON-NLS-1$
        shtThread.start();
    }

    /**
     * New State History constructor. This version provides default values for
     * blockSize and maxChildren.
     *
     * @param newStateFile
     *            The name of the history file that will be created. Should end
     *            in ".ht"
     * @param startTime
     *            The earliest timestamp stored in the history
     * @param providerVersion
     *            Version of of the state provider. We will only try to reopen
     *            existing files if this version matches the one in the
     *            framework.
     * @param queueSize
     *            The size of the interval insertion queue. 2000 - 10000 usually
     *            works well
     * @throws IOException
     *             If there was a problem opening the history file for writing
     */
    public ThreadedHistoryTreeBackend(File newStateFile, long startTime,
            int providerVersion, int queueSize) throws IOException {
        super(newStateFile, providerVersion, startTime);

        intervalQueue = new ArrayBlockingQueue<>(queueSize);
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

    @Override
    public void insertPastState(long stateStartTime, long stateEndTime,
            int quark, ITmfStateValue value) throws TimeRangeException {
        /*
         * Here, instead of directly inserting the elements in the History Tree
         * underneath, we'll put them in the Queue. They will then be taken and
         * processed by the other thread executing the run() method.
         */
        HTInterval interval = new HTInterval(stateStartTime, stateEndTime,
                quark, (TmfStateValue) value);
        try {
            intervalQueue.put(interval);
        } catch (InterruptedException e) {
            Activator.logError("State system interrupted", e); //$NON-NLS-1$
        }
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
        isFinishedBuilding = true;
        return;
    }

    @Override
    public void dispose() {
        if (!isFinishedBuilding) {
            stopRunningThread(TmfTimestamp.PROJECT_IS_CANNED.getValue());
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
         * Send a "poison pill" in the queue, then wait for the HT to finish
         * its closeTree()
         */
        try {
            HTInterval pill = new HTInterval(-1, endTime, -1, TmfStateValue.nullValue());
            intervalQueue.put(pill);
            shtThread.join();
        } catch (TimeRangeException e) {
            Activator.logError("Error closing state system", e); //$NON-NLS-1$
        } catch (InterruptedException e) {
            Activator.logError("State system interrupted", e); //$NON-NLS-1$
        }
    }

    @Override
    public void run() {
        if (intervalQueue == null) {
            Activator.logError("Cannot start the storage backend without its interval queue."); //$NON-NLS-1$
            return;
        }
        HTInterval currentInterval;
        try {
            currentInterval = intervalQueue.take();
            while (currentInterval.getStartTime() != -1) {
                /* Send the interval to the History Tree */
                sht.insertInterval(currentInterval);
                currentInterval = intervalQueue.take();
            }
            assert (currentInterval.getAttribute() == -1);
            /*
             * We've been told we're done, let's write down everything and quit.
             * The end time of this "signal interval" is actually correct.
             */
            sht.closeTree(currentInterval.getEndTime());
            return;
        } catch (InterruptedException e) {
            /* We've been interrupted abnormally */
            Activator.logError("State History Tree interrupted!", e); //$NON-NLS-1$
        } catch (TimeRangeException e) {
            /* This also should not happen */
            Activator.logError("Error starting the state system", e); //$NON-NLS-1$
        }
    }

}
