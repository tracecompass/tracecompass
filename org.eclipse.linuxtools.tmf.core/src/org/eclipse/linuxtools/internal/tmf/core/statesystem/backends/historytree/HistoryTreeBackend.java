/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.ClosedChannelException;
import java.util.List;

import org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.IStateHistoryBackend;
import org.eclipse.linuxtools.tmf.core.exceptions.StateSystemDisposedException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.statevalue.TmfStateValue;

/**
 * History Tree backend for storing a state history. This is the basic version
 * that runs in the same thread as the class creating it.
 *
 * @author Alexandre Montplaisir
 *
 */
public class HistoryTreeBackend implements IStateHistoryBackend {

    /** The history tree that sits underneath */
    protected final HistoryTree sht;

    /** Indicates if the history tree construction is done */
    protected boolean isFinishedBuilding = false;

    /**
     * Constructor for new history files. Use this when creating a new history
     * from scratch.
     *
     * @param newStateFile
     *            The filename/location where to store the state history (Should
     *            end in .ht)
     * @param blockSize
     *            The size of the blocks in the history file. This should be a
     *            multiple of 4096.
     * @param maxChildren
     *            The maximum number of children each core node can have
     * @param providerVersion
     *            Version of of the state provider. We will only try to reopen
     *            existing files if this version matches the one in the
     *            framework.
     * @param startTime
     *            The earliest time stamp that will be stored in the history
     * @throws IOException
     *             Thrown if we can't create the file for some reason
     */
    public HistoryTreeBackend(File newStateFile, int blockSize,
            int maxChildren, int providerVersion, long startTime) throws IOException {
        final HTConfig conf = new HTConfig(newStateFile, blockSize, maxChildren,
                providerVersion, startTime);
        sht = new HistoryTree(conf);
    }

    /**
     * Constructor for new history files. Use this when creating a new history
     * from scratch. This version supplies sane defaults for the configuration
     * parameters.
     *
     * @param newStateFile
     *            The filename/location where to store the state history (Should
     *            end in .ht)
     * @param providerVersion
     *            Version of of the state provider. We will only try to reopen
     *            existing files if this version matches the one in the
     *            framework.
     * @param startTime
     *            The earliest time stamp that will be stored in the history
     * @throws IOException
     *             Thrown if we can't create the file for some reason
     */
    public HistoryTreeBackend(File newStateFile, int providerVersion, long startTime)
            throws IOException {
        this(newStateFile, 64 * 1024, 50, providerVersion, startTime);
    }

    /**
     * Existing history constructor. Use this to open an existing state-file.
     *
     * @param existingStateFile
     *            Filename/location of the history we want to load
     * @param providerVersion
     *            Expected version of of the state provider plugin.
     * @throws IOException
     *             If we can't read the file, if it doesn't exist, is not
     *             recognized, or if the version of the file does not match the
     *             expected providerVersion.
     */
    public HistoryTreeBackend(File existingStateFile, int providerVersion)
            throws IOException {
        sht = new HistoryTree(existingStateFile, providerVersion);
        isFinishedBuilding = true;
    }

    @Override
    public long getStartTime() {
        return sht.getTreeStart();
    }

    @Override
    public long getEndTime() {
        return sht.getTreeEnd();
    }

    @Override
    public void insertPastState(long stateStartTime, long stateEndTime,
            int quark, ITmfStateValue value) throws TimeRangeException {
        HTInterval interval = new HTInterval(stateStartTime, stateEndTime,
                quark, (TmfStateValue) value);

        /* Start insertions at the "latest leaf" */
        sht.insertInterval(interval);
    }

    @Override
    public void finishedBuilding(long endTime) {
        sht.closeTree(endTime);
        isFinishedBuilding = true;
    }

    @Override
    public FileInputStream supplyAttributeTreeReader() {
        return sht.supplyATReader();
    }

    @Override
    public File supplyAttributeTreeWriterFile() {
        return sht.supplyATWriterFile();
    }

    @Override
    public long supplyAttributeTreeWriterFilePosition() {
        return sht.supplyATWriterFilePos();
    }

    @Override
    public void removeFiles() {
        sht.deleteFile();
    }

