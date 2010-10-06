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
package org.eclipse.linuxtools.lttng.ui.views.common;

import java.util.Arrays;

import org.eclipse.linuxtools.lttng.control.LttngCoreProviderFactory;
import org.eclipse.linuxtools.lttng.control.LttngSyntheticEventProvider;
import org.eclipse.linuxtools.lttng.event.LttngSyntheticEvent;
import org.eclipse.linuxtools.lttng.event.LttngSyntheticEvent.SequenceInd;
import org.eclipse.linuxtools.lttng.event.LttngTimestamp;
import org.eclipse.linuxtools.lttng.request.ILttngSyntEventRequest;
import org.eclipse.linuxtools.lttng.request.IRequestStatusListener;
import org.eclipse.linuxtools.lttng.request.LttngSyntEventRequest;
import org.eclipse.linuxtools.lttng.request.RequestCompletedSignal;
import org.eclipse.linuxtools.lttng.request.RequestStartedSignal;
import org.eclipse.linuxtools.lttng.state.evProcessor.ITransEventProcessor;
import org.eclipse.linuxtools.lttng.ui.TraceDebug;
import org.eclipse.linuxtools.lttng.ui.model.trange.ItemContainer;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.request.ITmfDataRequest.ExecutionType;
import org.eclipse.linuxtools.tmf.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.signal.TmfRangeSynchSignal;
import org.eclipse.linuxtools.tmf.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.ITimeAnalysisViewer;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.TmfTimeScaleSelectionEvent;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.TmfTimeSelectionEvent;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.ITmfTimeAnalysisEntry;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.swt.widgets.Display;

/**
 * <p>
 * Abstract class used as a base for views handling specific time range data
 * requests
 * </p>
 * <p>
 * The class handles a single element queue of data requests, i.e. request can
 * be triggered from different sources e.g. opening a file as well as a new
 * selected time window
 * </p>
 * 
 * @author alvaro
 * 
 */
