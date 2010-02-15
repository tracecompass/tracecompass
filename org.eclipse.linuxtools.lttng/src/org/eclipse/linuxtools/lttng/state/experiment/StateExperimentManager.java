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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.lttng.state.IStateDataRequestListener;
import org.eclipse.linuxtools.lttng.state.StateDataRequest;
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
	public StateDataRequest readExperimentTimeWindow(TmfTimeRange trange,
			String transactionID, IStateDataRequestListener listener) {
		StateDataRequest request = null;
		if (fExperiment != null) {
			String id = fExperiment.getExperimentId();
			StateManager manager = managersByID.get(id);
			if (manager != null) {
				// TODO: A loop to request data for each trace needs to be used
				// here when multiple traces are supported.
				request = manager.executeDataRequest(trange, transactionID,
						listener);
			}
		}

		return request;
	}

	public void readExperiment(String transactionID,
			IStateDataRequestListener listener) {
		// Need someone to listen to the updates as well as an fExperiment
		// loaded.
		if (listener != null && fExperiment != null) {
			TmfTimeRange trange = fExperiment.getTimeRange();
			String experimentId = fExperiment.getExperimentId();

			// FIXME: there should be an id field available at the trace level
			// to be fixed with the support of multiple files.
			// We also need to iterate over the traces in the Experiment and
			// execute a data Request on each of them
			// This is also on hold till the request can be performed at a trace
			// level.
			// ITmfTrace[] fTraces = fExperiment.getTraces();
			// for (int i=0; i < fTraces.length; i++) {
			StateManager manager = StateManagerFactory.getManager(experimentId);
			manager.executeDataRequest(trange, transactionID, listener);
			// }
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
	 * A new Experiment selected, notification received from the framework
	 * Notify the new log selection to the state handling managers
	 * 
	 * @param experiment
	 */
	private void traceSelected(TmfExperiment experiment) {
		// TODO: Re-factor when multiple traces are supported
		// traceId, as well as when the request can be specified at the trace
		// level
		// For the moment it does work for only one trace per experiment.
		String experimentId = experiment.getExperimentId();
		StateManager manager = StateManagerFactory.getManager(experimentId);
		// TODO: clearAllData shall not be applied to all manager calls below
		// since that would clean all data loaded within previous iterations in
		// the future loop. i.e. It can be applied to first manager in the loop.
		boolean clearAllData = true;
		manager.setTraceSelection(experiment, clearAllData);
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
