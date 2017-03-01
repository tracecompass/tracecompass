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
import static org.junit.Assert.fail;

import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.pattern.stateprovider.XmlPatternAnalysis;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.stateprovider.XmlStateSystemModule;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.common.TmfXmlTestFiles;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfAnalysisModuleWithStateSystems;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Base unit test for XML analyses
 *
 * @author Jean-Christian Kouame
 */
public abstract class XmlProviderTestBase {

    private ITmfTrace fTrace;
    private TmfAbstractAnalysisModule fModule;

    /**
     * Setup the test fields
     */
    @Before
    public void setupTest() {
        /* Initialize the trace */
        ITmfTrace trace = CtfTmfTestTraceUtils.getTrace(getTestTrace());
        fTrace = trace;

        /* Initialize the state provider module */
        Document doc = getXmlFile().getXmlDocument();
        assertNotNull(doc);

        /* get State Providers modules */
        NodeList analysisNodes = doc.getElementsByTagName(getAnalysisNodeName());

        Element node = (Element) analysisNodes.item(0);
        switch (getAnalysisNodeName()) {
        case TmfXmlStrings.PATTERN:
            fModule = new XmlPatternAnalysis();
            ((XmlPatternAnalysis) fModule).setXmlFile(getXmlFile().getFile().toPath());
            break;
        case TmfXmlStrings.STATE_PROVIDER:
            fModule = new XmlStateSystemModule();
            ((XmlStateSystemModule) fModule).setXmlFile(getXmlFile().getFile().toPath());
            break;

        default:
            fail();
        }
        String moduleId = node.getAttribute(TmfXmlStrings.ID);
        assertNotNull(moduleId);
        fModule.setId(moduleId);

        try {
            fModule.setTrace(trace);
            fModule.schedule();
        } catch (TmfAnalysisException e) {
            fail("Cannot set trace " + e.getMessage());
        }
    }

    /**
     * Clean up
     */
    @After
    public void cleanup() {
        fModule.dispose();
        CtfTmfTestTraceUtils.dispose(getTestTrace());
    }

    /**
     * The node name of the analysis in the file
     *
     * @return The name
     */
    protected abstract @NonNull String getAnalysisNodeName();

    /**
     * Get the XML file this test use
     *
     * @return The XML file
     */
    protected abstract TmfXmlTestFiles getXmlFile();

    /**
     * Get the trace this test use
     *
     * @return The trace
     */
    protected abstract @NonNull CtfTestTrace getTestTrace();

    /**
     * Get the trace to use for the tests
     *
     * @return The instance of the the test trace
     */
    protected ITmfTrace getTrace() {
        return fTrace;
    }

    /**
     * Cleanup after the test
     */
    @After
    public void cleanupTest() {
        fTrace.dispose();
    }

    /**
     * Test the building of the state system
     */
    @Test
    public void testStateSystem() {
        assertTrue(fModule instanceof ITmfAnalysisModuleWithStateSystems);
        assertTrue(fModule.waitForCompletion(new NullProgressMonitor()));
        ITmfStateSystem ss = ((ITmfAnalysisModuleWithStateSystems) fModule).getStateSystem(fModule.getId());
        assertNotNull(ss);

        List<Integer> quarks = ss.getQuarks("*");
        assertFalse(quarks.isEmpty());
    }

    /**
     * Get the module of this analysis
     *
     * @return The module
     */
    protected TmfAbstractAnalysisModule getModule() {
        return fModule;
    }

}
