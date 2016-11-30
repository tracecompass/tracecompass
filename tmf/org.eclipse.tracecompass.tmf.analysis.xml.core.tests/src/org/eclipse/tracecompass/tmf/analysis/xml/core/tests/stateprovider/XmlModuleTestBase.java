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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.pattern.stateprovider.XmlPatternAnalysis;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.stateprovider.XmlStateSystemModule;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.common.TmfXmlTestFiles;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Base test for XML analysis module
 *
 * @author Jean-Christian Kouame
 */
public abstract class XmlModuleTestBase {

    private TmfAbstractAnalysisModule fModule;

    /**
     * Test the module construction
     */
    @Test
    public void testModuleConstruction() {

        Document doc = getXmlFile().getXmlDocument();
        assertNotNull(doc);

        /* get analysis modules */
        NodeList analysisNodes = doc.getElementsByTagName(getAnalysisNodeName());
        assertTrue(analysisNodes.getLength() > 0);

        final Element node = (Element) analysisNodes.item(0);
        assertNotNull(node);

        createModule(node);

        String moduleId = node.getAttribute(TmfXmlStrings.ID);
        assertNotNull(moduleId);
        fModule.setId(moduleId);
        assertEquals(getAnalysisId(), fModule.getId());

        assertEquals(getAnalysisName(), fModule.getName());

        fModule.dispose();
    }

    private void createModule(@NonNull Element element) {
        switch (getAnalysisNodeName()) {
        case TmfXmlStrings.PATTERN:
            fModule = new XmlPatternAnalysis();
            ((XmlPatternAnalysis) fModule).setXmlFile(new Path(getXmlFile().getFile().getAbsolutePath()));
            fModule.setName(getName(element));
            break;
        case TmfXmlStrings.STATE_PROVIDER:
            fModule = new XmlStateSystemModule();
            ((XmlStateSystemModule) fModule).setXmlFile(new Path(getXmlFile().getFile().getAbsolutePath()));
            break;

        default:
            fail();
        }
    }

    /**
     * Get the analysis XML node name
     *
     * @return The name
     */
    protected abstract @NonNull String getAnalysisNodeName();

    /**
     * Get the XML file this test use
     *
     * @return The file
     */
    protected abstract TmfXmlTestFiles getXmlFile();

    /**
     * Get the analysis name
     *
     * @return The name
     */
    protected abstract String getAnalysisName();

    /**
     * Get the id of the analysis
     *
     * @return The id
     */
    protected abstract String getAnalysisId();

    /**
     * Get the trace this test use
     *
     * @return The trace
     */
    protected abstract @NonNull CtfTestTrace getTrace();

    /**
     * Get the name of the analysis
     *
     * @param element
     *            The analysis element
     * @return The name
     */
    public static @NonNull String getName(Element element) {
        String name = null;
        List<Element> head = XmlUtils.getChildElements(element, TmfXmlStrings.HEAD);
        if (head.size() == 1) {
            List<Element> labels = XmlUtils.getChildElements(head.get(0), TmfXmlStrings.LABEL);
            if (!labels.isEmpty()) {
                name = labels.get(0).getAttribute(TmfXmlStrings.VALUE);
            }
        }
        assertNotNull("analysis name", name);
        return name;
    }

    /**
     * Test the module executes correctly
     */
    @Test
    public void testModuleExecution() {
        Document doc = getXmlFile().getXmlDocument();
        assertNotNull(doc);

        /* get State Providers modules */
        NodeList stateproviderNodes = doc.getElementsByTagName(getAnalysisNodeName());

        Element node = (Element) stateproviderNodes.item(0);
        assertNotNull(node);

        createModule(node);

        String moduleId = node.getAttribute(TmfXmlStrings.ID);
        assertNotNull(moduleId);
        fModule.setId(moduleId);

        CtfTmfTrace trace = CtfTmfTestTraceUtils.getTrace(getTrace());
        try {
            fModule.setTrace(trace);
            fModule.schedule();
            assertTrue(fModule.waitForCompletion(new NullProgressMonitor()));

        } catch (TmfAnalysisException e) {
            fail("Cannot set trace " + e.getMessage());
        } finally {
            trace.dispose();
        }
        fModule.dispose();
        CtfTmfTestTraceUtils.dispose(getTrace());
    }
}
