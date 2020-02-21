/**********************************************************************
 * Copyright (c) 2014, 2015 Ericsson
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

package org.eclipse.tracecompass.internal.lttng2.control.ui.relayd;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.tracecompass.internal.lttng2.control.core.relayd.ILttngRelaydConnector;
import org.eclipse.tracecompass.internal.lttng2.control.core.relayd.commands.AttachReturnCode;
import org.eclipse.tracecompass.internal.lttng2.control.core.relayd.commands.AttachSessionResponse;
import org.eclipse.tracecompass.internal.lttng2.control.core.relayd.commands.CreateSessionResponse;
import org.eclipse.tracecompass.internal.lttng2.control.core.relayd.commands.CreateSessionReturnCode;
import org.eclipse.tracecompass.internal.lttng2.control.core.relayd.commands.IndexResponse;
import org.eclipse.tracecompass.internal.lttng2.control.core.relayd.commands.NextIndexReturnCode;
import org.eclipse.tracecompass.internal.lttng2.control.core.relayd.commands.SessionResponse;
import org.eclipse.tracecompass.internal.lttng2.control.core.relayd.commands.StreamResponse;
import org.eclipse.tracecompass.internal.lttng2.control.core.relayd.impl.LttngRelaydConnectorFactory;
import org.eclipse.tracecompass.internal.lttng2.control.ui.Activator;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;

/**
 * Consumer of the relay d.
 *
 * @author Matthew Khouzam
 */
public final class LttngRelaydConsumer {

    private static final Pattern PROTOCOL_HOST_PATTERN = Pattern.compile("(\\S+://)*(\\d+\\.\\d+\\.\\d+\\.\\d+)"); //$NON-NLS-1$
    private static final int SIGNAL_THROTTLE_NANOSEC = 10_000_000;
    private static final String ENCODING_UTF_8 = "UTF-8"; //$NON-NLS-1$

    private Job fConsumerJob;
    private CtfTmfTrace fCtfTmfTrace;
    private long fTimestampEnd;
    private AttachSessionResponse fSession;
    private Socket fConnection;
    private ILttngRelaydConnector fRelayd;
    private String fTracePath;
    private long fLastSignal = 0;
    private final LttngRelaydConnectionInfo fConnectionInfo;

    /**
     * Start a lttng consumer.
     *
     * @param address
     *            the ip address in string format
     * @param port
     *            the port, an integer
     * @param sessionName
     *            the session name
     * @param project
     *            the default project
     */
    LttngRelaydConsumer(final LttngRelaydConnectionInfo connectionInfo) {
        fConnectionInfo = connectionInfo;
        fTimestampEnd = 0;
    }

    /**
     * Connects to the relayd at the given address and port then attaches to the
     * given session name.
     *
     * @throws CoreException
     *             If something goes wrong during the connection
     *             <ul>
     *             <li>
     *             Connection could not be established (Socket could not be
     *             opened, etc)</li>
     *             <li>
     *             Connection timeout</li>
     *             <li>
     *             The session was not found</li>
     *             <li>
     *             Could not create viewer session</li>
     *             <li>
     *             Invalid trace (no metadata, no streams)</li>
     *             </ul>
     */
    public void connect() throws CoreException {
        if (fConnection != null) {
            return;
        }

        try {
            Matcher matcher = PROTOCOL_HOST_PATTERN.matcher(fConnectionInfo.getHost());
            String host = null;
            if (matcher.matches()) {
                host = matcher.group(2);
            }

            if (host == null || host.isEmpty()) {
                throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.LttngRelaydConsumer_ErrorConnecting));
            }

            fConnection = new Socket(host, fConnectionInfo.getPort());
            fRelayd = LttngRelaydConnectorFactory.getNewConnector(fConnection);
            List<SessionResponse> sessions = fRelayd.getSessions();
            SessionResponse selectedSession = null;
            for (SessionResponse session : sessions) {
                String asessionName = nullTerminatedByteArrayToString(session.getSessionName().getBytes());

                if (asessionName.equals(fConnectionInfo.getSessionName())) {
                    selectedSession = session;
                    break;
                }
            }

