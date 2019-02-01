/*******************************************************************************
 * Copyright (c) 2019 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.core.tests.fsm.compile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.AnalysisCompilationData;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.TmfXmlLocationCu;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.TmfXmlStateValueCu;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenStateSystemPath;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.IBaseQuarkProvider;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValue;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValueConstant;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValueEventField;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValueEventName;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValuePool;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValueQuery;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValueSelf;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.common.TmfXmlTestUtils;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.common.collect.ImmutableList;

/**
 * Test the compilation of state attributes
 *
 * @author Geneviève Bastien
 */
public class TmfXmlStateAttributeAndLocationCuTest {

    private static final @NonNull AnalysisCompilationData ANALYSIS_DATA = new AnalysisCompilationData();

    /**
     * @return The arrays of parameters
     */
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
                { "Null state attribute", "<stateAttribute type=\"null\" />", true },
                { "Constant valid", "<stateAttribute type=\"constant\" value=\"42\" />", true },
                { "Constant invalid", "<stateAttribute type=\"constant\" />", false },
                { "Event field valid", "<stateAttribute type=\"eventField\" value=\"myfield\" />", true },
                { "Event field invalid", "<stateAttribute type=\"eventField\" />", false },
                { "Event name valid 1", "<stateAttribute type=\"eventName\" />", true },
                { "Event name valid 2", "<stateAttribute type=\"eventName\" value=\"ignored\" />", true },
                { "Query", "<stateAttribute type=\"query\" ><stateAttribute type=\"constant\" value=\"queryPath\"/></stateAttribute>", true },
                { "Query no children", "<stateAttribute type=\"query\" />", false },
                { "Query with invalid children", "<stateAttribute type=\"query\" ><stateAttribute type=\"constant\" /></stateAttribute>", false },
                { "Self", "<stateAttribute type=\"self\" />", true },
                { "Pool", "<stateAttribute type=\"pool\" />", true },
        });
    }

    /**
     * Test the compilation of a valid state attribute strings, except locations
     *
     * @throws SAXException
     *             Exception thrown by parser
     * @throws IOException
     *             Exception thrown by parser
     * @throws ParserConfigurationException
     *             Exception thrown by parser
     */
    @Test
    public void testValidStateAttributeCompilation() throws SAXException, IOException, ParserConfigurationException {
        String[] validStrings = { "<stateAttribute type=\"null\" />",
                "<stateAttribute type=\"constant\" value=\"42\" />",
                "<stateAttribute type=\"eventField\" value=\"myfield\" />",
                "<stateAttribute type=\"eventName\" />",
                "<stateAttribute type=\"eventName\" value=\"ignored\" />",
                "<stateAttribute type=\"query\" ><stateAttribute type=\"constant\" value=\"queryPath\"/></stateAttribute>",
                "<stateAttribute type=\"self\" />",
                "<stateAttribute type=\"pool\" />" };
        DataDrivenValue[] generated = { TmfXmlTestUtils.NULL_VALUE,
                new DataDrivenValueConstant(null, ITmfStateValue.Type.NULL, "42"),
                new DataDrivenValueEventField(null, ITmfStateValue.Type.NULL, "myfield"),
                new DataDrivenValueEventName(null),
                new DataDrivenValueEventName(null),
                new DataDrivenValueQuery(null, ITmfStateValue.Type.NULL,
                        new DataDrivenStateSystemPath(ImmutableList.of(new DataDrivenValueConstant(null, ITmfStateValue.Type.NULL, "queryPath")), IBaseQuarkProvider.IDENTITY_BASE_QUARK)),
                new DataDrivenValueSelf(ITmfStateValue.Type.NULL),
                DataDrivenValuePool.getInstance() };
        for (int i = 0; i < validStrings.length; i++) {
            String validString = validStrings[i];
            DataDrivenValue runtimeObj = generated[i];
            Element xmlElement = TmfXmlTestUtils.getXmlElement(TmfXmlStrings.STATE_ATTRIBUTE, validString);
            assertNotNull(xmlElement);
            List<@NonNull TmfXmlStateValueCu> compileAttribute = TmfXmlStateValueCu.compileAttribute(ANALYSIS_DATA, xmlElement);
            assertNotNull(validString, compileAttribute);
            assertEquals("Number of attributes", 1, compileAttribute.size());
            TmfXmlStateValueCu value = compileAttribute.get(0);
            assertEquals("Expected attribute", runtimeObj, value.generate());
        }
    }

    /**
     * Test the compilation of a invalid state attribute strings, except
     * locations
     *
     * @throws SAXException
     *             Exception thrown by parser
     * @throws IOException
     *             Exception thrown by parser
     * @throws ParserConfigurationException
     *             Exception thrown by parser
     */
    @Test
    public void testInvalidStateAttributeCompilation() throws SAXException, IOException, ParserConfigurationException {
        String[] invalidStrings = { "<stateAttribute type=\"constant\" />",
                "<stateAttribute type=\"eventField\" />",
                "<stateAttribute type=\"query\" />",
                "<stateAttribute type=\"query\" ><stateAttribute type=\"constant\" /></stateAttribute>",
                "<stateAttribute type=\"location\" value=\"undefined\" />"
        };

        for (String invalidString : invalidStrings) {
            Element xmlElement = TmfXmlTestUtils.getXmlElement(TmfXmlStrings.STATE_ATTRIBUTE, invalidString);
            assertNotNull(xmlElement);
            assertNull(invalidString, TmfXmlStateValueCu.compileAttribute(ANALYSIS_DATA, xmlElement));
        }
    }

    /**
     * Test the compilation of a valid state location strings, and states
     * attributes that use it
     *
     * @throws SAXException
     *             Exception thrown by parser
     * @throws IOException
     *             Exception thrown by parser
     * @throws ParserConfigurationException
     *             Exception thrown by parser
     */
    @Test
    public void testValidLocationCompilation() throws SAXException, IOException, ParserConfigurationException {
        String locName = "loc";
        String location = "<location id=\"" + locName + "\">"
                + "<stateAttribute type=\"constant\" value=\"abc\" />"
                + "<stateAttribute type=\"eventField\" value=\"myField\" />"
                + "</location>";

        AnalysisCompilationData data = new AnalysisCompilationData();

        Element xmlElement = TmfXmlTestUtils.getXmlElement(TmfXmlStrings.LOCATION, location);
        assertNotNull(xmlElement);
        TmfXmlLocationCu locationCu = TmfXmlLocationCu.compile(data, xmlElement);
        assertNotNull("location", locationCu);

        // Add the location to the compilation data
        data.addLocation(locName, locationCu);

        // Compile a location state attribute
        String attributeXml = "<stateAttribute type=\"location\" value=\"" + locName + "\" />";
        xmlElement = TmfXmlTestUtils.getXmlElement(TmfXmlStrings.STATE_ATTRIBUTE, attributeXml);
        assertNotNull(xmlElement);
        List<@NonNull TmfXmlStateValueCu> attribute = TmfXmlStateValueCu.compileAttribute(data, xmlElement);
        assertNotNull("Location attribute compilation", attribute);
        assertEquals("Attribute count", 2, attribute.size());

        List<DataDrivenValue> expected = ImmutableList.of(new DataDrivenValueConstant(null, ITmfStateValue.Type.NULL, "abc"),
                new DataDrivenValueEventField(null, ITmfStateValue.Type.NULL, "myField"));

        List<DataDrivenValue> actual = attribute.stream().map(a -> a.generate()).collect(Collectors.toList());
        assertEquals("Location generated", expected, actual);

    }

}