public abstract class AbsTimeUpdateView extends TmfView implements
		IRequestStatusListener {

	// ========================================================================
	// Data
	// ========================================================================

	private static final long INITIAL_WINDOW_OFFSET = (1L * 100 * 1000 * 1000); // .1sec

	/**
	 * Number of events before a GUI refresh
	 */
	private static final Long INPUT_CHANGED_REFRESH = 3000L;
	private static final long DEFAULT_OFFSET = 0L;
	private static final int DEFAULT_CHUNK = 1;

	protected boolean synch = true; // time synchronisation, used to be an
									// option
	protected ITimeAnalysisViewer tsfviewer = null;

	private LttngSyntEventRequest fCurrentRequest = null;

		// ========================================================================
	// Constructor
	// ========================================================================
	public AbsTimeUpdateView(String viewID) {
		super(viewID);
		// freqState = UiCommonFactory.getQueue(this);
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
		LttngSyntEventRequest request = signal.getRequest();
		if (request != null) {
			// update queue with the id of the current request.
			// freqState.requestStarted(request);

			// if there was no new request then this one is still on
			// prepare for the reception of new data
			waitCursor(true);

			// no new time range for zoom orders
			TmfTimeRange trange = null;
			// Time Range will be used to filter out events which are
			// not visible in one pixel
			trange = request.getRange();

			// indicate if the data model needs to be cleared e.g. a new
			// experiment is being selected
			boolean clearData = request.isclearDataInd();
			// Indicate if current data needs to be cleared and if so
			// specify the new experiment time range that applies
			ModelUpdatePrep(trange, clearData);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.linuxtools.lttng.state.IStateDataRequestListener#
	 * processingCompleted(org.eclipse.linuxtools.lttng.state.StateDataRequest)
	 */
	@TmfSignalHandler
	public void processingCompleted(RequestCompletedSignal signal) {
		ILttngSyntEventRequest request = signal.getRequest();

		if (request == null) {
			return;
		}

		// Update wait cursor
		waitCursor(false);

		// No data refresh actions for cancelled requests.
		if (request.isCancelled() || request.isFailed()) {
			if (TraceDebug.isDEBUG()) {
				TmfTimeRange trange = request.getRange();
				if (request.isCancelled()) {
					TraceDebug.debug("Request cancelled "
							+ trange.getStartTime() + "-" + trange.getEndTime()
							+ " Handled Events: " + request.getSynEventCount()
							+ " " + request.toString(), 15);
				} else if (request.isFailed()) {
					TraceDebug.debug("Request Failed " + trange.getStartTime()
							+ "-" + trange.getEndTime() + " Handled Events: "
							+ request.getSynEventCount() + " "
							+ request.toString());
				}
			}

			return;
		} else {
			modelInputChanged(request, true);
		}
	}

	/**
	 * Registers as listener of time selection from other views
	 * 
	 * @param signal
	 */
	public void synchToTime(TmfTimeSynchSignal signal) {
		if (synch) {
			Object source = signal.getSource();
			if (signal != null && source != null && source != this) {
				// Internal value is expected in nano seconds.
				long selectedTime = signal.getCurrentTime().getValue();
				if (tsfviewer != null) {
					tsfviewer.setSelectedTime(selectedTime, true, source);
				}
			}
		}
	}

	/**
	 * Process the reception of time window adjustment in this view if the
	 * source of the update is not this view.
	 * 
	 * @param signal
	 * @param clearingData
	 */
	public void synchToTimeRange(TmfRangeSynchSignal signal, boolean clearingData) {
		if (synch) {
			Object source = signal.getSource();
			if (signal != null && source != null && source != this) {
				// Internal value is expected in nano seconds.
				TmfTimeRange trange = signal.getCurrentRange();
				TmfExperiment<?> experiment = TmfExperiment.getCurrentExperiment();
				if (experiment == null) {
					TraceDebug.debug("Current selected experiment is null");
					return;
				}

				// Clearing of process data is configurable
				dataRequest(trange, experiment.getTimeRange(), clearingData, ExecutionType.FOREGROUND);
			}
		}
	}

	/**
	 * Trigger time synchronisation to other views this method shall be called
	 * when a check has been performed to note that an actual change of time has
	 * been performed vs a pure re-selection of the same time
	 * 
	 * @param time
	 * @param source
	 */
	protected void synchTimeNotification(long time, Object source) {
		// if synchronisation selected
		if (synch) {
			// Notify other views
			TmfSignalManager.dispatchSignal(new TmfTimeSynchSignal(source, new LttngTimestamp(time)));
		}
	}

	/**
	 * Common implementation of ITmfTimeSelectionListener, not used by all the
	 * views extending this abstract class
	 * 
	 * @param event
	 */
	protected void tsfTmProcessSelEvent(TmfTimeSelectionEvent event) {
		Object source = event.getSource();
		if (source == null) {
			return;
		}

		ParamsUpdater paramUpdater = getParamsUpdater();
		Long savedSelTime = paramUpdater.getSelectedTime();

		long selTimens = event.getSelectedTime();

		// make sure the new selected time is different than saved before
		// executing update
		if (savedSelTime == null || savedSelTime != selTimens) {
			// Notify listener views.
			synchTimeNotification(selTimens, source);

			// Update the parameter updater to save the selected time
			paramUpdater.setSelectedTime(selTimens);

			if (TraceDebug.isDEBUG()) {
				TraceDebug.debug("Selected Time: " + new LttngTimestamp(selTimens) + "\n\t\t" + getName());
			}
		}
	}

	/**
	 * Common implementation of ITmfTimeScaleSelectionListener, not used by all
	 * the views extending this abstract class
	 * 
	 * @param event
	 */
	protected synchronized void tsfTmProcessTimeScaleEvent(TmfTimeScaleSelectionEvent event) {
		// source needed to keep track of source values
		Object source = event.getSource();

		if (source != null) {
			// Update the parameter updater before carrying out a read request
			ParamsUpdater paramUpdater = getParamsUpdater();
			boolean newParams = paramUpdater.processTimeScaleEvent(event);

			if (newParams) {
				// Read the updated time window
				TmfTimeRange trange = paramUpdater.getTrange();
				if (trange != null) {

					// Notify listener views. to perform data requests
					// upon this notification
					synchTimeRangeNotification(trange, paramUpdater.getSelectedTime(), source);
				}
			}
		}
	}

	/**
	 * Inform registered listeners about the new time range
	 * 
	 * @param trange
	 * @param selectedTime
	 * @param source
	 */
	protected void synchTimeRangeNotification(TmfTimeRange trange, Long selectedTime, Object source) {
		// if synchronisation selected
		if (synch) {
			// Notify other views
			TmfSignalManager.dispatchSignal(new TmfRangeSynchSignal(source, trange, new LttngTimestamp(selectedTime)));
		}
	}

	/**
	 * @param zoomedTRange
	 * @param experimentTRange
	 * @param execType 
	 */
	public void dataRequest(TmfTimeRange zoomedTRange,
			TmfTimeRange experimentTRange, boolean clearingData, ExecutionType execType) {

		// timeRange is the Experiment time range
		 boolean sent = processDataRequest(zoomedTRange, experimentTRange, clearingData, execType);

		if (sent) {
			waitCursor(true);
		}
	}

//	/**
//	 * @param zoomedTRange
//	 * @param experimentTRange
//	 * @param execType 
//	 */
//	public void dataRequest(TmfTimeRange zoomedTRange,
//			TmfTimeRange experimentTRange, boolean clearingData) {
//
//		// timeRange is the Experiment time range
//		 boolean sent = processDataRequest(zoomedTRange, experimentTRange, clearingData);
//
//		if (sent) {
//			waitCursor(true);
//		}
//	}

	/**
	 * send data request directly e.g. doesn't use a queue
	 * 
	 * @param requestTrange
	 * @param listener
	 * @param experimentTRange
	 * @param execType 
	 * @param processor
	 * @return
	 */
	private boolean processDataRequest(TmfTimeRange requestTrange,
			TmfTimeRange experimentTRange, boolean clearingData, ExecutionType execType) {
		// Validate input
		if (requestTrange == null || experimentTRange == null) {
			TraceDebug.debug("Invalid input");
			return false;
		}

		// Cancel the currently executing request before starting a new one
		if (fCurrentRequest != null && !fCurrentRequest.isCompleted()) {
//			System.out.println("Cancelling request");
//			fCurrentRequest.cancel();
		}
		
		fCurrentRequest = new LttngSyntEventRequest(
				requestTrange, DEFAULT_OFFSET, TmfDataRequest.ALL_DATA,
				DEFAULT_CHUNK, this, experimentTRange, getEventProcessor(), execType) {
	
			Long fCount = getSynEventCount();
			ITransEventProcessor processor = getProcessor();
			TmfTimestamp frunningTimeStamp;
	
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.linuxtools.lttng.request.LttngSyntEventRequest#handleData
			 * ()
			 */
////			int handleDataCount = 0;
////			int handleDataValidCount = 0;
//			@Override
//			public void handleData() {
//				LttngSyntheticEvent[] result = getData();
//	
//				TmfEvent evt = (result.length > 0) ? result[0] : null;
////				handleDataCount++;

			@Override
			public void handleData(LttngSyntheticEvent event) {
				super.handleData(event);
				if (event != null) {
//					handleDataValidCount++;
					LttngSyntheticEvent synEvent = (LttngSyntheticEvent) event;
					// process event
					SequenceInd indicator = synEvent.getSynType();
					if (indicator == SequenceInd.BEFORE
							|| indicator == SequenceInd.AFTER) {
						processor.process(event, synEvent.getTraceModel());
					} else if (indicator == SequenceInd.STARTREQ) {
						handleRequestStarted();
					} else if (indicator == SequenceInd.ENDREQ) {
						processor.process(event, synEvent.getTraceModel());
						// handleCompleted();
					}
	
					if (indicator == SequenceInd.BEFORE) {
						fCount++;
						if (fCount != 0 && fCount % INPUT_CHANGED_REFRESH == 0) {
							// send partial update
							modelInputChanged(this, false);
	
							if (TraceDebug.isDEBUG()) {
								frunningTimeStamp = event.getTimestamp();
								TraceDebug.debug("handled: " + fCount + " sequence: " + synEvent.getSynType());
							}
	
						}
					}
				}
			}
	
			public void handleRequestStarted() {
				notifyStarting();
			}

			@Override
			public void done() {
//				if (TraceDebug.isDEBUG()) {
//					TraceDebug.debug("AbsTimeUpdateView: Received=" + handleDataCount + ", Valid=" + handleDataCount + ", fCount=" + fCount);
//				}
				super.done();
			}
	
			@Override
			public void handleCompleted() {
				super.handleCompleted();

				// Data is not complete and should be handled as such
				if (isFailed() || isCancelled()) {
					modelIncomplete(this);
				}

				if (TraceDebug.isDEBUG()) {
					if (frunningTimeStamp != null) {
						TraceDebug.debug("Last event time stamp: "
								+ frunningTimeStamp.getValue());
					}
				}
			}
		};
	
		// obtain singleton core provider
		LttngSyntheticEventProvider provider = LttngCoreProviderFactory
				.getEventProvider();
	
		// send the request to TMF
		fCurrentRequest.startRequestInd(provider);
		fCurrentRequest.setclearDataInd(clearingData);
		return true;
	}

	/**
	 * Returns an initial smaller window to allow the user to select the area of
	 * interest
	 * 
	 * @param experimentTRange
	 * @return
	 */
	protected TmfTimeRange getInitTRange(TmfTimeRange experimentTRange) {
		TmfTimestamp expStartTime = experimentTRange.getStartTime();
		TmfTimestamp expEndTime = experimentTRange.getEndTime();
		TmfTimestamp initialEndOfWindow = new LttngTimestamp(expStartTime
				.getValue()
				+ INITIAL_WINDOW_OFFSET);
		if (initialEndOfWindow.compareTo(expEndTime, false) < 0) {
			return new TmfTimeRange(expStartTime, initialEndOfWindow);
		}

		// The original size of the experiment is smaller than proposed adjusted
		// time
		return experimentTRange;
	}

	/**
	 * Request the Time Analysis widget to enable or disable the wait cursor
	 * e.g. data request in progress or data request completed
	 * 
	 * @param waitInd
	 */
	protected void waitCursor(final boolean waitInd) {
		if (tsfviewer != null) {
			Display display = tsfviewer.getControl().getDisplay();

			// Perform the updates on the UI thread
			display.asyncExec(new Runnable() {
				public void run() {
					tsfviewer.waitCursor(waitInd);
				}
			});
		}
	}

	/**
	 * View preparation to override the current local information
	 * 
	 * @param timeRange
	 *            - new total time range e.g. Experiment level
	 * @param clearAllData
	 */
	protected void ModelUpdatePrep(TmfTimeRange timeRange, boolean clearAllData) {
		ItemContainer<?> itemContainer = getItemContainer();
		if (clearAllData) {
			// start fresh e.g. new experiment selected
			itemContainer.clearItems();
		} else {
			// clear children but keep processes
			itemContainer.clearChildren();
		}

		// Obtain the current resource array
		ITmfTimeAnalysisEntry[] itemArr = itemContainer.readItems();

		// clean up data and boundaries
		displayModel(itemArr, -1, -1, false, -1, -1, null);

		ParamsUpdater updater = getParamsUpdater();
		if (updater != null) {
			// Start over
			updater.setEventsDiscarded(0);

			// Update new visible time range if available
			if (timeRange != null) {
				updater.update(timeRange.getStartTime().getValue(), timeRange.getEndTime().getValue());
			}
		}
	}

	/**
	 * Initialize the model and view before reloading items
	 * 
	 * @param boundaryRange
	 * @param visibleRange
	 * @param source
	 */
	protected void ModelUpdateInit(TmfTimeRange boundaryRange, TmfTimeRange visibleRange, Object source) {
		// Update the view boundaries
		if (boundaryRange != null) {
			ItemContainer<?> itemContainer = getItemContainer();
			if (itemContainer != null) {
				itemContainer.clearItems();
				// Obtain the current process array
				ITmfTimeAnalysisEntry[] itemArr = itemContainer.readItems();

				long startTime = boundaryRange.getStartTime().getValue();
				long endTime = boundaryRange.getEndTime().getValue();

				// Update the view part
				displayModel(itemArr, startTime, endTime, true, visibleRange.getStartTime().getValue(), visibleRange
						.getEndTime().getValue(), source);
			}
		}

		// update the view filtering parameters
		if (visibleRange != null) {
			ParamsUpdater updater = getParamsUpdater();
			if (updater != null) {
				// Start over
				updater.setEventsDiscarded(0);
				// Update new visible time range if available
				updater.update(visibleRange.getStartTime().getValue(), visibleRange.getEndTime().getValue());
			}
		}
	}

	/**
	 * Actions taken by the view to refresh its widget(s) with the updated data
	 * model
	 * 
	 * @param request
	 * @param complete
	 *            true: yes, false: partial update
	 */
	protected void modelInputChanged(ILttngSyntEventRequest request, boolean complete) {
		long experimentStartTime = -1;
		long experimentEndTime = -1;
		TmfTimeRange experimentTimeRange = request.getExperimentTimeRange();
		if (experimentTimeRange != null) {
			experimentStartTime = experimentTimeRange.getStartTime().getValue();
			experimentEndTime = experimentTimeRange.getEndTime().getValue();
		}

		// Obtain the current resource list
		ITmfTimeAnalysisEntry[] itemArr = getItemContainer().readItems();

		if (itemArr != null) {
			// Sort the array by pid
			Arrays.sort(itemArr);

			// Update the view part
			displayModel(itemArr, experimentStartTime, experimentEndTime, false, request.getRange().getStartTime()
					.getValue(), request.getRange().getEndTime().getValue(), request.getSource());
		}

		if (complete) {
			// reselect to original time
			ParamsUpdater paramUpdater = getParamsUpdater();
			if (paramUpdater != null && tsfviewer != null) {
				final Long selTime = paramUpdater.getSelectedTime();
				if (selTime != null) {
					TraceDebug.debug("View: " + getName() + "\n\t\tRestoring the selected time to: " + selTime);
					Display display = tsfviewer.getControl().getDisplay();
					display.asyncExec(new Runnable() {
						public void run() {
							tsfviewer.setSelectedTime(selTime, false, this);
						}
					});
				}

//				System.out.println(System.currentTimeMillis() + ": AbsTimeUpdate (" + getName() + ") completed");

				if (TraceDebug.isDEBUG()) {
					int eventCount = 0;
					Long count = request.getSynEventCount();
					for (int pos = 0; pos < itemArr.length; pos++) {
						eventCount += itemArr[pos].getTraceEvents().size();
					}

					int discarded = paramUpdater.getEventsDiscarded();
					int discardedOutofOrder = paramUpdater.getEventsDiscardedWrongOrder();
					int discardedOutofViewRange = paramUpdater.getEventsDiscardedOutOfViewRange();
					int dicardedNotVisible = paramUpdater.getEventsDiscardedNotVisible();

					TmfTimeRange range = request.getRange();
					StringBuilder sb = new StringBuilder("View: " + getName() + ", Events handled: " + count
							+ ", Events loaded in view: " + eventCount + ", Number of events discarded: " + discarded
							+ "\n\tNumber of events discarded with start time earlier than next good time: "
							+ discardedOutofOrder + "\n\tDiscarded Not visible: " + dicardedNotVisible
							+ "\n\tDiscarded out of view Range: " + discardedOutofViewRange);

					sb.append("\n\t\tRequested Time Range: " + range.getStartTime() + "-" + range.getEndTime());
					sb.append("\n\t\tExperiment Time Range: " + experimentStartTime + "-" + experimentEndTime);
					TraceDebug.debug(sb.toString());
				}
			}

		}
	}

	// /**
	// * Obtains the remainder fraction on unit Seconds of the entered value in
	// * nanoseconds. e.g. input: 1241207054171080214 ns The number of seconds
	// can
	// * be obtain by removing the last 9 digits: 1241207054 the fractional
	// * portion of seconds, expressed in ns is: 171080214
	// *
	// * @param v
	// * @return
	// */
	// protected String formatNs(long v) {
	// StringBuffer str = new StringBuffer();
	// boolean neg = v < 0;
	// if (neg) {
	// v = -v;
	// str.append('-');
	// }
	//
	// String strVal = String.valueOf(v);
	// if (v < 1000000000) {
	// return strVal;
	// }
	//
	// // Extract the last nine digits (e.g. fraction of a S expressed in ns
	// return strVal.substring(strVal.length() - 9);
	// }

	/**
	 * The request was stopped, the data is incomplete
	 * 
	 * @param request
	 */
	protected abstract void modelIncomplete(ILttngSyntEventRequest request);

	/**
	 * Returns the Event processor instance related to a specific view
	 * 
	 * @return
	 */
	protected abstract ITransEventProcessor getEventProcessor();

	/**
	 * To be overridden by some sub-classes although may not be needed in some
	 * e.g. statistics view
	 * 
	 * @param items
	 * @param startBoundTime
	 * @param endBoundTime
	 * @param updateTimeBounds
	 *            - Time bounds updated needed e.g. if a new Experiment or trace
	 *            is selected
	 * @param startVisibleWindow
	 * @param endVisibleWindow
	 * @param source
	 */
	protected abstract void displayModel(final ITmfTimeAnalysisEntry[] items, final long startBoundTime,
			final long endBoundTime, final boolean updateTimeBounds, final long startVisibleWindow,
			final long endVisibleWindow, final Object source);

	/**
	 * To be overridden by some sub-classes although may not be needed in some
	 * e.g. statistics view
	 * 
	 * @return
	 */
	protected abstract ParamsUpdater getParamsUpdater();

	/**
	 * Returns the model's item container
	 * 
	 * @return
	 */
	protected abstract ItemContainer<?> getItemContainer();
}
