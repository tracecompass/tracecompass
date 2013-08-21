/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Patrick Tasse - Updated for location in checkpoint
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint;

import java.nio.ByteBuffer;

import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.location.ITmfLocation;

/**
 * The basic trace checkpoint structure in TMF. The purpose of the checkpoint is
 * to associate a trace location to an event timestamp.
 * *
 * @see ITmfTimestamp
 * @see ITmfLocation
 *
 * @author Francois Chouinard
 * @since 3.0
 */
public interface ITmfCheckpoint extends Comparable<ITmfCheckpoint> {

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * The maximum size of the serialize buffer when determining the checkpoint
     * size
     */
    static final int MAX_SERIALIZE_SIZE = 1024;

    /**
     * @return the timestamp of the event referred to by the context
     * @since 2.0
     */
    ITmfTimestamp getTimestamp();

    /**
     * @return the location of the event referred to by the checkpoint
     */
    ITmfLocation getLocation();

    // ------------------------------------------------------------------------
    // Comparable
    // ------------------------------------------------------------------------

    @Override
    int compareTo(ITmfCheckpoint checkpoint);

    /**
     * Returns the checkpoint rank for this checkpoint. The checkpoint rank can
     * be seen as the index of the checkpoint in the order it was added.
     *
     * @return the checkpoint rank for this checkpoint
     * @since 3.0
     */
    long getCheckpointRank();

    /**
     * Write the checkpoint to the ByteBuffer so that it can be saved to disk.
     *
     * @param bufferOut
     *            the buffer to write to
     *
     * @since 3.0
     */
    void serialize(ByteBuffer bufferOut);
}
