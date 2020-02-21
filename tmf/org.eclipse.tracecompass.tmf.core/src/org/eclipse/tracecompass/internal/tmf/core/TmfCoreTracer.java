/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.core.runtime.Platform;
import org.eclipse.tracecompass.tmf.core.component.ITmfEventProvider;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

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
 * <li><strong>Analysis</strong>: TMF analyzes
 * </ul>
 *
 * @version 1.0
 * @author Francois Chouinard
 */
@SuppressWarnings("nls")
public final class TmfCoreTracer {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final String PLUGIN_ID = Activator.PLUGIN_ID;

    // Tracing keys (in .options)
    private static final String COMPONENT_TRACE_KEY = PLUGIN_ID + "/component";
    private static final String REQUEST_TRACE_KEY   = PLUGIN_ID + "/request";
    private static final String SIGNAL_TRACE_KEY    = PLUGIN_ID + "/signal";
    private static final String EVENT_TRACE_KEY     = PLUGIN_ID + "/event";
    private static final String ANALYSIS_TRACE_KEY     = PLUGIN_ID + "/analysis";
    private static final String INDEXER_TRACE_KEY   = PLUGIN_ID + "/indexer";

    private static final String TRACE_FILE_NAME = "TmfTrace.log";

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // Classes tracing flags
    private static volatile boolean fComponentClassEnabled = false;
    private static volatile boolean fRequestClassEnabled   = false;
    private static volatile boolean fSignalClassEnabled    = false;
    private static volatile boolean fEventClassEnabled     = false;
    private static volatile boolean fAnalysisClassEnabled  = false;
    private static volatile boolean fIndexerClassEnabled   = false;

    // Trace log file
    private static BufferedWriter fTraceFile;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    /**
     * Constructor
     */
    private TmfCoreTracer() {
        // Do nothing
    }

    // ------------------------------------------------------------------------
    // Start/stop tracing - controlled by the plug-in
    // ------------------------------------------------------------------------

    /**
     * Set the tracing flags according to the launch configuration
     */
    public static synchronized void init() {

        String traceKey;
        boolean isTracing = false;

        traceKey = Platform.getDebugOption(COMPONENT_TRACE_KEY);
        if (traceKey != null) {
            fComponentClassEnabled = Boolean.parseBoolean(traceKey);
            isTracing |= fComponentClassEnabled;
        }

        traceKey = Platform.getDebugOption(REQUEST_TRACE_KEY);
        if (traceKey != null) {
            fRequestClassEnabled = Boolean.parseBoolean(traceKey);
            isTracing |= fRequestClassEnabled;
        }

        traceKey = Platform.getDebugOption(SIGNAL_TRACE_KEY);
        if (traceKey != null) {
            fSignalClassEnabled = Boolean.parseBoolean(traceKey);
            isTracing |= fSignalClassEnabled;
        }

        traceKey = Platform.getDebugOption(EVENT_TRACE_KEY);
        if (traceKey != null) {
            fEventClassEnabled = Boolean.parseBoolean(traceKey);
            isTracing |= fEventClassEnabled;
        }

        traceKey = Platform.getDebugOption(ANALYSIS_TRACE_KEY);
        if (traceKey != null) {
            fAnalysisClassEnabled = Boolean.parseBoolean(traceKey);
            isTracing |= fAnalysisClassEnabled;
        }

        traceKey = Platform.getDebugOption(INDEXER_TRACE_KEY);
        if (traceKey != null) {
            fIndexerClassEnabled = Boolean.parseBoolean(traceKey);
            isTracing |= fIndexerClassEnabled;
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
    public static synchronized void stop() {
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

    /**
     * Is component tracing enabled?
     *
     * @return true if components are traced, false otherwise
     */
    public static boolean isComponentTraced() {
        return fComponentClassEnabled;
    }

    /**
     * Is request tracing enabled? (useful to debug scheduling issues)
     *
     * @return true if requests are traced, false otherwise
     */
    public static boolean isRequestTraced() {
        return fRequestClassEnabled;
    }

    /**
     * Is signal tracing enabled? (useful to debug UI issues)
     *
     * @return true if signals are traced, false otherwise
     */
    public static boolean isSignalTraced() {
        return fSignalClassEnabled;
    }

    /**
     * Is event tracing enabled? (useful to debug parser issues)
     *
     * @return true if events are traced, false otherwise
     */
    public static boolean isEventTraced() {
        return fEventClassEnabled;
    }

    /**
     * Is analysis tracing enabled? (useful to debug analysis issues)
     *
     * @return true if analyses are traced, false otherwise
     */
    public static boolean isAnalysisTraced() {
        return fAnalysisClassEnabled;
    }

    /**
     * Is indexer tracing enabled? (useful to debug indexer issues)
     *
     * @return true if indexer is traced, false otherwise
     */
    public static boolean isIndexerTraced() {
        return fAnalysisClassEnabled;
    }

    // ------------------------------------------------------------------------
    // Tracing methods
    // ------------------------------------------------------------------------

    /**
     * The central tracing method. Prepends the timestamp and the thread id to
     * the trace message.
     *
     * @param msg
     *            the trace message to log
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

        System.out.println(message);

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

    /**
     * Trace an event happening in a component.
     *
     * @param componentName
     *            The name of the component being traced
     * @param msg
     *            The message to record for this component
     */
    public static void traceComponent(String componentName, String msg) {
        if (fComponentClassEnabled) {
            String message = ("[CMP] Cmp=" + componentName + " " + msg);
            trace(message);
        }
    }

    /**
     * Trace an event happening in an event request.
     *
     * @param requestId
     *            The request ID of the request being traced
     * @param msg
     *            The message to record for this component
     */
    public static void traceRequest(int requestId, String msg) {
        if (fRequestClassEnabled) {
            String message = ("[REQ] Req=" + requestId + " " + msg);
            trace(message);
        }
    }

    /**
     * Trace an event happening in an indexer.
     *
     * @param msg
     *            The message to record for this indexer
     */
    public static void traceIndexer(String msg) {
        if (fIndexerClassEnabled) {
            String message = ("[INDEXER] " + msg);
            trace(message);
        }
    }

    /**
     * Trace a signal being fired
     *
     * @param signal
     *            The signal
     * @param msg
     *            The message to record for this component
     */
    public static void traceSignal(TmfSignal signal, String msg) {
        if (fSignalClassEnabled) {
            String message = ("[SIG] Sig=" + signal.getClass().getSimpleName()
                    + " Target=" + msg);
            trace(message);
        }
    }

    /**
     * Trace an event with its provider and request
     *
     * @param provider
     *            The provider supplying the event
     * @param request
     *            The request being traced
     * @param event
     *            The event being traced
     */
    public static void traceEvent(ITmfEventProvider provider, ITmfEventRequest request, ITmfEvent event) {
        if (fEventClassEnabled) {
            String message = ("[EVT] Provider=" + provider.toString()
                    + ", Req=" + request.getRequestId() + ", Event=" + event.getTimestamp());
            trace(message);
        }
    }

    /**
     * Trace an event happening in an analysis
     *
     * @param analysisId
     *            The analysis ID of the analysis being run
     * @param trace
     *            The trace this analysis is run on
     * @param msg
     *            The message to record for this analysis
     */
    public static void traceAnalysis(String analysisId, ITmfTrace trace, String msg) {
        if (fAnalysisClassEnabled) {
            String traceName = (trace == null) ? "" : trace.getName();
            String message = ("[ANL] Anl=" + analysisId + " for " + traceName + " " + msg);
            trace(message);
        }
    }

}