    @Override
    public void dispose() {
        if (isFinishedBuilding) {
            sht.closeFile();
        } else {
            /*
             * The build is being interrupted, delete the file we partially
             * built since it won't be complete, so shouldn't be re-used in the
             * future (.deleteFile() will close the file first)
             */
            sht.deleteFile();
        }
    }

    @Override
    public void doQuery(List<ITmfStateInterval> stateInfo, long t)
            throws TimeRangeException, StateSystemDisposedException {
        if (!checkValidTime(t)) {
            /* We can't possibly have information about this query */
            throw new TimeRangeException();
        }

        /* We start by reading the information in the root node */
        // FIXME using CoreNode for now, we'll have to redo this part to handle
        // different node types
        CoreNode currentNode = sht.getRootNode();
        currentNode.writeInfoFromNode(stateInfo, t);

        /* Then we follow the branch down in the relevant children */
        try {
            while (currentNode.getNbChildren() > 0) {
                currentNode = (CoreNode) sht.selectNextChild(currentNode, t);
                currentNode.writeInfoFromNode(stateInfo, t);
            }
        } catch (ClosedChannelException e) {
            throw new StateSystemDisposedException(e);
        }

        /*
         * The stateInfo should now be filled with everything needed, we pass
         * the control back to the State System.
         */
        return;
    }

    @Override
    public ITmfStateInterval doSingularQuery(long t, int attributeQuark)
            throws TimeRangeException, StateSystemDisposedException {
        return getRelevantInterval(t, attributeQuark);
    }

    @Override
    public boolean checkValidTime(long t) {
        return (t >= sht.getTreeStart() && t <= sht.getTreeEnd());
    }

    /**
     * Inner method to find the interval in the tree containing the requested
     * key/timestamp pair, wherever in which node it is.
     *
     * @param t
     * @param key
     * @return The node containing the information we want
     */
    private HTInterval getRelevantInterval(long t, int key)
            throws TimeRangeException, StateSystemDisposedException {
        if (!checkValidTime(t)) {
            throw new TimeRangeException();
        }

        // FIXME using CoreNode for now, we'll have to redo this part to handle
        // different node types
        CoreNode currentNode = sht.getRootNode();
        HTInterval interval = currentNode.getRelevantInterval(key, t);

        try {
            while (interval == null && currentNode.getNbChildren() > 0) {
                currentNode = (CoreNode) sht.selectNextChild(currentNode, t);
                interval = currentNode.getRelevantInterval(key, t);
            }
        } catch (ClosedChannelException e) {
            throw new StateSystemDisposedException(e);
        }
        /*
         * Since we should now have intervals at every attribute/timestamp
         * combination, it should NOT be null here.
         */
        assert (interval != null);
        return interval;
    }

    /**
     * Return the size of the tree history file
     *
     * @return The current size of the history file in bytes
     */
    public long getFileSize() {
        return sht.getFileSize();
    }

    /**
     * Return the average node usage as a percentage (between 0 and 100)
     *
     * @return Average node usage %
     */
    public int getAverageNodeUsage() {
        HTNode node;
        long total = 0;
        long ret;

        try {
            for (int seq = 0; seq < sht.getNodeCount(); seq++) {
                node = sht.readNode(seq);
                total += node.getNodeUsagePercent();
            }
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }

        ret = total / sht.getNodeCount();
        assert (ret >= 0 && ret <= 100);
        return (int) ret;
    }

    @Override
    public void debugPrint(PrintWriter writer) {
        /* By default don't print out all the intervals */
        this.debugPrint(writer, false);
    }

    /**
     * The basic debugPrint method will print the tree structure, but not their
     * contents.
     *
     * This method here print the contents (the intervals) as well.
     *
     * @param writer
     *            The PrintWriter to which the debug info will be written
     * @param printIntervals
     *            Should we also print every contained interval individually?
     */
    public void debugPrint(PrintWriter writer, boolean printIntervals) {
        /* Only used for debugging, shouldn't be externalized */
        writer.println("------------------------------"); //$NON-NLS-1$
        writer.println("State History Tree:\n"); //$NON-NLS-1$
        writer.println(sht.toString());
        writer.println("Average node utilization: " //$NON-NLS-1$
                + this.getAverageNodeUsage());
        writer.println(""); //$NON-NLS-1$

        sht.debugPrintFullTree(writer, printIntervals);
    }
}
