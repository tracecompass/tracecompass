/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.tests;

import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * <b><u>TmfUITestPlugin</u></b>
 * <p>
 * The activator class controls the plug-in life cycle
 *
 * @author Marc-Andre Laperle
 */
public class TmfUITestPlugin extends AbstractUIPlugin {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     *  The plug-in ID
     */
    public static final String PLUGIN_ID = "org.eclipse.tracecompass.tmf.ui.tests";

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

    /**
     * Loads icons into the bundle's image registry
     *
     * @param bundle
     *          the bundle
     * @param url
     *          the icon url
     * @return the image
     */
    public static @Nullable Image loadIcon(Bundle bundle, String url) {
        if (bundle == null) {
            return null;
        }
        Activator plugin = Activator.getDefault();
        String key = bundle.getSymbolicName() + "/" + url; //$NON-NLS-1$
        Image icon = plugin.getImageRegistry().get(key);
        if (icon == null) {
            URL imageURL = bundle.getResource(url);
            ImageDescriptor descriptor = ImageDescriptor.createFromURL(imageURL);
            if (descriptor != null) {
                icon = descriptor.createImage();
                plugin.getImageRegistry().put(key, icon);
            }
        }
        return icon;
    }

    /**
     * Logs a message and exception with severity ERROR in the runtime log of
     * the plug-in.
     *
     * @param message
     *            A message to log
     */
    public void logError(String message) {
        getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message));

    }
}
