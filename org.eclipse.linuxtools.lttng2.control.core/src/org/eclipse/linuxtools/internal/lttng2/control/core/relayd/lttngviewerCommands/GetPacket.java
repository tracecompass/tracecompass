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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * VIEWER_GET_PACKET payload.
 *
 * @author Matthew Khouzam
 * @since 3.0
 */
public class GetPacket implements IRelayCommand {

    /**
     * Command size
     */
    public static final int SIZE = (Long.SIZE + Long.SIZE + Integer.SIZE) / 8;
    /** the stream Id */
    private final long fStreamId;
    /** the offset */
    private final long fOffset;
    /** the length of the packet */
    private final int fLength;

    /**
     * Get packet constructor
     *
     * @param streamId
     *            the stream id
     * @param offset
     *            the offset
     * @param length
     *            the packet length
     */
    public GetPacket(long streamId, long offset, int length) {
        fStreamId = streamId;
        fOffset = offset;
        fLength = length;
    }

    /**
     * Get the length of the packet
     *
     * @return the length of the packet in bytes
     */
    public int getLength() {
        return fLength;
    }

    /**
     * Gets the offset of the packet
     *
     * @return the offset
     */
    public long getOffset() {
        return fOffset;
    }

    /**
     * Gets the stream id
     *
     * @return the stream id
     */
    public long getStreamId() {
        return fStreamId;
    }

    @Override
    public byte[] serialize() {
        byte data[] = new byte[SIZE];
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putLong(getStreamId());
        bb.putLong(getOffset());
        bb.putInt(getLength());
        return data;
    }

}