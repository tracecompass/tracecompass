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

import org.eclipse.linuxtools.lttng.state.StateDataRequest;
import org.eclipse.linuxtools.lttng.ui.TraceDebug;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;

/**
 * One instance to keep track of the state of data request and a single time
 * range data request pending in queue
 * 
 * @author alvaro
 * 
 */
public class DataRequestState {

	// ========================================================================
	// Data
	// ========================================================================
	public enum RequestState {
		IDLE, BUSY
	}

	private TmfTimeRange queued = null;
	RequestState state = RequestState.IDLE;
	private StateDataRequest currentRequest = null;

	// ========================================================================
	// Methods
	// ========================================================================
	/**
	 * @return the data request time range in queue
	 */
	public synchronized TmfTimeRange peekQueued() {
		return queued;
	}

	/**
	 * @return the data request time range in queue and reset it reference to
	 *         null to get ready for a new entry in queue
	 */
	public synchronized TmfTimeRange popQueued() {
		// Save the reference to current request
		TmfTimeRange result = queued;
		// remove from queue
		queued = null;
		// Send original reference
		return result;
	}

	/**
	 * @param queued
	 *            <p>
	 *            Set the data request time range to be waiting for processing
	 *            </p>
	 *            <p>
	 *            Only the latest request is preserved
	 *            </p>
	 */
	public synchronized void setQueued(TmfTimeRange nqueued) {
		if (TraceDebug.isDEBUG()) {
			if (this.queued != null) {
				StringBuilder sb = new StringBuilder(
						"Queued request replaced from: "
								+ queued.getStartTime() + "-"
								+ queued.getEndTime() + "\n\t\t to: "
								+ nqueued.getStartTime() + "-"
								+ nqueued.getEndTime());
				TraceDebug.debug(sb.toString());
			}
		}

		this.queued = nqueued;
	}

	/**
	 * @return the state
	 */
	public synchronized RequestState getState() {
		return state;
	}

	/**
	 * @param state
	 *            the state to set
	 */
	public synchronized void setState(RequestState state) {
		this.state = state;
	}

	public synchronized void setCurrentRequest(StateDataRequest currentRequest) {
		this.currentRequest = currentRequest;
	}

	public synchronized StateDataRequest getCurrentRequest() {
		return currentRequest;
	}

}
