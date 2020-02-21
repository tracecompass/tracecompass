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

import java.util.List;

import org.eclipse.tracecompass.internal.lttng2.control.core.relayd.ILttngRelaydConnector;
import org.eclipse.tracecompass.internal.lttng2.control.core.relayd.commands.AttachSessionResponse;
import org.eclipse.tracecompass.internal.lttng2.control.core.relayd.commands.CreateSessionResponse;
import org.eclipse.tracecompass.internal.lttng2.control.core.relayd.commands.IndexResponse;
import org.eclipse.tracecompass.internal.lttng2.control.core.relayd.commands.SessionResponse;
import org.eclipse.tracecompass.internal.lttng2.control.core.relayd.commands.StreamResponse;
import org.eclipse.tracecompass.internal.lttng2.control.core.relayd.commands.TracePacketResponse;

/**
 * Unsupported version of the relay daemon
 *
 * @author Matthew Khouzam
 */
public class LttngRelaydConnector_Unsupported implements ILttngRelaydConnector {

    @Override
    public List<SessionResponse> getSessions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public AttachSessionResponse attachToSession(SessionResponse lttngViewerSession) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getMetadata(AttachSessionResponse attachedSession) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TracePacketResponse getNextPacket(StreamResponse stream) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TracePacketResponse getPacketFromStream(IndexResponse index, long id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<StreamResponse> getNewStreams() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CreateSessionResponse createSession() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IndexResponse getNextIndex(StreamResponse stream) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        // Do nothing
    }

}
