/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.trace;

import org.eclipse.linuxtools.tmf.signal.TmfSignal;

/**
 * <b><u>TmfTraceUpdatedEvent</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfTraceUpdatedSignal extends TmfSignal {

	private final TmfTrace fTrace;
	
	public TmfTraceUpdatedSignal(Object source, TmfTrace trace) {
		super(source);
		fTrace = trace;
	}

	public TmfTrace getTrace() {
		return fTrace;
	}
}
