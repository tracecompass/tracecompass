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

package org.eclipse.linuxtools.tmf.tests;

import org.eclipse.core.runtime.Plugin;

/**
 * <b><u>TmfTestPlugin</u></b>
 * <p>
 * The activator class controls the plug-in life cycle
 */
public class TmfCoreTestPlugin extends Plugin {

    // ========================================================================
    // Attributes
    // ========================================================================

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.linuxtools.tmf.tests";

	// The shared instance
	private static TmfCoreTestPlugin plugin;
	
    // ========================================================================
    // Constructors
    // ========================================================================

	/**
	 * The constructor
	 */
	public TmfCoreTestPlugin() {
		plugin = this;
	}

    // ========================================================================
    // Accessors
    // ========================================================================

    /**
     * @return the shared instance
     */
    public static TmfCoreTestPlugin getPlugin() {
        return plugin;
    }

}
