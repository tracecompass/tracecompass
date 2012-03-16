/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.tests.headless;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader;

public class ReadTrace {

    /**
     * @param args
     */
    @SuppressWarnings("nls")
    public static void main(String[] args) {
        final String TRACE_PATH = "Tests/traces/trace20m";

        // Change this to enable text output
        final boolean USE_TEXT = false;

        // Work variables
        Long nbEvent = 0L;
        CTFTrace trace = null;
        try {
            trace = new CTFTrace(TRACE_PATH);
        } catch (CTFReaderException e) {

            nbEvent = (long) -1;
        }
        long start, stop;
        start = System.nanoTime();
        if (nbEvent != -1) {
            CTFTraceReader traceReader = new CTFTraceReader(trace);

            start = System.nanoTime();
            while (traceReader.hasMoreEvents()) {
                EventDefinition ed = traceReader.getCurrentEventDef();
                nbEvent++;
                if (USE_TEXT) {
                    String output = formatDate(ed.timestamp + trace.getOffset());
                    System.out.println("Event " + nbEvent + " Time " + output
                            + " type " + ed.getDeclaration().getName()
                            + " on CPU " + ed.getCPU());
                }
                traceReader.advance();
            }
        }
        stop = System.nanoTime();
        System.out.println("Time taken for " + nbEvent + " " + (stop - start)
                + "ns ");
        System.out.println(((stop - start) / nbEvent) + "ns/event ");
    }

    /**
     * @param timestamp the timestamp in UTC to convert to nanoseconds.
     * @return formatted string.
     */
    private static String formatDate(long timestamp) {
        Date d = new Date(timestamp / 1000000);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss."); //$NON-NLS-1$
        String output = df.format(d) + (timestamp % 1000000000);
        return output;
    }
}
