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
 * Get viewer session response to command
 *
 * @author Matthew Khouzam
 * @since 3.0
 */
public class SessionResponse implements IRelayResponse {

    /**
     * Response size
     */
    public static final int SIZE =
            LttngViewerCommands.LTTNG_VIEWER_HOST_NAME_MAX + LttngViewerCommands.LTTNG_VIEWER_NAME_MAX + (Long.SIZE + Integer.SIZE + Integer.SIZE + Integer.SIZE) / 8;
    /** id of the session */
    private final long fId;
    /** live timer */
    private final int fLiveTimer;
    /** number of clients */
    private final int fClients;
    /** number streams */
    private final int fStreams;
    /** Hostname, like 'localhost' */
    private final String fHostname;
    /** Session name, like 'streaming session' */
    private final String fSessionName;

    /**
     * Session response network constructor
     *
     * @param inNet
     *            input network stream
     * @throws IOException
     *             network error
     */
    public SessionResponse(DataInputStream inNet) throws IOException {
        byte[] data = new byte[SIZE];
        inNet.readFully(data);
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.BIG_ENDIAN);
        fId = bb.getLong();
        fLiveTimer = bb.getInt();
        fClients = bb.getInt();
        fStreams = bb.getInt();
        byte[] hostName = new byte[LttngViewerCommands.LTTNG_VIEWER_HOST_NAME_MAX];
        byte[] sessionName = new byte[LttngViewerCommands.LTTNG_VIEWER_NAME_MAX];
        bb.get(hostName, 0, hostName.length);
        bb.get(sessionName, 0, sessionName.length);
        fHostname = new String(hostName);
        fSessionName = new String(sessionName);
    }

    /**
     * Gets the id of the session
     *
     * @return the id of the session
     */
    public long getId() {
        return fId;
    }

    /**
     * Gets the live timer
     *
     * @return the live timer
     */
    public int getLiveTimer() {
        return fLiveTimer;
    }

    /**
     * Gets the number of clients
     *
     * @return the number of clients
     */
    public int getClients() {
        return fClients;
    }

    /**
     * Gets the number streams
     *
     * @return the number streams
     */
    public int getStreams() {
        return fStreams;
    }

    /**
     * Gets the Hostname
     *
     * @return the Hostname
     */
    public String getHostname() {
        return fHostname;
    }

    /**
     * Gets the session name
     *
     * @return the session name
     */
    public String getSessionName() {
        return fSessionName;
    }

}