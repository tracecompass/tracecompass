/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *   Mathieu Rail - Added tests for getting a module's requirements
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModuleHelper;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAnalysisManager;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAnalysisModuleHelperConfigElement;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAnalysisRequirement;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.eclipse.tracecompass.tmf.tests.stubs.analysis.TestAnalysis;
import org.eclipse.tracecompass.tmf.tests.stubs.analysis.TestAnalysis2;
import org.eclipse.tracecompass.tmf.tests.stubs.analysis.TestRequirementAnalysis;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfTraceStub;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfTraceStub2;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfTraceStub3;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

/**
 * Test suite for the {@link TmfAnalysisModuleHelperConfigElement} class
 *
 * @author Geneviève Bastien
 */
public class AnalysisModuleHelperTest {

    private IAnalysisModuleHelper fModule;
    private IAnalysisModuleHelper fModuleOther;
    private IAnalysisModuleHelper fReqModule;
    private ITmfTrace fTrace;

    private static IAnalysisModuleHelper getModuleHelper(@NonNull String moduleId) {
        Multimap<String, IAnalysisModuleHelper> helpers = TmfAnalysisManager.getAnalysisModules();
        assertEquals(1, helpers.get(moduleId).size());
        return helpers.get(moduleId).iterator().next();
    }

    /**
     * Gets the module helpers for 2 test modules
     */
    @Before
    public void getModules() {
        fModule = getModuleHelper(AnalysisManagerTest.MODULE_PARAM);
        assertNotNull(fModule);
        assertTrue(fModule instanceof TmfAnalysisModuleHelperConfigElement);
        fModuleOther = getModuleHelper(AnalysisManagerTest.MODULE_SECOND);
        assertNotNull(fModuleOther);
        assertTrue(fModuleOther instanceof TmfAnalysisModuleHelperConfigElement);
        fReqModule = getModuleHelper(AnalysisManagerTest.MODULE_REQ);
        assertNotNull(fReqModule);
        assertTrue(fReqModule instanceof TmfAnalysisModuleHelperConfigElement);
        fTrace = TmfTestTrace.A_TEST_10K2.getTraceAsStub2();
    }

    /**
     * Some tests use traces, let's clean them here
     */
    @After
    public void cleanupTraces() {
        TmfTestTrace.A_TEST_10K.dispose();
        fTrace.dispose();
    }

    /**
     * Test the helper's getters and setters
     */
    @Test
    public void testHelperGetters() {
        /* With first module */
        assertEquals(AnalysisManagerTest.MODULE_PARAM, fModule.getId());
        assertEquals("Test analysis", fModule.getName());
        assertFalse(fModule.isAutomatic());

        Bundle helperbundle = fModule.getBundle();
        Bundle thisbundle = Platform.getBundle("org.eclipse.tracecompass.tmf.core.tests");
        assertNotNull(helperbundle);
        assertEquals(thisbundle, helperbundle);

        /* With other module */
        assertEquals(AnalysisManagerTest.MODULE_SECOND, fModuleOther.getId());
        assertEquals("Test other analysis", fModuleOther.getName());
        assertTrue(fModuleOther.isAutomatic());
    }

    /**
     * Test the
     * {@link TmfAnalysisModuleHelperConfigElement#appliesToTraceType(Class)}
     * method for the 2 modules
     */
    @Test
    public void testAppliesToTrace() {
        /* stub module */
        assertFalse(fModule.appliesToTraceType(TmfTrace.class));
        assertTrue(fModule.appliesToTraceType(TmfTraceStub.class));
        assertTrue(fModule.appliesToTraceType(TmfTraceStub2.class));
        assertFalse(fModule.appliesToTraceType(TmfTraceStub3.class));
        assertFalse(fModule.appliesToTraceType(TmfExperiment.class));

        /* stub module 2 */
        assertFalse(fModuleOther.appliesToTraceType(TmfTrace.class));
        assertFalse(fModuleOther.appliesToTraceType(TmfTraceStub.class));
        assertTrue(fModuleOther.appliesToTraceType(TmfTraceStub2.class));
        assertTrue(fModuleOther.appliesToTraceType(TmfTraceStub3.class));
        assertFalse(fModuleOther.appliesToTraceType(TmfExperiment.class));
    }

