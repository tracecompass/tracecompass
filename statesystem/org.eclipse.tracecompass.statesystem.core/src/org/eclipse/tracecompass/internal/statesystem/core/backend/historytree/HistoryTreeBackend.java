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
 *   Patrick Tasse - Add message to exceptions
 *******************************************************************************/

package org.eclipse.tracecompass.internal.statesystem.core.backend.historytree;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.common.core.log.TraceCompassLog;
import org.eclipse.tracecompass.internal.statesystem.core.Activator;
import org.eclipse.tracecompass.statesystem.core.backend.IStateHistoryBackend;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;

import com.google.common.annotations.VisibleForTesting;

/**
 * History Tree backend for storing a state history. This is the basic version
 * that runs in the same thread as the class creating it.
 *
 * @author Alexandre Montplaisir
 */
public class HistoryTreeBackend implements IStateHistoryBackend {

    private static final Logger LOGGER = TraceCompassLog.getLogger(HistoryTreeBackend.class);

    private final @NonNull String fSsid;

    /**
     * The history tree that sits underneath.
     */
    private final @NonNull IHistoryTree fSht;

    /** Indicates if the history tree construction is done */
    private volatile boolean fFinishedBuilding = false;

    /**
     * Indicates if the history tree construction is done
     *
     * @return if the history tree construction is done
     */
    protected boolean isFinishedBuilding() {
        return fFinishedBuilding;
    }

    /**
     * Sets if the history tree is finished building
     *
     * @param isFinishedBuilding
     *            is the history tree finished building
     */
    protected void setFinishedBuilding(boolean isFinishedBuilding) {
        fFinishedBuilding = isFinishedBuilding;
    }

    /**
     * Constructor for new history files. Use this when creating a new history
     * from scratch.
     *
     * @param ssid
     *            The state system's ID
     * @param newStateFile
     *            The filename/location where to store the state history (Should
     *            end in .ht)
     * @param providerVersion
     *            Version of of the state provider. We will only try to reopen
     *            existing files if this version matches the one in the
     *            framework.
     * @param startTime
     *            The earliest time stamp that will be stored in the history
     * @param blockSize
     *            The size of the blocks in the history file. This should be a
     *            multiple of 4096.
     * @param maxChildren
     *            The maximum number of children each core node can have
     * @throws IOException
     *             Thrown if we can't create the file for some reason
     */
    public HistoryTreeBackend(@NonNull String ssid,
            File newStateFile,
            int providerVersion,
            long startTime,
            int blockSize,
            int maxChildren) throws IOException {
        fSsid = ssid;
        final HTConfig conf = new HTConfig(newStateFile, blockSize, maxChildren,
                providerVersion, startTime);
        fSht = initializeSHT(conf);
    }

    /**
     * Constructor for new history files. Use this when creating a new history
     * from scratch. This version supplies sane defaults for the configuration
     * parameters.
     *
     * @param ssid
     *            The state system's id
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
     * @since 1.0
     */
    public HistoryTreeBackend(@NonNull String ssid, File newStateFile, int providerVersion, long startTime)
            throws IOException {
        this(ssid, newStateFile, providerVersion, startTime, 64 * 1024, 50);
    }

    /**
     * Existing history constructor. Use this to open an existing state-file.
     *
     * @param ssid
     *            The state system's id
     * @param existingStateFile
     *            Filename/location of the history we want to load
     * @param providerVersion
     *            Expected version of of the state provider plugin.
     * @throws IOException
     *             If we can't read the file, if it doesn't exist, is not
     *             recognized, or if the version of the file does not match the
     *             expected providerVersion.
     */
    public HistoryTreeBackend(@NonNull String ssid, @NonNull File existingStateFile, int providerVersion)
            throws IOException {
        fSsid = ssid;
        fSht = initializeSHT(existingStateFile, providerVersion);
        fFinishedBuilding = true;
    }

    /**
     * New-tree initializer for the History Tree wrapped by this backend. Can be
     * overriden to use different implementations.
     *
     * @param conf
     *            The HTConfig configuration object
     * @return The new history tree
     * @throws IOException
     *             If there was a problem during creation
     */
    @VisibleForTesting
    protected @NonNull IHistoryTree initializeSHT(@NonNull HTConfig conf) throws IOException {
        return HistoryTreeFactory.createHistoryTree(conf);
    }

    /**
     * Existing-tree initializer for the History Tree wrapped by this backend.
     * Can be overriden to use different implementations.
     *
     * @param existingStateFile
     *            The file to open
     * @param providerVersion
     *            The expected state provider version
     * @return The history tree opened from the given file
     * @throws IOException
     *             If there was a problem during creation
     */
    @VisibleForTesting
    protected @NonNull IHistoryTree initializeSHT(@NonNull File existingStateFile, int providerVersion) throws IOException {
        return HistoryTreeFactory.createFromFile(existingStateFile.toPath(), providerVersion);
    }

