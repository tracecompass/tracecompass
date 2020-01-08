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

package org.eclipse.tracecompass.tmf.core.tests.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.analysis.Messages;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestHelper;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.tests.stubs.analysis.TestAnalysis;
import org.eclipse.tracecompass.tmf.tests.stubs.analysis.TestAnalysis2;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

import com.google.common.collect.ImmutableSet;

/**
 * Test suite for the {@link TmfAbstractAnalysisModule} class
 */
public class AnalysisModuleTest {

    /** Test timeout */
    @Rule
    public TestRule timeoutRule = new Timeout(1, TimeUnit.MINUTES);

    private static final @NonNull String MODULE_GENERIC_ID = "test.id";
    private static final @NonNull String MODULE_GENERIC_NAME = "Test analysis";

    /**
     * Some tests use traces, let's clean them here
     */
    @After
    public void cleanupTraces() {
        TmfTestTrace.A_TEST_10K.dispose();
    }

    /**
     * Test suite for analysis module getters and setters
     */
    @Test
    public void testGettersSetters() {
        IAnalysisModule module = new TestAnalysis();

        module.setName(MODULE_GENERIC_NAME);
        module.setId(MODULE_GENERIC_ID);
        assertEquals(MODULE_GENERIC_ID, module.getId());
        assertEquals(MODULE_GENERIC_NAME, module.getName());

        module.setAutomatic(false);
        assertFalse(module.isAutomatic());
        module.setAutomatic(true);
        assertTrue(module.isAutomatic());
        module.addParameter(TestAnalysis.PARAM_TEST);
        assertNull(module.getParameter(TestAnalysis.PARAM_TEST));
        module.setParameter(TestAnalysis.PARAM_TEST, 1);
        assertEquals(1, module.getParameter(TestAnalysis.PARAM_TEST));

        assertEquals(0, module.getDependencyLevel());

        /* Try to set and get wrong parameter */
        String wrongParam = "abc";
        Exception exception = null;
        try {
            module.setParameter(wrongParam, 1);
        } catch (RuntimeException e) {
            exception = e;
            assertEquals(NLS.bind(Messages.TmfAbstractAnalysisModule_InvalidParameter, wrongParam, module.getName()), e.getMessage());
        }
        assertNotNull(exception);
        assertNull(module.getParameter(wrongParam));

        module.dispose();
    }

    private static TestAnalysis setUpAnalysis() {
        TestAnalysis module = new TestAnalysis();

        module.setName(MODULE_GENERIC_NAME);
        module.setId(MODULE_GENERIC_ID);
        module.addParameter(TestAnalysis.PARAM_TEST);

        return module;
    }

    /**
     * Test suite for analysis module
     * {@link TmfAbstractAnalysisModule#waitForCompletion} with successful
     * execution
     */
    @Test
    public void testWaitForCompletionSuccess() {
        TestAnalysis module = setUpAnalysis();

        IStatus status = module.schedule();
        assertEquals(IStatus.ERROR, status.getSeverity());

        /* Set a stub trace for analysis */
        try {
            assertTrue(module.setTrace(TmfTestTrace.A_TEST_10K.getTrace()));
        } catch (TmfAnalysisException e) {
            fail(e.getMessage());
        }

        /* Default execution, with output 1 */
        module.setParameter(TestAnalysis.PARAM_TEST, 1);
        status = module.schedule();
        assertEquals(Status.OK_STATUS, status);
        boolean completed = module.waitForCompletion();

        assertTrue(completed);
        assertEquals(1, module.getAnalysisOutput());

        module.dispose();
    }

    /**
     * Test suite for {@link TmfAbstractAnalysisModule#waitForCompletion} with
     * cancellation
     */
    @Test
    public void testWaitForCompletionCancelled() {
        TestAnalysis module = setUpAnalysis();
        try {
            /* Set a stub trace for analysis */
            ITmfTrace trace = TmfTestTrace.A_TEST_10K.getTrace();
            try {
                assertTrue(module.setTrace(trace));
            } catch (TmfAnalysisException e) {
                fail(e.getMessage());
            }

            module.setParameter(TestAnalysis.PARAM_TEST, 0);
            IStatus status = module.schedule();
            assertEquals(Status.OK_STATUS, status);
            boolean completed = module.waitForCompletion();

            assertFalse(completed);
            assertEquals(0, module.getAnalysisOutput());
        } finally {
            module.dispose();
        }
    }

