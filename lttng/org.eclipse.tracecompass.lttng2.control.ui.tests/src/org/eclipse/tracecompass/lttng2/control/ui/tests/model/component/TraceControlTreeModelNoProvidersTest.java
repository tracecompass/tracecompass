/**********************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Alexandre Montplaisir - Port to JUnit4
 *   Markus Schorn - Bug 448058: Use org.eclipse.remote in favor of RSE
 **********************************************************************/

package org.eclipse.tracecompass.lttng2.control.ui.tests.model.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TargetNodeState;
import org.eclipse.tracecompass.internal.lttng2.control.stubs.service.TestRemoteSystemProxy;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.ITraceControlComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TargetNodeComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceProviderGroup;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceSessionGroup;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.UstProviderComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.service.ILttngControlService;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.service.LTTngControlService;
import org.eclipse.tracecompass.tmf.remote.core.proxy.TmfRemoteConnectionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.FrameworkUtil;

/**
 * The class <code>TraceControlTreeModelNoProvidersTest</code> verifies that the
 * Tracer Control can handle the case where no kernel provider and only UST
 * provider are available.
 */
public class TraceControlTreeModelNoProvidersTest {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final String TEST_STREAM = "ListInfoTest.cfg";
    private static final String SCEN_LIST_INFO_TEST = "ListInfoTestNoKernel";
    private static final String TARGET_NODE_NAME = "myNode";

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
    public void tearDown()  {
        fFacility.dispose();
    }

    /**
     * Run the TraceControlComponent.
     */
    @Test
    public void testTraceControlComponents() {

        fProxy.setTestFile(fTestFile);
        fProxy.setScenario(SCEN_LIST_INFO_TEST);

        ITraceControlComponent root = fFacility.getControlView().getTraceControlRoot();

        TargetNodeComponent node = new TargetNodeComponent(TARGET_NODE_NAME, root, fProxy);

        root.addChild(node);
        node.connect();

        fFacility.waitForConnect(node);
        fFacility.waitForJobs();

        // ------------------------------------------------------------------------
        // Verify Parameters of TargetNodeComponent
        // ------------------------------------------------------------------------
        assertEquals("Local", node.getToolTip()); // assigned in createLocalHost() above
        Image connectedImage = node.getImage();
        assertNotNull(connectedImage);
        assertEquals(TargetNodeState.CONNECTED, node.getTargetNodeState());
        assertNotNull(node.getControlService());
        ILttngControlService service = node.getControlService();
        assertTrue(service instanceof LTTngControlService);
        node.setControlService(service);
        assertTrue(node.getControlService() instanceof LTTngControlService);

        // ------------------------------------------------------------------------
        // Verify Children of TargetNodeComponent
        // ------------------------------------------------------------------------
        ITraceControlComponent[] groups = node.getChildren();
        assertNotNull(groups);
        assertEquals(2, groups.length);

        assertTrue(groups[0] instanceof TraceProviderGroup);
        assertTrue(groups[1] instanceof TraceSessionGroup);

        // Check for kernel provider
        TraceProviderGroup providerGroup = (TraceProviderGroup) groups[0];
        assertFalse(providerGroup.hasKernelProvider());

        assertEquals("Provider", providerGroup.getName());
        assertEquals("Sessions", groups[1].getName());

        // ------------------------------------------------------------------------
        // Verify TraceProviderGroup
        // ------------------------------------------------------------------------
        ITraceControlComponent[] providers = groups[0].getChildren();

        assertNotNull(providers);
        assertEquals(1, providers.length);
        assertTrue(providers[0] instanceof UstProviderComponent);

        // disconnect
        node.disconnect();
        assertEquals(TargetNodeState.DISCONNECTED, node.getTargetNodeState());
        assertNotNull(node.getImage());
        assertNotSame(connectedImage, node.getImage());

        node.getParent().removeChild(node);
    }
}