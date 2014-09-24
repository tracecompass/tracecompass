/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.tmf.core.component.ITmfComponent;
import org.eclipse.linuxtools.tmf.core.component.ITmfEventProvider;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignal;

/**
 * The TMF Core tracer, used to trace TMF internal components.
 * <p>
 * The tracing classes are independently controlled (i.e no implicit inclusion)
 * from the launch configuration's Tracing. The resulting trace is stored in a
 * distinct file (TmfTrace.log) in a format that can later be analyzed by TMF.
 * <p>
 * The tracing classes are:
 * <ul>
 * <li><strong>Component</strong>: TMF components life-cycle
 * <li><strong>Request</strong>: TMF requests life-cycle
 * <li><strong>Signal</strong>: TMF signals triggering and distribution
 * <li><strong>Event</strong>: TMF trace events
 * </ul>
 *
 * @version 1.0
 * @author Francois Chouinard
 */
@SuppressWarnings("nls")
public class TmfCoreTracer {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final String PLUGIN_ID = Activator.PLUGIN_ID;

    // Tracing keys (in .options)
    private static final String COMPONENT_TRACE_KEY = PLUGIN_ID + "/component";
    private static final String REQUEST_TRACE_KEY   = PLUGIN_ID + "/request";
    private static final String SIGNAL_TRACE_KEY    = PLUGIN_ID + "/signal";
    private static final String EVENT_TRACE_KEY     = PLUGIN_ID + "/event";

    private static final String TRACE_FILE_NAME = "TmfTrace.log";

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // Classes tracing flags
    static boolean COMPONENT_CLASS_ENABLED = false;
    static boolean REQUEST_CLASS_ENABLED   = false;
    static boolean SIGNAL_CLASS_ENABLED    = false;
    static boolean EVENT_CLASS_ENABLED     = false;

    // Trace log file
    private static BufferedWriter fTraceFile;

    // ------------------------------------------------------------------------
    // Start/stop tracing - controlled by the plug-in
    // ------------------------------------------------------------------------

    /**
     * Set the tracing flags according to the launch configuration
     */
    public static void init() {

        String traceKey;
        boolean isTracing = false;

        traceKey = Platform.getDebugOption(COMPONENT_TRACE_KEY);
        if (traceKey != null) {
            COMPONENT_CLASS_ENABLED = (Boolean.valueOf(traceKey)).booleanValue();
            isTracing |= COMPONENT_CLASS_ENABLED;
        }

        traceKey = Platform.getDebugOption(REQUEST_TRACE_KEY);
        if (traceKey != null) {
            REQUEST_CLASS_ENABLED = (Boolean.valueOf(traceKey)).booleanValue();
            isTracing |= REQUEST_CLASS_ENABLED;
        }

        traceKey = Platform.getDebugOption(SIGNAL_TRACE_KEY);
        if (traceKey != null) {
            SIGNAL_CLASS_ENABLED = (Boolean.valueOf(traceKey)).booleanValue();
            isTracing |= SIGNAL_CLASS_ENABLED;
        }

        traceKey = Platform.getDebugOption(EVENT_TRACE_KEY);
        if (traceKey != null) {
            EVENT_CLASS_ENABLED = (Boolean.valueOf(traceKey)).booleanValue();
            isTracing |= EVENT_CLASS_ENABLED;
        }

        // Create trace log file if any of the flags was set
        if (isTracing) {
            try {
                fTraceFile = new BufferedWriter(new FileWriter(TRACE_FILE_NAME));
            } catch (IOException e) {
                Activator.logError("Error opening log file " + TRACE_FILE_NAME, e);
                fTraceFile = null;
            }
        }
    }

    /**
     * Close the trace log file
     */
    public static void stop() {
        if (fTraceFile != null) {
            try {
                fTraceFile.close();
                fTraceFile = null;
            } catch (IOException e) {
                Activator.logError("Error closing log file", e);
            }
        }
    }

    // ------------------------------------------------------------------------
    // Predicates
    // ------------------------------------------------------------------------

    @SuppressWarnings("javadoc")
    public static boolean isComponentTraced() {
        return COMPONENT_CLASS_ENABLED;
    }

    @SuppressWarnings("javadoc")
    public static boolean isRequestTraced() {
        return REQUEST_CLASS_ENABLED;
    }

    @SuppressWarnings("javadoc")
    public static boolean isSignalTraced() {
        return SIGNAL_CLASS_ENABLED;
    }

    @SuppressWarnings("javadoc")
    public static boolean isEventTraced() {
        return EVENT_CLASS_ENABLED;
    }

    // ------------------------------------------------------------------------
    // Tracing methods
    // ------------------------------------------------------------------------

    /**
     * The central tracing method. Prepends the timestamp and the thread id
     * to the trace message.
     *
     * @param msg the trace message to log
     */
    public static synchronized void trace(String msg) {
        // Leave when there is no place to write the message.
        if (fTraceFile == null) {
            return;
        }

        // Set the timestamp (ms resolution)
        long currentTime = System.currentTimeMillis();
        StringBuilder message = new StringBuilder("[");
        message.append(currentTime / 1000);
        message.append(".");
        message.append(String.format("%1$03d", currentTime % 1000));
        message.append("] ");

        // Set the thread id
        message.append("[TID=");
        message.append(String.format("%1$03d", Thread.currentThread().getId()));
        message.append("] ");

        // Append the trace message
        message.append(msg);

        // Write to file
        try {
            fTraceFile.write(message.toString());
            fTraceFile.newLine();
            fTraceFile.flush();
        } catch (IOException e) {
            Activator.logError("Error writing to log file", e);
        }
    }

    // ------------------------------------------------------------------------
    // TMF Core specific trace formatters
    // ------------------------------------------------------------------------

    @SuppressWarnings("javadoc")
    public static void traceComponent(ITmfComponent component, String msg) {
        if (COMPONENT_CLASS_ENABLED) {
            String message = ("[CMP] Cmp=" + component.getName() + " " + msg);
            trace(message);
        }
    }

    @SuppressWarnings("javadoc")
    public static void traceRequest(ITmfEventRequest request, String msg) {
        if (REQUEST_CLASS_ENABLED) {
            String message = ("[REQ] Req=" + request.getRequestId() + " " + msg);
            trace(message);
        }
    }

    @SuppressWarnings("javadoc")
    public static void traceSignal(TmfSignal signal, String msg) {
        if (SIGNAL_CLASS_ENABLED) {
            String message = ("[SIG] Sig=" + signal.getClass().getSimpleName()
                    + " Target=" + msg);
            trace(message);
        }
    }

    @SuppressWarnings("javadoc")
    public static void traceEvent(ITmfEventProvider provider, ITmfEventRequest request, ITmfEvent event) {
        if (EVENT_CLASS_ENABLED) {
            String message = ("[EVT] Provider=" + provider.toString()
                    + ", Req=" + request.getRequestId() + ", Event=" + event.getTimestamp());
            trace(message);
        }
    }

}
