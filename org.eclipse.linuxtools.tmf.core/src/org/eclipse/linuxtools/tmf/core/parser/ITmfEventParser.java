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

package org.eclipse.linuxtools.tmf.core.parser;

import java.io.IOException;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * <b><u>ITmfEventParser</u></b>
 * <p>
 */
public interface ITmfEventParser<T extends ITmfEvent> {

    /**
     * @return a parsed event
     * @throws IOException 
     */
	/**
	 * Parses the trace event referenced by the context.
	 * 
	 * @param trace the event stream
	 * @param context the trace context
	 * @return the parsed event
	 * @throws IOException
	 */
	public ITmfEvent parseNextEvent(ITmfTrace<T> trace, ITmfContext context) throws IOException;
}
