/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
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
import static org.junit.Assume.assumeTrue;

import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisModule;
import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisModuleHelper;
import org.eclipse.linuxtools.tmf.core.analysis.Messages;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAnalysisManager;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAnalysisModuleHelperConfigElement;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.linuxtools.tmf.core.tests.shared.CtfTmfTestTrace;
import org.eclipse.linuxtools.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;
import org.eclipse.linuxtools.tmf.tests.stubs.analysis.TestAnalysis;
import org.eclipse.linuxtools.tmf.tests.stubs.analysis.TestCtfAnalysis;
import org.eclipse.linuxtools.tmf.tests.stubs.ctf.CtfTmfTraceStub;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.TmfTraceStub;
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
    private IAnalysisModuleHelper fCtfModule;

    /**
     * Gets the module helpers for 2 test modules
     */
    @Before
    public void getModules() {
        fModule = TmfAnalysisManager.getAnalysisModule(AnalysisManagerTest.MODULE_PARAM);
        assertNotNull(fModule);
        assertTrue(fModule instanceof TmfAnalysisModuleHelperConfigElement);
        fCtfModule = TmfAnalysisManager.getAnalysisModule(AnalysisManagerTest.MODULE_CTF);
        assertNotNull(fCtfModule);
        assertTrue(fCtfModule instanceof TmfAnalysisModuleHelperConfigElement);
    }

    /**
     * Some tests use traces, let's clean them here
     */
    @After
    public void cleanupTraces() {
        TmfTestTrace.A_TEST_10K.dispose();
        CtfTmfTestTrace.KERNEL.dispose();
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

        /* With ctf module */
        assertEquals(AnalysisManagerTest.MODULE_CTF, fCtfModule.getId());
        assertEquals("Test analysis ctf", fCtfModule.getName());
        assertTrue(fCtfModule.isAutomatic());
    }

    /**
     * Test the {@link TmfAnalysisModuleHelperConfigElement#appliesToTraceType(Class)}
     * method for the 2 modules
     */
    @Test
    public void testAppliesToTrace() {
        /* non-ctf module */
        assertFalse(fModule.appliesToTraceType(TmfTrace.class));
        assertTrue(fModule.appliesToTraceType(TmfTraceStub.class));
        assertFalse(fModule.appliesToTraceType(CtfTmfTraceStub.class));

        /* ctf module */
        assertFalse(fCtfModule.appliesToTraceType(TmfTrace.class));
        assertFalse(fCtfModule.appliesToTraceType(TmfTraceStub.class));
        assertTrue(fCtfModule.appliesToTraceType(CtfTmfTraceStub.class));
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

        /* Test Analysis module with ctf trace, should return an exception */
        assumeTrue(CtfTmfTestTrace.KERNEL.exists());
        CtfTmfTraceStub ctfTrace = (CtfTmfTraceStub) CtfTmfTestTrace.KERNEL.getTrace();
        module = null;
        try {
            module = fModule.newModule(ctfTrace);
        } catch (TmfAnalysisException e) {
            exception = e;
        }
        assertNotNull(exception);
        assertEquals(NLS.bind(Messages.TmfAnalysisModuleHelper_AnalysisDoesNotApply, fModule.getName()), exception.getMessage());

        /* Test analysis CTF module with ctf trace stub */
        exception = null;
        module = null;
        try {
            module = fCtfModule.newModule(ctfTrace);
        } catch (TmfAnalysisException e) {
            exception = e;
        }
        assertNull(exception);
        assertNotNull(module);
        assertTrue(module instanceof TestCtfAnalysis);
    }

    /**
     * Test for the initialization of parameters from the extension points
     */
    @Test
    public void testParameters() {
        assumeTrue(CtfTmfTestTrace.KERNEL.exists());
        CtfTmfTrace ctftrace = CtfTmfTestTrace.KERNEL.getTrace();
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
        helper = TmfAnalysisManager.getAnalysisModule(AnalysisManagerTest.MODULE_CTF);
        try {
            module = helper.newModule(ctftrace);
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
