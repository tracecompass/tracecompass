/*******************************************************************************
 * Copyright (c) 2014, 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCpuAspect;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.tests.analysis.AnalysisManagerTest;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.tests.stubs.analysis.TestAnalysis;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfTraceStub;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

/**
 * Test suite for {@link TmfTraceUtils}
 */
public class TmfTraceUtilsTest {

    private static final TmfTestTrace TEST_TRACE = TmfTestTrace.A_TEST_10K;

    private TmfTrace fTrace;

    // ------------------------------------------------------------------------
    // Test trace class definition
    // ------------------------------------------------------------------------

    private static class TmfTraceStubWithAspects extends TmfTraceStub {

        private static final @NonNull Collection<ITmfEventAspect<?>> EVENT_ASPECTS;
        static {
            ImmutableList.Builder<ITmfEventAspect<?>> builder = ImmutableList.builder();
            builder.add(new TmfCpuAspect() {
                @Override
                public Integer resolve(ITmfEvent event) {
                    return 1;
                }
            });
            builder.addAll(TmfTrace.BASE_ASPECTS);
            EVENT_ASPECTS = builder.build();
        }

        public TmfTraceStubWithAspects(String path) throws TmfTraceException {
            super(path, ITmfTrace.DEFAULT_TRACE_CACHE_SIZE, false, null);
        }

        @Override
        public Iterable<ITmfEventAspect<?>> getEventAspects() {
            return EVENT_ASPECTS;
        }

    }

    private static class TestEventAspect implements ITmfEventAspect<Integer> {

        public static final Integer RESOLVED_VALUE = 2;
        public static final @NonNull String ASPECT_NAME = "test";

        @Override
        public @NonNull String getName() {
            return ASPECT_NAME;
        }

        @Override
        public @NonNull String getHelpText() {
            return ASPECT_NAME;
        }

        @Override
        public @Nullable Integer resolve(@NonNull ITmfEvent event) {
            return RESOLVED_VALUE;
        }

    }

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    /**
     * Test setup
     */
    @Before
    public void setUp() {
        try {
            fTrace = new TmfTraceStubWithAspects(TEST_TRACE.getFullPath());
            TmfSignalManager.deregister(fTrace);
            fTrace.indexTrace(true);
        } catch (final TmfTraceException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test cleanup
     */
    @After
    public void tearDown() {
        fTrace.dispose();
        fTrace = null;
    }

    // ------------------------------------------------------------------------
    // Test methods
    // ------------------------------------------------------------------------

    /**
     * Test the {@link TmfTraceUtils#getAnalysisModuleOfClass} method.
     */
    @Test
    public void testGetModulesByClass() {
        TmfTrace trace = fTrace;
        assertNotNull(trace);

        /* Open the trace, the modules should be populated */
        trace.traceOpened(new TmfTraceOpenedSignal(this, trace, null));

        Iterable<TestAnalysis> testModules = TmfTraceUtils.getAnalysisModulesOfClass(trace, TestAnalysis.class);
        assertTrue(testModules.iterator().hasNext());

        int count = 0;
        for (TestAnalysis module : testModules) {
            assertNotNull(module);
            count++;
        }
        /*
         * FIXME: The exact count depends on the context the test is run (full
         * test suite or this file only), but there must be at least 2 modules
         */
        assertTrue(count >= 2);

        TestAnalysis module = TmfTraceUtils.getAnalysisModuleOfClass(trace, TestAnalysis.class, AnalysisManagerTest.MODULE_PARAM);
        assertNotNull(module);
        IAnalysisModule traceModule = trace.getAnalysisModule(AnalysisManagerTest.MODULE_PARAM);
        assertNotNull(traceModule);
        assertEquals(module, traceModule);

    }

    /**
     * Test the {@link TmfTraceUtils#resolveEventAspectOfClassForEvent(ITmfTrace, Class, ITmfEvent)} method.
     */
    @Test
    public void testResolveEventAspectsOfClassForEvent() {
        TmfTrace trace = fTrace;
        assertNotNull(trace);

        ITmfContext context = trace.seekEvent(0L);
        ITmfEvent event = trace.getNext(context);
        assertNotNull(event);

        /* Make sure the CPU aspect returns the expected value */
        Object cpuObj = TmfTraceUtils.resolveEventAspectOfClassForEvent(trace, TmfCpuAspect.class, event);
        assertNotNull(cpuObj);
        assertEquals(1, cpuObj);

    }

    /**
     * Test the
     * {@link TmfTraceUtils#resolveAspectOfNameForEvent(ITmfTrace, String, ITmfEvent)}
     * method.
     */
    @Test
    public void testResolveEventAspectsOfNameForEvent() {
        TmfTrace trace = fTrace;
        assertNotNull(trace);

        ITmfContext context = trace.seekEvent(0L);
        ITmfEvent event = trace.getNext(context);
        assertNotNull(event);

        /* Make sure the CPU aspect returns the expected value */
        Object cpuObj = TmfTraceUtils.resolveAspectOfNameForEvent(trace, "cpu", event);
        assertNotNull(cpuObj);
        assertEquals(1, cpuObj);

    }

    /**
     * Test the {@link TmfTraceUtils#registerEventAspect(ITmfEventAspect)}
     * method
     */
    @Test
    public void testAdditionalAspects() {
        TmfTrace trace = fTrace;

        assertNotNull(trace);

        ITmfContext context = trace.seekEvent(0L);
        ITmfEvent event = trace.getNext(context);
        assertNotNull(event);

        // Make sure the aspect is not resolved
        Object obj = TmfTraceUtils.resolveEventAspectOfClassForEvent(trace, TestEventAspect.class, event);
        assertNull(obj);

        obj = TmfTraceUtils.resolveAspectOfNameForEvent(trace, TestEventAspect.ASPECT_NAME, event);
        assertNull(obj);

        // Register the aspect
        TmfTraceUtils.registerEventAspect(new TestEventAspect());
        // See that the aspect is resolved now
        obj = TmfTraceUtils.resolveEventAspectOfClassForEvent(trace, TestEventAspect.class, event);
        assertNotNull(obj);
        assertEquals(TestEventAspect.RESOLVED_VALUE, obj);

        // See if it is resolved by name as well
        obj = TmfTraceUtils.resolveAspectOfNameForEvent(trace, TestEventAspect.ASPECT_NAME, event);
        assertNotNull(obj);
        assertEquals(TestEventAspect.RESOLVED_VALUE, obj);
    }
}
