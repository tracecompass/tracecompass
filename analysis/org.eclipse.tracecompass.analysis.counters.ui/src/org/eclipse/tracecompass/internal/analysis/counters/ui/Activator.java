/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.counters.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 *
 * @author Mikael Ferland
 */
public class Activator extends AbstractUIPlugin {

    /** The plugin ID */
    public static final String PLUGIN_ID = "org.eclipse.tracecompass.internal.analysis.counters.ui"; //$NON-NLS-1$

    // The shared instance
    private static @Nullable Activator plugin;

    /**
     * The constructor
     */
    public Activator() {
        // do nothing
    }

    @Override
    public void start(@Nullable BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    @Override
    public void stop(@Nullable BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static @Nullable Activator getDefault() {
        return plugin;
    }

    /**
     * Logs a message and exception with severity ERROR in the runtime log of the
     * plug-in.
     *
     * @param message
     *            A message to log
     * @param exception
     *            The corresponding exception
     */
    public static void logError(String message, Throwable exception) {
        if (plugin != null) {
            plugin.getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message, exception));
        }
    }
}
