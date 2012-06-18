/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alvaro Sanchez-Leon (alvsan09@gmail.com) - Initial API and implementation
 *   Marc Dumais (marc.dumais@ericsson.com) - Fix for 316455 (first part)
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.core.request;

import org.eclipse.linuxtools.internal.lttng.core.event.LttngEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;

/**
 * This class is an extension of Tmf Event Request which includes specific
 * references i.e. a status listener to indicate the start and end of the
 * request
 *
 * @author alvaro
 *
 */
public abstract class LttngBaseEventRequest extends TmfEventRequest {

	// ========================================================================
	// Data
	// =======================================================================
	private long numOfEvents = 0;
	private boolean clearDataInd = false;
	/**
	 * The time to send events to the application as requested, Note: The start
	 * time of the request for base events is adjusted to the nearest check
	 * point
	 */
	private final ITmfTimestamp fDispatchTime;

	// ========================================================================
	// Constructors
	// =======================================================================
	/**
	 * @param range
	 * @param dispatchTime
	 * @param offset
	 * @param nbEvents
	 * @param maxBlockSize
	 * @param traceState
	 * @param listener
	 */
	public LttngBaseEventRequest(TmfTimeRange range, ITmfTimestamp dispatchTime, long offset, int nbEvents,
			int maxBlockSize, ITmfDataRequest.ExecutionType execType) {
		super(LttngEvent.class, range, (int) offset, nbEvents, maxBlockSize, execType);
		fDispatchTime = dispatchTime;
	}

	@Override
	public void cancel() {
		super.cancel();
	}

	// ========================================================================
	// Methods
	// =======================================================================

	/**
	 * Trigger the start to process this request right after the notification to
	 * the interested listeners
	 *
	 * @param experiment
	 * @param broadcast
	 *            true: All views, false: only to registered listeners
	 */
	public void startRequestInd(TmfExperiment experiment,
			boolean broadcast) {
		// trigger the start to process this request
		experiment.sendRequest(this);
	}

	/**
	 * @param numOfEvents
	 *            the numOfEvents to set
	 */
	public void setNumOfEvents(long numOfEvents) {
		this.numOfEvents = numOfEvents;
	}

	/**
	 * @return the numOfEvents
	 */
	public long getNumOfEvents() {
		return numOfEvents;
	}

	/**
	 * @param clearAllData
	 *            indicates the need to clear all previous data e.g. a new
	 *            experiment selection
	 */
	public void setclearDataInd(boolean clearAllData) {
		this.clearDataInd = clearAllData;
	}

	/**
	 * Returns indication - clearing of all existing data model is required e.g
	 * from the selection of a new experiment
	 *
	 * @return
	 */
	public boolean isclearDataInd() {
		return clearDataInd;
	}

	/**
	 * @return The time to start dispatching events to the application
	 */
	public ITmfTimestamp getDispatchTime() {
		return fDispatchTime;
	}

}