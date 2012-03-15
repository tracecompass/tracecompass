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
package org.eclipse.linuxtools.lttng2.ui.tests.control.model.component;

import java.io.File;
import java.net.URL;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.lttng2.stubs.service.TestRemoteSystemProxy;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.IConfirmDialog;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.ICreateChannelOnSessionDialog;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.ICreateSessionDialog;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.IGetEventInfoDialog;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.TraceControlDialogFactory;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IChannelInfo;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.TargetNodeState;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.TraceEnablement;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.TraceEventType;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.TraceLogLevel;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.TraceSessionState;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.BaseEventComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.ChannelInfo;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.KernelProviderComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TargetNodeComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceChannelComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceDomainComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceEventComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceSessionComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceSessionGroup;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.UstProviderComponent;
import org.eclipse.linuxtools.lttng2.ui.tests.Activator;
import org.eclipse.rse.core.model.Host;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.internal.core.model.SystemProfile;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Before;

/**
 * The class <code>TraceControlTreeModelTest</code> contains tests for the tree component classes.
 */
@SuppressWarnings("nls")
public class TraceControlProviderTests extends TestCase {
    
    private static final String DIRECTORY   = "testfiles";
    private static final String TEST_STREAM = "CreateTreeTest.cfg";
    private static final String SCEN_INIT_TEST = "Initialize";
    private static final String SCEN_SCENARIO1_TEST = "Scenario1";
    private static final String SCEN_SCENARIO2_TEST = "Scenario2";

    // ------------------------------------------------------------------------
    // Test data
    // ------------------------------------------------------------------------
    private TraceControlTestFacility fFacility;
    private TestRemoteSystemProxy fProxy;
    private String fTestFile; 
    
    // ------------------------------------------------------------------------
    // Static methods
    // ------------------------------------------------------------------------

    /**
     * Returns test setup used when executing test case stand-alone.
     * @return Test setup class 
     */
    public static Test suite() {
        return new ModelImplTestSetup(new TestSuite(TraceControlProviderTests.class));
    }

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    /**
     * Perform pre-test initialization.
     *
     * @throws Exception
     *         if the initialization fails for some reason
     *
     */
    @Override
    @Before
    public void setUp() throws Exception {
        fFacility = TraceControlTestFacility.getInstance();
        fProxy = new TestRemoteSystemProxy();
        URL location = FileLocator.find(Activator.getDefault().getBundle(), new Path(DIRECTORY + File.separator + TEST_STREAM), null);
        File testfile = new File(FileLocator.toFileURL(location).toURI());
        fTestFile = testfile.getAbsolutePath();
    }

    /**
     * Perform post-test clean-up.
     *
     * @throws Exception
     *         if the clean-up fails for some reason
     *
     */
    @Override
    @After
    public void tearDown()  throws Exception {
    }
    
