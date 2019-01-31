/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.analysis.xml.core.tests.model;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.pattern.stateprovider.XmlPatternAnalysis;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.segment.TmfXmlPatternSegment;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.PatternAnalysisTestUtils;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.common.TmfXmlTestFiles;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.module.XmlUtilsTest;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the pattern analysis fsm
 *
 * @author Jean-Christian Kouame
 */
public class FsmTest {
    private static final int END_TIME = 7;
    private static final @NonNull String TEST_TRACE = "test_traces/testTrace4.xml";
    private static final String TEST_SEGMENT_NEW = "NEW";
    private static final TmfXmlTestFiles TEST_FILE_1 = TmfXmlTestFiles.INITIAL_STATE_ELEMENT_TEST_FILE_1;
    private static final TmfXmlTestFiles TEST_FILE_2 = TmfXmlTestFiles.INITIAL_STATE_ELEMENT_TEST_FILE_2;
    private static XmlPatternAnalysis fModule;
    private static XmlPatternAnalysis fModule2;
    private static ITmfTrace fTrace;

    /**
     * End the test suite
     */
    @AfterClass
    public static void tearDown() {
        fModule.dispose();
        fModule2.dispose();
        fTrace.dispose();
    }

    /**
     * Before the test suite
     */
    @BeforeClass
    public static void before() {
        ITmfTrace trace = XmlUtilsTest.initializeTrace(TEST_TRACE);
        //Create first module
        fModule = PatternAnalysisTestUtils.initModule(TEST_FILE_1);
        try {
            fModule.setTrace(trace);
            fModule.schedule();
            assertTrue(fModule.waitForCompletion(new NullProgressMonitor()));
        } catch (TmfAnalysisException e) {
            fail("Cannot execute analyses " + e.getMessage());
        }

        //Create second module
        fModule2 = PatternAnalysisTestUtils.initModule(TEST_FILE_2);
        try {
            fModule2.setTrace(trace);
            fModule2.schedule();
            assertTrue(fModule2.waitForCompletion(new NullProgressMonitor()));
        } catch (TmfAnalysisException e) {
            fail("Cannot execute analyses " + e.getMessage());
        }
        fTrace = trace;
    }

    /**
     * Compare the execution of two state machines that do the same job, one
     * using the initial element, the second one using the initialState element.
     * The result should be the same for both state machines
     */
    @Test
    public void testInitialStateDeclaration() {
        ITmfStateSystem stateSystem = fModule.getStateSystem(fModule.getId());
        assertNotNull("state system exist", stateSystem);
        try {
            int quark = stateSystem.getQuarkAbsolute("fsm1");
            @NonNull ITmfStateInterval interval = stateSystem.querySingleState(END_TIME, quark);
            long count1 = interval.getStateValue().unboxLong();

            quark = stateSystem.getQuarkAbsolute("fsm2");
            interval = stateSystem.querySingleState(END_TIME, quark);
            long count2 = interval.getStateValue().unboxLong();
            assertTrue("Test the count value", count1 == count2);
        } catch (AttributeNotFoundException | StateSystemDisposedException e) {
            fail("Failed to query the state system");
        }
    }

    /**
     * Compare the execution of two state machines doing the same job, the tid
     * condition is ignored with the initial element and used with the
     * initialState element. The result should be different.
     */
    @Test
    public void testInitialStateWithCondition() {
        ITmfStateSystem stateSystem = fModule.getStateSystem(fModule.getId());
        assertNotNull("state system exist", stateSystem);
        try {
            int quark = stateSystem.getQuarkAbsolute("fsm1");
            @NonNull ITmfStateInterval interval = stateSystem.querySingleState(END_TIME, quark);
            long count1 = interval.getStateValue().unboxLong();

            quark = stateSystem.getQuarkAbsolute("fsm3");
            interval = stateSystem.querySingleState(END_TIME, quark);
            long count3 = interval.getStateValue().unboxLong();
            assertTrue("Test the count value", count1 > count3);
        } catch (AttributeNotFoundException | StateSystemDisposedException e) {
            fail("Failed to query the state system");
        }
    }

    /**
     * Execute one pattern, with the two types of initial state initialization,
     * then test that the new behavior is prioritized and that preconditions are
     * ignored with initialState element
     */
    @Test
    public void testTwoInitialStates() {
        //Test segment store
        @Nullable ISegmentStore<@NonNull ISegment> ss = fModule2.getSegmentStore();
        assertNotNull("segment store exist", ss);
        assertTrue("Segment store not empty", ss.size() == 1);
        Object item = ss.iterator().next();
        assertTrue(item instanceof TmfXmlPatternSegment);
        assertTrue(((TmfXmlPatternSegment) item).getName().equals(TEST_SEGMENT_NEW));

        //Test state system
        ITmfStateSystem stateSystem = fModule2.getStateSystem(fModule2.getId());
        assertNotNull("state system exist", stateSystem);
        int quark;
        try {
            quark = stateSystem.getQuarkAbsolute("count_new");
            @NonNull ITmfStateInterval interval = stateSystem.querySingleState(END_TIME, quark);
            int count = interval.getStateValue().unboxInt();
            assertTrue("Test the count value", count > 0);
        } catch (AttributeNotFoundException | StateSystemDisposedException e) {
            fail("Failed to query the state system");
        }

        try {
            quark = stateSystem.getQuarkAbsolute("precond");
        } catch (AttributeNotFoundException e) {
            return;
        }
        fail();
    }
}
