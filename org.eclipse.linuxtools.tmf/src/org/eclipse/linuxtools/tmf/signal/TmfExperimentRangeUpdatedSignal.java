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

package org.eclipse.linuxtools.tmf.signal;

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.experiment.TmfExperiment;

/**
 * <b><u>TmfExperimentRangeUpdatedSignal</u></b>
 * <p>
 */
public class TmfExperimentRangeUpdatedSignal extends TmfSignal {

	private final TmfExperiment<? extends TmfEvent> fExperiment;
	private final TmfTimeRange fTimeRange;
	
	public TmfExperimentRangeUpdatedSignal(Object source, TmfExperiment<? extends TmfEvent> experiment, TmfTimeRange range) { // , ITmfTrace trace) {
		super(source);
		fExperiment = experiment;
		fTimeRange = range;
	}

	public TmfExperiment<? extends TmfEvent> getExperiment() {
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
