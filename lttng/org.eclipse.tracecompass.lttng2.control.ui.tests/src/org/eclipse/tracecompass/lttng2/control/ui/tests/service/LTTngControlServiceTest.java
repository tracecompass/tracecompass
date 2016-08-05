/**********************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Alexandre Montplaisir - Port to JUnit4
 *   Marc-Andre Laperle - Support for creating a live session
 **********************************************************************/

package org.eclipse.tracecompass.lttng2.control.ui.tests.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IBaseEventInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IChannelInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IDomainInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IEventInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IFieldInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.ISessionInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.ISnapshotInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IUstProviderInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.LogLevelType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceChannelOutputType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceDomainType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceEnablement;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceEventType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceLogLevel;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceSessionState;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.BufferType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.ChannelInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.SessionInfo;
import org.eclipse.tracecompass.internal.lttng2.control.stubs.shells.LTTngToolsFileShell;
import org.eclipse.tracecompass.internal.lttng2.control.ui.Activator;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.logging.ControlCommandLogger;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.preferences.ControlPreferences;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.service.ILttngControlService;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.service.LTTngControlService;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.service.LTTngControlServiceFactory;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.service.LttngVersion;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.FrameworkUtil;

/**
 * The class <code>LTTngControlServiceTest</code> contains test for the class
 * <code>{@link  LTTngControlService}</code>.
 */
@SuppressWarnings("javadoc")
public class LTTngControlServiceTest {

    private static final String DIRECTORY   = "testfiles";
    private static final String TEST_STREAM = "LTTngServiceTest.cfg";

    private static final String SCEN_LTTNG_NOT_INSTALLED = "LttngNotInstalled";
    protected static final String SCEN_LTTNG_VERSION = "LttngVersion";
    protected static final String SCEN_LTTNG_VERSION_WITH_PROMPT = "LttngVersionWithPrompt";
    private static final String SCEN_LTTNG_UNSUPPORTED_VERSION = "LttngUnsupportedVersion";
    private static final String SCEN_LTTNG_NO_VERSION = "LttngNoVersion";
    protected static final String SCEN_LTTNG_COMPILED_VERSION = "LttngVersionCompiled";
    private static final String SCEN_NO_SESSION_AVAILABLE = "NoSessionAvailable";
    private static final String SCEN_GET_SESSION_NAMES1 = "GetSessionNames1";
    private static final String SCEN_GET_SESSION_WITH_GROUP = "GetSessionWithTracingGroup";
    private static final String SCEN_GET_SESSION_NAME_NOT_EXIST = "GetSessionNameNotExist";
    private static final String SCEN_GET_SESSION_NAME_NOT_EXIST_VERBOSE = "GetSessionNameNotExistVerbose";
    protected static final String SCEN_GET_SESSION_GARBAGE_OUT = "GetSessionGarbageOut";
    private static final String SCEN_GET_SESSION1 = "GetSession1";
    private static final String SCEN_GET_SESSION_WITH_LIVE = "GetSessionWithLiveInterval";
    private static final String SCEN_GET_KERNEL_PROVIDER1 = "GetKernelProvider1";
    private static final String SCEN_LIST_WITH_NO_KERNEL1 = "ListWithNoKernel1";
    private static final String SCEN_LIST_WITH_NO_KERNEL2 = "ListWithNoKernel2";
    private static final String SCEN_LIST_WITH_NO_KERNEL_VERBOSE = "ListWithNoKernelVerbose";
    private static final String SCEN_GET_UST_PROVIDER1 = "GetUstProvider1";
    private static final String SCEN_GET_UST_PROVIDER2 = "GetUstProvider2";
    private static final String SCEN_GET_UST_PROVIDER3 = "GetUstProvider3";
    private static final String SCEN_LIST_WITH_NO_UST1 = "ListWithNoUst1";
    private static final String SCEN_LIST_WITH_NO_UST2 = "ListWithNoUst2";
    private static final String SCEN_LIST_WITH_NO_UST3 = "ListWithNoUst3";
    private static final String SCEN_LIST_WITH_NO_UST_VERBOSE = "ListWithNoUstVerbose";
    private static final String SCEN_CREATE_SESSION1 = "CreateSession1";
    private static final String SCEN_CREATE_SESSION_WITH_PROMPT = "CreateSessionWithPrompt";
    private static final String SCEN_CREATE_SESSION_VARIANTS = "CreateSessionVariants";
    private static final String SCEN_DESTROY_SESSION1 = "DestroySession1";
    private static final String SCEN_DESTROY_SESSION_VERBOSE = "DestroySessionVerbose";
    private static final String SCEN_CHANNEL_HANDLING = "ChannelHandling";
    private static final String SCEN_EVENT_HANDLING = "EventHandling";
    private static final String SCEN_EVENT_EXCLUDED = "EventExcluded";
    private static final String SCEN_CONTEXT_HANDLING = "ContextHandling";
    private static final String SCEN_CONTEXT_ERROR_HANDLING = "ContextErrorHandling";
    private static final String SCEN_CREATE_SESSION_2_1 = "CreateSessionLttng2.1";
    private static final String SCEN_CREATE_SESSION_VERBOSE_2_1 = "CreateSessionLttngVerbose2.1";
    private static final String SCEN_CREATE_SNAPSHOT_SESSION = "CreateSessionSnapshot";
    private static final String SCEN_CREATE_SNAPSHOT_SESSION_2_5 = "CreateSessionSnapshot2.5";
    private static final String SCEN_CREATE_STREAMED_SNAPSHOT_SESSION = "CreateSessionStreamedSnapshot";
    private static final String SCEN_CREATE_SNAPSHOT_SESSION_ERRORS = "CreateSessionSnapshotErrors";
    protected static final String SCEN_CREATE_LIVE_SESSION = "CreateSessionLive";
    private static final String SCEN_CREATE_LIVE_SESSION_ERRORS = "CreateSessionLiveErrors";
    private static final String SCEN_SESSION_PROFILES = "SessionProfile";



    // ------------------------------------------------------------------------
    // Test data
    // ------------------------------------------------------------------------
    private String fTestfile;
    protected final @NonNull LTTngToolsFileShell fShell = new LTTngToolsFileShell();
    protected ILttngControlService fService;

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
        URL location = FileLocator.find(FrameworkUtil.getBundle(this.getClass()), new Path(getTestDirectory() + File.separator + getTestStream()), null);
        File testfile = new File(FileLocator.toFileURL(location).toURI());
        fTestfile = testfile.getAbsolutePath();
        fShell.loadScenarioFile(fTestfile);
        fService = getControlService();
        if (fService == null) {
            throw new Exception("Unable to obtain a valid ControlService");
        }

