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
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
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
	private DataRequestQueue reqState = UiCommonFactory.getQueue();
	private String viewID = "";

	// ========================================================================
	// Constructor
	// ========================================================================
	public AbsTimeUpdateView(String viewID) {
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
			// update queue with the id of the current request.
			reqState.requestStarted(request);
			// if there was no new request then this one is still on
			// prepare for the reception of new data

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

			// Indicate if current data needs to be cleared and if so
			// specify the new experiment time range that applies
			ModelUpdatePrep(traceId, clearData, trange);
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
			reqState.requestCompleted(request);

		}

		// Update wait cursor
		requestStateUpdate();
		// No data refresh actions for cancelled requests.
		if (request.isCancelled() || request.isFailed()) {
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
		boolean sent = reqState.processDataRequest(trange, viewID, this);

		if (sent) {
			waitCursor(true);
		}
	}

	/**
	 * Disable the wait cursor if the state is back to idle
	 */
	private synchronized void requestStateUpdate() {
		// disable the wait cursor if the state is back to idle
		if (reqState.isIdle()) {
			// no more in the queue
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
