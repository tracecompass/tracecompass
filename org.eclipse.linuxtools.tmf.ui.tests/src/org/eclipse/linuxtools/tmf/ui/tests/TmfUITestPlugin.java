/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.tests;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * <b><u>TmfUITestPlugin</u></b>
 * <p>
 * The activator class controls the plug-in life cycle
 *
 * @author Marc-Andre Laperle
 */
public class TmfUITestPlugin extends Plugin {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     *  The plug-in ID
     */
    public static final String PLUGIN_ID = "org.eclipse.linuxtools.tmf.ui.tests";

    // The shared instance
    private static TmfUITestPlugin fPlugin;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * The constructor
     */
    public TmfUITestPlugin() {
        setDefault(this);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @return the shared instance
     */
    public static TmfUITestPlugin getDefault() {
        return fPlugin;
    }

    /**
     * @param plugin the shared instance
     */
    private static void setDefault(TmfUITestPlugin plugin) {
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
