/*******************************************************************************
 * Copyright (c) 2013, 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.statesystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.statesystem.Messages;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.tests.TmfCoreTestPlugin;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.tests.stubs.analysis.TestStateSystemModule;
import org.eclipse.tracecompass.tmf.tests.stubs.analysis.TestStateSystemProvider;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.xml.TmfXmlTraceStub;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.xml.TmfXmlTraceStubNs;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

/**
 * Test the {@link TmfStateSystemAnalysisModule} class
 *
 * @author Geneviève Bastien
 */
public class StateSystemAnalysisModuleTest {

    /** Time-out tests after 1 minute. */
    @Rule
    public TestRule globalTimeout = new Timeout(1, TimeUnit.MINUTES);

    /** ID of the test state system analysis module */
    public static final String MODULE_SS = "org.eclipse.linuxtools.tmf.core.tests.analysis.sstest";
    private static final String XML_TRACE = "testfiles/stub_xml_traces/valid/analysis_dependency.xml";

    private TestStateSystemModule fModule;
    private ITmfTrace fTrace;

    /**
     * Setup test trace
     */
    @Before
    public void setupTraces() {
        TmfXmlTraceStub trace = TmfXmlTraceStubNs.setupTrace(TmfCoreTestPlugin.getAbsoluteFilePath(XML_TRACE));
        trace.traceOpened(new TmfTraceOpenedSignal(this, trace, null));
        fTrace = trace;

        fModule = (TestStateSystemModule) trace.getAnalysisModule(MODULE_SS);
    }

    /**
     * Some tests use traces, let's clean them here
     */
    @After
    public void cleanupTraces() {
        fTrace.dispose();
    }

    /**
     * Test the state system module execution and result
     */
    @Test
    public void testSsModule() {
        ITmfStateSystem ss = fModule.getStateSystem();
        assertNull(ss);
        fModule.schedule();
        if (fModule.waitForCompletion()) {
            ss = fModule.getStateSystem();
            assertNotNull(ss);
        } else {
            fail("Module did not complete properly");
        }
    }

    /**
     * Make sure that the state system is initialized after calling
     * {@link TmfStateSystemAnalysisModule#waitForInitialization()}.
     */
    @Test
    public void testInitialization() {
        assertNull(fModule.getStateSystem());
        fModule.schedule();

        assertTrue("Initialization succeeded", fModule.waitForInitialization());
        assertNotNull(fModule.getStateSystem());
    }

    /**
     * Test that helper returns the right properties
     */
    @Test
    public void testProperties() {

        /* The stub state system has in mem backend 2 properties */
        Map<String, String> properties = fModule.getProperties();
        assertEquals(fModule.getBackendName(), properties.get(Messages.TmfStateSystemAnalysisModule_PropertiesBackend));
        assertEquals(fModule.getId(), properties.get(org.eclipse.tracecompass.tmf.core.analysis.Messages.TmfAbstractAnalysisModule_LabelId));
    }

    private static final String CRUCIAL_EVENT = "crucialEvent";
    private static final String CRUCIAL_FIELD = "crucialInfo";

    private static void setupDependentAnalysisHandler(CyclicBarrier barrier) {
        TestStateSystemProvider.setEventHandler((ss, event) -> {
            try {
                /* Wait before processing the current event */
                barrier.await();
                if (event.getName().equals(CRUCIAL_EVENT)) {
                    String crucialInfo = (String) event.getContent().getField(CRUCIAL_FIELD).getValue();
                    int quark = ss.getQuarkAbsoluteAndAdd(CRUCIAL_FIELD);
                    try {
                        ss.modifyAttribute(event.getTimestamp().toNanos(), TmfStateValue.newValueString(crucialInfo), quark);
                    } catch (Exception e) {
                        fail(e.getMessage());
                    }
                }
                /* Wait before processing the next event */
                barrier.await();
                return true;
            } catch (InterruptedException | BrokenBarrierException e1) {
                return false;
            }

        });
    }

