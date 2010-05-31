/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon (alvsan09@gmail.com) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.lttng;

import org.eclipse.linuxtools.lttng.control.LttngCoreProviderFactory;
import org.eclipse.linuxtools.lttng.state.experiment.StateManagerFactory;

/**
 * @author alvaro
 *
 */
public class LttngFactory {
	static void init() {
		// Make sure the experiment component is ready to listen to experiment
		// selections
		StateManagerFactory.getExperimentManager();
		// The Synthetic event provider must also be notified of selections, in
		// order to keep the sychronization orders from TMF, this element is
		// shared for all synthetic event requests
		LttngCoreProviderFactory.getEventProvider();

		// start debugging as per .options
		TraceDebug.init();
	}
}
