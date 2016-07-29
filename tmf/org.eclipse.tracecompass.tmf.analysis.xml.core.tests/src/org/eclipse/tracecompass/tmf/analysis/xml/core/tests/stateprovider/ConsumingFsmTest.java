/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.core.tests.stateprovider;

import static org.junit.Assert.assertNotNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.pattern.stateprovider.XmlPatternAnalysis;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.common.TmfXmlTestFiles;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.module.XmlUtilsTest;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the consuming state of an fsm. This test creates a pattern with two fsm.
 * One is consuming, the other is not. Each fsm will create three scenarios. We
 * use a counter to verify if an event was consumed or not.
 *
 * @author Jean-Christian Kouame
 */
public class ConsumingFsmTest {

    private static final @NonNull String TEST_TRACE = "test_traces/testTrace5.xml";

    ITmfTrace fTrace;
    XmlPatternAnalysis fModule;

    /**
     * Initializes the trace and the module for the tests
     *
     * @throws TmfAnalysisException
     *             Any exception thrown during module initialization
     */
    @Before
    public void setUp() throws TmfAnalysisException {
        ITmfTrace trace = XmlUtilsTest.initializeTrace(TEST_TRACE);
        @NonNull XmlPatternAnalysis module = XmlUtilsTest.initializePatternModule(TmfXmlTestFiles.CONSUMING_FSM_TEST);

        module.setTrace(trace);

        module.schedule();
        module.waitForCompletion();

        fTrace = trace;
        fModule = module;
    }

    /**
     * Dispose the module and the trace
     */
    @After
    public void cleanUp() {
        fTrace.dispose();
        fModule.dispose();
    }

    /**
     * Test the consuming fsm counter
     *
     * @throws StateSystemDisposedException
     *             Exceptions thrown during state system verification
     * @throws AttributeNotFoundException
     *             Exceptions thrown during state system verification
     */
    @Test
    public void testConsumingFsm() throws AttributeNotFoundException, StateSystemDisposedException {
        XmlPatternAnalysis module = fModule;
        assertNotNull(module);

        ITmfStateSystem ss = module.getStateSystem(module.getId());
        assertNotNull(ss);

        int quark = ss.getQuarkAbsolute("consuming");

        final int[] expectedStarts = { 1, 7, 7 };
        ITmfStateValue[] expectedValues = { TmfStateValue.nullValue(), TmfStateValue.newValueLong(1l)};
        XmlUtilsTest.verifyStateIntervals("testConsuming", ss, quark, expectedStarts, expectedValues);

    }

    /**
     * Test the non consuming fsm counter
     *
     * @throws StateSystemDisposedException
     *             Exceptions thrown during state system verification
     * @throws AttributeNotFoundException
     *             Exceptions thrown during state system verification
     */
    @Test
    public void testNonConsumingFsm() throws AttributeNotFoundException, StateSystemDisposedException {
        XmlPatternAnalysis module = fModule;
        assertNotNull(module);

        ITmfStateSystem ss = module.getStateSystem(module.getId());
        assertNotNull(ss);

        int quark = ss.getQuarkAbsolute("non_consuming");

        final int[] expectedStarts = { 1, 7, 7 };
        ITmfStateValue[] expectedValues = { TmfStateValue.nullValue(), TmfStateValue.newValueLong(3l)};
        XmlUtilsTest.verifyStateIntervals("testNonConsuming", ss, quark, expectedStarts, expectedValues);

    }
}

