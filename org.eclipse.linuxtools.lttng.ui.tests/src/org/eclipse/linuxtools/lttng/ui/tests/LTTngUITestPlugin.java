/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.ui.tests;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class LTTngUITestPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.linuxtools.lttng.ui.tests";

	// The shared instance
	private static LTTngUITestPlugin plugin;
	
	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------

	public LTTngUITestPlugin() {
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

	// ------------------------------------------------------------------------
	// Accessors
	// ------------------------------------------------------------------------

	public static LTTngUITestPlugin getDefault() {
		return plugin;
	}

}
