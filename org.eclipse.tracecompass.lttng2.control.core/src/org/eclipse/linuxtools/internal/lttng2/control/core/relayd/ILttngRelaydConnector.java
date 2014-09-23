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

import java.io.IOException;
import java.util.List;

import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands.AttachSessionResponse;
import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands.CreateSessionResponse;
import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands.IndexResponse;
import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands.SessionResponse;
import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands.StreamResponse;
import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands.TracePacketResponse;

/**
 * Connector for Lttng Relayd
 *
 * @author Matthew Khouzam
 * @since 3.0
 */
public interface ILttngRelaydConnector extends AutoCloseable {

    /**
     * Gets a list of active Lttng sessions
     *
     * @return the session List (we need the get function)
     * @throws IOException
     *             timeout and such
     */
    List<SessionResponse> getSessions() throws IOException;

    /**
     * Create a session
     *
     * @return create session response
     * @throws IOException
     *             timeout and such
     */
    CreateSessionResponse createSession() throws IOException;

    /**
     * Attach the trace viewer to a Session
     *
     * @param lttngViewerSession
     *            viewer session
     * @return An AttachSessionResponse
     * @throws IOException
     *             timeout and such
     */
    AttachSessionResponse attachToSession(SessionResponse lttngViewerSession) throws IOException;

    /**
     * Get the metadata from the relayd
     *
     * @param attachedSession
     *            the attached session
     * @return a chunk of TSDL metadata
     * @throws IOException
     *             timeout and such
     */
    String getMetadata(AttachSessionResponse attachedSession) throws IOException;

    /**
     * Get the next index
     *
     * @param stream
     *            the stream
     * @return the stream input packet entry
     * @throws IOException
     *             timeout and such
     */
    IndexResponse getNextIndex(StreamResponse stream) throws IOException;

    /**
     * Get the next packet in a stream
     *
     * @param stream
     *            the stream response
     * @return the packet response
     * @throws IOException
     *             timeout and such
     */
    TracePacketResponse getNextPacket(StreamResponse stream) throws IOException;

    /**
     * Gets the packet from the stream
     *
     * @param index
     *            the index of the stream
     * @param id
     *            the stream id
     * @return the packet response
     * @throws IOException
     *             timeout and such
     */
    TracePacketResponse getPacketFromStream(IndexResponse index, long id) throws IOException;

    /**
     * Gets the new streams
     *
     * @return a list of stream responses
     * @throws IOException
     *             timeout and such
     */
    Iterable<StreamResponse> getNewStreams() throws IOException;

    @Override
    void close() throws IOException;
}
