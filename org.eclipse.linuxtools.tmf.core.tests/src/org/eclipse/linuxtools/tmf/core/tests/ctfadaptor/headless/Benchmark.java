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
package org.eclipse.linuxtools.tmf.core.tests.ctfadaptor.headless;

import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfIterator;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;

public class Benchmark {

    /**
     * @param args
     */
    @SuppressWarnings("nls")
    public static void main(String[] args) {
        final String TRACE_PATH = "../org.eclipse.linuxtools.ctf.core.tests/Tests/traces/trace20m1";
        final int NUM_LOOPS = 100;

        // Change this to enable text output
        final boolean USE_TEXT = false;
//
//        try {
//            System.in.read();
//        } catch (IOException e1) {
//            // TODO Auto-generated catch block
//            e1.printStackTrace();
//        }
        // Work variables
        Long nbEvent = 0L;
        Vector<Double> benchs = new Vector<Double>();
        CtfTmfTrace trace = null;
        long start, stop;
        for (int loops = 0; loops < NUM_LOOPS; loops++) {
            nbEvent = 0L;
            trace = new CtfTmfTrace();
            try {
                trace.initTrace("Test", TRACE_PATH, CtfTmfEvent.class, 1);
            } catch (FileNotFoundException e) {
                loops = NUM_LOOPS +1;
                break;
            }

            start = System.nanoTime();
            if (nbEvent != -1) {
                CtfIterator traceReader = (CtfIterator) trace.seekEvent(0);

                start = System.nanoTime();
                CtfTmfEvent current = traceReader.getCurrentEvent();
                while (current != null) {
                    nbEvent++;
                    if (USE_TEXT) {
                        String output = formatDate(current.getTimestampValue());
                        System.out.println("Event " + traceReader.getRank() + " Time "
                                + output + " type " + current.getSource()
                                + " on CPU " + current.getCPU());
                    }
                    traceReader.advance();
                    current = traceReader.getCurrentEvent();
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
        System.out.println("Time to read = " + avg + " events/ns");
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
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss."); //$NON-NLS-1$
        String output = df.format(d) + (timestamp % 1000000000);
        return output;
    }

}
