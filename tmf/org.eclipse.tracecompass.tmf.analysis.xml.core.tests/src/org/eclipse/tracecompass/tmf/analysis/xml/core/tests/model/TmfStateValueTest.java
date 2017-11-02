/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.core.tests.model;

import static org.junit.Assert.assertNotNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.DataDrivenAnalysisModule;
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
 * Test the various cases for the state value changes. To add new test cases,
 * the trace test file and the state value test files can be modified to cover
 * extra cases.
 *
 * @author Geneviève Bastien
 */
public class TmfStateValueTest {

    private static final @NonNull String TEST_TRACE = "test_traces/testTrace4.xml";

    private ITmfTrace fTrace;
    private DataDrivenAnalysisModule fModule;

    /**
     * Initializes the trace and the module for the tests
     *
     * @throws TmfAnalysisException
     *             Any exception thrown during module initialization
     */
    @Before
    public void setUp() throws TmfAnalysisException {
        ITmfTrace trace = XmlUtilsTest.initializeTrace(TEST_TRACE);
        fTrace = trace;

        DataDrivenAnalysisModule module = XmlUtilsTest.initializeModule(TmfXmlTestFiles.STATE_VALUE_FILE);
        fModule = module;

        module.setTrace(trace);
        module.schedule();
        module.waitForCompletion();
    }

    /**
     * Dispose the module and the trace
     */
    @After
    public void cleanUp() {
        ITmfTrace trace = fTrace;
        if (trace != null) {
            trace.dispose();
        }
        DataDrivenAnalysisModule module = fModule;
        if (module != null) {
            module.dispose();
        }
    }

    /**
     * Test that the ongoing state is updated instead of creating a new state
     *
     * @throws StateSystemDisposedException
     *             Exceptions thrown during state system verification
     * @throws AttributeNotFoundException
     *             Exceptions thrown during state system verification
     */
    @Test
    public void testStateValueUpdate() throws AttributeNotFoundException, StateSystemDisposedException {
        DataDrivenAnalysisModule module = fModule;
        assertNotNull(module);

        ITmfStateSystem ss = module.getStateSystem();
        assertNotNull(ss);

        int quark = ss.getQuarkAbsolute("update", "0");

        final int[] expectedStarts = { 1, 3, 5, 7, 7 };
        ITmfStateValue[] expectedValues = { TmfStateValue.newValueString("GOOD"), TmfStateValue.nullValue(), TmfStateValue.newValueString("BAD"), TmfStateValue.nullValue() };
        XmlUtilsTest.verifyStateIntervals("testStateValueUpdate", ss, quark, expectedStarts, expectedValues);

    }

    /**
     * Test that a state change with no update causes the modification of the
     * state value at the time of the event
     *
     * @throws StateSystemDisposedException
     *             Exceptions thrown during state system verification
     * @throws AttributeNotFoundException
     *             Exceptions thrown during state system verification
     */
    @Test
    public void testStateValueModify() throws AttributeNotFoundException, StateSystemDisposedException {
        DataDrivenAnalysisModule module = fModule;
        assertNotNull(module);

        ITmfStateSystem ss = module.getStateSystem();
        assertNotNull(ss);

        int quark = ss.getQuarkAbsolute("modify", "0");

        final int[] expectedStarts = { 1, 3, 5, 7, 7 };
        ITmfStateValue[] expectedValues = { TmfStateValue.newValueString("UNKNOWN"), TmfStateValue.newValueString("GOOD"), TmfStateValue.newValueString("UNKNOWN"), TmfStateValue.newValueString("BAD") };
        XmlUtilsTest.verifyStateIntervals("testStateValueModify", ss, quark, expectedStarts, expectedValues);

    }

