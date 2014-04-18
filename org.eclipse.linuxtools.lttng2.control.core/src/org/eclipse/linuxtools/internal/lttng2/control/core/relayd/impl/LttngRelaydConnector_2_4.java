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
 *   Marc-Andre Laperle - Create session and split getNextIndex from getNextPacket
 **********************************************************************/

package org.eclipse.linuxtools.internal.lttng2.control.core.relayd.impl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.ILttngRelaydConnector;
import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands.AttachSessionRequest;
import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands.AttachSessionResponse;
import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands.Command;
import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands.ConnectResponse;
import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands.CreateSessionResponse;
import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands.GetMetadata;
import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands.GetNextIndex;
import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands.GetPacket;
import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands.IndexResponse;
import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands.ListSessionsResponse;
import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands.MetadataPacketResponse;
import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands.NewStreamsResponse;
import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands.NextIndexReturnCode;
import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands.SeekCommand;
import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands.SessionResponse;
import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands.StreamResponse;
import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands.TracePacketResponse;
import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands.ViewerCommand;

/**
 * Lttng 2.4 implementation
 *
 * @author Matthew Khouzam
 */
public class LttngRelaydConnector_2_4 implements ILttngRelaydConnector {

    private final @NonNull DataInputStream fInNet;
    private final @NonNull DataOutputStream fOutNet;

    /**
     * Constructor needs two network streams
     *
     * @param inNet
     *            network incoming data
     * @param outNet
     *            network outgoing data
     */
    public LttngRelaydConnector_2_4(@NonNull DataInputStream inNet, @NonNull DataOutputStream outNet) {
        fInNet = inNet;
        fOutNet = outNet;
    }

    // ------------------------------------------------------------------------
    // AutoCloseable
    // ------------------------------------------------------------------------

    @Override
    public void close() throws IOException {
        fInNet.close();
        fOutNet.close();
    }

    // ------------------------------------------------------------------------
    // ILttngRelaydConnector
    // ------------------------------------------------------------------------

    @Override
    public List<SessionResponse> getSessions() throws IOException {
        ViewerCommand listSessionsCmd = new ViewerCommand(Command.VIEWER_LIST_SESSIONS, 0,0);

        fOutNet.write(listSessionsCmd.serialize());
        fOutNet.flush();

        return new ListSessionsResponse(fInNet).getSessionsList();
    }

    @Override
    public CreateSessionResponse createSession() throws IOException {
        ViewerCommand listSessionsCmd = new ViewerCommand(Command.VIEWER_CREATE_SESSION, 0, 0);
        fOutNet.write(listSessionsCmd.serialize());
        fOutNet.flush();

        return new CreateSessionResponse(fInNet);
    }

    @Override
    public AttachSessionResponse attachToSession(SessionResponse lttngViewerSession) throws IOException {
        ViewerCommand listSessionsCmd = new ViewerCommand(Command.VIEWER_ATTACH_SESSION, 0,0);
        fOutNet.write(listSessionsCmd.serialize());
        /*
         * only flush if you read after
         */

        AttachSessionRequest attachRequest = new AttachSessionRequest(lttngViewerSession.getId(), SeekCommand.VIEWER_SEEK_BEGINNING);
        fOutNet.write(attachRequest.serialize());
        fOutNet.flush();

        return new AttachSessionResponse(fInNet);
    }

    @Override
    public String getMetadata(AttachSessionResponse attachedSession) throws IOException {

        for (StreamResponse stream : attachedSession.getStreamList()) {
            if (stream.getMetadataFlag() == 1) {
                issueCommand(Command.VIEWER_GET_METADATA);

                GetMetadata metadataRequest = new GetMetadata(stream.getId());
                fOutNet.write(metadataRequest.serialize());
                fOutNet.flush();

                MetadataPacketResponse metaDataPacket = new MetadataPacketResponse(fInNet);
                return new String(metaDataPacket.getData());
            }
        }

        return null;
    }

    @Override
    public TracePacketResponse getPacketFromStream(IndexResponse index, long id) throws IOException {

        issueCommand(Command.VIEWER_GET_PACKET);

        GetPacket packetRequest = new GetPacket(id, index.getOffset(), (int) (index.getPacketSize() / 8));
        fOutNet.write(packetRequest.serialize());
        fOutNet.flush();

        return new TracePacketResponse(fInNet);
    }

    @Override
    public TracePacketResponse getNextPacket(StreamResponse stream) throws IOException {
        IndexResponse indexReply = getNextIndex(stream);

        TracePacketResponse packet = null;
        if (indexReply.getStatus() == NextIndexReturnCode.VIEWER_INDEX_OK) {
            packet = getPacketFromStream(indexReply, stream.getId());
        }
        return packet;
    }

    @Override
    public IndexResponse getNextIndex(StreamResponse stream) throws IOException {
        issueCommand(Command.VIEWER_GET_NEXT_INDEX);

        GetNextIndex indexRequest = new GetNextIndex(stream.getId());
        fOutNet.write(indexRequest.serialize());
        fOutNet.flush();

        return new IndexResponse(fInNet);
    }

    @Override
    public List<StreamResponse> getNewStreams() throws IOException {

        Command viewerGetNewStreams = Command.VIEWER_GET_NEW_STREAMS;

        issueCommand(viewerGetNewStreams);

        return new NewStreamsResponse(fInNet).getStreamList();
    }

    private void issueCommand(Command command) throws IOException {
        ViewerCommand connectCommand = new ViewerCommand(command, ConnectResponse.SIZE, 0);
        fOutNet.write(connectCommand.serialize());
        fOutNet.flush();
    }
}
