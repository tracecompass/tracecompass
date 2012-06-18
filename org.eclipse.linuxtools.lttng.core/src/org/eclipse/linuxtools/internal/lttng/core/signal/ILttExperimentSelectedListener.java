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

import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentRangeUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;

/**
 * @author alvaro
 *
 */
public interface ILttExperimentSelectedListener {

	/**
	 *
	 * @param source
	 * @param experiment
	 */
	public void experimentSelected(Object source, TmfExperiment experiment);

	/**
	 * @param signal
	 */
	public void experimentRangeUpdated(TmfExperimentRangeUpdatedSignal signal);

}