    /**
     * Test the {@link TmfStateSystemAnalysisModule#isQueryable(long)} method
     */
    @Test
    public void testIsQueryable() {

        CyclicBarrier barrier = new CyclicBarrier(2);
        setupDependentAnalysisHandler(barrier);

        TestStateSystemModule module = fModule;
        assertNotNull(module);

        /* Module is not started, it should be queriable */
        assertTrue(module.isQueryable(1));
        assertTrue(module.isQueryable(4));
        assertTrue(module.isQueryable(5));
        assertTrue(module.isQueryable(7));
        assertTrue(module.isQueryable(10));

        module.schedule();

        assertTrue(module.waitForInitialization());

        assertFalse(module.isQueryable(1));

        try {
            /* 2 waits for a barrier for one event */
            // event 1
            barrier.await();
            barrier.await();
            // event 2
            barrier.await();
            assertTrue(module.isQueryable(1));
            assertTrue(module.isQueryable(4));
            assertFalse(module.isQueryable(5));
            barrier.await();
            // event 3
            barrier.await();
            assertTrue(module.isQueryable(1));
            assertTrue(module.isQueryable(4));
            assertFalse(module.isQueryable(5));
            barrier.await();
            // event 4
            barrier.await();
            assertTrue(module.isQueryable(1));
            assertTrue(module.isQueryable(4));
            assertFalse(module.isQueryable(5));
            barrier.await();
            // event 5
            barrier.await();
            assertTrue(module.isQueryable(1));
            assertTrue(module.isQueryable(4));
            assertTrue(module.isQueryable(5));
            assertFalse(module.isQueryable(7));
            barrier.await();
            // event 6
            barrier.await();
            assertTrue(module.isQueryable(1));
            assertTrue(module.isQueryable(4));
            assertTrue(module.isQueryable(5));
            assertFalse(module.isQueryable(7));
            barrier.await();
            // event 7
            barrier.await();
            assertTrue(module.isQueryable(1));
            assertTrue(module.isQueryable(4));
            assertTrue(module.isQueryable(5));
            assertTrue(module.isQueryable(7));
            assertFalse(module.isQueryable(10));
            barrier.await();

            fModule.waitForCompletion();
            assertTrue(module.isQueryable(1));
            assertTrue(module.isQueryable(4));
            assertTrue(module.isQueryable(5));
            assertTrue(module.isQueryable(7));
            assertTrue(module.isQueryable(10));

            // Should return true only if later than trace time
            assertTrue(module.isQueryable(100));

        } catch (InterruptedException | BrokenBarrierException e1) {
            fail(e1.getMessage());
            fModule.cancel();
        } finally {
            TestStateSystemProvider.setEventHandler(null);
        }
    }

    /**
     * Test the {@link TmfStateSystemAnalysisModule#isQueryable(long)} method
     * when the analysis is cancelled
     */
    @Ignore("Hangs very often")
    @Test
    public void testIsQueryableCancel() {

        TestStateSystemModule module = fModule;
        assertNotNull(module);
        /* Set the queue to 1 to limit the number of events buffered */
        module.setPerEventSignalling(true);

        /* Module is not started, it should be queriable */
        assertTrue(module.isQueryable(1));
        assertTrue(module.isQueryable(4));
        assertTrue(module.isQueryable(5));
        assertTrue(module.isQueryable(7));
        assertTrue(module.isQueryable(10));

        fModule.schedule();

        assertTrue(module.waitForInitialization());

        assertFalse(module.isQueryable(1));

        // Process 2 events, then cancel
        module.signalNextEvent();
        module.signalNextEvent();
        module.cancel();
        module.setPerEventSignalling(false);

        fModule.waitForCompletion();
        assertTrue(module.isQueryable(1));
        assertTrue(module.isQueryable(4));
        assertTrue(module.isQueryable(5));
        assertTrue(module.isQueryable(7));
        assertTrue(module.isQueryable(10));
    }

    /**
     * Test that an analysis with full backend is re-read correctly
     * @throws TmfAnalysisException Propagates exceptions
     */
    @Test
    public void testReReadFullAnalysis() throws TmfAnalysisException {
        TestStateSystemModule module = new TestStateSystemModule(true);
        TestStateSystemModule module2 = new TestStateSystemModule(true);
        try {
            ITmfTrace trace = fTrace;
            assertNotNull(trace);
            module.setTrace(trace);
            module2.setTrace(trace);

            // Execute the first module
            module.schedule();
            assertTrue(module.waitForCompletion());

            // Execute the second module, it should read the state system file
            File ssFile = module2.getSsFile();
            assertNotNull(ssFile);
            assertTrue(ssFile.exists());
            module2.schedule();
            assertTrue(module2.waitForCompletion());
        } finally {
            module.dispose();
            module2.dispose();
        }
    }
}
