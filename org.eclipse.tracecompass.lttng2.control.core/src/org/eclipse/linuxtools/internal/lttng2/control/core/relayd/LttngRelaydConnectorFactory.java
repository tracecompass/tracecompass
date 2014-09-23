/**********************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial implementation
 **********************************************************************/

package org.eclipse.linuxtools.internal.lttng2.control.core.relayd;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.impl.LttngRelaydConnector_2_4;
import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.impl.LttngRelaydConnector_Unsupported;
import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands.Command;
import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands.ConnectResponse;
import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands.ConnectionType;
import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands.ViewerCommand;

/**
 * LTTng RelayD connector factory
 *
 * @author Matthew Khouzam
 * @since 3.0
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
