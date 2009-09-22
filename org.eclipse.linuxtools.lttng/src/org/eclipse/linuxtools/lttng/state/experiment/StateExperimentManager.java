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
package org.eclipse.linuxtools.lttng.state.experiment;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.lttng.state.IStateDataRequestListener;
import org.eclipse.linuxtools.lttng.state.StateManager;
import org.eclipse.linuxtools.tmf.component.TmfComponent;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.trace.TmfExperimentSelectedSignal;

/**
 * @author alvaro
 * 
 */
public class StateExperimentManager extends TmfComponent {
	
	// ========================================================================
	// Data
	// =======================================================================
	
	private final Map<String, StateManager> managersByID = new HashMap<String, StateManager>();
	private TmfExperiment fExperiment = null; // one experiment supported

	// ========================================================================
	// Constructors
	// =======================================================================

	/**
	 * package level constructor, creation from factory
	 */
	StateExperimentManager() {
		super();
	}

	// ========================================================================
	// Methods
	// =======================================================================

	/**
	 * Return the Map of unique id to Manager instance
	 * 
	 * @return
	 */
	public Map<String, StateManager> getManagersByID() {
		return managersByID;
	}

	/**
	 * Read all available traces from the nearest checkpoint from start position
	 * to the end of a specified time range
	 * 
	 * @param trange
	 * @param obs
	 * @param transactionID
	 * @param display
	 */
	public void readExperimentTimeWindow(TmfTimeRange trange,
			String transactionID, IStateDataRequestListener listener) {
		Collection<StateManager> mamangers = managersByID.values();
		for (StateManager manager : mamangers) {
			manager.executeDataRequest(trange, transactionID, listener);
		}
	}

	public void readExperiment(String transactionID,
			IStateDataRequestListener listener) {
		// Need someone to listen to the updates as well as an fExperiment
		// loaded.
		if (listener != null && fExperiment != null) {
			TmfTimeRange trange = fExperiment.getTimeRange();
			Collection<StateManager> mamangers = managersByID.values();
			for (StateManager manager : mamangers) {
				manager.executeDataRequest(trange, transactionID, listener);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.linuxtools.tmf.eventlog.ITmfEventLogEventListener#handleEvent
	 * (org.eclipse.linuxtools.tmf.eventlog.ITmfEventLogEvent)
	 */
	@TmfSignalHandler
	public void experimentSelected(TmfExperimentSelectedSignal signal) {
		// TmfExperiment experiment = signal.getExperiment();
		// ITmfTrace[] traces = experiment.getTraces();
		// for (ITmfTrace trace : traces) {
		//			
		// }
		if (signal != null) {
			fExperiment = signal.getExperiment();
			traceSelected(fExperiment);
		}
	}

	/**
	 * A new trace log is opened, notification received from the framework
	 * Notify the new log selection to the state handling manager
	 * 
	 * @param experiment
	 */
	private void traceSelected(TmfExperiment experiment) {
		// TODO: Re-factor when the experiment provides the list of traces per
		// traceId, as well as when the request can be specified at the trace
		// level
		// For the moment it does work for only one trace per experiment.
		String experimentId = experiment.getExperimentId();
		StateManager manager = StateManagerFactory.getManager(experimentId);
		manager.setTraceSelection(experiment);
	}

	/**
	 * @return
	 */
	public TmfTimeRange getExperimentTimeRange() {
		TmfTimeRange timeRangeResult = null;
		if (fExperiment != null) {
			timeRangeResult = fExperiment.getTimeRange();
		}
		return timeRangeResult;
	}

}
