/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Vincent Perot - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.pcap.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.framework.BundleContext;

/**
 * <b><u>Activator</u></b>
 * <p>
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The plug-in ID
     */
    public static final String PLUGIN_ID = "org.eclipse.linuxtools.tmf.pcap.ui"; //$NON-NLS-1$

    /**
     * The shared instance
     */
    private static @Nullable Activator fPlugin;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     */
    public Activator() {
        setDefault(this);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Returns the TMF Core plug-in instance.
     *
     * @return the TMF Core plug-in instance.
     */
    public static @Nullable Activator getDefault() {
        return fPlugin;
    }

    // Sets plug-in instance
    private static void setDefault(@Nullable Activator plugin) {
        fPlugin = plugin;
    }

    // ------------------------------------------------------------------------
    // Plugin
    // ------------------------------------------------------------------------

    @Override
    public void start(@Nullable BundleContext context) throws Exception {
        super.start(context);
        setDefault(this);
    }

    @Override
    public void stop(@Nullable BundleContext context) throws Exception {
        setDefault(null);
        super.stop(context);
    }


    // ------------------------------------------------------------------------
    // Log an IStatus
    // ------------------------------------------------------------------------

    /**
     * Log an IStatus object directly
     *
     * @param status
     *            The status to log
     */
    public static void log(IStatus status) {
        Activator activator = fPlugin;
        if (activator == null) {
            return;
        }
        activator.getLog().log(status);
    }

    // ------------------------------------------------------------------------
    // Log INFO
    // ------------------------------------------------------------------------

    /**
     * Logs a message with severity INFO in the runtime log of the plug-in.
     *
     * @param message
     *            A message to log
     */
    public static void logInfo(String message) {
        Activator activator = fPlugin;
        if (activator == null) {
            return;
        }
        activator.getLog().log(new Status(IStatus.INFO, PLUGIN_ID, message));
    }

    /**
     * Logs a message and exception with severity INFO in the runtime log of the
     * plug-in.
     *
     * @param message
     *            A message to log
     * @param exception
     *            The corresponding exception
     */
    public static void logInfo(String message, Throwable exception) {
        Activator activator = fPlugin;
        if (activator == null) {
            return;
        }
        activator.getLog().log(new Status(IStatus.INFO, PLUGIN_ID, message, exception));
    }

    // ------------------------------------------------------------------------
    // Log WARNING
    // ------------------------------------------------------------------------

    /**
     * Logs a message and exception with severity WARNING in the runtime log of
     * the plug-in.
     *
     * @param message
     *            A message to log
     */
    public static void logWarning(String message) {
        Activator activator = fPlugin;
        if (activator == null) {
            return;
        }
        activator.getLog().log(new Status(IStatus.WARNING, PLUGIN_ID, message));
    }

    /**
     * Logs a message and exception with severity WARNING in the runtime log of
     * the plug-in.
     *
     * @param message
     *            A message to log
     * @param exception
     *            The corresponding exception
     */
    public static void logWarning(String message, Throwable exception) {
        Activator activator = fPlugin;
        if (activator == null) {
            return;
        }
        activator.getLog().log(new Status(IStatus.WARNING, PLUGIN_ID, message, exception));
    }

    // ------------------------------------------------------------------------
    // Log ERROR
    // ------------------------------------------------------------------------

    /**
     * Logs a message and exception with severity ERROR in the runtime log of
     * the plug-in.
     *
     * @param message
     *            A message to log
     */
    public static void logError(String message) {
        Activator activator = fPlugin;
        if (activator == null) {
            return;
        }
        activator.getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message));
    }

    /**
     * Logs a message and exception with severity ERROR in the runtime log of
     * the plug-in.
     *
     * @param message
     *            A message to log
     * @param exception
     *            The corresponding exception
     */
    public static void logError(String message, Throwable exception) {
        Activator activator = fPlugin;
        if (activator == null) {
            return;
        }
        activator.getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message, exception));
    }
}
