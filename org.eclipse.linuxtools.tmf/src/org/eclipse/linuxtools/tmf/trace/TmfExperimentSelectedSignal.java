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
 * <b><u>TmfExperimentSelectedSignal</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfExperimentSelectedSignal extends TmfSignal {

	private final TmfExperiment fExperiment;
	
	public TmfExperimentSelectedSignal(Object source, TmfExperiment experiment) {
		super(source);
		fExperiment = experiment;
	}

	public TmfExperiment getExperiment() {
		return fExperiment;
	}
}
