/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.core.tests.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

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
 * Test the various cases for the state value changes in the context of a
 * pattern analysis. To add new test cases, the trace test file and the state
 * value test files can be modified to cover extra cases.
 *
 * @author Geneviève Bastien
 */
public class TmfStateValueScenarioTest {
    private static final @NonNull String TEST_TRACE = "test_traces/testTrace4.xml";

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
        XmlPatternAnalysis module = XmlUtilsTest.initializePatternModule(TmfXmlTestFiles.STATE_VALUE_PATTERN_FILE);

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
     * Test that attribute pool are generated and populated correctly
     *
     * @throws StateSystemDisposedException
     *             Exceptions thrown during state system verification
     * @throws AttributeNotFoundException
     *             Exceptions thrown during state system verification
     */
    @Test
    public void testAttributePool() throws AttributeNotFoundException, StateSystemDisposedException {
        XmlPatternAnalysis module = fModule;
        assertNotNull(module);

        ITmfStateSystem ss = module.getStateSystem(module.getId());
        assertNotNull(ss);

        int quark = ss.getQuarkAbsolute("Operations");
        List<Integer> subAttributes = ss.getSubAttributes(quark, false);
        assertEquals("Number of attribute pool children", 2, subAttributes.size());

        final int[] expectedStarts = { 1, 2, 3, 5, 7, 10, 14, 20, 20 };
        ITmfStateValue[] expectedValues = { TmfStateValue.newValueString("op1"), TmfStateValue.newValueString("op2"), TmfStateValue.nullValue(), TmfStateValue.newValueString("op1"), TmfStateValue.nullValue(), TmfStateValue.newValueString("op1"), TmfStateValue.newValueString("op2"), TmfStateValue.nullValue() };
        XmlUtilsTest.verifyStateIntervals("testAttributePool", ss, subAttributes.get(0), expectedStarts, expectedValues);

        final int[] expectedStarts2 = { 1, 2, 3, 4, 20 };
        ITmfStateValue[] expectedValues2 = { TmfStateValue.nullValue(), TmfStateValue.newValueString("op1"), TmfStateValue.newValueString("op2"), TmfStateValue.nullValue() };
        XmlUtilsTest.verifyStateIntervals("testAttributePool", ss, subAttributes.get(1), expectedStarts2, expectedValues2);

    }

}
