package org.eclipse.linuxtools.lttng.core.tests.headless;
/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   William Bourque (wbourque@gmail.com) - Initial API and implementation
 *******************************************************************************/

import org.eclipse.linuxtools.internal.lttng.core.event.LttngEvent;
import org.eclipse.linuxtools.internal.lttng.core.event.LttngTimestamp;
import org.eclipse.linuxtools.internal.lttng.core.trace.LTTngTrace;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

@SuppressWarnings("nls")
public class TmfTraceTest extends TmfEventRequest<LttngEvent> {
    
    @SuppressWarnings("unchecked")
	public TmfTraceTest(Class<? extends TmfEvent> dataType, TmfTimeRange range, int nbRequested) {
        super((Class<LttngEvent>)dataType, range, nbRequested, 1);
    }
    
    
    // Path of the trace
    public static final String TRACE_PATH = "/home/william/trace-614601events-nolost-newformat";
    
    // *** Change this to run several time over the same trace
    public static final int NB_OF_PASS = 1;
    
    // *** Change this to true to parse all the events in the trace
    //	Otherwise, events are just read
    public final boolean PARSE_EVENTS = true;
    
    
    // Work variables
    public static int nbEvent = 0;
    public static int nbPassDone = 0;
    public static TmfExperiment<LttngEvent> fExperiment = null;
    
    
	public static void main(String[] args) {
		
		try {
			// OUr experiment will contains ONE trace
        	@SuppressWarnings("unchecked")
			ITmfTrace<LttngEvent>[] traces = new ITmfTrace[1];
    		traces[0] = new LTTngTrace("", TRACE_PATH);
        	// Create our new experiment
            fExperiment = new TmfExperiment<LttngEvent>(LttngEvent.class, "Headless", traces);
            
            
            // Create a new time range from -infinity to +infinity
            //	That way, we will get "everything" in the trace
            LttngTimestamp ts1 = new LttngTimestamp(Long.MIN_VALUE);
            LttngTimestamp ts2 = new LttngTimestamp(Long.MAX_VALUE);
            TmfTimeRange tmpRange = new TmfTimeRange(ts1, ts2);
            
            
            // We will issue a request for each "pass".
            // TMF will then process them synchonously
            TmfTraceTest request = null;
            for ( int x=0; x<NB_OF_PASS; x++ ) {
                request = new TmfTraceTest(LttngEvent.class, tmpRange, Integer.MAX_VALUE );
        		fExperiment.sendRequest(request);
        		nbPassDone++;
            }
        }
		catch (NullPointerException e) {
			// Silently dismiss Null pointer exception
			// The only way to "finish" the threads in TMF is by crashing them with null
		}
		catch (Exception e) {
            e.printStackTrace();
        }

	}

	@Override
    public void handleData(LttngEvent event) {
		super.handleData(event);
        if ( (event != null) && (PARSE_EVENTS) ) {
            ((LttngEvent) event).getContent().getFields();
            
            // *** Uncomment the following to print the parsed content
            // Warning : this is VERY intensive
			//
            //System.out.println((LttngEvent)evt[0]);
            //System.out.println(((LttngEvent)evt[0]).getContent());
            
            nbEvent++;
        }
    }
	
    @Override
    public void handleCompleted() {
            if ( nbPassDone >= NB_OF_PASS ) {
                try {
                	System.out.println("Nb events : " + nbEvent);
                	
                    fExperiment.sendRequest(null);
               }
               catch (Exception e) {}
            }
    }
    
    @Override
    public void handleSuccess() {
    }
    
    @Override
    public void handleFailure() {
    }
    
    @Override
    public void handleCancel() {
    }

}
