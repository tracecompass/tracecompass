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
 * Response to getpacket command
 *
 * @author Matthew Khouzam
 * @since 3.0
 */
public class TracePacketResponse implements IRelayResponse {

    private static final int SIZE = (Integer.SIZE + Integer.SIZE) / 8;
    /** Enum lttng_viewer_get_packet_return_code */
    private final GetPacketReturnCode fStatus;
    /** flags: is there new metadata or new streams? */
    private final int fFlags;
    /** the packet */
    private final byte[] fData;

    /**
     * Trace packet response network constructor
     *
     * @param inNet
     *            network input stream
     * @throws IOException
     *             network error
     */
    public TracePacketResponse(DataInputStream inNet) throws IOException {
        byte[] data = new byte[SIZE];
        inNet.readFully(data);
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.BIG_ENDIAN);
        fStatus = GetPacketReturnCode.values()[bb.getInt() - 1];
        int length = bb.getInt();
        fFlags = bb.getInt();
        if (fStatus.equals(GetPacketReturnCode.VIEWER_GET_PACKET_OK)) {
            fData = new byte[length];
            inNet.readFully(fData);
        } else {
            fData = new byte[0];
        }
    }

    /**
     * Get the status
     *
     * @return the Status
     */
    public GetPacketReturnCode getStatus() {
        return fStatus;
    }

    /**
     * Get the flags
     *
     * @return the Flags
     */
    public int getFlags() {
        return fFlags;
    }

    /**
     * Get the packet data, please do not modify the data
     *
     * @return the Data
     */
    public byte[] getData() {
        return fData;
    }

}