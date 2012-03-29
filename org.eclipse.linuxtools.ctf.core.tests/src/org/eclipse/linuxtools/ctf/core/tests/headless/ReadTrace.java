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
import java.util.Vector;

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
        final String TRACE_PATH = "Tests/traces/trace20m1";

        // Change this to enable text output
        final boolean USE_TEXT = false;

        final int LOOP_COUNT = 1;

        // Work variables
        Long nbEvent = 0L;
        Vector<Double> benchs = new Vector<Double>();
        CTFTrace trace = null;
        long start, stop;
        for (int loops = 0; loops < LOOP_COUNT; loops++) {
            try {
                nbEvent = 0L;
                trace = new CTFTrace(TRACE_PATH);
            } catch (CTFReaderException e) {
                // do nothing
            }
            long prev = -1;
            start = System.nanoTime();
            if (USE_TEXT) {
                System.out.println("Event, " + " Time, " + " type, " + " CPU ");
            }
            if (trace != null) {
                CTFTraceReader traceReader = new CTFTraceReader(trace);

                start = System.nanoTime();

                while (traceReader.hasMoreEvents()) {
                    EventDefinition ed = traceReader.getCurrentEventDef();
                    nbEvent++;
                    if (prev == traceReader.getIndex()) {
                        System.out.println("Error on events " + prev);
                    }
                    prev = traceReader.getIndex();
                    if (USE_TEXT) {
                        String output = formatDate(ed.timestamp
                                + trace.getOffset());
                        System.out.println(traceReader.getIndex() + ", "
                                + output + ", " + ed.getDeclaration().getName()
                                + ", " + ed.getCPU());
                    }

                    traceReader.advance();
                }
            }
            stop = System.nanoTime();
            System.out.print('.');
            double time = (stop - start) / (double) nbEvent;
            benchs.add(time);
        }
        System.out.println("");
        double avg = 0;
        for (Double val : benchs) {
            avg += val;
        }
        avg /= benchs.size();
        System.out.println("Time to read " + nbEvent + " events = " + avg
                + " events/ns");
        for (Double val : benchs) {
            System.out.print(val);
            System.out.print(", ");
        }
        try {
            testSeekIndex(trace);
        } catch (CTFReaderException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            testSeekIndex(trace);
        } catch (CTFReaderException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * @return
     */
    private static long getTimestamp(CTFTraceReader fixture) {
        if (fixture.getCurrentEventDef() != null) {
            return fixture.getCurrentEventDef().timestamp;
        }
        return Long.MIN_VALUE;
    }

    public static void testSeekIndex(CTFTrace trace) throws CTFReaderException {
        CTFTraceReader fixture = new CTFTraceReader(trace);
        long rank = 300000L;
        long timeRank = 4281275394331L;
        long nearEnd = 4287422858132L;
        long seekTime_0;
        long seekIndex_0 = 0;
        long seekNext_300000 = 0;
        long seekIndex_300000 = 0;
        long seekTime_300000 = 0;
        String cr = "\n"; //$NON-NLS-1$
        fixture.seek(0);
        for (int i = 0; i < 100; i++) {
            fixture.advance();
        }

        fixture.seek(nearEnd);
        /*
         * we need to read the trace before seeking
         */
        fixture.seek(0);
        seekTime_0 = getTimestamp(fixture);
        for (int i = 0; i < rank; i++) {
            fixture.advance();
        }
        seekNext_300000 = getTimestamp(fixture);
        fixture.seek(timeRank);
        seekTime_300000 = getTimestamp(fixture);
        fixture.seekIndex(0);
        seekIndex_0 = getTimestamp(fixture);

        fixture.seekIndex(rank);
        seekIndex_300000 = getTimestamp(fixture);
        System.out.print(cr);
        System.out.println("seek(0) " + seekTime_0 + cr + //$NON-NLS-1$
                "seekIndex(0) " + seekIndex_0 + cr + //$NON-NLS-1$
                "Next(300000) " + seekNext_300000 + cr + //$NON-NLS-1$
                "seek(time(300000)) " + seekTime_300000 + cr + //$NON-NLS-1$
                "seekIndex(300000) " + seekIndex_300000 //$NON-NLS-1$
        );
    }

    /**
     * @return
     */
    private static long getTimestamp(CTFTraceReader fixture) {
        if (fixture.getCurrentEventDef() != null) {
            return fixture.getCurrentEventDef().timestamp;
        }
        return Long.MIN_VALUE;
    }

    public static void testSeekIndex(CTFTrace trace) throws CTFReaderException {
        CTFTraceReader fixture = new CTFTraceReader(trace);
        long rank = 300000L;
        long timeRank = 4281275394331L;
        long nearEnd = 4287422858132L;
        long seekTime_0;
        long seekIndex_0 = 0;
        long seekNext_300000 = 0;
        long seekIndex_300000 = 0;
        long seekTime_300000 = 0;
        String cr = "\n"; //$NON-NLS-1$
        fixture.seek(0);
        for (int i = 0; i < 100; i++) {
            fixture.advance();
        }

        fixture.seek(nearEnd);
        /*
         * we need to read the trace before seeking
         */
        fixture.seek(0);
        seekTime_0 = getTimestamp(fixture);
        for (int i = 0; i < rank; i++) {
            fixture.advance();
        }
        seekNext_300000 = getTimestamp(fixture);
        fixture.seek(timeRank);
        seekTime_300000 = getTimestamp(fixture);
        fixture.seekIndex(0);
        seekIndex_0 = getTimestamp(fixture);

        fixture.seekIndex(rank);
        seekIndex_300000 = getTimestamp(fixture);
        System.out.print(cr);
        System.out.println("seek(0) " + seekTime_0 + cr + //$NON-NLS-1$
                "seekIndex(0) " + seekIndex_0 + cr + //$NON-NLS-1$
                "Next(300000) " + seekNext_300000 + cr + //$NON-NLS-1$
                "seek(time(300000)) " + seekTime_300000 + cr + //$NON-NLS-1$
                "seekIndex(300000) " + seekIndex_300000 //$NON-NLS-1$
        );
    }

    /**
     * @param timestamp
     *            the timestamp in UTC to convert to nanoseconds.
     * @return formatted string.
     */
    private static String formatDate(long timestamp) {
        Date d = new Date(timestamp / 1000000);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss."); //$NON-NLS-1$
        String output = df.format(d) + (timestamp % 1000000000);
        return output;
    }
}
