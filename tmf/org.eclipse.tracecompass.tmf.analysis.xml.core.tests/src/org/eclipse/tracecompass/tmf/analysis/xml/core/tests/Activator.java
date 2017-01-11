/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation and API
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.core.tests;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

    /**
     * The plug-in ID
     */
    public static final String PLUGIN_ID = "org.eclipse.tracecompass.tmf.analysis.xml.core.tests"; //$NON-NLS-1$

    // The shared instance
    private static Activator plugin;

    /**
     * The constructor
     */
    public Activator() {
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    /**
     * Gets the absolute path from a path relative to this plugin's root
     *
     * @param relativePath
     *            The path relative to this plugin
     * @return The absolute path corresponding to this relative path
     */
    public static @NonNull IPath getAbsolutePath(Path relativePath) {
        Activator plugin2 = getDefault();
        if (plugin2 == null) {
            /*
             * Shouldn't happen but at least throw something to get the test to
             * fail early
             */
            throw new IllegalStateException();
        }
        URL location = FileLocator.find(plugin2.getBundle(), relativePath, null);
        try {
            IPath path = new Path(FileLocator.toFileURL(location).getPath());
            return path;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
