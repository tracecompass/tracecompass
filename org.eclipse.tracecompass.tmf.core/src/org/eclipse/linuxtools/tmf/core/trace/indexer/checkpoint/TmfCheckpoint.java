/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Updated as per TMF Trace Model 1.0
 *   Patrick Tasse - Updated for location in checkpoint
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint;

import java.nio.ByteBuffer;

import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.location.ITmfLocation;

/**
 * A basic implementation of ITmfCheckpoint. It simply maps an event timestamp
 * to a generic location.
 *
 * @see ITmfLocation
 * @see ITmfTimestamp
 *
 * @author Francois Chouinard
 * @since 3.0
 */
public class TmfCheckpoint implements ITmfCheckpoint {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // The checkpoint location
    private final ITmfLocation fLocation;

    // The checkpoint timestamp
    private final ITmfTimestamp fTimestamp;

    private final long fCheckpointRank;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Full constructor
     *
     * @param timestamp
     *            the checkpoint timestamp
     * @param location
     *            the corresponding trace location
     * @param checkpointRank
     *            the rank of the checkpoint
     * @since 3.0
     */
    public TmfCheckpoint(final ITmfTimestamp timestamp, final ITmfLocation location, long checkpointRank) {
        fTimestamp = timestamp;
        fLocation = location;
        fCheckpointRank = checkpointRank;
    }

    /**
     * Constructs a checkpoint using also a byte buffer to read the rank from
     * disk.
     *
     * @param timestamp
     *            the checkpoint timestamp
     * @param location
     *            the corresponding trace location
     * @param bufferIn
     *            the byte buffer to read from
     *
     * @since 3.0
     */
    public TmfCheckpoint(final ITmfTimestamp timestamp, final ITmfLocation location, ByteBuffer bufferIn) {
        fTimestamp = timestamp;
        fLocation = location;
        fCheckpointRank = bufferIn.getLong();
    }

    /**
     * Copy constructor
     *
     * @param other the other checkpoint
     */
    public TmfCheckpoint(final TmfCheckpoint other) {
        if (other == null) {
            throw new IllegalArgumentException();
        }
        fTimestamp = other.fTimestamp;
        fLocation = other.fLocation;
        fCheckpointRank = other.fCheckpointRank;
    }

    // ------------------------------------------------------------------------
    // ITmfCheckpoint
    // ------------------------------------------------------------------------

    /**
     * @since 2.0
     */
    @Override
    public ITmfTimestamp getTimestamp() {
        return fTimestamp;
    }

    @Override
    public ITmfLocation getLocation() {
        return fLocation;
    }

    // ------------------------------------------------------------------------
    // Comparable
    // ------------------------------------------------------------------------

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public int compareTo(final ITmfCheckpoint other) {
        int comp = 0;
        if ((fTimestamp != null) && (other.getTimestamp() != null)) {
            comp = fTimestamp.compareTo(other.getTimestamp(), false);
            if (comp != 0) {
                return comp;
            }
            // compare locations if timestamps are the same
        }

        if ((fLocation == null) && (other.getLocation() == null)) {
            return 0;
        }

        // treat location of other as null location which is before any location
        if ((fLocation != null) && (other.getLocation() == null)) {
            return 1;
        }

        // treat this as null location which is before any other locations
        if ((fLocation == null) && (other.getLocation() != null)) {
            return -1;
        }

        // compare location
        final Comparable location1 = getLocation().getLocationInfo();
        final Comparable location2 = other.getLocation().getLocationInfo();
        return location1.compareTo(location2);
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fLocation == null) ? 0 : fLocation.hashCode());
        result = prime * result + ((fTimestamp == null) ? 0 : fTimestamp.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TmfCheckpoint)) {
            return false;
        }
        final TmfCheckpoint other = (TmfCheckpoint) obj;
        if (fLocation == null) {
            if (other.fLocation != null) {
                return false;
            }
        } else if (!fLocation.equals(other.fLocation)) {
            return false;
        }
        if (fTimestamp == null) {
            if (other.fTimestamp != null) {
                return false;
            }
        } else if (!fTimestamp.equals(other.fTimestamp)) {
            return false;
        }
        return true;
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return getClass().getSimpleName() + " [fLocation=" + fLocation + ", fTimestamp=" + fTimestamp + ", fCheckpointRank=" + fCheckpointRank + "]";
    }

    /**
     * @since 3.0
     */
    @Override
    public void serialize(ByteBuffer bufferOut) {
        fLocation.serialize(bufferOut);
        // Always serialize as base TmfTimestamp, this should be sufficient for indexing.
        // If not, we can add API for the test to restore the time stamp, similarly to the location.
        TmfTimestamp t = new TmfTimestamp(fTimestamp);
        t.serialize(bufferOut);
        bufferOut.putLong(fCheckpointRank);
    }

    /**
     * @since 3.0
     */
    @Override
    public long getCheckpointRank() {
        return fCheckpointRank;
    }
}
