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

package org.eclipse.linuxtools.tmf.request;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.trace.TmfTraceStub;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace.TmfTraceContext;

/**
 * <b><u>TmfRequestHandlerStub</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfRequestHandlerStub implements ITmfRequestHandler<TmfEvent> {

    // The test file
    private static final String TEST_STREAM = "M-Test-100K";

    // A constant to limit the number of events for the tests
    public static final int MAX_GENERATED_EVENTS = 1000;

    private TmfTraceStub fTrace;

    public TmfRequestHandlerStub() throws IOException {
    	String directory = new File(".").getCanonicalPath() + File.separator + "testfiles";
    	String filename  = directory + File.separator + TEST_STREAM;
        fTrace = new TmfTraceStub(filename);
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.eventlog.ITmfRequestHandler#processRequest(org.eclipse.linuxtools.tmf.eventlog.TmfEventRequest, boolean)
     */
    public void processRequest(final TmfDataRequest<TmfEvent> request, boolean waitForCompletion) {

        Thread thread = new Thread() {
            @Override
            public void run() {
                TmfTimestamp startTime = request.getRange().getStartTime();
                TmfTimestamp endTime   = request.getRange().getEndTime();
                int blockSize = request.getBlockize();

                int nbRequestedEvents = request.getNbRequestedItems();
                if (nbRequestedEvents <= 0) {
                    nbRequestedEvents = MAX_GENERATED_EVENTS;
                }

                Vector<TmfEvent> events = new Vector<TmfEvent>();
                int nbEvents = 0;
            	TmfTraceContext context = new TmfTraceContext(null);
                TmfEvent event = fTrace.getEvent(context, startTime);
                while (!request.isCancelled() && nbEvents < nbRequestedEvents &&
                       event != null && event.getTimestamp().compareTo(endTime, false) <= 0 )
                {
                    events.add(event);
                    if (++nbEvents % blockSize == 0) {
                    	TmfEvent[] result = new TmfEvent[events.size()];
                    	events.toArray(result);
                    	request.setData(result);
                        request.handleData();
                        events.removeAllElements();
                    }
                    event = fTrace.getNextEvent(context);
                }
            	TmfEvent[] result = new TmfEvent[events.size()];
            	events.toArray(result);

            	request.setData(result);
                request.handleData();
                request.done();
            }
        };
        thread.start();

        if (waitForCompletion) {
            request.waitForCompletion();
        }
    }

}