    /**
     * Test the
     * {@link TmfAnalysisModuleHelperConfigElement#newModule(ITmfTrace)} method
     * for the 2 modules
     */
    @Test
    public void testNewModule() {
        /* Test analysis module with traceStub */
        IAnalysisModule module = null;
        try {
            module = fModule.newModule(TmfTestTrace.A_TEST_10K.getTrace());
            assertNotNull(module);
            assertTrue(module instanceof TestAnalysis);
        } catch (TmfAnalysisException e) {
            fail();
        } finally {
            if (module != null) {
                module.dispose();
            }
        }

        /* TestAnalysis2 module with trace, should return an exception */
        try {
            module = fModuleOther.newModule(TmfTestTrace.A_TEST_10K.getTrace());
            assertNull(module);
        } catch (TmfAnalysisException e) {
            fail();
        } finally {
            if (module != null) {
                module.dispose();
            }
        }

        /* TestAnalysis2 module with a TraceStub2 */
        ITmfTrace trace = fTrace;
        assertNotNull(trace);
        try {
            module = fModuleOther.newModule(trace);
            assertNotNull(module);
            assertTrue(module instanceof TestAnalysis2);
        } catch (TmfAnalysisException e) {
            fail();
        } finally {
            if (module != null) {
                module.dispose();
            }
        }
    }


    /**
     * Test the analysis modules with a differing result for experiments
     */
    @Test
    public void testAppliesToExperiment() {
        ITmfTrace trace1 = TmfTestTrace.A_TEST_10K.getTrace();
        ITmfTrace trace2 = TmfTestTrace.A_TEST_10K2.getTrace();
        ITmfTrace trace3 = TmfTestTrace.A_TEST_10K2.getTraceAsStub2();

        /* Create an experiment with TmfTraceStub */
        ITmfTrace[] tracesExp1 = { trace1, trace2 };
        TmfExperiment exp1 = new TmfExperiment(tracesExp1[0].getEventType(), "Experiment 1", tracesExp1, TmfExperiment.DEFAULT_INDEX_PAGE_SIZE, null);

        /* Create an experiment containing some TmfTraceStub2 */
        ITmfTrace[] tracesExp2 = { trace1, trace3 };
        TmfExperiment exp2 = new TmfExperiment(tracesExp2[0].getEventType(), "Experiment 1", tracesExp2, TmfExperiment.DEFAULT_INDEX_PAGE_SIZE, null);

        try {

            /* fModule should return null for both experiments */
            IAnalysisModule module = null;
            try {
                module = fModule.newModule(exp1);
                assertNull(module);
            } catch (TmfAnalysisException e) {
                fail();
            } finally {
                if (module != null) {
                    module.dispose();
                }
            }

            try {
                module = fModule.newModule(exp2);
                assertNull(module);
            } catch (TmfAnalysisException e) {
                fail();
            } finally {
                if (module != null) {
                    module.dispose();
                }
            }

            /* fModuleOther should throw exception for exp1, but not exp2 */
            try {
                module = fModuleOther.newModule(exp1);
                assertNull(module);
            } catch (TmfAnalysisException e) {
                fail();
            } finally {
                if (module != null) {
                    module.dispose();
                }
            }

            try {
                module = fModuleOther.newModule(exp2);
                assertNotNull(module);
            } catch (TmfAnalysisException e) {
                fail();
            } finally {
                if (module != null) {
                    module.dispose();
                }
            }

        } finally {
            exp2.dispose();
            exp1.dispose();
        }
    }

