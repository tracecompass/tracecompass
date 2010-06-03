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

package org.eclipse.linuxtools.lttng.control;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.lttng.TraceDebug;
import org.eclipse.linuxtools.lttng.event.LttngEvent;
import org.eclipse.linuxtools.lttng.event.LttngEventType;
import org.eclipse.linuxtools.lttng.event.LttngSyntheticEvent;
import org.eclipse.linuxtools.lttng.event.LttngSyntheticEvent.SequenceInd;
import org.eclipse.linuxtools.lttng.event.LttngTimestamp;
import org.eclipse.linuxtools.lttng.model.LTTngTreeNode;
import org.eclipse.linuxtools.lttng.request.LttngBaseEventRequest;
import org.eclipse.linuxtools.lttng.state.evProcessor.ITransEventProcessor;
import org.eclipse.linuxtools.lttng.state.evProcessor.state.StateEventToHandlerFactory;
import org.eclipse.linuxtools.lttng.state.model.LttngTraceState;
import org.eclipse.linuxtools.lttng.state.trace.IStateTraceManager;
import org.eclipse.linuxtools.tmf.component.TmfEventProvider;
import org.eclipse.linuxtools.tmf.event.TmfEventSource;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.trace.TmfContext;
import org.eclipse.linuxtools.tmf.trace.TmfTrace;

/**
 * @author alvaro
 * 
 */
