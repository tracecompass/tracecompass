/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
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

import java.util.Vector;

import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfContext;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;

/**
 * Test and benchmark reading a CTF LTTng kernel trace.
 *
 * @author Matthew Khouzam
 */
public class Benchmark {

    /**
     * Run the benchmark.
     *
     * @param args The command-line arguments
     */
    public static void main(final String[] args) {
        final String TRACE_PATH = "testfiles/kernel";
        final int NUM_LOOPS = 100;

        // Change this to enable text output
        final boolean USE_TEXT = true;

        // Work variables
        Long nbEvent = 0L;
        final Vector<Double> benchs = new Vector<>();
        CtfTmfTrace trace = null;
        long start, stop;
        for (int loops = 0; loops < NUM_LOOPS; loops++) {
            nbEvent = 0L;
            trace = new CtfTmfTrace();
            try {
                trace.initTrace(null, TRACE_PATH, CtfTmfEvent.class);
            } catch (final TmfTraceException e) {
                loops = NUM_LOOPS +1;
                break;
            }

            start = System.nanoTime();
            if (nbEvent != -1) {
                final CtfTmfContext traceReader = (CtfTmfContext) trace.seekEvent(0);

                start = System.nanoTime();
                CtfTmfEvent current = traceReader.getCurrentEvent();
                while (current != null) {
                    nbEvent++;
                    if (USE_TEXT) {

                        System.out.println("Event " + nbEvent + " Time "
                                + current.getTimestamp().toString() + " type " + current.getEventName()
                                + " on CPU " + current.getSource() + " " + current.getContent().toString());
                    }
                    // advance the trace to the next event.
                    boolean hasMore = traceReader.advance();
                    if( hasMore ){
                        // you can know the trace has more events.
                    }
                    current = traceReader.getCurrentEvent();
                }
            }
            stop = System.nanoTime();
            System.out.print('.');
            final double time = (stop - start) / (double) nbEvent;
            benchs.add(time);
        }
        System.out.println("");
        double avg = 0;
        for (final Double val : benchs) {
            avg += val;
        }
        avg /= benchs.size();
        System.out.println("Time to read = " + avg + " events/ns");
        for (final Double val : benchs) {
            System.out.print(val);
            System.out.print(", ");
        }

    }

}
