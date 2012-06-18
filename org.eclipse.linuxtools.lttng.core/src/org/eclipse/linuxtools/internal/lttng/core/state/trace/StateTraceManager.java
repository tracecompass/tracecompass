/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alvaro Sanchez-Leon (alvsan09@gmail.com) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.core.state.trace;

import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import org.eclipse.linuxtools.internal.lttng.core.Activator;
import org.eclipse.linuxtools.internal.lttng.core.TraceDebug;
import org.eclipse.linuxtools.internal.lttng.core.event.LttngSyntheticEvent;
import org.eclipse.linuxtools.internal.lttng.core.event.LttngSyntheticEvent.SequenceInd;
import org.eclipse.linuxtools.internal.lttng.core.event.LttngTimestamp;
import org.eclipse.linuxtools.internal.lttng.core.model.LTTngTreeNode;
import org.eclipse.linuxtools.internal.lttng.core.request.ILttngSyntEventRequest;
import org.eclipse.linuxtools.internal.lttng.core.request.IRequestStatusListener;
import org.eclipse.linuxtools.internal.lttng.core.request.LttngSyntEventRequest;
import org.eclipse.linuxtools.internal.lttng.core.state.LttngStateException;
import org.eclipse.linuxtools.internal.lttng.core.state.evProcessor.ITransEventProcessor;
import org.eclipse.linuxtools.internal.lttng.core.state.evProcessor.state.StateEventToHandlerFactory;
import org.eclipse.linuxtools.internal.lttng.core.state.model.LttngTraceState;
import org.eclipse.linuxtools.internal.lttng.core.state.model.StateModelFactory;
import org.eclipse.linuxtools.internal.lttng.core.state.resource.ILttngStateContext;
import org.eclipse.linuxtools.internal.lttng.core.trace.LTTngTextTrace;
import org.eclipse.linuxtools.internal.lttng.core.trace.LTTngTrace;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfCheckpoint;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.core.trace.TmfLocation;

