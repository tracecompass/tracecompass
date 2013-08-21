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

import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.ITmfCheckpoint;

/**
 * A common interface for collections containing checkpoints
 *
 * @author Marc-Andre Laperle
 */
public interface ICheckpointCollection {

    /**
     * Insert a checkpoint into the collection
     *
     * @param checkpoint
     *            the checkpoint to insert
     */
    void insert(ITmfCheckpoint checkpoint);

    /**
     * Search for a checkpoint and return the rank.
     *
     * @param checkpoint
     *            the checkpoint to search
     * @return the checkpoint rank of the searched checkpoint, if it is
     *         contained in the index; otherwise, (-(insertion point) - 1).
     */
    long binarySearch(ITmfCheckpoint checkpoint);

    /**
     * @return true if the collection was created from scratch, false otherwise
     */
    boolean isCreatedFromScratch();

    /**
     * Returns the size of the collection expressed as a number of checkpoints.
     *
     * @return the size of the collection
     */
    int size();

    /**
     * Set the trace time range
     *
     * @param timeRange
     *            the trace time range
     */
    void setTimeRange(TmfTimeRange timeRange);

    /**
     * Get the trace time range
     *
     * @return the trace time range
     */
    TmfTimeRange getTimeRange();

    /**
     * Set the number of events in the trace
     *
     * @param nbEvents
     *            the number of events in the trace
     */
    void setNbEvents(long nbEvents);

    /**
     * Get the number of events in the trace
     *
     * @return the number of events in the trace
     */
    long getNbEvents();

    /**
     * Set the index as complete. No more checkpoints will be inserted.
     */
    void setIndexComplete();

    /**
     * Dispose the collection and delete persistent data (file)
     */
    void delete();

    /**
     * Dispose the structure and its resources
     */
    void dispose();
}