    /**
     * Test the {@link TmfAbstractAnalysisModule#setTrace(ITmfTrace)} method
     * with wrong trace
     */
    @Test
    public void testSetWrongTrace() {
        IAnalysisModule module = new TestAnalysis2();

        module.setName(MODULE_GENERIC_NAME);
        module.setId(MODULE_GENERIC_ID);
        assertEquals(MODULE_GENERIC_ID, module.getId());
        assertEquals(MODULE_GENERIC_NAME, module.getName());

        try {
            assertFalse(module.setTrace(TmfTestTrace.A_TEST_10K.getTrace()));
        } catch (TmfAnalysisException e) {
            fail();
        }

        module.dispose();
    }

    /**
     * Test the {@link TmfAbstractAnalysisModule#setTrace(ITmfTrace)} method
     * with wrong trace
     */
    @Test
    public void testSetTraceTwice() {
        IAnalysisModule module = new TestAnalysis();

        module.setName(MODULE_GENERIC_NAME);
        module.setId(MODULE_GENERIC_ID);

        try {
            assertTrue(module.setTrace(TmfTestTrace.A_TEST_10K.getTrace()));
        } catch (TmfAnalysisException e) {
            fail();
        }
        TmfAnalysisException exception = null;
        try {
            module.setTrace(TmfTestTrace.A_TEST_10K.getTrace());
        } catch (TmfAnalysisException e) {
            exception = e;
        }
        assertNotNull(exception);

        module.dispose();
    }

    /**
     * Test suite for the {@link TmfAbstractAnalysisModule#cancel()} method
     */
    @Test
    public void testCancel() {
        TestAnalysis module = setUpAnalysis();

        module.setParameter(TestAnalysis.PARAM_TEST, 999);
        try {
            assertTrue(module.setTrace(TmfTestTrace.A_TEST_10K.getTrace()));
        } catch (TmfAnalysisException e) {
            fail(e.getMessage());
        }

        IStatus schedule = module.schedule();
        assertEquals(Status.OK_STATUS, schedule);

        /* Give the job a chance to start */
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }

        module.cancel();
        assertFalse(module.waitForCompletion());
        assertEquals(-1, module.getAnalysisOutput());

