/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.tests;

import java.io.File;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.linuxtools.internal.ctf.core.Activator;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class CtfCoreTestPlugin extends Plugin {

    private static final String TEMP_DIR_NAME = ".temp"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /** The plug-in ID */
    public static final String PLUGIN_ID = "org.eclipse.linuxtools.ctf.core.tests";

    // The shared instance
    private static CtfCoreTestPlugin fPlugin;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * The constructor
     */
    public CtfCoreTestPlugin() {
        setDefault(this);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @return the shared instance
     */
    public static CtfCoreTestPlugin getDefault() {
        return fPlugin;
    }

    /**
     * @param plugin
     *            the shared instance
     */
    private static void setDefault(CtfCoreTestPlugin plugin) {
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

    /**
     * Get the temporary directory path. If there is an instance of Eclipse
     * running, the temporary directory will reside under the workspace.
     *
     * @return the temporary directory path suitable to be passed to the
     *         java.io.File constructor without a trailing separator
     */
    public static String getTemporaryDirPath() {
        String property = System.getProperty("osgi.instance.area"); //$NON-NLS-1$
        if (property != null) {
            try {
                File dir = URIUtil.toFile(URIUtil.fromString(property));
                dir = new File(dir.getAbsolutePath() + File.separator + TEMP_DIR_NAME);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                return dir.getAbsolutePath();
            } catch (URISyntaxException e) {
                Activator.logError(e.getLocalizedMessage(), e);
            }
        }
        return System.getProperty("java.io.tmpdir"); //$NON-NLS-1$
    }
}
