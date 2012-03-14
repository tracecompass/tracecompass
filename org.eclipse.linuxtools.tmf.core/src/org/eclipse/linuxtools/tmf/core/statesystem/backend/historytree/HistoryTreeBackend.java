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

package org.eclipse.linuxtools.tmf.core.statesystem.backend.historytree;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.statesystem.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.statesystem.helpers.IStateHistoryBackend;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.statevalue.TmfStateValue;

/**
 * History Tree backend for storing a state history. This is the basic version
 * that runs in the same thread as the class creating it.
 * 
 * @author alexmont
 * 
 */
public class HistoryTreeBackend implements IStateHistoryBackend {

    protected final HistoryTree sht;
    private final HT_IO treeIO;

    /**
     * Construtor for new history files. Use this when creating a new history
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
     * @param startTime
     *            The earliest time stamp that will be stored in the history
     * @throws IOException
     *             Thrown if we can't create the file for some reason
     */
    public HistoryTreeBackend(File newStateFile, int blockSize,
            int maxChildren, long startTime) throws IOException {
        sht = new HistoryTree(newStateFile, blockSize, maxChildren, startTime);
        treeIO = sht.getTreeIO();
    }

    /**
     * Construtor for new history files. Use this when creating a new history
     * from scratch. This version supplies sane defaults for the configuration
     * parameters.
     * 
     * @param newStateFile
     *            The filename/location where to store the state history (Should
     *            end in .ht)
     * @param startTime
     *            The earliest time stamp that will be stored in the history
     * @throws IOException
     *             Thrown if we can't create the file for some reason
     */
    public HistoryTreeBackend(File newStateFile, long startTime)
            throws IOException {
        this(newStateFile, 64 * 1024, 50, startTime);
    }

    /**
     * Existing history constructor. Use this to open an existing state-file.
     * 
     * @param existingFileName Filename/location of the history we want to load
     * @throws IOException If we can't read the file, if it doesn't exist or is not recognized
     */
    public HistoryTreeBackend(File existingStateFile) throws IOException {
        sht = new HistoryTree(existingStateFile);
        treeIO = sht.getTreeIO();
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
    public void finishedBuilding(long endTime) throws TimeRangeException {
        sht.closeTree();
    }

    @Override
    public FileInputStream supplyAttributeTreeReader() {
        return treeIO.supplyATReader();
    }

    @Override
    public File supplyAttributeTreeWriterFile() {
        return treeIO.supplyATWriterFile();
    }

    @Override
    public long supplyAttributeTreeWriterFilePosition() {
        return treeIO.supplyATWriterFilePos();
    }

    @Override
    public void doQuery(List<ITmfStateInterval> stateInfo, long t)
            throws TimeRangeException {
        if (!checkValidTime(t)) {
            /* We can't possibly have information about this query */
            throw new TimeRangeException();
        }

        /* We start by reading the information in the root node */
        // FIXME using CoreNode for now, we'll have to redo this part to handle
        // different node types
        CoreNode currentNode = sht.latestBranch.firstElement();
        currentNode.writeInfoFromNode(stateInfo, t);

        /* Then we follow the branch down in the relevant children */
        while (currentNode.getNbChildren() > 0) {
            currentNode = (CoreNode) sht.selectNextChild(currentNode, t);
            currentNode.writeInfoFromNode(stateInfo, t);
        }

        /*
         * The stateInfo should now be filled with everything needed, we pass
         * the control back to the State System.
         */
        return;
    }

    @Override
    public ITmfStateInterval doSingularQuery(long t, int attributeQuark)
            throws TimeRangeException {
        return getRelevantInterval(t, attributeQuark);
    }

    /**
     * Simple check to make sure the requested timestamps are within the borders
     * of this tree.
     * 
     * @param t
     *            The queried timestamp
     * @return True if it's within range, false if not.
     */
    private boolean checkValidTime(long t) {
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
            throws TimeRangeException {
        if (!checkValidTime(t)) {
            throw new TimeRangeException();
        }

        // FIXME using CoreNode for now, we'll have to redo this part to handle
        // different node types
        CoreNode currentNode = sht.latestBranch.firstElement();
        HTInterval interval = currentNode.getRelevantInterval(key, t);

        while (interval == null && currentNode.getNbChildren() > 0) {
            currentNode = (CoreNode) sht.selectNextChild(currentNode, t);
            interval = currentNode.getRelevantInterval(key, t);
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
     * Return the current depth of the tree, ie the number of node levels.
     * 
     * @return The tree depth
     */
    public int getTreeDepth() {
        return sht.latestBranch.size();
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

        for (int seq = 0; seq < sht.getNodeCount(); seq++) {
            node = treeIO.readNode(seq);
            total += node.getNodeUsagePRC();
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
     * The basic debugPrint method will print the tree structure, but not
     * their contents.
     * 
     * This method here print the contents (the intervals) as well.
     * 
     * @param writer
     * @param printIntervals
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
