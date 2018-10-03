/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.core.tests.stateprovider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.pattern.stateprovider.XmlPatternAnalysis;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.PatternAnalysisTestUtils;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.common.TmfXmlTestFiles;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.module.XmlUtilsTest;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.junit.AfterClass;
import org.junit.Test;

/**
 * Test Doubles in xml state system
 *
 * @author Matthew Khouzam
 *
 */
public class TmfXmlSegmentTimestampDescriptionTest {

    private static final @NonNull String testTrace2 = "test_traces/testTrace2.xml";
    private static final long nbEvents = 4;
    private static ITmfTrace fTrace;
    private static XmlPatternAnalysis fModule;

    /**
     * Test the segment timestamp description
     *
     * @throws TmfAnalysisException
     *             if it happens, we fail
     *
     */
    @Test
    public void testTimestampDescription() throws TmfAnalysisException {
        fTrace = XmlUtilsTest.initializeTrace(testTrace2);
        assertNotNull(fTrace);

        fModule = PatternAnalysisTestUtils.initModule(TmfXmlTestFiles.VALID_SEGMENT_TIMESTAMP_DESCRIPTION);
        fModule.setTrace(Objects.requireNonNull(fTrace));

        fModule.schedule();
        fModule.waitForCompletion();

        ISegmentStore<@NonNull ISegment> ss = fModule.getSegmentStore();
        assertNotNull(ss);

        assertEquals("The number of segment should be equal to the number of events", nbEvents, ss.size());

        Iterator<@NonNull ISegment> iterator = ss.iterator();
        ISegment firstSegment = iterator.next();
        assertEquals("wrong segment duration", 9, firstSegment.getLength());
    }

    /**
     * After class method
     */
    @AfterClass
    public static void tearDown() {
        if (fTrace != null) {
        fTrace.dispose();
        }

        if (fModule != null) {
        fModule.dispose();
        }
    }
}
