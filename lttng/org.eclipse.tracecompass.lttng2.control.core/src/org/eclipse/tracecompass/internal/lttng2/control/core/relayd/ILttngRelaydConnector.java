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

package org.eclipse.tracecompass.internal.lttng2.control.core.relayd;

import java.io.IOException;
import java.util.List;

import org.eclipse.tracecompass.internal.lttng2.control.core.relayd.commands.AttachSessionResponse;
import org.eclipse.tracecompass.internal.lttng2.control.core.relayd.commands.CreateSessionResponse;
import org.eclipse.tracecompass.internal.lttng2.control.core.relayd.commands.IndexResponse;
import org.eclipse.tracecompass.internal.lttng2.control.core.relayd.commands.SessionResponse;
import org.eclipse.tracecompass.internal.lttng2.control.core.relayd.commands.StreamResponse;
import org.eclipse.tracecompass.internal.lttng2.control.core.relayd.commands.TracePacketResponse;

/**
 * Connector for Lttng Relayd
 *
 * @author Matthew Khouzam
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
