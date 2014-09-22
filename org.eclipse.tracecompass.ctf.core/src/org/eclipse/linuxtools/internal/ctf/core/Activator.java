/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.ctf.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * <b><u>Activator</u></b>
 * <p>
 * The activator class controls the plug-in life cycle.
 */
public class Activator extends Plugin {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The plug-in ID
     */
    public static final String PLUGIN_ID = "org.eclipse.linuxtools.ctf"; //$NON-NLS-1$

    /**
     *  The shared instance
     */
    private static Activator fPlugin;

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
     * Get the default activator
     * @return the default activator
     */
    public static Activator getDefault() {
        return fPlugin;
    }

    /**
     * Sets the default activator
     *
     * @param plugin the default activator
     */
    private static void setDefault(Activator plugin) {
        fPlugin = plugin;
    }

    // ------------------------------------------------------------------------
    // Plugin
    // ------------------------------------------------------------------------

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        setDefault(this);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        setDefault(null);
        super.stop(context);
    }

    // ------------------------------------------------------------------------
    // Logging
    // ------------------------------------------------------------------------

    /**
     * Log a message
     *
     * @param msg
     *            The message to log
     */
    public static void log(String msg) {
        log(msg, null);
    }

    /**
     * Log a message with an exception
     *
     * @param msg
     *            The message
     * @param e
     *            The exception
     */
    public static void log(String msg, Exception e) {
        getDefault().getLog().log(new Status(IStatus.INFO, PLUGIN_ID, IStatus.OK, msg, e));
    }

    /**
     * Log an error, with an associated exception
     *
     * @param msg
     *            The error message
     * @param e
     *            The cause
     */
    public static void logError(String msg, Exception e) {
        getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, msg, e));
    }

    /**
     * Log a message
     *
     * @param severity
     *            Desired severity of the message in the log, one of
     *            {@link IStatus#INFO}, {@link IStatus#WARNING} or
     *            {@link IStatus#ERROR}
     * @param msg
     *            The message to log
     */
    public static void log(int severity, String msg) {
        getDefault().getLog().log(new Status(severity, PLUGIN_ID, msg));
    }


}
