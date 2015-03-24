/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.core.runtime.Platform;

/**
 * Tracer class for the tmf.ui plugin
 */
@SuppressWarnings("nls")
public class TmfUiTracer {

    private static String pluginID = Activator.PLUGIN_ID;

    private static boolean fError   = false;
    private static boolean fWarning = false;
    private static boolean fInfo    = false;

    private static boolean fIndex   = false;
    private static boolean fDisplay = false;
    private static boolean fSorting = false;

    private static final String LOGNAME = "traceUI.log";
    private static BufferedWriter fTraceLog = null;

    private static BufferedWriter openLogFile(String filename) {
        BufferedWriter outfile = null;
        try {
            outfile = new BufferedWriter(new FileWriter(filename));
        } catch (IOException e) {
            Activator.getDefault().logError("Error creating log file " + LOGNAME, e); //$NON-NLS-1$
        }
        return outfile;
    }

    /**
     * Initialize tracing
     */
    public static void init() {

        String traceKey;
        boolean isTracing = false;

        traceKey = Platform.getDebugOption(pluginID + "/error");
        if (traceKey != null) {
            fError = (Boolean.valueOf(traceKey)).booleanValue();
            isTracing |= fError;
        }

        traceKey = Platform.getDebugOption(pluginID + "/warning");
        if (traceKey != null) {
            fWarning = (Boolean.valueOf(traceKey)).booleanValue();
            isTracing |= fWarning;
        }

        traceKey = Platform.getDebugOption(pluginID + "/info");
        if (traceKey != null) {
            fInfo = (Boolean.valueOf(traceKey)).booleanValue();
            isTracing |= fInfo;
        }

        traceKey = Platform.getDebugOption(pluginID + "/updateindex");
        if (traceKey != null) {
            fIndex = (Boolean.valueOf(traceKey)).booleanValue();
            isTracing |= fIndex;
        }

        traceKey = Platform.getDebugOption(pluginID + "/display");
        if (traceKey != null) {
            fDisplay = (Boolean.valueOf(traceKey)).booleanValue();
            isTracing |= fDisplay;
        }

        traceKey = Platform.getDebugOption(pluginID + "/sorting");
        if (traceKey != null) {
            fSorting = (Boolean.valueOf(traceKey)).booleanValue();
            isTracing |= fSorting;
        }

        // Create trace log file if needed
        if (isTracing) {
            fTraceLog = openLogFile(LOGNAME);
        }
    }

    /**
     * Stop tracing
     */
    public static void stop() {
        if (fTraceLog == null) {
            return;
        }

        try {
            fTraceLog.close();
            fTraceLog = null;
        } catch (IOException e) {
            Activator.getDefault().logError("Error closing log file " + LOGNAME, e); //$NON-NLS-1$
        }
    }

    // ------------------------------------------------------------------------
    // Predicates
    // ------------------------------------------------------------------------

    /**
     * @return If ERROR messages are traced
     */
    public static boolean isErrorTraced() {
        return fError;
    }

    /**
     * @return If INDEX messages are traced
     */
    public static boolean isIndexTraced() {
        return fIndex;
    }

    /**
     * @return If DISPLAY messages are traced
     */
    public static boolean isDisplayTraced() {
        return fDisplay;
    }

    /**
     * @return If SORTING messages are traced
     */
    public static boolean isSortingTraced() {
        return fSorting;
    }


    // ------------------------------------------------------------------------
    // Tracing methods
    // ------------------------------------------------------------------------

    /**
     * Trace a generic event
     *
     * @param msg
     *            The event's message
     */
    public static void trace(String msg) {
        // Leave when there is no place to write the message.
        if (fTraceLog == null) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        StringBuilder message = new StringBuilder("[");
        message.append(currentTime / 1000);
        message.append(".");
        message.append(String.format("%1$03d", currentTime % 1000));
        message.append("] ");
        message.append(msg);

        try {
            fTraceLog.write(message.toString());
            fTraceLog.newLine();
            fTraceLog.flush();
        } catch (IOException e) {
            Activator.getDefault().logError("Error writing to log file " + LOGNAME, e); //$NON-NLS-1$
        }
    }

    /**
     * Trace an INDEX event
     *
     * @param msg
     *            The event's message
     */
    public static void traceIndex(String msg) {
        String message = ("[INDEX] " + msg);
        trace(message);
    }

    /**
     * Trace a DISPLAY event
     *
     * @param msg
     *            The event's message
     */
    public static void traceDisplay(String msg) {
        String message = ("[DISPLAY]" + msg);
        trace(message);
    }

    /**
     * Trace a SORTING event
     *
     * @param msg
     *            The event's message
     */
    public static void traceSorting(String msg) {
        String message = ("[SORT] " + msg);
        trace(message);
    }

    /**
     * Trace an ERROR event
     *
     * @param msg
     *            The event's message
     */
    public static void traceError(String msg) {
        String message = ("[ERR] Thread=" + Thread.currentThread().getId() + " " + msg);
        trace(message);
    }

    /**
     * Trace a WARNING event
     *
     * @param msg
     *            The event's message
     */
    public static void traceWarning(String msg) {
        String message = ("[WARN] Thread=" + Thread.currentThread().getId() + " " + msg);
        trace(message);
    }

    /**
     * Trace an INFO event
     *
     * @param msg
     *            The event's message
     */
    public static void traceInfo(String msg) {
        String message = ("[INF] Thread=" + Thread.currentThread().getId() + " " + msg);
        trace(message);
    }

}
