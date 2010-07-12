/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui;

import org.eclipse.linuxtools.tmf.ui.parsers.ParserProviderManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * <b><u>TmfUiPlugin</u></b>
 * <p>
 * The activator class controls the plug-in life cycle
 */
public class TmfUiPlugin extends AbstractUIPlugin {

    // ========================================================================
    // Attributes
    // ========================================================================

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.linuxtools.tmf.ui";

	// The shared instance
	private static TmfUiPlugin plugin;
	
    // ========================================================================
    // Constructors
    // ========================================================================

	/**
	 * The constructor
	 */
	public TmfUiPlugin() {
	}

    // ========================================================================
    // Accessors
    // ========================================================================

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static TmfUiPlugin getDefault() {
		return plugin;
	}

    // ========================================================================
    // Operators
    // ========================================================================

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		ParserProviderManager.init();
	}

    /*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

    public Image getImageFromPath(String path)
    {
        return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path).createImage();
    }

}
