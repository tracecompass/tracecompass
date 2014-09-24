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

package org.eclipse.linuxtools.internal.tmf.core.trace.indexer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.ITmfCheckpoint;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.ITmfCheckpointIndex;

/**
 * A checkpoint index that store all checkpoints in memory.
 *
 * @since 3.0
 * @author Marc-Andre Laperle
 */
public class TmfMemoryIndex implements ITmfCheckpointIndex, ICheckpointCollection {

    private final List<ITmfCheckpoint> fCheckpoints;

    /**
     * Creates an index for the given trace
     *
     * @param trace the trace
     */
    public TmfMemoryIndex(ITmfTrace trace) {
        fCheckpoints = new ArrayList<>();
    }

    @Override
    public void dispose() {
        fCheckpoints.clear();
    }

    @Override
    public void insert(ITmfCheckpoint checkpoint) {
        fCheckpoints.add(checkpoint);
    }

    @Override
    public ITmfCheckpoint get(long checkpoint) {
        return fCheckpoints.get((int)checkpoint);
    }

    @Override
    public long binarySearch(ITmfCheckpoint checkpoint) {
        return Collections.binarySearch(fCheckpoints, checkpoint);
    }

    @Override
    public boolean isEmpty() {
        return fCheckpoints.isEmpty();
    }

    @Override
    public int size() {
        return fCheckpoints.size();
    }

    @Override
    public boolean isCreatedFromScratch() {
        return true;
    }

    @Override
    public void setTimeRange(TmfTimeRange timeRange) {
    }

    @Override
    public void setNbEvents(long nbEvents) {
    }

    @Override
    public TmfTimeRange getTimeRange() {
        return null;
    }

    @Override
    public long getNbEvents() {
        return 0;
    }

    @Override
    public void setIndexComplete() {
    }

    @Override
    public void delete() {
    }
}
