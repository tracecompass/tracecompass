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
 *   Francois Chouinard - Updated as per TMF Trace Model 1.0
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.trace;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;

/**
 * <b><u>ITmfEventParser</u></b>
 * <p>
 */
public interface ITmfEventParser<T extends ITmfEvent> {

	/**
	 * Parses the trace event referenced by the context.
	 * 
	 * @param context
	 * @return
	 */
	public ITmfEvent parseEvent(ITmfContext context);

}
