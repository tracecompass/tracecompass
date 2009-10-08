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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Set;
import java.util.Vector;

import org.eclipse.linuxtools.lttng.TraceDebug;
import org.eclipse.linuxtools.lttng.event.LttngTimestamp;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.eclipse.linuxtools.lttng.state.evProcessor.AbsEventProcessorFactory;
import org.eclipse.linuxtools.lttng.state.evProcessor.EventProcessorProxy;
import org.eclipse.linuxtools.lttng.state.evProcessor.IEventProcessing;
import org.eclipse.linuxtools.lttng.state.experiment.StateManagerFactory;
import org.eclipse.linuxtools.lttng.state.model.ILttngStateInputRef;
import org.eclipse.linuxtools.lttng.state.model.LttngProcessState;
import org.eclipse.linuxtools.lttng.state.model.LttngTraceState;
import org.eclipse.linuxtools.lttng.state.model.StateModelFactory;
import org.eclipse.linuxtools.lttng.trace.LTTngTrace;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.trace.TmfTrace;
import org.eclipse.linuxtools.tmf.trace.TmfTraceCheckpoint;

/**
 * 
 * @author alvaro
 * 
 */
public class StateManager extends Observable {

	private static final long LTTNG_STATE_SAVE_INTERVAL = 50000L;

	// These are used in the building of the data request.
	private static final long DEFAULT_OFFSET = 0L;
	private static final int DEFAULT_CHUNK = 1;

	// ========================================================================
	// Data
	// =======================================================================
	private TmfExperiment fExperiment = null;
	private LTTngTrace fEventLog = null;
	private StateStacksHandler stateIn = null;
	private JniTrace trace = null;
	private Long eventCount = 0L;

	private HashMap<Long, LttngTraceState> stateCheckpointsList = new HashMap<Long, LttngTraceState>();
	private Vector<TmfTraceCheckpoint> timestampCheckpointsList = new Vector<TmfTraceCheckpoint>();

	// ========================================================================
	// Constructor
	// =======================================================================

	// /**
	// * Default constructor
	// * <p>
	// * Instanciate its own StateStacksHandler.
	// *
	// */
	// public StateManager() {
	// this.stateIn = new StateStacksHandler();
	// }

	/**
	 * Constructor with parameter
	 * <p>
	 * 
	 * @param stateInputHandler
	 *            A valid StateStacksHandler
	 * 
	 */
	public StateManager(StateStacksHandler stateInputHandler) {
		this.stateIn = stateInputHandler;
	}

	// /**
	// * Copy constructor
	// * <p>
	// *
	// * @param oldStateManager
	// * the StateManager we want to copy
	// *
	// */
	// public StateManager(StateManager oldStateManager) {
	// fEventLog = oldStateManager.fEventLog;
	// stateIn = oldStateManager.stateIn;
	// trace = oldStateManager.trace;
	// eventCount = oldStateManager.eventCount;
	//
	// stateCheckpointsList = oldStateManager.stateCheckpointsList;
	// timestampCheckpointsList = oldStateManager.timestampCheckpointsList;
	// }

