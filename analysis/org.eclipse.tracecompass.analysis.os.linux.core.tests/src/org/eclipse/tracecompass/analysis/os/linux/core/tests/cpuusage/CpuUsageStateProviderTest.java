/*******************************************************************************
 * Copyright (c) 2014, 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.tests.cpuusage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.cpuusage.KernelCpuUsageAnalysis;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.Activator;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.trace.TmfXmlKernelTraceStub;
import org.eclipse.tracecompass.analysis.os.linux.core.tid.TidAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelTrace;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.Attributes;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.tests.shared.utils.StateIntervalStub;
import org.eclipse.tracecompass.statesystem.core.tests.shared.utils.StateSystemTestUtils;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestHelper;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

/**
 * Test suite for the {@link KernelCpuUsageAnalysis} class
 *
 * @author Geneviève Bastien
 */
public class CpuUsageStateProviderTest {

    private static final String CPU_USAGE_FILE = "testfiles/cpu_analysis.xml";

    private static final Object NULL_STATE_VALUE = null;

    private IKernelTrace fTrace;
    private KernelCpuUsageAnalysis fModule;

    private static void deleteSuppFiles(ITmfTrace trace) {
        /* Remove supplementary files */
        File suppDir = new File(TmfTraceManager.getSupplementaryFileDir(trace));
        for (File file : suppDir.listFiles()) {
            file.delete();
        }
    }

    /**
     * Setup the trace for the tests
     */
    @Before
    public void setUp() {
        IKernelTrace trace = new TmfXmlKernelTraceStub();
        IPath filePath = Activator.getAbsoluteFilePath(CPU_USAGE_FILE);
        IStatus status = trace.validate(null, filePath.toOSString());
        if (!status.isOK()) {
            fail(status.getException().getMessage());
        }
        try {
            trace.initTrace(null, filePath.toOSString(), TmfEvent.class);
        } catch (TmfTraceException e) {
            fail(e.getMessage());
        }
        deleteSuppFiles(trace);
        ((TmfTrace) trace).traceOpened(new TmfTraceOpenedSignal(this, trace, null));
        /*
         * FIXME: Make sure this analysis is finished before running the CPU
         * analysis. This block can be removed once analysis dependency and
         * request precedence is implemented
         */
        IAnalysisModule module = null;
        for (IAnalysisModule mod : TmfTraceUtils.getAnalysisModulesOfClass(trace, TidAnalysisModule.class)) {
            module = mod;
        }
        assertNotNull(module);
        module.schedule();
        module.waitForCompletion();
        /* End of the FIXME block */

        fModule = TmfTraceUtils.getAnalysisModuleOfClass(trace, KernelCpuUsageAnalysis.class, KernelCpuUsageAnalysis.ID);
        assertNotNull(fModule);
        fTrace = trace;
    }

    /**
     * Clean up
     */
    @After
    public void tearDown() {
        deleteSuppFiles(fTrace);
        fTrace.dispose();
    }

    /**
     * Test that the analysis executes without problems
     */
    @Test
    public void testAnalysisExecution() {
        /* Make sure the analysis hasn't run yet */
        assertNull(fModule.getStateSystem());

        /* Execute the analysis */
        assertTrue(TmfTestHelper.executeAnalysis(fModule));
        assertNotNull(fModule.getStateSystem());
    }

