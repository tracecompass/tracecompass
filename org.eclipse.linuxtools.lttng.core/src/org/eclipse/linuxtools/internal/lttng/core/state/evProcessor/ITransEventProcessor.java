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
 * 	 Michel Dagenais (michel.dagenais@polymtl.ca) - Reference C implementation, used with permission
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.core.state.evProcessor;

/**
 * @author alvaro
 *
 */
public interface ITransEventProcessor extends IBaseEventProcessor,
		ILttngEventProcessor {

	/**
	 * @return the eventCount
	 */
	public Long getBeforeEventCount();

	/**
	 * @return the stateUpdateCount
	 */
	public Long getStateUpdateCount();

	/**
	 * @return the count of filtered out events e.g. event received from a trace
	 *         not matching the trace state system
	 */
	public Long getFilteredOutEventCount();

}
