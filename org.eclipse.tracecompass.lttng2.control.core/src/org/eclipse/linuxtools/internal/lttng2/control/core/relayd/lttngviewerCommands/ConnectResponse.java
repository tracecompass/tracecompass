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
 * CONNECT payload.
 *
 * @author Matthew Khouzam
 * @since 3.0
 */
public class ConnectResponse implements IRelayResponse, IRelayCommand {

    /**
     * Response or command size
     *
     * fViewerSessionId + fMajor + fMinor + fType
     */
    public static final int SIZE = (Long.SIZE + Integer.SIZE + Integer.SIZE + Integer.SIZE) / 8;
    /** session id, counts from 1 and increments by session */
    private final long fViewerSessionId;
    /**
     * Major version, hint, it's at least 2
     */
    private final int fMajor;
    /**
     * Minor version, hint, it's at least 4
     */
    private final int fMinor;
    /**
     * type of connect to {@link ConnectionType}
     */
    private final ConnectionType fType;

    /**
     * Connection response reply constructor
     *
     * @param inStream
     *            the data input stream
     * @throws IOException
     *             a network error
     */
    public ConnectResponse(DataInputStream inStream) throws IOException {
        byte data[] = new byte[SIZE];
        inStream.readFully(data);
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.BIG_ENDIAN);
        fViewerSessionId = bb.getLong();
        fMajor = bb.getInt();
        fMinor = bb.getInt();
        bb.getInt(); // Should not be used, see http://bugs.lttng.org/issues/728
        fType = ConnectionType.VIEWER_CLIENT_COMMAND;
    }

    /**
     * Constructor for command
     *
     * @param sessionID
     *            session id
     * @param major
     *            the major version
     * @param minor
     *            the minor version
     * @param connection
     *            the connection type, typically VIEWER_CLIENT_COMMAND
     */
    public ConnectResponse(long sessionID, int major, int minor, ConnectionType connection) {
        fViewerSessionId = sessionID;
        fMajor = major;
        fMinor = minor;
        fType = connection;
    }

    /**
     * get the major version
     *
     * @return the major version
     */
    public int getMajor() {
        return fMajor;
    }

    /**
     * get the minor version
     *
     * @return the minor version
     */
    public int getMinor() {
        return fMinor;
    }

    @Override
    public byte[] serialize() {
        byte data[] = new byte[SIZE];
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putLong(fViewerSessionId);
        bb.putInt(fMajor);
        bb.putInt(fMinor);
        bb.putInt(fType.getCommand());
        return data;
    }

}