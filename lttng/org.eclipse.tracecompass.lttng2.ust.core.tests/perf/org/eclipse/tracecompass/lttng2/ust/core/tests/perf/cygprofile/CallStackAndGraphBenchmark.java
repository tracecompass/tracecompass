/*******************************************************************************
 * Copyright (c) 2018 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.ust.core.tests.perf.cygprofile;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.eclipse.tracecompass.analysis.profiling.core.callgraph.ICallGraphProvider;
import org.eclipse.tracecompass.analysis.profiling.core.callstack.CallStackAnalysis;
import org.eclipse.tracecompass.ctf.core.tests.shared.CtfBenchmarkTrace;
import org.eclipse.tracecompass.internal.analysis.profiling.core.callgraph.CallGraphAnalysis;
import org.eclipse.tracecompass.internal.lttng2.ust.core.callstack.LttngUstCallStackAnalysis;
import org.eclipse.tracecompass.lttng2.ust.core.trace.LttngUstTrace;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestHelper;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Benchmarks the lttng ust callstack analysis execution and call graph
 * execution
 *
 * @author Geneviève Bastien
 */
@SuppressWarnings("restriction")
@RunWith(Parameterized.class)
public class CallStackAndGraphBenchmark {

    /**
     * Test ID for this benchmark
     */
    public static final String TEST_ID = "org.eclipse.tracecompass.lttng2.ust#CallStack#";
    private static final String TEST_CALLSTACK_BUILD = "Building Callstack (%s)";
    private static final String TEST_CALLSTACK_PARSESEGSTORE = "Callstack segment store (%s)";
    private static final String TEST_CALLGRAPH_BUILD = "Building CallGraph (%s)";

    private static final int LOOP_COUNT = 25;

    private final String fName;
    private final String fTestTrace;

    private static String getPathFromCtfTestTrace(@NonNull CtfTestTrace testTrace) {
        CtfTmfTrace ctftrace = CtfTmfTestTraceUtils.getTrace(testTrace);
        String path = Objects.requireNonNull(ctftrace.getPath());
        ctftrace.dispose();
        return path;
    }

    /**
     * Get the traces on which to run the benchmark
     *
     * @return The arrays of parameters
     */
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
                { CtfTestTrace.CYG_PROFILE.name(), getPathFromCtfTestTrace(CtfTestTrace.CYG_PROFILE) },
                { CtfBenchmarkTrace.UST_QMLSCENE.name(), CtfBenchmarkTrace.UST_QMLSCENE.getTracePath().toString() },
        });
    }

    /**
     * Constructor
     *
     * @param name
     *            The name of this test
     * @param tracePath
     *            The path to the trace to the trace to test
     */
    public CallStackAndGraphBenchmark(String name, String tracePath) {
        fName = name;
        fTestTrace = tracePath;
    }

    /**
     * Run benchmark for the trace
     *
     * @throws TmfTraceException
     *             Exceptions thrown getting the trace
     */
    @Test
    public void runCpuBenchmark() throws TmfTraceException {
        Performance perf = Performance.getDefault();
        PerformanceMeter callStackBuildPm = Objects.requireNonNull(perf.createPerformanceMeter(TEST_ID + String.format(TEST_CALLSTACK_BUILD, fName)));
        perf.tagAsSummary(callStackBuildPm, String.format(TEST_CALLSTACK_BUILD, fName), Dimension.CPU_TIME);
        PerformanceMeter callStackSegStorePm = Objects.requireNonNull(perf.createPerformanceMeter(TEST_ID + String.format(TEST_CALLSTACK_PARSESEGSTORE, fName)));
        perf.tagAsSummary(callStackSegStorePm, String.format(TEST_CALLSTACK_PARSESEGSTORE, fName), Dimension.CPU_TIME);
        PerformanceMeter callgraphBuildPm = Objects.requireNonNull(perf.createPerformanceMeter(TEST_ID + String.format(TEST_CALLGRAPH_BUILD, fName)));
        perf.tagAsSummary(callgraphBuildPm, String.format(TEST_CALLGRAPH_BUILD, fName), Dimension.CPU_TIME);

        for (int i = 0; i < LOOP_COUNT; i++) {
            TmfTrace trace = null;
            try {
                trace = getTrace();
                trace.traceOpened(new TmfTraceOpenedSignal(this, trace, null));
                CallStackAnalysis callStackModule = TmfTraceUtils.getAnalysisModuleOfClass(trace, CallStackAnalysis.class, LttngUstCallStackAnalysis.ID);
                assertNotNull(callStackModule);
                callStackModule.triggerAutomatically(false);

                // Benchmark the call stack analysis
                callStackBuildPm.start();
                TmfTestHelper.executeAnalysis(callStackModule);
                callStackBuildPm.stop();

                // Benchmark the segment store iteration
                ISegmentStore<@NonNull ISegment> segmentStore = callStackModule.getSegmentStore();
                assertNotNull(segmentStore);
                callStackSegStorePm.start();
                // Iterate through the whole segment store
                Iterator<ISegment> iterator = segmentStore.iterator();
                while (iterator.hasNext()) {
                    iterator.next();
                }
                callStackSegStorePm.stop();

                ICallGraphProvider callGraphModule = callStackModule.getCallGraph();
                assertTrue(callGraphModule instanceof CallGraphAnalysis);
                // Benchmark the call graph analysis
                callgraphBuildPm.start();
                TmfTestHelper.executeAnalysis((CallGraphAnalysis) callGraphModule);
                callgraphBuildPm.stop();

                /*
                 * Delete the supplementary files, so that the next iteration rebuilds the state
                 * system.
                 */
                File suppDir = new File(TmfTraceManager.getSupplementaryFileDir(trace));
                for (File file : suppDir.listFiles()) {
                    file.delete();
                }

            } finally {
                if (trace != null) {
                    trace.dispose();
                }
            }
        }
        callStackBuildPm.commit();
        callgraphBuildPm.commit();
    }

    private TmfTrace getTrace() throws TmfTraceException {
        LttngUstTrace trace = new LttngUstTrace();
        trace.initTrace(null, fTestTrace, ITmfEvent.class);
        return trace;
    }

}
