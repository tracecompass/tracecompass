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

package org.eclipse.linuxtools.tmf.signal;

import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace;

/**
 * <b><u>TmfTraceUpdatedSignal</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfTraceUpdatedSignal extends TmfSignal {

	private final ITmfTrace<?> fTrace;
	private final TmfTimeRange fTimeRange;
	
	public TmfTraceUpdatedSignal(Object source, ITmfTrace<?> trace, TmfTimeRange range) {
		super(source);
		fTrace = trace;
		fTimeRange = range;
	}

	public ITmfTrace<?> getTrace() {
		return fTrace;
	}

	public TmfTimeRange getRange() {
		return fTimeRange;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
    @SuppressWarnings("nls")
	public String toString() {
		return "[TmfTraceUpdatedSignal (" + fTrace.toString() + ", " + fTimeRange.toString() + ")]";
	}

}
