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
package org.eclipse.linuxtools.internal.lttng.core.state.resource;

import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * <b><u>ILttngStateContext</u></b>
 * <p>Provides context information of a State system instance</p>
 *
 */

/**
 * @author alvaro
 *
 */
public interface ILttngStateContext {

	/**
	 * Returns the number of CPUs available in the Trace
	 *
	 * @return
	 */
	public int getNumberOfCpus();

	/**
	 * The current time range window covering the Trace
	 *
	 * @return
	 */
	public TmfTimeRange getTraceTimeWindow();

	/**
	 * The current time range window covering the Experiment
	 *
	 * @return
	 */
	public TmfTimeRange getExperimentTimeWindow();

	/**
	 * Returns the name of the associated experiment
	 *
	 * @return
	 */
	public String getExperimentName();

	/**
	 * Returns the corresponding trace id.
	 *
	 * @return
	 */
	public String getTraceId();

	/**
	 * Returns the corresponding trace id reference
	 *
	 * @return
	 */
	public ITmfTrace getTraceIdRef();

	/**
	 *  Returns Trace Identifier
	 *  @return Trace Identifier
	 */
	public long getIdentifier();

}
