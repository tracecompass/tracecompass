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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

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
     * @param args
     *            The command-line arguments
     */
    public static void main(final String[] args) {
        final String TRACE_PATH = "testfiles/kernel";
        final int NUM_LOOPS = 100;

        // Change this to enable text output
        final boolean USE_TEXT = false;

        // Work variables
        long nbEvent = 0L;
        final List<Double> benchs = new ArrayList<>();
        long start, stop;
        File f = new File(TRACE_PATH);
        if (!f.isDirectory() || f.list() == null) {
            System.err.println(String.format("Trace\n%s\nnot found", f.getAbsoluteFile()));
            return;
        }
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
        double fileSize = Arrays.asList(f.listFiles()).stream().mapToLong(file -> file.length()).sum() / 1024. / 1024;
        System.out.println(String.format("Trace size = %.2f MB, Number of events = %d, Average event size %.2f B/event", fileSize, nbEvent, fileSize * 1024. * 1024 / nbEvent));
        System.out.println(String.format("Throughput = %.2f MB/s (%.2f ns/event)", fileSize * 1e9 / avg / nbEvent, avg));
        StringBuilder sb = new StringBuilder();
        sb.append("\nRaw Results (ns/event) : ");
        StringJoiner sj = new StringJoiner(", ");

        for (final Double val : benchs) {
            sj.add((String.format("%.2f", val)));
        }
        sb.append(sj.toString());
        System.out.println(sb);

    }

}