        ControlPreferences.getInstance().init(Activator.getDefault().getPreferenceStore());
    }

    /**
     * @return the string of the test directory to use
     */
    protected String getTestDirectory() {
        return DIRECTORY;
    }

    /**
     * @return the LttngCon
     */
    protected ILttngControlService getControlService() {
        return new LTTngControlService(fShell);
    }

    /**
     * @return the shell implementation
     */
    public @NonNull LTTngToolsFileShell getShell() {
        return fShell;
    }

    /**
     * @return
     */
    protected String getTestStream() {
        return TEST_STREAM;
    }

    @After
    public void tearDown() {
        disableVerbose();
        ControlPreferences.getInstance().dispose();
    }

    // ------------------------------------------------------------------------
    // Test Cases
    // ------------------------------------------------------------------------

    @Test
    public void testVersion() {
        try {
            fShell.setScenario(SCEN_LTTNG_VERSION);
            ILttngControlService service = LTTngControlServiceFactory.getLttngControlService(fShell);
            assertNotNull(service);
            assertEquals("2.1.0", service.getVersionString());
        } catch (ExecutionException e) {
            fail("Exeption thrown " + e);
        }
    }

    @Test
    public void testVersionWithPrompt() {
        try {
            fShell.setScenario(SCEN_LTTNG_VERSION_WITH_PROMPT);
            ILttngControlService service = LTTngControlServiceFactory.getLttngControlService(fShell);
            assertNotNull(service);
            assertEquals("2.0.0", service.getVersionString());
        } catch (ExecutionException e) {
            fail("Exeption thrown " + e);
        }
    }

    @Test(expected=ExecutionException.class)
    public void testUnsupportedVersion() throws ExecutionException {
        fShell.setScenario(SCEN_LTTNG_UNSUPPORTED_VERSION);
        LTTngControlServiceFactory.getLttngControlService(fShell);
    }

    @Test
    public void testNoVersion() {
        try {
            fShell.setScenario(SCEN_LTTNG_NO_VERSION);
            LTTngControlServiceFactory.getLttngControlService(fShell);
            fail("No exeption thrown");
        } catch (ExecutionException e) {
            // success
        }
    }

    @Test
    public void testVersionCompiled() {
        try {
            fShell.setScenario(SCEN_LTTNG_COMPILED_VERSION);
            ILttngControlService service = LTTngControlServiceFactory.getLttngControlService(fShell);
            assertNotNull(service);
            assertEquals("2.5.0", service.getVersionString());
        } catch (ExecutionException e) {
            fail("Exeption thrown " + e);
        }
    }


    @Test
    public void testLttngNotInstalled() {
        try {
            fShell.setScenario(SCEN_LTTNG_NOT_INSTALLED);
            fService.getSessionNames(new NullProgressMonitor());
            fail("No exeption thrown");
        } catch (ExecutionException e) {
         // success
        }
    }

    @Test
    public void testGetSessionNames1() {
        try {
            fShell.setScenario(SCEN_NO_SESSION_AVAILABLE);
            List<String> result = fService.getSessionNames(new NullProgressMonitor());

            assertNotNull(result);
            assertEquals(0, result.size());

        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    @Test
    public void testGetSessionNames2() {
        try {
            fShell.setScenario(SCEN_GET_SESSION_NAMES1);
            List<String> result = fService.getSessionNames(new NullProgressMonitor());

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("mysession1", result.get(0));
            assertEquals("mysession", result.get(1));

        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    @Test
    public void testGetSessionNamesWithTracingGroup() {
        try {
            fShell.setScenario(SCEN_GET_SESSION_WITH_GROUP);
            ControlPreferences.getInstance().getPreferenceStore().setValue(
                    ControlPreferences.TRACE_CONTROL_TRACING_GROUP_PREF, "group");

            List<String> result = fService.getSessionNames(new NullProgressMonitor());

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("mysession1", result.get(0));
            assertEquals("mysession", result.get(1));

            ControlCommandLogger.init(ControlPreferences.getInstance().getLogfilePath(), false);
            ControlPreferences.getInstance().getPreferenceStore().setValue(
                    ControlPreferences.TRACE_CONTROL_TRACING_GROUP_PREF, ControlPreferences.TRACE_CONTROL_DEFAULT_TRACING_GROUP);


        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }


    @Test
    public void testGetSessionNotExist() {
        try {
            fShell.setScenario(SCEN_GET_SESSION_NAME_NOT_EXIST);
            fService.getSessionNames(new NullProgressMonitor());
            fail("No exeption thrown");

        } catch (ExecutionException e) {
            // success
        }
    }

    @Test
    public void testGetSessionNotExistVerbose() {
        try {
            enableVerbose();
            fShell.setScenario(SCEN_GET_SESSION_NAME_NOT_EXIST_VERBOSE);
            fService.getSessionNames(new NullProgressMonitor());
            fail("No exeption thrown");

        } catch (ExecutionException e) {
            // success
        } finally {
            disableVerbose();
        }
    }

    @Test
    public void testGetSessionNameGarbage() {
        try {
            fShell.setScenario(SCEN_GET_SESSION_GARBAGE_OUT);
            List<String> result = fService.getSessionNames(new NullProgressMonitor());

            assertNotNull(result);
            assertEquals(0, result.size());

        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    @Test
    public void testGetSession1() {
        try {
            fShell.setScenario(SCEN_GET_SESSION1);
            ISessionInfo session = fService.getSession("mysession", new NullProgressMonitor());

            // Verify Session
            assertNotNull(session);
            assertEquals("mysession", session.getName());
            assertEquals("/home/user/lttng-traces/mysession-20120129-084256", session.getSessionPath());
            assertEquals(TraceSessionState.ACTIVE, session.getSessionState());

            IDomainInfo[] domains = session.getDomains();
            assertNotNull(domains);
            assertEquals(2, domains.length);

            // Verify Kernel domain
            assertEquals("Kernel", domains[0].getName());
            IChannelInfo[] channels =  domains[0].getChannels();
            assertNotNull(channels);
            assertEquals(2, channels.length);

            // Verify Kernel's channel0
            assertEquals("channel0", channels[0].getName());
            assertEquals(4, channels[0].getNumberOfSubBuffers());
            assertEquals("splice()", channels[0].getOutputType().getInName());
            assertEquals(TraceChannelOutputType.SPLICE, channels[0].getOutputType());
            assertEquals(false, channels[0].isOverwriteMode());
            assertEquals(200, channels[0].getReadTimer());
            assertEquals(TraceEnablement.ENABLED, channels[0].getState());
            assertEquals(262144, channels[0].getSubBufferSize());
            assertEquals(0, channels[0].getSwitchTimer());

            // Verify event info
            IEventInfo[] channel0Events = channels[0].getEvents();
            assertNotNull(channel0Events);
            assertEquals(2, channel0Events.length);
            assertEquals("block_rq_remap", channel0Events[0].getName());
            assertEquals(TraceLogLevel.TRACE_EMERG, channel0Events[0].getLogLevel());
            assertEquals(TraceEventType.TRACEPOINT, channel0Events[0].getEventType());
            assertEquals(TraceEnablement.ENABLED, channel0Events[0].getState());

            assertEquals("block_bio_remap", channel0Events[1].getName());
            assertEquals(TraceLogLevel.TRACE_EMERG, channel0Events[1].getLogLevel());
            assertEquals(TraceEventType.TRACEPOINT, channel0Events[1].getEventType());
            assertEquals(TraceEnablement.DISABLED, channel0Events[1].getState());

            // Verify Kernel's channel1
            assertEquals("channel1", channels[1].getName());
            assertEquals(4, channels[1].getNumberOfSubBuffers());
            assertEquals("splice()", channels[1].getOutputType().getInName());
            assertEquals(TraceChannelOutputType.SPLICE, channels[1].getOutputType());
            assertEquals(true, channels[1].isOverwriteMode());
            assertEquals(400, channels[1].getReadTimer());
            assertEquals(TraceEnablement.DISABLED, channels[1].getState());
            assertEquals(524288, channels[1].getSubBufferSize());
            assertEquals(100, channels[1].getSwitchTimer());

            // Verify event info
            IEventInfo[] channel1Events = channels[1].getEvents();
            assertEquals(0, channel1Events.length);

            // Verify domain UST global
            assertEquals("UST global", domains[1].getName());

            IChannelInfo[] ustChannels =  domains[1].getChannels();

            // Verify UST global's mychannel1
            assertEquals("mychannel1", ustChannels[0].getName());
            assertEquals(8, ustChannels[0].getNumberOfSubBuffers());
            assertEquals("mmap()", ustChannels[0].getOutputType().getInName());
            assertEquals(TraceChannelOutputType.MMAP, ustChannels[0].getOutputType());
            assertEquals(true, ustChannels[0].isOverwriteMode());
            assertEquals(100, ustChannels[0].getReadTimer());
            assertEquals(TraceEnablement.DISABLED, ustChannels[0].getState());
            assertEquals(8192, ustChannels[0].getSubBufferSize());
            assertEquals(200, ustChannels[0].getSwitchTimer());

            // Verify event info
            IEventInfo[] ustEvents = ustChannels[0].getEvents();
            assertEquals(0, ustEvents.length);

            // Verify UST global's channel0
            assertEquals("channel0", ustChannels[1].getName());
            assertEquals(4, ustChannels[1].getNumberOfSubBuffers());
            assertEquals("mmap()", ustChannels[1].getOutputType().getInName());
            assertEquals(TraceChannelOutputType.MMAP, ustChannels[1].getOutputType());
            assertEquals(false, ustChannels[1].isOverwriteMode());
            assertEquals(200, ustChannels[1].getReadTimer());
            assertEquals(TraceEnablement.ENABLED, ustChannels[1].getState());
            assertEquals(4096, ustChannels[1].getSubBufferSize());
            assertEquals(0, ustChannels[1].getSwitchTimer());

            // Verify event info
            ustEvents = ustChannels[1].getEvents();
            assertEquals(2, ustEvents.length);

            assertEquals("ust_tests_hello:tptest_sighandler", ustEvents[0].getName());
            assertEquals(TraceLogLevel.TRACE_DEBUG_LINE, ustEvents[0].getLogLevel());
            assertEquals(TraceEventType.TRACEPOINT, ustEvents[0].getEventType());
            assertEquals(TraceEnablement.DISABLED, ustEvents[0].getState());

            assertEquals("*", ustEvents[1].getName());
            assertEquals(getAllEventTraceLogLevel(), ustEvents[1].getLogLevel());
            assertEquals(TraceEventType.TRACEPOINT, ustEvents[1].getEventType());
            assertEquals(TraceEnablement.ENABLED, ustEvents[1].getState());

            // next session (no detailed information available)
            session = fService.getSession("mysession1", new NullProgressMonitor());
            assertNotNull(session);
            assertEquals("mysession1", session.getName());
            assertEquals("/home/user/lttng-traces/mysession1-20120203-133225", session.getSessionPath());
            assertEquals(TraceSessionState.INACTIVE, session.getSessionState());

            domains = session.getDomains();
            assertNotNull(domains);
            assertEquals(0, domains.length);
        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    @Test
    @Ignore
    public void testGetSessionWithLive() {
        try {
            fShell.setScenario(SCEN_GET_SESSION_WITH_LIVE);

            // Verify Session (snapshot session, non-live)
            ISessionInfo session = fService.getSession("mysession", new NullProgressMonitor());
            assertNotNull(session);
            assertEquals("mysession", session.getName());
            assertEquals("/home/user/lttng-traces/mysession-20120129-084256", session.getSessionPath());
            assertEquals(TraceSessionState.INACTIVE, session.getSessionState());
            assertFalse(session.isLive());

            // Verify Session (regular session, non-live)
            session = fService.getSession("mysession1", new NullProgressMonitor());
            assertNotNull(session);
            assertEquals("mysession1", session.getName());
            assertEquals("/home/user/lttng-traces/mysession1-20120129-084256", session.getSessionPath());
            assertEquals(TraceSessionState.ACTIVE, session.getSessionState());
            assertFalse(session.isLive());

            // Verify Session (regular session, live)
            session = fService.getSession("mysession2", new NullProgressMonitor());
            assertNotNull(session);
            assertEquals("mysession2", session.getName());
            assertEquals("tcp4://172.0.0.1:5342/ [data: 5343]", session.getSessionPath());
            assertEquals("net://127.0.0.1", session.getLiveUrl());
            assertEquals(Integer.valueOf(5344), session.getLivePort());
            assertEquals(1000000, session.getLiveDelay());
            assertEquals(TraceSessionState.INACTIVE, session.getSessionState());
            assertTrue(session.isLive());

        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    /**
     * @return
     */
    protected TraceLogLevel getAllEventTraceLogLevel() {
        return TraceLogLevel.LEVEL_UNKNOWN;
    }

    public void testGetKernelProvider() {
        try {
            fShell.setScenario(SCEN_GET_KERNEL_PROVIDER1);
            List<IBaseEventInfo> events = fService.getKernelProvider(new NullProgressMonitor());

            // Verify event info
            assertNotNull(events);
            assertEquals(3, events.size());

            IBaseEventInfo baseEventInfo = events.get(0);
            assertNotNull(baseEventInfo);
            assertEquals("sched_kthread_stop", baseEventInfo.getName());
            assertEquals(TraceLogLevel.TRACE_EMERG, baseEventInfo.getLogLevel());
            assertEquals(TraceEventType.TRACEPOINT, baseEventInfo.getEventType());

            baseEventInfo = events.get(1);
            assertEquals("sched_kthread_stop_ret", baseEventInfo.getName());
            assertEquals(TraceLogLevel.TRACE_EMERG, baseEventInfo.getLogLevel());
            assertEquals(TraceEventType.TRACEPOINT, baseEventInfo.getEventType());

            baseEventInfo = events.get(2);
            assertEquals("sched_wakeup_new", baseEventInfo.getName());
            assertEquals(TraceLogLevel.TRACE_EMERG, baseEventInfo.getLogLevel());
            assertEquals(TraceEventType.TRACEPOINT, baseEventInfo.getEventType());

        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    @Test
    public void testGetKernelProviderNoKernel1() {
        try {
            fShell.setScenario(SCEN_LIST_WITH_NO_KERNEL1);
            List<IBaseEventInfo> events = fService.getKernelProvider(new NullProgressMonitor());

            // Verify event info
            assertNotNull(events);
            assertEquals(0, events.size());

        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    @Test
    public void testGetKernelProviderNoKernel2() {
        try {
            fShell.setScenario(SCEN_LIST_WITH_NO_KERNEL2);
            List<IBaseEventInfo> events = fService.getKernelProvider(new NullProgressMonitor());

            // Verify event info
            assertNotNull(events);
            assertEquals(0, events.size());

        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    @Test
    public void testGetKernelProviderNoKernelVerbose() {
        try {
            enableVerbose();
            fShell.setScenario(SCEN_LIST_WITH_NO_KERNEL_VERBOSE);
            List<IBaseEventInfo> events = fService.getKernelProvider(new NullProgressMonitor());

            // Verify event info
            assertNotNull(events);
            assertEquals(0, events.size());

        } catch (ExecutionException e) {
            fail(e.toString());
        } finally {
            disableVerbose();
        }
    }

    @Test
    public void testGetUstProvider() {
        try {
            fShell.setScenario(SCEN_GET_UST_PROVIDER1);
            List<IUstProviderInfo> providers = fService.getUstProvider();

            // Check all providers
            assertNotNull(providers);
            assertEquals(2, providers.size());

            //Verify first provider
            assertEquals("/home/user/git/lttng-ust/tests/hello.cxx/.libs/lt-hello", providers.get(0).getName());
            assertEquals(9379, providers.get(0).getPid());

            // Verify event info
            IBaseEventInfo[] events = providers.get(0).getEvents();
            assertNotNull(events);
            assertEquals(2, events.length);

            IBaseEventInfo baseEventInfo = events[0];
            assertNotNull(baseEventInfo);
            assertEquals("ust_tests_hello:tptest_sighandler", baseEventInfo.getName());
            assertEquals(TraceLogLevel.TRACE_DEBUG_MODULE, baseEventInfo.getLogLevel());
            assertEquals(TraceEventType.TRACEPOINT, baseEventInfo.getEventType());

            baseEventInfo = events[1];
            assertEquals("ust_tests_hello:tptest", baseEventInfo.getName());
            assertEquals(TraceLogLevel.TRACE_INFO, baseEventInfo.getLogLevel());
            assertEquals(TraceEventType.TRACEPOINT, baseEventInfo.getEventType());

            //Verify second provider
            assertEquals("/home/user/git/lttng-ust/tests/hello.cxx/.libs/lt-hello", providers.get(1).getName());
            assertEquals(4852, providers.get(1).getPid());

            // Verify event info
            events = providers.get(1).getEvents();
            assertNotNull(events);
            assertEquals(2, events.length);

            baseEventInfo = events[0];
            assertNotNull(baseEventInfo);
            assertEquals("ust_tests_hello:tptest_sighandler", baseEventInfo.getName());
            assertEquals(TraceLogLevel.TRACE_WARNING, baseEventInfo.getLogLevel());
            assertEquals(TraceEventType.TRACEPOINT, baseEventInfo.getEventType());

            baseEventInfo = events[1];
            assertEquals("ust_tests_hello:tptest", baseEventInfo.getName());
            assertEquals(TraceLogLevel.TRACE_DEBUG_FUNCTION, baseEventInfo.getLogLevel());
            assertEquals(TraceEventType.TRACEPOINT, baseEventInfo.getEventType());

        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    @Test
    public void testUstProvider2() {
        try {
            fShell.setScenario(SCEN_GET_UST_PROVIDER2);
            List<IUstProviderInfo> providers = fService.getUstProvider();

            assertNotNull(providers);
            assertEquals(0, providers.size());

        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    @Test
    public void testGetUstProvider3() {
        try {
            fShell.setScenario(SCEN_GET_UST_PROVIDER3);
            // Set version
            ((LTTngControlService)fService).setVersion("2.1.0");
            List<IUstProviderInfo> providers = fService.getUstProvider();

            // Check all providers
            assertNotNull(providers);
            assertEquals(2, providers.size());

            //Verify first provider
            assertEquals("/home/user/git/lttng-ust/tests/hello.cxx/.libs/lt-hello", providers.get(0).getName());
            assertEquals(9379, providers.get(0).getPid());

            // Verify event info
            IBaseEventInfo[] events = providers.get(0).getEvents();
            assertNotNull(events);
            assertEquals(2, events.length);

            IBaseEventInfo baseEventInfo = events[0];
            assertNotNull(baseEventInfo);
            IFieldInfo[] fields = baseEventInfo.getFields();
            assertNotNull(fields);
            assertEquals(0, fields.length);

            baseEventInfo = events[1];
            fields = baseEventInfo.getFields();
            assertNotNull(fields);
            assertEquals(3, fields.length);
            assertEquals("doublefield", fields[0].getName());
            assertEquals("float", fields[0].getFieldType());

            assertEquals("floatfield", fields[1].getName());
            assertEquals("float", fields[1].getFieldType());

            assertEquals("stringfield", fields[2].getName());
            assertEquals("string", fields[2].getFieldType());

            //Verify second provider
            assertEquals("/home/user/git/lttng-ust/tests/hello.cxx/.libs/lt-hello", providers.get(1).getName());
            assertEquals(4852, providers.get(1).getPid());

            // Verify event info
            events = providers.get(1).getEvents();
            assertNotNull(events);
            assertEquals(2, events.length);

            baseEventInfo = events[0];
            assertNotNull(baseEventInfo);
            fields = baseEventInfo.getFields();
            assertNotNull(fields);
            assertEquals(0, fields.length);

            baseEventInfo = events[1];
            fields = baseEventInfo.getFields();
            assertNotNull(fields);
            assertEquals(3, fields.length);

            assertEquals("doublefield", fields[0].getName());
            assertEquals("float", fields[0].getFieldType());

            assertEquals("floatfield", fields[1].getName());
            assertEquals("float", fields[1].getFieldType());

            assertEquals("stringfield", fields[2].getName());
            assertEquals("string", fields[2].getFieldType());

            // Reset version
            ((LTTngControlService)fService).setVersion("2.0.0");

        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }


    @Test
    public void testGetKernelProviderNoUst1() {
        try {
            fShell.setScenario(SCEN_LIST_WITH_NO_UST1);
            List<IUstProviderInfo> providerList = fService.getUstProvider(new NullProgressMonitor());

            // Verify Provider info
            assertNotNull(providerList);
            assertEquals(0, providerList.size());

        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }


    @Test
    public void testGetKernelProviderNoUst2() {
        try {
            // Set version
            ((LTTngControlService)fService).setVersion("2.1.0");

            fShell.setScenario(SCEN_LIST_WITH_NO_UST2);
            List<IUstProviderInfo> providerList = fService.getUstProvider(new NullProgressMonitor());

            // Verify Provider info
            assertNotNull(providerList);
            assertEquals(0, providerList.size());

            // Reset version
            ((LTTngControlService)fService).setVersion("2.0.0");

        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    @Test
    public void testGetKernelProviderNoUst3() {
        try {

            // Set version
            ((LTTngControlService)fService).setVersion("2.1.0");

            fShell.setScenario(SCEN_LIST_WITH_NO_UST3);
            List<IUstProviderInfo> providerList = fService.getUstProvider(new NullProgressMonitor());

            // Verify provider info
            assertNotNull(providerList);
            assertEquals(0, providerList.size());

            // Reset version
            ((LTTngControlService)fService).setVersion("2.0.0");

        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    @Test
    public void testGetKernelProviderNoUstVerbose() {
        try {
            enableVerbose();

            // Set version
            ((LTTngControlService)fService).setVersion("2.1.0");

            fShell.setScenario(SCEN_LIST_WITH_NO_UST_VERBOSE);
            List<IUstProviderInfo> providerList = fService.getUstProvider(new NullProgressMonitor());

            // Verify provider info
            assertNotNull(providerList);
            assertEquals(0, providerList.size());

            // Reset version
            ((LTTngControlService)fService).setVersion("2.0.0");

        } catch (ExecutionException e) {
            fail(e.toString());
        } finally {
            disableVerbose();
        }
    }



    @Test
    public void testCreateSession() {
        try {
            fShell.setScenario(SCEN_CREATE_SESSION1);

            ISessionInfo info = fService.createSession(new SessionInfo("mysession2"), new NullProgressMonitor());
            assertNotNull(info);
            assertEquals("mysession2", info.getName());
            assertNotNull(info.getSessionPath());
            assertTrue(info.getSessionPath().contains("mysession2"));
            assertEquals(TraceSessionState.INACTIVE, info.getSessionState());
        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    @Test
    public void testCreateSessionWithPrompt() {
        try {
            // First line has the shell prompt before the command output
            // This can happen in a real application if the command line is not echoed by the shell.
            fShell.setScenario(SCEN_CREATE_SESSION_WITH_PROMPT);

            // First line has no shell prompt before the output
            ISessionInfo info = fService.createSession(new SessionInfo("mysession2"), new NullProgressMonitor());
            assertNotNull(info);
            assertEquals("mysession2", info.getName());
            assertNotNull(info.getSessionPath());
            assertTrue(info.getSessionPath().contains("mysession2"));
            assertEquals(TraceSessionState.INACTIVE, info.getSessionState());
        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    @Test
    public void testCreateSessionVariants() {

        fShell.setScenario(SCEN_CREATE_SESSION_VARIANTS);

        try {
            fService.createSession(new SessionInfo("alreadyExist"), new NullProgressMonitor());
            fail("No exeption thrown");
        } catch (ExecutionException e) {
            // success
        }

        try {
            fService.createSession(new SessionInfo("wrongName"), new NullProgressMonitor());
            fail("No exeption thrown");
        } catch (ExecutionException e) {
            // success
        }

        try {
            ISessionInfo sessionInfo = new SessionInfo("withPath");
            sessionInfo.setSessionPath("/home/user/hallo");
            fService.createSession(sessionInfo, new NullProgressMonitor());
            fail("No exeption thrown");
        } catch (ExecutionException e) {
            // success
        }

        try {
            ISessionInfo info = fService.createSession(new SessionInfo("session with spaces"), new NullProgressMonitor());
            assertNotNull(info);
            assertEquals("session with spaces", info.getName());
            assertNotNull(info.getSessionPath());
            assertTrue(info.getSessionPath().contains("session with spaces"));
            assertEquals(TraceSessionState.INACTIVE, info.getSessionState());

        } catch (ExecutionException e) {
            fail(e.toString());
        }

        try {
            ISessionInfo sessionInfo = new SessionInfo("pathWithSpaces");
            sessionInfo.setSessionPath("/home/user/hallo user/here");
            ISessionInfo info = fService.createSession(sessionInfo, new NullProgressMonitor());
            assertNotNull(info);
            assertEquals("pathWithSpaces", info.getName());
            assertNotNull(info.getSessionPath());
            assertTrue(info.getSessionPath().contains("/home/user/hallo user/here"));
            assertEquals(TraceSessionState.INACTIVE, info.getSessionState());

        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    @Test
    public void testDestroySession() {
        try {
            fShell.setScenario(SCEN_DESTROY_SESSION1);
            fService.destroySession("mysession2", new NullProgressMonitor());
        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    @Test
    public void testDestroySessionVerbose() {
        try {
            enableVerbose();
            fShell.setScenario(SCEN_DESTROY_SESSION_VERBOSE);
            fService.destroySession("mysession2", new NullProgressMonitor());
        } catch (ExecutionException e) {
            fail(e.toString());
        } finally {
            disableVerbose();
        }
    }

    @Test
    public void testCreateChannel() {
        try {
            ((LTTngControlService)fService).setVersion("2.2.0");
            String sessionName = "mysession2";
            List<String> list = new ArrayList<>();
            String kernelChannel0 = "mychannel0";
            String kernelChannel1 = "mychannel1";
            list.add(kernelChannel0);
            list.add(kernelChannel1);

            fShell.setScenario(SCEN_CHANNEL_HANDLING);

            // Create/enable/configure 2 kernel channels
            ChannelInfo chanInfo = new ChannelInfo("");
            chanInfo.setOverwriteMode(true);
            chanInfo.setSubBufferSize(16384);
            chanInfo.setReadTimer(100);
            chanInfo.setSwitchTimer(200);
            chanInfo.setNumberOfSubBuffers(2);
            chanInfo.setMaxNumberTraceFiles(10);
            chanInfo.setMaxSizeTraceFiles(0);
            fService.enableChannels(sessionName, list, TraceDomainType.KERNEL, chanInfo, new NullProgressMonitor());

            // Create/enable/configure 1 UST channel
            list.clear();
            list.add("ustChannel");

            chanInfo = new ChannelInfo("");
            chanInfo.setOverwriteMode(true);
            chanInfo.setSubBufferSize(32768);
            chanInfo.setReadTimer(200);
            chanInfo.setSwitchTimer(100);
            chanInfo.setNumberOfSubBuffers(1);
            chanInfo.setMaxNumberTraceFiles(20);
            chanInfo.setMaxSizeTraceFiles(0);
            fService.enableChannels(sessionName, list, TraceDomainType.UST, chanInfo, new NullProgressMonitor());
            ((LTTngControlService)fService).setVersion("2.0.0");

        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    @Test
    public void testCreateChannelUIDBuffer() {
        try {
            ((LTTngControlService)fService).setVersion("2.2.0");
            String sessionName = "mysession2";
            List<String> list = new ArrayList<>();
            String USTChannel = "ustChannel";
            list.add(USTChannel);
            fShell.setScenario(SCEN_CHANNEL_HANDLING);

            ChannelInfo chanInfo = new ChannelInfo("");
            chanInfo.setOverwriteMode(true);
            chanInfo.setSubBufferSize(32768);
            chanInfo.setReadTimer(200);
            chanInfo.setSwitchTimer(100);
            chanInfo.setNumberOfSubBuffers(1);
            chanInfo.setMaxNumberTraceFiles(20);
            chanInfo.setMaxSizeTraceFiles(0);
            chanInfo.setBufferType(BufferType.BUFFER_PER_UID);
            fService.enableChannels(sessionName, list, TraceDomainType.UST, chanInfo, new NullProgressMonitor());
            ((LTTngControlService)fService).setVersion("2.0.0");

        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    @Test
    public void testCreateChannelPIDBuffer() {
        try {
            ((LTTngControlService)fService).setVersion("2.2.0");
            String sessionName = "mysession2";
            List<String> list = new ArrayList<>();
            String USTChannel = "ustChannel";
            list.add(USTChannel);
            fShell.setScenario(SCEN_CHANNEL_HANDLING);


            ChannelInfo chanInfo = new ChannelInfo("");
            chanInfo.setOverwriteMode(true);
            chanInfo.setSubBufferSize(-1);
            chanInfo.setReadTimer(-1);
            chanInfo.setSwitchTimer(-1);
            chanInfo.setNumberOfSubBuffers(-1);
            chanInfo.setMaxNumberTraceFiles(-1);
            chanInfo.setMaxSizeTraceFiles(-1);
            chanInfo.setBufferType(BufferType.BUFFER_PER_PID);

            fService.enableChannels(sessionName, list, TraceDomainType.UST, chanInfo, new NullProgressMonitor());
            ((LTTngControlService)fService).setVersion("2.0.0");

        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    @Test
    public void testDisableChannel() {
        try {

            String sessionName = "mysession2";
            List<String> list = new ArrayList<>();
            String kernelChannel0 = "mychannel0";
            String kernelChannel1 = "mychannel1";
            list.add(kernelChannel0);
            list.add(kernelChannel1);

            fShell.setScenario(SCEN_CHANNEL_HANDLING);
            fService.disableChannels(sessionName, list, TraceDomainType.KERNEL, new NullProgressMonitor());

            list.clear();
            list.add("ustChannel");
            fService.disableChannels(sessionName, list, TraceDomainType.UST, new NullProgressMonitor());

        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    @Test
    public void testEnableChannel() {
        try {

            String sessionName = "mysession2";
            List<String> list = new ArrayList<>();
            String kernelChannel0 = "mychannel0";
            String kernelChannel1 = "mychannel1";
            list.add(kernelChannel0);
            list.add(kernelChannel1);

            fShell.setScenario(SCEN_CHANNEL_HANDLING);
            fService.enableChannels(sessionName, list, TraceDomainType.KERNEL, null, new NullProgressMonitor());

            // Create/enable/configure 1 UST channel
            list.clear();
            list.add("ustChannel");

            fService.enableChannels(sessionName, list, TraceDomainType.UST, null, new NullProgressMonitor());

        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    @Test
    public void testEnableEvents() {
        try {
            String sessionName = "mysession2";
            String channelName = "mychannel";
            // Lists
            List<String> eventList = new ArrayList<>();
            List<String> excludeList = new ArrayList<>();
            // Events
            String eventName0 = "block_rq_remap";
            String eventName1 = "block_bio_remap";
            String eventName2 = "softirq_entry";
            String ustEventName0 = "ust_tests_hello:tptest_sighandler";
            String ustEventName1 = "ust_tests_bye:tptest_sighandler";
            String ustEventWildcard = "ust*";

            fShell.setScenario(SCEN_EVENT_HANDLING);

            // 1) session name, channel = null, 3 event names, kernel
            eventList.add(eventName0);
            eventList.add(eventName1);
            eventList.add(eventName2);
            fService.enableEvents(sessionName, null, eventList, TraceDomainType.KERNEL, null, null, new NullProgressMonitor());

            // 2) session name, channel = mychannel, event name= null, kernel
            fService.enableEvents(sessionName, channelName, null, TraceDomainType.KERNEL, null, null, new NullProgressMonitor());

            // 3) session name, channel = mychannel, 1 event name, ust, no filter
            eventList.clear();
            eventList.add(ustEventName0);
            fService.enableEvents(sessionName, channelName, eventList, TraceDomainType.UST, null, null, new NullProgressMonitor());

            // 4) session name, channel = mychannel, no event name, ust, with filter
            fService.enableEvents(sessionName, channelName, eventList, TraceDomainType.UST, "intfield==10", null, new NullProgressMonitor());

            // 5) session name, channel = mychannel, no event name, ust, no filter
            eventList.clear();
            fService.enableEvents(sessionName, channelName, eventList, TraceDomainType.UST, null, null, new NullProgressMonitor());

            // 6) session name, channel = null,
            fService.enableEvents(sessionName, null, ILttngControlService.ALL_EVENTS, TraceDomainType.KERNEL, null, null, new NullProgressMonitor());

            // 7) session name, channel = mychannel, all events, ust, exclude 1 event
            excludeList.add(ustEventName0);
            fService.enableEvents(sessionName, channelName, null, TraceDomainType.UST, null, excludeList, new NullProgressMonitor());

            // 8) session name, channel = mychannel, all events, ust, exclude 2 events
            excludeList.add(ustEventName1);
            fService.enableEvents(sessionName, channelName, null, TraceDomainType.UST, null, excludeList, new NullProgressMonitor());

            // 9) session name, channel = mychannel, enable 'ust*', ust, exclude 2 events
            eventList.add(ustEventWildcard);
            fService.enableEvents(sessionName, channelName, eventList, TraceDomainType.UST, null, excludeList, new NullProgressMonitor());

            // 10) session name, channel = mychannel, enable 'ust*', ust, with filter, exclude 2 events
            fService.enableEvents(sessionName, channelName, eventList, TraceDomainType.UST, "ust==\"Challenger\"", excludeList, new NullProgressMonitor());

        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    @Test
    public void testEnableJulLoggers() {
        // Trace Compass only supports JUL starting with LTTng 2.6 that supports MI.
        // The implementation of this test is the LTTngControlServiceMiTest.java.
    }

    @Test
    public void testEnableLog4jLoggers() {
        // Trace Compass only supports LOG4J starting with LTTng 2.6 that supports MI.
        // The implementation of this test is the LTTngControlServiceMiTest.java.
    }

    @Test
    public void testEventExcluded() {
        try {
            String sessionName = "mysession2";
            String channelName = "mychannel";
            // Lists
            List<String> eventList = new ArrayList<>();
            List<String> excludeList = new ArrayList<>();
            // Events
            String ustEventName0 = "ust_tests_hello:tptest_sighandler";
            String ustEventName1 = "ust_tests_bye:tptest_sighandler";
            String ustEventWildcard = "ust*";

            @NonNull
            LttngVersion version = fService.getVersion();
            fShell.setScenario(SCEN_EVENT_EXCLUDED);

            // 1) 1 event excluded
            eventList.add(ustEventWildcard);
            excludeList.add(ustEventName0);
            fService.enableEvents(sessionName, channelName, eventList, TraceDomainType.UST, null, excludeList, new NullProgressMonitor());

            @Nullable
            ISessionInfo session = fService.getSession(sessionName, new NullProgressMonitor());
            assertNotNull(session);
            IEventInfo eventInfo = session.getDomains()[0].getChannels()[0].getEvents()[0];
            assertNotNull(eventInfo);
            // version 2.8 and above supports the excluded events list
            if (version.getMajor() > 1 && version.getMinor() > 7) {
                assertTrue(eventInfo.getExcludedEvents().contains(ustEventName0));
            } else {
                assertEquals("has exclusions", eventInfo.getExcludedEvents());
            }

            // 2) 2 events excluded
            excludeList.add(ustEventName1);
            fService.enableEvents(sessionName, channelName, eventList, TraceDomainType.UST, null, excludeList, new NullProgressMonitor());

            session = fService.getSession(sessionName, new NullProgressMonitor());
            assertNotNull(session);
            eventInfo = session.getDomains()[0].getChannels()[0].getEvents()[0];
            assertNotNull(eventInfo);

            // version 2.8 and above supports the excluded events list
            if (version.getMajor() > 1 && version.getMinor() > 7) {
                assertTrue(eventInfo.getExcludedEvents().contains(ustEventName0));
                assertTrue(eventInfo.getExcludedEvents().contains(ustEventName1));
            } else {
                assertEquals("has exclusions", eventInfo.getExcludedEvents());
            }

            // 3) 2 events excluded and using a filter
            fService.enableEvents(sessionName, channelName, eventList, TraceDomainType.UST, "ust==\"Challenger\"", excludeList, new NullProgressMonitor());

            session = fService.getSession(sessionName, new NullProgressMonitor());
            assertNotNull(session);
            eventInfo = session.getDomains()[0].getChannels()[0].getEvents()[0];
            assertNotNull(eventInfo);

            // version 2.8 and above supports the excluded events list
            if (version.getMajor() > 1 && version.getMinor() > 7) {
                assertTrue(eventInfo.getExcludedEvents().contains(ustEventName0));
                assertTrue(eventInfo.getExcludedEvents().contains(ustEventName1));
            } else {
                assertEquals("has exclusions", eventInfo.getExcludedEvents());
            }

            if (version.getMajor() > 1 && version.getMinor() > 7) {
                assertTrue(eventInfo.getFilterExpression().equals("ust==Challenger"));
            } else {
                assertEquals("with filter", eventInfo.getFilterExpression());
            }

        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    @Test
    public void testEnableSyscalls() {
        try {
            // 1) session name, channel = null, 3 event names, kernel
            String sessionName = "mysession2";
            String channelName = "mychannel";
            List<String> syscalls = new ArrayList<>();

            fShell.setScenario(SCEN_EVENT_HANDLING);

            // 1) session name, channel = null, syscall = all
            fService.enableSyscalls(sessionName, null, null, new NullProgressMonitor());

            // 2) session name, channel = mychannel, syscall = all
            fService.enableSyscalls(sessionName, channelName, null, new NullProgressMonitor());

            // 3) session name, channel = mychannel, syscall = close
            syscalls.add("close");
            fService.enableSyscalls(sessionName, channelName, syscalls, new NullProgressMonitor());

            // 4) session name, channel = mychannel, syscall = close,read,write
            syscalls.add("read");
            syscalls.add("write");
            fService.enableSyscalls(sessionName, channelName, syscalls, new NullProgressMonitor());

        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    @Test
    public void testDynamicProbe() {
        try {
            // 1) session name, channel = null, 3 event names, kernel
            String sessionName = "mysession2";
            String channelName = "mychannel";
            String eventName0 = "myevent0";
            String eventName1 = "myevent1";
            String functionProbe = "0xc0101340";
            String dynProbe = "init_post";

            fShell.setScenario(SCEN_EVENT_HANDLING);

            // 1) session name, channel = null, event name, function probe, probe
            fService.enableProbe(sessionName, null, eventName0, true, functionProbe, new NullProgressMonitor());

            // 2) session name, channel = mychannel
            fService.enableProbe(sessionName, channelName, eventName1, false, dynProbe, new NullProgressMonitor());

        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    @Test
    public void testEnableLogLevel() {
        try {
            // 1) session name, channel = null, 3 event names, kernel
            String sessionName = "mysession2";
            String channelName = "mychannel";
            String eventName4 = "myevent4";
            String eventName5 = "myevent5";

            fShell.setScenario(SCEN_EVENT_HANDLING);

            // 1) session name, channel = null, event name, loglevel-only, TRACE_DEBUG
            fService.enableLogLevel(sessionName, null, Arrays.asList(eventName4), LogLevelType.LOGLEVEL_ONLY, TraceLogLevel.TRACE_DEBUG, null, TraceDomainType.UST, new NullProgressMonitor());

            // 2) session name, channel = mychannel, null, loglevel, TRACE_DEBUG_FUNCTION
            fService.enableLogLevel(sessionName, channelName, Arrays.asList(eventName5), LogLevelType.LOGLEVEL, TraceLogLevel.TRACE_DEBUG_FUNCTION, null, TraceDomainType.UST, new NullProgressMonitor());

            // TODO add test with filters

        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    @Test
    public void testAddContext() {
        try {
            // 1) session name, channel = null, 3 event names, kernel
            String sessionName = "mysession2";
            String channelName = "mychannel";
            String eventName = "ust_tests_hello:tptest_sighandler";
            List<String> contexts = new ArrayList<>();
            contexts.add("prio");
            contexts.add("pid");

            fShell.setScenario(SCEN_CONTEXT_HANDLING);

            List<String> availContexts = fService.getContextList(new NullProgressMonitor());
            assertNotNull(availContexts);
            assertEquals(12, availContexts.size());

            // A very "hard-coded" way to verify but it works ...
            Set<String> expectedContexts = new HashSet<>();
            expectedContexts.add("pid");
            expectedContexts.add("procname");
            expectedContexts.add("prio");
            expectedContexts.add("nice");
            expectedContexts.add("vpid");
            expectedContexts.add("tid");
            expectedContexts.add("pthread_id");
            expectedContexts.add("vtid");
            expectedContexts.add("ppid");
            expectedContexts.add("vppid");
            expectedContexts.add("perf:cpu-cycles");
            expectedContexts.add("perf:cycles");

            assertTrue(expectedContexts.containsAll(availContexts));

            // 1) session name, channel = null, event name, loglevel-only, TRACE_DEBUG
            fService.addContexts(sessionName, channelName, eventName, TraceDomainType.UST, contexts, new NullProgressMonitor());

        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    @Test
    public void testAddContextFailure() {

        // 1) session name, channel = null, 3 event names, kernel
        String sessionName = "mysession2";
        String channelName = "mychannel";
        String eventName = "ust_tests_hello:tptest_sighandler";
        List<String> contexts = new ArrayList<>();
        contexts.add("prio");
        contexts.add("pid");
        fShell.setScenario(SCEN_CONTEXT_ERROR_HANDLING);
        try {
            fService.getContextList(new NullProgressMonitor());
            fail("No exeption generated");
        } catch (ExecutionException e) {
            // success
        }
        try {
            // 1) session name, channel = null, event name, loglevel-only, TRACE_DEBUG
            fService.addContexts(sessionName, channelName, eventName, TraceDomainType.UST, contexts, new NullProgressMonitor());
            fail("No exeption generated");
        } catch (ExecutionException e) {
            // success
        }
    }

    @Test
    public void testCreateSession2_1() {

        try {
            fShell.setScenario(SCEN_CREATE_SESSION_2_1);

            ISessionInfo sessionInfo = new SessionInfo("mysession");
            sessionInfo.setNetworkUrl("net://172.0.0.1");
            sessionInfo.setStreamedTrace(true);
            ISessionInfo info = fService.createSession(sessionInfo, new NullProgressMonitor());
            assertNotNull(info);
            assertEquals("mysession", info.getName());
            assertEquals("net://172.0.0.1", info.getSessionPath());
            assertTrue(info.isStreamedTrace());
            fService.destroySession("mysession", new NullProgressMonitor());

            sessionInfo = new SessionInfo("mysession");
            sessionInfo.setStreamedTrace(true);
            sessionInfo.setNetworkUrl("file:///tmp");
            info = fService.createSession(sessionInfo, new NullProgressMonitor());
            assertNotNull(info);
            assertEquals("mysession", info.getName());
            assertEquals("file:///tmp", info.getSessionPath());
            assertTrue(!info.isStreamedTrace());
            fService.destroySession("mysession", new NullProgressMonitor());

            sessionInfo = new SessionInfo("mysession");
            sessionInfo.setStreamedTrace(true);
            sessionInfo.setNetworkUrl("file:///tmp");
            info = fService.createSession(sessionInfo, new NullProgressMonitor());
            assertNotNull(info);
            assertEquals("mysession", info.getName());
            assertEquals("file:///tmp", info.getSessionPath());
            assertTrue(!info.isStreamedTrace());
            fService.destroySession("mysession", new NullProgressMonitor());

            sessionInfo = new SessionInfo("mysession");
            sessionInfo.setStreamedTrace(true);
            sessionInfo.setControlUrl("tcp://172.0.0.1");
            sessionInfo.setDataUrl("tcp://172.0.0.1:5343");
            info = fService.createSession(sessionInfo, new NullProgressMonitor());
            assertNotNull(info);
            assertEquals("mysession", info.getName());
            assertEquals("", info.getSessionPath()); // the complete network path is not available at this point
            assertTrue(info.isStreamedTrace());
            fService.destroySession("mysession", new NullProgressMonitor());

            sessionInfo = new SessionInfo("mysession");
            sessionInfo.setStreamedTrace(true);
            sessionInfo.setNetworkUrl("net://172.0.0.1:1234:2345");
            info = fService.createSession(sessionInfo, new NullProgressMonitor());
            assertNotNull(info);
            assertEquals("mysession", info.getName());
            assertEquals("net://172.0.0.1:1234:2345", info.getSessionPath());
            assertTrue(info.isStreamedTrace());
            fService.destroySession("mysession", new NullProgressMonitor());

            // verbose
            enableVerbose();
            sessionInfo = new SessionInfo("mysession");
            sessionInfo.setStreamedTrace(true);
            sessionInfo.setNetworkUrl("net://172.0.0.1");
            info = fService.createSession(sessionInfo, new NullProgressMonitor());
            assertNotNull(info);
            assertEquals("mysession", info.getName());
            assertEquals("net://172.0.0.1", info.getSessionPath());
            assertTrue(info.isStreamedTrace());
            disableVerbose();
            fService.destroySession("mysession", new NullProgressMonitor());


        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    @Test
    public void testCreateSessionVerbose2_1() {
        try {
            fShell.setScenario(SCEN_CREATE_SESSION_VERBOSE_2_1);

            enableVerbose();
            ISessionInfo sessionInfo = new SessionInfo("mysession");
            sessionInfo.setStreamedTrace(true);
            sessionInfo.setNetworkUrl("net://172.0.0.1");
            ISessionInfo info = fService.createSession(sessionInfo, new NullProgressMonitor());
            assertNotNull(info);
            assertEquals("mysession", info.getName());
            assertEquals("net://172.0.0.1", info.getSessionPath());
            assertTrue(info.isStreamedTrace());
            fService.destroySession("mysession", new NullProgressMonitor());
        } catch (ExecutionException e) {
            fail(e.toString());
        } finally {
            disableVerbose();
        }
    }

    @Test
    public void testCreateSnapshotSession() {
        try {
            fShell.setScenario(SCEN_CREATE_SNAPSHOT_SESSION);
            ISessionInfo params = new SessionInfo("mysession");
            params.setSnapshot(true);
            ISessionInfo sessionInfo = fService.createSession(params, new NullProgressMonitor());
            assertNotNull(sessionInfo);
            assertEquals("mysession", sessionInfo.getName());
            assertTrue(sessionInfo.isSnapshotSession());
            assertEquals("", sessionInfo.getSessionPath());
            assertTrue(!sessionInfo.isStreamedTrace());

            assertEquals(TraceSessionState.INACTIVE, sessionInfo.getSessionState());

            List<String> names = fService.getSessionNames(new NullProgressMonitor());
            assertEquals(names.get(0), "mysession");

            ISnapshotInfo snapshotInfo = fService.getSnapshotInfo("mysession", new NullProgressMonitor());
            assertNotNull(snapshotInfo);
            assertEquals("snapshot-1", snapshotInfo.getName());
            assertEquals("/home/user/lttng-traces/mysession-20130913-141651", snapshotInfo.getSnapshotPath());
            assertEquals(1, snapshotInfo.getId());
            assertTrue(!snapshotInfo.isStreamedSnapshot());

            // we need to set the snapshotInfo to so that the session path is set correctly
            sessionInfo.setSnapshotInfo(snapshotInfo);
            assertEquals("/home/user/lttng-traces/mysession-20130913-141651", sessionInfo.getSessionPath());

            fService.recordSnapshot("mysession", new NullProgressMonitor());

            fService.destroySession("mysession", new NullProgressMonitor());

        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    @Test
    public void testCreateSnapshotSession2_5() {
        try {
            fShell.setScenario(SCEN_CREATE_SNAPSHOT_SESSION_2_5);
            ISessionInfo params = new SessionInfo("mysession");
            params.setSnapshot(true);
            ISessionInfo sessionInfo = fService.createSession(params, new NullProgressMonitor());
            assertNotNull(sessionInfo);
            assertEquals("mysession", sessionInfo.getName());
            assertTrue(sessionInfo.isSnapshotSession());
            assertEquals("", sessionInfo.getSessionPath());
            assertTrue(!sessionInfo.isStreamedTrace());

            assertEquals(TraceSessionState.INACTIVE, sessionInfo.getSessionState());

            List<String> names = fService.getSessionNames(new NullProgressMonitor());
            assertEquals(names.get(0), "mysession");

            ISnapshotInfo snapshotInfo = fService.getSnapshotInfo("mysession", new NullProgressMonitor());
            assertNotNull(snapshotInfo);
            assertEquals("snapshot-1", snapshotInfo.getName());
            assertEquals("/home/user/lttng-traces/mysession-20130913-141651", snapshotInfo.getSnapshotPath());
            assertEquals(1, snapshotInfo.getId());
            assertTrue(!snapshotInfo.isStreamedSnapshot());

            // we need to set the snapshotInfo to so that the session path is set correctly
            sessionInfo.setSnapshotInfo(snapshotInfo);
            assertEquals("/home/user/lttng-traces/mysession-20130913-141651", sessionInfo.getSessionPath());

            fService.recordSnapshot("mysession", new NullProgressMonitor());

            fService.destroySession("mysession", new NullProgressMonitor());

        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    public void testCreateStreamedSnapshotSession() {
        try {
            fShell.setScenario(SCEN_CREATE_STREAMED_SNAPSHOT_SESSION);

            ISessionInfo params = new SessionInfo("mysession");
            params.setNetworkUrl("net://172.0.0.1");
            ISessionInfo sessionInfo = fService.createSession(params, new NullProgressMonitor());
            assertNotNull(sessionInfo);
            assertEquals("mysession", sessionInfo.getName());
            assertTrue(sessionInfo.isSnapshotSession());

            assertEquals(TraceSessionState.INACTIVE, sessionInfo.getSessionState());
            assertTrue(sessionInfo.isStreamedTrace());

            List<String> names = fService.getSessionNames(new NullProgressMonitor());
            assertEquals(names.get(0), "mysession");

            ISnapshotInfo snapshotInfo = sessionInfo.getSnapshotInfo();
            assertNotNull(sessionInfo);
            assertEquals("snapshot-2", snapshotInfo.getName());
            assertEquals("net4://172.0.0.1:5342/", snapshotInfo.getSnapshotPath());
            assertEquals(2, snapshotInfo.getId());
            assertTrue(snapshotInfo.isStreamedSnapshot());

            // we need to set the snapshotInfo to so that the session path is set correctly
            sessionInfo.setSnapshotInfo(snapshotInfo);
            assertEquals("net4://172.0.0.1:5342/", sessionInfo.getSessionPath());

            fService.recordSnapshot("mysession", new NullProgressMonitor());

            fService.destroySession("mysession", new NullProgressMonitor());

        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    @Test
    public void testCreateSnapshotSessionErrors() {
        try {
            fShell.setScenario(SCEN_CREATE_SNAPSHOT_SESSION_ERRORS);

            List<String> names = fService.getSessionNames(new NullProgressMonitor());
            assertEquals(names.get(0), "mysession");
        } catch (ExecutionException e) {
            fail(e.toString());
        }

        try {
            fService.getSnapshotInfo("blabla", new NullProgressMonitor());
            fail("getSnapshoInfo() didn't fail");
        } catch (ExecutionException e) {
            // successful
        }

        try {
            fService.recordSnapshot("blabla", new NullProgressMonitor());
            fail("getSnapshoInfo() didn't fail");
        } catch (ExecutionException e) {
            // successful
        }

        try {
            fService.recordSnapshot("mysession", new NullProgressMonitor());
            fail("getSnapshoInfo() didn't fail");
        } catch (ExecutionException e) {
            // successful
        }
    }

    @Test
    @Ignore
    public void testCreateLiveSession() throws ExecutionException {
        fShell.setScenario(SCEN_CREATE_LIVE_SESSION);

        ISessionInfo params = new SessionInfo("mysession");
        params.setLive(true);
        params.setStreamedTrace(true);
        params.setNetworkUrl("net://127.0.0.1");
        ISessionInfo sessionInfo = fService.createSession(params, new NullProgressMonitor());
        assertNotNull(sessionInfo);
        assertEquals("mysession", sessionInfo.getName());
        assertEquals(TraceSessionState.INACTIVE, sessionInfo.getSessionState());
        assertTrue(sessionInfo.isStreamedTrace());
        assertTrue(sessionInfo.isLive());
        assertEquals("net://127.0.0.1", sessionInfo.getSessionPath());
        List<String> names = fService.getSessionNames(new NullProgressMonitor());
        assertEquals(names.get(0), "mysession");
        fService.destroySession("mysession", new NullProgressMonitor());
    }

    @Test
    @Ignore
    public void testCreateLiveSessionErrors() {
        try {
            fShell.setScenario(SCEN_CREATE_LIVE_SESSION_ERRORS);

            ISessionInfo parameters = new SessionInfo("mysession");
            parameters.setLive(true);
            parameters.setSnapshot(true);
            fService.createSession(parameters, new NullProgressMonitor());
            fail("createSession() didn't fail");
        } catch (ExecutionException e) {
            // successful
        }

        try {
            ISessionInfo parameters = new SessionInfo("mysession");
            parameters.setNetworkUrl("blah");
            parameters.setLive(true);
            fService.createSession(parameters, new NullProgressMonitor());
            fail("createSession() didn't fail");
        } catch (ExecutionException e) {
            // successful
        }

        try {
            ISessionInfo parameters = new SessionInfo("mysession");
            parameters.setControlUrl("net://127.0.0.1");
            parameters.setLive(true);
            fService.createSession(parameters, new NullProgressMonitor());
            fail("createSession() didn't fail");
        } catch (ExecutionException e) {
            // successful
        }
    }

    @Test
    public void testSessionProfile() {
        fShell.setScenario(SCEN_SESSION_PROFILES);
        final String profile = "/home/user/.lttng/sessions/mysession.lttng";
        try {
            fService.loadSession(profile, false, new NullProgressMonitor());
        } catch (ExecutionException e) {
            fail("load session failed: " + e);
        }

        try {
            fService.loadSession(profile, true, new NullProgressMonitor());
        } catch (ExecutionException e) {
            fail("load session failed: " + e);
        }

        try {
            fService.saveSession(null, null, false, new NullProgressMonitor());
        } catch (ExecutionException e) {
            fail("save session failed: " + e);
        }

        final String sessionName = "mysession";
        try {
            fService.saveSession(sessionName, null, false, new NullProgressMonitor());
        } catch (ExecutionException e) {
            fail("save session failed: " + e);
        }

        try {
            fService.saveSession(sessionName, null, true, new NullProgressMonitor());
        } catch (ExecutionException e) {
            fail("save session failed: " + e);
        }

        try {
            fService.saveSession(sessionName, "/tmp/test", true, new NullProgressMonitor());
        } catch (ExecutionException e) {
            fail("save session failed: " + e);
        }
    }

    private static void enableVerbose() {
        // verbose
        ControlCommandLogger.init(ControlPreferences.getInstance().getLogfilePath(), false);
        ControlPreferences.getInstance().getPreferenceStore().setDefault(ControlPreferences.TRACE_CONTROL_LOG_COMMANDS_PREF, true);
        ControlPreferences.getInstance().getPreferenceStore().setDefault(ControlPreferences.TRACE_CONTROL_VERBOSE_LEVEL_PREF, ControlPreferences.TRACE_CONTROL_VERBOSE_LEVEL_V_V_VERBOSE);
    }

    private static void disableVerbose() {
        ControlPreferences.getInstance().getPreferenceStore().setDefault(ControlPreferences.TRACE_CONTROL_LOG_COMMANDS_PREF, false);
    }


}