    /**
     * Test that the state system is returned with the expected results
     */
    @Test
    public void testReturnedStateSystem() {
        fModule.schedule();
        fModule.waitForCompletion();
        ITmfStateSystem ss = fModule.getStateSystem();
        assertNotNull(ss);
        assertEquals(1L, ss.getStartTime());
        assertEquals(25L, ss.getCurrentEndTime());

        try {
            int cpusQuark = ss.getQuarkAbsolute(Attributes.CPUS);

            /*
             * There should be 2 CPU entries: 0 and 1 and 3 process entries
             * under each
             */
            List<Integer> cpuQuarks = ss.getSubAttributes(cpusQuark, false);
            assertEquals(2, cpuQuarks.size());
            for (Integer cpuQuark : cpuQuarks) {
                assertEquals(3, ss.getSubAttributes(cpuQuark, false).size());
            }

            /* Test the intervals of proc2 on CPU 0 */
            List<@NonNull ITmfStateInterval> intervals = new ArrayList<>();
            intervals.add(new StateIntervalStub(1, 19, NULL_STATE_VALUE));
            intervals.add(new StateIntervalStub(20, 25, 19L));
            StateSystemTestUtils.testIntervalForAttributes(ss, intervals, Attributes.CPUS, "0", "2");

            /* Test the intervals of proc 4 CPU 1 */
            intervals.clear();
            intervals.add(new StateIntervalStub(1, 4, NULL_STATE_VALUE));
            intervals.add(new StateIntervalStub(5, 14, 3L));
            intervals.add(new StateIntervalStub(15, 25, 8L));
            StateSystemTestUtils.testIntervalForAttributes(ss, intervals, Attributes.CPUS, "1", "4");

            /* Test the intervals of proc 3 on both CPUs */
            intervals.clear();
            intervals.add(new StateIntervalStub(1, 24, NULL_STATE_VALUE));
            intervals.add(new StateIntervalStub(25, 25, 5L));
            StateSystemTestUtils.testIntervalForAttributes(ss, intervals, Attributes.CPUS, "0", "3");

            intervals.clear();
            intervals.add(new StateIntervalStub(1, 1, NULL_STATE_VALUE));
            intervals.add(new StateIntervalStub(2, 9, 1L));
            intervals.add(new StateIntervalStub(10, 25, 6L));
            StateSystemTestUtils.testIntervalForAttributes(ss, intervals, Attributes.CPUS, "1", "3");

            /*
             * Query at the end and make sure all processes on all CPU have the
             * expected values
             */
            Map<@NonNull String @NonNull [], @Nullable Object> map = new HashMap<>();
            map.put(StateSystemTestUtils.makeAttribute(Attributes.CPUS, "0", "1"), 0L);
            map.put(StateSystemTestUtils.makeAttribute(Attributes.CPUS, "0", "2"), 19L);
            map.put(StateSystemTestUtils.makeAttribute(Attributes.CPUS, "0", "3"), 5L);
            map.put(StateSystemTestUtils.makeAttribute(Attributes.CPUS, "1", "1"), 5L);
            map.put(StateSystemTestUtils.makeAttribute(Attributes.CPUS, "1", "3"), 6L);
            map.put(StateSystemTestUtils.makeAttribute(Attributes.CPUS, "1", "4"), 8L);
            StateSystemTestUtils.testValuesAtTime(ss, 25L, map);

        } catch (AttributeNotFoundException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test the
     * {@link KernelCpuUsageAnalysis#getCpuUsageInRange(java.util.Set, long, long)}
     * method.
     * <p>
     * TODO: extend!
     */
    @Test
    public void testUsageInRange() {
        fModule.schedule();
        fModule.waitForCompletion();

        /* This range should query the total range */
        Map<String, Long> expected = new HashMap<>();
        expected.put("0/1", 0L);
        expected.put("0/2", 19L);
        expected.put("0/3", 5L);
        expected.put("1/1", 5L);
        expected.put("1/3", 6L);
        expected.put("1/4", 13L);
        expected.put("total", 48L);
        expected.put("total/1", 5L);
        expected.put("total/2", 19L);
        expected.put("total/3", 11L);
        expected.put("total/4", 13L);
        expected.put("0", 24L);
        expected.put("1", 24L);
        Map<String, Long> resultMap = fModule.getCpuUsageInRange(Collections.EMPTY_SET, 0L, 30L);
        assertEquals(expected, resultMap);

        /* Verify a range when a process runs at the start */
        expected.clear();
        expected.put("0/1", 0L);
        expected.put("0/2", 0L);
        expected.put("0/3", 3L);
        expected.put("1/1", 0L);
        expected.put("1/3", 0L);
        expected.put("1/4", 3L);
        expected.put("total", 6L);
        expected.put("total/1", 0L);
        expected.put("total/2", 0L);
        expected.put("total/3", 3L);
        expected.put("total/4", 3L);
        expected.put("0", 3L);
        expected.put("1", 3L);
        resultMap = fModule.getCpuUsageInRange(Collections.EMPTY_SET, 22L, 25L);
        assertEquals(expected, resultMap);

        /* Verify a range when a process runs at the end */
        expected.clear();
        expected.put("0/1", 0L);
        expected.put("0/2", 3L);
        expected.put("0/3", 0L);
        expected.put("1/1", 0L);
        expected.put("1/3", 1L);
        expected.put("1/4", 2L);
        expected.put("total", 6L);
        expected.put("total/1", 0L);
        expected.put("total/2", 3L);
        expected.put("total/3", 1L);
        expected.put("total/4", 2L);
        expected.put("0", 3L);
        expected.put("1", 3L);
        resultMap = fModule.getCpuUsageInRange(Collections.EMPTY_SET, 1L, 4L);
        assertEquals(expected, resultMap);

        /* Verify a range when a process runs at start and at the end */
        expected.clear();
        expected.put("0/1", 0L);
        expected.put("0/2", 9L);
        expected.put("0/3", 0L);
        expected.put("1/1", 0L);
        expected.put("1/3", 5L);
        expected.put("1/4", 4L);
        expected.put("total", 18L);
        expected.put("total/1", 0L);
        expected.put("total/2", 9L);
        expected.put("total/3", 5L);
        expected.put("total/4", 4L);
        expected.put("0", 9L);
        expected.put("1", 9L);
        resultMap = fModule.getCpuUsageInRange(Collections.EMPTY_SET, 4L, 13L);
        assertEquals(expected, resultMap);
    }

    /**
     * Tests the cpu usage for a cpu subset within a range
     */
    @Test
    public void testInRangeWithCpuSubset() {

        fModule.schedule();
        fModule.waitForCompletion();

        /* Verify a range when a process runs at start and at the end */
        Map<String, Long> expected = new HashMap<>();
        expected.put("0/1", 0L);
        expected.put("0/2", 9L);
        expected.put("0/3", 0L);
        expected.put("total/1", 0L);
        expected.put("total/2", 9L);
        expected.put("total/3", 0L);
        expected.put("0", 9L);
        expected.put("total", 9L);
        Map<String, Long> resultMap = fModule.getCpuUsageInRange(Collections.<@NonNull Integer> singleton(0), 4L, 13L);
        assertEquals(expected, resultMap);

        /* Verify a range when a process runs at start and at the end */
        expected.clear();
        expected.put("1/1", 0L);
        expected.put("1/3", 5L);
        expected.put("1/4", 4L);
        expected.put("total/1", 0L);
        expected.put("total/3", 5L);
        expected.put("total/4", 4L);
        expected.put("1", 9L);
        expected.put("total", 9L);
        resultMap = fModule.getCpuUsageInRange(ImmutableSet.of(1, 2), 4L, 13L);
        assertEquals(expected, resultMap);

    }

    /**
     * Test the requirements of the analysis module
     */
    @Test
    public void testRequirements() {
        IKernelTrace trace = fTrace;
        assertNotNull(trace);
        IKernelAnalysisEventLayout layout = trace.getKernelEventLayout();
        Set<String> expected = ImmutableSet.of(layout.eventSchedSwitch());

        Set<String> actual = StreamSupport.stream(fModule.getAnalysisRequirements().spliterator(), false)
            .flatMap(req -> req.getValues().stream())
            .collect(Collectors.toSet());

        assertEquals(expected, actual);
    }
}
