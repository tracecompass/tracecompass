/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
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
import java.util.Vector;

import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader;

@SuppressWarnings("javadoc")
public class ReadTrace {

    /**
     * @param args
     */
    public static void main(String[] args) {
        final String TRACE_PATH = "traces/kernel";

        // Change this to enable text output
        final boolean USE_TEXT = false;

        final int LOOP_COUNT = 1;

        // Work variables
        long nbEvent = 0L;
        Vector<Double> benchs = new Vector<>();
        CTFTrace trace = null;
        long start, stop;
        for (int loops = 0; loops < LOOP_COUNT; loops++) {
            try {
                nbEvent = 0L;
                trace = new CTFTrace(TRACE_PATH);
            } catch (CTFReaderException e) {
                // do nothing
            }
            start = System.nanoTime();
            if (USE_TEXT) {
                System.out.println("Event, " + " Time, " + " type, " + " CPU ");
            }
            try {
                if (trace != null) {
                    CTFTraceReader traceReader = new CTFTraceReader(trace);

                    start = System.nanoTime();

                    while (traceReader.hasMoreEvents()) {
                        EventDefinition ed = traceReader.getCurrentEventDef();
                        nbEvent++;
                        if (USE_TEXT) {
                            String output = formatDate(ed.getTimestamp()
                                    + trace.getOffset());
                            System.out.println(nbEvent + ", "
                                    + output + ", " + ed.getDeclaration().getName()
                                    + ", " + ed.getCPU() + ed.getFields().toString());
                        }
                        // long endTime = traceReader.getEndTime();
                        // long timestamp =
                        // traceReader.getCurrentEventDef().getTimestamp();
                        traceReader.advance();
                    }
                    // Map<Long, Stream> streams =
                    // traceReader.getTrace().getStreams();
                }
                stop = System.nanoTime();

                System.out.print('.');
                double time = (stop - start) / (double) nbEvent;
                benchs.add(time);
            } catch (CTFReaderException e) {
                System.out.println("error");
            }
        }
        System.out.println("");
        double avg = 0;
        for (Double val : benchs) {
            avg += val;
        }
        avg /= benchs.size();
        System.out.println("Time to read " + nbEvent + " events = " + avg
                + " ns/event");
        for (Double val : benchs) {
            System.out.print(val);
            System.out.print(", ");
        }
    }

    /**
     * @param timestamp
     *            the timestamp in UTC to convert to nanoseconds.
     * @return formatted string.
     */
    private static String formatDate(long timestamp) {
        Date d = new Date(timestamp / 1000000);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.");
        String output = df.format(d) + (timestamp % 1000000000);
        return output;
    }
}
