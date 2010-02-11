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

package org.eclipse.linuxtools.lttng.tests.state;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.lttng.state.StateManager;
import org.eclipse.linuxtools.lttng.state.model.LttngTraceState;
import org.eclipse.linuxtools.lttng.state.model.StateModelFactory;

/**
 * @author alvaro
 * 
 */
public class StateManagerFactoryTestSupport {
	// ========================================================================
	// Data
	// =======================================================================

	private static final Map<String, StateManager> instanceBook = new HashMap<String, StateManager>();
	
	// ========================================================================
	// Methods
	// =======================================================================

	/**
	 * Provide a stateManager instance per trace
	 * 
	 * @return
	 */
	public static StateManager getManager(String traceUniqueId) {
		if (traceUniqueId == null) {
			return null;
		}

		if (instanceBook.containsKey(traceUniqueId)) {
			return instanceBook.get(traceUniqueId);
		}

		LttngTraceState traceModel = StateModelFactory.getStateEntryInstance();
		StateStacksHandlerTestSupport stateInputHandler = new StateStacksHandlerTestSupport(traceModel);
		StateManager manager = new StateManager(stateInputHandler);

		instanceBook.put(traceUniqueId, manager);
		return manager;
	}

	/**
	 * Remove previously registered managers
	 * @param traceUniqueId
	 */
	public static void removeManager(String traceUniqueId) {
		if (traceUniqueId != null && instanceBook.containsKey(traceUniqueId)) {
			instanceBook.remove(traceUniqueId);
		}
	}

}
