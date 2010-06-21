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
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.request;

import org.eclipse.linuxtools.lttng.event.LttngEvent;
import org.eclipse.linuxtools.lttng.state.model.LttngTraceState;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace;

/**
 * This class is an extension of Tmf Event Request which includes specific
 * references i.e. a status listener to indicate the start and end of the
 * request
 * 
 * @author alvaro
 * 
 */
public abstract class LttngBaseEventRequest extends TmfEventRequest<LttngEvent> {

	// ========================================================================
	// Data
	// =======================================================================
	private long numOfEvents = 0;
	private boolean clearDataInd = false;
	private final LttngTraceState ftraceModel;
	private final ITmfTrace ftrace;
	/**
	 * The time to send events to the application as requested, Note: The start
	 * time of the request for base events is adjusted to the nearest check
	 * point
	 */
	private final TmfTimestamp fDispatchTime;

	// ========================================================================
	// Constructors
	// =======================================================================
	/**
	 * @param range
	 * @param dispatchTime
	 * @param offset
	 * @param nbEvents
	 * @param maxBlockSize
	 * @param traceModel
	 * @param listener
	 */
	public LttngBaseEventRequest(TmfTimeRange range, TmfTimestamp dispatchTime, long offset, int nbEvents,
			int maxBlockSize, LttngTraceState traceModel, ITmfDataRequest.ExecutionType execType, ITmfTrace trace) {
		super(LttngEvent.class, range, nbEvents, maxBlockSize, execType);
		ftraceModel = traceModel;
		fDispatchTime = dispatchTime;
		ftrace = trace;

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
	public void startRequestInd(TmfExperiment<LttngEvent> experiment,
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
	 * @return the ftraceModel, Trace state-data-model associated to this event
	 */
	public LttngTraceState getTraceModel() {
		return ftraceModel;
	}

	/**
	 * @return The time to start dispatching events to the application
	 */
	public TmfTimestamp getDispatchTime() {
		return fDispatchTime;
	}

	/**
	 * @return
	 */
	public ITmfTrace getTrace() {
		return ftrace;
	}
}