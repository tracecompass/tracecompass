/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.signal;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;

/**
 * <b><u>TmfExperimentRangeUpdatedSignal</u></b>
 * <p>
 */
public class TmfExperimentRangeUpdatedSignal extends TmfSignal {

	private final TmfExperiment<? extends ITmfEvent> fExperiment;
	private final TmfTimeRange fTimeRange;
	
	public TmfExperimentRangeUpdatedSignal(Object source, TmfExperiment<? extends ITmfEvent> experiment, TmfTimeRange range) { // , ITmfTrace trace) {
		super(source);
		fExperiment = experiment;
		fTimeRange = range;
	}

	public TmfExperiment<? extends ITmfEvent> getExperiment() {
		return fExperiment;
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
		return "[TmfExperimentRangeUpdatedSignal (" + fExperiment.toString() + ", " + fTimeRange.toString() + ")]";
	}

}
