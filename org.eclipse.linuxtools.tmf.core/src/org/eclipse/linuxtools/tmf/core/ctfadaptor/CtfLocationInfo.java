/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.core.ctfadaptor;

import java.nio.ByteBuffer;

/**
 * The data object to go in a {@link CtfLocation}.
 *
 * @author Matthew Khouzam
 * @since 2.0
 */
public class CtfLocationInfo implements Comparable<CtfLocationInfo> {

    private final long timestamp;
    private final long index;

    /**
     * @param ts
     *            Timestamp
     * @param index
     *            Index of this event (if there are N elements with the same
     *            timestamp, which one is it.)
     */
    public CtfLocationInfo(long ts, long index) {
        this.timestamp = ts;
        this.index = index;
    }

    /**
     * Construct the location from the ByteBuffer.
     *
     * @param bufferIn
     *            the buffer to read from
     *
     * @since 3.0
     */
    public CtfLocationInfo(ByteBuffer bufferIn) {
        timestamp = bufferIn.getLong();
        index = bufferIn.getLong();
    }

    /**
     * @return The timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @return The index of the element
     */
    public long getIndex() {
        return index;
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + (int) (index ^ (index >>> 32));
        result = (prime * result) + (int) (timestamp ^ (timestamp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof CtfLocationInfo)) {
            return false;
        }
        CtfLocationInfo other = (CtfLocationInfo) obj;
        if (index != other.index) {
            return false;
        }
        if (timestamp != other.timestamp) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Element [" + timestamp + '/' + index + ']'; //$NON-NLS-1$
    }

    // ------------------------------------------------------------------------
    // Comparable
    // ------------------------------------------------------------------------

    @Override
    public int compareTo(CtfLocationInfo other) {
        if (this.timestamp > other.getTimestamp()) {
            return 1;
        }
        if (this.timestamp < other.getTimestamp()) {
            return -1;
        }
        if (this.index > other.getIndex()) {
            return 1;
        }
        if (this.index < other.getIndex()) {
            return -1;
        }
        return 0;
    }

    /**
     * Write the location to the ByteBuffer so that it can be saved to disk.
     *
     * @param bufferOut
     *            the buffer to write to
     *
     * @since 3.0
     */
    public void serialize(ByteBuffer bufferOut) {
        bufferOut.putLong(timestamp);
        bufferOut.putLong(index);
    }
}
