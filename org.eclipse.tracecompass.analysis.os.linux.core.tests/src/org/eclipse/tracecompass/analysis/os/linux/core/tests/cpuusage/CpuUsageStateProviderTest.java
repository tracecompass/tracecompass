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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.tracecompass.analysis.os.linux.core.cpuusage.KernelCpuUsageAnalysis;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.Attributes;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.KernelAnalysis;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.Activator;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestHelper;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.xml.TmfXmlTraceStub;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for the {@link KernelCpuUsageAnalysis} class
 *
 * @author Geneviève Bastien
 */
public class CpuUsageStateProviderTest {

    private static final String CPU_USAGE_FILE = "testfiles/cpu_analysis.xml";

    private ITmfTrace fTrace;
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
        ITmfTrace trace = new TmfXmlTraceStub();
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
        for (IAnalysisModule mod : TmfTraceUtils.getAnalysisModulesOfClass(trace, KernelAnalysis.class)) {
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

            /* Proc 2 on CPU 0 should run from 1 to 20 seconds */
            int proc2Quark = ss.getQuarkAbsolute(Attributes.CPUS, "0", "2");
            ITmfStateInterval interval = ss.querySingleState(2L, proc2Quark);
            assertEquals(1L, interval.getStartTime());
            assertEquals(19L, interval.getEndTime());

            /*
             * Query at the end and make sure all processes on all CPU have the
             * expected values
             */
            List<ITmfStateInterval> state = ss.queryFullState(25L);

            int quark = ss.getQuarkAbsolute("CPUs", "0", "1");
            assertEquals(0L, state.get(quark).getStateValue().unboxLong());

            quark = ss.getQuarkAbsolute("CPUs", "0", "2");
            assertEquals(19L, state.get(quark).getStateValue().unboxLong());

            quark = ss.getQuarkAbsolute("CPUs", "0", "3");
            assertEquals(5L, state.get(quark).getStateValue().unboxLong());

            quark = ss.getQuarkAbsolute("CPUs", "1", "1");
            assertEquals(5L, state.get(quark).getStateValue().unboxLong());

            quark = ss.getQuarkAbsolute("CPUs", "1", "3");
            assertEquals(6L, state.get(quark).getStateValue().unboxLong());

            quark = ss.getQuarkAbsolute("CPUs", "1", "4");
            assertEquals(8L, state.get(quark).getStateValue().unboxLong());

        } catch (AttributeNotFoundException | StateSystemDisposedException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test the
     * {@link KernelCpuUsageAnalysis#getCpuUsageInRange(long, long)}
     * method.
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
        Map<String, Long> resultMap = fModule.getCpuUsageInRange(0L, 30L);
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
        resultMap = fModule.getCpuUsageInRange(22L, 25L);
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
        resultMap = fModule.getCpuUsageInRange(1L, 4L);
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
        resultMap = fModule.getCpuUsageInRange(4L, 13L);
        assertEquals(expected, resultMap);

    }
}