            if (selectedSession == null) {
                throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.LttngRelaydConsumer_SessionNotFound));
            }

            CreateSessionResponse createSession = fRelayd.createSession();
            if (createSession.getStatus() != CreateSessionReturnCode.LTTNG_VIEWER_CREATE_SESSION_OK) {
                throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.LttngRelaydConsumer_CreateViewerSessionError + createSession.getStatus().toString()));
            }

            AttachSessionResponse attachedSession = fRelayd.attachToSession(selectedSession);
            if (attachedSession.getStatus() != AttachReturnCode.VIEWER_ATTACH_OK) {
                throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.LttngRelaydConsumer_AttachSessionError + attachedSession.getStatus().toString()));
            }

            String metadata = fRelayd.getMetadata(attachedSession);
            if (metadata == null) {
                throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.LttngRelaydConsumer_NoMetadata));
            }

            List<StreamResponse> attachedStreams = attachedSession.getStreamList();
            if (attachedStreams.isEmpty()) {
                throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.LttngRelaydConsumer_NoStreams));
            }

            fTracePath = nullTerminatedByteArrayToString(attachedStreams.get(0).getPathName().getBytes());

            fSession = attachedSession;
        } catch (IOException e) {
            throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.LttngRelaydConsumer_ErrorConnecting + (e.getMessage() != null ? e.getMessage() : ""))); //$NON-NLS-1$
        }
    }

    /**
     * Run the consumer operation for a give trace.
     *
     * @param trace
     *            the trace
     */
    public void run(final CtfTmfTrace trace) {
        if (fSession == null) {
            return;
        }

        fCtfTmfTrace = trace;
        fConsumerJob = new Job("RelayD consumer") { //$NON-NLS-1$

            @Override
            protected IStatus run(final IProgressMonitor monitor) {
                try {
                    while (!monitor.isCanceled()) {
                        List<StreamResponse> attachedStreams = fSession.getStreamList();
                        for (StreamResponse stream : attachedStreams) {
                            if (stream.getMetadataFlag() != 1) {
                                IndexResponse indexReply = fRelayd.getNextIndex(stream);
                                if (indexReply.getStatus() == NextIndexReturnCode.VIEWER_INDEX_OK) {
                                    long nanoTimeStamp = fCtfTmfTrace.timestampCyclesToNanos(indexReply.getTimestampEnd());
                                    if (nanoTimeStamp > fTimestampEnd) {
                                        ITmfTimestamp endTime = TmfTimestamp.fromNanos(nanoTimeStamp);
                                        TmfTimeRange range = new TmfTimeRange(fCtfTmfTrace.getStartTime(), endTime);

                                        long currentTime = System.nanoTime();
                                        if (currentTime - fLastSignal > SIGNAL_THROTTLE_NANOSEC) {
                                            TmfTraceRangeUpdatedSignal signal = new TmfTraceRangeUpdatedSignal(LttngRelaydConsumer.this, fCtfTmfTrace, range);
                                            fCtfTmfTrace.broadcastAsync(signal);
                                            fLastSignal = currentTime;
                                        }
                                        fTimestampEnd = nanoTimeStamp;
                                    }
                                } else if (indexReply.getStatus() == NextIndexReturnCode.VIEWER_INDEX_HUP) {
                                    // The trace is now complete because the trace session was destroyed
                                    fCtfTmfTrace.setComplete(true);
                                    TmfTraceRangeUpdatedSignal signal = new TmfTraceRangeUpdatedSignal(LttngRelaydConsumer.this, fCtfTmfTrace, new TmfTimeRange(fCtfTmfTrace.getStartTime(), TmfTimestamp.fromNanos(fTimestampEnd)));
                                    fCtfTmfTrace.broadcastAsync(signal);
                                    return Status.OK_STATUS;
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    Activator.getDefault().logError("Error during live trace reading", e); //$NON-NLS-1$
                    return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.LttngRelaydConsumer_ErrorLiveReading + (e.getMessage() != null ? e.getMessage() : "")); //$NON-NLS-1$
                }

                return Status.OK_STATUS;
            }
        };
        fConsumerJob.setSystem(true);
        fConsumerJob.schedule();
    }

    /**
     * Dispose the consumer and it's resources (sockets, etc).
     */
    public void dispose() {
        try {
            if (fConsumerJob != null) {
                fConsumerJob.cancel();
                fConsumerJob.join();
            }
            if (fConnection != null) {
                fConnection.close();
            }
            if (fRelayd != null) {
                fRelayd.close();
            }
        } catch (IOException e) {
            // Ignore
        } catch (InterruptedException e) {
            // Ignore
        }
    }

    /**
     * Once the consumer is connected to the relayd session, it knows the trace
     * path. This can be useful to know exactly where the trace is so that it
     * can be imported into the workspace and it can be opened.
     *
     * @return the trace path
     */
    public String getTracePath() {
        return fTracePath;
    }

    private static String nullTerminatedByteArrayToString(final byte[] byteArray) throws UnsupportedEncodingException {
        // Find length of null terminated string
        int length = 0;
        while (length < byteArray.length && byteArray[length] != 0) {
            length++;
        }

        String asessionName = new String(byteArray, 0, length, ENCODING_UTF_8);
        return asessionName;
    }

}