	// ========================================================================
	// Methods
	// =======================================================================
	public void setTraceSelection(TmfExperiment experiment) {
		// New log in use, read all events and build state transition stack
		if (experiment != null) {
			if (fExperiment != null && fExperiment != experiment) {
				this.fExperiment.dispose();
			}

			this.fExperiment = experiment;

			// if (fEventLog != null) {
			// this.fEventLog.dispose();
			// }

			this.fEventLog = (LTTngTrace) experiment.getTraces()[0];
			trace = fEventLog.getCurrentJniTrace();
			try {
				stateIn.init(trace, fEventLog);
			} catch (LttngStateException e) {
				e.printStackTrace();
			}

			// Restart count and collections
			eventCount = 0L;
			stateCheckpointsList.clear();
			timestampCheckpointsList.clear();

			// Obtain a dataRequest to pass to the processRequest function
			TmfTimeRange allTraceWindow = fEventLog.getTimeRange();
			StateDataRequest request = getDataRequestStateSave(allTraceWindow,
					null);

			// Wait for completion
			request.startRequestInd(fExperiment, true, true);

			if (TraceDebug.isDEBUG()) {
				List<LttngProcessState> processes = stateIn
						.getTraceStateModel().getProcesses();
				StringBuilder sb = new StringBuilder(
						"Total number of processes in the State provider: "
								+ processes.size());

				TmfTimeRange logTimes = fEventLog.getTimeRange();
				sb.append("\n\tLog file times "
						+ new LttngTimestamp(logTimes.getStartTime()));
				sb.append(" - " + new LttngTimestamp(logTimes.getEndTime()));

				sb.append("\n\tCheckPoints available at: ");
				for (TmfTraceCheckpoint cpoint : timestampCheckpointsList) {
					sb.append("\n\t" + cpoint.getTimestamp());
				}
				TraceDebug.debug(sb.toString());
			}
		}

	}

	/**
	 * TODO: Not ready for threading
	 * <p>
	 * Read events within specific time window
	 * </p>
	 * 
	 * @param trange
	 * @param obs
	 * @param transactionID
	 */
	public void executeDataRequest(TmfTimeRange trange, String transactionID,
			IStateDataRequestListener listener) {
		TmfTimestamp restoredStartTime = restoreCheckPointByTimestamp(trange
				.getStartTime());
		// Adjust the time range to consider rewinding to the start time
		trange = new TmfTimeRange(restoredStartTime, trange.getEndTime());
		// Get a data request for the time range we want (nearest checkpoint
		// to timestamp wanted)
		eventCount = 0L;

		// Process request to that point
		StateDataRequest request = getDataRequestByTimeRange(trange, listener);
		// don't wait for completion i.e. allow cancellations
		request.startRequestInd(fExperiment, false, false);

		if (TraceDebug.isDEBUG()) {
			List<LttngProcessState> processes = stateIn.getTraceStateModel()
					.getProcesses();
			TraceDebug
					.debug(" Time Window requested, (start adjusted to checkpoint): "
							+ trange.getStartTime()
					+ "-" + trange.getEndTime()
					+ " Total number of processes in the State provider: "
					+ processes.size() + " Completed");
		}
	}

	/**
	 * Current value of event counter
	 * 
	 * @return Long The number of events, if it is known
	 */
	public Long getEventCount() {
		return eventCount;
	}

	/**
	 * used to obtain details on the log associated with this manager e.g.
	 * logid.
	 * 
	 * @return
	 */
	public TmfTrace getEventLog() {
		return fEventLog;
	}

	/**
	 * Used for troubleshooting when debug mode is on
	 * 
	 * @return Set<String> Set of event that were not handled
	 */
	public Set<String> getEventsNotHandled() {
		return stateIn.getEventsNotHandled();
	}

	/**
	 * Needed for verification purposes
	 * 
	 * @param listener
	 *            The IEventProcessing we want to register
	 */
	void registerListener(IEventProcessing listener) {
		stateIn.registerListener(listener);
	}

	/**
	 * Needed for verification purposes
	 * 
	 * @param listener
	 *            The IEventProcessing we want to unregister
	 */
	void deregisterListener(IEventProcessing listener) {
		stateIn.deregisterListener(listener);
	}

	/**
	 * Save a checkpoint if it is needed at that point
	 * <p>
	 * The function will use "eventCount" internally to determine if a save was
	 * needed
	 * 
	 * @param eventCounter
	 *            The event "count" or event "id" so far
	 * @param eventTime
	 *            The timestamp of this event
	 * 
	 * @return boolean True if a checkpoint was saved, false otherwise
	 */
	private boolean saveCheckPointIfNeeded(Long eventCounter,
			TmfTimestamp eventTime) {
		boolean saveHappened = false;
		// Crate new location to store checkpoint reference
		Long location = new Long(eventCounter.longValue());
		// Save a checkpoint every LTTNG_STATE_SAVE_INTERVAL event
		if ((location % LTTNG_STATE_SAVE_INTERVAL) == 0) {
			// Save the checkpoint
			stateCheckpointsList.put(location, stateIn.traceStateModel.clone());
			// Save correlation between timestamp and checkpoint index

			timestampCheckpointsList.add(new TmfTraceCheckpoint(eventTime,
					location));

			saveHappened = true;
		}

		return saveHappened;
	}

