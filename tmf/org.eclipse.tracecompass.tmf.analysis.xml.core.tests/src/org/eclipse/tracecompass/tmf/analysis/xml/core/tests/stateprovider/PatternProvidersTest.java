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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.common.TmfXmlTestFiles;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfAnalysisModuleWithStateSystems;
import org.junit.Test;

/**
 * Test suite for the XML pattern analysis. This class test the state system and
 * the segment store generated
 *
 * @author Jean-Christian Kouame
 */
public class PatternProvidersTest extends XmlProviderTestBase {

    @Override
    protected CtfTestTrace getTestTrace() {
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
        TmfAbstractAnalysisModule module = getModule();
        if (module instanceof ITmfAnalysisModuleWithStateSystems) {
            // In this test, the module will be XmlPatternAnalysis which is an instance of ITmfAnalysisModuleWithStateSystems
            assertTrue(((ITmfAnalysisModuleWithStateSystems) module).waitForInitialization());
        }
        assertTrue(module.waitForCompletion());
        ISegmentStore<@NonNull ISegment> ss = ((ISegmentStoreProvider) module).getSegmentStore();
        assertNotNull(ss);
        assertFalse(ss.isEmpty());
    }

}
