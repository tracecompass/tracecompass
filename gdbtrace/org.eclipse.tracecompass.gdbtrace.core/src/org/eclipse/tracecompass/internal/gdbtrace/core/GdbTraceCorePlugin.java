/*******************************************************************************
 * Copyright (c) 2011, 2015 Ericsson
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
 *   Patrick Tasse - Updated for TMF 2.0
 *   Matthew Khouzam - Add logging methods
 *******************************************************************************/

package org.eclipse.tracecompass.internal.gdbtrace.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * GDB Tracepoint Analysis Core plug-in activator
 * @author Francois Chouinard
 */
public class GdbTraceCorePlugin extends Plugin {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /** The plug-in ID */
    public static final String PLUGIN_ID = "org.eclipse.tracecompass.gdbtrace.core"; //$NON-NLS-1$

    private static GdbTraceCorePlugin fPlugin;

    private static BundleContext fBundleContext;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor
     */
    public GdbTraceCorePlugin() {
        // Do nothing
    }

    // ------------------------------------------------------------------------
    // Plugin
    // ------------------------------------------------------------------------

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        fPlugin = this;
        fBundleContext = context;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        fPlugin = null;
        super.stop(context);
    }

    // ------------------------------------------------------------------------
    // Accessor
    // ------------------------------------------------------------------------

    /**
     * Returns the GDB Tracepoints Core plug-in instance.
     *
     * @return the GDB Tracepoints Core plug-in instance
     */
    public static GdbTraceCorePlugin getDefault() {
        return fPlugin;
    }

    /**
     * Returns the bundle context
     *
     * @return the bundle context
     */
    public static BundleContext getBundleContext() {
        return fBundleContext;
    }

    // ------------------------------------------------------------------------
    // Log INFO
    // ------------------------------------------------------------------------

    /**
     * Logs a message with severity INFO in the runtime log of the plug-in.
     *
     * @param message A message to log
     */
    public static void logInfo(String message) {
        fPlugin.getLog().log(new Status(IStatus.INFO, PLUGIN_ID, message));
    }

    /**
     * Logs a message and exception with severity INFO in the runtime log of the plug-in.
     *
     * @param message A message to log
     * @param exception The corresponding exception
     */
    public static void logInfo(String message, Throwable exception) {
        fPlugin.getLog().log(new Status(IStatus.INFO, PLUGIN_ID, message, exception));
    }

    // ------------------------------------------------------------------------
    // Log WARNING
    // ------------------------------------------------------------------------

    /**
     * Logs a message and exception with severity WARNING in the runtime log of the plug-in.
     *
     * @param message A message to log
     */
    public static void logWarning(String message) {
        fPlugin.getLog().log(new Status(IStatus.WARNING, PLUGIN_ID, message));
    }

    /**
     * Logs a message and exception with severity WARNING in the runtime log of the plug-in.
     *
     * @param message A message to log
     * @param exception The corresponding exception
     */
    public static void logWarning(String message, Throwable exception) {
        fPlugin.getLog().log(new Status(IStatus.WARNING, PLUGIN_ID, message, exception));
    }

    // ------------------------------------------------------------------------
    // Log ERROR
    // ------------------------------------------------------------------------

    /**
     * Logs a message and exception with severity ERROR in the runtime log of the plug-in.
     *
     * @param message A message to log
     */
    public static void logError(String message) {
        fPlugin.getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message));
    }

    /**
     * Logs a message and exception with severity ERROR in the runtime log of the plug-in.
     *
     * @param message A message to log
     * @param exception The corresponding exception
     */
    public static void logError(String message, Throwable exception) {
        fPlugin.getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message, exception));
    }

}
