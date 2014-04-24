/**********************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.lttng2.control.ui.tests.model.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.TargetNodeState;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.TraceSessionState;
import org.eclipse.linuxtools.internal.lttng2.control.stubs.dialogs.CreateSessionDialogStub;
import org.eclipse.linuxtools.internal.lttng2.control.stubs.dialogs.DestroyConfirmDialogStub;
import org.eclipse.linuxtools.internal.lttng2.control.stubs.service.TestRemoteSystemProxy;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.dialogs.TraceControlDialogFactory;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.ITraceControlComponent;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl.TargetNodeComponent;
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
 * The class <code>TraceControlKernelSessionTests</code> contains Kernel session/channel/event
 * handling test cases.
 */

@SuppressWarnings("javadoc")
public class TraceControlCreateSessionTests {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    private static final String TEST_STREAM = "CreateSessionTest.cfg";
    private static final String SCEN_SCENARIO_FILE_PROTO_TEST = "CreateSessionFileProto";
    private static final String SCEN_SCENARIO_CONTROL_DATA_TEST = "CreateSessionControlData";
    private static final String SCEN_SCENARIO_NETWORK_TEST = "CreateSessionNetwork";
    private static final String SCEN_SCENARIO_NETWORK2_TEST = "CreateSessionNetwork2";

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
     *
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
     *
     * @throws Exception
     *         if the clean-up fails for some reason
     *
     */
    @After
    public void tearDown() {
        fFacility.waitForJobs();
    }

    /**
     * Run the TraceControlComponent.
     */
    @Test
    public void testTraceSessionTree() throws Exception {

        fProxy.setTestFile(fTestFile);
        fProxy.setScenario(TraceControlTestFacility.SCEN_INIT_TEST);

        ITraceControlComponent root = TraceControlTestFacility.getInstance().getControlView().getTraceControlRoot();

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
        assertEquals("mysession", session.getName());
        assertEquals("file:///tmp", session.getSessionPath());
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
        assertEquals("mysession", session.getName());
        assertEquals("tcp://172.0.0.1:5342 [data: 5343]", session.getSessionPath());
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
        assertEquals("mysession", session.getName());
        assertEquals("net://172.0.0.1:1234 [data: 2345]", session.getSessionPath());
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
        assertEquals("mysession", session.getName());
        assertEquals("net://[ffff::eeee:dddd:cccc:0]:5342/mysession-20130221-144451 [data: 5343]", session.getSessionPath());
        assertTrue(session.isStreamedTrace());
        assertEquals(TraceSessionState.INACTIVE, session.getSessionState());
        sessionDialogStub.setNetworkUrl(null);

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
