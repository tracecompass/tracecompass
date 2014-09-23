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
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Response to a "new streams" command
 *
 * @author Matthew Khouzam
 * @since 3.0
 */
public class NewStreamsResponse implements IRelayResponse {

    /**
     * Response size
     *
     * fStatus + fNbStreams
     */
    private static final int SIZE = (Integer.SIZE + Integer.SIZE) / 8;
    /** status of the request */
    private final NewStreamsReturnCode fStatus;
    /** the number of streams */
    private final int fNbStreams;
    /** the list of streams in the response */
    private final List<StreamResponse> fStreamList;

    /**
     * New stream response network constructor
     *
     * @param inNet
     *            network stream
     * @throws IOException
     *             network error
     */
    public NewStreamsResponse(DataInputStream inNet) throws IOException {
        byte[] data = new byte[SIZE];
        inNet.readFully(data);
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.BIG_ENDIAN);
        fStatus = NewStreamsReturnCode.values()[bb.getInt() - 1];
        fNbStreams = bb.getInt();
        ImmutableList.Builder<StreamResponse> sl = new ImmutableList.Builder<>();
        if (getStatus().equals(NewStreamsReturnCode.LTTNG_VIEWER_NEW_STREAMS_OK)) {
            for (int stream = 0; stream < fNbStreams; stream++) {
                sl.add(new StreamResponse(inNet));
            }
        }
        fStreamList = sl.build();
    }

    /**
     * Gets the status
     *
     * @return the status
     */
    public NewStreamsReturnCode getStatus() {
        return fStatus;
    }

    /**
     * gets the stream list
     *
     * @return the stream list
     */
    public List<StreamResponse> getStreamList() {
        return fStreamList;
    }

    /**
     * The number of streams
     *
     * @return the number of streams
     */
    public int getNbStreams() {
        return fNbStreams;
    }

}