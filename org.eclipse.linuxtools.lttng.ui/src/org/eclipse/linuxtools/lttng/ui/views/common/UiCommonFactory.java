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
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.ui.views.common;

/**
 * @author alvaro
 */
public class UiCommonFactory {
	// ========================================================================
	// Data
	// ========================================================================
	private static DataRequestQueue queue = null;

	
	
	// ========================================================================
	// Methods
	// ========================================================================

	/**
	 * Needed when a queue is shared e.g. avoid multiple requests competing for
	 * same resources
	 * 
	 * @return
	 */
	public synchronized static DataRequestQueue getQueue() {
		if (queue == null) {
			queue = new DataRequestQueue();
		}
		return queue;
	}
}
