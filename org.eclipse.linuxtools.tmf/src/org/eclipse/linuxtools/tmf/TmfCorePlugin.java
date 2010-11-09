/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * <b><u>TmfCorePlugin</u></b>
 * <p>
 * The activator class controls the plug-in life cycle. No more than one such
 * plug-in can exist at any time.
 */
public class TmfCorePlugin extends Plugin {

	// ------------------------------------------------------------------------
    // Attributes
	// ------------------------------------------------------------------------

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.linuxtools.tmf"; //$NON-NLS-1$

	// The shared instance
	private static TmfCorePlugin fPlugin;
	
	// ------------------------------------------------------------------------
    // Constructors
	// ------------------------------------------------------------------------

	/**
	 * The constructor
	 */
	public TmfCorePlugin() {
		setDefault(this);
	}

	// ------------------------------------------------------------------------
    // Accessors
	// ------------------------------------------------------------------------

    /**
     * @return the shared instance
     */
    public static TmfCorePlugin getDefault() {
        return fPlugin;
    }

	/**
	 * @param plugin the shared instance
	 */
	private static void setDefault(TmfCorePlugin plugin) {
		fPlugin = plugin;
	}

	// ------------------------------------------------------------------------
    // Operations
	// ------------------------------------------------------------------------

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		setDefault(this);
		Tracer.init();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		Tracer.stop();
		setDefault(null);
		super.stop(context);
	}

}