    /**
     * Run the TraceControlComponent.
     */
    public void testTraceControlComponents()
        throws Exception {
        
        fProxy.setTestFile(fTestFile);
        fProxy.setScenario(SCEN_INIT_TEST);
        
        ITraceControlComponent root = TraceControlTestFacility.getInstance().getControlView().getTraceControlRoot();

        @SuppressWarnings("restriction")
        IHost host = new Host(new SystemProfile("myProfile", true));
        host.setHostName("127.0.0.1");

        TargetNodeComponent node = new TargetNodeComponent("myNode", root, host, fProxy);

        root.addChild(node);
        node.connect();

        fFacility.waitForJobs();

        // Verify that node is connected
        assertEquals(TargetNodeState.CONNECTED, node.getTargetNodeState());

        // Get provider groups
        ITraceControlComponent[] groups = node.getChildren();
        assertNotNull(groups);
        assertEquals(2, groups.length);

        // Get kernel provider
        ITraceControlComponent[] providers = groups[0].getChildren();
        KernelProviderComponent kernelProvider = (KernelProviderComponent) providers[0];

        // Get kernel provider events and select 2 events 
        ITraceControlComponent[] events = kernelProvider.getChildren();
        assertNotNull(events);
        assertEquals(3, events.length);

        BaseEventComponent baseEventInfo0 = (BaseEventComponent) events[0];
        BaseEventComponent baseEventInfo1  = (BaseEventComponent) events[1];

        // Initialize dialog implementations for command execution
        TraceControlDialogFactory.getInstance().setCreateSessionDialog(new CreateSessionDialogStub());
        TraceControlDialogFactory.getInstance().setGetEventInfoDialog(new GetEventInfoDialogStub());
        TraceControlDialogFactory.getInstance().setConfirmDialog(new DestroyConfirmDialogStub());

        // Initialize scenario
        fProxy.setScenario(SCEN_SCENARIO1_TEST);
 
        // ------------------------------------------------------------------------
        // Create session
        // ------------------------------------------------------------------------
        TraceSessionComponent session = createSession(groups[1]);
        
        // Verify that session was created
        assertNotNull(session);
        assertEquals("mysession", session.getName());
        assertEquals("/home/user/lttng-traces/mysession-20120314-132824", session.getSessionPath());
        assertEquals(TraceSessionState.INACTIVE, session.getSessionState());

        // ------------------------------------------------------------------------
        // Enable event on default channel on created session above
        // ------------------------------------------------------------------------
        ITraceControlComponent[] components =  { baseEventInfo0, baseEventInfo1 };

        fFacility.getControlView().setSelection(components);
        // Give GUI time to actually execute refresh
        fFacility.delay(TraceControlTestFacility.GUI_REFESH_DELAY);
        
        fFacility.executeCommand("assign.event");
        fFacility.waitForJobs();
        
        // Verify that kernel domain was created
        ITraceControlComponent[] domains = session.getChildren();
        assertNotNull(domains);
        assertEquals(1, domains.length);

        assertEquals("Kernel", domains[0].getName());
        
        // Verify that channel0 was created with default values
        ITraceControlComponent[] channels =  domains[0].getChildren();
        assertNotNull(channels);
        assertEquals(1, channels.length);

        assertTrue(channels[0] instanceof TraceChannelComponent);
        TraceChannelComponent channel = (TraceChannelComponent) channels[0]; 
        assertEquals("channel0", channel.getName());
        assertEquals(4, channel.getNumberOfSubBuffers());
        assertEquals("splice()", channel.getOutputType());
        assertEquals(false, channel.isOverwriteMode());
        assertEquals(200, channel.getReadTimer());
        assertEquals(TraceEnablement.ENABLED, channel.getState());
        assertEquals(262144, channel.getSubBufferSize());
        assertEquals(0, channel.getSwitchTimer());

        // Verify that event components were created
        ITraceControlComponent[] channel0Events = channel.getChildren();
        assertNotNull(channel0Events);
        assertEquals(2, channel0Events.length);
        assertTrue(channel0Events[0] instanceof TraceEventComponent);
        assertTrue(channel0Events[1] instanceof TraceEventComponent);

        TraceEventComponent event = (TraceEventComponent) channel0Events[0];
        assertEquals("sched_kthread_stop_ret", event.getName());
        assertEquals(TraceLogLevel.TRACE_EMERG, event.getLogLevel());
        assertEquals(TraceEventType.TRACEPOINT, event.getEventType());
        assertEquals(TraceEnablement.ENABLED, event.getState());

        TraceEventComponent event1 = (TraceEventComponent) channel0Events[1];
        assertEquals("sched_kthread_stop", event1.getName());
        assertEquals(TraceLogLevel.TRACE_EMERG, event1.getLogLevel());
        assertEquals(TraceEventType.TRACEPOINT, event1.getEventType());
        assertEquals(TraceEnablement.ENABLED, event1.getState());

        // ------------------------------------------------------------------------
        // Disable event components 
        // ------------------------------------------------------------------------
        ITraceControlComponent[] selection = { event, event1 };
        fFacility.getControlView().setSelection(selection);
        // Give GUI time to actually execute the selection
        fFacility.delay(TraceControlTestFacility.GUI_REFESH_DELAY);
        fFacility.executeCommand("disable.event");
        fFacility.waitForJobs();
        
        assertEquals(TraceEnablement.DISABLED, event.getState());
        assertEquals(TraceEnablement.DISABLED, event1.getState());

        // ------------------------------------------------------------------------
        // Enable event component 
        // ------------------------------------------------------------------------
        fFacility.getControlView().setSelection(event1);
        fFacility.executeCommand("enable.event");
        fFacility.waitForJobs();
        
        // Verify event state
        assertEquals(TraceEnablement.ENABLED, event1.getState());
        
        // ------------------------------------------------------------------------
        // Destroy session 
        // ------------------------------------------------------------------------
        destroySession(session);
        
        // Verify that no more session components exist
        assertEquals(0, groups[1].getChildren().length);
        

        // ------------------------------------------------------------------------
        // Create session
        // ------------------------------------------------------------------------
        fProxy.setScenario(SCEN_SCENARIO2_TEST);
        
        CreateSessionDialogStub sessionDialogStub = new CreateSessionDialogStub();
        sessionDialogStub.setSessionPath("/home/user/temp");
        TraceControlDialogFactory.getInstance().setCreateSessionDialog(sessionDialogStub);

        session = createSession(groups[1]);
        
        // Verify that session was created
        assertNotNull(session);
        assertEquals("mysession", session.getName());
        assertEquals("/home/user/temp", session.getSessionPath());
        assertEquals(TraceSessionState.INACTIVE, session.getSessionState());
        
        // ------------------------------------------------------------------------
        // Create Channel on UST global domain
        // ------------------------------------------------------------------------
        TraceControlDialogFactory.getInstance().setCreateChannelOnSessionDialog(new CreateChannelOnSessionDialogStub());

        fFacility.getControlView().setSelection(session);
        // Give GUI time to actually execute refresh
        fFacility.delay(TraceControlTestFacility.GUI_REFESH_DELAY);

        fFacility.executeCommand("createChannelOnSession");
        fFacility.waitForJobs();
        
        // Verify that UST domain was created
        domains = session.getChildren();
        assertNotNull(domains);
        assertEquals(1, domains.length);

        assertEquals("UST global", domains[0].getName());
        
        // Verify that channel was created with correct data
        channels =  domains[0].getChildren();
        assertNotNull(channels);
        assertEquals(1, channels.length);

        assertTrue(channels[0] instanceof TraceChannelComponent);
        channel = (TraceChannelComponent) channels[0]; 
        assertEquals("mychannel", channel.getName());
        assertEquals(2, channel.getNumberOfSubBuffers());
        assertEquals("mmap()", channel.getOutputType());
        assertEquals(false, channel.isOverwriteMode());
        assertEquals(100, channel.getReadTimer());
        assertEquals(TraceEnablement.ENABLED, channel.getState());
        assertEquals(16384, channel.getSubBufferSize());
        assertEquals(200, channel.getSwitchTimer());

        // ------------------------------------------------------------------------
        // Enable event on default channel on created session above
        // ------------------------------------------------------------------------
        // Get first UST provider
        UstProviderComponent ustProvider = (UstProviderComponent) providers[1];
        assertEquals("/home/user/git/lttng-ust/tests/hello.cxx/.libs/lt-hello", ustProvider.getName());
        assertEquals(9379, ustProvider.getPid());

        // Get events
        events = ustProvider.getChildren();
        assertNotNull(events);
        assertEquals(2, events.length);

        baseEventInfo0 = (BaseEventComponent) events[0];
        baseEventInfo1 = (BaseEventComponent) events[1];

        ITraceControlComponent[] ustSelection =  { baseEventInfo0, baseEventInfo1 };

        fFacility.getControlView().setSelection(ustSelection);
        // Give GUI time to actually execute refresh
        fFacility.delay(TraceControlTestFacility.GUI_REFESH_DELAY);
        fFacility.executeCommand("assign.event");
        fFacility.waitForJobs();

        // verify that events were created under the channel
        // Note that domain and channel has to be re-read because the tree is re-created

        domains = session.getChildren();

        // Verify that channel was created with correct data
        channels =  domains[0].getChildren();

        ITraceControlComponent[] ustEvents = channels[0].getChildren();
        assertEquals(2, ustEvents.length);

        event = (TraceEventComponent) ustEvents[0];
        assertEquals("ust_tests_hello:tptest_sighandler", event.getName());
        assertEquals(TraceLogLevel.TRACE_DEBUG_LINE, event.getLogLevel());
        assertEquals(TraceEventType.TRACEPOINT, event.getEventType());
        assertEquals(TraceEnablement.ENABLED, event.getState());

        event = (TraceEventComponent) ustEvents[1];
        assertEquals("ust_tests_hello:tptest", ustEvents[1].getName());
        assertEquals(TraceLogLevel.TRACE_DEBUG_LINE, event.getLogLevel());
        assertEquals(TraceEventType.TRACEPOINT, event.getEventType());
        assertEquals(TraceEnablement.ENABLED, event.getState());

        // ------------------------------------------------------------------------
        // Disable event components 
        // ------------------------------------------------------------------------
        fFacility.getControlView().setSelection(event);
        // Give GUI time to actually execute the selection
        fFacility.delay(TraceControlTestFacility.GUI_REFESH_DELAY);
        fFacility.executeCommand("disable.event");
        fFacility.waitForJobs();
        
        assertEquals(TraceEnablement.DISABLED, event.getState());

        // ------------------------------------------------------------------------
        // Enable event component 
        // ------------------------------------------------------------------------
        fFacility.getControlView().setSelection(event);
        fFacility.executeCommand("enable.event");
        fFacility.waitForJobs();
        
        // Verify event state
        assertEquals(TraceEnablement.ENABLED, event.getState());
        
        // ------------------------------------------------------------------------
        // Destroy session 
        // ------------------------------------------------------------------------
        destroySession(session);
        
        // Verify that no more session components exist
        assertEquals(0, groups[1].getChildren().length);

        
//        fFacility.delay(60000);
    }

