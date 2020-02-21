/**********************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Matthew Khouzam - Initial implementation
 **********************************************************************/

package org.eclipse.tracecompass.internal.lttng2.control.core.relayd.impl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.eclipse.tracecompass.internal.lttng2.control.core.relayd.ILttngRelaydConnector;
import org.eclipse.tracecompass.internal.lttng2.control.core.relayd.commands.Command;
import org.eclipse.tracecompass.internal.lttng2.control.core.relayd.commands.ConnectResponse;
import org.eclipse.tracecompass.internal.lttng2.control.core.relayd.commands.ConnectionType;
import org.eclipse.tracecompass.internal.lttng2.control.core.relayd.commands.ViewerCommand;

/**
 * LTTng RelayD connector factory
 *
 * @author Matthew Khouzam
 */
public final class LttngRelaydConnectorFactory {

    private LttngRelaydConnectorFactory() {
    }

    /**
     * Create a connection to a relayd
     *
     * @param myConnection
     *            a connection to the relayd
     *
     * @return A relayd connector
     * @throws IOException
     *             caused by invalid sockets
     */
    public static ILttngRelaydConnector getNewConnector(Socket myConnection) throws IOException {
        DataOutputStream outNet = new DataOutputStream(myConnection.getOutputStream());
        DataInputStream inNet = new DataInputStream(myConnection.getInputStream());

        ViewerCommand connectCommand = new ViewerCommand(Command.VIEWER_CONNECT, ConnectResponse.SIZE, 0);

        outNet.write(connectCommand.serialize());
        outNet.flush();

        ConnectResponse payload = new ConnectResponse(0, 2, 4, ConnectionType.VIEWER_CLIENT_COMMAND);
        outNet.write(payload.serialize());
        outNet.flush();

        ConnectResponse connectReply = new ConnectResponse(inNet);
        switch (connectReply.getMajor()) {
        case 2:
            switch (connectReply.getMinor()) {
            case 0:
            case 1:
            case 2:
            case 3:
                return new LttngRelaydConnector_Unsupported();
            case 4:
            default:
                return new LttngRelaydConnector_2_4(inNet, outNet);
            }
        default:
            return new LttngRelaydConnector_Unsupported();
        }
    }
}