    /**
     * Test for the initialization of parameters from the extension points
     */
    @Test
    public void testParameters() {
        ITmfTrace trace = TmfTestTrace.A_TEST_10K.getTrace();

        /*
         * This analysis has a parameter, but no default value. we should be
         * able to set the parameter
         */
        IAnalysisModuleHelper helper = getModuleHelper(AnalysisManagerTest.MODULE_PARAM);
        assertNotNull(helper);
        IAnalysisModule module = null;
        try {
            module = helper.newModule(trace);
            assertNotNull(module);
            assertNull(module.getParameter(TestAnalysis.PARAM_TEST));
            module.setParameter(TestAnalysis.PARAM_TEST, 1);
            assertEquals(1, module.getParameter(TestAnalysis.PARAM_TEST));

        } catch (TmfAnalysisException e1) {
            fail(e1.getMessage());
            return;
        } finally {
            if (module != null) {
                module.dispose();
            }
        }

        /* This module has a parameter with default value */
        helper = getModuleHelper(AnalysisManagerTest.MODULE_PARAM_DEFAULT);
        assertNotNull(helper);
        try {
            module = helper.newModule(trace);
            assertNotNull(module);
            assertEquals(3, module.getParameter(TestAnalysis.PARAM_TEST));
            module.setParameter(TestAnalysis.PARAM_TEST, 1);
            assertEquals(1, module.getParameter(TestAnalysis.PARAM_TEST));

        } catch (TmfAnalysisException e1) {
            fail(e1.getMessage());
            return;
        } finally {
            if (module != null) {
                module.dispose();
            }
        }

        /*
         * This module does not have a parameter so setting it should throw an
         * error
         */
        helper = getModuleHelper(AnalysisManagerTest.MODULE_SECOND);
        assertNotNull(helper);
        Exception exception = null;
        trace = fTrace;
        assertNotNull(trace);
        try {
            module = helper.newModule(trace);
            assertNotNull(module);
            assertNull(module.getParameter(TestAnalysis.PARAM_TEST));

            try {
                module.setParameter(TestAnalysis.PARAM_TEST, 1);
            } catch (RuntimeException e) {
                exception = e;
            }
        } catch (TmfAnalysisException e1) {
            fail(e1.getMessage());
            return;
        } finally {
            if (module != null) {
                module.dispose();
            }
        }
        assertNotNull(exception);
    }

    /**
     * Test for the
     * {@link TmfAnalysisModuleHelperConfigElement#getValidTraceTypes} method
     */
    @Test
    public void testGetValidTraceTypes() {
        Set<Class<? extends ITmfTrace>> expected = ImmutableSet.of((Class<? extends ITmfTrace>) TmfTraceStub.class, TmfTraceStub2.class, TmfTraceStub3.class);
        Iterable<Class<? extends ITmfTrace>> traceTypes = fReqModule.getValidTraceTypes();
        assertEquals(expected, traceTypes);
    }

    /**
     * Test for the
     * {@link TmfAnalysisModuleHelperConfigElement#getAnalysisRequirements}
     * method
     */
    @Test
    public void testGetRequirements() {
        Iterable<TmfAnalysisRequirement> requirements = fReqModule.getAnalysisRequirements();
        assertNotNull(requirements);

        Map<String, TmfAnalysisRequirement> rMap = new HashMap<>();

        for (TmfAnalysisRequirement req : requirements) {
            assertFalse(rMap.containsKey(req.getType()));
            rMap.put(req.getType(), req);
        }
        assertEquals(2, rMap.size());

        /* Test if all types and values have been obtained */
        TmfAnalysisRequirement req = rMap.get(TestRequirementAnalysis.EVENT_TYPE);
        assertNotNull(req);

        Set<String> values = req.getValues();
        assertEquals(3, values.size());
        assertTrue(values.contains(TestRequirementAnalysis.EXIT_SYSCALL));
        assertTrue(values.contains(TestRequirementAnalysis.SCHED_SWITCH));
        assertTrue(values.contains(TestRequirementAnalysis.SCHED_WAKEUP));

        req = rMap.get(TestRequirementAnalysis.FIELD_TYPE);
        assertNotNull(req);

        values = req.getValues();
        assertEquals(2, values.size());
        assertTrue(values.contains(TestRequirementAnalysis.PID));
        assertTrue(values.contains(TestRequirementAnalysis.TID));
    }
}
