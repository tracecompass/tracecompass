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
 * Metadata packet response containing a packet of metadata
 *
 * @author Matthew Khouzam
 * @since 3.0
 */
public class MetadataPacketResponse implements IRelayResponse {

    /**
     * Response size
     *
     * fData.length + fStatus
     */
    private static final int SIZE = (Long.SIZE + Integer.SIZE) / 8;
    /** status of the metadata request */
    private final GetMetadataReturnCode fStatus;
    /** the packet */
    private final byte fData[];

    /**
     * Read new metadata packet from the network
     *
     * @param inNet
     *            network input reader
     * @throws IOException
     *             network error
     */
    public MetadataPacketResponse(DataInputStream inNet) throws IOException {
        byte[] data = new byte[SIZE];
        inNet.readFully(data);
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.BIG_ENDIAN);
        long length = bb.getLong();
        fStatus = GetMetadataReturnCode.values()[bb.getInt() - 1];
        if (length >= Integer.MAX_VALUE) {
            throw new IOException("Metadata Packet too big " + length); //$NON-NLS-1$
        }
        fData = new byte[(int) length];
        inNet.readFully(fData);
    }

    /**
     * Get the packet
     *
     * @return the packet
     */
    public byte[] getData() {
        return fData;
    }

    /**
     * Gets the status
     *
     * @return the status
     */
    public GetMetadataReturnCode getStatus() {
        return fStatus;
    }
}