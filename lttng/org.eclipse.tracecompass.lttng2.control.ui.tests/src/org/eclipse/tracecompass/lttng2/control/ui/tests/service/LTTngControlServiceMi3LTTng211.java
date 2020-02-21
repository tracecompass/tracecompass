/******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.control.ui.tests.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IChannelInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IDomainInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IEventInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.ISessionInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceChannelOutputType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceEnablement;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceEventType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceLogLevel;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceSessionState;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.service.ILttngControlService;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.service.LTTngControlService;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.service.LTTngControlServiceFactory;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.service.LTTngControlServiceMI;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.service.LttngVersion;
import org.junit.Test;

/**
 * Tests for the MI service when using MI version 3.0
 */
@SuppressWarnings("javadoc")
public class LTTngControlServiceMi3LTTng211 extends LTTngControlServiceMiTest {

    private static final String MI_TEST_STREAM = "LTTngServiceMi3TestLTTng211.cfg";

    private static final String SCEN_GET_SESSION_FILTER_EXPRESSION = "GetSessionFilterExpression";
    private static final String SCEN_LIST_CONTEXT_211 = "ListContext211";

    @Override
    protected ILttngControlService getControlService() {
        try {
            return new LTTngControlServiceMI(getShell(), new LttngVersion("2.11.0"));
        } catch (ExecutionException e) {
            return null;
        }
    }

    @Override
    protected String getTestStream() {
        return MI_TEST_STREAM;
    }

    @Override
    public void testVersion() {
        try {
            fShell.setScenario(SCEN_LTTNG_VERSION);
            ILttngControlService service = LTTngControlServiceFactory.getLttngControlService(fShell);
            assertNotNull(service);
            assertEquals("2.11.0", service.getVersionString());
        } catch (ExecutionException e) {
            fail("Exeption thrown " + e);
        }
    }

    @Override
    public void testVersionWithPrompt() {
        try {
            fShell.setScenario(SCEN_LTTNG_VERSION_WITH_PROMPT);
            ILttngControlService service = LTTngControlServiceFactory.getLttngControlService(fShell);
            assertNotNull(service);
            assertEquals("2.11.0", service.getVersionString());
        } catch (ExecutionException e) {
            fail("Exeption thrown " + e);
        }
    }

    @Override
    public void testVersionCompiled() {
        try {
            fShell.setScenario(SCEN_LTTNG_COMPILED_VERSION);
            ILttngControlService service = LTTngControlServiceFactory.getLttngControlService(fShell);
            assertNotNull(service);
            assertEquals("2.11.0", service.getVersionString());
        } catch (ExecutionException e) {
            fail("Exeption thrown " + e);
        }
    }

    @Test
    public void testGetSessionWithFilterExpression() throws ExecutionException {
        fShell.setScenario(SCEN_GET_SESSION_FILTER_EXPRESSION);
        ISessionInfo session = fService.getSession("mysession", new NullProgressMonitor());

        // Verify Session
        assertNotNull(session);
        assertEquals("mysession", session.getName());
        assertEquals("/home/user/lttng-traces/mysession-20120129-084256", session.getSessionPath());
        assertEquals(TraceSessionState.ACTIVE, session.getSessionState());

        IDomainInfo[] domains = session.getDomains();
        assertNotNull(domains);
        assertEquals(2, domains.length);

        // Verify Kernel domain
        assertEquals("Kernel", domains[0].getName());
        IChannelInfo[] channels =  domains[0].getChannels();
        assertNotNull(channels);
        assertEquals(1, channels.length);

        // Verify Kernel's channel0
        assertEquals("channel0", channels[0].getName());
        assertEquals(4, channels[0].getNumberOfSubBuffers());
        assertEquals("splice()", channels[0].getOutputType().getInName());
        assertEquals(TraceChannelOutputType.SPLICE, channels[0].getOutputType());
        assertEquals(false, channels[0].isOverwriteMode());
        assertEquals(200, channels[0].getReadTimer());
        assertEquals(TraceEnablement.ENABLED, channels[0].getState());
        assertEquals(262144, channels[0].getSubBufferSize());
        assertEquals(0, channels[0].getSwitchTimer());

        // Verify event info
        IEventInfo[] channel0Events = channels[0].getEvents();
        assertNotNull(channel0Events);
        assertEquals(1, channel0Events.length);
        assertEquals("block_rq_remap", channel0Events[0].getName());
        assertEquals(TraceLogLevel.TRACE_EMERG, channel0Events[0].getLogLevel());
        assertEquals(TraceEventType.TRACEPOINT, channel0Events[0].getEventType());
        assertEquals(TraceEnablement.ENABLED, channel0Events[0].getState());
        assertEquals("foo > 10", channel0Events[0].getFilterExpression());

        // Verify domain UST global
        assertEquals("UST global", domains[1].getName());

        IChannelInfo[] ustChannels =  domains[1].getChannels();

        // Verify UST global's channel0
        assertEquals("channel0", ustChannels[0].getName());
        assertEquals(4, ustChannels[0].getNumberOfSubBuffers());
        assertEquals("mmap()", ustChannels[0].getOutputType().getInName());
        assertEquals(TraceChannelOutputType.MMAP, ustChannels[0].getOutputType());
        assertEquals(false, ustChannels[0].isOverwriteMode());
        assertEquals(200, ustChannels[0].getReadTimer());
        assertEquals(TraceEnablement.ENABLED, ustChannels[0].getState());
        assertEquals(4096, ustChannels[0].getSubBufferSize());
        assertEquals(0, ustChannels[0].getSwitchTimer());

        // Verify event info
        IEventInfo[] ustEvents = ustChannels[0].getEvents();
        assertEquals(1, ustEvents.length);

        assertEquals("ust_tests_hello:tptest_sighandler", ustEvents[0].getName());
        assertEquals(TraceLogLevel.TRACE_DEBUG_LINE, ustEvents[0].getLogLevel());
        assertEquals(TraceEventType.TRACEPOINT, ustEvents[0].getEventType());
        assertEquals(TraceEnablement.DISABLED, ustEvents[0].getState());
        assertEquals("foo > 10", ustEvents[0].getFilterExpression());
    }

    @Test
    public void testListContext211() throws ExecutionException {
        ((LTTngControlService)fService).setVersion("2.11.0");
        fShell.setScenario(SCEN_LIST_CONTEXT_211);

        List<String> availContexts = fService.getContextList(new NullProgressMonitor());
        assertNotNull(availContexts);
        assertEquals(12, availContexts.size());

        Set<String> expectedContexts = new HashSet<>();
        expectedContexts.add("pid");
        expectedContexts.add("procname");
        expectedContexts.add("prio");
        expectedContexts.add("nice");
        expectedContexts.add("vpid");
        expectedContexts.add("tid");
        expectedContexts.add("pthread_id");
        expectedContexts.add("vtid");
        expectedContexts.add("ppid");
        expectedContexts.add("vppid");
        expectedContexts.add("perf:cpu:cpu-cycles");
        expectedContexts.add("perf:cpu:cycles");

        assertTrue(expectedContexts.containsAll(availContexts));
    }
}
