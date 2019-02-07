/*******************************************************************************
 * Copyright (c) 2019 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.core.tests.common;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.TmfXmlStateProviderCu;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValue;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValueConstant;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.DataDrivenAnalysisModule;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.pattern.stateprovider.XmlPatternAnalysis;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlUtils;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Utility class for unit tests
 *
 * @author Geneviève Bastien
 */
public final class TmfXmlTestUtils {

    /**
     * A null state value
     */
    public static final @NonNull DataDrivenValue NULL_VALUE = new DataDrivenValueConstant(null, ITmfStateValue.Type.NULL, null);

    private TmfXmlTestUtils() {
        // Shouldn't be instantiated
    }

    /**
     * Get an XML element from an XML string
     *
     * @param elementName
     *            The name of the element to get
     * @param xmlString
     *            The XML String to parse
     * @return The XML element corresponding for the name
     * @throws SAXException
     *             Exception thrown by parser
     * @throws IOException
     *             Exception thrown by parser
     * @throws ParserConfigurationException
     *             Exception thrown by parser
     */
    public static Element getXmlElement(String elementName, String xmlString) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputSource src = new InputSource();
        src.setCharacterStream(new StringReader(xmlString));

        Document doc = builder.parse(src);
        NodeList elements = doc.getElementsByTagName(elementName);
        if (elements.getLength() == 0) {
            throw new NullPointerException("No element named " + elementName + " in " + xmlString);
        }
        return (Element) elements.item(0);
    }

    /**
     * Get the XML analysis module in the file. It will return either a pattern
     * or state provider, if there is one in the file
     *
     * @param xmlFilePath
     *            The absolute file path to the XML file
     * @param analysisId
     *            The ID of the analysis to get
     * @return The analysis module
     */
    public static TmfAbstractAnalysisModule getModuleInFile(String xmlFilePath, @NonNull String analysisId) {

        // Look for a pattern element
        Element element = TmfXmlUtils.getElementInFile(xmlFilePath, TmfXmlStrings.PATTERN, analysisId);

        if (element != null) {
            XmlPatternAnalysis module = new XmlPatternAnalysis();
            module.setXmlFile(Paths.get(xmlFilePath));
            module.setId(analysisId);
            module.setName(analysisId);
            return module;
        }

        // Look for a state provider
        element = TmfXmlUtils.getElementInFile(xmlFilePath, TmfXmlStrings.STATE_PROVIDER, analysisId);

        if (element != null) {
            TmfXmlStateProviderCu compile = TmfXmlStateProviderCu.compile(Paths.get(xmlFilePath), analysisId);
            assertNotNull(compile);
            DataDrivenAnalysisModule module = new DataDrivenAnalysisModule(analysisId, compile);
            module.setName(analysisId);
            return module;
        }

        throw new NullPointerException("No module named " + analysisId + " in file " + xmlFilePath);

    }

}
