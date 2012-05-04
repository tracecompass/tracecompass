/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng2.ui;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.preferences.ControlPreferences;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     *  The plug-in ID
     */
    public static final String PLUGIN_ID = "org.eclipse.linuxtools.lttng2.ui"; //$NON-NLS-1$

    /**
     *  The shared instance
     */
    private static Activator plugin;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * The constructor
     */
    public Activator() {
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    // ------------------------------------------------------------------------
    // AbstractUIPlugin
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        ControlPreferences.getInstance().init(getPreferenceStore());
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        ControlPreferences.getInstance().dispose();
        plugin = null;
        super.stop(context);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeImageRegistry(org.eclipse.jface.resource.ImageRegistry)
     */
    @Override
    protected void initializeImageRegistry(ImageRegistry reg) {
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    public Image getImageFromPath(String path) {
        return getImageDescripterFromPath(path).createImage();
    }

    public ImageDescriptor getImageDescripterFromPath(String path) {
        return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    public Image getImageFromImageRegistry(String path) {
        Image icon = getImageRegistry().get(path);
        if (icon == null) {
            icon = getImageDescripterFromPath(path).createImage();
            plugin.getImageRegistry().put(path, icon);
        }
        return icon;
    }
    
    /**
     * Loads the image in the plug-ins image registry (if necessary) and returns the image
     * @param url - URL relative to the Bundle
     * @return  the image
     */
    public Image loadIcon(String url) {
        String key = plugin.getBundle().getSymbolicName() + "/" + url; //$NON-NLS-1$
        Image icon = plugin.getImageRegistry().get(key);
        if (icon == null) {
            URL imageURL = plugin.getBundle().getResource(url);
            ImageDescriptor descriptor = ImageDescriptor.createFromURL(imageURL);
            icon = descriptor.createImage();
            plugin.getImageRegistry().put(key, icon);
        }
        return icon;
    }

}
