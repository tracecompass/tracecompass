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
 *   Marc Dumais (marc.dumais@ericsson.com) - Fix for 316455 (second part)
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.state.experiment;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.lttng.TraceDebug;
import org.eclipse.linuxtools.lttng.control.LttngCoreProviderFactory;
import org.eclipse.linuxtools.lttng.control.LttngSyntheticEventProvider;
import org.eclipse.linuxtools.lttng.event.LttngEvent;
import org.eclipse.linuxtools.lttng.event.LttngSyntheticEvent;
import org.eclipse.linuxtools.lttng.event.LttngSyntheticEvent.SequenceInd;
import org.eclipse.linuxtools.lttng.model.LTTngTreeNode;
import org.eclipse.linuxtools.lttng.signal.ILttExperimentSelectedListener;
import org.eclipse.linuxtools.lttng.signal.StateExperimentListener;
import org.eclipse.linuxtools.lttng.state.trace.IStateTraceManager;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.signal.TmfExperimentUpdatedSignal;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace;

/**
 * @author alvaro
 * 
 */
public class StateExperimentManager extends LTTngTreeNode implements
		ILttExperimentSelectedListener, IStateExperimentManager {
	
	// ========================================================================
	// Data
	// =======================================================================
	private LTTngTreeNode fSelectedExperiment = null; // one selected experiment
														// supported
	private final StateExperimentListener fexperimentListener;
	private boolean fwaitForCompletion = false;
	/**
	 * Used to route incoming events to proper trace manager, during check point
	 * building
	 */
	private final Map<ITmfTrace, IStateTraceManager> ftraceToManagerMap = new HashMap<ITmfTrace, IStateTraceManager>();
	private final Map<ITmfTrace, Long> ftraceEventsReadMap = new HashMap<ITmfTrace, Long>();
	private LttngSyntheticEvent syntheticEvent = null;
	private ITmfEventRequest<LttngEvent> fStateCheckPointRequest = null;


	// ========================================================================
	// Constructors
	// =======================================================================
	public StateExperimentManager(Long id, String name) {
		super(id, null, name, null);
		fexperimentListener = new StateExperimentListener("Experiment Manager", this);
	}


	// ========================================================================
	// Methods
	// =======================================================================

//	/* (non-Javadoc)
//	 * @see org.eclipse.linuxtools.lttng.state.experiment.IStateExperimentManager#readExperimentTimeWindow(org.eclipse.linuxtools.tmf.event.TmfTimeRange, java.lang.String, org.eclipse.linuxtools.lttng.state.IStateDataRequestListener)
//	 */
//	public ILttngSyntEventRequest readExperimentTimeWindow(TmfTimeRange trange,
//			Object source, IRequestStatusListener listener,
//			ITransEventProcessor processor) {
//
//		ILttngSyntEventRequest request = null;
//
//		// validate
//		if (fSelectedExperiment != null) {
//			// Get all trace manager nodes
//			LTTngTreeNode[] traceMgrs = fSelectedExperiment.getChildren();
//
//			if (traceMgrs != null && traceMgrs.length > 0) {
//				IStateTraceManager traceManager;
//				// Trigger one request per trace
//				for (LTTngTreeNode traceNode : traceMgrs) {
//					traceManager = (IStateTraceManager) traceNode;
//					request = traceManager.executeDataRequest(trange, source,
//							listener,
//							processor);
//				}
//			}
//		} else {
//			if (fSelectedExperiment == null) {
//				TraceDebug.debug("No experiment selected");
//			}
//		}
//
//		return request;
//	}

//	/* (non-Javadoc)
//	 * @see org.eclipse.linuxtools.lttng.state.experiment.IStateExperimentManager#readExperiment(java.lang.String, org.eclipse.linuxtools.lttng.state.IStateDataRequestListener)
//	 */
//	@SuppressWarnings("unchecked")
//	public void readExperiment(Object source, IRequestStatusListener listener,
//			ITransEventProcessor processor) {
//		// validate
//		if (fSelectedExperiment != null) {
//			TmfExperiment<LttngEvent> experiment = (TmfExperiment<LttngEvent>) fSelectedExperiment
//					.getValue();
//			TmfTimeRange trange = experiment.getTimeRange();
//			readExperimentTimeWindow(trange, source, listener, processor);
//		} else {
//			TraceDebug.debug("No selected experiment available");
//		}
//	}
	


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.linuxtools.lttng.state.experiment.IStateExperimentManager
	 * #experimentSelected_prep
	 * (org.eclipse.linuxtools.tmf.experiment.TmfExperiment)
	 */
	public void experimentSelected_prep(TmfExperiment<LttngEvent> experiment) {
		LTTngTreeNode experimentNode = null;
		if (experiment != null) {
			experimentNode = getChildByName(experiment.getName());
			// keep experiment if already loaded with the same value
			if (experimentNode != null
					&& experimentNode.getValue() != experiment) {
				// rebuild the experiment nodes from scratch
				removeChild(experimentNode);
				experimentNode = null;
			}

			// Make sure all traces involved have a corresponding state manager
			// and
			// state system to request its initial data
			if (experimentNode == null) {
				// Create the new experiment tree node
				experimentNode = new LTTngTreeNode(getNextUniqueId(), this,
						experiment.getName(), experiment);
				// add the new experiment to this children list
				addChild(experimentNode);
			}
			
			// Make sure the traces exists in the tree
			ITmfTrace[] rtraces = experiment.getTraces();
			String traceName;
			LTTngTreeNode traceStateManagerNode;
			// StateStacksHandler
			for (ITmfTrace rtrace : rtraces) {
				traceName = rtrace.getName();
				traceStateManagerNode = experimentNode.getChildByName(traceName);
				// Node does not exist for this experiment, so needs to be
				// created
				if (traceStateManagerNode == null) {
					traceStateManagerNode = StateManagerFactory.getManager(
							rtrace, experimentNode);
					experimentNode.addChild(traceStateManagerNode);
				}
			}

			// Reset event provider to handle requests for the new experiment
			LttngSyntheticEventProvider synEventProvider = LttngCoreProviderFactory
					.getEventProvider();
			synEventProvider.reset(experimentNode);

			// preserve the selected experiment
			fSelectedExperiment = experimentNode;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.linuxtools.lttng.signal.ILttExperimentSelectedListener#
	 * experimentSelected(java.lang.Object,
	 * org.eclipse.linuxtools.tmf.experiment.TmfExperiment)
	 */
	public void experimentSelected(Object source,
			TmfExperiment<LttngEvent> experiment) {
		// validate
		if (experiment == null) { 
			TraceDebug.debug("Received expriment is null");
			return;
		}

		// If previous request is ongoing, cancel it before requesting a new
		// one.
		if (fStateCheckPointRequest != null && !fStateCheckPointRequest.isCompleted()) {
			fStateCheckPointRequest.cancel();
		}

		// trigger data request to build the state system check points
		fStateCheckPointRequest = buildCheckPoints(experiment);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.linuxtools.lttng.signal.ILttExperimentSelectedListener#
	 * experimentUpdated
	 * (org.eclipse.linuxtools.tmf.signal.TmfExperimentUpdatedSignal, boolean)
	 */
	public void experimentUpdated(TmfExperimentUpdatedSignal signal, boolean wait) {
		// NOTE: This represents the end of TMF indexing for a trace, however
		// the node was already existing and the state system check points are
		// already requested and built upon selection.
		// No action for the time being
	}


	/**
	 * @return the SelectedExperiment tree node
	 */
	public LTTngTreeNode getSelectedExperiment() {
		return fSelectedExperiment;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.lttng.state.experiment.IStateExperimentManager#getExperimentTimeRange()
	 */
	@SuppressWarnings("unchecked")
	public TmfTimeRange getExperimentTimeRange() {
		TmfTimeRange timeRangeResult = null;
		if (fSelectedExperiment != null) {
			timeRangeResult = ((TmfExperiment<LttngEvent>) fSelectedExperiment
					.getValue()).getTimeRange();
		}
		return timeRangeResult;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		fexperimentListener.dispose();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.linuxtools.lttng.state.experiment.IStateExperimentManager
	 * #waitForComplete(boolean)
	 */
	public void waitForCompletion(boolean wait) {
		fwaitForCompletion = wait;
	}

	private ITmfEventRequest<LttngEvent> buildCheckPoints(TmfExperiment<LttngEvent> experiment) {
		// validate
		if (experiment == null) {
			TraceDebug.debug("Received expriment is null");
			return null;
		}
		
		LTTngTreeNode experimentNode = getChildByName(experiment.getName());
		if (experimentNode == null) {
			TraceDebug.debug("Expriment Node " + experiment.getName() + " does not exist");
			return null;
		}
		
		// get the trace manager nodes associated to the experiment
		LTTngTreeNode[] traceNodes = experimentNode.getChildren();
		synchronized (this) {
			ftraceToManagerMap.clear();
			ftraceEventsReadMap.clear();
		}
		
		ITmfTrace trace;
		for (LTTngTreeNode traceStateManagerNode : traceNodes) {
			IStateTraceManager traceManager;
			try {
				traceManager = (IStateTraceManager) traceStateManagerNode;
			} catch (ClassCastException e) {
				System.out.println(e.getStackTrace().toString());
				return null;
			}
		
			// Clear all previously created check points as preparation to
			// re-build
			traceManager.clearCheckPoints();
		
			// build the trace to manager mapping for event dispatching
			trace = traceManager.getTrace();
			synchronized (this) {
				ftraceToManagerMap.put(trace, traceManager);
				ftraceEventsReadMap.put(trace, new Long(0));
			}
		}
		
		// if no trace mapping
		if (ftraceToManagerMap.size() < 1) {
			TraceDebug.debug("No traces associated to experiment " + experiment.getName());
			return null;
		}
		
		// Prepare event data request to build state model
		ITmfEventRequest<LttngEvent> request = new TmfEventRequest<LttngEvent>(
				LttngEvent.class, TmfTimeRange.Eternity,
				TmfDataRequest.ALL_DATA, 1, ITmfDataRequest.ExecutionType.BACKGROUND) {
		
			long nbEventsHandled = 0;
			
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.linuxtools.tmf.request.TmfDataRequest#handleData()
			 */
			@Override
			public void handleData(LttngEvent event) {
				super.handleData(event);
				if (event != null) {
//					Tracer.trace("Chk: " + event.getTimestamp());
					nbEventsHandled++;
					ITmfTrace trace = event.getParentTrace();
					IStateTraceManager traceManager = ftraceToManagerMap.get(trace);
					long nbEvents = ftraceEventsReadMap.get(trace) + 1;
					ftraceEventsReadMap.put(trace, nbEvents);
					if (traceManager != null) {
						// obtain synthetic event
						LttngSyntheticEvent synEvent = updateSynEvent(event, traceManager);
						// update state system, and save check points as needed
						traceManager.handleEvent(synEvent, nbEvents);
					} else {
						TraceDebug
								.debug("StateTraceManager not found for trace"
										+ trace.getName());
					}
				}
			}
		
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.linuxtools.tmf.request.TmfDataRequest#handleFailure()
			 */
			@Override
			public void handleFailure() {
				printCompletedMessage();
			}
			
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.linuxtools.tmf.request.TmfDataRequest#handleCancel()
			 */
			@Override
			public void handleCancel() {
				printCompletedMessage();
			}
		
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.linuxtools.tmf.request.TmfDataRequest#handleSuccess()
			 */
			@Override
			public void handleSuccess() {
				printCompletedMessage();
			}
		
			/**
			 * @param header
			 */
			private void printCompletedMessage() {
				if (TraceDebug.isDEBUG()) {
					TraceDebug.debug("Trace check point building completed, number of events handled: " + nbEventsHandled
							+ "\n\t\t");
					for (IStateTraceManager traceMgr : ftraceToManagerMap.values()) {
						TraceDebug.debug(traceMgr.toString() + "\n\t\t");
					}
				}
			}
		};
		
		// Execute event data request
		experiment.sendRequest(request);

		if (fwaitForCompletion) {
			try {
				request.waitForCompletion();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return request;
	}


	private LttngSyntheticEvent updateSynEvent(LttngEvent e, IStateTraceManager stateTraceManager) {
		if (syntheticEvent == null || syntheticEvent.getBaseEvent() != e) {
			syntheticEvent = new LttngSyntheticEvent(e);
		}

		// Trace model needed by application handlers
		syntheticEvent.setTraceModel(stateTraceManager.getStateModel());
		syntheticEvent.setSequenceInd(SequenceInd.UPDATE);

		return syntheticEvent;
	}

}