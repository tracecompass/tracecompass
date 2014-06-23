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
 * VIEWER_GET_NEXT_INDEX payload.
 *
 * @author Matthew Khouzam
 * @since 3.0
 */
public class GetNextIndex implements IRelayCommand {

    /**
     * Command size (fStreamId)
     */
    public static final int SIZE = Long.SIZE / 8;
    /**
     * the id of the stream
     */
    private final long fStreamId;

    /**
     * Constructor
     *
     * @param streamId
     *            the index stream id
     */
    public GetNextIndex(long streamId) {
        fStreamId = streamId;
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
        return data;
    }

}