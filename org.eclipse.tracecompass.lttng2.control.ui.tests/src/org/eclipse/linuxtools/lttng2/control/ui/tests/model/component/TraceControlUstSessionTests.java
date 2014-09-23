/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
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

package org.eclipse.linuxtools.lttng2.control.ui.tests.model.component;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.lttng2.control.stubs.dialogs.CreateSessionDialogStub;
import org.eclipse.linuxtools.internal.lttng2.control.stubs.dialogs.DestroyConfirmDialogStub;
import org.eclipse.linuxtools.internal.lttng2.control.stubs.dialogs.EnableChannelDialogStub;
import org.eclipse.linuxtools.internal.lttng2.control.stubs.dialogs.EnableEventsDialogStub;
import org.eclipse.linuxtools.internal.lttng2.control.stubs.dialogs.GetEventInfoDialogStub;
import org.eclipse.linuxtools.internal.lttng2.control.stubs.service.TestRemoteSystemProxy;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.LogLevelType;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.TargetNodeState;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.TraceChannelOutputType;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.TraceEnablement;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.TraceEventType;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.TraceLogLevel;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.TraceSessionState;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.impl.BufferType;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.impl.ChannelInfo;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.dialogs.TraceControlDialogFactory;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.ITraceControlComponent;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl.TargetNodeComponent;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl.TraceChannelComponent;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl.TraceDomainComponent;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl.TraceEventComponent;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl.TraceSessionComponent;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.FrameworkUtil;

/**
 * The class <code>TraceControlUstSessionTests</code> contains UST
 * session/channel/event handling test cases.
 */
public class TraceControlUstSessionTests {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final String TEST_STREAM = "CreateTreeTest.cfg";
    private static final String SCEN_SCENARIO4_TEST = "Scenario4";

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
     *             This will fail the test
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
        fProxy.setScenario(SCEN_SCENARIO4_TEST);

        // ------------------------------------------------------------------------
        // Enable default channel on created session above
        // ------------------------------------------------------------------------
        EnableChannelDialogStub channelStub = new EnableChannelDialogStub();
        channelStub.setIsKernel(false);
        TraceControlDialogFactory.getInstance().setEnableChannelDialog(channelStub);

        fFacility.executeCommand(session, "enableChannelOnSession");

        // Verify that Kernel domain was created
        ITraceControlComponent[] domains = session.getChildren();
        assertNotNull(domains);
        assertEquals(1, domains.length);

        assertEquals("UST global", domains[0].getName());
        assertEquals("Domain buffer Type", BufferType.BUFFER_TYPE_UNKNOWN, ((TraceDomainComponent)domains[0]).getBufferType());

        // Verify that channel was created with correct data
        ITraceControlComponent[] channels =  domains[0].getChildren();
        assertNotNull(channels);
        assertEquals(1, channels.length);

        assertTrue(channels[0] instanceof TraceChannelComponent);
        TraceChannelComponent channel = (TraceChannelComponent) channels[0];
        assertEquals("mychannel", channel.getName());
        assertEquals(4, channel.getNumberOfSubBuffers());
        assertEquals("mmap()", channel.getOutputType().getInName());
        assertEquals(TraceChannelOutputType.MMAP, channel.getOutputType());
        assertEquals(true, channel.isOverwriteMode());
        assertEquals(200, channel.getReadTimer());
        assertEquals(TraceEnablement.ENABLED, channel.getState());
        assertEquals(16384, channel.getSubBufferSize());
        assertEquals(100, channel.getSwitchTimer());

        // ------------------------------------------------------------------------
        // Enable channel on domain
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
        assertEquals("mmap()", channel.getOutputType().getInName());
        assertEquals(TraceChannelOutputType.MMAP, channel.getOutputType());
        assertEquals(false, channel.isOverwriteMode());
        assertEquals(200, channel.getReadTimer());
        assertEquals(TraceEnablement.ENABLED, channel.getState());
        assertEquals(32768, channel.getSubBufferSize());
        assertEquals(100, channel.getSwitchTimer());

        // ------------------------------------------------------------------------
        // Enable event (tracepoint) on session and default channel
        // ------------------------------------------------------------------------
        EnableEventsDialogStub eventsDialogStub = new EnableEventsDialogStub();
        eventsDialogStub.setIsTracePoints(true);
        List<String> events = new ArrayList<>();
        events.add("ust_tests_hello:tptest_sighandler");
        eventsDialogStub.setNames(events);
        eventsDialogStub.setIsKernel(false);
        TraceControlDialogFactory.getInstance().setEnableEventsDialog(eventsDialogStub);

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
        assertEquals(1, channel0Events.length);

