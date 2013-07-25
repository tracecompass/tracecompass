/**********************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.tracing.rcp.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 *
 * @author Bernd Hufmann
 */
public class TracingRcpPlugin extends AbstractUIPlugin {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The plug-in ID
     */
    public static final String PLUGIN_ID = "org.eclipse.linuxtools.tracing.rcp.ui"; //$NON-NLS-1$

    /**
     * The default workspace name
     */
    public static final String WORKSPACE_NAME = ".traceviewer"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // The shared instance
    private static TracingRcpPlugin fPlugin;

    // ------------------------------------------------------------------------
    // Constructor(s)
    // ------------------------------------------------------------------------
    /**
     * The default constructor
     */
    public TracingRcpPlugin() {
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static TracingRcpPlugin getDefault() {
        return fPlugin;
    }

    /**
     * Gets the tracing workspace root directory
     *
     * @return the tracing workspace root directory
     */
    public static String getWorkspaceRoot() {
        return System.getProperty("user.home"); //$NON-NLS-1$
    }

    // ------------------------------------------------------------------------
    // Operation
    // ------------------------------------------------------------------------
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        fPlugin = this;
        }

    @Override
    public void stop(BundleContext context) throws Exception {
        fPlugin = null;
        super.stop(context);
}

    /**
     * Returns an image descriptor for the image file at the given
     * plug-in relative path
     *
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }
}
