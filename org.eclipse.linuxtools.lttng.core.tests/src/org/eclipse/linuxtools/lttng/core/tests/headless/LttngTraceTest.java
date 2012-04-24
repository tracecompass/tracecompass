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
import org.eclipse.linuxtools.internal.lttng.core.event.LttngLocation;
import org.eclipse.linuxtools.internal.lttng.core.event.LttngTimestamp;
import org.eclipse.linuxtools.internal.lttng.core.trace.LTTngTextTrace;
import org.eclipse.linuxtools.internal.lttng.core.trace.LTTngTrace;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;


@SuppressWarnings("nls")
public class LttngTraceTest {

    /**
     * @param args
     */
    public static void main(final String[] args) {

        // Path of the trace
        final String TRACE_PATH = "/home/francois/Desktop/Workspaces/LTTngTraces/trace_2GB";
        // *** Change to true to use the "fake" LttngTextTrace instead of LTTngTrace
        // To use this, you need a ".txt" trace.
        // You can get it using LTTv with the command "lttv -m textDump -t /tmp/sometrace > mytrace.txt"
        final boolean USE_TEXT_TRACE = false;

        // *** Change this to run several time over the same trace
        final int NB_OF_PASS = 1;

        // *** Change this to true to parse all the events in the trace
        //	Otherwise, events are just read
        final boolean PARSE_EVENTS = true;


        // Work variables
        TmfTrace<LttngEvent> tmptrace = null;
        LttngEvent tmpevent = null;
        ITmfContext tmpContext = null;
        Long nbEvent = 0L;

        try {
            // ** Use TextTrace (slow!) if it was asked
            if ( USE_TEXT_TRACE )
                tmptrace = new LTTngTextTrace(null, TRACE_PATH, true);
            else
                tmptrace = new LTTngTrace(null, TRACE_PATH, null, true, true);

            final LttngTimestamp tmpTime = new LttngTimestamp(0L);
            tmpContext = new TmfContext(new LttngLocation(0L), 0);


            final long startTime = System.nanoTime();
            System.out.println("Start: " + startTime);
            for ( int nb=0; nb<NB_OF_PASS; nb++) {

                // Seek to the beginning of the trace
                tmpContext = tmptrace.seekEvent( tmpTime  );
                tmpevent = (LttngEvent)tmptrace.readNextEvent(tmpContext);

                while ( tmpevent != null ) {
                    tmpevent = (LttngEvent)tmptrace.readNextEvent(tmpContext);

                    // Parse the events if it was asked
                    if ( (tmpevent != null) && (PARSE_EVENTS) )
                        tmpevent.getContent().getFields();

                    nbEvent++;
                }
            }

            System.out.println("NB events : " + nbEvent);

            final long endTime = System.nanoTime();
            final long elapsed = endTime - startTime;
            System.out.println("End: " + endTime);
            System.out.println("Elapsed: " + elapsed + ", Average: " + (elapsed/nbEvent) + "ns/evt");

        }
        catch (final Exception e) {
            e.printStackTrace();
        }

    }

}