    private TraceSessionComponent createSession(ITraceControlComponent group) throws ExecutionException, NotDefinedException, NotEnabledException, NotHandledException {
        fFacility.getControlView().setSelection(group);
        // Give GUI time to actually execute the selection
        fFacility.delay(TraceControlTestFacility.GUI_REFESH_DELAY);
        fFacility.executeCommand("createSession");
        fFacility.waitForJobs();
        
        ITraceControlComponent[] sessions = group.getChildren();
        if ((sessions == null) || (sessions.length == 0)) {
            return null;
        }
        return (TraceSessionComponent)sessions[0];
    }

    private void destroySession(TraceSessionComponent session) throws ExecutionException, NotDefinedException, NotEnabledException, NotHandledException {
        fFacility.getControlView().setSelection(session);
        // Give GUI time to actually execute the selection
        fFacility.delay(TraceControlTestFacility.GUI_REFESH_DELAY);
        fFacility.executeCommand("destroySession");
        fFacility.waitForJobs();
    }      
        
//        assertEquals("sched_kthread_stop_ret", baseEventInfo.getName());
//        assertEquals(TraceLogLevel.TRACE_EMERG, baseEventInfo.getLogLevel());
//        assertEquals(TraceEventType.TRACEPOINT, baseEventInfo.getEventType());
//
//        baseEventInfo = (BaseEventComponent) events[2];
//        assertEquals("sched_wakeup_new", baseEventInfo.getName());
//        assertEquals(TraceLogLevel.TRACE_EMERG, baseEventInfo.getLogLevel());
//        assertEquals(TraceEventType.TRACEPOINT, baseEventInfo.getEventType());
//
//        // ------------------------------------------------------------------------
//        // Verify UstProviderComponent
//        // ------------------------------------------------------------------------
//        UstProviderComponent ustProvider = (UstProviderComponent) providers[1];
//        assertEquals("/home/user/git/lttng-ust/tests/hello.cxx/.libs/lt-hello", ustProvider.getName());
//        assertEquals(9379, ustProvider.getPid());
//
//        // ------------------------------------------------------------------------
//        // Verify event info (UST provider)
//        // ------------------------------------------------------------------------
//        events = ustProvider.getChildren();
//        assertNotNull(events);
//        assertEquals(2, events.length);
//
//        baseEventInfo = (BaseEventComponent) events[0];
//        assertNotNull(baseEventInfo);
//        assertEquals("ust_tests_hello:tptest_sighandler", baseEventInfo.getName());
//        assertEquals(TraceLogLevel.TRACE_DEBUG_MODULE, baseEventInfo.getLogLevel());
//        assertEquals(TraceEventType.TRACEPOINT, baseEventInfo.getEventType());
//
//        baseEventInfo = (BaseEventComponent) events[1];
//        assertEquals("ust_tests_hello:tptest", baseEventInfo.getName());
//        assertEquals(TraceLogLevel.TRACE_INFO, baseEventInfo.getLogLevel());
//        assertEquals(TraceEventType.TRACEPOINT, baseEventInfo.getEventType());
//
//        // ------------------------------------------------------------------------
//        // Verify UstProviderComponent
//        // ------------------------------------------------------------------------
//        ustProvider = (UstProviderComponent) providers[2];
//        assertEquals("/home/user/git/lttng-ust/tests/hello.cxx/.libs/lt-hello", ustProvider.getName());
//        assertEquals(4852, ustProvider.getPid());
//        
//        // verify getters and setter
//        verifyUstProviderGettersSetters(ustProvider);
//
//        // ------------------------------------------------------------------------
//        // Verify event info (UST provider)
//        // ------------------------------------------------------------------------
//        events = ustProvider.getChildren();
//        assertNotNull(events);
//        assertEquals(2, events.length);
//
//        baseEventInfo = (BaseEventComponent) events[0];
//        assertNotNull(baseEventInfo);
//        assertEquals("ust_tests_hello:tptest_sighandler", baseEventInfo.getName());
//        assertEquals(TraceLogLevel.TRACE_WARNING, baseEventInfo.getLogLevel());
//        assertEquals(TraceEventType.TRACEPOINT, baseEventInfo.getEventType());
//
//        baseEventInfo = (BaseEventComponent) events[1];
//        assertEquals("ust_tests_hello:tptest", baseEventInfo.getName());
//        assertEquals(TraceLogLevel.TRACE_DEBUG_FUNCTION, baseEventInfo.getLogLevel());
//        assertEquals(TraceEventType.TRACEPOINT, baseEventInfo.getEventType());
//        
//        // verify getters and setters
//        verifyBaseEventGettersSetters(baseEventInfo);
//        
//        // ------------------------------------------------------------------------
//        // Verify TraceSessionGroup
//        // ------------------------------------------------------------------------
//        ITraceControlComponent[] sessions = groups[1].getChildren();
//        assertNotNull(sessions);
//        assertEquals(2, sessions.length);
//        for (int i = 0; i < sessions.length; i++) {
//            assertTrue(sessions[i] instanceof TraceSessionComponent);
//        }
//        assertEquals("mysession1", sessions[0].getName());
//        assertEquals("mysession", sessions[1].getName());
//
//        // ------------------------------------------------------------------------
//        // Verify TraceSessionComponent
//        // ------------------------------------------------------------------------
//        TraceSessionComponent session = (TraceSessionComponent)sessions[1];
//        assertEquals("mysession", session.getName());
//        assertEquals("/home/user/lttng-traces/mysession-20120129-084256", session.getSessionPath());
//        assertEquals(TraceSessionState.ACTIVE, session.getSessionState());
//
//        // Verify setters and setters
//        verifySessionGetterSetters(session);
//        
//        ITraceControlComponent[] domains = session.getChildren();
//        assertNotNull(domains);
//        assertEquals(2, domains.length);
//        
//        // ------------------------------------------------------------------------
//        // Verify Kernel domain
//        // ------------------------------------------------------------------------
//        assertEquals("Kernel", domains[0].getName());
//        ITraceControlComponent[] channels =  domains[0].getChildren();
//        assertNotNull(channels);
//        assertEquals(2, channels.length);
//
//        // ------------------------------------------------------------------------
//        // Verify Kernel's channel0
//        // ------------------------------------------------------------------------
//        assertTrue(channels[0] instanceof TraceChannelComponent);
//        TraceChannelComponent channel = (TraceChannelComponent) channels[0]; 
//        assertEquals("channel0", channel.getName());
//        assertEquals(4, channel.getNumberOfSubBuffers());
//        assertEquals("splice()", channel.getOutputType());
//        assertEquals(false, channel.isOverwriteMode());
//        assertEquals(200, channel.getReadTimer());
//        assertEquals(TraceEnablement.ENABLED, channel.getState());
//        assertEquals(262144, channel.getSubBufferSize());
//        assertEquals(0, channel.getSwitchTimer());
//
//        // ------------------------------------------------------------------------
//        // Verify event info (kernel, channel0)
//        // ------------------------------------------------------------------------
//        ITraceControlComponent[] channel0Events = channel.getChildren();
//        assertNotNull(channel0Events);
//        assertEquals(5, channel0Events.length);
//        assertTrue(channel0Events[0] instanceof TraceEventComponent);
//        assertTrue(channel0Events[1] instanceof TraceEventComponent);
//        assertTrue(channel0Events[2] instanceof TraceProbeEventComponent);
//        assertTrue(channel0Events[3] instanceof TraceProbeEventComponent);
//        assertTrue(channel0Events[4] instanceof TraceEventComponent);
//        
//        TraceEventComponent event = (TraceEventComponent) channel0Events[0];
//        assertEquals("block_rq_remap", event.getName());
//        assertEquals(TraceLogLevel.TRACE_EMERG, event.getLogLevel());
//        assertEquals(TraceEventType.TRACEPOINT, event.getEventType());
//        assertEquals(TraceEnablement.ENABLED, event.getState());
//        
//        event = (TraceEventComponent) channel0Events[1];
//        assertEquals("block_bio_remap", event.getName());
//        assertEquals(TraceLogLevel.TRACE_EMERG, event.getLogLevel());
//        assertEquals(TraceEventType.TRACEPOINT, event.getEventType());
//        assertEquals(TraceEnablement.DISABLED, event.getState());
//
//        TraceProbeEventComponent probeEvent = (TraceProbeEventComponent) channel0Events[2];
//        assertEquals("myevent2", probeEvent.getName());
//        assertEquals(TraceLogLevel.LEVEL_UNKNOWN, probeEvent.getLogLevel());
//        assertEquals(TraceEventType.PROBE, probeEvent.getEventType());
//        assertEquals(TraceEnablement.ENABLED, probeEvent.getState());
//        assertEquals("0xc0101340", probeEvent.getAddress());
//        assertNull(probeEvent.getOffset());
//        assertNull(probeEvent.getSymbol());
//
//        probeEvent = (TraceProbeEventComponent) channel0Events[3];
//        assertEquals("myevent0", probeEvent.getName());
//        assertEquals(TraceLogLevel.LEVEL_UNKNOWN, probeEvent.getLogLevel());
//        assertEquals(TraceEventType.PROBE, probeEvent.getEventType());
//        assertEquals(TraceEnablement.ENABLED, probeEvent.getState());
//        assertNull(probeEvent.getAddress());
//        assertEquals("0x0", probeEvent.getOffset());
//        assertEquals("init_post", probeEvent.getSymbol());
//        
//        event = (TraceEventComponent) channel0Events[4];
//        assertEquals("syscalls", event.getName());
//        assertEquals(TraceLogLevel.LEVEL_UNKNOWN, event.getLogLevel());
//        assertEquals(TraceEventType.SYSCALL, event.getEventType());
//        assertEquals(TraceEnablement.ENABLED, event.getState());
//        
//        // ------------------------------------------------------------------------
//        // Verify Kernel's channel1
//        // ------------------------------------------------------------------------
//        assertEquals("channel1", channels[1].getName());
//        channel = (TraceChannelComponent) channels[1]; 
//        assertEquals(4, channel.getNumberOfSubBuffers());
//        assertEquals("splice()", channel.getOutputType());
//        assertEquals(true, channel.isOverwriteMode());
//        assertEquals(400, channel.getReadTimer());
//        assertEquals(TraceEnablement.DISABLED, channel.getState());
//        assertEquals(524288, channel.getSubBufferSize());
//        assertEquals(100, channel.getSwitchTimer());
//
//        // ------------------------------------------------------------------------
//        // Verify event info (kernel, channel1)
//        // ------------------------------------------------------------------------
//        ITraceControlComponent[] channel1Events = channels[1].getChildren();
//        assertEquals(0, channel1Events.length);
//
//        // ------------------------------------------------------------------------
//        // Verify domain UST global
//        // ------------------------------------------------------------------------
//        assertEquals("UST global", domains[1].getName());
//        
//        ITraceControlComponent[] ustChannels = domains[1].getChildren();
//        
//        for (int i = 0; i < ustChannels.length; i++) {
//            assertTrue(ustChannels[i] instanceof TraceChannelComponent);
//        }
//
//        // ------------------------------------------------------------------------
//        // Verify UST global's mychannel1
//        // ------------------------------------------------------------------------
//        channel = (TraceChannelComponent) ustChannels[0]; 
//        assertEquals("mychannel1", channel.getName());
//        assertEquals(8, channel.getNumberOfSubBuffers());
//        assertEquals("mmap()", channel.getOutputType());
//        assertEquals(true, channel.isOverwriteMode());
//        assertEquals(100, channel.getReadTimer());
//        assertEquals(TraceEnablement.DISABLED, channel.getState());
//        assertEquals(8192, channel.getSubBufferSize());
//        assertEquals(200, channel.getSwitchTimer());
//        
//        // verify getters and setters 
//        verifyChannelGettersSetters(channel);
//
//        // ------------------------------------------------------------------------
//        // Verify event info (UST global, mychannel1)
//        // ------------------------------------------------------------------------
//        ITraceControlComponent[] ustEvents = channel.getChildren();
//        assertEquals(0, ustEvents.length);
//
//        // ------------------------------------------------------------------------
//        // Verify UST global's channel0
//        // ------------------------------------------------------------------------
//        channel = (TraceChannelComponent) ustChannels[1];
//        assertEquals("channel0", channel.getName());
//        assertEquals(4, channel.getNumberOfSubBuffers());
//        assertEquals("mmap()", channel.getOutputType());
//        assertEquals(false, channel.isOverwriteMode());
//        assertEquals(200, channel.getReadTimer());
//        assertEquals(TraceEnablement.ENABLED, channel.getState());
//        assertEquals(4096, channel.getSubBufferSize());
//        assertEquals(0, channel.getSwitchTimer());
//
//        // ------------------------------------------------------------------------
//        // Verify event info (UST global, channel0)
//        // ------------------------------------------------------------------------
//        ustEvents = channel.getChildren();
//        assertEquals(2, ustEvents.length);
//        
//        event = (TraceEventComponent) ustEvents[0];
//        assertEquals("ust_tests_hello:tptest_sighandler", event.getName());
//        assertEquals(TraceLogLevel.TRACE_DEBUG_LINE, event.getLogLevel());
//        assertEquals(TraceEventType.TRACEPOINT, event.getEventType());
//        assertEquals(TraceEnablement.DISABLED, event.getState());
//        
//        event = (TraceEventComponent) ustEvents[1];
//        assertEquals("*", ustEvents[1].getName());
//        assertEquals(TraceLogLevel.LEVEL_UNKNOWN, event.getLogLevel());
//        assertEquals(TraceEventType.TRACEPOINT, event.getEventType());
//        assertEquals(TraceEnablement.ENABLED, event.getState());
//        
//        // verify getters and setters
//        verifyEventGettersSetters(event);
//        
//        // disconnect
//        node.disconnect();
//        assertEquals(TargetNodeState.DISCONNECTED, node.getTargetNodeState());
//        assertNotNull(node.getImage());
//        assertNotSame(connectedImage, node.getImage());
//    }
//    
//    private void verifySessionGetterSetters(TraceSessionComponent session) {
//        // save original values
//        String name = session.getName();
//        String origPath = session.getSessionPath();
//        TraceSessionState origState = session.getSessionState();
//
//        // test cases
//        session.setName("newName");
//        assertEquals("newName", session.getName());
//        
//        session.setSessionPath("/home/user/tmp");
//        assertEquals("/home/user/tmp", session.getSessionPath());
//
//        session.setSessionState(TraceSessionState.INACTIVE);
//        assertEquals(TraceSessionState.INACTIVE, session.getSessionState());
//        Image inactiveImage = session.getImage();
//        assertNotNull(inactiveImage);
//        
//        session.setSessionState("active");
//        assertEquals(TraceSessionState.ACTIVE, session.getSessionState());
//
//        Image activeImage = session.getImage();
//        assertNotNull(activeImage);
//        assertNotSame(activeImage, inactiveImage);
//
//        
//        // restore original values      
//        session.setName(name);
//        session.setSessionPath(origPath);
//        session.setSessionState(origState);
//    }
//
//    private void verifyBaseEventGettersSetters(BaseEventComponent event) {
//        // save original values
//        String name =  event.getName();
//        TraceLogLevel level = event.getLogLevel();
//        TraceEventType type = event.getEventType();
//        
//        // test cases
//        event.setName("newName");
//        assertEquals("newName", event.getName());
//        
//        event.setLogLevel(TraceLogLevel.TRACE_INFO);
//        assertEquals(TraceLogLevel.TRACE_INFO, event.getLogLevel());
//        event.setLogLevel("TRACE_ALERT");
//        assertEquals(TraceLogLevel.TRACE_ALERT, event.getLogLevel());
//
//        event.setEventType(TraceEventType.UNKNOWN);
//        assertEquals(TraceEventType.UNKNOWN, event.getEventType());
//        event.setEventType("tracepoint");
//        assertEquals(TraceEventType.TRACEPOINT, event.getEventType());
//        
//        // restore original values
//        event.setName(name);
//        event.setLogLevel(level);
//        event.setEventType(type);
//    }
//    
//    private void verifyEventGettersSetters(TraceEventComponent event) {
//        // save original values
//        String name =  event.getName();
//        TraceLogLevel level = event.getLogLevel();
//        TraceEventType type = event.getEventType();
//        TraceEnablement state = event.getState();
//        
//        // test cases
//        event.setName("newName");
//        assertEquals("newName", event.getName());
//        
//        event.setLogLevel(TraceLogLevel.TRACE_INFO);
//        assertEquals(TraceLogLevel.TRACE_INFO, event.getLogLevel());
//        event.setLogLevel("TRACE_ALERT");
//        assertEquals(TraceLogLevel.TRACE_ALERT, event.getLogLevel());
//
//        event.setEventType(TraceEventType.UNKNOWN);
//        assertEquals(TraceEventType.UNKNOWN, event.getEventType());
//        event.setEventType("tracepoint");
//        assertEquals(TraceEventType.TRACEPOINT, event.getEventType());
//        
//        event.setState("disabled");
//        assertEquals(TraceEnablement.DISABLED, event.getState());
//        
//        Image disabledImage = event.getImage();
//        assertNotNull(disabledImage);
//
//        event.setState(TraceEnablement.ENABLED);
//        assertEquals(TraceEnablement.ENABLED, event.getState());
//
//        Image enabledImage = event.getImage();
//        assertNotNull(enabledImage);
//        assertNotSame(enabledImage, disabledImage);
//
//        // restore original values
//        event.setName(name);
//        event.setLogLevel(level);
//        event.setEventType(type);
//        event.setState(state);
//    }
//    
//    private void verifyChannelGettersSetters(TraceChannelComponent channel) {
//        // save original values
//        String name = channel.getName();
//        int nbSubBuffers = channel.getNumberOfSubBuffers();
//        String type = channel.getOutputType();
//        boolean mode = channel.isOverwriteMode();
//        long readTimer = channel.getReadTimer();
//        TraceEnablement state =  channel.getState();
//        long subBufferSize = channel.getSubBufferSize();
//        long switchTimer = channel.getSwitchTimer();
//        
//        // test cases
//        channel.setName("newName");
//        assertEquals("newName", channel.getName());
//        
//        channel.setNumberOfSubBuffers(2);
//        assertEquals(2, channel.getNumberOfSubBuffers());
//        
//        channel.setOutputType("splice()");
//        assertEquals("splice()", channel.getOutputType());
//        
//        channel.setOverwriteMode(false);
//        assertEquals(false, channel.isOverwriteMode());
//        
//        channel.setReadTimer(250);
//        assertEquals(250, channel.getReadTimer());
//        
//        channel.setState("enabled");
//        assertEquals(TraceEnablement.ENABLED, channel.getState());
//        
//        Image enabledImage = channel.getImage();
//        assertNotNull(enabledImage);
//        channel.setState(TraceEnablement.DISABLED);
//        assertEquals(TraceEnablement.DISABLED, channel.getState());
//
//        Image disabledImage = channel.getImage();
//        assertNotNull(disabledImage);
//        assertNotSame(enabledImage, disabledImage);
//        
//        channel.setSubBufferSize(1024);
//        assertEquals(1024, channel.getSubBufferSize());
//        
//        channel.setSwitchTimer(1000);
//        assertEquals(1000, channel.getSwitchTimer());
//
//        // restore original values
//        channel.setName(name);
//        channel.setNumberOfSubBuffers(nbSubBuffers);
//        channel.setOutputType(type);
//        channel.setOverwriteMode(mode);
//        channel.setReadTimer(readTimer);
//        channel.setState(state);
//        channel.setSubBufferSize(subBufferSize);
//        channel.setSwitchTimer(switchTimer);
//    }
//    
//    private void verifyUstProviderGettersSetters(UstProviderComponent ustProvider) {
//        // save original values
//        String name = ustProvider.getName();
//        int pid = ustProvider.getPid();
//        
//        // test cases
//        ustProvider.setName("newName");
//        assertEquals("newName", ustProvider.getName());
//
//        ustProvider.setPid(9876);
//        assertEquals(9876, ustProvider.getPid());
//        
//        // restore original values
//        ustProvider.setName(name);
//        ustProvider.setPid(pid);
//    }

