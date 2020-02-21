/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.analysis.xml.core.tests;

import static org.junit.Assert.assertNotNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.TmfXmlPatternCu;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.pattern.stateprovider.XmlPatternAnalysis;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.common.TmfXmlTestFiles;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.stateprovider.XmlModuleTestBase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Utilities method for pattern analysis tests
 *
 * @author Jean-Christian Kouame
 *
 */
public class PatternAnalysisTestUtils {

    private static XmlPatternAnalysis createModule(@NonNull Element element, TmfXmlTestFiles file) {
        String id = element.getAttribute(TmfXmlStrings.ID);
        TmfXmlPatternCu patternCu = TmfXmlPatternCu.compile(element);
        assertNotNull(patternCu);
        XmlPatternAnalysis module = new XmlPatternAnalysis(id, patternCu);
        module.setName(XmlModuleTestBase.getName(element));
        return module;
    }

    /**
     * Initialise a pattern analysis module
     *
     * @param file
     *            The xml file that contains the analysis description
     * @return The created module
     */
    public static XmlPatternAnalysis initModule(TmfXmlTestFiles file) {
        Document doc = file.getXmlDocument();
        assertNotNull(doc);

        /* get State Providers modules */
        NodeList stateproviderNodes = doc.getElementsByTagName(TmfXmlStrings.PATTERN);

        Element node = (Element) stateproviderNodes.item(0);
        assertNotNull(node);

        XmlPatternAnalysis module = createModule(node, file);

        String moduleId = node.getAttribute(TmfXmlStrings.ID);
        assertNotNull(moduleId);
        module.setId(moduleId);

        return module;
    }
}
