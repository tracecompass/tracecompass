/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.remote.core.tests;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class TmfRemoteCoreTestPlugin extends Plugin {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // The plug-in ID
    @SuppressWarnings("javadoc")
    public static final String PLUGIN_ID = "org.eclipse.tracecompass.tmf.remote.core.tests";

    // The shared instance
    private static TmfRemoteCoreTestPlugin fPlugin;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * The constructor
     */
    public TmfRemoteCoreTestPlugin() {
        setDefault(this);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @return the shared instance
     */
    public static TmfRemoteCoreTestPlugin getDefault() {
        return fPlugin;
    }

    /**
     * @param plugin the shared instance
     */
    private static void setDefault(TmfRemoteCoreTestPlugin plugin) {
        fPlugin = plugin;
    }

    // ------------------------------------------------------------------------
    // Operations
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

}
