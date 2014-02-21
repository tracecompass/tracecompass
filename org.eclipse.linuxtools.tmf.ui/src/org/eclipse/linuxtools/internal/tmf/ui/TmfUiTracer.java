/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui;

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

    static boolean ERROR   = false;
    static boolean WARNING = false;
    static boolean INFO    = false;

    static boolean INDEX   = false;
    static boolean DISPLAY = false;
    static boolean SORTING = false;

    private static String LOGNAME = "traceUI.log";
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
            ERROR = (Boolean.valueOf(traceKey)).booleanValue();
            isTracing |= ERROR;
        }

        traceKey = Platform.getDebugOption(pluginID + "/warning");
        if (traceKey != null) {
            WARNING = (Boolean.valueOf(traceKey)).booleanValue();
            isTracing |= WARNING;
        }

        traceKey = Platform.getDebugOption(pluginID + "/info");
        if (traceKey != null) {
            INFO = (Boolean.valueOf(traceKey)).booleanValue();
            isTracing |= INFO;
        }

        traceKey = Platform.getDebugOption(pluginID + "/updateindex");
        if (traceKey != null) {
            INDEX = (Boolean.valueOf(traceKey)).booleanValue();
            isTracing |= INDEX;
        }

        traceKey = Platform.getDebugOption(pluginID + "/display");
        if (traceKey != null) {
            DISPLAY = (Boolean.valueOf(traceKey)).booleanValue();
            isTracing |= DISPLAY;
        }

        traceKey = Platform.getDebugOption(pluginID + "/sorting");
        if (traceKey != null) {
            SORTING = (Boolean.valueOf(traceKey)).booleanValue();
            isTracing |= SORTING;
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
        return ERROR;
    }

    /**
     * @return If INDEX messages are traced
     */
    public static boolean isIndexTraced() {
        return INDEX;
    }

    /**
     * @return If DISPLAY messages are traced
     */
    public static boolean isDisplayTraced() {
        return DISPLAY;
    }

    /**
     * @return If SORTING messages are traced
     */
    public static boolean isSortingTraced() {
        return SORTING;
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
