/**********************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial implementation and API
 *   Marc-Andre Laperle - Initial implementation and API
 **********************************************************************/

package org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * An Lttng packet index
 *
 * @author Matthew Khouzam
 * @since 3.0
 */
public class IndexResponse implements IRelayResponse {

    /**
     * Command size
     *
     * Sum of the field sizes / 8 ( 7 longs and 2 ints):
     * fOffset + fPacketSize + fContentSize + fTimestampBegin + fTimestampEnd +
     * fEventsDiscarded + fStreamId + fStatus + fFlags
     */
    public final static int SIZE =
            (Long.SIZE * 7 + Integer.SIZE * 2) / 8;

    /** the offset */
    private final long fOffset;
    /** packet_size */
    private final long fPacketSize;
    /** the content size - how much of the packet is used */
    private final long fContentSize;
    /** timestamp of the beginning of the packet */
    private final long fTimestampBegin;
    /** timestamp of the end of the packet */
    private final long fTimestampEnd;
    /** number of discarded events BEFORE this packet */
    private final long fEventsDiscarded;
    /** the CTF stream id */
    private final long fStreamId;
    /** the status of the getNextIndex request */
    private final NextIndexReturnCode fStatus;
    /** whether there are new streams or metadata */
    private final int fFlags;

    /**
     * IndexResposne from network
     *
     * @param inNet
     *            data input stream
     * @throws IOException
     *             network error
     */
    public IndexResponse(DataInputStream inNet) throws IOException {
        byte[] data = new byte[SIZE];
        inNet.readFully(data);
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.BIG_ENDIAN);
        fOffset = bb.getLong();
        fPacketSize = bb.getLong();
        fContentSize = bb.getLong();
        fTimestampBegin = bb.getLong();
        fTimestampEnd = bb.getLong();
        fEventsDiscarded = bb.getLong();
        fStreamId = bb.getLong();
        fStatus = NextIndexReturnCode.values()[bb.getInt() - 1];
        fFlags = bb.getInt();
    }

    /**
     * Gets the offset
     *
     * @return the offset
     */
    public long getOffset() {
        return fOffset;
    }

    /**
     * Gets the packet size
     *
     * @return the packet size
     */
    public long getPacketSize() {
        return fPacketSize;
    }

    /**
     * Gets the content size - how much of the packet is used
     *
     * @return the content size
     */
    public long getContentSize() {
        return fContentSize;
    }

    /**
     * Gets the timestamp of the beginning of the packet
     *
     * @return the timestamp of the beginning of the packet
     */
    public long getTimestampBegin() {
        return fTimestampBegin;
    }

    /**
     * Gets the timestamp of the end of the packet
     *
     * @return the timestamp of the end of the packet
     */
    public long getTimestampEnd() {
        return fTimestampEnd;
    }

    /**
     * Gets the number of discarded events BEFORE this packet
     *
     * @return the number of discarded events BEFORE this packet
     */
    public long getEventsDiscarded() {
        return fEventsDiscarded;
    }

    /**
     * Gets the CTF stream id
     *
     * @return the CTF stream id
     */
    public long getStreamId() {
        return fStreamId;
    }

    /**
     * Gets the status
     *
     * @return the status
     */
    public NextIndexReturnCode getStatus() {
        return fStatus;
    }

    /**
     * Gets the flags that describe whether there are new streams or metadata
     *
     * @return the flags
     */
    public int getFlags() {
        return fFlags;
    }

}