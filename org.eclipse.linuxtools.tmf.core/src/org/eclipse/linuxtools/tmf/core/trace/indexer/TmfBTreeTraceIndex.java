/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.trace.indexer;

import java.io.File;

import org.eclipse.linuxtools.internal.tmf.core.trace.indexer.BTree;
import org.eclipse.linuxtools.internal.tmf.core.trace.indexer.FlatArray;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.ITmfCheckpoint;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.ITmfCheckpointIndex;

/**
 * A checkpoint index that uses a BTree to store and search checkpoints by time stamps.
 * It's possible to have the checkpoints time stamps in a different order than their checkpoint ranks.
 * Because of that, we use a separate structure FlatArray that is better suited for searching
 * by checkpoint rank (O(1)).
 *
 * @since 3.0
 * @author Marc-Andre Laperle
 */
public class TmfBTreeTraceIndex implements ITmfCheckpointIndex {

    private final BTree fCheckpoints;
    private final FlatArray fCheckpointRanks;

    private static final int BTREE_DEGREE = 15;

    /**
     * Creates an index for the given trace
     *
     * @param trace the trace
     */
    public TmfBTreeTraceIndex(ITmfTrace trace) {
        BTree bTree = createBTree(trace);
        FlatArray flatArray = createFlatArray(trace);

        // If one of the files is created from scratch, make sure we rebuild the other one too
        if (bTree.isCreatedFromScratch() != flatArray.isCreatedFromScratch()) {
            bTree.delete();
            flatArray.delete();
            bTree = createBTree(trace);
            flatArray = createFlatArray(trace);
        }

        fCheckpoints = bTree;
        fCheckpointRanks = flatArray;
    }

    private static FlatArray createFlatArray(ITmfTrace trace) {
        return new FlatArray(getIndexFile(trace, FlatArray.INDEX_FILE_NAME), (ITmfPersistentlyIndexable)trace);
    }

    private static BTree createBTree(ITmfTrace trace) {
        return new BTree(BTREE_DEGREE, getIndexFile(trace, BTree.INDEX_FILE_NAME), (ITmfPersistentlyIndexable)trace);
    }

    private static File getIndexFile(ITmfTrace trace, String fileName) {
        String directory = TmfTraceManager.getSupplementaryFileDir(trace);
        return new File(directory + fileName);
    }

    @Override
    public void dispose() {
        fCheckpoints.dispose();
        fCheckpointRanks.dispose();
    }

    @Override
    public void insert(ITmfCheckpoint checkpoint) {
        fCheckpoints.insert(checkpoint);
        fCheckpointRanks.insert(checkpoint);
        fCheckpoints.setSize(fCheckpoints.size() + 1);
    }

    @Override
    public ITmfCheckpoint get(long checkpoint) {
        return fCheckpointRanks.get(checkpoint);
    }

    @Override
    public long binarySearch(ITmfCheckpoint checkpoint) {
        return fCheckpoints.binarySearch(checkpoint);
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public int size() {
        return fCheckpoints.size();
    }

    @Override
    public boolean isCreatedFromScratch() {
        return fCheckpoints.isCreatedFromScratch();
    }

    @Override
    public void setTimeRange(TmfTimeRange timeRange) {
        fCheckpoints.setTimeRange(timeRange);
    }

    @Override
    public void setNbEvents(long nbEvents) {
        fCheckpoints.setNbEvents(nbEvents);
    }

    @Override
    public TmfTimeRange getTimeRange() {
        return fCheckpoints.getTimeRange();
    }

    @Override
    public long getNbEvents() {
        return fCheckpoints.getNbEvents();
    }

    @Override
    public void setIndexComplete() {
        fCheckpoints.setIndexComplete();
        fCheckpointRanks.setIndexComplete();
    }

}
