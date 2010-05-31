/*******************************************************************************
 * Copyright (c) 2009,2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon (alvsan09@gmail.com) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.control;

import org.eclipse.linuxtools.lttng.event.LttngSyntheticEvent;

/**
 * @author alvaro
 *
 */
public class LttngCoreProviderFactory {

	private static LttngSyntheticEventProvider fSynEventProvider = null;

	public static LttngSyntheticEventProvider getEventProvider() {
		// create if necessary
		if (fSynEventProvider == null) {
			fSynEventProvider = new LttngSyntheticEventProvider(
					LttngSyntheticEvent.class);
		}

		return fSynEventProvider;
	}
}
