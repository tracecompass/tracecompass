/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng2.kernel.core.tests.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.util.List;
import java.util.Set;

import org.eclipse.linuxtools.internal.lttng2.kernel.core.LttngStrings;
import org.eclipse.linuxtools.lttng2.control.core.session.SessionConfigStrings;
import org.eclipse.linuxtools.lttng2.kernel.core.analysis.LttngKernelAnalysisModule;
import org.eclipse.linuxtools.statesystem.core.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAnalysisRequirement;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.linuxtools.tmf.core.tests.shared.TmfTestHelper;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ctf.core.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.ctf.core.tests.shared.CtfTmfTestTrace;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

/**
 * Test the {@link LttngKernelAnalysisModule} class
 *
 * @author Geneviève Bastien
 */
public class LttngKernelAnalysisTest {

    private ITmfTrace fTrace;
    private LttngKernelAnalysisModule fKernelAnalysisModule;

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
        fKernelAnalysisModule = new LttngKernelAnalysisModule();
        fTrace = CtfTmfTestTrace.KERNEL.getTrace();
    }

    /**
     * Dispose test objects
     */
    @After
    public void tearDown() {
        fTrace.dispose();
        fKernelAnalysisModule.dispose();
    }

    /**
     * Test the LTTng kernel analysis execution
     */
    @Test
    public void testAnalysisExecution() {
        fKernelAnalysisModule.setId("test");
        try {
            fKernelAnalysisModule.setTrace(fTrace);
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
     * Test for {@link LttngKernelAnalysisModule#getAnalysisRequirements()}
     */
    @Test
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
                LttngStrings.EXIT_SYSCALL,
                LttngStrings.IRQ_HANDLER_ENTRY,
                LttngStrings.IRQ_HANDLER_EXIT,
                LttngStrings.SOFTIRQ_ENTRY,
                LttngStrings.SOFTIRQ_EXIT,
                LttngStrings.SOFTIRQ_RAISE,
                LttngStrings.SCHED_SWITCH,
                LttngStrings.SCHED_PROCESS_FORK,
                LttngStrings.SCHED_PROCESS_EXIT,
                LttngStrings.SCHED_PROCESS_FREE,
                LttngStrings.STATEDUMP_PROCESS_STATE,
                LttngStrings.SCHED_WAKEUP,
                LttngStrings.SCHED_WAKEUP_NEW,
                /* Add the prefix for syscalls */
                LttngStrings.SYSCALL_PREFIX
                );

        assertEquals(14, eventReq.getValues().size());
        for (String event : eventReq.getValues()) {
            assertTrue("Unexpected event " + event, expectedEvents.contains(event));
        }
    }
}
