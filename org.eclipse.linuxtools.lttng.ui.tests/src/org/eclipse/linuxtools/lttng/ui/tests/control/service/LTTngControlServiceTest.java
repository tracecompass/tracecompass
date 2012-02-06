/**********************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.lttng.ui.tests.control.service;

import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.linuxtools.lttng.stubs.service.CommandShellFactory;
import org.eclipse.linuxtools.lttng.ui.views.control.model.IBaseEventInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.IChannelInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.IDomainInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.IEventInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.ISessionInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.IUstProviderInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.TraceEnablement;
import org.eclipse.linuxtools.lttng.ui.views.control.model.TraceEventType;
import org.eclipse.linuxtools.lttng.ui.views.control.model.TraceLogLevel;
import org.eclipse.linuxtools.lttng.ui.views.control.model.TraceSessionState;
import org.eclipse.linuxtools.lttng.ui.views.control.service.ILttngControlService;
import org.eclipse.linuxtools.lttng.ui.views.control.service.LTTngControlService;
/**
 * The class <code>LTTngControlServiceTest</code> contains test for the class <code>{@link  LTTngControlService}</code>.
 */
@SuppressWarnings("nls")
public class LTTngControlServiceTest extends TestCase {
    // ------------------------------------------------------------------------
    // Test data
    // ------------------------------------------------------------------------
    private CommandShellFactory fShellFactory; 
    
   // ------------------------------------------------------------------------
    // Static methods
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------
    /**
     * Perform pre-test initialization.
     *
     * @throws Exception if the initialization fails for some reason
     *
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        fShellFactory = CommandShellFactory.getInstance();
    }

    /**
     * Perform post-test clean-up.
     *
     * @throws Exception if the clean-up fails for some reason
     *
     */
    @Override
    public void tearDown() throws Exception {
    }

    // ------------------------------------------------------------------------
    // Test Cases
    // ------------------------------------------------------------------------
    
    public void testGetSessionNames() {
        try {
            ILttngControlService service = new LTTngControlService(fShellFactory.getShellForLttngNotExistsShell());
            service.getSessionNames();
            fail("No exeption thrown");
            
        } catch (ExecutionException e) {
         // success
        }
    }
    