public class StateTraceManager extends LTTngTreeNode implements
        IStateTraceManager, ILttngStateContext {

	// constants
	private static final long DEFAULT_OFFSET = 0L;
	private static final int DEFAULT_CHUNK = 1;

	// configurable check point interval
	private static final long LTTNG_CHECK_POINT_INTERVAL = 50000L;
	private long fcheckPointInterval = LTTNG_CHECK_POINT_INTERVAL;

	private TmfExperiment fExperiment = null;

	// immutable Objects
	private final ITmfTrace fTrace;
	private int fcpuNumber = -1;
	private final ITransEventProcessor fStateUpdateProcessor;

	// potentially thread shared
	private final HashMap<Long, LttngTraceState> stateCheckpointsList = new HashMap<Long, LttngTraceState>();
	private final Vector<TmfCheckpoint> timestampCheckpointsList = new Vector<TmfCheckpoint>();
	private LttngTraceState fStateModel;
	private LttngTraceState fCheckPointStateModel;

	// locks
	private final Object fCheckPointsLock = new Object();
	private final Object fStateModelLock = new Object();



	// =======================================================================
	// Constructor
	// =======================================================================
	/**
	 * @param id
	 * @param parent
	 * @param name
	 * @param trace
	 * @throws LttngStateException
	 */
	public StateTraceManager(Long id, LTTngTreeNode parent, String name, ITmfTrace trace) throws LttngStateException {
		super(id, parent, name, trace);

		if (trace == null) {
			throw new LttngStateException("No TmfTrace object available!"); //$NON-NLS-1$
		}

		fTrace = trace;
		fStateUpdateProcessor = StateEventToHandlerFactory.getInstance();

		init();

		fStateModel = StateModelFactory.getStateEntryInstance(this);
		fStateModel.init(this);

		fCheckPointStateModel = StateModelFactory.getStateEntryInstance(this);
		fCheckPointStateModel.init(this);
	}

	// =======================================================================
	// Methods
	// =======================================================================
	@SuppressWarnings("unchecked")
	private void init() {
		// resolve the experiment
		Object obj = getParent().getValue();
		if (obj != null && obj instanceof TmfExperiment) {
			fExperiment = (TmfExperiment) obj;
		}

		// initialize the number of cpus
		if (fTrace instanceof LTTngTrace) {
			fcpuNumber = ((LTTngTrace) fTrace).getCpuNumber();
		} else if (fTrace instanceof LTTngTextTrace) {
			fcpuNumber = ((LTTngTextTrace) fTrace).getCpuNumber();
		}
	}





	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.linuxtools.lttng.state.IStateManager#getEventLog()
	 */
	@Override
	public ITmfTrace getStateTrace() {
		return fTrace;
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
	private void saveCheckPointIfNeeded(Long eventCounter, ITmfTimestamp eventTime) {
		// Save a checkpoint every LTTNG_STATE_SAVE_INTERVAL event
		if ((eventCounter.longValue() % fcheckPointInterval) == 0) {

			LttngTraceState stateCheckPoint;
			synchronized (fCheckPointsLock) {
				stateCheckPoint = fCheckPointStateModel.clone();
			}

			TraceDebug.debug("Check point created here: " + eventCounter //$NON-NLS-1$
					+ " -> " + eventTime.toString() + "************" //$NON-NLS-1$ //$NON-NLS-2$
					+ getStateTrace().getName() + "   >>>>> Thread: " //$NON-NLS-1$
					+ Thread.currentThread().getId());

			synchronized (fCheckPointsLock) {
				// Save the checkpoint
				stateCheckpointsList.put(eventCounter, stateCheckPoint);
				// Save correlation between timestamp and checkpoint index

				timestampCheckpointsList.add(new TmfCheckpoint(new TmfTimestamp(eventTime), new TmfContext(new TmfLocation<Long>(eventCounter), eventCounter)));
			}
		}
	}

	/**
	 * @return the lttng_check_point_interval
	 */
	public long getCheckPointInterval() {
		return fcheckPointInterval;
	}

	/**
	 * @param check_point_interval
	 *            , the lttng_check_point_interval to set
	 */
	public void setCheckPointInterval(long check_point_interval) {
		this.fcheckPointInterval = check_point_interval;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.linuxtools.lttng.state.trace.IStateTraceManager#
	 * restoreCheckPointByTimestamp
	 * (org.eclipse.linuxtools.tmf.event.TmfTimestamp)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public TmfCheckpoint restoreCheckPointByTimestamp(ITmfTimestamp eventTime) {
		TmfTimeRange experimentRange = fExperiment.getTimeRange();
		TmfCheckpoint checkpoint = new TmfCheckpoint(fTrace.getStartTime(), new TmfContext(new TmfLocation<Long>(0L), 0));

		// The GUI can have time limits higher than this log, since GUI can
		// handle multiple logs. Ignore special null value of experiment time range.
        if ((eventTime.getValue() < 0) ||
                (!experimentRange.equals(TmfTimeRange.NULL_RANGE) && (eventTime.getValue() > experimentRange.getEndTime().getValue()))) {
			return null;
		}

		// The GUI can have time limits lower than this trace, since experiment
		// can handle multiple traces
		if ((eventTime.getValue() < fTrace.getStartTime().getValue())) {
			eventTime = fTrace.getStartTime();
		}

	    LttngTraceState traceState;
		synchronized (fCheckPointsLock) {
		    Collections.sort(timestampCheckpointsList);
		    // Initiate the compare with a checkpoint containing the target time
		    // stamp to find
		    int index = Collections.binarySearch(timestampCheckpointsList, new TmfCheckpoint(eventTime, new TmfContext(new TmfLocation<Long>(0L), 0)));
		    // adjust index to round down to earlier checkpoint when exact match
		    // not
		    // found
		    index = getPrevIndex(index);

		    if (index == 0) {
		        // No checkpoint restore is needed, start with a brand new
		        // TraceState
		        traceState = StateModelFactory.getStateEntryInstance(this);
		    } else {

		        // Useful CheckPoint found
		        checkpoint = timestampCheckpointsList.get(index);
		        // get the location associated with the checkpoint
		        TmfLocation<Long> location = (TmfLocation<Long>) checkpoint.getLocation();
		        // reference a new copy of the checkpoint template
		        traceState = stateCheckpointsList.get(location.getLocation()).clone();
		    }

		}

		// Restore the stored traceState
		synchronized (fStateModelLock) {
			fStateModel = traceState;
		}

		return checkpoint;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.lttng.state.trace.IStateTraceManager#restoreCheckPointByIndex(long)
	 */
	@Override
	public TmfCheckpoint restoreCheckPointByIndex(long eventIndex) {
		TmfCheckpoint checkpoint = new TmfCheckpoint(fTrace.getStartTime(), new TmfContext(new TmfLocation<Long>(0L), 0));

	    LttngTraceState traceState;
		synchronized (fCheckPointsLock) {
		    Collections.sort(timestampCheckpointsList);
		    // Initiate the compare with a checkpoint containing the target time
		    // stamp to find
		    int index = Collections.binarySearch(timestampCheckpointsList, new TmfCheckpoint(null, new TmfContext(new TmfLocation<Long>(eventIndex), eventIndex)));
		    // adjust index to round down to earlier checkpoint when exact match not found
		    index = getPrevIndex(index);

		    if (index == 0) {
		        // No checkpoint restore is needed, start with a brand new
		        // TraceState
		        traceState = StateModelFactory.getStateEntryInstance(this);
		    } else {

		        // Useful CheckPoint found
		        checkpoint = timestampCheckpointsList.get(index);
		        // get the location associated with the checkpoint
		        @SuppressWarnings("unchecked")
				TmfLocation<Long> location = (TmfLocation<Long>) checkpoint.getLocation();
		        // reference a new copy of the checkpoint template
		        traceState = stateCheckpointsList.get(location.getLocation()).clone();
		    }

		}

		// Restore the stored traceState
		synchronized (fStateModelLock) {
			fStateModel = traceState;
		}

		return checkpoint;
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

	// TODO: Remove this request type when the UI handle their own requests
	/**
	 * Request Event data of a specified time range
	 *
	 * @param timeWindow
	 * @param listener
	 * @param processor
	 * @return ILttngEventRequest The request made
	 */
	ILttngSyntEventRequest getDataRequestByTimeRange(TmfTimeRange timeWindow, IRequestStatusListener listener,
			final ITransEventProcessor processor) {

		ILttngSyntEventRequest request = new StateTraceManagerRequest(timeWindow, DEFAULT_OFFSET,
				TmfDataRequest.ALL_DATA, DEFAULT_CHUNK, listener, getExperimentTimeWindow(), processor) {
		};

		return request;
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.linuxtools.lttng.state.trace.IStateTraceManager#getStateModel
	 * ()
	 */
	@Override
    public LttngTraceState getStateModel() {
	    LttngTraceState stateModel = null;
		synchronized (fStateModelLock) {
			stateModel = fStateModel;
		}
		return stateModel;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.linuxtools.lttng.state.trace.IStateTraceManager#
	 * getCheckPointStateModel()
	 */
	@Override
    public LttngTraceState getCheckPointStateModel() {
	    LttngTraceState checkPointStateModel = null;
		synchronized (fCheckPointsLock) {
		    checkPointStateModel = fCheckPointStateModel;
		}
		return checkPointStateModel;
	}

	/**
	 * @return the stateCheckpointsList
	 */
	HashMap<Long, LttngTraceState> getStateCheckpointsList() {
		return stateCheckpointsList;
	}

	/**
	 * @return the timestampCheckpointsList
	 */
	Vector<TmfCheckpoint> getTimestampCheckpointsList() {
		return timestampCheckpointsList;
	}
	// =======================================================================
	// Inner Class
	// =======================================================================
	class StateTraceManagerRequest extends LttngSyntEventRequest {
		// =======================================================================
		// Data
		// =======================================================================
		final TmfEvent[] evt = new TmfEvent[1];
		final ITransEventProcessor fprocessor;
		LttngSyntheticEvent synEvent;
		Long fCount = getSynEventCount();

		// =======================================================================
		// Constructor
		// =======================================================================
		public StateTraceManagerRequest(TmfTimeRange range, long offset, int nbEvents, int maxBlockSize,
				IRequestStatusListener listener, TmfTimeRange experimentTimeRange, ITransEventProcessor processor) {

			super(range, offset, nbEvents, maxBlockSize, listener, experimentTimeRange, processor);
			fprocessor = processor;
			TraceDebug.debug("Instance created for range: " + range.toString()); //$NON-NLS-1$
			fCount = 0L;
		}

		// =======================================================================
		// Methods
		// =======================================================================
		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * org.eclipse.linuxtools.lttng.request.LttngSyntEventRequest#handleData
		 * ()
		 */
		@Override
		public void handleData(ITmfEvent event) {
			super.handleData(event);
			if (event != null) {
				synEvent = (LttngSyntheticEvent) event;
				if (synEvent.getSynType() == SequenceInd.AFTER) {
					// Note : We call this function before incrementing
					// eventCount to save a default check point at the "0th"
					// event
					saveCheckPoint(fCount, synEvent.getTimestamp());
					fCount++;

					if (TraceDebug.isDEBUG()) {
						if (fCount % 1000 == 0) {
							TraceDebug.debug("handled: " + fCount + " sequence: " + synEvent.getSynType()); //$NON-NLS-1$ //$NON-NLS-2$
						}
					}
				}
			}
		}

		/**
		 * To be overridden by active save e.g. check points, this no action
		 * default is used for requests which do not require rebuilding of
		 * checkpoints e.g. requiring data of a new time range selection
		 *
		 * @param count
		 * @param time
		 */
		public void saveCheckPoint(Long count, ITmfTimestamp time) {

		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.linuxtools.lttng.state.resource.ILttngStateContext#
	 * getNumberOfCpus()
	 */
	@Override
	public int getNumberOfCpus() {
		return fcpuNumber;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.linuxtools.lttng.state.resource.ILttngStateContext#
	 * getTraceTimeWindow()
	 */
	@Override
	public TmfTimeRange getTraceTimeWindow() {
		if (fTrace != null) {
			return fTrace.getTimeRange();

		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.linuxtools.lttng.state.resource.ILttngStateContext#getTraceId
	 * ()
	 */
	@Override
	public String getTraceId() {
		if (fTrace != null) {
			return fTrace.getName();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.linuxtools.lttng.state.trace.IStateTraceManager#
	 * getExperimentTimeWindow()
	 */
	@Override
	public TmfTimeRange getExperimentTimeWindow() {
		if (fExperiment != null) {
			return fExperiment.getTimeRange();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.linuxtools.lttng.state.trace.IStateTraceManager#getExperimentName
	 * ()
	 */
	@Override
	public String getExperimentName() {
		return fExperiment.getName();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.linuxtools.lttng.state.resource.ILttngStateContext#getTraceIdRef
	 * ()
	 */
	@Override
	public ITmfTrace getTraceIdRef() {
		return fTrace;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.linuxtools.lttng.state.trace.IStateTraceManager#clearCheckPoints
	 * ()
	 */
	@Override
    public void clearCheckPoints() {
		synchronized (fCheckPointsLock) {
			stateCheckpointsList.clear();
			timestampCheckpointsList.clear();

			fCheckPointStateModel = StateModelFactory.getStateEntryInstance(this);

			try {
				fCheckPointStateModel.init(this);
			} catch (LttngStateException e) {
			    Activator.getDefault().logError("Unexpected Error", e);  //$NON-NLS-1$
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.linuxtools.lttng.state.trace.IStateTraceManager#handleEvent
	 * (org.eclipse.linuxtools.lttng.event.LttngSyntheticEvent, java.lang.Long)
	 */
	@Override
	public void handleEvent(LttngSyntheticEvent synEvent, Long eventCount) {
		fStateUpdateProcessor.process(synEvent, fCheckPointStateModel);

		// Save checkpoint as needed
		saveCheckPointIfNeeded(eventCount - 1, synEvent.getTimestamp());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
    @SuppressWarnings("nls")
	public String toString() {
		StringBuilder sb = new StringBuilder(super.toString());
		sb.append("\n\tTotal number of processes in the Shared State model: " + fStateModel.getProcesses().length
				+ "\n\t" + "Total number of processes in the Check point State model: "
				+ fCheckPointStateModel.getProcesses().length);

		TmfTimeRange traceTRange = fTrace.getTimeRange();
		sb.append("\n\tTrace time interval for trace " + fTrace.getName() + "\n\t"
				+ new LttngTimestamp(traceTRange.getStartTime()));
		sb.append(" - " + new LttngTimestamp(traceTRange.getEndTime()));
		sb.append("\n\tCheckPoints available at: ");
		for (TmfCheckpoint cpoint : timestampCheckpointsList) {
			sb.append("\n\t" + "Location: " + cpoint.getLocation() + " - " + cpoint.getTimestamp());
		}

		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.lttng.state.resource.ILttngStateContext#getIdentifier()
	 */
	@Override
	public long getIdentifier() {
	    return getId().longValue();
	}

}
