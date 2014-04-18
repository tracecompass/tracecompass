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

package org.eclipse.linuxtools.internal.lttng2.control.core.relayd.impl;

import java.util.List;

import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.ILttngRelaydConnector;
import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands.AttachSessionResponse;
import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands.CreateSessionResponse;
import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands.IndexResponse;
import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands.SessionResponse;
import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands.StreamResponse;
import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands.TracePacketResponse;

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
    }

}
