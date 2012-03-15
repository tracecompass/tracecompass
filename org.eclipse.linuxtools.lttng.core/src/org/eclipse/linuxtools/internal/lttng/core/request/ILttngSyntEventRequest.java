/*******************************************************************************
 * Copyright (c) 20010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon (alvsan09@gmail.com) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.core.request;

import org.eclipse.linuxtools.internal.lttng.core.event.LttngSyntheticEvent;
import org.eclipse.linuxtools.tmf.core.component.TmfEventProvider;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest;

public interface ILttngSyntEventRequest extends ITmfDataRequest<LttngSyntheticEvent> {

	/**
	 * Trigger the start to process this request right after the notification to
	 * the interested listeners
	 * 
	 * @param provider
	 * @param broadcast
	 *            true: All views, false: only to registered listeners
	 */
	public abstract void startRequestInd(
			TmfEventProvider<LttngSyntheticEvent> provider);

	/**
	 * to be called by the handleCompletion in superclass method, notifies the
	 * interested listeners. i.e. if the request start indicated broadcast, the
	 * completion will also be broadcasted otherwise only registered listeners
	 * will be notified.
	 */
	public abstract void notifyCompletion();

	public abstract void notifyStarting();

	public abstract TmfTimeRange getExperimentTimeRange();

	/**
	 * @param numOfEvents
	 *            the numOfEvents to set
	 */
	public abstract void setSynEventCount(Long numOfEvents);

	/**
	 * @return the numOfEvents
	 */
	public abstract Long getSynEventCount();

	/**
	 * @param clearAllData
	 *            indicates the need to clear all previous data e.g. a new
	 *            experiment selection
	 */
	public abstract void setclearDataInd(boolean clearAllData);

	/**
	 * Returns indication - clearing of all existing data model is required e.g
	 * from the selection of a new experiment
	 * 
	 * @return
	 */
	public abstract boolean isclearDataInd();

	/**
	 * @return <p>
	 *         The associated source of the request
	 *         </p>
	 *         <p>
	 *         Returns null if no source object has been previously set
	 *         </p>
	 * 
	 */
	public abstract Object getSource();

	/**
	 * Sets a reference to the source of this request
	 * 
	 * @param source
	 */
	public abstract void setSource(Object source);


	/**
	 * Return the time range associated to this request
	 * 
	 * @return
	 */
	public abstract TmfTimeRange getRange();
	
	public abstract String getExperimentName();

}