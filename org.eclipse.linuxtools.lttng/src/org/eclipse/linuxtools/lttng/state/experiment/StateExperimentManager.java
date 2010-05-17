/*******************************************************************************
+ * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon (alvsan09@gmail.com) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.state.experiment;

import org.eclipse.linuxtools.lttng.TraceDebug;
import org.eclipse.linuxtools.lttng.control.LttngCoreProviderFactory;
import org.eclipse.linuxtools.lttng.control.LttngSyntheticEventProvider;
import org.eclipse.linuxtools.lttng.event.LttngEvent;
import org.eclipse.linuxtools.lttng.model.LTTngTreeNode;
import org.eclipse.linuxtools.lttng.request.ILttngSyntEventRequest;
import org.eclipse.linuxtools.lttng.request.IRequestStatusListener;
import org.eclipse.linuxtools.lttng.signal.ILttExperimentSelectedListener;
import org.eclipse.linuxtools.lttng.signal.StateExperimentListener;
import org.eclipse.linuxtools.lttng.state.evProcessor.ITransEventProcessor;
import org.eclipse.linuxtools.lttng.state.trace.IStateTraceManager;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.experiment.TmfExperiment;
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


	// ========================================================================
	// Constructors
	// =======================================================================
	public StateExperimentManager(Long id, String name) {
		super(id, null, name, null);
		fexperimentListener = new StateExperimentListener("Experiment Manager",
				this);
	}


	// ========================================================================
	// Methods
	// =======================================================================

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.lttng.state.experiment.IStateExperimentManager#readExperimentTimeWindow(org.eclipse.linuxtools.tmf.event.TmfTimeRange, java.lang.String, org.eclipse.linuxtools.lttng.state.IStateDataRequestListener)
	 */
	public ILttngSyntEventRequest readExperimentTimeWindow(TmfTimeRange trange,
			Object source, IRequestStatusListener listener,
			ITransEventProcessor processor) {

		ILttngSyntEventRequest request = null;

		// validate
		if (fSelectedExperiment != null) {
			// Get all trace manager nodes
			LTTngTreeNode[] traceMgrs = fSelectedExperiment.getChildren();

			if (traceMgrs != null && traceMgrs.length > 0) {
				IStateTraceManager traceManager;
				// Trigger one request per trace
				for (LTTngTreeNode traceNode : traceMgrs) {
					traceManager = (IStateTraceManager) traceNode;
					request = traceManager.executeDataRequest(trange, source,
							listener,
							processor);
				}
			}
		} else {
			if (fSelectedExperiment == null) {
				TraceDebug.debug("No experiment selected");
			}
		}

		return request;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.lttng.state.experiment.IStateExperimentManager#readExperiment(java.lang.String, org.eclipse.linuxtools.lttng.state.IStateDataRequestListener)
	 */
	@SuppressWarnings("unchecked")
	public void readExperiment(Object source, IRequestStatusListener listener,
			ITransEventProcessor processor) {
		// validate
		if (fSelectedExperiment != null) {
			TmfExperiment<LttngEvent> experiment = (TmfExperiment<LttngEvent>) fSelectedExperiment
					.getValue();
			TmfTimeRange trange = experiment.getTimeRange();
			readExperimentTimeWindow(trange, source, listener, processor);
		} else {
			TraceDebug.debug("No selected experiment available");
		}
	}
	


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
			return;
		}

		LTTngTreeNode experimentNode = getChildByName(experiment.getName());
		if (experimentNode != null) {
			// get the trace manager nodes
			LTTngTreeNode[] traceNodes = experimentNode.getChildren();
			for (LTTngTreeNode traceStateManagerNode : traceNodes) {
				// The trace node needs to perform its first data request
				// for this experiment with the main goal of building its
				// checkpoints
				if (traceStateManagerNode instanceof ILttExperimentSelectedListener) {
					// no need to provide the trace to the trace manager
					((ILttExperimentSelectedListener) traceStateManagerNode).experimentUpdated(
							new TmfExperimentUpdatedSignal(source, experiment, null), fwaitForCompletion);
				}
			}
		}
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

}