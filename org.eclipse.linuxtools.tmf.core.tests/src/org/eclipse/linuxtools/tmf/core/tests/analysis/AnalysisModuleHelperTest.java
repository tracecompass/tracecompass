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

package org.eclipse.linuxtools.tmf.core.tests.analysis;

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
import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisModule;
import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisModuleHelper;
import org.eclipse.linuxtools.tmf.core.analysis.Messages;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAnalysisManager;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAnalysisModuleHelperConfigElement;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAnalysisRequirement;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.linuxtools.tmf.core.project.model.TmfTraceType;
import org.eclipse.linuxtools.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.linuxtools.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;
import org.eclipse.linuxtools.tmf.tests.stubs.analysis.TestAnalysis;
import org.eclipse.linuxtools.tmf.tests.stubs.analysis.TestAnalysis2;
import org.eclipse.linuxtools.tmf.tests.stubs.analysis.TestRequirementAnalysis;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.TmfTraceStub;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.TmfTraceStub2;
import org.eclipse.osgi.util.NLS;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;

/**
 * Test suite for the {@link TmfAnalysisModuleHelperConfigElement} class
 *
 * @author Geneviève Bastien
 */
public class AnalysisModuleHelperTest {

    private static final String VALID_TRACE_TYPE = "org.eclipse.linuxtools.tmf.core.tests.tracetype";

    private IAnalysisModuleHelper fModule;
    private IAnalysisModuleHelper fModuleOther;
    private IAnalysisModuleHelper fReqModule;
    private ITmfTrace fTrace;

    /**
     * Gets the module helpers for 2 test modules
     */
    @Before
    public void getModules() {
        fModule = TmfAnalysisManager.getAnalysisModule(AnalysisManagerTest.MODULE_PARAM);
        assertNotNull(fModule);
        assertTrue(fModule instanceof TmfAnalysisModuleHelperConfigElement);
        fModuleOther = TmfAnalysisManager.getAnalysisModule(AnalysisManagerTest.MODULE_SECOND);
        assertNotNull(fModuleOther);
        assertTrue(fModuleOther instanceof TmfAnalysisModuleHelperConfigElement);
        fReqModule = TmfAnalysisManager.getAnalysisModule(AnalysisManagerTest.MODULE_REQ);
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
        Bundle thisbundle = Platform.getBundle("org.eclipse.linuxtools.tmf.core.tests");
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

        /* stub module 2 */
        assertFalse(fModuleOther.appliesToTraceType(TmfTrace.class));
        assertFalse(fModuleOther.appliesToTraceType(TmfTraceStub.class));
        assertTrue(fModuleOther.appliesToTraceType(TmfTraceStub2.class));
    }

    /**
     * Test the
     * {@link TmfAnalysisModuleHelperConfigElement#newModule(ITmfTrace)} method
     * for the 2 modules
     */
    @Test
    public void testNewModule() {
        /* Test analysis module with traceStub */
        Exception exception = null;
        try (IAnalysisModule module = fModule.newModule(TmfTestTrace.A_TEST_10K.getTrace());) {
            assertNotNull(module);
            assertTrue(module instanceof TestAnalysis);
        } catch (TmfAnalysisException e) {
            exception = e;
        }
        assertNull(exception);

        /* TestAnalysis2 module with trace, should return an exception */
        try (IAnalysisModule module = fModuleOther.newModule(TmfTestTrace.A_TEST_10K.getTrace());) {
        } catch (TmfAnalysisException e) {
            exception = e;
        }
        assertNotNull(exception);
        assertEquals(NLS.bind(Messages.TmfAnalysisModuleHelper_AnalysisDoesNotApply, fModuleOther.getName()), exception.getMessage());

        /* TestAnalysis2 module with a TraceStub2 */
        exception = null;
        try (IAnalysisModule module = fModuleOther.newModule(fTrace);) {
            assertNotNull(module);
            assertTrue(module instanceof TestAnalysis2);
        } catch (TmfAnalysisException e) {
            exception = e;
        }
        assertNull(exception);
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
        IAnalysisModuleHelper helper = TmfAnalysisManager.getAnalysisModule(AnalysisManagerTest.MODULE_PARAM);
        try (IAnalysisModule module = helper.newModule(trace);) {
            assertNull(module.getParameter(TestAnalysis.PARAM_TEST));
            module.setParameter(TestAnalysis.PARAM_TEST, 1);
            assertEquals(1, module.getParameter(TestAnalysis.PARAM_TEST));

        } catch (TmfAnalysisException e1) {
            fail(e1.getMessage());
            return;
        }

        /* This module has a parameter with default value */
        helper = TmfAnalysisManager.getAnalysisModule(AnalysisManagerTest.MODULE_PARAM_DEFAULT);
        try (IAnalysisModule module = helper.newModule(trace);) {
            assertEquals(3, module.getParameter(TestAnalysis.PARAM_TEST));
            module.setParameter(TestAnalysis.PARAM_TEST, 1);
            assertEquals(1, module.getParameter(TestAnalysis.PARAM_TEST));

        } catch (TmfAnalysisException e1) {
            fail(e1.getMessage());
            return;
        }

        /*
         * This module does not have a parameter so setting it should throw an
         * error
         */
        helper = TmfAnalysisManager.getAnalysisModule(AnalysisManagerTest.MODULE_SECOND);
        Exception exception = null;
        try (IAnalysisModule module = helper.newModule(fTrace);) {

            assertNull(module.getParameter(TestAnalysis.PARAM_TEST));

            try {
                module.setParameter(TestAnalysis.PARAM_TEST, 1);
            } catch (RuntimeException e) {
                exception = e;
            }
        } catch (TmfAnalysisException e1) {
            fail(e1.getMessage());
            return;
        }
        assertNotNull(exception);
    }

    /**
     * Test for the
     * {@link TmfAnalysisModuleHelperConfigElement#getValidTraceTypes} method
     */
    @Test
    public void testGetValidTraceTypes() {
        TraceTypeHelper traceTypeHelper = TmfTraceType.getTraceType(VALID_TRACE_TYPE);
        int traceTypeCount = 0;
        Iterable<Class<? extends ITmfTrace>> traceTypes = fReqModule.getValidTraceTypes();
        for (Class<? extends ITmfTrace> traceType : traceTypes) {
            assertTrue(fReqModule.appliesToTraceType(traceType));
            assertNotNull(traceTypeHelper);
            assertEquals(traceTypeHelper.getTraceClass(), traceType);
            traceTypeCount++;
        }
        assertEquals(1, traceTypeCount);
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
