/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.trace;

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.experiment.TmfExperimentContext;
import org.eclipse.linuxtools.tmf.experiment.TmfExperimentLocation;
import org.eclipse.linuxtools.tmf.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.trace.TmfContext;

/**
 * <b><u>LTTngExperiment</u></b>
 * <p>
 * Temporary class to resolve a basic incompatibility between TMF and LTTng.
 * <p>
 */
public class LTTngExperiment<T extends TmfEvent> extends TmfExperiment<T> implements ITmfTrace {

	private static final int DEFAULT_INDEX_PAGE_SIZE = 5000;

	// ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * @param type
     * @param id
     * @param traces
     * @param epoch
     * @param indexPageSize
     */
    public LTTngExperiment(Class<T> type, String id, ITmfTrace[] traces, TmfTimestamp epoch, int indexPageSize) {
        this(type, id, traces, TmfTimestamp.Zero, indexPageSize, false);
	}

    public LTTngExperiment(Class<T> type, String id, ITmfTrace[] traces, TmfTimestamp epoch, int indexPageSize, boolean preIndexExperiment) {
    	super(type, id, traces, epoch, indexPageSize, preIndexExperiment);
	}

    /**
     * @param type
     * @param id
     * @param traces
     */
    public LTTngExperiment(Class<T> type, String id, ITmfTrace[] traces) {
        this(type, id, traces, TmfTimestamp.Zero, DEFAULT_INDEX_PAGE_SIZE);
    }

    /**
     * @param type
     * @param id
     * @param traces
     * @param indexPageSize
     */
    public LTTngExperiment(Class<T> type, String id, ITmfTrace[] traces, int indexPageSize) {
        this(type, id, traces, TmfTimestamp.Zero, indexPageSize);
    }
    
    public LTTngExperiment(LTTngExperiment<T> other) {
    	super(other.getName() + "(clone)", other.fType);
    	
    	fEpoch         = other.fEpoch;
    	fIndexPageSize = other.fIndexPageSize;
    	
    	fTraces = new ITmfTrace[other.fTraces.length];
    	for (int trace = 0; trace < other.fTraces.length; trace++) {
    		fTraces[trace] = other.fTraces[trace].createTraceCopy();
    	}
    	
    	fNbEvents  = other.fNbEvents;
    	fTimeRange = other.fTimeRange;
    }
    
	@Override
	public LTTngExperiment<T> createTraceCopy() {
		LTTngExperiment<T> experiment = new LTTngExperiment<T>(this);
		TmfSignalManager.deregister(experiment);
		return experiment;
	}
    
	// ------------------------------------------------------------------------
    // ITmfTrace trace positioning
    // ------------------------------------------------------------------------

	@Override
	public synchronized TmfEvent getNextEvent(TmfContext context) {

		// Validate the context
		if (!(context instanceof TmfExperimentContext)) {
			return null;	// Throw an exception?
		}

		if (!context.equals(fExperimentContext)) {
//    		Tracer.trace("Ctx: Restoring context");
			seekLocation(context.getLocation());
		}
		
		TmfExperimentContext expContext = (TmfExperimentContext) context;

//		dumpContext(expContext, true);

		// If an event was consumed previously, get the next one from that trace
		int lastTrace = expContext.getLastTrace();
		if (lastTrace != TmfExperimentContext.NO_TRACE) {
		    TmfContext traceContext = expContext.getContexts()[lastTrace];
			expContext.getEvents()[lastTrace] = expContext.getTraces()[lastTrace].getNextEvent(traceContext);
			expContext.setLastTrace(TmfExperimentContext.NO_TRACE);
		}

		// Scan the candidate events and identify the "next" trace to read from
		TmfEvent eventArray[] =  expContext.getEvents();
		if (eventArray == null) {
			return null;
		}
		int trace = TmfExperimentContext.NO_TRACE;
		TmfTimestamp timestamp = TmfTimestamp.BigCrunch;
		if (eventArray.length == 1) {
			timestamp = eventArray[0].getTimestamp();
			trace = 0;
		} else {
			for (int i = 0; i < eventArray.length; i++) {
				TmfEvent event = eventArray[i];
				if (event != null && event.getTimestamp() != null) {
					TmfTimestamp otherTS = event.getTimestamp();
					if (otherTS.compareTo(timestamp, true) < 0) {
						trace = i;
						timestamp = otherTS;
					}
				}
			}
		}

		// Update the experiment context and set the "next" event
		TmfEvent event = null;
		if (trace != TmfExperimentContext.NO_TRACE) {
//	        updateIndex(expContext, timestamp);

	        TmfContext traceContext = expContext.getContexts()[trace];
	        TmfExperimentLocation expLocation = (TmfExperimentLocation) expContext.getLocation();
	        expLocation.getLocation()[trace] = traceContext.getLocation();

	        updateIndex(expContext, timestamp);

	        expLocation.getRanks()[trace] = traceContext.getRank();
			expContext.setLastTrace(trace);
			expContext.updateRank(1);
			event = expContext.getEvents()[trace];
		}

//		if (event != null) {
//    		Tracer.trace("Exp: " + (expContext.getRank() - 1) + ": " + event.getTimestamp().toString());
//    		dumpContext(expContext, false);
//    		Tracer.trace("Ctx: Event returned= " + event.getTimestamp().toString());
//		}

		return event;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
    @SuppressWarnings("nls")
	public String toString() {
		return "[LTTngExperiment (" + getName() + ")]";
	}

}
