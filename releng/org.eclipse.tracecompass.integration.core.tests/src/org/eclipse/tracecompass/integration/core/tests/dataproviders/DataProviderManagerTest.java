/******************************************************************************
 * Copyright (c) 2021 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.integration.core.tests.dataproviders;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.core.model.DataProviderDescriptor;
import org.eclipse.tracecompass.lttng2.kernel.core.trace.LttngKernelTrace;
import org.eclipse.tracecompass.lttng2.lttng.kernel.core.tests.shared.LttngKernelTestTraceUtils;
import org.eclipse.tracecompass.lttng2.ust.core.tests.shared.LttngUstTestTraceUtils;
import org.eclipse.tracecompass.lttng2.ust.core.trace.LttngUstTrace;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderManager;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderDescriptor;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderDescriptor.ProviderType;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfTreeXYDataProvider;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit test for testing the data provider manager.
 */
@SuppressWarnings("restriction")
public class DataProviderManagerTest {

    private static LttngKernelTrace fKernelTrace;
    private static LttngUstTrace fUstTrace;
    private static TmfExperiment fExperiment;
    private static final Set<IDataProviderDescriptor> EXPECTED_KERNEL_DP_DESCRIPTORS = new HashSet<>();
    private static final Set<IDataProviderDescriptor> EXPECTED_UST_DP_DESCRIPTORS = new HashSet<>();

    private static final String CPU_USAGE_DP_ID = "org.eclipse.tracecompass.analysis.os.linux.core.cpuusage.CpuUsageDataProvider";

