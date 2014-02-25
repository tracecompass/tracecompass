/**********************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.logging;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.linuxtools.internal.lttng2.ui.Activator;

/**
 * <p>
 * Class to log control commands.
 * </p>
 *
 * @author Bernd Hufmann
 */
final public class ControlCommandLogger {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The bufferd writer reference
     */
    private static BufferedWriter fTraceLog = null;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    private ControlCommandLogger() {
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * Initializes the logger class and opens the log file with the given parameter.
     * @param filename - file name of logger output
     * @param append - true to open log file in append mode else false (overwrite)
     */
    public static void init(String filename, boolean append) {
        if (fTraceLog != null) {
            close();
        }
        fTraceLog = openLogFile(filename, append);
    }

    /**
     * Closes the log file if open.
     */
    public static void close() {
        if (fTraceLog == null) {
            return;
        }

        try {
            fTraceLog.close();
            fTraceLog = null;
        } catch (IOException e) {
            Activator.getDefault().logWarning("Can't close log file of the trace control", e); //$NON-NLS-1$
        }
    }

    /**
     * Logs a message to the log file.
     * @param msg - message (e.g. command or command result) to log
     */
    @SuppressWarnings("nls")
    public static void log(String msg) {
        long currentTime = System.currentTimeMillis();
        StringBuilder message = new StringBuilder("[");
        message.append(currentTime / 1000);
        message.append(".");
        message.append(String.format("%1$03d", currentTime % 1000));
        message.append("] ");
        message.append(msg);
        if (fTraceLog != null) {
            try {
                fTraceLog.write(message.toString());
                fTraceLog.newLine();
                fTraceLog.flush();
            } catch (IOException e) {
                Activator.getDefault().logError("Can't log message in log file of the tracer control", e); //$NON-NLS-1$
            }
        }
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------
    /**
     * Opens the trace log file with given name in the workspace root directory
     * @param filename - file name of logger output
     * @param append - true to open log file in append mode else false (overwrite)
     * @return the buffer writer class or null if not successful
     */
    private static BufferedWriter openLogFile(String filename, boolean append) {
        BufferedWriter outfile = null;
        try {
            outfile = new BufferedWriter(new FileWriter(filename, append));
        } catch (IOException e) {
            Activator.getDefault().logError("Can't open log file for logging of tracer control commands: " + filename, e); //$NON-NLS-1$
        }
        return outfile;
    }
}

