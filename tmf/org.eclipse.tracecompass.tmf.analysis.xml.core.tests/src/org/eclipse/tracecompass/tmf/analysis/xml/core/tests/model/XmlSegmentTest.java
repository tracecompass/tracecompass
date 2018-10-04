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
import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.pattern.stateprovider.XmlPatternAnalysis;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.segment.TmfXmlPatternSegment;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.common.TmfXmlTestFiles;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.module.XmlUtilsTest;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.stubs.PatternSegmentFactoryStub;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the XML pattern segment
 *
 * @author Jean-Christian Kouame
 */
public class XmlSegmentTest {

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
     * Test segment generated using a mapping group
     */
    @Test
    public void testMappingGroup() {
        XmlPatternAnalysis module = fModule;
        assertNotNull(module);

        @Nullable ISegmentStore<@NonNull ISegment> ss = module.getSegmentStore();
        assertNotNull(ss);
        assertEquals("Segment store size", 1, ss.size());
        Object segment = ss.iterator().next();
        assertTrue(segment instanceof TmfXmlPatternSegment);
        XmlUtilsTest.testPatternSegmentData(PatternSegmentFactoryStub.TEST_3, (TmfXmlPatternSegment)segment);
    }
}

