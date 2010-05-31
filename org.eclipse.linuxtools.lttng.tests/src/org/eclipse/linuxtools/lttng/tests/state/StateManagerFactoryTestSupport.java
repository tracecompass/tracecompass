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

import org.eclipse.linuxtools.lttng.control.LttngCoreProviderFactory;
import org.eclipse.linuxtools.lttng.event.LttngSyntheticEvent;
import org.eclipse.linuxtools.lttng.model.LTTngTreeNode;
import org.eclipse.linuxtools.lttng.state.LttngStateException;
import org.eclipse.linuxtools.lttng.state.model.LttngTraceState;
import org.eclipse.linuxtools.lttng.state.model.StateModelFactory;
import org.eclipse.linuxtools.lttng.state.trace.IStateTraceManager;
import org.eclipse.linuxtools.lttng.state.trace.StateTraceManager;
import org.eclipse.linuxtools.tmf.component.TmfEventProvider;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace;

/**
 * @author alvaro
 * 
 */
public class StateManagerFactoryTestSupport {
	// ========================================================================
	// Data
	// =======================================================================

	private static final Map<String, IStateTraceManager> instanceBook = new HashMap<String, IStateTraceManager>();
	private static TmfEventProvider<LttngSyntheticEvent> feventProvider = null;
	// ========================================================================
	// Methods
	// =======================================================================

	/**
	 * Provide a stateManager instance per trace
	 * 
	 * @return
	 */
	public static IStateTraceManager getManager(ITmfTrace trace) {
		String traceUniqueId = trace.getName();

		if (traceUniqueId == null) {
			return null;
		}

		if (instanceBook.containsKey(traceUniqueId)) {
			return instanceBook.get(traceUniqueId);
		}

		LttngTraceState traceModel = StateModelFactory.getStateEntryInstance();
		IStateTraceManager manager = null;

		if (feventProvider == null) {
			feventProvider = LttngCoreProviderFactory.getEventProvider();
		}

		// catch construction problems
		Long id = 0L;
		LTTngTreeNode parent = null;

		try {
			manager = new StateTraceManager(id, parent, traceUniqueId, trace,
					traceModel,
					feventProvider);
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