    /**
     * Get the History Tree built by this backend.
     *
     * Note: Do not override this method. If you want to extend the class to use
     * a different History Tree implementation, override both variants of
     * {@link #initializeSHT} instead.
     *
     * @return The history tree
     */
    protected final @NonNull IHistoryTree getSHT() {
        return fSht;
    }

    @Override
    public String getSSID() {
        return fSsid;
    }

    @Override
    public long getStartTime() {
        return getSHT().getTreeStart();
    }

    @Override
    public long getEndTime() {
        return getSHT().getTreeEnd();
    }

    @Override
    public void insertPastState(long stateStartTime, long stateEndTime,
            int quark, ITmfStateValue value) throws TimeRangeException {
        HTInterval interval = new HTInterval(stateStartTime, stateEndTime,
                quark, (TmfStateValue) value);

        /* Start insertions at the "latest leaf" */
        getSHT().insertInterval(interval);
    }

    @Override
    public void finishedBuilding(long endTime) {
        getSHT().closeTree(endTime);
        fFinishedBuilding = true;
    }

    @Override
    public FileInputStream supplyAttributeTreeReader() {
        return getSHT().supplyATReader();
    }

    @Override
    public File supplyAttributeTreeWriterFile() {
        return getSHT().supplyATWriterFile();
    }

    @Override
    public long supplyAttributeTreeWriterFilePosition() {
        return getSHT().supplyATWriterFilePos();
    }

    @Override
    public void removeFiles() {
        getSHT().deleteFile();
    }

    @Override
    public void dispose() {
        if (fFinishedBuilding) {
            LOGGER.info(() -> "[HistoryTreeBackend:ClosingFile] size=" + getSHT().getFileSize());  //$NON-NLS-1$
            getSHT().closeFile();
        } else {
            /*
             * The build is being interrupted, delete the file we partially
             * built since it won't be complete, so shouldn't be re-used in the
             * future (.deleteFile() will close the file first)
             */
            getSHT().deleteFile();
        }
    }

    @Override
    public void doQuery(List<ITmfStateInterval> stateInfo, long t)
            throws TimeRangeException, StateSystemDisposedException {
        checkValidTime(t);

        /* Queue is a stack of nodes containing nodes intersecting t */
        Deque<HTNode> queue = new LinkedList<>();

        /* We start by reading the information in the root node */
        queue.add(getSHT().getRootNode());

        /* Then we follow the down in the relevant children */
        try {
            while (!queue.isEmpty()) {
                HTNode currentNode = queue.pop();
                if (currentNode.getNodeType() == HTNode.NodeType.CORE) {
                    /*Here we add the relevant children nodes for BFS*/
                    queue.addAll(getSHT().selectNextChildren((ParentNode) currentNode, t));
                }
                currentNode.writeInfoFromNode(stateInfo, t);
            }
        } catch (ClosedChannelException e) {
            throw new StateSystemDisposedException(e);
        }

        /*
         * The stateInfo should now be filled with everything needed, we pass
         * the control back to the State System.
         */
    }

    @Override
    public ITmfStateInterval doSingularQuery(long t, int attributeQuark)
            throws TimeRangeException, StateSystemDisposedException {
        try {
            return getRelevantInterval(t, attributeQuark);
        } catch (ClosedChannelException e) {
            throw new StateSystemDisposedException(e);
        }
    }

    private void checkValidTime(long t) {
        long startTime = getStartTime();
        long endTime = getEndTime();
        if (t < startTime || t > endTime) {
            throw new TimeRangeException(String.format("%s Time:%d, Start:%d, End:%d", //$NON-NLS-1$
                    fSsid, t, startTime, endTime));
        }
    }

    /**
     * Inner method to find the interval in the tree containing the requested
     * key/timestamp pair, wherever in which node it is.
     */
    private HTInterval getRelevantInterval(long t, int key)
            throws TimeRangeException, ClosedChannelException {
        checkValidTime(t);

        Deque<HTNode> queue = new LinkedList<>();
        queue.add(getSHT().getRootNode());
        HTInterval interval = null;
        while (interval == null && !queue.isEmpty()) {
            HTNode currentNode = queue.pop();
            if (currentNode.getNodeType() == HTNode.NodeType.CORE) {
                queue.addAll(getSHT().selectNextChildren((ParentNode) currentNode, t));
            }
            interval = currentNode.getRelevantInterval(key, t);
        }
        return interval;
    }

    /**
     * Return the size of the tree history file
     *
     * @return The current size of the history file in bytes
     */
    public long getFileSize() {
        return getSHT().getFileSize();
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
            for (int seq = 0; seq < getSHT().getNodeCount(); seq++) {
                node = getSHT().readNode(seq);
                total += node.getNodeUsagePercent();
            }
        } catch (ClosedChannelException e) {
            Activator.getDefault().logError(e.getMessage(), e);
        }

        ret = total / getSHT().getNodeCount();
        /* The return value should be a percentage */
        if (ret < 0 || ret > 100) {
            throw new IllegalStateException("Average node usage is not a percentage: " + ret); //$NON-NLS-1$
        }
        return (int) ret;
    }

}
