/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.ctf.core.trace;

/**
 * CTF Packet descriptor, can come from a packet header or an index file, this
 * will show certain information about the packet such as the size and
 * timerange.
 *
 * @since 1.0
 */
public interface ICTFPacketDescriptor {

    /**
     * Returns whether the packet includes (inclusively) the given timestamp in
     * the begin-end timestamp range.
     *
     * @param ts
     *            The timestamp to check.
     * @return True if the packet includes the timestamp.
     */
    boolean includes(long ts);

    /**
     * Gets the offset of a packet within a stream in bits
     *
     * @return the offset bits
     */
    long getOffsetBits();

    /**
     * Gets the size of the packet in bits. If you have a 1mb packet that is 499kb
     * used and the header is 1kb, this will return 1mb
     *
     * @return the packetSizeBits
     */
    long getPacketSizeBits();

    /**
     * Get the content size of the packet in bits. If you have a 1mb packet that is 499kb
     * used and the header is 1kb, this will return 500kb (used data + header
     *
     * @return the contentSizeBits
     */
    long getContentSizeBits();

    /**
     * Gets the beginning timestamp of the packet, all events within the packet will have timestamps after or at this time
     *
     * @return the timestampBegin
     */
    long getTimestampBegin();

    /**
     * Gets the ending timestamp of the packet, all events within the packet will have timestamps before or at this time
     *
     * @return the timestampEnd
     */
    long getTimestampEnd();

    /**
     * Gets the number of lost events in this packet
     *
     * @return the lostEvents in this packet
     */
    long getLostEvents();

    /**
     * Retrieve the value of an existing attribute
     *
     * @param field
     *            The name of the attribute
     * @return The value that was stored, or null if it wasn't found
     */
    Object lookupAttribute(String field);

    /**
     * Get the target of the packet (what device generated this packet)
     *
     * @return The target that is being traced
     */
    String getTarget();

    /**
     * Get the id of the target of the packet (a number helper)
     *
     * @return The ID of the target
     */
    long getTargetId();

    /**
     * Get the offset of the packet in bytes within the stream
     *
     * @return The offset of the packet in bytes
     */
    long getOffsetBytes();

}