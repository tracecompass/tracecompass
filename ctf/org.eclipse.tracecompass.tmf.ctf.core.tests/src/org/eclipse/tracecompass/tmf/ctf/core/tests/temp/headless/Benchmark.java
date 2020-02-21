/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.tests.temp.headless;

import java.util.Vector;

import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.ctf.core.context.CtfTmfContext;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;

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
        long nbEvent = 0L;
        final Vector<Double> benchs = new Vector<>();
        long start, stop;
        for (int loops = 0; loops < NUM_LOOPS; loops++) {
            nbEvent = 0L;
            CtfTmfTrace trace = new CtfTmfTrace();
            try {
                trace.initTrace(null, TRACE_PATH, CtfTmfEvent.class);
            } catch (final TmfTraceException e) {
                loops = NUM_LOOPS + 1;
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
                                + current.getTimestamp().toString() + " type " + current.getType().getName()
                                + " on CPU " + current.getCPU() + " " + current.getContent().toString());
                    }
                    // advance the trace to the next event.
                    boolean hasMore = traceReader.advance();
                    if (hasMore) {
                        // you can know the trace has more events.
                    }
                    current = traceReader.getCurrentEvent();
                }
            }
            stop = System.nanoTime();
            System.out.print('.');
            final double time = (stop - start) / (double) nbEvent;
            benchs.add(time);

            trace.dispose();
        }
        System.out.println("");
        double avg = 0;
        for (final double val : benchs) {
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
