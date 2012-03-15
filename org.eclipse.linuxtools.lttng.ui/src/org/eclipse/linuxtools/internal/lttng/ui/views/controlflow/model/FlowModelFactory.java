/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon (alvsan09@gmail.com) - Initial API and implementation
 * 	 Michel Dagenais (michel.dagenais@polymtl.ca) - Reference C implementation, used with permission
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.ui.views.controlflow.model;

import org.eclipse.linuxtools.internal.lttng.ui.views.common.ParamsUpdater;

/**
 * Instantiates the entry point classes to build the data model for this view
 * 
 * @author alvaro
 * 
 */
public class FlowModelFactory {
	// ========================================================================
	// Data
	// ========================================================================
	private static FlowProcessContainer procContainer = null;
	private static ParamsUpdater updater = null;
	
	
	// ========================================================================
	// Methods
	// ========================================================================
	/**
	 * Get Process data container
	 * @return
	 */
	public static FlowProcessContainer getProcContainer() {
		if (procContainer == null) {
			procContainer = new FlowProcessContainer();
		}
		return procContainer;
	}
	
	public static ParamsUpdater getParamsUpdater() {
		if (updater == null) {
			updater = new ParamsUpdater();
		}
		return updater;
	}
	
}
