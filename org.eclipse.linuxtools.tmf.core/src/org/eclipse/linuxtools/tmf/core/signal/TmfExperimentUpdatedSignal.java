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

package org.eclipse.linuxtools.tmf.core.signal;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.experiment.TmfExperiment;

/**
 * <b><u>TmfExperimentUpdatedSignal</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfExperimentUpdatedSignal extends TmfSignal {

	private final TmfExperiment<? extends ITmfEvent> fExperiment;
//	private final ITmfTrace fTrace;
	
	public TmfExperimentUpdatedSignal(Object source, TmfExperiment<? extends ITmfEvent> experiment) { // , ITmfTrace trace) {
		super(source);
		fExperiment = experiment;
//		fTrace = trace;
	}

	public TmfExperiment<? extends ITmfEvent> getExperiment() {
		return fExperiment;
	}

//	public ITmfTrace getTrace() {
//		return fTrace;
//	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
    @SuppressWarnings("nls")
	public String toString() {
		return "[TmfExperimentUpdatedSignal (" + fExperiment.toString() /*+ ", " + fTrace.toString()*/ + ")]";
	}

}
