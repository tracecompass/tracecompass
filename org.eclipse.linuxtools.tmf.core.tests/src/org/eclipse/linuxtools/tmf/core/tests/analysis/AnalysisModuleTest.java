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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisModule;
import org.eclipse.linuxtools.tmf.core.analysis.Messages;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.linuxtools.tmf.core.tests.shared.CtfTmfTestTrace;
import org.eclipse.linuxtools.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.tests.stubs.analysis.TestAnalysis;
import org.eclipse.linuxtools.tmf.tests.stubs.analysis.TestCtfAnalysis;
import org.eclipse.osgi.util.NLS;
import org.junit.After;
import org.junit.Test;

/**
 * Test suite for the {@link TmfAbstractAnalysisModule} class
 */
public class AnalysisModuleTest {

    private static String MODULE_GENERIC_ID = "test.id";
    private static String MODULE_GENERIC_NAME = "Test analysis";

    /**
     * Some tests use traces, let's clean them here
     */
    @After
    public void cleanupTraces() {
        TmfTestTrace.A_TEST_10K.dispose();
        CtfTmfTestTrace.KERNEL.dispose();
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
            module.setTrace(TmfTestTrace.A_TEST_10K.getTrace());
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
    }

    /**
     * Test suite for {@link TmfAbstractAnalysisModule#waitForCompletion} with cancellation
     */
    @Test
    public void testWaitForCompletionCancelled() {
        TestAnalysis module = setUpAnalysis();

        /* Set a stub trace for analysis */
        ITmfTrace trace = TmfTestTrace.A_TEST_10K.getTrace();
        try {
            module.setTrace(trace);
        } catch (TmfAnalysisException e) {
            fail(e.getMessage());
        }

        module.setParameter(TestAnalysis.PARAM_TEST, 0);
        IStatus status = module.schedule();
        assertEquals(Status.OK_STATUS, status);
        boolean completed = module.waitForCompletion();

        assertFalse(completed);
        assertEquals(0, module.getAnalysisOutput());
    }

    /**
     * Test the {@link TmfAbstractAnalysisModule#setTrace(ITmfTrace)} method with wrong trace
     */
    @Test
    public void testSetWrongTrace() {
        IAnalysisModule module = new TestCtfAnalysis();

        module.setName(MODULE_GENERIC_NAME);
        module.setId(MODULE_GENERIC_ID);
        assertEquals(MODULE_GENERIC_ID, module.getId());
        assertEquals(MODULE_GENERIC_NAME, module.getName());

        Exception exception = null;
        try {
            module.setTrace(TmfTestTrace.A_TEST_10K.getTrace());
        } catch (TmfAnalysisException e) {
            exception = e;
        }
        assertNotNull(exception);
        assertEquals(NLS.bind(Messages.TmfAbstractAnalysisModule_AnalysisCannotExecute, module.getName()), exception.getMessage());

    }

    /**
     * Test suite for the {@link TmfAbstractAnalysisModule#cancel()} method
     */
    @Test
    public void testCancel() {
        TestAnalysis module = setUpAnalysis();

        module.setParameter(TestAnalysis.PARAM_TEST, 999);
        try {
            module.setTrace(TmfTestTrace.A_TEST_10K.getTrace());
        } catch (TmfAnalysisException e) {
            fail(e.getMessage());
        }

        assertEquals(Status.OK_STATUS, module.schedule());

        /* Give the job a chance to start */
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }

        module.cancel();
        assertFalse(module.waitForCompletion());
        assertEquals(-1, module.getAnalysisOutput());
    }

    /**
     * Test suite for the {@link IAnalysisModule#notifyParameterChanged(String)}
     * method
     */
    @Test
    public void testParameterChanged() {
        TestAnalysis module = setUpAnalysis();

        try {
            module.setTrace(TmfTestTrace.A_TEST_10K.getTrace());
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
    }
}
