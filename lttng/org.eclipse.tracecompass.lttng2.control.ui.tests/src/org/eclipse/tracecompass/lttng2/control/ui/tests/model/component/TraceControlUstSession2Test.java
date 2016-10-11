/**********************************************************************
 * Copyright (c) 2013, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Markus Schorn - Bug 448058: Use org.eclipse.remote in favor of RSE
 **********************************************************************/

package org.eclipse.tracecompass.lttng2.control.ui.tests.model.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TargetNodeState;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceDomainType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.BufferType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.ChannelInfo;
import org.eclipse.tracecompass.internal.lttng2.control.stubs.dialogs.CreateSessionDialogStub;
import org.eclipse.tracecompass.internal.lttng2.control.stubs.dialogs.DestroyConfirmDialogStub;
import org.eclipse.tracecompass.internal.lttng2.control.stubs.dialogs.EnableChannelDialogStub;
import org.eclipse.tracecompass.internal.lttng2.control.stubs.dialogs.GetEventInfoDialogStub;
import org.eclipse.tracecompass.internal.lttng2.control.stubs.service.TestRemoteSystemProxy;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs.TraceControlDialogFactory;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.ITraceControlComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TargetNodeComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceChannelComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceDomainComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceSessionComponent;
import org.eclipse.tracecompass.tmf.remote.core.proxy.TmfRemoteConnectionFactory;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.FrameworkUtil;

/**
 * The class {@link TraceControlUstSession2Test} contains UST
 * session/channel/event handling test cases for LTTng 2.2.
 */
public class TraceControlUstSession2Test {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final String TEST_STREAM = "CreateTreeTest2.cfg";
    private static final String SCEN_SCEN_PER_UID_TEST = "ScenPerUidTest";
    private static final String SCEN_SCEN_PER_PID_TEST = "ScenPerPidTest";
    private static final String SCEN_SCEN_BUF_SIZE_TEST = "ScenBufSizeTest";

    // ------------------------------------------------------------------------
    // Test data
    // ------------------------------------------------------------------------

    private TraceControlTestFacility fFacility;
    private IRemoteConnection fHost = TmfRemoteConnectionFactory.getLocalConnection();
    private @NonNull TestRemoteSystemProxy fProxy = new TestRemoteSystemProxy(fHost);
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
        URL location = FileLocator.find(FrameworkUtil.getBundle(this.getClass()), new Path(TraceControlTestFacility.DIRECTORY + File.separator + TEST_STREAM), null);
        File testfile = new File(FileLocator.toFileURL(location).toURI());
        fTestFile = testfile.getAbsolutePath();
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
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

        TargetNodeComponent node = new TargetNodeComponent("myNode", root, fProxy);

        root.addChild(node);
        fFacility.waitForJobs();

        fFacility.executeCommand(node, "connect");
        WaitUtils.waitUntil(new TargetNodeConnectedCondition(node));

        // Get provider groups
        ITraceControlComponent[] groups = node.getChildren();
        assertNotNull(groups);
        assertEquals(2, groups.length);

        // Initialize dialog implementations for command execution
        TraceControlDialogFactory.getInstance().setCreateSessionDialog(new CreateSessionDialogStub());
        TraceControlDialogFactory.getInstance().setGetEventInfoDialog(new GetEventInfoDialogStub());
        TraceControlDialogFactory.getInstance().setConfirmDialog(new DestroyConfirmDialogStub());

        // Initialize scenario
        fProxy.setScenario(SCEN_SCEN_PER_UID_TEST);

        // ------------------------------------------------------------------------
        // Create session
        // ------------------------------------------------------------------------
        TraceSessionComponent session = fFacility.createSession(groups[1]);

        // Verify that session was created
        assertNotNull(session);
        assertEquals("mysession", session.getName());

        // ------------------------------------------------------------------------
        // Enable default channel on created session above
        // ------------------------------------------------------------------------
        EnableChannelDialogStub channelStub = new EnableChannelDialogStub();
        channelStub.setDomain(TraceDomainType.UST);
        ChannelInfo info = (ChannelInfo)channelStub.getChannelInfo();
        info.setName("mychannel");
        info.setOverwriteMode(false);
        info.setSubBufferSize(-1);
        info.setNumberOfSubBuffers(-1);
        info.setSwitchTimer(-1);
        info.setReadTimer(-1);
        info.setMaxNumberTraceFiles(-1);
        info.setMaxSizeTraceFiles(-1);
        info.setBufferType(BufferType.BUFFER_PER_UID);
        channelStub.setChannelInfo(info);

