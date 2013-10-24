/**********************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Alexandre Montplaisir - Port to JUnit4
 **********************************************************************/

package org.eclipse.linuxtools.lttng2.ui.tests.control.model.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.TargetNodeState;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.TraceEnablement;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.TraceEventType;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.TraceLogLevel;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.TraceSessionState;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.impl.ChannelInfo;
import org.eclipse.linuxtools.internal.lttng2.stubs.dialogs.AddContextDialogStub;
import org.eclipse.linuxtools.internal.lttng2.stubs.dialogs.CreateSessionDialogStub;
import org.eclipse.linuxtools.internal.lttng2.stubs.dialogs.DestroyConfirmDialogStub;
import org.eclipse.linuxtools.internal.lttng2.stubs.dialogs.EnableChannelDialogStub;
import org.eclipse.linuxtools.internal.lttng2.stubs.dialogs.EnableEventsDialogStub;
import org.eclipse.linuxtools.internal.lttng2.stubs.dialogs.GetEventInfoDialogStub;
import org.eclipse.linuxtools.internal.lttng2.stubs.service.TestRemoteSystemProxy;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.TraceControlDialogFactory;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TargetNodeComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceChannelComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceEventComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceProbeEventComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceSessionComponent;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.FrameworkUtil;

/**
 * The class <code>TraceControlKernelSessionTests</code> contains Kernel
 * session/channel/event handling test cases.
 */
public class TraceControlKernelSessionTests {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final String TEST_STREAM = "CreateTreeTest.cfg";
    private static final String SCEN_SCENARIO3_TEST = "Scenario3";

    // ------------------------------------------------------------------------
    // Test data
    // ------------------------------------------------------------------------

    private TraceControlTestFacility fFacility;
    private TestRemoteSystemProxy fProxy;
    private String fTestFile;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    /**
     * Perform pre-test initialization.
     *
     * @throws Exception
     *         if the initialization fails for some reason
     */
    @Before
    public void setUp() throws Exception {
        fFacility = TraceControlTestFacility.getInstance();
        fFacility.init();
        fProxy = new TestRemoteSystemProxy();
        URL location = FileLocator.find(FrameworkUtil.getBundle(this.getClass()), new Path(TraceControlTestFacility.DIRECTORY + File.separator + TEST_STREAM), null);
        File testfile = new File(FileLocator.toFileURL(location).toURI());
        fTestFile = testfile.getAbsolutePath();
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        fFacility.waitForJobs();
        fFacility.dispose();
    }

