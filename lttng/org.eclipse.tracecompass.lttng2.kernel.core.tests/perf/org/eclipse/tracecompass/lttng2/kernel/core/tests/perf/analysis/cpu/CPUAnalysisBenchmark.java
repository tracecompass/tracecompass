/*******************************************************************************
 * Copyright (c) 2019 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.core.tests.perf.analysis.cpu;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.eclipse.tracecompass.analysis.os.linux.core.cpuusage.CpuUsageEntryModel;
import org.eclipse.tracecompass.analysis.os.linux.core.cpuusage.KernelCpuUsageAnalysis;
import org.eclipse.tracecompass.analysis.os.linux.core.tid.TidAnalysisModule;
import org.eclipse.tracecompass.ctf.core.tests.shared.CtfBenchmarkTrace;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.cpuusage.CpuUsageDataProvider;
import org.eclipse.tracecompass.lttng2.kernel.core.trace.LttngKernelTrace;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderParameterUtils;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectedCpuQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfXyModel;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestHelper;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.junit.Test;

/**
 * Benchmarks the CPU usage analysis building and data provider
 *
 * @author Geneviève Bastien
 */
public class CPUAnalysisBenchmark {

    private static final String TEST_ID = "org.eclipse.linuxtools#CPU usage analysis#";
    private static final IProgressMonitor NULL_MONITOR = new NullProgressMonitor();
    private static final String TEST_ANALYSIS_EXECUTION = "Execution";
    private static final String TEST_ANALYSIS_QUERY = "Query CPU Usage";
    private static final int LOOP_COUNT = 25;

    /**
     * Run the benchmark with "OS events" benchmark trace
     *
     * @throws TmfTraceException
     *             Exceptions thrown by test
     */
    @Test
    public void testOSEvents() throws TmfTraceException {
        runTest(CtfBenchmarkTrace.ALL_OS_ANALYSES.getTracePath().toString(), "OS Events", LOOP_COUNT);
    }

    /**
     * Run the benchmark with "many threads"
     *
     * @throws TmfTraceException
     *             Exceptions thrown by test
     * @throws IOException
     *             Exceptions thrown by test
     */
    @Test
    public void testManyThreads() throws TmfTraceException, IOException {
        try {
            runTest(FileUtils.toFile(FileLocator.toFileURL(CtfTestTrace.MANY_THREADS.getTraceURL())).getAbsolutePath(), "ManyThreads", 7);
        } finally {
            CtfTmfTestTraceUtils.dispose(CtfTestTrace.MANY_THREADS);
        }
    }

    /**
     * Run the benchmark with "django httpd"
     *
     * @throws TmfTraceException
     *             Exceptions thrown by test
     * @throws IOException
     *             Exceptions thrown by test
     */
    @Test
    public void testDjangoHttpd() throws TmfTraceException, IOException {
        try {
            runTest(FileUtils.toFile(FileLocator.toFileURL(CtfTestTrace.DJANGO_HTTPD.getTraceURL())).getAbsolutePath(), "Django HTTPD", LOOP_COUNT);
        } finally {
            CtfTmfTestTraceUtils.dispose(CtfTestTrace.MANY_THREADS);
        }
    }

    private void initializeTrace(@NonNull String path, @NonNull LttngKernelTrace trace) throws TmfTraceException {
        trace.initTrace(null, path, CtfTmfEvent.class);
        trace.traceOpened(new TmfTraceOpenedSignal(this, trace, null));
        IAnalysisModule module = trace.getAnalysisModule(TidAnalysisModule.ID);
        assertNotNull(module);
        module.schedule();
        assertTrue(module.waitForCompletion());
        // The data provider will use this module, so execute it
        module = trace.getAnalysisModule(KernelCpuUsageAnalysis.ID);
        assertNotNull(module);
        module.schedule();
        assertTrue(module.waitForCompletion());
    }

    private static KernelCpuUsageAnalysis getModule(@NonNull LttngKernelTrace trace) throws TmfAnalysisException {
        KernelCpuUsageAnalysis module = new KernelCpuUsageAnalysis();

        try {
            /* Initialize the analysis module */
            module.setId("test");
            module.setTrace(trace);
            return module;
        } catch (TmfAnalysisException e) {
            module.dispose();
            throw e;
        }

    }

    private static void deleteSupplementaryFiles(@NonNull ITmfTrace trace) {
        /*
         * Delete the supplementary files at the end of the benchmarks
         */
        File suppDir = new File(TmfTraceManager.getSupplementaryFileDir(trace));
        for (File file : suppDir.listFiles()) {
            file.delete();
        }
    }

