/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard (fchouinard@gmail.com) - Initial API and implementation
 *******************************************************************************/


package org.eclipse.linuxtools.tmf.eventlog;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;

/**
 * <b><u>TmfRequestHandlerStub</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfRequestHandlerStub implements ITmfRequestHandler {

    // The test file
    String filename = "Test-100K";

    // A constant to limit the number of events for the tests
    public static final int MAX_GENERATED_EVENTS = 1000;

    private ITmfEventParser fParser;
    private TmfEventStreamStub fStream;

    public TmfRequestHandlerStub() throws FileNotFoundException {
        fParser = new TmfEventParserStub();
        fStream = new TmfEventStreamStub(filename, fParser);
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.eventlog.ITmfRequestHandler#processRequest(org.eclipse.linuxtools.tmf.eventlog.TmfEventRequest, boolean)
     */
    @Override
    public void process(final TmfEventRequest request, boolean waitForCompletion) {

        Thread thread = new Thread() {
            @Override
            public void run() {
                TmfTimestamp startTime = request.getRange().getStartTime();
                TmfTimestamp endTime   = request.getRange().getEndTime();
                int blockSize = request.getBlockize();

                int nbRequestedEvents = request.getNbRequestedEvents();
                if (nbRequestedEvents <= 0) {
                    nbRequestedEvents = MAX_GENERATED_EVENTS;
                }

                Vector<TmfEvent> events = new Vector<TmfEvent>();
                int nbEvents = 0;
                try {
                    TmfEvent event = fStream.seek(startTime);
                    while (!request.isCancelled() && nbEvents < nbRequestedEvents &&
                           event != null && event.getTimestamp().compareTo(endTime, false) <= 0 )
                    {
                        events.add(event);
                        if (++nbEvents % blockSize == 0) {
                            request.newEvents(events);
                            events.removeAllElements();
                        }
                        event = fStream.getNextEvent();
                    }
                    request.newEvents(events);
                    request.done();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };
        thread.start();

        if (waitForCompletion) {
            request.waitForCompletion();
        }
    }

}
