/**********************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Marc-Andre Laperle - Initial implementation
 **********************************************************************/

package org.eclipse.linuxtools.internal.lttng2.control.ui.relayd;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.linuxtools.internal.lttng2.control.ui.Activator;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ctf.core.CtfConstants;
import org.eclipse.linuxtools.tmf.ctf.core.CtfTmfTrace;
import org.eclipse.ui.PlatformUI;

/**
 * Manages relayd connections. When a trace is opened, it creates a connection
 * if the trace was started with live support. When a trace is closed, is closes
 * the connection.
 *
 * @author Marc-Andre Laperle
 * @since 3.1
 */
public final class LttngRelaydConnectionManager {
    private static LttngRelaydConnectionManager fConnectionManager;
    private Map<LttngRelaydConnectionInfo, LttngRelaydConsumer> fConnections = new HashMap<>();

    /**
     * Get an instance of the trace manager.
     *
     * @return The trace manager
     */
    public static synchronized LttngRelaydConnectionManager getInstance() {
        if (fConnectionManager == null) {
            fConnectionManager = new LttngRelaydConnectionManager();
            TmfSignalManager.register(fConnectionManager);
        }
        return fConnectionManager;
    }

    /**
     * Get the cosumer for the given relayd connection information.
     *
     * @param connectionInfo
     *            the connection information
     *
     * @return the consumer
     */
    public LttngRelaydConsumer getConsumer(final LttngRelaydConnectionInfo connectionInfo) {
        if (!fConnections.containsKey(connectionInfo)) {
            LttngRelaydConsumer lttngRelaydConsumer = new LttngRelaydConsumer(connectionInfo);
            fConnections.put(connectionInfo, lttngRelaydConsumer);
            return lttngRelaydConsumer;
        }

        return fConnections.get(connectionInfo);
    }

    private static LttngRelaydConnectionInfo getEntry(final ITmfTrace trace) throws CoreException {
        if (trace instanceof CtfTmfTrace) {
            CtfTmfTrace ctfTmfTrace = (CtfTmfTrace) trace;
            if (!ctfTmfTrace.isComplete()) {
                IResource resource = ctfTmfTrace.getResource();
                String host = resource.getPersistentProperty(CtfConstants.LIVE_HOST);
                String port = resource.getPersistentProperty(CtfConstants.LIVE_PORT);
                String sessionName = resource.getPersistentProperty(CtfConstants.LIVE_SESSION_NAME);
                if (host != null && port != null && sessionName != null && !sessionName.isEmpty()) {
                    LttngRelaydConnectionInfo entry = new LttngRelaydConnectionInfo(host, Integer.parseInt(port), sessionName);
                    return entry;
                }
            }
        }

        return null;
    }

    /**
     * Listen to trace opened so that we can start the relayd job if necessary.
     *
     * @param signal
     *            the signal to be processed
     */
    @TmfSignalHandler
    public void traceOpened(final TmfTraceOpenedSignal signal) {

        try {
            LttngRelaydConnectionInfo entry = getEntry(signal.getTrace());
            if (entry != null) {
                LttngRelaydConsumer consumer = getConsumer(entry);
                consumer.connect();
                consumer.run((CtfTmfTrace) signal.getTrace());
            }
        } catch (CoreException e) {
            Activator.getDefault().logError(Messages.LttngRelaydConnectionManager_ConnectionError, e);
            ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), null, Messages.LttngRelaydConnectionManager_ConnectionError, new Status(IStatus.WARNING,
                    Activator.PLUGIN_ID, e.getLocalizedMessage(), e));
        }
    }

    /**
     * Listen to trace closed so that we can stop the relayd job.
     *
     * @param signal
     *            the signal to be processed
     */
    @TmfSignalHandler
    public void traceClosed(final TmfTraceClosedSignal signal) {
        LttngRelaydConnectionInfo entry;
        try {
            entry = getEntry(signal.getTrace());
            if (entry != null) {
                LttngRelaydConsumer comsumer = getConsumer(entry);
                if (comsumer != null) {
                    comsumer.dispose();
                }
                fConnections.remove(entry);
            }
        } catch (CoreException e) {
            // Something went wrong with the resource. That's OK, the trace is
            // getting closed anyway.
        }
    }

    /**
     * Dispose of all the manager's resources (i.e. its connections).
     */
    public void dispose() {
        for (LttngRelaydConsumer consumer : fConnections.values()) {
            consumer.dispose();
        }
    }
}
