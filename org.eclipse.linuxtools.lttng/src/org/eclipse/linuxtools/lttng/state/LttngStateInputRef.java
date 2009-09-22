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

import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.eclipse.linuxtools.lttng.state.model.ILttngStateInputRef;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.trace.TmfTrace;

/**
 * Interface data provider to the state.model package
 * 
 * @author alvaro
 * 
 */
public class LttngStateInputRef implements ILttngStateInputRef {
	// ========================================================================
	// Table data
	// =======================================================================
	JniTrace trace = null;
	TmfTrace log = null;

	// ========================================================================
	// Constructor
	// ========================================================================
	LttngStateInputRef(JniTrace trace, TmfTrace log) {
		this.trace = trace;
		this.log = log;
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
		if (trace != null) {
			return trace.getCpuNumber();
		}
		return 0;
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
