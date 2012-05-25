/*******************************************************************************
 * Copyright (c) 2009, 2010, 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * <b><u>TmfCorePlugin</u></b>
 * <p>
 * The activator class controls the plug-in life cycle. No more than one such
 * plug-in can exist at any time.
 */
public class Activator extends Plugin {

	// ------------------------------------------------------------------------
    // Attributes
	// ------------------------------------------------------------------------

    /**
     * The plug-in ID 
     */
	public static final String PLUGIN_ID = "org.eclipse.linuxtools.tmf.core"; //$NON-NLS-1$

	/**
     * The shared instance
     */
	private static Activator fPlugin;
	
	// ------------------------------------------------------------------------
    // Constructors
	// ------------------------------------------------------------------------

	/**
     * Constructor
     */
	public Activator() {
		setDefault(this);
	}

	// ------------------------------------------------------------------------
    // Accessors
	// ------------------------------------------------------------------------

	/**
     * Returns the TMF UI plug-in instance.
     *
     * @return the TMF UI plug-in instance.
     */
    public static Activator getDefault() {
        return fPlugin;
    }

    // Sets plug-in instance
	private static void setDefault(Activator plugin) {
		fPlugin = plugin;
	}

	// ------------------------------------------------------------------------
    // Plugin
	// ------------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		setDefault(this);
		Tracer.init();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		Tracer.stop();
		setDefault(null);
		super.stop(context);
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
