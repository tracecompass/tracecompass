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
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.common.core.log.TraceCompassLog;
import org.eclipse.tracecompass.internal.provisional.datastore.core.condition.RangeCondition;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.IHistoryTree;
import org.eclipse.tracecompass.statesystem.core.backend.IStateHistoryBackend;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;

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
    private final @NonNull IHistoryTree<@NonNull StateSystemInterval> fSht;

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
            @NonNull File newStateFile,
            int providerVersion,
            long startTime,
            int blockSize,
            int maxChildren) throws IOException {

        fSsid = ssid;
        fSht = initializeSHT(newStateFile,
                blockSize,
                maxChildren,
                providerVersion,
                startTime);
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
    public HistoryTreeBackend(@NonNull String ssid, @NonNull File newStateFile,
            int providerVersion, long startTime)
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
     * Instantiate the new history tree to be used.
     *
     * @param stateHistoryFile
     *            The name of the history file
     * @param blockSize
     *            The size of each "block" on disk in bytes. One node will
     *            always fit in one block. It should be at least 4096.
     * @param maxChildren
     *            The maximum number of children allowed per core (non-leaf)
     *            node.
     * @param providerVersion
     *            The version of the state provider. If a file already exists,
     *            and their versions match, the history file will not be rebuilt
     *            uselessly.
     * @param treeStart
     *            The start time of the history
     * @return The new history tree
     * @throws IOException
     *             If an error happens trying to open/write to the file
     *             specified in the config
     */
    @VisibleForTesting
    protected @NonNull IHistoryTree<@NonNull StateSystemInterval> initializeSHT(
            @NonNull File stateHistoryFile,
            int blockSize,
            int maxChildren,
            int providerVersion,
            long treeStart) throws IOException {

        return HistoryTreeFactory.createHistoryTree(stateHistoryFile,
                blockSize,
                maxChildren,
                providerVersion,
                treeStart);
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
    protected @NonNull IHistoryTree<@NonNull StateSystemInterval>initializeSHT(
            @NonNull File existingStateFile, int providerVersion) throws IOException {
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
    protected final @NonNull IHistoryTree<@NonNull StateSystemInterval> getSHT() {
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
        StateSystemInterval interval = new StateSystemInterval(stateStartTime, stateEndTime,
                quark, (TmfStateValue) value);

        /* Start insertions at the "latest leaf" */
        getSHT().insert(interval);
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

        RangeCondition<@NonNull Long> rc = RangeCondition.singleton(t);

        Iterable<StateSystemInterval> intervals = getSHT().getMatchingIntervals(rc, e -> true);
        Iterables.filter(intervals, e -> e.getAttribute() < stateInfo.size())
                .forEach(e -> stateInfo.set(e.getAttribute(), e));
    }

    @Override
    public ITmfStateInterval doSingularQuery(long t, int attributeQuark)
            throws TimeRangeException, StateSystemDisposedException {
            checkValidTime(t);

            RangeCondition<@NonNull Long> rc = RangeCondition.singleton(t);

            Iterable<StateSystemInterval> intervals = getSHT()
                    .getMatchingIntervals(rc, e -> e.getAttribute() == attributeQuark);

            return Iterables.getFirst(intervals, null);
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
        return 0;
        // TODO Reimplement, elsewhere?

//        HTNode node;
//        long total = 0;
//        long ret;
//
//        try {
//            for (int seq = 0; seq < getSHT().getNodeCount(); seq++) {
//                node = getSHT().readNode(seq);
//                total += node.getNodeUsagePercent();
//            }
//        } catch (ClosedChannelException e) {
//            Activator.getDefault().logError(e.getMessage(), e);
//        }
//
//        ret = total / getSHT().getNodeCount();
//        /* The return value should be a percentage */
//        if (ret < 0 || ret > 100) {
//            throw new IllegalStateException("Average node usage is not a percentage: " + ret); //$NON-NLS-1$
//        }
//        return (int) ret;
    }

}
