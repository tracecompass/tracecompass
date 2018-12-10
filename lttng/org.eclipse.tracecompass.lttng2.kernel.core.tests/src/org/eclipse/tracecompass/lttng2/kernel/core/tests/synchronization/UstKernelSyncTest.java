/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.core.tests.synchronization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelThreadInformationProvider;
import org.eclipse.tracecompass.lttng2.lttng.kernel.core.tests.shared.LttngKernelTestTraceUtils;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

/**
 * Test that synchronization between LTTng UST and kernel traces is done
 * correctly.
 *
 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=484620
 *
 * @author Alexandre Montplaisir
 */
public class UstKernelSyncTest {

    /** Time-out tests after 1 minute */
    @Rule public TestRule globalTimeout = new Timeout(1, TimeUnit.MINUTES);

    private static final @NonNull CtfTestTrace KERNEL_TRACE = CtfTestTrace.CONTEXT_SWITCHES_KERNEL;
    private static final @NonNull CtfTestTrace UST_TRACE = CtfTestTrace.CONTEXT_SWITCHES_UST;

    private TmfExperiment fExperiment;
    private ITmfTrace fUstTrace;
    private KernelAnalysisModule fKernelModule;

    /**
     * Test setup
     */
    @Before
    public void setup() {
        ITmfTrace ustTrace = CtfTmfTestTraceUtils.getTrace(UST_TRACE);
        ITmfTrace kernelTrace = LttngKernelTestTraceUtils.getTrace(KERNEL_TRACE);

        TmfExperiment experiment = new TmfExperiment(CtfTmfEvent.class,
                "test-exp",
                new ITmfTrace[] { ustTrace, kernelTrace },
                TmfExperiment.DEFAULT_INDEX_PAGE_SIZE,
                null);

        /* Simulate experiment being opened */
        TmfSignalManager.dispatchSignal(new TmfTraceOpenedSignal(this, experiment, null));
        TmfSignalManager.dispatchSignal(new TmfTraceSelectedSignal(this, experiment));

        KernelAnalysisModule module = TmfTraceUtils.getAnalysisModuleOfClass(experiment,
                KernelAnalysisModule.class, KernelAnalysisModule.ID);
        assertNotNull(module);
        module.waitForCompletion();

        fExperiment = experiment;
        fUstTrace = ustTrace;
        fKernelModule = module;
    }

    /**
     * Test teardown
     */
    @After
    public void tearDown() {
        if (fExperiment != null) {
            fExperiment.dispose();
        }
        CtfTmfTestTraceUtils.dispose(UST_TRACE);
        LttngKernelTestTraceUtils.dispose(KERNEL_TRACE);
    }

    /**
     * Test that the TID given by the kernel analysis matches the one from the
     * UST event's context for a given UST event that was known to fail.
     *
     * Reproduces the specific example that was pointed out in bug 484620.
     */
    @Test
    public void testOneEvent() {
        TmfExperiment experiment = fExperiment;
        ITmfTrace ustTrace = fUstTrace;
        KernelAnalysisModule module = fKernelModule;
        assertNotNull(experiment);
        assertNotNull(ustTrace);
        assertNotNull(module);

        Predicate<@NonNull ITmfEvent> eventFinder = event -> {
            Long addr = event.getContent().getFieldValue(Long.class, "addr");
            Long cs = event.getContent().getFieldValue(Long.class, "call_site");
            Long ctxVtid = event.getContent().getFieldValue(Long.class, "context._vtid");

            if (addr == null || cs == null || ctxVtid == null) {
                return false;
            }

            return Objects.equals(event.getType().getName(), "lttng_ust_cyg_profile:func_entry") &&
                    Objects.equals(Long.toHexString(addr), "804af97") &&
                    Objects.equals(Long.toHexString(cs), "804ab03") &&
                    Objects.equals(ctxVtid.longValue(), 594L);
        };

        /* The event we're looking for is the second event matching the predicate */
        CtfTmfEvent ustEvent = (CtfTmfEvent) TmfTraceUtils.getNextEventMatching(experiment, 0, eventFinder, null);
        assertNotNull(ustEvent);
        long rank = experiment.seekEvent(ustEvent.getTimestamp()).getRank() + 1;
        ustEvent = (CtfTmfEvent) TmfTraceUtils.getNextEventMatching(experiment, rank, eventFinder, null);
        assertNotNull(ustEvent);

        /* Make sure the correct event was retrieved */
        assertEquals(ustTrace, ustEvent.getTrace());
        assertEquals(1450193715128075054L, ustEvent.getTimestamp().toNanos());

        Integer tidFromKernel = KernelThreadInformationProvider.getThreadOnCpu(module,
                ustEvent.getCPU(), ustEvent.getTimestamp().toNanos());

        assertNotNull(tidFromKernel);
        assertEquals(594, tidFromKernel.intValue());
    }

    /**
     * Test going through the whole UST trace, making sure the VTID context of
     * each event corresponds to the TID given by the kernel analysis at the
     * same timestamp.
     */
    @Test
    public void testWholeUstTrace() {
        TmfExperiment experiment = fExperiment;
        ITmfTrace ustTrace = fUstTrace;
        KernelAnalysisModule module = fKernelModule;
        assertNotNull(experiment);
        assertNotNull(ustTrace);
        assertNotNull(module);

        ITmfContext context = ustTrace.seekEvent(0L);
        CtfTmfEvent ustEvent = (CtfTmfEvent) ustTrace.getNext(context);
        int count = 0;
        while (ustEvent != null) {
            Long ustVtid = ustEvent.getContent().getFieldValue(Long.class, "context._vtid");
            /* All events in the trace should have that context */
            assertNotNull(ustVtid);

            long ts = ustEvent.getTimestamp().toNanos();
            long cpu = ustEvent.getCPU();
            Integer kernelTid = KernelThreadInformationProvider.getThreadOnCpu(module, cpu, ts);
            assertNotNull(kernelTid);

            assertEquals("Wrong TID for trace event " + ustEvent.toString(), ustVtid.longValue(), kernelTid.longValue());

            ustEvent = (CtfTmfEvent) ustTrace.getNext(context);
            count++;
        }

        /* Make sure we've read all expected events */
        assertEquals(UST_TRACE.getNbEvents(), count);
    }
}
