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

package org.eclipse.linuxtools.lttng.core.tests.state;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.internal.lttng.core.model.LTTngTreeNode;
import org.eclipse.linuxtools.internal.lttng.core.state.LttngStateException;
import org.eclipse.linuxtools.internal.lttng.core.state.trace.IStateTraceManager;
import org.eclipse.linuxtools.internal.lttng.core.state.trace.StateTraceManager;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * @author alvaro
 * 
 */
public class StateManagerFactoryTestSupport {
	// ========================================================================
	// Data
	// =======================================================================

	private static final Map<String, IStateTraceManager> instanceBook = new HashMap<String, IStateTraceManager>();

	// ========================================================================
	// Methods
	// =======================================================================

	/**
	 * Provide a stateManager instance per trace
	 * 
	 * @return
	 */
	public static IStateTraceManager getManager(ITmfTrace<?> trace) {
		String traceUniqueId = trace.getName();

		if (traceUniqueId == null) {
			return null;
		}

		if (instanceBook.containsKey(traceUniqueId)) {
			return instanceBook.get(traceUniqueId);
		}

		// LttngTraceState traceModel =
		// StateModelFactory.getStateEntryInstance();
		IStateTraceManager manager = null;

		// catch construction problems
		Long id = 0L;
		LTTngTreeNode parent = null;

		try {
			manager = new StateTraceManager(id, parent, traceUniqueId, trace);
		} catch (LttngStateException e) {
			e.printStackTrace();
		}

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
