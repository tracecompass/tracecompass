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
package org.eclipse.linuxtools.lttng.state.model;

import org.eclipse.linuxtools.tmf.event.TmfTimeRange;

/**
 * <b><u>ILttngStateModelInput</u></b>
 * <p>Interface providing the data needed by the State model
 * 
 */

/**
 * @author alvaro
 *
 */
public interface ILttngStateInputRef {
	
	public int getNumberOfCpus();
	public TmfTimeRange getTraceTimeWindow();
	public String getTraceId();
	public TmfTimeRange getExperimentTimeWindow();

}
