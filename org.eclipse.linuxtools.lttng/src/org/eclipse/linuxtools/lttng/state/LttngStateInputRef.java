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
package org.eclipse.linuxtools.lttng.state;

import org.eclipse.linuxtools.lttng.event.LttngEvent;
import org.eclipse.linuxtools.lttng.state.model.ILttngStateInputRef;
import org.eclipse.linuxtools.lttng.trace.LTTngTextTrace;
import org.eclipse.linuxtools.lttng.trace.LTTngTrace;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.trace.TmfTrace;

/**
 * Interface data provider to the state.model package
 * 
 * @author alvaro
 * 
 */
public class LttngStateInputRef implements ILttngStateInputRef {
	
	private int cpuNumber = -1;
	
	// ========================================================================
	// Table data
	// =======================================================================
	TmfTrace<LttngEvent> log = null;

	// ========================================================================
	// Constructor
	// ========================================================================
	LttngStateInputRef(TmfTrace<LttngEvent> log) {
		this.log = log;
		
		if ( log instanceof LTTngTrace) {
			cpuNumber = ((LTTngTrace)log).getCpuNumber();
		}
		else if ( log instanceof LTTngTextTrace) {
			cpuNumber = ((LTTngTextTrace)log).getCpuNumber();
		}
	}

	// ========================================================================
	// Methods
	// =======================================================================
	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.linuxtools.lttng.ui.state.model.ILttngStateInputRef#
	 * getNumberOfCpus()
	 */
	// @Override
	public int getNumberOfCpus() {
		return cpuNumber;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.linuxtools.lttng.ui.state.model.ILttngStateInputRef#
	 * getTraceTimeWindow()
	 */
	// @Override
	public TmfTimeRange getTraceTimeWindow() {
		if (log != null) {
			return log.getTimeRange();
			
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.linuxtools.lttng.state.model.ILttngStateInputRef#getTraceId()
	 */
	public String getTraceId() {
		if (log != null) {
			return log.getName();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.linuxtools.lttng.state.model.ILttngStateInputRef#
	 * getExperimentTimeWindow()
	 */
	public TmfTimeRange getExperimentTimeWindow() {
		// TODO Using the Trace time window temporarily, we need to replace with
		// the Experiment level
		if (log != null) {
			return log.getTimeRange();
		}
		return null;
	}
}
