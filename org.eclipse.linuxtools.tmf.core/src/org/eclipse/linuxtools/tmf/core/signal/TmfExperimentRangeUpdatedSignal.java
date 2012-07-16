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
 * Experiment time range update
 * 
 * @version 1.0
 * @author Francois Chouinard
 */
public class TmfExperimentRangeUpdatedSignal extends TmfSignal {

	private final TmfExperiment<? extends ITmfEvent> fExperiment;
	private final TmfTimeRange fTimeRange;

    /**
     * Constructor
     *
     * @param source
     *            Object sending this signal
     * @param experiment
     *            Experiment whose range was updated
     * @param range
     *            The new time range of the experiment
     */
    public TmfExperimentRangeUpdatedSignal(Object source,
            TmfExperiment<? extends ITmfEvent> experiment, TmfTimeRange range) {
            // , ITmfTrace trace) {
        super(source);
        fExperiment = experiment;
        fTimeRange = range;
    }

	/**
	 * @return The experiment
	 */
	public TmfExperiment<? extends ITmfEvent> getExperiment() {
		return fExperiment;
	}

	/**
	 * @return The time range
	 */
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
