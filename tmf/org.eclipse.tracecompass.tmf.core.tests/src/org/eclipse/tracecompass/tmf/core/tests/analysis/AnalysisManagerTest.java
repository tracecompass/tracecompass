/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModuleHelper;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModuleSource;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAnalysisManager;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.tests.stubs.analysis.AnalysisModuleSourceStub;
import org.eclipse.tracecompass.tmf.tests.stubs.analysis.AnalysisModuleTestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Multimap;

/**
 * Test suite for the TmfAnalysisModule class
 */
public class AnalysisManagerTest {

    /** Id of analysis module with parameter */
    public static final @NonNull String MODULE_PARAM = "org.eclipse.linuxtools.tmf.core.tests.analysis.test";
    /** ID of analysis module with parameter and default value */
    public static final @NonNull String MODULE_PARAM_DEFAULT = "org.eclipse.linuxtools.tmf.core.tests.analysis.test2";
    /** ID of analysis module for trace 2 classes only */
    public static final @NonNull String MODULE_SECOND = "org.eclipse.linuxtools.tmf.core.tests.analysis.testother";
    /** Id of analysis module with requirements */
    public static final @NonNull String MODULE_REQ = "org.eclipse.linuxtools.tmf.core.tests.analysis.reqtest";

    private ITmfTrace fTrace;

    /**
     * Set up some trace code
     */
    @Before
    public void setupTraces() {
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
     * Test suite for the {@link TmfAnalysisManager#getAnalysisModules()} method
     */
    @Test
    public void testGetAnalysisModules() {
        Multimap<String, IAnalysisModuleHelper> modules = TmfAnalysisManager.getAnalysisModules();
        /* At least 3 modules should be found */
        assertTrue(modules.size() >= 3);

        Collection<IAnalysisModuleHelper> moduleList = modules.get(MODULE_PARAM_DEFAULT);
        assertEquals(1, moduleList.size());
        IAnalysisModuleHelper module = moduleList.iterator().next();
        assertTrue(module.isAutomatic());

        moduleList = modules.get(MODULE_PARAM);
        assertEquals(1, moduleList.size());
        module = moduleList.iterator().next();
        assertFalse(module.isAutomatic());
    }

    /**
     * Test suite for {@link TmfAnalysisManager#getAnalysisModules(Class)} Use
     * the test TMF trace and test trace2 stubs as sample traces
     */
    @Test
    public void testListForTraces() {
        /* Generic TmfTrace */
        ITmfTrace trace = TmfTestTrace.A_TEST_10K.getTrace();
        Class<@NonNull ? extends ITmfTrace> traceClass = trace.getClass();
        assertNotNull(traceClass);
        Map<String, IAnalysisModuleHelper> map = TmfAnalysisManager.getAnalysisModules(traceClass);

        assertTrue(map.containsKey(MODULE_PARAM));
        assertTrue(map.containsKey(MODULE_PARAM_DEFAULT));
        assertFalse(map.containsKey(MODULE_SECOND));

        /* TmfTraceStub2 class */
        traceClass = fTrace.getClass();
        assertNotNull(traceClass);
        map = TmfAnalysisManager.getAnalysisModules(traceClass);

        assertTrue(map.containsKey(MODULE_PARAM));
        assertTrue(map.containsKey(MODULE_PARAM_DEFAULT));
        assertTrue(map.containsKey(MODULE_SECOND));
    }

    /**
     * Test suite to test refresh of analysis module when adding a {@link IAnalysisModuleSource}
     */
    @Test
    public void testSources() {
        /* Make sure that modules in the new source are not in the list already */
        /* Generic TmfTrace */
        ITmfTrace trace = TmfTestTrace.A_TEST_10K.getTrace();
        Class<@NonNull ? extends ITmfTrace> traceClass = trace.getClass();
        assertNotNull(traceClass);
        Map<String, IAnalysisModuleHelper> map = TmfAnalysisManager.getAnalysisModules(traceClass);

        assertFalse(map.containsKey(AnalysisModuleTestHelper.moduleStubEnum.TEST.name()));

        /* TmfTraceStub2 class */
        Class<@NonNull ? extends ITmfTrace> ftraceClass = fTrace.getClass();
        assertNotNull(ftraceClass);
        map = TmfAnalysisManager.getAnalysisModules(ftraceClass);

        assertFalse(map.containsKey(AnalysisModuleTestHelper.moduleStubEnum.TEST2.name()));

        /* Add new source */
        TmfAnalysisManager.registerModuleSource(new AnalysisModuleSourceStub());

        /* Now make sure the modules are present */
        map = TmfAnalysisManager.getAnalysisModules(traceClass);
        assertTrue(map.containsKey(AnalysisModuleTestHelper.moduleStubEnum.TEST.name()));

        map = TmfAnalysisManager.getAnalysisModules(ftraceClass);
        assertTrue(map.containsKey(AnalysisModuleTestHelper.moduleStubEnum.TEST2.name()));
    }

}
