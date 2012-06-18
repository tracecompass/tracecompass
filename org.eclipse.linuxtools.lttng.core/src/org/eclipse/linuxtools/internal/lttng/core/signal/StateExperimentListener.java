/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alvaro Sanchez-Leon (alvsan09@gmail.com) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.core.signal;

import org.eclipse.linuxtools.tmf.core.component.TmfComponent;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentRangeUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentSelectedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;

/**
 * @author alvaro
 *
 */
public class StateExperimentListener extends TmfComponent {

	private final ILttExperimentSelectedListener fhandler;

	public StateExperimentListener(String name, ILttExperimentSelectedListener handler) {
		super(name);
		fhandler = handler;
	}

	@TmfSignalHandler
	public void experimentSelected(TmfExperimentSelectedSignal signal) {
		TmfExperiment experiment = signal.getExperiment();

		// notify handler
		fhandler.experimentSelected(signal.getSource(), experiment);
	}

	@TmfSignalHandler
	public void experimentRangeUpdated(TmfExperimentRangeUpdatedSignal signal) {
		// notify handler
		fhandler.experimentRangeUpdated(signal);
	}
}