    public class CreateSessionDialogStub implements ICreateSessionDialog {
        public String fPath = null;
        
        @Override
        public String getSessionName() {
            return "mysession";
        }

        @Override
        public String getSessionPath() {
            return fPath;
        }

        @Override
        public boolean isDefaultSessionPath() {
            return fPath == null;
        }

        @Override
        public void setTraceSessionGroup(TraceSessionGroup group) {

        }

        @Override
        public int open() {
            return 0;
        }
        
        public void setSessionPath(String path) {
            fPath = path;
        }
    }

    public class GetEventInfoDialogStub implements IGetEventInfoDialog {

        private TraceSessionComponent[] fSessions;
        
        @Override
        public TraceSessionComponent getSession() {
            return fSessions[0];
        }

        @Override
        public TraceChannelComponent getChannel() {
            return null;
        }

        @Override
        public void setIsKernel(boolean isKernel) {
        }

        @Override
        public void setSessions(TraceSessionComponent[] sessions) {
            fSessions = sessions;
        }

        @Override
        public int open() {
            return 0;
        }
    }
    
    public class DestroyConfirmDialogStub implements IConfirmDialog {

        @Override
        public boolean openConfirm(Shell parent, String title, String message) {
            return true;
        }
    }
    
    public class CreateChannelOnSessionDialogStub implements ICreateChannelOnSessionDialog {

        @Override
        public IChannelInfo getChannelInfo() {
            ChannelInfo info = new ChannelInfo("mychannel");
            info.setNumberOfSubBuffers(2);
            info.setOverwriteMode(false);
            info.setReadTimer(100);
            info.setSwitchTimer(200);
            info.setSubBufferSize(16384);
            return info;
        }

        @Override
        public void setDomainComponent(TraceDomainComponent domain) {
        }

        @Override
        public int open() {
            return 0;
        }

        @Override
        public boolean isKernel() {
            return false;
        }
        
    }
}