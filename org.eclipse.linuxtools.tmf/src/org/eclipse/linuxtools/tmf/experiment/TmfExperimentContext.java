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

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.trace.ITmfLocation;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.trace.TmfContext;

/**
 * <b><u>TmfExperimentContext</u></b>
 * <p>
 * Implement me. Please.
 */
public class TmfExperimentContext extends TmfContext {

	private ITmfTrace[]  fTraces = new ITmfTrace[0];
	private TmfContext[] fContexts;
	private TmfEvent[]   fEvents;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public TmfExperimentContext(ITmfTrace[] traces, TmfContext[] contexts) {
		super();
		fTraces   = traces;
		fContexts = contexts;
		fEvents   = new TmfEvent[fTraces.length];

		ITmfLocation<?>[] locations = new ITmfLocation[fTraces.length];
		long rank = 0;
		for (int i = 0; i < fTraces.length; i++) {
			if (contexts[i] != null) {
				locations[i] = contexts[i].getLocation();
				rank += contexts[i].getRank();
			}
		}
		
		setLocation(new TmfExperimentLocation(locations));
		setRank(rank);
	}

	public TmfExperimentContext(ITmfTrace[] traces) {
		this(traces, new TmfContext[traces.length]);
	}

	public TmfExperimentContext(TmfExperimentContext other) {
		this(other.fTraces, other.cloneContexts());
		fEvents = other.fEvents;
		setLocation(other.getLocation());
		setRank(other.getRank());
	}

	private TmfContext[] cloneContexts() {
		TmfContext[] contexts = new TmfContext[fContexts.length];
		for (int i = 0; i < fContexts.length; i++)
			contexts[i] = fContexts[i].clone();
		return contexts;
	}

	// ------------------------------------------------------------------------
	// Accessors
	// ------------------------------------------------------------------------

	public ITmfTrace[] getTraces() {
		return fTraces;
	}

	public TmfContext[] getContexts() {
		return fContexts;
	}

	public TmfEvent[] getEvents() {
		return fEvents;
	}

}
