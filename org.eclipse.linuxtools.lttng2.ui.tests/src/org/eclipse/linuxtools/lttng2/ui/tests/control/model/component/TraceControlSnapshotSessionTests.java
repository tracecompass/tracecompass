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

package org.eclipse.linuxtools.lttng2.ui.tests.control.model.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.TargetNodeState;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.TraceSessionState;
import org.eclipse.linuxtools.internal.lttng2.stubs.dialogs.CreateSessionDialogStub;
import org.eclipse.linuxtools.internal.lttng2.stubs.dialogs.DestroyConfirmDialogStub;
import org.eclipse.linuxtools.internal.lttng2.stubs.dialogs.GetEventInfoDialogStub;
import org.eclipse.linuxtools.internal.lttng2.stubs.service.TestRemoteSystemProxy;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.TraceControlDialogFactory;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TargetNodeComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceSessionComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.property.TraceSessionPropertySource;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.FrameworkUtil;

/**
 * The class <code>TraceControlSnapshotSessionTests</code> contains Snapshot
 * session test cases for support of LTTng Tools 2.3.
 */
public class TraceControlSnapshotSessionTests {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final String TEST_STREAM = "CreateSessionTest2.cfg";
    private static final String SCEN_CREATE_SESSION = "ScenCreateSession";

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
        // --- snapshot session ---
        CreateSessionDialogStub sessionDialog = new CreateSessionDialogStub();
        sessionDialog.setSnapshot(true);

        TraceControlDialogFactory.getInstance().setCreateSessionDialog(sessionDialog);
        TraceControlDialogFactory.getInstance().setGetEventInfoDialog(new GetEventInfoDialogStub());
        TraceControlDialogFactory.getInstance().setConfirmDialog(new DestroyConfirmDialogStub());

        // Initialize scenario
        fProxy.setScenario(SCEN_CREATE_SESSION);

        // ------------------------------------------------------------------------
        // Create session
        // ------------------------------------------------------------------------
        TraceSessionComponent session = fFacility.createSession(groups[1]);

        // Verify that session was created
        assertNotNull(session);
        assertEquals("mysession", session.getName());
        assertTrue(session.isSnapshotSession());
        assertNotNull(session.getSnapshotInfo());
        assertEquals("snapshot-1", session.getSnapshotInfo().getName());
        assertEquals("/home/user/lttng-traces/mysession-20130913-141651", session.getSnapshotInfo().getSnapshotPath());
        assertEquals(1, session.getSnapshotInfo().getId());

        // ------------------------------------------------------------------------
        // Start session
        // ------------------------------------------------------------------------
        fFacility.startSession(session);
        assertEquals(TraceSessionState.ACTIVE, session.getSessionState());

        // verify properties
        Object adapter = session.getAdapter(IPropertySource.class);
        assertNotNull(adapter);
        assertTrue(adapter instanceof TraceSessionPropertySource);

        TraceSessionPropertySource sessionSource = (TraceSessionPropertySource)adapter;
        IPropertyDescriptor[] descriptors = sessionSource.getPropertyDescriptors();
        assertNotNull(descriptors);

        Map<String,String> map = new HashMap<>();
        map.put(TraceSessionPropertySource.TRACE_SESSION_NAME_PROPERTY_ID, "mysession");
        map.put(TraceSessionPropertySource.TRACE_SNAPSHOT_NAME_PROPERTY_ID, "snapshot-1");
        map.put(TraceSessionPropertySource.TRACE_SNAPSHOT_PATH_PROPERTY_ID, "/home/user/lttng-traces/mysession-20130913-141651");
        map.put(TraceSessionPropertySource.TRACE_SNAPSHOT_ID_PROPERTY_ID, "1");
        map.put(TraceSessionPropertySource.TRACE_SESSION_STATE_PROPERTY_ID, TraceSessionState.ACTIVE.name());

        for (int j = 0; j < descriptors.length; j++) {
            String expected = map.get(descriptors[j].getId());
            assertNotNull(expected);
            assertEquals(expected, sessionSource.getPropertyValue(descriptors[j].getId()).toString());
        }

        // ------------------------------------------------------------------------
        // Record snapshot
        // ------------------------------------------------------------------------
        fFacility.executeCommand(session, "snapshot");

        // ------------------------------------------------------------------------
        // Stop snapshot
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