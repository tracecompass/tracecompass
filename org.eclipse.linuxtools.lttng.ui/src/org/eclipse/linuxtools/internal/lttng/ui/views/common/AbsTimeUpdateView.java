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
 *   Bernd Hufmann - Bug fixes
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.ui.views.common;

import java.util.Arrays;

import org.eclipse.linuxtools.internal.lttng.core.LttngConstants;
import org.eclipse.linuxtools.internal.lttng.core.control.LttngCoreProviderFactory;
import org.eclipse.linuxtools.internal.lttng.core.control.LttngSyntheticEventProvider;
import org.eclipse.linuxtools.internal.lttng.core.event.LttngSyntheticEvent;
import org.eclipse.linuxtools.internal.lttng.core.event.LttngTimestamp;
import org.eclipse.linuxtools.internal.lttng.core.request.ILttngSyntEventRequest;
import org.eclipse.linuxtools.internal.lttng.core.request.IRequestStatusListener;
import org.eclipse.linuxtools.internal.lttng.core.request.LttngSyntEventRequest;
import org.eclipse.linuxtools.internal.lttng.core.request.RequestCompletedSignal;
import org.eclipse.linuxtools.internal.lttng.core.request.RequestStartedSignal;
import org.eclipse.linuxtools.internal.lttng.core.state.evProcessor.ITransEventProcessor;
import org.eclipse.linuxtools.internal.lttng.ui.TraceDebug;
import org.eclipse.linuxtools.internal.lttng.ui.model.trange.ItemContainer;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest.ExecutionType;
import org.eclipse.linuxtools.tmf.core.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentDisposedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfRangeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
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
public abstract class AbsTimeUpdateView extends TmfView implements IRequestStatusListener {

	// ========================================================================
	// Data
	// ========================================================================

//	 private static final long INITIAL_WINDOW_OFFSET = (1L * 1    * 1000 * 1000); // .001sec
//	 private static final long INITIAL_WINDOW_OFFSET = (1L * 10   * 1000 * 1000); // .01sec
	 private static final long INITIAL_WINDOW_OFFSET = (1L * 100  * 1000 * 1000); // .1sec
//	 private static final long INITIAL_WINDOW_OFFSET = (1L * 1000 * 1000 * 1000); // 1sec

	/**
	 * Number of events before a GUI refresh
	 */
	protected static final Long INPUT_CHANGED_REFRESH = 75000L;
	private static final long DEFAULT_OFFSET = 0;

	protected boolean synch = true; // time synchronization, used to be an option
	protected ITimeAnalysisViewer tsfviewer = null;

	private LttngSyntEventRequest fCurrentRequest = null;

	protected LttngSyntheticEventProvider fProvider = LttngCoreProviderFactory.getEventProvider(getProviderId());
	
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
	
	/**
	 * Returns the number of events after which the relevant display will 
	 * be refreshed
	 * 
	 * @return  
	 */
	protected Long getInputChangedRefresh() {
	    return INPUT_CHANGED_REFRESH;
	}

