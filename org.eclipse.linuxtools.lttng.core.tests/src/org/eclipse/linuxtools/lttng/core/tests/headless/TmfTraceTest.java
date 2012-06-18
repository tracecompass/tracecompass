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
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;

@SuppressWarnings("nls")
public class TmfTraceTest extends TmfEventRequest {

    public TmfTraceTest(final Class<? extends TmfEvent> dataType, final TmfTimeRange range, final int nbRequested) {
        super(dataType, range, nbRequested, 1);
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
    public static TmfExperiment fExperiment = null;


    public static void main(final String[] args) {

        try {
            // OUr experiment will contains ONE trace
            final ITmfTrace[] traces = new ITmfTrace[1];
            traces[0] = new LTTngTrace(null, TRACE_PATH);
            // Create our new experiment
            fExperiment = new TmfExperiment(LttngEvent.class, "Headless", traces);


            // Create a new time range from -infinity to +infinity
            //	That way, we will get "everything" in the trace
            final LttngTimestamp ts1 = new LttngTimestamp(Long.MIN_VALUE);
            final LttngTimestamp ts2 = new LttngTimestamp(Long.MAX_VALUE);
            final TmfTimeRange tmpRange = new TmfTimeRange(ts1, ts2);


            // We will issue a request for each "pass".
            // TMF will then process them synchonously
            TmfTraceTest request = null;
            for ( int x=0; x<NB_OF_PASS; x++ ) {
                request = new TmfTraceTest(LttngEvent.class, tmpRange, Integer.MAX_VALUE );
                fExperiment.sendRequest(request);
                nbPassDone++;
            }
        }
        catch (final NullPointerException e) {
            // Silently dismiss Null pointer exception
            // The only way to "finish" the threads in TMF is by crashing them with null
        }
        catch (final Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void handleData(final ITmfEvent event) {
        super.handleData(event);
        if ( (event != null) && (PARSE_EVENTS) ) {
            event.getContent().getFields();

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
        catch (final Exception e) {}
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
