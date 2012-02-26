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

package org.eclipse.linuxtools.lttng.core.tests;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * <b><u>TmfCoreTestPlugin</u></b>
 * <p>
 * The activator class controls the plug-in life cycle
 */
@SuppressWarnings("nls")
public class LTTngCoreTestPlugin extends Plugin {

    // ========================================================================
    // Attributes
    // ========================================================================

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.linuxtools.lttng.tests";

	// The shared instance
	private static LTTngCoreTestPlugin plugin;
	
    // ========================================================================
    // Constructors
    // ========================================================================

	/**
	 * The constructor
	 */
	public LTTngCoreTestPlugin() {
		plugin = this;
	}

    // ========================================================================
    // Accessors
    // ========================================================================

    /**
     * @return the shared instance
     */
    public static LTTngCoreTestPlugin getPlugin() {
        return plugin;
    }
    
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
}
