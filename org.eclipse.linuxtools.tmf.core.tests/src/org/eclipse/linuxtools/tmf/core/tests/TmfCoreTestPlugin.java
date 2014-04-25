/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.linuxtools.internal.tmf.core.TmfCoreTracer;
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
    public static final String PLUGIN_ID = "org.eclipse.linuxtools.tmf.tests";

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

}