   /**
     * Cancel the ongoing request if another experiment is being selected
     * @param experimentDisposedSignal
     */
    @TmfSignalHandler
    public void experimentDisposed(TmfExperimentDisposedSignal<? extends TmfEvent> experimentDisposedSignal) {
        if (experimentDisposedSignal.getExperiment() != TmfExperiment.getCurrentExperiment()) {
            return;
        }
        fProvider.conditionallyCancelRequests();
    }
	
	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.linuxtools.lttng.state.IStateDataRequestListener#
	 * processingStarted(org.eclipse.linuxtools.lttng.state.StateDataRequest)
	 */
	@Override
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
			modelUpdatePrep(trange, clearData);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.linuxtools.lttng.state.IStateDataRequestListener#
	 * processingCompleted(org.eclipse.linuxtools.lttng.state.StateDataRequest)
	 */
	@Override
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
					TraceDebug.debug("Request cancelled " //$NON-NLS-1$
							+ trange.getStartTime() + "-" + trange.getEndTime() //$NON-NLS-1$
							+ " Handled Events: " + request.getSynEventCount() //$NON-NLS-1$
							+ " " + request.toString(), 15); //$NON-NLS-1$
				} else if (request.isFailed()) {
					TraceDebug.debug("Request Failed " + trange.getStartTime() //$NON-NLS-1$
							+ "-" + trange.getEndTime() + " Handled Events: " //$NON-NLS-1$ //$NON-NLS-2$
							+ request.getSynEventCount() + " " //$NON-NLS-1$
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
        if (signal == null)
	        return;
		if (synch) {
			Object source = signal.getSource();
			if (source != null && source != this) {

				if ((tsfviewer != null) && (!tsfviewer.getControl().isDisposed())) {

					// Check for GUI thread
					if (Display.getCurrent() != null) {
						// GUI thread - execute update right away.
						
						// Internal value is expected in nano seconds.
						long selectedTime = signal.getCurrentTime().getValue();
						if (tsfviewer != null) {
							tsfviewer.setSelectedTime(selectedTime, true, source);

							ParamsUpdater paramUpdater = getParamsUpdater();
						    Long savedSelTime = paramUpdater.getSelectedTime();
						    if ((savedSelTime == null) || (savedSelTime != selectedTime)) {
					            // Update the parameter updater to save the selected time
					            paramUpdater.setSelectedTime(selectedTime);   
					        }
						}
					} else {
						// Perform the updates on the UI thread
						
						// We need to clone the timestamp in the signal so that it won't be overwritten duo to multipe thread access 
						final TmfTimeSynchSignal savedSignal = new TmfTimeSynchSignal(signal.getSource(), signal.getCurrentTime().clone());
						tsfviewer.getControl().getDisplay().asyncExec(new Runnable() {
							@Override
							public void run() {
								if ((tsfviewer != null) && (!tsfviewer.getControl().isDisposed())) {
									synchToTime(savedSignal);
								}
							}
						});
					}
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
	    if (signal == null)
	         return;
		if (synch) {
			Object source = signal.getSource();
			if (source != null && source != this) {
				// Internal value is expected in nano seconds.
				TmfTimeRange trange = signal.getCurrentRange();
				TmfExperiment<?> experiment = TmfExperiment.getCurrentExperiment();
				if (experiment == null) {
					TraceDebug.debug("Current selected experiment is null"); //$NON-NLS-1$
					return;
				}

				// Clearing of process data is configurable
				eventRequest(trange, experiment.getTimeRange(), clearingData, ExecutionType.FOREGROUND);
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
				TraceDebug.debug("Selected Time: " + new LttngTimestamp(selTimens) + "\n\t\t" + getName()); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

	/**
	 * Common implementation of ITmfTimeScaleSelectionListener, not used by all
	 * the views extending this abstract class
	 * 
	 * @param event
	 */
	protected void tsfTmProcessTimeScaleEvent(TmfTimeScaleSelectionEvent event) {
        // source needed to keep track of source values
        Object source = event.getSource();

        boolean newParams = false;
        TmfTimeRange trange = null;
        Long selectedTime = null;

        // update all information and get relevant data
	    synchronized (this) {
	        if (source != null) {
	            // Update the parameter updater before carrying out a read request
	            ParamsUpdater paramUpdater = getParamsUpdater();
	            newParams = paramUpdater.processTimeScaleEvent(event);

	            if (newParams) {
	                // Read the updated time window
	                trange = paramUpdater.getTrange();
	                if (trange != null) {
	                    selectedTime = paramUpdater.getSelectedTime();
	                }
	            }
	        }
	    }

	    // Check for selectedTime is sufficient since it is only set if
	    // newParams is true and trange is not null
		if (selectedTime != null) {
		    // Notify listener views. to perform data requests
            // upon this notification

		    // Note that this has to be done outside the synchronized statement
		    // because otherwise we could end-up in a deadlock if a ongoing 
		    // request needs to be canceled.
            synchTimeRangeNotification(trange, selectedTime, source);		    
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
	 * @param clearingData
	 * @param execType 
	 */
	public void eventRequest(TmfTimeRange zoomedTRange, TmfTimeRange experimentTRange, boolean clearingData, ExecutionType execType) {

		// timeRange is the Experiment time range
		boolean sent = processDataRequest(zoomedTRange, experimentTRange, DEFAULT_OFFSET, TmfDataRequest.ALL_DATA, clearingData, execType);

		if (sent) {
			waitCursor(true);
		}
	}

	/**
	 * @param offset
	 * @param nbRequested
	 * @param startTime
	 * @param clearingData
	 * @param execType 
	 */
	public void eventRequest(long offset, TmfTimeRange range, boolean clearingData, ExecutionType execType) {

		// timeRange is the Experiment time range
		boolean sent = processDataRequest(range, null, offset, TmfDataRequest.ALL_DATA, clearingData, execType);

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
			TmfTimeRange experimentTRange, long offset, int nbRequested, boolean clearingData, ExecutionType execType) {
		// Validate input
		if (requestTrange == null) {
			TraceDebug.debug("Invalid input"); //$NON-NLS-1$
			return false;
		}

		// Cancel the currently executing request before starting a new one
		fProvider.conditionallyCancelRequests();
		fCurrentRequest = new LttngSyntEventRequest(
				requestTrange, offset, nbRequested,
				LttngConstants.DEFAULT_BLOCK_SIZE, this, experimentTRange, getEventProcessor(), 
				TmfExperiment.getCurrentExperiment().getName(), execType) {
	
			Long fCount = getSynEventCount();
			ITransEventProcessor processor = getProcessor();
			ITmfTimestamp frunningTimeStamp;
	
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
					switch (synEvent.getSynType()) {

					    case STARTREQ: {
					        handleRequestStarted();
					        break;
					    }

					    case BEFORE: {
					        processor.process(event, synEvent.getTraceModel());
					        fCount++;
					        if ((fCount != 0) && (fCount % getInputChangedRefresh() == 0)) {
					            // send partial update
					            modelInputChanged(this, false);
  
					            if (TraceDebug.isDEBUG()) {
					                frunningTimeStamp = event.getTimestamp();
					                TraceDebug.debug("handled: " + fCount + " sequence: " + synEvent.getSynType()); //$NON-NLS-1$ //$NON-NLS-2$
					            }
					        }
					        break;
					    }

					    case AFTER:
					        // fall-through
					    case ENDREQ:{
					        processor.process(event, synEvent.getTraceModel());
					        break;
					    }

					    default:
                          // nothing to do
                          break;
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
						TraceDebug.debug("Last event time stamp: " + frunningTimeStamp.getValue()); //$NON-NLS-1$
					}
				}
			}
		};
	
		// send the request to TMF
		fCurrentRequest.startRequestInd(fProvider);
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
		ITmfTimestamp expStartTime = experimentTRange.getStartTime();
		ITmfTimestamp expEndTime = experimentTRange.getEndTime();
		ITmfTimestamp initialEndOfWindow = new LttngTimestamp(expStartTime
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
		if ((tsfviewer != null) && (!tsfviewer.getControl().isDisposed())) {
			Display display = tsfviewer.getControl().getDisplay();

			// Perform the updates on the UI thread
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					if ((tsfviewer != null) && (!tsfviewer.getControl().isDisposed())) {
						tsfviewer.waitCursor(waitInd);
					}
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
	protected void modelUpdatePrep(TmfTimeRange timeRange, boolean clearAllData) {
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
	protected void modelUpdateInit(TmfTimeRange boundaryRange, TmfTimeRange visibleRange, Object source) {
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
	@SuppressWarnings("deprecation")
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
			if ((paramUpdater != null) && (tsfviewer != null) && (!tsfviewer.getControl().isDisposed())) {
				final Long selTime = paramUpdater.getSelectedTime();
				if (selTime != null) {
					TraceDebug.debug("View: " + getName() + "\n\t\tRestoring the selected time to: " + selTime); //$NON-NLS-1$ //$NON-NLS-2$
					Display display = tsfviewer.getControl().getDisplay();
					display.asyncExec(new Runnable() {
						@Override
						public void run() {
							if ((tsfviewer != null) && (!tsfviewer.getControl().isDisposed())) {
								tsfviewer.setSelectedTime(selTime, false, this);
							}
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
					StringBuilder sb = new StringBuilder("View: " + getName() + ", Events handled: " + count //$NON-NLS-1$ //$NON-NLS-2$
							+ ", Events loaded in view: " + eventCount + ", Number of events discarded: " + discarded //$NON-NLS-1$ //$NON-NLS-2$
							+ "\n\tNumber of events discarded with start time earlier than next good time: " //$NON-NLS-1$
							+ discardedOutofOrder + "\n\tDiscarded Not visible: " + dicardedNotVisible //$NON-NLS-1$
							+ "\n\tDiscarded out of view Range: " + discardedOutofViewRange); //$NON-NLS-1$

					sb.append("\n\t\tRequested Time Range: " + range.getStartTime() + "-" + range.getEndTime()); //$NON-NLS-1$ //$NON-NLS-2$
					sb.append("\n\t\tExperiment Time Range: " + experimentStartTime + "-" + experimentEndTime); //$NON-NLS-1$ //$NON-NLS-2$
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

	/**
	 * Returns LTTng Synthetic Provider ID used for current view
	 * 
	 * @return  
	 */
	protected abstract int getProviderId();
}