public class LttngSyntheticEventProvider extends
		TmfEventProvider<LttngSyntheticEvent> {

	// ========================================================================
	// Data
	// ========================================================================
	public static final int BLOCK_SIZE = 1;
	public static final int NB_EVENTS = 1;
	public static final int QUEUE_SIZE = 1; // lttng specific, one event at a
											// time

	// TmfDataProvider<LttngEvent> fExtProvider = null;
	private ITmfDataRequest<LttngSyntheticEvent> fmainRequest = null;
	private final Map<IStateTraceManager, LttngBaseEventRequest> fEventProviderRequests = new HashMap<IStateTraceManager, LttngBaseEventRequest>();
	private final LttngSyntheticEvent fStatusEvent;
	private final LttngSyntheticEvent fStatusEventAck;
	private int fMainReqEventCount = 0;
	volatile boolean startIndSent = false;
	private LTTngTreeNode fExperiment = null;
	private ITransEventProcessor fstateUpdateProcessor = StateEventToHandlerFactory
			.getInstance();
	private boolean waitForRequest = false;

	// ========================================================================
	// Constructor
	// ========================================================================
	/**
	 * Accessibility to package - use factory instead of this constructor
	 * 
	 * @param type
	 */
	LttngSyntheticEventProvider(Class<LttngSyntheticEvent> type) {
		super("LttngSyntheticEventProvider", type, QUEUE_SIZE);

		// prepare empty instance status indicators and allow them to travel via
		// the framework
		TmfEventSource source = new TmfEventSource(this);
		LttngEventType dtype = new LttngEventType();
		LttngTimestamp statusTimeStamp = new LttngTimestamp(
				TmfTimestamp.Zero);

		fStatusEvent = new LttngSyntheticEvent(null, statusTimeStamp, source,
				dtype, null, null, null);
		fStatusEvent.setSequenceInd(SequenceInd.STARTREQ);

		fStatusEventAck = new LttngSyntheticEvent(null, statusTimeStamp,
				source, dtype, null, null, null);
		fStatusEventAck.setSequenceInd(SequenceInd.ACK);
	}

	// ========================================================================
	// Methods
	// ========================================================================

	@SuppressWarnings("unchecked")
	@Override
	public ITmfContext armRequest(
			final ITmfDataRequest<LttngSyntheticEvent> request) {
		// validate
		// make sure we have the right type of request
		if (!(request instanceof ITmfEventRequest<?>)) {
			request.cancel();
			TraceDebug.debug("Request is not an instance of ITmfEventRequest");
			return null;
		}

		if (fExperiment == null) {
			TraceDebug.debug("Experiment is null");
			request.cancel();
			return null;
		}

		// get ready to start processing
		reset(fExperiment);

		// At least one base provider shall be available
		if (fEventProviderRequests.size() < 1) {
			request.cancel();
			TraceDebug.debug("No Base event providers available");
			return null;
		}

		fmainRequest = request;
		// define event data handling
		ITmfEventRequest<LttngSyntheticEvent> eventRequest = (ITmfEventRequest<LttngSyntheticEvent>) fmainRequest;
		TmfTimeRange reqWindow = eventRequest.getRange();

		TraceDebug.debug("Main Synthethic event request started on thread:  " + Thread.currentThread().getName());

		// loop for every traceManager in current experiment
		boolean subRequestQueued = false;
		for (IStateTraceManager traceManager : fEventProviderRequests.keySet()) {

			// restore trace state system to nearest check point
			TmfTimestamp checkPoint = traceManager
					.restoreCheckPointByTimestamp(reqWindow.getStartTime());

			// adjust start time bound to check point

			// validate so checkpoint restore is within requested bounds
			TmfTimeRange traceRange = traceManager.getTrace().getTimeRange();
			if ((checkPoint != null) && !(
					checkPoint.getValue() >= traceRange.getStartTime().getValue() &&
					checkPoint.getValue() <= traceRange.getEndTime().getValue() && 
					checkPoint.getValue() < reqWindow.getEndTime().getValue())
					) {
				// checkpoint is out of trace bounds
				continue;
			}
			TmfTimeRange adjustedRange = reqWindow;
			if (checkPoint != null) {
				adjustedRange = new TmfTimeRange(checkPoint, reqWindow.getEndTime());
			}

			LttngTraceState traceModel = traceManager.getStateModel();
			// create sub-request for one trace within experiment
			final LttngBaseEventRequest subRequest = new LttngBaseEventRequest(
					adjustedRange, reqWindow.getStartTime(), 0,
					TmfEventRequest.ALL_DATA, BLOCK_SIZE, traceModel, ITmfDataRequest.ExecutionType.SHORT) {

				private LttngSyntheticEvent syntheticEvent = null;
				private LttngSyntheticEvent syntheticAckIndicator = null;
				long subEventCount = 0L;

				private final long fDispatchTime = getDispatchTime().getValue();
				private final LttngTraceState fTraceModel = getTraceModel();

				/*
				 * (non-Javadoc)
				 * 
				 * @see
				 * org.eclipse.linuxtools.lttng.control.LttngEventRequest#handleData
				 * ()
				 */
				@Override
				public void handleData() {
					LttngEvent[] events = getData();
					if (events.length > 0) {
						for (LttngEvent e : events) {
							handleIncomingData(e);
						}
					} else {
						TraceDebug.debug("handle data received with no data");
//						handleProviderDone(getTraceModel());
//						done();
					}
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.linuxtools.tmf.request.TmfDataRequest#done()
				 */
				@Override
				public void done() {
					// mark this sub-request as completed
					super.done();
					handleProviderDone(getTraceModel());
//					super.done();
				}
				
				/**
				 * Trigger the Analysis and sequential control of the events.
				 * 
				 * @param e
				 */
				private void handleIncomingData(LttngEvent e) {
					long eventTime = e.getTimestamp().getValue();

					// if (eventTime == 13589777932952L) {
					// // syscall entry id 78 expected
					// System.out.println("debug mark at 13589777932952L");
					// }

					// queue the new event data and an ACK
					updateSynEvent(e);

					// If time at or above requested time, update application
					try {
						if (eventTime >= fDispatchTime) {
							// Before update
							syntheticEvent.setSequenceInd(SequenceInd.BEFORE);
							queueResult(syntheticEvent);
							queueResult(syntheticAckIndicator);

							// Update state locally
							syntheticEvent.setSequenceInd(SequenceInd.UPDATE);
							fstateUpdateProcessor.process(syntheticEvent, fTraceModel);

							// After Update
							syntheticEvent.setSequenceInd(SequenceInd.AFTER);
							queueResult(syntheticEvent);
							queueResult(syntheticAckIndicator);

							// increment once per dispatch
							incrementSynEvenCount();
							subEventCount++;
						} else {
							// event time is between checkpoint adjusted time and
							// requested time i.e. application does not expect the
							// event, however the state system needs to be re-built
							// to the dispatch point
							syntheticEvent.setSequenceInd(SequenceInd.UPDATE);
							fstateUpdateProcessor.process(syntheticEvent, fTraceModel);
						}
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}

				/**
				 * Create a synthetic event from the received new reference, if
				 * the reference is the same there is no need for a new instance
				 * 
				 * if this is the first event for this request, call start
				 * handler
				 * 
				 * @param e
				 * @return
				 */
				private LttngSyntheticEvent updateSynEvent(LttngEvent e) {
					if (syntheticEvent == null
							|| syntheticEvent.getBaseEvent() != e) {
						syntheticEvent = new LttngSyntheticEvent(e);
						syntheticAckIndicator = new LttngSyntheticEvent(e);
						syntheticAckIndicator.setSequenceInd(SequenceInd.ACK);
					}

					// Trace model needed by application handlers
					syntheticEvent.setTraceModel(fTraceModel);

					// send the start request indication once per request thread
					if (!startIndSent) {
						TraceDebug.debug("Thread started: " + Thread.currentThread().getName());
						handleProviderStarted(getTraceModel());
						startIndSent = true;
					}

					return syntheticEvent;
				}
			};
						
			// preserve the associated sub request to control it e.g.
			// cancellation
			fEventProviderRequests.put(traceManager, subRequest);

			// start request
			TmfTrace<LttngEvent> provider = (TmfTrace<LttngEvent>) traceManager
					.getTrace();
			// provider.sendRequest(subRequest, ExecutionType.LONG);
			provider.sendRequest(subRequest);
			subRequestQueued = true;
		}

		// Return a dummy context, not used for relay provider
		return (subRequestQueued) ? new TmfContext() : null;
	}

	/**
	 * Notify listeners to prepare to receive data e.g. clean previous data etc.
	 */
	public void handleProviderStarted(LttngTraceState traceModel) {
		LttngSyntheticEvent startIndEvent = new LttngSyntheticEvent(
				fStatusEvent);
		startIndEvent.setSequenceInd(SequenceInd.STARTREQ);

		// Notify application
		try {
			queueResult(startIndEvent);
			queueResult(fStatusEventAck);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Notify state event processor
		fstateUpdateProcessor.process(startIndEvent, null);
	}

	/**
	 * Notify listeners, no more events for the current request will be
	 * distributed e.g. update view.
	 */
	public synchronized void handleProviderDone(LttngTraceState traceModel) {
		// TODO: The use of a thread per main request and thread per sub-request
		// requires
		// to make sure the proper main request is marked completed. So a
		// relationship of sub-requests to parent needs to be established to
		// handle completion and cancellations properly

		// Close the main request when all sub-requests are marked completed
		for (LttngBaseEventRequest subRequest : fEventProviderRequests.values()) {
			if (subRequest != null) {
				if (!subRequest.isCompleted()) {
					// Not ready to complete main request
					return;
				}
			}
		}

		// All sub-requests are marked completed so the main request can be
		// completed as well
		// Notify application,
		LttngSyntheticEvent finishEvent = new LttngSyntheticEvent(fStatusEvent);
		finishEvent.setSequenceInd(SequenceInd.ENDREQ);
		finishEvent.setTraceModel(traceModel);

		try {
			queueResult(finishEvent);
			queueResult(fStatusEventAck);
			// End the loop in the main request
			queueResult(LttngSyntheticEvent.NullEvent);
		} catch (InterruptedException e) {
			// System.out.println(getName() +
			// ":handleProviderDone() failed to queue request");
			e.printStackTrace();
		}
	}

	/**
	 * Increment the global event counter i.e. events from any sub requests
	 */
	private synchronized void incrementSynEvenCount() {
		fMainReqEventCount++;
	}

	/**
	 * @return
	 */
	public synchronized int getSynEvenCount() {
		return fMainReqEventCount;
	}

	/**
	 * Reset provider to a state ready to begin thread execution
	 * 
	 * @param experimentNode
	 */
	public synchronized void reset(LTTngTreeNode experimentNode) {

		fmainRequest = null;

		// Make sure previous request are terminated
		for (LttngBaseEventRequest tmpRequest : fEventProviderRequests.values()) {
			if (tmpRequest != null && !tmpRequest.isCompleted()) {
				tmpRequest.cancel();
			}
		}

		fEventProviderRequests.clear();
		fMainReqEventCount = 0;
		startIndSent = false;

		// set of base event providers
		if (fExperiment != null) {
			LTTngTreeNode[] traces = fExperiment.getChildren();
			for (LTTngTreeNode trace : traces) {
				IStateTraceManager traceBaseEventProvider = (IStateTraceManager) trace;
				fEventProviderRequests.put(traceBaseEventProvider, null);
			}
		}

		if (fExperiment != experimentNode) {
			updateExperimentNode(experimentNode);
		}
	}

	/**
	 * Point to a new experiment reference
	 * 
	 * @param experiment
	 */
	private synchronized void updateExperimentNode(LTTngTreeNode experiment) {
		if (experiment != null
				&& experiment.getValue() instanceof TmfExperiment<?>) {
			fExperiment = experiment;
		} else {
			TraceDebug
					.debug("Experiment received is not instance of TmfExperiment: "
							+ experiment.getClass().getName());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.linuxtools.tmf.component.TmfDataProvider#sendRequest(org.
	 * eclipse.linuxtools.tmf.request.TmfDataRequest)
	 */
	public void sendRequest(final ITmfDataRequest<LttngSyntheticEvent> request) {
		super.sendRequest(request);
		if (waitForRequest) {
			try {
				request.waitForCompletion();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return the waitForRequest
	 */
	public boolean isWaitForRequest() {
		return waitForRequest;
	}

	/**
	 * @param waitForRequest
	 *            configures the provider to wait for the request completion
	 */
	public void setWaitForRequest(boolean waitForRequest) {
		this.waitForRequest = waitForRequest;
	}

}
