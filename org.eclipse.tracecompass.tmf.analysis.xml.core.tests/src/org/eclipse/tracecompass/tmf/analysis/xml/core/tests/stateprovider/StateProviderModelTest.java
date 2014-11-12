/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.core.tests.stateprovider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.XmlStateSystemModule;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.Activator;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.common.TmfXmlTestFiles;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.xml.TmfXmlTraceStub;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Test the XML model for state providers
 *
 * @author Geneviève Bastien
 */
public class StateProviderModelTest {

    private static final @NonNull String testTrace1 = "test_traces/testTrace1.xml";
    /* Factor to convert seconds to nanoseconds */
    private static final long TO_NS = 1000000000L;

    private static @NonNull ITmfTrace initializeTrace(String traceFile) {
        /* Initialize the trace */
        TmfXmlTraceStub trace = new TmfXmlTraceStub();
        try {
            trace.initTrace(null, Activator.getAbsolutePath(new Path(traceFile)).toOSString(), TmfEvent.class);
        } catch (TmfTraceException e1) {
            fail(e1.getMessage());
        }
        return trace;
    }

    private static @NonNull XmlStateSystemModule initializeModule(TmfXmlTestFiles xmlAnalysisFile) {

        /* Initialize the state provider module */
        Document doc = xmlAnalysisFile.getXmlDocument();
        assertNotNull(doc);

        /* get State Providers modules */
        NodeList stateproviderNodes = doc.getElementsByTagName(TmfXmlStrings.STATE_PROVIDER);
        assertFalse(stateproviderNodes.getLength() == 0);

        Element node = (Element) stateproviderNodes.item(0);
        XmlStateSystemModule module = new XmlStateSystemModule();
        String moduleId = node.getAttribute(TmfXmlStrings.ID);
        assertNotNull(moduleId);
        module.setId(moduleId);

        module.setXmlFile(xmlAnalysisFile.getPath());

        return module;
    }

    private static void verifyStateIntervals(String testId, @NonNull ITmfStateSystem ss, Integer quark, int[] expectedStarts, ITmfStateValue[] expectedValues) throws AttributeNotFoundException, StateSystemDisposedException {
        int expectedCount = expectedStarts.length - 1;
        List<ITmfStateInterval> intervals = StateSystemUtils.queryHistoryRange(ss, quark, expectedStarts[0] * TO_NS, expectedStarts[expectedCount] * TO_NS);
        assertEquals(testId + ": Interval count", expectedCount, intervals.size());
        for (int i = 0; i < expectedCount; i++) {
            ITmfStateInterval interval = intervals.get(i);
            assertEquals(testId + ": Start time of interval " + i, expectedStarts[i] * TO_NS, interval.getStartTime());
            long actualEnd = (i == expectedCount - 1) ? (expectedStarts[i + 1] * TO_NS) : (expectedStarts[i + 1] * TO_NS) - 1;
            assertEquals(testId + ": End time of interval " + i, actualEnd, interval.getEndTime());
            assertEquals(testId + ": Expected value of interval " + i, expectedValues[i], interval.getStateValue());
        }
    }

    /**
     * Test an increment of one, for an event name attribute
     */
    @Test
    public void testEventName() {
        ITmfTrace trace = initializeTrace(testTrace1);
        XmlStateSystemModule module = initializeModule(TmfXmlTestFiles.ATTRIBUTE_FILE);
        try {

            module.setTrace(trace);

            module.schedule();
            module.waitForCompletion();

            ITmfStateSystem ss = module.getStateSystem();
            assertNotNull(ss);

            List<Integer> quarks = ss.getQuarks("*");
            assertEquals(2, quarks.size());

            for (Integer quark : quarks) {
                String name = ss.getAttributeName(quark);
                switch (name) {
                case "test":
                {
                    final int[] expectedStarts = { 1, 5, 7 };
                    ITmfStateValue[] expectedValues = { TmfStateValue.newValueInt(1), TmfStateValue.newValueInt(2) };
                    verifyStateIntervals("test", ss, quark, expectedStarts, expectedValues);
                }
                    break;
                case "test1":
                {
                    final int[] expectedStarts = { 1, 3, 7, 7 };
                    ITmfStateValue[] expectedValues = { TmfStateValue.nullValue(), TmfStateValue.newValueInt(1), TmfStateValue.newValueInt(2) };
                    verifyStateIntervals("test1", ss, quark, expectedStarts, expectedValues);
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

}
