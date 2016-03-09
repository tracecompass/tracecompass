/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.common.core;

import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 *
 * @author Alexandre Montplaisir
 */
public abstract class TraceCompassActivator extends Plugin {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /** Map of all the registered activators, indexed by plugin ID */
    private static final Map<String, TraceCompassActivator> ACTIVATORS =
            Collections.synchronizedMap(new HashMap<String, TraceCompassActivator>());

    /** This instance's plug-in ID */
    private final String fPluginId;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * The constructor
     *
     * @param pluginID
     *            The ID of the plugin
     */
    public TraceCompassActivator(String pluginID) {
        fPluginId = pluginID;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Return this plug-in's ID.
     *
     * @return The plug-in ID
     */
    public String getPluginId() {
        return fPluginId;
    }

    /**
     * Get a registered activator. Subclasses should implement their own public
     * getInstance() method, which returns the result of this.
     *
     * @param id
     *            The activator's plugin ID
     * @return The corresponding activator
     */
    protected static TraceCompassActivator getInstance(String id) {
        TraceCompassActivator ret = ACTIVATORS.get(id);
        if (ret == null) {
            /* The activator should be registered at this point! */
            throw new IllegalStateException();
        }
        return ret;
    }

    // ------------------------------------------------------------------------
    // Abstract methods
    // ------------------------------------------------------------------------

    /**
     * Additional actions to run at the plug-in startup
     */
    protected abstract void startActions();

    /**
     * Additional actions to run at the plug-in shtudown
     */
    protected abstract void stopActions();

    // ------------------------------------------------------------------------
    // ore.eclipse.core.runtime.Plugin
    // ------------------------------------------------------------------------

    @Override
    public final void start(@Nullable BundleContext context) throws Exception {
        super.start(context);
        String id = this.getPluginId();
        synchronized (ACTIVATORS) {
            if (ACTIVATORS.containsKey(id)) {
                logError("Duplicate Activator ID : " + id); //$NON-NLS-1$
            }
            ACTIVATORS.put(id, this);
        }
        startActions();
    }

    @Override
    public final void stop(@Nullable BundleContext context) throws Exception {
        stopActions();
        ACTIVATORS.remove(this.getPluginId());
        super.stop(context);
    }

    // ------------------------------------------------------------------------
    // Logging helpers
    // ------------------------------------------------------------------------

    /**
     * Logs a message with severity INFO in the runtime log of the plug-in.
     *
     * @param message
     *            A message to log
     */
    public void logInfo(@Nullable String message) {
        getLog().log(new Status(IStatus.INFO, fPluginId, nullToEmptyString(message)));
    }

    /**
     * Logs a message and exception with severity INFO in the runtime log of the
     * plug-in.
     *
     * @param message
     *            A message to log
     * @param exception
     *            A exception to log
     */
    public void logInfo(@Nullable String message, Throwable exception) {
        getLog().log(new Status(IStatus.INFO, fPluginId, nullToEmptyString(message), exception));
    }

    /**
     * Logs a message and exception with severity WARNING in the runtime log of
     * the plug-in.
     *
     * @param message
     *            A message to log
     */
    public void logWarning(@Nullable String message) {
        getLog().log(new Status(IStatus.WARNING, fPluginId, nullToEmptyString(message)));
    }

    /**
     * Logs a message and exception with severity WARNING in the runtime log of
     * the plug-in.
     *
     * @param message
     *            A message to log
     * @param exception
     *            A exception to log
     */
    public void logWarning(@Nullable String message, Throwable exception) {
        getLog().log(new Status(IStatus.WARNING, fPluginId, nullToEmptyString(message), exception));
    }

    /**
     * Logs a message and exception with severity ERROR in the runtime log of
     * the plug-in.
     *
     * @param message
     *            A message to log
     */
    public void logError(@Nullable String message) {
        getLog().log(new Status(IStatus.ERROR, fPluginId, nullToEmptyString(message)));
    }

    /**
     * Logs a message and exception with severity ERROR in the runtime log of
     * the plug-in.
     *
     * @param message
     *            A message to log
     * @param exception
     *            A exception to log
     */
    public void logError(@Nullable String message, Throwable exception) {
        getLog().log(new Status(IStatus.ERROR, fPluginId, nullToEmptyString(message), exception));
    }

}
