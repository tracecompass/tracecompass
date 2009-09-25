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
package org.eclipse.linuxtools.lttng.ui.views.controlflow.model;

import java.util.Vector;

import org.eclipse.linuxtools.lttng.ui.model.trange.TimeRangeEventProcess;

/**
 * Common location to allocate the processes in use by the Control flow view
 * 
 * @author alvaro
 * 
 */
public class FlowProcessContainer {
	// ========================================================================
	// Data
	// ========================================================================
	private final Vector<TimeRangeEventProcess> processes = new Vector<TimeRangeEventProcess>();
	private int idgen = 0;

	// ========================================================================
	// Constructor
	// ========================================================================

	/**
	 * Package level constructor
	 */
	FlowProcessContainer() {

	}

	// ========================================================================
	// Methods
	// ========================================================================
	/**
	 * Interface to add processes.
	 * 
	 * @param process
	 */
	public void addProcesse(TimeRangeEventProcess process) {
		if (process != null) {
			processes.add(process);
		}
	}

	/**
	 * This method is intended for ready only purposes in order to keep the
	 * internal data structure in Synch
	 * 
	 * @return
	 */
	public Vector<TimeRangeEventProcess> readProcesses() {
		return processes;
	}

	/**
	 * Clear the children information for processes related to a specific trace
	 * e.g. just before refreshing data with a new time range
	 * 
	 * @param traceId
	 */
	public void clearChildren(String traceId) {
		String procTraceId;
		for (TimeRangeEventProcess process : processes) {
			procTraceId = process.getTraceID();
			if (procTraceId.equals(traceId)) {
				process.getTraceEvents().clear();
				process.getChildEventComposites().clear();
			}
		}
	}

	/**
	 * remove the processes related to a specific trace e.g. during trace
	 * removal
	 * 
	 * @param traceId
	 */
	public void removeProcesses(String traceId) {
		String procTraceId;
		for (TimeRangeEventProcess process : processes) {
			procTraceId = process.getTraceID();
			if (procTraceId.equals(traceId)) {
			    // Children and traceEvent will get claimed by the garbage collector when process is unreferenced
			    // Therefore, we don't need to removed them
				processes.remove(process);
			}
		}
	}

	/**
	 * A match is returned if the three arguments received match an entry in the
	 * Map, otherwise null is returned
	 * 
	 * @param pid
	 * @param creationtime
	 * @param traceID
	 * @return
	 */
	public TimeRangeEventProcess findProcess(Long pid, Long creationtime,
			String traceID) {
		TimeRangeEventProcess rprocess = null;

		for (TimeRangeEventProcess process : processes) {
			if (process.getPid().equals(pid)) {
				if (process.getCreationTime().equals(creationtime)) {
					if (process.getTraceID().equals(traceID)) {
						return process;
					}
				}
			}
		}

		return rprocess;
	}

	/**
	 * Generate a unique process id while building the process list
	 * 
	 * @return
	 */
	public int bookProcId() {
		return idgen++;
	}

}