	/**
	 * Restore to the closest checkpoint from TmfTimestamp
	 * <p>
	 * Note : it is heavier to restore by timestamp than by event position,
	 * restore by event position whichever possible.
	 * 
	 * @param eventTime
	 *            The timestamp of the event to restore to
	 * 
	 * @return TmfTimestamp indicates the nearest time used to restore the
	 *         state, null sent if input is invalid
	 */
	public TmfTimestamp restoreCheckPointByTimestamp(TmfTimestamp eventTime) {
		TmfTimeRange logRange = fExperiment.getTimeRange();
		TmfTimestamp nearestTimeStamp = logRange.getStartTime();

		// The GUI can have time limits higher than this log, since GUI can
		// handle multiple logs
		if ((eventTime.getValue() < 0)
				|| (eventTime.getValue() > logRange.getEndTime().getValue())) {
			return null;
		}

		// The GUI can have time limits lower than this log, since GUI can
		// handle multiple logs
		if ((eventTime.getValue() < logRange.getStartTime().getValue())) {
			eventTime = logRange.getStartTime();
		}

		// Sort the checkpoints, required before the binary search
		Collections.sort(timestampCheckpointsList);
		// Initiate the compare with a checkpoint containing the target time
		// stamp to find
		int index = Collections.binarySearch(timestampCheckpointsList,
				new TmfTraceCheckpoint(eventTime, 0));
		// adjust index to round down to earlier checkpoint when exact match not
		// found
		index = getPrevIndex(index);

		LttngTraceState traceState;
		Long location = 0L;
		if (index == 0) {
			// No checkpoint restore is needed, start with a brand new
			// TraceState
			ILttngStateInputRef inputDataRef = new LttngStateInputRef(trace,
					fEventLog);
			traceState = StateModelFactory.getStateEntryInstance(inputDataRef);
		} else {
			// Useful CheckPoint found
			TmfTraceCheckpoint checkpoint = timestampCheckpointsList.get(index);
			nearestTimeStamp = checkpoint.getTimestamp();
			// get the location associated with the checkpoint
			location = (Long) (checkpoint.getLocation());
			// reference a new copy of the checkpoint template
			traceState = stateCheckpointsList.get(location).clone();
		}

		// Make sure eventCount stay consistent!
		eventCount = new Long(location);

		// Restore the stored traceState
		stateIn.setTraceStateModel(traceState);

		return nearestTimeStamp;
	}

	/**
	 * Adjust the result from a binary search to the round down position
	 * 
	 * @param position
	 *            if Negative is: (-(insertion point) -1)
	 * @return position or if no match found, earlier than insertion point
	 */
	private int getPrevIndex(int position) {
		int roundDownPosition = position;
		if (position < 0) {
			roundDownPosition = -(position + 2);
		}

		roundDownPosition = roundDownPosition < 0 ? 0 : roundDownPosition;
		return roundDownPosition;
	}

