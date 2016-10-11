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
 *   Jonathan Rajotte - Support for LTTng 2.6
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
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceSessionState;
import org.eclipse.tracecompass.internal.lttng2.control.stubs.dialogs.CreateSessionDialogStub;
import org.eclipse.tracecompass.internal.lttng2.control.stubs.dialogs.DestroyConfirmDialogStub;
import org.eclipse.tracecompass.internal.lttng2.control.stubs.service.TestRemoteSystemProxy;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs.TraceControlDialogFactory;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.ITraceControlComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TargetNodeComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceSessionComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.service.ILttngControlService;
import org.eclipse.tracecompass.tmf.remote.core.proxy.TmfRemoteConnectionFactory;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.FrameworkUtil;

/**
 * The class {@link TraceControlKernelSessionTest} contains Kernel
 * session/channel/event handling test cases.
 */
public class TraceControlCreateSessionTest {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    private static final String TEST_STREAM = "CreateSessionTest.cfg";
    private static final String SCEN_SCENARIO_FILE_PROTO_TEST = "CreateSessionFileProto";
    private static final String SCEN_SCENARIO_CONTROL_DATA_TEST = "CreateSessionControlData";
    private static final String SCEN_SCENARIO_NETWORK_TEST = "CreateSessionNetwork";
    private static final String SCEN_SCENARIO_NETWORK2_TEST = "CreateSessionNetwork2";

    private static final String SESSION = "mysession";

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
     *             if the initialization fails for some reason
     */
    @Before
    public void setUp() throws Exception {
        fFacility = TraceControlTestFacility.getInstance();
        fFacility.init();
        URL location = FileLocator.find(FrameworkUtil.getBundle(this.getClass()), new Path(TraceControlTestFacility.DIRECTORY + File.separator + getTestStream()), null);
        File testfile = new File(FileLocator.toFileURL(location).toURI());
        fTestFile = testfile.getAbsolutePath();
    }

