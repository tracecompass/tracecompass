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

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.IChannelInfo;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.TargetNodeState;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.TraceEnablement;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.TraceEventType;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.TraceLogLevel;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.TraceSessionState;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.impl.BufferType;
import org.eclipse.linuxtools.internal.lttng2.stubs.service.TestRemoteSystemProxy;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.BaseEventComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.KernelProviderComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TargetNodeComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceChannelComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceDomainComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceEventComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceProbeEventComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceSessionComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.UstProviderComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.property.BaseEventPropertySource;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.property.KernelProviderPropertySource;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.property.TargetNodePropertySource;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.property.TraceChannelPropertySource;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.property.TraceDomainPropertySource;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.property.TraceEventPropertySource;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.property.TraceProbeEventPropertySource;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.property.TraceSessionPropertySource;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.property.UstProviderPropertySource;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.ui.views.properties.IPropertySource;
import org.junit.After;
import org.junit.Test;
import org.osgi.framework.FrameworkUtil;

/**
 * The class <code>TraceControlPropertiesTest</code> contains tests for the all
 * property class</code>.
 */
public class TraceControlPropertiesTest {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final String DIRECTORY   = "testfiles";
    private static final String TEST_STREAM = "ListInfoTest.cfg";
    private static final String SCEN_LIST_INFO_TEST = "ListInfoTest";

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        TraceControlTestFacility.getInstance().waitForJobs();
    }

    /**
     * Run the TraceControlComponent.
     *
     * @throws Exception
     *             This will fail the test
     */
    @Test
    public void testComponentProperties() throws Exception {

        TestRemoteSystemProxy proxy = new TestRemoteSystemProxy();

        URL location = FileLocator.find(FrameworkUtil.getBundle(this.getClass()), new Path(DIRECTORY + File.separator + TEST_STREAM), null);
        File testfile = new File(FileLocator.toFileURL(location).toURI());
        proxy.setTestFile(testfile.getAbsolutePath());
        proxy.setScenario(SCEN_LIST_INFO_TEST);

        ITraceControlComponent root = TraceControlTestFacility.getInstance().getControlView().getTraceControlRoot();

        ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
        ISystemProfile profile =  registry.createSystemProfile("myProfile", true);
        IHost host = registry.createLocalHost(profile, "myProfile", "user");

        TargetNodeComponent node = new TargetNodeComponent("myNode", root, host, proxy);

        root.addChild(node);
        node.connect();

        TraceControlTestFacility.getInstance().waitForJobs();

        // ------------------------------------------------------------------------
        // Verify Node Properties (adapter)
        // ------------------------------------------------------------------------
        Object adapter = node.getAdapter(IPropertySource.class);
        assertNotNull(adapter);
        assertTrue(adapter instanceof TargetNodePropertySource);

        TargetNodePropertySource source = (TargetNodePropertySource)adapter;

        assertNull(source.getEditableValue());
        assertFalse(source.isPropertySet(TargetNodePropertySource.TARGET_NODE_NAME_PROPERTY_ID));
        assertNotNull(source.getPropertyDescriptors());

        assertEquals("myNode", source.getPropertyValue(TargetNodePropertySource.TARGET_NODE_NAME_PROPERTY_ID));
        assertEquals("LOCALHOST",  source.getPropertyValue(TargetNodePropertySource.TARGET_NODE_ADDRESS_PROPERTY_ID));
        assertEquals(TargetNodeState.CONNECTED.name(), source.getPropertyValue(TargetNodePropertySource.TARGET_NODE_STATE_PROPERTY_ID));
        assertEquals("2.1.0", source.getPropertyValue(TargetNodePropertySource.TARGET_NODE_VERSION_PROPERTY_ID));
        assertNull(source.getPropertyValue("test"));

        adapter = node.getAdapter(IChannelInfo.class);
        assertNull(adapter);

        ITraceControlComponent[] groups = node.getChildren();
        assertNotNull(groups);
        assertEquals(2, groups.length);

        ITraceControlComponent[] providers = groups[0].getChildren();

        assertNotNull(providers);
        assertEquals(3, providers.length);

        // ------------------------------------------------------------------------
        // Verify Kernel Provider Properties (adapter)
        // ------------------------------------------------------------------------
        KernelProviderComponent kernelProvider = (KernelProviderComponent) providers[0];

        adapter = kernelProvider.getAdapter(IPropertySource.class);
        assertNotNull(adapter);
        assertTrue(adapter instanceof KernelProviderPropertySource);

        KernelProviderPropertySource kernelSource = (KernelProviderPropertySource)adapter;
        assertNotNull(kernelSource.getPropertyDescriptors());

        assertEquals("Kernel", kernelSource.getPropertyValue(KernelProviderPropertySource.KERNEL_PROVIDER_NAME_PROPERTY_ID));

        // ------------------------------------------------------------------------
        // Verify UST Provider Properties (adapter)
        // ------------------------------------------------------------------------
        UstProviderComponent ustProvider = (UstProviderComponent) providers[1];

        adapter = ustProvider.getAdapter(IPropertySource.class);
        assertNotNull(adapter);
        assertTrue(adapter instanceof UstProviderPropertySource);

        UstProviderPropertySource ustSource = (UstProviderPropertySource)adapter;
        assertNotNull(ustSource.getPropertyDescriptors());

        assertEquals("/home/user/git/lttng-ust/tests/hello.cxx/.libs/lt-hello [PID=9379]", ustSource.getPropertyValue(UstProviderPropertySource.UST_PROVIDER_NAME_PROPERTY_ID));
        assertEquals(String.valueOf(9379), ustSource.getPropertyValue(UstProviderPropertySource.UST_PROVIDER_PID_PROPERTY_ID));

        // ------------------------------------------------------------------------
        // Verify Base Event Properties (adapter)
        // ------------------------------------------------------------------------
        ITraceControlComponent[] events = ustProvider.getChildren();
        assertNotNull(events);
        assertEquals(2, events.length);

        BaseEventComponent baseEventInfo = (BaseEventComponent) events[0];
        assertNotNull(baseEventInfo);

        adapter = baseEventInfo.getAdapter(IPropertySource.class);
        assertNotNull(adapter);
        assertTrue(adapter instanceof BaseEventPropertySource);

        BaseEventPropertySource baseSource = (BaseEventPropertySource)adapter;
        assertNotNull(baseSource.getPropertyDescriptors());

        assertEquals("ust_tests_hello:tptest_sighandler", baseSource.getPropertyValue(BaseEventPropertySource.BASE_EVENT_NAME_PROPERTY_ID));
        assertEquals(TraceEventType.TRACEPOINT.name(), baseSource.getPropertyValue(BaseEventPropertySource.BASE_EVENT_TYPE_PROPERTY_ID));
        assertEquals(TraceLogLevel.TRACE_DEBUG_MODULE.name(), baseSource.getPropertyValue(BaseEventPropertySource.BASE_EVENT_LOGLEVEL_PROPERTY_ID));

        baseEventInfo = (BaseEventComponent) events[1];
        assertNotNull(baseEventInfo);

        adapter = baseEventInfo.getAdapter(IPropertySource.class);
        assertNotNull(adapter);
        assertTrue(adapter instanceof BaseEventPropertySource);
        baseSource = (BaseEventPropertySource)adapter;
        assertNotNull(baseSource.getPropertyDescriptors());
        assertEquals("doublefield=float;floatfield=float;stringfield=string", baseSource.getPropertyValue(BaseEventPropertySource.BASE_EVENT_FIELDS_PROPERTY_ID));

        // ------------------------------------------------------------------------
        // Verify Session Properties (adapter)
        // ------------------------------------------------------------------------
        ITraceControlComponent[] sessions = groups[1].getChildren();
        assertNotNull(sessions);
        assertEquals(2, sessions.length);

        TraceSessionComponent session = (TraceSessionComponent)sessions[1];

        adapter = session.getAdapter(IPropertySource.class);
        assertNotNull(adapter);
        assertTrue(adapter instanceof TraceSessionPropertySource);

        TraceSessionPropertySource sessionSource = (TraceSessionPropertySource)adapter;
        assertNotNull(sessionSource.getPropertyDescriptors());

        assertEquals("mysession", sessionSource.getPropertyValue(TraceSessionPropertySource.TRACE_SESSION_NAME_PROPERTY_ID));
        assertEquals("/home/user/lttng-traces/mysession-20120129-084256", sessionSource.getPropertyValue(TraceSessionPropertySource.TRACE_SESSION_PATH_PROPERTY_ID));
        assertEquals(TraceSessionState.ACTIVE.name(), sessionSource.getPropertyValue(TraceSessionPropertySource.TRACE_SESSION_STATE_PROPERTY_ID));

        // ------------------------------------------------------------------------
        // Verify Domain Provider Properties (adapter)
        // ------------------------------------------------------------------------
        ITraceControlComponent[] domains = session.getChildren();
        assertNotNull(domains);
        assertEquals(2, domains.length);

        TraceDomainComponent domain = (TraceDomainComponent) domains[0];
        adapter = domain.getAdapter(IPropertySource.class);
        assertNotNull(adapter);
        assertTrue(adapter instanceof TraceDomainPropertySource);

        TraceDomainPropertySource domainSource = (TraceDomainPropertySource)adapter;
        assertNotNull(domainSource.getPropertyDescriptors());

        assertEquals("Kernel", domainSource.getPropertyValue(TraceDomainPropertySource.TRACE_DOMAIN_NAME_PROPERTY_ID));
        assertEquals(BufferType.BUFFER_SHARED.getInName(), domainSource.getPropertyValue(TraceDomainPropertySource.BUFFER_TYPE_PROPERTY_ID));

        ITraceControlComponent[] channels =  domains[0].getChildren();
        assertNotNull(channels);
        assertEquals(2, channels.length);

        // ------------------------------------------------------------------------
        // Verify Channel Properties (adapter)
        // ------------------------------------------------------------------------
        assertTrue(channels[0] instanceof TraceChannelComponent);
        TraceChannelComponent channel = (TraceChannelComponent) channels[0];

        adapter = channel.getAdapter(IPropertySource.class);
        assertNotNull(adapter);
        assertTrue(adapter instanceof TraceChannelPropertySource);

        TraceChannelPropertySource channelSource = (TraceChannelPropertySource)adapter;
        assertNotNull(channelSource.getPropertyDescriptors());

        assertEquals("channel0", channelSource.getPropertyValue(TraceChannelPropertySource.TRACE_CHANNEL_NAME_PROPERTY_ID));
        assertEquals(String.valueOf(4), channelSource.getPropertyValue(TraceChannelPropertySource.TRACE_CHANNEL_NO_SUBBUFFERS_PROPERTY_ID));
        assertEquals(TraceEnablement.ENABLED.name(), channelSource.getPropertyValue(TraceChannelPropertySource.TRACE_CHANNEL_STATE_PROPERTY_ID));
        assertEquals(String.valueOf(false), channelSource.getPropertyValue(TraceChannelPropertySource.TRACE_CHANNEL_OVERWRITE_MODE_PROPERTY_ID));
        assertEquals("splice()", channelSource.getPropertyValue(TraceChannelPropertySource.TRACE_CHANNEL_OUTPUT_TYPE_PROPERTY_ID));
        assertEquals(String.valueOf(200), channelSource.getPropertyValue(TraceChannelPropertySource.TRACE_CHANNEL_READ_TIMER_PROPERTY_ID));
        assertEquals(String.valueOf(262144), channelSource.getPropertyValue(TraceChannelPropertySource.TRACE_CHANNEL_SUBBUFFER_SIZE_PROPERTY_ID));
        assertEquals(String.valueOf(0), channelSource.getPropertyValue(TraceChannelPropertySource.TRACE_CHANNEL_SWITCH_TIMER_PROPERTY_ID));

        // ------------------------------------------------------------------------
        // Verify Event Properties (adapter)
        // ------------------------------------------------------------------------
        ITraceControlComponent[] channel0Events = channel.getChildren();
        assertNotNull(channel0Events);
        assertEquals(5, channel0Events.length);
        assertTrue(channel0Events[0] instanceof TraceEventComponent);

        TraceEventComponent event = (TraceEventComponent) channel0Events[0];

        adapter = event.getAdapter(IPropertySource.class);
        assertNotNull(adapter);
        assertTrue(adapter instanceof TraceEventPropertySource);

        TraceEventPropertySource eventSource = (TraceEventPropertySource)adapter;
        assertNotNull(eventSource.getPropertyDescriptors());

        assertEquals("block_rq_remap", eventSource.getPropertyValue(TraceEventPropertySource.TRACE_EVENT_NAME_PROPERTY_ID));
        assertEquals(TraceLogLevel.TRACE_EMERG.name(), eventSource.getPropertyValue(TraceEventPropertySource.TRACE_EVENT_LOGLEVEL_PROPERTY_ID));
        assertEquals(TraceEventType.TRACEPOINT.name(), eventSource.getPropertyValue(TraceEventPropertySource.TRACE_EVENT_TYPE_PROPERTY_ID));
        assertEquals(TraceEnablement.ENABLED.name(), eventSource.getPropertyValue(TraceEventPropertySource.TRACE_EVENT_STATE_PROPERTY_ID));

        // ------------------------------------------------------------------------
        // Verify Probe Event Properties (adapter)
        // ------------------------------------------------------------------------
        assertTrue(channel0Events[2] instanceof TraceProbeEventComponent);

        TraceProbeEventComponent probeEvent = (TraceProbeEventComponent) channel0Events[2];

        adapter = probeEvent.getAdapter(IPropertySource.class);
        assertNotNull(adapter);
        assertTrue(adapter instanceof TraceProbeEventPropertySource);

        TraceProbeEventPropertySource probeEventSource = (TraceProbeEventPropertySource)adapter;
        assertNotNull(probeEventSource.getPropertyDescriptors());
        assertEquals(4, probeEventSource.getPropertyDescriptors().length);

        assertEquals("myevent2", probeEventSource.getPropertyValue(TraceEventPropertySource.TRACE_EVENT_NAME_PROPERTY_ID));
        assertEquals(TraceEventType.PROBE.name(), probeEventSource.getPropertyValue(TraceEventPropertySource.TRACE_EVENT_TYPE_PROPERTY_ID));
        assertEquals(TraceEnablement.ENABLED.name(), probeEventSource.getPropertyValue(TraceEventPropertySource.TRACE_EVENT_STATE_PROPERTY_ID));
        assertEquals("0xc0101340", probeEventSource.getPropertyValue(TraceProbeEventPropertySource.TRACE_EVENT_PROBE_ADDRESS_PROPERTY_ID));

        assertTrue(channel0Events[3] instanceof TraceProbeEventComponent);

        probeEvent = (TraceProbeEventComponent) channel0Events[3];

        adapter = probeEvent.getAdapter(IPropertySource.class);
        assertNotNull(adapter);
        assertTrue(adapter instanceof TraceProbeEventPropertySource);

        probeEventSource = (TraceProbeEventPropertySource)adapter;
        assertNotNull(probeEventSource.getPropertyDescriptors());
        assertEquals(5, probeEventSource.getPropertyDescriptors().length);

        assertEquals("myevent0", probeEventSource.getPropertyValue(TraceEventPropertySource.TRACE_EVENT_NAME_PROPERTY_ID));
        assertEquals(TraceEventType.PROBE.name(), probeEventSource.getPropertyValue(TraceEventPropertySource.TRACE_EVENT_TYPE_PROPERTY_ID));
        assertEquals(TraceEnablement.ENABLED.name(), probeEventSource.getPropertyValue(TraceEventPropertySource.TRACE_EVENT_STATE_PROPERTY_ID));
        assertEquals("0x0", probeEventSource.getPropertyValue(TraceProbeEventPropertySource.TRACE_EVENT_PROBE_OFFSET_PROPERTY_ID));
        assertEquals("init_post", probeEventSource.getPropertyValue(TraceProbeEventPropertySource.TRACE_EVENT_PROBE_SYMBOL_PROPERTY_ID));

        //-------------------------------------------------------------------------
        // Verify Filter of UST event
        //-------------------------------------------------------------------------
        event = (TraceEventComponent) domains[1].getChildren()[1].getChildren()[0];
        adapter = event.getAdapter(IPropertySource.class);
        assertEquals("with filter", event.getFilterExpression());

        //-------------------------------------------------------------------------
        // Delete node
        //-------------------------------------------------------------------------
        node.disconnect();
        node.getParent().removeChild(node);
    }
}