        TraceControlDialogFactory.getInstance().setEnableChannelDialog(channelStub);

        fFacility.executeCommand(session, "enableChannelOnSession");

        // Verify that UST domain was created
        ITraceControlComponent[] domains = session.getChildren();
        assertNotNull(domains);
        assertEquals(1, domains.length);

        assertEquals("UST global", domains[0].getName());
        assertEquals("Domain buffer Type", BufferType.BUFFER_PER_UID, ((TraceDomainComponent)domains[0]).getBufferType());

        // Verify that channel was created with correct data
        ITraceControlComponent[] channels =  domains[0].getChildren();
        assertNotNull(channels);
        assertEquals(1, channels.length);

        assertTrue(channels[0] instanceof TraceChannelComponent);
        TraceChannelComponent channel = (TraceChannelComponent) channels[0];
        assertEquals("mychannel", channel.getName());

        // ------------------------------------------------------------------------
        // Destroy session
        // ------------------------------------------------------------------------
        fFacility.destroySession(session);

        // Verify that no more session components exist
        assertEquals(0, groups[1].getChildren().length);

        // ------------------------------------------------------------------------
        // Create session (per-pid buffers)
        // ------------------------------------------------------------------------

        // Initialize scenario
        fProxy.setScenario(SCEN_SCEN_PER_PID_TEST);

        session = fFacility.createSession(groups[1]);

        // Verify that session was created
        assertNotNull(session);
        assertEquals("mysession", session.getName());

        // ------------------------------------------------------------------------
        // Enable default channel on created session above
        // ------------------------------------------------------------------------
        info = (ChannelInfo)channelStub.getChannelInfo();
        info.setName("mychannel");
        info.setBufferType(BufferType.BUFFER_PER_PID);
        channelStub.setChannelInfo(info);

        fFacility.executeCommand(session, "enableChannelOnSession");

        // Verify that UST domain was created
        domains = session.getChildren();
        assertNotNull(domains);
        assertEquals(1, domains.length);

        assertEquals("UST global", domains[0].getName());
        assertEquals("Domain buffer Type", BufferType.BUFFER_PER_PID, ((TraceDomainComponent)domains[0]).getBufferType());

        // Verify that channel was created with correct data
        channels =  domains[0].getChildren();
        assertNotNull(channels);
        assertEquals(1, channels.length);

        assertTrue(channels[0] instanceof TraceChannelComponent);
        channel = (TraceChannelComponent) channels[0];
        assertEquals("mychannel", channel.getName());

        // ------------------------------------------------------------------------
        // Destroy session
        // ------------------------------------------------------------------------
        fFacility.destroySession(session);

        // Verify that no more session components exist
        assertEquals(0, groups[1].getChildren().length);

        // ------------------------------------------------------------------------
        // Create session (configured file size and number of files)
        // ------------------------------------------------------------------------

        // Initialize scenario
        fProxy.setScenario(SCEN_SCEN_BUF_SIZE_TEST);

        session = fFacility.createSession(groups[1]);

        // Verify that session was created
        assertNotNull(session);
        assertEquals("mysession", session.getName());

        // ------------------------------------------------------------------------
        // Enable default channel on created session above
        // ------------------------------------------------------------------------
        info = (ChannelInfo)channelStub.getChannelInfo();
        info.setName("mychannel");
        info.setMaxNumberTraceFiles(10);
        info.setMaxSizeTraceFiles(1024);
        info.setBufferType(BufferType.BUFFER_TYPE_UNKNOWN);
        channelStub.setChannelInfo(info);

        fFacility.executeCommand(session, "enableChannelOnSession");

        // Verify that UST domain was created
        domains = session.getChildren();
        assertNotNull(domains);
        assertEquals(1, domains.length);

        assertEquals("UST global", domains[0].getName());
        assertEquals("Domain buffer Type", BufferType.BUFFER_PER_PID, ((TraceDomainComponent)domains[0]).getBufferType());

        // Verify that channel was created with correct data
        channels =  domains[0].getChildren();
        assertNotNull(channels);
        assertEquals(1, channels.length);

        assertTrue(channels[0] instanceof TraceChannelComponent);
        channel = (TraceChannelComponent) channels[0];
        assertEquals("mychannel", channel.getName());

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