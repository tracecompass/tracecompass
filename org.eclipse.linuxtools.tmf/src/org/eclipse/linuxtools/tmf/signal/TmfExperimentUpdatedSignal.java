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

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace;

/**
 * <b><u>TmfExperimentUpdatedSignal</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfExperimentUpdatedSignal extends TmfSignal {

	private final TmfExperiment<? extends TmfEvent> fExperiment;
	private final ITmfTrace fTrace;
	
	public TmfExperimentUpdatedSignal(Object source, TmfExperiment<? extends TmfEvent> experiment, ITmfTrace trace) {
		super(source);
		fExperiment = experiment;
		fTrace = trace;
	}

	public TmfExperiment<? extends TmfEvent> getExperiment() {
		return fExperiment;
	}

	public ITmfTrace getTrace() {
		return fTrace;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[TmfExperimentUpdatedSignal (" + fExperiment.toString() + ", " + fTrace.toString() + ")]";
	}

}