    /**
     *
     * it tests that a state change on stack, with a peek() condition. This test
     * verifies the value on the top of the stack and verifies that the peek
     * operation do not remove the value on the top of the stack.
     *
     * @throws StateSystemDisposedException
     *             Exceptions thrown during state system verification
     * @throws AttributeNotFoundException
     *             Exceptions thrown during state system verification
     */
    @Test
    public void testStateValuePeek() throws AttributeNotFoundException, StateSystemDisposedException {
        DataDrivenAnalysisModule module = fModule;
        assertNotNull(module);

        ITmfStateSystem ss = module.getStateSystem();
        assertNotNull(ss);

        int quark = ss.getQuarkAbsolute("stack");

        final int[] expectedStarts = { 1, 2, 5, 7, 7 };
        ITmfStateValue[] expectedValues = { TmfStateValue.newValueLong(1l), TmfStateValue.newValueLong(2l), TmfStateValue.newValueLong(5l), TmfStateValue.newValueLong(2l) };
        XmlUtilsTest.verifyStackStateIntervals("testStateValuePeek", ss, quark, expectedStarts, expectedValues);
    }

    /**
     * Test the mapping groups. This test verifies that, when needed, the mapped
     * value is used. In this test, the mapping group is used on the 'entry'
     * event.
     *
     * @throws StateSystemDisposedException
     *             Exceptions thrown during state system verification
     * @throws AttributeNotFoundException
     *             Exceptions thrown during state system verification
     */
    @Test
    public void testStateValueMapping() throws AttributeNotFoundException, StateSystemDisposedException {
        DataDrivenAnalysisModule module = fModule;
        assertNotNull(module);

        ITmfStateSystem ss = module.getStateSystem();
        assertNotNull(ss);

        int quark = ss.getQuarkAbsolute("mapped");

        final int[] expectedStarts = { 1, 3, 5, 7, 7 };
        ITmfStateValue[] expectedValues = { TmfStateValue.newValueString("TRUE"), TmfStateValue.newValueString("FALSE"), TmfStateValue.newValueString("TRUE"), TmfStateValue.newValueString("FALSE") };
        XmlUtilsTest.verifyStateIntervals("testMappingGroups", ss, quark, expectedStarts, expectedValues);

    }

    /**
     * Test using the HostID event field. It should give the host ID for value
     *
     * @throws StateSystemDisposedException
     *             Exceptions thrown during state system verification
     * @throws AttributeNotFoundException
     *             Exceptions thrown during state system verification
     */
    @Test
    public void testStateValueHostId() throws AttributeNotFoundException, StateSystemDisposedException {
        DataDrivenAnalysisModule module = fModule;
        assertNotNull(module);

        ITmfStateSystem ss = module.getStateSystem();
        assertNotNull(ss);

        int quark = ss.getQuarkAbsolute("hostID");

        final int[] expectedStarts = { 1, 7 };
        ITmfStateValue[] expectedValues = { TmfStateValue.newValueString("testTrace4.xml") };
        XmlUtilsTest.verifyStateIntervals("testHostId", ss, quark, expectedStarts, expectedValues);

    }

    /**
     * Test that a script state value is returning the right value.
     *
     * @throws StateSystemDisposedException
     *             Exceptions thrown during state system verification
     * @throws AttributeNotFoundException
     *             Exceptions thrown during state system verification
     */
    @Test
    public void testStateValueScript() throws AttributeNotFoundException, StateSystemDisposedException {
        DataDrivenAnalysisModule module = fModule;
        assertNotNull(module);

        ITmfStateSystem ss = module.getStateSystem();
        assertNotNull(ss);

        int quark = ss.getQuarkAbsolute("script");

        final int[] expectedStarts = { 1, 3, 5, 7, 7 };
        ITmfStateValue[] expectedValues = { TmfStateValue.newValueString("TRUE"), TmfStateValue.newValueString("FALSE"), TmfStateValue.newValueString("TRUE"), TmfStateValue.newValueString("FALSE") };
        XmlUtilsTest.verifyStateIntervals("testStateValueScript", ss, quark, expectedStarts, expectedValues);

    }
}
