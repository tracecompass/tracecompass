/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
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
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.core.TmfCoreTracer;
import org.osgi.framework.BundleContext;

/**
 * <b><u>TmfTestPlugin</u></b>
 * <p>
 * The activator class controls the plug-in life cycle
 */
public class TmfCoreTestPlugin extends Plugin {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // The plug-in ID
    @SuppressWarnings("javadoc")
    public static final String PLUGIN_ID = "org.eclipse.tracecompass.tmf.tests";

    // The shared instance
    private static TmfCoreTestPlugin fPlugin;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * The constructor
     */
    public TmfCoreTestPlugin() {
        setDefault(this);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @return the shared instance
     */
    public static TmfCoreTestPlugin getDefault() {
        return fPlugin;
    }

    /**
     * @param plugin the shared instance
     */
    private static void setDefault(TmfCoreTestPlugin plugin) {
        fPlugin = plugin;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        setDefault(this);
        TmfCoreTracer.init();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        TmfCoreTracer.stop();
        setDefault(null);
        super.stop(context);
    }

    /**
     * Return a path to a file relative to this plugin's base directory
     *
     * @param relativePath
     *            The path relative to the plugin's root directory
     * @return The path corresponding to the relative path in parameter
     */
    public static @NonNull IPath getAbsoluteFilePath(String relativePath) {
        Plugin plugin = TmfCoreTestPlugin.getDefault();
        if (plugin == null) {
            /*
             * Shouldn't happen but at least throw something to get the test to
             * fail early
             */
            throw new IllegalStateException();
        }
        URL location = FileLocator.find(plugin.getBundle(), new Path(relativePath), null);
        try {
            return new Path(FileLocator.toFileURL(location).getPath());
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }

}
