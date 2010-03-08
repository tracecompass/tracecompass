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

import org.eclipse.linuxtools.lttng.state.IStateDataRequestListener;
import org.eclipse.linuxtools.lttng.state.RequestCompletedSignal;
import org.eclipse.linuxtools.lttng.state.RequestStartedSignal;
import org.eclipse.linuxtools.lttng.state.StateDataRequest;
import org.eclipse.linuxtools.lttng.state.StateManager;
import org.eclipse.linuxtools.lttng.state.experiment.StateManagerFactory;
import org.eclipse.linuxtools.lttng.ui.TraceDebug;
import org.eclipse.linuxtools.lttng.ui.views.common.DataRequestState.RequestState;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;

/**
 * <p>
 * Abstract class used as a base for views handling specific time range data
 * requests
 * </p>
 * <p>
 * The class handles a single element queue of data requests, and a list of
 * requests in progress i.e. request can be triggered from different sources
 * e.g. opening a file as well as a new selected time window
 * </p>
 * 
 * @author alvaro
 * 
 */
public abstract class AbsTimeUpdateView extends TmfView implements
		IStateDataRequestListener {

	// ========================================================================
	// Data
	// ========================================================================
	private DataRequestState reqState = new DataRequestState();
	private String viewID = "";

	// ========================================================================
	// Constructor
	// ========================================================================
	public AbsTimeUpdateView(String viewID) {
		super("AbsTimeUpdateView");
		this.viewID = viewID;
	}

	// ========================================================================
	// Methods
	// ========================================================================
	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.linuxtools.lttng.state.IStateDataRequestListener#
	 * processingStarted(org.eclipse.linuxtools.lttng.state.StateDataRequest)
	 */
	@TmfSignalHandler
	public synchronized void processingStarted(RequestStartedSignal signal) {
		StateDataRequest request = signal.getRequest();
		if (request != null) {
			// Check if a newer request is in the queue
			TmfTimeRange newerReq = reqState.peekQueued();
			if (newerReq == null) {
				reqState.setState(DataRequestState.RequestState.BUSY);
				reqState.setCurrentRequest(request);

				waitCursor(true);

				StateManager smanager = request.getStateManager();
				// Clear the children on the Processes related to this
				// manager.
				// Leave the GUI in charge of the updated data.
				String traceId = smanager.getEventLog().getName();

				// indicate if the data model needs to be cleared e.g. a new
				// experiment is being selected
				boolean clearData = request.isclearDataInd();
				// no new time range for zoom orders
				TmfTimeRange trange = null;
				if (clearData) {
					// Time Range will be used to filter out events which are
					// not visible in one pixel
					trange = StateManagerFactory.getExperimentManager()
							.getExperimentTimeRange();
				}
				
				//Indicate if current data needs to be cleared and if so 
				//specify the new experiment time range that applies
				ModelUpdatePrep(traceId, clearData, trange);
			} else {
				// clean up any possible pending request
				request.cancel();

				// Start the new request.
				StateManagerFactory.getExperimentManager()
						.readExperimentTimeWindow(newerReq, viewID, this);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.linuxtools.lttng.state.IStateDataRequestListener#
	 * processingCompleted(org.eclipse.linuxtools.lttng.state.StateDataRequest)
	 */
	@TmfSignalHandler
	public synchronized void processingCompleted(RequestCompletedSignal signal) {
		StateDataRequest request = signal.getRequest();

		if (request == null) {
			return;
		} else {
			reqState.setCurrentRequest(null);

		}

		// No data refresh actions for cancelled requests.
		if (request.isCancelled() || request.isFailed()) {

			requestStateUpdate();

			if (TraceDebug.isDEBUG()) {
				TmfTimeRange trange = request.getRange();
				if (request.isCancelled()) {
					TraceDebug.debug("Request cancelled "
							+ trange.getStartTime() + "-" + trange.getEndTime()
							+ " Handled Events: " + request.getNumOfEvents());
				} else if (request.isFailed()) {
					TraceDebug.debug("Request Failed " + trange.getStartTime()
							+ "-" + trange.getEndTime() + " Handled Events: "
							+ request.getNumOfEvents());
				}
			}

			return;
		} else {
			ModelUpdateComplete(request);
			requestStateUpdate();
		}
	}

	/**
	 * Evaluates the need to either send a new data request or queue the request
	 * till next available opportunity. One element queue to keep the latest
	 * request only.
	 * 
	 * @param trange
	 */
	public synchronized void dataRequest(TmfTimeRange trange) {
		if (trange != null) {
			// cancelPendingRequests();
			StateDataRequest currentRequest = reqState.getCurrentRequest();
			// If a request is ongoing queue the new request
			if (reqState.getState().equals(RequestState.BUSY)) {
				reqState.setQueued(trange);
				currentRequest = reqState.getCurrentRequest();
				if (currentRequest != null) {
					currentRequest.cancel();
				} else {
					TraceDebug
							.debug("Exception : State busy but current request is null");
				}
			} else {
				// Set the state to busy
				reqState.setState(DataRequestState.RequestState.BUSY);
				waitCursor(true);
				if (TraceDebug.isDEBUG()) {
					TraceDebug
							.debug("Requesting data: " + trange.getStartTime()
									+ "-" + trange.getEndTime());
				}
				// no request is ongoing, proceed with request
				StateManagerFactory.getExperimentManager()
						.readExperimentTimeWindow(trange, viewID, this);

			}
		}
	}

	/**
	 * Check for pending request an either send a new request or change the
	 * state to idle
	 */
	private synchronized void requestStateUpdate() {
		// Check if a new time range update is waiting to be processed
		TmfTimeRange queuedRequest = reqState.popQueued();
		if (queuedRequest != null) {
			// Trigger the pending request
			if (TraceDebug.isDEBUG()) {
				TmfTimestamp start = queuedRequest.getStartTime();
				TmfTimestamp end = queuedRequest.getEndTime();
				TraceDebug.debug("New request about to start: " + start + "-"
						+ end);
			}

			StateManagerFactory.getExperimentManager()
					.readExperimentTimeWindow(queuedRequest, viewID, this);
		} else {
			// All requests cancelled and no more pending requests
			TraceDebug.debug("No requests pending in the queue");
			reqState.setState(RequestState.IDLE);
			waitCursor(false);
		}
	}

	/**
	 * Request the Time Analysis widget to enable or disable the wait cursor
	 * e.g. data request in progress or data request completed
	 * 
	 * @param waitInd
	 */
	protected abstract void waitCursor(boolean waitInd);

	/**
	 * View preparation to override the current local information related to the
	 * given traceId
	 * 
	 * @param traceId
	 * @param clearAllData
	 *            - reset all data e.g when a new experiment is selected
	 * @param timeRange
	 *            - new total time range e.g. Experiment level
	 */
	public abstract void ModelUpdatePrep(String traceId, boolean clearAllData,
			TmfTimeRange timeRange);

	/**
	 * Actions taken by the view to refresh its widget(s) with the updated data
	 * model
	 * 
	 * @param request
	 */
	public abstract void ModelUpdateComplete(StateDataRequest request);
}