    private void runTest(String testTrace, String testName, int loopCount) throws TmfTraceException {
        assertNotNull(testTrace);

        /* First, initialize the trace and run the required analysis */
        LttngKernelTrace trace = new LttngKernelTrace();
        try {

            deleteSupplementaryFiles(trace);
            initializeTrace(testTrace, trace);

            /* Benchmark the CPU usage module */
            benchmarkCPUModule(testName, trace, loopCount);

            deleteSupplementaryFiles(trace);
        } catch (TmfAnalysisException e) {
            fail(e.getMessage());
        } finally {
            trace.dispose();
        }
    }

    @SuppressWarnings("restriction")
    private static void benchmarkCPUModule(String testName, @NonNull LttngKernelTrace trace, int loopCount) throws TmfAnalysisException {

        Performance perf = Performance.getDefault();
        PerformanceMeter pmAnalysisExecution = perf.createPerformanceMeter(TEST_ID + testName + ": " + TEST_ANALYSIS_EXECUTION);
        perf.tagAsSummary(pmAnalysisExecution, TEST_ANALYSIS_EXECUTION + '(' + testName + ')', Dimension.CPU_TIME);

        PerformanceMeter pmQueryUsage = perf.createPerformanceMeter(TEST_ID + testName + ": " + TEST_ANALYSIS_QUERY);
        perf.tagAsSummary(pmQueryUsage, TEST_ANALYSIS_QUERY + '(' + testName + ')', Dimension.CPU_TIME);

        for (int i = 0; i < loopCount; i++) {
            KernelCpuUsageAnalysis module = getModule(trace);
            try {

                pmAnalysisExecution.start();
                TmfTestHelper.executeAnalysis(module);
                pmAnalysisExecution.stop();

                CpuUsageDataProvider dataProvider = CpuUsageDataProvider.create(trace);
                assertNotNull(dataProvider);

                // Query all CPU Usage for full time range, then 10 times 10%
                // smaller ranges
                int resolution = 1500;
                long startTime = trace.getStartTime().toNanos();
                long endTime = trace.getEndTime().toNanos();

                pmQueryUsage.start();
                for (int j = 0; j < 10; j++) {
                    // Query the tree for that range
                    TimeQueryFilter filter = new SelectedCpuQueryFilter(startTime, endTime, 2, Collections.emptyList(), Collections.emptySet());

                    @NonNull Map<@NonNull String, @NonNull Object> parameters = new HashMap<>();
                    parameters.put(DataProviderParameterUtils.TIME_REQUESTED_KEY, getTimeRequested(filter));
                    parameters.put(DataProviderParameterUtils.SELECTED_ITEMS_KEY, Collections.emptyList());
                    parameters.put("cpus", Collections.emptySet());
                    TmfModelResponse<@NonNull TmfTreeModel<@NonNull CpuUsageEntryModel>> response = dataProvider.fetchTree(parameters, NULL_MONITOR);
                    TmfTreeModel<@NonNull CpuUsageEntryModel> model = response.getModel();
                    assertNotNull(model);

                    List<CpuUsageEntryModel> entries = model.getEntries();
                    assertNotNull(entries);

                    // Add all entries to the list of selected
                    List<Long> selected = new ArrayList<>();
                    for (CpuUsageEntryModel entry : entries) {
                        selected.add(entry.getId());
                    }

                    // Get the usage for all threads
                    filter = new SelectedCpuQueryFilter(startTime, endTime, resolution, selected, Collections.emptySet());
                    parameters = new HashMap<>();
                    parameters.put(DataProviderParameterUtils.TIME_REQUESTED_KEY, getTimeRequested(filter));
                    parameters.put(DataProviderParameterUtils.SELECTED_ITEMS_KEY, selected);
                    parameters.put("cpus", Collections.emptySet());
                    TmfModelResponse<@NonNull ITmfXyModel> fetchXY = dataProvider.fetchXY(parameters, NULL_MONITOR);
                    ITmfXyModel model2 = fetchXY.getModel();
                    assertNotNull(model2);

                    // Reduce the time range
                    long step = (endTime - startTime) / 20;
                    startTime += step;
                    endTime -= step;
                }
                pmQueryUsage.stop();

            } finally {
                module.dispose();
            }

        }
        pmAnalysisExecution.commit();
        pmQueryUsage.commit();
    }

    private static @NonNull List<Long> getTimeRequested(TimeQueryFilter filter) {
        List<Long> times = new ArrayList<>();
        for (long time : filter.getTimesRequested()) {
            times.add(time);
        }
        return times;
    }
}
