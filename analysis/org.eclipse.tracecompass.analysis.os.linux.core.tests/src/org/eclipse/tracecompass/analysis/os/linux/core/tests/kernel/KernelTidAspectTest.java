/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.tests.kernel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelTidAspect;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.LinuxTestCase;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.kernel.KernelAnalysisTestFactory;
import org.eclipse.tracecompass.analysis.os.linux.core.tid.TidAnalysisModule;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the {@link KernelTidAspect} class
 *
 * @author Geneviève Bastien
 */
public class KernelTidAspectTest {

    private static final @NonNull LinuxTestCase KERNEL_TEST_CASE = KernelAnalysisTestFactory.KERNEL_SCHED;

    // ------------------------------------------------------------------------
    // Test trace class definition
    // ------------------------------------------------------------------------

    private ITmfTrace fTrace;

    private static void deleteSuppFiles(ITmfTrace trace) {
        /* Remove supplementary files */
        File suppDir = new File(TmfTraceManager.getSupplementaryFileDir(trace));
        for (File file : suppDir.listFiles()) {
            file.delete();
        }
    }

    /**
     * Test setup
     */
    @Before
    public void setUp() {
        ITmfTrace trace = KERNEL_TEST_CASE.getKernelTrace();
        deleteSuppFiles(trace);
        /* Make sure the Kernel analysis has run */
        ((TmfTrace) trace).traceOpened(new TmfTraceOpenedSignal(this, trace, null));
        IAnalysisModule module = null;
        for (IAnalysisModule mod : TmfTraceUtils.getAnalysisModulesOfClass(trace, TidAnalysisModule.class)) {
            module = mod;
        }
        assertNotNull(module);
        module.schedule();
        module.waitForCompletion();
        fTrace = trace;
    }

    /**
     * Test clean up
     */
    @After
    public void tearDown() {
        fTrace.dispose();
    }

    private Integer resolveNextEvent(ITmfContext context) {
        ITmfTrace trace = fTrace;
        ITmfEvent event = trace.getNext(context);
        assertNotNull(event);
        return (Integer) TmfTraceUtils.resolveEventAspectOfClassForEvent(trace, KernelTidAspect.class, event);
    }

    /**
     * Test the {@link KernelTidAspect#resolve(ITmfEvent)} method method
     */
    @Test
    public void testResolveTidAspect() {

        ITmfContext context = fTrace.seekEvent(0L);
        List<Integer> expected = new ArrayList<>();
        expected.add(null);
        expected.add(null);
        expected.add(null);
        expected.add(null);
        expected.add(null);
        expected.add(null);
        expected.add(11);
        expected.add(20);
        expected.add(20);
        expected.add(null);
        expected.add(21);
        expected.add(30);
        expected.add(21);
        List<Integer> tids = new ArrayList<>();

        for (int i = 0; i < expected.size(); i++) {
            tids.add(resolveNextEvent(context));
        }
        assertEquals(expected, tids);
    }

}
