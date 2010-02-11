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

package org.eclipse.linuxtools.lttng.state.experiment;

import java.util.Map;

import org.eclipse.linuxtools.lttng.state.StateManager;
import org.eclipse.linuxtools.lttng.state.StateStacksHandler;
import org.eclipse.linuxtools.lttng.state.model.LttngTraceState;
import org.eclipse.linuxtools.lttng.state.model.StateModelFactory;

/**
 * @author alvaro
 * 
 */
public class StateManagerFactory {
	// ========================================================================
	// Data
	// =======================================================================

	private static StateExperimentManager experimentManager = null;
	private static Map<String, StateManager> instanceBook = null;

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

		initCheck();

		if (instanceBook.containsKey(traceUniqueId)) {
			return instanceBook.get(traceUniqueId);
		}

		LttngTraceState traceModel = StateModelFactory.getStateEntryInstance();
		StateStacksHandler stateInputHandler = new StateStacksHandler(
				traceModel);
		StateManager manager = new StateManager(stateInputHandler);

		instanceBook.put(traceUniqueId, manager);
		return manager;
	}

	/**
	 * Provide the State trace set manager
	 * 
	 * @return
	 */
	public static StateExperimentManager getExperimentManager() {
		initCheck();
		return experimentManager;
	}

	/**
	 * Remove previously registered managers
	 * 
	 * @param traceUniqueId
	 */
	public static void removeManager(String traceUniqueId) {
		initCheck();
		if (traceUniqueId != null && instanceBook.containsKey(traceUniqueId)) {
			instanceBook.remove(traceUniqueId);
		}
	}

	/**
	 * initialization of factory
	 */
	private static void initCheck() {
		if (experimentManager == null) {
			experimentManager = new StateExperimentManager();
			instanceBook = experimentManager.getManagersByID();
		}
	}

	/**
	 * Clea up resources
	 */
	public static void dispose() {
		if (experimentManager != null) {
			experimentManager.dispose();
			experimentManager = null;
			instanceBook = null;
		}
	}
}
