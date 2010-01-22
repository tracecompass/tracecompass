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

package org.eclipse.linuxtools.tmf;

import org.eclipse.core.runtime.Plugin;

/**
 * <b><u>TmfCorePlugin</u></b>
 * <p>
 * The activator class controls the plug-in life cycle
 */
public class TmfCorePlugin extends Plugin {

    // ========================================================================
    // Attributes
    // ========================================================================

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.linuxtools.tmf";

	// The shared instance
	private static TmfCorePlugin plugin;
	
    // ========================================================================
    // Constructors
    // ========================================================================

	/**
	 * The constructor
	 */
	public TmfCorePlugin() {
		plugin = this;
	}

    // ========================================================================
    // Accessors
    // ========================================================================

    /**
     * @return the shared instance
     */
    public static TmfCorePlugin getDefault() {
        return plugin;
    }

}