        module.dispose();
    }

    /**
     * Test suite for the {@link IAnalysisModule#notifyParameterChanged(String)}
     * method
     */
    @Test
    public void testParameterChanged() {
        TestAnalysis module = setUpAnalysis();

        try {
            assertTrue(module.setTrace(TmfTestTrace.A_TEST_10K.getTrace()));
        } catch (TmfAnalysisException e) {
            fail(e.getMessage());
        }

        /* Check exception if no wrong parameter name */
        Exception exception = null;
        try {
            module.notifyParameterChanged("aaa");
        } catch (RuntimeException e) {
            exception = e;
        }
        assertNotNull(exception);

        /*
         * Cannot test anymore of this method, need a parameter provider to do
         * this
         */
        module.dispose();
    }

    /**
     * Test the {@link TmfTestHelper#executeAnalysis(IAnalysisModule)} method
     */
    @Test
    public void testHelper() {
        TestAnalysis module = setUpAnalysis();

        try {
            assertTrue(module.setTrace(TmfTestTrace.A_TEST_10K.getTrace()));
        } catch (TmfAnalysisException e) {
            fail(e.getMessage());
        }

        module.setParameter(TestAnalysis.PARAM_TEST, 1);
        boolean res = TmfTestHelper.executeAnalysis(module);
        assertTrue(res);

        module.setParameter(TestAnalysis.PARAM_TEST, 0);
        res = TmfTestHelper.executeAnalysis(module);
        assertFalse(res);

        module.dispose();
    }

    /**
     * Test the {@link TmfAbstractAnalysisModule} also executes the dependent
     * analyses
     */
    @Test
    public void testDependentAnalyses() {

        ITmfTrace trace = TmfTestTrace.A_TEST_10K.getTrace();
        int paramAndResult = 5;

        /* Setup the dependent module */
        final String suffix = " dep";
        final TestAnalysis depModule = new TestAnalysis() {

            @Override
            protected boolean executeAnalysis(IProgressMonitor monitor) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return false;
                }
                return super.executeAnalysis(monitor);
            }

        };
        depModule.setName(MODULE_GENERIC_NAME + suffix);
        depModule.setId(MODULE_GENERIC_ID + suffix);
        depModule.addParameter(TestAnalysis.PARAM_TEST);
        depModule.setParameter(TestAnalysis.PARAM_TEST, paramAndResult);

        /* Prepare the main analysis with a dependent analysis */
        TestAnalysis module = new TestAnalysis() {

            @Override
            protected Iterable<IAnalysisModule> getDependentAnalyses() {
                Set<IAnalysisModule> modules = new HashSet<>();
                modules.add(depModule);
                return modules;
            }

        };

        module.setName(MODULE_GENERIC_NAME);
        module.setId(MODULE_GENERIC_ID);
        module.addParameter(TestAnalysis.PARAM_TEST);
        module.setParameter(TestAnalysis.PARAM_TEST, paramAndResult);

        try {
            assertTrue(depModule.setTrace(trace));
            assertTrue(module.setTrace(trace));
        } catch (TmfAnalysisException e) {
            fail(e.getMessage());
        }

        /* Verify none of the module has run */
        assertEquals(0, module.getAnalysisOutput());
        assertEquals(0, depModule.getAnalysisOutput());

        module.schedule();
        assertTrue(module.waitForCompletion());
        assertEquals(paramAndResult, module.getAnalysisOutput());

        /* Make sure the dependent analysis has run and completed */
        assertEquals(paramAndResult, depModule.getAnalysisOutput());

        /* Check the dependency level of both analyses */
        assertEquals(0, depModule.getDependencyLevel());
        assertEquals(1, module.getDependencyLevel());

        module.dispose();
        depModule.dispose();
        trace.dispose();

    }

    /**
     * Test that the dependency level is consistent with a case where
     * B depends on A, and C depends on A and B
     */
    @Test
    public void testMultipleDependencies() {

        ITmfTrace trace = TmfTestTrace.A_TEST_10K.getTrace();

        /* Prepare module A with no dependency */
        IAnalysisModule moduleA = new TestAnalysis();
        moduleA.setName(MODULE_GENERIC_NAME);
        moduleA.setId(MODULE_GENERIC_ID);
        moduleA.addParameter(TestAnalysis.PARAM_TEST);
        moduleA.setParameter(TestAnalysis.PARAM_TEST, 1);

        /* Prepare module B depending on A */
        String suffix = " B";
        IAnalysisModule moduleB = new TestAnalysis() {

            @Override
            protected Iterable<IAnalysisModule> getDependentAnalyses() {
                return ImmutableSet.of(moduleA);
            }

        };
        moduleB.setName(MODULE_GENERIC_NAME + suffix);
        moduleB.setId(MODULE_GENERIC_ID + suffix);
        moduleB.addParameter(TestAnalysis.PARAM_TEST);
        moduleB.setParameter(TestAnalysis.PARAM_TEST, 1);

        /* Prepare module C depending on A and B */
        suffix = " C";
        IAnalysisModule moduleC = new TestAnalysis() {

            @Override
            protected Iterable<IAnalysisModule> getDependentAnalyses() {
                return ImmutableSet.of(moduleA, moduleB);
            }

        };
        moduleC.setName(MODULE_GENERIC_NAME + suffix);
        moduleC.setId(MODULE_GENERIC_ID + suffix);
        moduleC.addParameter(TestAnalysis.PARAM_TEST);
        moduleC.setParameter(TestAnalysis.PARAM_TEST, 1);

        try {
            assertTrue(moduleA.setTrace(trace));
            assertTrue(moduleB.setTrace(trace));
            assertTrue(moduleC.setTrace(trace));
        } catch (TmfAnalysisException e) {
            fail(e.getMessage());
        }

        moduleC.schedule();
        assertTrue(moduleC.waitForCompletion());

        /* Check the dependency level of the analyses */
        assertEquals(0, moduleA.getDependencyLevel());
        assertEquals(1, moduleB.getDependencyLevel());
        assertEquals(3, moduleC.getDependencyLevel());

        moduleA.dispose();
        moduleB.dispose();
        moduleC.dispose();
        trace.dispose();

    }
}