        assertTrue(channel0Events[0] instanceof TraceEventComponent);

        TraceEventComponent event = (TraceEventComponent) channel0Events[0];
        assertEquals("ust_tests_hello:tptest_sighandler", event.getName());
        assertEquals(TraceLogLevel.LEVEL_UNKNOWN, event.getLogLevel()); // TODO
        assertEquals(TraceEventType.TRACEPOINT, event.getEventType());
        assertEquals(TraceEnablement.ENABLED, event.getState());

        // ------------------------------------------------------------------------
        // Enable event (tracepoint) on domain and default channel
        // ------------------------------------------------------------------------
        events.clear();
        events.add("ust_tests_hello:tptest");
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

        channel0Events = channel.getChildren();
        assertEquals(2, channel0Events.length);

        assertTrue(channel0Events[1] instanceof TraceEventComponent);

        event = (TraceEventComponent) channel0Events[1];
        assertEquals("ust_tests_hello:tptest", event.getName());
        assertEquals(TraceLogLevel.LEVEL_UNKNOWN, event.getLogLevel()); // TODO
        assertEquals(TraceEventType.TRACEPOINT, event.getEventType());
        assertEquals(TraceEnablement.ENABLED, event.getState());

        // ------------------------------------------------------------------------
        // Enable event (all tracepoints) on specific channel
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
        channel = (TraceChannelComponent) channels[1];
        // No need to check parameters of default channel because that has been done in other tests

        channel = (TraceChannelComponent) channels[1];

        channel0Events = channel.getChildren();
        assertEquals(1, channel0Events.length);

        assertTrue(channel0Events[0] instanceof TraceEventComponent);

        event = (TraceEventComponent) channel0Events[0];
        assertEquals("*", event.getName());
        assertEquals(TraceLogLevel.LEVEL_UNKNOWN, event.getLogLevel());
        assertEquals(TraceEventType.TRACEPOINT, event.getEventType());
        assertEquals(TraceEnablement.ENABLED, event.getState());

        // ------------------------------------------------------------------------
        // Enable event (wildcard) on specific channel
        // ------------------------------------------------------------------------
        events.clear();
        eventsDialogStub.setIsTracePoints(false);
        eventsDialogStub.setIsAllTracePoints(false);
        eventsDialogStub.setIsWildcard(true);
        eventsDialogStub.setWildcard("ust*");

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
        assertEquals(1, channel0Events.length);

        assertTrue(channel0Events[0] instanceof TraceEventComponent);

        event = (TraceEventComponent) channel0Events[0];
        assertEquals("ust*", event.getName());
        assertEquals(TraceLogLevel.LEVEL_UNKNOWN, event.getLogLevel());
        assertEquals(TraceEventType.TRACEPOINT, event.getEventType());
        assertEquals(TraceEnablement.ENABLED, event.getState());

        // ------------------------------------------------------------------------
        // Enable event (wildcard) on domain
        // ------------------------------------------------------------------------
        events.clear();
        eventsDialogStub.setIsTracePoints(false);
        eventsDialogStub.setIsAllTracePoints(false);
        eventsDialogStub.setIsWildcard(true);
        eventsDialogStub.setWildcard("ust*");

        fFacility.executeCommand(domains[0], "enableEventOnDomain");

        // Get Kernel domain component instance
        domains = session.getChildren();
        assertNotNull(domains);
        assertEquals(1, domains.length);

        // Verify that channel was created with correct data
        channels =  domains[0].getChildren();
        channel = (TraceChannelComponent) channels[0];
        // No need to check parameters of default channel because that has been done in other tests

        channel0Events = channel.getChildren();
        assertEquals(1, channel0Events.length);

        assertTrue(channel0Events[0] instanceof TraceEventComponent);

        event = (TraceEventComponent) channel0Events[0];
        assertEquals("ust*", event.getName());
        assertEquals(TraceLogLevel.LEVEL_UNKNOWN, event.getLogLevel());
        assertEquals(TraceEventType.TRACEPOINT, event.getEventType());
        assertEquals(TraceEnablement.ENABLED, event.getState());

