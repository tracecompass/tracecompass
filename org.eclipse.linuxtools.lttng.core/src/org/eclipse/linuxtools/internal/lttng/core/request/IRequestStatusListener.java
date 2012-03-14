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
package org.eclipse.linuxtools.internal.lttng.core.request;



/**
 * Interface to be used to receive notification of Processing start and
 * completed
 * 
 * @author alvaro
 * 
 */
public interface IRequestStatusListener {
	// ========================================================================
	// Methods
	// ========================================================================
	
	/**
	 * 
	 * @param request
	 * @return
	 */
	public void processingStarted(RequestStartedSignal request);

	/**
	 * @param signal
	 */
	public void processingCompleted(RequestCompletedSignal signal);
}