	// /**
	// * Restore to the closest checkpoint from position
	// * <p>
	// *
	// * @param position
	// * The position of the event to restore to
	// *
	// * @return boolean True if a checkpoint was restored, false otherwise
	// */
	// private boolean restoreCheckPointByPosition(long position) {
	// long nearestCheckPoint = (position - (position %
	// LTTNG_STATE_SAVE_INTERVAL));
	//
	// // Some sanity check :
	// // Not under 0
	// // Not over eventCount
	// // A checkpoint exist
	// if ((nearestCheckPoint < 0) || (nearestCheckPoint > eventCount)
	// || (stateCheckpointsList.get(nearestCheckPoint) == null)) {
	// return false;
	// } else {
	// // Restore the stored traceState
	// stateIn.setTraceStateModel(stateCheckpointsList
	// .get(nearestCheckPoint));
	//
	// // Make sure eventCount stay consistent!
	// eventCount = new Long(nearestCheckPoint);
	//
	// // * Rewind to the correct position
	// // To do so, we need a request to the correct window
	// // We will seek to nearestCheckPoint and read next events until
	// // position
	// TmfDataRequest<TmfEvent> request = getDataRequestByPosition(
	// (int) nearestCheckPoint, (int) position);
	//
	// // Process request to that point
	// fExperiment.processRequest(request, true);
	//
	// return true;
	// }
	// }

	/**
	 * Get a Tmf data request for the current eventlog
	 * <p>
	 * 
	 * @param TmfTimeRange
	 *            The time range we want events from.
	 * 
	 * @return TmfDataRequest<TmfEvent> The request made
	 */
	StateDataRequest getDataRequestByTimeRange(TmfTimeRange timeWindow,
			IStateDataRequestListener listener) {

		final TmfEvent[] evt = new TmfEvent[1];

		// ***TODO***
		// The override of handlePartialResult is similar to the one in
		// getDataRequestByPosition()
		// ***

		// Create the new request and override the handlePartialResult function
		StateDataRequest request = new StateDataRequest(timeWindow,
				DEFAULT_OFFSET, TmfDataRequest.ALL_EVENTS, DEFAULT_CHUNK,
				listener, this) {
			@Override
			public void handleData() {
				TmfEvent[] result = getData();

				evt[0] = (result.length > 0) ? result[0] : null;
				// Dispatch information for Event processing
				stateIn.processEvent(evt[0]);

				// increment internal and external number of events
				setNumOfEvents(getNumOfEvents() + 1);
				eventCount++;
			}

			@Override
			public void handleCompleted() {
				if (isCancelled() || isFailed()) {
					// No notification to end request handlers
				} else {
					// notify the associated end request handlers
					requestCompleted();
				}

				// notify listeners
				notifyCompletion();
			}
		};

		return request;
	}

	private StateDataRequest getDataRequestStateSave(TmfTimeRange timeWindow,
			IStateDataRequestListener requestListener) {

		final TmfEvent[] evt = new TmfEvent[1];

		// ***TODO***
		// The override of handlePartialResult is similar to the one in
		// getDataRequestByPosition()
		// ***

		// Create the new request and override the handlePartialResult function
		StateDataRequest request = new StateDataRequest(timeWindow,
				DEFAULT_OFFSET, TmfDataRequest.ALL_EVENTS, DEFAULT_CHUNK,
				requestListener, this) {

			@Override
			public void handleData() {
				TmfEvent[] result = getData();

				evt[0] = (result.length > 0) ? result[0] : null;
				// Dispatch information for Event processing
				stateIn.processEvent(evt[0]);

				// Call the function that will save a checkpoint if needed at
				// that point
				// Note : We call this function before incrementing eventCount
				// to avoid skipping the "0th" event
				if (evt[0] != null) {
					saveCheckPointIfNeeded(getNumOfEvents(), evt[0]
							.getTimestamp());
				}

				// increment internal and external counters
				setNumOfEvents(getNumOfEvents() + 1);
				eventCount++;
			}

			@Override
			public void handleCompleted() {
				if (isCancelled() || isFailed()) {
					// No notification to end request handlers
				} else {
					// notify the associated end request handlers
					requestCompleted();
				}

				// notify listeners
				notifyCompletion();
				TraceDebug.debug("number of events processed on file opening"
						+ getNumOfEvents());
			}
		};

		return request;
	}

