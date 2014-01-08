/**********************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Matthew Khouzam - Implementation of File->Open
 **********************************************************************/
package org.eclipse.linuxtools.internal.tracing.rcp.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.linuxtools.internal.tracing.rcp.ui.cli.TracingRCPCliException;
import org.eclipse.linuxtools.internal.tracing.rcp.ui.cli.CliParser;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 *
 * @author Bernd Hufmann
 */
public class TracingRcpPlugin extends AbstractUIPlugin {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The plug-in ID
     */
    public static final String PLUGIN_ID = "org.eclipse.linuxtools.tracing.rcp.ui"; //$NON-NLS-1$

    /**
     * The default workspace name
     */
    public static final String WORKSPACE_NAME = ".traceviewer"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // The shared instance
    private static TracingRcpPlugin fPlugin;
    private static CliParser fCli;

    // ------------------------------------------------------------------------
    // Constructor(s)
    // ------------------------------------------------------------------------
    /**
     * The default constructor
     */
    public TracingRcpPlugin() {
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static TracingRcpPlugin getDefault() {
        return fPlugin;
    }

    /**
     * Gets the tracing workspace root directory. By default it uses the user's
     * home directory. This value can be overwritten by using the global
     * TRACING_RCP_ROOT environment variable.
     *
     * @return the tracing workspace root directory
     */
    public static String getWorkspaceRoot() {
        /* Look for the environment variable in the global environment variables */
        String workspaceRoot = System.getenv().get("TRACING_RCP_ROOT"); //$NON-NLS-1$
        if (workspaceRoot == null) {
            /* Use the user's home directory */
            workspaceRoot = System.getProperty("user.home"); //$NON-NLS-1$
        }
        return workspaceRoot;
    }

    // ------------------------------------------------------------------------
    // Operation
    // ------------------------------------------------------------------------
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        fPlugin = this;
        String args[] = Platform.getCommandLineArgs();
        fCli = null;
        try {
            fCli = new CliParser(args);
        } catch (TracingRCPCliException e) {
            logError(e.getMessage());
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        fPlugin = null;
        super.stop(context);
    }

    /**
     * Gets the command line parser
     *
     * @return the command line parser
     */
    public CliParser getCli() {
        return fCli;
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in
     * relative path
     *
     * @param path
     *            the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    /**
     * Log an error
     *
     * @param message
     *            the error message to log
     */
    public void logError(String message) {
        getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message));
    }

    /**
     * Log an error
     *
     * @param message
     *            the error message to log
     * @param e
     *            the exception to log
     */
    public void logError(String message, Exception e) {
        getDefault().getLog().log(
                new Status(IStatus.ERROR, PLUGIN_ID, message, e));
    }

    /**
     * Log a warning
     *
     * @param message
     *            the warning message to log
     */
    public void logWarning(String message) {
        getDefault().getLog().log(
                new Status(IStatus.WARNING, PLUGIN_ID, message));
    }

}
