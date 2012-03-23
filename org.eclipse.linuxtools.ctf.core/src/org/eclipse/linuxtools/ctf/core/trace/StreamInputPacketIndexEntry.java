/*******************************************************************************
 * Copyright (c) 2011-2012 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.trace;

/**
 * <b><u>StreamInputPacketIndexEntry</u></b>
 * <p>
 * Represents an entry in the index of event packets.
 */
public class StreamInputPacketIndexEntry {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * Offset of the packet in the file, in bytes
     */
    public long offsetBytes;

    /**
     * Offset of the data in the packet, in bits
     */
    public int dataOffsetBits = 0;

    /**
     * Packet size, in bits
     */
    public int packetSizeBits = 0;

    /**
     * Content size, in bits
     */
    public int contentSizeBits = 0;

    /**
     * Begin timestamp
     */
    public long timestampBegin = 0;

    /**
     * End timestamp
     */
    public long timestampEnd = 0;


    public long indexBegin = 0;

    public long indexEnd = 0;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs an index entry.
     *
     * @param offset
     *            The offset of the packet in the file, in bytes.
     */

    public StreamInputPacketIndexEntry(long offset) {
        this.offsetBytes = offset;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Returns whether the packet includes (inclusively) the given timestamp in
     * the begin-end timestamp range.
     *
     * @param ts
     *            The timestamp to check.
     * @return True if the packet includes the timestamp.
     */
    boolean includes(long ts) {
        return (ts >= timestampBegin) && (ts <= timestampEnd);
    }

    boolean includesIndex(long index){
        return (index >= indexBegin) && (index <= indexEnd);
    }

    @Override
    public String toString() {
        /* Only for debugging, shouldn't be externalized */
        return "PacketIndexEntry [offset=" + offsetBytes + ", timestampBegin=" //$NON-NLS-1$ //$NON-NLS-2$
                + Long.toHexString(timestampBegin) + ',' + " timestampEnd=" //$NON-NLS-1$
                + Long.toHexString(timestampEnd) + ", dataOffset=" //$NON-NLS-1$
                + dataOffsetBits + ", packetSize=" + packetSizeBits //$NON-NLS-1$
                + ", contentSize=" + contentSizeBits + ']'; //$NON-NLS-1$
    }

}