    static {
        // Kernel Trace
        DataProviderDescriptor.Builder builder = new DataProviderDescriptor.Builder();
        builder.setName("CPU Usage")
                .setDescription("Show the CPU usage of a Linux kernel trace, returns the CPU usage per process and can be filtered by CPU core")
                .setProviderType(ProviderType.TREE_TIME_XY)
                .setId(CPU_USAGE_DP_ID);
        EXPECTED_KERNEL_DP_DESCRIPTORS.add(builder.build());
        builder = new DataProviderDescriptor.Builder();
        builder.setName("Disk I/O View")
                .setDescription("Show the input and output throughput for each drive on a machine")
                .setProviderType(ProviderType.TREE_TIME_XY)
                .setId("org.eclipse.tracecompass.analysis.os.linux.core.inputoutput.DisksIODataProvider");
        EXPECTED_KERNEL_DP_DESCRIPTORS.add(builder.build());
        builder = new DataProviderDescriptor.Builder();
        builder.setName("Events Table")
                .setDescription("Show the raw events in table form for a given trace")
                .setProviderType(ProviderType.TABLE)
                .setId("org.eclipse.tracecompass.internal.provisional.tmf.core.model.events.TmfEventTableDataProvider");
        EXPECTED_KERNEL_DP_DESCRIPTORS.add(builder.build());
        builder = new DataProviderDescriptor.Builder();
        builder.setName("Futex Contention Analysis - Latency Statistics")
                .setDescription("Show latency statistics provided by Analysis module: Futex Contention Analysis")
                .setProviderType(ProviderType.DATA_TREE)
                .setId("org.eclipse.tracecompass.analysis.timing.core.segmentstore.SegmentStoreStatisticsDataProvider:lttng.analysis.futex");
        EXPECTED_KERNEL_DP_DESCRIPTORS.add(builder.build());
        builder = new DataProviderDescriptor.Builder();
        builder.setName("Futex Contention Analysis - Latency Statistics")
                .setDescription("Show latency statistics provided by Analysis module: Futex Contention Analysis")
                .setProviderType(ProviderType.DATA_TREE)
                .setId("org.eclipse.tracecompass.analysis.timing.core.segmentstore.SegmentStoreStatisticsDataProvider:lttng.analysis.futex");
        EXPECTED_KERNEL_DP_DESCRIPTORS.add(builder.build());
        builder = new DataProviderDescriptor.Builder();
        builder.setName("Futex Contention Analysis - Latency vs Time")
                .setDescription("Show latencies provided by Analysis module: Futex Contention Analysis")
                .setProviderType(ProviderType.TREE_TIME_XY)
                .setId("org.eclipse.tracecompass.internal.analysis.timing.core.segmentstore.scatter.dataprovider:lttng.analysis.futex");
        EXPECTED_KERNEL_DP_DESCRIPTORS.add(builder.build());
        builder = new DataProviderDescriptor.Builder();
        builder.setName("Histogram")
                .setDescription("Show a histogram of number of events to time for a trace")
                .setProviderType(ProviderType.TREE_TIME_XY)
                .setId("org.eclipse.tracecompass.internal.tmf.core.histogram.HistogramDataProvider");
        EXPECTED_KERNEL_DP_DESCRIPTORS.add(builder.build());
        builder = new DataProviderDescriptor.Builder();
        builder.setName("IRQ Analysis - Latency Statistics")
                .setDescription("Show latency statistics provided by Analysis module: IRQ Analysis")
                .setProviderType(ProviderType.DATA_TREE)
                .setId("org.eclipse.tracecompass.analysis.timing.core.segmentstore.SegmentStoreStatisticsDataProvider:lttng.analysis.irq");
        EXPECTED_KERNEL_DP_DESCRIPTORS.add(builder.build());
        builder = new DataProviderDescriptor.Builder();
        builder.setName("IRQ Analysis - Latency vs Time")
                .setDescription("Show latencies provided by Analysis module: IRQ Analysis")
                .setProviderType(ProviderType.TREE_TIME_XY)
                .setId("org.eclipse.tracecompass.internal.analysis.timing.core.segmentstore.scatter.dataprovider:lttng.analysis.irq");
        EXPECTED_KERNEL_DP_DESCRIPTORS.add(builder.build());
        builder = new DataProviderDescriptor.Builder();
        builder.setName("Memory Usage")
                .setDescription("Show the relative memory usage in the Linux kernel by process, can be filtered to show only the processes which were active on a time range")
                .setProviderType(ProviderType.TREE_TIME_XY)
                .setId("org.eclipse.tracecompass.analysis.os.linux.core.kernelmemoryusage");
        EXPECTED_KERNEL_DP_DESCRIPTORS.add(builder.build());
        builder = new DataProviderDescriptor.Builder();
        builder.setName("Resources Status")
                .setDescription("Show the state of CPUs (SYSCALL, RUNNING, IRQ, SOFT_IRQ or IDLE) and its IRQs/SOFT_IRQs as well as its frequency.")
                .setProviderType(ProviderType.TIME_GRAPH)
                .setId("org.eclipse.tracecompass.internal.analysis.os.linux.core.threadstatus.ResourcesStatusDataProvider");
        EXPECTED_KERNEL_DP_DESCRIPTORS.add(builder.build());
        builder = new DataProviderDescriptor.Builder();
        builder.setName("System Call Latency - Latency Statistics")
                .setDescription("Show latency statistics provided by Analysis module: System Call Latency")
                .setProviderType(ProviderType.DATA_TREE)
                .setId("org.eclipse.tracecompass.analysis.timing.core.segmentstore.SegmentStoreStatisticsDataProvider:org.eclipse.tracecompass.analysis.os.linux.latency.syscall");
        EXPECTED_KERNEL_DP_DESCRIPTORS.add(builder.build());
        builder = new DataProviderDescriptor.Builder();
        builder.setName("System Call Latency - Latency vs Time")
                .setDescription("Show latencies provided by Analysis module: System Call Latency")
                .setProviderType(ProviderType.TREE_TIME_XY)
                .setId("org.eclipse.tracecompass.internal.analysis.timing.core.segmentstore.scatter.dataprovider:org.eclipse.tracecompass.analysis.os.linux.latency.syscall");
        EXPECTED_KERNEL_DP_DESCRIPTORS.add(builder.build());
        builder = new DataProviderDescriptor.Builder();
        builder.setName("Thread Status")
                .setDescription("Show the hierarchy of Linux threads and their status (RUNNING, SYSCALL, IRQ, IDLE)")
                .setProviderType(ProviderType.TIME_GRAPH)
                .setId("org.eclipse.tracecompass.internal.analysis.os.linux.core.threadstatus.ThreadStatusDataProvider");
        EXPECTED_KERNEL_DP_DESCRIPTORS.add(builder.build());

        // UST Trace
        builder = new DataProviderDescriptor.Builder();
        builder.setName("Events Table")
                .setDescription("Show the raw events in table form for a given trace")
                .setProviderType(ProviderType.TABLE)
                .setId("org.eclipse.tracecompass.internal.provisional.tmf.core.model.events.TmfEventTableDataProvider");
        EXPECTED_UST_DP_DESCRIPTORS.add(builder.build());
        builder = new DataProviderDescriptor.Builder();
        builder.setName("Flame Chart")
                .setDescription("Show a call stack over time")
                .setProviderType(ProviderType.TIME_GRAPH)
                .setId("org.eclipse.tracecompass.internal.analysis.profiling.callstack.provider.CallStackDataProvider");
        EXPECTED_UST_DP_DESCRIPTORS.add(builder.build());
        builder = new DataProviderDescriptor.Builder();
        builder.setName("Function Duration Statistics")
                .setDescription("Show the function duration statistics")
                .setProviderType(ProviderType.DATA_TREE)
                .setId("org.eclipse.tracecompass.internal.analysis.profiling.core.callgraph.callgraphanalysis.statistics");
        EXPECTED_UST_DP_DESCRIPTORS.add(builder.build());
        builder = new DataProviderDescriptor.Builder();
        builder.setName("Histogram")
                .setDescription("Show a histogram of number of events to time for a trace")
                .setProviderType(ProviderType.TREE_TIME_XY)
                .setId("org.eclipse.tracecompass.internal.tmf.core.histogram.HistogramDataProvider");
        EXPECTED_UST_DP_DESCRIPTORS.add(builder.build());
        builder = new DataProviderDescriptor.Builder();
        builder.setName("LTTng-UST CallStack - Latency Statistics")
                .setDescription("Show latency statistics provided by Analysis module: LTTng-UST CallStack")
                .setProviderType(ProviderType.DATA_TREE)
                .setId("org.eclipse.tracecompass.analysis.timing.core.segmentstore.SegmentStoreStatisticsDataProvider:org.eclipse.linuxtools.lttng2.ust.analysis.callstack");
        EXPECTED_UST_DP_DESCRIPTORS.add(builder.build());
        builder = new DataProviderDescriptor.Builder();
        builder.setName("LTTng-UST CallStack - Latency vs Time")
                .setDescription("Show latencies provided by Analysis module: LTTng-UST CallStack")
                .setProviderType(ProviderType.TREE_TIME_XY)
                .setId("org.eclipse.tracecompass.internal.analysis.timing.core.segmentstore.scatter.dataprovider:org.eclipse.linuxtools.lttng2.ust.analysis.callstack");
        EXPECTED_UST_DP_DESCRIPTORS.add(builder.build());
    }

