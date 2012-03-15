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
package org.eclipse.linuxtools.internal.lttng.core.state.model;

import org.eclipse.linuxtools.internal.lttng.core.state.LttngStateException;
import org.eclipse.linuxtools.internal.lttng.core.state.resource.ILttngStateContext;

/**
 * Entry point to the package
 * 
 * @author alvaro
 * 
 */
public class StateModelFactory {

	// ========================================================================
	// Methods
	// =======================================================================

	/**
	 * Return an instance of the entry class, the entry class contains
	 * references to the internal model elements.
	 * 
	 * One instance is expected to be created per LttngTrace.
	 * 
	 * This method shall be used when a trace is not opened yet and will be
	 * initialized later via the init() method.
	 * 
	 * @return
	 */
	public static LttngTraceState getStateEntryInstance() {
		return new LttngTraceState();
	}

	/**
	 * Provide a LttngTraceState when the input data reference is known e.g.
	 * when exchanging the State provider to check point clone.
	 * 
	 * @return
	 */
	public static LttngTraceState getStateEntryInstance(
			ILttngStateContext stateInputRef) {
		LttngTraceState traceState = new LttngTraceState();
		try {
			traceState.init(stateInputRef);
		} catch (LttngStateException e) {
			e.printStackTrace();
		}
		return traceState;
	}
}
