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
 * Get response of viewer stream
 *
 * @author Matthew Khouzam
 * @since 3.0
 */
public class StreamResponse implements IRelayResponse {

    /**
     * Response size
     */
    public static final int SIZE = (Long.SIZE + Long.SIZE + Integer.SIZE) / 8 + LttngViewerCommands.LTTNG_VIEWER_PATH_MAX + LttngViewerCommands.LTTNG_VIEWER_NAME_MAX;

    /**
     * id of the stream
     */
    private final long fId;
    /**
     * It is guaranteed to be unique, because the value is assigned sequentially
     * by the relay.
     */
    private final long fCtfTraceId;
    /**
     * if the stream is a metadata stream
     */
    private final int fMetadataFlag;
    /**
     * the path
     */
    private final String fPathName;
    /**
     * The channel, traditionally channel0
     */
    private final String fChannelName;

    /**
     * Stream response
     *
     * @param inNet
     *            input data stream
     * @throws IOException
     *             network time
     */
    public StreamResponse(DataInputStream inNet) throws IOException {
        byte[] streamData = new byte[SIZE];
        inNet.readFully(streamData, 0, SIZE);
        ByteBuffer bb = ByteBuffer.wrap(streamData);
        bb.order(ByteOrder.BIG_ENDIAN);
        fId = (bb.getLong());
        fCtfTraceId = bb.getLong();
        fMetadataFlag = bb.getInt();
        byte pathName[] = new byte[LttngViewerCommands.LTTNG_VIEWER_PATH_MAX];
        byte channelName[] = new byte[LttngViewerCommands.LTTNG_VIEWER_NAME_MAX];
        bb.get(pathName, 0, LttngViewerCommands.LTTNG_VIEWER_PATH_MAX);
        bb.get(channelName, 0, LttngViewerCommands.LTTNG_VIEWER_NAME_MAX);
        fPathName = new String(pathName);
        fChannelName = new String(channelName);
    }

    /**
     * Get the id
     *
     * @return the Id
     */
    public long getId() {
        return fId;
    }

    /**
     * Get the CtfTraceId
     *
     * @return the CtfTraceId
     */
    public long getCtfTraceId() {
        return fCtfTraceId;
    }

    /**
     * Get the metadata flag
     *
     * @return the MetadataFlag
     */
    public int getMetadataFlag() {
        return fMetadataFlag;
    }

    /**
     * Get the path name
     *
     * @return the PathName
     */
    public String getPathName() {
        return fPathName;
    }

    /**
     * get the Channel name
     *
     * @return the ChannelName
     */
    public String getChannelName() {
        return fChannelName;
    }

}