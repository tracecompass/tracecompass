/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Patrick Tasse - Updated for TMF 2.0
 *******************************************************************************/

package org.eclipse.linuxtools.internal.gdbtrace.core;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * GDB Tracepoint Analysis Core plug-in activator
 * @author Francois Chouinard
 */
public class GdbTraceCorePlugin extends Plugin {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /** The plug-in ID */
    public static final String PLUGIN_ID = "org.eclipse.linuxtools.gdbtrace.core"; //$NON-NLS-1$

    private static GdbTraceCorePlugin plugin;

    private static BundleContext fBundleContext;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor
     */
    public GdbTraceCorePlugin() {
    }

    // ------------------------------------------------------------------------
    // Plugin
    // ------------------------------------------------------------------------

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        fBundleContext = context;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    // ------------------------------------------------------------------------
    // Accessor
    // ------------------------------------------------------------------------

    /**
     * Returns the GDB Tracepoints Core plug-in instance.
     *
     * @return the GDB Tracepoints Core plug-in instance
     */
    public static GdbTraceCorePlugin getDefault() {
        return plugin;
    }

    /**
     * Returns the bundle context
     *
     * @return the bundle context
     */
    public static BundleContext getBundleContext() {
        return fBundleContext;
    }

}
