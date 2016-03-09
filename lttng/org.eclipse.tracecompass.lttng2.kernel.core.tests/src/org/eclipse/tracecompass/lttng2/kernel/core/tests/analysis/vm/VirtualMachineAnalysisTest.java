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

package org.eclipse.tracecompass.lttng2.kernel.core.tests.analysis.vm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.KernelAnalysisModule;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.VcpuStateValues;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.VmAttributes;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.module.VirtualMachineCpuAnalysis;
import org.eclipse.tracecompass.lttng2.lttng.kernel.core.tests.shared.vm.VmTestExperiment;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.junit.Test;

import com.google.common.collect.Multimap;

/**
 * Test suite for the {@link VirtualMachineCpuAnalysis} class
 *
 * @author Geneviève Bastien
 */
public class VirtualMachineAnalysisTest {

    private static void verifyStateIntervals(String testId, List<ITmfStateInterval> intervals, int[] expectedStarts, ITmfStateValue[] expectedValues) {
        int expectedCount = expectedStarts.length - 1;

        assertEquals(testId + ": Interval count", expectedCount, intervals.size());
        for (int i = 0; i < expectedCount; i++) {
            ITmfStateInterval interval = intervals.get(i);
            assertEquals(testId + ": Start time of interval " + i, expectedStarts[i], interval.getStartTime());
            long actualEnd = (i == expectedCount - 1) ? (expectedStarts[i + 1]) : (expectedStarts[i + 1]) - 1;
            assertEquals(testId + ": End time of interval " + i, actualEnd, interval.getEndTime());
            assertEquals(testId + ": Expected value of interval " + i, expectedValues[i], interval.getStateValue());
        }
    }

    private static void verifyIntervalsWithMask(String testId, Collection<ITmfStateInterval> intervals, int[] expectedStarts, int[] expectedEnds, ITmfStateValue[] expectedValues, int mask) {
        int expectedCount = expectedStarts.length - 1;

        assertEquals(testId + ": Interval count", expectedCount, intervals.size());
        int i = 0;
        for (ITmfStateInterval interval : intervals) {
            assertEquals(testId + ": Start time of interval " + i, expectedStarts[i], interval.getStartTime());
            assertEquals(testId + ": End time of interval " + i, expectedEnds[i], interval.getEndTime());
            assertEquals(testId + ": Expected value of interval " + i, expectedValues[i].unboxInt() & mask, interval.getStateValue().unboxInt() & mask);
            i++;
        }
    }

