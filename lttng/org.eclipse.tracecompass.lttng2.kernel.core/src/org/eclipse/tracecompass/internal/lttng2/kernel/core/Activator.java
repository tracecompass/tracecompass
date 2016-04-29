/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.core;

import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.event.matching.TcpEventMatching;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.event.matching.TcpLttngEventMatching;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.ConfigFileLamiAnalysisFactory.ConfigFileLamiAnalysisFactoryException;
import org.eclipse.tracecompass.tmf.core.event.matching.TmfEventMatching;
import org.osgi.framework.BundleContext;

/**
 * <b><u>Activator</u></b>
 * <p>
 * The activator class controls the plug-in life cycle
 */
@NonNullByDefault({})
public class Activator extends Plugin {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The plug-in ID
     */
    public static final String PLUGIN_ID = "org.eclipse.tracecompass.lttng2.kernel.core"; //$NON-NLS-1$

    /**
     * The shared instance
     */
    private static Activator plugin;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * The constructor
     */
    public Activator() {
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    // ------------------------------------------------------------------------
    // Operators
    // ------------------------------------------------------------------------

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        TmfEventMatching.registerMatchObject(new TcpEventMatching());
        TmfEventMatching.registerMatchObject(new TcpLttngEventMatching());

        try {
            LttngAnalysesLoader.load();
        } catch (ConfigFileLamiAnalysisFactoryException | IOException e) {
            // Not the end of the world if the analyses are not available
            logWarning("Cannot find LTTng analyses configuration files: " + e.getMessage()); //$NON-NLS-1$
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Logs a message with severity INFO in the runtime log of the plug-in.
     *
     * @param message A message to log
     */
    public void logInfo(String message) {
        getLog().log(new Status(IStatus.INFO, PLUGIN_ID, message));
    }

    /**
     * Logs a message and exception with severity INFO in the runtime log of the plug-in.
     *
     * @param message A message to log
     * @param exception A exception to log
     */
    public void logInfo(String message, Throwable exception) {
        getLog().log(new Status(IStatus.INFO, PLUGIN_ID, message, exception));
    }

    /**
     * Logs a message and exception with severity WARNING in the runtime log of the plug-in.
     *
     * @param message A message to log
     */
    public void logWarning(String message) {
        getLog().log(new Status(IStatus.WARNING, PLUGIN_ID, message));
    }

    /**
     * Logs a message and exception with severity WARNING in the runtime log of the plug-in.
     *
     * @param message A message to log
     * @param exception A exception to log
     */
    public void logWarning(String message, Throwable exception) {
        getLog().log(new Status(IStatus.WARNING, PLUGIN_ID, message, exception));
    }

    /**
     * Logs a message and exception with severity ERROR in the runtime log of the plug-in.
     *
     * @param message A message to log
     */
    public void logError(String message) {
        getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message));
    }

    /**
     * Logs a message and exception with severity ERROR in the runtime log of the plug-in.
     *
     * @param message A message to log
     * @param exception A exception to log
     */
    public void logError(String message, Throwable exception) {
        getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message, exception));
    }

}
