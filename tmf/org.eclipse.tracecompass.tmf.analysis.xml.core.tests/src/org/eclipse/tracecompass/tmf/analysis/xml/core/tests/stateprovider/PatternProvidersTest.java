/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.analysis.xml.core.tests.stateprovider;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.pattern.stateprovider.XmlPatternStateProvider;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.common.TmfXmlTestFiles;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.junit.Test;

/**
 * Test suite for the XML pattern analysis. This class test the state system and
 * the segment store generated
 *
 * @author Jean-Christian Kouame
 */
public class PatternProvidersTest extends XmlProviderTestBase {

    /**
     * Test an invalid instantiation
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidInput() {
        assertNotNull(new XmlPatternStateProvider(CtfTmfTestTraceUtils.getTrace(getTrace()), "Bla", Path.fromOSString(""), (@NonNull ISegment segment) -> { }));
    }

    @Override
    protected CtfTestTrace getTrace() {
        return CtfTestTrace.ARM_64_BIT_HEADER;
    }

    @Override
    protected TmfXmlTestFiles getXmlFile() {
        return TmfXmlTestFiles.VALID_PATTERN_FILE;
    }

    @Override
    protected @NonNull String getAnalysisNodeName() {
        return TmfXmlStrings.PATTERN;
    }

    /**
     * Test the generated segment store
     */
    @Test
    public void testSegmentStore() {
        assertTrue(getModule().waitForCompletion(new NullProgressMonitor()));
        ISegmentStore<@NonNull ISegment> ss = ((ISegmentStoreProvider) getModule()).getSegmentStore();
        assertNotNull(ss);
        assertFalse(ss.isEmpty());
    }

}
