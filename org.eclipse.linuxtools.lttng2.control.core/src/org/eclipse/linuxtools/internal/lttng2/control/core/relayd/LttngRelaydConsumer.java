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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.Metadata;
import org.eclipse.linuxtools.ctf.core.trace.CTFStream;
import org.eclipse.linuxtools.ctf.core.trace.CTFStreamInput;
import org.eclipse.linuxtools.internal.lttng2.control.core.Activator;
import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands.AttachSessionResponse;
import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands.LttngViewerCommands;
import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands.SessionResponse;
import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands.StreamResponse;
import org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands.TracePacketResponse;
import org.eclipse.linuxtools.tmf.ctf.core.CtfTmfTrace;

/**
 * Consumer of the relay d
 *
 * @author Matthew Khouzam
 * @since 3.0
 */
public class LttngRelaydConsumer {

    private final Job fConsumerJob;
    private final String fAddress;
    private final int fPort;
    private final int fSession;
    private final CtfTmfTrace fCtfTrace;
    /**
     * Map with the Lttng streams
     *
     * key: stream id value: stream file
     */
    private final Map<Long, File> fStreams = new TreeMap<>();

    /**
     * Start a lttng consumer
     *
     * @param address
     *            the ip address in string format
     * @param port
     *            the port, an integer
     * @param session
     *            the session id
     * @param ctfTrace
     *            the parent trace
     */
    public LttngRelaydConsumer(String address, final int port, final int session, final CtfTmfTrace ctfTrace) {
        fAddress = address;
        fPort = port;
        fSession = session;
        fCtfTrace = ctfTrace;
        for (CTFStream s : fCtfTrace.getCTFTrace().getStreams()) {
            for (CTFStreamInput si : s.getStreamInputs()) {
                fStreams.put(si.getStream().getId(), new File(si.getStream().getTrace().getPath() + si.getFilename()));
            }
        }

        fConsumerJob = new Job("RelayD consumer") { //$NON-NLS-1$

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try (Socket connection = new Socket(fAddress, fPort);
                        ILttngRelaydConnector relayd = LttngRelaydConnectorFactory.getNewConnector(connection);) {

                    List<SessionResponse> sessions = relayd.getSessions();
                    AttachSessionResponse attachedSession = relayd.attachToSession(sessions.get(fSession));

                    while (!monitor.isCanceled()) {

                        List<StreamResponse> attachedStreams = attachedSession.getStreamList();
                        for (StreamResponse stream : attachedStreams) {

                            TracePacketResponse packet = relayd.getNextPacket(stream);
                            // more streams
                            if ((packet.getFlags() & LttngViewerCommands.NEW_STREAM) == LttngViewerCommands.NEW_STREAM) {
                                Iterable<StreamResponse> newStreams = relayd.getNewStreams();
                                for (StreamResponse streamToAdd : newStreams) {

                                    File f = new File(fCtfTrace.getPath() + File.separator + streamToAdd.getPathName() + streamToAdd.getChannelName());
                                    // touch the file
                                    f.setLastModified(System.currentTimeMillis());
                                    fStreams.put(Long.valueOf(streamToAdd.getId()), f);
                                    fCtfTrace.getCTFTrace().addStream(streamToAdd.getId(), f);

                                }

                            }
                            // more metadata
                            if ((packet.getFlags() & LttngViewerCommands.NEW_METADATA) == LttngViewerCommands.NEW_METADATA) {

                                String metaData = relayd.getMetadata(attachedSession);
                                (new Metadata(ctfTrace.getCTFTrace())).parseTextFragment(metaData);
                            }

                            try (FileOutputStream fos = new FileOutputStream(fStreams.get(stream.getId()), true)) {
                                fos.write(packet.getData());
                                monitor.worked(1);
                            }
                        }

                    }

                } catch (IOException | CTFReaderException e) {
                    Activator.getDefault().logError("Error during live trace reading", e); //$NON-NLS-1$
                }
                return null;
            }
        };
        fConsumerJob.schedule();
    }

}
