/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Jean-Christian Kouame - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.analysis.xml.core.tests.stateprovider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.DataDrivenAnalysisModule;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.pattern.stateprovider.XmlPatternAnalysis;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.common.TmfXmlTestFiles;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.module.XmlUtilsTest;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.junit.Test;

/**
 * Test suite for the XML conditions
 *
 */
public class TmfXmlConditionTest {

    private static final @NonNull String testTrace2 = "test_traces/testTrace2.xml";
    private static final @NonNull String testTrace4 = "test_traces/testTrace4.xml";

    /**
     * Test basic conditions on a state provider analysis
     */
    @Test
    public void testConditionsValidation() {
        ITmfTrace trace = XmlUtilsTest.initializeTrace(testTrace2);
        DataDrivenAnalysisModule module = XmlUtilsTest.initializeModule(TmfXmlTestFiles.CONDITION_FILE);
        try {
            module.setTrace(trace);

            module.schedule();
            module.waitForCompletion();

            ITmfStateSystem ss = module.getStateSystem();
            assertNotNull(ss);

            List<Integer> quarks = ss.getQuarks("*");
            assertEquals(5, quarks.size());

            for (Integer quark : quarks) {
                String name = ss.getAttributeName(quark);
                switch (name) {
                case "test": {
                    final int[] expectedStarts = { 1, 5, 7 };
                    ITmfStateValue[] expectedValues = { TmfStateValue.newValueLong(1), TmfStateValue.newValueLong(0) };
                    XmlUtilsTest.verifyStateIntervals("test", ss, quark, expectedStarts, expectedValues);
                }
                    break;
                case "test1": {
                    final int[] expectedStarts = { 1, 3, 7, 7 };
                    ITmfStateValue[] expectedValues = { TmfStateValue.nullValue(), TmfStateValue.newValueLong(0), TmfStateValue.newValueLong(1) };
                    XmlUtilsTest.verifyStateIntervals("test1", ss, quark, expectedStarts, expectedValues);
                }
                    break;
                case "checkpoint": {
                    final int[] expectedStarts = { 1, 5, 7, 7 };
                    ITmfStateValue[] expectedValues = { TmfStateValue.newValueLong(0), TmfStateValue.newValueLong(1), TmfStateValue.newValueLong(0) };
                    XmlUtilsTest.verifyStateIntervals("checkpoint", ss, quark, expectedStarts, expectedValues);
                }
                    break;
                case "and_three_operands": {
                    final int[] expectedStarts = { 1, 5, 7, 7 };
                    ITmfStateValue[] expectedValues = { TmfStateValue.newValueLong(1), TmfStateValue.newValueLong(0), TmfStateValue.newValueLong(1) };
                    XmlUtilsTest.verifyStateIntervals("and_three_operands", ss, quark, expectedStarts, expectedValues);
                }
                    break;
                case "not_operand": {
                    final int[] expectedStarts = { 1, 5, 7, 7 };
                    ITmfStateValue[] expectedValues = { TmfStateValue.newValueLong(0), TmfStateValue.newValueLong(1), TmfStateValue.newValueLong(0) };
                    XmlUtilsTest.verifyStateIntervals("not_operand", ss, quark, expectedStarts, expectedValues);
                }
                    break;
                default:
                    fail("Wrong attribute name " + name);
                    break;
                }
            }
        } catch (TmfAnalysisException | AttributeNotFoundException | StateSystemDisposedException e) {
            fail(e.getMessage());
        } finally {
            module.dispose();
            trace.dispose();
        }
    }

    /**
     * Test time range and elapsed validations
     */
    @Test
    public void testConditionsPattern() {
        ITmfTrace trace = XmlUtilsTest.initializeTrace(testTrace4);
        XmlPatternAnalysis module = XmlUtilsTest.initializePatternModule(TmfXmlTestFiles.VALID_PATTERN_SIMPLE_FILE);
        try {
            module.setTrace(trace);

            module.schedule();
            module.waitForCompletion();

            ISegmentStore<@NonNull ISegment> segmentStore = module.getSegmentStore();
            assertNotNull(segmentStore);

            assertEquals(1, segmentStore.size());
            Iterator<@NonNull ISegment> elements = segmentStore.getIntersectingElements(6).iterator();
            assertTrue(elements.hasNext());
            ISegment next = elements.next();
            assertEquals(5, next.getStart());
            assertEquals(2, next.getLength());

        } catch (TmfAnalysisException e) {
            fail(e.getMessage());
        } finally {
            module.dispose();
            trace.dispose();
        }
    }
}