        // ------------------------------------------------------------------------
        // Enable event (wildcard) on session
        // ------------------------------------------------------------------------
        events.clear();
        eventsDialogStub.setIsTracePoints(false);
        eventsDialogStub.setIsAllTracePoints(false);
        eventsDialogStub.setIsWildcard(true);
        eventsDialogStub.setWildcard("ust*");

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
        assertEquals(4, channel0Events.length);

        assertTrue(channel0Events[0] instanceof TraceEventComponent);

        event = (TraceEventComponent) channel0Events[0];
        assertEquals("u*", event.getName());
        assertEquals(TraceLogLevel.LEVEL_UNKNOWN, event.getLogLevel());
        assertEquals(TraceEventType.TRACEPOINT, event.getEventType());
        assertEquals(TraceEnablement.ENABLED, event.getState());

        // ------------------------------------------------------------------------
        // Enable event (loglevel) on domain
        // ------------------------------------------------------------------------
        events.clear();
        eventsDialogStub.setIsWildcard(false);
        eventsDialogStub.setIsLogLevel(true);
        eventsDialogStub.setLogLevelEventName("myevent1");
        eventsDialogStub.setLogLevelType(LogLevelType.LOGLEVEL);
        eventsDialogStub.setLogLevel(TraceLogLevel.TRACE_WARNING);

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

        assertTrue(channel0Events[0] instanceof TraceEventComponent);

        event = (TraceEventComponent) channel0Events[0];
        assertEquals("myevent1", event.getName());
        assertEquals(TraceLogLevel.TRACE_WARNING, event.getLogLevel());
        assertEquals(TraceEventType.TRACEPOINT, event.getEventType());
        assertEquals(TraceEnablement.ENABLED, event.getState());

        // ------------------------------------------------------------------------
        // Enable event (loglevel) on session
        // ------------------------------------------------------------------------
        eventsDialogStub.setLogLevelEventName("myevent2");
        eventsDialogStub.setLogLevelType(LogLevelType.LOGLEVEL_ONLY);
        eventsDialogStub.setLogLevel(TraceLogLevel.TRACE_DEBUG_FUNCTION);

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
        assertEquals(6, channel0Events.length);

        assertTrue(channel0Events[0] instanceof TraceEventComponent);

        event = (TraceEventComponent) channel0Events[0];
        assertEquals("myevent2", event.getName());
        assertEquals(TraceLogLevel.TRACE_DEBUG_FUNCTION, event.getLogLevel());
        assertEquals(TraceEventType.TRACEPOINT, event.getEventType());
        assertEquals(TraceEnablement.ENABLED, event.getState());

        // ------------------------------------------------------------------------
        // Enable event (loglevel) on channel
        // ------------------------------------------------------------------------
        eventsDialogStub.setLogLevelEventName("myevent0");
        eventsDialogStub.setLogLevelType(LogLevelType.LOGLEVEL_ONLY);
        eventsDialogStub.setLogLevel(TraceLogLevel.TRACE_DEBUG_FUNCTION);

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

        assertTrue(channel0Events[0] instanceof TraceEventComponent);

        event = (TraceEventComponent) channel0Events[0];
        assertEquals("myevent0", event.getName());
        assertEquals(TraceLogLevel.TRACE_DEBUG_FUNCTION, event.getLogLevel());
        assertEquals(TraceEventType.TRACEPOINT, event.getEventType());
        assertEquals(TraceEnablement.ENABLED, event.getState());

        // ------------------------------------------------------------------------
        // Destroy session
        // ------------------------------------------------------------------------
        // Initialize session handling scenario
        fProxy.setScenario(TraceControlTestFacility.SCEN_SCENARIO_SESSION_HANDLING);

        fFacility.destroySession(session);

        // Verify that no more session components exist
        assertEquals("Session components still exist.", 0, groups[1].getChildren().length);

        //-------------------------------------------------------------------------
        // Disconnect node
        //-------------------------------------------------------------------------
        fFacility.executeCommand(node, "disconnect");
        assertEquals(TargetNodeState.DISCONNECTED, node.getTargetNodeState());

        //-------------------------------------------------------------------------
        // Delete node
        //-------------------------------------------------------------------------

        fFacility.executeCommand(node, "delete");

        assertEquals("Node not deleted.", 0, fFacility.getControlView().getTraceControlRoot().getChildren().length);
    }
}