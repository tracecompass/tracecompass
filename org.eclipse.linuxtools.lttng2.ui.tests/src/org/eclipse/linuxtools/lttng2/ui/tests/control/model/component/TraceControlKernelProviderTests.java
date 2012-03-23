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

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.lttng2.stubs.dialogs.CreateSessionDialogStub;
import org.eclipse.linuxtools.internal.lttng2.stubs.dialogs.DestroyConfirmDialogStub;
import org.eclipse.linuxtools.internal.lttng2.stubs.dialogs.GetEventInfoDialogStub;
import org.eclipse.linuxtools.internal.lttng2.stubs.service.TestRemoteSystemProxy;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.TraceControlDialogFactory;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.TargetNodeState;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.TraceEnablement;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.TraceEventType;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.TraceLogLevel;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.TraceSessionState;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.BaseEventComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.KernelProviderComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TargetNodeComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceChannelComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceEventComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceSessionComponent;
import org.eclipse.linuxtools.lttng2.ui.tests.Activator;
import org.eclipse.rse.core.model.Host;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.internal.core.model.SystemProfile;
import org.junit.After;
import org.junit.Before;

/**
 * The class <code>TraceControlKernelProviderTests</code> contains UST provider handling
 * test cases.
 */
@SuppressWarnings("nls")
public class TraceControlKernelProviderTests extends TestCase {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    private static final String TEST_STREAM = "CreateTreeTest.cfg";
    private static final String SCEN_SCENARIO1_TEST = "Scenario1";

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
        return new ModelImplTestSetup(new TestSuite(TraceControlKernelProviderTests.class));
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
        URL location = FileLocator.find(Activator.getDefault().getBundle(), new Path(TraceControlTestFacility.DIRECTORY + File.separator + TEST_STREAM), null);
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
    public void tearDown() throws Exception {
        fFacility.waitForJobs();
    }
    
    /**
     * Run the TraceControlComponent.
     */
    public void testKernelProviderTree() throws Exception {
        
        
        fProxy.setTestFile(fTestFile);
        fProxy.setScenario(TraceControlTestFacility.SCEN_INIT_TEST);
        
        ITraceControlComponent root = TraceControlTestFacility.getInstance().getControlView().getTraceControlRoot();

        @SuppressWarnings("restriction")
        IHost host = new Host(new SystemProfile("myProfile", true));
        host.setHostName("127.0.0.1");
        
        TargetNodeComponent node = new TargetNodeComponent("myNode", root, host, fProxy);

        root.addChild(node);

        fFacility.waitForJobs();

        fFacility.executeCommand(node, "connect");
        int i = 0;
        while ((i < 20) && (node.getTargetNodeState() != TargetNodeState.CONNECTED)) {
            i++;
            fFacility.delay(TraceControlTestFacility.GUI_REFESH_DELAY);
        }

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

        // ------------------------------------------------------------------------
        // Enable event on default channel on created session above
        // ------------------------------------------------------------------------
        // Initialize scenario
        fProxy.setScenario(SCEN_SCENARIO1_TEST);

        ITraceControlComponent[] components =  { baseEventInfo0, baseEventInfo1 };

        fFacility.executeCommand(components, "assign.event");
        
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
        fFacility.executeCommand(selection, "disableEvent");
        
        assertEquals(TraceEnablement.DISABLED, event.getState());
        assertEquals(TraceEnablement.DISABLED, event1.getState());

        // ------------------------------------------------------------------------
        // Enable event component 
        // ------------------------------------------------------------------------
        fFacility.executeCommand(event1, "enableEvent");
        
        // Verify event state
        assertEquals(TraceEnablement.ENABLED, event1.getState());
        
        // ------------------------------------------------------------------------
        // Destroy session 
        // ------------------------------------------------------------------------
        // Initialize session handling scenario
        fProxy.setScenario(TraceControlTestFacility.SCEN_SCENARIO_SESSION_HANDLING);
        
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
        assertEquals(0, fFacility.getControlView().getTraceControlRoot().getChildren().length);

    }
}