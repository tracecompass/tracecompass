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

package org.eclipse.linuxtools.internal.lttng.core.state.experiment;

import org.eclipse.linuxtools.internal.lttng.core.event.LttngEvent;
import org.eclipse.linuxtools.internal.lttng.core.model.LTTngTreeNode;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;

public interface IStateExperimentManager {

//	/**
//	 * Read all available traces from the nearest checkpoint from start position
//	 * to the end of a specified time range. One request per trace in the
//	 * experiment will be triggered
//	 * 
//	 * @param trange
//	 * @param obs
//	 * @param transactionID
//	 * @param display
//	 * @return
//	 */
//	public abstract ILttngSyntEventRequest readExperimentTimeWindow(
//			TmfTimeRange trange,
//			Object origin, IRequestStatusListener listener,
//			ITransEventProcessor processor);
//
//	/**
//	 * Read available traces from the Experiment start time, One request per
//	 * trace in the Experiment
//	 * 
//	 * @param source
//	 * @param listener
//	 * @param processor
//	 */
//	public abstract void readExperiment(Object source,
//			IRequestStatusListener listener, ITransEventProcessor processor);

	/**
	 * A new Experiment selected, notification received from the framework
	 * Notify the new experiment selection to the state handling managers
	 * 
	 * @param source
	 * @param experiment
	 */
	public abstract void experimentSelected_prep(
			TmfExperiment<LttngEvent> experiment);

	/**
	 * @param source
	 * @param experiment
	 */
	public void experimentSelected(Object source,
			TmfExperiment<LttngEvent> experiment);

	/**
	 * @return
	 */
	public abstract TmfTimeRange getExperimentTimeRange();

	/**
	 * @return
	 */
	public abstract LTTngTreeNode getSelectedExperiment();

	/**
	 * Wait for request completion upon experiment selection
	 * 
	 * @param wait
	 */
	public abstract void waitForCompletion(boolean wait);

}