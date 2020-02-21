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

package org.eclipse.tracecompass.lttng2.control.core.tests.relayd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

import org.eclipse.tracecompass.internal.lttng2.control.core.relayd.ILttngRelaydConnector;
import org.eclipse.tracecompass.internal.lttng2.control.core.relayd.commands.AttachSessionResponse;
import org.eclipse.tracecompass.internal.lttng2.control.core.relayd.commands.Command;
import org.eclipse.tracecompass.internal.lttng2.control.core.relayd.commands.ConnectResponse;
import org.eclipse.tracecompass.internal.lttng2.control.core.relayd.commands.CreateSessionResponse;
import org.eclipse.tracecompass.internal.lttng2.control.core.relayd.commands.CreateSessionReturnCode;
import org.eclipse.tracecompass.internal.lttng2.control.core.relayd.commands.GetNextIndex;
import org.eclipse.tracecompass.internal.lttng2.control.core.relayd.commands.IndexResponse;
import org.eclipse.tracecompass.internal.lttng2.control.core.relayd.commands.NextIndexReturnCode;
import org.eclipse.tracecompass.internal.lttng2.control.core.relayd.commands.SessionResponse;
import org.eclipse.tracecompass.internal.lttng2.control.core.relayd.commands.StreamResponse;
import org.eclipse.tracecompass.internal.lttng2.control.core.relayd.commands.ViewerCommand;
import org.eclipse.tracecompass.internal.lttng2.control.core.relayd.impl.LttngRelaydConnectorFactory;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test application for lttng-relayd. It actually allows us to test the API.
 * Written as a Junit test for convenience.
 *
 * @author Matthew Khouzam
 */
public class LttngRelayd24TestApplication {

    private static final int PACKETS_TO_READ = 100;
    private static final String ADDRESS = "127.0.0.1"; // change me //$NON-NLS-1$
    private static final int PORT = 5344;

    private static void getPackets(AttachSessionResponse attachedSession, Socket connection, ILttngRelaydConnector relayD) throws IOException {
        int numPacketsReceived = 0;
        DataOutputStream fOutNet = new DataOutputStream(connection.getOutputStream());
        DataInputStream fInNet = new DataInputStream(connection.getInputStream());
        while (numPacketsReceived < PACKETS_TO_READ) {
            for (StreamResponse stream : attachedSession.getStreamList()) {
                if (stream.getMetadataFlag() != 1) {
                    ConnectResponse connectPayload = new ConnectResponse(fInNet);
                    assertNotNull(connectPayload);

                    ViewerCommand connectCommand = new ViewerCommand(Command.VIEWER_GET_NEXT_INDEX, ConnectResponse.SIZE, 0);
                    fOutNet.write(connectCommand.serialize());
                    fOutNet.flush();

                    GetNextIndex indexRequest = new GetNextIndex(stream.getId());
                    fOutNet.write(indexRequest.serialize());
                    fOutNet.flush();

                    IndexResponse indexReply = new IndexResponse(fInNet);
                    // Nothing else supported for now
                    if (indexReply.getStatus() == NextIndexReturnCode.VIEWER_INDEX_OK) {
                        if (relayD.getPacketFromStream(indexReply, stream.getId()) != null) {
                            numPacketsReceived++;
                        }
                    }
                }
            }
        }
    }

    /**
     * Test a connection
     *
     * @throws IOException
     *             network timeout?
     */
    @Test
    @Ignore
    public void testViewerConnection() throws IOException {
        InetAddress addr = InetAddress.getByName(ADDRESS);
        try (Socket connection = new Socket(addr, PORT);
                ILttngRelaydConnector relayD = LttngRelaydConnectorFactory.getNewConnector(connection);) {

            List<SessionResponse> sessions = relayD.getSessions();
            assertTrue(sessions.size() > 0);
            SessionResponse lttngViewerSession = sessions.get(0);
            assertNotNull(lttngViewerSession);
            CreateSessionResponse createSession = relayD.createSession();
            assertEquals(createSession.getStatus(), CreateSessionReturnCode.LTTNG_VIEWER_CREATE_SESSION_OK);
            AttachSessionResponse attachedSession = relayD.attachToSession(lttngViewerSession);

            String metaData = relayD.getMetadata(attachedSession);
            assertNotNull(metaData);

            getPackets(attachedSession, connection, relayD);
        }
    }
}
