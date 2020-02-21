/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModuleHelper;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisParameterProvider;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAnalysisManager;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.tests.stubs.analysis.TestAnalysis;
import org.eclipse.tracecompass.tmf.tests.stubs.analysis.TestAnalysisParameterProvider;
import org.junit.After;
import org.junit.Test;

import com.google.common.collect.Multimap;

/**
 * Test the TmfAbstractParameterProvider class
 *
 * @author Geneviève Bastien
 */
public class AnalysisParameterProviderTest {

    private static IAnalysisModuleHelper getModuleHelper(@NonNull String moduleId) {
        Multimap<String, IAnalysisModuleHelper> helpers = TmfAnalysisManager.getAnalysisModules();
        assertEquals(1, helpers.get(moduleId).size());
        return helpers.get(moduleId).iterator().next();
    }

    private static final @NonNull String MODULE_ID = "org.eclipse.linuxtools.tmf.core.tests.analysis.testParamProvider";

    /**
     * Cleanup the trace after testing
     */
    @After
    public void cleanupTrace() {
        TmfTestTrace.A_TEST_10K.dispose();
    }

    /**
     * Test that the provider's value is used
     */
    @Test
    public void testProviderTmfTrace() {
        ITmfTrace trace = TmfTestTrace.A_TEST_10K.getTrace();
        /* Make sure the value is set to null */
        IAnalysisModuleHelper helper = getModuleHelper(MODULE_ID);
        assertNotNull(helper);
        IAnalysisModule module = null;
        IAnalysisModule module2 = null;
        try {
            module = helper.newModule(trace);
            assertNotNull(module);

            assertEquals(10, module.getParameter(TestAnalysis.PARAM_TEST));

            /* Change the value of the parameter in the provider */
            Set<IAnalysisParameterProvider> providers = TmfAnalysisManager.getParameterProvidersForModule(module, trace);
            assertEquals(1, providers.size());
            TestAnalysisParameterProvider provider = (TestAnalysisParameterProvider) providers.iterator().next();
            provider.setValue(5);
            assertEquals(5, module.getParameter(TestAnalysis.PARAM_TEST));

            /* Make sure the parameter provider is the same instance for another module */
            module2 = helper.newModule(trace);
            assertNotNull(module2);
            assertTrue(module != module2);

            providers = TmfAnalysisManager.getParameterProvidersForModule(module2, trace);
            assertEquals(1, providers.size());
            TestAnalysisParameterProvider provider2 = (TestAnalysisParameterProvider) providers.iterator().next();
            assertTrue(provider == provider2);

        } catch (TmfAnalysisException e) {
            fail(e.getMessage());
        } finally {
            if (module != null) {
                module.dispose();
            }
            if (module2 != null) {
                module2.dispose();
            }
        }
    }

}
