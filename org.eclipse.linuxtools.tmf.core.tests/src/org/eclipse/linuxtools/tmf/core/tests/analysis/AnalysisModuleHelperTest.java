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
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisModule;
import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisModuleHelper;
import org.eclipse.linuxtools.tmf.core.analysis.Messages;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAnalysisManager;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAnalysisModuleHelperConfigElement;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.linuxtools.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;
import org.eclipse.linuxtools.tmf.tests.stubs.analysis.TestAnalysis;
import org.eclipse.linuxtools.tmf.tests.stubs.analysis.TestAnalysis2;
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

    private IAnalysisModuleHelper fModule;
    private IAnalysisModuleHelper fModuleOther;
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
     * Test the {@link TmfAnalysisModuleHelperConfigElement#appliesToTraceType(Class)}
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
     * Test the {@link TmfAnalysisModuleHelperConfigElement#newModule(ITmfTrace)} method
     * for the 2 modules
     */
    @Test
    public void testNewModule() {
        /* Test analysis module with traceStub */
        Exception exception = null;
        IAnalysisModule module = null;
        try {
            module = fModule.newModule(TmfTestTrace.A_TEST_10K.getTrace());
        } catch (TmfAnalysisException e) {
            exception = e;
        }
        assertNull(exception);
        assertNotNull(module);
        assertTrue(module instanceof TestAnalysis);

        /* TestAnalysis2 module with trace, should return an exception */
        module = null;
        try {
            module = fModuleOther.newModule(TmfTestTrace.A_TEST_10K.getTrace());
        } catch (TmfAnalysisException e) {
            exception = e;
        }
        assertNotNull(exception);
        assertEquals(NLS.bind(Messages.TmfAnalysisModuleHelper_AnalysisDoesNotApply, fModuleOther.getName()), exception.getMessage());

        /* TestAnalysis2 module with a TraceStub2 */
        exception = null;
        module = null;
        try {
            module = fModuleOther.newModule(fTrace);
        } catch (TmfAnalysisException e) {
            exception = e;
        }
        assertNull(exception);
        assertNotNull(module);
        assertTrue(module instanceof TestAnalysis2);
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
        IAnalysisModule module;
        try {
            module = helper.newModule(trace);
        } catch (TmfAnalysisException e1) {
            fail(e1.getMessage());
            return;
        }

        assertNull(module.getParameter(TestAnalysis.PARAM_TEST));
        module.setParameter(TestAnalysis.PARAM_TEST, 1);
        assertEquals(1, module.getParameter(TestAnalysis.PARAM_TEST));

        /* This module has a parameter with default value */
        helper = TmfAnalysisManager.getAnalysisModule(AnalysisManagerTest.MODULE_PARAM_DEFAULT);
        try {
            module = helper.newModule(trace);
        } catch (TmfAnalysisException e1) {
            fail(e1.getMessage());
            return;
        }
        assertEquals(3, module.getParameter(TestAnalysis.PARAM_TEST));
        module.setParameter(TestAnalysis.PARAM_TEST, 1);
        assertEquals(1, module.getParameter(TestAnalysis.PARAM_TEST));

        /*
         * This module does not have a parameter so setting it should throw an
         * error
         */
        helper = TmfAnalysisManager.getAnalysisModule(AnalysisManagerTest.MODULE_SECOND);
        try {
            module = helper.newModule(fTrace);
        } catch (TmfAnalysisException e1) {
            fail(e1.getMessage());
            return;
        }
        assertNull(module.getParameter(TestAnalysis.PARAM_TEST));
        Exception exception = null;
        try {
            module.setParameter(TestAnalysis.PARAM_TEST, 1);
        } catch (RuntimeException e) {
            exception = e;
        }
        assertNotNull(exception);
    }
}
