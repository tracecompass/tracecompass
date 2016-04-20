/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.stateprovider.XmlStateSystemModule;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.common.TmfXmlTestFiles;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Test suite for the XmlStateSystemModule Test. It tests the reading of the
 * file, the header and the module's proper functioning as a module, but not the
 * state system building, which is covered by another test suite.
 *
 * @author Geneviève Bastien
 */
public class StateProviderModuleTest {

    private static String ANALYSIS_ID = "kernel.linux.sp";
    private static String ANALYSIS_NAME = "Xml kernel State System";

    private XmlStateSystemModule fModule;

    /**
     * Test the module construction
     */
    @Test
    public void testModuleConstruction() {

        Document doc = TmfXmlTestFiles.VALID_FILE.getXmlDocument();
        assertNotNull(doc);

        /* get State Providers modules */
        NodeList stateproviderNodes = doc.getElementsByTagName(TmfXmlStrings.STATE_PROVIDER);
        assertTrue(stateproviderNodes.getLength() > 0);

        Element node = (Element) stateproviderNodes.item(0);
        fModule = new XmlStateSystemModule();
        String moduleId = node.getAttribute(TmfXmlStrings.ID);
        assertNotNull(moduleId);
        fModule.setId(moduleId);
        assertEquals(ANALYSIS_ID, fModule.getId());

        fModule.setXmlFile(TmfXmlTestFiles.VALID_FILE.getPath());

        assertEquals(ANALYSIS_NAME, fModule.getName());
    }

    /**
     * Test the module executes correctly
     */
    @Test
    public void testModuleExecution() {
        Document doc = TmfXmlTestFiles.VALID_FILE.getXmlDocument();
        assertNotNull(doc);

        /* get State Providers modules */
        NodeList stateproviderNodes = doc.getElementsByTagName(TmfXmlStrings.STATE_PROVIDER);

        Element node = (Element) stateproviderNodes.item(0);
        fModule = new XmlStateSystemModule();
        String moduleId = node.getAttribute(TmfXmlStrings.ID);
        assertNotNull(moduleId);
        fModule.setId(moduleId);

        fModule.setXmlFile(TmfXmlTestFiles.VALID_FILE.getPath());

        CtfTmfTrace trace = CtfTmfTestTraceUtils.getTrace(CtfTestTrace.KERNEL);
        try {
            fModule.setTrace(trace);
            fModule.schedule();
            assertTrue(fModule.waitForCompletion(new NullProgressMonitor()));

        } catch (TmfAnalysisException e) {
            fail("Cannot set trace " + e.getMessage());
        } finally {
            trace.dispose();
        }

    }
}