	// /**
	// * Get a Tmf data request for the current eventlog
	// * <p>
	// *
	// * @param startPosition
	// * The position to start the get request from
	// * @param endPosition
	// * The position to ed the get request at
	// *
	// * @return TmfDataRequest<TmfEvent> The request made
	// */
	// private TmfDataRequest<TmfEvent> getDataRequestByPosition(
	// long startPosition, long endPosition) {
	// final TmfEvent[] evt = new TmfEvent[1];
	//
	// // ***FIXME***
	// // The override of handlePartialResult is exactly the same as the one in
	// // getDataRequestByPosition()
	// // However, there is no way to override it in only one place to avoid
	// // code duplication!
	// // ***
	//
	// // Create the new request and override the handlePartialResult function
	// TmfDataRequest<TmfEvent> request = new TmfDataRequest<TmfEvent>(
	// (int) startPosition, DEFAULT_OFFSET,
	// (int) (endPosition - startPosition), DEFAULT_CHUNK) {
	// //@Override
	// public void handlePartialResult() {
	// TmfEvent[] result = getData();
	//
	// evt[0] = (result.length > 0) ? result[0] : null;
	// // Dispatch information for Event processing
	// stateIn.processEvent(evt[0]);
	// }
	// };
	//
	// return request;
	// }

	/**
	 * @return
	 */
	public TmfTimeRange getExperimentTimeWindow() {
		if (fExperiment != null) {
			return fExperiment.getTimeRange();
		}
		return null;
	}

	/**
	 * This method has to be called once all events of the associated Trace have
	 * been processed, this method then triggers the drawing of the final state
	 * which is necessary e.g when zooming.
	 */
	public synchronized void requestCompleted() {
		Set<AbsEventProcessorFactory> handlerRegister = EventProcessorProxy
				.getInstance().getProcessingFactories();
		// Notify the FINISH handlers
		for (Iterator<AbsEventProcessorFactory> iterator = handlerRegister
				.iterator(); iterator.hasNext();) {
			AbsEventProcessorFactory handlerRegistry = (AbsEventProcessorFactory) iterator
					.next();
			IEventProcessing handler = handlerRegistry.getfinishProcessor();
			if (handler != null) {
				// process State Update
				handler.process(null, stateIn.traceStateModel);
			}
		}
	}

	// *** MAIN : For testing only ***
	public static void main(String[] args) {

		// Timestamp for the "197500th" events
		long timefor197500 = 953098902827L;

		// A new StateManager
		StateManager stateManagerTest = StateManagerFactory.getManager("test");

		LTTngTrace[] testStream = new LTTngTrace[1];
		try {
			// The stream is needed by the eventLog, which is needed by the
			// StateManager
			// testStream[0] = new LttngEventStream("/home/william/trace1",
			// true);
			testStream[0] = new LTTngTrace(
					"/home/william/runtime-EclipseApplication/TEST_JOIE/Traces/trace3",
					true);

			TmfExperiment newExpt = new TmfExperiment("trace1", testStream);

			// This will create all the checkpoint
			stateManagerTest.setTraceSelection(newExpt);
			System.out.println("JOIE JOIE FIN DE LA CREATION DES CHECKPOINTS");

			// *** Restore some checkpoint to test

			// Test the restoration from position
			// stateManagerTest.restoreCheckPointByPosition(197500);

			if (testStream[0].getCurrentEvent().getTimestamp().getValue() == timefor197500) {
				System.out.println("Successfully restored by Position!");
			} else {
				System.out.println("FAILED : "
						+ testStream[0].getCurrentEvent().getTimestamp()
								.getValue() + " != " + timefor197500);
			}

			// Test the restoration from Timestamp
			TmfTimestamp newTimestamp = new TmfTimestamp(timefor197500,
					(byte) -9);
			stateManagerTest.restoreCheckPointByTimestamp(newTimestamp);
			// test the timestamp
			if (testStream[0].getCurrentEvent().getTimestamp().getValue() == timefor197500) {
				System.out.println("Successfully restored by Timestamp!");
			} else {
				System.out.println("FAILED : "
						+ testStream[0].getCurrentEvent().getTimestamp()
								.getValue() + " != " + timefor197500);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
