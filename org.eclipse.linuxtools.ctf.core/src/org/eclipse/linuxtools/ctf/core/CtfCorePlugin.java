/*******************************************************************************
 * Copyright (c) 2011-2012 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * <b><u>CtfCorePlugin</u></b>
 * <p>
 * The activator class controls the plug-in life cycle. No more than one such
 * plug-in can exist at any time.
 */
public class CtfCorePlugin extends Plugin {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // The plug-in ID
    public static final String PLUGIN_ID = "org.eclipse.linuxtools.ctf"; //$NON-NLS-1$

    // The shared instance
    private static CtfCorePlugin fPlugin;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public CtfCorePlugin() {
        setDefault(this);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    public static CtfCorePlugin getDefault() {
        return fPlugin;
    }

    private static void setDefault(CtfCorePlugin plugin) {
        fPlugin = plugin;
    }

    // ------------------------------------------------------------------------
    // Plugin
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

    // ------------------------------------------------------------------------
    // Logging
    // ------------------------------------------------------------------------

    public void log(String msg) {
        log(msg, null);
    }

    public void log(String msg, Exception e) {
        getLog().log(new Status(IStatus.INFO, PLUGIN_ID, IStatus.OK, msg, e));
    }

}
