/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.analysis.xml.core.tests.stateprovider;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.tmf.analysis.xml.core.module.XmlHeadInfo;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.XmlStateSystemModule;
import org.eclipse.linuxtools.tmf.analysis.xml.core.tests.common.TmfXmlTestFiles;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ctf.core.tests.shared.CtfTmfTestTrace;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Test suite for the xml state providers
 *
 * TODO: instead of using one of the test traces, we should make a custom trace
 * to make sure it covers the different possibilities of the state provider
 *
 * @author Geneviève Bastien
 */
public class StateProviderTest {

    private ITmfTrace fTrace;
    private XmlStateSystemModule fModule;

    /**
     * Setup the test fields
     */
    @Before
    public void setupTest() {
        /* Initialize the trace */
        assumeTrue(CtfTmfTestTrace.KERNEL.exists());
        fTrace = CtfTmfTestTrace.KERNEL.getTrace();

        /* Initialize the state provider module */
        Document doc = null;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(TmfXmlTestFiles.VALID_FILE.getFile());
            doc.getDocumentElement().normalize();
        } catch (ParserConfigurationException e) {
            fail("Xml document parse exception");
        } catch (SAXException e) {
            fail("Exception parsing xml file");
        } catch (IOException e) {
            fail("File io exception");
        }
        assertNotNull(doc);

        /* get State Providers modules */
        NodeList stateproviderNodes = doc.getElementsByTagName(TmfXmlStrings.STATE_PROVIDER);

        Element node = (Element) stateproviderNodes.item(0);
        fModule = new XmlStateSystemModule();
        String moduleId = node.getAttribute(TmfXmlStrings.ID);
        fModule.setId(moduleId);

        fModule.setXmlFile(new Path(TmfXmlTestFiles.VALID_FILE.getFile().getAbsolutePath()));
        NodeList head = node.getElementsByTagName(TmfXmlStrings.HEAD);
        XmlHeadInfo headInfo = null;
        if (head.getLength() == 1) {
            headInfo = new XmlHeadInfo(head.item(0));
        }
        fModule.setHeadInfo(headInfo);
        try {
            fModule.setTrace(fTrace);
            fModule.schedule();
        } catch (TmfAnalysisException e) {
            fail("Cannot set trace " + e.getMessage());
        }
    }

    /**
     * Cleanup after the test
     */
    @After
    public void cleanupTest() {
        CtfTmfTestTrace.KERNEL.dispose();
    }

    /**
     * Test the building of the state system
     */
    @Test
    public void testStateSystem() {
        assertTrue(fModule.waitForCompletion(new NullProgressMonitor()));
        ITmfStateSystem ss = fModule.getStateSystem();
        assertNotNull(ss);

        List<Integer> quarks = ss.getQuarks("*");
        assertFalse(quarks.isEmpty());
    }

}
