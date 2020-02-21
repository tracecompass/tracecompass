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

package org.eclipse.tracecompass.internal.tmf.core.trace.indexer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.indexer.checkpoint.ITmfCheckpoint;
import org.eclipse.tracecompass.tmf.core.trace.indexer.checkpoint.ITmfCheckpointIndex;

/**
 * A checkpoint index that store all checkpoints in memory.
 *
 * @author Marc-Andre Laperle
 */
public class TmfMemoryIndex implements ITmfCheckpointIndex, ICheckpointCollection {

    private final List<ITmfCheckpoint> fCheckpoints;
    private TmfTimeRange fTimeRange;
    private long fNbEvents;

    /**
     * Creates an index for the given trace
     *
     * @param trace the trace
     */
    public TmfMemoryIndex(ITmfTrace trace) {
        fCheckpoints = new ArrayList<>();
        fTimeRange = new TmfTimeRange(TmfTimestamp.ZERO, TmfTimestamp.ZERO);
        fNbEvents = 0;
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
        fTimeRange = timeRange;
    }

    @Override
    public void setNbEvents(long nbEvents) {
        fNbEvents = nbEvents;
    }

    @Override
    public TmfTimeRange getTimeRange() {
        return fTimeRange;
    }

    @Override
    public long getNbEvents() {
        return fNbEvents;
    }

    @Override
    public void delete() {
        // Do nothing
    }
}