    /**
     * Test the analysis execution with stub traces of a virtual machine with
     * one virtual machine and one CPU
     */
    @Test
    public void testStubTracesOneQemuKvm() {

        assumeTrue(VmTestExperiment.ONE_QEMUKVM.exists());
        TmfExperiment experiment = VmTestExperiment.ONE_QEMUKVM.getExperiment(true);

        /* Open the traces */
        for (ITmfTrace trace : experiment.getTraces()) {
            ((TmfTrace) trace).traceOpened(new TmfTraceOpenedSignal(this, trace, null));
        }

        /*
         * TODO For now, make sure the LttngKernelAnalysis have been run for
         * each trace before running the analysis. When event request precedence
         * is implemented, we can remove this
         */
        for (ITmfTrace trace : experiment.getTraces()) {
            for (KernelAnalysisModule module : TmfTraceUtils.getAnalysisModulesOfClass(trace, KernelAnalysisModule.class)) {
                module.schedule();
                module.waitForCompletion();
            }
        }
        /* End of TODO block */

        experiment.traceOpened(new TmfTraceOpenedSignal(this, experiment, null));
        VirtualMachineCpuAnalysis module = null;
        for (VirtualMachineCpuAnalysis mod : TmfTraceUtils.getAnalysisModulesOfClass(experiment, VirtualMachineCpuAnalysis.class)) {
            module = mod;
            break;
        }
        assertNotNull(module);
        module.schedule();
        if (!module.waitForCompletion()) {
            fail("Module did not complete properly");
        }

        try {
            /* Check the state system */
            ITmfStateSystem ss = module.getStateSystem();
            assertNotNull(ss);
            int vmQuark;

            vmQuark = ss.getQuarkAbsolute(VmAttributes.VIRTUAL_MACHINES);

            List<Integer> guestQuarks = ss.getSubAttributes(vmQuark, false);
            assertEquals("Number of guests", 1, guestQuarks.size());
            List<Integer> vcpuQuarks = ss.getSubAttributes(guestQuarks.get(0), false);
            assertEquals("Number of virtual CPUs", 1, vcpuQuarks.size());
            Integer statusQuark = ss.getQuarkRelative(vcpuQuarks.get(0), VmAttributes.STATUS);

            /* Check the intervals for the virtual CPU */
            List<ITmfStateInterval> intervals = StateSystemUtils.queryHistoryRange(ss, statusQuark, ss.getStartTime(), ss.getCurrentEndTime());

            /* Expected interval values for the virtual CPU */
            int[] expectedStarts = { 1, 60, 75, 95, 100, 150, 155, 195, 210, 245, 260, 295, 300, 350, 355, 375 };
            ITmfStateValue[] expectedValues = { TmfStateValue.nullValue(),
                    TmfStateValue.newValueInt(VcpuStateValues.VCPU_UNKNOWN),
                    TmfStateValue.newValueInt(VcpuStateValues.VCPU_RUNNING),
                    TmfStateValue.newValueInt(VcpuStateValues.VCPU_RUNNING | VcpuStateValues.VCPU_VMM),
                    TmfStateValue.newValueInt(VcpuStateValues.VCPU_RUNNING | VcpuStateValues.VCPU_VMM | VcpuStateValues.VCPU_PREEMPT),
                    TmfStateValue.newValueInt(VcpuStateValues.VCPU_RUNNING | VcpuStateValues.VCPU_VMM),
                    TmfStateValue.newValueInt(VcpuStateValues.VCPU_RUNNING),
                    TmfStateValue.newValueInt(VcpuStateValues.VCPU_RUNNING | VcpuStateValues.VCPU_VMM),
                    TmfStateValue.newValueInt(VcpuStateValues.VCPU_RUNNING),
                    TmfStateValue.newValueInt(VcpuStateValues.VCPU_RUNNING | VcpuStateValues.VCPU_VMM),
                    TmfStateValue.newValueInt(VcpuStateValues.VCPU_RUNNING),
                    TmfStateValue.newValueInt(VcpuStateValues.VCPU_RUNNING | VcpuStateValues.VCPU_VMM),
                    TmfStateValue.newValueInt(VcpuStateValues.VCPU_RUNNING | VcpuStateValues.VCPU_VMM | VcpuStateValues.VCPU_PREEMPT),
                    TmfStateValue.newValueInt(VcpuStateValues.VCPU_RUNNING | VcpuStateValues.VCPU_VMM),
                    TmfStateValue.newValueInt(VcpuStateValues.VCPU_RUNNING) };
            verifyStateIntervals("Virtual CPU", intervals, expectedStarts, expectedValues);

            /* Check the status of the guest's threads */
            int[] expectedStartsT130 = { 10, 35, 75, 175, 195, 225, 275, 295, 300, 350, 375 };
            int[] expectedEndsT130 = { 34, 74, 174, 224, 209, 274, 374, 299, 349, 354, 375 };
            ITmfStateValue[] expectedValuesT30 = { TmfStateValue.newValueInt(VcpuStateValues.VCPU_IDLE),
                    TmfStateValue.newValueInt(VcpuStateValues.VCPU_RUNNING),
                    TmfStateValue.newValueInt(VcpuStateValues.VCPU_IDLE),
                    TmfStateValue.newValueInt(VcpuStateValues.VCPU_RUNNING),
                    TmfStateValue.newValueInt(VcpuStateValues.VCPU_PREEMPT),
                    TmfStateValue.newValueInt(VcpuStateValues.VCPU_IDLE),
                    TmfStateValue.newValueInt(VcpuStateValues.VCPU_RUNNING),
                    TmfStateValue.newValueInt(VcpuStateValues.VCPU_PREEMPT),
                    TmfStateValue.newValueInt(VcpuStateValues.VCPU_PREEMPT),
                    TmfStateValue.newValueInt(VcpuStateValues.VCPU_PREEMPT),
                    TmfStateValue.newValueInt(VcpuStateValues.VCPU_RUNNING) };

            int[] expectedStartsT131 = { 10, 35, 75, 95, 100, 150, 175, 225, 245, 275, 375 };
            int[] expectedEndsT131 = { 34, 74, 174, 99, 149, 154, 224, 274, 259, 374, 375 };
            ITmfStateValue[] expectedValuesT31 = { TmfStateValue.newValueInt(VcpuStateValues.VCPU_RUNNING),
                    TmfStateValue.newValueInt(VcpuStateValues.VCPU_IDLE),
                    TmfStateValue.newValueInt(VcpuStateValues.VCPU_RUNNING),
                    TmfStateValue.newValueInt(VcpuStateValues.VCPU_PREEMPT),
                    TmfStateValue.newValueInt(VcpuStateValues.VCPU_PREEMPT),
                    TmfStateValue.newValueInt(VcpuStateValues.VCPU_PREEMPT),
                    TmfStateValue.newValueInt(VcpuStateValues.VCPU_IDLE),
                    TmfStateValue.newValueInt(VcpuStateValues.VCPU_RUNNING),
                    TmfStateValue.newValueInt(VcpuStateValues.VCPU_PREEMPT),
                    TmfStateValue.newValueInt(VcpuStateValues.VCPU_IDLE),
                    TmfStateValue.newValueInt(VcpuStateValues.VCPU_RUNNING) };

            Multimap<Integer, ITmfStateInterval> threadIntervals = module.getUpdatedThreadIntervals(guestQuarks.get(0), ss.getStartTime(), ss.getCurrentEndTime(), 1, new NullProgressMonitor());
            verifyIntervalsWithMask("Thread 130", threadIntervals.get(130), expectedStartsT130, expectedEndsT130, expectedValuesT30, VcpuStateValues.VCPU_PREEMPT);
            verifyIntervalsWithMask("Thread 131", threadIntervals.get(131), expectedStartsT131, expectedEndsT131, expectedValuesT31, VcpuStateValues.VCPU_PREEMPT);

        } catch (AttributeNotFoundException | StateSystemDisposedException e) {
            fail(e.getMessage());
        } finally {
            experiment.dispose();
        }
    }

}