    public void testGetSessionNames1() {
        try {
            ILttngControlService service = new LTTngControlService(fShellFactory.getShellForNoSessionNames());
            String[] result = service.getSessionNames();

            assertNotNull(result);
            assertEquals(0, result.length);
            
        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }
    
    public void testGetSessionNames2() {
        try {
            ILttngControlService service = new LTTngControlService(fShellFactory.getShellForSessionNames());
            String[] result = service.getSessionNames();

            assertNotNull(result);
            assertEquals(2, result.length);
            assertEquals("mysession1", result[0]);
            assertEquals("mysession", result[1]);
            
        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }
    
    public void testGetSessionNotExist() {
        try {
            ILttngControlService service = new LTTngControlService(fShellFactory.getShellForSessionNotExists());
            service.getSessionNames();
            fail("No exeption thrown");
            
        } catch (ExecutionException e) {
            // success
        }
    }
    
    public void testGetSessionNameGarbage() {
        try {
            ILttngControlService service = new LTTngControlService(fShellFactory.getShellForSessionGarbage());
            String[] result = service.getSessionNames();

            assertNotNull(result);
            assertEquals(0, result.length);
            
        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }
    
    public void testGetSession1() {
        try {
            ILttngControlService service = new LTTngControlService(fShellFactory.getShellForSessionNames());
            ISessionInfo session = service.getSession("mysession");

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
            assertEquals(2, channels.length);

            // Verify Kernel's channel0 
            assertEquals("channel0", channels[0].getName());
            assertEquals(4, channels[0].getNumberOfSubBuffers());
            assertEquals("splice()", channels[0].getOutputType());
            assertEquals(false, channels[0].isOverwriteMode());
            assertEquals(200, channels[0].getReadTimer());
            assertEquals(TraceEnablement.ENABLED, channels[0].getState());
            assertEquals(262144, channels[0].getSubBufferSize());
            assertEquals(0, channels[0].getSwitchTimer());
            
            // Verify event info
            IEventInfo[] channel0Events = channels[0].getEvents();
            assertNotNull(channel0Events);
            assertEquals(2, channel0Events.length);
            assertEquals("block_rq_remap", channel0Events[0].getName());
            assertEquals(TraceLogLevel.TRACE_EMERG, channel0Events[0].getLogLevel());
            assertEquals(TraceEventType.TRACEPOINT, channel0Events[0].getEventType());
            assertEquals(TraceEnablement.ENABLED, channel0Events[0].getState());
            
            assertEquals("block_bio_remap", channel0Events[1].getName());
            assertEquals(TraceLogLevel.TRACE_EMERG, channel0Events[1].getLogLevel());
            assertEquals(TraceEventType.TRACEPOINT, channel0Events[1].getEventType());
            assertEquals(TraceEnablement.DISABLED, channel0Events[1].getState());
            
            // Verify Kernel's channel1 
            assertEquals("channel1", channels[1].getName());
            assertEquals(4, channels[1].getNumberOfSubBuffers());
            assertEquals("splice()", channels[1].getOutputType());
            assertEquals(true, channels[1].isOverwriteMode());
            assertEquals(400, channels[1].getReadTimer());
            assertEquals(TraceEnablement.DISABLED, channels[1].getState());
            assertEquals(524288, channels[1].getSubBufferSize());
            assertEquals(100, channels[1].getSwitchTimer());
            
            // Verify event info
            IEventInfo[] channel1Events = channels[1].getEvents();
            assertEquals(0, channel1Events.length);
            
            // Verify domain UST global
            assertEquals("UST global", domains[1].getName());
            
            IChannelInfo[] ustChannels =  domains[1].getChannels();
            
            // Verify UST global's mychannel1 
            assertEquals("mychannel1", ustChannels[0].getName());
            assertEquals(8, ustChannels[0].getNumberOfSubBuffers());
            assertEquals("mmap()", ustChannels[0].getOutputType());
            assertEquals(true, ustChannels[0].isOverwriteMode());
            assertEquals(100, ustChannels[0].getReadTimer());
            assertEquals(TraceEnablement.DISABLED, ustChannels[0].getState());
            assertEquals(8192, ustChannels[0].getSubBufferSize());
            assertEquals(200, ustChannels[0].getSwitchTimer());
            
            // Verify event info
            IEventInfo[] ustEvents = ustChannels[0].getEvents();
            assertEquals(0, ustEvents.length);

            // Verify UST global's channel0 
            assertEquals("channel0", ustChannels[1].getName());
            assertEquals(4, ustChannels[1].getNumberOfSubBuffers());
            assertEquals("mmap()", ustChannels[1].getOutputType());
            assertEquals(false, ustChannels[1].isOverwriteMode());
            assertEquals(200, ustChannels[1].getReadTimer());
            assertEquals(TraceEnablement.ENABLED, ustChannels[1].getState());
            assertEquals(4096, ustChannels[1].getSubBufferSize());
            assertEquals(0, ustChannels[1].getSwitchTimer());
            
            // Verify event info
            ustEvents = ustChannels[1].getEvents();
            assertEquals(2, ustEvents.length);
            
            assertEquals("ust_tests_hello:tptest_sighandler", ustEvents[0].getName());
            assertEquals(TraceLogLevel.TRACE_DEFAULT, ustEvents[0].getLogLevel());
            assertEquals(TraceEventType.TRACEPOINT, ustEvents[0].getEventType());
            assertEquals(TraceEnablement.DISABLED, ustEvents[0].getState());
            
            assertEquals("*", ustEvents[1].getName());
            assertEquals(TraceLogLevel.TRACE_EMERG, ustEvents[1].getLogLevel());
            assertEquals(TraceEventType.TRACEPOINT, ustEvents[1].getEventType());
            assertEquals(TraceEnablement.ENABLED, ustEvents[1].getState());
            
            // next session (no detailed information available)
            session = service.getSession("mysession1");
            assertNotNull(session);
            assertEquals("mysession1", session.getName());
            assertEquals("/home/user/lttng-traces/mysession1-20120203-133225", session.getSessionPath());
            assertEquals(TraceSessionState.INACTIVE, session.getSessionState());
            
            domains = session.getDomains();
            assertNotNull(domains);
            assertEquals(0, domains.length);
        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    public void testGetKernelProvider() {
        try {
            ILttngControlService service = new LTTngControlService(fShellFactory.getShellForSessionNames());
            List<IBaseEventInfo> events = service.getKernelProvider();

            // Verify event info
            assertNotNull(events);
            assertEquals(3, events.size());
            
            IBaseEventInfo baseEventInfo = (IBaseEventInfo) events.get(0);
            assertNotNull(baseEventInfo);
            assertEquals("sched_kthread_stop", baseEventInfo.getName());
            assertEquals(TraceLogLevel.TRACE_EMERG, baseEventInfo.getLogLevel());
            assertEquals(TraceEventType.TRACEPOINT, baseEventInfo.getEventType());
            
            baseEventInfo = (IBaseEventInfo) events.get(1);
            assertEquals("sched_kthread_stop_ret", baseEventInfo.getName());
            assertEquals(TraceLogLevel.TRACE_EMERG, baseEventInfo.getLogLevel());
            assertEquals(TraceEventType.TRACEPOINT, baseEventInfo.getEventType());
            
            baseEventInfo = (IBaseEventInfo) events.get(2);
            assertEquals("sched_wakeup_new", baseEventInfo.getName());
            assertEquals(TraceLogLevel.TRACE_EMERG, baseEventInfo.getLogLevel());
            assertEquals(TraceEventType.TRACEPOINT, baseEventInfo.getEventType());

        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    public void testGetUstProvider() {
        try {
            ILttngControlService service = new LTTngControlService(fShellFactory.getShellForSessionNames());
            List<IUstProviderInfo> providers = service.getUstProvider();

            // Check all providers
            assertNotNull(providers);
            assertEquals(2, providers.size());
            
            //Verify first provider
            assertEquals("/home/user/git/lttng-ust/tests/hello.cxx/.libs/lt-hello", providers.get(0).getName());
            assertEquals(9379, providers.get(0).getPid());
            
            // Verify event info
            IBaseEventInfo[] events = providers.get(0).getEvents();
            assertNotNull(events);
            assertEquals(2, events.length);

            IBaseEventInfo baseEventInfo = (IBaseEventInfo) events[0];
            assertNotNull(baseEventInfo);
            assertEquals("ust_tests_hello:tptest_sighandler", baseEventInfo.getName());
            assertEquals(TraceLogLevel.TRACE_MODULE, baseEventInfo.getLogLevel());
            assertEquals(TraceEventType.TRACEPOINT, baseEventInfo.getEventType());
            
            baseEventInfo = (IBaseEventInfo) events[1];
            assertEquals("ust_tests_hello:tptest", baseEventInfo.getName());
            assertEquals(TraceLogLevel.TRACE_INFO, baseEventInfo.getLogLevel());
            assertEquals(TraceEventType.TRACEPOINT, baseEventInfo.getEventType());

            //Verify second provider
            assertEquals("/home/user/git/lttng-ust/tests/hello.cxx/.libs/lt-hello", providers.get(1).getName());
            assertEquals(4852, providers.get(1).getPid());
            
            // Verify event info
            events = providers.get(1).getEvents();
            assertNotNull(events);
            assertEquals(2, events.length);

            baseEventInfo = (IBaseEventInfo) events[0];
            assertNotNull(baseEventInfo);
            assertEquals("ust_tests_hello:tptest_sighandler", baseEventInfo.getName());
            assertEquals(TraceLogLevel.TRACE_WARNING, baseEventInfo.getLogLevel());
            assertEquals(TraceEventType.TRACEPOINT, baseEventInfo.getEventType());
            
            baseEventInfo = (IBaseEventInfo) events[1];
            assertEquals("ust_tests_hello:tptest", baseEventInfo.getName());
            assertEquals(TraceLogLevel.TRACE_FUNCTION, baseEventInfo.getLogLevel());
            assertEquals(TraceEventType.TRACEPOINT, baseEventInfo.getEventType());

        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }
    
    public void testUstProvider2() {
        try {
            ILttngControlService service = new LTTngControlService(fShellFactory.getShellForNoUstProvider());
            List<IUstProviderInfo> providers = service.getUstProvider();

            assertNotNull(providers);
            assertEquals(0, providers.size());
            
        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }
    
}
