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

package org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint;

import java.util.Collections;

import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;

/**
 * A trace index contains the data (checkpoints) needed by the indexer so that it can perform
 * its operations. Implementors can store checkpoints in various ways and
 * optionally restore them later, see ({@link #isCreatedFromScratch})
 *
 * @since 3.0
 * @author Marc-Andre Laperle
 */
public interface ITmfCheckpointIndex {

    /**
     * Add a checkpoint to the index
     *
     * @param checkpoint
     *            the checkpoint to add
     */
    void insert(ITmfCheckpoint checkpoint);

    /**
     * Get a checkpoint by checkpoint rank
     *
     * @param checkpointRank
     *            the checkpoint rank to search for
     * @return the checkpoint found for the given checkpoint rank
     */
    ITmfCheckpoint get(long checkpointRank);

    /**
     * Find the checkpoint rank of a checkpoint. Implementors must respect the
     * contract of {@link Collections#binarySearch}
     *
     * @param checkpoint
     *            the checkpoint to search for
     * @return the checkpoint rank of the searched checkpoint, if it is
     *         contained in the index; otherwise, (-(insertion point) - 1).
     */
    long binarySearch(ITmfCheckpoint checkpoint);

    /**
     * Returns whether or not the index is empty
     *
     * @return true if empty false otherwise
     */
    boolean isEmpty();

    /**
     * Returns the number of checkpoints in the index
     *
     * @return the number of checkpoints
     */
    int size();

    /**
     * Dispose the index and its resources
     */
    void dispose();

    /**
     * Returns whether or not the index was created from scratch. An index not
     * created from scratch was typically loaded from disk.
     *
     * @return true if the index was created from scratch, false otherwise
     */
    boolean isCreatedFromScratch();

    /**
     * Set trace time range to be stored in the index
     *
     * @param timeRange
     *            the time range to be stored in the index
     */
    void setTimeRange(TmfTimeRange timeRange);

    /**
     * Set the total number of events in the trace to be stored in the index
     *
     * @param nbEvents
     *            the total number of events
     */
    void setNbEvents(long nbEvents);

    /**
     * Get the trace time range stored in the index
     *
     * @return the trace time range
     */
    TmfTimeRange getTimeRange();

    /**
     * Get the total number of events in the trace stored in the index
     *
     * @return the total number of events
     */
    long getNbEvents();

    /**
     * Set the index as complete. No more checkpoints will be inserted.
     */
    void setIndexComplete();
}
