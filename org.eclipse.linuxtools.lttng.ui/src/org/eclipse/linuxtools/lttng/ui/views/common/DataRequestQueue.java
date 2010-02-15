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
import org.eclipse.linuxtools.lttng.state.StateDataRequest;
import org.eclipse.linuxtools.lttng.state.experiment.StateManagerFactory;
import org.eclipse.linuxtools.lttng.ui.TraceDebug;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;

/**
 * One instance to keep track of the state of data request and a single time
 * range data request pending in queue
 * 
 * @author alvaro
 * 
 */
class DataRequestQueue {

	// ========================================================================
	// Data
	// ========================================================================
	private TmfTimeRange queued = null;
	private StateDataRequest currentRequest = null;

	// ========================================================================
	// Methods
	// ========================================================================

	/**
	 * @return the data request time range in queue and reset it reference to
	 *         null to get ready for a new entry in queue
	 */
	private synchronized TmfTimeRange popQueued() {
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
	private synchronized void pushQueued(TmfTimeRange nqueued) {
		if (TraceDebug.isDEBUG()) {
			if (this.queued != null) {
				StringBuilder sb = new StringBuilder(
						"Queued request replaced from: "

						+ queued.getStartTime() + "-" + queued.getEndTime()
								+ "\n\t\t to: " + nqueued.getStartTime() + "-"
								+ nqueued.getEndTime());
				TraceDebug.debug(sb.toString());
			}
		}

		this.queued = nqueued;
	}

	private synchronized boolean processNextInQueue() {
		// initialise return value
		boolean next = false;
	
		// If currentRequest is still active, no action is taken as it needs to
		// be completed first
		if (currentRequest != null) {
			boolean reqActive = !(currentRequest.isCancelled()
					| currentRequest.isCompleted() | currentRequest.isFailed());
	
			if (reqActive) {
				TraceDebug.debug("Exception: current request is still active");
				return false;
			} else {
				currentRequest = null;
			}
		}
	
		// get next in queue
		TmfTimeRange trange = popQueued();
		if (trange != null) {
			// clean up any possible pending request
			if (TraceDebug.isDEBUG()) {
				TmfTimestamp start = trange.getStartTime();
				TmfTimestamp end = trange.getEndTime();
				TraceDebug.debug("New request about to start: " + start + "-"
						+ end);
			}
	
			// Start the new request.
			// TODO: ViewID and listener don't seem to be needed if all views
			// participate. this needs to be revisited when the request perform
			// individual requests for synthetic events and TMF supports request
			// coalescing
			currentRequest = StateManagerFactory.getExperimentManager()
					.readExperimentTimeWindow(trange, new String(""), null);
			next = true;
		} else {
			// All requests cancelled and no more pending requests
			TraceDebug.debug("No requests pending in the queue");
		}
	
		return next;
	}

	/**
	 * check if the current state is IDLE
	 * 
	 * @return
	 */
	public synchronized boolean isIdle() {
		if (currentRequest == null) {
			return true;
		}

		return false;
	}

	/**
	 * Evaluates the need to either send a new data request or queue the request
	 * till next available opportunity. One element queue to keep the latest
	 * request only.
	 * 
	 * @param trange
	 * @param viewID
	 * @param listener
	 * @return
	 */
	public synchronized boolean processDataRequest(TmfTimeRange trange,
			String viewID, IStateDataRequestListener listener) {

		// Validate input
		if (trange == null) {
			return false;
		}

		// initialise return value
		boolean requested = false;
		// cancelPendingRequests();
		// If a request is ongoing queue the new request
		if (currentRequest != null) {
			if (currentTRangeEquals(trange)) {
				// Requesting same data, no need for a new request
				return requested;
			}

			// check if the currentRequest is still active
			boolean reqActive = !(currentRequest.isCancelled()
					|| currentRequest.isFailed() || currentRequest
					.isCompleted());

			// Queue the new request and trigger cancel for the current one.
			if (reqActive) {
				pushQueued(trange);
				// make sure the request gets cancelled, request Completed is
				// expected before processing queue or next request
				currentRequest.cancel();
				return requested;
			}
		}

		if (TraceDebug.isDEBUG()) {
			TraceDebug.debug("Requesting data: " + trange.getStartTime() + "-"
					+ trange.getEndTime());
		}

		// no request is ongoing, proceed with data read of all events
		// within the specified time window
		currentRequest = StateManagerFactory.getExperimentManager()
				.readExperimentTimeWindow(
				trange, viewID, listener);

		requested = true;

		return requested;
	}

	/**
	 * Receive an update with current request.
	 * 
	 * @param request
	 */
	public synchronized void requestStarted(StateDataRequest request) {
		if (request == null) {
			return;
		}

		if (currentRequest == null) {
			TraceDebug
					.debug("Data requested started when currentRequest is null");
			return;
		}

		if (!request.equals(currentRequest)) {
			TraceDebug
					.debug("Started Data Request and current request are different");
		}
	}

	public synchronized void requestCompleted(StateDataRequest request) {
		if (currentRequest == null) {
			TraceDebug
					.debug("Data request completed when currentRequest is null");
		}

		if (request.equals(currentRequest)) {
			currentRequest = null;
			processNextInQueue();
		} else {
			TraceDebug.debug("completed request not equal to current request");
		}
	}

	/**
	 * Compare new request time range with the one related to the current
	 * request
	 * 
	 * @param trange
	 * @return
	 */
	private boolean currentTRangeEquals(TmfTimeRange trange) {
		if (currentRequest != null) {
			if (currentRequest.getRange().equals(trange)) {
				return true;
			}
		}
		return false;
	}
}
