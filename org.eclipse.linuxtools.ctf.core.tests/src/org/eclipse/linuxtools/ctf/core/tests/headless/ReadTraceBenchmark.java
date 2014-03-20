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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.Vector;

import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader;

/**
 * Tests for performance regressions of the ctf reader. It only tests the ctf
 * reader, not tmf. You can give it the arguments : <b>trace path</b>(trace to
 * read), <b>output file</b>(the path to the resulting output) and <b>title</b>
 * (The name of this benchmark run) <br>
 * <br>
 * This test runs in 3 passes.
 * <ul>
 * <li>first it opens a trace</li>
 * <li>then it reads the trace completely</li>
 * <li>then it randomly (seeded) seeks 1000 locations in the trace and reads one
 * event after</li>
 * </ul>
 *
 * @author Matthew Khouzam
 *
 */
public class ReadTraceBenchmark {

    /**
     * @param args
     *            Tracepath, where to store, title
     * @throws IOException
     *             file not found or such
     */
    public static void main(String[] args) throws IOException {
        String tracePath = "traces/kernel";
        String outFile = "results.csv";
        String title = "run a";
        Random random = new Random(1000);
        if (args.length > 0) {
            tracePath = args[0];
        }
        if (args.length > 1) {
            outFile = args[1];
        }
        if (args.length > 2) {
            title = args[2];
        }

        final int LOOP_COUNT = 4;

        Vector<Long> timestamps = new Vector<>();
        for (int i = 0; i < 500; i++) {
            long start = 4277198419110L;
            long range = 4287422865814L - 4277198419110L;
            timestamps.add(start + (random.nextLong() % range));
        }

        // Work variables
        long nbEvent = 0L;
        Vector<Long> openTime = new Vector<>();
        Vector<Double> benchs = new Vector<>();
        Vector<Double> seeks = new Vector<>();
        CTFTrace trace = null;
        long start, stop;
        double time;
        for (int loops = 0; loops < LOOP_COUNT; loops++) {
            start = System.nanoTime();
            try {
                nbEvent = 0L;
                trace = new CTFTrace(tracePath);
            } catch (CTFReaderException e) {
                // do nothing
            }
            stop = System.nanoTime();
            openTime.add(stop - start);
            start = System.nanoTime();
            try {
                if (trace != null) {
                    CTFTraceReader traceReader = new CTFTraceReader(trace);

                    start = System.nanoTime();

                    while (traceReader.hasMoreEvents()) {
                        traceReader.getCurrentEventDef();
                        nbEvent++;
                        traceReader.advance();
                    }
                    stop = System.nanoTime();

                    time = (stop - start) / (double) nbEvent;
                    benchs.add(time);
                    start = System.nanoTime();
                    for (Long ts : timestamps) {
                        traceReader.seek(ts);
                        traceReader.advance();
                    }
                    stop = System.nanoTime();
                    seeks.add((double) (stop - start) / timestamps.size());
                }
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
        File output = new File(outFile);
        boolean writeHeader = !output.exists();
        try (FileOutputStream fos = new FileOutputStream(output, true)) {
            if (writeHeader) {
                fos.write(new String("title,open time(us),read time(us),seeks(us) \n").getBytes());
            }
            for (int i = 0; i < LOOP_COUNT; i++) {
                fos.write(new String(title + "," + openTime.get(i) * 0.001 + "," + benchs.get(i) * 0.001 + "," + seeks.get(i) * 0.001 + "\n").getBytes());
            }
        }
    }
}
