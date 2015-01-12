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

package org.eclipse.tracecompass.lttng2.kernel.core.tests.analysis.kernel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.util.List;
import java.util.Set;

import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.KernelAnalysis;
import org.eclipse.tracecompass.lttng2.control.core.session.SessionConfigStrings;
import org.eclipse.tracecompass.lttng2.kernel.core.trace.LttngKernelTrace;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAnalysisRequirement;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestHelper;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTrace;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

/**
 * Test the {@link KernelAnalysis} class
 *
 * @author Geneviève Bastien
 */
public class LttngKernelAnalysisTest {

    private LttngKernelTrace fTrace;
    private KernelAnalysis fKernelAnalysisModule;

    /**
     * Class setup
     */
    @BeforeClass
    public static void setUpClass() {
        assumeTrue(CtfTmfTestTrace.KERNEL.exists());
    }

    /**
     * Set-up the test
     */
    @Before
    public void setUp() {
        fKernelAnalysisModule = new KernelAnalysis();
        fTrace = new LttngKernelTrace();
        try {
            fTrace.initTrace(null, CtfTmfTestTrace.KERNEL.getPath(), CtfTmfEvent.class);
        } catch (TmfTraceException e) {
            /* Should not happen if tracesExist() passed */
            throw new RuntimeException(e);
        }
    }

    /**
     * Dispose test objects
     */
    @After
    public void tearDown() {
        fTrace.dispose();
        fKernelAnalysisModule.dispose();
        fTrace = null;
        fKernelAnalysisModule = null;
    }

    /**
     * Test the LTTng kernel analysis execution
     */
    @Test
    public void testAnalysisExecution() {
        fKernelAnalysisModule.setId("test");
        ITmfTrace trace = fTrace;
        assertNotNull(trace);
        try {
            fKernelAnalysisModule.setTrace(trace);
        } catch (TmfAnalysisException e) {
            fail(e.getMessage());
        }
        // Assert the state system has not been initialized yet
        ITmfStateSystem ss = fKernelAnalysisModule.getStateSystem();
        assertNull(ss);

        assertTrue(TmfTestHelper.executeAnalysis(fKernelAnalysisModule));

        ss = fKernelAnalysisModule.getStateSystem();
        assertNotNull(ss);

        List<Integer> quarks = ss.getQuarks("*");
        assertFalse(quarks.isEmpty());
    }

    /**
     * Test the canExecute method on valid and invalid traces
     */
    @Test
    public void testCanExecute() {
        /* Test with a valid kernel trace */
        assertNotNull(fTrace);
        assertTrue(fKernelAnalysisModule.canExecute(fTrace));

        /* Test with a CTF trace that does not have required events */
        assumeTrue(CtfTmfTestTrace.CYG_PROFILE.exists());
        try (CtfTmfTrace trace = CtfTmfTestTrace.CYG_PROFILE.getTrace();) {
            /*
             * TODO: This should be false, but for now there is no mandatory
             * events in the kernel analysis so it will return true.
             */
            assertTrue(fKernelAnalysisModule.canExecute(trace));
        }
    }

    /**
     * Test for {@link KernelAnalysis#getAnalysisRequirements()}
     *
     * FIXME Ignored for now because the analysis does not provide any
     * requirements (it doesn't look for particular event names anymore).
     */
    @Test
    @Ignore
    public void testGetAnalysisRequirements() {
        Iterable<TmfAnalysisRequirement> requirements = fKernelAnalysisModule.getAnalysisRequirements();
        assertNotNull(requirements);

        /* There should be the event and domain type */
        TmfAnalysisRequirement eventReq = null;
        TmfAnalysisRequirement domainReq = null;
        int numberOfRequirement = 0;
        for (TmfAnalysisRequirement requirement : requirements) {
            ++numberOfRequirement;
            if (requirement.getType().equals(SessionConfigStrings.CONFIG_ELEMENT_EVENT)) {
                eventReq = requirement;
            } else if (requirement.getType().equals(SessionConfigStrings.CONFIG_ELEMENT_DOMAIN)) {
                domainReq = requirement;
            }
        }
        assertNotNull(eventReq);
        assertNotNull(domainReq);

        /* There should be two requirements */
        assertEquals(2, numberOfRequirement);

        /* Verify the content of the requirements themselves */
        /* Domain should be kernel */
        assertEquals(1, domainReq.getValues().size());
        for (String domain : domainReq.getValues()) {
            assertEquals(SessionConfigStrings.CONFIG_DOMAIN_TYPE_KERNEL, domain);
        }

        /* Events */
        Set<String> expectedEvents = ImmutableSet.of(
//                LttngStrings.EXIT_SYSCALL,
//                LttngStrings.IRQ_HANDLER_ENTRY,
//                LttngStrings.IRQ_HANDLER_EXIT,
//                LttngStrings.SOFTIRQ_ENTRY,
//                LttngStrings.SOFTIRQ_EXIT,
//                LttngStrings.SOFTIRQ_RAISE,
//                LttngStrings.SCHED_SWITCH,
//                LttngStrings.SCHED_PROCESS_FORK,
//                LttngStrings.SCHED_PROCESS_EXIT,
//                LttngStrings.SCHED_PROCESS_FREE,
//                LttngStrings.STATEDUMP_PROCESS_STATE,
//                LttngStrings.SCHED_WAKEUP,
//                LttngStrings.SCHED_WAKEUP_NEW,
//                /* Add the prefix for syscalls */
//                LttngStrings.SYSCALL_PREFIX
                );

        assertEquals(0, eventReq.getValues().size());
        for (String event : eventReq.getValues()) {
            assertTrue("Unexpected event " + event, expectedEvents.contains(event));
        }
    }
}