    /**
     * Get the test stream file name to use for the test suite
     *
     * @return the name of the test stream file
     */
    protected String getTestStream() {
        return TEST_STREAM;
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
     *             on internal error
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

        // Verify that node is connected
        assertEquals(TargetNodeState.CONNECTED, node.getTargetNodeState());

        ILttngControlService controleService = node.getControlService();

        // Get provider groups
        ITraceControlComponent[] groups = node.getChildren();
        assertNotNull(groups);
        assertEquals(2, groups.length);

        // Initialize dialog implementations for command execution
        CreateSessionDialogStub sessionDialogStub = new CreateSessionDialogStub();
        TraceControlDialogFactory.getInstance().setCreateSessionDialog(sessionDialogStub);
        TraceControlDialogFactory.getInstance().setConfirmDialog(new DestroyConfirmDialogStub());

        // ------------------------------------------------------------------------
        // Create session (--U file://...) and destroy
        // ------------------------------------------------------------------------
        // Initialize session handling scenario
        fProxy.setScenario(SCEN_SCENARIO_FILE_PROTO_TEST);

        sessionDialogStub.setNetworkUrl("file:///tmp");
        sessionDialogStub.setStreamedTrace(true);
        TraceSessionComponent session = fFacility.createSession(groups[1]);

        // Verify that session was created
        assertNotNull(session);
        assertEquals(getSessionName(), session.getName());
        if (controleService.isVersionSupported("2.6.0")) {
            assertEquals("/tmp", session.getSessionPath());
        } else {
            assertEquals("file:///tmp", session.getSessionPath());
        }

        assertTrue(!session.isStreamedTrace());
        assertEquals(TraceSessionState.INACTIVE, session.getSessionState());
        sessionDialogStub.setNetworkUrl(null);
        sessionDialogStub.setStreamedTrace(false);

        fFacility.destroySession(session);

        // Verify that no more session components exist
        assertEquals(0, groups[1].getChildren().length);

        // ------------------------------------------------------------------------
        // Create session (--U file://,,, and destroy
        // ------------------------------------------------------------------------
        // Initialize session handling scenario
        fProxy.setScenario(SCEN_SCENARIO_CONTROL_DATA_TEST);

        sessionDialogStub.setControlUrl("tcp://172.0.0.1");
        sessionDialogStub.setDataUrl("tcp://172.0.0.1:5343");
        sessionDialogStub.setStreamedTrace(true);

        session = fFacility.createSession(groups[1]);

        // Verify that session was created
        assertNotNull(session);
        assertEquals(getSessionName(), session.getName());
        if (controleService.isVersionSupported("2.6.0")) {
            assertEquals("tcp4://172.0.0.1:5342/ [data: 5343]", session.getSessionPath());
        } else {
            assertEquals("tcp://172.0.0.1:5342 [data: 5343]", session.getSessionPath());
        }
        assertTrue(session.isStreamedTrace());
        assertEquals(TraceSessionState.INACTIVE, session.getSessionState());
        sessionDialogStub.setControlUrl(null);
        sessionDialogStub.setDataUrl(null);
        sessionDialogStub.setStreamedTrace(false);

        fFacility.destroySession(session);

        // Verify that no more session components exist
        assertEquals(0, groups[1].getChildren().length);

        // ------------------------------------------------------------------------
        // Create session (--U file://... and destroy
        // ------------------------------------------------------------------------
        // Initialize session handling scenario
        fProxy.setScenario(SCEN_SCENARIO_NETWORK_TEST);

        sessionDialogStub.setNetworkUrl("net://172.0.0.1:1234:2345");
        sessionDialogStub.setStreamedTrace(true);

        session = fFacility.createSession(groups[1]);

        // Verify that session was created
        assertNotNull(session);
        assertEquals(getSessionName(), session.getName());
        if (controleService.isVersionSupported("2.6.0")) {
            assertEquals("tcp4://172.0.0.1:1234/mysession-20140820-153527 [data: 2345]", session.getSessionPath());
        } else {
            assertEquals("net://172.0.0.1:1234 [data: 2345]", session.getSessionPath());
        }
        assertTrue(session.isStreamedTrace());
        assertEquals(TraceSessionState.INACTIVE, session.getSessionState());
        sessionDialogStub.setNetworkUrl(null);

        fFacility.destroySession(session);

        // Verify that no more session components exist
        assertEquals(0, groups[1].getChildren().length);

        // ------------------------------------------------------------------------
        // Create session (--U net6://[...] and destroy
        // ------------------------------------------------------------------------
        // Initialize session handling scenario
        fProxy.setScenario(SCEN_SCENARIO_NETWORK2_TEST);

        sessionDialogStub.setNetworkUrl("net6://[ffff::eeee:dddd:cccc:0]");
        sessionDialogStub.setStreamedTrace(true);

        session = fFacility.createSession(groups[1]);

        // Verify that session was created
        assertNotNull(session);
        assertEquals(getSessionName(), session.getName());
        if (controleService.isVersionSupported("2.6.0")) {
            assertEquals("tcp6://[ffff::eeee:dddd:cccc:0]:5342/mysession-20140820-153801 [data: 5343]", session.getSessionPath());
        } else {
            assertEquals("net://[ffff::eeee:dddd:cccc:0]:5342/mysession-20130221-144451 [data: 5343]", session.getSessionPath());
        }
        assertTrue(session.isStreamedTrace());
        assertEquals(TraceSessionState.INACTIVE, session.getSessionState());
        sessionDialogStub.setNetworkUrl(null);

        fFacility.destroySession(session);

        // Verify that no more session components exist
        assertEquals(0, groups[1].getChildren().length);

        // -------------------------------------------------------------------------
        // Disconnect node
        // -------------------------------------------------------------------------
        fFacility.executeCommand(node, "disconnect");
        assertEquals(TargetNodeState.DISCONNECTED, node.getTargetNodeState());

        // -------------------------------------------------------------------------
        // Delete node
        // -------------------------------------------------------------------------

        fFacility.executeCommand(node, "delete");
        assertEquals(0, fFacility.getControlView().getTraceControlRoot().getChildren().length);
    }

    private static String getSessionName() {
        return SESSION;
    }

}
