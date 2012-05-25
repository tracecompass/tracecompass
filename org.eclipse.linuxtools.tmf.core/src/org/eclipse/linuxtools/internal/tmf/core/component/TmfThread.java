/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.component;

import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest.ExecutionType;

/**
 * Utility class to add an execution class to a simple Java thread
 * 
 * @version 1.0
 * @author Francois Chouinard
 */
public class TmfThread extends Thread {

	private final ExecutionType fExecType;
	
	public TmfThread(ExecutionType execType) {
		fExecType = execType;
	}
	
	public ExecutionType getExecType() {
		return fExecType;
	}

	public void cancel() {
	}

}
