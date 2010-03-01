/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.experiment;

import java.util.Vector;

import org.eclipse.linuxtools.tmf.component.ITmfContext;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.trace.TmfTraceContext;

/**
 * <b><u>TmfExperimentContext</u></b>
 * <p>
 * Implement me. Please.
 */
public class TmfExperimentContext implements ITmfContext, Cloneable {

	private ITmfTrace[]       fTraces = new ITmfTrace[0];	// The set of traces
	private TmfTraceContext[] fContexts;					// The set of trace contexts
	private TmfEvent[]        fEvents;

	public TmfExperimentContext(Vector<ITmfTrace> traces) {
		fTraces   = traces.toArray(fTraces);
		fContexts = new TmfTraceContext[fTraces.length];
		fEvents   = new TmfEvent[fTraces.length];
	}

	@Override
	public TmfExperimentContext clone() {
		try {
			return (TmfExperimentContext) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public ITmfTrace[] getTraces() {
		return fTraces;
	}

	public TmfTraceContext[] getContexts() {
		return fContexts;
	}

	public TmfTraceContext[] cloneContexts() {
		TmfTraceContext[] contexts = new TmfTraceContext[fContexts.length];
		for (int i = 0; i < fContexts.length; i++)
			contexts[i] = fContexts[i].clone();
		return contexts;
	}

	public TmfEvent[] getEvents() {
		return fEvents;
	}

}