    /**
     * Test Class setup
     */
    @BeforeClass
    public static void init() {
        DataProviderManager.getInstance();
        // Open kernel trace
        fKernelTrace = LttngKernelTestTraceUtils.getTrace(CtfTestTrace.KERNEL);

        // Open UST trace
        fUstTrace = LttngUstTestTraceUtils.getTrace(CtfTestTrace.CYG_PROFILE);

        // Open experiment with kernel and UST trace
        ITmfTrace[] traces = { fKernelTrace, fUstTrace };
        fExperiment = new TmfExperiment(ITmfEvent.class, "TextExperiment", traces, TmfExperiment.DEFAULT_INDEX_PAGE_SIZE, null);
        TmfTraceOpenedSignal openTraceSignal = new TmfTraceOpenedSignal(fExperiment, fExperiment, null);
        TmfSignalManager.dispatchSignal(openTraceSignal);
        fExperiment.indexTrace(true);
    }

    /**
     * Test class tear down method.
     */
    @AfterClass
    public static void tearDown() {
        // Dispose experiment and traces
        if (fExperiment != null) {
            fExperiment.dispose();
        }
        DataProviderManager.dispose();
    }

    /**
     * Main test case
     */
    @Test
    public void test() {
        List<IDataProviderDescriptor> kernelDescriptors = DataProviderManager.getInstance().getAvailableProviders(fKernelTrace);
        List<IDataProviderDescriptor> ustDescriptors = DataProviderManager.getInstance().getAvailableProviders(fUstTrace);
        List<IDataProviderDescriptor> expDescriptors = DataProviderManager.getInstance().getAvailableProviders(fExperiment);

        // Verify kernel data provider descriptors
        for (IDataProviderDescriptor descriptor : kernelDescriptors) {
            assertTrue(expDescriptors.contains(descriptor));
            assertTrue(descriptor.getName(), EXPECTED_KERNEL_DP_DESCRIPTORS.contains(descriptor));
        }
        // Verify UST data provider descriptors
        for (IDataProviderDescriptor descriptor : ustDescriptors) {
            assertTrue(expDescriptors.contains(descriptor));
            assertTrue(descriptor.getName(), EXPECTED_UST_DP_DESCRIPTORS.contains(descriptor));
        }
    }

    /**
     * Test different get methods
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testGetter() {
        ITmfTrace trace = fKernelTrace;
        assertNotNull(trace);
        ITmfTreeXYDataProvider<@NonNull ITmfTreeDataModel> dp = DataProviderManager.getInstance().getExistingDataProvider(trace, CPU_USAGE_DP_ID, ITmfTreeXYDataProvider.class);
        assertNull(dp);
        dp = DataProviderManager.getInstance().getOrCreateDataProvider(trace, CPU_USAGE_DP_ID, ITmfTreeXYDataProvider.class);
        assertNotNull(dp);
        ITmfTreeXYDataProvider<@NonNull ITmfTreeDataModel> dp2 = DataProviderManager.getInstance().getExistingDataProvider(trace, CPU_USAGE_DP_ID, ITmfTreeXYDataProvider.class);
        assertNotNull(dp2);
        assertTrue(dp == dp2);
        ITmfTreeXYDataProvider<@NonNull ITmfTreeDataModel> dp3 = DataProviderManager.getInstance().getOrCreateDataProvider(trace, CPU_USAGE_DP_ID, ITmfTreeXYDataProvider.class);
        assertNotNull(dp3);
        assertTrue(dp == dp3);
        assertTrue(dp == dp2);
    }
}