    /**
     * Run the TraceControlComponent.
     *
     * @throws Exception
     *             Would fail the test
     */
    @Test
    public void testTraceSessionTree() throws Exception {

        fProxy.setTestFile(fTestFile);
        fProxy.setScenario(TraceControlTestFacility.SCEN_INIT_TEST);

        ITraceControlComponent root = fFacility.getControlView().getTraceControlRoot();

        ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
        ISystemProfile profile =  registry.createSystemProfile("myProfile", true);
        IHost host = registry.createLocalHost(profile, "myProfile", "user");

        TargetNodeComponent node = new TargetNodeComponent("myNode", root, host, fProxy);

        root.addChild(node);
        fFacility.waitForJobs();

        fFacility.executeCommand(node, "connect");
        int i = 0;
        while ((i < 10) && (node.getTargetNodeState() != TargetNodeState.CONNECTED)) {
            i++;
            fFacility.delay(TraceControlTestFacility.GUI_REFESH_DELAY);
        }

        // Verify that node is connected
        assertEquals(TargetNodeState.CONNECTED, node.getTargetNodeState());

        // Get provider groups
        ITraceControlComponent[] groups = node.getChildren();
        assertNotNull(groups);
        assertEquals(2, groups.length);

        // Initialize dialog implementations for command execution
        TraceControlDialogFactory.getInstance().setCreateSessionDialog(new CreateSessionDialogStub());
        TraceControlDialogFactory.getInstance().setGetEventInfoDialog(new GetEventInfoDialogStub());
        TraceControlDialogFactory.getInstance().setConfirmDialog(new DestroyConfirmDialogStub());

        // Initialize session handling scenario
        fProxy.setScenario(TraceControlTestFacility.SCEN_SCENARIO_SESSION_HANDLING);

        // ------------------------------------------------------------------------
        // Create session
        // ------------------------------------------------------------------------
        TraceSessionComponent session = fFacility.createSession(groups[1]);

        // Verify that session was created
        assertNotNull(session);
        assertEquals("mysession", session.getName());
        assertEquals("/home/user/lttng-traces/mysession-20120314-132824", session.getSessionPath());
        assertEquals(TraceSessionState.INACTIVE, session.getSessionState());

        // Initialize scenario
        fProxy.setScenario(SCEN_SCENARIO3_TEST);

        // ------------------------------------------------------------------------
        // Enable channel on session
        // ------------------------------------------------------------------------
        EnableChannelDialogStub channelStub = new EnableChannelDialogStub();
        channelStub.setIsKernel(true);
        TraceControlDialogFactory.getInstance().setEnableChannelDialog(channelStub);

        fFacility.executeCommand(session, "enableChannelOnSession");

        // Verify that Kernel domain was created
        ITraceControlComponent[] domains = session.getChildren();
        assertNotNull(domains);
        assertEquals(1, domains.length);

        assertEquals("Kernel", domains[0].getName());

        // Verify that channel was created with correct data
        ITraceControlComponent[] channels =  domains[0].getChildren();
        assertNotNull(channels);
        assertEquals(1, channels.length);

        assertTrue(channels[0] instanceof TraceChannelComponent);
        TraceChannelComponent channel = (TraceChannelComponent) channels[0];
        assertEquals("mychannel", channel.getName());
        assertEquals(4, channel.getNumberOfSubBuffers());
        assertEquals("splice()", channel.getOutputType());
        assertEquals(true, channel.isOverwriteMode());
        assertEquals(200, channel.getReadTimer());
        assertEquals(TraceEnablement.ENABLED, channel.getState());
        assertEquals(16384, channel.getSubBufferSize());
        assertEquals(100, channel.getSwitchTimer());

        // ------------------------------------------------------------------------
        // Create channel on domain
        // ------------------------------------------------------------------------
        ChannelInfo info = (ChannelInfo)channelStub.getChannelInfo();
        info.setName("mychannel2");
        info.setOverwriteMode(false);
        info.setSubBufferSize(32768);
        info.setNumberOfSubBuffers(2);
        info.setSwitchTimer(100);
        info.setReadTimer(200);
        channelStub.setChannelInfo(info);

        fFacility.executeCommand(domains[0], "enableChannelOnDomain");

        // Get Kernel domain component instance
        domains = session.getChildren();
        assertNotNull(domains);
        assertEquals(1, domains.length);

        // Verify that channel was created with correct data
        channels =  domains[0].getChildren();
        assertNotNull(channels);
        assertEquals(2, channels.length);

        assertTrue(channels[1] instanceof TraceChannelComponent);
        channel = (TraceChannelComponent) channels[1];
        assertEquals("mychannel2", channel.getName());
        assertEquals(2, channel.getNumberOfSubBuffers());
        assertEquals("splice()", channel.getOutputType());
        assertEquals(false, channel.isOverwriteMode());
        assertEquals(200, channel.getReadTimer());
        assertEquals(TraceEnablement.ENABLED, channel.getState());
        assertEquals(32768, channel.getSubBufferSize());
        assertEquals(100, channel.getSwitchTimer());

        EnableEventsDialogStub eventsDialogStub = new EnableEventsDialogStub();
        eventsDialogStub.setIsTracePoints(true);
        List<String> events = new ArrayList<String>();
        events.add("sched_kthread_stop");
        events.add("sched_kthread_stop_ret");
        eventsDialogStub.setNames(events);
        eventsDialogStub.setIsKernel(true);
        TraceControlDialogFactory.getInstance().setEnableEventsDialog(eventsDialogStub);

        // ------------------------------------------------------------------------
        // disable channels
        // ------------------------------------------------------------------------
        // Get Kernel domain component instance
        domains = session.getChildren();
        assertNotNull(domains);
        assertEquals(1, domains.length);

        // Verify that channel was created with correct data
        channels =  domains[0].getChildren();
        assertNotNull(channels);
        assertEquals(2, channels.length);

        fFacility.executeCommand(channels, "disableChannel");

        assertEquals(TraceEnablement.DISABLED, ((TraceChannelComponent)channels[0]).getState());
        assertEquals(TraceEnablement.DISABLED, ((TraceChannelComponent)channels[1]).getState());

        // ------------------------------------------------------------------------
        // enable channels
        // ------------------------------------------------------------------------
        // Get Kernel domain component instance
        domains = session.getChildren();
        assertNotNull(domains);
        assertEquals(1, domains.length);

        // Verify that channel was created with correct data
        channels =  domains[0].getChildren();
        assertNotNull(channels);
        assertEquals(2, channels.length);

        fFacility.executeCommand(channels, "enableChannel");

        assertEquals(TraceEnablement.ENABLED, ((TraceChannelComponent)channels[0]).getState());
        assertEquals(TraceEnablement.ENABLED, ((TraceChannelComponent)channels[1]).getState());

        // ------------------------------------------------------------------------
        // enable event (tracepoints) on session
        // ------------------------------------------------------------------------
        fFacility.executeCommand(session, "enableEventOnSession");

        // Get Kernel domain component instance
        domains = session.getChildren();
        assertNotNull(domains);
        assertEquals(1, domains.length);

        // Verify that channel was created with correct data
        channels =  domains[0].getChildren();
        assertNotNull(channels);
        assertEquals(3, channels.length);

        assertTrue(channels[2] instanceof TraceChannelComponent);
        channel = (TraceChannelComponent) channels[2];
        assertEquals("channel0", channel.getName());
        // No need to check parameters of default channel because that has been done in other tests

        ITraceControlComponent[] channel0Events = channel.getChildren();
        assertEquals(2, channel0Events.length);

        assertTrue(channel0Events[0] instanceof TraceEventComponent);
        assertTrue(channel0Events[1] instanceof TraceEventComponent);

        TraceEventComponent event = (TraceEventComponent) channel0Events[0];
        assertEquals("sched_kthread_stop_ret", event.getName());
        assertEquals(TraceLogLevel.TRACE_EMERG, event.getLogLevel());
        assertEquals(TraceEventType.TRACEPOINT, event.getEventType());
        assertEquals(TraceEnablement.ENABLED, event.getState());

        event = (TraceEventComponent) channel0Events[1];
        assertEquals("sched_kthread_stop", event.getName());
        assertEquals(TraceLogLevel.TRACE_EMERG, event.getLogLevel());
        assertEquals(TraceEventType.TRACEPOINT, event.getEventType());
        assertEquals(TraceEnablement.ENABLED, event.getState());

        // ------------------------------------------------------------------------
        // enable event (tracepoints) on domain
        // ------------------------------------------------------------------------
        events.clear();
        events.add("sched_wakeup_new");
        eventsDialogStub.setNames(events);

        fFacility.executeCommand(domains[0], "enableEventOnDomain");

        // Get Kernel domain component instance
        domains = session.getChildren();
        assertNotNull(domains);
        assertEquals(1, domains.length);

        // Verify that channel was created with correct data
        channels =  domains[0].getChildren();
        channel = (TraceChannelComponent) channels[2];
        // No need to check parameters of default channel because that has been done in other tests

        channel = (TraceChannelComponent) channels[2];

        channel0Events = channel.getChildren();
        assertEquals(3, channel0Events.length);

        assertTrue(channel0Events[2] instanceof TraceEventComponent);

        event = (TraceEventComponent) channel0Events[2];
        assertEquals("sched_wakeup_new", event.getName());
        assertEquals(TraceLogLevel.TRACE_EMERG, event.getLogLevel());
        assertEquals(TraceEventType.TRACEPOINT, event.getEventType());
        assertEquals(TraceEnablement.ENABLED, event.getState());

        // ------------------------------------------------------------------------
        // enable event (tracepoints) on channel
        // ------------------------------------------------------------------------
        events.clear();
        eventsDialogStub.setNames(events);
        eventsDialogStub.setIsAllTracePoints(true);

        fFacility.executeCommand(channels[1], "enableEventOnChannel");

        // Get Kernel domain component instance
        domains = session.getChildren();
        assertNotNull(domains);
        assertEquals(1, domains.length);

        // Verify that channel was created with correct data
        channels =  domains[0].getChildren();
        // No need to check parameters of default channel because that has been done in other tests
        channel = (TraceChannelComponent) channels[1];

        channel0Events = channel.getChildren();
        assertEquals(3, channel0Events.length);

        assertTrue(channel0Events[0] instanceof TraceEventComponent);
        assertTrue(channel0Events[1] instanceof TraceEventComponent);
        assertTrue(channel0Events[2] instanceof TraceEventComponent);

        event = (TraceEventComponent) channel0Events[0];
        assertEquals("sched_kthread_stop_ret", event.getName());
        assertEquals(TraceLogLevel.TRACE_EMERG, event.getLogLevel());
        assertEquals(TraceEventType.TRACEPOINT, event.getEventType());
        assertEquals(TraceEnablement.ENABLED, event.getState());

        event = (TraceEventComponent) channel0Events[1];
        assertEquals("sched_kthread_stop", event.getName());
        assertEquals(TraceLogLevel.TRACE_EMERG, event.getLogLevel());
        assertEquals(TraceEventType.TRACEPOINT, event.getEventType());
        assertEquals(TraceEnablement.ENABLED, event.getState());

        event = (TraceEventComponent) channel0Events[2];
        assertEquals("sched_wakeup_new", event.getName());
        assertEquals(TraceLogLevel.TRACE_EMERG, event.getLogLevel());
        assertEquals(TraceEventType.TRACEPOINT, event.getEventType());
        assertEquals(TraceEnablement.ENABLED, event.getState());

        // ------------------------------------------------------------------------
        // enable event (syscall) on channel
        // ------------------------------------------------------------------------
        events.clear();
        eventsDialogStub.setIsTracePoints(false);
        eventsDialogStub.setIsAllTracePoints(false);
        eventsDialogStub.setIsSysCalls(true);

        fFacility.executeCommand(channels[0], "enableEventOnChannel");

        // Get Kernel domain component instance
        domains = session.getChildren();
        assertNotNull(domains);
        assertEquals(1, domains.length);

        // Verify that channel was created with correct data
        channels =  domains[0].getChildren();
        channel = (TraceChannelComponent) channels[0];
        // No need to check parameters of default channel because that has been done in other tests

        channel = (TraceChannelComponent) channels[0];

        channel0Events = channel.getChildren();
        assertEquals(1, channel0Events.length);

        assertTrue(channel0Events[0] instanceof TraceEventComponent);

        event = (TraceEventComponent) channel0Events[0];
        assertEquals("syscalls", event.getName());
        assertEquals(TraceLogLevel.LEVEL_UNKNOWN, event.getLogLevel());
        assertEquals(TraceEventType.SYSCALL, event.getEventType());
        assertEquals(TraceEnablement.ENABLED, event.getState());

        // ------------------------------------------------------------------------
        // enable event (syscall) on domain
        // ------------------------------------------------------------------------
        fFacility.executeCommand(domains[0], "enableEventOnDomain");

        // Get Kernel domain component instance
        domains = session.getChildren();
        assertNotNull(domains);
        assertEquals(1, domains.length);

        // Verify that channel was created with correct data
        channels =  domains[0].getChildren();
        channel = (TraceChannelComponent) channels[0];
        // No need to check parameters of default channel because that has been done in other tests

        channel = (TraceChannelComponent) channels[2];

        channel0Events = channel.getChildren();
        assertEquals(4, channel0Events.length);

        assertTrue(channel0Events[0] instanceof TraceEventComponent);

        event = (TraceEventComponent) channel0Events[0];
        assertEquals("syscalls", event.getName());
        assertEquals(TraceLogLevel.LEVEL_UNKNOWN, event.getLogLevel());
        assertEquals(TraceEventType.SYSCALL, event.getEventType());
        assertEquals(TraceEnablement.ENABLED, event.getState());

        // ------------------------------------------------------------------------
        // enable event (syscall) on session
        // ------------------------------------------------------------------------
        fFacility.executeCommand(session, "enableEventOnSession");

        // Get Kernel domain component instance
        domains = session.getChildren();
        assertNotNull(domains);
        assertEquals(1, domains.length);

        // Verify that channel was created with correct data
        channels =  domains[0].getChildren();
        channel = (TraceChannelComponent) channels[0];
        // No need to check parameters of default channel because that has been done in other tests

        channel = (TraceChannelComponent) channels[2];

        channel0Events = channel.getChildren();
        assertEquals(4, channel0Events.length);

        assertTrue(channel0Events[0] instanceof TraceEventComponent);

        event = (TraceEventComponent) channel0Events[0];
        assertEquals("syscalls", event.getName());
        assertEquals(TraceLogLevel.LEVEL_UNKNOWN, event.getLogLevel());
        assertEquals(TraceEventType.SYSCALL, event.getEventType());
        assertEquals(TraceEnablement.ENABLED, event.getState());


        // ------------------------------------------------------------------------
        // enable event (dynamic probe) on domain
        // ------------------------------------------------------------------------
        events.clear();
        eventsDialogStub.setIsSysCalls(false);
        eventsDialogStub.setIsDynamicProbe(true);
        eventsDialogStub.setDynamicProbe("0xc0101280");
        eventsDialogStub.setProbeEventName("myevent1");

        fFacility.executeCommand(domains[0], "enableEventOnDomain");

        // Get Kernel domain component instance
        domains = session.getChildren();
        assertNotNull(domains);
        assertEquals(1, domains.length);

        // Verify that channel was created with correct data
        channels =  domains[0].getChildren();
        channel = (TraceChannelComponent) channels[2];
        // No need to check parameters of default channel because that has been done in other tests

        channel0Events = channel.getChildren();
        assertEquals(5, channel0Events.length);

        assertTrue(channel0Events[0] instanceof TraceProbeEventComponent);

        TraceProbeEventComponent probeEvent = (TraceProbeEventComponent) channel0Events[0];
        assertEquals("myevent1", probeEvent.getName());
        assertEquals(TraceLogLevel.LEVEL_UNKNOWN, probeEvent.getLogLevel());
        assertEquals(TraceEventType.PROBE, probeEvent.getEventType());
        assertEquals(TraceEnablement.ENABLED, probeEvent.getState());
        assertNull(probeEvent.getOffset());
        assertEquals("0xc0101280", probeEvent.getAddress());
        assertNull(probeEvent.getSymbol());

        // ------------------------------------------------------------------------
        // enable event (dynamic probe) on channel
        // ------------------------------------------------------------------------
        eventsDialogStub.setIsDynamicProbe(true);
        eventsDialogStub.setDynamicProbe("init_post");
        eventsDialogStub.setProbeEventName("myevent2");

        fFacility.executeCommand(channels[2], "enableEventOnChannel");

        // Get Kernel domain component instance
        domains = session.getChildren();
        assertNotNull(domains);
        assertEquals(1, domains.length);

        // Verify that channel was created with correct data
        channels =  domains[0].getChildren();
        channel = (TraceChannelComponent) channels[2];
        // No need to check parameters of default channel because that has been done in other tests

        channel0Events = channel.getChildren();
        assertEquals(6, channel0Events.length);

        assertTrue(channel0Events[0] instanceof TraceProbeEventComponent);

        probeEvent = (TraceProbeEventComponent) channel0Events[0];
        assertEquals("myevent2", probeEvent.getName());
        assertEquals(TraceLogLevel.LEVEL_UNKNOWN, probeEvent.getLogLevel());
        assertEquals(TraceEventType.PROBE, probeEvent.getEventType());
        assertEquals(TraceEnablement.ENABLED, probeEvent.getState());
        assertEquals("0x0", probeEvent.getOffset());
        assertNull(null, probeEvent.getAddress());
        assertEquals("init_post", probeEvent.getSymbol());

        // ------------------------------------------------------------------------
        // enable event (dynamic probe) on session
        // ------------------------------------------------------------------------
        eventsDialogStub.setIsDynamicProbe(true);
        eventsDialogStub.setDynamicProbe("init_post:0x1000");
        eventsDialogStub.setProbeEventName("myevent3");

        fFacility.executeCommand(session, "enableEventOnSession");

        // Get Kernel domain component instance
        domains = session.getChildren();
        assertNotNull(domains);
        assertEquals(1, domains.length);

        // Verify that channel was created with correct data
        channels =  domains[0].getChildren();
        channel = (TraceChannelComponent) channels[2];
        // No need to check parameters of default channel because that has been done in other tests

        channel0Events = channel.getChildren();
        assertEquals(7, channel0Events.length);

        assertTrue(channel0Events[0] instanceof TraceProbeEventComponent);

        probeEvent = (TraceProbeEventComponent) channel0Events[0];
        assertEquals("myevent3", probeEvent.getName());
        assertEquals(TraceLogLevel.LEVEL_UNKNOWN, probeEvent.getLogLevel());
        assertEquals(TraceEventType.PROBE, probeEvent.getEventType());
        assertEquals(TraceEnablement.ENABLED, probeEvent.getState());
        assertEquals("0x1000", probeEvent.getOffset());
        assertNull(null, probeEvent.getAddress());
        assertEquals("init_post", probeEvent.getSymbol());

        // ------------------------------------------------------------------------
        // enable event (dynamic function probe) on session
        // ------------------------------------------------------------------------
        eventsDialogStub.setIsDynamicProbe(false);
        eventsDialogStub.setDynamicProbe(null);
        eventsDialogStub.setProbeEventName(null);
        eventsDialogStub.setIsFunctionProbe(true);
        eventsDialogStub.setFunctionEventName("myevent4");
        eventsDialogStub.setFunctionProbe("create_dev");

        fFacility.executeCommand(session, "enableEventOnSession");

        // Get Kernel domain component instance
        domains = session.getChildren();
        assertNotNull(domains);
        assertEquals(1, domains.length);

        // Verify that channel was created with correct data
        channels =  domains[0].getChildren();
        channel = (TraceChannelComponent) channels[2];
        // No need to check parameters of default channel because that has been done in other tests

        channel0Events = channel.getChildren();
        assertEquals(8, channel0Events.length);

        assertTrue(channel0Events[0] instanceof TraceProbeEventComponent);

        probeEvent = (TraceProbeEventComponent) channel0Events[0];
        assertEquals("myevent4", probeEvent.getName());
        assertEquals(TraceLogLevel.LEVEL_UNKNOWN, probeEvent.getLogLevel());
        // Changed for Bug fix 419454 to function event which was introduced by LTTng 2.2
        assertEquals(TraceEventType.FUNCTION, probeEvent.getEventType());
        assertEquals(TraceEnablement.ENABLED, probeEvent.getState());
        assertEquals("0x0", probeEvent.getOffset());
        assertNull(null, probeEvent.getAddress());
        assertEquals("create_dev", probeEvent.getSymbol());

        // ------------------------------------------------------------------------
        // enable event (dynamic function probe) on domain
        // ------------------------------------------------------------------------
        eventsDialogStub.setIsFunctionProbe(true);
        eventsDialogStub.setFunctionEventName("myevent5");
        eventsDialogStub.setFunctionProbe("create_dev:0x2000");

        fFacility.executeCommand(domains[0], "enableEventOnDomain");

        // Get Kernel domain component instance
        domains = session.getChildren();
        assertNotNull(domains);
        assertEquals(1, domains.length);

        // Verify that channel was created with correct data
        channels =  domains[0].getChildren();
        channel = (TraceChannelComponent) channels[2];
        // No need to check parameters of default channel because that has been done in other tests

        channel0Events = channel.getChildren();
        assertEquals(9, channel0Events.length);

        assertTrue(channel0Events[0] instanceof TraceProbeEventComponent);

        probeEvent = (TraceProbeEventComponent) channel0Events[0];
        assertEquals("myevent5", probeEvent.getName());
        assertEquals(TraceLogLevel.LEVEL_UNKNOWN, probeEvent.getLogLevel());
        assertEquals(TraceEventType.PROBE, probeEvent.getEventType());
        assertEquals(TraceEnablement.ENABLED, probeEvent.getState());
        assertEquals("0x2000", probeEvent.getOffset());
        assertNull(null, probeEvent.getAddress());
        assertEquals("create_dev", probeEvent.getSymbol());

        // ------------------------------------------------------------------------
        // enable event (dynamic function probe) on channel
        // ------------------------------------------------------------------------
        eventsDialogStub.setIsFunctionProbe(true);
        eventsDialogStub.setFunctionEventName("myevent");
        eventsDialogStub.setFunctionProbe("create_dev:0x2000");

        fFacility.executeCommand(channels[0], "enableEventOnChannel");

        // Get Kernel domain component instance
        domains = session.getChildren();
        assertNotNull(domains);
        assertEquals(1, domains.length);

        // Verify that channel was created with correct data
        channels =  domains[0].getChildren();
        channel = (TraceChannelComponent) channels[0];
        // No need to check parameters of default channel because that has been done in other tests

        channel0Events = channel.getChildren();
        assertEquals(2, channel0Events.length);

        assertTrue(channel0Events[0] instanceof TraceProbeEventComponent);

        probeEvent = (TraceProbeEventComponent) channel0Events[0];
        assertEquals("myevent", probeEvent.getName());
        assertEquals(TraceLogLevel.LEVEL_UNKNOWN, probeEvent.getLogLevel());
        assertEquals(TraceEventType.PROBE, probeEvent.getEventType());
        assertEquals(TraceEnablement.ENABLED, probeEvent.getState());
        assertEquals("0x2000", probeEvent.getOffset());
        assertNull(null, probeEvent.getAddress());
        assertEquals("create_dev", probeEvent.getSymbol());

        // ------------------------------------------------------------------------
        // Add Context on domain
        // ------------------------------------------------------------------------
        // Get Kernel domain component instance
        domains = session.getChildren();
        assertNotNull(domains);
        assertEquals(1, domains.length);

        AddContextDialogStub addContextStub = new AddContextDialogStub();
        List<String> contexts = new ArrayList<String>();
        contexts.add("prio");
        contexts.add("perf:branch-misses");
        contexts.add("perf:cache-misses");
        addContextStub.setContexts(contexts);
        TraceControlDialogFactory.getInstance().setAddContextDialog(addContextStub);

        fFacility.executeCommand(domains[0], "addContextOnDomain");
        // Currently there is nothing to verify because the list commands don't show any context information
        // However, the execution of the command make sure that the correct service command line is build and executed.

        // ------------------------------------------------------------------------
        // Add Context on channel
        // ------------------------------------------------------------------------

        // Get Kernel domain component instance
        domains = session.getChildren();
        assertNotNull(domains);
        assertEquals(1, domains.length);

        //Verify that channel was created with correct data
        channels =  domains[0].getChildren();
        channel = (TraceChannelComponent) channels[0];

        try {
            // The setContext() verifies that the contexts set are part of the available contexts
            // The available contexts are set by the command handler addContextOnDomain above.
            // So we indirectly test here that the parsing and setting of available contexts were
            // done correctly above.
            addContextStub.setContexts(contexts);
        } catch (IllegalArgumentException e) {
            fail("Exception caught - unknown context");
        }

        fFacility.executeCommand(channel, "addContextOnChannel");
        // Currently there is nothing to verify because the list commands don't show any context information
        // However, the execution of the command make sure that the correct service command line is build and executed.

        // ------------------------------------------------------------------------
        // Add Context on event
        // ------------------------------------------------------------------------
        // Get Kernel domain component instance
        domains = session.getChildren();
        assertNotNull(domains);
        assertEquals(1, domains.length);

        //Verify that channel was created with correct data
        channels =  domains[0].getChildren();
        channel = (TraceChannelComponent) channels[2];

        channel0Events = channel.getChildren();

        event = (TraceEventComponent) channel0Events[6];

        fFacility.executeCommand(event, "addContextOnEvent");
        // Currently there is nothing to verify because the list commands don't show any context information
        // However, the execution of the command make sure that the correct service command line is build and executed.

        // ------------------------------------------------------------------------
        // Calibrate
        // ------------------------------------------------------------------------
        // Get Kernel domain component instance
        domains = session.getChildren();
        assertNotNull(domains);
        assertEquals(1, domains.length);

        fFacility.executeCommand(domains[0], "calibrate");
        // There is nothing to verify here.
        // However, the execution of the command make sure that the correct service command line is build and executed.

        // ------------------------------------------------------------------------
        // refresh
        // ------------------------------------------------------------------------
        fFacility.executeCommand(node, "refresh");
        groups = node.getChildren();
        assertNotNull(groups);
        assertEquals(2, groups.length);
        assertEquals(3, groups[0].getChildren().length); // provider
        assertEquals(1, groups[1].getChildren().length); // sessions
        assertEquals(1, groups[1].getChildren()[0].getChildren().length); // domains
        assertEquals(3, groups[1].getChildren()[0].getChildren()[0].getChildren().length); // channels
        assertEquals(2, groups[1].getChildren()[0].getChildren()[0].getChildren()[0].getChildren().length); // events (of channel[0])

        // Initialize session handling scenario
        fProxy.setScenario(TraceControlTestFacility.SCEN_SCENARIO_SESSION_HANDLING);

        session = (TraceSessionComponent)groups[1].getChildren()[0];

        // ------------------------------------------------------------------------
        // start session
        // ------------------------------------------------------------------------
        fFacility.startSession(session);
        assertEquals(TraceSessionState.ACTIVE, session.getSessionState());

        // ------------------------------------------------------------------------
        // stop session
        // ------------------------------------------------------------------------
        fFacility.stopSession(session);
        assertEquals(TraceSessionState.INACTIVE, session.getSessionState());

        // ------------------------------------------------------------------------
        // Destroy session
        // ------------------------------------------------------------------------

        fFacility.destroySession(session);

        // Verify that no more session components exist
        assertEquals(0, groups[1].getChildren().length);

        //-------------------------------------------------------------------------
        // Disconnect node
        //-------------------------------------------------------------------------
        fFacility.executeCommand(node, "disconnect");
        assertEquals(TargetNodeState.DISCONNECTED, node.getTargetNodeState());

        //-------------------------------------------------------------------------
        // Delete node
        //-------------------------------------------------------------------------

        fFacility.executeCommand(node, "delete");
        assertEquals(0,fFacility.getControlView().getTraceControlRoot().getChildren().length);
    }

}