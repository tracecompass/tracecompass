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
 *     Geneviève Bastien - Convert to JUnit performance test
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ctf.core.tests.perf.experiment;

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
import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.junit.Test;

/**
 * Coalescing benchmark
 *
 * @author Matthew Khouzam
 */
public class ExperimentBenchmark {

    private static final String TEST_ID = "org.eclipse.linuxtools#Experiment benchmark";
    private static final int MAX_TRACES = 160;
    private static final int BLOCK_SIZE = 100;
    private static final String TRACES_ROOT_PATH = CtfTestTrace.TRACE_EXPERIMENT.getPath();
    private static final int SAMPLE_SIZE = 5;

    private TmfExperimentStub fExperiment;

    /**
     * Run the benchmark
     */
    @Test
    public void benchmarkExperimentSizeRequest() {
        Performance perf = Performance.getDefault();

        for (int numTraces = 1; numTraces < MAX_TRACES; numTraces = (int) (1.6 * (numTraces + 1))) {
            PerformanceMeter pm = perf.createPerformanceMeter(TEST_ID + '(' + numTraces + ')');
            perf.tagAsSummary(pm, "Experiment Benchmark traces:" + numTraces, Dimension.CPU_TIME);
            if ((int) (1.6 * (numTraces + 1)) > MAX_TRACES) {
                perf.tagAsGlobalSummary(pm, "Experiment Benchmark traces: " + numTraces, Dimension.CPU_TIME);
            }

            for (int s = 0; s < SAMPLE_SIZE; s++) {

                InnerEventRequest expReq = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
                InnerEventRequest traceReq[] = new InnerEventRequest[numTraces];

                init(numTraces);
                fExperiment.sendRequest(expReq);
                ITmfTrace[] traces = fExperiment.getTraces();
                for (int i = 0; i < numTraces; i++) {
                    traceReq[i] = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
                    traces[i].sendRequest(traceReq[i]);
                }

                pm.start();
                waitForRequest(expReq, traceReq);
                pm.stop();

                for (int i = 0; i < traces.length; i++) {
                    if (!expReq.isTraceHandled(traces[i])) {
                        System.err.println("Trace " + i + " not handled!");
                    }
                }

                fExperiment.dispose();
            }
            pm.commit();
        }
    }

    /**
     * Initialization
     *
     * @param maxTraces
     *            maximum number of traces to open
     */
    private void init(int maxTraces) {
        try {
            File parentDir = new File(TRACES_ROOT_PATH);
            File[] traceFiles = parentDir.listFiles();
            ITmfTrace[] traces = new CtfTmfTrace[Math.min(maxTraces, traceFiles.length)];
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
        } catch (TmfTraceException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void waitForRequest(InnerEventRequest expReq, InnerEventRequest[] traceReqs) {
        try {
            expReq.waitForCompletion();
            List<InnerEventRequest> reqs = Arrays.asList(traceReqs);
            for (InnerEventRequest traceReq : reqs) {
                traceReq.waitForCompletion();
            }
        } catch (InterruptedException e) {
        }
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
