/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.lami.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
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
    public static final String PLUGIN_ID = "org.eclipse.tracecompass.analysis.lami.ui"; //$NON-NLS-1$

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

    @Override
    protected void initializeImageRegistry(ImageRegistry reg) {
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Get the image object from a given path
     *
     * @param path
     *            The path to the image file
     * @return The Image object
     */
    public Image getImageFromPath(String path) {
        return getImageDescripterFromPath(path).createImage();
    }

    /**
     * Get the ImageDescriptor from a given path
     *
     * @param path
     *            The path to the image file
     * @return The ImageDescriptor object
     */
    public ImageDescriptor getImageDescripterFromPath(String path) {
        return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    /**
     * Get the Image from a registry
     *
     * @param path
     *            The path to the image registry
     * @return The Image object
     */
    public Image getImageFromImageRegistry(String path) {
        Image icon = getImageRegistry().get(path);
        if (icon == null) {
            icon = getImageDescripterFromPath(path).createImage();
            plugin.getImageRegistry().put(path, icon);
        }
        return icon;
    }

    /**
     * Logs a message with severity INFO in the runtime log of the plug-in.
     *
     * @param message A message to log
     */
    public void logInfo(String message) {
        getLog().log(new Status(IStatus.INFO, PLUGIN_ID, message));
    }

    /**
     * Logs a message and exception with severity INFO in the runtime log of the plug-in.
     *
     * @param message A message to log
     * @param exception A exception to log
     */
    public void logInfo(String message, Throwable exception) {
        getLog().log(new Status(IStatus.INFO, PLUGIN_ID, message, exception));
    }

    /**
     * Logs a message and exception with severity WARNING in the runtime log of the plug-in.
     *
     * @param message A message to log
     */
    public void logWarning(String message) {
        getLog().log(new Status(IStatus.WARNING, PLUGIN_ID, message));
    }

    /**
     * Logs a message and exception with severity WARNING in the runtime log of the plug-in.
     *
     * @param message A message to log
     * @param exception A exception to log
     */
    public void logWarning(String message, Throwable exception) {
        getLog().log(new Status(IStatus.WARNING, PLUGIN_ID, message, exception));
    }

    /**
     * Logs a message and exception with severity ERROR in the runtime log of the plug-in.
     *
     * @param message A message to log
     */
    public void logError(String message) {
        getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message));
    }

    /**
     * Logs a message and exception with severity ERROR in the runtime log of the plug-in.
     *
     * @param message A message to log
     * @param exception A exception to log
     */
    public void logError(String message, Throwable exception) {
        getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message, exception));
    }

}
