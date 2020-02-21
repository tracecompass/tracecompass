/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.trace.indexer;

import java.io.File;

import org.eclipse.tracecompass.internal.tmf.core.trace.indexer.FlatArray;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.indexer.checkpoint.ITmfCheckpoint;
import org.eclipse.tracecompass.tmf.core.trace.indexer.checkpoint.ITmfCheckpointIndex;

/**
 * <p>A checkpoint index that uses a FlatArray to store and search checkpoints by
 * time stamps and by checkpoint rank.</p>
 *
 * <p>Note: This index alone will not work for
 * traces that have events with time stamps that are out of order.</p>
 *
 * @author Marc-Andre Laperle
 */
public class TmfFlatArrayTraceIndex implements ITmfCheckpointIndex {

    private final FlatArray fCheckpoints;

    /**
     * Creates an index for the given trace
     *
     * @param trace the trace
     */
    public TmfFlatArrayTraceIndex(ITmfTrace trace) {
        fCheckpoints = new FlatArray(getIndexFile(trace, FlatArray.INDEX_FILE_NAME), (ITmfPersistentlyIndexable)trace);
    }

    private static File getIndexFile(ITmfTrace trace, String fileName) {
        String directory = TmfTraceManager.getSupplementaryFileDir(trace);
        return new File(directory + fileName);
    }

    @Override
    public void dispose() {
        fCheckpoints.dispose();
    }

    @Override
    public void insert(ITmfCheckpoint checkpoint) {
        fCheckpoints.insert(checkpoint);
    }

    @Override
    public ITmfCheckpoint get(long checkpoint) {
        return fCheckpoints.get(checkpoint);
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
}
