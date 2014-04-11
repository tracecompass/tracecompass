/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ctf.core.tests.headless;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.linuxtools.ctf.core.tests.shared.CtfTestTrace;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest.ExecutionType;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ctf.core.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.TmfExperimentStub;

/**
 * Coalescing benchmark
 *
 * @author Matthew Khouzam
 *
 */
public class ExperimentBenchmark {
    private static final int MAX_TRACES = 160;
    private static final int BLOCK_SIZE = 100;
    private static final String TRACES_ROOT_PATH = CtfTestTrace.TRACE_EXPERIMENT.getPath();
    private ITmfTrace[] traces;
    private TmfExperimentStub fExperiment;

    /**
     * initialization
     *
     * @param maxTraces
     *            maximum number of traces to open
     *
     * @throws TmfTraceException
     *             problem
     */
    private void init(int maxTraces) throws TmfTraceException {
        File parentDir = new File(TRACES_ROOT_PATH);
        File[] traceFiles = parentDir.listFiles();
        traces = new CtfTmfTrace[Math.min(maxTraces, traceFiles.length)];
        for (int i = 0; i < traces.length; i++) {
            traces[i] = new CtfTmfTrace();
        }
        fExperiment = new TmfExperimentStub("MegaExperiment", traces, BLOCK_SIZE);
        int j = 0;
        for (int i = 0; i < (traces.length) && (j < traces.length); i++) {
            String absolutePath = traceFiles[j].getAbsolutePath();
            if (traces[i].validate(null, absolutePath).isOK()) {
                traces[i].initTrace(null, absolutePath, ITmfEvent.class);
            } else {
                i--;
            }
            j++;
        }
        if (traces[traces.length - 1].getPath() == null) {
            throw new TmfTraceException("Insufficient valid traces in directory");
        }

    }

    /**
     * Main benchmark
     *
     * @param args
     *            benchmark
     */
    public static void main(final String[] args) {
        ExperimentBenchmark eb = new ExperimentBenchmark();
        eb.testRun();

    }

    /**
     * Run the benchmark
     */
    public void testRun() {
        System.out.println("Test, init, request, dispose");

        for (int numTraces = 1; numTraces < MAX_TRACES; numTraces = (int) (1.1 * (numTraces + 1))) {
            InnerEventRequest expReq = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);

            InnerEventRequest traceReq[] = new InnerEventRequest[numTraces];

            System.out.print(numTraces);

            waitForInit(numTraces);
            fExperiment.sendRequest(expReq);
            for (int i = 0; i < numTraces; i++) {
                traceReq[i] = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
                traces[i].sendRequest(traceReq[i]);
            }
            waitForRequest(expReq, traceReq);

            for (int i = 0; i < traces.length; i++) {
                if (!expReq.isTraceHandled(traces[i])) {
                    System.err.println("Trace " + i + " not handled!");
                }
            }
            waitForDispose();
            System.out.println("");
        }
    }

    private void waitForDispose() {
        long start = System.nanoTime();
        fExperiment.dispose();
        for (int i = 0; i < traces.length; i++) {
            traces[i].dispose();
        }
        long end = System.nanoTime();
        printTime(start, end);
    }

    private void waitForInit(int numTraces) {
        long start = System.nanoTime();
        try {
            init(numTraces);
        } catch (TmfTraceException e) {
            System.out.println(e.getMessage());
        }
        long end = System.nanoTime();
        printTime(start, end);
    }

    private static void waitForRequest(InnerEventRequest expReq, InnerEventRequest[] traceReqs) {
        long start = System.nanoTime();
        try {
            expReq.waitForCompletion();
            List<InnerEventRequest> reqs = Arrays.asList(traceReqs);
            for (InnerEventRequest traceReq : reqs) {
                traceReq.waitForCompletion();
            }
        } catch (InterruptedException e) {
        }
        long end = System.nanoTime();
        printTime(start, end);
    }

    private static void printTime(long start, long end) {
        /* print out the difference between the two nanosecond times in ms */
        System.out.format(", %.3f", 0.000000001 * (end - start));
    }

    private static class InnerEventRequest extends TmfEventRequest {
        private Set<String> fTraces = new HashSet<>();

        public InnerEventRequest(Class<? extends ITmfEvent> dataType, long index, int nbRequested, ExecutionType priority) {
            super(dataType, index, nbRequested, priority);
        }

        @Override
        public void handleData(ITmfEvent event) {
            super.handleData(event);
            if (!fTraces.contains(event.getTrace().getName())) {
                fTraces.add(event.getTrace().getName());
            }
        }

        public boolean isTraceHandled(ITmfTrace trace) {
            return fTraces.contains(trace.getName());
        }
    }
}
